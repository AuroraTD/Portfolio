/*************************************************************************************************************
 * FILE:            TFIDF.c
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Calculate TFIDF for words in many documents using MPI.
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw6/
 *
 * TO RUN:          As described in link above.
 *************************************************************************************************************/

// INCLUDE
#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<dirent.h>
#include<math.h>
#include<mpi.h>

// DEFINE
#define MAX_WORDS_IN_CORPUS             32
#define MAX_FILEPATH_LENGTH             16
#define MAX_LENGTH_WORD                 16
#define MAX_DOCUMENT_NAME_LENGTH        14
#define MAX_LENGTH_TFIDF_STRING         64
#define N_RANK_MASTER                   0
#define N_TAG_UNIQUE_WORDS_WORD         1
#define N_TAG_UNIQUE_WORDS_NUM_DOCS     2
#define N_TAG_TFIDF_CONTENT             3

// Hack to get around a problem I have in my IDE setup
#ifndef NULL
    #define NULL   ((void *) 0)
#endif

// Declare non-MPI datatypes
typedef char tfidf_result_string[MAX_LENGTH_TFIDF_STRING];
typedef char word_string[MAX_LENGTH_WORD];
typedef struct o {
	char word[32];
	char document[8];
	int wordCountInDoc;
	int numWordsInDoc;
	int numDocsWithWord;
} obj;
typedef struct w {
	char word[32];
	int numDocsWithWord;
	int currentDocument;
} u_w;

/*************************************************************************************************************
 * FUNCTION:        myCompare
 *
 * DESCRIPTION:     Wrapper for strcmp
 *
 * ARGUMENTS:       See file comment block
 *************************************************************************************************************/
static int myCompare (const void * a, const void * b) {
    return strcmp (a, b);
}

/*************************************************************************************************************
 * FUNCTION:        main
 *
 * DESCRIPTION:     Calculate TFIDF for words in many documents using MPI
 *
 * ARGUMENTS:       None
 *************************************************************************************************************/
int main (int argc , char *argv[]) {

    // Declare variables

    MPI_Datatype        MPI_TFIDF_STRING;
    MPI_Datatype        MPI_WORD;

    obj                 tfidf_structs[MAX_WORDS_IN_CORPUS];
    u_w                 unique_word_structs[MAX_WORDS_IN_CORPUS];
    tfidf_result_string asResultsTFIDF[MAX_WORDS_IN_CORPUS];
    word_string         asUniqueWordsSend[MAX_WORDS_IN_CORPUS];
    word_string         asUniqueWordsRecv[MAX_WORDS_IN_CORPUS];
    int                 anNumDocsWithWordSend[MAX_WORDS_IN_CORPUS];
    int                 anNumDocsWithWordRecv[MAX_WORDS_IN_CORPUS];
    char                filename[MAX_FILEPATH_LENGTH];
    char                word[MAX_LENGTH_WORD];
    char                document[MAX_DOCUMENT_NAME_LENGTH];

    int                 nNumWorkers;
    int                 nNumTasks;
    int                 nMyRank;
    int                 nNumDocs;
    int                 nResponsbilityFirstDoc;
    int                 nResponsibilityNumDocs;
    int                 nResponsibilityDocsAssigned;
    int                 numWordsInDoc;
    int                 bArrayContainsWord;
    int                 TFIDF_index;
    int                 unique_words_index;
    int                 nReceivedCount;
    int                 i;
    int                 j;
    int                 k;

    int*                anResponsibilitiesFirstDoc;
    int*                anResponsibilitiesNumDocs;
    DIR*                files;
    struct dirent*      file;
    MPI_Request*        aoSendRequestsWords;
    MPI_Request*        aoSendRequestsCounts;
    MPI_Status          oReceiveStatusWords;
    MPI_Status          oReceiveStatusNumDocs;
    MPI_Status          oReceiveStatusTFIDF;

    // Initialize MPI
    MPI_Init(&argc ,&argv);
    MPI_Comm_size(MPI_COMM_WORLD, &nNumTasks);
    MPI_Comm_rank(MPI_COMM_WORLD, &nMyRank);

    // Declare MPI datatypes
    MPI_Type_contiguous(MAX_LENGTH_TFIDF_STRING, MPI_CHAR, &MPI_TFIDF_STRING);
    MPI_Type_commit(&MPI_TFIDF_STRING);
    MPI_Type_contiguous(MAX_LENGTH_WORD, MPI_CHAR, &MPI_WORD);
    MPI_Type_commit(&MPI_WORD);

    // Initialize variables that all ranks use
    nNumWorkers = nNumTasks - 1;
    TFIDF_index = 0;

    // Initialize master variables
    if (nMyRank == N_RANK_MASTER) {

        nNumDocs =                      0;
        anResponsibilitiesFirstDoc =    malloc(sizeof(int) * nNumWorkers);
        anResponsibilitiesNumDocs =     malloc(sizeof(int) * nNumWorkers);

    }

    // Initialize Worker variables
    if (nMyRank != N_RANK_MASTER) {

        unique_words_index =    0;
        aoSendRequestsWords =   malloc(sizeof(MPI_Request) * nNumWorkers);
        aoSendRequestsCounts =  malloc(sizeof(MPI_Request) * nNumWorkers);

    }

	// If Master, figure out how to distribute work to Workers
	if (nMyRank == N_RANK_MASTER) {

	    // Count documents
        if ((files = opendir("input")) == NULL) {
            printf("Directory failed to open\n");
            exit(1);
        }
        while ((file = readdir(files))!= NULL) {
            // On linux/Unix we don't want current and parent directories
            if (!strcmp(file->d_name, "."))  continue;
            if (!strcmp(file->d_name, "..")) continue;
            nNumDocs++;
        }

	    // Determine which documents each rank is responsible for
        nResponsibilityDocsAssigned = 0;
        for (i = 0; i < nNumTasks; i++) {
            if (i == N_RANK_MASTER) {
                anResponsibilitiesNumDocs[i] =  0;
                anResponsibilitiesFirstDoc[i] = 0;
            }
            else {
                nResponsibilityNumDocs =        (nNumDocs / nNumWorkers) + (i <= (nNumDocs % nNumWorkers) ? 1 : 0);
                anResponsibilitiesNumDocs[i] =  nResponsibilityNumDocs;
                anResponsibilitiesFirstDoc[i] = nResponsibilityDocsAssigned + 1;
                nResponsibilityDocsAssigned +=  nResponsibilityNumDocs;
            }
        }

	}

	// Broadcast number of documents
	MPI_Bcast(
        // Buffer
        &nNumDocs,
        // Count
        1,
        // Type
        MPI_INT,
        // Root
        N_RANK_MASTER,
        // Communicator
        MPI_COMM_WORLD
    );

	/* Scatter first responsible document
	 * Sent to all OTHER processes: https://www.mpich.org/static/docs/v3.1/www3/MPI_Scatter.html
	 * Sent to ALL processes:       https://www.open-mpi.org/doc/v2.0/man3/MPI_Scatter.3.php
	 *                              http://mpitutorial.com/tutorials/mpi-scatter-gather-and-allgather/
	 * The latter seems to be correct, this is sent to all processes including the Master
	 */
	MPI_Scatter(
	    // Send buffer
        anResponsibilitiesFirstDoc,
        // Send count
        1,
        // Send type
        MPI_INT,
        // Receive buffer
        &nResponsbilityFirstDoc,
        // Receive count
        1,
        // Receive type
        MPI_INT,
        // Root
        N_RANK_MASTER,
        // Communicator
        MPI_COMM_WORLD
    );

    /* Scatter number responsible documents
     * Sent to all OTHER processes: https://www.mpich.org/static/docs/v3.1/www3/MPI_Scatter.html
     * Sent to ALL processes:       https://www.open-mpi.org/doc/v2.0/man3/MPI_Scatter.3.php
     *                              http://mpitutorial.com/tutorials/mpi-scatter-gather-and-allgather/
     * The latter seems to be correct, this is sent to all processes including the Master
     */
    MPI_Scatter(
        // Send buffer
        anResponsibilitiesNumDocs,
        // Send count
        1,
        // Send type
        MPI_INT,
        // Receive buffer
        &nResponsibilityNumDocs,
        // Receive count
        1,
        // Receive type
        MPI_INT,
        // Root
        N_RANK_MASTER,
        // Communicator
        MPI_COMM_WORLD
    );

    // If Worker, print what you're responsible for (for debugging purposes)
    if (nMyRank != N_RANK_MASTER) {
        printf(
            "Rank %d Responsibilities: # docs %d, first doc %d, # docs to process %d\n",
            nMyRank,
            nNumDocs,
            nResponsbilityFirstDoc,
            nResponsibilityNumDocs
        );
    }

    // If Worker, loop through each document for which you are responsible and gather TFIDF variables for each word
    if (nMyRank != N_RANK_MASTER) {

        for (i = nResponsbilityFirstDoc; i < (nResponsbilityFirstDoc + nResponsibilityNumDocs); i++) {

            // Open document
            sprintf(document, "doc%d", i);
            sprintf(filename, "input/%s", document);
            FILE* fp = fopen(filename, "r");
            if (fp == NULL){
                printf("Error Opening File: %s\n", filename);
                exit(0);
            }

            // Get the document size
            numWordsInDoc = 0;
            while ((fscanf(fp, "%s", word))!= EOF) {
                numWordsInDoc++;
            }

            // For each word in the document
            fseek(fp, 0, SEEK_SET);
            while((fscanf(fp, "%s", word)) != EOF){

                bArrayContainsWord = 0;

                // If TFIDF array already contains the word@document, just increment wordCountInDoc and break
                for (j = 0; j < TFIDF_index; j++) {
                    if (!strcmp(tfidf_structs[j].word, word) && !strcmp(tfidf_structs[j].document, document)){
                        bArrayContainsWord = 1;
                        tfidf_structs[j].wordCountInDoc++;
                        break;
                    }
                }

                // If TFIDF array does not contain it, make a new one with wordCountInDoc=1
                if (!bArrayContainsWord) {
                    strcpy(tfidf_structs[TFIDF_index].word, word);
                    strcpy(tfidf_structs[TFIDF_index].document, document);
                    tfidf_structs[TFIDF_index].wordCountInDoc = 1;
                    tfidf_structs[TFIDF_index].numWordsInDoc = numWordsInDoc;
                    TFIDF_index++;
                }

                // Reset flag for next time
                bArrayContainsWord = 0;

                // If unique_words array already contains the word, just increment numDocsWithWord
                for (j = 0; j < unique_words_index; j++) {
                    if (!strcmp(unique_word_structs[j].word, word)){
                        bArrayContainsWord = 1;
                        if (unique_word_structs[j].currentDocument != i) {
                            unique_word_structs[j].numDocsWithWord++;
                            unique_word_structs[j].currentDocument = i;
                        }
                        break;
                    }
                }

                // If unique_words array does not contain it, make a new one with numDocsWithWord=1
                if (!bArrayContainsWord) {
                    strcpy(unique_word_structs[unique_words_index].word, word);
                    unique_word_structs[unique_words_index].numDocsWithWord = 1;
                    unique_word_structs[unique_words_index].currentDocument = i;
                    unique_words_index++;
                }
            }

            // Close file
            fclose(fp);

        }

    }

    // If Worker, print TF job similar to HW4/HW5 (for debugging purposes)
    if (nMyRank != N_RANK_MASTER) {
        for (j = 0; j < TFIDF_index; j++) {
            printf(
                "Rank %d TF Job: %s@%s\t%d/%d\n",
                nMyRank,
                tfidf_structs[j].word,
                tfidf_structs[j].document,
                tfidf_structs[j].wordCountInDoc,
                tfidf_structs[j].numWordsInDoc
            );
        }
    }

    /* Workers exchange information about unique words they found
     *  In order to do IDF calculation, all Workers need to know,
     *      for all of their words, how many documents the word was found in.
     *  So they must exchange information with each other.
     *  It is not known how many words each Worker will have found.
     *  Each Worker sends two arrays out to other Workers.
     *      One array holds the words the Worker found.
     *      The other array hold the counts of how many documents each word was found in by the Worker.
     *  Each Worker then waits to receive like messages from other Workers.
     *  The receiving Worker looks to see how many unique words were sent,
     *      and then loops over the information,
     *      updating counts for the words which the Worker is interested in.
     */
    if (nMyRank != N_RANK_MASTER) {

        // Send unique words found, and number of docs they were found in, to every other Worker
        for (i = 0; i < unique_words_index; i++) {
            strcpy(asUniqueWordsSend[i], unique_word_structs[i].word);
            anNumDocsWithWordSend[i] =  unique_word_structs[i].numDocsWithWord;
        }
        for (i = 1; i <= nNumWorkers; i++) {
            if (i != nMyRank) {
                MPI_Isend(
                    // Buffer
                    asUniqueWordsSend,
                    // Count
                    unique_words_index,
                    // Type
                    MPI_WORD,
                    // Destination
                    i,
                    // Tag
                    N_TAG_UNIQUE_WORDS_WORD,
                    // Communicator
                    MPI_COMM_WORLD,
                    // Pointer to request
                    &aoSendRequestsWords[i]
                );
                MPI_Isend(
                    // Buffer
                    anNumDocsWithWordSend,
                    // Count
                    unique_words_index,
                    // Type
                    MPI_INT,
                    // Destination
                    i,
                    // Tag
                    N_TAG_UNIQUE_WORDS_NUM_DOCS,
                    // Communicator
                    MPI_COMM_WORLD,
                    // Pointer to request
                    &aoSendRequestsCounts[i]
                );
            }
        }

        // Receive unique words found, and number of docs they were found in, from every other Worker
        for (i = 1; i <= nNumWorkers; i++) {
            if (i != nMyRank) {

                // Receive information from this fellow Worker
                MPI_Recv(
                    asUniqueWordsRecv,
                    MAX_WORDS_IN_CORPUS,
                    MPI_WORD,
                    i,
                    N_TAG_UNIQUE_WORDS_WORD,
                    MPI_COMM_WORLD,
                    &oReceiveStatusWords
                );
                MPI_Recv(
                    anNumDocsWithWordRecv,
                    MAX_WORDS_IN_CORPUS,
                    MPI_INT,
                    i,
                    N_TAG_UNIQUE_WORDS_NUM_DOCS,
                    MPI_COMM_WORLD,
                    &oReceiveStatusNumDocs
                );

                // Figure out how many unique words were found by other Worker
                MPI_Get_count(
                    &oReceiveStatusNumDocs,
                    MPI_INT,
                    &nReceivedCount
                );

                // Update the contents of our struct which tracks our found unique words
                for (j = 0; j < nReceivedCount; j++) {
                    for (k = 0; k < unique_words_index; k++) {
                        if (!strcmp(unique_word_structs[k].word, asUniqueWordsRecv[j])) {
                            unique_word_structs[k].numDocsWithWord += anNumDocsWithWordRecv[j];
                            break;
                        }
                    }
                }

            }
        }

        // Wait to make sure your own sends have completed
        for (i = 1; i <= nNumWorkers; i++) {
            if (i != nMyRank) {
                MPI_Wait(
                    &aoSendRequestsWords[i],
                    MPI_STATUS_IGNORE
                );
                MPI_Wait(
                    &aoSendRequestsCounts[i],
                    MPI_STATUS_IGNORE
                );
            }
        }

    }

    // If Worker, use unique_words array to populate TFIDF objects with: numDocsWithWord
    if (nMyRank != N_RANK_MASTER) {

        for (i = 0; i < TFIDF_index; i++) {
            for (j = 0; j < unique_words_index; j++) {
                if (!strcmp(tfidf_structs[i].word, unique_word_structs[j].word)) {
                    tfidf_structs[i].numDocsWithWord = unique_word_structs[j].numDocsWithWord;
                    break;
                }
            }
        }

    }
	
    // If Worker, print IDF job similar to HW4/HW5 (for debugging purposes)
    if (nMyRank != N_RANK_MASTER) {
        for (j = 0; j < TFIDF_index; j++) {
            printf(
                "Rank %d IDF Job: %s@%s\t%d/%d\n",
                nMyRank,
                tfidf_structs[j].word,
                tfidf_structs[j].document,
                nNumDocs,
                tfidf_structs[j].numDocsWithWord
            );
        }
    }

    // If Worker, calculate TFIDF as "document@word\tTFIDF" and put into array
    if (nMyRank != N_RANK_MASTER) {

        for (j = 0; j < TFIDF_index; j++) {
            double TF = 1.0 * tfidf_structs[j].wordCountInDoc / tfidf_structs[j].numWordsInDoc;
            double IDF = log(1.0 * nNumDocs / tfidf_structs[j].numDocsWithWord);
            double TFIDF_value = TF * IDF;
            sprintf(
                asResultsTFIDF[j],
                "%s@%s\t%.16f",
                tfidf_structs[j].document,
                tfidf_structs[j].word,
                TFIDF_value
            );
        }

    }

    /* Workers send TFIDF strings to Master
     *  The Master needs all TFIDF strings from the Workers so that it can sort them before printing to file.
     *  It is not known how many TFIDF strings each Worker will have produced.
     *  Each Worker sends one array of TFIDF strings to the Master.
     */
    if (nMyRank != N_RANK_MASTER) {

        MPI_Send(
            asResultsTFIDF,
            TFIDF_index,
            MPI_TFIDF_STRING,
            N_RANK_MASTER,
            N_TAG_TFIDF_CONTENT,
            MPI_COMM_WORLD
        );

    }

    // Master receives TFIDF strings from Workers
    if (nMyRank == N_RANK_MASTER) {

        for (i = 1; i <= nNumWorkers; i++) {

            // Receive TFIDF string array from Worker
            MPI_Recv(
                asResultsTFIDF + TFIDF_index,
                MAX_WORDS_IN_CORPUS,
                MPI_TFIDF_STRING,
                i,
                N_TAG_TFIDF_CONTENT,
                MPI_COMM_WORLD,
                &oReceiveStatusTFIDF
            );

            // Figure out how many TFIDF strings were sent by Worker
            MPI_Get_count(
                &oReceiveStatusTFIDF,
                MPI_TFIDF_STRING,
                &nReceivedCount
            );
            TFIDF_index += nReceivedCount;

        }

    }

	// If Master, sort asResultsTFIDF and print to file
	if (nMyRank == N_RANK_MASTER) {

        qsort(asResultsTFIDF, TFIDF_index, (sizeof(char) * MAX_LENGTH_TFIDF_STRING), myCompare);
        FILE* fp = fopen("output.txt", "w");
        if (fp == NULL){
            printf("Error Opening File: output.txt\n");
            exit(0);
        }
        for (i = 0; i < TFIDF_index; i++) {
            fprintf(fp, "%s\n", asResultsTFIDF[i]);
        }
        fclose(fp);

	}
	
	// Finalize
	MPI_Finalize();
	return 0;

}

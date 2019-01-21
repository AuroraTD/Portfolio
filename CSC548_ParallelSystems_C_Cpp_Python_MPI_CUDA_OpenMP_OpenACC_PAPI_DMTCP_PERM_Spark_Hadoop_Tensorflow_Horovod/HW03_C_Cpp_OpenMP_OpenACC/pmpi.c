/*************************************************************************************************************
 * FILE:            pmpi.c
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Profiling wrappers for MPI_Init, MPI_Send, and MPI_Finalize
 *
 * TO RUN:          Include in Lulesh makefile, run Lulesh
 *************************************************************************************************************/

// INCLUDES
#include <stdio.h>
#include <stdlib.h>
#include "mpi.h"

// DEFINES
#define N_RANK_ROOT 0
#define N_DEBUG     0

// Hack to get around a problem I have in my IDE setup
#ifndef NULL
    #define NULL ((void *) 0)
#endif

// Globals
int     nNumTasks =         -1;
int     nMyRank =           -1;
int*    anSendCountsMe =    NULL;
int*    anSendCountsAll =   NULL;

/*************************************************************************************************************
 * FUNCTION:        MPI_Init
 *
 * DESCRIPTION:     Profiling wrapper for MPI_Init
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          nResult -   MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Init(int *argc, char ***argv) { // @suppress("Name convention for function")

    // Declare variables
    int nReturn;

    // Assume success until we know otherwise
    nReturn = MPI_SUCCESS;

    // Call MPI function
    if (nReturn == MPI_SUCCESS) {
        nReturn = PMPI_Init(argc, argv);
    }

    /// Get the number of tasks in the communicator
    if (nReturn == MPI_SUCCESS) {
        nReturn = PMPI_Comm_size(MPI_COMM_WORLD, &nNumTasks);
    }

    // Get my rank in the communicator
    if (nReturn == MPI_SUCCESS) {
        nReturn = PMPI_Comm_rank(MPI_COMM_WORLD, &nMyRank);
    }

    // Allocate memory for send counts (me)
    if (nReturn == MPI_SUCCESS) {
        anSendCountsMe = (int*) calloc(nNumTasks, sizeof(int));
        if (anSendCountsMe == NULL) {
            printf("Task rank %d could not allocate memory for its own send counts array\n", nMyRank);
            fflush(stdout);
            nReturn = MPI_ERR_BUFFER;
        }
    }

    /* Allocate memory for send counts (all)
     *  Concept is one big array (rather than 2D array), to facilitate MPI_Gather
     */
    if (nReturn == MPI_SUCCESS && nMyRank == N_RANK_ROOT) {
        anSendCountsAll = (int*) calloc(nNumTasks * nNumTasks, sizeof(int));
        if (anSendCountsAll == NULL) {
            printf("Task rank %d could not allocate memory for final send counts array\n", nMyRank);
            fflush(stdout);
            nReturn = MPI_ERR_BUFFER;
        }
    }

    // Print debug info
    if (N_DEBUG >= 2) {
        printf("Task rank %d has initialized\n", nMyRank);
        printf("Task rank %d sees number of tasks as %d\n", nMyRank, nNumTasks);
        fflush(stdout);
    }

    // Return
    return nReturn;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Send
 *
 * DESCRIPTION:     Profiling wrapper for MPI_Send
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          nResult -   MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Send(const void *buf, int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm) { // @suppress("Name convention for function")

    // Declare variables
    int nReturn;

    // Assume success until we know otherwise
    nReturn = MPI_SUCCESS;

    // Update send count
    if (nReturn == MPI_SUCCESS) {
        anSendCountsMe[dest] = anSendCountsMe[dest] + 1;
    }

    // Call MPI function
    if (nReturn == MPI_SUCCESS) {
        nReturn = PMPI_Send(buf, count, datatype, dest, tag, comm);
    }

    // Return
    return nReturn;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Isend
 *
 * DESCRIPTION:     Profiling wrapper for MPI_Isend
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          nResult -   MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Isend(const void *buf, int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm, MPI_Request *request) { // @suppress("Name convention for function")

    // Declare variables
    int nReturn;

    // Assume success until we know otherwise
    nReturn = MPI_SUCCESS;

    // Update send count
    if (nReturn == MPI_SUCCESS) {
        anSendCountsMe[dest] = anSendCountsMe[dest] + 1;
    }

    // Call MPI function
    if (nReturn == MPI_SUCCESS) {
        nReturn = PMPI_Isend(buf, count, datatype, dest, tag, comm, request);
    }

    // Return
    return nReturn;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Finalize
 *
 * DESCRIPTION:     Profiling wrapper for MPI_Finalize
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          nResult -   MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Finalize() { // @suppress("Name convention for function")

    // Print debug info
    if (N_DEBUG >= 1) {
        printf("Task rank %d finalizing\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    FILE*   oFilePointer; // @suppress("Type cannot be resolved")
    int     nReturn;
    int     nIndexSender;
    int     nIndexCount;
    int     i;

    // Assume success until we know otherwise
    nReturn = MPI_SUCCESS;

    // Barrier, to be on the safe side
    if (nReturn == MPI_SUCCESS) {
        nReturn = PMPI_Barrier(MPI_COMM_WORLD);
    }

    // Gather send counts back to root
    if (nReturn == MPI_SUCCESS) {

        // Participate in gather
        nReturn = PMPI_Gather(
            // Send buffer
            (void *) anSendCountsMe,
            // Send count
            nNumTasks,
            // Send type
            MPI_INT,
            // Receive buffer
            (void *) anSendCountsAll,
            // Receive count
            nNumTasks,
            // Receive type
            MPI_INT,
            // Root
            N_RANK_ROOT,
            // Communicator
            MPI_COMM_WORLD
        );

        // Print debug info
        if (N_DEBUG >= 1 && nMyRank == N_RANK_ROOT) {
            printf("Task rank %d has participated in gather operation\n", nMyRank);
            printf("Task rank %d contents of send counts buffer:\n", nMyRank);
            for (i = 0; i < nNumTasks; i++) {
                printf("%d ", anSendCountsMe[i]);
            }
            printf("\n");
            fflush(stdout);
        }

    }

    // Print send counts to file
    if (nReturn == MPI_SUCCESS && nMyRank == N_RANK_ROOT) {

        // Open file
        oFilePointer = fopen("matrix.data", "w");

        // Write to file
        for (nIndexSender = 0; nIndexSender < nNumTasks; nIndexSender++) {
            // Rank of sender
            fprintf(oFilePointer, "%d ", nIndexSender);
            for (nIndexCount = 0; nIndexCount < nNumTasks; nIndexCount++) {
                /* Count of sends to other ranks
                 *  Buffer holds all results reported by rank 0, then all results reported by rank 1, etc.
                 *  Concept is one big array (rather than 2D array), to facilitate MPI_Gather
                 */
                fprintf(oFilePointer, "%d ", anSendCountsAll[nIndexSender * nNumTasks + nIndexCount]);
                if (nIndexCount < nNumTasks - 1) {
                    fprintf(oFilePointer, " ");
                }
            }
            // Newline before next rank
            fprintf(oFilePointer, "\n");
        }

        // Close file
        fclose(oFilePointer);

        // Print debug info
        if (N_DEBUG >= 1) {
            printf("Task rank %d has written to file\n", nMyRank);
            fflush(stdout);
        }

    }

    // Free memory
    if (nReturn == MPI_SUCCESS) {
        if (anSendCountsMe != NULL) {
            free(anSendCountsMe);
        }
        if (anSendCountsAll != NULL) {
            free(anSendCountsAll);
        }
    }

    // Call MPI function
    if (nReturn == MPI_SUCCESS) {
        nReturn = PMPI_Finalize();
    }

    // Return
    return nReturn;

}

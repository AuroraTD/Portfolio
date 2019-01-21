/*************************************************************************************************************
 * FILE:            my_mpi.c
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     A very basic implementation of MPI using C Socket programming.
 *                  Implements all functions used in my_rtt.c.
 *                  Does not implement tags used in my_rtt.c
 *                      because those tags are not functionally necessary.
 *
 * TO RUN:          Include my_mpi.h in a C file, call MPI functions
 *************************************************************************************************************/

// INCLUDES
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>
#include <unistd.h>
#include <errno.h>
#include <netdb.h>
#include <dirent.h>
#include <limits.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "my_mpi.h"

// Defines
#define N_RANK_ROOT             0
#define N_MAX_NUM_TASKS         100
#define N_MAX_SIZE_HOST_NAME    1024
#define N_MAX_SIZE_LINE         1024
#define N_TAG_GATHER            0
#define N_TAG_BARRIER           1
#define B_DEBUG                 0

// GLOBALS
int                 nNumTasks =                 -1;
int                 nMyRank =                   -1;
int                 sServerSocketDescriptor =   -1;
char*               sPortListFile;
FILE*               fp;
int*                anPortNumbers;
int*                anSocketsToTalkTo;
char                asHostNames[N_MAX_NUM_TASKS][N_MAX_SIZE_HOST_NAME];
struct sockaddr_in  oServerAddressInfo;
const char          sBarrier[] = "Z";
int                 nSizeBarrierString = sizeof(sBarrier);

/*************************************************************************************************************
 * FUNCTION:        MPI_Init
 *
 * DESCRIPTION:     Initializes message passing
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *                  argv should have these elements:
 *                      0 - (as always, name of program)
 *                      1 - Task rank
 *                      2 - Number of tasks
 *                      3 - Name of port list file
 *
 * RETURN:          nResult -   MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Init(int *argc, char ***argv) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Some task executing MPI_Init\n");
        fflush(stdout);
    }

    // Declare variables
    int nResult;
    int bAllocateSuccess;
    int bCreateServerSuccess;
    int bCreateClientSuccess;
    int bPortListSuccess;
    int bAcceptClientSuccess;
    int nNumExpectedClients;
    int nRankOfServerToConnectTo;
    int i;

    // Assume success until we know otherwise
    nResult = MPI_SUCCESS;

    // Check arguments
    if (
        // Should have correct number of arguments
        *argc != 4 ||
        // Task rank should be non-negative number
        is_number((*argv)[1]) != 1 ||
        atoi((*argv)[1]) < 0 ||
        // Number of tasks should be greater-than-zero number
        is_number((*argv)[2]) != 1 ||
        atoi((*argv)[2]) <= 0 ||
        // port list file should be non-number
        is_number((*argv)[3]) != 0
    ) {

        // Complain about bad arguments
        printf("%d arguments provided to MPI_Init:\n", *argc);
        for (i = 0; i < *argc; i++) {
            printf("%s\n", (*argv)[i]);
        }
        printf("Usage: [MPI program name] [task rank] [number of tasks] [name of port list file]\n");
        fflush(stdout);
        nResult = MPI_ERROR;

    }

    // Save arguments to globals
    if (nResult != MPI_ERROR) {
        nMyRank =       atoi((*argv)[1]);
        nNumTasks =     atoi((*argv)[2]);
        sPortListFile = (*argv)[3];
    }

    /* Allocate memory
     *  Do this after arguments are saved because this depends on number of tasks
     */
    if (nResult != MPI_ERROR) {
        bAllocateSuccess = allocate_memory();
        nResult = bAllocateSuccess == 1 ? MPI_SUCCESS : MPI_ERROR;
    }

    // If even-numbered task, create server socket
    if (nResult != MPI_ERROR && nMyRank % 2 == 0) {
        bCreateServerSuccess = create_server_socket();
        nResult = bCreateServerSuccess == 1 ? MPI_SUCCESS : MPI_ERROR;
    }

    // Write your info to a port list file that all tasks can read
    if (nResult != MPI_ERROR) {
        bPortListSuccess = write_to_port_list_file();
        nResult = bPortListSuccess == 1 ? MPI_SUCCESS : MPI_ERROR;
    }

    /* Read everybody's info from a port list file that all tasks can read
     *  Will keep trying the read until everybody's info is in the file
     *  Effectively, this functions as a sort of barrier
     */
    if (nResult != MPI_ERROR) {
        bPortListSuccess = read_from_port_list_file();
        nResult = bPortListSuccess == 1 ? MPI_SUCCESS : MPI_ERROR;
    }

    /* Create client socket to a specific server socket
     *  Who talks to who
     *      Every even node talks to the root
     *          to support my_rtt.c functionality
     *      Every node talks to the root
     *          to support barrier
     *      Every odd node talks to partner even node
     */
    if (nResult != MPI_ERROR && nMyRank != N_RANK_ROOT) {
        bCreateClientSuccess = create_client_socket(N_RANK_ROOT);
        nResult = bCreateClientSuccess == 1 ? MPI_SUCCESS : MPI_ERROR;
    }
    if (nResult != MPI_ERROR && nMyRank % 2 != 0 && nMyRank != (N_RANK_ROOT+1)) {
        bCreateClientSuccess = create_client_socket(nMyRank-1);
        nResult = bCreateClientSuccess == 1 ? MPI_SUCCESS : MPI_ERROR;
    }

    /* Accept client connections
     *  Who talks to who
     *      Every even node talks to the root
     *          to support my_rtt.c functionality
     *      Every node talks to the root
     *          to support barrier
     *      Every odd node talks to partner even node
     */
    if (nResult != MPI_ERROR && nMyRank % 2 == 0) {

        // Figure out how many clients are expected
        if (nMyRank == N_RANK_ROOT) {
            nNumExpectedClients = nNumTasks - 1;
        }
        else {
            nNumExpectedClients = 1;
        }

        // Block until we have an error or all expected clients have connected
        while (nResult != MPI_ERROR && nNumExpectedClients > 0) {
            // Accept one client connection (blocking)
            bAcceptClientSuccess = accept_client_connection();
            nResult = bAcceptClientSuccess == 1 ? MPI_SUCCESS : MPI_ERROR;
            // Update the count of expected clients
            nNumExpectedClients--;
        }

    }

    // Barrier
    if (nResult != MPI_ERROR) {
        nResult = MPI_Barrier(MPI_COMM_WORLD);
    }

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d has these sockets to talk to: ", nMyRank);
        for (i = 0; i < nNumTasks; i++) {
            printf("%d ", anSocketsToTalkTo[i]);
        }
        printf("\n");
        fflush(stdout);
    }

    // Return
    return nResult;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Comm_size
 *
 * DESCRIPTION:     Stores the number of tasks in the given communicator
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Comm_size (MPI_Comm comm, int *size) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing MPI_Comm_size\n", nMyRank);
        fflush(stdout);
    }

    // Store
    *size = nNumTasks;

    // Return
    nNumTasks < 0 ? MPI_ERROR : MPI_SUCCESS;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Comm_rank
 *
 * DESCRIPTION:     Stores the rank of the current task in the given communicator
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Comm_rank (MPI_Comm comm, int *rank) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing MPI_Comm_rank\n", nMyRank);
        fflush(stdout);
    }

    // Store
    *rank = nMyRank;

    // Return
    nMyRank < 0 ? MPI_ERROR : MPI_SUCCESS;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Ssend
 *
 * DESCRIPTION:     Wraps MPI_Send, because in this simplified implementation
 *                  there is no distinction between these variants
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Ssend (const void *buf, int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing MPI_Ssend\n", nMyRank);
        fflush(stdout);
    }

    // Wrap basic blocking send
    return MPI_Send(buf, count, datatype, dest, tag, comm);

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Ssend
 *
 * DESCRIPTION:     Sends a message with the given tag to the given destination
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Send (const void *buf, int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing MPI_Send\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int     nBytesWritten;
    int     nResult;

    // Assume success until we know otherwise
    nResult = MPI_SUCCESS;

    // Send a message (blocks)
    nBytesWritten = write(anSocketsToTalkTo[dest], buf, (count * get_size_of_data_type(datatype)));
    if (nBytesWritten < 0) {
        printf("Task rank %d could not write to task rank %d\n", nMyRank, dest);
        fflush(stdout);
        nResult = MPI_ERROR;
    }

    // Return
    return nResult;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Recv
 *
 * DESCRIPTION:     Receives a message with the given tag from the given source
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Recv (void *buf, int count, MPI_Datatype datatype, int source, int tag, MPI_Comm comm, MPI_Status *status) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing MPI_Recv\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int nBytesReadThisTime;
    int nBytesReadTotal;
    int nBytesPending;
    int nResult;

    // Assume success until we know otherwise
    nResult = MPI_SUCCESS;

    /* Read a message (blocks)
     *  read either the total number of characters in the socket,
     *      or the number of chars specified here,
     *      whichever is less,
     *      and return the number of characters read
     *  so we keep reading until we get all of the bytes we expect
     */
    nBytesPending = count * get_size_of_data_type(datatype);
    nBytesReadTotal = 0;
    while (nResult != MPI_ERROR && nBytesPending > 0) {
        nBytesReadThisTime = read(anSocketsToTalkTo[source], (buf + nBytesReadTotal), nBytesPending);
        nBytesPending -= nBytesReadThisTime;
        nBytesReadTotal += nBytesReadThisTime;
        if (nBytesReadThisTime < 0) {
            printf("Task rank %d could not read from task rank %d\n", nMyRank, source);
            fflush(stdout);
            nResult = MPI_ERROR;
        }
        else if (nBytesReadThisTime == 0) {
            printf("Task rank %d has detected that task rank %d has closed the connection\n", nMyRank, source);
            fflush(stdout);
            nResult = MPI_ERROR;
        }
    }

    // Return
    return nResult;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Gatherv
 *
 * DESCRIPTION:     Gathers messages of varying counts from multiple nodes to the root node
 *                  Does this by using multiple blocking receives at the root node,
 *                      so there is no expectation that this is efficient.
 *                  This function assumes that recvcounts and displs
 *                      have a number of elements equal to the number of tasks.
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Gatherv (
    const void *sendbuf,
    int sendcount,
    MPI_Datatype sendtype,
    void *recvbuf,
    const int *recvcounts,
    const int *displs,
    MPI_Datatype recvtype,
    int root,
    MPI_Comm comm
) {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing MPI_Gatherv\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int         nResult;
    int         i;
    MPI_Status  nDummyStatus;

    // Assume success until we know otherwise
    nResult = MPI_SUCCESS;

    // If root, you are receiving multiple messages
    if (nMyRank == root) {

        // Zero out the receive buffer regardless of data type (helps with debug)
        memset(recvbuf, 0, sizeof(recvbuf));

        // Gather results from other nodes and from self
        for (i = 0; i < nNumTasks; i++) {

            // At the gather point, simply do a memory copy
            if (i == root && recvcounts[i] > 0) {
                memcpy(
                    (recvbuf + displs[i] * get_size_of_data_type(sendtype)),
                    sendbuf,
                    sendcount * (get_size_of_data_type(sendtype))
                );
            }

            /* Receive a message (maybe) from every node that isn't the gather point
             *  Blocking, and done in a loop, so gather is going to be slow
             *  Put whatever is received into the correct location in the receive buffer
             */
            else if (recvcounts[i] > 0) {
                nResult = MPI_Recv(
                    (recvbuf + displs[i] * get_size_of_data_type(sendtype)),
                    recvcounts[i],
                    recvtype,
                    i,
                    N_TAG_GATHER,
                    MPI_COMM_WORLD,
                    &nDummyStatus
                );
            }

            // If we had any problems, quit so we can debug them
            if (nResult != MPI_SUCCESS) {
                break;
            }

        }

    }

    // If not root, you are sending one message (maybe)
    else if (sendcount > 0) {
        MPI_Send(
            sendbuf,
            sendcount,
            sendtype,
            root,
            N_TAG_GATHER,
            MPI_COMM_WORLD
        );
    }

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d leaving MPI_Gatherv\n", nMyRank);
        fflush(stdout);
    }


    // Return
    return nResult;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Finalize
 *
 * DESCRIPTION:     Finalizes communication
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Finalize () {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing MPI_Finalize\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int i;
    int nResult;

    /* Wait until all tasks get here (helps with debug)
     *  Normally we would make use of any error from a function that is capable of returning an error
     *  But in this case even if this has an error we need to proceed with other clean-up tasks
     */
    nResult = MPI_Barrier(MPI_COMM_WORLD);

    /* Close sockets and release ports
     *  Different task ranks act differently so take that into account and be flexible here
     *  sServerSocketDescriptor is initialized to -1
     *  anSocketsToTalkTo is initialized with values of zero via calloc
     */
    if (sServerSocketDescriptor >= 0) {
        close(sServerSocketDescriptor);
    }
    for (i = 0; i < nNumTasks; i++) {
        if (anSocketsToTalkTo[i] != 0) {
            close(anSocketsToTalkTo[i]);
        }
    }

    /* Free dynamically allocated memory
     *  Normally we would make use of any error from a function that is capable of returning an error
     *  But in this case even if this has an error we need to proceed with other clean-up tasks
     */
    free_memory();

    // If ROOT, remove all files (after a short sleep)
    if (nMyRank == N_RANK_ROOT) {
        sleep(2);
        remove_all_files();
    }

    // If we made it this far, return success
    return nResult;

}

/*************************************************************************************************************
 * FUNCTION:        MPI_Barrier
 *
 * DESCRIPTION:     Uses messages to implement barrier so WILL NOT WORK before messaging is up and running.
 *
 * ARGUMENTS:       (as described here: https://www.mpich.org/static/docs/v3.2/)
 *
 * RETURN:          nResult -   MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int MPI_Barrier (MPI_Comm comm) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("\nTask rank %d executing MPI_Barrier\n\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int         nResult;
    int         i;

    // Assume success until we know otherwise
    nResult = MPI_SUCCESS;

    /* Perform barrier operation
     *  Steps
     *      1 - Everyone reports to root that they have reached the barrier
     *      2 - Root waits to hear that everyone has reached the barrier
     *      3 - Root reports back out to everyone that all have reached the barrier
     *      4 - Everyone waits to hear back from root that everyone has reached the barrier
     *  A message with a specific value
     *      is interpreted uniquely as a barrier message (tags are ignored)
     *  This is just one way in which this very basic implementation of MPI
     *      is not extendible beyond my_rtt.c
     */

    // Everyone reports to root that they have reached the barrier
    if (nResult != MPI_ERROR && nMyRank != N_RANK_ROOT) {
        nResult = MPI_Send(
            &sBarrier,
            nSizeBarrierString,
            MPI_BYTE,
            N_RANK_ROOT,
            N_TAG_BARRIER,
            MPI_COMM_WORLD
        );
    }

    // Root waits to hear that everyone has reached the barrier
    if (nResult != MPI_ERROR && nMyRank == N_RANK_ROOT) {
        for (i = 0; i < nNumTasks; i++) {
            if (i != N_RANK_ROOT) {
                nResult = wait_for_barrier_message(i);
                if (nResult == MPI_ERROR) {
                    break;
                }
            }
        }
    }

    // Root reports back out to everyone that all have reached the barrier
    if (nResult != MPI_ERROR && nMyRank == N_RANK_ROOT) {
        for (i = 0; i < nNumTasks; i++) {
            if (i != N_RANK_ROOT) {
                nResult = MPI_Send(
                    &sBarrier,
                    nSizeBarrierString,
                    MPI_BYTE,
                    i,
                    N_TAG_BARRIER,
                    MPI_COMM_WORLD
                );
                if (nResult == MPI_ERROR) {
                    break;
                }
            }
        }
    }

    // Everyone waits to hear back from root that everyone has reached the barrier
    if (nResult != MPI_ERROR && nMyRank != N_RANK_ROOT) {
        nResult = wait_for_barrier_message(N_RANK_ROOT);
    }

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("\nTask rank %d leaving MPI_Barrier\n\n", nMyRank);
        fflush(stdout);
    }

    // Return
    return nResult;

}

/*************************************************************************************************************
 * FUNCTION:        wait_for_barrier_message
 *
 * DESCRIPTION:     Wait for one specific message to arrive, the barrier message
 *
 * ARGUMENTS:       nSourceRank -   The rank of the node from whom we expect a barrier message
 *
 * RETURN:          MPI_SUCCESS or MPI_ERROR
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int wait_for_barrier_message (int nSourceRank) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing wait_for_barrier_message\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int         nResult;
    int         bGotBarrierMessage;
    MPI_Status  nDummyStatus;
    char        sReceived[20];

    // Assume success until we know otherwise
    nResult = MPI_SUCCESS;

    /* Wait for specifically the barrier message
     *  If we get something else,
     *  there's some junk in the socket buffer, throw it out and try again
     *  It's not wise to ignore this junk but that's what this code does
     *  This is a very dumb implementation of MPI due to time constraints
     *  PS -
     *      I wrote this code before I found the cause of my partially read messages
     *      This code should no longer be needed,
     *      but "it works so I'm not touching it!"
     */
    bGotBarrierMessage = 0;
    while (nResult != MPI_ERROR && bGotBarrierMessage == 0) {
        nResult = MPI_Recv(
            &sReceived,
            nSizeBarrierString,
            MPI_BYTE,
            nSourceRank,
            N_TAG_BARRIER,
            MPI_COMM_WORLD,
            &nDummyStatus
        );
        if (nResult != MPI_ERROR) {
            if (strcmp(sReceived, sBarrier) == 0) {
                bGotBarrierMessage = 1;
            }
        }
    }

    // Return
    return nResult;

}

/*************************************************************************************************************
 * FUNCTION:        remove_all_files
 *
 * DESCRIPTION:     Remove all files that might have been created while the program was running
 *
 * ARGUMENTS:       None
 *
 * RETURN:          None
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
void remove_all_files () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing remove_all_files\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int i;

    // Remove all files
    remove(sPortListFile);

}

/*************************************************************************************************************
 * FUNCTION:        is_number
 *
 * DESCRIPTION:     Checks to see if a char array represents a number
 *
 * ARGUMENTS:       sPossibleNumber - Something we hope actually represents a number
 *
 * RETURN:          1 if number, 0 otherwise
 *
 * SOURCE:          https://stackoverflow.com/questions/29248585/c-checking-command-line-argument-is-integer-or-not
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int is_number (char sPossibleNumber[]) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing is_number\n", nMyRank);
        fflush(stdout);
    }

    // Declare variable
    int bNumber = 1;
    int i = 0;

    // Checking for negative numbers
    if (sPossibleNumber[0] == '-') {
        i = 1;
    }
    for (; sPossibleNumber[i] != 0; i++)
    {
        //if (number[i] > '9' || number[i] < '0')
        if (!isdigit(sPossibleNumber[i])) {
            bNumber = 0;
            break;
        }
    }

    // Return
    return bNumber;

}

/*************************************************************************************************************
 * FUNCTION:        get_port_number
 *
 * DESCRIPTION:     Returns the port number of this server
 *
 * ARGUMENTS:       None
 *
 * RETURN:          nPort - Port number if this task is acting as a server, else -1
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int get_port_number () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing get_port_number\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int nPort;

    // Only even-numbered tasks act as servers so need port numbers
    if (nMyRank % 2 == 0) {

        // Pull port number out of this object
        nPort = ntohs(oServerAddressInfo.sin_port);

    }
    else {
        nPort = -1;
    }

    // Return
    return nPort;

}

/*************************************************************************************************************
 * FUNCTION:        write_to_port_list_file
 *
 * DESCRIPTION:     Write port number for this task to a port list file
 *
 * ARGUMENTS:       None
 *
 * RETURN:          bSuccess -  1 if it seemed to go okay, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int write_to_port_list_file () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing write_to_port_list_file\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    char    sHostName[N_MAX_SIZE_HOST_NAME];
    char    sIP[INET_ADDRSTRLEN];
    int     nPort;
    int     bSuccess;
    int     nResult;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Open (create if it doesn't exist) port list file for reading and appending
    fp = fopen(sPortListFile, "a+");
    if (fp == NULL) {
        printf("Task rank %d could not open port list file for writing\n", nMyRank);
        fflush(stdout);
        bSuccess = 0;
    }
    else {

        /* Get host name
         *  https://stackoverflow.com/questions/5190553/linux-c-get-server-hostname/5190590
         */
        gethostname(sHostName, sizeof(sHostName) - 1);

        // Get port number
        nPort = get_port_number();

        /* Write information about yourself to the file
         *  Each node writes 1 tab-delimited line
         *  Each line contains
         *      Rank
         *      Hostname
         *      Port number
         * Lines will probably be out of order but we don't care
         */
        fprintf(fp, "%d\t%s\t%d\n", nMyRank, sHostName, nPort);
        fflush(fp);

        // Close the port list file
        fclose(fp);

    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        read_from_port_list_file
 *
 * DESCRIPTION:     Read relevant information from the port list file, once it is fully populated
 *
 * ARGUMENTS:       None
 *
 * RETURN:          bSuccess -  1 if it seemed to go okay, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int read_from_port_list_file () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing read_from_port_list_file\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int     bSuccess;
    int     nLinesLeftToRead;
    int     nAttemptsLeft;
    char    sSingleLineFromFile[N_MAX_SIZE_LINE];
    char*   sReadResult;
    char*   sRank;
    char*   sHostName;
    char*   sIP;
    char*   sPort;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Try to open the file for reading
    fp = fopen(sPortListFile, "r");
    if(fp == NULL) {
        printf("Task rank %d could not open port list file for reading\n", nMyRank);
        fflush(stdout);
        bSuccess = 0;
    }

    /* Populate host names and port numbers
     *  No idea how many tasks have already written useful information to the file
     *  Poll until they are all available in the file and populated into arrays
     */
    if (bSuccess != 0) {

        // Keep trying until the file is fully populated or we've exhausted all our attempts
        nLinesLeftToRead = nNumTasks;
        nAttemptsLeft = nNumTasks * 10;

        while (nLinesLeftToRead > 0 && nAttemptsLeft > 0) {
            nAttemptsLeft--;
            sReadResult = fgets(sSingleLineFromFile, N_MAX_SIZE_LINE, fp);
            if (sReadResult == NULL) {
                // Reached the end of the file
                if (nLinesLeftToRead > 0) {
                    // File not fully populated - close file, sleep, then try again
                    fclose(fp);
                    sleep(1);
                    nLinesLeftToRead = nNumTasks;
                    fp = fopen(sPortListFile, "r");
                }
            }
            else {

                /* This is a line in the file
                 *  Each node writes 1 tab-delimited line
                 *  Each line contains
                 *      Rank
                 *      Hostname
                 *      Port number
                 * Lines will probably be out of order but we don't care
                 */
                nLinesLeftToRead--;

                // Get information from this file line
                sRank =                         strtok(sSingleLineFromFile, "\t");
                sHostName =                     strtok(NULL, "\t");
                sPort =                         strtok(NULL, "\t");

                // Save information from this file line
                strcpy(asHostNames[atoi(sRank)], sHostName);
                anPortNumbers[atoi(sRank)] =    atoi(sPort);

            }

        }

        // Close the file
        fclose(fp);

        // If we used up all of our attempts, that's a problem
        if (nAttemptsLeft <= 0) {
            printf("Task rank %d could read full port list file\n", nMyRank);
            fflush(stdout);
            bSuccess = 0;
        }

    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        allocate_memory
 *
 * DESCRIPTION:     Dynamically allocate memory
 *
 * ARGUMENTS:       None
 *
 * RETURN:          bSuccess -  1 if everything went okay, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int allocate_memory () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing allocate_memory\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Allocate memory
    if (bSuccess != 0) {
        anPortNumbers = calloc(nNumTasks, sizeof(int));
        if (anPortNumbers == NULL) {
            printf("Task rank %d could not allocate memory for port numbers array\n", nMyRank);
            fflush(stdout);
            bSuccess = 0;
        }
    }
    if (bSuccess != 0) {
        anSocketsToTalkTo = calloc(nNumTasks, sizeof(int));
        if (anSocketsToTalkTo == NULL) {
            printf("Task rank %d could not allocate memory for client socket descriptors array\n", nMyRank);
            fflush(stdout);
            bSuccess = 0;
        }
    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        free_memory
 *
 * DESCRIPTION:     Free dynamically allocated memory
 *
 * ARGUMENTS:       None
 *
 * RETURN:          bSuccess -  1 if everything went okay, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int free_memory () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing free_memory\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Free memory
    if (anPortNumbers != NULL) {
        free(anPortNumbers);
    }
    if (anSocketsToTalkTo != NULL) {
        free(anSocketsToTalkTo);
    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        create_server_socket
 *
 * DESCRIPTION:     Create a server socket
 *
 * ARGUMENTS:       None
 *
 * RETURN:          bSuccess -  1 if everything seemed to go okay, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int create_server_socket () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing create_server_socket\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;
    int nBindResult;
    int nServerAddressInfoLength;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Create socket using internet address domain, streaming, and most appropriate protocol
    if (bSuccess != 0) {

        sServerSocketDescriptor = socket(AF_INET, SOCK_STREAM, 0);
        if (sServerSocketDescriptor < 0) {
            printf("Task rank %d could not create server socket\n", nMyRank);
            fflush(stdout);
            bSuccess = 0;
        }

    }

    // Set up server socket
    if (bSuccess != 0) {

        // Set all server address info values to zero
        bzero((char *) &oServerAddressInfo, sizeof(oServerAddressInfo));

        // Set up socket to use default constant
        oServerAddressInfo.sin_family = AF_INET;

        // Set up socket to use the host's IP address, whatever it is
        oServerAddressInfo.sin_addr.s_addr = INADDR_ANY;

        // Set up socket to use port 0 - will actually get some available port
        oServerAddressInfo.sin_port = htons(0);

        // Attempt to bind the socket to a port
        nServerAddressInfoLength = sizeof(oServerAddressInfo);
        nBindResult = bind(sServerSocketDescriptor, (struct sockaddr *) &oServerAddressInfo, nServerAddressInfoLength);
        if (nBindResult < 0) {
            printf("Task rank %d could not bind socket to port\n", nMyRank);
            fflush(stdout);
            bSuccess = 0;
        }

    }

    // Listen on server socket
    if (bSuccess != 0) {

        // Allow the process to listen on the socket for connections, with backlog queue size of 5
        listen(sServerSocketDescriptor, 5);

        /* Update server address info with actual port on which we are listening
         * https://stackoverflow.com/questions/4046616/sockets-how-to-find-out-what-port-and-address-im-assigned
         */
        getsockname(sServerSocketDescriptor, (struct sockaddr *) &oServerAddressInfo, &nServerAddressInfoLength);

    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        create_client_socket
 *
 * DESCRIPTION:     Create a client socket.
 *                  This function assumes that desired server socket is already listening.
 *                  Why do we assume this?
 *                  No node can write to the port list file before listening (if that node is a server).
 *                  No node can finish reading the port list file before all nodes have written to it.
 *                  Every node comes here (if they come here at all) after reading the port list file.
 *
 * ARGUMENTS:       nServerRank -   The task rank of the server to connect to
 *
 * RETURN:          bSuccess -      1 if everything seemed to go okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int create_client_socket (int nServerRank) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing create_client_socket\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int                 bSuccess;
    int                 nConnectionResult;
    int                 nMySocketDescriptor;
    int                 nAttemptsLeft;
    struct hostent*     oHostEntity;
    struct sockaddr_in  oHostAddressInfo;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Create socket using internet address domain, streaming, and most appropriate protocol
    if (bSuccess != 0) {
        nMySocketDescriptor = socket(AF_INET, SOCK_STREAM, 0);
        if (nMySocketDescriptor < 0) {
            printf("Task rank %d could not create client socket\n", nMyRank);
            fflush(stdout);
            bSuccess = 0;
        }
    }

    // Set up client socket to connect to a particular server (by server's task rank)
    if (bSuccess != 0) {
        oHostEntity = gethostbyname(asHostNames[nServerRank]);
        if (oHostEntity == NULL) {
            printf("Task rank %d could not find server at task rank %d using host name %s\n", nMyRank, nServerRank, asHostNames[nServerRank]);
            fflush(stdout);
            bSuccess = 0;
        }
    }

    // Set up server info for client socket to use
    if (bSuccess != 0) {

        // Set all server address info values to zero
        bzero((char *) &oHostAddressInfo, sizeof(oHostAddressInfo));

        // Set up socket to use default constant
        oHostAddressInfo.sin_family = AF_INET;

        // Set up socket to use correct server address
        bcopy((char *) oHostEntity->h_addr, (char *) &oHostAddressInfo.sin_addr.s_addr, oHostEntity->h_length);

        // Set up socket to use correct port number
        oHostAddressInfo.sin_port = htons(anPortNumbers[nServerRank]);

    }

    // Connect client socket to server socket
    if (bSuccess != 0) {
        nConnectionResult = connect(nMySocketDescriptor, (struct sockaddr *) &oHostAddressInfo, sizeof(oHostAddressInfo));
        if (nConnectionResult < 0) {
            printf("Task rank %d could not connect its client socket to the server socket at task rank %d using host name %s\n", nMyRank, nServerRank, asHostNames[nServerRank]);
            fflush(stdout);
            bSuccess = 0;
        }
    }

    // Save this socket in sockets array so we can talk to it later
    if (bSuccess != 0) {
        anSocketsToTalkTo[nServerRank] = nMySocketDescriptor;
    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        accept_client_connection
 *
 * DESCRIPTION:     Accept a client connection (as server)
 *                  Intended to be executed only be even-numbered task ranks
 *
 * ARGUMENTS:       None
 *
 * RETURN:          bSuccess -      1 if everything seemed to go okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int accept_client_connection () {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing accept_client_connection\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int                 bSuccess;
    int                 bFoundHostName;
    int                 i;
    int                 nClientSocketDescriptor;
    socklen_t           nClientLength;
    struct sockaddr_in  oClientAddress;
    char                sHostNameOfConnectedClient[NI_MAXHOST];
    char                sServiceName[NI_MAXSERV];
    char*               pPointerToCharInString;

    // Assume success until we know otherwise
    bSuccess = 1;

    /* Block until one client has connected
     *  accept() extracts the first connection on the queue of pending connections
     */
    if (bSuccess != 0) {
        nClientLength = sizeof(oClientAddress);
        nClientSocketDescriptor = accept(sServerSocketDescriptor, (struct sockaddr *) &oClientAddress, &nClientLength);
        if (nClientSocketDescriptor < 0) {
            printf("Task rank %d could not accept a new client connection\n", nMyRank);
            fflush(stdout);
            bSuccess = 0;
        }
    }

    /* Save client descriptor so that it can be found again by client task rank
     *  https://www.freebsd.org/cgi/man.cgi?query=getnameinfo&sektion=3&manpath=freebsd-release-ports
     */
    if (bSuccess != 0) {

        // Get the client's host name
        getnameinfo(
            (struct sockaddr *) &oClientAddress,
            nClientLength,
            sHostNameOfConnectedClient,
            sizeof(sHostNameOfConnectedClient),
            sServiceName,
            sizeof(sServiceName),
            0
        );

        /* Look for this name in our saved list of host names
         *  Host names are coming back e.g. "c45.localdomain" rather than just "c45"
         */
        pPointerToCharInString = strchr(sHostNameOfConnectedClient, '.');
        *pPointerToCharInString = '\0';
        bFoundHostName = 0;
        for (i = 0; i < nNumTasks; i++) {
            if (strcmp(sHostNameOfConnectedClient, asHostNames[i]) == 0) {
            bFoundHostName = 1;
                break;
            }
        }

        // Complain if it wasn't found
        if (bFoundHostName == 0) {
            printf("Task rank %d could not find the new client's host name %s\n", nMyRank, sHostNameOfConnectedClient);
            fflush(stdout);
            bSuccess = 0;
            i = -1;
        }

        // Save if it was found
        else {
            anSocketsToTalkTo[i] = nClientSocketDescriptor;
        }

    }

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d got a client connection from task rank %d (host name %s)\n", nMyRank, i, sHostNameOfConnectedClient);
        fflush(stdout);
    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        get_size_of_data_type
 *
 * DESCRIPTION:     Get the size of an MPI data type
 *
 * ARGUMENTS:       nDataType -     An indication of the MPI data type we are curious about
 *                                  Expected to match to MPI_BYTE or MPI_DOUBLE
 *
 * RETURN:          bSuccess -      1 if everything seemed to go okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
size_t get_size_of_data_type (int nDataType) {

    // Print debug info
    if (B_DEBUG >= 2) {
        printf("Task rank %d executing get_size_of_data_type\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    size_t nSizeOfDataType;

    // Get size
    switch (nDataType) {
        case MPI_BYTE:
            nSizeOfDataType = sizeof(char);
            break;
        case MPI_DOUBLE:
            nSizeOfDataType = sizeof(double);
            break;
        default:
            printf("Task rank %d could not get size of data type %d\n", nMyRank, nDataType);
            fflush(stdout);
            break;
    }

    // Return
    return nSizeOfDataType;

}

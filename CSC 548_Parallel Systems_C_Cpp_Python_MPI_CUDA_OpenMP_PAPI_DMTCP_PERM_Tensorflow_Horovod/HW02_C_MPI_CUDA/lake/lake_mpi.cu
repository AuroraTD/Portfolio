/*************************************************************************************************************
 * FILE:            lake_mpi.cu
 *
 * AUTHORS:	        attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *
 * DESCRIPTION:	    Model the surface of a lake, where some pebbles have been thrown onto the surface.
 *                  The energy level at any point on the lake is influenced by
 *                      the energy level on that point in the past,
 *                      and by the current energy levels at neighboring points.
 *                  This program takes into account all 8 neighboring points,
 *                      and parallelizes the simulation by using EXACTLY FOUR compute nodes,
 *                      each using multiple GPU threads.
 *
 * TO RUN:          srun -N4 -n4 -p opteron -x c[53,101,102] --pty /bin/bash
 *                  make -f p3.Makefile lake-mpi
 *                  prun ./lake [lake size] [# pebbles] [duration of simulation in seconds] [# GPU threads]
 *************************************************************************************************************/

// FUNCTION PROTOTYPES
int validate_inputs (int argc, char *argv[]);
int is_number (char sPossibleNumber[]);
void input_validation_error (const char *sMessage);
int allocate_memory ();
int scatter_info ();
int simulate_my_lake_section ();
int start_boundary_exchange ();
int finish_boundary_exchange ();
int update_time (double *nTime, double nTimeStep, double nFinishTime);
int gather_info ();
void report_completion (long int nStartTime_us, long int nEndTime_us);
void shut_down (int nshut_downType);
void initialize_pebbles (double *aPebbleSizes, int nPebbles, int nLakePointsOneAxis);
void initialize_energy (double *aEnergy, double *aPebbleSizes, int nLakePointsOneAxis, int bFullMap);
void print_heatmap (const char *sFilename, double *aEnergy, int bFullMap);
double get_pebble_impact (double nPebbleSize, double nTime);
extern void run_gpu(
    double *aEnergyStepOld,
    double *aEnergyStepCurrent,
    int nLakePointsOneAxis,
    int nNumTaskPointsWithBoundaries,
    double nPointSpacing,
    double nTime,
    int nThreads, int nMyRank,
	int nNumTasks,
	double *P
);


extern void gpu_memory_setup (int nNumTaskPointsWithBoundaries, double *aPebbleSizes);
extern void gpu_memory_free (void);
void report_buffer_double (double *aBuffer, int nNumValues);

// INCLUDES
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>
#include <unistd.h>
#include <ctype.h>
#include "mpi.h"

// DEFINES
#define N_ROOT_RANK                         0
#define N_TAG_NORTH_EAST                    1
#define N_TAG_NORTH                         2
#define N_TAG_NORTH_WEST                    3
#define N_TAG_WEST                          4
#define N_TAG_SOUTH_WEST                    5
#define N_TAG_SOUTH                         6
#define N_TAG_SOUTH_EAST                    7
#define N_TAG_EAST                          8
#define N_TAG_BOUNDARY_ABOVE_CUR            100
#define N_TAG_BOUNDARY_BELOW_CUR            101
#define N_BAD_INDEX                         -9999
#define N_US_IN_SEC                         1000000
#define XMIN                                0.0
#define XMAX                                1.0
#define YMIN                                0.0
#define YMAX                                1.0
#define MAX_PSZ                             10
#define TSCALE                              1.0
#define VSQR                                0.1
#define B_DEBUG                             0
#define _USE_MATH_DEFINES

// Hack to get around a problem I have in my IDE setup
#ifndef NULL
    #define NULL ((void *) 0)
#endif

// Declare globals

int     nMyRank;
int     nNumTasks;
int     nLakePointsOneAxis;
int     nNumTaskPoints;
int     nNumTaskPointsWithBoundaries;
int     nPebbles;
int     nThreads;

double  nFinishTime;
double  nLakePointSpacing;

double  *aLakePebbleSizes =         NULL;
double  *aLakeEnergyStep0 =         NULL;
double  *aLakeEnergyFinal =         NULL;

double  *aTaskPebbleSizes =         NULL;
double  *aTaskEnergyStepOld =       NULL;
double  *aTaskEnergyStepCurrent =   NULL;

double  *aSendBufferBoundaryAboveCur = NULL;
double  *aSendBufferBoundaryBelowCur = NULL;
double  *aRecvBufferBoundaryAboveCur = NULL;
double  *aRecvBufferBoundaryBelowCur = NULL;

MPI_Request nSendRequestBoundaryAboveCur;
MPI_Request nSendRequestBoundaryBelowCur;
MPI_Request nRecvRequestBoundaryAboveCur;
MPI_Request nRecvRequestBoundaryBelowCur;

/*************************************************************************************************************
 * FUNCTION:        main
 *
 * DESCRIPTION:     Participate the modeling of the surface of a lake,
 *                      where some pebbles have been thrown onto the surface.
 *                  This is one of several nodes participating.
 *                  This and other nodes further parallelize by performing work on multiple GPU threads.
 *
 * ARGUMENTS:       0 -         Lake size (number of points along one axis)
 *                  1 -         Number of pebbles
 *                  2 -         Duration of the simulation in seconds
 *                  3 -         Number of GPU threads along one axis of each thread block
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int main (int argc, char *argv[]) {

    // Declare variables
    int             bOkaySoFar;
    int             nResultMPI;
    long int        nStartTime_us;
    long int        nEndTime_us;
    struct timeval  oStartTime;
    struct timeval  oEndTime;

    // Assume success until we know otherwise
    bOkaySoFar = 1;

    // Initialize MPI
    nResultMPI = MPI_Init(&argc, &argv);
    bOkaySoFar = nResultMPI == MPI_SUCCESS ? 1 : 0;

    /// Get the number of tasks in the communicator
    if (bOkaySoFar == 1) {
        nResultMPI = MPI_Comm_size(MPI_COMM_WORLD, &nNumTasks);
        bOkaySoFar = nResultMPI == MPI_SUCCESS ? 1 : 0;
    }

    // Get my rank in the communicator
    if (bOkaySoFar == 1) {
        nResultMPI = MPI_Comm_rank(MPI_COMM_WORLD, &nMyRank);
        bOkaySoFar = nResultMPI == MPI_SUCCESS ? 1 : 0;
    }

    // Print debug info
    if (bOkaySoFar == 1 && B_DEBUG >= 1) {
        printf("Task rank %d executing main\n", nMyRank);
        fflush(stdout);
    }

    // Validate inputs (after getting comm size, because it matters for validation)
    if (bOkaySoFar == 1) {
        bOkaySoFar = validate_inputs(argc, argv);
    }

    // Save arguments
    if (bOkaySoFar == 1) {
        nLakePointsOneAxis    = atoi(argv[1]);
        nPebbles              = atoi(argv[2]);
        nFinishTime           = (double)atof(argv[3]);
        if (argc >= 5) {
            nThreads = atoi(argv[4]);
        }
        else {
            nThreads = -1;
        }
    }

    // Let the user know what scenario we are running
    if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {
        printf(
            "Running %s on %d nodes with %d threads, with (%d x %d) grid, with %d pebbles, until %f\n",
            argv[0],
            nNumTasks,
            nThreads,
            nLakePointsOneAxis,
            nLakePointsOneAxis,
            nPebbles,
            nFinishTime
        );
    }

    /* Check the wall clock
     * As soon as we know whether we are in charge of measuring time
     *  so that we can include as much as possible in our measurement.
     * Because we want to measure the total time to model the lake
     *  using parallel computation and message passing,
     *  we only want to measure / report this from the root task.
     */
    if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {
        gettimeofday(&oStartTime, NULL);
        nStartTime_us = oStartTime.tv_sec * N_US_IN_SEC + oStartTime.tv_usec;
    }

    // Allocate memory
    if (bOkaySoFar == 1) {
        bOkaySoFar = allocate_memory();
    }

    // Initialize pebbles so we can scatter the info to all tasks
    if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {
        initialize_pebbles(aLakePebbleSizes, nPebbles, nLakePointsOneAxis);
    }

    // Initialize lake energy (given pebble info) so we can print initial heat map
    if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {
        initialize_energy(aLakeEnergyStep0, aLakePebbleSizes, nLakePointsOneAxis, 1);
    }

    // Save lake point spacing before first attempt to print heat map
    if (bOkaySoFar == 1) {
        nLakePointSpacing = (XMAX - XMIN) / nLakePointsOneAxis;
    }

    // Print initial heat map of the entire lake
    if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {
        print_heatmap("lake_i.dat", aLakeEnergyStep0, 1);
    }

    // Barrier to make sure information is scattered when all tasks are ready
    if (bOkaySoFar == 1) {
        nResultMPI = MPI_Barrier(MPI_COMM_WORLD);
        bOkaySoFar = nResultMPI == MPI_SUCCESS ? 1 : 0;
    }

    // Scatter information to all tasks to get started
    if (bOkaySoFar == 1) {
        bOkaySoFar = scatter_info();
    }

    // Initialize energy in my section of the lake (given scattered info) so we can start simulation
    if (bOkaySoFar == 1) {
        initialize_energy(aTaskEnergyStepOld, aTaskPebbleSizes, nLakePointsOneAxis, 0);
        initialize_energy(aTaskEnergyStepCurrent, aTaskPebbleSizes, nLakePointsOneAxis, 0);
    }

    // Simulate the energy changes over time in my section of the lake
    if (bOkaySoFar == 1) {
        bOkaySoFar = simulate_my_lake_section();
    }

    // Barrier to make sure information is gathered when all tasks are ready
    if (bOkaySoFar == 1) {
        nResultMPI = MPI_Barrier(MPI_COMM_WORLD);
        bOkaySoFar = nResultMPI == MPI_SUCCESS ? 1 : 0;
    }

    // Gather information back from all tasks to finish up
    if (bOkaySoFar == 1) {
        bOkaySoFar = gather_info();
    }

    // Print final heat map of the entire lake
    if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {
        print_heatmap("lake_f.dat", aLakeEnergyFinal, 1);
    }

    // If ROOT, measure elapsed time and report completion
    if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {

        // Measure elapsed time
        gettimeofday(&oEndTime, NULL);
        nEndTime_us = oEndTime.tv_sec * N_US_IN_SEC + oEndTime.tv_usec;

        // Report completion to the user
        report_completion(nStartTime_us, nEndTime_us);

    }

    // Shut down
    shut_down(bOkaySoFar);

}

/*************************************************************************************************************
 * FUNCTION:        validate_inputs
 *
 * DESCRIPTION:     Ensure the required command line arguments are present.
 *                  Intended only to be executed by root task
 *
 * ARGUMENTS:       0 -         Lake size (number of points along one axis)
 *                  1 -         Number of pebbles
 *                  2 -         Duration of the simulation in seconds
 *                  3 -         Number of GPU threads along one axis of each thread block
 *                              OPTIONAL: "If GPU threads (nthreads) are specified from the command line,
 *                                          the GPU version should run, otherwise the CPU version"
 *
 * RETURNS:         bSuccess -  1 if inputs look okay, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora Tiffany-Davis
 *                  wpmoore2    Wade Moore
 *************************************************************************************************************/
int validate_inputs (int argc, char *argv[]) {

    // Declare variables
    int bSuccess;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Check arguments
    if (argc != 5) {
        input_validation_error("Incorrect number of arguments provided");
        bSuccess = 0;
    }
    else if (
        is_number(argv[1]) != 1 ||
        atoi(argv[1]) <= 0
    ) {
        input_validation_error("Lake size must be a positive number");
        bSuccess = 0;
    }
    else if (atoi(argv[1]) % nNumTasks != 0) {
        input_validation_error("Lake size must be evenly divisible by number of tasks");
        bSuccess = 0;
    }
    else if (
        is_number(argv[2]) != 1 ||
        atoi(argv[2]) <= 0
    ) {
        input_validation_error("Number of pebbles must be a positive number");
        bSuccess = 0;
    }
    else if (
        is_number(argv[3]) != 1 ||
        atoi(argv[3]) <= 0
    ) {
        input_validation_error("Duration of simulation must be a positive number");
        bSuccess = 0;
    }
    else if (
        is_number(argv[4]) != 1 ||
        atoi(argv[4]) <= 0
    ) {
        input_validation_error("Number of GPU threads along one axis of each thread block must be a positive number");
        bSuccess = 0;
    }

    // Return
    return bSuccess;
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
        if (!isdigit(sPossibleNumber[i]) && sPossibleNumber[i] != '.') {
            bNumber = 0;
            break;
        }
    }

    // Return
    return bNumber;

}

/*************************************************************************************************************
 * FUNCTION:        input_validation_error
 *
 * DESCRIPTION:     Complain about an input validation error
 *
 * ARGUMENTS:       sMessage -  A message about the error
 *
 * AUTHOR:          attiffan    Aurora Tiffany-Davis
 *                  wpmoore2    Wade Moore
 *************************************************************************************************************/
void input_validation_error (const char *sMessage) {

    printf("%s\n", sMessage);
    printf("\nUsage: prun ./lake-mpi [lake size] [# pebbles] [duration of simulation in seconds] [# GPU threads]\n");

}

/*************************************************************************************************************
 * FUNCTION:        allocate_memory
 *
 * DESCRIPTION:     Dynamically allocate memory
 *                  (what is needed depends upon task rank)
 *
 * ARGUMENTS:       None
 *
 * RETURN:          bSuccess - 1 if we succeeded, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int allocate_memory () {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing allocate_memory\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;
    int nNumLakePoints;

    int bExchgAbove = 0;
    int bExchgBelow = 0;

    // Assume success until we know otherwise
    bSuccess = 1;

    /* Figure out who my boundary exchange partners are
     * We assume 4 nodes and the code is NOT expected to work properly with any other number
     * The lake is split up among nodes numbered 0 - 3 as follows:
     *  3
     *  2
     *  1
     *  0
     * If we "exchange above" this means that we are the "above" partner in an exchange
     *  Rank 0 "exchanges below" since it is the "below" partner in a boundary exchange with rank 1
     *  Rank 3 "exchanges above" since it is the "above" partner in a boundary exchange with rank 2
     */
    if (bSuccess == 1) {
        switch (nMyRank) {
            case 0:
                // Send/recv below
                bExchgBelow = 1;
                break;
            case 1:
                // Send/recv above && Send/recv below
                bExchgBelow = 1;
                bExchgAbove = 1;
                break;
            case 2:
                // Send/recv above && Send/recv below
                bExchgBelow = 1;
                bExchgAbove = 1;
                break;
            case 3:
                // Send/recv above
                bExchgAbove = 1;
                break;
            default:
                printf("Task rank %d is not a valid task rank\n", nMyRank);
                bSuccess = 0;
                break;
        }
    }

    // If ROOT, allocate memory for the entire lake
    if (nMyRank == N_ROOT_RANK) {

        // Calculate number of lake points
        nNumLakePoints = nLakePointsOneAxis * nLakePointsOneAxis;

        // Allocate lake-level memory
        if (bSuccess == 1) {
            aLakePebbleSizes = (double*) malloc(sizeof(double) * nNumLakePoints);
            if (aLakePebbleSizes == NULL) {
                printf("Task rank %d could not allocate memory for lake pebble sizes\n", nMyRank);
                bSuccess = 0;
            }
        }
        if (bSuccess == 1) {
            aLakeEnergyStep0 = (double*) malloc(sizeof(double) * nNumLakePoints);
            if (aLakeEnergyStep0 == NULL) {
                printf("Task rank %d could not allocate memory for lake energy (step 0)\n", nMyRank);
                bSuccess = 0;
            }
        }
        if (bSuccess == 1) {
            aLakeEnergyFinal = (double*) malloc(sizeof(double) * nNumLakePoints);
            if (aLakeEnergyFinal == NULL) {
                printf("Task rank %d could not allocate memory for lake energy (final step)\n", nMyRank);
                bSuccess = 0;
            }
        }

    }

    /* Allocate task-level memory
     *  Each node needs enough memory for the energy levels in its own slice of the lake,
     *      plus an extra line at the bottom and the top,
     *      in which to place boundary information received from another node
     *  A node does not need to know about pebbles in the boundaries
     *      touching its slice of the lake,
     *      however pebble array is sized the same as energy array
     *      to make it easier to work with
     */
    nNumTaskPoints = (nLakePointsOneAxis / nNumTasks) * nLakePointsOneAxis;
    nNumTaskPointsWithBoundaries = nNumTaskPoints + (2 * nLakePointsOneAxis);

    if (bSuccess == 1) {
        aTaskPebbleSizes = (double*) malloc(sizeof(double) * nNumTaskPointsWithBoundaries);
        if (aTaskPebbleSizes == NULL) {
            printf("Task rank %d could not allocate memory for task pebble sizes\n", nMyRank);
            bSuccess = 0;
        }
    }
    if (bSuccess == 1) {
        aTaskEnergyStepOld = (double*) malloc(sizeof(double) * nNumTaskPointsWithBoundaries);
        if (aTaskEnergyStepOld == NULL) {
            printf("Task rank %d could not allocate memory for task energy (CPU step old)\n", nMyRank);
            bSuccess = 0;
        }
    }
    if (bSuccess == 1) {
        aTaskEnergyStepCurrent = (double*) malloc(sizeof(double) * nNumTaskPointsWithBoundaries);
        if (aTaskEnergyStepCurrent == NULL) {
            printf("Task rank %d could not allocate memory for task energy (CPU step current)\n", nMyRank);
            bSuccess = 0;
        }
    }

    // Allocate boundary exchange memory

    // ABOVE (we are the "above" partner in a boundary exchange)
    if (bExchgAbove == 1) {

        if (bSuccess == 1) {
          // CURRENT
          // Send buffer
          aSendBufferBoundaryAboveCur = (double*) malloc(sizeof(double) * nLakePointsOneAxis);
          if (aSendBufferBoundaryAboveCur == NULL) {
              printf("Task rank %d could not allocate memory for boundary exchange current above send buffer\n", nMyRank);
              bSuccess = 0;
          }
          // Recv buffer
          aRecvBufferBoundaryAboveCur = (double*) malloc(sizeof(double) * nLakePointsOneAxis);
          if (aRecvBufferBoundaryAboveCur == NULL) {
              printf("Task rank %d could not allocate memory for boundary exchange for current recv buffer\n", nMyRank);
              bSuccess = 0;
          }
       }
    }
    // BELOW (we are the "below" partner in a boundary exchange)
    if (bExchgBelow == 1) {
        // CURRENT
        // Send buffer
        if (bSuccess == 1) {
          aSendBufferBoundaryBelowCur = (double*) malloc(sizeof(double) * nLakePointsOneAxis);
          if (aSendBufferBoundaryBelowCur == NULL) {
            printf("Task rank %d could not allocate memory for boundary exchange for current send buffer\n", nMyRank);
            bSuccess = 0;
          }
        }
        // Recv buffer
        if (bSuccess == 1) {
          aRecvBufferBoundaryBelowCur = (double*) malloc(sizeof(double) * nLakePointsOneAxis);
          if (aRecvBufferBoundaryBelowCur == NULL) {
            printf("Task rank %d could not allocate memory for boundary exchange for current recv buffer\n", nMyRank);
            bSuccess = 0;
          }
        }
    }

    /* Allocate GPU memory
     * GPU needs to know about the pebbles in this node's slice of the lake.
     * GPU does not need to know about pebbles just outside of this slice of the lake,
     *  however pebble array is sized the same as energy arrays,
     *  to make it easier to work with.
     */
    if (bSuccess == 1) {
        gpu_memory_setup(nNumTaskPointsWithBoundaries, aTaskPebbleSizes);
    }

    // Return indication of success / failure
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        scatter_info
 *
 * DESCRIPTION:     Scatter information out from the root to all tasks to get started
 *
 * ARGUMENTS:       None
 *
 * RETURNS:         bSuccess -      1 if everything seems okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int scatter_info () {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing scatter_info\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;
    int nResultMPI;

    // Assume success until we know otherwise
    bSuccess = 1;

    /* Scatter information out from the root to all tasks to get started
     *  Each task needs to know where the pebbles are in its section of the lake,
     *      and does not need to know about any pebbles in the boundaries
     *      that touch its section of the lake.
     *  However, pebble array is sized the same as energy arrays,
     *      to make it easier to work with.
     *  Energy levels in its section of the lake at time 0 and 1
     *      can then be calculated independently by each task.
     *  Scatter distributes the elements in the order of process rank.
     */
    nResultMPI = MPI_Scatter(
        // Start address of send buffer
        (void *) aLakePebbleSizes,
        // Send count
        nNumTaskPoints,
        // Send type
        MPI_DOUBLE,
        // Start address of receive buffer
        (void *) (aTaskPebbleSizes + nLakePointsOneAxis),
        // Receive count
        nNumTaskPoints,
        // Receive type
        MPI_DOUBLE,
        // Root
        N_ROOT_RANK,
        // Communicator
        MPI_COMM_WORLD
    );
    bSuccess = nResultMPI == MPI_SUCCESS ? 1 : 0;

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        simulate_my_lake_section
 *
 * DESCRIPTION:     Simulate the energy changes over time in my section of the lake
 *
 * ARGUMENTS:       None
 *
 * RETURNS:         bSuccess -      1 if everything seems okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int simulate_my_lake_section () {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing simulate_my_lake_section\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int     bSuccess;
    int     bKeepGoing;
    double  nTime;
    double  nTimeStep;
    char    sFileName[10];

    // Assume success until we know otherwise
    bSuccess = 1;

    // Print initial heat map of my section of the lake
    if (bSuccess == 1) {
        sprintf(sFileName, "lake_i_%d.dat", nMyRank);
        print_heatmap(sFileName, aTaskEnergyStepCurrent, 0);
    }

    // Set initial values for the loop
    nTime =             0.;
    nTimeStep =         nLakePointSpacing / 2.;
    bKeepGoing =        1;

    // Loop through the duration of the simulation
    while (bKeepGoing == 1) {

        // Start boundary exchange (non-blocking)
        if (bSuccess == 1) {
            bSuccess = start_boundary_exchange();
        }

        // Finish boundary exchange (wait for sends and receives to finish)
        if (bSuccess == 1) {
            bSuccess = finish_boundary_exchange();
        }
        /* Simulate the energy changes over one time step in my section of the lake
         *  GPU mode is the only mode that is supported
         *  Each node should communicate boundary information to the appropriate neighbor,
         *  then run the CUDA kernel during a time-step
         *  (one iteration of evolve)
         */

        if (bSuccess == 1) {
            /* Let the GPU update the lake energies for this node's section of the lake
             *  GPU will copy information from CPU to GPU, update values, and copy back to CPU "current" buffer
             *  GPU needs to know how much memory to allocate,
             *      and this must be enough to store
             *      info about this node's slice of the lake,
             *      as well as the boundaries right next to this slice
             */
            run_gpu(
                aTaskEnergyStepOld,
                aTaskEnergyStepCurrent,
                nLakePointsOneAxis,
                nNumTaskPointsWithBoundaries,
                nLakePointSpacing,
                nTime,
                nThreads,
				nMyRank,
				nNumTasks,
				aTaskPebbleSizes
            );

        }

        // Update the simulation time counter
        if (bSuccess == 1) {
            bKeepGoing = update_time(&nTime, nTimeStep, nFinishTime);
        }
    }

    // Free GPU memory
	gpu_memory_free();

    // Print final heat map of my section of the lake
    if (bSuccess == 1) {
        sprintf(sFileName, "lake_f_%d.dat", nMyRank);
        print_heatmap(sFileName, aTaskEnergyStepCurrent, 0);
    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        start_boundary_exchange
 *
 * DESCRIPTION:     Start boundary exchanges between tasks for one time step
 *                  Lake is split into exactly 4 quadrants each owned by one compute node
 *                  They are organized in slices.
 *                  Rank 3
 *                      Boundary exchange below with rank 2
 *                  Rank 2
 *                      Boundary exchange above with rank 3
 *                      Boundary exchange below with rank 1
 *                  Rank 1
 *                      Boundary exchange above with rank 2
 *                      Boundary exchange below with rank 0
 *                  Rank 0
 *                      Boundary exchange above with rank 1
 *
 * ARGUMENTS:       None
 *
 * RETURNS:         bSuccess -      1 if everything seems okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int start_boundary_exchange () {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing start_boundary_exchange\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;
    int nResultMPI;
    int bExchgAbove = 0;
    int bExchgBelow = 0;

    // Assume success until we know otherwise
    bSuccess = 1;

    /* Figure out who my boundary exchange partners are
     * We assume 4 nodes and the code is NOT expected to work properly with any other number
     * The lake is split up among nodes numbered 0 - 3 as follows:
     *  3
     *  2
     *  1
     *  0
     * If we "exchange above" this means that we are the "above" partner in an exchange
     *  Rank 0 "exchanges below" since it is the "below" partner in a boundary exchange with rank 1
     *  Rank 3 "exchanges above" since it is the "above" partner in a boundary exchange with rank 2
     */
    if (bSuccess == 1) {
        bExchgBelow = nMyRank < 3 ? 1 : 0;
        bExchgAbove = nMyRank > 0 ? 1 : 0;
    }

    // Perform non-blocking sends from send buffers

    // ABOVE BOUNDARY SEND (we are the "above" partner in a boundary exchange)
    if (bSuccess == 1) {
        if (bExchgAbove == 1) {

            // Fill send boundary buffer with our bottom row skipping our own boundary exchange space
            for (int i = 0; i < nLakePointsOneAxis; i++) {
                aSendBufferBoundaryAboveCur[i] = aTaskEnergyStepCurrent[nLakePointsOneAxis + i];
            }

            if (bSuccess == 1) {
                nResultMPI = MPI_Isend(
                    // Send buffer
                    aSendBufferBoundaryAboveCur,
                    // Send count
                    nLakePointsOneAxis,
                    // Send data type
                    MPI_DOUBLE,
                    // Destination task (one above - zero is bottom)
                    nMyRank - 1,
                    // Tag (named from point of view of SENDER)
                    N_TAG_BOUNDARY_ABOVE_CUR,
                    // Communicator
                    MPI_COMM_WORLD,
                    // Pointer to request
                    &nSendRequestBoundaryAboveCur
                );
                bSuccess = nResultMPI == MPI_SUCCESS ? 1 : 0;
            }
		}
    }

    // BELOW BOUNDARY SEND (we are the "below" partner in a boundary exchange)
	 if (bSuccess == 1) {
        if (bExchgBelow == 1) {
            // Fill send boundary buffer with our top row skipping our own boundary exchange space
            for (int i = 0; i < nLakePointsOneAxis; i++) {
                aSendBufferBoundaryBelowCur[i] = aTaskEnergyStepCurrent[i + nNumTaskPoints];
            }

            if (bSuccess == 1) {
                nResultMPI = MPI_Isend(
                    // Send buffer
                    aSendBufferBoundaryBelowCur,
                    // Send count
                    nLakePointsOneAxis,
                    // Send data type
                    MPI_DOUBLE,
                    // Destination task (one below - zero is bottom)
                    nMyRank + 1,
                    // Tag (named from point of view of SENDER)
                    N_TAG_BOUNDARY_BELOW_CUR,
                    // Communicator
                    MPI_COMM_WORLD,
                    // Pointer to request
                    &nSendRequestBoundaryBelowCur
                );
                bSuccess = nResultMPI == MPI_SUCCESS ? 1 : 0;
            }
		}
	}

	 // ABOVE BOUNDARY RECEIVE (we are the "above" partner in a boundary exchange)
	if (bSuccess == 1) {
		if(bExchgAbove) {
		// Recv boundary (above)
			nResultMPI = MPI_Irecv(
                    // Receive buffer
                    aRecvBufferBoundaryAboveCur,
                    // Receive count
                    nLakePointsOneAxis,
                    // Receive data type
                    MPI_DOUBLE,
                    // Source task (one above - zero is bottom)
                    nMyRank - 1,
                    // Tag (named from point of view of SENDER)
                    N_TAG_BOUNDARY_BELOW_CUR,
                    // Communicator
                    MPI_COMM_WORLD,
                    // Pointer to request
                    &nRecvRequestBoundaryAboveCur
                );
                bSuccess = nResultMPI == MPI_SUCCESS ? 1 : 0;
		}
	}

	// BELOW BOUNDARY RECEIVE (we are the "below" partner in a boundary exchange)
	if (bSuccess == 1) {
		if(bExchgBelow) {
                // Recv boundary (below)
                nResultMPI = MPI_Irecv(
                    // Receive buffer
                    aRecvBufferBoundaryBelowCur,
                    // Receive count
                    nLakePointsOneAxis,
                    // Receive data type
                    MPI_DOUBLE,
                    // Source task (one below - zero is bottom)
                    nMyRank + 1,
                    // Tag (named from point of view of SENDER)
                    N_TAG_BOUNDARY_ABOVE_CUR,
                    // Communicator
                    MPI_COMM_WORLD,
                    // Pointer to request
                    &nRecvRequestBoundaryBelowCur
                );
                bSuccess = nResultMPI == MPI_SUCCESS ? 1 : 0;
            
        }
    }
    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        finish_boundary_exchange
 *
 * DESCRIPTION:     Finish boundary exchanges between tasks for one time step
 *
 * ARGUMENTS:       None
 *
 * RETURNS:         bSuccess -      1 if everything seems okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int finish_boundary_exchange () {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing finish_boundary_exchange\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int         bSuccess;
    int         nResultMPI;
    int         nRequestCount;
    int         bExchgAbove;
    int         bExchgBelow;
    int         nBufferOffset;
    int         nRequestIndex;
    MPI_Request aRequests[4];
    MPI_Status  aStatuses[4];

    // Assume success until we know otherwise
    bSuccess = 1;

    /* Figure out who my boundary exchange partners are
     * We assume 4 nodes and the code is NOT expected to work properly with any other number
     * The lake is split up among nodes numbered 0 - 3 as follows:
     *  3
     *  2
     *  1
     *  0
     * If we "exchange above" this means that we are the "above" partner in an exchange
     *  Rank 0 "exchanges below" since it is the "below" partner in a boundary exchange with rank 1
     *  Rank 3 "exchanges above" since it is the "above" partner in a boundary exchange with rank 2
     */
    if (bSuccess == 1) {
        bExchgBelow = nMyRank < 3 ? 1 : 0;
        bExchgAbove = nMyRank > 0 ? 1 : 0;
    }

    // Set up requests
    if (bSuccess == 1) {
        nRequestIndex = 0;
        if (bExchgAbove == 1) {
            aRequests[nRequestIndex] = nSendRequestBoundaryAboveCur;
            nRequestIndex++;
            aRequests[nRequestIndex] = nRecvRequestBoundaryAboveCur;
            nRequestIndex++;
        }
        if (bExchgBelow == 1) {
            aRequests[nRequestIndex] = nSendRequestBoundaryBelowCur;
            nRequestIndex++;
            aRequests[nRequestIndex] = nRecvRequestBoundaryBelowCur;
            nRequestIndex++;
        }
        nRequestCount = nRequestIndex;
    }

    // Wait for all non-blocking sends and receives to complete
    if (bSuccess == 1) {
        nResultMPI = MPI_Waitall(nRequestCount, aRequests, aStatuses);
        bSuccess = nResultMPI == MPI_SUCCESS ? 1 : 0;
    }

    // Copy Receive buffers into task's current energy buffer
    if (bSuccess == 1) {
        /* Use the info that we got as the "above" partner in a boundary exchange
         *  We are copying into an over-sized buffer
         *      meant to hold this boundary exchange info.
         *  The first point we copy over needs to offset - not at all!
         */
        if (bExchgAbove == 1) {
            nBufferOffset = 0;
            for (int i = 0; i < nLakePointsOneAxis; i++) {
                aTaskEnergyStepCurrent[nBufferOffset + i] = aRecvBufferBoundaryAboveCur[i];
            }
        }
        /* Use the info that we got as the "below" partner in a boundary exchange
         *  We are copying into an over-sized buffer
         *      meant to hold this boundary exchange info.
         *  The first point we copy over needs to offset past
         *      the first (below) boundary exchange space,
         *      and also past the space established for this node's slice of the lake.
         */
        if (bExchgBelow == 1) {
            nBufferOffset = nNumTaskPoints + nLakePointsOneAxis;
            for (int i = 0; i < nLakePointsOneAxis; i++) {
                aTaskEnergyStepCurrent[i + nBufferOffset] = aRecvBufferBoundaryBelowCur[i];
            }
        }
    }

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d leaving finish_boundary_exchange\n", nMyRank);
        fflush(stdout);
    }

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        update_time
 *
 * DESCRIPTION:     Update the time counter during the simulation
 *
 * ARGUMENTS:       nTime -         The amount of time that has elapsed in the simulation
 *                  nTimeStep -     The amount of time between one simulation step and the next
 *                  nFinishTime -   The total intended duration of the simulation
 *
 * RETURNS:         bKeepGoing -    1 if we should keep going, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int update_time (double *nTime, double nTimeStep, double nFinishTime) {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing update_time\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bKeepGoing;

    // Update the time counter
    if((*nTime) + nTimeStep > nFinishTime) {
        bKeepGoing = 0;
    }
    else {
        (*nTime) = (*nTime) + nTimeStep;
        bKeepGoing = 1;
    }

    // Return
    return bKeepGoing;

}

/*************************************************************************************************************
 * FUNCTION:        gather_info
 *
 * DESCRIPTION:     Gather information back to the root from all tasks to finish up
 *
 * ARGUMENTS:       None
 *
 * RETURNS:         bSuccess -      1 if everything seems okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int gather_info () {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing gather_info\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;
    int nResultMPI;

    // Assume success until we know otherwise
    bSuccess = 1;

    /* Gather information back to the root from all tasks to finish up
     *  The root needs to know the final energy levels in every point of the lake.
     *  Each task is responsible for a separate section of the lake,
     *      and we gather back only each node's own section
     *  We are careful to exclude the extra memory space allocated on each node
     *      to store boundary exchange values,
     *      so we offset the pointer to our energy array,
     *      doing the arithmetic before casting to the required void pointer.
     *  Elements are ordered in the receive buffer
     *      by the rank of the process from which they were received.
     *  Only the root process needs to have a valid receive buffer.
     */
    nResultMPI = MPI_Gather(
        // Start address of send buffer
        (void *) (aTaskEnergyStepCurrent + nLakePointsOneAxis),
        // Send count
        nNumTaskPoints,
        // Send type
        MPI_DOUBLE,
        // Start address of receive buffer
        (void *) aLakeEnergyFinal,
        // Receive count
        nNumTaskPoints,
        // Receive type
        MPI_DOUBLE,
        // Root
        N_ROOT_RANK,
        // Communicator
        MPI_COMM_WORLD
    );
    bSuccess = nResultMPI == MPI_SUCCESS ? 1 : 0;

    // Return
    return bSuccess;

}

/*************************************************************************************************************
 * FUNCTION:        report_completion
 *
 * DESCRIPTION:     Report the completion of the entire job
 *                  Intention is to call this only from the root rank
 *
 * ARGUMENTS:       nStartTime_ms - Start time for the entire job, in microseconds
 *                  nEndTime_ms -   End time for the entire job, in microseconds
 *
 * RETURNS:         None
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
void report_completion (long int nStartTime_us, long int nEndTime_us) {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing report_completion\n", nMyRank);
        fflush(stdout);
    }

    // Report results
    printf("\nLake size (points along one axis): %d\n", nLakePointsOneAxis);
    printf("Number of pebbles: %d\n", nPebbles);
    printf("Duration of simulation (s): %f\n", nFinishTime);
    printf("Number of GPU threads along one axis of each thread block: %d\n", nThreads);
    printf("Total job run time (s): %f\n\n", ((double) (nEndTime_us - nStartTime_us) / N_US_IN_SEC));

}

/*************************************************************************************************************
 * FUNCTION:        shut_down
 *
 * DESCRIPTION:     Shut down (free dynamically allocated memory, finalize MPI)
 *
 * ARGUMENTS:       bOkaySoFar - 1 if everything seems okay, 0 otherwise
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
void shut_down (int bOkaySoFar) {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing shut_down\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int bSuccess;
    int nResultMPI;

    // Wait until all tasks get here (helps with debug)
    nResultMPI = MPI_Barrier(MPI_COMM_WORLD);
    bSuccess = (bOkaySoFar == 1 && nResultMPI == MPI_SUCCESS) ? 1 : 0;

    // Free lake-level allocated memory
    if (aLakePebbleSizes != NULL) {
        free(aLakePebbleSizes);
    }
    if (aLakeEnergyStep0 != NULL) {
        free(aLakeEnergyStep0);
    }
    if (aLakeEnergyFinal != NULL) {
        free(aLakeEnergyFinal);
    }

    // Free task-level allocated memory
    if (aTaskPebbleSizes != NULL) {
        free(aTaskPebbleSizes);
    }
    if (aTaskEnergyStepOld != NULL) {
        free(aTaskEnergyStepOld);
    }
    if (aTaskEnergyStepCurrent != NULL) {
        free(aTaskEnergyStepCurrent);
    }

    // Free boundary exchange buffers
    if (aSendBufferBoundaryAboveCur != NULL) {
        free(aSendBufferBoundaryAboveCur);
    }
    if (aSendBufferBoundaryBelowCur != NULL) {
        free(aSendBufferBoundaryBelowCur);
    }
    if (aRecvBufferBoundaryAboveCur != NULL) {
        free(aRecvBufferBoundaryAboveCur);
    }
    if (aRecvBufferBoundaryBelowCur != NULL) {
        free(aRecvBufferBoundaryBelowCur);
    }

    // Free GPU memory
    gpu_memory_free();

    // Finalize MPI
    MPI_Finalize();

    // Exit
    exit(bSuccess == 1 ? 0 : 1);

}

/*************************************************************************************************************
 * FUNCTION:        initialize_pebbles
 *
 * DESCRIPTION:     Randomly distribute the specified number of pebbles in the lake, giving each a random size
 *
 * ARGUMENTS:       aPebbleSizes -          Array representing the pebble sizes at every point in the lake (sparse)
 *                  nPebbles -              The number of pebbles to drop into the lake
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *
 * RETURNS:         None
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void initialize_pebbles(double *aPebbleSizes, int nPebbles, int nLakePointsOneAxis)
{

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing initialize_pebbles\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int i, j, k, nIndex;
    int nPebbleSize;

    // Initialize
    srand( time(NULL) );
    memset(aPebbleSizes, 0, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);

    // Loop through all pebbles, distributing them randomly into the lake
    for( k = 0; k < nPebbles ; k++ )
    {
        i = rand() % (nLakePointsOneAxis - 4) + 2;
        j = rand() % (nLakePointsOneAxis - 4) + 2;
        nPebbleSize = rand() % MAX_PSZ;
        nIndex = j + i * nLakePointsOneAxis;
        aPebbleSizes[nIndex] = (double) nPebbleSize;
    }

}

/*************************************************************************************************************
 * FUNCTION:        get_pebble_impact
 *
 * DESCRIPTION:     Get the energy impact of a given pebble size on the lake based on time.
 *                  Impact decreases as time increases.
 *
 * ARGUMENTS:       nPebbleSize -   The size of a given pebble
 *                  nTime -         The amount of time that has elapsed in the simulation
 *
 * RETURNS:         (unnamed) -     The energy impact of the specified pebble size given the current time
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
double get_pebble_impact(double nPebbleSize, double nTime)
{
  return -expf(-TSCALE * nTime) * nPebbleSize;
}

/*************************************************************************************************************
 * FUNCTION:        initialize_energy
 *
 * DESCRIPTION:     Initialize the energy levels in the lake based solely upon initial drops of pebbles
 *
 * ARGUMENTS:       aEnergy -               Array representing energy levels at every point in the lake
 *                  aPebbleSizes -          Array representing the pebble sizes at every point in the lake (sparse)
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *                  bFullMap -              1 for the entire lake, 0 for this task's slice of the lake
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void initialize_energy(double *aEnergy, double *aPebbleSizes, int nLakePointsOneAxis, int bFullMap)
{

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing initialize_energy\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int nY;
    int nX;
    int nIndex;
    int nMinY;
    int nMaxY;

    // Determine maximum y axis value
    if (bFullMap == 1) {
        // Iterate over the entire lake
        nMinY = 0;
        nMaxY = nLakePointsOneAxis;
    }
    else {
        // Iterate over this task's section of the lake (ignoring boundary exchange space)
        nMinY = 1;
        nMaxY = (nLakePointsOneAxis / nNumTasks) + 1;
    }

    // Loop through both axes of the lake, initializing energy levels at each point
    for(nY = nMinY; nY < nMaxY ; nY++)
    {
        for(nX = 0; nX < nLakePointsOneAxis ; nX++)
        {
            nIndex = nX + nY * nLakePointsOneAxis;
            aEnergy[nIndex] = get_pebble_impact(aPebbleSizes[nIndex], 0.0);
        }
    }
}

/*************************************************************************************************************
 * FUNCTION:        print_heatmap
 *
 * DESCRIPTION:     Print (to file) a heat map showing the energy levels in the lake,
 *                  or in some portion of the lake.
 *
 * ARGUMENTS:       sFilename -     The name of the heat map file to write
 *                  aEnergy -       Array representing energy levels at every point in the lake
 *                  bFullMap -      True (one) if the entire 2d map, false (zero) if a single tasks
                                    space (which includes a upper and lower bound - must exclude)
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void print_heatmap (const char *sFilename, double *aEnergy, int bFullMap) {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing print_heatmap\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int i, ii, j, nIndex;
    int imax;
    int jmax;

    // Initialize variables
    if (bFullMap == 1) {
        i = 0;
        j = 0;
        imax = nLakePointsOneAxis;
        jmax = nLakePointsOneAxis;
    } else {
        // Task map has outer bounds included
        i = 1;
        j = 0;
        imax = nLakePointsOneAxis / nNumTasks;
        jmax = nLakePointsOneAxis;
    }

    // Open file
    FILE *fp = fopen(sFilename, "w");


    // Work your way up the vertical axis
    for(ii = 0; ii < imax; ii++, i++ )
    {
        // Work your way across the horizontal axis
        for( j = 0; j < jmax; j++ )
        {
            nIndex = j + i * nLakePointsOneAxis;
            // Print: x, y, energy level
            fprintf(fp, "%f %f %f\n", j*nLakePointSpacing, ii*nLakePointSpacing, aEnergy[nIndex]);
        }
    }

    // Close file
    fclose(fp);

}

/*************************************************************************************************************
 * FUNCTION:        report_buffer_double
 *
 * DESCRIPTION:     Print (to terminal) the values in a buffer of double values - used for debug
 *
 * ARGUMENTS:       aBuffer -       Pointer to the buffer
 *                  nNumValues -    The number of values to print
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
void report_buffer_double (double *aBuffer, int nNumValues) {

    // Print debug info
    if (B_DEBUG >= 1) {
        printf("Task rank %d executing report_buffer_double\n", nMyRank);
        fflush(stdout);
    }

    // Declare variables
    int i;

    // Print
    for(i = 0; i < nNumValues; i++ ) {
        printf("%f\n", aBuffer[i]);
        fflush(stdout);
    }

}

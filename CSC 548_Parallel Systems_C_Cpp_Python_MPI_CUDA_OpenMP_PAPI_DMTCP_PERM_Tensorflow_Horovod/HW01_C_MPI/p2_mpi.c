/*************************************************************************************************************
 * FILE:            p2_mpi.c
 *
 * AUTHORS:	        attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *
 * DESCRIPTION:	    An MPI program that differentiates and plots a function.
 *
 * TO RUN:          srun -N[# nodes] -n[# tasks] -p opteron --pty /bin/bash
 *                  make -f p2.makefile
 *                  prun ./p2 [arguments - see function comments for "main" function]
 *************************************************************************************************************/

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

// FUNCTION PROTOTYPES (not permitted to submit *.h file)
typedef double (*fn_equation)(double);
double fn(double x); // Name comes directly from https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw1/p2_func.c
int verifyInputs(int argc, char *argv[]);
int inputValidationError(char *msg);
int decomposeGrid(int nNumGridPoints, int nNumTasks, int nMyRank);
double mpi_calc_derivative(int idx);
double getMostUpperFxBound();
double getMostLowerFxBound();
int mpi_exchange_boundary_fx_nonblocking(int rank, int numproc,
                                          double sendLower, double sendUpper,
                                          double *recvLower, double *recvUpper, MPI_Request *reqs);
int gatherResults(int nSendRecvType, int nGatherType, int nMyRank);
void mpi_exchange_boundary_fx_blocking(int, int, double, double, double *, double *);
int gatherMPI(int nMessageTag);
int gatherManualSync(int nMessageTag);
int gatherManualAsyncStart(int nMessageTag);
int gatherManualAsyncFinish(int nMessageTag);
void reportCompletion(char * sFileName, long int nStartTime_us, long int nEndTime_us);
void reportBufferContentsAll();
void reportBufferContentsOne();
void shutDown(int nShutDownType);
#ifdef TEST
extern double fn1(double);
extern double fn2(double);
extern double fn3(double);
extern double fn4(double);
extern double fn5(double);
extern double fn6(double);
extern double fn7(double);
extern double fn8(double);
#endif

// Hack to get around a problem I have in my IDE setup
#ifndef NULL
#define NULL   ((void *) 0)
#endif

// DEFINES
#define N_ROOT_RANK             0
#define N_GRID_POINT_INITIAL    1
#define N_GRID_POINT_FINAL      100
#define N_TAG_X                 1
#define N_TAG_Y                 2
#define N_TAG_DY                3
#define NON_BLOCK_EXCHG_CODE    343
#define MAX_NUM_FUNC            8

// Declare globals

int	   	nNumTasks;
int     nMyRank;
int 	nNumGridPoints;
int     nMyNumGridPoints;
int     nStrategySendRecv;
int     nStrategyGather;
double  nStepSize;

#ifdef TEST
int	nFunctionIndex;;
#endif

double* pPointerToBufferX =         NULL;
double* pPointerToBufferY =         NULL;
double* pPointerToBufferDY =        NULL;
double* pPointerToBufferX_Final =   NULL;
double* pPointerToBufferY_Final =   NULL;
double* pPointerToBufferDY_Final =  NULL;

int*    pPointerToResultCounts =        NULL;
int*    pPointerToResultDisplacements = NULL;

#ifdef TEST
fn_equation funcp[MAX_NUM_FUNC] = {fn1, fn2, fn3, fn4, fn5, fn6, fn7, fn8};
fn_equation fnp;
#endif

MPI_Request*    pPointerToRequests =    NULL;
MPI_Request     oSingleRequest;

/******************************************************************************
 * FUNCTION:        main
 *
 * DESCRIPTION:	    Approximate the derivative function for an input function
 *                      over a set range with a given level of resolution (# slices).
 *                  Do this using parallel computation and message passing.
 *                  Support multiple strategies for message passing.
 *
 * ARGUMENTS:       0 -         Number of grid points a.k.a. number of slices
 *                  1 -         Setting for point-to-point communication strategy
 *                              0 for blocking
 *                              1 for non-blocking
 *                  2 -         Setting for results gathering strategy
 *                              0 for MPI_Gather
 *                              1 for manual gather with blocking / non-blocking
 *                              as defined in previous argument
 *                  3 -         Function index (1 = x^2, ...)
 *                              This is ONLY relevant with p2.makefile.test
 *                              (NOT relevant for the grader)
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 ******************************************************************************/
int main (int argc, char *argv[]) {

    // Declare variables

    int             bOkaySoFar, i, itr;
    long int        nStartTime_us, nEndTime_us;
    struct timeval  oStartTime, oEndTime;
    char            sFileName[1024];
    FILE*           fp;

    // Initialize MPI
  	MPI_Init(&argc, &argv);

  	/// Get the number of tasks in the communicator
  	MPI_Comm_size(MPI_COMM_WORLD, &nNumTasks);

  	// Get my rank in the communicator
  	MPI_Comm_rank(MPI_COMM_WORLD, &nMyRank);

    // Only need to check same inputs once, but no one should move forward till verified.
    int nInputStatus;
    if (nMyRank == N_ROOT_RANK) {
        // Function will exit with error message upon need
        nInputStatus = verifyInputs(argc, argv);
    }
    MPI_Bcast(&nInputStatus, 1, MPI_INT, N_ROOT_RANK, MPI_COMM_WORLD);
    if (nInputStatus == EXIT_FAILURE) {
        shutDown(EXIT_FAILURE);
    }

    // Save command-line arguments argv[0] is program name
    if (argc > 1) {
        nNumGridPoints =    atoi(argv[1]);
    }
    if (argc > 2) {
        nStrategySendRecv = atoi(argv[2]);
    }
    // Default to non-blocking
    else {
        nStrategySendRecv = 1;
    }
    if (argc > 3) {
        nStrategyGather = atoi(argv[3]);
    }
    // Default to MPI_Gather
    else {
        nStrategyGather = 0;
    }

    #ifdef TEST
        if (argc > 4) {
			nFunctionIndex = atoi(argv[4]);
        } else
			nFunctionIndex = 0;
		fnp = funcp[nFunctionIndex];
    #endif

    /* Check the wall clock
     * As soon as we know whether we are in charge of measuring time
     *  so that we can include as much as possible in our measurement.
     * Because we want to measure the total time to
     *  approximate the derivative function
     *  using parallel computation and message passing,
     *  we only want to measure / report this from the root task.
     */
  	if (nMyRank == N_ROOT_RANK) {
  	    gettimeofday(&oStartTime, NULL);
  	    nStartTime_us = oStartTime.tv_sec * 1000000 + oStartTime.tv_usec;
  	}

  	// Print debug info?
    #ifdef DEBUG
	    printf("\n");
	    printf("nNumTasks: %d\n",           nNumTasks);
	    printf("nMyRank: %d\n",             nMyRank);
	    printf("nNumGridPoints: %d\n",      nNumGridPoints);
	    printf("nStrategySendRecv: %d\n",   nStrategySendRecv);
	    printf("nStrategyGather: %d\n",     nStrategyGather);
	#ifdef TEST
	    printf("nFunctionIndex: %d\n",	    nFunctionIndex);
	#endif
    #endif

  	// Decompose the grid into grid points a.k.a. slices that this particular task works with
  	bOkaySoFar = decomposeGrid(nNumGridPoints, nNumTasks, nMyRank);

  	// If decomposition went okay, then continue
  	if (bOkaySoFar == 1) {


        // Perform boundary exchange with neighbors to get f(x) values outside of my own grid range
        // Overall we're setting values to pPointerToBufferY[0] & pPointerToBufferY[length - 1]
        double prev;
        double next;
        // At most 4.
        MPI_Request reqs[4];
        MPI_Status stats[4];
        int request_cnt = 0;
        if (nStrategySendRecv == 1) {
            // Initiate boundary exchange prior to calculating Y values
            request_cnt = mpi_exchange_boundary_fx_nonblocking(
                nMyRank,
                nNumTasks,
		#ifdef TEST
				(*fnp)(pPointerToBufferX[0]),
				(*fnp)(pPointerToBufferX[nMyNumGridPoints - 1]),
		#else
                fn(pPointerToBufferX[0]),    // send f(x) values
                fn(pPointerToBufferX[nMyNumGridPoints - 1]),
		#endif
                &prev,     // recieve f(x) values
                &next,
                reqs);
        }

  	    // Calculate Y values
        // Skip first entry - will get via send/recv.
        // Last entry also left empty
  	    for (i = 1; i <= nMyNumGridPoints; i++) {
			#ifdef TEST
			pPointerToBufferY[i] = (*fnp)(pPointerToBufferX[i - 1]);
			#else
  	        pPointerToBufferY[i] = fn(pPointerToBufferX[i - 1]);
			#endif
  	    }

  	    // Print debug info?
  	    #ifdef DEBUG
  	        printf("\nAfter populating Y values:\n");
  	        reportBufferContentsAll();
	      #endif

        // Send & Receive (0 = blocking, 1 = non-blocking)
        if (nStrategySendRecv == 0) {
            mpi_exchange_boundary_fx_blocking(
                nMyRank,
                nNumTasks,
                pPointerToBufferY[1],
                pPointerToBufferY[nMyNumGridPoints],
                &prev,
                &next);
        }

        /* If non-blocking, wait for boundary exchange to complete
         * Why put the wait here?
         * Why not calculate some of the dY values that don't depend on the boundary exchanges,
         *  then wait for the boundary exchanges,
         *  then complete calculation of the dY values?
         * The reason is that we believe that this would be over-optimization
         *  that would introduce unnecessary complexity into our code.
         * By this point in the code, the boundary exchange messages should have had ample time to complete
         *  because there are a maximum of 4 such very small messages,
         *  and they were kicked off prior to calculating ALL of the f(x) values that this task is responsible for.
         */
        if (nStrategySendRecv == 1) {
            MPI_Waitall(request_cnt, reqs, stats);
        }
        pPointerToBufferY[0] = prev;
        pPointerToBufferY[nMyNumGridPoints + 1] = next;

        // Calculate dY values
        for (int itr = 1; itr <= nMyNumGridPoints; itr++) {
              pPointerToBufferDY[itr - 1] = mpi_calc_derivative(itr);
        }

  	    // Gather results back to the root task
  	    bOkaySoFar = gatherResults(nStrategySendRecv, nStrategyGather, nMyRank);

  	}

  	/* If everything is okay so far (and we're ROOT)
  	 *  Measure elapsed time
  	 *  Produce *.date file for GNU Plot
  	 *  Report completion to user
  	 */
  	if (bOkaySoFar == 1 && nMyRank == N_ROOT_RANK) {

  	        // Measure elapsed time
  	        gettimeofday(&oEndTime, NULL);
            nEndTime_us = oEndTime.tv_sec * 1000000 + oEndTime.tv_usec;

            // Determine name for *.dat file (fn-{number_of_slices}.dat)
            sprintf(sFileName, "fn-%d.dat", nNumGridPoints);

            // Populate *.dat file
            fp = fopen(sFileName, "w");
            for(i = 0; i < nNumGridPoints; i++) {
                    fprintf(
                            fp,
                            "%f %f %f\n",
                            pPointerToBufferX_Final[i],
                            pPointerToBufferY_Final[i],
                            pPointerToBufferDY_Final[i]
                    );
            }
            fclose(fp);

            // Report to the user
            reportCompletion(sFileName, nStartTime_us, nEndTime_us);

            // Print extra debug info as well?
            #ifdef DEBUG
                    printf("\nAfter completion:\n");
                    reportBufferContentsAll();
            #endif
	}

  	// Shut down (success)
  	shutDown(EXIT_SUCCESS);

}

/******************************************************************************
 * FUNCTION:        verifyInputs
 *
 * DESCRIPTION:	    Ensure the required, and optional, command line arugments
 *                  are valid for performing the calculation. This should only be run
 *                  by one process, while any other processes wait. To avoid performing
 *                  calculations on data that could crash the program.
 *
 * ARGUMENTS:        0 -         Number of grid points a.k.a. number of slices
 *                   1 -         0 for blocking point-to-point communication
 *                               1 for non-blocking point-to-point communication
 *                   2 -         0 for collective gather operation of results to root
 *                               1 for individual "manual" send-receive of results to root
 *                   3 -         Function index (1 = x^2, ...)
 *
 * RETURNS:	        None. The program will exit with the appropriate messages if
 *                  invalid input is provided.
 *
 * AUTHOR:	        wpmoore2            Wade Moore
 ******************************************************************************/
int verifyInputs(int argc, char *argv[]) {

    // Check for at least one argument (must pass grid size)
    if (argc <= 1) {
        return inputValidationError("Grid size must be provided");
    }

    char msg[100];
    // Check Grid Size
    if (!isdigit((int) *argv[1])) {
        sprintf(msg, "Grid Size (%s) must be a positive integer", argv[1]);
        return inputValidationError(msg);
    }
    int gridSize = atoi(argv[1]);
    if ( gridSize < 1) {
        sprintf(msg, "Grid Size (%s) must be a positive integer", argv[1]);
        return inputValidationError(msg);
    }


    // Check Blocking flag
    if (argc > 2) {
        sprintf(msg, "The send-receive flag (%s) must be either 0 (blocking) or 1 (non-blocking)", argv[2]);
        if (!isdigit((int) *argv[2])) {
            return inputValidationError(msg);
        }
        int blocking = atoi(argv[2]);
        if (blocking != 0 && blocking != 1) {
            return inputValidationError(msg);
        }
    }

    // Check Simple/Manual Gather flag
    if (argc > 3) {
        sprintf(msg, "The Gather Type (%s) must be either 0 (MPI), or 1 (Manual)", argv[3]);
        if (!isdigit((int) *argv[3])) {
            return inputValidationError(msg);
        }
        int blocking = atoi(argv[3]);
        if (blocking != 0 && blocking != 1) {
            return inputValidationError(msg);
        }
    }

#ifdef TEST
	// check if function index is correct
	if (argc > 4) {
		sprintf(msg, "The function index (%s) must be within <0...MAX_NUM_FUNC(7)>", argv[4]);
		if (!isdigit((int) *argv[4])) {
			return inputValidationError(msg);
		}
		int findex = atoi(argv[4]);
		if (findex < 0 || findex >= MAX_NUM_FUNC) {
			return inputValidationError(msg);
		}
	}
#endif

    return EXIT_SUCCESS;
}

/*
 * Helper function to print error/usage message then exit program.
 * Data has not yet been allocated so no need to free.
 */
int inputValidationError(char *msg) {
    // Newlines make it more readable amid arc logs
    char usage[] = "Usage: prun ./p2 <grid-size> [<blockingFlag>] [<customGatherFlag>]\n\n";
    printf("\n\n%s\n", msg);
    printf("%s", usage);
    return EXIT_FAILURE;
}

double getMostUpperFxBound() {
#ifdef TEST
	return (*fnp)(N_GRID_POINT_FINAL + nStepSize);
#else
	return fn(N_GRID_POINT_FINAL + nStepSize);
#endif
}

double getMostLowerFxBound() {
#ifdef TEST
	return (*fnp)(N_GRID_POINT_INITIAL - nStepSize);
#else
	return fn(N_GRID_POINT_INITIAL - nStepSize);
#endif
}

/******************************************************************************
 * FUNCTION:        mpi_exchange_boundary_fx_nonblocking
 *
 * DESCRIPTION:	    Obtain the f(0) and f(length - 1) values, or the 'bounds'
 *                  of our function by communicating with our neighboring
 *                  process. If there is not a neighbor, then manually calculate
 *                  the needed value - should only be the 2 ourside of the whole range.
 *
 * ARGUMENTS:        rank       - Process Rank
 *                   numproc    - 1 for blocking point-to-point communication
 *                   sendLower  - value of f(lower bound). Will send to neighbor
 *                   sendUpper  - value of f(upper bound). Will send to neighbor
 *                   recvLower  - address of f(lower bound) that will be stored and
 *                                used as an output var.
 *                   recvUpper  - address of f(upper bound) that will be stored and
 *                                used as an output var.
 *
 * RETURNS:	        None
 *
 * AUTHOR:	        wpmoore2            Wade Moore
 ******************************************************************************/
int mpi_exchange_boundary_fx_nonblocking(int rank, int numproc, double sendLower, double sendUpper,
                                          double *recvLower, double *recvUpper, MPI_Request *reqs) {
    // Initiate send/recv for my values
    int tag = NON_BLOCK_EXCHG_CODE;
    int destUpper;
    int destLower;

    int request_cnt = 0;

    // If process is on edge (or alone) must manually calculate outermost bounds
    if (numproc == 1) {
        *recvLower = getMostLowerFxBound();
        *recvUpper = getMostUpperFxBound();
    }
    // Manually get lower
    else if (rank == N_ROOT_RANK) {
        request_cnt = 2;
        *recvLower = getMostLowerFxBound();
        MPI_Isend(&sendUpper, 1, MPI_DOUBLE, rank + 1, tag, MPI_COMM_WORLD, &reqs[0]);
        MPI_Irecv(recvUpper, 1, MPI_DOUBLE, rank + 1, tag, MPI_COMM_WORLD, &reqs[1]);
    }
    // Manually get upper
    else if ((rank + 1) == numproc) {
        request_cnt = 2;
        *recvUpper = getMostUpperFxBound();
        MPI_Isend(&sendLower, 1, MPI_DOUBLE, rank - 1, tag, MPI_COMM_WORLD, &reqs[0]);
        MPI_Irecv(recvLower, 1, MPI_DOUBLE, rank - 1, tag, MPI_COMM_WORLD, &reqs[1]);
    }
    // Else get from both neighbors
    else {
        request_cnt = 4;
        MPI_Isend(&sendLower, 1, MPI_DOUBLE, rank - 1, tag, MPI_COMM_WORLD, &reqs[0]);
        MPI_Isend(&sendUpper, 1, MPI_DOUBLE, rank + 1, tag, MPI_COMM_WORLD, &reqs[2]);
        MPI_Irecv(recvLower, 1, MPI_DOUBLE, rank - 1, tag, MPI_COMM_WORLD, &reqs[1]);
        MPI_Irecv(recvUpper, 1, MPI_DOUBLE, rank + 1, tag, MPI_COMM_WORLD, &reqs[3]);
    }

    return request_cnt;
}

/*
 * @author: Subhendu S Behera
 * args: processor rank, num of processors, left-most grid point fx, right-most
 *       grid point fx, pointer to return left neighbour's boundary fx, pointer
 *       to return right most neighbour's boundary fx.
 * return: void
 *
 * The exchange of boundary values is facilitated by dividing the whole set of
 * nodes into odd-numbered and even-numbered sets. At first, exchange of boundary
 * values happens in between the even-numbered sets and their left neighbour.
 * Then the exchange happens in between the even-numbered sets and their right
 * neighbour.
 */
void mpi_exchange_boundary_fx_blocking(int rank, int nproc, double fx_l, double fx_r,
				      double *fx_prev, double *fx_next)
{
	MPI_Status status;

	if (rank % 2 == 0) {
		if (rank > N_ROOT_RANK) {
			/*
		 	 * send the left boundary Y/f(x) to its left neighbour.
		 	 */
			MPI_Send(&fx_l, 1, MPI_DOUBLE, rank - 1,
				 	 rank, MPI_COMM_WORLD);
		}

		if (rank == N_ROOT_RANK) {
			/*
			 * if root set the f(x) for 1st grid point to fn(XI - dx).
		 	 */
			*fx_prev = getMostLowerFxBound();
		} else {
			/*
		 	 * receive the right boundary Y/f(x) of its left neighbour.
		 	 */
			MPI_Recv(fx_prev, 1, MPI_DOUBLE, rank - 1,
				    rank - 1, MPI_COMM_WORLD, &status);
		}

		if (rank < (nproc  - 1)) {
			/*
			 * send the right boundary Y/f(x) to its right neighbour.
			 */
			MPI_Send(&fx_r, 1, MPI_DOUBLE, rank + 1,
				 	 rank, MPI_COMM_WORLD);
		}

		if (rank == (nproc - 1)) {
			/*
			 * if the last rank then set the f(x) for last grid point
			 * to fn(XF + dx).
			 */
			*fx_next = getMostUpperFxBound();
		} else {
			/*
			 * receive the left boundary Y/f(x) of its right neighbour.
			 */
			MPI_Recv(fx_next, 1, MPI_DOUBLE, rank + 1,
					 rank + 1, MPI_COMM_WORLD, &status);
		}
	} else {
		if (rank == (nproc - 1)) {
			/*
			 * if last rank then set the f(x) for last grid point to
			 * fn(XF + dx).
			 */
			*fx_next = getMostUpperFxBound();
		} else if(rank < (nproc - 1)) {
			/*
			 * receive the left boundary Y/f(x) of its right neighbour.
			 */
			MPI_Recv(fx_next, 1, MPI_DOUBLE, rank + 1,
					 rank + 1, MPI_COMM_WORLD, &status);
			/*
			 * send the right boundary Y/f(x) to its right neighbour.
			 */
			MPI_Send(&fx_r, 1, MPI_DOUBLE, rank + 1,
					 rank, MPI_COMM_WORLD);
		}

		if (rank > 0) {
			/*
			 * receive the right boundary Y/f(x) of its left neighbour.
			 */
			MPI_Recv(fx_prev, 1, MPI_DOUBLE, rank - 1,
					 rank - 1, MPI_COMM_WORLD, &status);
			/*
			 * send the left boundary Y/f(x) to its left neighbour.
			 */
			MPI_Send(&fx_l, 1, MPI_DOUBLE, rank - 1,
					 rank, MPI_COMM_WORLD);
		}
	}
}

/*
 * FUNCTION:    mpi_calc_derivative
 *
 * DESCRIPTION: Use the F(x) buffer (also called Y).
 *              Assume Y buffer is filled appropriately,
 *              and global variable 'nStepSize' has been initialized
 * Authors:		wpmoore2            Wade Moore
 * 				ssbehera			Subhendu S Behera
 */
double mpi_calc_derivative(int idx)
{
	return (( pPointerToBufferY[idx + 1] - pPointerToBufferY[idx - 1] ) / (2 * nStepSize));
}



/******************************************************************************
 * FUNCTION:        decomposeGrid
 *
 * DESCRIPTION:	    Determine which of the grid points the
 *                  current task is responsible for,
 *                  and establish buffers to hold
 *                  all needed info for these grid points
 *
 * ARGUMENTS:       nNumGridPoints -    The total number of grid points
 *                                      for which we need to perform calculations
 *                  nNumTasks -         The total number of tasks
 *                                      performing calculations
 *                  nMyRank -           My rank (zero-indexed) among all tasks
 *
 * RETURNS:	        (globals ref'd) -   pPointerToBufferX
 *                                      pPointerToBufferY
 *                                      pPointerToBufferDY
 *                  bSuccess -          1 if everything went okay, 0 otherwise
 *
 * AUTHOR:	        attiffan            Aurora T. Tiffany-Davis
 * 					ssbehera			Subhendu S Behera
 ******************************************************************************/
int decomposeGrid (int nNumGridPoints, int nNumTasks, int nMyRank) {

    // Declare variables
    int     bSuccess;
    int     i;
    double  nGridRange;
    double  nMyStartPoint;

    // Until we know otherwise, assume success
    bSuccess = 1;

    // Get the total range of the grid
  	nGridRange = N_GRID_POINT_FINAL - N_GRID_POINT_INITIAL;
  	// Get everybody's step size
  	nStepSize = nGridRange / (nNumGridPoints - 1);

  	/*
  	 * Set num of grid points and initial starting point. In a
  	 * non-uniform distribution of grid points we distribute the
  	 * (%)remaining grid points to the Tasks/Ranks at the begining.
  	 * So, there will be set of Tasks/Ranks. Members of first set
  	 * will get one extra grid point each. Therefore, the initial
  	 * grid point calculation will change for the second set of
  	 * Tasks/Ranks. This set will take the range of first of set of
  	 * Tasks/Ranks and the preceeding Tasks' range of the second set
  	 * into account.
  	 */
  	nMyNumGridPoints = nNumGridPoints / nNumTasks;
  	if (nMyRank < nNumGridPoints % nNumTasks) {
  		// First set.
  		nMyNumGridPoints++;
  		nMyStartPoint = N_GRID_POINT_INITIAL +
  				nStepSize * nMyNumGridPoints * nMyRank;
  	} else {
  		// Second set.
  		nMyStartPoint = N_GRID_POINT_INITIAL;
  		nMyStartPoint += nStepSize * (nMyNumGridPoints + 1) *
  				 (nNumGridPoints % nNumTasks);
  		nMyStartPoint += nStepSize * (nMyNumGridPoints) *
  				 (nMyRank - (nNumGridPoints % nNumTasks));
  	}

  	// Allocate and populate buffer for X values
  	pPointerToBufferX = calloc(nMyNumGridPoints, sizeof(double));
  	if (pPointerToBufferX == NULL) {
    		printf("Task rank %d could not allocate memory for X value Buffer\n", nMyRank);
    		bSuccess = 0;
  	} else {
      	// Calculate my grid points (x values)
      	for (i = 0; i < nMyNumGridPoints; i++) {
  			     pPointerToBufferX[i] = nMyStartPoint + (i * nStepSize);
        }
    }

    // Allocate buffer for Y values
    if (bSuccess == 1) {
        // 2 more for the outside bounds
        pPointerToBufferY = calloc(nMyNumGridPoints + 2, sizeof(double));
        if (pPointerToBufferY == NULL) {
            printf("Task rank %d could not allocate memory for Y value buffer", nMyRank);
            bSuccess = 0;
        }
    }

    // Allocate buffer for dY values
    if (bSuccess == 1) {
        pPointerToBufferDY = calloc(nMyNumGridPoints, sizeof(double));
        if (pPointerToBufferDY == NULL) {
            printf("Task rank %d could not allocate memory for dY value buffer", nMyRank);
            bSuccess = 0;
        }
    }

    // If root, keep track of counts and displacements for gathering results later
    if (bSuccess == 1 && nMyRank == N_ROOT_RANK) {
        pPointerToResultCounts = malloc(nNumTasks * sizeof(int));
        if (pPointerToResultCounts == NULL) {
            printf("Task rank %d could not allocate memory for results count buffer", nMyRank);
            bSuccess = 0;
        }
    }

    if (bSuccess == 1 && nMyRank == N_ROOT_RANK) {
        pPointerToResultDisplacements = malloc(nNumTasks * sizeof(int));
        if (pPointerToResultDisplacements == NULL) {
            printf("Task rank %d could not allocate memory for results displacement buffer", nMyRank);
            bSuccess = 0;
        }
    }

    /*
     * set the no of grid point results to be received by the root Task/Rank
     * and displacement for each Task/Rank in the receive buffer provided by
     * root Task/Rank in MPI_Gatherv function call.
     */
    if (bSuccess == 1 && nMyRank == N_ROOT_RANK) {
        int curDisp = 0;
        for (i = 0; i < nNumTasks; i++) {
            pPointerToResultCounts[i] = nNumGridPoints / nNumTasks;
      	    if (i < nNumGridPoints % nNumTasks) {
      		      pPointerToResultCounts[i]++;
            }
            pPointerToResultDisplacements[i] = curDisp;
            curDisp += pPointerToResultCounts[i];
        }
    }

    // Print debug info?
    #ifdef DEBUG
        printf("\nAfter decomposing the grid:\n");
        reportBufferContentsAll();
    #endif

    // Return
    return bSuccess;

}

/******************************************************************************
 * FUNCTION:        gatherResults
 *
 * DESCRIPTION:	    Perform the work of getting sub-results back to the root rank.
 *                  Either by individual send-receives, or by a collective gather.
 *                  Every task calls this method.
 *                  The method behaves differently based upon task rank.
 *
 * ARGUMENTS:       nSendRecvType - Setting for send-receive strategy
 *                                          0 for blocking
 *                                          1 for non-blocking
 *                  nGatherType -   Setting for results gathering strategy
 *                                          0 for MPI_Gather
 *                                          1 for manual gather
 *                                          (blocking or non-blocking per previous argument)
 *
 * RETURNS:         (globals ref'd) -   pPointerToBufferX_Final
 *                                      pPointerToBufferY_Final
 *                                      pPointerToBufferDY_Final
 *                  bSuccess -          1 if everything went okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 ******************************************************************************/
int gatherResults (int nSendRecvType, int nGatherType, int nMyRank) {

    // Declare variables
    int bSuccess;

    // Until we know otherwise, assume success
    bSuccess = 1;

    // If root, establish buffers to put all the gathered results in
    if (nMyRank == N_ROOT_RANK) {
    	pPointerToBufferX_Final = calloc(nNumGridPoints, sizeof(double));
    	if (pPointerToBufferX_Final == NULL) {
            printf("Task rank %d could not allocate memory for final X value buffer",
		   nMyRank);
            bSuccess = 0;
    	}
    	if (bSuccess == 1) {
    	    pPointerToBufferY_Final = calloc(nNumGridPoints, sizeof(double));
            if (pPointerToBufferY_Final == NULL) {
            	printf("Task rank %d could not allocate memory for final Y value buffer",
			               nMyRank);
                bSuccess = 0;
            }
    	}
    	if (bSuccess == 1) {
            pPointerToBufferDY_Final = calloc(nNumGridPoints, sizeof(double));
            if (pPointerToBufferDY_Final == NULL) {
            	printf("Task rank %d could not allocate memory for final dY value buffer",
			               nMyRank);
            	bSuccess = 0;
            }
    	}
    }

    // Gather the results, either using collective operation or individual send-receive
    if (bSuccess == 1) {
        if (nGatherType == 0) {

            // MPI_Gather
            bSuccess = gatherMPI(N_TAG_X);
            if (bSuccess == 1) {
                bSuccess = gatherMPI(N_TAG_Y);
            }
            if (bSuccess == 1) {
                bSuccess = gatherMPI(N_TAG_DY);
            }

        }
        else if (nGatherType == 1 && nSendRecvType == 0) {

            // Manual gather with synchronous calls
            bSuccess = gatherManualSync(N_TAG_X);
            if (bSuccess == 1) {
                bSuccess = gatherManualSync(N_TAG_Y);
            }
            if (bSuccess == 1) {
                bSuccess = gatherManualSync(N_TAG_DY);
            }

        }

        else if (nGatherType == 1 && nSendRecvType == 1) {

            /* Manual gather with asynchronous calls
             * First, kick ALL gather operations off
             * Second, wait for ALL gather operations to complete
             * This way, they can complete in any order
             *  and we won't force an unnatural blocking behavior
             */
            bSuccess = gatherManualAsyncStart(N_TAG_X);
            if (bSuccess == 1) {
                bSuccess = gatherManualAsyncStart(N_TAG_Y);
            }
            if (bSuccess == 1) {
                bSuccess = gatherManualAsyncStart(N_TAG_DY);
            }
            if (bSuccess == 1) {
                bSuccess = gatherManualAsyncFinish(N_TAG_X);
            }
            if (bSuccess == 1) {
                bSuccess = gatherManualAsyncFinish(N_TAG_Y);
            }
            if (bSuccess == 1) {
                bSuccess = gatherManualAsyncFinish(N_TAG_DY);
            }

        }

    }

    // Print debug info
    #ifdef DEBUG
        if (nMyRank == N_ROOT_RANK) {
            printf("\nAfter gathering results:\n");
            reportBufferContentsAll();
        }
    #endif

    // Return
    return bSuccess;


}

/******************************************************************************
 * FUNCTION:        gatherMPI
 *
 * DESCRIPTION:     Perform an MPI gather for one results buffer
 *
 * ARGUMENTS:       nMessageTag -       Meaning as defined in #define section of this file
 *
 * RETURNS:         (globals ref'd) -   pPointerToBufferX_Final
 *                                      pPointerToBufferY_Final
 *                                      pPointerToBufferDY_Final
 *                  bSuccess -          1 if everything went okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 ******************************************************************************/
int gatherMPI (int nMessageTag) {

    // Declare variables
    int     nReturnCode;
    double *pPointerToBufferSend;
    double *pPointerToBufferRecv;

    /* Figure out what kind of message we're dealing with
     *  and get pointers to the send-receive buffers.
     * If we are not root, then the receive buffers
     *  have not been allocated, and will be NULL,
     *  which is appropriate (they will not be used by MPI_Gather anyway)
     */
    if (nMessageTag == N_TAG_X) {
        pPointerToBufferSend = pPointerToBufferX;
        pPointerToBufferRecv = pPointerToBufferX_Final;
    }
    else if (nMessageTag == N_TAG_Y) {
        // Skip first, outermost boundary
        pPointerToBufferSend = pPointerToBufferY + 1;
        pPointerToBufferRecv = pPointerToBufferY_Final;
    }
    else if (nMessageTag == N_TAG_DY) {
        pPointerToBufferSend = pPointerToBufferDY;
        pPointerToBufferRecv = pPointerToBufferDY_Final;
    }

    // Perform the gather operation
    nReturnCode = MPI_Gatherv(
        // Send buffer
        (void *) pPointerToBufferSend,
        // Send count
        nMyNumGridPoints,
        // Send data type
        MPI_DOUBLE,
        // Receive buffer
        (void *) pPointerToBufferRecv,
        // # elements received from each task
        pPointerToResultCounts,
        // Displacement relative to recv buffer for data from task i
        pPointerToResultDisplacements,
        // Receive data type
        MPI_DOUBLE,
        // Root rank
        N_ROOT_RANK,
        // Communicator handle
        MPI_COMM_WORLD
    );

    // Return
    return nReturnCode == MPI_SUCCESS ? 1 : 0;


}

/******************************************************************************
 * FUNCTION:        gatherManualSync
 *
 * DESCRIPTION:     Perform a manual synchronous gather for one results buffer
 *
 * ARGUMENTS:       nMessageTag -       Meaning as defined in #define section of this file
 *
 * RETURNS:         (globals ref'd) -   pPointerToBufferX_Final
 *                                      pPointerToBufferY_Final
 *                                      pPointerToBufferDY_Final
 *                  bSuccess -          1 if everything went okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 ******************************************************************************/
int gatherManualSync (int nMessageTag) {

    // Declare variables
    int         bSuccess;
    int         nReturnCode;
    int         i;
    double      *pPointerToBufferSend;
    double      *pPointerToBufferRecv;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Figure out what kind of message we're dealing with
    if (nMessageTag == N_TAG_X) {
        pPointerToBufferSend = pPointerToBufferX;
        pPointerToBufferRecv = pPointerToBufferX_Final;
    }
    else if (nMessageTag == N_TAG_Y) {
        pPointerToBufferSend = pPointerToBufferY + 1;
        pPointerToBufferRecv = pPointerToBufferY_Final;
    }
    else if (nMessageTag == N_TAG_DY) {
        pPointerToBufferSend = pPointerToBufferDY;
        pPointerToBufferRecv = pPointerToBufferDY_Final;
    }

    // Root Task
    if (nMyRank == N_ROOT_RANK) {

        /* Copy my own results over into the larger buffer
         * (this blocks but so do receives, so do it now or later, no difference)
         */
        memcpy(pPointerToBufferRecv, pPointerToBufferSend, (nMyNumGridPoints * sizeof(double)));

        // Expect results from other tasks via synchronous messaging
        for (i = 1; i < nNumTasks; i++) {

            // Print debug info?
            #ifdef DEBUG
                printf(
                    "\nReceive %d elements from task %d and place at displacement %d\n",
                    pPointerToResultCounts[i],
                    i,
                    pPointerToResultDisplacements[i]
                );
            #endif

            // Receive (synchronously)
            nReturnCode = MPI_Recv (
                // Address of where in receive buffer to place data
                pPointerToBufferRecv + pPointerToResultDisplacements[i],
                // # elements to receive
                pPointerToResultCounts[i],
                // Receive data type
                MPI_DOUBLE,
                // Rank of source task
                i,
                // Tag of message
                nMessageTag,
                // Communicator handle
                MPI_COMM_WORLD,
                // Address into which status should be placed
                MPI_STATUS_IGNORE
            );

            // Successful?
            bSuccess = (nReturnCode == MPI_SUCCESS ? 1 : 0);
            if (bSuccess == 0) {
                break;
            }

        }

    }

    // Non-root Tasks
    else {

        // Print debug info?
        #ifdef DEBUG
            printf(
                "\nTask rank %d sending %d elements to root\n",
                nMyRank,
                nMyNumGridPoints
            );
        #endif

        // Send (synchronously)
        nReturnCode = MPI_Ssend(
            // Send buffer
            pPointerToBufferSend,
            // # elements to send
            nMyNumGridPoints,
            // Send data type
            MPI_DOUBLE,
            // Destination rank
            N_ROOT_RANK,
            // Tag of message
            nMessageTag,
            // Communicator handle
            MPI_COMM_WORLD
        );

        // Successful?
        bSuccess = (nReturnCode == MPI_SUCCESS ? 1 : 0);

    }

    // Return
    return bSuccess;

}

/******************************************************************************
 * FUNCTION:        gatherManualAsyncStart
 *
 * DESCRIPTION:     Start a manual asynchronous gather for one results buffer
 *
 * ARGUMENTS:       nMessageTag -       Meaning as defined in #define section of this file
 *
 * RETURNS:         (globals ref'd) -   pPointerToBufferX_Final
 *                                      pPointerToBufferY_Final
 *                                      pPointerToBufferDY_Final
 *                  bSuccess -          1 if everything went okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 ******************************************************************************/
int gatherManualAsyncStart (int nMessageTag) {

    // Declare variables
    int         bSuccess;
    int         nReturnCode;
    int         i;
    double      *pPointerToBufferSend;
    double      *pPointerToBufferRecv;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Figure out what kind of message we're dealing with
    if (nMessageTag == N_TAG_X) {
        pPointerToBufferSend = pPointerToBufferX;
        pPointerToBufferRecv = pPointerToBufferX_Final;
    }
    else if (nMessageTag == N_TAG_Y) {
        pPointerToBufferSend = pPointerToBufferY + 1;
        pPointerToBufferRecv = pPointerToBufferY_Final;
    }
    else if (nMessageTag == N_TAG_DY) {
        pPointerToBufferSend = pPointerToBufferDY;
        pPointerToBufferRecv = pPointerToBufferDY_Final;
    }

    // Root Task
    if (nMyRank == N_ROOT_RANK) {

        // Allocate memory for multiple requests, so we can wait for them to complete, later
        pPointerToRequests = malloc(nNumTasks * sizeof(MPI_Request));

        // Expect results from other tasks via asynchronous messaging
        for (i = 1; i < nNumTasks; i++) {

            // Print debug info?
            #ifdef DEBUG
                printf(
                    "\nReceive %d elements from task %d and place at displacement %d\n",
                    pPointerToResultCounts[i],
                    i,
                    pPointerToResultDisplacements[i]
                );
            #endif

            // Receive (asynchronously)
            nReturnCode = MPI_Irecv (
                // Address of where in receive buffer to place data
                pPointerToBufferRecv + pPointerToResultDisplacements[i],
                // # elements to receive
                pPointerToResultCounts[i],
                // Receive data type
                MPI_DOUBLE,
                // Rank of source task
                i,
                // Tag of message
                nMessageTag,
                // Communicator handle
                MPI_COMM_WORLD,
                // Request handle
                &pPointerToRequests[i]
            );

            // Successful?
            bSuccess = (nReturnCode == MPI_SUCCESS ? 1 : 0);
            if (bSuccess == 0) {
                break;
            }

        }

        /* Copy my own results over into the larger buffer AFTER kicking off async receives
         * This way we can work on copying the memory while waiting for the results to come in
         */
        memcpy(pPointerToBufferRecv, pPointerToBufferSend, (nMyNumGridPoints * sizeof(double)));

    }

    // Non-root Tasks
    else {

        // Print debug info?
        #ifdef DEBUG
            printf(
                "\nTask rank %d sending %d elements to root\n",
                nMyRank,
                nMyNumGridPoints
            );
        #endif

        // Send (asynchronously)
        nReturnCode = MPI_Isend(
            // Send buffer
            pPointerToBufferSend,
            // # elements to send
            nMyNumGridPoints,
            // Send data type
            MPI_DOUBLE,
            // Destination rank
            N_ROOT_RANK,
            // Tag of message
            nMessageTag,
            // Communicator handle
            MPI_COMM_WORLD,
            // Request handle
            &oSingleRequest
        );

        // Successful?
        bSuccess = (nReturnCode == MPI_SUCCESS ? 1 : 0);

    }

    // Return
    return bSuccess;

}

/******************************************************************************
 * FUNCTION:        gatherManualAsyncFinish
 *
 * DESCRIPTION:     Finish a manual asynchronous gather for one results buffer
 *
 * ARGUMENTS:       nMessageTag -       Meaning as defined in #define section of this file
 *
 * RETURNS:         (globals ref'd) -   pPointerToBufferX_Final
 *                                      pPointerToBufferY_Final
 *                                      pPointerToBufferDY_Final
 *                  bSuccess -          1 if everything went okay, 0 otherwise
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 ******************************************************************************/
int gatherManualAsyncFinish (int nMessageTag) {

    // Declare variables
    int         bSuccess;
    int         nReturnCode;
    int         i;
    double      *pPointerToBufferSend;
    double      *pPointerToBufferRecv;

    // Assume success until we know otherwise
    bSuccess = 1;

    // Figure out what kind of message we're dealing with
    if (nMessageTag == N_TAG_X) {
        pPointerToBufferSend = pPointerToBufferX;
        pPointerToBufferRecv = pPointerToBufferX_Final;
    }
    else if (nMessageTag == N_TAG_Y) {
        pPointerToBufferSend = pPointerToBufferY + 1;
        pPointerToBufferRecv = pPointerToBufferY_Final;
    }
    else if (nMessageTag == N_TAG_DY) {
        pPointerToBufferSend = pPointerToBufferDY;
        pPointerToBufferRecv = pPointerToBufferDY_Final;
    }

    // Root Task
    if (nMyRank == N_ROOT_RANK) {

        // Wait for asynchronous results from other tasks
        for (i = 1; i < nNumTasks; i++) {

            // Wait for asynchronous receive to complete
            nReturnCode = MPI_Wait(&pPointerToRequests[i], MPI_STATUS_IGNORE);

            // Successful?
            bSuccess = (nReturnCode == MPI_SUCCESS ? 1 : 0);
            if (bSuccess == 0) {
                break;
            }

        }

    }

    // Non-root Tasks
    else {

        // Wait for asynchronous send to complete
        nReturnCode = MPI_Wait(&oSingleRequest, MPI_STATUS_IGNORE);

        // Successful?
        bSuccess = (nReturnCode == MPI_SUCCESS ? 1 : 0);

    }

    // Return
    return bSuccess;

}

/******************************************************************************
 * FUNCTION:        reportCompletion
 *
 * DESCRIPTION:     Report the completion of the entire job
 *                  Intention is to call this only from the root rank
 *                  Reports several things, so that we can use these reports
 *                  to compare how the code runs in different scenarios:
 *                  - # grid points
 *                  - # tasks
 *                  - point-to-point strategy (blocking or non-blocking)
 *                  - gather strategy (collective or individual)
 *
 * ARGUMENTS:       sFilename -     The name of the *.dat file where results are stored
 *                  nStartTime_ms - Start time for the entire job, in microseconds
 *                  nEndTime_ms -   End time for the entire job, in microseconds
 *
 * RETURNS:         None
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 ******************************************************************************/
void reportCompletion (char * sFileName, long int nStartTime_us, long int nEndTime_us) {

    // Declare constants
    const int N_US_IN_SECOND = 1000000;

    // Report results

    printf("\nNumber of grid points:\t%d\n", nNumGridPoints);

    printf("Number of tasks:\t%d\n", nNumTasks);

    printf("Point-to-point:\t\t");
    printf(nStrategySendRecv == 0 ? "Blocking" : "Non-Blocking");
    printf("\n");

    printf("Gather strategy:\t");
    printf(nStrategyGather == 0 ? "MPI_Gather" : "Manual");
    printf("\n");

    printf("Results available at:\t%s\n", sFileName);

    printf("Total job run time (s):\t%f\n\n", ((double) (nEndTime_us - nStartTime_us) / N_US_IN_SECOND));

}

/******************************************************************************
 * FUNCTION:        shutDown
 *
 * DESCRIPTION:     Shut down (free dynamically allocated memory, finalize MPI)
 *
 * ARGUMENTS:       nShutDownType - 0 for success, 1 for failure.
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 ******************************************************************************/
void shutDown (int nShutDownType) {

    // Wait until all tasks get here (helps with debug)
    MPI_Barrier(MPI_COMM_WORLD);

    // Free dynamically allocated memory
    if (pPointerToBufferX != NULL) {
        free(pPointerToBufferX);
    }
    if (pPointerToBufferY != NULL) {
        free(pPointerToBufferY);
    }
    if (pPointerToBufferDY != NULL) {
        free(pPointerToBufferDY);
    }
    if (pPointerToBufferX_Final != NULL) {
        free(pPointerToBufferX_Final);
    }
    if (pPointerToBufferY_Final != NULL) {
        free(pPointerToBufferY_Final);
    }
    if (pPointerToBufferDY_Final != NULL) {
        free(pPointerToBufferDY_Final);
    }
    if (pPointerToResultCounts != NULL) {
        free(pPointerToResultCounts);
    }
    if (pPointerToResultDisplacements != NULL) {
        free(pPointerToResultDisplacements);
    }
    if (pPointerToRequests != NULL) {
        free(pPointerToRequests);
    }

    // Finalize MPI
    MPI_Finalize();

    // Exit
    exit(nShutDownType);


}

/******************************************************************************
 * FUNCTION:        reportBufferContentsAll
 *
 * DESCRIPTION:     Report all buffer contents (intended for use during debug)
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 ******************************************************************************/
void reportBufferContentsAll () {

    // Report the contents of all buffers of interest

    reportBufferContentsOne("X Values",         pPointerToBufferX,          nMyNumGridPoints);
    reportBufferContentsOne("Y Values",         pPointerToBufferY,          nMyNumGridPoints + 2);
    reportBufferContentsOne("dY Values",        pPointerToBufferDY,         nMyNumGridPoints);

    reportBufferContentsOne("X Final Values",   pPointerToBufferX_Final,    nNumGridPoints);
    reportBufferContentsOne("Y Final Values",   pPointerToBufferY_Final,    nNumGridPoints);
    reportBufferContentsOne("dY Final Values",  pPointerToBufferDY_Final,   nNumGridPoints);

}

/******************************************************************************
 * FUNCTION:        reportBufferContentsOne
 *
 * DESCRIPTION:     Report one buffer's contents (intended for use during debug)
 *
 * ARGUMENTS:       sBufferName -       The name of the buffer to report
 *                  pPointerToBuffer -  A pointer to the start address of the buf
 *                  nNumValues -        The number of values to report on
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 ******************************************************************************/
void reportBufferContentsOne (char* sBufferName, double *pPointerToBuffer, int nNumValues) {

    // Declare variables
    int i;

    // Report the contents of all 3 buffers of interest

    printf("\nTask rank %d buffer contents (", nMyRank);
    printf("%s", sBufferName);
    printf("):\n");
    if (pPointerToBuffer == NULL) {
        printf("EMPTY\n");
    }
    else {
        for (i = 0; i < nNumValues; i++) {
            printf("%f\n", pPointerToBuffer[i]);
        }
    }

    // Print extra newline for readability
    printf("\n");

}

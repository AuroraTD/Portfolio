/*************************************************************************************************************
 * FILE:			p1.c
 *
 * AUTHOR:			attiffan	Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:		An MPI program that determines the point-to-point message latency for pairs of nodes.
 * 					Exchanges messages with varying message sizes from 32B to 2MB (32B, 64B, 128B, ..., 2MB).
 * 					Iterates through each of the message sizes with 10 iterations for each.
 * 					Intended use:
 * 						Use with 8 nodes (4 pairs)
 * 						Set -N and -n both to 8
 * 						This way there is only one process per node
 * 							(total # nodes = total # processes, so each node gets one,
 * 							and we can analyze communication between nodes)
 *
 * TO RUN:			srun -N8 -n8 -p opteron --pty /bin/bash
 * 					make -f p1.makefile
 * 					prun ./p1
 *************************************************************************************************************/

// INCLUDES
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <math.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include "mpi.h"

// DEFINES
#define N_ROOT_RANK				0
#define	N_TRIALS 				10
#define N_MIN_MESSAGE_SIZE_EXP 	5		// 2^5 bytes = 32B
#define N_MAX_MESSAGE_SIZE_EXP	21 		// 2^21 bytes = 2MB
#define N_MESSAGE_SIZES			17 		// 21 - 5 = 16
#define N_TAG_RTT				1
#define N_TAG_RESULTS			2

// Hack to get around a problem I have in my IDE setup
#ifndef NULL
#define NULL   ((void *) 0)
#endif

// Declare globals

int 	nMyRank;
int		nNumTasks;
int 	nResultsReportedFromEachPair;
int		nSizeOfSendBufferResults_Bytes;
int 	nSizeOfRecvBufferResults_Bytes;

void* 	pPointerToSendBufferRTT = 		NULL;
void* 	pPointerToRecvBufferRTT =		NULL;
double* pPointerToSendBufferResults =	NULL;
double* pPointerToRecvBufferResults =	NULL;
int* 	pPointerToResultCounts =		NULL;
int* 	pPointerToResultDisplacements =	NULL;

// Function declarations
int allocateMemory ();
void gatherResults ();
void shutDown ();
void barrier ();

/******************************************************************************
 * FUNCTION:		main
 *
 * DESCRIPTION:		Determine the point-to-point message latency for pairs of nodes.
 * 					Exchanges messages with varying message sizes.
 * 					Iterate through each of the message sizes with 10 iterations for each.
 * 					Even-numbered tasks are in charge of measuring round trip time (RTT).
 * 						They calculate average and standard deviation for each message size.
 * 						They report these results back to root once all message sizes have been tested.
 * 					Root task gathers all results and reports the final results.
 *
 * AUTHOR:			attiffan	Aurora T. Tiffany-Davis
 ******************************************************************************/
int main (int argc, char *argv[]) {

	// Initialize MPI
	MPI_Init(&argc, &argv);

	/// Get the number of tasks in the communicator
	MPI_Comm_size(MPI_COMM_WORLD, &nNumTasks);

	// Get my rank in the communicator
	MPI_Comm_rank(MPI_COMM_WORLD, &nMyRank);

	// Check to make sure we have an even # tasks (code will not work properly with odd #)
	if (nNumTasks % 2 != 0) {

		// Only one task reports
		if (nMyRank == N_ROOT_RANK) {
			printf("Cannot run with %d tasks - must have an even number!\n", nNumTasks);
		}

		// All tasks exit
		shutDown();

	}
	else {

	    // Declare constants
	    const int N_US_IN_SECOND = 1000000;

		// Declare variables
		int             nPartnerRank;
		int             nCurrentProcessorHostNameLength;
		int             nIteratorMessageSize;
		int             nIteratorTrial;
		int             nIteratorTrialInner;
		int             nIteratorResultsSend;
		int             nIteratorPair;
		int             nStartIndexForResults;

		double          nResponseTimeSum;
		double          nResponseTimeMean;
		double          nResponseTimeVariance;
		double          nResponseTimeStDev;

		int             anMessageSizes_Bytes[N_MESSAGE_SIZES];

		double          anResponseTimes_s[N_TRIALS];

		char            sCurrentProcessorHostName[MPI_MAX_PROCESSOR_NAME];

		MPI_Status      oMessageStatus;

        struct timeval  oStartTime;
        struct timeval  oEndTime;
	    long int        nStartTime_us;
	    long int        nEndTime_us;
	    double          nElapsedTime_s;

		// Determine how many numbers will be reported from each pair, for the results
		nResultsReportedFromEachPair = 2 * N_MESSAGE_SIZES;

		// Dynamically allocate memory
		if (allocateMemory() == 1) {

			// Get some information about the host I'm running on
			MPI_Get_processor_name(sCurrentProcessorHostName, &nCurrentProcessorHostNameLength);

			// Populate array of message sizes (start at min size, double each time, end at max size)
			for (nIteratorMessageSize = 0; nIteratorMessageSize < N_MESSAGE_SIZES; nIteratorMessageSize++) {
				if (nIteratorMessageSize == 0) {
					anMessageSizes_Bytes[nIteratorMessageSize] = pow(2,N_MIN_MESSAGE_SIZE_EXP);
				}
				else {
					anMessageSizes_Bytes[nIteratorMessageSize] = anMessageSizes_Bytes[nIteratorMessageSize-1] * 2;
				}
			}

			// Iterate through each of the message sizes, with several trials per message size
			for (nIteratorMessageSize = 0; nIteratorMessageSize < N_MESSAGE_SIZES; nIteratorMessageSize++) {

				/* Iterate through the trials
                 * For each message size, perform an extra (ignored) RTT to start.
                 * This keeps any overhead associated with starting communication in a par,
                 *  or with allocating more buffer space behind the scenes,
                 *  from skewing our results.
				 */
				for (nIteratorTrial = 0; nIteratorTrial < (N_TRIALS + 1); nIteratorTrial++) {

					// Initiate round-trip from even-rank tasks
					if (nMyRank % 2 == 0) {

                        // 0 partners to 1, 2 partners to 3, etc.
                        nPartnerRank = nMyRank + 1;

						// Get start time (not on ignored first trial)
						if (nIteratorTrial > 0) {
						    // Change from MPI_Wtime to gettimeofday per change to assignment
				            gettimeofday(&oStartTime, NULL);
				            nStartTime_us = (oStartTime.tv_sec * N_US_IN_SECOND) + oStartTime.tv_usec;
						}

						// Send a message to partner (blocking, synchronous)
						MPI_Ssend(
							pPointerToSendBufferRTT,
							anMessageSizes_Bytes[nIteratorMessageSize],
							MPI_BYTE,
							nPartnerRank,
							N_TAG_RTT,
							MPI_COMM_WORLD
						);

						// Wait for response from partner
						MPI_Recv(
							pPointerToRecvBufferRTT,
							anMessageSizes_Bytes[nIteratorMessageSize],
							MPI_BYTE,
							nPartnerRank,
							N_TAG_RTT,
							MPI_COMM_WORLD,
							&oMessageStatus
						);

						// Calculate and report results (not on ignored first trial)
						if (nIteratorTrial > 0) {

							/* Calculate elapsed time
							 * Change from MPI_Wtime to gettimeofday per change to assignment
							 */
                            gettimeofday(&oEndTime, NULL);
                            nEndTime_us = (oEndTime.tv_sec * N_US_IN_SECOND) + oEndTime.tv_usec;
							nElapsedTime_s = (double) (nEndTime_us - nStartTime_us) / N_US_IN_SECOND;
							anResponseTimes_s[nIteratorTrial-1] = nElapsedTime_s;

							// Calculate & report results on last trial for any given message size
							if (nIteratorTrial == N_TRIALS) {

								// Calculate
								nResponseTimeSum = 0;
								for (nIteratorTrialInner = 0; nIteratorTrialInner < N_TRIALS; nIteratorTrialInner++) {
									nResponseTimeSum += anResponseTimes_s[nIteratorTrialInner];
								}
								nResponseTimeMean = nResponseTimeSum / N_TRIALS;
								nResponseTimeVariance = 0;
								for (nIteratorTrialInner = 0; nIteratorTrialInner < N_TRIALS; nIteratorTrialInner++) {
									nResponseTimeVariance +=
									        pow(
                                                (anResponseTimes_s[nIteratorTrialInner] - nResponseTimeMean),
                                                2
                                            );
								}
								nResponseTimeVariance = nResponseTimeVariance / N_TRIALS;
								nResponseTimeStDev = sqrt(nResponseTimeVariance);

								/* Store results for this message size in a buffer, to send once all message sizes have been tested
								 * Results are gathered from each pair, and are reported from the even-numbered task in a pair
								 * Results are an array of doubles - for each message size, one for mean RTT, and one for RTT standard deviation
								 */
								nStartIndexForResults = nIteratorMessageSize * 2;
								pPointerToSendBufferResults[nStartIndexForResults] = nResponseTimeMean;
								pPointerToSendBufferResults[nStartIndexForResults + 1] = nResponseTimeStDev;

							}

						}

					}

					// Respond from odd-ranked tasks
					else {

						// 0 partners to 1, 2 partners to 3, etc.
						nPartnerRank = nMyRank - 1;

						// Wait for message from partner
						MPI_Recv(
							pPointerToRecvBufferRTT,
							anMessageSizes_Bytes[nIteratorMessageSize],
							MPI_BYTE,
							nPartnerRank,
							N_TAG_RTT,
							MPI_COMM_WORLD,
							&oMessageStatus
						);

						// Echo the same message back to the sender
						MPI_Send(
							pPointerToRecvBufferRTT,
							anMessageSizes_Bytes[nIteratorMessageSize],
							MPI_BYTE,
							nPartnerRank,
							N_TAG_RTT,
							MPI_COMM_WORLD
						);

					}

				}
			}

			// Gather results
			gatherResults();

			/* Report final results
			 * What's in the results receive buffer:
			 * 	1st pair 1st message size mean RTT,
			 * 	1st pair 1st message size RTT stdev,
			 * 	1st pair 2nd message size mean RTT,
			 * 	...,
			 * 	2nd pair 1st message size mean RTT,
			 * 	...,
			 * 	Last pair last message size RTT stdev
			 * Report one line per message size
			 * 	Each line has message size, mean RTT from 1st pair, RTT standard deviation from 1st pair, mean RTT from 2nd pair, etc.
			 */
			if (nMyRank == N_ROOT_RANK) {

				// Print header
				printf("Msg Size (Bytes)");
				for (nIteratorPair = 0; nIteratorPair < (nNumTasks / 2); nIteratorPair++) {
					printf("\tPair %d RTT Mean Time (s)", nIteratorPair);
					printf("\tPair %d RTT StDev (s)", nIteratorPair);
				}
				printf("\n");

				// Print contents
				for (nIteratorMessageSize = 0; nIteratorMessageSize < N_MESSAGE_SIZES; nIteratorMessageSize++) {
					printf("%d", anMessageSizes_Bytes[nIteratorMessageSize]);
					for (nIteratorPair = 0; nIteratorPair < (nNumTasks / 2); nIteratorPair++) {

						// Get start index within receive buffer for the two values associated with this RTT pair
						nStartIndexForResults = pPointerToResultDisplacements[nIteratorPair * 2] + (nIteratorMessageSize * 2);

						// Print the results for this RTT pair
						printf("\t%f", pPointerToRecvBufferResults[nStartIndexForResults]);
						printf("\t%f", pPointerToRecvBufferResults[nStartIndexForResults + 1]);

					}
					printf("\n");
				}

			}

			// Shut down
			shutDown();

		}
		else {
			shutDown();
		}

	}

}

/******************************************************************************
 * FUNCTION:		allocateMemory
 *
 * DESCRIPTION:		Dynamically allocate memory
 * 					(what is needed depends upon task rank)
 *
 * RETURN:			bSuccess - 1 if we succeeded, otherwise 0
 *
 * AUTHOR:			attiffan	Aurora T. Tiffany-Davis
 ******************************************************************************/
int allocateMemory () {

	// Declare variables
	int nMaxMessageSize_Bytes;
	int nIteratorTask;
	int bSuccess;

	// Assume success until we know otherwise
	bSuccess = 1;

	// RTT Send - Only even numbered tasks (odd numbered tasks simply echo recv buffer)
	nMaxMessageSize_Bytes = pow(2, N_MAX_MESSAGE_SIZE_EXP);
	if (nMyRank % 2 == 0) {
		pPointerToSendBufferRTT = malloc(nMaxMessageSize_Bytes);
		if (pPointerToSendBufferRTT == NULL) {
			printf("Task rank %d could not allocate memory for RTT send buffer", nMyRank);
			bSuccess = 0;
		}
	}

	// RTT Receive - All tasks participate in RTT
		pPointerToRecvBufferRTT = malloc(nMaxMessageSize_Bytes);
	if (pPointerToRecvBufferRTT == NULL) {
		printf("Task rank %d could not allocate memory for RTT receive buffer", nMyRank);
		bSuccess = 0;
	}

	/* Results send - All tasks need this
	 * Even if they are not tasks that calculate and send results!
	 * Per https://www.open-mpi.org/doc/v2.1/man3/MPI_Gatherv.3.php,
	 * "The outcome is as if each process, including the root process, sends a message to the root..."
	 * Even though this might be ignored / garbage for some tasks given the contents of the result counts buffer
	 * And indeed when we fail to allocate a buffer for all tasks, we get
	 * "MPI_Gatherv failed(sbuf=(nil)... Null buffer pointer..."
	 */
	nSizeOfSendBufferResults_Bytes = sizeof(double) * nResultsReportedFromEachPair;
	pPointerToSendBufferResults = malloc(nSizeOfSendBufferResults_Bytes);
	if (pPointerToSendBufferResults == NULL) {
		printf("Task rank %d could not allocate memory for results send buffer", nMyRank);
		bSuccess = 0;
	}

	// Results receive & associated buffers - Only the root task gathers results
	if (nMyRank == N_ROOT_RANK) {

		// Allocate memory
		nSizeOfRecvBufferResults_Bytes = (nNumTasks / 2) * sizeof(double) * nResultsReportedFromEachPair;
		pPointerToRecvBufferResults = malloc(nSizeOfRecvBufferResults_Bytes);
		if (pPointerToRecvBufferResults == NULL) {
			printf("Task rank %d could not allocate memory for results receive buffer", nMyRank);
			bSuccess = 0;
		}
		pPointerToResultCounts = malloc(nNumTasks * sizeof(int));
		if (pPointerToResultCounts == NULL) {
			printf("Task rank %d could not allocate memory for results count buffer", nMyRank);
			bSuccess = 0;
		}
		pPointerToResultDisplacements = malloc(nNumTasks * sizeof(int));
		if (pPointerToResultDisplacements == NULL) {
			printf("Task rank %d could not allocate memory for results displacement buffer", nMyRank);
			bSuccess = 0;
		}

		// While we're here, go ahead and populate counts and displacements
		for (nIteratorTask = 0; nIteratorTask < nNumTasks; nIteratorTask++) {
			if (nIteratorTask % 2 == 0) {
				// Even numbered tasks report results
				pPointerToResultCounts[nIteratorTask] = nResultsReportedFromEachPair;
				pPointerToResultDisplacements[nIteratorTask] =	nResultsReportedFromEachPair * (nIteratorTask / 2);
			}
			else {
				// Odd numbered tasks do not report results
				pPointerToResultCounts[nIteratorTask] = 0;
				pPointerToResultDisplacements[nIteratorTask] = 0;
			}
		}

	}

	// Return indication of success / failure
	return bSuccess;

}

/******************************************************************************
 * FUNCTION:		gatherResults
 *
 * DESCRIPTION:		Gather the results from every task that has compiled results,
 * 					back to the root task
 *
 * AUTHOR:			attiffan	Aurora T. Tiffany-Davis
 ******************************************************************************/
void gatherResults () {

	// Wait until all tasks get here (helps with debug)
	barrier();

	// Declare variables
	int nIteratorResults;
	int nIteratorTask;
	int nSizeToCopy_Bytes;
	MPI_Status oMessageStatus;

	/* Gather results
	 * Root gathers its own results as well
	 */
	MPI_Gatherv(
		(void *) pPointerToSendBufferResults,
		nResultsReportedFromEachPair,
		MPI_DOUBLE,
		(void *) pPointerToRecvBufferResults,
		pPointerToResultCounts,
		pPointerToResultDisplacements,
		MPI_DOUBLE,
		N_ROOT_RANK,
		MPI_COMM_WORLD
	);

	// Wait until all tasks get here (helps with debug)
	barrier();

}

/******************************************************************************
 * FUNCTION:		shutDown
 *
 * DESCRIPTION:		Shut down (free dynamically allocated memory, finalize MPI)
 *
 * AUTHOR:			attiffan	Aurora T. Tiffany-Davis
 ******************************************************************************/
void shutDown () {

	// Wait until all tasks get here (helps with debug)
	barrier();

	// Free dynamically allocated memory
	if (pPointerToSendBufferRTT != NULL) {
		free(pPointerToSendBufferRTT);
	}
	if (pPointerToRecvBufferRTT != NULL) {
		free(pPointerToRecvBufferRTT);
	}
	if (pPointerToSendBufferResults != NULL) {
		free(pPointerToSendBufferResults);
	}
	if (pPointerToRecvBufferResults != NULL) {
		free(pPointerToRecvBufferResults);
	}
	if (pPointerToResultCounts != NULL) {
		free(pPointerToResultCounts);
	}
	if (pPointerToResultDisplacements != NULL) {
		free(pPointerToResultDisplacements);
	}

	// Finalize MPI
	MPI_Finalize();

}

/******************************************************************************
 * FUNCTION:		barrier
 *
 * DESCRIPTION:		Wait until all tasks reach this point, then wait some more
 * 					What a terrible idea, waiting in system geared for efficiency
 * 					Therefore this function should be called only when all results exist
 * 					and are ready to be gathered / reported
 * 					This is a time where we might wish to do lots of debug "printf"
 * 					and this function exists for sanity's sake
 * 					as it is much easier to see what's going on
 * 					if everyone starts a given work item of interest
 * 					around the same time and "printf" outputs
 * 					are not stepping over each other
 *
 * ARGUMENTS:		pPointerToBuffer -	A pointer to the buffer
 * 					nNumDoubles -		The quantity of double values to report
 *
 * AUTHOR:			attiffan	Aurora T. Tiffany-Davis
 ******************************************************************************/
void barrier () {

	// Wait until all tasks reach this point
	MPI_Barrier(MPI_COMM_WORLD);

	// Wait some more
	sleep(1);

}

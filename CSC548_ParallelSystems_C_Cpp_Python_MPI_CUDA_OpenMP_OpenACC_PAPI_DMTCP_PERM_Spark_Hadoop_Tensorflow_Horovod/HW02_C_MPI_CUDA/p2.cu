/*************************************************************************************************************
 * FILE:            p2.cu
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     A CUDA program that calculates an approximate value for PI using Monte Carlo methods.
 *
 * TO RUN:          srun -N1 -n1 -p opteron --pty /bin/bash
 *					make -f p2.Makefile
 *                  ./p2 [number of iterations for Monte Carlo simulation]
 *************************************************************************************************************/

// INCLUDES
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <curand.h>
#include <curand_kernel.h>

// DEFINES
#define N_NUM_BLOCKS            64
#define N_NUM_THREADS_PER_BLOCK 512

/*************************************************************************************************************
 * FUNCTION:        iterateMonteCarlo
 *
 * DESCRIPTION:     GPU kernel that iterates through part of a Monte Carlo simulation for approximating PI.
 *
 * ARGUMENTS:       nThreads -      Number of threads which are participating in simulation
 *                  nIterations -   Total number of iterations to run
 *                  aCounts -       Pointer to an array of "raindrop" counts
 *
 * AUTHOR:          attiffan        Aurora T. Tiffany-Davis
 *************************************************************************************************************/
__global__ void iterateMonteCarlo (int nThreads, int nIterations, int *aCounts) {

    // Declare variables
    int         i;
    int         nThreadID;
    int         nCount;
    double      nX;
    double      nY;
    double      nZ;
    curandState state;

    // Initialize variables
    nThreadID = threadIdx.x + blockIdx.x * blockDim.x;
    nCount = 0;

    // If this thread is meant to participate, proceed
    if (nThreadID < nThreads) {

        // Initialize random number generator
        curand_init(clock64(), nThreadID, 0, &state);

        // Iterate through part of a Monte Carlo simulation, counting how many "raindrops" land in circle
        for (i = nThreadID; i < nIterations; i += nThreads) {

            nX = (double) curand_uniform(&state);
            nY = (double) curand_uniform(&state);
            nZ = (nX * nX) + (nY * nY);
            if (nZ <= 1) {
                nCount++;
            }

        }

        // Put your count in the counts array by thread index
        aCounts[nThreadID] = nCount;

    }

}

/*************************************************************************************************************
 * FUNCTION:        main
 *
 * DESCRIPTION:     Calculate an approximate value for PI using Monte Carlo methods.
 *
 * ARGUMENTS:       0 -         (as always, name of program)
 *                  1 -         Number of iterations to run
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
int main(int argc, char** argv) {

    // Declare variables (CPU)
    double nPI;
    int i;
    int nTotalCount;
    int nTotalThreads = N_NUM_BLOCKS * N_NUM_THREADS_PER_BLOCK;
    int nIterations;
    int aCounts_h[nTotalThreads];

    // Declare variables (GPU)
    int *aCounts_d;

    // Initialize variables
    nIterations = atoi(argv[1]);
    nTotalCount = 0;

    // Allocate memory (GPU)
    cudaMalloc((void **) &aCounts_d, sizeof(int) * nTotalThreads);

    // Run Monte Carlo simulation on GPU
    iterateMonteCarlo <<< N_NUM_BLOCKS, N_NUM_THREADS_PER_BLOCK >>> (nTotalThreads, nIterations, aCounts_d);

    // Copy back from GPU to CPU
    cudaMemcpy(&aCounts_h, aCounts_d, sizeof(int) * nTotalThreads, cudaMemcpyDeviceToHost);

    // Get total count of "raindrops" that fell within circle
    for (i = 0; i < nTotalThreads; i++) {
        nTotalCount += aCounts_h[i];
    }

    // Calculate approximate value of pi based on relationship between square and inscribed circle
    nPI = (double) nTotalCount / nIterations * 4;

    // Print results
    printf("# of trials= %d, estimate of pi is %.16f \n", nIterations, nPI);

    // Free memory (GPU)
    cudaFree(aCounts_d);

}

/*************************************************************************************************************
 * FILE:            lakegpu.cu
 *
 * AUTHORS:	        attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *
 * DESCRIPTION:	    Assist with modeling the surface of a lake,
 *                      where some pebbles have been thrown onto the surface.
 *                  The energy level at any point on the lake is influenced by
 *                      the energy level on that point in the past,
 *                      and by the current energy levels at neighboring points.
 *                  This program takes into account all 8 neighboring points,
 *                      and parallelizes the simulation by using EXACTLY ONE compute node,
 *                      using multiple GPU threads.
 *
 * TO RUN:          srun -N1 -n1 -p opteron -x c[53,101,102] --pty /bin/bash
 *                  make -f p3.Makefile lake
 *                  prun ./lake [lake size] [# pebbles] [duration of simulation in seconds] [# GPU threads]
 *************************************************************************************************************/

// INCLUDES
#include <stdlib.h>
#include <stdio.h>
#include <cuda_runtime.h>
#include <time.h>

// DEFINES
#define __DEBUG
#define TSCALE  1.0
#define VSQR    0.1

/*************************************************************************************************************
 * FUNCTION:        kf
 *
 * DESCRIPTION:     Get the energy impact of a given pebble size on the lake based on time.
 *                  Impact decreases as time increases.
 *
 * ARGUMENTS:       nPebbleSize -   The size of a given pebble
 *                  nTime -         The amount of time that has elapsed in the simulation
 *
 * AUTHORS:         ssbehera    Subhendu S. Behera
 *************************************************************************************************************/
__device__ double kf(double nPebbleSize, double nTime)
{
  return -expf(-TSCALE * nTime) * nPebbleSize;
}

/*************************************************************************************************************
 * FUNCTION:        evolve
 *
 * DESCRIPTION:     Update the energy levels in the lake for every lake point.
 *                  Each lake point's new energy level depends upon
 *                  old energy levels and the energy levels of neighboring points.
 *                  This version uses 9 points (point of interest and 8 neighboring points).
 *
 * ARGUMENTS:       aDeviceEnergy -         Array representing new energy levels at every point in the lake
 *                  aDeviceEnergyStep0 -    Array representing current energy levels at every point in the lake
 *                  aDeviceEnergyStep1 -    Array representing old energy levels at every point in the lake
 *                  aDevicePebbleSizes -    Array representing the pebble sizes at every point in the lake (sparse)
 *                  nTime -                 The amount of time that has elapsed in the simulation
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *                  timeStep -              The amount of time between one simulation step and the next
 *                  nPointSpacing -         The spacing between two points on the lake
 *
 * RETURNS:         None
 *
 * AUTHOR:          ssbehera    Subhendu S. Behera
 *************************************************************************************************************/
__global__ void evolve(
    double *aDeviceEnergy,
    double *aDeviceEnergyStep0,
    double *aDeviceEnergyStep1,
    double *aDevicePebbleSizes,
    float nTime,
    int nLakePointsOneAxis,
    float timeStep,
    double nPointSpacing
)
{
	int idx, idy;
	int nIndex;

	/*
	 * calculate idx & idy.
	 */
	idx = threadIdx.x + blockIdx.x * blockDim.x;
	idy = threadIdx.y + blockIdx.y * blockDim.y;

	if (idx <= nLakePointsOneAxis - 1 && idy <= nLakePointsOneAxis - 1) {
		/*
		 * calculate energy only if you are inside the lake.
		 */
		nIndex = idx * nLakePointsOneAxis + idy;
		if (idx == 0 || idx == nLakePointsOneAxis - 1 || idy == 0 || idy == nLakePointsOneAxis - 1)
			aDeviceEnergy[nIndex] = 0.;
		else
			aDeviceEnergy[nIndex] = 2 * aDeviceEnergyStep1[nIndex] - aDeviceEnergyStep0[nIndex] +
									VSQR * (timeStep * timeStep) * ((aDeviceEnergyStep1[nIndex - 1] +
									aDeviceEnergyStep1[nIndex + 1] + aDeviceEnergyStep1[nIndex + nLakePointsOneAxis]
									+ aDeviceEnergyStep1[nIndex - nLakePointsOneAxis] + 0.25 *
									(aDeviceEnergyStep1[nIndex + nLakePointsOneAxis - 1] +
									 aDeviceEnergyStep1[nIndex + nLakePointsOneAxis + 1] +
									 aDeviceEnergyStep1[nIndex - nLakePointsOneAxis - 1] +
									 aDeviceEnergyStep1[nIndex - nLakePointsOneAxis + 1]) -
									5 * aDeviceEnergyStep1[nIndex]) /
									(nPointSpacing * nPointSpacing) + kf(aDevicePebbleSizes[nIndex], nTime));
	}

}

/*************************************************************************************************************
 * FUNCTION:        run_gpu
 *
 * DESCRIPTION:     Simulate the energy changes over time in the lake, on the GPU
 *
 * ARGUMENTS:       aEnergy -               Array representing energy levels at every point in the lake
 *                  aEnergyStep0 -          Array representing energy levels at every point in the lake at time 0
 *                  aEnergyStep1 -          Array representing energy levels at every point in the lake at time 1
 *                  aPebbleSizes -          Array representing the pebble sizes at every point in the lake (sparse)
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *                  nPointSpacing -         The spacing between two points on the lake
 *                  nFinishTime -           The duration of the simulation in seconds
 *                  nThreads -              The number of threads to be used per block
 *                                              for instance, with nthreads=8,
 *                                              and a domain of grid points (nLakePointsOneAxis=128 x 128),
 *                                              you will create (nLakePointsOneAxis/nthreads)x(nLakePointsOneAxis/nthreads) = (16 x 16) blocks,
 *                                              with (8 x 8) threads on each block.
 *
 * RETURNS:         None
 *
 * AUTHORS:         ssbehera    Subhendu S. Behera
 *************************************************************************************************************/
void run_gpu(
    double *aEnergy,
    double *aEnergyStep0,
    double *aEnergyStep1,
    double *aPebbleSizes,
    int nLakePointsOneAxis,
    double nPointSpacing,
    double nFinishTime,
    int nThreads
)
{
	cudaEvent_t kstart, kstop;
	float ktime, timeStep, nTime = 0.0f;
	double *aDeviceEnergy, *aDeviceEnergyStep0, *aDeviceEnergyStep1, *aDevicePebbleSizes;
	double *aEnergyCurrent, *aEnergyOld;
	int nLakePointsTotal = nLakePointsOneAxis * nLakePointsOneAxis;
	int blockDimension = (nLakePointsOneAxis / nThreads) +
						 (nLakePointsOneAxis % nThreads != 0 ? 1 : 0);
	dim3 threadsPerBlock(nThreads, nThreads);
	dim3 noOfBlocks(blockDimension, blockDimension);

	/*
	 * allocate host memory
	 */
	aEnergyCurrent = (double *)malloc(sizeof(double) * nLakePointsTotal);
	aEnergyOld = (double *)malloc(sizeof(double) * nLakePointsTotal);

	/*
	 * copy the data of energy step 0, step 1 to current & old energy respectively.
	 */
	memcpy(aEnergyOld, aEnergyStep0, sizeof(double) * nLakePointsTotal);
	memcpy(aEnergyCurrent, aEnergyStep1, sizeof(double) * nLakePointsTotal);

	/*
	 * allocate memory on the device.
	 */
	cudaMalloc((void **)&aDeviceEnergy, sizeof(double) * nLakePointsTotal);
	cudaMalloc((void **)&aDeviceEnergyStep0, sizeof(double) * nLakePointsTotal);
	cudaMalloc((void **)&aDeviceEnergyStep1, sizeof(double) * nLakePointsTotal);
	cudaMalloc((void **)&aDevicePebbleSizes, sizeof(double) * nLakePointsTotal);

	/*
	 * setup the timers before copying the memory from host to device.
	 */

	cudaSetDevice(0);
	cudaEventCreate(&kstart);
	cudaEventCreate(&kstop);

	/*
	 * Start recording time.
	 */
	cudaEventRecord(kstart, 0);

	/*
	 * copy the pebblesize data only once to device memory.
	 */
	cudaMemcpy(aDevicePebbleSizes, aPebbleSizes, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis,
			   cudaMemcpyHostToDevice);

	for (timeStep = nPointSpacing / 2; nTime < nFinishTime; nTime += timeStep) {
		/*
		 * copy data
		 */
		cudaMemcpy(aDeviceEnergyStep0, aEnergyOld, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis,
				   cudaMemcpyHostToDevice);
		cudaMemcpy(aDeviceEnergyStep1, aEnergyCurrent, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis,
				   cudaMemcpyHostToDevice);

		/*
	 	 * Make the kernel call.
	 	 */
		evolve<<<noOfBlocks, threadsPerBlock>>>(aDeviceEnergy, aDeviceEnergyStep0,
												aDeviceEnergyStep1, aDevicePebbleSizes,
												nTime, nLakePointsOneAxis, timeStep, nPointSpacing);

		/*
		 * copy the current energy to old energy as cpu is free.
		 */
		memcpy(aEnergyOld, aEnergyCurrent, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);

		/*
		 * copy the new energy to current energy directly from the device.
		 */
		cudaMemcpy(aEnergyCurrent, aDeviceEnergy, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis,
				   cudaMemcpyDeviceToHost);
	}

	memcpy(aEnergy, aEnergyCurrent, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);
	
	/* Stop GPU computation timer */
	cudaEventRecord(kstop, 0);
	cudaEventSynchronize(kstop);
	cudaEventElapsedTime(&ktime, kstart, kstop);
	printf("GPU computation: %f msec\n", ktime);

	/*
	 * Free the device & host memory.
	 */
	free(aEnergyCurrent);
	free(aEnergyOld);
	cudaFree(aDeviceEnergy);
	cudaFree(aDeviceEnergyStep0);
	cudaFree(aDeviceEnergyStep1);
	cudaFree(aDevicePebbleSizes);

	/* timer cleanup */
	cudaEventDestroy(kstart);
	cudaEventDestroy(kstop);
}

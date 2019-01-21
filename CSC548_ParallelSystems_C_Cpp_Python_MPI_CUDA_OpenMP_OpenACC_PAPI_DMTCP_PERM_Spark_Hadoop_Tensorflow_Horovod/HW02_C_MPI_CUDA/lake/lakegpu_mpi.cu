/*************************************************************************************************************
 * FILE:            lakegpu_mpi.cu
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *
 * DESCRIPTION:     Assist with modeling the surface of a slice of a lake,
 *                      where some pebbles have been thrown onto the surface.
 *                  The energy level at any point on the lake is influenced by
 *                      the energy level on that point in the past,
 *                      and by the current energy levels at neighboring points.
 *                  This program takes into account all 8 neighboring points,
 *                      and parallelizes the simulation by using EXACTLY ONE compute node,
 *                      using multiple GPU threads.
 *
 * TO RUN:          srun -N4 -n4 -p opteron -x c[53,101,102] --pty /bin/bash
 *                  make -f p3.Makefile lake-mpi
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

// Declare globals
double *aDeviceEnergy, *aDeviceEnergyStepOld, *aDeviceEnergyStepCurrent, *aDevicePebbleSizes;

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
 * DESCRIPTION:     Update the energy levels in this node's slice of the lake for every lake point therein.
 *                  Each lake point's new energy level depends upon
 *                  old energy levels and the energy levels of neighboring points.
 *                  This version uses 9 points (point of interest and 8 neighboring points).
 *                  This code runs on 1 thread and is responsible for updating the energy level of 1 lake point.
 *
 * ARGUMENTS:       aDeviceEnergy -             Array representing new energy levels at every point in the lake
 *                  aDeviceEnergyStepOld -      Array representing current energy levels at every point in the lake
 *                  aDeviceEnergyStepCurrent -  Array representing old energy levels at every point in the lake
 *                  aDevicePebbleSizes -        Array representing the pebble sizes at every point in the lake (sparse)
 *                  nTime -                     The amount of time that has elapsed in the simulation
 *                  nLakePointsOneAxis -                  Number of lake points in this node's slice of the lake, on the x axis
 *                  nPointsY -                  Number of lake points in this node's slice of the lake, on the y axis
 *                  nTimeStep -                  The amount of time between one simulation step and the next
 *                  nPointSpacing -             The spacing between two points on the lake
 *
 * RETURNS:         None
 *
 * AUTHOR:          ssbehera    Subhendu S. Behera
 *                  attiffan    Aurora T. Tiffany-Davis
 *************************************************************************************************************/
__global__ void evolve(
    double *aDeviceEnergy,
    double *aDeviceEnergyStepOld,
    double *aDeviceEnergyStepCurrent,
    double *aDevicePebbleSizes,
    double nTime,
    int nPointsX,
	int nPointsY,
    double nTimeStep,
    double nPointSpacing,
	int nMyRank,
	int nNumTaskPoints
)
{

    // Declare variables
	int nIndexInMemory;

	int idx, idy;

	// Calculate thread index
	idx = threadIdx.x + blockIdx.x * blockDim.x;
	idy = threadIdx.y + blockIdx.y * blockDim.y;

	int nLakePointsOneAxis = nPointsX;

	// If the thread is actually needed to help with the calculation
	if (idx <= nPointsX - 1 && idy <= nPointsY - 1) {
		nIndexInMemory = (idy + 1) * (nPointsX) + idx;

		if (idy == 0 && nMyRank == 0) {
			aDeviceEnergy[nIndexInMemory] = 0.;
		}
		else if (idy == nPointsY - 1 && nMyRank == 3)
			aDeviceEnergy[nIndexInMemory] = 0.;
		else if (idx == 0 || idx == nPointsX - 1)
			aDeviceEnergy[nIndexInMemory] = 0.;
		else {
        aDeviceEnergy[nIndexInMemory] =
            2 * aDeviceEnergyStepCurrent[nIndexInMemory] -
            aDeviceEnergyStepOld[nIndexInMemory] +
            VSQR * (nTimeStep * nTimeStep) * (
                (
                    aDeviceEnergyStepCurrent[nIndexInMemory - 1] +
                    aDeviceEnergyStepCurrent[nIndexInMemory + 1] +
                    aDeviceEnergyStepCurrent[nIndexInMemory + nLakePointsOneAxis] +
                    aDeviceEnergyStepCurrent[nIndexInMemory - nLakePointsOneAxis] +
                    0.25 * (
                        aDeviceEnergyStepCurrent[nIndexInMemory + nLakePointsOneAxis - 1] +
                        aDeviceEnergyStepCurrent[nIndexInMemory + nLakePointsOneAxis + 1] +
                        aDeviceEnergyStepCurrent[nIndexInMemory - nLakePointsOneAxis - 1] +
                        aDeviceEnergyStepCurrent[nIndexInMemory - nLakePointsOneAxis + 1]
                    ) -
                    5 * aDeviceEnergyStepCurrent[nIndexInMemory]
                ) /
                (nPointSpacing * nPointSpacing) +
                kf(aDevicePebbleSizes[nIndexInMemory], nTime)
            );

		}
	}

}

/*************************************************************************************************************
 * FUNCTION:        gpu_memory_setup
 *
 * DESCRIPTION:     Allocate memory used by the GPU, and copy over pebble sizes array
 *                      (which does not change during execution).
 *
 * ARGUMENTS:       nNumTaskPointsWithBoundaries -  The total number of lake points that this node cares about
 *                                                  Includes this node's own lake points,
 *                                                      plus boundaries above / north and below / south
 *                  aPebbleSizes -                  Array representing pebbles in this node's slice of the lake
 *
 * RETURNS:         None
 *
 * AUTHOR:          ssbehera    Subhendu S. Behera
 *************************************************************************************************************/
void gpu_memory_setup(int nNumTaskPointsWithBoundaries, double *aPebbleSizes)
{

	cudaMalloc((void **)&aDeviceEnergy, sizeof(double) * nNumTaskPointsWithBoundaries);
	cudaMalloc((void **)&aDeviceEnergyStepOld, sizeof(double) * nNumTaskPointsWithBoundaries);
	cudaMalloc((void **)&aDeviceEnergyStepCurrent, sizeof(double) * nNumTaskPointsWithBoundaries);
	cudaMalloc((void **)&aDevicePebbleSizes, sizeof(double) * nNumTaskPointsWithBoundaries);
}

/*************************************************************************************************************
 * FUNCTION:        gpu_memory_free
 *
 * DESCRIPTION:     Free memory allocated by the GPU
 *
 * ARGUMENTS:       None
 *
 * RETURNS:         None
 *
 * AUTHOR:          ssbehera    Subhendu S. Behera
 *************************************************************************************************************/
void gpu_memory_free(void)
{
	/*
	 * Free the device memory.
	 */
	cudaFree(aDeviceEnergy);
	cudaFree(aDeviceEnergyStepOld);
	cudaFree(aDeviceEnergyStepCurrent);
	cudaFree(aDevicePebbleSizes);
}

/*************************************************************************************************************
 * FUNCTION:        run_gpu
 *
 * DESCRIPTION:     Simulate the energy changes over time in this node's slice of the lake, on the GPU
 *
 * ARGUMENTS:       aEnergyStepOld -                Array representing energy levels at every point in the lake at the previous time step
 *                  aEnergyStepCurrent -            Array representing energy levels at every point in the lake at the current time step
 *                  nLakePointsOneAxis -            The number of points in the map of the lake (one axis)
 *                  nNumTaskPointsWithBoundaries -  The total number of lake points that this node cares about
 *                                                  Includes this node's own lake points,
 *                                                      plus boundaries above / north and below / south
 *                  nPointSpacing -                 The spacing between two points on the lake
 *                  nTime -                         The current time value within the lake simulation
 *                  nThreads -                      The number of threads to be used per axis of 2D block (if block is 2D)
 *                                                  The intention is to have the total number of threads
 *                                                  equal the total number of lake points that this node is responsible for
 *
 * RETURNS:         None
 *
 * AUTHORS:         ssbehera    Subhendu S. Behera
 *************************************************************************************************************/
void run_gpu(
    double *aEnergyStepOld,
    double *aEnergyStepCurrent,
    int nLakePointsOneAxis,
    int nNumTaskPointsWithBoundaries,
    double nPointSpacing,
    double nTime,
    int nThreads, int nMyRank,
	int nNumTasks,
	double *aPebbleSizes
)
{

    // Declare variables
    double nTimeStep;
    int nNumTaskPoints;
	int nPointsY;

	/* Calculate block dimensions
	 *  One drawback of our design decision to split up the lake in slices instead of quadrants
	 *  is that this could result in some unused threads
	 */
	int blockDimensionX = (nLakePointsOneAxis / nThreads);
	if (blockDimensionX == 0)
		blockDimensionX++;

	int blockDimensionY = nLakePointsOneAxis / nNumTasks / nThreads;
	if (blockDimensionY == 0)
		blockDimensionY++;

	dim3 threadsPerBlock(nThreads, nThreads);
	dim3 noOfBlocks(blockDimensionX, blockDimensionY);


    // Calculate time step
	nTimeStep = nPointSpacing / 2;
	
	nNumTaskPoints = nNumTaskPointsWithBoundaries - (2 * nLakePointsOneAxis);

	nPointsY = nLakePointsOneAxis / nNumTasks;


	/*
	 * copy data
	 */
	cudaMemcpy(aDeviceEnergyStepOld, aEnergyStepOld, sizeof(double) * nNumTaskPointsWithBoundaries,
			   cudaMemcpyHostToDevice);
	cudaMemcpy(aDeviceEnergyStepCurrent, aEnergyStepCurrent, sizeof(double) * nNumTaskPointsWithBoundaries,
			   cudaMemcpyHostToDevice);

 	cudaMemcpy(aDevicePebbleSizes, aPebbleSizes, sizeof(double) * nNumTaskPointsWithBoundaries,
               cudaMemcpyHostToDevice);


	/*
	 * Make the kernel call.
	 */
	//nPointsY = nNumTaskPoints / nLakePointsOneAxis;
	evolve<<<noOfBlocks, threadsPerBlock>>>(
        aDeviceEnergy,
        aDeviceEnergyStepOld,
        aDeviceEnergyStepCurrent,
        aDevicePebbleSizes,
        nTime,
        nLakePointsOneAxis,
		nPointsY,
        nTimeStep,
        nPointSpacing,
		nMyRank,
		nNumTaskPoints
    );

	/*
	 * copy the current energy to old energy as cpu is free.
	 */
	memcpy(aEnergyStepOld, aEnergyStepCurrent, sizeof(double) * nNumTaskPointsWithBoundaries);

	/*
	 * copy the new energy to current energy directly from the device.
	 */
	cudaMemcpy(aEnergyStepCurrent + nLakePointsOneAxis, aDeviceEnergy + nLakePointsOneAxis, sizeof(double) * nNumTaskPoints,
			   cudaMemcpyDeviceToHost);
}

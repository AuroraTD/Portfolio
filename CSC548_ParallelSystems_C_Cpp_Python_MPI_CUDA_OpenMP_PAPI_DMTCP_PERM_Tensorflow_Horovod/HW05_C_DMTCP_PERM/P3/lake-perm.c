/*************************************************************************************************************
 * FILE:            lake.cu
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *
 * DESCRIPTION:     Model the surface of a lake, where some pebbles have been thrown onto the surface.
 *                  The energy level at any point on the lake is influenced by
 *                      the energy level on that point in the past,
 *                      and by the current energy levels at neighboring points.
 *                  This program takes into account all 8 neighboring points,
 *                      and parallelizes the simulation by using EXACTLY ONE compute node,
 *                      optionally using multiple GPU threads.
 *                  Based upon the original lake.cu file, with added comments and more descriptive variable names.
 *
 * HW05 CHANGES     Per https://moodle-courses1819.wolfware.ncsu.edu/mod/forum/discuss.php?d=125780,
 *                      use hard-coded seed in the program for random number generation
 *                  Per https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw5/,
 *                      use default values of npoints = 128, npebs = 8, and end_time = 1.0
 *
 * TO RUN:          srun -N 1 -n 16 -p opteron -x c[53,101,102] --pty /bin/bash
 *                  wget --no-check-certificate https://computation.llnl.gov/projects/memory-centric-architectures/download/perm-je-0.9.7.tgz
 *                  tar xzvf perm-je-0.9.7.tgz
 *                  cd perm-je-0.9.7/
 *                  ./configure
 *                  make
 *                  export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$PWD/lib
 *                  cd ..
 *                  make lake
 *                  ./lake-perm
 *************************************************************************************************************/

// FUNCTION PROTOTYPES
void initialize_energy (double *aEnergy, double *aPebbleSizes, int nLakePointsOneAxis);
void evolve (
    double *aEnergyNew,
    double *aEnergyCurrent,
    double *aEnergyOld,
    double *aPebbleSizes,
    int nLakePointsOneAxis,
    double nPointSpacing,
    double nTimeStep,
    double nTime
);
void evolve9pt (
    double *aEnergyNew,
    double *aEnergyCurrent,
    double *aEnergyOld,
    double *aPebbleSizes,
    int nLakePointsOneAxis,
    double nPointSpacing,
    double nTimeStep,
    double nTime
);
int update_time (double *nTime, double nTimeStep, double nFinishTime);
void print_heatmap (const char *sFilename, double *aEnergy, int nLakePointsOneAxis, double nPointSpacing);
void initialize_pebbles (double *aPebbleSizes, int nPebbles, int nLakePointsOneAxis);
void run_cpu ();
double east (double *aEnergyCurrent, int nIndex);
double west (double *aEnergyCurrent, int nIndex);
double north (double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis);
double northeast (double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis);
double northwest (double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis);
double south (double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis);
double southeast (double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis);
double southwest (double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis);
double get_pebble_impact (double nPebbleSize, double nTime);

// INCLUDES
#include <stdlib.h>
#include <stdio.h>
#include <stddef.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <sys/time.h>
#include <unistd.h>
#include <ctype.h>
#include "jemalloc/jemalloc.h"

#define BACK_FILE "/tmp/app.back"
#define MMAP_FILE "/tmp/app.mmap"
#define MMAP_SIZE ((size_t)1 << 30)

// DEFINES
#define _USE_MATH_DEFINES
#define XMIN    0.0
#define XMAX    1.0
#define YMIN    0.0
#define YMAX    1.0
#define MAX_PSZ 10
#define TSCALE  1.0
#define VSQR    0.1

// Hack to get around a problem I have in my IDE setup
#ifndef NULL
    #define NULL ((void *) 0)
#endif

// Declare global variables
int nLakePointsOneAxis, nPebbles, nArea;
double nTimeStep, nFinishTime, nPointSpacing;

/* Declare PERM variables
 *  nTime -             Altered within run_cpu while loop
 *  aPebbleSizes -      Checkpointing specifically required by
 *                      https://moodle-courses1819.wolfware.ncsu.edu/mod/forum/discuss.php?d=125780
 *  aEnergyCurrent -    Altered within run_cpu while loop
 *  aEnergyOld -        Altered within run_cpu while loop
 *  aEnergyNew -        Altered within run_cpu while loop
 */
PERM double nTime;
PERM double *aPebbleSizes, *aEnergyCurrent, *aEnergyOld, *aEnergyNew;

/*************************************************************************************************************
 * FUNCTION:        main
 *
 * DESCRIPTION:     Model the surface of a lake, where some pebbles have been thrown onto the surface.
 *                  This work is parallelized by performing work on multiple GPU threads.
 *
 * ARGUMENTS:       "-r" to restore from checkpoint, or no argument to run normally
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
int main(int argc, char *argv[])
{

    // Must be called before any malloc
    int do_restore = argc > 1 && strcmp("-r", argv[1]) == 0;
    const char *mode = (do_restore) ? "r+" : "w+";
    perm(PERM_START, PERM_SIZE);
    mopen(MMAP_FILE, mode, MMAP_SIZE);
    bopen(BACK_FILE, mode);

    // Declare variables
    double nElapsedTimeCPU, nElapsedTimeGPU;
    struct timeval nStartTimeCPU, nEndTimeCPU, nStartTimeGPU, nEndTimeGPU;

    // Initial values that we need whether we are running from start or from restart
    nLakePointsOneAxis =    128;
    nPointSpacing =         (XMAX - XMIN)/ nLakePointsOneAxis;
    nTimeStep =             nPointSpacing / 2.;
    nPebbles =              8;
    nFinishTime =           1.0;
    nArea =                 nLakePointsOneAxis * nLakePointsOneAxis;

    // Initialize PERM variables
    if (!do_restore) {

        // Initial values
        nTime =             0.;

        // Memory allocation
        aPebbleSizes =      (double*)malloc(sizeof(double) * nArea);
        aEnergyCurrent =    (double*)malloc(sizeof(double) * nArea);
        aEnergyOld =        (double*)malloc(sizeof(double) * nArea);
        aEnergyNew =        (double*)malloc(sizeof(double) * nArea);

        // Initial array values
        initialize_pebbles(aPebbleSizes, nPebbles, nLakePointsOneAxis);
        initialize_energy(aEnergyOld, aPebbleSizes, nLakePointsOneAxis);
        initialize_energy(aEnergyCurrent, aPebbleSizes, nLakePointsOneAxis);

        // PERM commands
        mflush();
        backup();

    } else {
        printf("restarting...\n");
        restore();
    }

    // Let the user know what scenario we are running
    printf(
        "Running %s with (%d x %d) grid, with %d pebbles, until %f\n",
        argv[0],
        nLakePointsOneAxis,
        nLakePointsOneAxis,
        nPebbles,
        nFinishTime
    );

    // Print starting heat map to file
    print_heatmap("lake_i.dat", aEnergyOld, nLakePointsOneAxis, nPointSpacing);

    // Run simulation on CPU
    gettimeofday(&nStartTimeCPU, NULL);
    run_cpu();
    gettimeofday(&nEndTimeCPU, NULL);

    // Report how long CPU took
    nElapsedTimeCPU = ((nEndTimeCPU.tv_sec + nEndTimeCPU.tv_usec * 1e-6)-(
                       nStartTimeCPU.tv_sec + nStartTimeCPU.tv_usec * 1e-6));
    printf("Entire job on CPU took %f seconds\n", nElapsedTimeCPU);

    // Print final heat map to file (use same name whether CPU or GPU)
    print_heatmap("lake_f.dat", aEnergyNew, nLakePointsOneAxis, nPointSpacing);

    // Save state
    backup();

    // Free allocated memory
    free(aPebbleSizes);
    free(aEnergyCurrent);
    free(aEnergyOld);
    free(aEnergyNew);

    // Cleanup
    mclose();
    bclose();
    remove(BACK_FILE);
    remove(MMAP_FILE);

    // Return
    return 0;

}

/*************************************************************************************************************
 * FUNCTION:        run_cpu
 *
 * DESCRIPTION:     Simulate the energy changes over time in the lake, on the CPU
 *
 * ARGUMENTS:       None (all needed values are global PERM values)
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void run_cpu() {

    // Run simulation
    while(1)
    {

        // Print time value (recommended by TA)
        printf("Time Value: %f\n", nTime);
        fflush(stdout);

        // Perform a single simulation step
        evolve9pt(
            aEnergyNew,
            aEnergyCurrent,
            aEnergyOld,
            aPebbleSizes,
            nLakePointsOneAxis,
            nPointSpacing,
            nTimeStep,
            nTime
        );
        memcpy(aEnergyOld, aEnergyCurrent, sizeof(double) * nArea);
        memcpy(aEnergyCurrent, aEnergyNew, sizeof(double) * nArea);
        if(!update_time(&nTime,nTimeStep,nFinishTime)) break;

        /* Back up for checkpoint
         *  Per https://computation.llnl.gov/projects/memory-centric-architectures/perm,
         *      for (...) {
         *          Application_Step();
         *          backup();
         *      }
         */
        backup();

    }

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
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void initialize_pebbles(double *aPebbleSizes, int nPebbles, int nLakePointsOneAxis)
{

    // Declare variables
    int i, j, k, nIndex;
    int nPebbleSize;

    // Initialize (use hard-coded seed per https://moodle-courses1819.wolfware.ncsu.edu/mod/forum/discuss.php?d=125780)
    srand(1);
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
 * FUNCTION:        update_time
 *
 * DESCRIPTION:     Update the current time in the simulation
 *
 * ARGUMENTS:       nTime -         The amount of time that has elapsed in the simulation
 *                  nTimeStep -     The amount of time between one simulation step and the next
 *                  nFinishTime -   The total intended duration of the simulation
 *
 * RETURNS:         bKeepGoing -    1 if we should keep going, 0 otherwise
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
int update_time(double *nTime, double nTimeStep, double nFinishTime)
{

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
 * FUNCTION:        initialize_energy
 *
 * DESCRIPTION:     Initialize the energy levels in the lake based solely upon initial drops of pebbles
 *
 * ARGUMENTS:       aEnergy -               Array representing energy levels at every point in the lake
 *                  aPebbleSizes -          Array representing the pebble sizes at every point in the lake (sparse)
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void initialize_energy(double *aEnergy, double *aPebbleSizes, int nLakePointsOneAxis)
{

    // Declare variables
    int i, j, nIndex;

    // Loop through both axes of the lake, initializing energy levels at each point
    for(i = 0; i < nLakePointsOneAxis ; i++)
    {
        for(j = 0; j < nLakePointsOneAxis ; j++)
        {
            nIndex = j + i * nLakePointsOneAxis;
            aEnergy[nIndex] = get_pebble_impact(aPebbleSizes[nIndex], 0.0);
        }
    }

}

/*************************************************************************************************************
 * FUNCTION:        evolve
 *
 * DESCRIPTION:     Update the energy levels in the lake for every lake point.
 *                  Each lake point's new energy level depends upon
 *                  old energy levels and the energy levels of neighboring points.
 *                  This version uses 5 points (point of interest and 4 neighboring points).
 *
 * ARGUMENTS:       aEnergyNew -            Array representing new energy levels at every point in the lake
 *                  aEnergyCurrent -        Array representing current energy levels at every point in the lake
 *                  aEnergyOld -            Array representing old energy levels at every point in the lake
 *                  aPebbleSizes -          Array representing the pebble sizes at every point in the lake (sparse)
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *                  nPointSpacing -         The spacing between two points on the lake
 *                  nTimeStep -             The amount of time between one simulation step and the next
 *                  nTime -                 The amount of time that has elapsed in the simulation
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void evolve(double *aEnergyNew, double *aEnergyCurrent, double *aEnergyOld, double *aPebbleSizes, int nLakePointsOneAxis, double nPointSpacing, double nTimeStep, double nTime)
{

    // Declare variables
    int i, j, nIndex;

    // Loop through both axes of the lake, updating energy levels at each point
    for( i = 0; i < nLakePointsOneAxis; i++)
    {
        for( j = 0; j < nLakePointsOneAxis; j++)
        {
            nIndex = j + i * nLakePointsOneAxis;

            if( i == 0 || i == nLakePointsOneAxis - 1 || j == 0 || j == nLakePointsOneAxis - 1)
            {
                aEnergyNew[nIndex] = 0.;
            }
            else
            {
                aEnergyNew[nIndex] =
                    2*aEnergyCurrent[nIndex] -
                    aEnergyOld[nIndex] +
                    VSQR * (nTimeStep * nTimeStep) * (
                        (
                            west(aEnergyCurrent, nIndex) +
                            east(aEnergyCurrent, nIndex) +
                            north(aEnergyCurrent, nIndex, nLakePointsOneAxis) +
                            south(aEnergyCurrent, nIndex, nLakePointsOneAxis) -
                            4 * aEnergyCurrent[nIndex]
                        ) / (nPointSpacing * nPointSpacing) +
                        get_pebble_impact(aPebbleSizes[nIndex],nTime)
                    );
            }
        }
    }

}

/*************************************************************************************************************
 * FUNCTION:        evolve9pt
 *
 * DESCRIPTION:     Update the energy levels in the lake for every lake point.
 *                  Each lake point's new energy level depends upon
 *                  old energy levels and the energy levels of neighboring points.
 *                  This version uses 9 points (point of interest and 8 neighboring points).
 *
 * ARGUMENTS:       aEnergyNew -            Array representing new energy levels at every point in the lake
 *                  aEnergyCurrent -        Array representing current energy levels at every point in the lake
 *                  aEnergyOld -            Array representing old energy levels at every point in the lake
 *                  aPebbleSizes -          Array representing the pebble sizes at every point in the lake (sparse)
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *                  nPointSpacing -         The spacing between two points on the lake
 *                  nTimeStep -             The amount of time between one simulation step and the next
 *                  nTime -                 The amount of time that has elapsed in the simulation
 *
 * RETURNS:         None
 *
 * AUTHOR:          wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void evolve9pt(double *aEnergyNew, double *aEnergyCurrent, double *aEnergyOld, double *aPebbleSizes, int nLakePointsOneAxis, double nPointSpacing, double nTimeStep, double nTime)
{

    // Declare variables
    int i, j, nIndex;

    // Loop through both axes of the lake, updating energy levels at each point
    for( i = 0; i < nLakePointsOneAxis; i++)
    {
        for( j = 0; j < nLakePointsOneAxis; j++)
        {
            nIndex = j + i * nLakePointsOneAxis;
            if( i == 0 || i == nLakePointsOneAxis - 1 || j == 0 || j == nLakePointsOneAxis - 1)
            {
                aEnergyNew[nIndex] = 0.;
            }
            else
            {
                aEnergyNew[nIndex] =
                    2*aEnergyCurrent[nIndex] -
                    aEnergyOld[nIndex] +
                    VSQR *(nTimeStep * nTimeStep) *(
                        (
                            west(aEnergyCurrent, nIndex) +
                            east(aEnergyCurrent, nIndex) +
                            north(aEnergyCurrent, nIndex, nLakePointsOneAxis) +
                            south(aEnergyCurrent, nIndex, nLakePointsOneAxis) +
                            + 0.25*(
                                northwest(aEnergyCurrent, nIndex, nLakePointsOneAxis) +
                                northeast(aEnergyCurrent, nIndex, nLakePointsOneAxis) +
                                southwest(aEnergyCurrent, nIndex, nLakePointsOneAxis) +
                                southeast(aEnergyCurrent, nIndex, nLakePointsOneAxis)
                            ) -
                            5 * aEnergyCurrent[nIndex]
                        ) / (nPointSpacing * nPointSpacing) +
                        get_pebble_impact(aPebbleSizes[nIndex],nTime)
                    );
            }
        }
    }

}

/*************************************************************************************************************
 * FUNCTION:        print_heatmap
 *
 * DESCRIPTION:     Print (to file) a heat map showing the energy levels in the lake
 *
 * ARGUMENTS:       sFilename -             The name of the heat map file to write
 *                  aEnergy -               Array representing energy levels at every point in the lake
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *                  nPointSpacing -         The spacing between two points on the lake
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void print_heatmap(const char *sFilename, double *aEnergy, int nLakePointsOneAxis, double nPointSpacing)
{

    // Declare variables
    int i, j, nIndex;

    // Open file
    FILE *fp = fopen(sFilename, "w");

    // Loop through both axes of the lake, printing energy level at each point to file
    for( i = 0; i < nLakePointsOneAxis; i++ )
    {
        for( j = 0; j < nLakePointsOneAxis; j++ )
        {
            nIndex = j + i * nLakePointsOneAxis;
            fprintf(fp, "%f %f %f\n", i*nPointSpacing, j*nPointSpacing, aEnergy[nIndex]);
        }
    }

    // Close file
    fclose(fp);

}

/*************************************************************************************************************
 * FUNCTION:        (various)
 *
 * DESCRIPTION:     Get the current energy level of a neighboring point on the lake
 *
 * ARGUMENTS:       aEnergyCurrent -    Array representing current energy levels at every point in the lake
 *                  nIndex -            An index into the above array
 *
 * RETURNS:         (unnamed) -         The energy level at a neighboring point on the lake
 *
 * AUTHORS:         wpmoore2    Wade P. Moore
 *************************************************************************************************************/
double east(double *aEnergyCurrent, int nIndex) {
    return aEnergyCurrent[nIndex+1];
}

double west(double *aEnergyCurrent, int nIndex) {
    return aEnergyCurrent[nIndex-1];
}

double north(double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis) {
    return aEnergyCurrent[nIndex + nLakePointsOneAxis];
}

double northwest(double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis) {
    return aEnergyCurrent[nIndex + nLakePointsOneAxis - 1];
}

double northeast(double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis) {
    return aEnergyCurrent[nIndex + nLakePointsOneAxis + 1];
}

double south(double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis) {
    return aEnergyCurrent[nIndex - nLakePointsOneAxis];
}

double southwest(double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis) {
    return aEnergyCurrent[nIndex - nLakePointsOneAxis - 1];
}

double southeast(double *aEnergyCurrent, int nIndex, int nLakePointsOneAxis) {
    return aEnergyCurrent[nIndex - nLakePointsOneAxis + 1];
}

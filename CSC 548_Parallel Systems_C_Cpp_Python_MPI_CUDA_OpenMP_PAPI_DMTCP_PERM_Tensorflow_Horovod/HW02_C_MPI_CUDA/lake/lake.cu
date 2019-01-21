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
 * TO RUN:          srun -N1 -n1 -p opteron -x c[53,101,102] --pty /bin/bash
 *                  make -f p3.Makefile lake
 *                  prun ./lake [lake size] [# pebbles] [duration of simulation in seconds] [# GPU threads (optional)]
 *************************************************************************************************************/

// FUNCTION PROTOTYPES
int validate_inputs (int argc, char *argv[]);
int is_number (char sPossibleNumber[]);
void input_validation_error (const char *sMessage);
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
void run_cpu (
    double *aEnergy,
    double *aEnergyStep0,
    double *aEnergyStep1,
    double *aPebbleSizes,
    int nLakePointsOneAxis,
    double nPointSpacing,
    double nFinishTime
);
extern void run_gpu (
    double *aEnergy,
    double *aEnergyStep0,
    double *aEnergyStep1,
    double *aPebbleSizes,
    int nLakePointsOneAxis,
    double nPointSpacing,
    double nFinishTime,
    int nThreads
);
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

/*************************************************************************************************************
 * FUNCTION:        main
 *
 * DESCRIPTION:     Model the surface of a lake, where some pebbles have been thrown onto the surface.
 *                  This work is parallelized by performing work on multiple GPU threads.
 *
 * ARGUMENTS:       0 -         Lake size (number of points along one axis)
 *                  1 -         Number of pebbles
 *                  2 -         Duration of the simulation in seconds
 *                  3 -         Number of GPU threads along one axis of each thread block
 *                              (optional - if omitted, run on CPU rather than on GPU)
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
int main(int argc, char *argv[])
{

    // Check arguments
    int bSuccess = validate_inputs(argc, argv);
    if(bSuccess == 1) {

        // Declare variables

        int     nLakePointsOneAxis;
        int     nPebbles;
        double  nFinishTime;
        int     nThreads;
        int     nArea;

        double *aEnergyStep0, *aEnergyStep1;
        double *aEnergyCPU, *aEnergyGPU, *aPebbleSizes;
        double nPointSpacing;

        double nElapsedTimeCPU, nElapsedTimeGPU;
        struct timeval nStartTimeCPU, nEndTimeCPU, nStartTimeGPU, nEndTimeGPU;

        // Save arguments
        nLakePointsOneAxis    = atoi(argv[1]);
        nPebbles              = atoi(argv[2]);
        nFinishTime           = (double)atof(argv[3]);
        if (argc >= 5) {
            nThreads = atoi(argv[4]);
        }
        else {
            nThreads = -1;
        }
        nArea = nLakePointsOneAxis * nLakePointsOneAxis;

        // Allocate memory
        aEnergyStep0 = (double*)malloc(sizeof(double) * nArea);
        aEnergyStep1 = (double*)malloc(sizeof(double) * nArea);
        aPebbleSizes = (double*)malloc(sizeof(double) * nArea);
        aEnergyCPU = (double*)malloc(sizeof(double) * nArea);
        aEnergyGPU = (double*)malloc(sizeof(double) * nArea);

        // Let the user know what scenario we are running
        printf(
            "Running %s with %d threads, with (%d x %d) grid, with %d pebbles, until %f\n",
            argv[0],
            nThreads,
            nLakePointsOneAxis,
            nLakePointsOneAxis,
            nPebbles,
            nFinishTime
        );

        // Initialize
        nPointSpacing = (XMAX - XMIN)/nLakePointsOneAxis;
        initialize_pebbles(aPebbleSizes, nPebbles, nLakePointsOneAxis);
        initialize_energy(aEnergyStep0, aPebbleSizes, nLakePointsOneAxis);
        initialize_energy(aEnergyStep1, aPebbleSizes, nLakePointsOneAxis);

        // Print starting heat map to file
        print_heatmap("lake_i.dat", aEnergyStep0, nLakePointsOneAxis, nPointSpacing);

        // Run on CPU?
        if (nThreads < 0) {

            // Run simulation on CPU
            gettimeofday(&nStartTimeCPU, NULL);
            run_cpu(aEnergyCPU, aEnergyStep0, aEnergyStep1, aPebbleSizes, nLakePointsOneAxis, nPointSpacing, nFinishTime);
            gettimeofday(&nEndTimeCPU, NULL);

            // Report how long CPU took
            nElapsedTimeCPU = ((nEndTimeCPU.tv_sec + nEndTimeCPU.tv_usec * 1e-6)-(
                               nStartTimeCPU.tv_sec + nStartTimeCPU.tv_usec * 1e-6));
            printf("Entire job on CPU took %f seconds\n", nElapsedTimeCPU);

            // Print final heat map to file (use same name whether CPU or GPU)
            print_heatmap("lake_f.dat", aEnergyCPU, nLakePointsOneAxis, nPointSpacing);

        }

        // Run on GPU?
        else {

            // Run simulation on GPU
            gettimeofday(&nStartTimeGPU, NULL);
            run_gpu(aEnergyGPU, aEnergyStep0, aEnergyStep1, aPebbleSizes, nLakePointsOneAxis, nPointSpacing, nFinishTime, nThreads);
            gettimeofday(&nEndTimeGPU, NULL);

            // Report how long GPU took
            nElapsedTimeGPU = ((nEndTimeGPU.tv_sec + nEndTimeGPU.tv_usec * 1e-6)-(
                      nStartTimeGPU.tv_sec + nStartTimeGPU.tv_usec * 1e-6));
            printf("Entire job on GPU took %f seconds\n", nElapsedTimeGPU);

            // Print final heat map to file (use same name whether CPU or GPU)
            print_heatmap("lake_f.dat", aEnergyGPU, nLakePointsOneAxis, nPointSpacing);

        }

        // Free allocated memory
        free(aEnergyStep0);
        free(aEnergyStep1);
        free(aPebbleSizes);
        free(aEnergyCPU);
        free(aEnergyGPU);

    }

    // Return 0 if everything is okay
    return bSuccess == 1 ? 0 : 1;

}

/*************************************************************************************************************
 * FUNCTION:        validate_inputs
 *
 * DESCRIPTION:     Ensure the required command line arguments are present
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
    if (argc < 4) {
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
        argc >= 5 &&
        (
            is_number(argv[4]) != 1 ||
            atoi(argv[4]) <= 0
        )
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
    printf("\nUsage: prun ./lake-mpi [lake size] [# pebbles] [duration of simulation in seconds] [# GPU threads (optional)]\n");

}

/*************************************************************************************************************
 * FUNCTION:        run_cpu
 *
 * DESCRIPTION:     Simulate the energy changes over time in the lake, on the CPU
 *
 * ARGUMENTS:       aEnergy -               Array representing energy levels at every point in the lake
 *                  aEnergyStep0 -          Array representing energy levels at every point in the lake at time 0
 *                  aEnergyStep1 -          Array representing energy levels at every point in the lake at time 1
 *                  aPebbleSizes -          Array representing the pebble sizes at every point in the lake (sparse)
 *                  nLakePointsOneAxis -    The number of points in the map of the lake (one axis)
 *                  nPointSpacing -         The spacing between two points on the lake
 *                  nFinishTime -           The duration of the simulation in seconds
 *
 * RETURNS:         None
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *************************************************************************************************************/
void run_cpu(double *aEnergy, double *aEnergyStep0, double *aEnergyStep1, double *aPebbleSizes, int nLakePointsOneAxis, double nPointSpacing, double nFinishTime)
{

    // Declare variables
    double *aEnergyNew, *aEnergyCurrent, *aEnergyOld;
    double nTime, nTimeStep;

    // Allocate memory
    aEnergyNew = (double*)malloc(sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);
    aEnergyCurrent = (double*)malloc(sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);
    aEnergyOld = (double*)malloc(sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);

    // Initialize
    memcpy(aEnergyOld, aEnergyStep0, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);
    memcpy(aEnergyCurrent, aEnergyStep1, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);
    nTime = 0.;
    nTimeStep = nPointSpacing / 2.;

    // Run simulation
    while(1)
    {
        #ifdef __FIVE_POINT
            evolve(aEnergyNew, aEnergyCurrent, aEnergyOld, aPebbleSizes, nLakePointsOneAxis, nPointSpacing, nTimeStep, nTime);
        #else
            evolve9pt(aEnergyNew, aEnergyCurrent, aEnergyOld, aPebbleSizes, nLakePointsOneAxis, nPointSpacing, nTimeStep, nTime);
        #endif
        memcpy(aEnergyOld, aEnergyCurrent, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);
        memcpy(aEnergyCurrent, aEnergyNew, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);
        if(!update_time(&nTime,nTimeStep,nFinishTime)) break;
    }

    // Finalize
    memcpy(aEnergy, aEnergyNew, sizeof(double) * nLakePointsOneAxis * nLakePointsOneAxis);

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

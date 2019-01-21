/*************************************************************************************************************
 * FILE:            lake.c
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Models pebbles on a lake.
 *                  This program uses centered finite differencing to solve the wave equation with sources.
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw3/lake.tar
 *
 * TO RUN:          srun -n16 -p opteron --pty /bin/bash
 *                  make -f Makefile.serial OR
 *                  make -f Makefile.omp OR
 *                  make -f Makefile.acc
 *                  ./lake [grid_size] [# of pebbles] [end time] [# threads]
 *                      [mem copy flag] [inner loop flag] [outer loop flag]
 *                      [init energy values flag] [init energy copy flag]
 *
 *                  Where:
 *                      grid_size -                 integer, size of one edge of the square grid;
 *                                                  so the true size of the computational grid will
 *                                                  be grid_size * grid_size
 *                      # of pebbles -              number of simulated "pebbles" to start with
 *                      end time -                  the simulation starts from t=0.0 and goes to t=[end time]
 *                      # threads -                 the number of threads the simulation uses
 *
 *                  Note:
 *                      Several arguments are supported in lake.c, beyond those arguments that were already present.
 *                      Each of these is a flag meant to have the value of 0 or of 1.
 *                      Each flag supports testing in some way.
 *                      Each flag is totally optional and has a reasonable default value,
 *                          to support the grader running the program without these arguments.
 *                      These flags translate into more complex code (if/else statements).
 *                      This extra level of complexity would not be defensible if this code was to be maintained into the future.
 *                      However, for a project whose explicit goal is to vary the operation of the code
 *                          to hone in one the most efficient options,
 *                          this is quite advantageous, as the flags support this variation and testing,
 *                          and importantly, also support RE-testing to ensure recent work hasn't messed up older work.
 *
 *                      logging flag -              1 if we should do more logging with lake_log
 *                                                  Supports all testing
 *                      mem copy flag -             1 if the run_sim() memory copies should be optimized
 *                                                  Supports V1 testing
 *                      inner loop flag -           1 if the run_sim() inner loop should be optimized
 *                                                  Supports V1 testing
 *                      outer loop flag -           1 if the run_sim() outer loop should be optimized
 *                                                  Supports V1 testing
 *                      init energy values flag -   1 if the init() energy array value initialization should be optimized
 *                                                  Supports V2 testing
 *                      init energy copy flag -     1 if the init() energy array copy initialization should be optimized
 *                                                  Supports V2 testing
 *                      dynamic flag -              1 if OpenMP directives should use dynamic scheduling
 *                                                  Supports V3 testing
 *************************************************************************************************************/

// INCLUDES
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <sys/time.h>
#include "./lake.h"
#include "./lake_util.h"
#ifdef _OPENMP
    #include <omp.h>
#endif
#ifdef _OPENACC
    #include <openacc.h>
#endif

// Hack to get around a problem I have in my IDE setup
#ifndef NULL
    #define NULL   ((void *) 0)
#endif

// Mode
#ifdef _OPENMP
    #define B_OPEN_MP 1
#else
    #define B_OPEN_MP 0
#endif
#ifdef _OPENACC
    #define B_OPEN_ACC 1
#else
    #define B_OPEN_ACC 0
#endif

/* Probably not necessary but doesn't hurt */
#define _USE_MATH_DEFINES

// Number of threads is always 16 per assignment
#define N_THREADS 16

/* Function prototypes
 * "You may not modify the Makefile, lake.h, or lake_util.h that is provided. I will be using the same ones to grade your work!"
 */
#ifdef _OPENACC
    #pragma acc routine seq
#endif
void update_one_lake_point (
    int i,
    int j,
    int nLakePointsOneAxis,
    double *anEnergyNew,
    double *anEnergyCurrent,
    double *anEnergyOld,
    double *anPebbles,
    double t,
    double dt,
    double nGridStep
);

/* Number of OpenMP threads */
int nthreads;

// Logging flag
int bExtraLogging =             0;

// Optimization flags (V1)
int bOptimizeMemCpy =           1;
int bOptimizeLoopInner =        0;
int bOptimizeLoopOuter =        1;

// Optimization flags (V2)
int bOptimizeInitEnergyValues = 0;
int bOptimizeInitEnergyCopy =   1;

// Optimization flags (V3)
int bOptimizeDynamic =          0;

/*************************************************************************************************************
 * FUNCTION:        main
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Main function.
 *                  Stores arguments.
 *                  Runs simulation.
 *                  Allocates and frees memory.
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw3/lake.tar
 *
 * ARGUMENTS:       See file comment block
 *************************************************************************************************************/
int main (int argc, char *argv[]) {
  if(argc < 5)
  {
    fprintf(stdout, "Usage: %s npoints npebs time_finish nthreads \n",argv[0]);
    return 0;
  }

  /* grab the arguments and setup some vars */
  int     npoints   = atoi(argv[1]);
  int     npebs     = atoi(argv[2]);
  double  end_time  = (double)atof(argv[3]);
  int     nthreads  = atoi(argv[4]);
  int     narea     = npoints * npoints;

  /* check input params for restrictions */
  if ( npoints % nthreads != 0 )
  {
    fprintf(stderr, "BONK! npoints must be evenly divisible by nthreads\n Try again!");
    return 0;
  }

  /* Store optional flags
   *    These flags exist only to support testing
   *    They all have reasonable default values
   */
  if (argc > 5) {
      bExtraLogging = atoi(argv[5]);
  }
  if (argc > 6) {
      bOptimizeMemCpy = atoi(argv[6]);
  }
  if (argc > 7) {
      bOptimizeLoopInner = atoi(argv[7]);
  }
  if (argc > 8) {
      bOptimizeLoopOuter = atoi(argv[8]);
  }
  if (argc > 9) {
      bOptimizeInitEnergyValues = atoi(argv[9]);
  }
  if (argc > 10) {
      bOptimizeInitEnergyCopy = atoi(argv[10]);
  }
  if (argc > 11) {
      bOptimizeDynamic = atoi(argv[11]);
  }

  /* get the program directory */
  set_wrkdir(argv[0]);

  /* main simulation arrays */
  double *u_i0, *u_i1;
  double *u_cpu, *pebs;

  /* u_err is used when calculating the 
   * error between one version of the code
   * and another. */
  double *u_err;

  /* h is the size of each grid cell */
  double h;
  /* used for error analysis */
  double avgerr;

  /* used for time analysis */
  double elapsed_cpu, elapsed_init;
  struct timeval cpu_start, cpu_end;
  
  /* allocate arrays */
  u_i0 = (double*)malloc(sizeof(double) * narea);
  u_i1 = (double*)malloc(sizeof(double) * narea);
  pebs = (double*)malloc(sizeof(double) * narea);

  u_cpu = (double*)malloc(sizeof(double) * narea);

  start_lake_log("lake.log");

  // Perform extra logging?
  if (bExtraLogging >= 1) {
      lake_log("ARGUMENTS:\n");
      lake_log("npoints: %d\n",                     npoints);
      lake_log("npebs: %d\n",                       npebs);
      lake_log("end_time: %f\n",                    end_time);
      lake_log("nthreads: %d\n",                    nthreads);
      lake_log("bExtraLogging: %d\n",               bExtraLogging);
      lake_log("bOptimizeMemCpy: %d\n",             bOptimizeMemCpy);
      lake_log("bOptimizeLoopInner: %d\n",          bOptimizeLoopInner);
      lake_log("bOptimizeLoopOuter: %d\n",          bOptimizeLoopOuter);
      lake_log("bOptimizeInitEnergyValues: %d\n",   bOptimizeInitEnergyValues);
      lake_log("bOptimizeInitEnergyCopy: %d\n",     bOptimizeInitEnergyCopy);
      lake_log("bOptimizeDynamic: %d\n",            bOptimizeDynamic);
  }

  lake_log("running %s with (%d x %d) grid, until %f, with %d threads\n", argv[0], npoints, npoints, end_time, nthreads);

  /* initialize the simulation */
  h = (XMAX - XMIN)/npoints;

#ifdef __DEBUG
  lake_log("grid step size is %f\n",h);
  lake_log("initializing pebbles\n");
#endif

  init_pebbles(pebs, npebs, npoints);

#ifdef __DEBUG
  lake_log("initializing u0, u1\n");
#endif
  
  gettimeofday(&cpu_start, NULL);
  init(u_i0, pebs, npoints);
  init(u_i1, pebs, npoints);
  gettimeofday(&cpu_end, NULL);
  elapsed_init = ((cpu_end.tv_sec + cpu_end.tv_usec * 1e-6)-(
                  cpu_start.tv_sec + cpu_start.tv_usec * 1e-6));
  lake_log("Initialization took %f seconds\n", elapsed_init);
  
  /* print the initial configuration */
#ifdef __DEBUG
  lake_log("printing initial configuration file\n");
#endif

  print_heatmap("lake_i.dat", u_i0, npoints, h);

  /* time, run the simulation */
#ifdef __DEBUG
  lake_log("beginning simulation\n");
#endif

  gettimeofday(&cpu_start, NULL);
  run_sim(u_cpu, u_i0, u_i1, pebs, npoints, h, end_time);
  gettimeofday(&cpu_end, NULL);
  elapsed_cpu = ((cpu_end.tv_sec + cpu_end.tv_usec * 1e-6)-(
                  cpu_start.tv_sec + cpu_start.tv_usec * 1e-6));
  lake_log("Simulation took %f seconds\n", elapsed_cpu);
  lake_log("Init+Simulation took %f seconds\n", elapsed_init+elapsed_cpu);

  /* print the final configuration */
#ifdef __DEBUG
  lake_log("printing final configuration file\n");
#endif

  print_heatmap("lake_f.dat", u_cpu, npoints, h);

#ifdef __DEBUG
  lake_log("freeing memory\n");
#endif

  /* free memory */
  free(u_i0);
  free(u_i1);
  free(pebs);
  free(u_cpu);
  
  stop_lake_log();
  return 1;
}

/*************************************************************************************************************
 * FUNCTION:        run_sim
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     run_sim is the main driver of the program.
 *                  It takes in the initial configuration and parameters,
 *                  and runs them until end_time is reached.
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw3/lake.tar
 *
 * ARGUMENTS:       double *u0 -        the initial configuration
 *                  double *u1 -        the initial + 1 configuration
 *                  double *pebbles -   the array of pebbles
 *                  int n -             the grid size
 *                  double h -          the grid step size
 *                  double end_time -   the final time
 *************************************************************************************************************/
void run_sim (double *u, double *u0, double *u1, double *pebbles, int n, double h, double end_time) {
  /* arrays used in the calculation */
    double *anEnergyA;
    double *anEnergyB;
    double *anEnergyC;
    double *anEnergyNew;
    double *anEnergyCurrent;
    double *anEnergyOld;

  /* time vars */
  double t, dt;
  int i, j, idx;
  int nIterationWhile;

  /* allocate the calculation arrays */
  anEnergyA =   (double*)malloc(sizeof(double) * n * n);
  anEnergyB =   (double*)malloc(sizeof(double) * n * n);
  anEnergyC =   (double*)malloc(sizeof(double) * n * n);

  /* Secondary level of pointers to support memory optimization
   * This code change optimizes away memcpy operations at the end of each run_sim() while loop iteration.
   * The optimization uses pointer switching.
   * There are 3 lake energy arrays - new, current, and old.
   * These 3 arrays have pointers, initialized at the time of malloc.
   * Additionally, 3 "extra" pointers are created to facilitate the optimization.
   * At the end of each iteration of the run_sim() while loop, the "extra" pointers are switched around so that:
   *    - the pointer named "...New" points to the array previously pointed to by "...Old"
   *    - the pointer named "...Current" points to the array previously pointed to by "...New"
   *    - the pointer named "...Old" points to the array previously pointed to by "Current"
   * In this way, the lake energy values effectively cycled through New --> Current --> Old, the same results we get from the memcpy.
   */
  anEnergyNew =     anEnergyA;
  anEnergyCurrent = anEnergyB;
  anEnergyOld =     anEnergyC;

    // Put the initial configurations into the calculation arrays
    if (B_OPEN_MP == 1 && bOptimizeInitEnergyCopy >= 1) {
        /* Perform 2 copies in two parallel regions
         * http://jakascorner.com/blog/2016/05/omp-sections.html
         * https://www.openmp.org/wp-content/uploads/openmp-4.5.pdf
         * The "schedule" clause is not supported for the "parallel" or "sections" directive
         * https://stackoverflow.com/questions/49309000/why-cant-pragma-omp-critical-sections-have-opening-brace-on-the-same-line
         * Take care to put bracket on new line
         */
        #pragma omp parallel sections num_threads(2)
        {
            #pragma omp section
            {
                memcpy(anEnergyOld, u0, sizeof(double) * n * n);
            }
            #pragma omp section
            {
                memcpy(anEnergyCurrent, u1, sizeof(double) * n * n);
            }
        }
    }
    else if (B_OPEN_ACC == 1) {
        // Don't copy at all - just point to the existing arrays
        anEnergyOld = u0;
        anEnergyCurrent = u1;
    }
    else {
        // No optimizations
        memcpy(anEnergyOld, u0, sizeof(double) * n * n);
        memcpy(anEnergyCurrent, u1, sizeof(double) * n * n);
    }

  /* start at t=0.0 */
  t = 0.;
  /* this is probably not ideal.  In principal, we should
   * keep the time-step at the size determined by the 
   * CFL condition
   * 
   * dt = h / vel_max
   *
   * where vel_max is the maximum velocity in the current
   * model.  The condition dt = h/2. should suffice, but 
   * be aware the possibility exists for madness and mayhem */
  dt = h / 2.;

    /* loop until time >= end_time */
    nIterationWhile = 1;
    #ifdef _OPENACC
        #pragma acc data copy(anEnergyNew[:n*n],anEnergyCurrent[:n*n],anEnergyOld[:n*n],pebbles[:n*n])
    #endif
    while(1) {

        /* run a central finite differencing scheme to solve
         * the wave equation in 2D */

        // OpenACC Optimize
        if (B_OPEN_ACC == 1) {
            #pragma acc kernels loop
            for (i = 0; i < n; i++) {
                for (j = 0; j < n; j++) {
                    /* Attempted pointer switching on GPU after parallel region for OpenACC mode
                     * Causes lots of "Scalar last value needed after loop for" errors, so in that case we do something different.
                     * Don't mess with this memory copy / pointer switching at all outside of the OpenACC parallel region.
                     * Instead, change what you pass to the update_one_lake_point method depending on while loop iteration.
                     */
                    if (nIterationWhile % 3 == 1) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                    else if (nIterationWhile % 3 == 2) {
                        update_one_lake_point (i, j, n, anEnergyOld, anEnergyNew, anEnergyCurrent, pebbles, t, dt, h);
                    }
                    else if (nIterationWhile % 3 == 0) {
                        update_one_lake_point (i, j, n, anEnergyCurrent, anEnergyOld, anEnergyNew, pebbles, t, dt, h);
                    }
                }
            }
        }

        /* OpenMP Optimize
         *  The use of all these if / elses was to support thorough testing.
         *      Certainly would never leave these in place for "production" code.
         *  https://stackoverflow.com/questions/13357065/how-does-openmp-handle-nested-loops
         *  When both the outer and the inner loop are optimized, the "collapse" clause is used to simplify the code
         *  (versus multiple "parallel for" directives).
         *  http://www.bowdoin.edu/~ltoma/teaching/cs3225-GIS/fall17/Lectures/openmp.html
         *  By default, all variables in the work sharing region are shared except the loop iteration counter.
         *      In the case where only the outer loop is optimized, the "j" variable,
         *      which is dependent upon the private-by-default loop iteration counter "i",
         *      must itself be explicitly made private.
         *  By default, the loop iteration counters in the OpenMP loop constructs are private.
         */
        else if (B_OPEN_MP == 1) {
            if (bOptimizeLoopOuter == 0 && bOptimizeLoopInner == 0) {
                for (i = 0; i < n; i++) {
                    for (j = 0; j < n; j++) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                }
            }
            else if (bOptimizeLoopOuter == 0 && bOptimizeLoopInner >= 1 && bOptimizeDynamic == 0) {
                for (i = 0; i < n; i++) {
                    #pragma omp parallel for num_threads(N_THREADS) schedule(static)
                    for (j = 0; j < n; j++) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                }
            }
            else if (bOptimizeLoopOuter >= 1 && bOptimizeLoopInner == 0 && bOptimizeDynamic == 0) {
                #pragma omp parallel for num_threads(N_THREADS) schedule(static) private(j)
                for (i = 0; i < n; i++) {
                    for (j = 0; j < n; j++) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                }
            }
            else if (bOptimizeLoopOuter >= 1 && bOptimizeLoopInner >= 1 && bOptimizeDynamic == 0) {
                #pragma omp parallel for num_threads(N_THREADS) collapse(2) schedule(static)
                for (i = 0; i < n; i++) {
                    for (j = 0; j < n; j++) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                }
            }
            else if (bOptimizeLoopOuter == 0 && bOptimizeLoopInner >= 1 && bOptimizeDynamic >= 1) {
                for (i = 0; i < n; i++) {
                    #pragma omp parallel for num_threads(N_THREADS) schedule(dynamic)
                    for (j = 0; j < n; j++) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                }
            }
            else if (bOptimizeLoopOuter >= 1 && bOptimizeLoopInner == 0 && bOptimizeDynamic >= 1) {
                #pragma omp parallel for num_threads(N_THREADS) schedule(dynamic) private(j)
                for (i = 0; i < n; i++) {
                    for (j = 0; j < n; j++) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                }
            }
            else if (bOptimizeLoopOuter >= 1 && bOptimizeLoopInner >= 1 && bOptimizeDynamic >= 1) {
                #pragma omp parallel for num_threads(N_THREADS) collapse(2) schedule(dynamic)
                for (i = 0; i < n; i++) {
                    for (j = 0; j < n; j++) {
                        update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                    }
                }
            }
        }

        // Serial is unoptimized with regard to finite differencing loops
        else {
            for (i = 0; i < n; i++) {
                for (j = 0; j < n; j++) {
                    update_one_lake_point (i, j, n, anEnergyNew, anEnergyCurrent, anEnergyOld, pebbles, t, dt, h);
                }
            }
        }

        /* Attempted pointer switching on GPU after parallel region for OpenACC mode
         * Causes lots of "Scalar last value needed after loop for" errors, so in that case we do something different.
         * Don't mess with this memory copy / pointer switching at all outside of the OpenACC parallel region.
         * Instead, change what you pass to the update_one_lake_point method depending on while loop iteration.
         */
        if (B_OPEN_ACC == 0) {

            /* Update the calculation arrays for the next time step
             * This code change optimizes away memcpy operations at the end of each run_sim() while loop iteration.
             * The optimization uses pointer switching.
             * There are 3 lake energy arrays - new, current, and old.
             * These 3 arrays have pointers, initialized at the time of malloc.
             * Additionally, 3 "extra" pointers are created to facilitate the optimization.
             * At the end of each iteration of the run_sim() while loop, the "extra" pointers are switched around so that:
             *    - the pointer named "...New" points to the array previously pointed to by "...Old"
             *    - the pointer named "...Current" points to the array previously pointed to by "...New"
             *    - the pointer named "...Old" points to the array previously pointed to by "Current"
             * In this way, the lake energy values effectively cycled through New --> Current --> Old, the same results we get from the memcpy.
             */
            if (bOptimizeMemCpy >= 1) {

                if (anEnergyOld == anEnergyA) {
                    anEnergyNew =       anEnergyA;
                    anEnergyCurrent =   anEnergyB;
                    anEnergyOld =       anEnergyC;
                }
                else if (anEnergyOld == anEnergyB) {
                    anEnergyNew =       anEnergyB;
                    anEnergyCurrent =   anEnergyC;
                    anEnergyOld =       anEnergyA;
                }
                else if (anEnergyOld == anEnergyC) {
                    anEnergyNew =       anEnergyC;
                    anEnergyCurrent =   anEnergyA;
                    anEnergyOld =       anEnergyB;
                }

            }
            else {
                memcpy(anEnergyOld, anEnergyCurrent, sizeof(double) * n * n);
                memcpy(anEnergyCurrent, anEnergyNew, sizeof(double) * n * n);
            }

        }
        /* have we reached the end? */
        if(!tpdt(&t,dt,end_time)) break;

        // Update counter
        nIterationWhile++;

    }

    // Copy the last updated to the output array - OpenACC
    if (B_OPEN_ACC == 1) {
        if (nIterationWhile % 3 == 1) {
            memcpy(u, anEnergyNew, sizeof(double) * n * n);
        }
        else if (nIterationWhile % 3 == 2) {
            memcpy(u, anEnergyOld, sizeof(double) * n * n);
        }
        else if (nIterationWhile % 3 == 0) {
            memcpy(u, anEnergyCurrent, sizeof(double) * n * n);
        }
    }

    /* Copy the last updated to the output array - OpenMP & Serial
     * If pointer switching has been in use, get the "current" array instead of the "new" one,
     * Because the new info was already demoted in anticipation of running another iteration
     */
    else {
        if (bOptimizeMemCpy >= 1) {
            memcpy(u, anEnergyCurrent, sizeof(double) * n * n);
        }
        else {
            memcpy(u, anEnergyNew, sizeof(double) * n * n);
        }
    }

}

/*************************************************************************************************************
 * FUNCTION:        update_one_lake_point
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Updates a single lake point's energy level
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw3/lake.tar
 *
 * ARGUMENTS:       i -                 Index along the x axis of the lake
 *                  j -                 Index along the y axis of the lake
 *                  nLakePointsOnAxis - Number of points on the lake (along one axis)
 *                  anEnergyNew -       Energy levels in the lake (new)
 *                  anEnergyCurrent -   Energy levels in the lake (current)
 *                  anEnergyOld -       Energy levels in the lake (old)
 *                  anPebbles -         Size of pebbles in the lake
 *                  t -                 Current time value
 *                  dt -                Time step value
 *                  nGridStep -         Grid step value
 *************************************************************************************************************/
#ifdef _OPENACC
    #pragma acc routine seq
#endif
void update_one_lake_point (
    int i,
    int j,
    int nLakePointsOneAxis,
    double *anEnergyNew,
    double *anEnergyCurrent,
    double *anEnergyOld,
    double *anPebbles,
    double t,
    double dt,
    double nGridStep
) {

    /* Declare variables
     * Thread local variables are automatically considered private to the thread
     */
    int nIndex;

    // Set index into arrays
    nIndex = j + i * nLakePointsOneAxis;

    /* impose the u|_s = 0 boundary conditions */
    if(i == 0 || i == nLakePointsOneAxis - 1 || j == 0 || j == nLakePointsOneAxis - 1) {
        anEnergyNew[nIndex] = 0.;
    }

    /* otherwise do the FD scheme */
    else
    {

        anEnergyNew[nIndex] =
            2*anEnergyCurrent[nIndex] -
            anEnergyOld[nIndex] +
            VSQR *(dt * dt) *(
                (
                    anEnergyCurrent[nIndex - 1] +
                    anEnergyCurrent[nIndex + 1] +
                    anEnergyCurrent[nIndex + nLakePointsOneAxis] +
                    anEnergyCurrent[nIndex - nLakePointsOneAxis] +
                    0.25 * (
                        anEnergyCurrent[nIndex - nLakePointsOneAxis-1] +
                        anEnergyCurrent[nIndex + nLakePointsOneAxis-1] +
                        anEnergyCurrent[nIndex - nLakePointsOneAxis+1] +
                        anEnergyCurrent[nIndex + nLakePointsOneAxis+1]
                    ) - 5 * anEnergyCurrent[nIndex]
                )/(nGridStep * nGridStep) + f(anPebbles[nIndex],t)
            );

    }

}

/*****************************
* init_pebbles
*
* Input
* ----------
*   int pn - the number of pebbles
*   int n - the grid size
*
* Output
* ----------
*   double *p - an array (dimensioned same as the grid) that
*           gives the initial pebble size.
*
* Description
* ----------
*   init_pebbles creates a random scattering of some pn pebbles,
* along with a random size.  The range of the can be adjusted by changing
* the constant MAX_PSZ.
*
*******************************/

void init_pebbles(double *p, int pn, int n)
{
  int i, j, k, idx;
  int sz;

  srand( 10 );
  /* set to zero */
  memset(p, 0, sizeof(double) * n * n);

  for ( k = 0; k < pn ; k++ )
  {
    /* the offset is to ensure that no pebbles
     * are spawned on the very edge of the grid */
    i = rand() % (n - 4) + 2;
    j = rand() % (n - 4) + 2;
    sz = rand() % MAX_PSZ;
    idx = j + i * n;
    p[idx] = (double) sz;
  }
}

/*****************************
* f
*
* Input
* ----------
*   double p -  the initial pebble value
*   double t -  the current time
* Returns
* ----------
*   the value of the "pebble" source term at time t
*
* Description
* ----------
*   Each pebbles influence on the surface will "fade" as
*   time marches forward (they may sink away, for instance).
*   This function models that - at large t ("large" defined
*   relative to the constant TSCALE) the pebble will have
*   little to no effect.
*
*   NB: this function can be updated to model whatever behavior
*   you wish the pebbles to have - they could continually jump
*   up and down on the surface, driving more energetic waves, for
*   example.
******************************/
#ifdef _OPENACC
    #pragma acc routine seq
#endif
double f(double p, double t)
{
  return -expf(-TSCALE * t) * p;
}

int tpdt(double *t, double dt, double tf)
{
  if((*t) + dt > tf) return 0;
  (*t) = (*t) + dt;
  return 1;
}

/*************************************************************************************************************
 * FUNCTION:        init
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Initializes a lake energy array, based upon a pebbles array
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw3/lake.tar
 *
 * ARGUMENTS:       anEnergy -          Energy levels in the lake
 *                  anPebbles -         Size of pebbles in the lake
 *                  nLakePointsOnAxis - Number of points on the lake (along one axis)
 *************************************************************************************************************/
void init (double *anEnergy, double *anPebbles, int nLakePointsOnAxis) {

    // Declare variables
    int i, j, idx;

    /* OMP Optimize
     *  The fastest loop optimization method from V1 was found to be optimization of outer loop only, so that is duplicated here.
     *  In the case where only the outer loop is optimized, the "j" and "idx" variables,
     *      which conceptually should be private to each thread,
     *      will be treated as shared by default by OpenMP.
     *  So, these variables are made private explicitly using the appropriate directive clause.
     */
    if (B_OPEN_MP == 1) {

        if (bOptimizeInitEnergyValues >= 1 && bOptimizeDynamic == 0) {
            #pragma omp parallel for num_threads(N_THREADS) schedule(static) private(j, idx)
            for (i = 0; i < nLakePointsOnAxis ; i++) {
                for (j = 0; j < nLakePointsOnAxis ; j++) {
                    idx = j + i * nLakePointsOnAxis;
                    anEnergy[idx] = f(anPebbles[idx], 0.0);
                }
            }
        }
        if (bOptimizeInitEnergyValues >= 1 && bOptimizeDynamic >= 1) {
            #pragma omp parallel for num_threads(N_THREADS) schedule(dynamic) private(j, idx)
            for (i = 0; i < nLakePointsOnAxis ; i++) {
                for (j = 0; j < nLakePointsOnAxis ; j++) {
                    idx = j + i * nLakePointsOnAxis;
                    anEnergy[idx] = f(anPebbles[idx], 0.0);
                }
            }
        }
        else {
            for (i = 0; i < nLakePointsOnAxis ; i++) {
                for (j = 0; j < nLakePointsOnAxis ; j++) {
                    idx = j + i * nLakePointsOnAxis;
                    anEnergy[idx] = f(anPebbles[idx], 0.0);
                }
            }
        }

    }

    // Serial and ACC do not optimize this code
    else {
        for (i = 0; i < nLakePointsOnAxis ; i++) {
            for (j = 0; j < nLakePointsOnAxis ; j++) {
                idx = j + i * nLakePointsOnAxis;
                anEnergy[idx] = f(anPebbles[idx], 0.0);
            }
        }
    }

}

/*****************************
* error_u
*
* Input
* ----------
*   double *ua  -   error 1
*   double *ub  -   error 2
*   int n       -   array extent
* 
* Output
* ----------
*   double *uerr - array of errors
*       double *avgerr - pointer to the average error
*
* Description
* ----------
*   Calculates the relative error between ua and ub
*
********************************/
void error_u(double *uerr, double *avgerr, double *ua, double *ub, int n)
{
  int i, j, idx;
  
  (*avgerr) = 0.;

  for (i = 0; i < n; i++ ) {
    for (j = 0; j < n; j++ ) {
      idx = j + i * n;
      uerr[idx] = fabs((ua[idx]-ub[idx])/ua[idx]);
      (*avgerr) = (*avgerr) * ((double)idx/(double)(idx + 1)) + uerr[idx] / (double)(idx + 1);      
    } 
  }
}

/*****************************
* print_heatmap
*
* Input
* ----------
*   char *filename  - the output file name
*   double *u       - the array to output
*   int n           - the edge extent of u (i.e., u is (n x n))
*   double h        - the step size in u
* Output
* ----------
*   None
*
* Description
* ----------
*   Outputs the array u to the file filename
********************************/
void print_heatmap(char *filename, double *u, int n, double h)
{
  char full_filename[64];
  int i, j, idx;

  dir_string(filename, full_filename);
  FILE *fp = fopen(full_filename, "w");  

  for (i = 0; i < n; i++ ) {
    for (j = 0; j < n; j++ ) {
      idx = j + i * n;
      fprintf(fp, "%f %f %f\n", i*h, j*h, u[idx]);
    }
  }
  
  fclose(fp);
} 

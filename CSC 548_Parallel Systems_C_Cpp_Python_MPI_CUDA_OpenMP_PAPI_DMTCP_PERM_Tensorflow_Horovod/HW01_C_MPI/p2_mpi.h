/*************************************************************************************************************
 * FILE:            p2_mpi.c
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *s
 * DESCRIPTION:     Header file for an MPI program that differentiates and plots a function.
 *************************************************************************************************************/

#define MAX_NUM_FUNC	8

// Function prototypes
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

/*************************************************************************************************************
 * FILE:            p2_mpi_func.c
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *                  ssbehera    Subhendu S. Behera
 *                  wpmoore2    Wade P. Moore
 *
 * DESCRIPTION:     A file that provides many f(x) to enable testing of derivative calculation
 *************************************************************************************************************/

#include <math.h>

#define PI 3.14159265

/*
 * function to return x^2.
 */
double fn1(double x)
{
	return x * x;
}

/*
 * function to return x^3 + x^2.
 */
double fn2(double x)
{
	return (x * x * x) + (x * x);
}

/*
 * function to return sin(x)
 */
double fn3(double x)
{
	return sin(x);
}

/*
 * function to return a constant(1)
 */
double fn4(double x)
{
	return 1;
}

/*
 * function to return whatever was the input
 */
double fn5(double x)
{
	return x;
}

/*
 * function tor return sin-1(x)
 */
double fn6(double x)
{
	return asin(x);
}

/*
 * function to return 3^x
 */
double fn7(double x)
{
	return pow(3, x);
}

/*
 * function to return ln(x)
 */
double fn8(double x) {
	return log(x);
}

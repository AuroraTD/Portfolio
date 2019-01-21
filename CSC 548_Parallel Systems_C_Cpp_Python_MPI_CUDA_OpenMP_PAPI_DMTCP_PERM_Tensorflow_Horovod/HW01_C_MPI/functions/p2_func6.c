/*************************************************************************************************************
 * FILE:            p2_func.c
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     A file that provides an input function to enable testing of derivative approximation
 *                  We have many such functions defined and this is not the default one included in p2.makefile.
 *************************************************************************************************************/

#include <math.h>

#define PI 3.14159265358979323846

// function to return sin-1(x)
double fn (double x) {
    return asin(x * PI / 180);
}

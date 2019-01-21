/*************************************************************************************************************
 * FILE:            p2_func.c
 *
 * AUTHORS:         ssbehera    Subhendu S. Behera
 *
 * DESCRIPTION:     A file that provides an input function to enable testing of derivative approximation
 *                  We have many such functions defined but this is the default one
 *                  with which we compile, per p2.makefile.
 *                  sin(x) was chosen as the default function due to this quote from the homework assignment:
 *                  "Plot the sin(x) function and it's derivative for various grid sizes (100, 1000, 10000)"
 *************************************************************************************************************/

#include <math.h>

#define PI 3.14159265358979323846

// function to return sin(x)
double fn (double x) {
    return sin(x * PI / 180);
}

/*************************************************************************************************************
 * FILE:            p2_func.c
 *
 * AUTHORS:         ssbehera    Subhendu S. Behera
 *
 * DESCRIPTION:     A file that provides an input function to enable testing of derivative approximation
 *                  We have many such functions defined and this is not the default one included in p2.makefile.
 *************************************************************************************************************/

// function to return x^3 + x^2.
double fn (double x) {
    return (x * x * x) + (x * x);
}

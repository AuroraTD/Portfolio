/**
    @file warmup.c
    @author Aurora Tiffany-Davis (attiffan)

    Print boxes and descriptions of lines.
    Print a short, wide box with "#" on the outside and "." on the inside.
    Print tall, thin box with "*" on the outside and spaces inside.
    Describe two lines in the format y = mx + b, and also note the x-intercepts.
 */

#include <stdio.h>
#include <stdlib.h>

/**
    Print n copies of the given character c.
    @param c character to print.
    @param n number of times to print c, must be non-negative.
 */
void nTimes( char c, int n )
{
    int i;
    // Loop n times
    for ( i = 0; i < n; i++ ) {
        // Print c
        printf("%c", c);
    }
}

/**
    Print out a rectangle of a given size and made from the given inner
    and outer character symbols.
    @param width the width of the rectangle to be printed, should be 2 or greater.
    @param height the number of rows high the rectangle should be, must be 2 or greater.
    @param outer the character symbol to use for the outer edge of the rectangle.
    @param inner the character symbol fill the inside of the rectangle with.
 */
void filledBox( int width, int height, char outer, char inner )
{
    int i;
    // Loop over the rows of the rectangle
    for ( i = 0; i < height; i++ ) {
        // First or last row is a special case
        if ( i == 0 || i == height-1 ) {
            nTimes(outer,width);
        }
        else {
            printf("%c", outer);
            nTimes(inner,width-2);
            printf("%c", outer);
        }
        // After every row print a new line
        printf("\n");
    }
}


/**
    Return the X intercept for a line defined as y = m x + b.
    @param m the slope of the line, must be non-zero.
    @param b the y intercept of the line.
    @return the x intercept
 */
double intercept( double m, double b )
{
    // For y = mx + b, the value of x when y is zero will be -b/m
    return -b/m;
}

/**
    Print a report about the line, y = m x + b, including its x intercept.
    @param m the slope of the line, must be non-zero.
    @param b the y intercept of the line.
 */
void describeLine( double m, double b )
{
    // Calculate x-intercept
    double xIntercept = intercept(m,b);
    // Format of description: "Line y = 0.750 x + 18.300 has an x intercept of: -24.400"
    printf("Line y = %.3f x + %.3f has an x intercept of: %.3f", m, b, xIntercept);
    printf("\n");
}

/**
    Starting point for the program, prints a couple of rectangles and
    reports about a couple of lines.
    @return Exit status for the program.
 */
int main()
{
    // Print a short, wide box with # on the outside and . on the inside.
    filledBox( 20, 6, '#', '.' );
    printf( "\n" );

    // Print tall, thin box with * on the outside and spaces inside.
    filledBox( 7, 11, '*', ' ' );
    printf( "\n" );

    // Report about one line.
    describeLine( 0.75, 18.3 );

    // Then, another line.
    describeLine( -3.4281, 102.0 );

    return EXIT_SUCCESS;
}

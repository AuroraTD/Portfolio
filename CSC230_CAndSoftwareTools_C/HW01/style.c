/**
    @file style.c
    @author Aurora Tiffany-Davis (attiffan)

    Print letters and numbers:
    Print each letter in the alphabet (lower case).
    For each of the integers 1, 11, 21, ... 91,
    print the next integer that is divisible by 7.
 */

#include <stdio.h>
#include <stdlib.h>

/**
    Print each letter in the alphabet (lower case).
 */
void sleepy()
{
    for ( char c = 'a'; c <= 'z'; c++ ) {
        printf( "%c", c );
    }
    printf( "\n" );
}

/**
    Increment an integer until it becomes divisible by 7.
    @param x the integer to increment.
    @return x after the increment loop.
 */
int grumpy( int x )
{
    while ( x % 7 != 0 ) {
        x++;
    }
    return x;
}

/**
    For each of the integers 1, 11, 21, ... 91,
    print the next integer that is divisible by 7.
 */
void dopey()
{
    for ( int i = 1; i < 100; i += 10 ) {
        int x = grumpy( i );
        printf( "%d\n", x );
    }
}

/**
    Print letters and numbers.
    @return success
 */
int main()
{
    sleepy();
    dopey();
    return EXIT_SUCCESS;
}

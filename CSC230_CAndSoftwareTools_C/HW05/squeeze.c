/**
    @file squeeze.c
    @author Aurora Tiffany-Davis (attiffan)

    Compress a given text file.
*/

// Include libraries
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include "bits.h"
#include "codes.h"

/** Compress a given text file.
    Responsible for handling the command-line arguments,
    reading bytes from the input file,
    using the codes component to convert them to codes
    and using the bits component to write them out to the output file.

    @param argc Argument count
    @param argv Array of pointers to arguments

    @return Failure via exit(), or Success via actual return
*/
int main( int argc, char *argv[] )
{
    // Declare variables
    FILE *fpIn;
    FILE *fpOut;
    FILE *fpTmp;
    BitBuffer bitBuffer = {0, 0};
    int nextChar;
    int nextCode;
    unsigned char formatCode;

    // Check command-line args
    if ( argc != EXPECTED_ARGS ){
        fprintf( stderr, "usage: squeeze <infile> <outfile>\n" );
        exit( EXIT_FAILURE );
    }

    // Open files in binary mode
    if ( ( fpIn = fopen( argv[1], "rb" ) ) == NULL ) {
        perror(argv[1]);
        exit( EXIT_FAILURE );
    }
    if ( ( fpOut = fopen( argv[2], "wb" ) ) == NULL ) {
        perror(argv[2]);
        exit( EXIT_FAILURE );
    }
    if ( ( fpTmp = tmpfile() ) == NULL ) {
        perror("Temp File");
        exit( EXIT_FAILURE );
    }

    // Regular or Extended squeeze
    formatCode = strcmp(argv[0], "./esqueeze")? 1:0;

    // Just pretend we're always doing regular squeeze
    write5Bits( 1, &bitBuffer, fpTmp );
    while ( (nextChar = getc(fpIn)) != EOF ){
        nextCode = symToCode( (unsigned char) nextChar );
        if ( nextCode == KEEP_CHAR ){
            // Write escape code to indicate full char incoming
            write5Bits( ESCAPE_CODE, &bitBuffer, fpTmp );
            // Write full char
            write8Bits( nextChar, &bitBuffer, fpTmp );
        }
        else {
            // Write code for char
            write5Bits( nextCode, &bitBuffer, fpTmp );
        }
    }
    flushBits(&bitBuffer, fpTmp);

    // Check file sizes (way less code than figuring this w/out file write)
    fseek(fpTmp, 0, SEEK_END);
    fseek(fpIn, 0, SEEK_END);
    if ( formatCode == 1 || ftell(fpTmp) < ftell(fpIn) ){
        // All's well, just dump tmp into actual output file
        rewind(fpTmp);
        while ( (nextChar = getc(fpTmp)) != EOF) {
            putc(nextChar, fpOut);
        }
    }
    else {
        // Oops, that's too big, try again without compression
        rewind(fpIn);
        write5Bits( 0, &bitBuffer, fpOut );
        while ( (nextChar = getc(fpIn)) != EOF) {
            write8Bits( nextChar, &bitBuffer, fpOut );
        }
        flushBits(&bitBuffer, fpOut);
    }

    // Clean up
    fclose(fpIn);
    fclose(fpTmp);
    fclose(fpOut);
}

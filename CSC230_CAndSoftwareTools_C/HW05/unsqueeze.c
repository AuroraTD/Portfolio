/**
    @file unsqueeze.c
    @author Aurora Tiffany-Davis (attiffan)

    Decompress a given text file.
*/

// Include libraries
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "bits.h"
#include "codes.h"

/** Decompress a given text file.
    Responsible for handling the command-line arguments,
    using the bits component to read codes from the input file,
    using the codes component to convert them back to characters
    and writing those to the output file.

    @param argc Argument count
    @param argv Array of pointers to arguments

    @return Failure via exit(), or Success via actual return
*/
int main( int argc, char *argv[] )
{
    // Declare variables
    FILE *fpIn;
    FILE *fpOut;
    BitBuffer bitBuffer = {0, 0};
    unsigned char formatCode;
    int next5;
    int nextChar = '\0';

    // Check command-line args
    if ( argc != EXPECTED_ARGS ){
        fprintf( stderr, "usage: unsqueeze <infile> <outfile>\n" );
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

    // Read format code
    formatCode = read5Bits(&bitBuffer, fpIn);

    // Right now, we only support two compression techniques
    if ( formatCode != 0 && formatCode != 1 ){
        fprintf(stderr, "Invalid compressed format\n");
        exit(EXIT_FAILURE);
    }
    else {
        if ( formatCode == 0 ){
            // Uncompressed file - just copy out
            while ( ( nextChar = read8Bits(&bitBuffer, fpIn) ) != -1 ){
                putc(nextChar, fpOut);
            }
        }
        else {
            // Compressed file - figure out the next character to write
            while ( ( next5 = read5Bits(&bitBuffer, fpIn) ) != -1 ){
                if ( next5 == ESCAPE_CODE ){
                    // Next char couldn't be compressed so it's in a full byte
                    nextChar = read8Bits(&bitBuffer, fpIn);
                }
                else {
                    // Next char could be compressed so it's in 5 bits (this 5 bits)
                    nextChar = codeToSym(next5);
                }
                // Write to the output file unless we've got an end of file flag
                if (nextChar != -1){
                    putc(nextChar, fpOut);
                }
            }
        }
    }

    // Clean up
    fclose(fpIn);
    fclose(fpOut);
}

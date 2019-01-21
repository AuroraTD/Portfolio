/**
    @file code.c
    @author Aurora Tiffany-Davis (attiffan)

    Functions to help map between symbols and the codes used to represent them.
*/

// Include libraries
#include <stdio.h>
#include "codes.h"

// Code-symbol mappings to be used for all comp / decomp
static Mapping mappings[] = {
    {0, 'e'},
    {1, 't'},
    {2, 'a'},
    {3, 'i'},
    {4, '"'},
    {5, 'n'},
    {6, ' '},
    {7, 's'},
    {8, 'o'},
    {9, 'l'},
    {10, 'r'},
    {11, 'd'},
    {12, 'c'},
    {13, '>'},
    {14, '<'},
    {15, '/'},
    {16, 'p'},
    {17, 'm'},
    {18, '-'},
    {19, 'u'},
    {20, '.'},
    {21, 'h'},
    {22, 'f'},
    {23, '_'},
    {24, '='},
    {25, 'g'},
    {26, ':'},
    {27, 'b'},
    {28, '0'},
    {29, 'y'},
    {30, '\n'}
};

// See codes.h for description
int symToCode( unsigned char ch )
{
    // Declare variables
    int numValidChars = sizeof(mappings) / sizeof(mappings[0]);
    int i;
    int returnCode = KEEP_CHAR;

    // Loop through the valid chars, seeing if this char is among them
    for ( i=0; i<numValidChars; i++ ){
        if ( mappings[i].symbol == ch ){
            returnCode = mappings[i].code;
            break;
        }
    }

    // Return
    return returnCode;
}

// See codes.h for description
int codeToSym( int code )
{
    // Declare variables
    int numValidCodes = sizeof(mappings) / sizeof(mappings[0]);
    int i;
    int returnChar = '\0';

    // Loop through the valid codes, seeing if this code is among them
    for ( i=0; i<numValidCodes; i++ ){
        if ( mappings[i].code == code ){
            returnChar = mappings[i].symbol;
            break;
        }
    }

    // Return
    return returnChar;
}

/**
    @file codes.h
    @author Aurora Tiffany-Davis (attiffan)

    Header file for the codes.c component,
    with functions to help map between symbols and the codes used to represent them.
*/

#ifndef _CODES_H_
#define _CODES_H_

// Include libraries
#include <stdio.h>

// Code for characters we do not consider compressible
#define KEEP_CHAR 31
// Magic number suppressor
#define EXPECTED_ARGS 3

//Struct for a single code-symbol mapping
typedef struct {
    // Using char because it is small
    unsigned char code;
    // Symbol is naturally a char
    unsigned char symbol;
} Mapping;

/** Given ASCII char, return 5-bit code
    @param ch ASCII code for a character
    @return returnCode the 5-bit code used to represent the char (or escape code 31)
*/
int symToCode( unsigned char ch );

/** Given 5-bit code, return ASCII char.
    Only defined for codes from 0 up to 30, inclusive.
    @param code 5-bit code for a character
    @return returnChar ASCII code for a character
*/
int codeToSym( int code );

#endif

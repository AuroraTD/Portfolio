/**
    @file bits.c
    @author Aurora Tiffany-Davis (attiffan)

    Functions supporting reading and writing a file just a few bits at a time.
*/

// Include libraries
#include <stdio.h>
#include "bits.h"

// See bits.h for description
void write5Bits( int code, BitBuffer *buffer, FILE *fp )
{
    // Declare variables (lots, and all init'd, for debugging)
    int combinedBits = 0;
    int leftoverBitLen = 0;
    int charToWrite = 0;

    // Combine 5-bit code with whatever's in the buffer
    combinedBits = (buffer->bits << COMPRESS_LEN) | code;

    // If we've got a byte after adding these 5 bits, write to output file
    if ( buffer->bcount >= BITS_PER_BYTE - COMPRESS_LEN ){
        // Prep your single byte (high order)
        leftoverBitLen = buffer->bcount + COMPRESS_LEN - BITS_PER_BYTE;
        charToWrite = highOrder(leftoverBitLen, combinedBits);
        // Write to file
        putc(charToWrite, fp);
        // Leave only the leftovers (low order) in the buffer
        buffer->bits = lowOrder(leftoverBitLen, combinedBits);
        buffer->bcount = leftoverBitLen;
    }
    else {
        buffer->bits = combinedBits;
        buffer->bcount = buffer->bcount + COMPRESS_LEN;
    }
}

// See bits.h for description
void write8Bits( int code, BitBuffer *buffer, FILE *fp )
{
    // Declare variables (lots, and all init'd, for debugging)
    int combinedBits = 0;
    int charToWrite = 0;

    // Combine 8-bit code with whatever's in the buffer
    combinedBits = (buffer->bits << BITS_PER_BYTE) | code;

    // Prep your single byte (high order)
    charToWrite = highOrder(buffer->bcount, combinedBits);

    // Write to file
    putc(charToWrite, fp);

    // Leave only the leftovers (low order) in the buffer
    buffer->bits = lowOrder(buffer->bcount, combinedBits);
}

// See bits.h for description
void flushBits( BitBuffer *buffer, FILE *fp )
{
    // Anything to flush?
    if ( buffer->bcount > 0 ){
        // Declare variables
        int spaceToFillLen = BITS_PER_BYTE - buffer->bcount;
        unsigned char charToWrite;

        // Is there something to write?
        if ( spaceToFillLen >= COMPRESS_LEN ){
            // Bits after our leftovers could be seen as a code, so make it escape
            charToWrite =
                (buffer->bits << spaceToFillLen) |
                (ESCAPE_CODE << (spaceToFillLen - COMPRESS_LEN));
        }
        else {
            // Simply write out the leftovers (as high order)
            charToWrite = buffer->bits << spaceToFillLen;
        }
        putc(charToWrite, fp);
    }
    // Allow for re-use of buffer past flush
    buffer->bcount = 0;
    buffer->bits = 0;
}

// See bits.h for description
int read5Bits( BitBuffer *buffer, FILE *fp )
{
    // Declare variables (lots, and all init'd, for debugging)
    unsigned char nextByte = 0;
    int combinedBits = 0;
    int combinedBitLen = 0;
    int leftoverBitLen = 0;
    int toReturn = 0;

    // May not even need to go to the file
    if ( buffer->bcount >= COMPRESS_LEN ){
        // Nothing to combine, just use what's in the buffer
        combinedBits = buffer->bits;
        combinedBitLen = buffer->bcount;
    }
    else {
        // Read one byte from the file
        if ( fread( &nextByte, sizeof(unsigned char), 1, fp ) < 1 ){
            return -1;
        }
        // Use this byte to get 5 bits
        else {
            // Combine this byte (low order) with whatever's in the buffer (high order)
            combinedBits = buffer->bits << BITS_PER_BYTE | nextByte;
            combinedBitLen = BITS_PER_BYTE + buffer->bcount;
        }
    }

    // Get the 5 highest order bits from the combined bit string
    leftoverBitLen = combinedBitLen - COMPRESS_LEN;
    toReturn = highOrder(leftoverBitLen, combinedBits);
    // Put the leftovers back in the buffer
    buffer->bits = lowOrder(leftoverBitLen, combinedBits);
    buffer->bcount = leftoverBitLen;

    // Return
    return toReturn;
}

// See bits.h for description
int read8Bits( BitBuffer *buffer, FILE *fp )
{
    // Declare variables (lots, and all init'd, for debugging)
    unsigned char nextByte = 0;
    int combinedBits = 0;
    int toReturn = 0;

    // Read one byte from the file
    if ( fread( &nextByte, sizeof(unsigned char), 1, fp ) < 1 ){
        toReturn = -1;
    }
    else {
        // Combine this byte (low order) with whatever's in the buffer (high order)
        combinedBits = buffer->bits << BITS_PER_BYTE | nextByte;
        // Get the 5 highest order bits from this combined bit string
        toReturn = highOrder(buffer->bcount, combinedBits);
        // Put the leftovers back in the buffer
        buffer->bits = lowOrder(buffer->bcount, combinedBits);
    }

    // Return
    return toReturn;
}

// See bits.h for description
int highOrder( int numDropBits, int bitString )
{
    return (bitString & 0xFF << numDropBits) >> numDropBits;
}

// See bits.h for description
int lowOrder( int numBits, int bitString )
{
    return bitString & 0xFFFF >> (BITS_PER_BYTE*2 - numBits);
}

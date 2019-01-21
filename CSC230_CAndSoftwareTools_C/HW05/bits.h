/**
    @file bits.h
    @author Aurora Tiffany-Davis (attiffan)

    Header file for the bits.c component,
    with functions supporting reading and writing a file
    just a few bits at a time.
*/

#ifndef _BITS_H_
#define _BITS_H_

// Include libraries
#include <stdio.h>

/** Number of bits per byte.
    This isn't going to change,
    but it lets us give a good explanation
    instead of just the literal value, 8.
*/
#define BITS_PER_BYTE 8
// To avoid complaints of magic numbers, do the same for 5
#define COMPRESS_LEN 5
// Escape code, also 5-bit mask
#define ESCAPE_CODE 0x1F

/** Buffer space for up to 8 bits that have been written by the application
    but haven't yet been written out to a file,
    or that have been read from a file but not yet used by the application.
    When this structure is initialized,
    zeros should be stored in both fields.
*/
typedef struct {
    /** Storage for up to 8 bits left over
        from an earlier read
        or waiting to be written in a subsequent write.
    */
    unsigned char bits;
    /** Number of bits currently buffered. */
    int bcount;
} BitBuffer;

/** Write the 5-bit code stored in the code parameter.
    Temporarily store bits in the given buffer
    until we have 8 of them and can write them to the file.
    @param code to write out, a value betteen 0 and 31
    @param pointer to storage for bits that can't be written to the file yet.
        When this function is called,
        this buffer may already contain some bits left-over from a previous write.
    @param fp file we're writing to, opened for writing in binary mode.
*/
void write5Bits( int code, BitBuffer *buffer, FILE *fp );

/** Just like write5Bits(), but writing 8 bits at a time.
    @param code to write out, a value betteen 0 and 255
    @param pointer to storage for bits that can't be written to the file yet.
    @param fp file we're writing to.
*/
void write8Bits( int code, BitBuffer *buffer, FILE *fp );

/** If there are any bits buffered in buffer,
    write them out to the given file in the high-order bit positions of a byte,
    leaving zeros in the low-order bits.
    Special case: if there are non-zero but few bits (room for another code in there),
    write out an extra escape code within low-order bits.
    @param buffer pointer to storage for unwritten bits left over from a previous write.
    @param fp file these bits are to be written to, opened for writing.
*/
void flushBits( BitBuffer *buffer, FILE *fp );

/** Read and return the next 5-bit code from the given file.
    The given buffer may contain some bits left over from the last read,
    and if this read has any left-over bits,
    it should leave them in that buffer.
    @param buffer pointer to storage for left-over
        from one read call to the next.
    @param fp file bits are being read from, opened for reading in binary.
    @return value of the 5-bit code read in,
        or -1 if we reach the end-of-file before getting all 5 bits.
*/
int read5Bits( BitBuffer *buffer, FILE *fp );

/** Just like read5bits() but reading 8 bits at a time.
    @param buffer storage for bits left over from one read to the next.
    @param fp file we're supposed to write to.
    @return value of the 8-bit code read in, or -1 if we can't read 8 bits.
*/
int read8Bits( BitBuffer *buffer, FILE *fp );

/** Return the highest order bits from the given bit string
    @param numDropBits number of meaningless bits in the bit string
    @param bitString Given bit string
    @return bit string with all but highest order masked out
*/
int highOrder( int numDropBits, int bitString );

/** Return the lowest order bits from the given bit string
    @param numBits number of meaningful bits in the bit string
    @param bitString Given bit string
    @return bit string with all but lowest order masked out
*/
int lowOrder( int numBits, int bitString );

#endif

# @file Makefile
# @author Aurora Tiffany-Davis (attiffan)
# Makefile for squeeze / unsqueeze

# Set rules
CC = gcc
CFLAGS = -Wall -std=c99 -g

# Establish targets and prerequisites (cannot rely on defaults for esqueeze)

all: esqueeze squeeze unsqueeze

esqueeze: squeeze.o codes.o bits.o
	gcc squeeze.o codes.o bits.o -o esqueeze
squeeze: squeeze.o codes.o bits.o
unsqueeze: unsqueeze.o codes.o bits.o

squeeze.o: squeeze.c
unsqueeze.o: unsqueeze.c
codes.o: codes.c codes.h
bits.o: bits.c bits.h

# Delete any temporary files made during build or during tests

clean:
	rm -f esqueeze
	rm -f squeeze
	rm -f unsqueeze
	rm -f squeeze.o
	rm -f unsqueeze.o
	rm -f codes.o
	rm -f bits.o
	rm -f output.txt
	rm -f compressed.bin
	rm -f dumpbits
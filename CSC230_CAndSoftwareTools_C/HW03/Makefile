# @file Makefile
# @author Aurora Tiffany-Davis (attiffan)
# Makefile for crossword solver and connect four game

# Set rules
CC = gcc
CFLAGS = -Wall -std=c99

# Establish targets and prerequisites
all: cross connect
cross: cross.o
cross.o: cross.c
connect: connect.o board.o
connect.o: board.h connect.c
board.o: board.h board.c

# Delete any temporary files made during build or during tests
clean:
	rm -f cross cross.o
	rm -f connect connect.o board.o
	rm -f output.txt
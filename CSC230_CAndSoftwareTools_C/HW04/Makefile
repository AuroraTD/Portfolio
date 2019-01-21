# @file Makefile
# @author Aurora Tiffany-Davis (attiffan)
# Makefile for scheduler

# Set rules
CC = gcc
CFLAGS = -Wall -std=c99 -g

# Establish targets and prerequisites
all: daily
daily: daily.o schedule.o activity.o
daily.o: daily.c
schedule.o: schedule.h activity.h schedule.c
activity.o: schedule.h activity.h activity.c

# Delete any temporary files made during build or during tests
clean:
	rm -f daily daily.o
	rm -f schedule.o activity.o
	rm -f output.txt
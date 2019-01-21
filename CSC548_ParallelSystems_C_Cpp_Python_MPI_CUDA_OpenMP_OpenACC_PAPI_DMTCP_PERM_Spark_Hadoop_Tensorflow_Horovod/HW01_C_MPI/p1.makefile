################################################################################
# FILE:			p1.makefile
#
# AUTHOR:		attiffan	Aurora T. Tiffany-Davis
#
# DESCRIPTION:	Build an executable named p1 from p1.c
# 				Compile and link MPI program written in C (mpicc)
# 				Optimizing aggressively for code size & execution time (-O3)
# 				Writing the build output to a file (-o)
# 				Which is called (p1)
# 				Given the input (p1.c)
# 				And link the math library (-lm)
#
# TO RUN:		make -f p1.makefile
#				make -f p1.makefile clean
################################################################################

p1: p1.c
	mpicc -O3 -o p1 p1.c -lm

clean: 
	rm p1
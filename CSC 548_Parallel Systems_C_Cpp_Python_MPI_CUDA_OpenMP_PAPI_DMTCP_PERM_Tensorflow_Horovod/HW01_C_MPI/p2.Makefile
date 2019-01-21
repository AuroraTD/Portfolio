################################################################################
# FILE:			p1.Makefile
#
# AUTHORS:		attiffan	Aurora T. Tiffany-Davis
#				ssbehera	Subhendu S. Behera
#				wpmoore2	Wade P. Moore
#
# DESCRIPTION:	The STANDARD version of p2.Makefile.
#
#				Build an executable named p2 from p2_mpi.c
# 				Compile and link MPI program written in C (mpicc)
# 				Optimizing aggressively for code size & execution time (-O3)
# 				Writing the build output to a file (-o)
# 				Which is called (p2)
# 				Given the main *.c file (p2_mpi.c)
#				And also a *.c file defining an input function
#					for which we approximate the derivative function
#					we will create several such *.c files
#					but by default will compile with the one
#					that is named "p2_func.c"
#					per https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw1/
# 				And link the math library (-lm)
#
# TO RUN:		make -f p2.Makefile
#				make -f p2.Makefile clean
################################################################################

p2: p2_mpi.c
	mpicc -O3 -o p2 p2_mpi.c p2_func.c -lm

clean:
	rm p2

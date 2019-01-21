################################################################################
# FILE:         p1.Makefile
#
# AUTHOR:       attiffan	Aurora T. Tiffany-Davis
#
# DESCRIPTION:  Build an executable named my_rtt from my_rtt.c
#               Compile and link MPI program written in C (mpicc)
#               Optimizing aggressively for code size & execution time (-O3)
#               Writing the build output to a file (-o)
#               Which is called (my_rtt)
#               Given the inputs (my_rtt.c) (my_mpi.c)
#               Link the math library (-lm)
#
# TO RUN:       make -f p1.Makefile
#               make -f p1.Makefile clean
################################################################################

my_rtt: my_rtt.c
	mpicc -O3 -o my_rtt my_rtt.c my_mpi.c -lm

clean: 
	rm my_rtt
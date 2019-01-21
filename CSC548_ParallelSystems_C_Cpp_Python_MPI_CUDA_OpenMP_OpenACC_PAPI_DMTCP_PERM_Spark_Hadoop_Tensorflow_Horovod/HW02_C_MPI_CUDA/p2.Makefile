################################################################################
# FILE          p2.Makefile
#
# AUTHOR:       attiffan	Aurora T. Tiffany-Davis
#
# DESCRIPTION:  Build an executable named my_rtt from my_rtt.c
#               Compile and link CUDA program written in C (nvcc)
#               Optimizing aggressively for code size & execution time (-O3)
#               Writing the build output to a file (-o)
#               Which is called (p2)
#               Given the input (p2.cu)
#               Link the math library (-lm)
#               Suppress a warning about deprecated architectures (-Wno-deprecated-gpu-targets)
#
# TO RUN:       make -f p1.Makefile
#               make -f p1.Makefile clean
################################################################################

my_rtt: p2.cu
	nvcc -O3 -o p2 p2.cu -lm -Wno-deprecated-gpu-targets

clean: 
	rm p2
################################################################################
# FILE:			p3.Makefile
#
# AUTHORS:		attiffan	Aurora T. Tiffany-Davis
#				ssbehera	Subhendu S. Behera
#				wpmoore2	Wade P. Moore
#
# DESCRIPTION:  Build an executable named lake
#                   Compile and link CUDA program written in C (nvcc)
#                   Given the input (lakegpu.cu and lake.cu)
#                   Writing the build output to a file (-o)
#                   Which is called (lake)
#                   Optimizing aggressively for code size & execution time (-O3)
#                   Link the math library (-lm)
#                   Suppress a warning about deprecated architectures (-Wno-deprecated-gpu-targets)
#               Build an executable named lake-mpi
#                   
#
# TO RUN:		make -f p3.Makefile lake (OR)
#               make -f p3.Makefile lake-mpi
#				make -f p3.Makefile clean
################################################################################

lake: lakegpu.cu lake.cu
	nvcc lakegpu.cu lake.cu -o lake -O3 -lm -Wno-deprecated-gpu-targets

lake-mpi: lake_mpi.cu  lakegpu_mpi.cu
	nvcc -c lakegpu_mpi.cu -O3 -lm -Wno-deprecated-gpu-targets -I/opt/ohpc/pub/mpi/mvapich2-gnu/2.2/include
		nvcc -c lake_mpi.cu -O3 -lm -Wno-deprecated-gpu-targets -I/opt/ohpc/pub/mpi/mvapich2-gnu/2.2/include
			mpicc lakegpu_mpi.o lake_mpi.o -o lake -O3 -lm -L/usr/local/cuda/lib64/ -lcudart

clean: 
	rm -f lake *.o

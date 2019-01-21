#Flags for MPI + OPENMP
CXXFLAGS = -I.
LDFLAGS_OPENMP = -g -O3 -fopenmp -L/opt/ohpc/pub/libs/gnu/mvapich2/mpiP/3.4.1/lib -lmpiP -lbfd -liberty
LDFLAGS = -g -O3 -L/opt/ohpc/pub/libs/gnu/mvapich2/mpiP/3.4.1/lib -lmpiP -lbfd -liberty

MPI_OPENMP: 
	mpic++ luleshMPI_OMP.cc $(CXXFLAGS) $(LDFLAGS_OPENMP) -o lulesh

MPI_ONLY:
	mpic++ luleshMPI_OMP.cc $(LDFLAGS) -o lulesh

clean:
	/bin/rm -f lulesh
	/bin/rm -rf *.dSYM


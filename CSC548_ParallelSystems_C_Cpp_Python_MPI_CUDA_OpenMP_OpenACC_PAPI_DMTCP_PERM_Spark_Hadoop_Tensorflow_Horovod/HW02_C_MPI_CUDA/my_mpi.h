/*************************************************************************************************************
 * FILE:            my_mpi.h
 *
 * AUTHORS:         attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Header file for a very basic implementation of MPI
 *************************************************************************************************************/

// DEFINITIONS

#define MPI_SUCCESS     0
#define MPI_ERROR       1
#define MPI_COMM_WORLD  1000
#define MPI_BYTE        1001
#define MPI_DOUBLE      1002

// TYPE DEFINITIONS

typedef int MPI_Comm;
typedef int MPI_Status;
typedef int MPI_Datatype;

// FUNCTION PROTOTYPES

int MPI_Init (int *argc, char ***argv);
int MPI_Comm_size (MPI_Comm comm, int *size);
int MPI_Comm_rank (MPI_Comm comm, int *rank);
int MPI_Ssend (const void *buf, int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm);
int MPI_Send (const void *buf, int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm);
int MPI_Recv (void *buf, int count, MPI_Datatype datatype, int source, int tag, MPI_Comm comm, MPI_Status *status);
int MPI_Gatherv (
    const void *sendbuf,
    int sendcount,
    MPI_Datatype sendtype,
    void *recvbuf,
    const int *recvcounts,
    const int *displs,
    MPI_Datatype recvtype,
    int root,
    MPI_Comm comm
);
int MPI_Finalize ();
int MPI_Barrier (MPI_Comm comm);
void remove_all_files ();
void list_files_in_current_directory ();
int is_number (char number[]);
int get_port_number ();
int write_to_port_list_file ();
int read_from_port_list_file ();
int allocate_memory ();
int free_memory ();
int create_server_socket ();
int create_client_socket (int nServerRank);
int accept_client_connection ();
size_t get_size_of_data_type (int nDataType);
int wait_for_barrier_message (int nSourceRank);

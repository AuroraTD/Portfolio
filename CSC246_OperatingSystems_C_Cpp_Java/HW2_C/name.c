/**
 *
 * @author Aurora Tiffany-Davis (attiffan)
 *
 * 2/5/17
 * CSC 246
 * Homework 2
 * name.c
 *
 * Compile command: gcc -Wall name.c
 * Test environment:
 *
 */

// Include libraries
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

// Main function
int main() {

    // Declare variables
    char *first = "Aurora";
    char *last = "Tiffany-Davis";
    int id;

    // Fork
    id = fork();

    // Child Process
    // your last name must be displayed by the child process
    if (id == 0){
        // Print last name
        printf("%s, ", last);
        // Exit so that parent can continue
        exit(0);
    }

    // Parent Process
    // first name must be displayed by the parent process
    else {
        // Wait for child to finish
        wait(NULL);
        // Print first name
        printf("%s\n", first);
    }

    // Return success
    return EXIT_SUCCESS;

}

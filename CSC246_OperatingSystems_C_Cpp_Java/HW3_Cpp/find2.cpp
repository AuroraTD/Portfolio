/**
 *
 * @file find2.cpp
 * @author Aurora Tiffany-Davis (attiffan)
 *
 * 2/17/17
 * CSC 246
 * Homework 3
 *
 * Report lines in multiple files which contain a given word.
 * Uses multiple threads.
 *
 * Compile command: g++ -Wall -o find2 find2.cpp -lpthread
 * Test environment: remote-linux.eos.ncsu.edu
 *
 */

 // Include standard libraries
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <cstdlib>
#include <pthread.h>

// Establish namespace
using namespace std;

// Define a struct for passing info to thread task
struct task_struct {
    string word;
    const char * filename;
};

// Task function
void * task (void * x) {

    // Declare variables
    struct task_struct * arguments;
    ifstream fileToSearch;
    string nextLine;
    string nextWord;
    // Get info from struct
    arguments = static_cast<task_struct *>(x);
    // Deal with a single file
    fileToSearch.open(arguments -> filename);
    if (fileToSearch.is_open()) {
        // Once per line
        while (getline(fileToSearch,nextLine)) {
            stringstream iss(nextLine);
            // Once per word in line
            while (iss >> nextWord) {
                // Search is case sensitive!
                // Search only successful if delimited by whitespace
                // Search fails if delimited e.g. by punctuation
                if (nextWord == arguments -> word) {
                    // Display the line if the word appears in it
                    cout << nextLine << endl;
                    // Stop looking in this line
                    break;
                }
            }
        }
        fileToSearch.close();
    }
    else {
        cout << arguments -> filename << ": file not found" << endl;
    }

    // Exit thread explicitly
    pthread_exit(NULL);
}

// Main function
int main(int argc, char* argv[])
{
    // Declare variables
    int i;
    int numFiles;

    // argv[0] is the path and name of the program itself
    // The program's first argument is the word to find
    // The remaining arguments are 1 to 5 file names that the program should search
    if (argc < 3 || argc > 7) {
        cout << "usage: find2 [word to find] [1-5 filenames]" << endl;
    }
    else {
        // How many files
        numFiles = argc - 2;
        pthread_t threads[numFiles];
        struct task_struct infoToPass[numFiles];
        // Set up a thread for each file
        for (i = 0; i < numFiles; i++) {
            infoToPass[i].word = argv[1];
            infoToPass[i].filename = argv[i+2];
            pthread_create(&threads[i], NULL, &task, (void *)&infoToPass[i]);
        }
        // Join on all threads
        for (i = 0; i < numFiles; i++) {
            pthread_join(threads[i],NULL);
        }
    }

    return 0;
}

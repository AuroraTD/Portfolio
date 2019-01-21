/**
 *
 * @file find.cpp
 * @author Aurora Tiffany-Davis (attiffan)
 *
 * 2/15/17
 * CSC 246
 * Homework 3
 *
 * Report lines in multiple files which contain a given word.
 * Uses multiple processes.
 *
 * Compile command: g++ -Wall -o find1 find.cpp
 * Test environment: remote-linux.eos.ncsu.edu
 *
 */

 // Include standard libraries
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <cstdlib>

// Establish namespace
using namespace std;

// Main function
int main(int argc, char* argv[])
{
    // Declare variables
    ifstream fileToSearch;
    string wordToFind;
    string nextLine;
    string nextWord;
    int i;
    int pid = 0;

    // argv[0] is the path and name of the program itself
    // The program's first argument is the word to find
    // The remaining arguments are 1 to 5 file names that the program should search
    if (argc < 3 || argc > 7) {
        cout << "usage: find1 [word to find] [1-5 filenames]" << endl;
    }
    else {

        // Remember the word we're looking for
        wordToFind = argv[1];

        // Once per file
        for (i = 2; i < argc; i++) {
            if (pid == 0) {
                // Initial (or child) process deals with file
                fileToSearch.open(argv[i]);
                if (fileToSearch.is_open()) {
                    // Once per line
                    while (getline(fileToSearch,nextLine)) {
                        stringstream iss(nextLine);
                        // Once per word in line
                        while (iss >> nextWord) {
                            // Search is case sensitive!
                            // Search only successful if delimited by whitespace
                            // Search fails if delimited e.g. by punctuation
                            if (nextWord == wordToFind) {
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
                    cout << argv[i] << ": file not found" << endl;
                    exit(1);
                }
            }
            else if (i < argc - 1) {
                // Parent waits for child then forks again for next file
                wait();
                fork();
            }
        }
    }

    return 0;
}

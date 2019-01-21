/**
    @file cross.c
    @author Aurora Tiffany-Davis (attiffan)

    Read a list of dictionary words,
    then help the user guess answers by showing words
    of a particular length that match user provided characters.
    For example, could be called as follows to report five-letter words
    that start with b and have an a as the second-to-last character.
    $ ./cross words-med.txt
    pattern> b??a?
*/

// Include headers
#include "board.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <ctype.h>
#include <string.h>

// Define constants
#define EXPECTED_NUM_ARGS 2
#define MAX_WORD_LENGTH 20
#define MAX_WORD_COUNT 100000
#define MAX_PATTERN_LENGTH 20

// Function prototypes
void readWords( char const *filename );
bool getPattern( char *pat );
bool matches( char const *word, char const *pat );

// Declare  global variables
int wordCount = 0;
char words[MAX_WORD_COUNT][MAX_WORD_LENGTH+1];

/**
    Read a list of dictionary words,
    prompt user for pattern(s) to match,
    print all words from the word list that match the pattern(s).

    @param argc Argument count
    @param argv Array of pointers to arguments

    @return Failure, via exit(), if there are any problems, Success otherwise
*/
int main( int argc, char *argv[] )
{
    // Declare variables
    char nextPattern[MAX_PATTERN_LENGTH+1];
    int i;

    // Check command-line arguments
    if ( argc != EXPECTED_NUM_ARGS ) {
        fprintf( stderr, "usage: cross <word-file>\n" );
        exit( EXIT_FAILURE );
    }

    // Read words from input file
    readWords(argv[1]);

    // Match patterns for the user until they stop providing patterns
    while ( getPattern( nextPattern ) ){
        // Print all words from the word list that match the pattern
        for ( i=0; i<wordCount; i++ ){
            if ( matches( words[i], nextPattern ) ){
                printf("%s\n", words[i]);
            }
        }
    }

    // Return
    return EXIT_SUCCESS;
}

/**
    Read the word list from the file with the given name,
    storing it in the global words array and setting wordCount.

    @param filename The name of the words file
*/
void readWords( char const *filename )
{
    // Declare variables
    FILE *fp;
    char nextChar;
    char nextWord[MAX_WORD_LENGTH+1];
    int wordLength;

    // Try to open the input file
    if ( ( fp = fopen( filename, "r" ) ) == NULL ) {
        fprintf( stderr, "Can't open word file\n" );
        exit( EXIT_FAILURE );
    }

    // Read the contents of the file
    wordLength = 0;
    while ( (nextChar = getc(fp)) != EOF ){
        if ( isspace(nextChar) ){
            // Space after a word?
            if ( wordLength > 0 ){
                // Null terminate the word and copy into array
                nextWord[wordLength] = '\0';
                strcpy(words[wordCount], nextWord);
                // Update the counts
                wordLength = 0;
                wordCount++;
            }
        }
        else if ( wordLength == MAX_WORD_LENGTH ||
                    wordCount == MAX_WORD_COUNT ||
                    isupper(nextChar)){
            // Words should be few, short, and lower case, complain otherwise
            fprintf( stderr, "Invalid word file\n" );
            exit( EXIT_FAILURE );
        }
        else{
            // Populate a word
            nextWord[wordLength] = nextChar;
            wordLength++;
        }
    }

    // Close the file
    fclose(fp);
}

/**
    Repeatedly prompt the user for a pattern until they provide a valid one,
    and store it in the given array.

    @param pat Pointer to the latest pattern to match against words

    @return True if the user (eventually) enters a valid pattern, False otherwise
*/
bool getPattern( char *pat )
{
    // Declare variables
    char nextChar = '\0';
    char possiblePattern[MAX_PATTERN_LENGTH+1];
    int patternLength;
    bool gotValidPattern = false;

    // Try repeatedly to get a valid pattern from the user
    while ( gotValidPattern == false && nextChar != EOF ){
        // Prompt the user for a pattern
        printf("pattern> ");

        // Evaluate the provided pattern character-by-character
        patternLength = 0;
        do {
            // Get the next character
            nextChar = getchar();

            // Reached end of pattern?
            if ( nextChar == EOF || nextChar == '\n' ){
                // This is fine, will just try again if needed
            }
            // Pattern short, and '?' / lower case?
            else if ( patternLength < MAX_PATTERN_LENGTH &&
                    (islower(nextChar) || nextChar == '?') ){
                // Populate the pattern
                possiblePattern[patternLength] = nextChar;
                patternLength++;
                // Looking good so far
                gotValidPattern = true;
            }
            // Anything else is a problem
            else{
                // Complain about bad pattern
                printf("Invalid pattern\n");
                gotValidPattern = false;
                // Eat up remaining characters
                while ( (nextChar = getchar()) != '\n' ){
                    // This will also case the loop to break so we can try again
                }
            }
        } while ( nextChar != EOF && nextChar != '\n' );
    }
    if ( gotValidPattern == true ){
        // Null terminate the pattern and copy into array
        possiblePattern[patternLength] = '\0';
        strcpy(pat, possiblePattern);
    }

    // Return indication of whether the pattern is valid
    return gotValidPattern;
}

/**
    Determine if the given word matches the given pattern.
    They match if they are the same length and each letter is identical.
    A question mark in the pattern will match any character at that position in the word.

    @param word Pointer to a word to match against a pattern
    @param pat Pointer to a pattern to match against a word

    @return True if the given word matches the given pattern, False otherwise
*/
bool matches( char const *word, char const *pat )
{
    // Declare variables
    bool match = true;
    int wordLength = strlen(word);
    int patternLength = strlen(pat);
    int i = 0;

    // Determine if the word and pattern are a match
    if ( wordLength != patternLength ){
        match = false;
    }
    else{
        while ( match == true && i < wordLength ){
            if ( word[i] != pat[i] && pat[i] != '?' ){
                match = false;
            }
            i++;
        }
    }

    // Return indication of whether this is a match
    return match;
}

/**
    @file dent.c
    @author Aurora Tiffany-Davis (attiffan)

    Read text from standard input and write out properly indented code
    based on the nesting depth of the curly brackets.

    Lines that only have spaces and tabs are printed without any content.

    All characters between double-quotes are examined and printed literally \
    (without regard to the notes above).

    Exit with a non-zero status if the curly bracket counts do not make sense.
*/

// Include standard libraries
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

// Define number of space characters to use for each level of curly bracket nesting
#define INDENT_CHARS 2
// Define an exit status in the case of curly bracket count mismatch
#define FAIL_CODE_BRACKET_COUNT 100
// Define an exit message in the case of curly bracket count mismatch
#define FAIL_MSG_BRACKET_COUNT "Unmatched brackets\n"

// Function prototypes
void indent(int d);
bool isASpace(char ch);

/** Read text from standard input and write out properly indented code
    based on the nesting depth of the curly brackets.

    Process one character at a time until EOF is reached.
    Keep track of opening and closing curly brackets.
    Keep track of opening and closing quotes.
    Print out characters based on current tracking.

    @return FAIL_MSG_BRACKET_COUNT if there is a problem with curly bracket count,
    EXIT_SUCCESS otherwise.
*/
int main()
{
    // Declare variables
    char nextChar;
    int nestDepth = 0;
    bool onNewLine = true;
    bool printingLiterally = false;

    // Read text from standard input until it reaches the end-of-file
    while ((nextChar = getchar()) != EOF) {
        // Track whether we are meant to be printing all characters literally
        if (nextChar == '"') {
            // Toggle literal print flag
            printingLiterally = !printingLiterally;
        }
        // Examine and possibly print character
        if (!printingLiterally && onNewLine && isASpace(nextChar)) {
            /** Discard spaces at the start of the line until we reach a non-whitespace character
                After these are discarded,
                we can print almost any other character we see in this line.
                If a line contains only spaces and tabs, it will be printed as a blank line.
            */
        }
        else {
            /** Nesting depth decreases BEFORE we print
                This makes the closing bracket line up
                with the start of the line containing matching opening bracket.
                This will only work for one curly bracket, not arbitrarily many.
            */
            if (!printingLiterally && nextChar == '}') {
                nestDepth--;
                if (nestDepth < 0) {
                    /** There are more closing curly brackets
                        than opening curly brackets at this point.
                        Exit immediately.
                    */
                    printf(FAIL_MSG_BRACKET_COUNT);
                    exit(FAIL_CODE_BRACKET_COUNT);
                }
            }
            /** If this is the first non-space char on new line, it's time to indent
                If a line contains only spaces and tabs, it will be printed as a blank line.
            */
            if (onNewLine && !isASpace(nextChar) && nextChar != '\n') {
                indent(INDENT_CHARS*nestDepth);
            }
            // Print the character
            putchar(nextChar);
            // Nesting depth increases AFTER we print
            if (!printingLiterally && nextChar == '{') {
                nestDepth++;
            }
            // Keep track of new lines
            if (nextChar == '\n') {
                onNewLine = true;
            }
            else {
                onNewLine = false;
            }
        }
    }

    // Exit
    if (nestDepth == 0) {
        // Every opening curly bracket was matched by a closing curly bracket
        exit(EXIT_SUCCESS);
    }
    else {
        // There were opening curly brackets that weren't matched by closing curly brackets
        printf(FAIL_MSG_BRACKET_COUNT);
        exit(FAIL_CODE_BRACKET_COUNT);
    }
}

/** This function will print out spaces to properly indent
the start of a line to an indentation depth of d.
    It is used to indent the start of each line,
    before printing the remaining characters on the line.

    @param d Desired indentation depth.
*/
void indent(int d)
{
    // Declare variables
    int i;
    // Print d space characters
    for ( i = 0; i < d; i++ ) {
        putchar(' ');
    }
}

/** This function returns true if the given char is either a space or a tab character.
    It will make it easy to detect character
    at the start of a line that should be discarded.

    @param ch The character to examine.
    @return True if the character is space or tab (False otherwise).
*/
bool isASpace(char ch)
{
    return ch == ' ' || ch == '\t';
}

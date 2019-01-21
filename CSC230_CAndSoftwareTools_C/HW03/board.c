/**
    @file board.c
    @author Aurora Tiffany-Davis (attiffan)

    Functions to support a generalized game of connect four.
*/

// Include headers
#include "board.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

//See board.h for function description
void getBoardSize( int * rows, int * cols )
{
    // Declare variables
    int inputsFound;
    int minBoardSize = RUNLEN;
    int maxBoardSize = RUNLEN + MAX_BOARD_SIZE_OFFSET;

    // Prompt user for number of rows and columns
    printf("Rows and cols> ");

    // Evaluate the user's input
    inputsFound = scanf("%d %d", rows, cols);
    if ( getchar() != '\n'
        || inputsFound != 2
        || *rows < minBoardSize
        || *rows > maxBoardSize
        || *cols < minBoardSize
        || *cols > maxBoardSize ){
        // Too many or too few inputs, or out of bounds board size
        fprintf(stderr, "Invalid board size\n");
        exit(EXIT_FAILURE);
    }
}

//See board.h for function description
void printBoard( int rows, int cols, char board[ rows ][ cols ] )
{
    // Declare variables
    int currentRow, currentCol, helperDigit;

    // Print blank line to help separate the board from other program I/O
    printf("\n");

    // Print all board rows (first row is at the bottom of the board)
    for ( currentRow=rows-1; currentRow>=0; currentRow-- ){
        // Print all columns in this row
        for ( currentCol=0; currentCol<cols; currentCol++ ){
            printf("|%c", board[currentRow][currentCol]);
        }
        // Finish off the last column
        printf("|");
        // Finish off the row
        printf("\n");
    }

    // Print separator row
    for ( currentCol=0; currentCol<cols; currentCol++ ){
        printf("+-");
    }
    printf("+");
    printf("\n");

    // Print numeric row
    // This is a hint, to make it easier for the player to tell the column number.
    // It shows just the low-order digit for the column number (the ones place),
    // counting from column 1 as the left-hand column.
    for ( currentCol=0; currentCol<cols; currentCol++ ){
        helperDigit = (currentCol+1)%HELPER_DIGIT_DIVISOR;
        printf(" %d", helperDigit);
    }
    printf("\n");
}

//See board.h for function description
void clearBoard( int rows, int cols, char board[ rows ][ cols ] )
{
    // Declare variables
    int currentRow, currentCol;

    // Iterate through the 2d array, replacing content with spaces
    for ( currentRow=0; currentRow<rows; currentRow++ ){
        for ( currentCol=0; currentCol<cols; currentCol++ ){
            board[currentRow][currentCol] = ' ';
        }
    }
}

//See board.h for function description
void makeMove( char player, int rows, int cols, char board[ rows ][ cols ] )
{
    // Declare variables
    int moveColumn = -1;
    int currentRow, lowestFreeRow, numberInputs;
    bool moveIsLegal = false;

    while ( moveIsLegal == false ){
        // Start by assuming move is not legal
        moveIsLegal = false;

        // Prompt the user for their next move
        printf("%c move> ", player);

        // Read a column number from the player
        numberInputs = scanf("%d", &moveColumn);

        // Something wrong?
        if ( numberInputs <= 0 ){
            // Eat up the rest of the line
            // (to handle wacky input like "four" instead of "4")
            int nextchar;
            while ( (nextchar = getchar()) != '\n' && nextchar != EOF ){
                // Nothing to do here
            }
            // At EOF,terminate successfully without printing anything
            if ( nextchar == EOF ){
                exit(EXIT_SUCCESS);
            }
        }

        // Evaluate input
        lowestFreeRow = rows;
        if ( moveColumn >= 1 && moveColumn <= cols ){
            // Find lowest free row in the desired column
            for ( currentRow=0; currentRow<rows; currentRow++ ){
                if ( board[currentRow][moveColumn-1] == ' ' ){
                    lowestFreeRow = currentRow;
                    break;
                }
            }
            if ( lowestFreeRow < rows ){
                // Move is legal
                moveIsLegal = true;
                // Make the requested move (adding an X or O to the board)
                board[lowestFreeRow][moveColumn-1] = player;
                // Return
                return;
            }
        }

        // Input represents illegal move?
        if ( moveIsLegal == false ){
            printf("Invalid move\n");
        }
    }
}

//See board.h for function description
void makeComputerMove( int rows, int cols, char board[ rows ][ cols ] )
{
    // Declare variables
    int i;
    int thisRow, thisCol;
    int targetRow = -1;
    int targetCol = -1;
    int streak = STREAK_NOT_FOUND;
    char playerSymb;
    bool moveMade = false;

    // Declare static variable (tend to clump computer moves together)
    static int moveColumn = 0;

    // First, try to win (look for almost-winning streaks of 'O')
    // Then, try to block a win (look for almost-winning streaks of 'X')
    for ( i=0; i<2; i++ ){

        // Choose player symbol to look for
        playerSymb = i==0? 'O': 'X';

        // Start looking
        thisRow = 0;
        while ( streak == STREAK_NOT_FOUND && thisRow<rows ){
            thisCol = 0;
            while ( streak == STREAK_NOT_FOUND && thisCol<cols ){

                streak = findStreak(rows, cols, board, thisRow, thisCol, playerSymb, RUNLEN-1);

                // Found an almost-winning streak?
                if ( streak != STREAK_NOT_FOUND ){

                    // Find the target position on the board that would complete this streak
                    if ( streak == STREAK_VERTICAL ){
                        targetRow = thisRow+RUNLEN-1;
                        targetCol = thisCol;
                    }
                    else if ( streak == STREAK_HORIZONTAL ){
                        targetRow = thisRow;
                        targetCol = thisCol+RUNLEN-1;
                    }
                    else if ( streak == STREAK_DIAGONAL_RIGHT ){
                        targetRow = thisRow+RUNLEN-1;
                        targetCol = thisCol+RUNLEN-1;
                    }
                    else if ( streak == STREAK_DIAGONAL_LEFT ){
                        targetRow = thisRow+RUNLEN-1;
                        targetCol = thisCol-RUNLEN-1;
                    }
                    // See if this move is legal
                    if ( isSpotAvailable(rows, cols, board, targetRow, targetCol) ){
                        // Looks good, make the move
                        printf("Computer Move %d\n", targetCol+1);
                        board[targetRow][targetCol] = 'O';
                        moveMade = true;
                    }
                    else{
                        // Can't complete this streak (move illegal), keep trying
                        streak = STREAK_NOT_FOUND;
                    }
                }
                thisCol++;
            }
            thisRow++;
        }
    }

    // No specific move needed?  Make some other move then
    i = 0;
    while ( moveMade == false ){

        // Get a column to try out
        if ( i < RANDOM_COLUMN_DIVISOR ){
            // Get random column close to where you went last time
            moveColumn = (rand() % RANDOM_COLUMN_DIVISOR) + moveColumn-1;
        }
        else {
            // Get random column anywhere
            moveColumn = rand() % cols;
        }

        // See if you can go in this column
        for ( thisRow=0; thisRow<rows; thisRow++ ){
            // See if this move is legal
            if ( isSpotAvailable(rows, cols, board, thisRow, moveColumn) ){
                // Looks good, make the move
                printf("Computer Move %d\n", moveColumn+1);
                board[thisRow][moveColumn] = 'O';
                moveMade = true;
                break;
            }
        }
    }
}

//See board.h for function description
bool isSpotAvailable( int rows, int cols, char board[ rows ][ cols ],
                      int spotRow, int spotCol )
{
    return spotRow >=0
           && spotRow < rows
           && spotCol >=0
           && spotCol < cols
           && board[spotRow][spotCol] == ' '
           && board[spotRow-1][spotCol] != ' ';
}

//See board.h for function description
int gameStatus( int rows, int cols, char board[ rows ][ cols ] )
{
    // Declare variables
    int gStatus = STATUS_OTHER;
    int streakStatus = STREAK_NOT_FOUND;
    int currentRow, currentCol;
    bool foundEmptySpot = false;

    // Examine each spot on the board, looking around for streaks of symbols
    currentRow = 0;
    while ( streakStatus == STREAK_NOT_FOUND && currentRow<rows ){
        currentCol = 0;
        while ( streakStatus == STREAK_NOT_FOUND && currentCol<cols ){
            // Look for streak of 'X', if no luck, try 'O'
            streakStatus = findStreak(rows, cols, board, currentRow, currentCol, 'X', RUNLEN);
            if ( streakStatus == STREAK_NOT_FOUND ){
                streakStatus = findStreak(rows, cols, board, currentRow, currentCol, 'O', RUNLEN);
            }
            currentCol++;
        }
        currentRow++;
    }

    // Found winner?
    if ( streakStatus != STREAK_NOT_FOUND ){
        gStatus = STATUS_WINNER;
    }

    // All positions filled?
    else {
        for ( currentRow=0; currentRow<rows; currentRow++ ){
            for ( currentCol=0; currentCol<cols; currentCol++ ){
                if ( board[currentRow][currentCol] == ' ' ){
                    foundEmptySpot = true;
                    break;
                }
            }
            if ( foundEmptySpot == true ){
                break;
            }
        }
        if ( foundEmptySpot == false ){
            gStatus = STATUS_STALEMATE;
        }
    }

    // Return game status
    return gStatus;
}

//See board.h for function description
int findStreak( int rows, int cols, char board[ rows ][ cols ],
                int startRow, int startCol, char playerSymbol, int streakLen )
{
    // Declare variables
    int streakStatus = STREAK_NOT_FOUND;

    // Only worth checking if at least the start position is the right symbol
    if ( board[ startRow ][ startCol ] == playerSymbol ){

        // Check vertically (up)
        if ( streakStatus == STREAK_NOT_FOUND
             && startRow <= rows-streakLen
             && isStreak(rows, cols, board, startRow, startCol, 1, 0, playerSymbol, streakLen) ){
            streakStatus = STREAK_VERTICAL;
        }

        // Check horizontally (right)
        if ( streakStatus == STREAK_NOT_FOUND
             && startCol <= cols-streakLen
             && isStreak(rows, cols, board, startRow, startCol, 0, 1, playerSymbol, streakLen) ){
            streakStatus = STREAK_HORIZONTAL;
        }

        // Check diagonal-right (up)
        if ( streakStatus == STREAK_NOT_FOUND
             && startRow <= rows-streakLen
             && startCol <= cols-streakLen
             && isStreak(rows, cols, board, startRow, startCol, 1, 1, playerSymbol, streakLen) ){
            streakStatus = STREAK_DIAGONAL_RIGHT;
        }

        // Check diagonal-LEFT (up)
        if ( streakStatus == STREAK_NOT_FOUND
             && startRow <= rows-streakLen
             && startCol >= streakLen-1
             && isStreak(rows, cols, board, startRow, startCol, 1, -1, playerSymbol, streakLen) ){
            streakStatus = STREAK_DIAGONAL_LEFT;
        }

    }

    // Return streak status
    return streakStatus;
}

//See board.h for function description
bool isStreak( int rows, int cols, char board[ rows ][ cols ],
                int startRow, int startCol, int dRow, int dCol,
                char playerSymbol, int streakLen ) {

    // Start by assuming there is not a streak here
    bool foundStreak = false;

    // Only worth checking if at least the start position is the right symbol
    if ( board[ startRow ][ startCol ] == playerSymbol ){

        // Number of X and O symbols in this sequence of locations
        int symbolCount = 0;

        // Walk down the sequence of board spaces
        for ( int k = 0; k < streakLen; k++ ) {

            // Figure out its row and column index
            int r = startRow + k * dRow;
            int c = startCol + k * dCol;

            // Count the player symbol if you find it
            if ( board[ r ][ c ] == playerSymbol ){
                symbolCount++;
                if ( symbolCount == streakLen ){
                    foundStreak = true;
                    break;
                }
            }
        }
    }

    // Return indication of whether a streak was found
    return foundStreak;
}

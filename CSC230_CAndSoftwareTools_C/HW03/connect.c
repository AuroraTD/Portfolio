/**
    @file connect.c
    @author Aurora Tiffany-Davis (attiffan)

    Generalized game of connect four.
    Two players (or player and computer) each get to drop markers (X or O)
    into the top of a chosen column in a two-dimensional game board.
    The marker drops down the column until it rests at the bottom of the column
    or on top of a marker that's already been placed.
    Gameplay continues until a player manages to get four of their markers in a row
    or the board fills up with neither player winning.
*/

// Include headers
#include "board.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

/**
    Generalized game of connect four.
    Prompt user for board size.
    Alternate between two players (or player and computer)
    asking for moves until game is over (or EOF is reached).
    If game ends, print winner or stalemate.

    @param argc Argument count
    @param argv Array of pointers to args ('a': computer plays 'O')

    @return Success
*/
int main( int argc, char *argv[] )
{
    // Determine if the computer is one of the players
    int computerPlayer = argc == 2 && strcmp(argv[1], "a") == 0;
    if ( computerPlayer ){
        // Seed pseudo random number generator used for computer moves
        srand((unsigned)time(NULL));
    }

    // Prompt user for the board size
    int rows, cols;
    getBoardSize( &rows, &cols );

    // Set up the board
    char board[rows][cols];
    clearBoard( rows, cols, board );

    // Display empty board
    printBoard( rows, cols, board );

    // Alternate between two players asking for moves until game is over
    int status = STATUS_OTHER;
    bool player1 = false;
    char playerSymbol = '?';
    while ( status == STATUS_OTHER ){

        // Toggle player
        player1 = !player1;

        if ( computerPlayer && !player1 ){
            // It's O's turn, and O is played by the computer
            playerSymbol = 'O';
            makeComputerMove(rows, cols, board);
        }
        else{
            // Ask player for move and make the first legal move they provide
            playerSymbol = player1? 'X' : 'O';
            makeMove( playerSymbol, rows, cols, board );
        }

        // Display updated board
        printBoard( rows, cols, board );

        // Determine game status
        status = gameStatus(rows, cols, board);
    }

    // Game is over
    if ( status == STATUS_WINNER ){
        printf("Player %c wins\n", playerSymbol);
    }
    else if ( status == STATUS_STALEMATE ){
        printf("Stalemate\n");
    }

    return EXIT_SUCCESS;
}

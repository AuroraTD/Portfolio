/**
    @file board.h
    @author Aurora Tiffany-Davis (attiffan)

    Function prototypes and macro definitions to support a generalized game of connect four.
*/

#include <stdbool.h>

// This trick will let us define the length of a winning
// run when we compile the program, if we want to.
#ifndef RUNLEN
/** Number of markers in a row required for a win. */
#define RUNLEN 4
#endif

// An offset, which when added to RUNLEN, gives the maximum board size
#define MAX_BOARD_SIZE_OFFSET 16

// Game status ints
#define STATUS_WINNER 1
#define STATUS_STALEMATE 2
#define STATUS_OTHER 0

// Streak of symbols status ints
#define STREAK_NOT_FOUND 0
#define STREAK_VERTICAL 1
#define STREAK_HORIZONTAL 2
#define STREAK_DIAGONAL_LEFT 3
#define STREAK_DIAGONAL_RIGHT 4

// Divisors for modulos, to shut Jenkins up about magic numbers :)
#define HELPER_DIGIT_DIVISOR 10
#define RANDOM_COLUMN_DIVISOR 3


/** Ask for the size of the board, measured in rows and columns.
    Store this information using the provided pointers.
    The board size will be determined by this initial user input.

    @param rows Pointer to desired number of rows
    @param cols Pointer to desired number of columns
*/
void getBoardSize( int * rows, int * cols );

/** Print the given board (of the given rows/cols size) to standard output.
    Example of a 6x7 board with three moves already made:

    | | | | | | | |
    | | | | | | | |
    | | | | | | | |
    | | | | | | | |
    | | | |X| | | |
    | |O| |X| | | |
    +-+-+-+-+-+-+-+
     1 2 3 4 5 6 7

    @param rows Number of rows to print
    @param cols Number of columns to print
    @param board Pointer to array of characters to print on board
*/
void printBoard( int rows, int cols, char board[ rows ][ cols ] );

/** Set the board to its initial state, filled with spaces.

    @param row Number of rows on the board
    @param cols Number of columns on the board
    @param board Pointer to board array
*/
void clearBoard( int rows, int cols, char board[ rows ][ cols ] );

/** Repeatedly prompt the user for their next move until they provide a legal move.
    Then make that move by putting their player symbol in the board array.

    @param player 'X' or 'O'
    @param rows Number of rows on the board
    @param cols Number of columns on the board
    @param board Pointer to the board array
*/
void makeMove( char player, int rows, int cols, char board[ rows ][ cols ] );

/** Make a move on behalf of 'O'
    First, try to win.
    Then, try to keep 'X' from winning.
    Finally, just pick a spot and go.

    @param rows Number of rows on the board
    @param cols Number of columns on the board
    @param board Pointer to the board array
*/
void makeComputerMove( int rows, int cols, char board[ rows ][ cols ] );

/** Determine if the given spot on the board is available.
    Is it on the board?
    Empty?
    Supported by a "checker" beneath it?

    @param rows Number of rows on the board
    @param cols Number of columns on the board
    @param board Pointer to the board array
    @param spotRow Position of row caller is interested in
    @param spotCol Position of column caller is interested in

    @return True if spot is available, False otherwise
*/
bool isSpotAvailable( int rows, int cols, char board[ rows ][ cols ],
                      int spotRow, int spotCol );

/** Check to see if the game is over.
    Look for vertical, horizontal, and diagonal (two ways) wins.
    Only look in spots where there is enough room on the board to possibly have a win.
    Stop if/when a win is found.

    @param rows Number of rows on the board
    @param cols Number of columns on the board
    @param board Pointer to the board array

    @return Number indicating game status (won, stalemate, other)
*/
int gameStatus( int rows, int cols, char board[ rows ][ cols ] );

/** Try to find a "streak" of the given player symbol.
    Start at the given board location.
    Try vertically (up), horizontally (right),
    diagonal-left (up), and diagonal-right (up).

    @param rows Number of rows the board has.
    @param cols Number of columns the board has.
    @param board The game board.
    @param startRow Row start position to look for a streak.
    @param startCol Column start position to look for a streak.
    @param playerSymbol The symbol to look for a "streak" of.
    @param streakLen The length of streak to look for (# of board locations).

    @return status Determination about streak (none, or what type).
*/
int findStreak( int rows, int cols, char board[ rows ][ cols ],
                int startRow, int startCol, char playerSymbol, int streakLen );

/** Return true if there's a "streak" of the given player symbol.
    Start at the given board location.
    The dRow and dCol parameters indicate what direction to look,
    with dRow giving change-in-row for each step and dCol giving the change-in-column.
    For example, if dRow and dCol are both 1 would look diagonally.

    @param rows Number of rows the board has.
    @param cols Number of columns the board has.
    @param board The game board.
    @param startRow Row start position to look for a streak.
    @param startCol Column start position to look for a streak.
    @param dRow Direction to move vertically looking for a streak.
    @param dCol Direction to move horizontally looking for a streak.
    @param playerSymbol The symbol to look for a "streak" of.
    @param streakLen The length of streak to look for (# of board locations).

    @return true if there's a streak in the given board location.
*/
bool isStreak( int rows, int cols, char board[ rows ][ cols ],
                int startRow, int startCol, int dRow, int dCol,
                char playerSymbol, int streakLen );

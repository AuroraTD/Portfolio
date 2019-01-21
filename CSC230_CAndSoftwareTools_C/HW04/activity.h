/**
    @file schedule.h
    @author Aurora Tiffany-Davis (attiffan)

    Function prototypes and macro definitions to support working with activities.
*/

#ifndef ACTIVITY_H
#define ACTIVITY_H

// Include headers
#include <stdbool.h>

// Maximums used to check validity
#define MAX_LENGTH_NON_TITLE 20
#define MAX_HOUR 23
#define MAX_MINUTE 59
#define NON_TITLE_FORMAT_LENGTH 4
#define ITEMS_SCANNED_TIME 2

// Memory complaint
#define OUT_OF_MEMORY "Ran out of memory!\n"

// Struct for time
struct TimeTag {
    int hours;
    int minutes;
};
typedef struct TimeTag Time;

// Struct for activities
struct ActivityTag {
    char *title;
    char leader[MAX_LENGTH_NON_TITLE+1];
    Time startT;
    Time endT;
    int id;
};
typedef struct ActivityTag Activity;

// Function prototypes

/** Read input from stdin after the "add" command.
    If the input is valid, allocate and init an Activity and return a pointer to it.
    If not, return NULL.
    Use a resizeable array to read the arbitrarily long title of the activity,
    reading characters and growing the title as necessary until it reaches the end-of-line.

    @return a Pointer to new Activity
*/
Activity *readActivity();

/** Eat up the rest of the current line to allow clean start next time.
    Don't call when there might be NOTHING at stdin.
    In that case, this function would hang.

    @param storage A string where the chars should be stored (excluding leading whitespace)

    @return storage A string where the chars were stored
*/
char *eatRestOfLine(char *storage);

/** Read in a relatively short non-activity-title word.
    Examples: the name of an activity leader,
    a word to match against an activity title.

    @return word The word that was read in
*/
char *readInNonTitleWord();

/** Free the dynamically allocated memory used to store an activity,
    including the structure itself and the title.

    @param act Pointer to an activity
*/
void freeActivity( Activity *act );

/** Check a time value to see if it makes sense.
    Valid hour (24-hour clock)?
    Valid minute?

    @param hours Hour value
    @param minutes Minute value

    @return True if time value is acceptable, False otherwise
*/
bool checkTimeValue( int hours, int minutes );

#endif // ACTIVITY_H

/**
    @file activity.c
    @author Aurora Tiffany-Davis (attiffan)

    Functions for working with instances of the Activity struct.
*/

// Include headers
#include "schedule.h"
#include "activity.h"
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdlib.h>

// See activity.h for description
Activity *readActivity(){

    // Declare variables
    int startHour;
    int startMinutes;
    int endHour;
    int endMinutes;
    int itemsScanned;
    char leader[MAX_LENGTH_NON_TITLE+1];
    bool valid = false;
    char *title = NULL;
    Activity *a  = NULL;

    /* Read in the start and end time, check validity
        After the add keyword, the user needs to enter a starting time for the activity,
        an ending time, the name of the activity leader and a title for the activity leader.
        The start and end times must have a valid hour and minute,
        and the start time must not be after the end time.
        The starting and ending times may contain whitespace around the ':' character.
    */
    itemsScanned = scanf("%d : %d %d : %d", &startHour, &startMinutes, &endHour, &endMinutes);

    if ( itemsScanned == ITEMS_SCANNED_TIME*2 &&
         checkTimeValue(startHour, startMinutes) &&
         checkTimeValue(endHour, endMinutes) &&
         startHour <= endHour &&
         (startHour != endHour || startMinutes <= endMinutes )){
        /* Read in the leader name, check validity
            The name of the leader can't be longer than 20 characters,
            and it can't contain spaces.
        */
        strcpy(leader, readInNonTitleWord());
        if ( strlen(leader) <= MAX_LENGTH_NON_TITLE ){
            /* Read in the title, check validity
                The activity title starts from the first
                non-whitespace character after the leader's name,
                and it extends up to the end of the line.
                The title may contain whitespace.
            */
            title = (char *)malloc(sizeof(char));
            title = eatRestOfLine(title);
            if ( strlen(title) > 0 ){
                valid = true;
            }
        }
        else {
            // Probably left input on the line
            eatRestOfLine(NULL);
        }
    }
    else {
        // Probably left input on the line
        eatRestOfLine(NULL);
    }

    if ( valid ){
        // Get space for an activity
        a = (Activity *)malloc( sizeof( Activity ) );
        // Populate new activity (id is just a placeholder)
        a->title = title;
        strcpy(a->leader, leader);
        a->startT = (Time){startHour, startMinutes};
        a->endT = (Time){endHour, endMinutes};
        a->id = 0;
        // Return new activity
        return a;
    }

    else {
        // Return null pointer
        return NULL;
    }

}

// See activity.h for description
char *eatRestOfLine(char *storage){

    // Declare basic variables
    int nextChar = EOF;
    int storageLen = 1;
    bool inLeadingWS = true;
    static char charsEaten = 0;
    static char *ptrCharsEaten = &charsEaten;
    // Reset char counter to zero (static is just meant to allow caller access)
    charsEaten = 0;

    // Eat up the rest of the line
    while (nextChar) {
        nextChar = getchar();
        if ( nextChar == EOF || nextChar == '\n' ){
            nextChar = 0;
        }
        else {
            charsEaten++;
            if ( storage ){
                // Store the remaining characters (excluding leading whitespace)
                if ( inLeadingWS == true && nextChar != ' ' ){
                    inLeadingWS = false;
                }
                if ( !inLeadingWS ){
                    // For 1 char, we'd need 2 bytes (to null terminate string)
                    storageLen++;
                    storage = (char *)realloc(storage, sizeof(char)*storageLen);
                    if ( storage ){
                        storage[storageLen-2] = nextChar;
                    }
                    else {
                        printf("%s", OUT_OF_MEMORY);
                        exit(EXIT_FAILURE);
                    }
                }
                storage[storageLen-1] = '\0';
            }
        }
    }

    /* Return
        If caller wanted storage for the chars on the line, return updated ptr to that
        Otherwise, return ptr to count of chars eaten
    */
    if ( storage ){
        return storage;
    }
    else {
        return ptrCharsEaten;
    }
}

// See activity.h for description
char *readInNonTitleWord(){

    // Declare variables
    static char word[MAX_LENGTH_NON_TITLE+1];
    char format[NON_TITLE_FORMAT_LENGTH];

    // Create a format string for reading in the word
    sprintf(format, "%%%ds", MAX_LENGTH_NON_TITLE+1);

    // Read in the word
    scanf(format, word);

    // Return
    return word;

}

// See activity.h for description
void freeActivity( Activity *act ){
    free(act->title);
    free(act);
}

// See activity.h for description
bool checkTimeValue( int hours, int minutes ){
    return hours >= 0 &&
            hours <= MAX_HOUR &&
            minutes >= 0 &&
            minutes <= MAX_MINUTE;
}

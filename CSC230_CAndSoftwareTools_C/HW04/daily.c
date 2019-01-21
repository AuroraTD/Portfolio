/**
    @file daily.c
    @author Aurora Tiffany-Davis (attiffan)

    Store, update and report on a daily schedule of activities,
    each with a start time and end time.
    There are interactive commands to add and remove activities from the schedule
    and query activities based on different criteria.
*/

// Include headers
#include "activity.h"
#include "schedule.h"
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <stdlib.h>

// Macro values
#define MAX_LENGTH_COMMAND 8
#define INVALID_COMMAND "Invalid command\n"
#define SCHEDULE_CONFLICT "Schedule conflict\n"

/** Store, update and report on a daily schedule of activities.
    First, create a schedule.
    Then, repeatedly prompt the user for commands to act on that structure.

    @return Success
*/
int main()
{

    // Declare variables
    char command[MAX_LENGTH_COMMAND];
    char leaderToTest[MAX_LENGTH_NON_TITLE+1];
    char matchWordToTest[MAX_LENGTH_NON_TITLE+1];
    int hourToTest;
    int minuteToTest;
    int idToRemove;
    int i;
    int itemsScanned;
    bool foundID;
    bool quitting = false;
    Activity *newAct;
    Schedule *mySchedule;

    // Create schedule
    mySchedule = createSchedule();

    // Repeatedly prompt for input and act on that input
    do {

        // Prompt for input
        printf("> ");
        itemsScanned = scanf("%s", command);

        /* QUIT
            Terminate the program without prompting for any more commands
        */
        if ( itemsScanned < 0 || !strcmp(command, "quit") ){
            quitting = true;
            freeSchedule(mySchedule);
            mySchedule = NULL;
        }

        /* ADD
            See comments in called functions for more details.
        */
        else if ( !strcmp(command, "add") ){
            // Get info about what the user wants to add
            newAct = readActivity();
            // Add to schedule if appropriate
            if ( newAct ){
                if ( !addActivity( mySchedule, newAct ) ){
                    printf("%s", SCHEDULE_CONFLICT);
                }
            }
            else {
                printf("%s", INVALID_COMMAND);
            }
        }

        /* REMOVE
            Entered as the keyword remove followed by whitespace,
            then the ID of an activity.
            On success, the requested activity is removed from the schedule
            (and nothing is printed).
            The command is considered invalid if
            remove isn't followed by the integer ID of an activity
            that's currently in the schedule.
        */
        else if ( !strcmp(command, "remove") ){
            // Get info about what the user wants to remove
            itemsScanned = scanf("%d", &idToRemove);

            // Loop through all activities to find and remove the activity
            foundID = false;
            // Don't bother if the ID is obviously wacky
            if ( itemsScanned > 0 && idToRemove >= 0 && idToRemove <= mySchedule->numActs ){
                for (i=0; i<mySchedule->numActs; i++){
                    if ( mySchedule->activities[i] &&
                         mySchedule->activities[i]->id == idToRemove ){
                        freeActivity(mySchedule->activities[i]);
                        mySchedule->activities[i] = NULL;
                        foundID = true;
                        break;
                    }
                }
            }
            if ( !foundID ){
                // Complain
                printf("%s", INVALID_COMMAND);
                // Don't leave garbage on the buffer
                if ( itemsScanned <= 0 ){
                    eatRestOfLine(NULL);
                }
            }
        }

        /* SCHEDULE
            The schedule command prints a report of all activities in the schedule.
            This command doesn't take any arguments.
            It will report activities one per line.
        */
        else if ( !strcmp(command, "schedule") ){
            // Print all activities
            printSchedule( mySchedule, &testAlwaysTrue, NULL );
        }

        /* SUMMARY
            Print out table summarizing the scheduled activities,
            with each row summarizing what's going on in a particular half-hour block,
            and each column reporting on what a particular activity leader is doing.
            This command doesn't take any arguments.
        */
        else if ( !strcmp(command, "summary") ){
            // Print all activities
            printSummary( mySchedule );
        }

        /* LEADER
            Print all activities for which the given person is the leader.
            The leader command is entered as the keyword leader followed by whitespace,
            then the name of a person, given as a word of up to 20 characters.
            The name can't contain whitespace.
            It's OK to ask about leaders that aren't associated with any activities.
            This should just print an empty report.
        */
        else if ( !strcmp(command, "leader") ){
            // Print activities that the given leader is in charge of
            strcpy(leaderToTest, readInNonTitleWord());
            // Only do this if the leader name is valid
            if ( strlen(leaderToTest) > MAX_LENGTH_NON_TITLE || *(eatRestOfLine(NULL)) > 0 ){
                printf("%s", INVALID_COMMAND);
            }
            else {
                printSchedule( mySchedule, &testLeader, leaderToTest );
            }
        }

        /* AT
            Print a list of all activities occurring at the given time.
            The at command is given as the keyword at,
            followed by whitespace then a time of day.
        */
        else if ( !strcmp(command, "at") ){
            // Print activities underway at the given time
            itemsScanned = scanf("%d : %d", &hourToTest, &minuteToTest);
            // Only do this if the time is valid
            if ( itemsScanned == ITEMS_SCANNED_TIME && checkTimeValue(hourToTest, minuteToTest) ){
                // Allocate memory for a Time instance
                Time *timeToTest = (Time *)malloc( sizeof( Time ) );
                timeToTest->hours = hourToTest;
                timeToTest->minutes = minuteToTest;
                // Print
                printSchedule( mySchedule, &testTime, timeToTest );
                // Free memory
                free(timeToTest);
            }
            else {
                // Complain
                printf("%s", INVALID_COMMAND);
                // Don't leave garbage on the buffer
                eatRestOfLine(NULL);
            }
        }

        /* MATCH
            Print a report of the activities
            that contain the given word as a substring in their title.
            The match command is given by the keyword match
            followed by a word of up to 20 characters.
            The given word can't contain spaces.
        */
        else if ( !strcmp(command, "match") ){
            //Print activities that have a title partially matching the given word
            strcpy(matchWordToTest, readInNonTitleWord());
            // Only do this if the leader name is valid
            if ( strlen(matchWordToTest) > MAX_LENGTH_NON_TITLE || *(eatRestOfLine(NULL)) > 0 ){
                printf("%s", INVALID_COMMAND);
            }
            else {
                printSchedule( mySchedule, &testMatch, matchWordToTest );
            }
        }

        /* INVALID
            Print to standard output, ignore the whole input line, and prompt for another command
        */
        else {
            printf("%s", INVALID_COMMAND);
            eatRestOfLine(NULL);
        }

    } while ( quitting == false );

    return EXIT_SUCCESS;

}

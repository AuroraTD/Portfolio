/**
    @file schedule.c
    @author Aurora Tiffany-Davis (attiffan)

    Functions for working with instances of the Schedule struct.
*/

// Include headers
#include "schedule.h"
#include "activity.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

// See schedule.h for description
Schedule *createSchedule(){
    // Get space for a schedule
    Schedule *s = (Schedule *)malloc( sizeof( Schedule ) );
    // Populate schedule with empty resizeable array of pointers
    s->activities = (Activity **)malloc(sizeof(Activity *));
    s->numActs = 0;
    // Return new schedule
    return s;
}

// See schedule.h for description
bool addActivity( Schedule *sched, Activity *act ){

    // Declare variables
    bool added = false;

    // Either free or add activity, depending on conflict status
    if ( isConflict(sched, act) ){
        freeActivity(act);
    }
    else {
        /* Activity ID was a placeholder before, gets a real ID now
            The activity will be given the next consecutive ID number,
            starting from one for the first activity.
        */
        sched->numActs++;
        act->id = sched->numActs;
        // Add to schedule
        sched->activities =
            (Activity **)realloc(sched->activities, sizeof(Activity *)*(sched->numActs));
        if ( sched->activities ){
            sched->activities[sched->numActs-1] = act;
        }
        else {
            printf("%s", OUT_OF_MEMORY);
            exit(EXIT_FAILURE);
        }
        // Remember it was added
        added = true;
    }

    // Return
    return added;

}

// See schedule.h for description
bool isConflict( Schedule *sched, Activity *act ){
    // Declare variables
    int i;
    bool conflict = false;
    // Go through all activities, stopping if a conflict is found
    for (i=0; i<sched->numActs; i++){
        if ( sched->activities[i] == NULL ||
             strcmp(sched->activities[i]->leader, act->leader) ||
             sinceMidnight(act->startT) >= sinceMidnight(sched->activities[i]->endT) ||
             sinceMidnight(act->endT) <= sinceMidnight(sched->activities[i]->startT) ){
                // Keep looking for a conflict
        }
        else {
            // Remember the conflict
            conflict = true;
            // Stop looking
            break;
        }
    }
    // Return
    return conflict;
}

// See schedule.h for description
int sinceMidnight( Time time ){

    // Declare variables
    int minutesSinceMidnight = 0;

    // Calculate
    minutesSinceMidnight += time.hours*MINUTES_IN_HOUR;
    minutesSinceMidnight += time.minutes;

    // Return
    return minutesSinceMidnight;

}

// See schedule.h for description
void printSchedule( Schedule *sched, bool (*test)( Activity *, void *arg ), void *arg ){

    // Declare variables
    int i;
    int lenLongestName = 0;
    int lenThisName;
    bool testResults[sched->numActs];
    char formatString[LEADER_NAME_FORMAT_LENGTH];

    /* Sort the activities
        Activities are sorted by starting time
        and by the name of the leader for the activity
        if activities start at the same time.
    */
    qsort(sched->activities, sched->numActs, sizeof(Activity *), *compareActivities);

    // Loop through all activities to find the longest reportable leader name
    for (i=0; i<sched->numActs; i++){
        if ( sched->activities[i] && test(sched->activities[i], arg) ){
            testResults[i] = true;
            lenThisName = strlen(sched->activities[i]->leader);
            if ( lenThisName > lenLongestName ){
                lenLongestName = lenThisName;
            }
        }
        else {
            testResults[i] = false;
        }
    }

    // Create a format string for printing leader names
    sprintf(formatString, "%%-%2ds ", lenLongestName);

    // Whenever printing a schedule of activities, always print a blank line first
    printf("\n");

    // Loop through all activities, printing if needed
    for (i=0; i<sched->numActs; i++){
        if ( testResults[i] ){
            /* Print start time
                The report for an activity will start with its starting time,
                printed as hours, then a colon, then minutes.
                The hours value should be printed right-justified in a two-column field,
                padded with spaces if necessary.
                The minutes should be printed in a two-column field,
                padded with zeros on the left if needed.
            */
            printf("%2d:", sched->activities[i]->startT.hours);
            printf("%02d ", sched->activities[i]->startT.minutes);
            /* Print end time
                The starting time should be followed by a space,
                then the ending time for the activity, in the same format.
            */
            printf("%2d:", sched->activities[i]->endT.hours);
            printf("%02d ", sched->activities[i]->endT.minutes);
            /* Print ID
                This should be followed by a space,
                then the activity ID in parentheses.
                The activity ID should be right-justified
                in a field of at least 3-columns,
                padded with zeros on the left as needed.
            */
            printf("(%03d) ", sched->activities[i]->id);
            /* Print leader
                The ID should be followed by a space,
                then the name of the activity leader.
                The name of the leader should be
                left-justified in a field wide enough
                for the longest name being reported
                (not necessarily the longest name in the whole schedule).
            */
            printf(formatString, sched->activities[i]->leader);
            /* Print title
                The leader should be followed by another space,
                then the title of the activity.
            */
            printf("%s\n", sched->activities[i]->title);
        }
    }

}

// See schedule.h for description
void printSummary( Schedule *sched ){

    // If there are no activities, there is nothing to summarize
    if ( sched->numActs > 0 ){

        // Declare variables
        int i, j;
        int numLeaders = 0;
        int numTimeSlots =
            (MINUTES_IN_HOUR/(double)MINUTES_PER_TIME_SLOT) * HOURS_PER_DAY;
        int slotStartSinceMidnight, startJustHours, startJustMinutes;
        int slotEndSinceMidnight, endJustHours, endJustMinutes;
        int earliestStartSinceMidnight, latestEndSinceMidnight;
        int lenLongestName = 0;
        int lenThisName;
        char **leaders;
        bool addLeader;
        Activity dummyAct;

        /* Find overall time range of the day's activities
            Rows of the summary should go from the first 30-minute block containing any activity,
            to the last 30 minute block containing any activity.
            See schedule printing function for description of sorting
            Inefficiency: both print schedule and print summary will sort schedule on each call.
        */
        qsort(sched->activities, sched->numActs, sizeof(Activity *), *compareActivities);
        for (i=0; i<sched->numActs; i++){
            if ( sched->activities[i] ){
                earliestStartSinceMidnight = sinceMidnight( sched->activities[i]->startT );
                break;
            }
        }
        latestEndSinceMidnight = sinceMidnight( sched->activities[sched->numActs-1]->endT );

        // Create a sorted list of leaders, find longest leader name
        for (i=0; i<sched->numActs; i++){

            if ( sched->activities[i] ){

                // Assume this activity will tell us a new leader name
                addLeader = true;
                for (j=0; j<numLeaders; j++){

                    if ( strcmp(leaders[j], sched->activities[i]->leader) == 0 ){
                        addLeader = false;
                        break;
                    }
                }

                // If that assumption held, keep track of this new leader name
                if ( addLeader ){
                    numLeaders++;
                    if ( numLeaders == 1 ){
                        leaders = (char **)malloc(sizeof(char *));
                    }
                    else {
                        leaders = (char **)realloc(leaders, numLeaders * sizeof(char *));
                    }
                    leaders[numLeaders-1] = malloc(MAX_LENGTH_NON_TITLE * sizeof(char));
                    strcpy(leaders[numLeaders-1], sched->activities[i]->leader);
                }

                lenThisName = strlen(sched->activities[i]->leader);
                if ( lenThisName > lenLongestName ){
                    lenLongestName = lenThisName;
                }

            }
        }
        qsort(leaders, numLeaders, sizeof(char *), *compareLeaders);

        // Go through all time slots
        for (i=0; i<numTimeSlots; i++){

            // Is this even a time slot we care about?
            slotStartSinceMidnight = i*MINUTES_PER_TIME_SLOT;
            slotEndSinceMidnight = (i+1)*MINUTES_PER_TIME_SLOT;
            if (
                slotStartSinceMidnight >= earliestStartSinceMidnight &&
                slotEndSinceMidnight <= latestEndSinceMidnight
                ){

                // Advance to the next line
                printf("\n");

                // Clarify what time slot we're looking at
                startJustHours = slotStartSinceMidnight / MINUTES_IN_HOUR;
                startJustMinutes = slotStartSinceMidnight % MINUTES_IN_HOUR;
                endJustHours = slotEndSinceMidnight / MINUTES_IN_HOUR;
                endJustMinutes = slotEndSinceMidnight % MINUTES_IN_HOUR;

                /* Each row should start with a time,
                    reported in the same format as for the schedule command.
                */
                printf("%2d:", startJustHours);
                printf("%02d", startJustMinutes);

                /* Print asterisk(s)
                    There is an asterisk (star)
                    in each column where the leader for that column is doing some activity.
                    The column for the left-most leader starts two spaces after the time,
                    and there are two-space gaps between the columns.
                */
                for (j=0; j<numLeaders; j++){
                    printf("  ");
                    // Create a dummy "activity" for this time slot and leader
                    dummyAct = (Activity){
                                .startT =
                                (Time){.hours = startJustHours, .minutes = startJustMinutes},
                                .endT =
                                (Time){.hours = endJustHours, .minutes = endJustMinutes},
                                .id = 0
                                };
                    strcpy(dummyAct.leader, leaders[j]);
                    // Looking for an activity matching ("conflicting") time slot and leader
                    if ( isConflict( sched, &dummyAct ) ){
                        printf("*");
                    }
                    else {
                        printf(" ");
                    }
                }

            }

        }

        /* Print leader names
            At the bottom of each column, print the name of each leader,
            with letters going top to bottom.
            Here also, every row will be the same length,
            so you may print some spaces at the end of some rows,
            if the rightmost names are shorter than other names.
            The name section at the bottom should be just tall enough for all the leaders' names.
        */
        for (i=0; i<lenLongestName; i++){
            // Add spaces to line up with printed times above
            printf("\n     ");
            for (j=0; j<numLeaders; j++){
                printf("  ");
                // Either print a char or a space to stay lined up
                if ( strlen(leaders[j]) > i ){
                    printf("%c", leaders[j][i]);
                }
                else {
                    printf(" ");
                }
            }
        }
        printf("\n");

        // Free memory used to track leader names
        for (i=0; i<numLeaders; i++){
            free(leaders[i]);
        }
        free(leaders);

    }

}

// See schedule.h for description
bool testAlwaysTrue( Activity *act, void *arg ){
    return true;
}

// See schedule.h for description
bool testLeader( Activity *act, void *arg ){
    // Matching the leader's name is case sensitive
    return !strcmp(act->leader, (char *)arg);
}

// See schedule.h for description
bool testMatch( Activity *act, void *arg ){

    // Declare variables
    int matchWordLength = strlen((char *)arg);
    int titleLength = strlen(act->title);
    char matchWordLC[matchWordLength+1];
    char titleLC[titleLength+1];
    int i;

    /* Create lower-case versions to compare
        Comparison is case insensitive.
    */
    for (i = 0; i<matchWordLength; i++){
      matchWordLC[i] = tolower(((char*)arg)[i]);
    }
    matchWordLC[matchWordLength] = '\0';
    for (i = 0; i<titleLength; i++){
      titleLC[i] = tolower(act->title[i]);
    }
    titleLC[titleLength] = '\0';

    /* Perform comparison
        Can match any sequence of characters in the title,
        even just part of a longer word.
        A null pointer means no match.
    */
    return strstr(titleLC, matchWordLC) ? true:false;

}

// See schedule.h for description
bool testTime( Activity *act, void *arg ){
    // Starts at or before given time, ends after?
    return sinceMidnight(act->startT) <= sinceMidnight(*(Time *)arg) &&
            sinceMidnight(act->endT) > sinceMidnight(*(Time *)arg);
}

// See schedule.h for description
int compareActivities(const void * act1, const void * act2){
    // Declare variables
    int sinceMidnight1;
    int sinceMidnight2;
    int comparison = 0;
    Activity *ptrAct1 = *((Activity **)act1);
    Activity *ptrAct2 = *((Activity **)act2);
    /* The pointers passed in are pointers to pointers to activities
        Removing an activity doesn't rearrange the array of pointers,
        just frees the activity and sets the given pointer to NULL.
        So, don't try to compare values in a freed activity.
        But need some comparison basis.
        So say that a freed activity is always less than a meaningful activity.
    */
    if ( !ptrAct1 || !ptrAct2 ){
        // Compare
        if ( !ptrAct1 && ptrAct2 ){
            comparison = -1;
        }
        else if ( ptrAct1 && !ptrAct2 ){
            comparison = 1;
        }
        else {
            comparison = 0;
        }
    }
    else {
        // Get info on start time for convenience
        sinceMidnight1 = sinceMidnight( ptrAct1->startT );
        sinceMidnight2 = sinceMidnight( ptrAct2->startT );
        // Compare
        if ( sinceMidnight1 < sinceMidnight2 ){
            comparison = -1;
        }
        else if ( sinceMidnight1 > sinceMidnight2 ){
            comparison = 1;
        }
        else {
            // strcmp returns <0 if leader1 < leader 2
            comparison = strcmp( ptrAct1->leader, ptrAct2->leader );
        }
    }
    // Return
    return comparison;
}

// See schedule.h for description
int compareLeaders (const void * a, const void * b){
    // The pointers passed in are pointers to strings (char **)
    return strcmp (*(char **) a, *(char **) b);
}

// See schedule.h for description
void freeSchedule( Schedule *sched ){

    // Declare variables
    int i;
    // Loop through all activities, freeing them
    for (i=0; i<sched->numActs; i++){
        if ( sched->activities[i] ){
            freeActivity(sched->activities[i]);
            sched->activities[i] = NULL;
        }
    }
    // Free activities list
    free(sched->activities);
    // Free schedule itself
    free(sched);

}

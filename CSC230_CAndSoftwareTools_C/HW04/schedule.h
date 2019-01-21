/**
    @file schedule.h
    @author Aurora Tiffany-Davis (attiffan)

    Function prototypes and macro definitions to support working with schedules.
*/

#ifndef SCHEDULE_H
#define SCHEDULE_H

// Include headers
#include "activity.h"
#include <stdbool.h>

// Macro values
#define MINUTES_IN_HOUR 60
#define LEADER_NAME_FORMAT_LENGTH 5
#define MINUTES_PER_TIME_SLOT 30
#define HOURS_PER_DAY 24

// Struct for schedules
struct ScheduleTag {
    Activity **activities;
    int numActs;
};
typedef struct ScheduleTag Schedule;

// Function prototypes

/** Dynamically allocate space for a new, empty Schedule instance and return a pointer to it.
    Includes creating the resizeable array of activity pointers stored within the Schedule.

    @return s Pointer to new Schedule
*/
Schedule *createSchedule();

/** Add the given activity to the given schedule,
    growing the resizable array as needed and returning true if successful.
    Check for scheduling conflicts;
    return false if the given activity overlaps with an activity the leader is already doing.

    @param sched Pointer to the schedule
    @param act Pointer to an activity
    @param added True if the activity was added, False otherwise
*/
bool addActivity( Schedule *sched, Activity *act );

/** Find out if there is a conflict between a given activity
    and a given existing schedule.
    Conflict means same leader, overlapping time.
    It's not a conflict if a leader has to start a new activity
    exactly when their previous activity ends.

    @param conflict True if there is a conflict, False otherwise.
*/
bool isConflict( Schedule *sched, Activity *act );

/** Calculate the minutes since midnight, for a given time struct.

    @param time Time of interest

    @return minutesSinceMidnight Number of minutes since midnight
*/
int sinceMidnight( Time time );

/** Go through the list of activities and print a subset in the right order.

    @param sched Pointer to the schedule
    @param test Pointer to a test function to determine if an activity should be printed
    @param arg Argument to pass to the test function to help it decide
*/
void printSchedule( Schedule *sched, bool (*test)( Activity *, void *arg ), void *arg );

/** Print out table summarizing the scheduled activities,
    with each row summarizing what's going on in a particular half-hour block,
    and each column reporting on what a particular activity leader is doing.

    @param sched Pointer to the schedule
*/
void printSummary( Schedule *sched );

/** Simply return true (used to print all activities in a schedule).

    @param activity Pointer to an activity, not used in this test function
    @param arg Expected to be NULL, not used in this test function

    @return True
*/
bool testAlwaysTrue( Activity *act, void *arg );

/** Test an activity to see if it is led by the given leader.

    @param activity Pointer to an activity
    @param arg Pointer to the name of a leader

    @return True if the activity is led by the given leader, False otherwise.
*/
bool testLeader( Activity *act, void *arg );

/** Test an activity to see if its title is a partial match against the given word.
    Case insensitive.

    @param activity Pointer to an activity
    @param arg Pointer to a word to match

    @return True if the title is a partial match against the given word, False otherwise.
*/
bool testMatch( Activity *act, void *arg );

/** Test an activity to see if it is underway at the given time.

    @param activity Pointer to an activity
    @param arg Pointer to a time struct

    @return True if the activity is underway at the given time, False otherwise.
*/
bool testTime( Activity *act, void *arg );

/** Compare two array values and return a value indicating which comes first.
    Sort by starting time and by the name of the leader for the activity
    if activities start at the same time

    @param act1 Pointer to pointer to an activity
    @param act2 Pointer to pointer to an activity

    @return comparison
                <0 if act1 comes first
                0 if equivalent
                >0 if act2 comes first
*/
int compareActivities(const void * act1, const void * act2);

/** Compare two leader names and return a value indicating which comes first.

    @param a Pointer to a string
    @param b Pointer to a string

    @return comparison
                <0 if a comes first
                0 if equivalent
                >0 if b comes first
*/
int compareLeaders (const void * a, const void * b);

/** Free all the dynamically allocated memory used by a schedule,
    including the schedule object itself,
    its resizable array of Activity pointers,
    and the Activity instances themselves.

    @param sched Pointer to the schedule
*/
void freeSchedule( Schedule *sched );

#endif // SCHEDULE_H

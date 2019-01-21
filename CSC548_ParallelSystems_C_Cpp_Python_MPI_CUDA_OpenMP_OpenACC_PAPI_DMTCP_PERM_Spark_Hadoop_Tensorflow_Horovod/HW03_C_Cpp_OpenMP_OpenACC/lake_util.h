#ifndef __LAKE_LOG_H__
#define __LAKE_LOG_H__

#include <stdio.h>
#include <stdarg.h>
#include <string.h>

typedef struct {
  char logfile[64];
  FILE *lf;
} _LOGFILE;

_LOGFILE mylog;

char lake_workdir[64];

void set_wrkdir(char *arg0)
{
  int ln_dir = strrchr(arg0,'/')-arg0+1;
  strncpy(lake_workdir, arg0, ln_dir);
  lake_workdir[ln_dir]='\0';
}

void dir_string(char *file, char *full)
{
  sprintf(full, "%s/%s", lake_workdir, file);
}

void start_lake_log(char *lf_name)
{
  dir_string(lf_name, mylog.logfile);
  mylog.lf = fopen(mylog.logfile, "w+");
}

/*************************************************************************************************************
 * FUNCTION:        lake_log
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Log a message to a log file, and also printf the same message
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw3/lake.tar
 *
 * ARGUMENTS:       msg -   The message with format flags embedded, e.g. "%d"
 *                  args -  Arguments that go along with format flags
 *************************************************************************************************************/
void lake_log (char *msg, ...) {

    // Declare variables
    va_list args;

    // Print to file
    va_start(args, msg);
    vfprintf(mylog.lf, msg, args);
    va_end(args);

    // Print to terminal
    va_start(args, msg);
    vprintf(msg, args);
    va_end(args);

}

void stop_lake_log()
{
  fclose(mylog.lf);
}



#endif

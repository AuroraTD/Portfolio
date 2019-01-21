/*************************************************************************************************************
 * FILE:            S3_InstallationAnalysis.java
 *
 * AUTHOR:          nnegi2      Natansh Negi
 *                  cshao       Chengcheng Shao
 *                  zsun12      Zelin Sun
 *                  attiffan    Aurora Tiffany-Davis
 *
 * DESCRIPTION:     Performs a default-configuration installation, followed by a launch,
 *                  of each of 3 Database Management Systems (DBMS):
 *                      - MySQL
 *                      - PostgresQL
 *                      - MongoDB
 *                  Measures and reports the following:
 *                      - Continuously
 *                          - CPU usage
 *                          - RAM usage
 *                          - Disk space
 *                      - Via logging during operations
 *                          - Number of dependencies installed
 *                          - Time to install
 *                          - Time to launch
 *                         
 * TO COMPILE:      javac S3_InstallationAnalysis.java
 *                  For Linux, you must also have a few other things installed: 
 *                      'sudo apt-get install mpstat'
 *                      'sudo apt-get install xterm'
 * 
 * TO RUN:          java S3_InstallationAnalysis [OS]
 *************************************************************************************************************/

// IMPORTS
import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;

// Main class
public class S3_InstallationAnalysis implements Runnable
{
    
    // Declare constants
    private static final int OS_WINDOWS = 1;
    private static final int OS_MAC = 2;
    private static final int OS_LINUX = 3;
    
    // Declare variables
    private static PrintWriter oWriter = null;
    private static int operatingSystem = -1;
    private static boolean shuttingDown = false;
    
    /*********************************************************************************************************
     * FUNCTION:        S3_InstallationAnalysis constructor
     *
     * DESCRIPTION:     Exists only to support running a separate thread for monitoring CPU, etc.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public S3_InstallationAnalysis () {
        // Nothing to do here
    }
    
    /*********************************************************************************************************
     * FUNCTION:        run
     *
     * DESCRIPTION:     A method that can be run as a separate thread to monitor CPU, etc.
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    public void run() {
        try {
            while (shuttingDown == false) {
                snapshot_CPU_RAM_Disk();
                Thread.sleep(100);
            }
        }
        catch (Throwable oError) {
            handleError(oError);
        }
    }
             
    /*********************************************************************************************************
     * FUNCTION:        main
     *
     * DESCRIPTION:     Orchestrates the installation / launch testing
     *
     * ARGUMENTS:       0 - Operating system
     *                      1 = Windows
     *                      2 = Mac
     *                      3 = Linux
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    public static void main(String[] args) throws IOException, AWTException 
    {
        
        try {

        	// Remember what OS we are using
            operatingSystem = checkOperatingSystem();
            
            // Declare variables
            String sOS;
            
            // Start up
            logMessage("Starting up");
            
            // Check arguments
            if (operatingSystem ==- 1) {
                throw new Exception("Usage: java S3_InstallationAnalysis [OS (1 = Windows, 2 = Mac, 3 = Linux)]");
            }
            else {
                
                // Start a separate thread that will monitor CPU, etc.
                S3_InstallationAnalysis instance = new S3_InstallationAnalysis();
                Thread thread = new Thread(instance);
                thread.start();
                
                switch (operatingSystem) {
                case OS_WINDOWS:
                    sOS = "Windows";
                    break;
                case OS_MAC:
                    sOS = "Mac";
                    break;
                case OS_LINUX:
                    sOS = "Linux";
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
                }
                logMessage("Running on " + sOS);
                
                // Install DBMS
                installMySQL();
                installPostgresQL();
                installMongoDB();
                
                // Shut down
                shutDown();
                
            }
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        snapshot_CPU_RAM_Disk
     *
     * DESCRIPTION:     Takes a snapshot of CPU and RAM and Disk Space usage
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void snapshot_CPU_RAM_Disk() throws IOException, AWTException {
        
        try {
            
            // Perform actions based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	// Command for CPU performance
                	executeCommand("@for /f \"skip=1\" %p in ('wmic cpu get loadpercentage') do @echo %p%");            
                    // Command for Disk space of C Drive (where database is being installed)
                	executeCommand("fsutil volume diskfree c:");           	
                    break;
                case OS_MAC:
                    //make ORS="" in awk to avoid /n
                    executeCommand("ps -A -o %cpu,%mem | awk '{s+=$1;b+=$2} END {print \"cpu:\" s \"%\",\"ram:\" b \"%\",ORS=\"\"}'; df | awk '/ \\/$/{print \"HDD \"$5}'");
                    break;
                case OS_LINUX:
                    /* Get CPU, RAM, and Disk
                     * https://askubuntu.com/questions/941949/one-liner-to-show-cpu-ram-and-hdd-usage
                     */
                    executeCommand("echo \"CPU `LC_ALL=C top -bn1 | grep \"Cpu(s)\" | sed \"s/.*, *\\([0-9.]*\\)%* id.*/\\1/\" | awk '{print 100 - $1}'`% RAM `free -m | awk '/Mem:/ { printf(\"%3.1f%%\", $3/$2*100) }'` HDD `df -h / | awk '/\\// {print $(NF-1)}'`\"");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
        
    /*********************************************************************************************************
     * FUNCTION:        checkOperatingSystem
     *
     * DESCRIPTION:     Detects the Operaing System
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         int
     *********************************************************************************************************/
    private static int checkOperatingSystem(){
 	   
    	String OS = System.getProperty("os.name").toLowerCase();
 	   	logMessage(OS);
 		if (OS.indexOf("win") >= 0) {
 			logMessage("This is Windows");
 			return 1;
 		} else if (OS.indexOf("mac") >= 0) {
 			logMessage("This is Mac");
 			return 2;
 		} else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
 			logMessage("This is Linux");
 			return 3;
 		} else {
 			logMessage("Your OS is not supported!!");
 			return -1;
 		}
    }  
    
    /*********************************************************************************************************
     * FUNCTION:        installMySQL
     *
     * DESCRIPTION:     Install MySQL, then launch it, then stop it, then uninstall it (for multiple runs)
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void installMySQL() {
        
        try {

            // Log
            logMessage("Installing MySQL");
            
            // Perform actions needed to install this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	executeCommand("pushd C:\\mysql\\bin");
                	executeCommand("mysqld --initialize-insecure --console");
                    break;
                case OS_MAC:
                    executeCommand("brew install mysql");
                    break;
                case OS_LINUX:
                    // Pre clean
                    cleanupInstallLinux();
                    // Extra MySQL cleanup
                    cleanupInstallLinuxMySQL();
                    // Install with flags to force it through
                    executeCommand("sudo apt-get -y -f install mysql*");
                    // Safeguard
                    executeCommand("sudo apt -y -f --fix-broken install");
                    // Print version number
                    executeCommand("mysql --version");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Starting MySQL");
            
            // Perform actions needed to start this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	executeCommand("mysql -u root");
                    break;
                case OS_MAC:
                    executeCommand("brew services start mysql");
                    break;
                case OS_LINUX:
                    executeCommand("sudo service mysql start");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Stopping MySQL");
            
            // Perform actions needed to stop this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	executeCommand("mysql -u root shutdown");
                    break;
                case OS_MAC:
                    executeCommand("brew services stop mysql");
                    break;
                case OS_LINUX:
                    executeCommand("sudo service mysql stop");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Uninstalling MySQL");
            
            // Perform actions needed to un-install this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	executeCommand("pushd C:\\mysql");
                	executeCommand("\"rmdir /s C:\\mysql\\data");
                    break;
                case OS_MAC:
                    executeCommand("brew remove mysql");
                    break;
                case OS_LINUX:
                    /* Uninstall with flags to force it through
                     *  The one thing I could not figure out how to force was a new window prompt opening
                     *      making the user confirm that all MySQL databases should be destroyed.
                     *  When the uninstall is started from Java, the symptom is that 
                     *      the window does NOT show but it IS waiting on feedback (unacceptable).
                     *  This window appears to be launched at a lower level than I have access to with this command.
                     *  So, launch a new terminal to start the uninstall, allowing for the window to pop up
                     *  We don't care how long the UNinstall takes, so although this user interaction is annoying, 
                     *      it won't mess up our measurements.
                     */
                    executeCommand("xterm -e \"sudo apt-get -y -f remove --purge mysql*\"");
                    // Post clean
                    cleanupInstallLinux();
                    // Extra MySQL cleanup
                    cleanupInstallLinuxMySQL();
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        installPostgresQL
     *
     * DESCRIPTION:     Install PostgresQL, then launch it, then stop it, then uninstall it (for multiple runs)
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void installPostgresQL() {
            
        try {

            // Log
            logMessage("Installing PostgresQL");
            
            // Perform actions needed to install this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                    executeCommand("pushd C:\\pgsql");            
                    executeCommand("mkdir data"); 
                    executeCommand("pushd C:\\pgsql\\bin");
                	executeCommand("initdb.exe -U postgres -A password -E utf8 -W -D C:\\postgresql\\pgsql\\data");
                	break;
                case OS_MAC:
                    executeCommand("brew install postgresql@11");
                    break;
                case OS_LINUX:
                    // Pre clean
                    cleanupInstallLinux();
                    // Extra PostgresQL cleanup
                    cleanupInstallLinuxPostgresQL();
                    // Install with flags to force it through
                    executeCommand("sudo apt-get -y -f install postgresql");
                    // Safeguard
                    executeCommand("sudo apt -y -f --fix-broken install");
                    // Print version number
                    executeCommand("postgresql --version");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Starting PostgresQL");
            
            // Perform actions needed to start this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	executeCommand("pg_ctl -D ^\"C^:^\\pgsql^\\data^\" -l logfile start");
                    break;
                case OS_MAC:
                    executeCommand("brew services start postgresql");
                    break;
                case OS_LINUX:
                    executeCommand("sudo service postgresql start");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Stopping PostgresQL");
            
            // Perform actions needed to stop this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	executeCommand("pg_ctl -D ^\"C^:^\\pgsql^\\data^\" -l logfile stop");
                    break;
                case OS_MAC:
                    executeCommand("brew services stop postgresql");
                    break;
                case OS_LINUX:
                    executeCommand("sudo service postgresql stop");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Uninstalling PostgresQL");
            
            // Perform actions needed to un-install this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                     executeCommand("pushd C:\\pgsql");
                     executeCommand("rmdir /s C:\\pgsql\\data");
                    break;
                case OS_MAC:
                    executeCommand("brew remove postgresql");
                    break;
                case OS_LINUX:
                    // Uninstall with flags to force it through
                    executeCommand("sudo apt-get -y -f remove --purge postgresql");
                    // Post clean
                    cleanupInstallLinux();
                    // Extra PostgresQL cleanup
                    cleanupInstallLinuxPostgresQL();
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        installMongoDB
     *
     * DESCRIPTION:     Install MongoDB, then launch it, then stop it, then uninstall it (for multiple runs)
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void installMongoDB() {
        
        try {
           //Process reference
        	Process process=null;
        
            // Log
            logMessage("Installing MongoDB");
            
            // Perform actions needed to install this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                    executeCommand("pushd \"C:\\Program Files\"");
                	executeCommand("msiexec.exe /l*v mdbinstall.log  /qb /i mongodb-win32-x86_64-2008plus-ssl-4.0.4-signed.msi");
                	executeCommand("md \"\\data\\db\" \"\\data\\log\"");
                    break;
                case OS_MAC:
                    executeCommand("brew install mongodb@3.6");
                    break;
                case OS_LINUX:
                    // Pre clean
                    cleanupInstallLinux();
                    // Extra MongoDB cleanup
                    cleanupInstallLinuxMongoDB();
                    // Install with flags to force it through
                    executeCommand("sudo apt-get -y -f install mongodb*");
                    // Safeguard
                    executeCommand("sudo apt -y -f --fix-broken install");
                    // Print version number
                    executeCommand("mongodb --version");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Starting MongoDB");
            
            // Perform actions needed to start this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	process=executeCommand("\"C:\\Program Files\\MongoDB\\Server\\4.0\\bin\\mongod.exe\" --dbpath=\"c:\\data\\db\"");
                    break;
                case OS_MAC:
                    executeCommand("brew services start mongodb");
                    break;
                case OS_LINUX:
                    executeCommand("sudo service mongodb start");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Stopping MongoDB");
            
            // Perform actions needed to stop this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	process.destroy();
                    break;
                case OS_MAC:
                    executeCommand("brew services stop mongodb");
                    break;
                case OS_LINUX:
                    executeCommand("sudo service mongodb stop");
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
            // Log
            logMessage("Uninstalling MongoDB");
            
            // Perform actions needed to un-install this DBMS, based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                	executeCommand("msiexec.exe /l*v mdbinstall.log  /qb /x mongodb-win32-x86_64-2008plus-ssl-4.0.4-signed.msi");
                    break;
                case OS_MAC:
                    executeCommand("brew remove mongodb");
                    break;
                case OS_LINUX:
                    // Uninstall with flags to force it through
                    executeCommand("sudo apt-get -y -f remove --purge mongodb*");
                    // Post clean
                    cleanupInstallLinux();
                    // Extra MongoDB cleanup
                    cleanupInstallLinuxMongoDB();
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        cleanupInstallLinux
     *
     * DESCRIPTION:     Perform pre- and post-install cleanup tasks
     *                  These may not all be necessary on every run, but should not be harmful either
     *                  Some of these are common-sense
     *                  Some of these were necessary while Linux install of MySQL was in development
     *                      (and I needed to clean up after partial installs or uninstalls)
     *                  Some sources:
     *                      https://help.cloud66.com/maestro/how-to-guides/databases/shells/uninstall-mysql.html
     *                      https://stackoverflow.com/questions/25244606/completely-remove-mysql-ubuntu-14-04-lts
     *                      https://askubuntu.com/questions/984797/clean-autoclean-and-autoremove-combining-them-is-a-good-step?noredirect=1&lq=1
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void cleanupInstallLinux() {
        
        try {
            
            executeCommand("sudo rm /var/lib/dpkg/lock*");
            executeCommand("sudo rm /var/cache/debconf/config.dat");
            executeCommand("sudo rm /var/cache/apt/archives/lock");
            executeCommand("sudo apt-get clean");
            executeCommand("sudo apt-get autoremove -y --purge");
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        cleanupInstallLinuxMySQL
     *
     * DESCRIPTION:     Perform pre- and post-install cleanup tasks
     *                  These may not all be necessary on every run, but should not be harmful either
     *                  Some of these are common-sense
     *                  Some of these were necessary while Linux install of MySQL was in development
     *                      (and I needed to clean up after partial installs or uninstalls)
     *                  Some sources:
     *                      https://help.cloud66.com/maestro/how-to-guides/databases/shells/uninstall-mysql.html
     *                      https://stackoverflow.com/questions/25244606/completely-remove-mysql-ubuntu-14-04-lts
     *                      https://askubuntu.com/questions/984797/clean-autoclean-and-autoremove-combining-them-is-a-good-step?noredirect=1&lq=1
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void cleanupInstallLinuxMySQL() {
        
        try {
            
            executeCommand("sudo rm -rf /etc/mysql"); 
            executeCommand("sudo rm -rf /var/lib/mysql"); 
            executeCommand("sudo rm /etc/apparmor.d/usr.sbin.mysqld");
            executeCommand("sudo find / -iname 'mysql*' -exec rm -rf {} \\;");
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        cleanupInstallLinuxPostgresQL
     *
     * DESCRIPTION:     Perform pre- and post-install cleanup tasks
     *                  These may not all be necessary on every run, but should not be harmful either
     *                  https://stackoverflow.com/questions/2748607/how-to-thoroughly-purge-and-reinstall-postgresql-on-ubuntu
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void cleanupInstallLinuxPostgresQL() {
        
        try {
            
            executeCommand("sudo rm -r /etc/postgresql/");
            executeCommand("sudo rm -r /etc/postgresql-common/");
            executeCommand("sudo rm -r /var/lib/postgresql/");
            executeCommand("userdel -r postgres");
            executeCommand("groupdel postgres");
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        cleanupInstallLinuxMongoDB
     *
     * DESCRIPTION:     Perform pre- and post-install cleanup tasks
     *                  These may not all be necessary on every run, but should not be harmful either
     *                  https://stackoverflow.com/questions/29554521/uninstall-mongodb-from-ubuntu
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void cleanupInstallLinuxMongoDB() {
        
        try {
            
            executeCommand("sudo rm -r /var/log/mongodb");
            executeCommand("sudo rm -r /var/lib/mongodb");
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        executeCommand
     *
     * DESCRIPTION:     Execute a command as you would on the terminal
     *                  Send all output to the log so that we can see what happened (with timestamps)
     *                  https://www.admfactory.com/how-to-execute-a-shell-command-from-java/
     *                  https://stackoverflow.com/questions/25199307/unable-using-runtime-exec-to-execute-shell-command-echo-in-android-java-code
     *
     * ARGUMENTS:       commandToExecute
     *                      
     * RETURNS:         Process object
     * @return 
     *********************************************************************************************************/
    private static Process executeCommand(String commandToExecute) {
        
    	Process process=null;
        try {
            
            // Declare variables      
            BufferedReader processReader;
            String processOutputLine;
            String commandPrefix1 = "";
            String commandPrefix2 = "";
            
            // Build the command based on OS
            switch (operatingSystem) {
                case OS_WINDOWS:
                    commandPrefix1 = "cmd.exe";
                    commandPrefix2 = "/c";
                    break;
                case OS_MAC:
                    commandPrefix1 = "sh";
                    commandPrefix2 = "-c";
                    break;
                case OS_LINUX:
                    commandPrefix1 = "sh";
                    commandPrefix2 = "-c";
                    break;
                default:
                    throw new Exception("Do not recognize OS number " + operatingSystem);
            }
            String[] command = {commandPrefix1, commandPrefix2, commandToExecute};
            
            // Run the process
            process = Runtime.getRuntime().exec(command);
            
            /* Log the output of the process
             * Do this BEFORE waiting for completion
             * https://stackoverflow.com/questions/5483830/process-waitfor-never-returns
             * ADD another tab so that the explicitly logged messages are easier to find
             */
            processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((processOutputLine = processReader.readLine()) != null) {       	
            	if(!(processOutputLine.equals("") || processOutputLine.equals("%"))){
            		logMessage("\t" + processOutputLine);
                }            
            }
            
            // Wait for completion
            process.waitFor();
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        return process;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        logMessage
     *
     * DESCRIPTION:     Logs a provided message to file (with a timestamp)
     *                  Does NOT call error handler, to avoid circular logic
     *                  (error handler calls logger)
     *                  This method is synchronized so that it can be called by multiple threads if needed
     *
     * ARGUMENTS:       messageToLog -  The message to add to the log
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static synchronized void logMessage(String messageToLog) {
        
        try {
            
            // Declare constants
            final String S_LOG_FILE = "log.txt";
            
            // Initialize writer if needed
            if (oWriter == null) {
                oWriter = new PrintWriter(S_LOG_FILE, "UTF-8");
            }
            
            // Log to file
            oWriter.println(new Timestamp(System.currentTimeMillis()) + "\t" + messageToLog);
            
            // Flush to be on the safe side
            oWriter.flush();
            
        }
        catch (Throwable oError) {
            // IGNORE
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleError
     *
     * DESCRIPTION:     Handle an error.
     *                  Any error causes the program to terminate immediately, to speed up debugging.
     *
     * ARGUMENTS:       oError -    A throwable object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void handleError (Throwable oError) {
        
        // Tell the user in a friendly way that we have an error
        System.out.println("\nOops!  We have an error.  Program will shut down.\n");
        
        // Print the stack trace
        oError.printStackTrace();
        
        // Log the stack trace
        logMessage(oError.getMessage());
        
        // Abandon ship (gets us debugging quicker)
        shutDown();
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        shutDown
     *
     * DESCRIPTION:     Clean up whatever needs cleaning up
     *                  Does NOT call error handler, to avoid circular logic
     *                  (error handler calls shut down)
     *
     * ARGUMENTS:       None
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    private static void shutDown() {
        
        // Log
        logMessage("Shutting down");
        
        // Set flag so that we can stop the separate thread
        shuttingDown = true;
        
        // Close log writer
        if (oWriter != null) {
            oWriter.close();
        }
        
        // Exit
        System.exit(0);

    }

}

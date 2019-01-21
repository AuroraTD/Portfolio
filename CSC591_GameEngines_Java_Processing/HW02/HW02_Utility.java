package HW02;
/*************************************************************************************************************
 * FILE:            HW03_Utility.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Provide utility methods for use by either the client or the server
 *************************************************************************************************************/

// IMPORTS
import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

// CLASS DEFINITION
public class HW02_Utility {
    
    private static final int    N_WINDOW_SIZE_PX =          500;
    
    /*********************************************************************************************************
     * FUNCTION:        getWindowSize
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the window size that both the client(s) and server should use
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Port number
     *********************************************************************************************************/
    public static int getWindowSize () {
        return N_WINDOW_SIZE_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPortNumber
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *                  https://www.tutorialspoint.com/java/io/java_io_file.htm
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nPort - Port number
     *********************************************************************************************************/
    public static int getPortNumber () {
        
        int nPortNumber = -1;
        try {
            
            // Declare variables
            File oFile;
            Scanner oScanner;
            
            // Get port file
            oFile = new File("port.txt");
            
            // Get port number
            if (oFile.exists() == false) {
                nPortNumber = -1;
            }
            else {
                oScanner = new Scanner(oFile);
                if (oScanner.hasNext() == false) {
                    nPortNumber = -1;
                }
                else {
                    nPortNumber = oScanner.nextInt();
                }
                oScanner.close();
            }
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        return nPortNumber;
     
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setPortNumber
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Randomly choose a port number and write it to file so that the client can use same.
     *                  Mitigates risk of in-use port numbers if ports not properly released by
     *                  previous run of my homework or previous run of another student's homework.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static int setPortNumber () {
        
        int nPortNumber = -1;
        try {
            
            // Declare variables
            File oFile;
            FileWriter oFileWriter;
            Random oRandomizer;
            
            // Get port file
            oFile = new File("port.txt");
            
            // Start fresh
            if (oFile.exists()) {
                oFile.delete();
            }
            
            // Pick a random port number
            oRandomizer = new Random();
            nPortNumber = 5100 + oRandomizer.nextInt(300);
            
            // Write port number to file
            oFileWriter = new FileWriter(oFile);
            oFileWriter.write(String.valueOf(nPortNumber));
            oFileWriter.close();
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        return nPortNumber;
        
    }

    /*********************************************************************************************************
     * FUNCTION:        handleError
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle an error.
     *                  While error handling was not specifically required in this homework assignment,
     *                      it is good practice to always include some explicit error handling.
     *                  The same error handler is used on both the server and client,
     *                      to provide a consistent look and feel for the users and maintainers of the game.
     *                  At this beginning stage of game engine development,
     *                      any error causes the program to terminate immediately, 
     *                      to speed up debugging.
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
        
        // Abandon ship (gets us debugging quicker)
        System.exit(1);
        
    }

}
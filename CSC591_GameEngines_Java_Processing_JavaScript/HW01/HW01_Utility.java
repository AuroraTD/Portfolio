package HW01;
/*************************************************************************************************************
 * FILE:            HW01_Utility.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Provide utility methods for use by either the client or the server
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW01_Utility {
    
    private static final int    N_PORT =            5432;
    private static final int    N_WINDOW_SIZE_PX =  600;
    private static final int    N_PLAYER_WIDTH_PX = 20;
    
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
     * FUNCTION:        getPlayerWidth
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the player object width that both the client(s) and server should use
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Port number
     *********************************************************************************************************/
    public static int getPlayerWidth () {
        return N_PLAYER_WIDTH_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPortNumber
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the port number that both the client(s) and server should use
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Port number
     *********************************************************************************************************/
    public static int getPortNumber () {
        return N_PORT;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getIndexOfGUID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get a specific game object, given that object's GUID
     *
     * ARGUMENTS:       aoGameObjects - All known game objects
     *                  nGUID -         The GUID of the object of interest
     * 
     * RETURNS:         nIndex -        The index of the object within the collection
     *********************************************************************************************************/
    public static int getIndexOfGUID (CopyOnWriteArrayList<?> aoGameObjects, int nGUID) {
        
        int nIndex = -1;
        
        try {
            
            // Declare variables
            int i;
            
            // Find game object
            for (i = 0; i < aoGameObjects.size(); i++) {
                if (((HW01_RenderableObject) aoGameObjects.get(i)).getGUID() == nGUID) {
                    nIndex = i;
                    break;
                }
            }
            
        }
        catch (Throwable oError) {
            handleError(oError);
        }
        
        return nIndex;
        
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
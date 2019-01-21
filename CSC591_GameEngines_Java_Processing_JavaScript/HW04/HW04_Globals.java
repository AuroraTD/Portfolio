/*************************************************************************************************************
 * FILE:            HW04_Globals.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class simply to store global items
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

// CLASS DEFINITION
public class HW04_Globals implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    public static enum GameChoice {
        PLATFORMER,
        BUBBLE_SHOOTER,
        SPACE_INVADERS
    }
    
    // Object properties
    public static HW04_ObjectCharacter                                                  oMyCharacterObject =    null;
    public static ConcurrentHashMap<Integer, BlockingQueue<HW04_ObjectCommunicable>>    oObjectsToWrite =       null;
    public static boolean                                                               bClient =               true;
    public static GameChoice                                                            oGameChoice =           GameChoice.PLATFORMER;
    public static int                                                                   nFrameRate =            60;
    public static int                                                                   nPlayerID =             -1;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Globals Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new parameters object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_Globals () {
        try {
            // Nothing to do here
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }

}
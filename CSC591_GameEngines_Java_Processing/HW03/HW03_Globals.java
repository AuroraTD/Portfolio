/*************************************************************************************************************
 * FILE:            HW03_Globals.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class simply to store global items
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

// CLASS DEFINITION
public class HW03_Globals implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties
    public static ConcurrentHashMap<Integer, BlockingQueue<HW03_ObjectCommunicable>>   oObjectsToWrite =    null;
    public static boolean   bClient =                                                                       true;
    public static boolean   bStandardEventManagementProtocol =                                              true;
    public static boolean   bRunPerformanceTest =                                                           false;
    public static int       nPerformanceTestIterations =                                                    -1;
    public static int       nPerformanceTestNumEvents =                                                     100;
    public static int       nNumPlatformsStatic =                                                           3;
    public static int       nNumPlatformsDynamic =                                                          6;
    public static int       nFrameRate =                                                                    60;
    public static int       nPlayerID =                                                                     -1;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Globals Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new parameters object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Globals () {
        try {
            // Nothing to do here
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

}
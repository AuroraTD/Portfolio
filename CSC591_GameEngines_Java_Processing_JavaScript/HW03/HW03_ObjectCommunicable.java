/*************************************************************************************************************
 * FILE:            HW03_ObjectCommunicable.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a communicable object.
 *                  Game object, Event object, etc.
 *************************************************************************************************************/

// IMPORTS
import java.io.ObjectStreamException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW03_ObjectCommunicable implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Private properties
    private String sObjectType;
    protected int nPlayerID;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectCommunicable Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new communicable object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectCommunicable () {
        try {
                    
            // Remember properties that will be useful in making sense of objects rec'd across the network
            this.sObjectType = this.getClass().getSimpleName();
            this.nPlayerID = HW03_Globals.nPlayerID;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getType
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         sObjectType - "Player", "World", etc.
     *********************************************************************************************************/
    public String getType () {
        return this.sObjectType;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPlayerID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nPlayerID -     The player ID of the client who created this object
     *                                  Or, -1 if the server is the one who created this object
     *********************************************************************************************************/
    public int getPlayerID () {
        return this.nPlayerID;
    }

    /*********************************************************************************************************
     * FUNCTION:        castObject
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Cast an object to a specific type
     *                  so that we know how to work it.
     *                  This is needed because of serialization.
     *                  Server serializes and sends objects of various specific types.
     *                  Client must make sense of what it receives.
     *
     * ARGUMENTS:       oObject -   The object to cast
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void castObject (HW03_ObjectCommunicable oObject) {
        try {
            
            // Declare variables
            String sObjectType;
            
            // Cast object
            sObjectType = oObject.getType();
            switch (sObjectType) {
                case "HW03_ObjectSpawnPoint":
                    oObject = (HW03_ObjectSpawnPoint) oObject;
                    break;
                case "HW03_ObjectWinZone":
                    oObject = (HW03_ObjectWinZone) oObject;
                    break;
                case "HW03_ObjectDeathZone":
                    oObject = (HW03_ObjectDeathZone) oObject;
                    break;
                case "HW03_ObjectBoundary":
                    oObject = (HW03_ObjectBoundary) oObject;
                    break;
                case "HW03_ObjectStatusScoreboard":
                    oObject = (HW03_ObjectStatusScoreboard) oObject;
                    break;
                case "HW03_ObjectStatusSummary":
                    oObject = (HW03_ObjectStatusSummary) oObject;
                    break;
                case "HW03_ObjectStatusInstructions":
                    oObject = (HW03_ObjectStatusInstructions) oObject;
                    break;
                case "HW03_ObjectPlatformStatic":
                    oObject = (HW03_ObjectPlatformStatic) oObject;
                    break;
                case "HW03_ObjectPlatformH":
                    oObject = (HW03_ObjectPlatformH) oObject;
                    break;
                case "HW03_ObjectPlatformV":
                    oObject = (HW03_ObjectPlatformV) oObject;
                    break;
                case "HW03_ObjectCharacter":
                    oObject = (HW03_ObjectCharacter) oObject;
                    break;
                case "HW03_ObjectCollidable":
                    oObject = (HW03_ObjectCollidable) oObject;
                    break;
                case "HW03_ObjectEvent":
                    oObject = (HW03_ObjectEvent) oObject;
                    break;
                case "HW03_ObjectEventRemoteRegistration":
                    oObject = (HW03_ObjectEventRemoteRegistration) oObject;
                    break;
                default:
                    throw new Exception("Object type '" + sObjectType + "' not recognized");
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
}
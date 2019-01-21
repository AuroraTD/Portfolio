/*************************************************************************************************************
 * FILE:            HW04_ObjectCommunicable.java
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
public class HW04_ObjectCommunicable implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Private properties
    private String sObjectType;
    protected int nPlayerID;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectCommunicable Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new communicable object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectCommunicable () {
        try {
                    
            // Remember properties that will be useful in making sense of objects rec'd across the network
            this.sObjectType = this.getClass().getSimpleName();
            this.nPlayerID = HW04_Globals.nPlayerID;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
    public static void castObject (HW04_ObjectCommunicable oObject) {
        try {
            
            // Declare variables
            String sObjectType;
            
            // Cast object
            sObjectType = oObject.getType();
            switch (sObjectType) {
                case "HW04_ObjectSpawnPoint":
                    oObject = (HW04_ObjectSpawnPoint) oObject;
                    break;
                case "HW04_ObjectZoneWin":
                    oObject = (HW04_ObjectZoneWin) oObject;
                    break;
                case "HW04_ObjectZoneDeath":
                    oObject = (HW04_ObjectZoneDeath) oObject;
                    break;
                case "HW04_ObjectBoundary":
                    oObject = (HW04_ObjectBoundary) oObject;
                    break;
                case "HW04_ObjectStatusScoreboard":
                    oObject = (HW04_ObjectStatusScoreboard) oObject;
                    break;
                case "HW04_ObjectStatusSummary":
                    oObject = (HW04_ObjectStatusSummary) oObject;
                    break;
                case "HW04_ObjectStatusInstructions":
                    oObject = (HW04_ObjectStatusInstructions) oObject;
                    break;
                case "HW04_ObjectPlatformStatic":
                    oObject = (HW04_ObjectPlatformStatic) oObject;
                    break;
                case "HW04_ObjectPlatformH":
                    oObject = (HW04_ObjectPlatformH) oObject;
                    break;
                case "HW04_ObjectPlatformV":
                    oObject = (HW04_ObjectPlatformV) oObject;
                    break;
                case "HW04_ObjectBubble":
                    oObject = (HW04_ObjectBubble) oObject;
                    break;
                case "HW04_ObjectArrow":
                    oObject = (HW04_ObjectArrow) oObject;
                    break;
                case "HW04_ObjectBullet":
                    oObject = (HW04_ObjectBullet) oObject;
                    break;
                case "HW04_ObjectEnemy":
                    oObject = (HW04_ObjectEnemy) oObject;
                    break;
                case "HW04_ObjectCharacter":
                    oObject = (HW04_ObjectCharacter) oObject;
                    break;
                case "HW04_ObjectCollidable":
                    oObject = (HW04_ObjectCollidable) oObject;
                    break;
                case "HW04_ObjectEvent":
                    oObject = (HW04_ObjectEvent) oObject;
                    break;
                case "HW04_ObjectEventRemoteRegistration":
                    oObject = (HW04_ObjectEventRemoteRegistration) oObject;
                    break;
                default:
                    throw new Exception("Object type '" + sObjectType + "' not recognized");
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
}
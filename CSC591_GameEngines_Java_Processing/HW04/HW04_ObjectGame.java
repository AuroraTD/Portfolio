/*************************************************************************************************************
 * FILE:            HW04_ObjectGame.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a basic game object.
 *                  Maintain a collection of all game objects.
 *                  Game objects are maintained as a hash map for easy lookup / replace / remove.
 *                  Game objects are exposed to the rest of the game engine in array lists.
 *                      This is for historical (not ripping apart tons of code) reasons,
 *                      as game objects were maintained in an array list in a previous version.
 *************************************************************************************************************/

// IMPORTS
import java.io.ObjectStreamException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW04_ObjectGame extends HW04_ObjectCommunicable implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    //  Maintain collection of all game objects
    private static ConcurrentHashMap<Integer, HW04_ObjectGame> oObjectsGame = new ConcurrentHashMap<Integer, HW04_ObjectGame>();
    
    // Object properties
    static private int      nNextGUID = 1;
    private int             nGUID;
    private boolean         bRemoveObject;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectGame Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new game object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectGame (int nExistingGUID) {
        try {
            
            /* Check for problems
             * Only the server should create game objects!
             * This is what puts the "G" in "GUID"
             */
            if (nExistingGUID < 0 && HW04_Globals.bClient == true) {
                throw new Exception("Cannot create '" + this.getType() + "' game object on Client!");
            }
            else {
                
                // Set GUID
                if (nExistingGUID < 0) {
                    this.nGUID = nNextGUID;
                    nNextGUID++;
                }
                else {
                    this.nGUID = nExistingGUID;
                }
                
                // Default to not flagged for removal
                this.bRemoveObject = false;
                
                // Add new object to collection
                addToGameObjects(this);
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRemovalFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bRemoveObject - True if the object is flagged for removal
     *********************************************************************************************************/
    public boolean getRemovalFlag () {
        return this.bRemoveObject;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setRemovalFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setRemovalFlag () {
        this.bRemoveObject = true;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getGUID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nGUID - The game object's GUID
     *********************************************************************************************************/
    public int getGUID () {
        return this.nGUID;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getGameObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oObjectsGame - Collection of all game objects (hashmap, key is object GUID)
     *********************************************************************************************************/
    public static ConcurrentHashMap<Integer, HW04_ObjectGame> getGameObjects () {
        return oObjectsGame;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addToGameObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Set
     *
     * ARGUMENTS:       oNewObject -    A new object to add to our collection
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addToGameObjects (HW04_ObjectGame oNewObject) {
        if (oObjectsGame.containsKey(oNewObject.nGUID) == false) {
            HW04_ObjectCommunicable.castObject(oNewObject);
            oObjectsGame.put(oNewObject.nGUID, oNewObject);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getObjectByGUID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       nGUID -         GUID of desired game object
     * 
     * RETURNS:         oObjectGame -   Game object
     *********************************************************************************************************/
    public static HW04_ObjectGame getObjectByGUID (int nGUID) {
        HW04_ObjectGame oObjectGame = null;
        try {
            
            if (oObjectsGame.containsKey(nGUID) == true) {
                oObjectGame = oObjectsGame.get(nGUID);
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        return oObjectGame;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        replaceObjectByGUID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Replace a game object in the game object collection, by its GUID
     *
     * ARGUMENTS:       nGUID -         GUID of desired game object
     *                  oObjectGame -   Updated game object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void replaceObjectByGUID (int nGUID, HW04_ObjectGame oObjectGame) {
        
        try {
            
            // Declare variables
            float nTeleportX_px =       -1;
            float nTeleportY_px =       -1;
            boolean bHidden =           false;
            boolean bShootingAllowed =  false;
            
            // Check for problems
            if (getObjectByGUID(nGUID).getType().equals(oObjectGame.getType()) == false) {
                throw new Exception(
                    "Object with GUID " + 
                    nGUID + 
                    " is of type " + 
                    getObjectByGUID(nGUID).getType() + 
                    " so we cannot replace it with object of type " +
                    oObjectGame.getType()
                );
            }
            else {
                
                /* Save teleportation location and hidden flag
                 * These are used by the replay feature
                 * These are attributes of an object that the CLIENT controls
                 * Even if all other aspects of the object are controlled by someone else
                 */
                if (oObjectGame instanceof HW04_ObjectLocated) {
                    nTeleportX_px = ((HW04_ObjectLocated) getObjectByGUID(nGUID)).getReplayTeleportX();
                    nTeleportY_px = ((HW04_ObjectLocated) getObjectByGUID(nGUID)).getReplayTeleportY();
                }
                if (oObjectGame instanceof HW04_ObjectRenderable) {
                    bHidden = ((HW04_ObjectRenderable) getObjectByGUID(nGUID)).getHiddenFlag();
                }
                
                /* Save allowed-to-shoot flag
                 * These are used by some games
                 * These are attributes of a character that the SERVER controls
                 * Even if all other aspects of the character are controlled by someone else
                 */
                if (oObjectGame instanceof HW04_ObjectCharacter) {
                    bShootingAllowed = ((HW04_ObjectCharacter) getObjectByGUID(nGUID)).getShootingAllowed();
                }
                
                // Cast and replace
                HW04_ObjectCommunicable.castObject(oObjectGame);
                oObjectsGame.replace(nGUID, oObjectGame);
                
                // Fix certain flags
                if (oObjectGame instanceof HW04_ObjectLocated) {
                    ((HW04_ObjectLocated) getObjectByGUID(nGUID)).setReplayTeleportX(nTeleportX_px);
                    ((HW04_ObjectLocated) getObjectByGUID(nGUID)).setReplayTeleportY(nTeleportY_px);
                }
                if (oObjectGame instanceof HW04_ObjectRenderable) {
                    ((HW04_ObjectRenderable) getObjectByGUID(nGUID)).setHiddenFlag(bHidden);
                }
                if (oObjectGame instanceof HW04_ObjectCharacter) {
                    ((HW04_ObjectCharacter) getObjectByGUID(nGUID)).setShootingAllowed(bShootingAllowed);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        replaceObject
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Replace a game object in the game object collection, by its GUID
     *
     * ARGUMENTS:       oObjectToReplace -  The game object to replace in the game object collection
     * 
     * RETURNS:         oRemovedObject -    The removed game object (or null)
     *********************************************************************************************************/
    public static void replaceObject (HW04_ObjectGame oObjectToReplace) {
        replaceObjectByGUID(oObjectToReplace.getGUID(), oObjectToReplace);
    }
    
    /*********************************************************************************************************
     * FUNCTION:        removeObjectByGUID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       nGUID -             GUID of game object to remove
     * 
     * RETURNS:         oRemovedObject -    The removed game object (or null)
     *********************************************************************************************************/
    public static HW04_ObjectGame removeObjectByGUID (int nGUID) {
        
        // Deregister interest in any events
        HW04_EventManager.getInstance().deRegisterForEvents(oObjectsGame.get(nGUID));
        
        // Remove from game object collections and return
        return oObjectsGame.remove(nGUID);
    }
    
    /*********************************************************************************************************
     * FUNCTION:        removeObject
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       oObjectToRemove -   The game object to remove
     * 
     * RETURNS:         oRemovedObject -    The removed game object (or null)
     *********************************************************************************************************/
    public static HW04_ObjectGame removeObject (HW04_ObjectGame oObjectToRemove) {
        return removeObjectByGUID(oObjectToRemove.getGUID());
    }
    
}
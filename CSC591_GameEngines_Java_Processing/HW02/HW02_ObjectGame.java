package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectGame.java
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
public class HW02_ObjectGame implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    //  Maintain collection of all game objects
    private static ConcurrentHashMap<Integer, HW02_ObjectGame> oObjectsGame = new ConcurrentHashMap<Integer, HW02_ObjectGame>();
    
    // Object properties
    static private int nProtocol = 0;
    static private int nNextGUID = 1;
    private int nGUID;
    private String sObjectType;
    private boolean bRemoveObject;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectGame Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new game object
     *                  Two scenarios in which this is called
     *                  - Actually creating a new object
     *                  - Re-creating an object in .readResolve() method
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW02_ObjectGame (int nExistingGUID) {
        try {
            
            // Set GUID - should ONLY be done by the server!
            if (nExistingGUID < 0) {
                this.nGUID = nNextGUID;
                nNextGUID++;
            }
            else {
                this.nGUID = nExistingGUID;
            }
            
            // Remember object type
            this.sObjectType = this.getClass().getSimpleName();
            
            // Default to not flagged for removal
            this.bRemoveObject = false;
            
            // Add new object to collection
            addToGameObjects(this);
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
     * FUNCTION:        getGameObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oObjectsGame - Collection of all game objects
     *********************************************************************************************************/
    public static ConcurrentHashMap<Integer, HW02_ObjectGame> getGameObjects () {
        return oObjectsGame;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setGameObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Set
     *
     * ARGUMENTS:       oNewCollection - Collection of all game objects
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void setGameObjects (CopyOnWriteArrayList<HW02_ObjectGame> oNewCollection) {
        
        // Declare variables
        int i;
        
        // Set
        oObjectsGame.clear();
        for (i = 0; i < oNewCollection.size(); i++) {
            addToGameObjects(oNewCollection.get(i));
        }
                
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
    public static void addToGameObjects (HW02_ObjectGame oNewObject) {
        if (oObjectsGame.containsKey(oNewObject.nGUID) == false) {
            oObjectsGame.put(oNewObject.nGUID, oNewObject);
            castGameObjectByGUID(oNewObject.nGUID);
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
    public static HW02_ObjectGame getObjectByGUID (int nGUID) {
        return oObjectsGame.get(nGUID);
    }
    
    /*********************************************************************************************************
     * FUNCTION:        replaceObjectByGUID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       nGUID -         GUID of desired game object
     *                  oObjectGame -   Updated game object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void replaceObjectByGUID (int nGUID, HW02_ObjectGame oObjectGame) {
        oObjectsGame.replace(nGUID, oObjectGame);
        castGameObjectByGUID(nGUID);
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
     * RETURNS:         oRemovedObject -    The removed game object (or null
     *********************************************************************************************************/
    public static HW02_ObjectGame removeObjectByGUID (int nGUID) {
        return oObjectsGame.remove(nGUID);
    }
    
    /*********************************************************************************************************
     * FUNCTION:        castGameObject
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Cast a game object to a specific type
     *                  so that we know how to work it.
     *                  This is needed because of serialization.
     *                  Server serializes game objects of various specific types and sends the collection.
     *                  Client must make sense of what it receives.
     *
     * ARGUMENTS:       nCastGUID - The GUID of the object to cast
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private static void castGameObjectByGUID (int nCastGUID) {
        try {
            
            // Declare variables
            String sObjectType;
            
            // Cast object
            sObjectType = oObjectsGame.get(nCastGUID).getType();
            switch (sObjectType) {
                case "HW03_ObjectSpawnPoint":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectSpawnPoint) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectWinZone":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectWinZone) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectDeathZone":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectDeathZone) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectBoundary":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectBoundary) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectStatusScoreboard":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectScoreboard) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectPlatformStatic":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectPlatformStatic) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectPlatformH":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectPlatformH) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectPlatformV":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectPlatformV) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectCharacter":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectCharacter) oObjectsGame.get(nCastGUID));
                    break;
                case "HW03_ObjectCollidable":
                    oObjectsGame.replace(nCastGUID, (HW02_ObjectCollidable) oObjectsGame.get(nCastGUID));
                    break;
                default:
                    throw new Exception("Object type '" + sObjectType + "' not recognized");
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setNetworkProtocol
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Set the network protocol
     *
     * ARGUMENTS:       nNetworkProtocol -  The network protocol to use
     *                                      0 - Send game objects
     *                                      1 - Use .writeReplace() and .readResolve()            
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void setNetworkProtocol (int nNetworkProtocol) {
        nProtocol = nNetworkProtocol;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        writeReplace
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Provide a method to replace an object before writing
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    Object writeReplace () throws ObjectStreamException {
        if (nProtocol == 0) {
            return this;
        }
        else {
            return new HW02_ObjectGameProxy(this);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW02_ObjectGameProxy
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Proxy game object class
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private static class HW02_ObjectGameProxy implements java.io.Serializable {
    
        // Required for serializable class
        private static final long serialVersionUID = 1L;
        
        // Delimiter
        private final String S_DELIMITER = ", ";
        
        // A place to keep the string representation
        private StringBuffer sStringRepresentation;

        /*********************************************************************************************************
         * FUNCTION:        HW02_ObjectGameProxy Constructor
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Constructor for a new game object proxy
         * 
         * ARGUMENTS:       oObjectGameOriginal - The original game object
         * 
         * RETURNS:         None
         *********************************************************************************************************/
        HW02_ObjectGameProxy (HW02_ObjectGame oObjectGameOriginal) {

            // Declare variables
            StringBuffer sObjectAsString;
            
            // Create
            sObjectAsString = new StringBuffer("");
            
            // Populate
            sObjectAsString.append(oObjectGameOriginal.getType());
            sObjectAsString.append(S_DELIMITER);
            sObjectAsString.append(oObjectGameOriginal.getGUID());
            sObjectAsString.append(S_DELIMITER);
            sObjectAsString.append(oObjectGameOriginal.getRemovalFlag());
            sObjectAsString.append(S_DELIMITER);
            
            /* Continue to populate based on object type
             * Include in the string only those values needed by this object's constructor
             * Or needed to set values on the object after creation
             * We do NOT need to set all values on objects - 
             *  so we don't need to include all values in the string!
             * Let's take a character object as an example
             *  When server first tells client about their character object, 
             *  default values from character object constructor are what we want
             *  After that, server should not tell client about their character object,
             *  And other clients only care about those attributes needed to render the object
             */
            if (oObjectGameOriginal instanceof HW02_ObjectLocated) {
                sObjectAsString.append(((HW02_ObjectLocated) oObjectGameOriginal).getPositionX());
                sObjectAsString.append(S_DELIMITER);
                sObjectAsString.append(((HW02_ObjectLocated) oObjectGameOriginal).getPositionY());
                sObjectAsString.append(S_DELIMITER);
            }
            if (oObjectGameOriginal instanceof HW02_ObjectCollidable) {
                sObjectAsString.append(((HW02_ObjectCollidable) oObjectGameOriginal).getWidth());
                sObjectAsString.append(S_DELIMITER);
                sObjectAsString.append(((HW02_ObjectCollidable) oObjectGameOriginal).getHeight());
                sObjectAsString.append(S_DELIMITER);
                sObjectAsString.append(((HW02_ObjectCollidable) oObjectGameOriginal).getRadius());
                sObjectAsString.append(S_DELIMITER);
            }
            if (oObjectGameOriginal instanceof HW02_ObjectRenderable) {
                sObjectAsString.append(((HW02_ObjectRenderable) oObjectGameOriginal).getColor().R);
                sObjectAsString.append(S_DELIMITER);
                sObjectAsString.append(((HW02_ObjectRenderable) oObjectGameOriginal).getColor().G);
                sObjectAsString.append(S_DELIMITER);
                sObjectAsString.append(((HW02_ObjectRenderable) oObjectGameOriginal).getColor().B);
                sObjectAsString.append(S_DELIMITER);
            }
            if (oObjectGameOriginal instanceof HW02_ObjectMoveable) {
                sObjectAsString.append(((HW02_ObjectMoveable) oObjectGameOriginal).getSpeedHorizontal());
                sObjectAsString.append(S_DELIMITER);
                sObjectAsString.append(((HW02_ObjectMoveable) oObjectGameOriginal).getSpeedVertical());
                sObjectAsString.append(S_DELIMITER);
            }
            if (oObjectGameOriginal instanceof HW02_ObjectCharacter) {
                sObjectAsString.append(((HW02_ObjectCharacter) oObjectGameOriginal).getPlayerID());
                sObjectAsString.append(S_DELIMITER);
            }
            
            // Save
            this.sStringRepresentation = sObjectAsString;
            
        }

        /*********************************************************************************************************
         * FUNCTION:        readResolve
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Provide a method to replace an object after reading
         *
         * ARGUMENTS:       None
         * 
         * RETURNS:         None
         * @throws Exception 
         *********************************************************************************************************/
        Object readResolve () throws Exception {
            
            // Declare variables
            
            String[] asTokens;
            String sTokenType =                 "";
            int nTokenGUID =                    -1;
            int nTokenX_px =                    -1;
            int nTokenY_px =                    -1;
            int nTokenWidth_px =                -1;
            int nTokenHeight_px =               -1;
            int nTokenRadius_px =               -1;
            int nTokenSpeedHorizontal_px =      -1;
            int nTokenSpeedVertical_px =        -1;
            int nTokenPlayerID =                -1;
            boolean bTokenRemovalFlag =         false;
            HW02_Color oTokenColor =            null;
            HW02_ObjectGame oObjectToReturn =   null;
            
            // Get tokens
            asTokens = this.sStringRepresentation.toString().split(", ");
            
            // Get values that are present in every game object
            if (asTokens.length > 2) {
                sTokenType = asTokens[0];
                nTokenGUID = Integer.parseInt(asTokens[1]);
                bTokenRemovalFlag = (asTokens[2].equals("1") ? true : false);
            }
            
            /* Get values that are present in every located game object
             * There are no proxy object values in non-located objects
             * beyond what we've already grabbed 
             */
            if (asTokens.length > 4) {
                nTokenX_px = Integer.parseInt(asTokens[3]);
                nTokenY_px = Integer.parseInt(asTokens[4]);
            }
            
            /* Get values that are present in every collidable game object
             * There are no proxy object values in non-collidable objects
             * beyond what we've already grabbed 
             */
            if (asTokens.length > 7) {
                nTokenWidth_px = Integer.parseInt(asTokens[5]);
                nTokenHeight_px = Integer.parseInt(asTokens[6]);
                nTokenRadius_px = Integer.parseInt(asTokens[7]);  
            }
            
            /* Get values that are present in every renderable game object
             * There are no proxy object values in non-renderable objects
             * beyond what we've already grabbed 
             */
            if (asTokens.length > 10) {
                oTokenColor = new HW02_Color(
                    Integer.parseInt(asTokens[8]),
                    Integer.parseInt(asTokens[9]),
                    Integer.parseInt(asTokens[10])
                );
            }
            
            /* Get values that are present in every moveable game object
             * There are no proxy object values in non-moveable objects
             * beyond what we've already grabbed 
             */
            if (asTokens.length > 12) {
                nTokenSpeedHorizontal_px = Integer.parseInt(asTokens[11]);
                nTokenSpeedVertical_px = Integer.parseInt(asTokens[12]);
            }
            
            /* Get values that are present in every character game object
             * There are no proxy object values in non-character objects
             * beyond what we've already grabbed 
             */
            if (asTokens.length > 13) {
                nTokenPlayerID = Integer.parseInt(asTokens[13]);
            }
            
            // Determine what kind of game object this string represents
            oObjectToReturn = null;
            switch (sTokenType) {
                case "HW03_ObjectBoundary":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectBoundary(
                        nTokenGUID, 
                        nTokenX_px, 
                        nTokenY_px, 
                        nTokenWidth_px, 
                        nTokenHeight_px
                    );
                    break;
                case "HW03_ObjectCharacter":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectCharacter(
                        nTokenGUID, 
                        nTokenPlayerID, 
                        nTokenX_px, 
                        nTokenY_px, 
                        null
                    );
                    // Make sure a given character is displayed in the same color by all clients
                    ((HW02_ObjectCharacter) oObjectToReturn).setColor(oTokenColor);
                    break;
                case "HW03_ObjectCollidable":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectCollidable(
                        nTokenGUID, 
                        nTokenX_px, 
                        nTokenY_px, 
                        nTokenWidth_px, 
                        nTokenHeight_px, 
                        nTokenRadius_px
                    );
                    break;
                case "HW03_ObjectDeathZone":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectDeathZone(
                        nTokenGUID, 
                        nTokenX_px, 
                        nTokenY_px, 
                        nTokenWidth_px, 
                        nTokenHeight_px
                    );
                    break;
                case "HW03_ObjectPlatformStatic":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectPlatformStatic(
                        nTokenGUID, 
                        nTokenX_px, 
                        nTokenY_px, 
                        nTokenWidth_px, 
                        nTokenHeight_px,
                        null
                    );
                    // Make sure a given platform is displayed in the same color by all clients
                    ((HW02_ObjectPlatform) oObjectToReturn).setColor(oTokenColor);
                    break;
                case "HW03_ObjectPlatformH":
                    oObjectToReturn = new HW02_ObjectPlatformH(
                        nTokenGUID, 
                        nTokenX_px, 
                        nTokenY_px, 
                        nTokenWidth_px, 
                        nTokenHeight_px,
                        null
                    );
                    // Make sure a given platform is displayed in the same color by all clients
                    ((HW02_ObjectPlatform) oObjectToReturn).setColor(oTokenColor);
                    // Make sure a given platform has the same speed setting for all clients
                    ((HW02_ObjectPlatform) oObjectToReturn).setSpeedHorizontal(nTokenSpeedHorizontal_px);
                    ((HW02_ObjectPlatform) oObjectToReturn).setSpeedVertical(nTokenSpeedVertical_px);
                    break;
                case "HW03_ObjectPlatformV":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectPlatformV(
                        nTokenGUID, 
                        nTokenX_px, 
                        nTokenY_px, 
                        nTokenWidth_px, 
                        nTokenHeight_px,
                        null
                    );
                    // Make sure a given platform is displayed in the same color by all clients
                    ((HW02_ObjectPlatform) oObjectToReturn).setColor(oTokenColor);
                    // Make sure a given platform has the same speed setting for all clients
                    ((HW02_ObjectPlatform) oObjectToReturn).setSpeedHorizontal(nTokenSpeedHorizontal_px);
                    ((HW02_ObjectPlatform) oObjectToReturn).setSpeedVertical(nTokenSpeedVertical_px);
                    break;
                case "HW03_ObjectStatusScoreboard":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectScoreboard(
                        nTokenGUID, 
                        null
                    );
                    break;
                case "HW03_ObjectSpawnPoint":
                    // Create new object
                    oObjectToReturn = new HW02_ObjectSpawnPoint(
                        nTokenGUID, 
                        nTokenX_px, 
                        nTokenY_px
                    );
                    break;
                case "HW03_ObjectWinZone":
                    oObjectToReturn = new HW02_ObjectWinZone(
                        nTokenGUID,
                        nTokenX_px,
                        nTokenY_px,
                        nTokenWidth_px,
                        nTokenHeight_px
                    );
                    break;
                default:
                    throw new Exception("Object type '" + asTokens[0] + "' not recognized");
            }
            
            // Flag object for removal if necessary
            if (bTokenRemovalFlag == true) {
                oObjectToReturn.setRemovalFlag();
            }
            
            // Return
            return oObjectToReturn;
            
        }
    
    }
    
}
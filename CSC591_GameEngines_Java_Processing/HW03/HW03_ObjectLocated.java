/*************************************************************************************************************
 * FILE:            HW03_ObjectLocated.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a game world object that has some location
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW03_ObjectLocated extends HW03_ObjectGame {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (variable)
    private float nX_px;
    private float nY_px;
    private float nReplayTeleportX_px;
    private float nReplayTeleportY_px;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectLocated Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new located game world object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectLocated (
        int nExistingGUID,
        float nX_px, 
        float nY_px
    ) {
        
        // Construct generic game world object
        super(nExistingGUID);
        
        // Set values
        try {
            
            // Set straightforward values
            this.nX_px = nX_px;
            this.nY_px = nY_px;
            
            // Set defaults for teleport so we can see if they have ever been given "real" values
            this.nReplayTeleportX_px = -1;
            this.nReplayTeleportY_px = -1;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getLocatedObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsLocated -    All located objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW03_ObjectLocated> getLocatedObjects () {
        CopyOnWriteArrayList<HW03_ObjectLocated> aoObjectsLocated = new CopyOnWriteArrayList<HW03_ObjectLocated>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW03_ObjectLocated) {
                    aoObjectsLocated.add((HW03_ObjectLocated) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return aoObjectsLocated;
    }
   
    /*********************************************************************************************************
     * FUNCTION:        getPositionX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nX_px - The object's X position, in pixels
     *********************************************************************************************************/
    public float getPositionX () {
        return this.nX_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setPositionX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *                  Enforce window boundaries as a safeguard
     *
     * ARGUMENTS:       nX_px - The object's X position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setPositionX (float nX_px) {
        try {
            
            // Declare variables
            float nMinX_px;
            float nMaxX_px;

            // Set safeguards
            nMinX_px = 0;
            if (this instanceof HW03_ObjectRenderable) {
                nMaxX_px = 
                    HW03_Utility.getWindowSize() - 
                    ((HW03_ObjectRenderable) this).getWidth();
            }
            else {
                nMaxX_px = HW03_Utility.getWindowSize();
            }
            
            // Set position
            if (nX_px < nMinX_px) {
                this.nX_px = nMinX_px;
            }
            else if (nX_px > nMaxX_px) {
                this.nX_px = nMaxX_px;
            }
            else {
                this.nX_px = nX_px;
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPositionY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nY_px - The object's Y position, in pixels
     *********************************************************************************************************/
    public float getPositionY () {
        return this.nY_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setPositionY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *                  Enforce window boundaries as a safeguard
     *
     * ARGUMENTS:       nY_px - The object's Y position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setPositionY (float nY_px) {
        try {

            // Declare variables
            float nMinY_px;
            float nMaxY_px;

            // Set safeguards
            nMinY_px = 0;
            if (this instanceof HW03_ObjectRenderable) {
                nMaxY_px = 
                    HW03_Utility.getWindowSize() - 
                    ((HW03_ObjectRenderable) this).getHeight();
            }
            else {
                nMaxY_px = HW03_Utility.getWindowSize();
            }
            
            // Set position
            if (nY_px < nMinY_px) {
                this.nY_px = nMinY_px;
            }
            else if (nY_px > nMaxY_px) {
                this.nY_px = nMaxY_px;
            }
            else {
                this.nY_px = nY_px;
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getReplayTeleportX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTeleportX_px - The object's X position, in pixels
     *********************************************************************************************************/
    public float getReplayTeleportX () {
        return this.nReplayTeleportX_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setReplayTeleportX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nTeleportX_px - The object's X position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setReplayTeleportX (float nTeleportX_px) {
        this.nReplayTeleportX_px = nTeleportX_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getReplayTeleportY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTeleportY_px - The object's Y position, in pixels
     *********************************************************************************************************/
    public float getReplayTeleportY () {
        return this.nReplayTeleportY_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setReplayTeleportY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nTeleportY_px - The object's Y position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setReplayTeleportY (float nTeleportY_px) {
        this.nReplayTeleportY_px = nTeleportY_px;
    }

}
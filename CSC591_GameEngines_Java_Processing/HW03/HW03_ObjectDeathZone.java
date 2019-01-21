import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/*************************************************************************************************************
 * FILE:            HW03_ObjectDeathZone.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a death zone in the game world
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW03_ObjectDeathZone extends HW03_ObjectCollidable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_RADIUS_PX = 0;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectDeathZone Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new death zone object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The x-axis location of the new object
     *                  nY_px -         The y-axis location of the new object
     *                  nWidth_px -     The width of the new object
     *                  nHeight_px -    The height of the new object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectDeathZone (int nExistingGUID, int nX_px, int nY_px, int nWidth_px, int nHeight_px) {
        
        // Construct collidable object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            nWidth_px, 
            nHeight_px, 
            N_RADIUS_PX
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getDeathZoneObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsDeathZone -    All death zone objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW03_ObjectDeathZone> getDeathZoneObjects () {
        CopyOnWriteArrayList<HW03_ObjectDeathZone> aoObjectsDeathZone = new CopyOnWriteArrayList<HW03_ObjectDeathZone>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW03_ObjectDeathZone) {
                    aoObjectsDeathZone.add((HW03_ObjectDeathZone) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return aoObjectsDeathZone;
    }

}
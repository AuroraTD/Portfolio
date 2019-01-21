/*************************************************************************************************************
 * FILE:            HW04_ObjectZoneWin.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a win zone in the game world
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW04_ObjectZoneWin extends HW04_ObjectCollidable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_RADIUS_PX = 0;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectZoneWin Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new win zone object
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
    public HW04_ObjectZoneWin (int nExistingGUID, int nX_px, int nY_px, int nWidth_px, int nHeight_px) {
        
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
     * FUNCTION:        addWinZoneToTopOfWindow
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add win zones to the game world
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addWinZoneToTopOfWindow () {
        try {
            
            // Add a single win zone at the top of the window
            new HW04_ObjectZoneWin(
                // Get auto GUID
                -1,
                0,
                HW04_ObjectStatus.getTotalHeight(),
                HW04_Utility.getWindowSize(),
                1
            );
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleCollision
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle collision with another object
     *
     * ARGUMENTS:       aoCollidingObjects -    The objects with which we have collided
     *                  nOriginalX -            X-axis location before the move that caused the collision
     *                  nOriginalY -            Y-axis location before the move that caused the collision
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void handleCollision(
        CopyOnWriteArrayList<HW04_ObjectCollidable> oCollidingObjects, 
        float nOriginalX,
        float nOriginalY
    ) {
        // No-Op (other objects care if they collide with us - we don't care)
    }

}
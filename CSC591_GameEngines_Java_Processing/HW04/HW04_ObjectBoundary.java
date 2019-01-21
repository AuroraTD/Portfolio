import java.util.concurrent.CopyOnWriteArrayList;

/*************************************************************************************************************
 * FILE:            HW04_ObjectBoundary.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a boundary in the game world
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW04_ObjectBoundary extends HW04_ObjectCollidable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_SKINNY_DIMENSION_PX =    1;
    private final static int N_RADIUS_PX =              0;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectBoundary Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new boundary object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     *                  nLength_px -    The length of the new object
     *                  bVertical -     True if the boundary is vertical (otherwise horizontal)
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectBoundary (int nExistingGUID, int nX_px, int nY_px, int nLength_px, boolean bVertical) {
        
        // Construct collidable object
        super(
            nExistingGUID, 
            nX_px, 
            nY_px, 
            // width
            (bVertical ? N_SKINNY_DIMENSION_PX : nLength_px), 
            // height
            (bVertical ? nLength_px : N_SKINNY_DIMENSION_PX), 
            N_RADIUS_PX
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectBoundary Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new boundary object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     *                  nWidth_px -     The width of the new object
     *                  nHeight_px -    The height of the new object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectBoundary (int nExistingGUID, int nX_px, int nY_px, int nWidth_px, int nHeight_px) {
        
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
     * FUNCTION:        getStaticSkinnyDimension
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the scoreboard object static height that both the client(s) and server should use
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Skinny dimension (pixels)
     *********************************************************************************************************/
    public static int getStaticSkinnyDimension () {
        return N_SKINNY_DIMENSION_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addBoundaryObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add boundary objects at each of the window's edges
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addBoundaryObjects () {
        try {

            // Create boundary on window (top)
            new HW04_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                0, 
                // Length
                HW04_Utility.getWindowSize(), 
                // Vertical
                false
            );
            
            // Create boundary on window (right)
            new HW04_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                HW04_Utility.getWindowSize() - HW04_ObjectBoundary.getStaticSkinnyDimension(),
                // Y
                0, 
                // Length
                HW04_Utility.getWindowSize(), 
                // Vertical
                true
            );
            
            // Create boundary on window (bottom)
            new HW04_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                HW04_Utility.getWindowSize() - HW04_ObjectBoundary.getStaticSkinnyDimension(),
                // Length
                HW04_Utility.getWindowSize(), 
                // Vertical
                false
            );
            
            // Create boundary on window (left)
            new HW04_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                0, 
                // Length
                HW04_Utility.getWindowSize(), 
                // Vertical
                true
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
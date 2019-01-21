/*************************************************************************************************************
 * FILE:            HW03_ObjectBoundary.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a boundary in the game world
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW03_ObjectBoundary extends HW03_ObjectCollidable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_SKINNY_DIMENSION_PX =    1;
    private final static int N_RADIUS_PX =              0;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectBoundary Constructor
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
    public HW03_ObjectBoundary (int nExistingGUID, int nX_px, int nY_px, int nLength_px, boolean bVertical) {
        
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
     * FUNCTION:        HW03_ObjectBoundary Constructor
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
    public HW03_ObjectBoundary (int nExistingGUID, int nX_px, int nY_px, int nWidth_px, int nHeight_px) {
        
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

}
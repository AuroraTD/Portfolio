package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectPlatformH.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a platform object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW02_ObjectPlatformH extends HW02_ObjectPlatform {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_MIN_SPEED_PX =   1;
    private final static int N_MAX_SPEED_PX =   4;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectPlatformH Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new platform object
     * 
     * ARGUMENTS:       nExistingGUID -     The GUID of the new object
     *                                      If -1, a new GUID will be automatically assigned
     *                  nX_px -             The position of the new object on the x axis (0 = left)
     *                  nY_px -             The position of the new object on the y axis (0 = top)
     *                  nWidth_px -         The width of the new object
     *                  nHeight_px -        The height of the new object
     *                  oApplet -           A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW02_ObjectPlatformH (
            int nExistingGUID,
            int nX_px,
            int nY_px,
            int nWidth_px,
            int nHeight_px,
            PApplet oApplet
    ) {
        
        // Construct platform object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            nWidth_px,
            nHeight_px,
            // Color randomly
            HW02_Color.getRandomShade("Yellow"),
            // Provide applet
            oApplet,
            // Move horizontally
            N_MIN_SPEED_PX + new Random().nextInt(N_MAX_SPEED_PX - N_MIN_SPEED_PX),
            0
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getHorizontalPlatformObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsRenderable -    All renderable objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW02_ObjectPlatformH> getHorizontalPlatformObjects () {
        CopyOnWriteArrayList<HW02_ObjectPlatformH> aoObjectsPlatformH = new CopyOnWriteArrayList<HW02_ObjectPlatformH>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW02_ObjectGame> oObjectsGame = HW02_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW02_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW02_ObjectPlatformH) {
                    aoObjectsPlatformH.add((HW02_ObjectPlatformH) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        return aoObjectsPlatformH;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleCollision
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle collision with another object
     *                  This is called after this object has been moved based on horizontal and vertical speeds
     *
     * ARGUMENTS:       oCollidingObjects - The objects with which we have collided
     *                  bYAxis -            True if the collision is on the Y (up/down) axis, otherwise false
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void handleCollision (
        CopyOnWriteArrayList<HW02_ObjectCollidable> oCollidingObjects,
        boolean yAxis
    ) {
        
        try {

            // Act only if it's "our fault"
            if (yAxis == false) {
                this.backOutOfCollisionX();
                this.reverseCourse();
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        
    }

}
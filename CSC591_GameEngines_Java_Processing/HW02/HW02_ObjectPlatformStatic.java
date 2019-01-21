package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectPlatformStatic.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a static platform object.
 *                  Although platforms are considered moveable objects, this one does not actually move.
 *                  It's a moveable object with horizontal and vertical speeds of zero.
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW02_ObjectPlatformStatic extends HW02_ObjectPlatform {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectPlatformStatic Constructor
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
    public HW02_ObjectPlatformStatic (
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
            HW02_Color.getRandomShade("Gray"),
            // Provide applet
            oApplet,
            // Zero speed
            0,
            0
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticPlatformObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsRenderable -    All renderable objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW02_ObjectPlatformStatic> getStaticPlatformObjects () {
        CopyOnWriteArrayList<HW02_ObjectPlatformStatic> aoObjectsPlatformStatic = new CopyOnWriteArrayList<HW02_ObjectPlatformStatic>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW02_ObjectGame> oObjectsGame = HW02_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW02_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW02_ObjectPlatformStatic) {
                    aoObjectsPlatformStatic.add((HW02_ObjectPlatformStatic) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        return aoObjectsPlatformStatic;
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
        
        // Static platform objects do nothing upon collision
        
    }

}
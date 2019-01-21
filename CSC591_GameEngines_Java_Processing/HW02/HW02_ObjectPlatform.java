package HW02;
/*************************************************************************************************************
 * FILE:            HW02_Platform.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a platform object
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public abstract class HW02_ObjectPlatform extends HW02_ObjectMoveable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_RADIUS_PX =      2;
    
    // Declare abstract methods
    abstract void handleCollision(
        CopyOnWriteArrayList<HW02_ObjectCollidable> oCollidingObjects,
        boolean yAxis
    );

    /*********************************************************************************************************
     * FUNCTION:        HW02_Platform Constructor
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
     *                  oColor -            The color of the new object
     *                  oApplet -           A PApplet object (window) that we want to draw an object onto
     *                  nSpeedDefaultH -    The default speed (pixels per game loop iteration)
     *                                      that the object moves along the horizontal axis
     *                                      Negative number = left
     *                                      Positive number = right
     *                                      Zero = stationary
     *                  nSpeedDefaultV -    The default speed (pixels per game loop iteration)
     *                                      that the object moves along the vertical axis
     *                                      Negative number = down
     *                                      Positive number = up
     *                                      Zero = stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW02_ObjectPlatform (
        int nExistingGUID,
        int nX_px,
        int nY_px,
        int nWidth_px,
        int nHeight_px,
        HW02_Color oColor,
        PApplet oApplet,
        int nSpeedDefaultH,
        int nSpeedDefaultV
    ) {
        
        // Construct moveable object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            nWidth_px, 
            nHeight_px, 
            N_RADIUS_PX, 
            oColor,
            oApplet,
            nSpeedDefaultH,
            nSpeedDefaultV
        );
        
    }

    
}
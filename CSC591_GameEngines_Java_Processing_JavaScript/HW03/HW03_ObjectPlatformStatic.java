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
public class HW03_ObjectPlatformStatic extends HW03_ObjectPlatform implements HW03_EventObserver {
    
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
    public HW03_ObjectPlatformStatic (
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
            HW03_Color.getRandomShade("Gray"),
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
    public static CopyOnWriteArrayList<HW03_ObjectPlatformStatic> getStaticPlatformObjects () {
        CopyOnWriteArrayList<HW03_ObjectPlatformStatic> aoObjectsPlatformStatic = new CopyOnWriteArrayList<HW03_ObjectPlatformStatic>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW03_ObjectPlatformStatic) {
                    aoObjectsPlatformStatic.add((HW03_ObjectPlatformStatic) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
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
     * ARGUMENTS:       oCollidingObjects -     The objects with which we have collided
     *                  nOriginalX -            X-axis location before the move that caused the collision
     *                  nOriginalY -            Y-axis location before the move that caused the collision
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void handleCollision (
        CopyOnWriteArrayList<HW03_ObjectCollidable> oCollidingObjects,
        float nOriginalX,
        float nOriginalY
    ) {
        
        // Static platform objects do nothing upon collision
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleEvent
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle event
     * 
     * ARGUMENTS:       oEventToHandle - An event for which we have registered interest
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    @Override
    public void handleEvent (HW03_ObjectEvent oEventToHandle) {
        try {
            
            // Declare variables
            ConcurrentHashMap<String, Object> oEventArguments;
            
            // Get event arguments
            oEventArguments = oEventToHandle.getEventArguments();
            
            // COLLISION
            if (
                oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.COLLISION && 
                ((HW03_ObjectCollidable) oEventArguments.get("oMovedObject")).getGUID() == this.getGUID()
            ) {
                this.handleCollision(
                    (CopyOnWriteArrayList<HW03_ObjectCollidable>) oEventArguments.get("aoCollidingObjects"), 
                    (float) oEventArguments.get("nOriginalX"),
                    (float) oEventArguments.get("nOriginalY")
                );
            }

        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

}
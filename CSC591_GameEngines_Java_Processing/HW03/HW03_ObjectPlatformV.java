/*************************************************************************************************************
 * FILE:            HW03_ObjectPlatformV.java
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
public class HW03_ObjectPlatformV extends HW03_ObjectPlatform implements HW03_EventObserver {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_MIN_SPEED_PX_PER_SEC =   20;
    private final static int N_MAX_SPEED_PX_PER_SEC =   80;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectPlatformV Constructor
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
    public HW03_ObjectPlatformV (
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
            HW03_Color.getRandomShade("Green"),
            // Provide applet
            oApplet,
            // Move vertically
            0,
            N_MIN_SPEED_PX_PER_SEC + new Random().nextInt(N_MAX_SPEED_PX_PER_SEC - N_MIN_SPEED_PX_PER_SEC)
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getVerticalPlatformObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsRenderable -    All renderable objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW03_ObjectPlatformV> getVerticalPlatformObjects () {
        CopyOnWriteArrayList<HW03_ObjectPlatformV> aoObjectsPlatformV = new CopyOnWriteArrayList<HW03_ObjectPlatformV>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW03_ObjectPlatformV) {
                    aoObjectsPlatformV.add((HW03_ObjectPlatformV) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return aoObjectsPlatformV;
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
        
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW03_ObjectCollidable> oObjectsToIgnore;
            HW03_ObjectCharacter oCarriedCharacter;
            int i;
            
            // Act only if it's "our fault"
            if (this.getPositionY() != nOriginalY) {

                /* Get character we are carrying
                 * There might be more than one, but we don't really care
                 */
                oCarriedCharacter = null;
                for (i = 0; i < oCollidingObjects.size(); i++) {
                    if (oCollidingObjects.get(i) instanceof HW03_ObjectCharacter) {
                        oCarriedCharacter = (HW03_ObjectCharacter) oCollidingObjects.get(i);
                        break;
                    }
                }
                
                // Figure out what objects we want to ignore in querying whether we're about to squish the character
                oObjectsToIgnore = new CopyOnWriteArrayList<HW03_ObjectCollidable>();
                oObjectsToIgnore.add(this);
                if (oCarriedCharacter != null) {
                    oObjectsToIgnore.add(oCarriedCharacter);
                }

                // Should we back off?
                if (
                    // We are not carrying a character object OR
                    oCarriedCharacter == null || 
                    // We are not carrying the character up OR
                    this.getSpeedVertical() <= 0 ||
                    // We are carrying the character object up but if we keep going we'll squish 'em
                    HW03_ObjectCollidable.isSpaceOccupied(
                        /* Objects to ignore
                         * Part of this platform might already be in the space - don't care
                         * Character might already be in the space - don't care
                         * Something else collidable in the space - that's what we care about, that's what'll squish the character
                         */
                        oObjectsToIgnore,
                        // X Min
                        this.getPositionX(), 
                        // X Max
                        (this.getPositionX() + this.getWidth()), 
                        // Y Min (look ahead up to where you and the character will end up - this is the top of that space)
                        (this.getFuturePositionY() - oCarriedCharacter.getHeight()), 
                        // Y Max (look ahead up to where you and the character will end up - this is the bottom of that space)
                        (this.getFuturePositionY() + this.getHeight() )
                    ) == true
                ) {
                    this.backOutOfCollision(nOriginalX, nOriginalY);
                    this.reverseCourse();
                }
                
            }
        
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
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
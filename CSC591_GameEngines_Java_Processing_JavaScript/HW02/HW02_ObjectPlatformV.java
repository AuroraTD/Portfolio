package HW02;
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
public class HW02_ObjectPlatformV extends HW02_ObjectPlatform {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private final static int N_MIN_SPEED_PX =   1;
    private final static int N_MAX_SPEED_PX =   4;
    
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
    public HW02_ObjectPlatformV (
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
            HW02_Color.getRandomShade("Green"),
            // Provide applet
            oApplet,
            // Move vertically
            0,
            N_MIN_SPEED_PX + new Random().nextInt(N_MAX_SPEED_PX - N_MIN_SPEED_PX)
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
    public static CopyOnWriteArrayList<HW02_ObjectPlatformV> getVerticalPlatformObjects () {
        CopyOnWriteArrayList<HW02_ObjectPlatformV> aoObjectsPlatformV = new CopyOnWriteArrayList<HW02_ObjectPlatformV>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW02_ObjectGame> oObjectsGame = HW02_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW02_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW02_ObjectPlatformV) {
                    aoObjectsPlatformV.add((HW02_ObjectPlatformV) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            
            // Declare variables
            CopyOnWriteArrayList<HW02_ObjectCollidable> oObjectsToIgnore;
            HW02_ObjectCharacter oCarriedCharacter;
            int i;
            
            /* Get character we are carrying
             * There might be more than one, but we don't really care
             */
            oCarriedCharacter = null;
            for (i = 0; i < oCollidingObjects.size(); i++) {
                if (oCollidingObjects.get(i) instanceof HW02_ObjectCharacter) {
                    oCarriedCharacter = (HW02_ObjectCharacter) oCollidingObjects.get(i);
                    break;
                }
            }
            
            // Figure out what objects we want to ignore in querying whether we're about to squish the character
            oObjectsToIgnore = new CopyOnWriteArrayList<HW02_ObjectCollidable>();
            oObjectsToIgnore.add(this);
            if (oCarriedCharacter != null) {
                oObjectsToIgnore.add(oCarriedCharacter);
            }

            // Should we back off?
            if (
                // Collision is due to our vertical motion AND
                yAxis == true && 
                (
                    // We are not carrying a character object OR
                    oCarriedCharacter == null || 
                    // We are not carrying the character up OR
                    this.getSpeedVertical() <= 0 ||
                    // We are carrying the character object up but if we keep going we'll squish 'em
                    HW02_ObjectCollidable.isSpaceOccupied(
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
                        (this.getPositionY() - this.getSpeedVertical() - oCarriedCharacter.getHeight()), 
                        // Y Max (look ahead up to where you and the character will end up - this is the bottom of that space)
                        (this.getPositionY() + this.getHeight() - this.getSpeedVertical())
                    ) == true
                )
            ) {
                this.backOutOfCollisionY();
                this.reverseCourse();
            }
        
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        
    }

}
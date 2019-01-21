/*************************************************************************************************************
 * FILE:            HW04_ObjectBullet.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents an bullet object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW04_ObjectBullet extends HW04_ObjectMoveable implements HW04_EventObserver {
    
    // Required for serializable class
    private static final long serialVersionUID =                1L;
    
    // Constants
    private final static HW04_EventManager O_EVENT_MANAGER =    HW04_EventManager.getInstance();
    private final static int N_WIDTH_PX =                       3;
    private final static int N_HEIGHT_PX =                      20;
    private final static int N_RADIUS_PX =                      3;
    private final static int N_SPEED_PX_PER_SEC =               200;
    
    // Variables
    private HW04_ObjectGame oBulletShotBy =                     null;

    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectBullet Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new bullet object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     *                  oShooter -      The game object that is shooting the bullet
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectBullet (
        int nExistingGUID,
        int nX_px,
        int nY_px,
        PApplet oApplet,
        HW04_ObjectRenderable oShooter
    ) {
        
        // Construct moveable object
        super(
            // GUID
            nExistingGUID,
            // X
            nX_px, 
            // Y
            nY_px, 
            // Width
            N_WIDTH_PX, 
            // Height
            N_HEIGHT_PX, 
            // Radius
            N_RADIUS_PX, 
            // Color
            oShooter.getColor(),
            // Applet
            oApplet,
            // Default horizontal speed (pixels per second)
            0,
            // Default vertical speed (pixels per second)
            (oShooter instanceof HW04_ObjectCharacter ? N_SPEED_PX_PER_SEC : N_SPEED_PX_PER_SEC * -1)
        );
        
        // Remember who shot this bullet
        this.oBulletShotBy = oShooter;
        
        // Register interest in events that affect the object
        O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.COLLISION, this, false);
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getBulletObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsBullet -    All bullet objects in the game
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW04_ObjectBullet> getBulletObjects () {
        CopyOnWriteArrayList<HW04_ObjectBullet> aoObjectsBullet = new CopyOnWriteArrayList<HW04_ObjectBullet>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW04_ObjectGame> oObjectsGame = HW04_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW04_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW04_ObjectBullet) {
                    aoObjectsBullet.add((HW04_ObjectBullet) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        return aoObjectsBullet;
    }

    /*********************************************************************************************************
     * FUNCTION:        moveBullets
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Update all connected clients about what's going on with bullets,
     *                  and then move them around.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void moveBullets () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectBullet> aoBullets = null;
            int i;
            
            // Get object collections
            aoBullets = HW04_ObjectBullet.getBulletObjects();
            
            if (aoBullets != null) {
                
                /* Send updates to clients then move enemy
                 *  Send updates before moving objects for which the server is responsible
                 *  Why? Moving objects may cause collision events which need to be handled
                 *  It's better not to send collided / funky objects out
                 */
                for (i = 0; i < aoBullets.size(); i++) {
                    HW04_Server.notifyClientsAboutOneObject(-1, aoBullets.get(i));
                    aoBullets.get(i).updateLocationAndCheckForCollisions();
                }
                
            }
            
            // Free
            aoBullets = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        shootBullet
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Shoot a bullet from the given object
     *
     * ARGUMENTS:       oShooter -  The game object that is shooting the bullet
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void shootBullet (HW04_ObjectMoveable oShooter) {
        try {
            
            // Declare constants
            final int N_SCOOCH_PX = 5;
            
            // Declare variables
            HW04_ObjectBullet oNewBullet;
            int nX_px;
            int nY_px;
            boolean bShootDown;
            
            // Calculate values
            bShootDown =    oShooter instanceof HW04_ObjectCharacter ? false : true;
            nX_px =         (int) oShooter.getPositionX() + oShooter.getWidth() / 2;
            nY_px =         (int) oShooter.getPositionY();
            if (bShootDown == true) {
                nY_px += oShooter.getHeight() + N_SCOOCH_PX;
            }
            else {
                nY_px -= (HW04_ObjectBullet.getStaticHeight() + N_SCOOCH_PX);
            }
            
            // Create a bullet shooting from the given object
            oNewBullet = new HW04_ObjectBullet(
                // GUID
                -1,
                // X
                nX_px,
                // Y
                nY_px,
                // Applet
                null,
                // Shooting object
                oShooter
            );
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getBulletShotBy
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oBulletShotBy
     *********************************************************************************************************/
    public HW04_ObjectGame getBulletShotBy () {
        return this.oBulletShotBy;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticRadius
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the bullet object static radius
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         N_RADIUS_PX
     *********************************************************************************************************/
    public static int getStaticRadius () {
        return N_RADIUS_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticHeight
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the bullet object static height
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         N_RADIUS_PX
     *********************************************************************************************************/
    public static int getStaticHeight () {
        return N_HEIGHT_PX;
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
        try {
            
            // Declare variables
            HW04_ObjectCollidable oCollidedObject;
            HW04_ObjectBullet oCollidedBullet;
            HW04_ObjectCollidable oObjectThatGotShot;
            HW04_ObjectGame oShooterLatestCopy;
            HW04_ObjectGame oOtherShooterLatestCopy;
            int i;
            
            // Initialize
            oShooterLatestCopy = null;
            oOtherShooterLatestCopy = null;
            
            /* Get a reference to the object that got shot
             * If a bullet collided with multiple objects, we only care about one of them
             */
            oObjectThatGotShot = oCollidingObjects.get(0);
            
            /* Shot the player (they die)
             * You can't shoot yourself (even if you move into a bullet)
             */
            if (
                oObjectThatGotShot instanceof HW04_ObjectCharacter &&
                oObjectThatGotShot.getGUID() != this.getBulletShotBy().getGUID()
            ) {
                O_EVENT_MANAGER.raiseEventGameEnd(false);
            }
            
            /* Shot an enemy (they die)
             * You can't shoot yourself (even if you move into a bullet)
             */
            else if (
                oObjectThatGotShot instanceof HW04_ObjectEnemy &&
                oObjectThatGotShot.getGUID() != this.getBulletShotBy().getGUID()
            ) {
                ((HW04_ObjectEnemy) oObjectThatGotShot).destroyEnemy();
            }
            
            // Shot another bullet (they both are destroyed)
            else if (oObjectThatGotShot instanceof HW04_ObjectBullet) {
                HW04_Server.removeObjectFromGame(oObjectThatGotShot, -1);
                oOtherShooterLatestCopy = 
                    HW04_ObjectGame.getObjectByGUID(((HW04_ObjectBullet) oObjectThatGotShot).getBulletShotBy().getGUID());
            }
            
            // No matter what the bullet collided with, the bullet goes away
            HW04_Server.removeObjectFromGame(this, -1);
            
            /* Once the character fires, it must wait until the bullet hits something before firing again
             *  It might hit an enemy, or the top of the window
             *  Why do we get character object by GUID instead of acting on it directly?
             *      Because, to eliminate "intelligent" code,
             *      both the server and the client replace game objects
             *      when they get updates on objects from network partners
             *      So we want the latest game object to act on the event,
             *      not some "stale" object
             */
            oShooterLatestCopy = HW04_ObjectGame.getObjectByGUID(this.getBulletShotBy().getGUID());
            if (
                oShooterLatestCopy != null &&
                oShooterLatestCopy instanceof HW04_ObjectCharacter
            ) {
                ((HW04_ObjectCharacter) oShooterLatestCopy).setShootingAllowed(true);
            }
            if (
                oOtherShooterLatestCopy != null &&
                oOtherShooterLatestCopy instanceof HW04_ObjectCharacter
            ) {
                ((HW04_ObjectCharacter) oOtherShooterLatestCopy).setShootingAllowed(true);
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
    public void handleEvent (HW04_ObjectEvent oEventToHandle) {
        try {
            
            // Declare variables
            ConcurrentHashMap<String, Object> oEventArguments;
            
            // Get event arguments
            oEventArguments = oEventToHandle.getEventArguments();
            
            // COLLISION
            if (
                oEventToHandle.getEventType() == HW04_ObjectEvent.EventType.COLLISION && 
                ((HW04_ObjectCollidable) oEventArguments.get("oMovedObject")).getGUID() == this.getGUID()
            ) {
                this.handleCollision(
                    (CopyOnWriteArrayList<HW04_ObjectCollidable>) oEventArguments.get("aoCollidingObjects"), 
                    (float) oEventArguments.get("nOriginalX"),
                    (float) oEventArguments.get("nOriginalY")
                );
            }

        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
}
/*************************************************************************************************************
 * FILE:            HW04_ObjectEnemy.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents an enemy object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW04_ObjectEnemy extends HW04_ObjectMoveable implements HW04_EventObserver {
    
    // Required for serializable class
    private static final long serialVersionUID =                1L;
    
    // Constants
    private final static HW04_EventManager O_EVENT_MANAGER =    HW04_EventManager.getInstance();
    private final static HW04_Time_Real O_TIME_MANAGER =        HW04_Time_Real.getInstance();
    private final static Random O_RANDOMIZER =                  new Random();
    private final static int N_RADIUS_PX =                      20;
    private final static int N_SPEED_PX_PER_SEC =               30;
    private final static int N_TIME_BETWEEN_SHOTS_MIN_MS =      3000;
    private final static int N_TIME_BETWEEN_SHOTS_MAX_MS =      5000;
    
    // Variables (static)
    private static int bLastWallCollisionWasRight =             -1;
    private static boolean bFrontLineExists =                   false;
    private static boolean bTargetExists =                      false;
    
    // Variables (per object)
    private double nTimeOfLastShot_ms =                         -1;
    private int nTimeBetweenShots_ms =                          -1;
    private boolean bFrontLineEnemy =                            false;

    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectEnemy Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new enemy object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectEnemy (
        int nExistingGUID,
        int nX_px,
        int nY_px,
        PApplet oApplet
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
            N_RADIUS_PX, 
            // Height
            N_RADIUS_PX, 
            // Radius
            N_RADIUS_PX, 
            // Color
            HW04_Color.getPureColor("Red"),
            // Applet
            oApplet,
            // Default horizontal speed (pixels per second)
            N_SPEED_PX_PER_SEC,
            // Default vertical speed (pixels per second)
            0
        );
        
        // Start "timer" for next shot
        this.setTimeOfLastShot();
        this.setTimeBetweenShots();
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getEnemyObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsEnemy -    All enemy objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW04_ObjectEnemy> getEnemyObjects () {
        CopyOnWriteArrayList<HW04_ObjectEnemy> aoObjectsEnemy = new CopyOnWriteArrayList<HW04_ObjectEnemy>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW04_ObjectGame> oObjectsGame = HW04_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW04_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW04_ObjectEnemy) {
                    aoObjectsEnemy.add((HW04_ObjectEnemy) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        return aoObjectsEnemy;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addNewRowOfEnemiesAtTopOfWindow
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add enemy objects to the game world
     *                  Place these enemies in two layers at the top of the window
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addNewRowOfEnemiesAtTopOfWindow () {
        try {
            
            // Declare constants
            final int N_NUM_ENEMIES_ONE_ROW = HW04_Utility.getWindowSize() / HW04_ObjectEnemy.getStaticRadius() / 4;
            
            // Declare variables
            HW04_ObjectEnemy oThisEnemy;
            HW04_ObjectEnemy oNewEnemy;
            int i;
            
            // Move all existing enemies down one row
            moveAllEnemiesDown(false);
            
            // Add one row of enemies starting at the top of the window
            for (i = 0; i < N_NUM_ENEMIES_ONE_ROW; i++) {
                
                // Create enemy
                oNewEnemy = new HW04_ObjectEnemy(
                    // Get auto GUID
                    -1,
                    // X
                    HW04_ObjectEnemy.getStaticRadius() * 2 * i,
                    // Y
                    HW04_ObjectStatus.getTotalHeight(),
                    // Applet
                    null
                );
                
                // Register interest in events that affect the object
                O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.COLLISION, oNewEnemy, false);
                
                // Set front line flag
                if (bFrontLineExists == false) {
                    oNewEnemy.setFrontLineEnemy(true);
                }
                
                // Tell clients about the new enemy
                HW04_Server.notifyClientsAboutOneObject(-1, oNewEnemy);
            }
            
            // Remember whether we have ever created a front line
            if (bFrontLineExists == false) {
                bFrontLineExists = true;
            }

        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        moveAllEnemiesDown
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Move all enemies down towards the player and 
     *                  (optionally) reverse their horizontal direction
     *
     * ARGUMENTS:       bReverseDirection - True if after moving down, 
     *                                      the enemies should reverse their horizontal direction
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void moveAllEnemiesDown (boolean bReverseDirection) {
        try {
            
            // Declare constants
            final int N_SCOOCH_PX = 5;
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectEnemy> aoObjectsEnemy = null;
            HW04_ObjectEnemy oThisEnemy;
            boolean bMovingRight;
            int i;
            
            // Get object collections
            aoObjectsEnemy = HW04_ObjectEnemy.getEnemyObjects();
            
            // Move all existing enemies down one row
            for (i = 0; i < aoObjectsEnemy.size(); i++) {
                
                // Get enemy
                oThisEnemy = aoObjectsEnemy.get(i);
                
                // Move down and reverse horizontal direction
                if (bReverseDirection == true) {
                    bMovingRight = oThisEnemy.getSpeedHorizontal() > 0;
                    oThisEnemy.setPositionX(oThisEnemy.getPositionX() + (bMovingRight ? (N_SCOOCH_PX * -1) : N_SCOOCH_PX));
                    oThisEnemy.setSpeedHorizontal(oThisEnemy.getSpeedHorizontal() * -1);
                }
                oThisEnemy.setPositionY(oThisEnemy.getPositionY() + oThisEnemy.getHeight());
                
                // Tell clients about the new position and direction
                HW04_Server.notifyClientsAboutOneObject(-1, oThisEnemy);
                
                /* Check to make sure it didn't move down to the death zone
                 * Collision checking is expensive so minimize it
                 */
                if (oThisEnemy.getPositionY() > (HW04_Utility.getWindowSize() * 0.9)) {
                    oThisEnemy.checkForCollisionsAndRaiseEvent();
                }
                
            }
            
            // Free
            aoObjectsEnemy = null;

        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }

    /*********************************************************************************************************
     * FUNCTION:        moveEnemies
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Update all connected clients about what's going on with enemies,
     *                  and then move them around.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void moveEnemies () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectEnemy> aoEnemies = null;
            HW04_ObjectEnemy oThisEnemy;
            int i;
            
            // Get object collections
            aoEnemies = HW04_ObjectEnemy.getEnemyObjects();
            
            if (aoEnemies != null) {
                
                /* Send updates to clients then move enemy
                 *  Send updates before moving objects for which the server is responsible
                 *  Why? Moving objects may cause collision events which need to be handled
                 *  It's better not to send collided / funky objects out
                 */
                for (i = 0; i < aoEnemies.size(); i++) {
                    
                    // Send update to clients
                    HW04_Server.notifyClientsAboutOneObject(-1, aoEnemies.get(i));
                    
                    /* Move enemy
                     * Collision checking is expensive so minimize it
                     */
                    oThisEnemy = aoEnemies.get(i);
                    if (
                        oThisEnemy.getPositionX() < (HW04_Utility.getWindowSize() * 0.1) ||
                        oThisEnemy.getPositionX() > (HW04_Utility.getWindowSize() * 0.9) ||
                        oThisEnemy.getPositionY() > (HW04_Utility.getWindowSize() * 0.9)
                    ) {
                        oThisEnemy.updateLocationAndCheckForCollisions();
                    }
                    else {
                        oThisEnemy.updateLocation();
                    }
                    
                    /* Don't start counting down to the first shot until there is something to shoot at
                     * (not fair for the player to awaken amid a hail of bullets)
                     */
                    if (bTargetExists == false) {
                        oThisEnemy.nTimeOfLastShot_ms = O_TIME_MANAGER.getTime();
                    }
                    
                    /* Take a shot if it's time to do so
                     *  Only if you're on the front line   
                     */
                    else if (
                        oThisEnemy.getFrontLineEnemy() == true && 
                        (O_TIME_MANAGER.getTime() - oThisEnemy.getTimeOfLastShot()) > oThisEnemy.getTimeBetweenShots()
                    ) {
                        
                        // Take a shot
                        HW04_ObjectBullet.shootBullet(oThisEnemy);
                        
                        // Reset timer for next shot
                        oThisEnemy.setTimeOfLastShot();
                        oThisEnemy.setTimeBetweenShots();
                        
                    }
                    
                }
                
            }
            
            // Update knowledge of whether there is something to shoot at
            if (bTargetExists == false) {
                bTargetExists = HW04_ObjectCharacter.getCharacterObjects().size() > 0;
            }
            
            // Free
            aoEnemies = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }

    /*********************************************************************************************************
     * FUNCTION:        getStaticRadius
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the enemy object static radius
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         N_RADIUS_PX
     *********************************************************************************************************/
    public static int getStaticRadius () {
        return N_RADIUS_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getTimeOfLastShot
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTimeOfLastShot_ms
     *********************************************************************************************************/
    public double getTimeOfLastShot () {
        return this.nTimeOfLastShot_ms;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setTimeOfLastShot
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setTimeOfLastShot () {
        this.nTimeOfLastShot_ms = O_TIME_MANAGER.getTime();
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getTimeBetweenShots
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTimeBetweenShots_ms
     *********************************************************************************************************/
    public int getTimeBetweenShots () {
        return this.nTimeBetweenShots_ms;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setTimeBetweenShots
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setTimeBetweenShots () {
        this.nTimeBetweenShots_ms =
            O_RANDOMIZER.nextInt(N_TIME_BETWEEN_SHOTS_MAX_MS - N_TIME_BETWEEN_SHOTS_MIN_MS) + 
            N_TIME_BETWEEN_SHOTS_MIN_MS;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getFrontLineEnemy
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bFrontLineEnemy
     *********************************************************************************************************/
    public boolean getFrontLineEnemy () {
        return this.bFrontLineEnemy;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setFrontLineEnemy
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       bFrontLine -    True if this enemy is on the front line,
     *                                  False otherwise
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setFrontLineEnemy (boolean bFrontLine) {
        this.bFrontLineEnemy = bFrontLine;
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
            HW04_ObjectEnemy oCollidedEnemy;
            boolean bLostGame;
            int bThisWallCollisionWasRight;
            int i;
            
            // Initialize
            bLostGame = false;
            bThisWallCollisionWasRight = -1;
            
            // Check for LOSE condition (enemies have invaded!)
            for (i = 0; i < oCollidingObjects.size(); i++) {
                if (
                    oCollidingObjects.get(i) instanceof HW04_ObjectZoneDeath ||
                    oCollidingObjects.get(i) instanceof HW04_ObjectCharacter
                ) {
                    bLostGame = true;
                    O_EVENT_MANAGER.raiseEventGameEnd(false);
                    break;
                }
            }
            
            // Check for collision with side wall (move all enemies down and reverse horizontal direction)
            if (bLostGame == false) {
                oCollidingObjects = this.getCollidingObjects();
                for (i = 0; i < oCollidingObjects.size(); i++) {
                    oCollidedObject = oCollidingObjects.get(i);
                    if (
                        oCollidedObject instanceof HW04_ObjectBoundary &&
                        oCollidedObject.getWidth() == 1
                    ) {
                        /* Do not handle burst of collisions of many rows of enemies hitting the wall at the same time
                         * Treat enemies as a group
                         */
                        bThisWallCollisionWasRight = this.getSpeedHorizontal() > 0 ? 1 : 0;
                        if (bLastWallCollisionWasRight == -1 || bThisWallCollisionWasRight != bLastWallCollisionWasRight) {
                            moveAllEnemiesDown(true);
                            bLastWallCollisionWasRight = bThisWallCollisionWasRight;
                            break;
                        }
                    }
                }
            }
            
            // We do not check for collision with bullet here - rather, the bullet checks for collisions
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        destroyEnemy
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Destroy an enemy (remove it from the game)
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void destroyEnemy () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectEnemy> aoEnemies;
            HW04_ObjectEnemy oPossibleEnemyToBePromoted;
            HW04_ObjectEnemy oEnemyToBePromoted;
            int i;
            
            // Every destroyed enemy gets you a point
            O_EVENT_MANAGER.raiseEventScoreChange(null, true);
            
            // If there's another enemy behind you, he is promoted to the "front line" (can shoot)
            oEnemyToBePromoted = null;
            aoEnemies = getEnemyObjects();
            for (i = 0; i < aoEnemies.size(); i++) {
                oPossibleEnemyToBePromoted = aoEnemies.get(i);
                if (
                    oPossibleEnemyToBePromoted.getGUID() != this.getGUID() &&
                    oPossibleEnemyToBePromoted.getPositionX() > this.getPositionX() - this.getWidth() &&
                    oPossibleEnemyToBePromoted.getPositionX() < this.getPositionX() + this.getWidth() &&
                    (
                        oEnemyToBePromoted == null ||
                        oPossibleEnemyToBePromoted.getPositionY() > oEnemyToBePromoted.getPositionY()
                    )
                ) {
                    oEnemyToBePromoted = oPossibleEnemyToBePromoted;
                }
            }
            if (oEnemyToBePromoted != null) {
                oEnemyToBePromoted.setFrontLineEnemy(true);
            }
            
            // Destroy enemy
            HW04_Server.removeObjectFromGame(this, -1);
            
            // If you have defeated all enemies, YOU WIN!
            if(HW04_ObjectEnemy.getEnemyObjects().size() <= 0) {
                O_EVENT_MANAGER.raiseEventGameEnd(true);
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
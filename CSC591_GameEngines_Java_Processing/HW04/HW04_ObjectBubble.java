/*************************************************************************************************************
 * FILE:            HW04_ObjectBubble.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a bubble object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW04_ObjectBubble extends HW04_ObjectMoveable implements HW04_EventObserver {
    
    // Required for serializable class
    private static final long serialVersionUID =                1L;
    
    // Object properties (static)
    private final static HW04_EventManager O_EVENT_MANAGER =    HW04_EventManager.getInstance();
    private final static int N_RADIUS_PX =                      20;
    private static HW04_ObjectBubble oLatestShooterBubble;
    
    // Object properties (variable)
    private boolean bShooterBubble;
    private boolean bPopPending;

    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectBubble Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new bubble object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     *                  bShooter -      True if the bubble is a shootER, false if shootEE
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectBubble (
        int nExistingGUID,
        int nX_px,
        int nY_px,
        PApplet oApplet,
        boolean bShooter
    ) {
        
        // Construct moveable object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            N_RADIUS_PX, 
            N_RADIUS_PX, 
            N_RADIUS_PX, 
            HW04_Color.getRandomColor(),
            oApplet,
            0,
            0
        );
        
        // Set flags
        this.bShooterBubble = bShooter;
        this.bPopPending = false;
        
        // Remember the latest shooter bubble
        if (bShooter == true) {
            oLatestShooterBubble = this;
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getBubbleObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsBubble -    All bubble objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW04_ObjectBubble> getBubbleObjects () {
        CopyOnWriteArrayList<HW04_ObjectBubble> aoObjectsBubble = new CopyOnWriteArrayList<HW04_ObjectBubble>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW04_ObjectGame> oObjectsGame = HW04_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW04_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW04_ObjectBubble) {
                    aoObjectsBubble.add((HW04_ObjectBubble) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        return aoObjectsBubble;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addNewRowOfBubblesAtTopOfWindow
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add bubble objects to the game world
     *                  Place these bubbles in two layers at the top of the window
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addNewRowOfBubblesAtTopOfWindow () {
        try {
            
            // Declare constants
            final int N_NUM_BUBBLES_ONE_ROW = HW04_Utility.getWindowSize() / HW04_ObjectBubble.getStaticRadius();
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectBubble> aoObjectsBubble = null;
            HW04_ObjectBubble oThisBubble;
            HW04_ObjectBubble oNewBubble;
            int i;
            
            // Get object collections
            aoObjectsBubble = HW04_ObjectBubble.getBubbleObjects();
            
            // Move all existing shootee bubbles down one row
            for (i = 0; i < aoObjectsBubble.size(); i++) {
                
                // Get bubble
                oThisBubble = aoObjectsBubble.get(i);
                
                // Shootee?
                if (oThisBubble.getShooterFlag() == false) {
                    
                    // Move down
                    oThisBubble.setPositionY(oThisBubble.getPositionY() + oThisBubble.getHeight());
                    
                    // Tell clients about the new position
                    HW04_Server.notifyClientsAboutOneObject(-1, oThisBubble);
                    
                    /* Check to make sure it didn't move down to the death zone
                     * Collision checking is expensive so minimize it
                     */
                    if (oThisBubble.getPositionY() > (HW04_Utility.getWindowSize() * 0.9)) {
                        oThisBubble.checkForCollisionsAndRaiseEvent();
                    }
                    
                }
            }
            
            // Add one row of bubbles starting at the top of the window
            for (i = 0; i < N_NUM_BUBBLES_ONE_ROW; i++) {
                
                // Create bubble
                oNewBubble = new HW04_ObjectBubble(
                    // Get auto GUID
                    -1,
                    // X
                    HW04_ObjectBubble.getStaticRadius() * i,
                    // Y
                    HW04_ObjectStatus.getTotalHeight(),
                    // Applet
                    null,
                    // Shooter (as opposed to shootee) bubble
                    false
                );
                
                // Register interest in events that affect the object
                O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.COLLISION, oNewBubble, false);
                
                // Tell clients about the new bubble
                HW04_Server.notifyClientsAboutOneObject(-1, oNewBubble);
            }
            
            // Free
            aoObjectsBubble = null;

        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }

    /*********************************************************************************************************
     * FUNCTION:        moveShooterBubbles
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Update all connected clients about what's going on with shooter bubbles,
     *                  and then move them around.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void moveShooterBubbles () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectBubble> aoBubbles = null;
            int i;
            
            // Get object collections
            aoBubbles = HW04_ObjectBubble.getBubbleObjects();
            
            if (aoBubbles != null) {
                
                /* Send updates to clients then move bubble
                 *  Send updates before moving objects for which the server is responsible
                 *  Why? Moving objects may cause collision events which need to be handled
                 *  It's better not to send collided / funky objects out
                 */
                for (i = 0; i < aoBubbles.size(); i++) {
                    if (aoBubbles.get(i).getShooterFlag() == true) {
                        HW04_Server.notifyClientsAboutOneObject(-1, aoBubbles.get(i));
                        aoBubbles.get(i).updateLocationAndCheckForCollisions();
                    }
                }
                
            }
            
            // Free
            aoBubbles = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
   
    /*********************************************************************************************************
     * FUNCTION:        shootBubble
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Shoot latest shooter bubble off at some angle
     *                  Initial game bubbles are created by the Server
     *                  This method however is expected to be called by the Client
     *
     * ARGUMENTS:       nAngle_deg -    Angle at which to shoot the bubble
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void shootBubble (float nAngle_deg) {
        try {
            
            // Declare constants
            final int N_ANGLE_STRAIGHT_UP_DEG = 0;
            final int N_SHOOT_SPEED_PX_PER_SEC = 250;
            
            // Declare variables
            HW04_ObjectBubble oLatestShooter;
            float nCalculationAngle_deg;
            int nSpeedHorizontal_px = 0;
            int nSpeedVertical_px = 0;
            
            // Create a new shooter bubble
            oLatestShooter = HW04_ObjectBubble.getLatestShooterBubble();
            
            /* Set horizontal and vertical speeds based on angle
             *  Think of a triangle
             *  - The angle is the difference between 
             *      the absolute value and 90 degrees (straight up)
             *  - The speed is the hypotenuse
             *  - The vertical component of the speed is the adjacent side
             *  - The horizontal component of the speed is the opposite side
             */
            if (nAngle_deg == N_ANGLE_STRAIGHT_UP_DEG) {
                nSpeedHorizontal_px = 0;
                nSpeedVertical_px = N_SHOOT_SPEED_PX_PER_SEC;
            }
            else if (nAngle_deg > N_ANGLE_STRAIGHT_UP_DEG) {
                nCalculationAngle_deg = nAngle_deg - N_ANGLE_STRAIGHT_UP_DEG;
                nSpeedHorizontal_px = (int) (
                    N_SHOOT_SPEED_PX_PER_SEC * 
                    PApplet.sin(PApplet.radians(nCalculationAngle_deg))
                );
                nSpeedVertical_px = (int) (
                    N_SHOOT_SPEED_PX_PER_SEC * 
                    PApplet.cos(PApplet.radians(nCalculationAngle_deg))
                );
            }
            else if (nAngle_deg < N_ANGLE_STRAIGHT_UP_DEG) {
                nCalculationAngle_deg = N_ANGLE_STRAIGHT_UP_DEG - nAngle_deg;
                nSpeedHorizontal_px = (int) (
                    -1 * 
                    N_SHOOT_SPEED_PX_PER_SEC * 
                    PApplet.sin(PApplet.radians(nCalculationAngle_deg))
                );
                nSpeedVertical_px = (int) (
                    N_SHOOT_SPEED_PX_PER_SEC * 
                    PApplet.cos(PApplet.radians(nCalculationAngle_deg))
                );
            }
            oLatestShooter.setSpeedHorizontal(nSpeedHorizontal_px);
            oLatestShooter.setSpeedVertical(nSpeedVertical_px);
            
            // Create another shooter bubble for the next shot
            addShooterBubbleAtBottomOfWindow();
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addShooterBubbleAtBottomOfWindow
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Create a new shooter bubble
     *                  Initial game bubbles are created by the Server
     *                  This method however is expected to be called by the Client
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addShooterBubbleAtBottomOfWindow () {
        try {
            
            // Declare variables
            HW04_ObjectBubble oNewBubble;
            
            // Create a new shooter bubble
            oNewBubble = new HW04_ObjectBubble(
                // Get auto GUID
                -1,
                // X
                (int) (HW04_Utility.getWindowSize() / 2 - HW04_ObjectBubble.getStaticRadius() / 2),
                // Y
                (int)(HW04_Utility.getWindowSize() - HW04_ObjectBubble.getStaticRadius()),
                // Applet
                null,
                // Shooter (as opposed to shootee) bubble
                true
            );
            
            // Register interest in events that affect the object
            O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.COLLISION, oNewBubble, false);
            
            // Tell the clients about this new game object
            HW04_Server.notifyClientsAboutOneObject(-1, oNewBubble);
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getLatestShooterBubble
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bShooterBubble
     *********************************************************************************************************/
    public static HW04_ObjectBubble getLatestShooterBubble () {
        return oLatestShooterBubble;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getShooterFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bShooterBubble
     *********************************************************************************************************/
    public boolean getShooterFlag () {
        return this.bShooterBubble;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setShooterFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       bShooter -  True if this is a shootER bubble, false if shootEE bubble
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setShooterFlag (boolean bShooter) {
        this.bShooterBubble = bShooter;
    }

    /*********************************************************************************************************
     * FUNCTION:        getPopPendingFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bPopPending
     *********************************************************************************************************/
    public boolean getPopPendingFlag () {
        return this.bPopPending;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setPopPendingFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       bPending -  True if the bubble has a "pop" pending but can't be popped quite yet
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setPopPendingFlag (boolean bPending) {
        this.bPopPending = bPending;
    }

    /*********************************************************************************************************
     * FUNCTION:        getStaticRadius
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the bubble object static radius
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         N_RADIUS_PX
     *********************************************************************************************************/
    public static int getStaticRadius () {
        return N_RADIUS_PX;
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
            HW04_ObjectBubble oCollidedBubble;
            boolean bLostGame;
            boolean bShooterPoppedBubbles;
            boolean bShooterGotStuck;
            boolean bShooterRicocheted;
            int i;
            
            // Initialize
            bLostGame =             false;
            bShooterPoppedBubbles = false;
            bShooterGotStuck =      false;
            bShooterRicocheted =    false;
            
            // Check for LOSE condition
            if (this.getShooterFlag() == false) {
                for (i = 0; i < oCollidingObjects.size(); i++) {
                    if (oCollidingObjects.get(i) instanceof HW04_ObjectZoneDeath) {
                        bLostGame = true;
                        O_EVENT_MANAGER.raiseEventGameEnd(false);
                        break;
                    }
                }
            }
            
            // Check for any other interesting conditions
            if (bLostGame == false) {
                
                // Shooter bubble?
                if (this.getShooterFlag() == true) {
                    
                    // Check everything it's collided with
                    for (i = 0; i < oCollidingObjects.size(); i++) {
                        
                        // Get collided object
                        oCollidedObject = oCollidingObjects.get(i);
                        
                        // If the shooter bubble has collided with a shootee bubble of the same color
                        if (oCollidedObject instanceof HW04_ObjectBubble) {
                            oCollidedBubble = (HW04_ObjectBubble) oCollidedObject;
                            if (
                                oCollidedBubble.getShooterFlag() == false && 
                                oCollidedBubble.getColor().R == this.getColor().R && 
                                oCollidedBubble.getColor().G == this.getColor().G && 
                                oCollidedBubble.getColor().B == this.getColor().B 
                            ) {
                                
                                // Turn the collided bubble into a shooter bubble
                                oCollidedBubble.setShooterFlag(true);
                                
                                /* Check the collided bubble itself for collisions
                                 * This will kick off a chain of events which will pop all connected bubbles of the same color
                                 */
                                oCollidedBubble.checkForCollisionsAndRaiseEvent();
                                
                                /* Need to pop the collided bubble
                                 * but can't do it quite yet
                                 * it might need to handle an event!
                                 */
                                oCollidedBubble.setPopPendingFlag(true);
                                
                                // Remember what happened
                                bShooterPoppedBubbles = true;
                                
                            }
                        }

                    }
                    
                    // Pop the shooter bubble if it's done it's job
                    if (bShooterPoppedBubbles == true || this.getPopPendingFlag() == true) {
                        popBubble(this);
                    }
                    
                    // Shooter becomes shootee if it collides with a bubble of a different color or with the top of the window
                    if (bShooterPoppedBubbles == false) {
                        oCollidingObjects = this.getCollidingObjects();
                        for (i = 0; i < oCollidingObjects.size(); i++) {
                            oCollidedObject = oCollidingObjects.get(i);
                            if (
                                (
                                    oCollidedObject instanceof HW04_ObjectBubble &&
                                    ((HW04_ObjectBubble) oCollidedObject).getShooterFlag() == false
                                ) ||
                                oCollidedObject instanceof HW04_ObjectStatus
                            ) {
                                this.setShooterFlag(false);
                                this.setSpeedHorizontal(0);
                                this.setSpeedVertical(0);
                                bShooterGotStuck = true;
                                break;
                            }
                        }
                    }

                    // Shooter ricochets if it collides with a side wall
                    if (bShooterPoppedBubbles == false && bShooterGotStuck == false) {
                        oCollidingObjects = this.getCollidingObjects();
                        for (i = 0; i < oCollidingObjects.size(); i++) {
                            oCollidedObject = oCollidingObjects.get(i);
                            if (
                                oCollidedObject instanceof HW04_ObjectBoundary &&
                                oCollidedObject.getWidth() == 1
                            ) {
                                this.setSpeedHorizontal(this.getSpeedHorizontal() * -1);
                                bShooterRicocheted = true;
                                break;
                            }
                        }
                    }
                }
                
                // If the only bubble left is the next shooter bubble, YOU WIN!
                if(HW04_ObjectBubble.getBubbleObjects().size() <= 1) {
                    O_EVENT_MANAGER.raiseEventGameEnd(true);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        popBubble
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Pop a bubble (remove it from the game)
     *                  Each popped bubble gets you a point
     *                  See if this pop has caused any nearby bubbles to be orphaned (if so, pop them too)
     *
     * ARGUMENTS:       oBubbleToPop -  The bubble to pop
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void popBubble (HW04_ObjectBubble oBubbleToPop) {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectCollidable> aoCollidingWithPoppedBubble;
            CopyOnWriteArrayList<HW04_ObjectCollidable> aoCollidingWithNeigborBubble;
            HW04_ObjectBubble oNeighborBubble;
            boolean bOrphanedBubble;
            int i;
            int j;
            
            // Ignore if this bubble is already popped
            if (HW04_ObjectGame.getObjectByGUID(oBubbleToPop.getGUID()) != null) {
                
                // Pop bubble
                HW04_Server.removeObjectFromGame(oBubbleToPop, -1);
                
                // Every popped bubble gets you a point
                O_EVENT_MANAGER.raiseEventScoreChange(null, true);
                
                // Any nearby bubbles orphaned by this?  Pop them too
                aoCollidingWithPoppedBubble = oBubbleToPop.getCollidingObjects();
                for (i = 0; i < aoCollidingWithPoppedBubble.size(); i++) {
                    if (aoCollidingWithPoppedBubble.get(i) instanceof HW04_ObjectBubble) {
                        oNeighborBubble = ((HW04_ObjectBubble) aoCollidingWithPoppedBubble.get(i));
                        aoCollidingWithNeigborBubble = oNeighborBubble.getCollidingObjects();
                        bOrphanedBubble = true;
                        for (j = 0; j < aoCollidingWithNeigborBubble.size(); j++) {
                            if (
                                aoCollidingWithNeigborBubble.get(j) instanceof HW04_ObjectStatus ||
                                (
                                    aoCollidingWithNeigborBubble.get(j) instanceof HW04_ObjectBubble &&
                                    aoCollidingWithNeigborBubble.get(j).getPositionY() < oNeighborBubble.getPositionY()
                                )
                            ) {
                                bOrphanedBubble = false;
                                break;
                            }
                        }
                        if (bOrphanedBubble == true) {
                            popBubble(oNeighborBubble);
                        }
                    }
                }
                
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
/*************************************************************************************************************
 * FILE:            HW03_ObjectCharacter.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a character object in the game world
 *************************************************************************************************************/

// IMPORTS
import java.awt.desktop.SystemEventListener;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW03_ObjectCharacter extends HW03_ObjectMoveable implements HW03_EventObserver, Comparable<HW03_ObjectCharacter> {
    
    // Required for serializable class
    private static final long serialVersionUID =    1L;
    
    // Object properties (static)
    
    private final static HW03_EventManager          O_EVENT_MANAGER = HW03_EventManager.getInstance();
    private final static int N_RADIUS_PX =          10;
    private final static int N_WIDTH_PX =           20;
    private final static int N_HEIGHT_PX =          N_WIDTH_PX;
    private final static int N_SPEED_PX_PER_SEC =   250;
    private final static int JUMP_HEIGHT_PX =       60;
    
    private static int nNextPlayerID =              1;
    
    private boolean bJumping;
    private boolean bJumpAllowed;
    
    private int nPlayerID;
    private int nJumpHeightAchieved_px;
    private int nBaseSpeedHorizontal_px_per_sec;
    private int nBaseSpeedVertical_px_per_sec;
    private int nScore;
    
    private HW03_ObjectCollidable oLastCollidedObject;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectCharacter Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new character object
     * 
     * ARGUMENTS:       nExistingGUID -     The GUID of the new object
     *                                      If -1, a new GUID will be automatically assigned
     *                  nExistingPlayerID - The player of the new object
     *                                      If -1, a new player ID will be automatically assigned
     *                  nX_px -             The position of the new object on the x axis
     *                  nY_px -             The position of the new object on the y axis
     *                  oApplet -           A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectCharacter (int nExistingGUID, int nExistingPlayerID, int nX_px, int nY_px, PApplet oApplet) {
        
        // Construct moveable object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            N_WIDTH_PX, 
            N_HEIGHT_PX, 
            N_RADIUS_PX, 
            HW03_Color.getRandomShade("Red"),
            oApplet,
            // Default horizontal speed
            0,
            // Default vertical speed
            (N_SPEED_PX_PER_SEC * -1)
        );
        
        try {
            
            /* Set player ID - should ONLY be done by the server!
             * This only exists to show onscreen 
             * (first player could have GUID of 20, that could be confusing to user)
             */
            if (nExistingPlayerID < 0) {
                this.nPlayerID = nNextPlayerID;
                nNextPlayerID++;
            }
            else {
                this.nPlayerID = nExistingPlayerID;
            }
            
            // Default to score zero
            this.nScore = 0;
            
            // Default to no collided objects
            this.oLastCollidedObject = null;
            
            // Default to not jumping but jumping allowed
            this.nJumpHeightAchieved_px = 0;
            this.bJumping = false;
            this.bJumpAllowed = true;
            
            // Set default base speeds (always falling due to gravity)
            this.nBaseSpeedVertical_px_per_sec = N_SPEED_PX_PER_SEC * -1;
            this.nBaseSpeedHorizontal_px_per_sec = 0;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getCharacterObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsCharacter -    All character objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW03_ObjectCharacter> getCharacterObjects () {
        CopyOnWriteArrayList<HW03_ObjectCharacter> aoObjectsCharacter = new CopyOnWriteArrayList<HW03_ObjectCharacter>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW03_ObjectCharacter) {
                    aoObjectsCharacter.add((HW03_ObjectCharacter) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return aoObjectsCharacter;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getCharacterObjectByPlayerID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get a character object by its player ID
     *
     * ARGUMENTS:       nObjectPlayerID -   The player ID of the desired character object
     * 
     * RETURNS:         oObjectCharacter -  The character object with the specified player ID (or null)
     *********************************************************************************************************/
    public static HW03_ObjectCharacter getCharacterObjectByPlayerID (int nObjectPlayerID) {
        HW03_ObjectCharacter oObjectCharacter = null;
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (
                    oEntry.getValue() instanceof HW03_ObjectCharacter && 
                    ((HW03_ObjectCharacter) oEntry.getValue()).getPlayerID() == nObjectPlayerID
                ) {
                    oObjectCharacter = (HW03_ObjectCharacter) oEntry.getValue();
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return oObjectCharacter;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPlayerID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nPlayerID - The character object's player ID
     *********************************************************************************************************/
    public int getPlayerID () {
        return this.nPlayerID;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getJumpAllowed
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bJumpAllowed - True if the character is allowed to jump
     *********************************************************************************************************/
    public boolean getJumpAllowed () {
        return this.bJumpAllowed;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setJumpAllowed
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       bAllowed - True if the character is allowed to jump
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setJumpAllowed (boolean bAllowed) {
       this.bJumpAllowed = bAllowed;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getScore
     *f
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nScore - The character's current score
     *********************************************************************************************************/
    public int getScore () {
        return this.nScore;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        incrementScore
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void incrementScore () {
        this.nScore++;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        decrementScore
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void decrementScore () {
        this.nScore--;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getJumping
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bJumping -  True if the player object should be jumping, otherwise false
     *********************************************************************************************************/
    public boolean getJumping () {
        return this.bJumping;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        startJump
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Start jumping up (only if we're not already jumping - can't fly)
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void startJump () {
        try {
            if (this.bJumping == false) {
                this.bJumping = true;
            }
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleJumpStatus
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Move a player object in a jump motion continuously every game / draw loop iteration
     *                  Ignore the command to jump if we are already jumping
     *                  Go up, then stop going up, then go down, then stop going down.
     *                  How far up and down?
     *                  Based on a number of pixels, rather than a counter.
     *                  This way, we are flexible to changes in frame rate.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void handleJumpStatus () {
        
        try {
            
            // In the process of jumping up?
            if (this.bJumping == true && this.nJumpHeightAchieved_px < JUMP_HEIGHT_PX) {
                
                // Set base speed to move us up
                this.nBaseSpeedVertical_px_per_sec = N_SPEED_PX_PER_SEC;

                // Increment jump height tracker
                this.nJumpHeightAchieved_px += this.getPositionY() - this.getFuturePositionY();
                
            }
            
            // Otherwise gravity is always acting upon us
            else {
                this.nBaseSpeedVertical_px_per_sec = N_SPEED_PX_PER_SEC * -1;
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handlePlatformAdjacency
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle the possibility that this character object is standing on a platform.
     *                  Intended to be called AFTER base speeds are already set (key presses, jump status).
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void handlePlatformAdjacency () {
        
        try {
            
            // Default behavior is just to use base speed
            this.setSpeedHorizontal(this.nBaseSpeedHorizontal_px_per_sec);
            this.setSpeedVertical(this.nBaseSpeedVertical_px_per_sec);
            
            /* If this character object is standing on a platform,
             * adjust speed to account for platform motion
             */
            if (
                this.oLastCollidedObject != null && 
                this.oLastCollidedObject instanceof HW03_ObjectPlatform
            ) {
                this.setSpeedHorizontal(
                    this.nBaseSpeedHorizontal_px_per_sec + 
                    ((HW03_ObjectPlatform) this.oLastCollidedObject).getSpeedHorizontal()
                );
                this.setSpeedVertical(
                    this.nBaseSpeedVertical_px_per_sec + 
                    ((HW03_ObjectPlatform) this.oLastCollidedObject).getSpeedVertical()
                );
            }

            /* Forget about last collided objects
             * Don't want to continue to be influenced by them after we leave them
             * Will get re-populated with anything we are standing on
             * because gravity causes us to constantly collide with anything we are standing on
             */
            this.oLastCollidedObject = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setHorizontalDirection
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nDirection -    Negative = left
     *                                  Positive = Right
     *                                  Zero = Stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setHorizontalDirection (int nDirection) {
        try {
            
            // Calculate speed
            if (nDirection == 0) {
                nBaseSpeedHorizontal_px_per_sec = 0;
            }
            else if (nDirection < 0) {
                nBaseSpeedHorizontal_px_per_sec = N_SPEED_PX_PER_SEC * -1;
            }
            else {
                nBaseSpeedHorizontal_px_per_sec = N_SPEED_PX_PER_SEC;
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleCollision
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle collision with another object
     *                  This is called after this object has been moved based on horizontal and vertical speeds
     *
     * ARGUMENTS:       aoCollidingObjects -    The objects with which we have collided
     *                  nOriginalX -            X-axis location before the move that caused the collision
     *                  nOriginalY -            Y-axis location before the move that caused the collision
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
   public void handleCollision (
        CopyOnWriteArrayList<HW03_ObjectCollidable> aoCollidingObjects, 
        float nOriginalX,
        float nOriginalY
    ) {
        try {
            
            // Declare variables
            HW03_ObjectCollidable oObjectOfImportance;
            boolean bDeath;
            boolean bWin;
            int i;
            
            // Assume our score will not change until we know differently
            bDeath = false;
            bWin = false;
            
            /* Characters react differently depending on what they collided with
             *  If a character is collided with multiple objects, there is a preference ranking
             *  Platform > Death Zone > Win Zone > Character > Boundary
             *  Breaking it down:
             *      Platform > Death Zone
             *          If you are collided with both, you are allowed to hit the platform instead of dying
             *      Death Zone > Win Zone
             *          Stay away from death zones!
             *      Win Zone > Character
             *          If you both get there, good for both of you!
             *      Character > Boundary
             *          Get out of each others' way
             */
            oObjectOfImportance = null;
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW03_ObjectPlatform) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW03_ObjectDeathZone) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        bDeath = true;
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW03_ObjectWinZone) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        bWin = true;
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW03_ObjectCharacter) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW03_ObjectBoundary) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                oObjectOfImportance = aoCollidingObjects.get(0);
            }
            
            // Hit Death Zone?
            if (bDeath == true) {
                O_EVENT_MANAGER.raiseEventScoreChange(this, false);
            }
            
            // Hit Win Zone?
            else if (bWin == true) {
                O_EVENT_MANAGER.raiseEventScoreChange(this, true);
            }
            
            // Hit something "solid"?
            else {
                
                // Back out of the collision
                this.backOutOfCollision(nOriginalX, nOriginalY);
                
                /* Character objects, if collided while jumping, stop jumping and start falling
                 * However, they may not start jumping again immediately!
                 */
                this.stopJumpSequence();
                
                /* Character objects are either collided with something, or jumping through the air
                 * This is because of gravity - e.g. you might be constantly colliding with the ground
                 * Keep track of the last object that the character was collided with,
                 * to support adjusting character movement if standing on a platform
                 */
                this.oLastCollidedObject = oObjectOfImportance;
                
            }
        
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

    /*********************************************************************************************************
     * FUNCTION:        stopJumpSequence
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Reset variables associated with the jump sequence
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void stopJumpSequence () {
        try {

            this.bJumping = false;
            this.nJumpHeightAchieved_px = 0;
        
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        spawn
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Spawn (or re-spawn) a character at a spawn point
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void spawn () {
        try {
            
            // Declare constants
            final int N_MAX_ATTEMPTS = 100;
            final int N_MAX_MOVE_POSITIVE_PX = 6;
            final int N_MAX_MOVE_NEGATIVE_PX = 3;
            
            // Declare variables
            HW03_ObjectSpawnPoint oSpawnPoint;
            Random oRandomizer;
            boolean bFailedToSpawn;
            float nX_px;
            float nY_px;
            int nNumAttempts;
            
            // If this is a re-spawn, the character might be in motion - stop!
            this.stopJumpSequence();
            this.stopMoving();
            
            // Take a guess about where the character should spawn (random spawn point)
            oSpawnPoint = HW03_ObjectSpawnPoint.getRandomSpawnPoint();
            nX_px = oSpawnPoint.getPositionX();
            nY_px = oSpawnPoint.getPositionY();
            
            /* Move the new character object until it has no collisions
             *  This assumes that we won't ever have so many clients that there just isn't room for them all
             *  Risk is mitigated by randomizing the attempt to find a non-colliding spot
             *  This is one of the simplifying assumptions made for the time scale of a HW assignment
             */
            oRandomizer = new Random();
            bFailedToSpawn = false;
            nNumAttempts = 0;
            while (bFailedToSpawn == false && this.doesObjectCollide() == true) { 
                nNumAttempts++;
                if (nNumAttempts >= N_MAX_ATTEMPTS) {
                    bFailedToSpawn = true;
                }
                else {
                    nX_px += oRandomizer.nextInt(N_MAX_MOVE_POSITIVE_PX + N_MAX_MOVE_NEGATIVE_PX) - N_MAX_MOVE_NEGATIVE_PX; 
                    nY_px -= oRandomizer.nextInt(N_MAX_MOVE_POSITIVE_PX + N_MAX_MOVE_NEGATIVE_PX) - N_MAX_MOVE_NEGATIVE_PX; 
                    this.setPositionX(nX_px); 
                    this.setPositionY(nY_px); 
                }
            }
            
            /* If this spawn point is so crowded that we just can't find a spot, 
             * try again, maybe we'll get a different spawn point
             */
            if (bFailedToSpawn == true) {
                O_EVENT_MANAGER.raiseEventSpawn(this);
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticHeight
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the character object static height that both the client(s) and server should use
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Static height (pixels)
     *********************************************************************************************************/
    public static int getStaticHeight () {
        return N_HEIGHT_PX;
    }

    /*********************************************************************************************************
     * FUNCTION:        display
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Display a character object onscreen
     *                  https://processing.org/tutorials/text/
     *
     * ARGUMENTS:       bSpecial - True if this is a special object that should be drawn in a special color
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    public void display (boolean bSpecial) {
        try {
            
            // Skip entirely if the object is temporarily hidden
            if (this.getHiddenFlag() == false) {
               
                // Display like all renderable objects are displayed
                super.display(bSpecial);
                
                // Also display player ID
                this.getApplet().fill(HW03_Color.black());
                this.getApplet().textAlign(PConstants.CENTER, PConstants.CENTER);
                this.getApplet().text(
                    this.getPlayerID(), 
                    this.getPositionX() + this.getWidth() / 2, 
                    this.getPositionY() + this.getHeight() / 2
                );
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        toString
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Override the toString method
     *
     * ARGUMENTS:       sObjectAsString -   The game object represented as a string
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    public String toString() { 
        
        // Declare variables
        String sObjectAsString;
        
        // Build string
        sObjectAsString = String.join(
            ", ", 
            Integer.toString(this.getGUID()), 
            Integer.toString(this.getHeight()), 
            (this.getJumping() ? "1" : "0"), 
            Integer.toString(this.getPlayerID()), 
            Float.toString(this.getPositionX()), 
            Float.toString(this.getPositionY()), 
            Integer.toString(this.getRadius()), 
            Integer.toString(this.getScore()), 
            Integer.toString(this.getSpeedHorizontal()), 
            Integer.toString(this.getSpeedVertical()), 
            this.getType(), 
            Integer.toString(this.getWidth())
        );
        
        // Return
        return sObjectAsString;
                
    }

    /*********************************************************************************************************
     * FUNCTION:        compareTo
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Override the compareTo method
     *                  Used to provide sorting of character objects
     *                  Character objects might be out of order if 
     *                  many clients join the game at nearly the same time
     *
     * ARGUMENTS:       sObjectAsString -   The game object represented as a string
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    public int compareTo (HW03_ObjectCharacter oComparisonCharacter) {
        return this.getGUID() - oComparisonCharacter.getGUID();
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
            
            // USER INPUT
            if (oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.USER_INPUT) {
                
                if (oEventArguments.get("sKey").equals("SPACE") && oEventArguments.get("bPressed").equals("TRUE")) {
                    this.startJump();
                    this.setJumpAllowed(false);
                }
                else if (oEventArguments.get("sKey").equals("LEFT") && oEventArguments.get("bPressed").equals("TRUE")) {
                    this.setHorizontalDirection(-1);
                }
                else if (oEventArguments.get("sKey").equals("RIGHT") && oEventArguments.get("bPressed").equals("TRUE")) {
                    this.setHorizontalDirection(1);
                }
                else if (oEventArguments.get("sKey").equals("SPACE") && oEventArguments.get("bPressed").equals("FALSE")) {
                    this.setJumpAllowed(true);
                }
                else if (oEventArguments.get("sKey").equals("LEFT") && oEventArguments.get("bPressed").equals("FALSE")) {
                    this.setHorizontalDirection(0);
                }
                else if (oEventArguments.get("sKey").equals("RIGHT") && oEventArguments.get("bPressed").equals("FALSE")) {     
                    this.setHorizontalDirection(0);
                }
                // There may be other user input types but the character object doesn't care
            }
            
            // COLLISION
            else if (
                oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.COLLISION && 
                ((HW03_ObjectCollidable) oEventArguments.get("oMovedObject")).getGUID() == this.getGUID()
            ) {
                // Separate function because it's (relatively) a lot of code
                this.handleCollision(
                    (CopyOnWriteArrayList<HW03_ObjectCollidable>) oEventArguments.get("aoCollidingObjects"), 
                    (float) oEventArguments.get("nOriginalX"),
                    (float) oEventArguments.get("nOriginalY")
                );
            }
            
            // SCORE CHANGE
            else if (
                oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.SCORE_CHANGE && 
                ((HW03_ObjectCollidable) oEventArguments.get("oCharacterObject")).getGUID() == this.getGUID()
            ) {
                if (oEventArguments.get("bScoreIncrement").equals("TRUE")) {
                    this.incrementScore();
                }
                else {
                    this.decrementScore();
                }
                // Either way, re-spawn
                O_EVENT_MANAGER.raiseEventSpawn(this);
            }
            
            // SPAWN
            else if (
                oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.SPAWN && 
                ((HW03_ObjectCollidable) oEventArguments.get("oCharacterObject")).getGUID() == this.getGUID()
            ) {
                this.spawn();
            }

        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
   
}
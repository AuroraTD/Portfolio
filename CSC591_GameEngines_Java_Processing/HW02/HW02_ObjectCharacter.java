package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectCharacter.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a character object in the game world
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW02_ObjectCharacter extends HW02_ObjectMoveable implements Comparable<HW02_ObjectCharacter> {
    
    // Required for serializable class
    private static final long serialVersionUID =    1L;
    
    // Object properties (static)
    
    private final static int N_RADIUS_PX =          10;
    private final static int N_WIDTH_PX =           20;
    private final static int N_HEIGHT_PX =          N_WIDTH_PX;
    private final static int N_SPEED_PX =           10;
    private final static int N_JUMP_COUNTER_MAX =   15;
    
    private static int nNextPlayerID =              1;
    
    private boolean bJumping;
    private boolean bJumpAllowed;
    
    private int nPlayerID;
    private int nJumpCounter;
    private int nBaseSpeedHorizontal;
    private int nBaseSpeedVertical;
    private int nScore;
    
    private HW02_ObjectCollidable oLastCollidedObject;
    
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
    public HW02_ObjectCharacter (int nExistingGUID, int nExistingPlayerID, int nX_px, int nY_px, PApplet oApplet) {
        
        // Construct moveable object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            N_WIDTH_PX, 
            N_HEIGHT_PX, 
            N_RADIUS_PX, 
            HW02_Color.getRandomShade("Red"),
            oApplet,
            // Default horizontal speed
            0,
            // Default vertical speed
            (N_SPEED_PX * -1)
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
            this.nJumpCounter = 0;
            this.bJumping = false;
            this.bJumpAllowed = true;
            
            // Set default base speeds (always falling due to gravity)
            this.nBaseSpeedVertical = N_SPEED_PX * -1;
            this.nBaseSpeedHorizontal = 0;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
    public static CopyOnWriteArrayList<HW02_ObjectCharacter> getCharacterObjects () {
        CopyOnWriteArrayList<HW02_ObjectCharacter> aoObjectsCharacter = new CopyOnWriteArrayList<HW02_ObjectCharacter>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW02_ObjectGame> oObjectsGame = HW02_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW02_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW02_ObjectCharacter) {
                    aoObjectsCharacter.add((HW02_ObjectCharacter) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        return aoObjectsCharacter;
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
            HW02_Utility.handleError(oError);
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
     *                      Based on a "dumb" counter constant.
     *                      This will result in different behavior for different frame rates.
     *                      Not a future-proof strategy, 
     *                      but acceptable for time constraints of homework assignment.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void handleJumpStatus () {
        
        try {
            
            // In the process of jumping up?
            if (this.bJumping == true && this.nJumpCounter < N_JUMP_COUNTER_MAX) {
                
                // Set base speed to move us up
                this.nBaseSpeedVertical = N_SPEED_PX;

                // Increment jump counter
                this.nJumpCounter++;
                
            }
            
            // Otherwise gravity is always acting upon us
            else {
                this.nBaseSpeedVertical = N_SPEED_PX * -1;
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            this.setSpeedHorizontal(this.nBaseSpeedHorizontal);
            this.setSpeedVertical(this.nBaseSpeedVertical);
            
            /* If this character object is standing on a platform,
             * adjust speed to account for platform motion
             */
            if (
                this.oLastCollidedObject != null && 
                this.oLastCollidedObject instanceof HW02_ObjectPlatform
            ) {
                this.setSpeedHorizontal(
                        this.nBaseSpeedHorizontal + 
                        ((HW02_ObjectPlatform) this.oLastCollidedObject).getSpeedHorizontal()
                    );
                    this.setSpeedVertical(
                        this.nBaseSpeedVertical + 
                        ((HW02_ObjectPlatform) this.oLastCollidedObject).getSpeedVertical()
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
            HW02_Utility.handleError(oError);
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
                nBaseSpeedHorizontal = 0;
            }
            else if (nDirection < 0) {
                nBaseSpeedHorizontal = N_SPEED_PX * -1;
            }
            else {
                nBaseSpeedHorizontal = N_SPEED_PX;
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
     *                  bYAxis -                True if the collision is on the Y (up/down) axis, otherwise false
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
   public void handleCollision (
        CopyOnWriteArrayList<HW02_ObjectCollidable> aoCollidingObjects, 
        boolean yAxis
    ) {
        try {
            
            // Declare variables
            HW02_ObjectCollidable oObjectOfImportance;
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
                    if (aoCollidingObjects.get(i) instanceof HW02_ObjectPlatform) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW02_ObjectDeathZone) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        bDeath = true;
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW02_ObjectWinZone) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        bWin = true;
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW02_ObjectCharacter) {
                        oObjectOfImportance = aoCollidingObjects.get(i);
                        break;
                    }
                }
            }
            if (oObjectOfImportance == null) {
                for (i = 0; i < aoCollidingObjects.size(); i++) {
                    if (aoCollidingObjects.get(i) instanceof HW02_ObjectBoundary) {
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
                this.decrementScore();
                this.spawn();
            }
            
            // Hit Win Zone?
            else if (bWin == true) {
                this.incrementScore();
                this.spawn();
            }
            
            // Hit something "solid"?
            else {
                
                // Back out of the collision
                if (yAxis == true) {
                    this.backOutOfCollisionY();
                }
                else {
                    this.backOutOfCollisionX();
                }
                
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
            HW02_Utility.handleError(oError);
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
            this.nJumpCounter = 0;
        
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            HW02_ObjectSpawnPoint oSpawnPoint;
            Random oRandomizer;
            boolean bFailedToSpawn;
            int nX_px;
            int nY_px;
            int nNumAttempts;
            
            // If this is a re-spawn, the character might be in motion - stop!
            this.stopJumpSequence();
            this.stopMoving();
            
            // Take a guess about where the character should spawn (random spawn point)
            oSpawnPoint = HW02_ObjectSpawnPoint.getRandomSpawnPoint();
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
                this.spawn();
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            
            // Display like all renderable objects are displayed
            super.display(bSpecial);
            
            // Also display player ID
            this.getApplet().fill(HW02_Color.black());
            this.getApplet().textAlign(PConstants.CENTER, PConstants.CENTER);
            this.getApplet().text(
                this.getPlayerID(), 
                this.getPositionX() + this.getWidth() / 2, 
                this.getPositionY() + this.getHeight() / 2
            );
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            Integer.toString(this.getPositionX()), 
            Integer.toString(this.getPositionY()), 
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
    public int compareTo (HW02_ObjectCharacter oComparisonCharacter) {
        return this.getGUID() - oComparisonCharacter.getGUID();
    } 
   
   
}
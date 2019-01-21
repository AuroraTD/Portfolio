package HW01;
/*************************************************************************************************************
 * FILE:            HW01_PlayerObject.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a player object in the game world
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW01_PlayerObject extends HW01_RenderableObject {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    final static String S_OBJECT_TYPE =     "Player";
    final static HW01_Color O_COLOR =       new HW01_Color(240, 30, 30);
    final static int N_RADIUS_PX =          10;
    final static int N_WIDTH_PX =           HW01_Utility.getPlayerWidth();
    final static int N_HEIGHT_PX =          20;
    final static int N_SPEED_PX =           10;
    final static int N_JUMP_COUNTER_MAX =   35;
    
    // Object properties (protected)
    protected boolean bJumping;
    protected int     nMovementDirection;
    
    // Object properties (private)
    private int nJumpCounter;
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_PlayerObject Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new game player object
     *
     * ARGUMENTS:       nNewGUID -      The GUID of the new object
     *                  nX_px -         The position of the new object on the x axis
     *                  nY_px -         The position of the new object on the y axis
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW01_PlayerObject (int nNewGUID, int nX_px, int nY_px, PApplet oApplet) {
        
        // Construct renderable object
        super(
            nNewGUID, 
            S_OBJECT_TYPE,
            nX_px, 
            nY_px, 
            N_WIDTH_PX, 
            N_HEIGHT_PX, 
            N_RADIUS_PX, 
            O_COLOR,
            oApplet
        );
        
        try {
            this.nMovementDirection =   0;
            this.nJumpCounter =         0;
            this.bJumping =             false;
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getMovementDirection
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nDirection -    The direction to move in
     *                                  0 means stop
     *                                  > 0 means go right
     *                                  < 0 means go left
     *********************************************************************************************************/
    int getMovementDirection () {
        return this.nMovementDirection;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setMovementDirection
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nDirection -    The direction to move in
     *                                  0 means stop
     *                                  > 0 means go right
     *                                  < 0 means go left
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setMovementDirection (int nDirection) {
        try {
            this.nMovementDirection = nDirection;
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
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
    boolean getJumping () {
        return this.bJumping;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setJumping
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       bJumping -  True if the player object should be jumping, otherwise false
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setJumping (boolean bJumping) {
        try {
            this.bJumping = bJumping;
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        moveLeftRight
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Move a player object to the left or right
     *
     * ARGUMENTS:       aoGameObjects - Collection of all game objects, used for collision detection
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void moveLeftRight (CopyOnWriteArrayList<HW01_RenderableObject>  aoGameObjects) {
        
        try {
            
            // Declare variables
            int nOriginalX_px;
            
            // Initialize variables
            nOriginalX_px = this.nX_px;
            
            // Move the player object
            if (this.nMovementDirection != 0) {
                if (this.nMovementDirection > 0) {
                    this.nX_px += N_SPEED_PX;
                }
                else {
                    this.nX_px -= N_SPEED_PX;
                }
            }
            
            /* Don't move it past the borders of the window
             * (0,0) is at the TOP LEFT corner of the object
             * (0,0) is at the TOP LEFT corner of the drawing window
             */
            if (this.nX_px > this.oApplet.width - N_WIDTH_PX) {
                this.nX_px = this.oApplet.width - N_WIDTH_PX;
            }
            else if (this.nX_px < 0) {
                this.nX_px = 0;
            }
            
            // Stop if you hit another object
            if (this.doesObjectCollide(aoGameObjects) == true) {
                this.setPositionX(nOriginalX_px);
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        jump
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
     * ARGUMENTS:       aoGameObjects - Collection of all game objects, used for collision detection
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void jump (CopyOnWriteArrayList<HW01_RenderableObject>  aoGameObjects) {
        
        try {
            
            // Declare variables
            int nOriginalY_px;
            
            // Initialize variables
            nOriginalY_px = this.nY_px;
            
            // In the process of jumping?
            if (this.bJumping == true) {
                
                // Increment jump counter
                this.nJumpCounter++;
                
                // In the process of jumping and still going up?
                if (this.nJumpCounter < N_JUMP_COUNTER_MAX / 2) {
                    this.nY_px -= N_SPEED_PX;
                }
                
                // In the process of jumping and now going down?
                else if (this.nJumpCounter < N_JUMP_COUNTER_MAX) {
                    this.nY_px += N_SPEED_PX;
                }
                
                // Done with jump?
                else {
                    this.bJumping = false;
                    this.nJumpCounter = 0;
                }
                
            }
            
            /* Otherwise gravity is always acting upon us
             * While not specifically required in this homework assignment,
             * it was the only sane thing to do,
             * since a jump could result in the player being on top of an obstacle or another player
             */
            else {
                this.nY_px += N_SPEED_PX;
            }
            
            /* Don't move it past the borders of the window
             * (0,0) is at the TOP LEFT corner of the object
             * (0,0) is at the TOP LEFT corner of the drawing window
             */
            if (this.nY_px > this.oApplet.height - N_HEIGHT_PX) {
                this.nY_px = this.oApplet.height - N_HEIGHT_PX;
            }
            else if (this.nY_px < 0) {
                this.nY_px = 0;
            }
            
            // Stop if you hit another object
            if (this.doesObjectCollide(aoGameObjects) == true) {
                this.setPositionY(nOriginalY_px);
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
        
    }

}
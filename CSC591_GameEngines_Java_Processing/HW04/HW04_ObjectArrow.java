/*************************************************************************************************************
 * FILE:            HW04_ObjectArrow.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents an arrow object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW04_ObjectArrow extends HW04_ObjectRenderable {
    
    // Required for serializable class
    private static final long serialVersionUID =    1L;
    
    // Object properties (static)
    private final static int N_LENGTH_BODY_PX =     HW04_Utility.getWindowSize() / 2;
    private final static int N_LENGTH_HEAD_PX =     10;
    private final static int N_HEAD_ANGLE_DEG =     135;
    private final static int N_SPEED_DEG_PER_SEC =  60;
    private static HW04_ObjectArrow oLatestArrow;
    
    // Object properties (variable)
    private float nAngle_deg;
    private int nMovement;

    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectArrow Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new arrow object
     * 
     * ARGUMENTS:       nExistingGUID -             The GUID of the new object
     *                                              If -1, a new GUID will be automatically assigned
     *                  nX_px -                     The origin of the new arrow on the x axis (0 = left)
     *                  nY_px -                     The origin of the new arrow on the y axis (0 = top)
     *                  oApplet -                   A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectArrow (
        int nExistingGUID,
        int nX_px,
        int nY_px,
        PApplet oApplet
    ) {
        
        // Construct renderable object
        super(
            // GUID
            nExistingGUID,
            // X
            nX_px, 
            // Y
            nY_px, 
            // Width
            0, 
            // Height
            0, 
            // Radius
            0, 
            // Color
            HW04_Color.getPureColor("Gray"),
            // Applet
            oApplet
        );
        
        // Set angle to point straight up
        this.nAngle_deg = 0;
        
        // Remember the latest arrow
        oLatestArrow = this;
        
    }

    
    /*********************************************************************************************************
     * FUNCTION:        getLatestArrow
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oLatestArrow
     *********************************************************************************************************/
    public static HW04_ObjectArrow getLatestArrow () {
        return oLatestArrow;
    }

    /*********************************************************************************************************
     * FUNCTION:        addArrowToLatestShooterBubble
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add an arrow object
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addArrowToLatestShooterBubble () {
        try {
            
            // Declare variables
            HW04_ObjectArrow oNewArrow;
            HW04_ObjectBubble oLatestShooter;
            
            // Find latest shooter bubble
            oLatestShooter = HW04_ObjectBubble.getLatestShooterBubble();
            
            // Create arrow
            oNewArrow = new HW04_ObjectArrow(
                // Get auto GUID
                -1,
                // X
                (int) (oLatestShooter.getPositionX() + HW04_ObjectBubble.getStaticRadius() / 2),
                // Y
                (int) (oLatestShooter.getPositionY() + HW04_ObjectBubble.getStaticRadius() / 2),
                // Applet
                null
            );

        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setMovementDirection
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nMovementDirection -    1   Clockwise
     *                                          0   Stopped
     *                                          -1  Counter-Clockwise
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setMovementDirection (int nMovementDirection) {
        try {
            
            this.nMovement = nMovementDirection;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getAngle
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nAngle_deg
     *********************************************************************************************************/
    public float getAngle () {
        return this.nAngle_deg;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        updateAngle
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Update the angle of the arrow, 
     *                  based on speed and how long a game loop iteration takes.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void updateAngle () {
        try {
            
            // Declare constants
            final int N_MS_PER_SEC = 1000;
            final double N_TIME_SINCE_LAST_UPDATE_SEC = 
                (double) HW04_Time_Loop.getPlayInstance().getTimeDelta() * 
                (double) HW04_Time_Loop.getPlayInstance().getTickSize() / 
                (double) N_MS_PER_SEC;
            
            // Update the angle of the arrow
            this.nAngle_deg += 
                this.nMovement * 
                N_SPEED_DEG_PER_SEC * 
                N_TIME_SINCE_LAST_UPDATE_SEC;
            
            // Limit to -90, 90
            if (this.nAngle_deg < -90) {
                this.nAngle_deg = -90;
            }
            if (this.nAngle_deg > 90) {
                this.nAngle_deg = 90;
            }
            
            // Let others know
            if (HW04_Globals.bClient == false) {
                HW04_Server.notifyClientsAboutOneObject(-1, this);
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        display
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Display an arrow object onscreen
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    public void display () {
        try {
            
            // Declare variables
            float nEndArrowX_px;
            float nEndArrowY_px;
            float nEndHeadLeftX_px;
            float nEndHeadLeftY_px;
            float nEndHeadRightX_px;
            float nEndHeadRightY_px;
            
            // Set arrow color and width
            this.getApplet().stroke(this.getColor().R, this.getColor().G, this.getColor().B);
            this.getApplet().strokeWeight(2);
            
            // Calculate end points
            nEndArrowX_px = 
                    this.getPositionX() + 
                    PApplet.sin(PApplet.radians(this.nAngle_deg)) * N_LENGTH_BODY_PX;
            nEndArrowY_px = 
                    this.getPositionY() - 
                    PApplet.cos(PApplet.radians(this.nAngle_deg)) * N_LENGTH_BODY_PX;
            nEndHeadLeftX_px = 
                    nEndArrowX_px + 
                    PApplet.sin(PApplet.radians(this.nAngle_deg - N_HEAD_ANGLE_DEG)) * N_LENGTH_HEAD_PX;
            nEndHeadLeftY_px = 
                    nEndArrowY_px - 
                    PApplet.cos(PApplet.radians(this.nAngle_deg - N_HEAD_ANGLE_DEG)) * N_LENGTH_HEAD_PX;
            nEndHeadRightX_px = 
                    nEndArrowX_px + 
                    PApplet.sin(PApplet.radians(this.nAngle_deg + N_HEAD_ANGLE_DEG)) * N_LENGTH_HEAD_PX;
            nEndHeadRightY_px = 
                    nEndArrowY_px - 
                    PApplet.cos(PApplet.radians(this.nAngle_deg + N_HEAD_ANGLE_DEG)) * N_LENGTH_HEAD_PX;
            
            // Draw arrow body
            this.getApplet().line(
                this.getPositionX(), 
                this.getPositionY(), 
                nEndArrowX_px, 
                nEndArrowY_px
            );
            
            // Draw arrow head
            this.getApplet().line(
                nEndArrowX_px, 
                nEndArrowY_px, 
                nEndHeadLeftX_px, 
                nEndHeadLeftY_px
            );
            this.getApplet().line(
                nEndArrowX_px, 
                nEndArrowY_px, 
                nEndHeadRightX_px, 
                nEndHeadRightY_px
            );
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
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
    public void handleCollision (
        CopyOnWriteArrayList<HW04_ObjectCollidable> oCollidingObjects, 
        float nOriginalX,
        float nOriginalY
    ) {
        // No-Op (other objects care if they collide with us - we don't care)
    }
    
}
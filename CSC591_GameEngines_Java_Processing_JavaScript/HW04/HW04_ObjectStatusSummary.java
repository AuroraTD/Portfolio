/*************************************************************************************************************
 * FILE:            HW04_ObjectStatusSummary.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a status summary
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.CopyOnWriteArrayList;

import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW04_ObjectStatusSummary extends HW04_ObjectStatus {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectStatusSummary Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new scoreboard object
     * 
     * ARGUMENTS:       nExistingGUID -     The GUID of the new object
     *                                      If -1, a new GUID will be automatically assigned
     *                  oApplet -           A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectStatusSummary (int nExistingGUID, PApplet oApplet) {
        
        // Construct status object
        super(
            nExistingGUID,
            oApplet
        );
        
    }

    /*********************************************************************************************************
     * FUNCTION:        display
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Display a status summary object onscreen
     *                  https://processing.org/tutorials/text/
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    public void display () {
        try {
            
            // Display like all renderable objects are displayed
            super.display();
            
            // Declare variables
            String sSummaryText;
            
            // Draw the status summary box
            this.getApplet().fill(HW04_Color.white());
            this.getApplet().stroke(HW04_Color.black());
            this.getApplet().rect(
                // X
                this.getPositionX(), 
                // Y
                this.getPositionY(), 
                // Width
                HW04_Utility.getWindowSize(), 
                // Height
                HW04_ObjectStatus.getStaticHeight(), 
                // Radius
                0
            );
            
            // Build text
            switch (HW04_Replay.getCurrentState()) {
                case IDLE:
                    sSummaryText = 
                        HW04_Time.isGamePaused() ? 
                        ("PAUSED by player " + HW04_Time.getLastClientPausedPlayerID()) : 
                        "Have Fun!";
                    break;
                case RECORDING:
                    sSummaryText = "Recording your play for replay...";
                    break;
                case WAITING_TO_REPLAY:
                    sSummaryText = "Waiting to learn replay speed...";
                    break;
                case REPLAYING:
                    sSummaryText = "Replaying your recording...";
                    break;
                default:
                    throw new Exception("Do not recognize replay feature state " + HW04_Replay.getCurrentState());
            }
            
            // Draw text
            this.getApplet().fill(HW04_Color.black());
            this.getApplet().textAlign(PConstants.LEFT, PConstants.TOP);
            this.getApplet().text(
                sSummaryText, 
                this.getPositionX() + HW04_ObjectStatus.getStaticMargin(), 
                this.getPositionY() + HW04_ObjectStatus.getStaticMargin()
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
    void handleCollision(
        CopyOnWriteArrayList<HW04_ObjectCollidable> oCollidingObjects, 
        float nOriginalX,
        float nOriginalY
    ) {
        // No-Op (other objects care if they collide with us - we don't care)
    }
    
}
/*************************************************************************************************************
 * FILE:            HW04_ObjectStatusInstructions.java
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
public class HW04_ObjectStatusInstructions extends HW04_ObjectStatus {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectStatusInstructions Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new instructions object
     * 
     * ARGUMENTS:       nExistingGUID -     The GUID of the new object
     *                                      If -1, a new GUID will be automatically assigned
     *                  oApplet -           A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectStatusInstructions (int nExistingGUID, PApplet oApplet) {
        
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
     * DESCRIPTION:     Display an instructions object onscreen
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
            String nInstructionsText;
            
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
                    if (HW04_Time.isGamePaused() == true) {
                        // If you're the one who paused, you're allowed to un-pause
                        if (HW04_Time.getLastClientPausedPlayerID() == HW04_Globals.nPlayerID) {
                            nInstructionsText = ", 'P' un-pause";
                        }
                        // Otherwise you just gotta wait
                        else {
                            nInstructionsText = "";
                        }
                    }
                    else {
                        nInstructionsText = 
                            ", 'P' pause";
                        nInstructionsText += 
                            (boolean) HW04_ScriptManager.invokeFunction("isReplayEnabled") ? 
                            ", 'R' start recording" : 
                            "";
                        nInstructionsText += 
                            ", '<-' go left, '->' go right, [SPACE] " + 
                            (String) HW04_ScriptManager.invokeFunction("getSpaceBarAction");
                    }
                    break;
                case RECORDING:
                    nInstructionsText = 
                        (boolean) HW04_ScriptManager.invokeFunction("isReplayEnabled") ? 
                        ", 'R' stop recording" : 
                        "";
                    nInstructionsText += 
                        ", '<-' go left, '-> go right, [SPACE] " + 
                        (String) HW04_ScriptManager.invokeFunction("getSpaceBarAction");
                    break;
                case WAITING_TO_REPLAY:
                    nInstructionsText = 
                        (boolean) HW04_ScriptManager.invokeFunction("isReplayEnabled") ? 
                        ", '1' replay at 0.5x, '2' replay at 1x, '3' replay at 2x" : 
                        "";
                    break;
                case REPLAYING:
                    nInstructionsText = 
                        (boolean) HW04_ScriptManager.invokeFunction("isReplayEnabled") ? 
                        ", '1' replay at 0.5x, '2' replay at 1x, '3' replay at 2x" : 
                        "";
                    break;
                default:
                    throw new Exception("Do not recognize replay feature state " + HW04_Replay.getCurrentState());
            }
            nInstructionsText = "'Q' quit" + nInstructionsText;
            
            // Draw text
            this.getApplet().fill(HW04_Color.black());
            this.getApplet().textAlign(PConstants.LEFT, PConstants.TOP);
            this.getApplet().text(
                nInstructionsText, 
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
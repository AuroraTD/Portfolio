/*************************************************************************************************************
 * FILE:            HW03_ObjectStatusInstructions.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a status summary
 *************************************************************************************************************/

// IMPORTS
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW03_ObjectStatusInstructions extends HW03_ObjectStatus {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectStatusInstructions Constructor
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
    public HW03_ObjectStatusInstructions (int nExistingGUID, PApplet oApplet) {
        
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
     * ARGUMENTS:       bSpecial - True if this is a special object that should be drawn in a special color
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    public void display (boolean bSpecial) {
        try {
            
            // Display like all renderable objects are displayed
            super.display(bSpecial);
            
            // Declare variables
            String nInstructionsText;
            
            // Draw the status summary box
            this.getApplet().fill(HW03_Color.white());
            this.getApplet().stroke(HW03_Color.black());
            this.getApplet().rect(
                // X
                this.getPositionX(), 
                // Y
                this.getPositionY(), 
                // Width
                HW03_Utility.getWindowSize(), 
                // Height
                HW03_ObjectStatus.getStaticHeight(), 
                // Radius
                0
            );
            
            // Build text
            switch (HW03_Replay.getCurrentState()) {
                case IDLE:
                    if (HW03_Time.isGamePaused() == true) {
                        // If you're the one who paused, you're allowed to un-pause
                        if (HW03_Time.getLastClientPausedPlayerID() == HW03_Globals.nPlayerID) {
                            nInstructionsText = ", 'P' un-pause";
                        }
                        // Otherwise you just gotta wait
                        else {
                            nInstructionsText = "";
                        }
                    }
                    else {
                        nInstructionsText = ", 'P' pause, 'R' start recording, '<-' go left, '->' go right, [SPACE] jump";
                    }
                    break;
                case RECORDING:
                    nInstructionsText = ", 'R' stop recording, '<-' go left, '-> go right, [SPACE] jump";
                    break;
                case WAITING_TO_REPLAY:
                    nInstructionsText = ", '1' replay at 0.5x, '2' replay at 1x, '3' replay at 2x";
                    break;
                case REPLAYING:
                    nInstructionsText = ", '1' replay at 0.5x, '2' replay at 1x, '3' replay at 2x";
                    break;
                default:
                    throw new Exception("Do not recognize replay feature state " + HW03_Replay.getCurrentState());
            }
            nInstructionsText = "'Q' quit" + nInstructionsText;
            
            // Draw text
            this.getApplet().fill(HW03_Color.black());
            this.getApplet().textAlign(PConstants.LEFT, PConstants.TOP);
            this.getApplet().text(
                nInstructionsText, 
                this.getPositionX() + HW03_ObjectStatus.getStaticMargin(), 
                this.getPositionY() + HW03_ObjectStatus.getStaticMargin()
            );
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
}
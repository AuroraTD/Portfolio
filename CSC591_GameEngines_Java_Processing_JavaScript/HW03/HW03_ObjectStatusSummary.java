/*************************************************************************************************************
 * FILE:            HW03_ObjectStatusSummary.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a status summary
 *************************************************************************************************************/

// IMPORTS
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW03_ObjectStatusSummary extends HW03_ObjectStatus {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectStatusSummary Constructor
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
    public HW03_ObjectStatusSummary (int nExistingGUID, PApplet oApplet) {
        
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
            String sSummaryText;
            
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
                    sSummaryText = 
                        HW03_Time.isGamePaused() ? 
                        ("PAUSED by player " + HW03_Time.getLastClientPausedPlayerID()) : 
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
                    throw new Exception("Do not recognize replay feature state " + HW03_Replay.getCurrentState());
            }
            
            // Draw text
            this.getApplet().fill(HW03_Color.black());
            this.getApplet().textAlign(PConstants.LEFT, PConstants.TOP);
            this.getApplet().text(
                sSummaryText, 
                this.getPositionX() + HW03_ObjectStatus.getStaticMargin(), 
                this.getPositionY() + HW03_ObjectStatus.getStaticMargin()
            );
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
}
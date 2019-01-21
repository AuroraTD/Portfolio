/*************************************************************************************************************
 * FILE:            HW03_ObjectStatusScoreboard.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a scoreboard
 *************************************************************************************************************/

// IMPORTS
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW03_ObjectStatusScoreboard extends HW03_ObjectStatus {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectStatusScoreboard Constructor
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
    public HW03_ObjectStatusScoreboard (int nExistingGUID, PApplet oApplet) {
        
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
     * DESCRIPTION:     Display a scoreboard object onscreen
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
            CopyOnWriteArrayList<HW03_ObjectCharacter> aoObjectsCharacter;
            String sTextForCharacter;
            int nCellWidth_px;
            float nCellX_px;
            int i;
            
            // Get all character objects
            aoObjectsCharacter = HW03_ObjectCharacter.getCharacterObjects();
            
            /* Sort character objects by GUID
             * They might be out of order if many clients joined the game at nearly the same time
             */
            Collections.sort(aoObjectsCharacter);
            
            // Draw each character's score
            for (i = 0; i < aoObjectsCharacter.size(); i++) {
                
                // Draw table cell
                nCellWidth_px = this.getWidth() / aoObjectsCharacter.size();
                nCellX_px = this.getPositionX() + nCellWidth_px * i;
                this.getApplet().fill(HW03_Color.white());
                this.getApplet().stroke(HW03_Color.black());
                this.getApplet().rect(
                    // X
                    nCellX_px, 
                    // Y
                    this.getPositionY(), 
                    // Width
                    nCellWidth_px, 
                    // Height
                    this.getHeight(), 
                    // Radius
                    0
                );
                
                // Build text
                sTextForCharacter = 
                    "P" +
                    aoObjectsCharacter.get(i).getPlayerID() + 
                    ": " + 
                    aoObjectsCharacter.get(i).getScore();
                
                // Draw text
                this.getApplet().fill(HW03_Color.black());
                this.getApplet().textAlign(PConstants.LEFT, PConstants.TOP);
                this.getApplet().text(
                    sTextForCharacter, 
                    nCellX_px + HW03_ObjectStatus.getStaticMargin(), 
                    this.getPositionY() + HW03_ObjectStatus.getStaticMargin()
                );
                
            }
            
            // Free
            aoObjectsCharacter = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
}
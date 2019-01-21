/*************************************************************************************************************
 * FILE:            HW04_ObjectStatusScoreboard.java
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
public class HW04_ObjectStatusScoreboard extends HW04_ObjectStatus {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectStatusScoreboard Constructor
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
    public HW04_ObjectStatusScoreboard (int nExistingGUID, PApplet oApplet) {
        
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
            CopyOnWriteArrayList<HW04_ObjectCharacter> aoObjectsCharacter;
            String sTextForCharacter;
            int nCellWidth_px;
            float nCellX_px;
            int i;
            
            // Get all character objects
            aoObjectsCharacter = HW04_ObjectCharacter.getCharacterObjects();
            
            /* Sort character objects by GUID
             * They might be out of order if many clients joined the game at nearly the same time
             */
            Collections.sort(aoObjectsCharacter);
            
            // Draw each character's score
            for (i = 0; i < aoObjectsCharacter.size(); i++) {
                
                // Draw table cell
                nCellWidth_px = this.getWidth() / aoObjectsCharacter.size();
                nCellX_px = this.getPositionX() + nCellWidth_px * i;
                this.getApplet().fill(HW04_Color.white());
                this.getApplet().stroke(HW04_Color.black());
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
                this.getApplet().fill(HW04_Color.black());
                this.getApplet().textAlign(PConstants.LEFT, PConstants.TOP);
                this.getApplet().text(
                    sTextForCharacter, 
                    nCellX_px + HW04_ObjectStatus.getStaticMargin(), 
                    this.getPositionY() + HW04_ObjectStatus.getStaticMargin()
                );
                
            }
            
            // Free
            aoObjectsCharacter = null;
            
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
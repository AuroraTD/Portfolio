package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectStatusScoreboard.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a scoreboard object
 *************************************************************************************************************/

// IMPORTS
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

// CLASS DEFINITION
public class HW02_ObjectScoreboard extends HW02_ObjectRenderable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /* Object properties (static)
     * By default the scoreboard is drawn at the top of the window
     */
    private final static HW02_Color O_COLOR =       new HW02_Color(
                                                        HW02_Color.white(), 
                                                        HW02_Color.white(), 
                                                        HW02_Color.white()
                                                    );
    private final static int N_X_PX =               0;
    private final static int N_Y_PX =               0;
    private final static int N_WIDTH_PX =           HW02_Utility.getWindowSize();
    private final static int N_RADIUS_PX =          0;
    private final static int N_MARGIN_PX =          4;
    private final static int N_HEIGHT_PX =          HW02_ObjectRenderable.getStaticFontSize() + N_MARGIN_PX * 2;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectStatusScoreboard Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new scoreboard object
     * 
     * ARGUMENTS:       nExistingGUID -     The GUID of the new object
     *                                      If -1, a new GUID will be automatically assigned
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW02_ObjectScoreboard (int nExistingGUID, PApplet oApplet) {
        
        // Construct renderable object
        super(
            nExistingGUID,
            N_X_PX, 
            N_Y_PX, 
            N_WIDTH_PX, 
            N_HEIGHT_PX, 
            N_RADIUS_PX, 
            O_COLOR,
            oApplet
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticHeight
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the scoreboard object static height that both the client(s) and server should use
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
            CopyOnWriteArrayList<HW02_ObjectCharacter> aoObjectsCharacter;
            String sTextForCharacter;
            int nCellWidth_px;
            int nCellX_px;
            int i;
            
            // Get all character objects
            aoObjectsCharacter = HW02_ObjectCharacter.getCharacterObjects();
            
            /* Sort character objects by GUID
             * They might be out of order if many clients joined the game at nearly the same time
             */
            Collections.sort(aoObjectsCharacter);
            
            // Draw each character's score
            for (i = 0; i < aoObjectsCharacter.size(); i++) {
                
                // Draw table cell
                nCellWidth_px = this.getWidth() / aoObjectsCharacter.size();
                nCellX_px = this.getPositionX() + nCellWidth_px * i;
                this.getApplet().fill(HW02_Color.white());
                this.getApplet().stroke(HW02_Color.black());
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
                this.getApplet().fill(HW02_Color.black());
                this.getApplet().textAlign(PConstants.LEFT, PConstants.TOP);
                this.getApplet().text(
                    sTextForCharacter, 
                    nCellX_px + N_MARGIN_PX, 
                    this.getPositionY() + N_MARGIN_PX
                );
                
            }
            
            // Free
            aoObjectsCharacter = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
}
/*************************************************************************************************************
 * FILE:            HW03_ObjectRenderable.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a game world object that is renderable
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;
import processing.core.PFont;

// CLASS DEFINITION
public class HW03_ObjectRenderable extends HW03_ObjectCollidable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /* Object properties (transient)
     * https://stackoverflow.com/questions/5177013/how-does-marking-a-field-as-transient-make-it-possible-to-serialise-an-object
     */
    private transient PApplet oApplet;
    private transient PFont oFont = null;
    
    // Object properties (other)

    private final static int N_FONT_PX = 12;
    private HW03_Color oColor;
    private boolean bHidden;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectRenderable Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new renderable game world object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     *                  nWidth_px -     The width of the new object
     *                  nHeight_px -    The height of the new object
     *                  nRadius_px -    The corner radius of the new object
     *                  oColor -        The color of the new object
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectRenderable (
        int nExistingGUID,
        float nX_px, 
        float nY_px, 
        int nWidth_px, 
        int nHeight_px, 
        int nRadius_px, 
        HW03_Color oColor,
        PApplet oApplet
    ) {
        
        // Construct located object
        super(nExistingGUID, nX_px, nY_px, nWidth_px, nHeight_px, nRadius_px);
            
        try {
            this.oColor =  oColor;
            this.oApplet = oApplet;
            this.bHidden = false;
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRenderableObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsRenderable -    All renderable objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW03_ObjectRenderable> getRenderableObjects () {
        CopyOnWriteArrayList<HW03_ObjectRenderable> aoObjectsRenderable = new CopyOnWriteArrayList<HW03_ObjectRenderable>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW03_ObjectRenderable) {
                    aoObjectsRenderable.add((HW03_ObjectRenderable) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return aoObjectsRenderable;
    }

    /*********************************************************************************************************
     * FUNCTION:        display
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Display a renderable object onscreen
     *
     * ARGUMENTS:       bSpecial -  True if this is a special object that should be drawn in a special color
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void display (boolean bSpecial) {
        try {
            
            // Skip entirely if the object is temporarily hidden
            if (this.getHiddenFlag() == false) {
                
                // Declare variables
                HW03_Color oColorInverse;
                
                /* Set up font if we haven't already
                 * Can't set this up in constructor
                 * Almost all objects are created by the server
                 * Server doesn't actually draw onscreen, so doesn't have the necessary PApplet
                 */
                if (oFont == null) {
                    oFont = oApplet.createFont("Arial", N_FONT_PX, true);
                    oApplet.textFont(oFont);
                }
                
                // Set the fill color
                if (bSpecial == false) {
                    oApplet.fill(this.oColor.R, this.oColor.G, this.oColor.B);
                }
                else {
                    oColorInverse = HW03_Color.getInverse(this.oColor);
                    oApplet.fill(oColorInverse.R, oColorInverse.G, oColorInverse.B);
                }
                
                // Set the default border to non-existent
                oApplet.noStroke();
                
                /* Draw the object on the screen
                 *  (0,0) is at the TOP LEFT corner of the renderable object
                 *  (0,0) is at the TOP LEFT of the drawing window
                 */
                oApplet.rect(
                    this.getPositionX(), 
                    this.getPositionY(), 
                    this.getWidth(), 
                    this.getHeight(), 
                    this.getRadius()
                );
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticFontSize
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         The default font size
     *********************************************************************************************************/
    public static int getStaticFontSize () {
        return N_FONT_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getColor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oColor - the object's color
     *********************************************************************************************************/
    public HW03_Color getColor () {
        return this.oColor;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setColor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       oColor - the object's color
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setColor (HW03_Color oColor) {
        this.oColor = oColor;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getHiddenFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bHidden -   True if the object is temporarily hidden
     *********************************************************************************************************/
    public boolean getHiddenFlag () {
        return this.bHidden;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setHiddenFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       bHide - True to temporarily hide the object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setHiddenFlag (boolean bHide) {
        this.bHidden = bHide;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getApplet
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oApplet - the object's Processing API applet
     *********************************************************************************************************/
    PApplet getApplet () {
        return this.oApplet;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setApplet
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       oApplet - the object's Processing API applet
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setApplet (PApplet oApplet) {
        this.oApplet = oApplet;
    }

}
package HW01;
/*************************************************************************************************************
 * FILE:            HW01_RenderableObject.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a game world object that is renderable
 *************************************************************************************************************/

// IMPORTS
import java.awt.Rectangle;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW01_RenderableObject extends HW01_GameObject {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    private HW01_Color O_COLOR_SPECIAL = new HW01_Color(20, 0, 190);
    
    /* Object properties (transient)
     * https://stackoverflow.com/questions/5177013/how-does-marking-a-field-as-transient-make-it-possible-to-serialise-an-object
     */
    protected transient PApplet oApplet;
    
    // Object properties (variable)
    protected int nX_px;
    protected int nY_px;
    protected int nWidth_px;
    protected int nHeight_px;
    protected int nRadius_px;
    protected HW01_Color oColor;
    
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_RenderableObject Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new renderable game world object
     *
     * ARGUMENTS:       nNewGUID -      The GUID of the new object
     *                  sObjectType -   "Player", "World", etc.
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
    public HW01_RenderableObject (
        int nNewGUID, 
        String sObjectType,
        int nX_px, 
        int nY_px, 
        int nWidth_px, 
        int nHeight_px, 
        int nRadius_px, 
        HW01_Color oColor,
        PApplet oApplet
    ) {
        
        // Construct generic game world object
        super(nNewGUID, sObjectType);
            
        try {
            this.nX_px =        nX_px;
            this.nY_px =        nY_px;
            this.nWidth_px =    nWidth_px;
            this.nHeight_px =   nHeight_px;
            this.nRadius_px =   nRadius_px;
            this.oColor =       oColor;
            this.oApplet =      oApplet;
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
        
    }

    /*********************************************************************************************************
     * FUNCTION:        display
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Display a renderable object onscreen
     *
     * ARGUMENTS:       bSpecial -  True if this is a special object that should be draw in a special color
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void display (boolean bSpecial) {
        try {
            
            // Set the fill color
            if (bSpecial == false) {
                oApplet.fill(this.oColor.R, this.oColor.G, this.oColor.B);
            }
            else {
                oApplet.fill(O_COLOR_SPECIAL.R, O_COLOR_SPECIAL.G, O_COLOR_SPECIAL.B);
            }
            
            // Set the default border to non-existent
            oApplet.noStroke();
            
            /* Draw the object on the screen
             *  (0,0) is at the TOP LEFT corner of the renderable object
             *  (0,0) is at the TOP LEFT of the drawing window
             */
            oApplet.rect(this.nX_px, this.nY_px, this.nWidth_px, this.nHeight_px, this.nRadius_px);
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPositionX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nX_px - The renderable game object's X position, in pixels
     *********************************************************************************************************/
    int getPositionX () {
        return this.nX_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setPositionX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nX_px - The renderable game object's X position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setPositionX (int nX_px) {
        this.nX_px = nX_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPositionY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nY_px - The renderable game object's Y position, in pixels
     *********************************************************************************************************/
    int getPositionY () {
        return this.nY_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setPositionY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nY_px - The renderable game object's Y position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setPositionY (int nY_px) {
        this.nY_px = nY_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getWidth
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nWidth_px - The renderable game object's width, in pixels
     *********************************************************************************************************/
    int getWidth () {
        return this.nWidth_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getHeight
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nHeight_px - The renderable game object's height, in pixels
     *********************************************************************************************************/
    int getHeight () {
        return this.nHeight_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRadius
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nRadius_px - The renderable game object's radius, in pixels
     *********************************************************************************************************/
    int getRadius () {
        return this.nRadius_px;
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
     * RETURNS:         oColor - The renderable game object's color
     *********************************************************************************************************/
    HW01_Color getColor () {
        return this.oColor;
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
     * RETURNS:         oApplet - The renderable game object's Processing API applet
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
     * ARGUMENTS:       oApplet - The renderable game object's Processing API applet
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setApplet (PApplet oApplet) {
        this.oApplet = oApplet;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        doesObjectCollide
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Determine if this game world object collides with any other game world object
     *                  The homework assignment provides this hint:
     *                      "The java.awt.Shape interface, 
     *                      for which there are many implementing classes including Polygon, Rectangle, etc., 
     *                      provides a .intersects() method. 
     *                      If you convert your Processing shapes into java.awt shapes, 
     *                      collision detection should be straightforward."
     *                  This function uses that hint, but without the use of Processing shapes.
     *
     * ARGUMENTS:       aoGameObjects - A collection of all game objects
     * 
     * RETURNS:         bCollides -     True if the game object collides with some other game object.
     *********************************************************************************************************/
    public boolean doesObjectCollide (CopyOnWriteArrayList<HW01_RenderableObject> aoGameObjects) {
        
        boolean bCollides = false;
        
        try {
            
            // Declare variables
            HW01_RenderableObject oObjectOfComparison;
            Rectangle oObjectShape1;
            Rectangle oObjectShape2;
            int i;
            
            // Assume no collision until we know otherwise
            bCollides = false;
            
            /* Find out whether this game object intersects with any other game object
             *  The choice to use CopyOnWriteArrayList for the collection of game objects 
             *  means that collision detection is not terribly efficient.
             *  This isn't great - we could use a different collection type to get better performance.
             *  However, for the short timeline of a homework assignment, took the easiest route.
             */
            oObjectShape1 = new Rectangle(
                this.getPositionX(), 
                this.getPositionY(), 
                this.getWidth(), 
                this.getHeight()
            );
            for (i = 0; i < aoGameObjects.size(); i++) {
                if (aoGameObjects.get(i).nGUID != this.nGUID) {
                    oObjectOfComparison = aoGameObjects.get(i);
                    oObjectShape2 = new Rectangle(
                        oObjectOfComparison.getPositionX(), 
                        oObjectOfComparison.getPositionY(), 
                        oObjectOfComparison.getWidth(), 
                        oObjectOfComparison.getHeight()
                    );
                    if (oObjectShape1.intersects(oObjectShape2)) {
                        bCollides = true;
                        break;
                    }
                }
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
        return bCollides;
        
    }

}
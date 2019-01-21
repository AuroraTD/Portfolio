/*************************************************************************************************************
 * FILE:            HW03_ObjectStatus.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a status display
 *************************************************************************************************************/

// IMPORTS
import processing.core.PApplet;

// CLASS DEFINITION
public class HW03_ObjectStatus extends HW03_ObjectRenderable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties
    private final static HW03_Color O_COLOR =       new HW03_Color(
                                                        HW03_Color.white(), 
                                                        HW03_Color.white(), 
                                                        HW03_Color.white()
                                                    );
    private final static int N_X_ORIGIN_PX =        0;
    private final static int N_Y_ORIGIN_PX =        0;
    private final static int N_WIDTH_PX =           HW03_Utility.getWindowSize();
    private final static int N_RADIUS_PX =          0;
    private final static int N_MARGIN_PX =          4;
    private final static int N_HEIGHT_PX =          HW03_ObjectRenderable.getStaticFontSize() + N_MARGIN_PX * 2;
    private static int nNumStatusObjects =          0;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectStatus Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new status object
     * 
     * ARGUMENTS:       nExistingGUID -     The GUID of the new object
     *                                      If -1, a new GUID will be automatically assigned
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectStatus (int nExistingGUID, PApplet oApplet) {
        
        // Construct renderable object
        super(
            nExistingGUID,
            N_X_ORIGIN_PX, 
            N_Y_ORIGIN_PX + nNumStatusObjects * N_HEIGHT_PX, 
            N_WIDTH_PX, 
            N_HEIGHT_PX, 
            N_RADIUS_PX, 
            O_COLOR,
            oApplet
        );
        
        // Keep count
        nNumStatusObjects++;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getTotalHeight
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the total height consumed by status objects
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Total height consumed by status objects (pixels)
     *********************************************************************************************************/
    public static int getTotalHeight () {
        return nNumStatusObjects * N_HEIGHT_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticHeight
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the status object static height that both the client(s) and server should use
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Static height (pixels)
     *********************************************************************************************************/
    public static int getStaticHeight () {
        return N_HEIGHT_PX;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStaticMargin
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the status object static margin that descendant classes can easily use
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Static height (pixels)
     *********************************************************************************************************/
    public static int getStaticMargin () {
        return N_MARGIN_PX;
    }
    
}
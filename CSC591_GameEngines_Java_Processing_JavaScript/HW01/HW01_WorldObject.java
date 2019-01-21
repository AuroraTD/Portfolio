package HW01;
/*************************************************************************************************************
 * FILE:            HW01_WorldObject.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a basic game world (non-player) object
 *************************************************************************************************************/

// IMPORTS
import processing.core.PApplet;

// CLASS DEFINITION
public class HW01_WorldObject extends HW01_RenderableObject {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (static)
    final static String S_OBJECT_TYPE = "World";
    final static int N_RADIUS_PX =      2;
    final static HW01_Color O_COLOR =   new HW01_Color(80, 70, 70);
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_WorldObject Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new game world object
     *
     * ARGUMENTS:       nNewGUID -      The GUID of the new object
     *                  nX_px -         The position of the new object on the x axis
     *                  nY_px -         The position of the new object on the y axis
     *                  nWidth_px -     The width of the new object
     *                  nHeight_px -    The height of the new object
     *                  oApplet -       A PApplet object (window) that we want to draw an object onto
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW01_WorldObject (
        int nNewGUID, 
        int nX_px, 
        int nY_px, 
        int nWidth_px, 
        int nHeight_px, 
        PApplet oApplet
    ) {
        
        // Construct renderable object
        super(
            nNewGUID, 
            S_OBJECT_TYPE,
            nX_px, 
            nY_px, 
            nWidth_px, 
            nHeight_px, 
            N_RADIUS_PX, 
            O_COLOR,
            oApplet
        );
        
    }

}
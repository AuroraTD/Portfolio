package HW01;
/*************************************************************************************************************
 * FILE:            HW01_Color.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a basic game object
 *************************************************************************************************************/

// IMPORTS
// None yet

// CLASS DEFINITION
public class HW01_Color implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties
    int R;
    int G;
    int B;
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_Color Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new color object
     * 
     * ARGUMENTS:       R - Red
     *                  G - Green
     *                  B - Blue
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW01_Color (int R, int G, int B) {
        try {
            this.R = R;
            this.G = G;
            this.B = B;
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
    }

}
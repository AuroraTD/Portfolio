/*************************************************************************************************************
 * FILE:            HW03_Color.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a basic game object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;

// CLASS DEFINITION
public class HW03_Color implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties
    int R;
    int G;
    int B;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Color Constructor
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
    public HW03_Color (int R, int G, int B) {
        try {
            this.R = R;
            this.G = G;
            this.B = B;
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRandomShade
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return a random shade of a specified color
     * 
     * ARGUMENTS:       sColor -    "Red", "Green", "Blue", "Yellow", "Purple", "Gray"
     * 
     * RETURNS:         oColor -    Color object
     *********************************************************************************************************/
    public static HW03_Color getRandomShade (String sColor) {
        
        HW03_Color oColor = null;
        try {
            
            // Declare constants
            int N_BASE = 155;
            int N_ADDER = 100;
            
            // Declare variables
            Random oRandomizer;
            int R;
            int G;
            int B;
            
            // Switch
            oRandomizer = new Random();
            switch (sColor) {
                // RED: R high, GB low
                case "Red":
                    R = N_BASE + oRandomizer.nextInt(N_ADDER);
                    G = oRandomizer.nextInt(N_ADDER);
                    B = G;
                    oColor = new HW03_Color(R, G, B);
                    break;
                // GREEN: G high, RB low
                case "Green":
                    R = oRandomizer.nextInt(N_ADDER);
                    G = N_BASE + oRandomizer.nextInt(N_ADDER);
                    B = R;
                    oColor = new HW03_Color(R, G, B);
                    break;
                // BLUE: B high, RG low
                case "Blue":
                    R = oRandomizer.nextInt(N_ADDER);
                    G = R;
                    B = N_BASE + oRandomizer.nextInt(N_ADDER);
                    oColor = new HW03_Color(R, G, B);
                    break;
                // YELLOW: RG high, B low
                case "Yellow":
                    R = N_BASE + oRandomizer.nextInt(N_ADDER);
                    G = R;
                    B = oRandomizer.nextInt(N_ADDER);
                    oColor = new HW03_Color(R, G, B);
                    break;
                // PURPLE: RB high, G low
                case "Purple":
                    R = N_BASE + oRandomizer.nextInt(N_ADDER);
                    G = oRandomizer.nextInt(N_ADDER);
                    B = R;
                    oColor = new HW03_Color(R, G, B);
                    break;
                // GRAY: RGB high
                case "Gray":
                    R = N_BASE + oRandomizer.nextInt(N_ADDER);
                    G = R;
                    B = R;
                    oColor = new HW03_Color(R, G, B);
                    break;
                default:
                    throw new Exception("Do not recognize color name '" + sColor + "'");
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
        // Return
        return oColor;
        
    }

    /*********************************************************************************************************
     * FUNCTION:        getInverse
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return the inverse color
     * 
     * ARGUMENTS:       oColorIn -  A color object
     * 
     * RETURNS:         oColorOut - An inverted color object
     *********************************************************************************************************/
    public static HW03_Color getInverse (HW03_Color oColorIn) {
        
        return new HW03_Color(
            255 - oColorIn.R,
            255 - oColorIn.G,
            255 - oColorIn.B
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        white
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return the RGB value for white
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         The RGB value for white
     *********************************************************************************************************/
    public static int white () {
        return 255;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        black
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return the RGB value for black
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         The RGB value for black
     *********************************************************************************************************/
    public static int black () {
        return 0;
    }

}
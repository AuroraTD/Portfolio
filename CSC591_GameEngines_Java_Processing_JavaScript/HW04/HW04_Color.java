/*************************************************************************************************************
 * FILE:            HW04_Color.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a basic game object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;

// CLASS DEFINITION
public class HW04_Color implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties
    int R;
    int G;
    int B;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Color Constructor
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
    public HW04_Color (int R, int G, int B) {
        try {
            this.R = R;
            this.G = G;
            this.B = B;
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRandomColor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return an object for a pure color (color randomly selected)
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         oColor -    Color object
     *********************************************************************************************************/
    public static HW04_Color getRandomColor () {
        
        HW04_Color oColor = null;
        try {
            
            // Declare variables
            int nRandomNumber;
            
            // Pick a random color
            nRandomNumber = new Random().nextInt(4);
            switch (nRandomNumber) {
                case 0:
                    oColor = getPureColor("Red");
                    break;
                case 1:
                    oColor = getPureColor("Blue");
                    break;
                case 2:
                    oColor = getPureColor("Yellow");
                    break;
                case 3:
                    oColor = getPureColor("Gray");
                    break;
                default:
                    throw new Exception("Do not recognize color index '" + nRandomNumber + "'");
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        
        // Return
        return oColor;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPureColor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return an object for a pure color
     * 
     * ARGUMENTS:       sColor -    "Red", "Green", "Blue", "Yellow", "Gray"
     * 
     * RETURNS:         oColor -    Color object
     *********************************************************************************************************/
    public static HW04_Color getPureColor (String sColor) {
        
        HW04_Color oColor = null;
        try {
            
            // Declare constants
            int N_HIGH =    255;
            int N_MED =     100;
            int N_LOW =     0;
            
            // Switch
            switch (sColor) {
                // RED: R high, GB low
                case "Red":
                    oColor = new HW04_Color(N_HIGH, N_LOW, N_LOW);
                    break;
                // GREEN: G high, RB low
                case "Green":
                    oColor = new HW04_Color(N_LOW, N_HIGH, N_LOW);
                    break;
                // BLUE: B high, RG low
                case "Blue":
                    oColor = new HW04_Color(N_LOW, N_LOW, N_HIGH);
                    break;
                // YELLOW: RG high, B low
                case "Yellow":
                    oColor = new HW04_Color(N_HIGH, N_HIGH, N_LOW);
                    break;
                // GRAY: RGB high
                case "Gray":
                    oColor = new HW04_Color(N_MED, N_MED, N_MED);
                    break;
                default:
                    throw new Exception("Do not recognize color name '" + sColor + "'");
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        
        // Return
        return oColor;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRandomShade
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return a random shade of a specified color
     * 
     * ARGUMENTS:       sColor -    "Red", "Green", "Blue", "Yellow", "Gray"
     * 
     * RETURNS:         oColor -    Color object
     *********************************************************************************************************/
    public static HW04_Color getRandomShade (String sColor) {
        
        HW04_Color oColor = null;
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
                    oColor = new HW04_Color(R, G, B);
                    break;
                // GREEN: G high, RB low
                case "Green":
                    R = oRandomizer.nextInt(N_ADDER);
                    G = N_BASE + oRandomizer.nextInt(N_ADDER);
                    B = R;
                    oColor = new HW04_Color(R, G, B);
                    break;
                // BLUE: B high, RG low
                case "Blue":
                    R = oRandomizer.nextInt(N_ADDER);
                    G = R;
                    B = N_BASE + oRandomizer.nextInt(N_ADDER);
                    oColor = new HW04_Color(R, G, B);
                    break;
                // YELLOW: RG high, B low
                case "Yellow":
                    R = N_BASE + oRandomizer.nextInt(N_ADDER);
                    G = R;
                    B = oRandomizer.nextInt(N_ADDER);
                    oColor = new HW04_Color(R, G, B);
                    break;
                // GRAY: RGB high
                case "Gray":
                    R = N_BASE + oRandomizer.nextInt(N_ADDER);
                    G = R;
                    B = R;
                    oColor = new HW04_Color(R, G, B);
                    break;
                default:
                    throw new Exception("Do not recognize color name '" + sColor + "'");
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
    public static HW04_Color getInverse (HW04_Color oColorIn) {
        
        return new HW04_Color(
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
package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectLocated.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a game world object that has some location
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW02_ObjectLocated extends HW02_ObjectGame {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (variable)
    private int nX_px;
    private int nY_px;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectLocated Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new located game world object
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW02_ObjectLocated (
        int nExistingGUID,
        int nX_px, 
        int nY_px
    ) {
        
        // Construct generic game world object
        super(nExistingGUID);
        
        // Set values
        try {
            this.nX_px = nX_px;
            this.nY_px = nY_px;
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
     * RETURNS:         nX_px - The object's X position, in pixels
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
     *                  Enforce window boundaries as a safeguard
     *
     * ARGUMENTS:       nX_px - The object's X position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setPositionX (int nX_px) {
        try {
            
            // Declare variables
            int nMinX_px;
            int nMaxX_px;

            // Set safeguards
            nMinX_px = 0;
            if (this instanceof HW02_ObjectRenderable) {
                nMaxX_px = 
                    HW02_Utility.getWindowSize() - 
                    ((HW02_ObjectRenderable) this).getWidth();
            }
            else {
                nMaxX_px = HW02_Utility.getWindowSize();
            }
            
            // Set position
            if (nX_px < nMinX_px) {
                this.nX_px = nMinX_px;
            }
            else if (nX_px > nMaxX_px) {
                this.nX_px = nMaxX_px;
            }
            else {
                this.nX_px = nX_px;
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
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
     * RETURNS:         nY_px - The object's Y position, in pixels
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
     *                  Enforce window boundaries as a safeguard
     *
     * ARGUMENTS:       nY_px - The object's Y position, in pixels
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setPositionY (int nY_px) {
        try {

            // Declare variables
            int nMinY_px;
            int nMaxY_px;

            // Set safeguards
            nMinY_px = 0;
            if (this instanceof HW02_ObjectRenderable) {
                nMaxY_px = 
                    HW02_Utility.getWindowSize() - 
                    ((HW02_ObjectRenderable) this).getHeight();
            }
            else {
                nMaxY_px = HW02_Utility.getWindowSize();
            }
            
            // Set position
            if (nY_px < nMinY_px) {
                this.nY_px = nMinY_px;
            }
            else if (nY_px > nMaxY_px) {
                this.nY_px = nMaxY_px;
            }
            else {
                this.nY_px = nY_px;
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }

}
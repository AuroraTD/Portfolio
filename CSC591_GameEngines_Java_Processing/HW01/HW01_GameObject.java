package HW01;
/*************************************************************************************************************
 * FILE:            HW01_GameObject.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a basic game object
 *************************************************************************************************************/

// IMPORTS
// None yet

// CLASS DEFINITION
public class HW01_GameObject implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties
    protected int nGUID;
    protected String sObjectType;
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_GameObject Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new game object
     * 
     * ARGUMENTS:       nNewGUID -          The GUID of the new object
     *                  sNewObjectType -    The type of game object (world object, player object, etc)
     *                                      Purely for convenience of de-serialization
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW01_GameObject (int nNewGUID, String sNewObjectType) {
        try {
            this.nGUID = nNewGUID;
            this.sObjectType = sNewObjectType;
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getGUID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nGUID - The game object's GUID
     *********************************************************************************************************/
    public int getGUID () {
        return this.nGUID;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getType
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         sObjectType - "Player", "World", etc.
     *********************************************************************************************************/
    public String getType () {
        return this.sObjectType;
    }

}
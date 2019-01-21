/*************************************************************************************************************
 * FILE:            HW03_ObjectCollidable.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a collidable game world object
 *************************************************************************************************************/

// IMPORTS
import java.awt.Rectangle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW03_ObjectCollidable extends HW03_ObjectLocated {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (variable)
    private int nWidth_px;
    private int nHeight_px;
    private int nRadius_px;

    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectCollidable Constructor
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
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectCollidable (
        int nExistingGUID,
        float nX_px, 
        float nY_px, 
        int nWidth_px, 
        int nHeight_px, 
        int nRadius_px
    ) {
        
        // Construct located object
        super(nExistingGUID, nX_px, nY_px);
            
        try {
            this.nWidth_px =    nWidth_px;
            this.nHeight_px =   nHeight_px;
            this.nRadius_px =   nRadius_px;
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getCollidableObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsRenderable -    All collidable objects in the game.
     *********************************************************************************************************/
    public static CopyOnWriteArrayList<HW03_ObjectCollidable> getCollidableObjects () {
        CopyOnWriteArrayList<HW03_ObjectCollidable> aoObjectsCollidable = new CopyOnWriteArrayList<HW03_ObjectCollidable>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame = HW03_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW03_ObjectCollidable) {
                    aoObjectsCollidable.add((HW03_ObjectCollidable) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return aoObjectsCollidable;
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
     * RETURNS:         nWidth_px - the object's width, in pixels
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
     * RETURNS:         nHeight_px - the object's height, in pixels
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
     * RETURNS:         nRadius_px - the object's radius, in pixels
     *********************************************************************************************************/
    int getRadius () {
        return this.nRadius_px;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getCollidingObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Determine if this game world object collides with any other game world objects
     *                  The homework assignment provides this hint:
     *                      "The java.awt.Shape interface, 
     *                      for which there are many implementing classes including Polygon, Rectangle, etc., 
     *                      provides a .intersects() method. 
     *                      If you convert your Processing shapes into java.awt shapes, 
     *                      collision detection should be straightforward."
     *                  This function uses that hint, but without the use of Processing shapes.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oCollidedObjects -  The collidable objects that this one collides with.
     *********************************************************************************************************/
    public CopyOnWriteArrayList<HW03_ObjectCollidable> getCollidingObjects () {
        
        CopyOnWriteArrayList<HW03_ObjectCollidable> oCollidedObjects = new CopyOnWriteArrayList<HW03_ObjectCollidable>();
        
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW03_ObjectCollidable> aoObjectsCollidable;
            HW03_ObjectCollidable oObjectOfComparison;
            Rectangle oObjectShape1;
            Rectangle oObjectShape2;
            int i;
            
            /* Find out whether this game object intersects with any other game objects
             *  The choice to use CopyOnWriteArrayList for the collection of game objects 
             *  means that collision detection is not terribly efficient.
             *  This isn't great - we could use a different collection type to get better performance.
             *  However, for the short timeline of a homework assignment, took the easiest route.
             */
            oObjectShape1 = new Rectangle(
                (int) this.getPositionX(), 
                (int) this.getPositionY(), 
                this.getWidth(), 
                this.getHeight()
            );
            aoObjectsCollidable = getCollidableObjects();
            for (i = 0; i < aoObjectsCollidable.size(); i++) {
                if (aoObjectsCollidable.get(i).getGUID() != this.getGUID()) {
                    oObjectOfComparison = (HW03_ObjectCollidable) aoObjectsCollidable.get(i);
                    oObjectShape2 = new Rectangle(
                        (int) oObjectOfComparison.getPositionX(), 
                        (int) oObjectOfComparison.getPositionY(), 
                        oObjectOfComparison.getWidth(), 
                        oObjectOfComparison.getHeight()
                    );
                    if (oObjectShape1.intersects(oObjectShape2)) {
                        oCollidedObjects.add(oObjectOfComparison);
                    }
                }
            }
            
            // Free
            aoObjectsCollidable = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return oCollidedObjects;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        doesObjectCollide
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Determine if this game world object collides with any other game world objects
     *                  Do not return list of colliding objects, so that list may be freed sooner
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         bObjectCollides -  True if the object collides
     *********************************************************************************************************/
    public boolean doesObjectCollide () {
        return this.getCollidingObjects().size() > 0;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        isSpaceOccupied
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Find out if any collidable game objects that exist in the specified space (x,y range)
     *
     * ARGUMENTS:       oObjectsToIgnore -  The objects where we don't care if they are in the space
     *                  nXMin_px -          Left of the space
     *                  nXMax_px -          Right of the space
     *                  nYMin_px -          Top of the space
     *                  nYMax_px -          Bottom of the space
     * 
     * RETURNS:         bOccupied -         True if there are any collidable game objects in the specified space
     *********************************************************************************************************/
    public static boolean isSpaceOccupied (
        CopyOnWriteArrayList<HW03_ObjectCollidable> oObjectsToIgnore,
        float nXMin_px, 
        float nXMax_px, 
        float nYMin_px, 
        float nYMax_px
    ) {
        
        boolean bOccupied = false;
        try {
            
            // Declare variables
            HW03_ObjectCollidable oFakeObject;
            CopyOnWriteArrayList<HW03_ObjectCollidable> aoCollidingObjects;
            boolean bMatchToIgnoredObject;
            int i, j;
            
            // Create fake object to look for collisions
            oFakeObject = new HW03_ObjectCollidable(
                // Use nonsense GUID
                0,
                // X
                nXMin_px, 
                // Y
                nYMin_px, 
                // Width
                (int) (nXMax_px - nXMin_px), 
                // Height
                (int) (nYMax_px - nYMin_px), 
                // Radius
                0
            );
            
            // Find out if this fake object collides with anything we don't specifically want to ignore
            aoCollidingObjects = oFakeObject.getCollidingObjects();
            for (i = 0; i < aoCollidingObjects.size(); i++) {
                bMatchToIgnoredObject = false;
                for (j = 0; j < oObjectsToIgnore.size(); j++) {
                    if (aoCollidingObjects.get(i).getGUID() == oObjectsToIgnore.get(j).getGUID()) {
                        bMatchToIgnoredObject = true;
                        break;
                    }
                }
                if (bMatchToIgnoredObject == false) {
                    bOccupied = true;
                    break;
                }
            }
            
            // Remove fake object
            HW03_ObjectGame.removeObjectByGUID(oFakeObject.getGUID());
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return bOccupied;
    }

}
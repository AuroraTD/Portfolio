package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectMoveable.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a moveable object in the game world
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public abstract class HW02_ObjectMoveable extends HW02_ObjectRenderable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (variable)
    private int nSpeedHorizontal;
    private int nSpeedVertical;
    
    // Declare abstract methods
    abstract void handleCollision(
        CopyOnWriteArrayList<HW02_ObjectCollidable> oCollidingObjects,
        boolean bYAxis
    );
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectMoveable Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new moveable object
     *
     * ARGUMENTS:       nExistingGUID -     The GUID of the new object
     *                                      If -1, a new GUID will be automatically assigned
     *                  nX_px -             The position of the new object on the x axis (0 = left)
     *                  nY_px -             The position of the new object on the y axis (0 = top)
     *                  nWidth_px -         The width of the new object
     *                  nHeight_px -        The height of the new object
     *                  nRadius_px -        The corner radius of the new object
     *                  oColor -            The color of the new object
     *                  oApplet -           A PApplet object (window) that we want to draw an object onto
     *                  nSpeedDefaultH -    The default speed (pixels per game loop iteration)
     *                                      that the object moves along the horizontal axis
     *                                      Negative number = left
     *                                      Positive number = right
     *                                      Zero = stationary
     *                  nSpeedDefaultV -    The default speed (pixels per game loop iteration)
     *                                      that the object moves along the vertical axis
     *                                      Negative number = down
     *                                      Positive number = up
     *                                      Zero = stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW02_ObjectMoveable (
        int nExistingGUID,
        int nX_px, 
        int nY_px, 
        int nWidth_px,
        int nHeight_px,
        int nRadius_px,
        HW02_Color oColor,
        PApplet oApplet,
        int nSpeedDefaultH,
        int nSpeedDefaultV
    ) {
        
        // Construct renderable object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            nWidth_px, 
            nHeight_px, 
            nRadius_px, 
            oColor,
            oApplet
        );
        
        try {
            this.setSpeedHorizontal(nSpeedDefaultH);
            this.setSpeedVertical(nSpeedDefaultV);
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getSpeedHorizontal
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nSpeedHorizontal -  The speed (pixels per game loop iteration)
     *                                      that the object moves along the horizontal axis
     *                                      Negative number = left
     *                                      Positive number = right
     *                                      Zero = stationary
     *********************************************************************************************************/
    int getSpeedHorizontal () {
        return this.nSpeedHorizontal;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setSpeedHorizontal
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *                  Enforce speed limit
     *                  Do not allow a game object to move faster 
     *                  than it's size in the axis along which it is moving.
     *                  If that happens, we could end up jumping "through" another object
     *                  and collision detection falls apart!
     *
     * ARGUMENTS:       nSpeedHorizontal -  The speed (pixels per game loop iteration)
     *                                      that the object moves along the horizontal axis
     *                                      Negative number = left
     *                                      Positive number = right
     *                                      Zero = stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setSpeedHorizontal (int nSpeedHorizontal) {
        try {
            
            if (nSpeedHorizontal > 0 && nSpeedHorizontal >= this.getWidth()) {
                this.nSpeedHorizontal = this.getWidth() - 1;
            }
            else if (nSpeedHorizontal < 0 && nSpeedHorizontal <= (this.getWidth() * -1)) {
                this.nSpeedHorizontal = (this.getWidth() * -1) + 1;
            }
            else {
                this.nSpeedHorizontal = nSpeedHorizontal;
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getSpeedVertical
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nSpeedVertical -    The speed (pixels per game loop iteration)
     *                                      that the object moves along the vertical axis
     *                                      Negative number = down
     *                                      Positive number = up
     *                                      Zero = stationary
     *********************************************************************************************************/
    int getSpeedVertical () {
        return this.nSpeedVertical;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setSpeedVertical
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *                  Enforce speed limit
     *                  Do not allow a game object to move faster 
     *                  than it's size in the axis along which it is moving.
     *                  If that happens, we could end up jumping "through" another object
     *                  and collision detection falls apart!
     *
     * ARGUMENTS:       nSpeedVertical -    The speed (pixels per game loop iteration)
     *                                      that the object moves along the vertical axis
     *                                      Negative number = down
     *                                      Positive number = up
     *                                      Zero = stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setSpeedVertical (int nSpeedVertical) {
        try {
            
            if (nSpeedVertical > 0 && nSpeedVertical >= this.getHeight()) {
                this.nSpeedVertical = this.getHeight() - 1;
            }
            else if (nSpeedVertical < 0 && nSpeedVertical <= (this.getHeight() * -1)) {
                this.nSpeedVertical = (this.getHeight() * -1) + 1;
            }
            else {
                this.nSpeedVertical = nSpeedVertical;
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        updateLocation
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Move an object, and react if you hit another object
     *                  Takes care of one axis at a time and this is important
     *                  If we move along both axes, then try to handle collisions,
     *                  it becomes confusing as to how to handle them
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void updateLocation () {
        
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW02_ObjectCollidable> oCollidingObjects;
            
            // Move object along Y axis (0,0 is top left of window, positive speed is UP)
            this.setPositionY(this.getPositionY() - this.getSpeedVertical());
            
            // React if you hit another object
            oCollidingObjects = this.getCollidingObjects();
            if (oCollidingObjects.size() > 0) {
                this.handleCollision(oCollidingObjects, true);
            }
            
            // Move object along X axis (0,0 is top left of window, positive speed is RIGHT)
            this.setPositionX(this.getPositionX() + this.getSpeedHorizontal());
            
            // React if you hit another object
            oCollidingObjects = this.getCollidingObjects();
            if (oCollidingObjects.size() > 0) {
                this.handleCollision(oCollidingObjects, false);
            }
            
            // Free
            oCollidingObjects = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        
    }
  
  /*********************************************************************************************************
  * FUNCTION:        reverseCourse
  *
  * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
  *
  * DESCRIPTION:     Reverse course
  *
  * ARGUMENTS:       None
  * 
  * RETURNS:         None
  *********************************************************************************************************/
  public void reverseCourse () {
     
     try {
         
         // Reverse direction on both axes
         this.setSpeedHorizontal(this.getSpeedHorizontal() * -1);
         this.setSpeedVertical(this.getSpeedVertical() * -1);
         
     }
     catch (Throwable oError) {
         HW02_Utility.handleError(oError);
     }
     
 }
  
  /*********************************************************************************************************
  * FUNCTION:        stopMoving
  *
  * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
  *
  * DESCRIPTION:     Stop motion of this object
  *
  * ARGUMENTS:       None
  * 
  * RETURNS:         None
  *********************************************************************************************************/
  public void stopMoving () {
     
     try {
         
         // Stop all motion
         this.setSpeedHorizontal(0);
         this.setSpeedVertical(0);
         
     }
     catch (Throwable oError) {
         HW02_Utility.handleError(oError);
     }
     
 }
  
  /*********************************************************************************************************
  * FUNCTION:        backOutOfCollisionY
  *
  * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
  *
  * DESCRIPTION:     Back up until no longer collided with another object
  *                  Give up and stay where you are if you try a huge number of times and cannot succeed
  *                  This is purely to support extreme cases caused by performance testing
  *                  (hundreds of objects on the screen all moving around as fast as they can)
  *
  * ARGUMENTS:       None
  * 
  * RETURNS:         None
  *********************************************************************************************************/
  public void backOutOfCollisionY () {
     
     try {
         
         // Declare constants
         final int N_MAX_ATTEMPTS = 100;
         
         // Declare variables
         int nAdjustY_px;
         int nNumAttempts;
         
         // Back up until collision is avoided
         nNumAttempts = 0;
         while (this.doesObjectCollide() == true && nNumAttempts <= N_MAX_ATTEMPTS) {
             if (this.getSpeedVertical() != 0) {
                 // If you were going up, move down, and vice versa
                 nAdjustY_px = (this.getSpeedVertical() > 0) ? 1 : -1;
                 this.setPositionY(this.getPositionY() + nAdjustY_px);
             }
             nNumAttempts++;
         }
         
     }
     catch (Throwable oError) {
         HW02_Utility.handleError(oError);
     }
     
 }
  
  /*********************************************************************************************************
  * FUNCTION:        backOutOfCollisionX
  *
  * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
  *
  * DESCRIPTION:     Back up until no longer collided with another object
  *                  This is purely to support extreme cases caused by performance testing
  *                  (hundreds of objects on the screen all moving around as fast as they can)
  *
  * ARGUMENTS:       None
  * 
  * RETURNS:         None
  *********************************************************************************************************/
  public void backOutOfCollisionX () {
     
     try {
         
         // Declare constants
         final int N_MAX_ATTEMPTS = 100;
         
         // Declare variables
         int nAdjustX_px;
         int nNumAttempts;
         
         // Back up until collision is avoided
         nNumAttempts = 0;
         while (this.doesObjectCollide() == true && nNumAttempts <= N_MAX_ATTEMPTS) {
             if (this.getSpeedHorizontal() != 0) {
                 // If you were going left, move right, and vice versa
                 nAdjustX_px = (this.getSpeedHorizontal() > 0) ? -1 : 1;
                 this.setPositionX(this.getPositionX() + nAdjustX_px);
             }
             nNumAttempts++;
         }
         
     }
     catch (Throwable oError) {
         HW02_Utility.handleError(oError);
     }
     
 }

}
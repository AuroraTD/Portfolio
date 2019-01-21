/*************************************************************************************************************
 * FILE:            HW03_ObjectMoveable.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a moveable object in the game world
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public abstract class HW03_ObjectMoveable extends HW03_ObjectRenderable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Object properties (variable)
    private int nSpeedHorizontal_px_per_sec;
    private int nSpeedVertical_px_per_sec;
    
    // Declare abstract methods
    abstract void handleCollision(
        CopyOnWriteArrayList<HW03_ObjectCollidable> oCollidingObjects,
        float nOriginalX,
        float nOriginalY
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
    public HW03_ObjectMoveable (
        int nExistingGUID,
        float nX_px, 
        float nY_px, 
        int nWidth_px,
        int nHeight_px,
        int nRadius_px,
        HW03_Color oColor,
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
            HW03_Utility.handleError(oError);
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
     * RETURNS:         nSpeedHorizontal_px_per_sec -  The speed (pixels per game loop iteration)
     *                                      that the object moves along the horizontal axis
     *                                      Negative number = left
     *                                      Positive number = right
     *                                      Zero = stationary
     *********************************************************************************************************/
    int getSpeedHorizontal () {
        return this.nSpeedHorizontal_px_per_sec;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setSpeedHorizontal
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nSpeedHorizontal_px_per_sec -   The speed (pixels per game loop iteration)
     *                                                  that the object moves along the horizontal axis
     *                                                  Negative number = left
     *                                                  Positive number = right
     *                                                  Zero = stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setSpeedHorizontal (int nSpeedHorizontal_px_per_sec) {
        try {
            
            this.nSpeedHorizontal_px_per_sec = nSpeedHorizontal_px_per_sec;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
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
     * RETURNS:         nSpeedVertical_px_per_sec -    The speed (pixels per game loop iteration)
     *                                      that the object moves along the vertical axis
     *                                      Negative number = down
     *                                      Positive number = up
     *                                      Zero = stationary
     *********************************************************************************************************/
    int getSpeedVertical () {
        return this.nSpeedVertical_px_per_sec;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setSpeedVertical
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     *
     * ARGUMENTS:       nSpeedVertical_px_per_sec - The speed (pixels per game loop iteration)
     *                                              that the object moves along the vertical axis
     *                                              Negative number = down
     *                                              Positive number = up
     *                                              Zero = stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    void setSpeedVertical (int nSpeedVertical_px_per_sec) {
        try {
            
            this.nSpeedVertical_px_per_sec = nSpeedVertical_px_per_sec;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
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
            CopyOnWriteArrayList<HW03_ObjectCollidable> oCollidingObjects;
            float nOriginalX;
            float nOriginalY;
            
            // Get original location
            nOriginalX = this.getPositionX();
            nOriginalY = this.getPositionY();
            
            // Move object along both axes
            this.setPositionX(this.getFuturePositionX());
            this.setPositionY(this.getFuturePositionY());
            
            // React if you hit another object
            oCollidingObjects = this.getCollidingObjects();
            if (oCollidingObjects.size() > 0) {
                HW03_EventManager.getInstance().raiseEventCollision(this, oCollidingObjects, nOriginalX, nOriginalY);
            }
            
            // Free
            oCollidingObjects = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getFuturePositionY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the future (next update) Y location of the object
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nFuturePositionY -  The Y location that the object should be at 
     *                                      after the next location update
     *********************************************************************************************************/
    public float getFuturePositionY () {
        float nFuturePositionY = -1;
        try {
            
            // Declare constants
            final int N_MS_PER_SEC = 1000;
            final double N_TIME_SINCE_LAST_UPDATE_SEC = 
                (double) HW03_Time_Loop.getPlayInstance().getTimeDelta() * 
                (double) HW03_Time_Loop.getPlayInstance().getTickSize() / 
                (double) N_MS_PER_SEC;
            
            // Declare variables
            float nVerticalMovement_px;
            
            /* Move object along Y axis 
             * (0,0) is top left of window, positive speed is UP
             * How much we move an object depends on speed (px / sec) and on time since last update (sec)
             * Enforce speed limit: 
             *      Do not allow a game object to move faster 
             *      than it's size in the axis along which it is moving.
             *      If that happens, we could end up jumping "through" another object
             *      and collision detection falls apart!
             */
            nVerticalMovement_px = (float) (
                Math.abs(this.getSpeedVertical()) * 
                N_TIME_SINCE_LAST_UPDATE_SEC
            );
            if (nVerticalMovement_px >= this.getHeight()) {
                nVerticalMovement_px = this.getHeight() - 1;
            }
            nFuturePositionY = (float) (
                this.getPositionY() + 
                nVerticalMovement_px * 
                (this.getSpeedVertical() < 0 ? 1 : -1)
            );
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nFuturePositionY;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getFuturePositionX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the future (next update) X location of the object
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         nFuturePositionX -  The X location that the object should be at 
     *                                      after the next location update
     *********************************************************************************************************/
    public float getFuturePositionX () {
        float nFuturePositionX = -1;
        try {
            
            // Declare constants
            final int N_MS_PER_SEC = 1000;
            final double N_TIME_SINCE_LAST_UPDATE_SEC = 
                (double) HW03_Time_Loop.getPlayInstance().getTimeDelta() * 
                (double) HW03_Time_Loop.getPlayInstance().getTickSize() / 
                (double) N_MS_PER_SEC;
            
            // Declare variables
            float nHorizontalMovement_px;
            
            /* Move object along Y axis 
             * (0,0) is top left of window, positive speed is UP
             * How much we move an object depends on speed (px / sec) and on time since last update (sec)
             * Enforce speed limit: 
             *      Do not allow a game object to move faster 
             *      than it's size in the axis along which it is moving.
             *      If that happens, we could end up jumping "through" another object
             *      and collision detection falls apart!
             */
            nHorizontalMovement_px = (float) (
                Math.abs(this.getSpeedHorizontal()) *
                N_TIME_SINCE_LAST_UPDATE_SEC
            );
            if (nHorizontalMovement_px >= this.getWidth()) {
                nHorizontalMovement_px = this.getWidth() - 1;
            }
            nFuturePositionX = (float) (
                this.getPositionX() + 
                nHorizontalMovement_px * (this.getSpeedHorizontal() < 0 ? -1 : 1)
            );
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nFuturePositionX;
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
            HW03_Utility.handleError(oError);
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
            HW03_Utility.handleError(oError);
        }
     
    }
  
    /*********************************************************************************************************
     * FUNCTION:        backOutOfCollision
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Back up until no longer collided with another object
     *                  Show preference to backing out along the Y-axis (gravity is a major factor)
     *
     * ARGUMENTS:       nOriginalX -   X-axis location before the move that caused the collision
     *                  nOriginalY -   Y-axis location before the move that caused the collision
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void backOutOfCollision (float nOriginalX, float nOriginalY) {
         
        try {
            
            // Declare constants
            final int N_MAX_ATTEMPTS =  100;
                 
            // Declare variables
            boolean bGoingUpBackingDown;
            boolean bGoingDownBackingUp;
            boolean bGoingRightBackingLeft;
            boolean bGoingLeftBackingRight;
            int nNumAttempts;
            int i;
            Random oRandom;
            CopyOnWriteArrayList<HW03_ObjectCollidable> oCollidedObjects;
            HW03_ObjectCollidable oSomeCollidedObject;
            
            // Get randomizer
            oRandom = new Random();
             
            // Which way are we headed?
            bGoingUpBackingDown = this.getSpeedVertical() > 0;
            bGoingDownBackingUp = this.getSpeedVertical() < 0;
            bGoingRightBackingLeft = this.getSpeedHorizontal() > 0;
            bGoingLeftBackingRight = this.getSpeedHorizontal() < 0;
            
            // Initialize some stuff used by the loop
            nNumAttempts = 0;
            
            // Keep trying until we are backed out of all collisions or it has taken too long
            while (true) {
                
                // Get a collection of all objects with whom we are collided
                oCollidedObjects = this.getCollidingObjects();
                
                // See if it's time to stop
                if (
                    oCollidedObjects.size() == 0 ||
                    nNumAttempts >= N_MAX_ATTEMPTS
                ) {
                    break;
                }
                else {
                    
                    // Get a randomly selected collided object
                    oSomeCollidedObject = oCollidedObjects.get(oRandom.nextInt(oCollidedObjects.size()));
                    
                    // If it's a platform, back out along Y axis if possible
                    if (oSomeCollidedObject instanceof HW03_ObjectPlatform) {
                        if (bGoingDownBackingUp == true) {
                            backAwayFromObjectY(oSomeCollidedObject, true);
                        }
                        else if (bGoingUpBackingDown == true) {
                            backAwayFromObjectY(oSomeCollidedObject, false);
                        }
                        else if (bGoingRightBackingLeft == true) {
                            backAwayFromObjectX(oSomeCollidedObject, true);
                        }
                        else if (bGoingLeftBackingRight == true) {
                            backAwayFromObjectX(oSomeCollidedObject, false);
                        }
                    }
                    
                    // If it's a boundary, back out along X axis if possible
                    else if (oSomeCollidedObject instanceof HW03_ObjectBoundary) {
                        if (bGoingRightBackingLeft == true) {
                            backAwayFromObjectX(oSomeCollidedObject, true);
                        }
                        else if (bGoingLeftBackingRight == true) {
                            backAwayFromObjectX(oSomeCollidedObject, false);
                        }
                        else if (bGoingDownBackingUp == true) {
                            backAwayFromObjectY(oSomeCollidedObject, true);
                        }
                        else if (bGoingUpBackingDown == true) {
                            backAwayFromObjectY(oSomeCollidedObject, false);
                        }
                    }
                    
                    // Otherwise, back out along randomly selected axis
                    else if (oRandom.nextInt(2) < 1) {
                        if (bGoingRightBackingLeft == true) {
                            backAwayFromObjectX(oSomeCollidedObject, true);
                        }
                        else if (bGoingLeftBackingRight == true) {
                            backAwayFromObjectX(oSomeCollidedObject, false);
                        }
                    }
                    else {
                        if (bGoingDownBackingUp == true) {
                            backAwayFromObjectY(oSomeCollidedObject, true);
                        }
                        else if (bGoingUpBackingDown == true) {
                            backAwayFromObjectY(oSomeCollidedObject, false);
                        }
                    }

                    // Increment counter
                    nNumAttempts++;
                    
                }
                
            }
                 
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
         
    }
    
    /*********************************************************************************************************
     * FUNCTION:        backAwayFromObjectX
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Back away from a given object along the X axis
     *
     * ARGUMENTS:       nObject -   The object to back away from
     *                  bGoToTop -     True if we should back up by going LEFT
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void backAwayFromObjectX (HW03_ObjectCollidable oObject, boolean bGoToLeft) {
         
        try {
            
            // Declare constants
            final float N_SCOOCH_PX = (float) 0.2;
            
            // Declare variables
            float nJumpToX_px;
            
            if (bGoToLeft == true) {
                nJumpToX_px = 
                    oObject.getPositionX() - 
                    this.getWidth() -
                    N_SCOOCH_PX;
                this.setPositionX(nJumpToX_px);
            }
            else {
                nJumpToX_px = 
                    oObject.getPositionX() + 
                    oObject.getWidth() +
                    N_SCOOCH_PX;
                this.setPositionX(nJumpToX_px);
            }
                 
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
         
    }
    
    /*********************************************************************************************************
     * FUNCTION:        backAwayFromObjectY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Back away from a given object along the Y axis
     *
     * ARGUMENTS:       nObject -   The object to back away from
     *                  bGoToTop -     True if we should back up by going UP
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void backAwayFromObjectY (HW03_ObjectCollidable oObject, boolean bGoToTop) {
         
        try {
            
            // Declare constants
            final float N_SCOOCH_PX = (float) 0.2;
            
            // Declare variables
            float nJumpToY_px;
            
            if (bGoToTop == false) {
                nJumpToY_px = 
                    oObject.getPositionY() + 
                    oObject.getHeight() +
                    N_SCOOCH_PX;
                this.setPositionY(nJumpToY_px);
            }
            else {
                nJumpToY_px = 
                    oObject.getPositionY() - 
                    this.getHeight() -
                    N_SCOOCH_PX;
                this.setPositionY(nJumpToY_px);
            }
                 
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
         
    }

}
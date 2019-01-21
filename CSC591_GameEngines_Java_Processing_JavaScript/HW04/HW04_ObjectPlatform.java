/*************************************************************************************************************
 * FILE:            HW04_Platform.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a platform object
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// CLASS DEFINITION
public abstract class HW04_ObjectPlatform extends HW04_ObjectMoveable {
    
    // Required for serializable class
    private static final long               serialVersionUID =  1L;
    
    // Constants
    private static final HW04_EventManager  O_EVENT_MANAGER =   HW04_EventManager.getInstance();
    private final static int                N_RADIUS_PX =       2;

    /*********************************************************************************************************
     * FUNCTION:        HW04_Platform Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new platform object
     * 
     * ARGUMENTS:       nExistingGUID -             The GUID of the new object
     *                                              If -1, a new GUID will be automatically assigned
     *                  nX_px -                     The position of the new object on the x axis (0 = left)
     *                  nY_px -                     The position of the new object on the y axis (0 = top)
     *                  nWidth_px -                 The width of the new object
     *                  nHeight_px -                The height of the new object
     *                  oColor -                    The color of the new object
     *                  oApplet -                   A PApplet object (window) that we want to draw an object onto
     *                  nSpeedDefaultH_px_per_sec - The default speed
     *                                              that the object moves along the horizontal axis
     *                                              Negative number = left
     *                                              Positive number = right
     *                                              Zero = stationary
     *                  nSpeedDefaultV_px_per_sec - The default speed
     *                                              that the object moves along the vertical axis
     *                                              Negative number = down
     *                                              Positive number = up
     *                                              Zero = stationary
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectPlatform (
        int nExistingGUID,
        int nX_px,
        int nY_px,
        int nWidth_px,
        int nHeight_px,
        HW04_Color oColor,
        PApplet oApplet,
        int nSpeedDefaultH_px_per_sec,
        int nSpeedDefaultV_px_per_sec
    ) {
        
        // Construct moveable object
        super(
            nExistingGUID,
            nX_px, 
            nY_px, 
            nWidth_px, 
            nHeight_px, 
            N_RADIUS_PX, 
            oColor,
            oApplet,
            nSpeedDefaultH_px_per_sec,
            nSpeedDefaultV_px_per_sec
        );
        
    }

    /*********************************************************************************************************
     * FUNCTION:        addPlatformObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add platform objects to the game world
     *                  Place these objects semi-randomly
     *                  Static platforms in particular also serve as spawn points
     *                      so don't draw them so close to the top of the window 
     *                      that no character could possibly fit on top
     *                  Character cougetInstanceld also get pushed onto some other platform
     *                      so just go ahead and use this rule for all platforms
     *                  Also, since static platforms serve as spawn points,
     *                      Make sure (if there are few of them) that they are wide enough
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addPlatformObjects () {
        try {
            
            // Declare constants
            
            final int N_WINDOW_AREA_PX =        HW04_Utility.getWindowSize() * HW04_Utility.getWindowSize();
            final int N_PLATFORM_AREA_PX =      (int) ( ((double) N_WINDOW_AREA_PX) / 5);
            
            final int N_PLATFORMS_STATIC =      3;
            final int N_PLATFORMS_DYNAMIC =     3;
            final int N_PLATFORMS_TOTAL_PX =    N_PLATFORMS_STATIC + N_PLATFORMS_DYNAMIC;
            
            final int N_MAX_LENGTH_GUESS =      Math.max(2, (int) Math.sqrt( ((double) N_PLATFORM_AREA_PX) / ((double) N_PLATFORMS_TOTAL_PX) ));
            
            final int N_MIN_WIDTH_STATIC_PX =   (N_PLATFORMS_STATIC < 10 ? (HW04_ObjectCharacter.getStaticHeight() * 3) : 1);
            final int N_MIN_WIDTH_MOVING_PX =   1;
            
            final int N_MAX_WIDTH_STATIC_PX =   Math.max(N_MAX_LENGTH_GUESS, N_MIN_WIDTH_STATIC_PX) + 1;
            final int N_MAX_WIDTH_MOVING_PC =   Math.max(N_MAX_LENGTH_GUESS, N_MIN_WIDTH_MOVING_PX) + 1;
            
            final int N_MIN_HEIGHT_STATIC_PX =  1;
            final int N_MIN_HEIGHT_MOVING_PX =  1;
            
            final int N_MAX_HEIGHT_STATIC_PX =  Math.max(N_MAX_LENGTH_GUESS, N_MIN_HEIGHT_STATIC_PX) + 1;
            final int N_MAX_HEIGHT_MOVING_PX =  Math.max(N_MAX_LENGTH_GUESS, N_MIN_HEIGHT_MOVING_PX) + 1;
            
            // Declare variables
            Random oRandomizer;
            HW04_ObjectPlatformStatic oPlatformStatic;
            HW04_ObjectPlatformH oPlatformH;
            HW04_ObjectPlatformV oPlatformV;
            int nX_px;
            int nY_px;
            int i;
            
            // Get randomizer
            oRandomizer = new Random();
            
            // STATIC
            for (i = 0; i < N_PLATFORMS_STATIC; i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW04_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformStatic = new HW04_ObjectPlatformStatic(
                    // Get auto GUID
                    -1,
                    nX_px,
                    nY_px,
                    (oRandomizer.nextInt(N_MAX_WIDTH_STATIC_PX - N_MIN_WIDTH_STATIC_PX) + N_MIN_WIDTH_STATIC_PX),
                    (oRandomizer.nextInt(N_MAX_HEIGHT_STATIC_PX - N_MIN_HEIGHT_STATIC_PX) + N_MIN_HEIGHT_STATIC_PX),
                    null
                );
                
                // Move it if it's in collision with some other object
                while (oPlatformStatic.doesObjectCollide() == true) {
                    oPlatformStatic.setPositionX(oRandomizer.nextInt(HW04_Utility.getWindowSize()));
                    oPlatformStatic.setPositionY(getRandomPlatformY());
                }
                
            }
            
            // HORIZONTAL
            for (i = 0; i < (N_PLATFORMS_DYNAMIC / 2); i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW04_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformH = new HW04_ObjectPlatformH(
                    // Get auto GUID
                    -1,
                    nX_px,
                    nY_px,
                    (oRandomizer.nextInt(N_MAX_WIDTH_MOVING_PC - N_MIN_WIDTH_MOVING_PX) + N_MIN_WIDTH_MOVING_PX),
                    ((oRandomizer.nextInt(N_MAX_HEIGHT_MOVING_PX - N_MIN_HEIGHT_MOVING_PX) + N_MIN_HEIGHT_MOVING_PX) / 2),
                    null
                );
                
                // Move it if it's in collision with some other object
                while (oPlatformH.doesObjectCollide() == true) {
                    oPlatformH.setPositionX(oRandomizer.nextInt(HW04_Utility.getWindowSize()));
                    oPlatformH.setPositionY(getRandomPlatformY());
                }
                
                // Register interest in events that affect the object
                O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.COLLISION, oPlatformH, false);
                
            }
            
            // VERTICAL
            for (i = 0; i < (N_PLATFORMS_DYNAMIC / 2) + (N_PLATFORMS_DYNAMIC % 2); i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW04_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformV = new HW04_ObjectPlatformV(
                    // Get auto GUID
                    -1,
                    nX_px,
                    nY_px,
                    ((oRandomizer.nextInt(N_MAX_WIDTH_MOVING_PC - N_MIN_WIDTH_MOVING_PX) + N_MIN_WIDTH_MOVING_PX) / 2),
                    (oRandomizer.nextInt(N_MAX_HEIGHT_MOVING_PX - N_MIN_HEIGHT_MOVING_PX) + N_MIN_HEIGHT_MOVING_PX),
                    null
                );
                
                // Move it if it's in collision with some other object
                while (oPlatformV.doesObjectCollide() == true) {
                    oPlatformV.setPositionX(oRandomizer.nextInt(HW04_Utility.getWindowSize()));
                    oPlatformV.setPositionY(getRandomPlatformY());
                }
                
                // Register interest in events that affect the object
                O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.COLLISION, oPlatformV, false);
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRandomPlatformY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get a random Y location for a platform
     *                  Static platforms in particular also serve as spawn points
     *                      so don't draw them so close to the top of the window 
     *                      that no character could possibly fit on top
     *                  Character could also get pushed onto some other platform
     *                      so just go ahead and use this rule for all platforms
     *
     * ARGUMENTS:       nY_px - A semi-random Y location for a platform object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private static int getRandomPlatformY () {
        
        int nY_px = -1;
        try {
            
            // Declare constants
            final int N_CHARACTER_HEIGHT_PX = HW04_ObjectCharacter.getStaticHeight();
            final int N_STATUS_OBJECTS_HEIGHT_PX = HW04_ObjectStatus.getTotalHeight();
            final int N_MIN_Y_PX = (N_CHARACTER_HEIGHT_PX * 4) + N_STATUS_OBJECTS_HEIGHT_PX;
            
            // Declare variables
            Random oRandomizer;
            
            // Get randomizer
            oRandomizer = new Random();
            
            // Calculate
            nY_px = N_MIN_Y_PX + oRandomizer.nextInt(HW04_Utility.getWindowSize() - N_MIN_Y_PX);
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        return nY_px;
        
    }
  
    /*********************************************************************************************************
     * FUNCTION:        movePlatforms
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Update all connected clients about what's going on with platforms,
     *                  and then move them around.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void movePlatforms () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectPlatformH> aoObjectsPlatformH = null;
            CopyOnWriteArrayList<HW04_ObjectPlatformV> aoObjectsPlatformV = null;
            int i;
            
            // Get object collections
            aoObjectsPlatformH = HW04_ObjectPlatformH.getHorizontalPlatformObjects();
            aoObjectsPlatformV = HW04_ObjectPlatformV.getVerticalPlatformObjects();
            
            if (
                aoObjectsPlatformH != null &&
                aoObjectsPlatformV != null
            ) {
                
                /* Send updates to clients
                 *  Do this before moving objects for which the server is responsible
                 *  Why? Moving platforms may cause collision events which need to be handled
                 *  It's better not to send collided / funky objects out
                 */
                HW04_Server.notifyClientsAboutManyObjects(-1, aoObjectsPlatformH);
                HW04_Server.notifyClientsAboutManyObjects(-1, aoObjectsPlatformV);
                
                /* Move platforms
                 * Do this after sending updates to clients
                 *  Why? Moving platforms may cause collision events which need to be handled
                 *  It's better not to send collided / funky objects out
                 */
                for (i = 0; i < aoObjectsPlatformH.size(); i++) {
                    aoObjectsPlatformH.get(i).updateLocationAndCheckForCollisions();
                }
                for (i = 0; i < aoObjectsPlatformV.size(); i++) {
                    aoObjectsPlatformV.get(i).updateLocationAndCheckForCollisions();
                }
                
            }
            
            // Free
            aoObjectsPlatformH = null;
            aoObjectsPlatformV = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
  
}
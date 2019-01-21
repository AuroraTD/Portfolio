/*************************************************************************************************************
 * FILE:            HW04_ObjectSpawnPoint.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a spawn point in the game world
 *************************************************************************************************************/

// IMPORTS
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW04_ObjectSpawnPoint extends HW04_ObjectLocated {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectSpawnPoint Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new spawn point
     * 
     * ARGUMENTS:       nExistingGUID - The GUID of the new object
     *                                  If -1, a new GUID will be automatically assigned
     *                  nX_px -         The position of the new object on the x axis (0 = left)
     *                  nY_px -         The position of the new object on the y axis (0 = top)
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectSpawnPoint (
        int nExistingGUID,
        float nX_px, 
        float nY_px
    ) {
        // Construct located object
        super(nExistingGUID, nX_px, nY_px);
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addSpawnPointToEveryStaticPlatform
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add spawn points to the game world.
     *                  A spawn point is added just above each stationary platform object,
     *                  so that characters will spawn standing on a stationary platform.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addSpawnPointToEveryStaticPlatform () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectPlatformStatic> aoObjectsPlatformStatic;
            int i;
            
            // Create spawn points
            aoObjectsPlatformStatic = HW04_ObjectPlatformStatic.getStaticPlatformObjects();
            for (i = 0; i < aoObjectsPlatformStatic.size(); i++) {
                
                // Add a spawn point on TOP of each static platform
                new HW04_ObjectSpawnPoint(
                    // Get auto GUID
                    -1,
                    aoObjectsPlatformStatic.get(i).getPositionX(),
                    aoObjectsPlatformStatic.get(i).getPositionY() - 1 - HW04_ObjectCharacter.getStaticHeight()
                );
                
            }
            
            // Free
            aoObjectsPlatformStatic = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addSpawnPointAtBottomOfWindow
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add a single spawn point to the game world.
     *                  A spawn point is added just above the bottom of the window.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void addSpawnPointAtBottomOfWindow () {
        try {
            
            // Create spawn point
            new HW04_ObjectSpawnPoint(
                // Get auto GUID
                -1,
                // X
                HW04_Utility.getWindowSize() / 2 - HW04_ObjectCharacter.getStaticHeight() / 2,
                // Y
                HW04_Utility.getWindowSize() - HW04_ObjectCharacter.getStaticHeight()
            );
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRandomSpawnPoint
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Return a randomly selected spawn point
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         oSpawnPoint -   A randomly selected spawn point
     *********************************************************************************************************/
    public static HW04_ObjectSpawnPoint getRandomSpawnPoint () {
        
        HW04_ObjectSpawnPoint oSpawnPoint = null;
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW04_ObjectSpawnPoint> aoSpawnPoints;
            Random oRandomizer;
            
            // Get randomizer
            oRandomizer = new Random();
            
            // Find out about all spawn points in the game
            aoSpawnPoints = getSpawnPointObjects();
            
            // Error checking
            if (aoSpawnPoints.size() < 1) {
                throw new Exception("Must have at least one spawn point!");
            }
            
            // Assign a random spawn point
            else {
                oSpawnPoint = aoSpawnPoints.get(oRandomizer.nextInt(aoSpawnPoints.size()));
            }
            
            // Free
            aoSpawnPoints = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        return oSpawnPoint;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getSpawnPointObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         aoObjectsRenderable -    All renderable objects in the game.
     *********************************************************************************************************/
    private static CopyOnWriteArrayList<HW04_ObjectSpawnPoint> getSpawnPointObjects () {
        CopyOnWriteArrayList<HW04_ObjectSpawnPoint> aoObjectsSpawn = new CopyOnWriteArrayList<HW04_ObjectSpawnPoint>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW04_ObjectGame> oObjectsGame = HW04_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW04_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW04_ObjectSpawnPoint) {
                    aoObjectsSpawn.add((HW04_ObjectSpawnPoint) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        return aoObjectsSpawn;
    }
    
}
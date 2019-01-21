package HW02;
/*************************************************************************************************************
 * FILE:            HW03_ObjectSpawnPoint.java
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
public class HW02_ObjectSpawnPoint extends HW02_ObjectLocated {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectSpawnPoint Constructor
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
    public HW02_ObjectSpawnPoint (
        int nExistingGUID,
        int nX_px, 
        int nY_px
    ) {
        // Construct located object
        super(nExistingGUID, nX_px, nY_px);
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
    public static HW02_ObjectSpawnPoint getRandomSpawnPoint () {
        
        HW02_ObjectSpawnPoint oSpawnPoint = null;
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW02_ObjectSpawnPoint> aoSpawnPoints;
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
            HW02_Utility.handleError(oError);
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
    private static CopyOnWriteArrayList<HW02_ObjectSpawnPoint> getSpawnPointObjects () {
        CopyOnWriteArrayList<HW02_ObjectSpawnPoint> aoObjectsSpawn = new CopyOnWriteArrayList<HW02_ObjectSpawnPoint>();
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW02_ObjectGame> oObjectsGame = HW02_ObjectGame.getGameObjects();
            
            // Get all character objects
            for (ConcurrentHashMap.Entry<Integer, HW02_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                if (oEntry.getValue() instanceof HW02_ObjectSpawnPoint) {
                    aoObjectsSpawn.add((HW02_ObjectSpawnPoint) oEntry.getValue());
                }
            }
            
            // Free
            oObjectsGame = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
        return aoObjectsSpawn;
    }
    
}
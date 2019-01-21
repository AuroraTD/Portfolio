/*************************************************************************************************************
 * FILE:            HW03_PerformanceTest.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that supports performance testing
 *                  This test kicks off a burst of fake collisions 
 *                      of the character object with the death zone,
 *                      and waits for this to result in the movement of the character object.
 *                  This tests the interaction of multiple events:
 *                      COLLISION --> SCORE_CHANGE --> SPAWN
 *                  Specifically, we measure the time that it takes for all of the collision events 
 *                  to cause score change events and subsequently spawn events.
 *                  We can then vary parameters to see how this time responds.
 *                      - Event Management Scheme (distributed or server-centric)
 *                      - Number of moving platforms in the game
 *                      - Number of collision events in the "burst"
 *************************************************************************************************************/

// IMPORTS
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW03_PerformanceTest implements HW03_EventObserver, java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Private constants
    private static final HW03_EventManager O_EVENT_MANAGER =    HW03_EventManager.getInstance();
    private static final String S_TIMER_PERFORMANCE_TEST =      "PerformanceTest";
    private static int nExpectedResponsesReceived =             0;
    private static HW03_ObjectCharacter oMyCharacter;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_PerformanceTest Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new performance test object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_PerformanceTest () {
        try {
            // Nothing to do here, there is a separate method to explicitly start testing
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        startTesting
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Start the performance test
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void startTesting () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW03_ObjectCollidable> aoCollidingObjects;
            int i;
            
            // Get an instance
            HW03_PerformanceTest oTestInstance = new HW03_PerformanceTest();
            
            // Start timer
            HW03_Time.startTimer(S_TIMER_PERFORMANCE_TEST);
            
            // Register interest in game object changes
            O_EVENT_MANAGER.registerForEvents(
                HW03_ObjectEvent.EventType.SPAWN, 
                oTestInstance, 
                false
            );
            
            // Raise fake collision events with the death zone
            oMyCharacter = HW03_ObjectCharacter.getCharacterObjectByPlayerID(HW03_Globals.nPlayerID);
            aoCollidingObjects = new CopyOnWriteArrayList<HW03_ObjectCollidable>();
            aoCollidingObjects.add(HW03_ObjectDeathZone.getDeathZoneObjects().get(0));
            for (i = 0; i < HW03_Globals.nPerformanceTestNumEvents; i++) {
                O_EVENT_MANAGER.raiseEventCollision(
                    oMyCharacter, 
                    aoCollidingObjects, 
                    0, 
                    0
                );
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        printPerformanceTestResults
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Print the results of a performance test
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void printPerformanceTestResults () {
        try {
            
            // Declare constants
            final String S_LOG_FILE = "performance_test_client.txt";
            final String S_DELIMITER = "\t";
            
            // Declare variables
            FileWriter  oFileWriter;
            PrintWriter oPrintWriter;
            boolean     bFileExists;
            
            // Find out if the file already exists
            bFileExists = new File(S_LOG_FILE).isFile();
            
            // Initialize writer
            oFileWriter = new FileWriter(S_LOG_FILE, true);
            oPrintWriter = new PrintWriter(oFileWriter);
            
            // Write header if needed
            if (bFileExists == false) {
                oPrintWriter.println(
                    "Event Management Protocol" + 
                    S_DELIMITER + 
                    "Frame Rate" + 
                    S_DELIMITER + 
                    "Number of Events Raised" + 
                    S_DELIMITER + 
                    "Time to Receive All Expected Events (ms)"
                );
            }
    
            // Print results
            oPrintWriter.println(
                (HW03_Globals.bStandardEventManagementProtocol ? "Distributed" : "Server-Centric") + 
                S_DELIMITER + 
                HW03_Globals.nFrameRate + 
                S_DELIMITER + 
                HW03_Globals.nPerformanceTestNumEvents + 
                S_DELIMITER + 
                HW03_Time.readTimer(S_TIMER_PERFORMANCE_TEST, "REAL")
            );
            
            // Flush to be on the safe side
            oPrintWriter.flush();
            
            // Close
            oPrintWriter.close();
            oFileWriter.close();
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

    /*********************************************************************************************************
     * FUNCTION:        handleEvent
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle an event
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    public void handleEvent (HW03_ObjectEvent oEventToHandle) {
        try {
            
            // My character object has changed?
            if (
                oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.SPAWN &&
                (
                    ((HW03_ObjectGame) oEventToHandle.getEventArguments().get("oCharacterObject")).getGUID() == 
                    oMyCharacter.getGUID()
                )
            ) {
                
                // Count the times we've gotten the expected response
                nExpectedResponsesReceived++;
                
                // If we've gotten all expected responses, wrap up the test
                if (nExpectedResponsesReceived == HW03_Globals.nPerformanceTestNumEvents) {
                    
                    // Stop timer
                    HW03_Time.stopTimer(S_TIMER_PERFORMANCE_TEST);
                    
                    // Print results
                    printPerformanceTestResults();
                    
                    // Destroy timer
                    HW03_Time.destroyTimer(S_TIMER_PERFORMANCE_TEST);
                    
                }
                
            }

        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

}
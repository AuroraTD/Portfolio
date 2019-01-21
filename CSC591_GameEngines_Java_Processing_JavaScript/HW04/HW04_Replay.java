/*************************************************************************************************************
 * FILE:            HW04_Replay.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class to manage replays.
 *                  Replays are handled ENTIRELY on the client side.
 *                      This is because if they were assisted by the server, 
 *                          then it would be really challenging to support replays on multiple clients at the same time, 
 *                          which is a feature that is not required by the homework assignment, 
 *                          but is a very reasonable feature to expect in a real system.
 *                      The server is in charge of moving platforms around 
 *                          and handling events related to those platforms (e.g. collisions).
 *                      So, we cannot expect help from the server on this during replay.
 *                      The end result is that we pay attention only to those events 
 *                          which indicate the positions of game objects.
 *                      This makes perfect sense, since a replay after all is only replaying 
 *                          game objects moving around onscreen.
 *                  Important assumption (unhandled cases):
 *                      We assume that no game objects will be created or destroyed 
 *                          during the time that the recording is being done for the replay!
 *                      We assume that the user will not wish to pause during recording or during replay!
 *                  How does replay actually work?
 *                      There are many ways to skin a cat.
 *                      Here's how this particular cat is skinned.
 *                      Located objects have two additional attributes 
 *                          which mark their location (x,y) at the start of replay recording.
 *                      At the start of replay, they are teleported back to those locations.
 *                      During replay, a special replay log is read out which notes their locations.
 *                      They are moved to these locations according to local (replay) loop time.
 *                          They may be moved less than once per game loop iteration (slow speed),
 *                          or more than once per game loop iteration (fast speed).
 *************************************************************************************************************/

// IMPORTS
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW04_Replay implements HW04_EventObserver {
    
    // Constants
    public static enum ReplayEventType {
        RECORD_START,
        RECORD_END,
        REPLAY_START,
        REPLAY_END
    }
    public static enum ReplayState {
        IDLE,
        RECORDING,
        WAITING_TO_REPLAY,
        REPLAYING
    }
    private static final String S_TIMER_RECORDING =                 "Recording";
    private static final HW04_EventManager O_EVENT_MANAGER =        HW04_EventManager.getInstance();
    private static final HW04_Time_Loop O_REPLAY_LOOP =             HW04_Time_Loop.getReplayInstance();
    private static final HW04_Time_Loop O_PLAY_LOOP =               HW04_Time_Loop.getPlayInstance();
    private static ReplayState nCurrentState =                      ReplayState.IDLE;
    private static int nReplayArrayIndex =                          -1;
    private static ArrayList<String> asReplayLogContents =          null;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Replay Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new logger
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_Replay () {
        try {
            // Register interest in select events
            O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.USER_INPUT, this, false);
            O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.REPLAY, this, false);
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getCurrentState
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nCurrentState 
     *********************************************************************************************************/
    public static ReplayState getCurrentState () {
        return nCurrentState;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        stepReplayForward
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void stepReplayForward () {
        try {
            
            // Declare variables
            double nPlayLoopTimeToGetTo;
            float nPlayLoopTime;
            float nObjectX_px;
            float nObjectY_px;
            boolean bObjectFlaggedForRemoval;
            String[] asTokens1;
            String[] asTokens2;
            String sUsefulPartOfLine;
            HW04_ObjectLocated oGameObjectToMove;
            ConcurrentHashMap<Integer, HW04_ObjectGame> aoGameObjects;
            int nObjectGUID;
            int i;
            
            // Control replay loop
            if (O_REPLAY_LOOP.isStarted() == false) {
                O_REPLAY_LOOP.start();
            }
            if (O_REPLAY_LOOP.getTime() == 0) {
                // Read contents of replay log into an array of strings
                asReplayLogContents = HW04_Logger.getLogContents(true);
                // Set a pointer to the next element to read from this array
                nReplayArrayIndex = 0;
            }
            
            // Tick forward the replay loop time
            O_REPLAY_LOOP.tick();
            
            // Figure out what play loop time we need to get to in the course of executing this step
            nPlayLoopTimeToGetTo = 
                (O_REPLAY_LOOP.getTime() * O_PLAY_LOOP.getTickSize()) + 
                HW04_Time.getTimerStartTime(S_TIMER_RECORDING, "LOOP");
            
            /* Go through contents of replay log, updating locations of game objects, until we reach the appropriate time
             *  Example line from replay log:
             *      1541460694937   3662    77  2   GAME_OBJECT_CHANGE  oGameObject HW04_ObjectPlatformV, 17, 212.0, 314.88342, FALSE
             *  Explained:
             *      Real Time \t Game Time \t Play Loop Time \t Player ID of Event Originator \t Event Type \t Key \t Game object type, GUID, X position, Y position, removal flag
             */
            while (true) {
                
                if (nReplayArrayIndex >= asReplayLogContents.size()) {
                    break;
                }
                else {
                    
                    // Parse play loop time out
                    asTokens1 = asReplayLogContents.get(nReplayArrayIndex).split("\t");
                    nPlayLoopTime = Float.valueOf(asTokens1[2]);
                    
                    // Still in the part of the replay log that matters for this step?
                    if (nPlayLoopTime > nPlayLoopTimeToGetTo) {
                        break;
                    }
                    else {
                        
                        // Parse everything else out
                        sUsefulPartOfLine = asTokens1[6];
                        asTokens2 = sUsefulPartOfLine.split(", ");
                        nObjectGUID = Integer.parseInt(asTokens2[1]);
                        nObjectX_px = Float.valueOf(asTokens2[2]);
                        nObjectY_px = Float.valueOf(asTokens2[3]);
                        bObjectFlaggedForRemoval = 
                                asTokens2.length > 4 ? 
                                asTokens2[4].toUpperCase().equals("TRUE") : 
                                false;
                        
                        // Update the location of this game object
                        oGameObjectToMove = ((HW04_ObjectLocated) HW04_ObjectGame.getObjectByGUID(nObjectGUID));
                        oGameObjectToMove.setPositionX(nObjectX_px);
                        oGameObjectToMove.setPositionY(nObjectY_px);
                        
                        /* Make sure the game object visibility is correct
                         *  Object created during replay recording
                         *      Will be hidden when replay starts
                         *  Object "destroyed" during replay recording
                         *      Will be shown when replay starts
                         */
                        if (oGameObjectToMove instanceof HW04_ObjectRenderable) {
                            ((HW04_ObjectRenderable) oGameObjectToMove).setHiddenFlag(bObjectFlaggedForRemoval);
                        }
                        
                        // Move the index for next time
                        nReplayArrayIndex++;
                        
                    }
                    
                }
                
            }
            
            // Wrap up
            if (nReplayArrayIndex >= asReplayLogContents.size()) {
                
                // Can finally get rid of game objects that are flagged for removal
                aoGameObjects = HW04_ObjectGame.getGameObjects();
                for (HW04_ObjectGame oSingleGameObject : aoGameObjects.values()){
                    if (oSingleGameObject.getRemovalFlag() == true) {
                        HW04_ObjectGame.removeObject(oSingleGameObject);
                    }
                }
                
                // Reset timing as a safeguard for next replay
                O_REPLAY_LOOP.reset();
                
                // Note that we're done
                O_EVENT_MANAGER.raiseEventReplay(HW04_Replay.ReplayEventType.REPLAY_END);
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }

    /*********************************************************************************************************
     * FUNCTION:        handleEvent
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle event
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    @Override
    public synchronized void handleEvent (HW04_ObjectEvent oEventToHandle) {
        try {
            
            // Declare variables
            ConcurrentHashMap<String, Object> oEventArguments;
            CopyOnWriteArrayList<HW04_ObjectLocated> aoObjectsLocated;
            HW04_ObjectLocated oLocatedObject;
            int i;
            
            // Get event arguments
            oEventArguments = oEventToHandle.getEventArguments();
            
            // USER INPUT
            if (oEventToHandle.getEventType() == HW04_ObjectEvent.EventType.USER_INPUT) {
                if (oEventArguments.get("sKey").equals("START_RECORDING") && oEventArguments.get("bPressed").equals("TRUE")) {
                    O_EVENT_MANAGER.raiseEventReplay(HW04_Replay.ReplayEventType.RECORD_START);
                }
                else if (oEventArguments.get("sKey").equals("STOP_RECORDING") && oEventArguments.get("bPressed").equals("TRUE")) {
                    O_EVENT_MANAGER.raiseEventReplay(HW04_Replay.ReplayEventType.RECORD_END);
                }
                else if (oEventArguments.get("sKey").equals("SET_REPLAY_SPEED") && oEventArguments.get("bPressed").equals("TRUE")) {
                    O_REPLAY_LOOP.setTickSize((float) oEventArguments.get("nReplaySpeed"));
                }
                else if (oEventArguments.get("sKey").equals("START_REPLAY") && oEventArguments.get("bPressed").equals("TRUE")) {
                    O_EVENT_MANAGER.raiseEventReplay(HW04_Replay.ReplayEventType.REPLAY_START);
                }
            }
            
            // REPLAY
            if (oEventToHandle.getEventType() == HW04_ObjectEvent.EventType.REPLAY) {
                if (oEventArguments.get("nReplayEventType") == HW04_Replay.ReplayEventType.RECORD_START) {
                    
                    // Save state
                    nCurrentState = ReplayState.RECORDING;
                    
                    // Start (or restart - to support multiple replays) timer
                    if (HW04_Time.doesTimerExist(S_TIMER_RECORDING) == true) {
                        HW04_Time.restartTimer(S_TIMER_RECORDING);
                    }
                    else {
                        HW04_Time.startTimer(S_TIMER_RECORDING);
                    }
                    
                    /* Save current state of all game objects so we can teleport them back here at the start of replay
                     * Objects that do not exist now, but are created during recording,
                     * will be identifiable by the fact that they still have default / invalid teleport locations
                     */
                    aoObjectsLocated = HW04_ObjectLocated.getLocatedObjects();
                    for (i = 0; i < aoObjectsLocated.size(); i++) {
                        oLocatedObject = aoObjectsLocated.get(i);
                        oLocatedObject.setReplayTeleportX(oLocatedObject.getPositionX());
                        oLocatedObject.setReplayTeleportY(oLocatedObject.getPositionY());
                    }
                    
                }
                else if (oEventArguments.get("nReplayEventType") == HW04_Replay.ReplayEventType.RECORD_END) {
                    
                    // Save state
                    nCurrentState = ReplayState.WAITING_TO_REPLAY;
                    
                    // Stop timer
                    HW04_Time.stopTimer(S_TIMER_RECORDING);
                    
                    // Pause the game during replay
                    O_EVENT_MANAGER.raiseEventGamePause(true);
                    
                }
                else if (oEventArguments.get("nReplayEventType") == HW04_Replay.ReplayEventType.REPLAY_START) {
                    
                    // Save state
                    nCurrentState = ReplayState.REPLAYING;
                    
                    /* Teleport objects back to where they were at the start of the recording
                     * If the object did not exist at the start of recording, 
                     * it will not have a valid teleport location,
                     * and should therefore be hidden
                     */
                    aoObjectsLocated = HW04_ObjectLocated.getLocatedObjects();
                    for (i = 0; i < aoObjectsLocated.size(); i++) {
                        oLocatedObject = aoObjectsLocated.get(i);
                        if (
                            oLocatedObject.getReplayTeleportX() >= 0 &&
                            oLocatedObject.getReplayTeleportY() >= 0
                        ) {
                            oLocatedObject.setPositionX(oLocatedObject.getReplayTeleportX());
                            oLocatedObject.setPositionY(oLocatedObject.getReplayTeleportY());
                            if (oLocatedObject instanceof HW04_ObjectRenderable) {
                                ((HW04_ObjectRenderable) oLocatedObject).setHiddenFlag(false);
                            }
                        }
                        else {
                            if (oLocatedObject instanceof HW04_ObjectRenderable) {
                                ((HW04_ObjectRenderable) oLocatedObject).setHiddenFlag(true);
                            }
                            else {
                                throw new Exception("Did not expect a non-renderable object to lack a replay teleport location!");
                            }
                        }

                    }
                    
                    /* Actually moving objects around 
                     * will be accomplished by a call from the game loop to another replay engine method
                     */
                    
                }
                else if (oEventArguments.get("nReplayEventType") == HW04_Replay.ReplayEventType.REPLAY_END) {
                    
                    // Save state
                    nCurrentState = ReplayState.IDLE;
                    
                    // Resume the game after replay
                    O_EVENT_MANAGER.raiseEventGamePause(false);
                    
                }
            }

        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
}
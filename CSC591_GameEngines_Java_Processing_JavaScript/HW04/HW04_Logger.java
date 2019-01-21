/*************************************************************************************************************
 * FILE:            HW04_Logger.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class to manage logging
 *                  All events are logged all the time to assist in debugging and record-keeping.
 *                  During replay recording, a separate, smaller, temporary replay log is also maintained.
 *************************************************************************************************************/

// IMPORTS
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

// CLASS DEFINITION
public class HW04_Logger implements HW04_EventObserver, Runnable {
    
    // Private constants
    private static final String S_LOG_FILE_ALL =    HW04_Globals.bClient ? "LogClient.txt" : "LogServer.txt";
    private static final String S_LOG_FILE_REPLAY = "LogReplay.txt";
    
    // Private variables
    private static PrintWriter oWriterAll =             null;
    private static PrintWriter oWriterReplay =          null;
    private static boolean bRecordingForReplay =        false;
    private static PriorityBlockingQueue<HW04_ObjectEvent> oEventWriteQueue = 
        new PriorityBlockingQueue<HW04_ObjectEvent>();
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Logger Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new logger
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_Logger () {
        try {
            // Get event manager proxy instance
            final HW04_EventManager O_EVENT_MANAGER = HW04_EventManager.getInstance();
            // Register interest in events of all types
            O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.WILDCARD, this, false);
            // Note that the logger has started up 
            O_EVENT_MANAGER.raiseEventAdmin("Logger Started");
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        run
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A function that can be run as a separate thread.
     *                  This way, the expensive file I/O should have
     *                  a minimized effect on the rest of the program.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    public void run () {
        try {
            
            while (true) {
                
                // Declare variables
                HW04_ObjectEvent oEventToWrite;
                
                /* Get next event-to-write from queue
                 * .poll() is the non-blocking version (returns even if queue empty)
                 * .take() is the blocking version (waits for queue to be non-empty)
                 */
                oEventToWrite = oEventWriteQueue.take();

                // Declare constants
                String S_DELIMITER_MAJOR = "\t";
                String S_DELIMITER_MINOR = ", ";
                
                // Declare variables
                ConcurrentHashMap<String, Object> oEventArguments;
                StringBuilder sStringToLog;
                HW04_ObjectGame oGameObject;
                String sGameObjectType;
                CopyOnWriteArrayList<HW04_ObjectCollidable> aoCollidingObjects;
                File oFileToDelete;
                int i;
                
                // Get event arguments
                oEventArguments = oEventToWrite.getEventArguments();
                
                // USER INPUT
                if (oEventToWrite.getEventType() == HW04_ObjectEvent.EventType.REPLAY) {
                    if (oEventArguments.get("nReplayEventType") == HW04_Replay.ReplayEventType.RECORD_START) {
                        // Remember that we are recording for replay
                        bRecordingForReplay = true;
                        // Make sure that we start a fresh recording file
                        if (oWriterReplay != null) {
                            oFileToDelete = new File(S_LOG_FILE_REPLAY);
                            oFileToDelete.delete();
                            oWriterReplay = null;
                        }
                    }
                    else if (oEventArguments.get("nReplayEventType") == HW04_Replay.ReplayEventType.RECORD_END) {
                        // Remember that we are no longer recording for replay
                        bRecordingForReplay = false;
                    }
                }
                
                // Initialize writers if needed
                if (oWriterAll == null) {
                    oWriterAll = new PrintWriter(S_LOG_FILE_ALL, "UTF-8");
                    oWriterAll.println(
                        "Time (Real)" +
                        S_DELIMITER_MAJOR +
                        "Time (Game)" +
                        S_DELIMITER_MAJOR + 
                        "Time (Loop)" + 
                        S_DELIMITER_MAJOR + 
                        "Player ID of Event Originator" +
                        S_DELIMITER_MAJOR +
                        "Event Type" + 
                        S_DELIMITER_MAJOR + 
                        "Event Arguments"
                   );
                }
                if (bRecordingForReplay == true && oWriterReplay == null) {
                    oWriterReplay = new PrintWriter(S_LOG_FILE_REPLAY, "UTF-8");
                }
                
                // Build string to log
                sStringToLog = new StringBuilder();
                sStringToLog.append(oEventToWrite.getEventTimeReal());
                sStringToLog.append(S_DELIMITER_MAJOR);
                sStringToLog.append(oEventToWrite.getEventTimeGame());
                sStringToLog.append(S_DELIMITER_MAJOR);
                sStringToLog.append(oEventToWrite.getEventTimeLoop());
                sStringToLog.append(S_DELIMITER_MAJOR);
                sStringToLog.append(oEventToWrite.getPlayerID());
                sStringToLog.append(S_DELIMITER_MAJOR);
                sStringToLog.append(oEventToWrite.getEventType());
                sStringToLog.append(S_DELIMITER_MAJOR);
                for (ConcurrentHashMap.Entry<String, Object> oEntry : oEventToWrite.getEventArguments().entrySet()) {
                    sStringToLog.append(oEntry.getKey());
                    sStringToLog.append(S_DELIMITER_MAJOR);
                    /* "oGameObject" key used in GAME_OBJECT_CHANGE events
                     * Need to log attributes that can be used during replay
                     */
                    if (oEntry.getKey().equals("oGameObject")) {
                        oGameObject = (HW04_ObjectGame) oEntry.getValue();
                        sGameObjectType = oGameObject.getType();
                        sStringToLog.append(sGameObjectType);
                        sStringToLog.append(S_DELIMITER_MINOR);
                        sStringToLog.append(oGameObject.getGUID());
                        if (
                            sGameObjectType.equals("HW04_ObjectCharacter") ||
                            sGameObjectType.equals("HW04_ObjectPlatformH") ||
                            sGameObjectType.equals("HW04_ObjectPlatformV")
                        ) {
                            sStringToLog.append(S_DELIMITER_MINOR);
                            sStringToLog.append(((HW04_ObjectLocated) oGameObject).getPositionX());
                            sStringToLog.append(S_DELIMITER_MINOR);
                            sStringToLog.append(((HW04_ObjectLocated) oGameObject).getPositionY());
                            sStringToLog.append(S_DELIMITER_MINOR);
                            sStringToLog.append(((HW04_ObjectRenderable) oGameObject).getRemovalFlag());
                        }
                    }
                    // "oCharacterObject" key used in SCORE_CHANGE and SPAWN events
                    else if (oEntry.getKey().equals("oCharacterObject")) {
                        sStringToLog.append(((HW04_ObjectCharacter) oEntry.getValue()).getGUID());
                    }
                    // "oMovedObject" key used in COLLISION events
                    else if (oEntry.getKey().equals("oMovedObject")) {
                        sStringToLog.append(((HW04_ObjectCollidable) oEntry.getValue()).getGUID());
                    }
                    // "aoCollidingObjects" key used in COLLISION events
                    else if (oEntry.getKey().equals("aoCollidingObjects")) {
                        aoCollidingObjects = (CopyOnWriteArrayList<HW04_ObjectCollidable>) oEntry.getValue();
                        for (i = 0; i < aoCollidingObjects.size(); i++) {
                            sStringToLog.append(aoCollidingObjects.get(i).getGUID());
                            if (i < aoCollidingObjects.size()-1) {
                                sStringToLog.append(S_DELIMITER_MINOR);
                            }
                        }
                    }
                    else {
                        sStringToLog.append(oEntry.getValue());
                    }
                    sStringToLog.append(S_DELIMITER_MAJOR);
                }
                
                // Log to file(s) and flush to be on the safe side
                oWriterAll.println(sStringToLog);
                oWriterAll.flush();
                if (
                    bRecordingForReplay == true && 
                    oEventToWrite.getEventType() == HW04_ObjectEvent.EventType.GAME_OBJECT_CHANGE
                ) {
                    oWriterReplay.println(sStringToLog);
                    oWriterReplay.flush();
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getLogContents
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get and return the contents of a log as an array of strings (one string per line)
     *                  https://stackoverflow.com/questions/285712/java-reading-a-file-into-an-array
     * 
     * ARGUMENTS:       bReplayLog -    True if we want the contents of the replay log
     *                                  False if we want the contents of the complete log
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static ArrayList<String> getLogContents (boolean bReplayLog) {
        
        // Initialize
        ArrayList<String> asLines = new ArrayList<String>();
        
        // Work
        try {
            
            // Declare constants
            FileReader O_FILE_READER = new FileReader(bReplayLog ? S_LOG_FILE_REPLAY : S_LOG_FILE_ALL);
            BufferedReader O_BUFFERED_READER = new BufferedReader(O_FILE_READER);
            String sSingleLine;
            
            // Initialize
            sSingleLine = null;
            
            // Read out all lines from file
            while ((sSingleLine = O_BUFFERED_READER.readLine()) != null) {
                asLines.add(sSingleLine);
            }
            
            // Close file
            O_BUFFERED_READER.close();
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        
        // Return
        return asLines;
        
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
            
            /* Simply add the event to a queue
             * A separate thread will dequeue and deal with file I/O
             * Don't want to bog down the program by waiting on that here
             */
            oEventWriteQueue.add(oEventToHandle);
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }

}
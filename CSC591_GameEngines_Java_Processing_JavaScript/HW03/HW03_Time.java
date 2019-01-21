/*************************************************************************************************************
 * FILE:            HW03_Time.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents time.
 *                  There are several timelines established in the game.
 *                  REAL time - Clock time, expressed as milliseconds since epoch.
 *                  GAME time - Anchored to REAL time, expressed as milliseconds since the game has started.
 *                  LOOP time - Expressed as iterations of the game loop.
 *                              PLAY LOOP time -    Iterations of the game loop while the game is not paused.
 *                              REPLAY LOOP time -  Anchored to PLAY LOOP time.
 *                                                  Iterations of the play loop 
 *                                                  during replay of a recorded game section.
 *                                                  Tick size is determined by replay speed.
 *                                                  For example, if tick size is 2, 
 *                                                  then each game loop iteration during replay
 *                                                  will replay 2 iterations of the play loop.
 *************************************************************************************************************/

// IMPORTS
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

// CLASS DEFINITION
public abstract class HW03_Time implements HW03_EventObserver, java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID =                    1L;
    
    // Private constants
    private static HW03_EventManager O_EVENT_MANAGER =         HW03_EventManager.getInstance();
    
    // Private variables
    private static ConcurrentHashMap<String, HW03_Timer> oTimers =  new ConcurrentHashMap<String, HW03_Timer>();
    private static int nLastClientPausedPlayerID =                  -1;
    private static int nTimelineInstances =                         0;
    
    // Declare abstract methods
    abstract double getTime();
    abstract void start();
    abstract void reset();
    abstract boolean isStarted();
    abstract void pause();
    abstract boolean isPaused();
    abstract void resume();
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Time Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new time object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Time () {
        try {
            
            // Register interest in pause events, but only for one instance otherwise we get swamped
            if (nTimelineInstances == 0) {
                
                // Register interest in user saying they want to pause so we can handle that locally
                O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.USER_INPUT, this, false);

                /* Register interest in game pause events game-wide
                 * interested if we raise event or if network partner does)
                 */
                O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.GAME_PAUSE, this, true);
                
            }
            
            // Increment
            nTimelineInstances++;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        startTimer
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Determine if a given timer exists
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     * 
     * RETURNS:         bTimerExists -  True if the timer exists, otherwise false
     *********************************************************************************************************/
    public static boolean doesTimerExist (String sTimerName) {
        
        return oTimers.containsKey(sTimerName);
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        isTimerTiming
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Determine if a given timer is currently timing
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     * 
     * RETURNS:         bIsTiming -  True if the timer is currently timing, otherwise false
     *********************************************************************************************************/
    public static boolean isTimerTiming (String sTimerName) {
        
        return oTimers.get(sTimerName).getIsTiming();
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        pauseGame
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Pause the game (game time and game loop time)
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private static void pauseGame () {
        try {

            // Check for problems
            if (isGamePaused() == true) {
                throw new Exception("Cannot pause the game (already paused)");
            }
            // Pause game and loop time
            else {
                HW03_Time_Game.getInstance().pause();
                HW03_Time_Loop.getPlayInstance().pause();
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        isGamePaused
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Determine if the game is currently paused
     * 
     * ARGUMENTS:       bIsPaused - True if the game (game time or loop time) is paused, otherwise false
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static boolean isGamePaused () {
        
        return HW03_Time_Game.getInstance().isPaused() || HW03_Time_Loop.getPlayInstance().isPaused();
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getLastClientPausedPlayerID
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       nLastClientPausedPlayerID
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static int getLastClientPausedPlayerID () {
        
        return nLastClientPausedPlayerID;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        resumeGame
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Resume the game (game time and game loop time)
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private static void resumeGame () {
        try {

            // Check for problems
            if (isGamePaused() == false) {
                throw new Exception("Cannot resume the game (not paused)");
            }
            // Resume game and loop time
            else {
                HW03_Time_Game.getInstance().resume();
                HW03_Time_Loop.getPlayInstance().resume();
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        startTimer
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Start a timer
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void startTimer (String sTimerName) {
        
        try {
            
            // Check for problems
            if (oTimers.containsKey(sTimerName) == true) {
                throw new Exception ("Cannot create new timer '" + sTimerName + "' (already exists)");
            }
            // Add a new timer to the hash map
            else {         
                oTimers.put(sTimerName, new HW03_Timer());
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        restartTimer
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Restart a timer
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void restartTimer (String sTimerName) {
        
        try {
            
            // Check for problems
            if (oTimers.containsKey(sTimerName) == false) {
                throw new Exception ("Cannot restart timer '" + sTimerName + "' (does not exist)");
            }
            // Add a new timer to the hash map
            else {         
                oTimers.get(sTimerName).restart();
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        stopTimer
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Stop a timer
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void stopTimer (String sTimerName) {
        
        try {
            
            // Check for problems
            if (oTimers.containsKey(sTimerName) == false) {
                throw new Exception ("Cannot stop timer '" + sTimerName + "' (does not exist)");
            }
            // Stop the timer
            else {
                oTimers.get(sTimerName).stop();
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        readTimer
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the elapsed time from a timer
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     *                  sTimeType -     The type of time you wish the elapsed time to be represented in
     *                                  "Real", "Game", or "Loop"
     * 
     * RETURNS:         nElapsedTime -  The elapsed time from a given timer
     *********************************************************************************************************/
    public static double readTimer (String sTimerName, String sTimeType) {
        double nElapsedTime = -1;
        try {
            
            // Declare variables
            HW03_Timer oTimer;
            
            // Check for problems
            if (oTimers.containsKey(sTimerName) == false) {
                throw new Exception ("Cannot read timer '" + sTimerName + "' (does not exist)");
            }
            // Read the timer
            else {
                oTimer = oTimers.get(sTimerName);
                switch (sTimeType.toUpperCase()) {
                    case "REAL":
                        nElapsedTime = oTimer.getElapsedTimeReal();
                        break;
                    case "GAME":
                        nElapsedTime = oTimer.getElapsedTimeGame();
                        break;
                    case "LOOP":
                        nElapsedTime = oTimer.getElapsedTimeLoop();
                        break;
                    default:
                        throw new Exception ("Do not recognize time type '" + sTimeType + "'");
                }
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nElapsedTime;        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getTimerStartTime
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the start time from a timer
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     *                  sTimeType -     The type of time you wish the time to be represented in
     *                                  "Real", "Game", or "Loop"
     * 
     * RETURNS:         nStartTime -  The start time of a given timer
     *********************************************************************************************************/
    public static double getTimerStartTime (String sTimerName, String sTimeType) {
        double nStartTime = -1;
        try {
            
            // Declare variables
            HW03_Timer oTimer;
            
            // Check for problems
            if (oTimers.containsKey(sTimerName) == false) {
                throw new Exception ("Cannot get start time from timer '" + sTimerName + "' (does not exist)");
            }
            // Read the timer
            else {
                oTimer = oTimers.get(sTimerName);
                switch (sTimeType.toUpperCase()) {
                    case "REAL":
                        nStartTime = oTimer.getStartTimeReal();
                        break;
                    case "GAME":
                        nStartTime = oTimer.getStartTimeGame();
                        break;
                    case "LOOP":
                        nStartTime = oTimer.getStartTimeLoop();
                        break;
                    default:
                        throw new Exception ("Do not recognize time type '" + sTimeType + "'");
                }
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nStartTime;        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        destroyTimer
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Destroy a timer
     * 
     * ARGUMENTS:       sTimerName -    An arbitrary name for the timer
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void destroyTimer (String sTimerName) {
        try {
            
            // Check for problems
            if (oTimers.containsKey(sTimerName) == false) {
                throw new Exception ("Cannot destroy timer '" + sTimerName + "' (does not exist)");
            }
            // Destroy the timer
            else {
                oTimers.remove(sTimerName);
            }
            
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
     * DESCRIPTION:     Handle event
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    @Override
    public synchronized void handleEvent (HW03_ObjectEvent oEventToHandle) {
        try {
            
            // Declare variables
            ConcurrentHashMap<String, Object> oEventArguments;
            
            // Get event arguments
            oEventArguments = oEventToHandle.getEventArguments();
            
            // USER INPUT
            if (oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.USER_INPUT) {
                if (
                    oEventArguments.get("sKey").equals("PAUSE") && 
                    oEventArguments.get("bPressed").equals("TRUE")
                ) {
                    if (isGamePaused() == true) {
                        if (HW03_Time.getLastClientPausedPlayerID() == HW03_Globals.nPlayerID) {
                            O_EVENT_MANAGER.raiseEventGamePause(false);
                        }
                    }
                    else {
                        O_EVENT_MANAGER.raiseEventGamePause(true);
                    }
                }
            }
            
            /* PAUSE
             *  Why, when the user wants to pause, do we first raise pause event and then handle it?
             *  Because another client may raise a pause event, we're not the only one who can do that
             */
            if (oEventToHandle.getEventType() == HW03_ObjectEvent.EventType.GAME_PAUSE) {
                if (oEventArguments.get("bPaused").equals("TRUE")) {
                    pauseGame();
                    nLastClientPausedPlayerID = oEventToHandle.getPlayerID();
                }
                else {
                    resumeGame();
                }
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

}
/*************************************************************************************************************
 * FILE:            HW03_Time_Game.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents time
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW03_Time_Game extends HW03_Time {
    
    // Required for serializable class
    private static final long serialVersionUID =            1L;
    
    // Private variables
    private static HW03_Time_Game oInstance =               null;
    private static double nTimeOffset_ms =                  -1;
    private static double nTimeGame_ms =                    -1;
    private static double nTimeGameLastResume_ms =          -1;
    private static double nTimeGameLastPause_ms =           -1;
    private static double nTimeRealLastResume_ms =          -1;
    private static double nTimeRealLastPause_ms =           -1;
    private static double nTimeRealElapsedDuringPause_ms =  -1;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Time_Game Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new time object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Time_Game () {
        try {
            // Nothing to do here
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getInstance
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter for singleton instance
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static HW03_Time_Game getInstance () {
        if (oInstance == null) {
            oInstance = new HW03_Time_Game();
        }
        return oInstance;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        start
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Start game time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void start () {
        
        try {
            
            // Check for problems
            if (isStarted() == true) {
                throw new Exception("Cannot start game time (already started)");
            }
            else {
                // Start the game time
                nTimeGame_ms =              0;
                nTimeOffset_ms =            HW03_Time_Real.getInstance().getTime();
                nTimeGame_ms =              HW03_Time_Real.getInstance().getTime() - nTimeOffset_ms;
                nTimeGameLastResume_ms =    nTimeGame_ms;
                nTimeRealLastResume_ms =    HW03_Time_Real.getInstance().getTime();
                // Note that the game time has started
                HW03_EventManager.getInstance().raiseEventAdmin("Game Time Started");
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        reset
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Reset game time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void reset () {
        try {
            throw new Exception("It is not appropriate to reset game time");
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }     
    }
    
    /*********************************************************************************************************
     * FUNCTION:        isStarted
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Find out if game time has been started
     * 
     * ARGUMENTS:       bIsStarted - True if game time has been started, otherwise false
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    boolean isStarted () {
        
        return nTimeGame_ms >= 0;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        pause
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Pause game time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void pause () {
        
        try {
            
            // Check for problems
            if (isPaused() == true) {
                throw new Exception("Cannot pause game time (already paused)");
            }
            // Pause game time
            else {
                nTimeGameLastPause_ms = nTimeGame_ms;
                nTimeRealLastPause_ms = HW03_Time_Real.getInstance().getTime();
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        isPaused
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Find out if game time is currently paused
     * 
     * ARGUMENTS:       bIsPaused - True if game time is currently paused
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    boolean isPaused () {
        
        return (
            isStarted() && 
            nTimeRealLastPause_ms > nTimeRealLastResume_ms
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        resume
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Resume game time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void resume () {
        
        try {
            
            // Check for problems
            if (isPaused() == false) {
                throw new Exception("Cannot resume game time (not paused)");
            }
            // Resume game time
            else {
                nTimeGameLastResume_ms =            nTimeGame_ms;
                nTimeRealLastResume_ms =            HW03_Time_Real.getInstance().getTime();
                nTimeRealElapsedDuringPause_ms =    nTimeRealLastResume_ms - nTimeRealLastPause_ms;
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }

    /*********************************************************************************************************
     * FUNCTION:        getTime
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     (Update and ) get the game time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime_ms -  Game time (elapsed real time while not paused), expressed as ms
     *********************************************************************************************************/
    @Override
    double getTime() {
        double nTime_ms = -1;
        try {
            
            /* A few possibilities exist
             *  - We haven't even started game time yet
             *  - We have started and have never paused
             *  - We have started and we are paused
             *  - We have started, paused, and resumed (possibly many times)
             * This code should update game time correctly for all such cases
             */
            if (nTimeGame_ms < 0) {
                nTime_ms = 0;
            }
            else if (nTimeRealLastPause_ms >= nTimeRealLastResume_ms) {
                // No need to update game time just return as-is
                nTime_ms = nTimeGame_ms;
            }
            else if (nTimeRealLastPause_ms < nTimeRealLastResume_ms) {
                // Update game time then return (game time at which we resumed, plus elapsed time since then)
                nTimeGame_ms = nTimeGameLastResume_ms + (HW03_Time_Real.getInstance().getTime() - nTimeRealLastResume_ms);
                nTime_ms = nTimeGame_ms;
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nTime_ms;
    }

}
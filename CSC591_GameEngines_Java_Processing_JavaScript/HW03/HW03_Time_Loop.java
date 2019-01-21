/*************************************************************************************************************
 * FILE:            HW03_Time_Loop.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents time
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW03_Time_Loop extends HW03_Time {
    
    // Required for serializable class
    private static final long serialVersionUID =    1L;
    
    // Private variables
    private static HW03_Time_Loop oPlayInstance =   null;
    private static HW03_Time_Loop oReplayInstance = null;
    private double nTimeLoopCurrent =               -1;
    private double nTimeLoopLastResume =            -1;
    private double nTimeLoopLastPause =             -1;
    private double nTimeGameLastIteration_ms =      -1;
    private double nTimeGameDelta_ms =              -1;
    private float nTickSize =                       1;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Time_Loop Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new time object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Time_Loop () {
        try {
            // Nothing to do here
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getPlayInstance
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter for singleton instance
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static HW03_Time_Loop getPlayInstance () {
        if (oPlayInstance == null) {
            oPlayInstance = new HW03_Time_Loop();
        }
        return oPlayInstance;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getReplayInstance
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter for singleton instance
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static HW03_Time_Loop getReplayInstance () {
        if (oReplayInstance == null) {
            oReplayInstance = new HW03_Time_Loop();
        }
        return oReplayInstance;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        start
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Start loop time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void start () {
        
        try {
            
            // Check for problems
            if (this.isStarted() == true) {
                throw new Exception("Cannot start loop time (already started)");
            }
            else {
                // Start the loop time
                this.nTimeLoopCurrent = 0;
                this.nTimeGameLastIteration_ms = HW03_Time_Game.getInstance().getTime();
                // Note that the loop time has started
                HW03_EventManager.getInstance().raiseEventAdmin("Loop Time Started");
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
     * DESCRIPTION:     Reset loop time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void reset () {

        try {
            
            // Check for problems
            if (this.isStarted() == false) {
                throw new Exception("Cannot reset loop time (not started)");
            }
            else {
                // Reset the loop time
                this.nTimeLoopCurrent = 0;
                this.nTimeGameLastIteration_ms = HW03_Time_Game.getInstance().getTime();
            }
            
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
     * DESCRIPTION:     Find out if loop time has been started
     * 
     * ARGUMENTS:       bIsStarted - True if loop time has been started, otherwise false
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    boolean isStarted () {
        
        return this.nTimeLoopCurrent >= 0;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        pause
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Pause loop time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void pause () {
        
        try {
            
            // Check for problems
            if (this.isPaused() == true) {
                throw new Exception("Cannot pause loop time (already paused)");
            }
            // Pause loop time
            else {
                this.nTimeLoopLastPause = this.nTimeLoopCurrent;
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
     * DESCRIPTION:     Find out if loop time is currently paused
     * 
     * ARGUMENTS:       bIsPaused - True if loop time is currently paused
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    boolean isPaused () {
        
        return (
            isStarted() && 
            this.nTimeLoopLastPause > this.nTimeLoopLastResume
        );
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        resume
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Resume loop time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void resume () {
        
        try {
            
            // Check for problems
            if (this.isPaused() == false) {
                throw new Exception("Cannot resume loop time (not paused)");
            }
            // Resume loop time
            else {
                this.nTimeLoopLastResume = this.nTimeLoopCurrent;
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setTickSize
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter
     * 
     * ARGUMENTS:       nSize - The number of tick counts to advance per game loop iteration
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setTickSize (float nSize) {
        
        this.nTickSize = nSize;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getTickSize
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nSize - The number of tick counts to advance per game loop iteration
     *********************************************************************************************************/
    public float getTickSize () {
        
        return this.nTickSize;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        tick
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Increment loop iterations
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void tick () {
        
        // Declare variables
        double nGameTimeCurrent_ms;
        
        /* Time delta calculations
         * Very simplistic - window is just one loop iteration wide!
         */
        nGameTimeCurrent_ms = HW03_Time_Game.getInstance().getTime();
        this.nTimeGameDelta_ms = (long) ((nGameTimeCurrent_ms - this.nTimeGameLastIteration_ms) / this.nTickSize);
        this.nTimeGameLastIteration_ms = nGameTimeCurrent_ms;
        
        // Tick
        this.nTimeLoopCurrent += this.nTickSize;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getTimeDelta
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the calculated time delta
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTimeDelta_ms - Calculated value for how long the last game loop iteration took.
     *                                  This is used to keep client and server objects moving at the correct speeds
     *                                  event when client and server frame rates differ.
     *********************************************************************************************************/
    public double getTimeDelta() {
        double nTimeDelta_ms = -1;
        try {
            
            nTimeDelta_ms = this.nTimeGameDelta_ms;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nTimeDelta_ms;
    }

    /*********************************************************************************************************
     * FUNCTION:        getTime
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get a time value
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime -  Game loop time, expressed as number of iterations
     *********************************************************************************************************/
    @Override
    public double getTime() {
        double nTime = -1;
        try {
            
            nTime = this.nTimeLoopCurrent;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nTime;
    }

}
/*************************************************************************************************************
 * FILE:            HW03_Time_Real.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents time
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW03_Time_Real extends HW03_Time {
    
    // Required for serializable class
    private static final long serialVersionUID =    1L;
    
    // Private variables
    private static HW03_Time_Real oInstance =       null;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Time_Real Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new time object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Time_Real () {
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
    public static HW03_Time_Real getInstance () {
        if (oInstance == null) {
            oInstance = new HW03_Time_Real();
        }
        return oInstance;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        start
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Start real time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void start () {
        try {
            throw new Exception("It is not possible to start real time");
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
     * DESCRIPTION:     Reset real time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void reset () {
        try {
            throw new Exception("It is not possible to reset real time"); 
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
     * DESCRIPTION:     Find out if real time has been started
     * 
     * ARGUMENTS:       bIsStarted - True if real time has been started, otherwise false
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    boolean isStarted () {
        
        return true;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        pause
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Pause real time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void pause () {
        try {
            throw new Exception("Cannot pause/ resume real time");
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
     * DESCRIPTION:     Find out if real time is currently paused
     * 
     * ARGUMENTS:       bIsPaused - True if real time is currently paused
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    boolean isPaused () {
        
        return false;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        resume
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Resume real time
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @Override
    void resume () {
        try {
            throw new Exception("Cannot pause / resume real time");
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
     * DESCRIPTION:     Get a time value
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime_ms -  Real time, expressed as ms since epoch
     *********************************************************************************************************/
    @Override
    public double getTime() {
        double nTime_ms = -1;
        try {
            
            nTime_ms = System.currentTimeMillis();
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nTime_ms;
    }

}
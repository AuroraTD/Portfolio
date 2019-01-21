/*************************************************************************************************************
 * FILE:            HW04_Timer.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a timer
 *************************************************************************************************************/

// IMPORTS
// None

// CLASS DEFINITION
public class HW04_Timer implements java.io.Serializable {
    
    // Required for serializable class
    private static final long serialVersionUID =    1L;
    
    // Private variables
    private double nStartTimeReal =                 -1;
    private double nStartTimeGame =                 -1;
    private double nStartTimeLoop =                 -1;
    private double nEndTimeReal =                   -1;
    private double nEndTimeGame =                   -1;
    private double nEndTimeLoop =                   -1;
    private boolean bIsTiming =                     false;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Timer Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new timer object
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_Timer () {
        try {
            this.start();
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        start
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Start this timer
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public void start () {
        this.nStartTimeReal = HW04_Time_Real.getInstance().getTime();
        this.nStartTimeGame = HW04_Time_Game.getInstance().getTime();
        this.nStartTimeLoop = HW04_Time_Loop.getPlayInstance().getTime();
        this.bIsTiming = true;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        restart
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Restart this timer
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public void restart () {
        this.nStartTimeReal =   HW04_Time_Real.getInstance().getTime();
        this.nStartTimeGame =   HW04_Time_Game.getInstance().getTime();
        this.nStartTimeLoop =   HW04_Time_Loop.getPlayInstance().getTime();
        this.nEndTimeReal =     -1;
        this.nEndTimeGame =     -1;
        this.nEndTimeLoop =     -1;
        this.bIsTiming =        true;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        stop
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Stop this timer
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public void stop () {
        this.nEndTimeReal = HW04_Time_Real.getInstance().getTime();
        this.nEndTimeGame = HW04_Time_Game.getInstance().getTime();
        this.nEndTimeLoop = HW04_Time_Loop.getPlayInstance().getTime();
        this.bIsTiming = false;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getElapsedTimeReal
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public double getElapsedTimeReal () {
        return this.nEndTimeReal < 0 ? 
            (HW04_Time_Real.getInstance().getTime() - this.nStartTimeReal) : 
            (this.nEndTimeReal - this.nStartTimeReal);
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getElapsedTimeGame
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public double getElapsedTimeGame () {
        
        return this.nEndTimeGame < 0 ? 
            (HW04_Time_Game.getInstance().getTime() - this.nStartTimeGame) : 
            (this.nEndTimeGame - this.nStartTimeGame);
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getElapsedTimeLoop
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public double getElapsedTimeLoop () {
        
        return this.nEndTimeLoop < 0 ? 
            (HW04_Time_Loop.getPlayInstance().getTime() - this.nStartTimeLoop) : 
            (this.nEndTimeLoop - this.nStartTimeLoop);
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStartTimeReal
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public double getStartTimeReal () {
        return this.nStartTimeReal;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStartTimeGame
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public double getStartTimeGame () {
        return this.nStartTimeGame;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getStartTimeLoop
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nTime
     *********************************************************************************************************/
    public double getStartTimeLoop () {
        return this.nStartTimeLoop;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getIsTiming
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         bIsTiming
     *********************************************************************************************************/
    public boolean getIsTiming () {
        
        return this.bIsTiming;
        
    }

}
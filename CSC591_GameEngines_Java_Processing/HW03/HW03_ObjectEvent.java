/*************************************************************************************************************
 * FILE:            HW03_ObjectEvent.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a game world event
 *************************************************************************************************************/

// IMPORTS
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// CLASS DEFINITION
public class HW03_ObjectEvent extends HW03_ObjectCommunicable implements Comparable<HW03_ObjectEvent> {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Static properties
    public static enum EventType {
        WILDCARD,
        ADMIN,
        USER_INPUT,
        COLLISION,
        SCORE_CHANGE,
        SPAWN,
        REPLAY,
        GAME_OBJECT_CHANGE,
        GAME_PAUSE
    }
    
    // Object properties
    private double                              nTimeReal_ms;
    private double                              nTimeGame_ms;
    private double                              nTimePlayLoop;
    private EventType                           nEventType;
    private boolean                             bClient;
    private boolean                             bHandledByOriginator;
    private ConcurrentHashMap<String, Object>   oArgs;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_ObjectEvent Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new game world event
     * 
     * ARGUMENTS:       nNewEventType - An indication of what type of event this is
     *                  oNewEventArgs - Extra arguments, further describing 
     *                                  something we need to track about this event
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_ObjectEvent (
        EventType nNewEventType, 
        ConcurrentHashMap<String, Object> oNewEventArgs
    ) {
        
        // Set values
        try {
            this.nTimeReal_ms =         HW03_Time_Real.getInstance().getTime();
            this.nTimeGame_ms =         HW03_Time_Game.getInstance().getTime();
            this.nTimePlayLoop =        HW03_Time_Loop.getPlayInstance().getTime();
            this.nEventType =           nNewEventType;
            this.oArgs =                oNewEventArgs;
            this.bClient =              HW03_Globals.bClient;
            this.nPlayerID =            this.bClient ? HW03_Globals.nPlayerID : -1;
            this.bHandledByOriginator = false;
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        compareTo
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Override comparison function
     *                  so that event objects can be compared (in support of priority queue)
     * 
     * ARGUMENTS:       oOtherEvent - Another event object we are comparing ourselves to
     * 
     * RETURNS:         nComparisonValue -  Negative if this event is "less than" the other event
     *********************************************************************************************************/
    @Override
    public int compareTo (HW03_ObjectEvent oOtherEvent) {
        int nComparisonValue = 0;
        try {

            // Prioritize by event type
            if (this.getEventTypeValue() > oOtherEvent.getEventTypeValue()) {
                nComparisonValue = -1;
            }
            else if (this.getEventTypeValue() < oOtherEvent.getEventTypeValue()) {
                nComparisonValue = 1;
            }
            
            // Prioritize by time
            if (nComparisonValue == 0) {
                if (this.nTimeReal_ms < oOtherEvent.nTimeReal_ms) {
                    nComparisonValue = -1;
                }
                else if (this.nTimeReal_ms > oOtherEvent.nTimeReal_ms) {
                    nComparisonValue = 1;
                }
            }
            
            // Prioritize events that have not yet been handled by their originator
            if (nComparisonValue == 0) {
                if (this.getHandledFlag() == false && oOtherEvent.getHandledFlag() == true) {
                    nComparisonValue = -1;
                }
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nComparisonValue;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getHandledFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public boolean getHandledFlag () {
        return this.bHandledByOriginator;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setHandledFlag
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Setter (set a flag indicating that the event has been handled by its originator)
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setHandledFlag () {
        this.bHandledByOriginator = true;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getEventTimeReal
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nEventTimeReal -   Time the event occurred (REAL)
     *********************************************************************************************************/
    public double getEventTimeReal () {
        return this.nTimeReal_ms;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getEventTimeGame
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nEventTimeReal -   Time the event occurred (GAME)
     *********************************************************************************************************/
    public double getEventTimeGame () {
        return this.nTimeGame_ms;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getEventTimeLoop
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         getEventTimeLoop -   Time the event occurred (PLAY LOOP ITERATION)
     *********************************************************************************************************/
    public double getEventTimeLoop () {
        return this.nTimePlayLoop;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getEventType
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         sEventType -   Type of event
     *********************************************************************************************************/
    public EventType getEventType () {
        return this.nEventType;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getEventArguments
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         sEventType -   Type of event
     *********************************************************************************************************/
    public ConcurrentHashMap<String, Object> getEventArguments () {
        return this.oArgs;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getEventTypeValue
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get some first guess about the importance of an event based on its type
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         nEventTypeValue -   Bigger number = more important event
     *********************************************************************************************************/
    private int getEventTypeValue () {
        int nEventTypeValue = 0;
        try {

            switch (this.nEventType) {
                case WILDCARD:
                    nEventTypeValue = 0;
                    break;
                case ADMIN:
                    nEventTypeValue = 1;
                    break;
                case USER_INPUT:
                    nEventTypeValue = 2;
                    break;
                case SCORE_CHANGE:
                    nEventTypeValue = 4;
                    break;
                case SPAWN:
                    nEventTypeValue = 4;
                    break;
                case REPLAY:
                    nEventTypeValue = 4;
                    break;
                case GAME_OBJECT_CHANGE:
                    nEventTypeValue = 4;
                    break;
                case GAME_PAUSE:
                    nEventTypeValue = 4;
                    break;
                case COLLISION:
                    nEventTypeValue = 5;
                    break;
                default:
                    throw new Exception ("Unknown event type " + this.nEventType);
            }

        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nEventTypeValue;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        toString
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Override method (used for debugging)
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         String describing event
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    public String toString () {
        
        // Declare variables
        boolean bCollisionEvent;
        String sFirstCollidedObjectType = "";
        
        // Get first collided object if applicable
        bCollisionEvent = this.getEventType() == HW03_ObjectEvent.EventType.COLLISION;
        if (bCollisionEvent == true) {
            sFirstCollidedObjectType = 
                ((CopyOnWriteArrayList<HW03_ObjectCollidable>) this.getEventArguments().get("aoCollidingObjects")).get(0).getType();
        }
        
        // Build & return string
        return 
                new Date() +
                ", Object Type " + 
                this.getType() + 
                ", Event Type " + 
                this.getEventType() + 
                ", Player ID " + 
                this.getPlayerID() + 
                ", Handled Flag " + 
                this.getHandledFlag() + 
                (bCollisionEvent ? (", First Collided Object " + sFirstCollidedObjectType) : "");
    
    }

}
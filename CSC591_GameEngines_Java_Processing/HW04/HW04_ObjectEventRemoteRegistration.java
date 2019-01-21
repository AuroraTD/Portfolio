/*************************************************************************************************************
 * FILE:            HW04_ObjectEventRemoteRegistration.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a game world event registration request
 *                  to be communicated over the network (client-server)
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.ConcurrentHashMap;

// CLASS DEFINITION
public class HW04_ObjectEventRemoteRegistration extends HW04_ObjectCommunicable {
    
    // Required for serializable class
    private static final long serialVersionUID = 1L;
    
    // Private variables
    private HW04_ObjectEvent.EventType nEventType;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_ObjectEventRemoteRegistration Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new game world event registration request
     * 
     * ARGUMENTS:       nEventTypeOfInterest -    The event type in which we are interested
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_ObjectEventRemoteRegistration (HW04_ObjectEvent.EventType nEventTypeOfInterest) {
        
        // Set values
        try {
            this.nEventType = nEventTypeOfInterest;
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
        
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
     * RETURNS:         nEventType -   The type of event in which a client or the server is interested
     *********************************************************************************************************/
    public HW04_ObjectEvent.EventType getEventType () {
        return this.nEventType;
    }

}
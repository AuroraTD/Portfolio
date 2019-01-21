/*************************************************************************************************************
 * FILE:            HW04_NetworkPartnerProxy.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class to act as a proxy for a network partner (the server or another client).
 *                  Objects of this class exist to handle events raised locally which this network partner cares about,
 *                  by sending such an event to the network parter.
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.ConcurrentHashMap;

// CLASS DEFINITION
public class HW04_NetworkPartnerProxy implements HW04_EventObserver {
    
    // Private variables
    private static ConcurrentHashMap<Integer, HW04_NetworkPartnerProxy> oProxies = 
        new ConcurrentHashMap<Integer, HW04_NetworkPartnerProxy>();;
    private int nNetworkPartnerPlayerID = -1;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_NetworkPartnerProxy Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new network partner proxy
     * 
     * ARGUMENTS:       nPartnerPlayerID -  The player ID of a client
     *                                      Or -1 if the partner is the server
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_NetworkPartnerProxy (int nPartnerPlayerID) {
        try {
            this.nNetworkPartnerPlayerID = nPartnerPlayerID;
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getInstance
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter for singleton instance
     * 
     * ARGUMENTS:       nPartnerPlayerID -  The player ID of a client
     *                                      Or -1 if the partner is the server
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static HW04_NetworkPartnerProxy getInstance (int nPartnerPlayerID) {
        if (oProxies.containsKey(nPartnerPlayerID) == false) {
            oProxies.put(nPartnerPlayerID, new HW04_NetworkPartnerProxy(nPartnerPlayerID));
        }
        return oProxies.get(nPartnerPlayerID);
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
    @Override
    public synchronized void handleEvent (HW04_ObjectEvent oEventToHandle) {
        try {
            
            /* Send the event to the network partner
             *  But avoid ping-ponging an event back and forth between two network partners forever
             *      - Do not send an event back to the partner that originated it
             *      - Do not send an event to yourself
             *      - If you are a client, do not send an event to the server unless you originated it
             */
            if (
                oEventToHandle.getPlayerID() != this.nNetworkPartnerPlayerID &&
                HW04_Globals.nPlayerID != this.nNetworkPartnerPlayerID &&
                (
                    HW04_Globals.bClient == false ||
                    HW04_Globals.nPlayerID == oEventToHandle.getPlayerID()
                )
            ) {
                HW04_Globals.oObjectsToWrite.get(this.nNetworkPartnerPlayerID).add(oEventToHandle);
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }

}
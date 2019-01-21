/*************************************************************************************************************
 * FILE:            HW03_NetworkPartnerProxy.java
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
public class HW03_NetworkPartnerProxy implements HW03_EventObserver {
    
    // Private variables
    private static ConcurrentHashMap<Integer, HW03_NetworkPartnerProxy> oProxies = 
        new ConcurrentHashMap<Integer, HW03_NetworkPartnerProxy>();;
    private int nNetworkPartnerPlayerID = -1;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_NetworkPartnerProxy Constructor
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
    public HW03_NetworkPartnerProxy (int nPartnerPlayerID) {
        try {
            this.nNetworkPartnerPlayerID = nPartnerPlayerID;
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
     * ARGUMENTS:       nPartnerPlayerID -  The player ID of a client
     *                                      Or -1 if the partner is the server
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static HW03_NetworkPartnerProxy getInstance (int nPartnerPlayerID) {
        if (oProxies.containsKey(nPartnerPlayerID) == false) {
            oProxies.put(nPartnerPlayerID, new HW03_NetworkPartnerProxy(nPartnerPlayerID));
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
    public synchronized void handleEvent (HW03_ObjectEvent oEventToHandle) {
        try {
            
            /* Send the event to the network partner
             *  But avoid ping-ponging an event back and forth between two network partners forever
             *      - Do not send an event back to the partner that originated it
             *      - If you are a client, do not send an event to the server unless you originated it
             */
            if (
                oEventToHandle.getPlayerID() != this.nNetworkPartnerPlayerID &&
                (
                    HW03_Globals.bClient == false ||
                    HW03_Globals.nPlayerID == oEventToHandle.getPlayerID()
                )
            ) {
                HW03_Globals.oObjectsToWrite.get(this.nNetworkPartnerPlayerID).add(oEventToHandle);
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

}
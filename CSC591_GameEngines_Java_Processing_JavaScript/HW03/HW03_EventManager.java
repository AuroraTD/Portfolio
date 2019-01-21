/*************************************************************************************************************
 * FILE:            HW03_EventManager.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class to manage events.
 *                  This class supports TWO DIFFERENT event management schemes.
 *                  1 - Distributed Event Management
 *                      Description from assignment
 *                          "In this design, both the client and the server are to implement all features of event handling
 *                          (registration, queuing, and scheduling handling)."
 *                      Implementation
 *                          This is considered the normal / default event management scheme.
 *                          Event Registration
 *                              If a client or server object is interested in an event type, 
 *                              it calls an event manager method to register for notifications about that event type.  
 *                              Optionally, it may also register its interest in that event type from other network partners 
 *                              (server / clients).  
 *                              In this case, a remote registration object is communicated out.
 *                              When the network partner receives that remote registration object, 
 *                              it registers a network partner proxy as an object that is interested in the event type.
 *                              The event manager tracks registration by means of a hashmap 
 *                              whose keys are event types and whose values are a collection of interested objects.
 *                          Event Raising
 *                              If a client or server object wishes to raise an event, it calls an event manager method 
 *                              which creates a timestamped event object and adds that object to a prioritized queue.
 *                              Events are prioritized first by event type and then by timestamp 
 *                              (with recent events being higher priority).
 *                          Event Scheduling
 *                              A separate event manager thread simply pulls an event from the prioritized queue, 
 *                              handles it, pulls another event, handles it, etc.
 *                              Depending on how fast events are raised and how fast the code to handle them runs,
 *                              we could have almost-always-nearly-empty queue, or we could have often-quite-full queue.
 *                              Event prioritization ensures that the most important events are handled first.
 *                          Event Handling
 *                              When the event manager handles an event, it consults gets from the 
 *                              event-type-as-key hashmap a collection of all interested objects.
 *                              It then calls the event handling method of that object, 
 *                              which is implemented as part of the event observer interface.
 *                              It then does the same for the collection of all interested objects which are
 *                              associated with the event type of "wildcard".
 *                              Earlier, it was noted that during event registration, 
 *                              an object may register interest in an event type from other network partners.
 *                              In this case, the event manager may find a network partner proxy object 
 *                              in the collection of objects that is interested in an event.
 *                              To the event manager, this is just another interested object.
 *                              The network partner proxy object will handle the event by 
 *                              communicating it to the appropriate network partner.
 *                  2 - Server-Centric Event Management
 *                      Description from assignment
 *                          "In this design, event registration, event queuing, and the scheduling of event handling 
 *                          should be done on the server. 
 *                          Events can be raised and handled on either clients or the server."
 *                      Implementation
 *                          Here we discuss only those aspects which differ from Distributed Event Management.
 *                          Event Registration
 *                              This functionality is the same as in Distributed Event Management.
 *                              Even in a Server-Centric scheme, a Client still must have some means 
 *                              of tracking which Client objects are interested in which event types.
 *                          Event Raising
 *                              Instead of adding the new event object to a prioritized queue,
 *                              a client simply sends the event to the server for management.
 *                          Event Scheduling
 *                              Because the client has send the event to the server for management,
 *                              it is the server which performs event scheduling.
 *                              If prioritization of events as described for Distributed Event Management
 *                              results in two events with the same priority, the tie is broken 
 *                              in favor of the event that has not yet been handled by its originator 
 *                              (that is, the client who has passed its event to the server for management).
 *                          Event Handling
 *                              The server handles events which have been sent to it for management by an originating client
 *                              both in the normal way (if there are any objects which have registered interest in the event),
 *                              and by sending the event back to the originating client for handling.
 *                              When a client receives such an "echo" event, 
 *                              it handles the event immediately rather than queueing it.
 *                      LIMITATIONS
 *                          Bugs
 *                              As noted by the instructor, it is NOT required that the game be 
 *                              fully debugged in the alternative event management scheme.
 *                              Indeed, gameplay is buggy in Server-Centric Event Management.
 *                              These bugs could be worked out, but that is not a requirement of the assignment.
 *                          Delays
 *                              It is expected that events will be handled less efficiently in the server-centric model,
 *                              because any event raised by a client must first be communicated to the server,
 *                              then be scheduled for handling, then be communicated back to the client, and finally be handled.
 *                              However, the delays are surprisingly long.
 *                              Through debug print statements (since removed from the code),
 *                              it was confirmed that the delays are occurring between the server 
 *                              placing the "echo" event on the output stream, 
 *                              and the client receiving the "echo" event on the input stream.
 *                              This delay is thought to be due to the streams getting overwhelmed 
 *                              by a huge number of events flying back and forth.
 *                              Events are generated quite often in the game.
 *                              One example is that due to "gravity", character objects are always attempting to move down,
 *                              and usually are stopped by colliding with platforms.
 *                              In other words, there are collision events being raised on nearly every game loop iteration.
 *                              In the Distributed Event Management model, these collisions are quickly handled locally by the client.
 *                              In the Server-Centric Event Management model, these collisions (and any knock-on events after them) 
 *                              trigger network traffic, resulting in extreme inefficiencies.
 *************************************************************************************************************/

// IMPORTS
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

// CLASS DEFINITION
public class HW03_EventManager implements Runnable {
    
    // Private variables
    private static HW03_EventManager oInstance = null;
    private static PriorityBlockingQueue<HW03_ObjectEvent> oEventQueue = 
        new PriorityBlockingQueue<HW03_ObjectEvent>();
    private static ConcurrentHashMap<HW03_ObjectEvent.EventType, CopyOnWriteArrayList<Object>> oObservers = 
        new ConcurrentHashMap<HW03_ObjectEvent.EventType, CopyOnWriteArrayList<Object>>();
    private static ConcurrentHashMap<HW03_ObjectEvent.EventType, CopyOnWriteArrayList<Integer>> oServerEventRegistrationTracker = 
            new ConcurrentHashMap<HW03_ObjectEvent.EventType, CopyOnWriteArrayList<Integer>>();
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_EventManager Constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Constructor for a new event manager
     * 
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_EventManager () {
        try {
            // Nothing to do here
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        run
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A function that can be run as a separate thread.
     *                  This way, event handling should have a minimized effect on the rest of the program.
     *                  Take the highest-priority event from the priority queue and handle it.
     *                  Repeat ad infinitum.
     *                  Depending on how fast events are raised and how fast the code to handle them runs,
     *                      could have almost-always-nearly-empty queue, and that's fine,
     *                      or could have often-quite-full queue, and that's less fine but what can we do.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void run () {
        try {
            
            while (true) {
                
                /* Get highest priority event from priority queue and handle it
                 * .poll() is the non-blocking version (returns even if queue empty)
                 * .take() is the blocking version (waits for queue to be non-empty)
                 */
                handleEvent(oEventQueue.take());
                
            }
            
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
    public static HW03_EventManager getInstance () {
        if (oInstance == null) {
            // Create instance
            oInstance = new HW03_EventManager();
            // Start thread that actually handles events
            (new Thread(oInstance)).start();
        }
        return oInstance;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        raiseEvent
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise a new event
     * 
     * ARGUMENTS:       nNewEventType -     An indication of what type of event this is
     *                  oNewEventArgs -     Extra arguments, further describing 
     *                                      something we need to track about this event
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    public void raiseEvent (
        HW03_ObjectEvent.EventType nNewEventType, 
        ConcurrentHashMap<String, Object> oNewEventArgs
    ) {
        try {
            
            // Declare variables
            HW03_ObjectEvent oCreatedEvent;
            
            // Create event
            oCreatedEvent = new HW03_ObjectEvent(nNewEventType, oNewEventArgs);
            
            /* If we are a client raising an event for the first time in a server-centric model,
             *  then the server is supposed to queue and handle this event
             */
            if (
                HW03_Globals.bClient == true &&
                HW03_Globals.bStandardEventManagementProtocol == false
            ) {
                HW03_Globals.oObjectsToWrite.get(-1).add(oCreatedEvent);
            }
            
            /* Under normal circumstances, 
             *  Add the event object to the priority queue
             *  Separate thread will deal with the event according to its priority
             */
            else {
                oCreatedEvent.setHandledFlag();
                oEventQueue.add(oCreatedEvent);
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        reRaiseEvent
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Re-raise an event for local handling (after received from network partner)
     * 
     * ARGUMENTS:       oReceivedEvent -    The event that was received and should now be handled locally
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    public void reRaiseEvent (HW03_ObjectEvent oReceivedEvent) {
        try {
            
            /* This is an event we originated
             *  Handle it immediately 
             *  (it's already been managed / prioritized by the main event manager in the system)
             */
            if (
                HW03_Globals.bClient == true &&
                HW03_Globals.bStandardEventManagementProtocol == false &&
                oReceivedEvent.getPlayerID() == HW03_Globals.nPlayerID
            ) {
                oReceivedEvent.setHandledFlag();
                handleEvent(oReceivedEvent);
            }
            
            /* This is an event someone else originated
             *  Add to the queue to get handled when it is highest priority
             */
            else {
                oEventQueue.add(oReceivedEvent);
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        registerForEvents
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Register interest in certain events
     * 
     * ARGUMENTS:       nEventTypeOfInterest -  An indication of what type of event the caller is interested in
     *                  oObjectInterested -     A reference to the interested object
     *                  bGameWide -             If true, interested in this event game-wide 
     *                                          (if raised by the server or any client).
     *                                          If false, interested in this event only if this server/client 
     *                                          raised the event.
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void registerForEvents (
        HW03_ObjectEvent.EventType nEventTypeOfInterest, 
        Object oObjectInterested,
        boolean bGameWide
    ) {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<Object> aoObservers;
            HW03_ObjectEventRemoteRegistration oEventRegistrationRequest;
            
            // Add to observers interested in this event type
            if (oObservers.containsKey(nEventTypeOfInterest)) {
                oObservers.get(nEventTypeOfInterest).add(oObjectInterested);
            }
            else {
                aoObservers = new CopyOnWriteArrayList<Object>();
                aoObservers.add(oObjectInterested);
                oObservers.put(nEventTypeOfInterest, aoObservers);
            }
            
            // If interested in this event even from other places in the network, send registration out
            if (bGameWide == true) {
                
                // Create the registration request
                oEventRegistrationRequest = new HW03_ObjectEventRemoteRegistration(nEventTypeOfInterest);
                
                // If we are the client, send the request to the server
                if (HW03_Globals.bClient) {
                    HW03_Globals.oObjectsToWrite.get(-1).add(oEventRegistrationRequest);
                }
                
                /* If we are the server, send the request to all clients
                 *  It's okay if no clients exist yet
                 *  Either way, remember our interest so that we may send the requests to any clients who join in the future
                 */
                else {
                    // Make sure we have the ability to track our interest in this event type
                    if (oServerEventRegistrationTracker.contains(nEventTypeOfInterest) == false) {
                        oServerEventRegistrationTracker.put(nEventTypeOfInterest, new CopyOnWriteArrayList<Integer>());
                    }
                    // Proceed if we have anybody to talk to
                    if (HW03_Globals.oObjectsToWrite != null) {
                        // Add the event registration request object to each connected client's queue of objects to write
                        for (BlockingQueue<HW03_ObjectCommunicable> value : HW03_Globals.oObjectsToWrite.values()){
                            value.add(oEventRegistrationRequest);
                        }
                        // Keep track of the fact that we have sent this registration request to these clients
                        for (Integer key : HW03_Globals.oObjectsToWrite.keySet()){
                            oServerEventRegistrationTracker.get(nEventTypeOfInterest).add(key);
                        }
                    }
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        reregisterForEventsWithNewClient
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A new client has connected, 
     *                  so we need to tell them about all the events we are interested in.
     *                  This method is intended to be called only by the server
     * 
     * ARGUMENTS:       nNewClientPlayerID -    The player ID of the newly connected client
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void reregisterForEventsWithNewClient (int nNewClientPlayerID) {
        try {
            
            // Declare variables
            HW03_ObjectEvent.EventType nEventType;
            CopyOnWriteArrayList<Integer> anClientsAlreadyNotified;
            int anClientAlreadyNotifiedPlayerID;
            int i;
            
            // Check for problems
            if (HW03_Globals.bClient == true) {
                throw new Exception("Called a method from a Client that is intended for use only by the Server");
            }
            
            // Send event registration requests to the newly connected client
            else {
                for (ConcurrentHashMap.Entry<HW03_ObjectEvent.EventType, CopyOnWriteArrayList<Integer>> oEntry : oServerEventRegistrationTracker.entrySet()) {
                    nEventType = oEntry.getKey();
                    anClientsAlreadyNotified = oEntry.getValue();
                    HW03_Globals.oObjectsToWrite.get(nNewClientPlayerID).add(new HW03_ObjectEventRemoteRegistration(nEventType));
                    anClientsAlreadyNotified.add(nNewClientPlayerID);
                }
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getTimestampEventQueue
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get the timestamp of the oldest unhandled event
     * 
     * ARGUMENTS:       oTimeReal_ms -  The timestamp of the oldest unhandled event, expressed in real time
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public double getTimestampEventQueue () {
        double oTimeReal_ms = -1;
        try {
            if (oEventQueue.isEmpty() == false) {
                oTimeReal_ms = oEventQueue.peek().getEventTimeReal();
            }
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return oTimeReal_ms;
    }
        
    /*********************************************************************************************************
     * FUNCTION:        handleEvent
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle an event that has been taken from the event queue,
     *                  or handle an event that bypassed the event queue and is handled immediately
     *
     * ARGUMENTS:       oEventToHandle -    The event to handle
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    public void handleEvent (HW03_ObjectEvent oEventToHandle) {
        try {

            // Declare variables
            CopyOnWriteArrayList<Object> aoObservers;
            int i;
            
            /* We might need to send this back to the originator
             *  If the originator hasn't even handled their own event yet,
             *  then certainly they need to hear about it in order to handle it
             */
            if (
                HW03_Globals.bClient == false &&
                HW03_Globals.bStandardEventManagementProtocol == false && 
                oEventToHandle.getHandledFlag() == false
            ) {
                HW03_Globals.oObjectsToWrite.get(oEventToHandle.getPlayerID()).add(oEventToHandle);
            }

            /* Pass to interested parties
             * Interested party might be a locally-existing object
             * Or might be a network partner proxy object,
             * in which case the event should be sent over the network to that partner
             */
            // Get event type
            aoObservers = oObservers.get(oEventToHandle.getEventType());
            if (aoObservers != null) {
                for (i = 0; i < aoObservers.size(); i++) {
                    ((HW03_EventObserver) aoObservers.get(i)).handleEvent(oEventToHandle);
                } 
            }
            
            /* Pass to parties interested in all events
             * Interested party might be a locally-existing object
             * Or might be a network partner proxy object,
             * in which case the event should be sent over the network to that partner
             */
            aoObservers = oObservers.get(HW03_ObjectEvent.EventType.WILDCARD);
            if (aoObservers != null) {
                for (i = 0; i < aoObservers.size(); i++) {
                    ((HW03_EventObserver) aoObservers.get(i)).handleEvent(oEventToHandle);
                }
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    // CONVENIENCE - WRAPPER METHODS

    /*********************************************************************************************************
     * FUNCTION:        raiseEventAdmin
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise a generic "admin" event that just has a string description
     * 
     * ARGUMENTS:       sDescription - A simple description of an interesting thing that has happened
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventAdmin (String sDescription) {
        
        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put(sDescription, "");
        this.raiseEvent(HW03_ObjectEvent.EventType.ADMIN, oEventArguments);
        
    }

    /*********************************************************************************************************
     * FUNCTION:        raiseEventUserInput
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise a user input event
     * 
     * ARGUMENTS:       sKey -          The key that was pressed or released
     *                  bPressed -      True if pressed, false if released
     *                  nReplaySpeed -  Desired replay speed
     *                                  Or -1 if the user input is not related to replay speed 
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventUserInput (String sKey, boolean bPressed, float nReplaySpeed) {

        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put("sKey", sKey);
        oEventArguments.put("bPressed", (bPressed ? "TRUE" : "FALSE"));
        oEventArguments.put("nReplaySpeed", nReplaySpeed);
        this.raiseEvent(HW03_ObjectEvent.EventType.USER_INPUT, oEventArguments);
        
    }

    /*********************************************************************************************************
     * FUNCTION:        raiseEventCollision
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise a collision event
     * 
     * ARGUMENTS:       oMovedObject -          The game object we tried to move
     *                  aoCollidingObjects -    Objects with which it collided
     *                  nOriginalX -            X-axis location before the move that caused the collision
     *                  nOriginalY -            Y-axis location before the move that caused the collision
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventCollision (
        HW03_ObjectGame oMovedObject, 
        CopyOnWriteArrayList<HW03_ObjectCollidable> aoCollidingObjects, 
        float nOriginalX, 
        float nOriginalY
    ) {
        
        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put("oMovedObject", oMovedObject);
        oEventArguments.put("aoCollidingObjects", aoCollidingObjects);
        oEventArguments.put("nOriginalX", nOriginalX);
        oEventArguments.put("nOriginalY", nOriginalY);
        this.raiseEvent(HW03_ObjectEvent.EventType.COLLISION, oEventArguments);
        
    }

    /*********************************************************************************************************
     * FUNCTION:        raiseEventScoreChange
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise a score change event
     * 
     * ARGUMENTS:       oCharacterObject -      The character whose score has changed
     *                  bScoreIncrement -       True if the score went up, false if it went down
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventScoreChange (HW03_ObjectCharacter oCharacterObject, boolean bScoreIncrement) {

        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put("oCharacterObject", oCharacterObject);
        oEventArguments.put("bScoreIncrement", (bScoreIncrement ? "TRUE" : "FALSE"));
        this.raiseEvent(HW03_ObjectEvent.EventType.SCORE_CHANGE, oEventArguments);
        
    }

    /*********************************************************************************************************
     * FUNCTION:        raiseEventSpawn
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise a character spawn event
     * 
     * ARGUMENTS:       oCharacterObject -      The character who should (re)spawn
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventSpawn (HW03_ObjectCharacter oCharacterObject) {
        
        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put("oCharacterObject", oCharacterObject);
        this.raiseEvent(HW03_ObjectEvent.EventType.SPAWN, oEventArguments);
        
    }

    /*********************************************************************************************************
     * FUNCTION:        raiseEventReplay
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise an event related to replays
     * 
     * ARGUMENTS:       nReplayEventType -  Sub-type of event describing what kind of replay event this is
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventReplay (HW03_Replay.ReplayEventType nReplayEventType) {
        
        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put("nReplayEventType", nReplayEventType);
        this.raiseEvent(HW03_ObjectEvent.EventType.REPLAY, oEventArguments);
        
    }

    /*********************************************************************************************************
     * FUNCTION:        raiseEventGameObjectChange
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise an event for a game object change
     *                  Why is there no "@Override" below?
     *                      Great question!
     *                      Eclipse is claiming that this class cannot be resolved with it present
     *                      Compilation works with or without it
     *                      This definitely is overriding a method from the interface
     *                      Just leaving it this way and moving on, I can't debug Eclipse
     * 
     * ARGUMENTS:       oGameObject -  A game object that has changed in some way
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventGameObjectChange (HW03_ObjectGame oGameObject) {

        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put("oGameObject", oGameObject);
        this.raiseEvent(HW03_ObjectEvent.EventType.GAME_OBJECT_CHANGE, oEventArguments);
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        raiseEventGamePause
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Raise an event for a game pause
     *                  Any client can raise this event and it should 
     *                  percolate out to the server and other clients.
     * 
     * ARGUMENTS:       bPaused -   True if the client is pausing the game,
     *                              False if the client is unpausing the game.
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void raiseEventGamePause (boolean bPaused) {

        // Declare variables
        ConcurrentHashMap<String, Object> oEventArguments;
        
        // Raise event
        oEventArguments = new ConcurrentHashMap<String, Object>();
        oEventArguments.put("bPaused", (bPaused ? "TRUE" : "FALSE"));
        this.raiseEvent(HW03_ObjectEvent.EventType.GAME_PAUSE, oEventArguments);
        
    }

}
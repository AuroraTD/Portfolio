/*************************************************************************************************************
 * FILE:            HW03_Server.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     The server application initializes the game world,
 *                      and keeps track of where all clients' character objects are within the world.
 *                  Synchronized blocks are used to control access to certain object.
 *                      In general, an effort is made to minimize the use of synchronized blocks,
 *                      and to synchronize on the correct (smallest scope) objects,
 *                      in other words to use just enough synchronization to safeguard against problems.
 *************************************************************************************************************/

// IMPORTS
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW03_Server extends PApplet implements Runnable {
    
    // Constants
    private static final HW03_EventManager                          O_EVENT_MANAGER =       HW03_EventManager.getInstance();
    private static final String                                     S_TIMER_PERFORMANCE =   "Performance";
    private static final String                                     S_TIMER_INIT =          "Init";
    private static final String                                     S_TIMER_CLIENT_WAIT =   "ClientWait";

    //  Object properties
    private static ConcurrentHashMap<Integer, ObjectOutputStream>   oStreamsOut;
    private static ConcurrentHashMap<Integer, ObjectInputStream>    oStreamsIn;
    private static ServerSocket                                     oServerSocket;
    private static boolean                                          bQuit;
    
    private static int                                              nClients =              0;
    
    String                                                          sThreadType;
    int                                                             nThreadClientPlayerID;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Server constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A function that creates an object with a .run() method (used for threads).
     *                  The .run() method can serve one of three functions:
     *                      Accept -    Accept connections from new clients
     *                      Read -      Read from a client.
     *                      Write -     Write to a client.
     *
     * ARGUMENTS:       sType - "Accept", "Read", or "Write"
     *                  nClientPlayerID -   The player ID of the client that the .run() method is concerned with.
     *                                      Not used in the case of "Accept".
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Server (String sType, int nClientPlayerID) {
        this.sThreadType = sType;
        this.nThreadClientPlayerID = nClientPlayerID;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Server constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A second constructor, this exists to accommodate the call:
     *                  PApplet.main("HW03_Server");
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Server () {
        // Nothing to do here, this exists to accommodate the call: PApplet.main("HW03_Server");
    }
    
    /*********************************************************************************************************
     * FUNCTION:        main
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Rough flow of this function:
     *                      Hello World
     *                      Initialize collections
     *                      Create game world objects
     *                      Start thread to listen for new client connections
     *                      Start game loop (draw function of Processing API)
     *
     * ARGUMENTS:       0 - Frame rate
     *                      Default = 60
     *                  1 - Event Management protocol
     *                      0 means distributed
     *                      1 means server-centric
     *                      Default = 0
     *                  2 - Performance Test
     *                      0 means do not run performance test
     *                      1 means do run performance test
     *                      Default = 0
     *                  3 - Number of game loop iterations to run performance testing on
     *                      If and only if this number is > 0,
     *                      the program will automatically shut down 
     *                      after the specified number of iterations.
     *                      Default = 0
     *                  4 - Number of static platforms to include in the game world
     *                      Default = 3
     *                  5 - Number of moving platforms to include in the game world
     *                      Default = 6
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void main (String[] args) {
        try {
            
            // Declare variables
            HW03_Server oServerInstanceAccept;
            HW03_Logger oLoggerInstance;
            
            // Check arguments
            if (args.length > 7) {
                System.out.println("\nUsage: java -classpath .:./core.jar HW03_Server [see source code for optional arguments]");
            }
            else {
                
                // Save arguments
                HW03_Globals.bClient = false;
                if (args.length > 0) {
                    HW03_Globals.nFrameRate = Integer.parseInt(args[0]);
                }
                if (args.length > 1) {
                    HW03_Globals.bStandardEventManagementProtocol = Integer.parseInt(args[1]) == 0;
                }
                if (args.length > 2) {
                    HW03_Globals.bRunPerformanceTest = Integer.parseInt(args[2]) > 0;
                }
                if (args.length > 3) {
                    HW03_Globals.nPerformanceTestIterations = Integer.parseInt(args[3]);
                }
                if (args.length > 4) {
                    HW03_Globals.nNumPlatformsStatic = Integer.parseInt(args[4]);
                }
                if (args.length > 5) {
                    HW03_Globals.nNumPlatformsDynamic = Integer.parseInt(args[5]);
                }
                
                // Start logger
                oLoggerInstance = new HW03_Logger();
                (new Thread(oLoggerInstance)).start();
                
                // Start game time
                HW03_Time_Game.getInstance().start();
                HW03_Time_Loop.getPlayInstance().setTickSize(1);
                HW03_Time.startTimer(S_TIMER_INIT);
                
                // Hello World
                instructUser();
                
                // Assume the user does not want to quit until we know otherwise
                bQuit = false;
                
                // Initialize thread-safe collections
                oStreamsOut =                   new ConcurrentHashMap<Integer, ObjectOutputStream>();
                oStreamsIn =                    new ConcurrentHashMap<Integer, ObjectInputStream>();
                HW03_Globals.oObjectsToWrite =  new ConcurrentHashMap<Integer, BlockingQueue<HW03_ObjectCommunicable>>();
                
                // Add status summary
                new HW03_ObjectStatusSummary(-1, null);
                
                // Add instructions
                new HW03_ObjectStatusInstructions(-1, null);
                
                // Add scoreboard
                new HW03_ObjectStatusScoreboard(-1, null);
                
                // Add boundary objects
                addBoundaryObjects();
                
                // Add platform objects
                addPlatformObjects();
                
                // Add spawn points
                addSpawnPoints();
                
                // Add death zones
                addDeathZones();
                
                // Add win zones
                addWinZones();
                
                // Create a server socket
                oServerSocket = new ServerSocket(HW03_Utility.setPortNumber());

                // Start a thread to listen for new client connections
                oServerInstanceAccept = new HW03_Server("Accept", -1);
                (new Thread(oServerInstanceAccept)).start();
                
                /* Start a PApplet application and tell it to use our class
                 * This will run settings() and setup() and then will start running draw() continuously
                 * So this comes after communication is established with the server
                 */
                PApplet.main("HW03_Server");
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        instructUser
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Instruct the user on how to play the game
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private static void instructUser () {
        try {
            
            // Hello World
            System.out.println("\nWelcome to SPP (simplest possible platformer) 3.0!");
            if (HW03_Globals.bRunPerformanceTest == true) {
                System.out.println("Will run performance test");
            }
            else {
                System.out.println("Normal gameplay (no performance test)");
            }
            if (HW03_Globals.nPerformanceTestIterations > 0) {
                System.out.println("Will automatically stop after " + HW03_Globals.nPerformanceTestIterations + " game loop iterations");
            }
            System.out.println(
                "Event Management Protocol: " + 
                (HW03_Globals.bStandardEventManagementProtocol ? "Distributed" : "Server-Centric")
            );
            System.out.println("Frame Rate: " + HW03_Globals.nFrameRate);
            System.out.println("Static Platforms: " + HW03_Globals.nNumPlatformsStatic);
            System.out.println("Moving Platforms: " + HW03_Globals.nNumPlatformsDynamic);
            System.out.println("\nOptions (click the window to give it focus first):");
            System.out.println("\t'Q': Quit");
            
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
     *                  Can serve one of three functions
     *                      Accept -    Accept connections from new clients
     *                      Read -      Read from a client.
     *                      Write -     Write to a client.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void run () {
        try {
            
            // ACCEPT NEW CLIENTS
            if (this.sThreadType.equals("Accept")) {
                threadAcceptNewClient();
            }
            
            // READ FROM CLIENT
            else if (this.sThreadType.equals("Read")) {
                threadReadFromClient(this.nThreadClientPlayerID); 
            }
            
            // WRITE TO CLIENT
            else if (this.sThreadType.equals("Write")) {
                threadWriteToClient(this.nThreadClientPlayerID);
            }
            
            // Unknown thread operation
            else {
                throw new Exception("Thread operation type '" + this.sThreadType + "' not recognized");
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        threadAcceptNewClient
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Accept connections from new clients
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void threadAcceptNewClient () {
        try {
            
            // Declare variables
            ConcurrentHashMap<Integer, HW03_ObjectGame> oObjectsGame;
            HW03_Server oServerInstanceRead;
            HW03_Server oServerInstanceWrite;
            Socket oSocket = null;
            HW03_ObjectCharacter oNewObjectCharacter;
            int nNewClientPlayerID;
            
            // Accept new client connections "forever"
            while (bQuit != true) {
                
                /* Wait for a connection from a new client (if the socket hasn't been closed)
                 * https://stackoverflow.com/questions/2983835/how-can-i-interrupt-a-serversocket-accept-method
                 */
                try {
                    oSocket = oServerSocket.accept();
                }
                catch (SocketException oError) {
                    // Make super sure everyone knows we're quitting
                    bQuit = true;
                }
                
                if (bQuit != true) {
                    
                    // Increment counter
                    nClients++;
                    
                    // Create a new character object for the newly connected client
                    oNewObjectCharacter = new HW03_ObjectCharacter(
                        -1,
                        -1,
                        // Give the player an x-axis location (will move to spawn point later)
                        0, 
                        // Give the player a y-axis location (will move to spawn point later)
                        0,
                        // Do not pass a window to draw in, server doesn't draw objects
                        null
                    );
                    
                    // Register this new object for events in which it is interested
                    O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.SPAWN, oNewObjectCharacter, false);
                    
                    // Print to terminal that we have a new client
                    System.out.println("Player " + oNewObjectCharacter.getPlayerID() + " has joined the game!");
                    
                    /* Synchronize here so that we can make sure the first thing the new client hears about is its own character
                     *  Otherwise the draw loop could pick up on the new output stream and tell the client about some moving platforms
                     */
                    synchronized (HW03_Server.class) {
                    
                        // Add this connection to our list of connections
                        nNewClientPlayerID = oNewObjectCharacter.getPlayerID();
                        oStreamsOut.put(nNewClientPlayerID, new ObjectOutputStream(oSocket.getOutputStream()));
                        oStreamsIn.put(nNewClientPlayerID, new ObjectInputStream(oSocket.getInputStream()));
                        
                        /* Move the new character object to a spawn point 
                         * (go ahead and handle this now so rapidly connecting clients don't get in each other's way)
                         */
                        O_EVENT_MANAGER.raiseEventSpawn(oNewObjectCharacter);
                        
                        /* Tell the client about the game world
                         * The first object we send, the client interprets as their character object
                         */
                        HW03_Globals.oObjectsToWrite.put(nNewClientPlayerID, new LinkedBlockingQueue<HW03_ObjectCommunicable>());
                        HW03_Globals.oObjectsToWrite.get(nNewClientPlayerID).add(oNewObjectCharacter);
                        
                        /* Start 2 new threads, to read from and write to our new client
                         * Do this after there is an appropriate entry in the hashmap from .put()
                         */
                        oServerInstanceRead = new HW03_Server("Read", nNewClientPlayerID);
                        (new Thread(oServerInstanceRead)).start();
                        oServerInstanceWrite = new HW03_Server("Write", nNewClientPlayerID);
                        (new Thread(oServerInstanceWrite)).start();
                        
                        // Now, tell the client about other game objects and about events we are interested in
                        oObjectsGame = HW03_ObjectGame.getGameObjects();
                        for (ConcurrentHashMap.Entry<Integer, HW03_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                            if (oEntry.getValue().getPlayerID() != nNewClientPlayerID) {
                                HW03_Globals.oObjectsToWrite.get(nNewClientPlayerID).add(oEntry.getValue());
                            }
                        }
                        oObjectsGame = null;
                        O_EVENT_MANAGER.reregisterForEventsWithNewClient(nNewClientPlayerID);

                        // Every other client needs to know about this new character object
                        notifyClientsAboutOneObject(nNewClientPlayerID, oNewObjectCharacter);
                        
                    }

                }

            }
            
            // Close socket
            try {
                oSocket.close();
            }
            catch (Throwable oError) {
                // Ignore
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        threadReadFromClient
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Read from a client
     *
     * ARGUMENTS:       nThreadClientPlayerID - The client's player ID
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void threadReadFromClient (int nThreadClientPlayerID) {
        try {
            
            // Declare variables
            HW03_ObjectCommunicable oObjectReadFromClient;
            boolean bClientStillExistsRead;
            
            // Read from this one client "forever"
            bClientStillExistsRead = true;
            while (bQuit != true && bClientStillExistsRead == true) {
                
                /* Figure out which input stream we're reading from
                 * Input streams come and go as clients connect and disconnect
                 * We want to get the one input stream that we were born for
                 * If it isn't there, die a natural death
                 */
                if (oStreamsIn.containsKey(nThreadClientPlayerID)) {
                    
                    /* Wait for this client to send us an object
                     *  https://stackoverflow.com/questions/30559373/socket-isclosed-returns-false-after-client-disconnected
                     */
                    oObjectReadFromClient = null;
                    try {
                        // Read object sent by client
                        oObjectReadFromClient = (HW03_ObjectCommunicable) oStreamsIn.get(nThreadClientPlayerID).readObject();
                    }
                    catch (Throwable oError) {
                        bClientStillExistsRead = false;
                        handleClientDeparture(nThreadClientPlayerID);
                    }
                    
                    /* ACT on what was read
                     *  Client should write about its own character object if / when that character object changes
                     *  Client should write about any event registration for events that it is interested in
                     *  Client should write about any events that this client is interested in
                     */
                    if (bClientStillExistsRead == true && oObjectReadFromClient != null) {
                        
                        // Cast object to more specific type
                        HW03_ObjectCommunicable.castObject((HW03_ObjectCommunicable) oObjectReadFromClient);
                        
                        // Deal with character object
                        if (oObjectReadFromClient instanceof HW03_ObjectCharacter) {

                            // Check for existence of client again, it might have changed while we were waiting
                            if (oStreamsIn.containsKey(nThreadClientPlayerID)) {
                                
                                // Update our own copy of the character object
                                HW03_ObjectGame.replaceObject((HW03_ObjectGame) oObjectReadFromClient);
                                
                                // Everyone else needs to know that this client updated
                                notifyClientsAboutOneObject(nThreadClientPlayerID, (HW03_ObjectGame) oObjectReadFromClient);
                                
                            }
                            else {
                                bClientStillExistsRead = false;
                                handleClientDeparture(nThreadClientPlayerID);
                            }
                            
                        }
                        
                        // Deal with event registration (that is, someone else in the network is interested in our events)
                        if (oObjectReadFromClient instanceof HW03_ObjectEventRemoteRegistration) {
                            
                            // Register to handle the specified event type locally
                            O_EVENT_MANAGER.registerForEvents(
                                // Note the event type that is of interest to someone else in the network
                                ((HW03_ObjectEventRemoteRegistration) oObjectReadFromClient).getEventType(),
                                // Note the event observer that will handle this interest should the event be raised locally
                                HW03_NetworkPartnerProxy.getInstance(((HW03_ObjectEventRemoteRegistration) oObjectReadFromClient).getPlayerID()),
                                // Do not propagate this network event registration any further in the network
                                false
                            );
                            
                        }
                        
                        // Deal with event (that is, we are interested in an event raised by someone else in the network)
                        if (oObjectReadFromClient instanceof HW03_ObjectEvent) {
                            // Raise the event locally so that it can be handled by any local interested objects
                            O_EVENT_MANAGER.reRaiseEvent((HW03_ObjectEvent) oObjectReadFromClient);
                        }
                        
                    }
                    
                }
                else {
                    bClientStillExistsRead = false;
                    handleClientDeparture(nThreadClientPlayerID);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        threadWriteToClient
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Write to a client
     *
     * ARGUMENTS:       nThreadClientPlayerID - The client's unique player ID
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void threadWriteToClient (int nThreadClientPlayerID) {
        try {
            
            // Declare variables
            boolean bClientStillExistsWrite;
            HW03_ObjectCommunicable oObjectToWrite;
            
            // Write to this one client "forever"
            bClientStillExistsWrite = true;
            while (bQuit != true && bClientStillExistsWrite) {
                
                /* Figure out which output stream we're writing to
                 * Output streams come and go as clients connect and disconnect
                 * We want to get the one output stream that we were born for
                 * If it isn't there, die a natural death
                 */
                if (oStreamsOut.containsKey(nThreadClientPlayerID)) {
                    
                    /* Wait for some information to write out
                     * .poll() is the non-blocking version (returns even if queue empty)
                     * .take() is the blocking version (waits for queue to be non-empty)
                     */
                    oObjectToWrite = HW03_Globals.oObjectsToWrite.get(nThreadClientPlayerID).take();
                    
                    /* Write all changed game objects to this client (if they haven't quit)
                     * https://stackoverflow.com/questions/41058548/why-an-object-doesnt-change-when-i-send-it-through-writeobject-method
                     */
                    if (oStreamsOut.containsKey(nThreadClientPlayerID)) {
                            
                        try {

                            // Write
                            oStreamsOut.get(nThreadClientPlayerID).reset();
                            oStreamsOut.get(nThreadClientPlayerID).writeObject(oObjectToWrite);
                            oStreamsOut.get(nThreadClientPlayerID).flush();

                        }
                        catch (Throwable oError) {
                            bClientStillExistsWrite = false;
                            handleClientDeparture(nThreadClientPlayerID);
                        }
                        
                    }
                    else {
                        // Make sure we don't loop again
                        bClientStillExistsWrite = false;
                        handleClientDeparture(nThreadClientPlayerID);
                    }
                    
                }
                else {
                    bClientStillExistsWrite = false;
                    handleClientDeparture(nThreadClientPlayerID);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        notifyClientsAboutOneObject
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Notify clients of the current status of one game object
     *
     * ARGUMENTS:       nExcludedPlayerID -     The player ID of a client to exclude from the notification (or -1)
     *                  oObjectToWrite -        A game object to write to clients
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void notifyClientsAboutOneObject (int nExcludedPlayerID, HW03_ObjectGame oObjectToWrite) {
        try {
            
            // Check to make sure that we actually have some clients
            if (HW03_Globals.oObjectsToWrite.isEmpty() == false) {
                
                // Populate info to write
                for (ConcurrentHashMap.Entry<Integer, BlockingQueue<HW03_ObjectCommunicable>> oEntry : HW03_Globals.oObjectsToWrite.entrySet()) {
                    if (nExcludedPlayerID == -1 || oEntry.getKey() != nExcludedPlayerID) {
                        /* Synchronize here so that we can make sure the first thing the new client hears about is its own character
                         *  Otherwise the draw loop could pick up on the new output stream and tell the client about some moving platforms
                         */
                        synchronized (HW03_Server.class) {
                            oEntry.getValue().add(oObjectToWrite);
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
     * FUNCTION:        notifyClientsAboutManyObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Notify clients of the current status of multiple game objects
     *
     * ARGUMENTS:       nExcludedPlayerID -                 The player ID of a client to exclude from the notification (or -1)
     *                  aHW03_Globals.oObjectsToWrite -     Game objects to write to clients
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    static void notifyClientsAboutManyObjects(int nExcludedPlayerID, CopyOnWriteArrayList<?> aoObjectsPlatformH) {
        try {
            
            // Check to make sure that we actually have some clients
            if (HW03_Globals.oObjectsToWrite.isEmpty() == false) {
                
                for (ConcurrentHashMap.Entry<Integer, BlockingQueue<HW03_ObjectCommunicable>> oEntry : HW03_Globals.oObjectsToWrite.entrySet()) {
                    if (nExcludedPlayerID == -1 || oEntry.getKey() != nExcludedPlayerID) {
                        /* Synchronize here so that we can make sure the first thing the new client hears about is its own character
                         *  Otherwise the draw loop could pick up on the new output stream and tell the client about some moving platforms
                         */
                        synchronized (HW03_Server.class) {
                            oEntry.getValue().addAll((CopyOnWriteArrayList<HW03_ObjectGame>) aoObjectsPlatformH);
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
     * FUNCTION:        addBoundaryObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add boundary objects at each of the window's edges
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void addBoundaryObjects () {
        try {

            // Create boundary on window (top)
            new HW03_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                0, 
                // Length
                HW03_Utility.getWindowSize(), 
                // Vertical
                false
            );
            
            // Create boundary on window (right)
            new HW03_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                HW03_Utility.getWindowSize() - HW03_ObjectBoundary.getStaticSkinnyDimension(),
                // Y
                0, 
                // Length
                HW03_Utility.getWindowSize(), 
                // Vertical
                true
            );
            
            // Create boundary on window (bottom)
            new HW03_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                HW03_Utility.getWindowSize() - HW03_ObjectBoundary.getStaticSkinnyDimension(),
                // Length
                HW03_Utility.getWindowSize(), 
                // Vertical
                false
            );
            
            // Create boundary on window (left)
            new HW03_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                0, 
                // Length
                HW03_Utility.getWindowSize(), 
                // Vertical
                true
            );
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addPlatformObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add platform objects to the game world
     *                  Place these objects semi-randomly
     *                  Static platforms in particular also serve as spawn points
     *                      so don't draw them so close to the top of the window 
     *                      that no character could possibly fit on top
     *                  Character cougetInstanceld also get pushed onto some other platform
     *                      so just go ahead and use this rule for all platforms
     *                  Also, since static platforms serve as spawn points,
     *                      Make sure (if there are few of them) that they are wide enough
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void addPlatformObjects () {
        try {
            
            // Declare constants
            
            final int N_WINDOW_AREA_PX = HW03_Utility.getWindowSize() * HW03_Utility.getWindowSize();
            final int N_PLATFORM_AREA_PX = (int) ( ((double) N_WINDOW_AREA_PX) / 5);
            final int N_PLATFORMS_TOTAL_PX = HW03_Globals.nNumPlatformsStatic + HW03_Globals.nNumPlatformsDynamic;
            
            final int N_MAX_LENGTH_GUESS = max(2, (int) Math.sqrt( ((double) N_PLATFORM_AREA_PX) / ((double) N_PLATFORMS_TOTAL_PX) ));
            
            final int N_MIN_WIDTH_STATIC_PX = (HW03_Globals.nNumPlatformsStatic < 10 ? (HW03_ObjectCharacter.getStaticHeight() * 3) : 1);
            final int N_MIN_WIDTH_MOVING_PX = 1;
            
            final int N_MAX_WIDTH_STATIC_PX = max(N_MAX_LENGTH_GUESS, N_MIN_WIDTH_STATIC_PX) + 1;
            final int N_MAX_WIDTH_MOVING_PC = max(N_MAX_LENGTH_GUESS, N_MIN_WIDTH_MOVING_PX) + 1;
            
            final int N_MIN_HEIGHT_STATIC_PX = 1;
            final int N_MIN_HEIGHT_MOVING_PX = 1;
            
            final int N_MAX_HEIGHT_STATIC_PX = max(N_MAX_LENGTH_GUESS, N_MIN_HEIGHT_STATIC_PX) + 1;
            final int N_MAX_HEIGHT_MOVING_PX = max(N_MAX_LENGTH_GUESS, N_MIN_HEIGHT_MOVING_PX) + 1;
            
            // Declare variables
            Random oRandomizer;
            HW03_ObjectPlatformStatic oPlatformStatic;
            HW03_ObjectPlatformH oPlatformH;
            HW03_ObjectPlatformV oPlatformV;
            int nX_px;
            int nY_px;
            int i;
            
            // Get randomizer
            oRandomizer = new Random();
            
            // STATIC
            for (i = 0; i < HW03_Globals.nNumPlatformsStatic; i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW03_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformStatic = new HW03_ObjectPlatformStatic(
                    // Get auto GUID
                    -1,
                    nX_px,
                    nY_px,
                    (oRandomizer.nextInt(N_MAX_WIDTH_STATIC_PX - N_MIN_WIDTH_STATIC_PX) + N_MIN_WIDTH_STATIC_PX),
                    (oRandomizer.nextInt(N_MAX_HEIGHT_STATIC_PX - N_MIN_HEIGHT_STATIC_PX) + N_MIN_HEIGHT_STATIC_PX),
                    null
                );
                
                // Move it if it's in collision with some other object
                while (oPlatformStatic.doesObjectCollide() == true) {
                    oPlatformStatic.setPositionX(oRandomizer.nextInt(HW03_Utility.getWindowSize()));
                    oPlatformStatic.setPositionY(getRandomPlatformY());
                }
                
            }
            
            // HORIZONTAL
            for (i = 0; i < (HW03_Globals.nNumPlatformsDynamic / 2); i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW03_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformH = new HW03_ObjectPlatformH(
                    // Get auto GUID
                    -1,
                    nX_px,
                    nY_px,
                    (oRandomizer.nextInt(N_MAX_WIDTH_MOVING_PC - N_MIN_WIDTH_MOVING_PX) + N_MIN_WIDTH_MOVING_PX),
                    ((oRandomizer.nextInt(N_MAX_HEIGHT_MOVING_PX - N_MIN_HEIGHT_MOVING_PX) + N_MIN_HEIGHT_MOVING_PX) / 2),
                    null
                );
                
                // Move it if it's in collision with some other object
                while (oPlatformH.doesObjectCollide() == true) {
                    oPlatformH.setPositionX(oRandomizer.nextInt(HW03_Utility.getWindowSize()));
                    oPlatformH.setPositionY(getRandomPlatformY());
                }
                
                // Register interest in events that affect the object
                O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.COLLISION, oPlatformH, false);
                
            }
            
            // VERTICAL
            for (i = 0; i < (HW03_Globals.nNumPlatformsDynamic / 2) + (HW03_Globals.nNumPlatformsDynamic % 2); i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW03_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformV = new HW03_ObjectPlatformV(
                    // Get auto GUID
                    -1,
                    nX_px,
                    nY_px,
                    ((oRandomizer.nextInt(N_MAX_WIDTH_MOVING_PC - N_MIN_WIDTH_MOVING_PX) + N_MIN_WIDTH_MOVING_PX) / 2),
                    (oRandomizer.nextInt(N_MAX_HEIGHT_MOVING_PX - N_MIN_HEIGHT_MOVING_PX) + N_MIN_HEIGHT_MOVING_PX),
                    null
                );
                
                // Move it if it's in collision with some other object
                while (oPlatformV.doesObjectCollide() == true) {
                    oPlatformV.setPositionX(oRandomizer.nextInt(HW03_Utility.getWindowSize()));
                    oPlatformV.setPositionY(getRandomPlatformY());
                }
                
                // Register interest in events that affect the object
                O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.COLLISION, oPlatformV, false);
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getRandomPlatformY
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Get a random Y location for a platform
     *                  Static platforms in particular also serve as spawn points
     *                      so don't draw them so close to the top of the window 
     *                      that no character could possibly fit on top
     *                  Character could also get pushed onto some other platform
     *                      so just go ahead and use this rule for all platforms
     *
     * ARGUMENTS:       nY_px - A semi-random Y location for a platform object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private static int getRandomPlatformY () {
        
        int nY_px = -1;
        try {
            
            // Declare constants
            final int N_CHARACTER_HEIGHT_PX = HW03_ObjectCharacter.getStaticHeight();
            final int N_STATUS_OBJECTS_HEIGHT_PX = HW03_ObjectStatus.getTotalHeight();
            final int N_MIN_Y_PX = (N_CHARACTER_HEIGHT_PX * 4) + N_STATUS_OBJECTS_HEIGHT_PX;
            
            // Declare variables
            Random oRandomizer;
            
            // Get randomizer
            oRandomizer = new Random();
            
            // Calculate
            nY_px = N_MIN_Y_PX + oRandomizer.nextInt(HW03_Utility.getWindowSize() - N_MIN_Y_PX);
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        return nY_px;
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addSpawnPoints
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add spawn points to the game world.
     *                  A spawn point is added just above each stationary platform object,
     *                  so that characters will spawn standing on a stationary platform.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void addSpawnPoints () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW03_ObjectPlatformStatic> aoObjectsPlatformStatic;
            int i;
            
            // Create spawn points
            aoObjectsPlatformStatic = HW03_ObjectPlatformStatic.getStaticPlatformObjects();
            for (i = 0; i < aoObjectsPlatformStatic.size(); i++) {
                
                // Add a spawn point on TOP of each static platform
                new HW03_ObjectSpawnPoint(
                    // Get auto GUID
                    -1,
                    aoObjectsPlatformStatic.get(i).getPositionX(),
                    aoObjectsPlatformStatic.get(i).getPositionY() - 1 - HW03_ObjectCharacter.getStaticHeight()
                );
                
            }
            
            // Free
            aoObjectsPlatformStatic = null;
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }

    /*********************************************************************************************************
     * FUNCTION:        addDeathZones
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add death zones to the game world
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void addDeathZones () {
        try {
            
            // Add a single death zone at the bottom of the window ("the floor is lava")
            new HW03_ObjectDeathZone(
                // Get auto GUID
                -1,
                0,
                HW03_Utility.getWindowSize() - 1,
                HW03_Utility.getWindowSize(),
                1
            );
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        addWinZones
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Add win zones to the game world
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void addWinZones () {
        try {
            
            // Add a single win zone at the top of the window
            new HW03_ObjectWinZone(
                // Get auto GUID
                -1,
                0,
                HW03_ObjectStatus.getTotalHeight(),
                HW03_Utility.getWindowSize(),
                1
            );
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
   
    /*********************************************************************************************************
     * FUNCTION:        settings
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     https://processing.org/reference/settings_.html
     *                  The settings() method runs before the sketch has been set up, 
     *                      so other Processing functions cannot be used at that point.
     *                  The settings() method runs "passively" to set a few variables, 
     *                      compared to the setup() command that call commands in the Processing API.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void settings () {
        try {
            
            // Set window size
            size(0, 0);
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        setup
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     https://processing.org/reference/setup_.html
     *                  The setup() function is run once, when the program starts. 
     *                  It's used to define initial environment properties and to 
     *                      load media such as images and fonts as the program starts. 
     *                  There can only be one setup() function for each program 
     *                      and it shouldn't be called again after its initial execution.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void setup () {
        frameRate(HW03_Globals.nFrameRate);
    }
    
    /*********************************************************************************************************
     * FUNCTION:        draw
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     https://processing.org/reference/draw_.html
     *                  
     *                  Called directly after setup(), 
     *                      the draw() function continuously executes the lines of code contained inside its block 
     *                      until the program is stopped or noLoop() is called. 
     *                      draw() is called automatically and should never be called explicitly. 
     *                  All Processing programs update the screen at the end of draw(), never earlier.
     * 
     *                  There can only be one draw() function for each sketch, 
     *                      and draw() must exist if you want the code to run continuously, 
     *                      or to process events such as mousePressed().
     *                  
     *                  This draw() loop functions as our game loop.
     *                      Detects key presses.
     *                      Moves platforms.
     *                      Sends updated platform locations to all clients.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void draw () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW03_ObjectPlatformH> aoObjectsPlatformH = null;
            CopyOnWriteArrayList<HW03_ObjectPlatformV> aoObjectsPlatformV = null;
            int i;
            
            // Detect key presses
            if (bQuit == true) {
                shutDown();
            }
            else {
                
                // Start loop time if needed
                if (HW03_Time_Loop.getPlayInstance().isStarted() == false) {
                    HW03_Time_Loop.getPlayInstance().start();
                    HW03_Time.stopTimer(S_TIMER_INIT);
                    HW03_Time.startTimer(S_TIMER_CLIENT_WAIT);
                }

                // No action is needed if the game is paused
                if (HW03_Time.isGamePaused() == false) {
                    
                    // Get object collections
                    aoObjectsPlatformH = HW03_ObjectPlatformH.getHorizontalPlatformObjects();
                    aoObjectsPlatformV = HW03_ObjectPlatformV.getVerticalPlatformObjects();
                    
                    /* Send updates to clients
                     *  Do this before moving objects for which the server is responsible
                     *  Why? Moving platforms may cause collision events which need to be handled
                     *  It's better not to send collided / funky objects out
                     */
                    notifyClientsAboutManyObjects(-1, aoObjectsPlatformH);
                    notifyClientsAboutManyObjects(-1, aoObjectsPlatformV);
                    
                }
                
                // Detect start and end of performance run
                if (nClients > 0) {
                    if (HW03_Time.doesTimerExist(S_TIMER_PERFORMANCE) == false) {
                        HW03_Time.stopTimer(S_TIMER_CLIENT_WAIT);
                        HW03_Time.startTimer(S_TIMER_PERFORMANCE);
                    }
                    if (
                        HW03_Globals.nPerformanceTestIterations > 0 && 
                        HW03_Time_Game.readTimer(S_TIMER_PERFORMANCE, "Loop") >= HW03_Globals.nPerformanceTestIterations
                    ) {
                        bQuit = true;
                    }
                }
                
                // Increment loop time if needed
                if (HW03_Time.isGamePaused() == false) {
                    HW03_Time_Loop.getPlayInstance().tick();
                }
                
                // No action is needed if the game is paused
                if (
                    HW03_Time.isGamePaused() == false &&
                    aoObjectsPlatformH != null &&
                    aoObjectsPlatformV != null
                ) {
                    
                    /* Move objects for which the server is responsible
                     * Do this after sending updates to clients
                     *  Why? Moving platforms may cause collision events which need to be handled
                     *  It's better not to send collided / funky objects out
                     */
                    for (i = 0; i < aoObjectsPlatformH.size(); i++) {
                        aoObjectsPlatformH.get(i).updateLocation();
                    }
                    for (i = 0; i < aoObjectsPlatformV.size(); i++) {
                        aoObjectsPlatformV.get(i).updateLocation();
                    }
                    
                    // Free
                    aoObjectsPlatformH = null;
                    aoObjectsPlatformV = null;
                    
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        keyPressed
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     https://processing.org/reference/keyPressed_.html
     *                  The keyPressed() function is called once every time a key is pressed. 
     *                  Mouse and keyboard events only work when a program has draw(). 
     *                      Without draw(), the code is only run once and then stops listening for events.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void keyPressed () {
       
        try {
            
            // QUIT is an instant action that happens on key press
            if (key == 'q' || key == 'Q') {
                bQuit = true;
                O_EVENT_MANAGER.raiseEventUserInput("QUIT", true, -1);
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleClientDeparture
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle the case that a client leaves the game.
     *                  If the client has left the game, 
     *                      it's objects should be removed from collections immediately.
     *                  Other threads should detect this.
     *                  Only 1 thread at a time may call this method,
     *                      to avoid problems with indexing into game objects array as objects are removed.
     *
     * ARGUMENTS:       nDepartedClientPlayerID - The departed client's player ID
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private synchronized void handleClientDeparture (int nDepartedClientPlayerID) {
        try {
            
            // Declare variables
            HW03_ObjectGame oGameObjectToRemove;
            
            // Find the game object for this client
            oGameObjectToRemove = HW03_ObjectCharacter.getCharacterObjectByPlayerID(nDepartedClientPlayerID);
            
            /* IF we are the first to know about it
             * (read and write threads could independently discover that a client has left the game)
             */
            if (oGameObjectToRemove != null && oGameObjectToRemove.getRemovalFlag() == false) {
                
                // Goodbye cruel world
                System.out.println(
                    "Player " + 
                    ((HW03_ObjectCharacter) oGameObjectToRemove).getPlayerID() + 
                    " has left the game!"
                );
                
                // Tell everybody else that this character object is out of the game
                oGameObjectToRemove.setRemovalFlag();
                notifyClientsAboutOneObject(nDepartedClientPlayerID, oGameObjectToRemove);
                
                // Close streams
                try {
                    oStreamsOut.get(nDepartedClientPlayerID).close();
                    oStreamsIn.get(nDepartedClientPlayerID).close();
                }
                catch (Throwable oError) {
                    // Ignore
                }
                
                // Remove client character object from ALL collections where it resides
                HW03_ObjectGame.removeObject(oGameObjectToRemove);
                oStreamsOut.remove(nDepartedClientPlayerID);
                oStreamsIn.remove(nDepartedClientPlayerID);

                // If this was the only paused client, resume the game
                if (
                    HW03_Time.isGamePaused() == true && 
                    HW03_Time.getLastClientPausedPlayerID() == nDepartedClientPlayerID
                ) {
                    O_EVENT_MANAGER.raiseEventGamePause(false);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        printResults
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Print the results of the performance test
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void printResults () {
        try {
            
            // Declare constants
            final String S_LOG_FILE = "performance_test_server.txt";
            final String S_DELIMITER = "\t";
            
            // Declare variables
            FileWriter  oFileWriter;
            PrintWriter oPrintWriter;
            boolean     bFileExists;
            
            // Find out if the file already exists
            bFileExists = new File(S_LOG_FILE).isFile();
            
            // Initialize writer
            oFileWriter = new FileWriter(S_LOG_FILE, true);
            oPrintWriter = new PrintWriter(oFileWriter);
            
            // Write header if needed
            if (bFileExists == false) {
                oPrintWriter.println(
                    "Event Management Protocol" + 
                    S_DELIMITER + 
                    "# Static Platforms" +
                    S_DELIMITER + 
                    "# Moving Platforms" + 
                    S_DELIMITER + 
                    "# Clients" + 
                    S_DELIMITER + 
                    "# Total Game Objects" + 
                    S_DELIMITER + 
                    "Requested # measured iterations" + 
                    S_DELIMITER + 
                    "Actual # measured iterations" + 
                    S_DELIMITER + 
                    "Time to Initialize (ms)" + 
                    S_DELIMITER + 
                    "Time to Wait for Clients (ms)" + 
                    S_DELIMITER + 
                    "Time to Run Game Loop (ms)" + 
                    S_DELIMITER + 
                    "Time per Game Loop Iteration (ms)"
                );
            }
    
            // Print results
            oPrintWriter.println(
                (HW03_Globals.bStandardEventManagementProtocol ? "Distributed" : "Server-Centric") + 
                S_DELIMITER + 
                HW03_Globals.nNumPlatformsStatic +
                S_DELIMITER + 
                HW03_Globals.nNumPlatformsDynamic + 
                S_DELIMITER + 
                nClients + 
                S_DELIMITER + 
                HW03_ObjectGame.getGameObjects().size() + 
                S_DELIMITER + 
                HW03_Globals.nPerformanceTestIterations + 
                S_DELIMITER + 
                HW03_Time.readTimer(S_TIMER_PERFORMANCE, "Loop") +
                S_DELIMITER + 
                HW03_Time.readTimer(S_TIMER_INIT, "Real") + 
                S_DELIMITER + 
                HW03_Time.readTimer(S_TIMER_CLIENT_WAIT, "Real") + 
                S_DELIMITER + 
                HW03_Time.readTimer(S_TIMER_PERFORMANCE, "Real") + 
                S_DELIMITER + 
                (
                    HW03_Time.readTimer(S_TIMER_PERFORMANCE, "Real") / 
                    HW03_Time.readTimer(S_TIMER_PERFORMANCE, "Loop")
                )
            );
            
            // Flush to be on the safe side
            oPrintWriter.flush();
            
            // Destroy timers
            HW03_Time.destroyTimer(S_TIMER_PERFORMANCE);
            HW03_Time.destroyTimer(S_TIMER_INIT);
            HW03_Time.destroyTimer(S_TIMER_CLIENT_WAIT);
            
            // Close
            oPrintWriter.close();
            oFileWriter.close();
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        shutDown
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Shut down the server
     *                      Let threads know (raise flag) that it's time to shut down
     *                      Close all streams
     *                      Close the socket
     *                      Exit
     *                  Shutting down the server is a normal activity
     *                      and should be handled gracefully by both the client and the server.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void shutDown () {
        try {
            
            // Detect end of performance run
            if (HW03_Globals.bRunPerformanceTest == true) {
                HW03_Time.stopTimer(S_TIMER_PERFORMANCE);
                printResults();
            }
            
            // Goodbye Cruel World
            System.out.println("Game server is shutting down!\n");
            
            // Close streams
            for (ConcurrentHashMap.Entry<Integer, ObjectOutputStream> oEntry : oStreamsOut.entrySet()) {
                oEntry.getValue().close();
            }
            for (ConcurrentHashMap.Entry<Integer, ObjectInputStream> oEntry : oStreamsIn.entrySet()) {
                oEntry.getValue().close();
            }
            
            // Close socket
            try {
                oServerSocket.close();
            }
            catch (Throwable oError) {
                // Ignore
            }

        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
        // Exit no matter what
        this.exit();
        System.exit(0);
        
    }

}
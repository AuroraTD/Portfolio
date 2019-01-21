/*************************************************************************************************************
 * FILE:            HW04_Server.java
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
public class HW04_Server extends PApplet implements Runnable {
    
    // Constants
    private static final HW04_EventManager                          O_EVENT_MANAGER =       HW04_EventManager.getInstance();

    //  Object properties
    private static ConcurrentHashMap<Integer, ObjectOutputStream>   oStreamsOut;
    private static ConcurrentHashMap<Integer, ObjectInputStream>    oStreamsIn;
    private static ServerSocket                                     oServerSocket;
    private static boolean                                          bQuit;
    
    private static int                                              nClients =              0;
    
    String                                                          sThreadType;
    int                                                             nThreadClientPlayerID;
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Server constructor
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
    public HW04_Server (String sType, int nClientPlayerID) {
        this.sThreadType = sType;
        this.nThreadClientPlayerID = nClientPlayerID;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Server constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A second constructor, this exists to accommodate the call:
     *                  PApplet.main("HW04_Server");
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_Server () {
        // Nothing to do here, this exists to accommodate the call: PApplet.main("HW04_Server");
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
     * ARGUMENTS:       0 - Game Choice
     *                      0 is 2D Platformer
     *                      1 is Bubble Shooter
     *                      2 is Space Invaders
     *                      Default = 0
     *                  1 - Frame rate
     *                      Default = 60
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void main (String[] args) {
        try {
            
            // Declare variables
            HW04_Server oServerInstanceAccept;
            HW04_Logger oLoggerInstance;
            int         nGameChoice;
            
            // Check arguments
            if (args.length > 2) {
                System.out.println("\nUsage: java -classpath .:./core.jar HW04_Server [see source code for optional arguments]");
            }
            else {
                
                /* Initialize hashmap to hold objects to write to clients
                 *  Why is this a hashmap?
                 *      So that client and server can use the same object
                 *      On the server, this will have a blocking queue of objects for every client
                 *      On the server, this will just have one blocking queue of objects (for the server)
                 *  Why is this done so early?
                 *      Because anywhere in the server that raises or registers for an event
                 *      may try to use this hashmap
                 */
                oStreamsOut =                   new ConcurrentHashMap<Integer, ObjectOutputStream>();
                oStreamsIn =                    new ConcurrentHashMap<Integer, ObjectInputStream>();
                HW04_Globals.oObjectsToWrite =  new ConcurrentHashMap<Integer, BlockingQueue<HW04_ObjectCommunicable>>();
                
                // Save arguments
                HW04_Globals.bClient = false;
                if (args.length > 0) {
                    nGameChoice = Integer.parseInt(args[0]);
                    switch (nGameChoice) {
                        case 0:
                            HW04_Globals.oGameChoice = HW04_Globals.GameChoice.PLATFORMER;
                            break;
                        case 1:
                            HW04_Globals.oGameChoice = HW04_Globals.GameChoice.BUBBLE_SHOOTER;
                            break;
                        case 2:
                            HW04_Globals.oGameChoice = HW04_Globals.GameChoice.SPACE_INVADERS;
                            break;
                        default:
                            throw new Exception("Game choice '" + nGameChoice + "' not recognized");
                    }
                }
                switch (HW04_Globals.oGameChoice) {
                    case PLATFORMER:
                        HW04_ScriptManager.loadScript("HW04_Script_Platformer.js");
                        break;
                    case BUBBLE_SHOOTER:
                        HW04_ScriptManager.loadScript("HW04_Script_BubbleShooter.js");
                        break;
                    case SPACE_INVADERS:
                        HW04_ScriptManager.loadScript("HW04_Script_SpaceInvaders.js");
                        break;
                    default:
                        throw new Exception("Game choice '" + HW04_Globals.oGameChoice + "' not recognized");
                }
                if (args.length > 1) {
                    HW04_Globals.nFrameRate = Integer.parseInt(args[1]);
                }
                
                // Start logger
                oLoggerInstance = new HW04_Logger();
                (new Thread(oLoggerInstance)).start();
                
                // Start game time
                HW04_Time_Game.getInstance().start();
                HW04_Time_Loop.getPlayInstance().setTickSize(1);

                // User instruction depends upon which game we are playing (use scripting)
                instructUser();
                
                // Assume the user does not want to quit until we know otherwise
                bQuit = false;
                
                // Add status summary
                new HW04_ObjectStatusSummary(-1, null);
                
                // Add instructions
                new HW04_ObjectStatusInstructions(-1, null);
                
                // Add scoreboard
                new HW04_ObjectStatusScoreboard(-1, null);
                
                // Population of game world depends upon which game we are playing (use scripting)
                HW04_ScriptManager.invokeFunction("populateGameWorld");
                
                // Create a server socket
                oServerSocket = new ServerSocket(HW04_Utility.setPortNumber());

                // Start a thread to listen for new client connections
                oServerInstanceAccept = new HW04_Server("Accept", -1);
                (new Thread(oServerInstanceAccept)).start();
                
                /* Start a PApplet application and tell it to use our class
                 * This will run settings() and setup() and then will start running draw() continuously
                 * So this comes after communication is established with the server
                 */
                PApplet.main("HW04_Server");
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
            
            // User instruction depends upon which game we are playing (use scripting)
            HW04_ScriptManager.invokeFunction("instructUser", false);
            
            // Hello World
            System.out.println("\nWelcome to the game!");
            System.out.println("\nFrame Rate: " + HW04_Globals.nFrameRate);
            System.out.println("\nOptions (click the window to give it focus first):");
            System.out.println("\t'Q': Quit");
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
            HW04_Utility.handleError(oError);
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
            ConcurrentHashMap<Integer, HW04_ObjectGame> oObjectsGame;
            HW04_Server oServerInstanceRead;
            HW04_Server oServerInstanceWrite;
            Socket oSocket = null;
            HW04_ObjectCharacter oNewObjectCharacter;
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
                    oNewObjectCharacter = new HW04_ObjectCharacter(
                        -1,
                        -1,
                        // Give the player an x-axis location (will move to spawn point later)
                        0, 
                        // Give the player a y-axis location (will move to spawn point later)
                        0,
                        // Do not pass a window to draw in, server doesn't draw objects
                        null
                    );
                    
                    // New character setup details depend upon which game we are playing (use scripting)
                    HW04_ScriptManager.invokeFunction("setUpNewCharacter", oNewObjectCharacter);
                    
                    /* Register this new object for events in which it is interested
                     * We are interested in user input events raised on the clients,
                     * because such an event could justify creating a new game object,
                     * and the server being the only one to create new game objects
                     * is what puts the "G" in "GUID"
                     */
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.SPAWN, oNewObjectCharacter, false);
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.USER_INPUT, oNewObjectCharacter, true);
                    
                    // Print to terminal that we have a new client
                    System.out.println("Player " + oNewObjectCharacter.getPlayerID() + " has joined the game!");
                    
                    /* Synchronize here so that we can make sure the first thing the new client hears about is its own character
                     *  Otherwise the draw loop could pick up on the new output stream and tell the client about some moving platforms
                     */
                    synchronized (HW04_Server.class) {
                    
                        // Add this connection to our list of connections
                        nNewClientPlayerID = oNewObjectCharacter.getPlayerID();
                        oStreamsOut.put(nNewClientPlayerID, new ObjectOutputStream(oSocket.getOutputStream()));
                        oStreamsIn.put(nNewClientPlayerID, new ObjectInputStream(oSocket.getInputStream()));
                        
                        /* Move the new character object to a spawn point 
                         * (go ahead and handle this now so rapidly connecting clients don't get in each other's way)
                         */
                        O_EVENT_MANAGER.raiseEventSpawn(oNewObjectCharacter);
                        
                        /* Explicitly raise game object change event 
                         * so that arrow object associated with this character (if there is one) can pick it up
                         * If not, no harm done, game object has changed so it's truthful
                         */
                        O_EVENT_MANAGER.raiseEventGameObjectChange(oNewObjectCharacter);
                        
                        /* Tell the client about the game world
                         * The first object we send, the client interprets as their character object
                         */
                        HW04_Globals.oObjectsToWrite.put(nNewClientPlayerID, new LinkedBlockingQueue<HW04_ObjectCommunicable>());
                        HW04_Globals.oObjectsToWrite.get(nNewClientPlayerID).add(oNewObjectCharacter);
                        
                        /* Start 2 new threads, to read from and write to our new client
                         * Do this after there is an appropriate entry in the hashmap from .put()
                         */
                        oServerInstanceRead = new HW04_Server("Read", nNewClientPlayerID);
                        (new Thread(oServerInstanceRead)).start();
                        oServerInstanceWrite = new HW04_Server("Write", nNewClientPlayerID);
                        (new Thread(oServerInstanceWrite)).start();
                        
                        // Now, tell the client about other game objects and about events we are interested in
                        oObjectsGame = HW04_ObjectGame.getGameObjects();
                        for (ConcurrentHashMap.Entry<Integer, HW04_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                            if (oEntry.getValue().getPlayerID() != nNewClientPlayerID) {
                                HW04_Globals.oObjectsToWrite.get(nNewClientPlayerID).add(oEntry.getValue());
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
            HW04_Utility.handleError(oError);
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
            HW04_ObjectCommunicable oObjectReadFromClient;
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
                        oObjectReadFromClient = (HW04_ObjectCommunicable) oStreamsIn.get(nThreadClientPlayerID).readObject();
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
                        HW04_ObjectCommunicable.castObject((HW04_ObjectCommunicable) oObjectReadFromClient);
                        
                        // Deal with character object
                        if (oObjectReadFromClient instanceof HW04_ObjectCharacter) {

                            // Check for existence of client again, it might have changed while we were waiting
                            if (oStreamsIn.containsKey(nThreadClientPlayerID)) {
                                
                                // Update our own copy of the character object
                                HW04_ObjectGame.replaceObject((HW04_ObjectGame) oObjectReadFromClient);
                                
                                // Everyone else needs to know that this client updated
                                notifyClientsAboutOneObject(nThreadClientPlayerID, (HW04_ObjectGame) oObjectReadFromClient);
                                
                            }
                            else {
                                bClientStillExistsRead = false;
                                handleClientDeparture(nThreadClientPlayerID);
                            }
                            
                        }
                        
                        // Deal with event registration (that is, someone else in the network is interested in our events)
                        if (oObjectReadFromClient instanceof HW04_ObjectEventRemoteRegistration) {
                            
                            // Register to handle the specified event type locally
                            O_EVENT_MANAGER.registerForEvents(
                                // Note the event type that is of interest to someone else in the network
                                ((HW04_ObjectEventRemoteRegistration) oObjectReadFromClient).getEventType(),
                                // Note the event observer that will handle this interest should the event be raised locally
                                HW04_NetworkPartnerProxy.getInstance(((HW04_ObjectEventRemoteRegistration) oObjectReadFromClient).getPlayerID()),
                                // Do not propagate this network event registration any further in the network
                                false
                            );
                            
                        }
                        
                        // Deal with event (that is, we are interested in an event raised by someone else in the network)
                        if (oObjectReadFromClient instanceof HW04_ObjectEvent) {
                            // Raise the event locally so that it can be handled by any local interested objects
                            O_EVENT_MANAGER.reRaiseEvent((HW04_ObjectEvent) oObjectReadFromClient);
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
            HW04_Utility.handleError(oError);
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
            HW04_ObjectCommunicable oObjectToWrite;
            
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
                    oObjectToWrite = HW04_Globals.oObjectsToWrite.get(nThreadClientPlayerID).take();
                    
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
            HW04_Utility.handleError(oError);
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
    public static void notifyClientsAboutOneObject (int nExcludedPlayerID, HW04_ObjectGame oObjectToWrite) {
        try {
            
            // Check to make sure that we actually have some clients
            if (HW04_Globals.oObjectsToWrite.isEmpty() == false) {
                
                // Populate info to write
                for (ConcurrentHashMap.Entry<Integer, BlockingQueue<HW04_ObjectCommunicable>> oEntry : HW04_Globals.oObjectsToWrite.entrySet()) {
                    if (nExcludedPlayerID == -1 || oEntry.getKey() != nExcludedPlayerID) {
                        /* Synchronize here so that we can make sure the first thing the new client hears about is its own character
                         *  Otherwise the draw loop could pick up on the new output stream and tell the client about some moving platforms
                         */
                        synchronized (HW04_Server.class) {
                            oEntry.getValue().add(oObjectToWrite);
                        }
                    }
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
     *                  aHW04_Globals.oObjectsToWrite -     Game objects to write to clients
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    public static void notifyClientsAboutManyObjects(int nExcludedPlayerID, CopyOnWriteArrayList<?> aoObjectsPlatformH) {
        try {
            
            // Check to make sure that we actually have some clients
            if (HW04_Globals.oObjectsToWrite.isEmpty() == false) {
                
                for (ConcurrentHashMap.Entry<Integer, BlockingQueue<HW04_ObjectCommunicable>> oEntry : HW04_Globals.oObjectsToWrite.entrySet()) {
                    if (nExcludedPlayerID == -1 || oEntry.getKey() != nExcludedPlayerID) {
                        /* Synchronize here so that we can make sure the first thing the new client hears about is its own character
                         *  Otherwise the draw loop could pick up on the new output stream and tell the client about some moving platforms
                         */
                        synchronized (HW04_Server.class) {
                            oEntry.getValue().addAll((CopyOnWriteArrayList<HW04_ObjectGame>) aoObjectsPlatformH);
                        }
                    }
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
            HW04_Utility.handleError(oError);
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
        frameRate(HW04_Globals.nFrameRate);
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
            
            // Detect key presses
            if (bQuit == true) {
                shutDown();
            }
            else {
                
                // Start loop time if needed
                if (HW04_Time_Loop.getPlayInstance().isStarted() == false) {
                    HW04_Time_Loop.getPlayInstance().start();
                }

                // No action is needed if the game is paused
                if (HW04_Time.isGamePaused() == false) {

                    // What we do here depends upon which game we are playing (use scripting)
                    HW04_ScriptManager.invokeFunction("performGameLoopIterationServer");
                    
                }
                
                // Increment loop time if needed
                if (HW04_Time.isGamePaused() == false) {
                    HW04_Time_Loop.getPlayInstance().tick();
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
            HW04_Utility.handleError(oError);
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
            HW04_ObjectGame oGameObjectToRemove;
            
            // Find the game object for this client
            oGameObjectToRemove = HW04_ObjectCharacter.getCharacterObjectByPlayerID(nDepartedClientPlayerID);
            
            /* IF we are the first to know about it
             * (read and write threads could independently discover that a client has left the game)
             */
            if (oGameObjectToRemove != null && oGameObjectToRemove.getRemovalFlag() == false) {
                
                // Goodbye cruel world
                System.out.println(
                    "Player " + 
                    ((HW04_ObjectCharacter) oGameObjectToRemove).getPlayerID() + 
                    " has left the game!"
                );
                
                // Remove this character object from the game (and tell everybody else)
                removeObjectFromGame(oGameObjectToRemove, nDepartedClientPlayerID);
                
                // Close streams
                try {
                    oStreamsOut.get(nDepartedClientPlayerID).close();
                    oStreamsIn.get(nDepartedClientPlayerID).close();
                }
                catch (Throwable oError) {
                    // Ignore
                }
                
                // Remove client streams
                oStreamsOut.remove(nDepartedClientPlayerID);
                oStreamsIn.remove(nDepartedClientPlayerID);

                // If this was the only paused client, resume the game
                if (
                    HW04_Time.isGamePaused() == true && 
                    HW04_Time.getLastClientPausedPlayerID() == nDepartedClientPlayerID
                ) {
                    O_EVENT_MANAGER.raiseEventGamePause(false);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        removeObjectFromGame
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Set a removal flag on the object
     *                  Notify all connected clients about the object (so they can see removal flag)
     *                  Remove the object from the server's collection of game objects
     *
     * ARGUMENTS:       oObjectToRemove -   The object to remove from the game
     *                  nSkipPlayerID -     The player ID of a client to NOT notify
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void removeObjectFromGame (HW04_ObjectGame oObjectToRemove, int nSkipPlayerID) {
        try {
            
            oObjectToRemove.setRemovalFlag();
            notifyClientsAboutOneObject(nSkipPlayerID, oObjectToRemove);
            HW04_ObjectGame.removeObject(oObjectToRemove);
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
            HW04_Utility.handleError(oError);
        }
        
        // Exit no matter what
        this.exit();
        System.exit(0);
        
    }

}
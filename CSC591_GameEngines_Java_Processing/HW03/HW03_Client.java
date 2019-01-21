/*************************************************************************************************************
 * FILE:            HW03_Client.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     The client application draws game world objects to the screen,
 *                      and interacts with the user who controls one additional object, their character.
 *                  User can move their character left / right / jump.
 *                  This program uses a monolithic hierarchy of game objects.
 *                      This is not the most flexible approach, as discussed in class.
 *                      However, for the limited time frame of a homework assignment, it is acceptable for now.
 *                  Synchronized blocks are used to control access to certain objects.
 *                      In general, an effort is made to minimize the use of synchronized blocks,
 *                      in other words to use just enough synchronization to safeguard against problems.
 *************************************************************************************************************/

// IMPORTS
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import processing.core.PApplet;

// CLASS DEFINITION
public class HW03_Client extends PApplet implements Runnable {
    
    // Static constants
    private static final HW03_EventManager O_EVENT_MANAGER = HW03_EventManager.getInstance();

    // Static variables
    private static HW03_ObjectCharacter     oMyCharacterObject;
    private static ObjectOutputStream       oStreamOut;
    private static ObjectInputStream        oStreamIn;
    private static Socket                   oSocket;
    private static boolean                  bQuit;
    private static boolean                  bShutdownInProgress;
    private static boolean                  bServerShutdownBeingHandled;
    private static String                   sCharacterAsStringPrevious =    "";
    private static String                   sCharacterAsStringCurrent =     "";
    
    String sThreadType;
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Client constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A function that creates an object with a .run() method (used for threads).
     *                  The .run() method can serve one of two functions:
     *                      Read -      Read from the server
     *                      Write -     Write to the server
     *
     * ARGUMENTS:       sType - "Read", or "Write"
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Client (String sType) {
        this.sThreadType = sType;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW03_Server_Async constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A second constructor, this exists to accommodate the call:
     *                  PApplet.main("HW03_Client");
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW03_Client () {
        // Nothing to do here, this exists to accommodate the call: PApplet.main("HW03_Client");
    }

    /*********************************************************************************************************
     * FUNCTION:        main
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Rough flow of this function:
     *                      Hello World
     *                      Initialize collections
     *                      Establish communication with game server
     *                      Save game objects described by the server
     *                      Keep track of which game object is mine
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
     *                  3 - Number of events to raise during performance test
     *                      Default = 100
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings({ "unchecked", "unused" })
    public static void main (String[] args) {
        try {
            
            // Declare variables
            HW03_Client oClientInstance;
            HW03_Logger oLoggerInstance;
            ConcurrentHashMap<String, Object> oEventArguments;
            int i;
            
            // Check arguments
            if (args.length > 4) {
                System.out.println("\nUsage: java -classpath .:./core.jar HW03_Client [see source code for optional arguments]");
            }
            else {
                
                // Save arguments
                HW03_Globals.bClient = true;
                if (args.length > 0) {
                    HW03_Globals.nFrameRate = Integer.parseInt(args[0]);
                }
                if (args.length > 1) {
                    HW03_Globals.bStandardEventManagementProtocol = Integer.parseInt(args[1]) == 0;
                }
                if (args.length > 2) {
                    HW03_Globals.bRunPerformanceTest = Integer.parseInt(args[2]) == 1;
                }
                if (args.length > 3) {
                    HW03_Globals.nPerformanceTestNumEvents = Integer.parseInt(args[3]);
                }
                
                /* Initialize hashmap to hold objects to write to server
                 *  Why is this a hashmap?
                 *      So that client and server can use the same object
                 *      On the server, this will have a blocking queue of objects for every client
                 *      On the server, this will just have one blocking queue of objects (for the server)
                 *  Why is this done so early?
                 *      Because anywhere in the client that raises or registers for an event
                 *      may try to use this hashmap
                 */
                HW03_Globals.oObjectsToWrite = new ConcurrentHashMap<Integer, BlockingQueue<HW03_ObjectCommunicable>>();
                HW03_Globals.oObjectsToWrite.put(-1, new LinkedBlockingQueue<HW03_ObjectCommunicable>());

                // Assume the user does not want to quit until we know otherwise
                bQuit =                         false;
                bShutdownInProgress =           false;
                bServerShutdownBeingHandled =   false;
                
                // Create a socket to the server, and create streams on that socket
                oSocket = null;
                try {
                    oSocket = new Socket("127.0.0.1", HW03_Utility.getPortNumber());
                }
                catch (Throwable oError) {
                    System.out.println("Game server is not running!");
                    bQuit = true;
                    new HW03_Client().shutDown();
                }
                if (bQuit != true) {
                    
                    // Create streams on the socket
                    oStreamOut = new ObjectOutputStream(oSocket.getOutputStream());
                    oStreamIn = new ObjectInputStream(oSocket.getInputStream());
                    
                    /* Wait to hear back from the server about my character object
                     *  This is the first object the server will send me
                     *      The server may also send additional game objects
                     *      and those should be processed in the normal way
                     *  Gotta have this because a lot of event stuff depends on having a valid player ID
                     *      that's why we do this before almost anything else
                     */
                    oMyCharacterObject = (HW03_ObjectCharacter) oStreamIn.readObject();
                    HW03_ObjectGame.addToGameObjects(oMyCharacterObject);
                    HW03_Globals.nPlayerID = oMyCharacterObject.getPlayerID();
                    
                    // Start logger & replay manager
                    oLoggerInstance = new HW03_Logger();
                    (new Thread(oLoggerInstance)).start();
                    new HW03_Replay();
                    
                    // Start game time
                    HW03_Time_Game.getInstance().start();
                    HW03_Time_Loop.getPlayInstance().setTickSize(1);
                    
                    // Register interest in events that affect the character object
                    O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.USER_INPUT,    oMyCharacterObject, false);
                    O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.COLLISION,     oMyCharacterObject, false);
                    O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.SCORE_CHANGE,  oMyCharacterObject, false);
                    O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.SPAWN,         oMyCharacterObject, false);
                    O_EVENT_MANAGER.registerForEvents(HW03_ObjectEvent.EventType.REPLAY,        oMyCharacterObject, false);
                    
                    // Hello World AFTER we have a player ID to tell them
                    instructUser();
                    
                    // Start 2 new threads, to write to and read from the server
                    oClientInstance = new HW03_Client("Write");
                    (new Thread(oClientInstance)).start();
                    oClientInstance = new HW03_Client("Read");
                    (new Thread(oClientInstance)).start();
                    
                    /* Start a PApplet application and tell it to use our class
                     * This will run settings() and setup() and then will start running draw() continuously
                     * So this comes after communication is established with the server
                     */
                    PApplet.main("HW03_Client");
                    
                }
                
            }
            
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
     *                  Can serve one of two functions:
     *                      Read -      Read from the server
     *                      Write -     Write to the server
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    public void run () {
        try {
            
            // Declare variables
            Object oObjectReadFromServer;
            HW03_ObjectCommunicable oObjectToWrite;
            HW03_ObjectGame oLocalCopyOfChangedObject;
            HW03_ObjectCharacter oThisChangedCharacter;
            int nGameObjectGUID;
            int i;
            
            // WRITE TO SERVER
            if (this.sThreadType.equals("Write")) {
                
                // Write to the server "forever"
                while (bQuit != true) {
                    
                    /* Wait for some information to write out
                     * .poll() is the non-blocking version (returns even if queue empty)
                     * .take() is the blocking version (waits for queue to be non-empty)
                     */
                    oObjectToWrite = HW03_Globals.oObjectsToWrite.get(-1).take();
                    
                    /* WRITE to game server (if the server hasn't shut down)
                     *  https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
                     *  All game clients, on every game loop, 
                     *  send their character object to the server IF it has changed
                     *  May also send events to server
                     */
                    synchronized (HW03_Client.class) {
                        if (bQuit == false && bShutdownInProgress == false && bServerShutdownBeingHandled == false) {
                            try {
                                
                                /* Write object to server
                                 * https://stackoverflow.com/questions/41058548/why-an-object-doesnt-change-when-i-send-it-through-writeobject-method
                                 */
                                oStreamOut.reset();
                                oStreamOut.writeObject(oObjectToWrite);
                                oStreamOut.flush();
                                
                            }
                            catch (IOException oError) {
                                handleServerShutdown();
                            }
                        }
                    }
                    
                }
                
            }
            
            // READ FROM SERVER
            else if (this.sThreadType.equals("Read")) {
                
                // Read from the server "forever"
                while (bQuit != true) {
                    
                    // READ from game server
                    oObjectReadFromServer = null;
                    try {
                        oObjectReadFromServer = oStreamIn.readObject();
                    }
                    catch (Throwable oError) {
                        handleServerShutdown();
                    }
                    
                    /* ACT on what was read
                     *  Server should write about any game object that has changed
                     *      - New game objects
                     *      - Removed game objects (via a removal flag)
                     *      - Changed game objects
                     *  Server should write about any event registration for events that it is interested in
                     *  Server should write about any events that this client is interested in
                     */
                    if (oObjectReadFromServer != null) {
                            
                        // Cast object to more specific type
                        HW03_ObjectCommunicable.castObject((HW03_ObjectCommunicable) oObjectReadFromServer);
                        
                        // Deal with game object
                        if (oObjectReadFromServer instanceof HW03_ObjectGame) {
                            
                            /* Synchronize here so that we don't end up 
                             *  trying to draw stuff while that stuff is being replaced (by the read stream thread)
                             */
                            synchronized (HW03_Client.class) {
                                
                                // Get GUID and local copy
                                nGameObjectGUID = ((HW03_ObjectGame) oObjectReadFromServer).getGUID();
                                oLocalCopyOfChangedObject = HW03_ObjectGame.getObjectByGUID(nGameObjectGUID);
                                
                                // Error checking (server should trust client to handle their own character object)s
                                if (nGameObjectGUID == oMyCharacterObject.getGUID()) {
                                    throw new Exception("Server should not update client about client's own character object");
                                }
                                else {
                                    
                                    // Some interesting change happened - see that it gets logged
                                    O_EVENT_MANAGER.raiseEventGameObjectChange((HW03_ObjectGame) oObjectReadFromServer);
                                    
                                    /* Object is flagged for removal
                                     *  If we won't need this for a replay, go ahead and remove it
                                     *  If we will need this for a replay, have to hang on to it for awhile
                                     */
                                    if (((HW03_ObjectGame) oObjectReadFromServer).getRemovalFlag() == true) {
                                        if (
                                            HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.RECORDING && 
                                            oLocalCopyOfChangedObject != null && 
                                            oLocalCopyOfChangedObject instanceof HW03_ObjectRenderable
                                        ) {
                                            ((HW03_ObjectRenderable) oLocalCopyOfChangedObject).setHiddenFlag(true);
                                            ((HW03_ObjectRenderable) oLocalCopyOfChangedObject).setRemovalFlag();
                                        }
                                        else {
                                            HW03_ObjectGame.removeObjectByGUID(nGameObjectGUID);
                                        }
                                    }
                                    
                                    // Object is new
                                    else {
                                        
                                        // Add to game objects collection
                                        if (oLocalCopyOfChangedObject == null) {
                                            HW03_ObjectGame.addToGameObjects((HW03_ObjectGame) oObjectReadFromServer);
                                        }
                                        
                                        /* Changed Object
                                         * Ignore if we are replaying right now
                                         * Possible that a few things already in flight will come in immediately after we start replaying
                                         * and we don't want this to step on our teleporting / replaying
                                         * It's okay to ignore these because when normal gameplay resumes,
                                         * the server will again start blabbing about all changed game objects
                                         */
                                        else if (HW03_Replay.getCurrentState() != HW03_Replay.ReplayState.REPLAYING) {
                                            // Update our local representation of this game object
                                            HW03_ObjectGame.replaceObjectByGUID(nGameObjectGUID, (HW03_ObjectGame) oObjectReadFromServer);
                                        }
                                        
                                    }
                                    
                                }
                                
                            }

                        }
                        
                        // Deal with event registration (that is, someone else in the network is interested in our events)
                        if (oObjectReadFromServer instanceof HW03_ObjectEventRemoteRegistration) {
                            
                            // Register to handle the specified event type locally
                            O_EVENT_MANAGER.registerForEvents(
                                // Note the event type that is of interest to someone else in the network
                                ((HW03_ObjectEventRemoteRegistration) oObjectReadFromServer).getEventType(),
                                // Note the event observer that will handle this interest should the event be raised locally
                                HW03_NetworkPartnerProxy.getInstance(((HW03_ObjectEventRemoteRegistration) oObjectReadFromServer).getPlayerID()),
                                // Do not propagate this network event registration any further in the network
                                false
                            );
                        }
                        
                        // Deal with event (that is, we are interested in an event raised by someone else in the network)
                        if (oObjectReadFromServer instanceof HW03_ObjectEvent) {
                            // Raise the event locally so that it can be handled by any local interested objects
                            O_EVENT_MANAGER.reRaiseEvent((HW03_ObjectEvent) oObjectReadFromServer);
                        }
                        
                    }

                }
                
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
            System.out.println(
                "Event Management Protocol: " + 
                (HW03_Globals.bStandardEventManagementProtocol ? "Distributed" : "Server-Centric")
            );
            System.out.println("Frame Rate: " + HW03_Globals.nFrameRate);
            
            System.out.println("\nYou are player # " + oMyCharacterObject.getPlayerID() + ".");
            System.out.println("Your avatar is the BLUE one.");
            System.out.println("If you reach the TOP, you win a point!");
            System.out.println("If you fall to the BOTTOM, you lose a point ('the floor is lava')!\n");
            
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
            size(HW03_Utility.getWindowSize(), HW03_Utility.getWindowSize());
            
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
        try {
            
            // Just to be explicit
            frameRate(HW03_Globals.nFrameRate);
            
            // Set default shape fill color (light gray)
            fill(200);
            
            // Set the window title
            surface.setTitle("Player " + oMyCharacterObject.getPlayerID());
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
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
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings({ "unused" })
    public void draw () {
        try {
            
            // Declare variables
            CopyOnWriteArrayList<HW03_ObjectRenderable> aoObjectsRenderable;
            Object oObjectReadFromServer;
            HW03_ObjectRenderable oThisRenderableObject;
            int nIndexOfMyCharacterObject;
            int i;
            boolean bSpecialColor;
            
            // Quitting?
            if (bQuit == true) {
                shutDown();
            }
            else {
                
                // Start loop time if needed
                if (HW03_Time_Loop.getPlayInstance().isStarted() == false) {
                    HW03_Time_Loop.getPlayInstance().start();
                }
                
                /* Set the background color of the window (black)
                 * It is common to call background() near the beginning of the draw() loop to clear the contents of the window. 
                 * Since pixels drawn to the window are cumulative, omitting background() may result in unintended results.
                 */
                background(HW03_Color.black());
                
                // Draw a border around the window
                stroke(HW03_Color.white());
                fill(HW03_Color.black());
                rect(0, 0, HW03_Utility.getWindowSize()-1, HW03_Utility.getWindowSize()-1, 0);
                
                /* Draw all renderable game objects
                 *  Do this before moving objects for which the client is responsible
                 *      Why? Moving objects may cause collision events which need to be handled
                 *      It's better not to send collided / funky objects out
                 *      Likewise it's better not to render collided / funky objects
                 *  Synchronize here so that we don't end up 
                 *      trying to draw stuff while that stuff is being replaced (by the read stream thread)
                 */
                synchronized (HW03_Client.class) {
                    
                    aoObjectsRenderable = HW03_ObjectRenderable.getRenderableObjects();
                    for (i = 0; i < aoObjectsRenderable.size(); i++) {
                        
                        // Get this object
                        oThisRenderableObject = aoObjectsRenderable.get(i);
                        
                        /* Before we can use any Processing methods, must give game objects a window
                         * Objects are received from the server, but the client has its own window to draw in
                         */
                        oThisRenderableObject.setApplet(this);
                        
                        // Make sure overriding display methods are actually called
                        if (oThisRenderableObject instanceof HW03_ObjectStatusScoreboard) {
                            ((HW03_ObjectStatusScoreboard) oThisRenderableObject).display(false);
                        }
                        else if (oThisRenderableObject instanceof HW03_ObjectCharacter) {
                            bSpecialColor = (oThisRenderableObject.getGUID() == oMyCharacterObject.getGUID());
                            ((HW03_ObjectCharacter) oThisRenderableObject).display(bSpecialColor);
                        }
                        else {
                            oThisRenderableObject.display(false);
                        }
                        
                    }
                    aoObjectsRenderable = null;
                    
                }
                
                // Take a screenshot sometimes
                if (new Random().nextInt(10000) < 10) {
                    saveFrame(new Random().nextInt(10000) + ".png");
                }
                
                /* Send updates to server
                 *  Do this before moving objects for which the client is responsible
                 *      Why? Moving objects may cause collision events which need to be handled
                 *      It's better not to send collided / funky objects out
                 *      Likewise it's better not to render collided / funky objects
                 *  Only do this if something actually changed, to minimize traffic
                 *      Every time the client updates the server, 
                 *          the server has to update all other clients, 
                 *          and all other clients have to do work based on this update
                 */
                if (HW03_Time.isGamePaused() == false) {
                    sCharacterAsStringCurrent = oMyCharacterObject.toString();
                    if (sCharacterAsStringCurrent.equals(sCharacterAsStringPrevious) == false) {
                        // Notify the write thread (by adding to queue that it reads from in blocking fashion)
                        HW03_Globals.oObjectsToWrite.get(-1).add(oMyCharacterObject);
                        // If we're writing, some interesting change happened - see that it gets logged
                        O_EVENT_MANAGER.raiseEventGameObjectChange(oMyCharacterObject);
                    }
                    sCharacterAsStringPrevious = new String(sCharacterAsStringCurrent);
                }
                

                
                // Wait just a bit before moving to encourage previous (collision-handled) stuff to be sent
                Thread.sleep(HW03_Globals.nFrameRate / 3);
                
                /* Move objects for which the client is responsible
                 * Do this after sending updates to server and rendering
                 *  Why? Moving platforms may cause collision events which need to be handled
                 *  It's better not to send collided / funky objects out
                 *  Likewise it's better not to render collided / funky objects
                 */
                if (HW03_Time.isGamePaused() == false) {
                    synchronized (HW03_Client.class) {
                        // Move character
                        oMyCharacterObject.handleJumpStatus();
                        oMyCharacterObject.handlePlatformAdjacency();
                        oMyCharacterObject.updateLocation();
                    }
                }
                
                // Move all objects around if we are replaying
                if (HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.REPLAYING) {
                    HW03_Replay.stepReplayForward();
                }
                
                // Kick off the performance test if appropriate
                if (
                    HW03_Globals.bRunPerformanceTest == true && 
                    HW03_Time_Loop.getPlayInstance().getTime() == 0
                ) {
                    HW03_PerformanceTest.startTesting();
                }
                
                // Increment loop time
                if (HW03_Time.isGamePaused() == false) {
                    HW03_Time_Loop.getPlayInstance().tick();
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
     *                  The key that was pressed is stored in the key variable. 
     *                      Space bar: ' '
     *                  For non-ASCII keys, use the keyCode variable.
     *                      UP, DOWN, LEFT, and RIGHT (etc.)
     *                  Mouse and keyboard events only work when a program has draw(). 
     *                      Without draw(), the code is only run once and then stops listening for events. 
     *                  Because of how operating systems handle key repeats, 
     *                      holding down a key may cause multiple calls to keyPressed(). 
     *                      The rate of repeat is set by the operating system, 
     *                      and may be configured differently on each computer. 
     *                      
     *                  When a key is pressed, start moving the client's character object.
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
            
            /* PAUSE is an instant action that happens on key press
             * Cannot pause during replay or during recording of replay
             */
            if (HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.IDLE) {
                if (key == 'p' || key == 'P') {
                    O_EVENT_MANAGER.raiseEventUserInput("PAUSE", true, -1);
                }
            }
            
            /* REPLAY is an instant action that happens on key press
             * These commands are only valid if we are waiting to hear the user's desired replay speed
             * Or if we are currently replaying
             */
            if (
                HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.WAITING_TO_REPLAY ||
                HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.REPLAYING
            ) {
                if (key == '1') {
                    O_EVENT_MANAGER.raiseEventUserInput("SET_REPLAY_SPEED", true, (float) 0.5);
                    if (HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.WAITING_TO_REPLAY) {
                        O_EVENT_MANAGER.raiseEventUserInput("START_REPLAY", true, -1);
                    }
                }
                else if (key == '2') {
                    O_EVENT_MANAGER.raiseEventUserInput("SET_REPLAY_SPEED", true, 1);
                    if (HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.WAITING_TO_REPLAY) {
                        O_EVENT_MANAGER.raiseEventUserInput("START_REPLAY", true, -1);
                    }
                }
                else if (key == '3') {
                    O_EVENT_MANAGER.raiseEventUserInput("SET_REPLAY_SPEED", true, 2);
                    if (HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.WAITING_TO_REPLAY) {
                        O_EVENT_MANAGER.raiseEventUserInput("START_REPLAY", true, -1);
                    }
                }
            }
            
            /* If we are paused, ignore all other commands
             * We are effectively paused during replay so that is covered here
             */
            if (HW03_Time.isGamePaused() == false) {
                
                /* JUMP is an instant action that happens on key press
                 * Holding down the key may result in this getting called multiple times
                 * and we do not want that behavior so we guard against it here
                 */
                if (key == ' ' && oMyCharacterObject.getJumpAllowed() == true) {
                    O_EVENT_MANAGER.raiseEventUserInput("SPACE", true, -1);
                }
                
                // LEFT is a continuous action that happens as long as the key is depressed
                else if (keyCode == LEFT) {
                    O_EVENT_MANAGER.raiseEventUserInput("LEFT", true, -1);
                }
                
                // RIGHT is a continuous action that happens as long as the key is depressed
                else if (keyCode == RIGHT) {
                    O_EVENT_MANAGER.raiseEventUserInput("RIGHT", true, -1);
                }
                
                // RECORDING is an instant action that happens on key press
                else if (key == 'r' || key == 'R') {
                    if (HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.IDLE) {
                        O_EVENT_MANAGER.raiseEventUserInput("START_RECORDING", true, -1);
                    }
                    else if (HW03_Replay.getCurrentState() == HW03_Replay.ReplayState.RECORDING) {
                        O_EVENT_MANAGER.raiseEventUserInput("STOP_RECORDING", true, -1);
                    }
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        keyReleased
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     https://processing.org/reference/keyReleased_.html
     *                  The keyReleased() function is called once every time a key is released. 
     *                  The key that was released will be stored in the key variable. 
     *                  See key and keyCode for more information. 
     *                  Mouse and keyboard events only work when a program has draw(). 
     *                      Without draw(), the code is only run once and then stops listening for events.
     *                      
     *                  When a key is released, stop moving the client's character object.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void keyReleased () {
        try {

            /* JUMP is an instant action that happens on key press
             * Holding down the key may result in this getting called multiple times
             * and we do not want that behavior so we guard against it here
             */
            if (key == ' ') {
                O_EVENT_MANAGER.raiseEventUserInput("SPACE", false, -1);
            }
            // LEFT is a continuous action that happens as long as the key is depressed
            else if (keyCode == LEFT) {
                O_EVENT_MANAGER.raiseEventUserInput("LEFT", false, -1);
            }
            // RIGHT is a continuous action that happens as long as the key is depressed
            else if (keyCode == RIGHT) {
                O_EVENT_MANAGER.raiseEventUserInput("RIGHT", false, -1);
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleServerShutdown
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle the case that the game server has shut down
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void handleServerShutdown () {
        try {
            
            /* Avoid repeated work
             * Read and write threads could independently discover that a client has left the game
             */
            if (bQuit == false && bShutdownInProgress == false && bServerShutdownBeingHandled == false) {
                
                // Set a flag so the other thread doesn't do the same thing
                bServerShutdownBeingHandled = true;
                
                // Make dang sure everybody knows that we are quitting
                bQuit = true;
                
                // Goodbye Cruel World
                System.out.println("Game server has shut down!");
                
                // Shut down
                shutDown();
                
            }
            
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
     * DESCRIPTION:     Shut down the client
     *                      Stop the game loop
     *                      Close streams
     *                      Close the socket to the server
     *                      Exit
     *                  Shutting down a client is a normal activity
     *                      and should be handled gracefully by both the client and the server.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void shutDown () {
        try {
            
            /* Avoid repeated work
             * Read and write threads could independently discover that a client has left the game
             */
            if (bShutdownInProgress == false) {
                
                // Set a flag so the other thread doesn't do the same thing
                bShutdownInProgress = true;
                
                // Make dang sure everybody knows that we are quitting
                bQuit = true;

                // Stop the game loop
                noLoop();
                
                /* Close streams and close socket to the server 
                 * (ignore errors - maybe server is already killed)
                 */
                try {
                    oStreamIn.close();
                    oStreamOut.close();
                    oSocket.close();
                }
                catch (Throwable oError) {
                    // Ignore
                }
                
                /* Goodbye Cruel World
                 * (ignore errors - maybe we don't have a character object yet)
                 */
                try {
                    System.out.println("Player " + oMyCharacterObject.getPlayerID() + " is leaving the game!");
                }
                catch (Throwable oError) {
                    // Ignore
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW03_Utility.handleError(oError);
        }
        
        // Exit no matter what
        try {
            this.exit();
        }
        catch (Throwable oError) {
            // Ignore
        }
        System.exit(0);
        
    }
    
}
/*************************************************************************************************************
 * FILE:            HW04_Client.java
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
public class HW04_Client extends PApplet implements Runnable {
    
    // Static constants
    private static final HW04_EventManager O_EVENT_MANAGER = HW04_EventManager.getInstance();

    // Static variables
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
     * FUNCTION:        HW04_Client constructor
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
    public HW04_Client (String sType) {
        this.sThreadType = sType;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW04_Client constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A second constructor, this exists to accommodate the call:
     *                  PApplet.main("HW04_Client");
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW04_Client () {
        // Nothing to do here, this exists to accommodate the call: PApplet.main("HW04_Client");
    }
    
    /*********************************************************************************************************
     * FUNCTION:        getInstance
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Getter
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         Instance of Client class for calling non-static methods
     *********************************************************************************************************/
    public static HW04_Client getInstance () {
        return new HW04_Client();
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
    @SuppressWarnings({ "unchecked", "unused" })
    public static void main (String[] args) {
        try {
            
            // Declare variables
            HW04_Client oClientInstance;
            HW04_Logger oLoggerInstance;
            ConcurrentHashMap<String, Object> oEventArguments;
            int nGameChoice;
            int i;
            
            // Check arguments
            if (args.length > 2) {
                System.out.println("\nUsage: java -classpath .:./core.jar HW04_Client [see source code for optional arguments]");
            }
            else {
                
                /* Initialize hashmap to hold objects to write to server
                 *  Why is this a hashmap?
                 *      So that client and server can use the same object
                 *      On the server, this will have a blocking queue of objects for every client
                 *      On the server, this will just have one blocking queue of objects (for the server)
                 *  Why is this done so early?
                 *      Because anywhere in the client that raises or registers for an event
                 *      may try to use this hashmap
                 */
                HW04_Globals.oObjectsToWrite = new ConcurrentHashMap<Integer, BlockingQueue<HW04_ObjectCommunicable>>();
                HW04_Globals.oObjectsToWrite.put(-1, new LinkedBlockingQueue<HW04_ObjectCommunicable>());
                
                // Save arguments
                HW04_Globals.bClient = true;
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

                // Assume the user does not want to quit until we know otherwise
                bQuit =                         false;
                bShutdownInProgress =           false;
                bServerShutdownBeingHandled =   false;
                
                // Create a socket to the server, and create streams on that socket
                oSocket = null;
                try {
                    oSocket = new Socket("127.0.0.1", HW04_Utility.getPortNumber());
                }
                catch (Throwable oError) {
                    System.out.println("Game server is not running!");
                    bQuit = true;
                    new HW04_Client().shutDown();
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
                    HW04_Globals.oMyCharacterObject = (HW04_ObjectCharacter) oStreamIn.readObject();
                    HW04_Globals.oMyCharacterObject.setColor(HW04_Color.getRandomShade("Blue"));
                    HW04_ObjectGame.addToGameObjects(HW04_Globals.oMyCharacterObject);
                    HW04_Globals.nPlayerID = HW04_Globals.oMyCharacterObject.getPlayerID();
                    
                    // Start logger & replay manager
                    oLoggerInstance = new HW04_Logger();
                    (new Thread(oLoggerInstance)).start();
                    new HW04_Replay();
                    
                    // Start game time
                    HW04_Time_Game.getInstance().start();
                    HW04_Time_Loop.getPlayInstance().setTickSize(1);
                    
                    // Register interest in local events that affect the character object
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.USER_INPUT,    HW04_Globals.oMyCharacterObject, false);
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.COLLISION,     HW04_Globals.oMyCharacterObject, false);
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.SPAWN,         HW04_Globals.oMyCharacterObject, false);
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.REPLAY,        HW04_Globals.oMyCharacterObject, false);
                    
                    /* Register interest in network-wide events that affect the character object
                     * In some games, the server changes a client's score / ends the game
                     */
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.SCORE_CHANGE,  HW04_Globals.oMyCharacterObject, true);
                    O_EVENT_MANAGER.registerForEvents(HW04_ObjectEvent.EventType.GAME_END,      HW04_Globals.oMyCharacterObject, true);

                    // User instruction depends upon which game we are playing (use scripting)
                    instructUser();
                    
                    // Start 2 new threads, to write to and read from the server
                    oClientInstance = new HW04_Client("Write");
                    (new Thread(oClientInstance)).start();
                    oClientInstance = new HW04_Client("Read");
                    (new Thread(oClientInstance)).start();
                    
                    /* Start a PApplet application and tell it to use our class
                     * This will run settings() and setup() and then will start running draw() continuously
                     * So this comes after communication is established with the server
                     */
                    PApplet.main("HW04_Client");
                    
                }
                
            }
            
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
            HW04_ObjectCommunicable oObjectToWrite;
            HW04_ObjectGame oLocalCopyOfChangedObject;
            HW04_ObjectCharacter oThisChangedCharacter;
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
                    oObjectToWrite = HW04_Globals.oObjectsToWrite.get(-1).take();
                    
                    /* WRITE to game server (if the server hasn't shut down)
                     *  https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
                     *  All game clients, on every game loop, 
                     *  send their character object to the server IF it has changed
                     *  May also send events to server
                     */
                    synchronized (HW04_Client.class) {
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
                        HW04_ObjectCommunicable.castObject((HW04_ObjectCommunicable) oObjectReadFromServer);
                        
                        // Deal with game object
                        if (oObjectReadFromServer instanceof HW04_ObjectGame) {
                            
                            /* Synchronize here so that we don't end up 
                             *  trying to draw stuff while that stuff is being replaced (by the read stream thread)
                             */
                            synchronized (HW04_Client.class) {
                                
                                // Get GUID and local copy
                                nGameObjectGUID = ((HW04_ObjectGame) oObjectReadFromServer).getGUID();
                                oLocalCopyOfChangedObject = HW04_ObjectGame.getObjectByGUID(nGameObjectGUID);
                                
                                // Error checking (server should trust client to handle their own character object)s
                                if (nGameObjectGUID == HW04_Globals.oMyCharacterObject.getGUID()) {
                                    throw new Exception("Server should not update client about client's own character object");
                                }
                                else {
                                    
                                    // Some interesting change happened - see that it gets logged
                                    O_EVENT_MANAGER.raiseEventGameObjectChange((HW04_ObjectGame) oObjectReadFromServer);
                                    
                                    /* Object is flagged for removal
                                     *  If we won't need this for a replay, go ahead and remove it
                                     *  If we will need this for a replay, have to hang on to it for awhile
                                     */
                                    if (((HW04_ObjectGame) oObjectReadFromServer).getRemovalFlag() == true) {
                                        if (
                                            HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.RECORDING && 
                                            oLocalCopyOfChangedObject != null && 
                                            oLocalCopyOfChangedObject instanceof HW04_ObjectRenderable
                                        ) {
                                            ((HW04_ObjectRenderable) oLocalCopyOfChangedObject).setHiddenFlag(true);
                                            ((HW04_ObjectRenderable) oLocalCopyOfChangedObject).setRemovalFlag();
                                        }
                                        else {
                                            HW04_ObjectGame.removeObjectByGUID(nGameObjectGUID);
                                        }
                                    }
                                    
                                    // Object is not flagged for removal
                                    else {
                                        
                                        // Object is new
                                        if (oLocalCopyOfChangedObject == null) {
                                            HW04_ObjectGame.addToGameObjects((HW04_ObjectGame) oObjectReadFromServer);
                                        }
                                        
                                        /* Changed Object
                                         * Ignore if we are replaying right now
                                         * Possible that a few things already in flight will come in immediately after we start replaying
                                         * and we don't want this to step on our teleporting / replaying
                                         * It's okay to ignore these because when normal gameplay resumes,
                                         * the server will again start blabbing about all changed game objects
                                         */
                                        else if (HW04_Replay.getCurrentState() != HW04_Replay.ReplayState.REPLAYING) {
                                            // Update our local representation of this game object
                                            HW04_ObjectGame.replaceObjectByGUID(nGameObjectGUID, (HW04_ObjectGame) oObjectReadFromServer);
                                        }
                                        
                                    }
                                    
                                }
                                
                            }

                        }
                        
                        // Deal with event registration (that is, someone else in the network is interested in our events)
                        if (oObjectReadFromServer instanceof HW04_ObjectEventRemoteRegistration) {
                            
                            // Register to handle the specified event type locally
                            O_EVENT_MANAGER.registerForEvents(
                                // Note the event type that is of interest to someone else in the network
                                ((HW04_ObjectEventRemoteRegistration) oObjectReadFromServer).getEventType(),
                                // Note the event observer that will handle this interest should the event be raised locally
                                HW04_NetworkPartnerProxy.getInstance(((HW04_ObjectEventRemoteRegistration) oObjectReadFromServer).getPlayerID()),
                                // Do not propagate this network event registration any further in the network
                                false
                            );
                        }
                        
                        // Deal with event (that is, we are interested in an event raised by someone else in the network)
                        if (oObjectReadFromServer instanceof HW04_ObjectEvent) {
                            // Raise the event locally so that it can be handled by any local interested objects
                            O_EVENT_MANAGER.reRaiseEvent((HW04_ObjectEvent) oObjectReadFromServer);
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
            HW04_ScriptManager.invokeFunction("instructUser", true);
            
            // Hello World
            System.out.println("\nWelcome to the game!");
            System.out.println("\nFrame Rate: " + HW04_Globals.nFrameRate);
            
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
            size(HW04_Utility.getWindowSize(), HW04_Utility.getWindowSize());
            
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
        try {
            
            // Just to be explicit
            frameRate(HW04_Globals.nFrameRate);
            
            // Set default shape fill color (light gray)
            fill(200);
            
            // Set the window title
            surface.setTitle("Player " + HW04_Globals.oMyCharacterObject.getPlayerID());
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
            CopyOnWriteArrayList<HW04_ObjectRenderable> aoObjectsRenderable;
            Object oObjectReadFromServer;
            HW04_ObjectRenderable oThisRenderableObject;
            int nIndexOfMyCharacterObject;
            int i;
            
            // Quitting?
            if (bQuit == true) {
                shutDown();
            }
            else {
                
                // Start loop time if needed
                if (HW04_Time_Loop.getPlayInstance().isStarted() == false) {
                    HW04_Time_Loop.getPlayInstance().start();
                }
                
                /* Set the background color of the window (black)
                 * It is common to call background() near the beginning of the draw() loop to clear the contents of the window. 
                 * Since pixels drawn to the window are cumulative, omitting background() may result in unintended results.
                 */
                background(HW04_Color.black());
                
                // Draw a border around the window
                stroke(HW04_Color.white());
                fill(HW04_Color.black());
                rect(0, 0, HW04_Utility.getWindowSize()-1, HW04_Utility.getWindowSize()-1, 0);
                
                /* Draw all renderable game objects
                 *  Do this before moving objects for which the client is responsible
                 *      Why? Moving objects may cause collision events which need to be handled
                 *      It's better not to send collided / funky objects out
                 *      Likewise it's better not to render collided / funky objects
                 *  Synchronize here so that we don't end up 
                 *      trying to draw stuff while that stuff is being replaced (by the read stream thread)
                 */
                synchronized (HW04_Client.class) {
                    
                    aoObjectsRenderable = HW04_ObjectRenderable.getRenderableObjects();
                    for (i = 0; i < aoObjectsRenderable.size(); i++) {
                        
                        // Get this object
                        oThisRenderableObject = aoObjectsRenderable.get(i);
                        
                        /* Before we can use any Processing methods, must give game objects a window
                         * Objects are received from the server, but the client has its own window to draw in
                         */
                        oThisRenderableObject.setApplet(this);
                        
                        // Make sure overriding display methods are actually called
                        HW04_ObjectCommunicable.castObject(oThisRenderableObject);
                        oThisRenderableObject.display();
                        
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
                if (HW04_Time.isGamePaused() == false) {
                    sCharacterAsStringCurrent = HW04_Globals.oMyCharacterObject.toString();
                    if (sCharacterAsStringCurrent.equals(sCharacterAsStringPrevious) == false) {
                        // Notify the write thread (by adding to queue that it reads from in blocking fashion)
                        HW04_Globals.oObjectsToWrite.get(-1).add(HW04_Globals.oMyCharacterObject);
                        // If we're writing, some interesting change happened - see that it gets logged
                        O_EVENT_MANAGER.raiseEventGameObjectChange(HW04_Globals.oMyCharacterObject);
                    }
                    sCharacterAsStringPrevious = new String(sCharacterAsStringCurrent);
                }
                
                // Wait just a bit before moving to encourage previous (collision-handled) stuff to be sent
                Thread.sleep(HW04_Globals.nFrameRate / 3);
                
                /* Operate on objects for which the client is responsible
                 * Do this after sending updates to server and rendering
                 *  Why? Moving platforms may cause collision events which need to be handled
                 *  It's better not to send collided / funky objects out
                 *  Likewise it's better not to render collided / funky objects
                 */
                if (HW04_Time.isGamePaused() == false) {
                    synchronized (HW04_Client.class) {
                        // What we do here depends upon which game we are playing (use scripting)
                        HW04_ScriptManager.invokeFunction("performGameLoopIterationClient", HW04_Globals.oMyCharacterObject);
                    }
                }
                
                // Move all objects around if we are replaying
                if (HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.REPLAYING) {
                    HW04_Replay.stepReplayForward();
                }
                
                // Increment loop time
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
            if (HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.IDLE) {
                if (key == 'p' || key == 'P') {
                    O_EVENT_MANAGER.raiseEventUserInput("PAUSE", true, -1);
                }
            }
            
            /* REPLAY is an instant action that happens on key press
             * These commands are only valid if we are waiting to hear the user's desired replay speed
             * Or if we are currently replaying
             */
            if (
                HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.WAITING_TO_REPLAY ||
                HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.REPLAYING
            ) {
                if (((boolean) HW04_ScriptManager.invokeFunction("isReplayEnabled")) == true) {
                    if (key == '1') {
                        O_EVENT_MANAGER.raiseEventUserInput("SET_REPLAY_SPEED", true, (float) 0.5);
                        if (HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.WAITING_TO_REPLAY) {
                            O_EVENT_MANAGER.raiseEventUserInput("START_REPLAY", true, -1);
                        }
                    }
                    else if (key == '2') {
                        O_EVENT_MANAGER.raiseEventUserInput("SET_REPLAY_SPEED", true, 1);
                        if (HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.WAITING_TO_REPLAY) {
                            O_EVENT_MANAGER.raiseEventUserInput("START_REPLAY", true, -1);
                        }
                    }
                    else if (key == '3') {
                        O_EVENT_MANAGER.raiseEventUserInput("SET_REPLAY_SPEED", true, 2);
                        if (HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.WAITING_TO_REPLAY) {
                            O_EVENT_MANAGER.raiseEventUserInput("START_REPLAY", true, -1);
                        }
                    }
                }
            }
            
            /* If we are paused, ignore all other commands
             * We are effectively paused during replay so that is covered here
             */
            if (HW04_Time.isGamePaused() == false) {
                
                // SPACE is an instant action that happens on key press
                if (key == ' ') {
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
                    if (((boolean) HW04_ScriptManager.invokeFunction("isReplayEnabled")) == true) {
                        if (HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.IDLE) {
                            O_EVENT_MANAGER.raiseEventUserInput("START_RECORDING", true, -1);
                        }
                        else if (HW04_Replay.getCurrentState() == HW04_Replay.ReplayState.RECORDING) {
                            O_EVENT_MANAGER.raiseEventUserInput("STOP_RECORDING", true, -1);
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
            
            // SPACE is an instant action that happens on key press
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
            HW04_Utility.handleError(oError);
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
            HW04_Utility.handleError(oError);
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
    public void shutDown () {
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
                    System.out.println("Player " + HW04_Globals.oMyCharacterObject.getPlayerID() + " is leaving the game!");
                }
                catch (Throwable oError) {
                    // Ignore
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
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
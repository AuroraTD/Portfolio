package HW01;
/*************************************************************************************************************
 * FILE:            HW01_Client_Async.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Explore the basics of constructing a multi-threaded network server.
 *                  ASYNCHRONOUS version.
 *                  The client application draws game world objects to the screen,
 *                      and interacts with the user who controls one additional object, their character.
 *                  User can move their character left / right / jump.
 *                  Data is sent to and received from the game server:
 *                      For this assignment, we may send whatever data we choose,
 *                      in future assignments we will be expected to send game objects,
 *                      so to set up better for future assignments, we send game objects.
 *                  This program uses a monolithic hierarchy of game objects.
 *                      This is not the most flexible approach, as discussed in class.
 *                      However, for the limited time frame of a homework assignment, it is acceptable for now.
 *                  This program uses CopyOnWriteArrayList collections, because they are thread safe.
 *                      They are not the most efficient collections for lookup or update of elements.
 * 
 * SOURCE:          This file is a heavily modified version of the example given at:
 *                  https://moodle-courses1819.wolfware.ncsu.edu/pluginfile.php/773435/mod_resource/content/1/SampleClient.java
 *
 * TO COMPILE:      javac -classpath .:./core.jar HW01_Client_Async.java
 * 
 * TO RUN:          java -classpath .:./core.jar HW01_Client_Async
 *************************************************************************************************************/

// IMPORTS
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW01_Client_Async extends PApplet implements Runnable {

    // Object properties
    private static CopyOnWriteArrayList<HW01_RenderableObject>  aoGameObjectsFromServer;
    private static CopyOnWriteArrayList<HW01_RenderableObject>  aoGameObjects;
    private static CopyOnWriteArrayList<HW01_WorldObject>       aoWorldObjects;
    private static CopyOnWriteArrayList<HW01_PlayerObject>      aoPlayerObjects;
    private static HW01_PlayerObject                            oMyPlayerObject;
    private static ObjectOutputStream                           oStreamOut;
    private static ObjectInputStream                            oStreamIn;
    private static Socket                                       oSocket;
    private static boolean                                      bQuit;
    private static boolean                                      bShutdownInProgress;
    private static boolean                                      bServerShutdownBeingHandled;
    private static final int                                    B_DEBUG = 0;
    private static final int                                    N_DEBUG_WAIT_MS = 2000;
    String sThreadType;
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_Client_Async constructor
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
    public HW01_Client_Async (String sType) {
        this.sThreadType = sType;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_Server_Async constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A second constructor, this exists to accommodate the call:
     *                  PApplet.main("HW01_Client_Async");
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW01_Client_Async () {
        // Nothing to do here, this exists to accommodate the call: PApplet.main("HW01_Client_Async");
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
    @SuppressWarnings({ "unchecked", "unused" })
    public void run () {
        try {
            
            // Declare variables
            Object oObjectReadFromServer;
            
            // WRITE TO SERVER
            if (this.sThreadType.equals("Write")) {
                
                // Write to the server "forever"
                while (bQuit != true) {
                    
                    /* Wait to be woken up at the end of a game loop
                     * Synchronizing on the output stream so that specifically the write thread can be woken
                     */
                    synchronized (oStreamOut) {
                        oStreamOut.wait();
                    }
                    
                    /* WRITE to game server (if the server hasn't shut down)
                     *  https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
                     *  All game clients, on every game loop, should send their player object so the server knows where they are
                     *  Synchronizing on the class to be very explicit for readers of the code,
                     *      and to control access to items used across multiple threads
                     */
                    synchronized (HW01_Client_Async.class) {
                        
                        if (bQuit == false && bShutdownInProgress == false && bServerShutdownBeingHandled == false) {
                            
                            try {
                                
                                /* Write player object to server
                                 * https://stackoverflow.com/questions/41058548/why-an-object-doesnt-change-when-i-send-it-through-writeobject-method
                                 */
                                oStreamOut.reset();
                                oStreamOut.writeObject(oMyPlayerObject);
                                oStreamOut.flush();
                                
                                // Print debug info
                                if (B_DEBUG >= 1) {
                                    System.out.println("Client has written its player object to server");
                                    Thread.sleep(N_DEBUG_WAIT_MS);
                                }
                                
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
                    
                    /* READ from game server
                     *  Server should write, on every server loop, all player objects to all clients, 
                     *      so they know where everyone else is
                     *  Stuff that might have changed since we last heard:
                     *      Player object has appeared (client joined)
                     *      Player object has disappeared (client left)
                     *      Player object has moved
                     *  Rather than have code to handle all these cases carefully, we do something dumb here
                     *      Blow away and recreate local collection of player objects
                     *      Update reference to the player object for this client
                     *      Blow away and reconstruct local collection of game objects
                     *  This is certainly not efficient, 
                     *      but is acceptable in the short development time frame of a homework assignment
                     */
                    oObjectReadFromServer = null;
                    try {
                        oObjectReadFromServer = oStreamIn.readObject();
                    }
                    catch (Throwable oError) {
                        handleServerShutdown();
                    }
                    
                    /* Step into synchronized block before touching collections that other threads might need
                     * Synchronizing on the class to be very explicit for readers of the code, 
                     *  and to control access to items used across multiple threads
                     */
                    if (oObjectReadFromServer != null) {
                        
                        synchronized (HW01_Client_Async.class) {
                            
                            // Save object
                            aoPlayerObjects.clear();
                            aoPlayerObjects = (CopyOnWriteArrayList<HW01_PlayerObject>) oObjectReadFromServer;
                            
                            // Print debug info
                            if (B_DEBUG >= 1) {
                                System.out.println("Client has read object from server: " + oObjectReadFromServer.toString());
                                System.out.println("Client has read " + aoPlayerObjects.size() + " player objects from server");
                                Thread.sleep(N_DEBUG_WAIT_MS);
                            }
                            
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
            HW01_Utility.handleError(oError);
        }
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
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings({ "unchecked", "unused" })
    public static void main (String[] args) {
        try {
            
            // Declare variables
            HW01_Client_Async oClientInstance;
            HW01_RenderableObject oSingleGameObject;
            String sObjectType;
            int i;
            
            // Check arguments
            if (args.length != 0) {
                System.out.println("\nUsage: java HW01_Client_Async");
            }
            else {
                
                // Hello World
                System.out.println("\nGame client is running!");
                System.out.println("\nYour avatar is the BLUE one.");
                System.out.println("Options (click the window to give it focus first):");
                System.out.println("\tLeft Arrow: Move Left");
                System.out.println("\tRight Arrow: Move Right");
                System.out.println("\tSpace Bar: Jump");
                System.out.println("\t'Q': Quit");
                
                // Assume the user does not want to quit until we know otherwise
                bQuit = false;
                bShutdownInProgress = false;
                bServerShutdownBeingHandled = false;
                
                // Initialize thread-safe collections
                aoWorldObjects = new CopyOnWriteArrayList<HW01_WorldObject>();
                aoPlayerObjects = new CopyOnWriteArrayList<HW01_PlayerObject>();
                aoGameObjects = new CopyOnWriteArrayList<HW01_RenderableObject>();
                
                // Create a socket to the server, and create streams on that socket
                oSocket = null;
                try {
                    oSocket = new Socket("127.0.0.1", HW01_Utility.getPortNumber());
                }
                catch (ConnectException oError) {
                    System.out.println("Game server is not running!");
                    bQuit = true;
                }
                if (bQuit != true) {
                    
                    // Create streams on the socket
                    oStreamOut = new ObjectOutputStream(oSocket.getOutputStream());
                    oStreamIn = new ObjectInputStream(oSocket.getInputStream());
                    
                    // Wait to hear back from the server about what objects exist in the game
                    aoGameObjectsFromServer = (CopyOnWriteArrayList<HW01_RenderableObject>) oStreamIn.readObject();
                    
                    // Print debug info
                    if (B_DEBUG >= 1) {
                        System.out.println("Client has read " + aoGameObjectsFromServer.size() + " game objects from server");
                        Thread.sleep(N_DEBUG_WAIT_MS);
                    }
                    
                    // Save all these game objects locally to the client so that we can draw them onscreen
                    for (i = 0; i < aoGameObjectsFromServer.size(); i++) {
                        
                        oSingleGameObject = aoGameObjectsFromServer.get(i);
                        sObjectType = oSingleGameObject.getType();
                        
                        // Save world objects because they won't change (in this very simple game)
                        if (sObjectType.equals("World")) {
                            aoWorldObjects.add((HW01_WorldObject) oSingleGameObject);
                        }
                        
                        // Save player objects because they will change
                        else if (sObjectType.equals("Player")) {
                            aoPlayerObjects.add((HW01_PlayerObject) oSingleGameObject);
                        }
                        
                        // Save all game objects because we draw them to screen every game loop
                        aoGameObjects.add((HW01_RenderableObject) oSingleGameObject);
                        
                    }
                    
                    /* Remember which player object is mine (it's the latest one)
                     * This assumption is fragile at high rates of client connections
                     * because of the synchronous nature of server-client communications
                     * This problem should go away with asynchronous communications
                     */
                    oMyPlayerObject = aoPlayerObjects.get(aoPlayerObjects.size()-1);
                    
                    /* Start a PApplet application and tell it to use our class
                     * This will run settings() and setup() and then will start running draw() continuously
                     * So this comes after communication is established with the server
                     */
                    PApplet.main("HW01_Client_Async");
                    
                    /* Start 2 new threads, to write to and read from the server
                     * Do this only after all the collections have been populated,
                     * because the threads might need them
                     */
                    oClientInstance = new HW01_Client_Async("Write");
                    (new Thread(oClientInstance)).start();
                    oClientInstance = new HW01_Client_Async("Read");
                    (new Thread(oClientInstance)).start();
                    
                }

            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
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
            size(HW01_Utility.getWindowSize(), HW01_Utility.getWindowSize());
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
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
            
            // Set default shape fill color (light gray)
            fill(200);
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
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
    @SuppressWarnings({ "unchecked", "unused" })
    public void draw () {
        try {
            
            // Declare variables
            Object oObjectReadFromServer;
            int nIndexOfMyPlayerObject;
            int i;
            
            // Quitting?
            if (bQuit == true) {
                shutDown();
            }
            else {
                
                // Step into synchronized block before touching collections that other threads might need
                synchronized (HW01_Client_Async.class) {
                    
                    nIndexOfMyPlayerObject = HW01_Utility.getIndexOfGUID(aoPlayerObjects, oMyPlayerObject.getGUID());
                    aoPlayerObjects.set(nIndexOfMyPlayerObject, oMyPlayerObject);
                    
                    // Also update the game objects collection (we draw all game objects every game loop)
                    aoGameObjects.clear();
                    aoGameObjects.addAll(aoWorldObjects);
                    aoGameObjects.addAll(aoPlayerObjects);
                    
                    /* Set the background color of the window (dark gray)
                     * It is common to call background() near the beginning of the draw() loop to clear the contents of the window. 
                     * Since pixels drawn to the window are cumulative, omitting background() may result in unintended results.
                     */
                    background(100);
                    
                    /* Before we can use any Processing methods, must give game objects a window
                     * Objects are received from the server, but the client has its own window to draw in
                     */
                    for (i = 0; i < aoGameObjects.size(); i++) {                      
                        aoGameObjects.get(i).setApplet(this);
                    }
                    
                    // Move the player object if needed
                    oMyPlayerObject.moveLeftRight(aoGameObjects);
                    oMyPlayerObject.jump(aoGameObjects);
                    
                }
                
                // Draw all game objects
                nIndexOfMyPlayerObject = HW01_Utility.getIndexOfGUID(aoGameObjects, oMyPlayerObject.getGUID());
                for (i = 0; i < aoGameObjects.size(); i++) {
                    // Draw this client's player object in a special color
                    if (i == nIndexOfMyPlayerObject) {
                        aoGameObjects.get(i).display(true);
                    }
                    else {
                        aoGameObjects.get(i).display(false);
                    }
                }
                
                /* Wake up the write thread to tell the server where we are now
                 * Synchronizing on the output stream so that specifically the write thread can be woken
                 */
                synchronized (oStreamOut) {
                    oStreamOut.notify();
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
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
     *                      
     *                  When a key is pressed, start moving the client's player object.
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
            }
            // JUMP is an instant action that happens on key press
            else if (key == ' ') {
                oMyPlayerObject.setJumping(true);
            }
            // LEFT is a continuous action that happens as long as the key is depressed
            else if (keyCode == LEFT) {
                oMyPlayerObject.setMovementDirection(-1);
            }
            // RIGHT is a continuous action that happens as long as the key is depressed
            else if (keyCode == RIGHT) {
                oMyPlayerObject.setMovementDirection(1);
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
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
     *                  When a key is released, stop moving the client's player object.
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void keyReleased () {
        try {

            // LEFT is a continuous action that happens as long as the key is depressed
            if (keyCode == LEFT) {
                oMyPlayerObject.setMovementDirection(0);
            }
            // RIGHT is a continuous action that happens as long as the key is depressed
            else if (keyCode == RIGHT) {
                oMyPlayerObject.setMovementDirection(0);
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
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
    public void handleServerShutdown () {
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
            HW01_Utility.handleError(oError);
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
                catch (SocketException oError) {
                    // Ignore
                }
                
                // Goodbye Cruel World
                System.out.println("Game client " + oMyPlayerObject.getGUID() + " is leaving the game!");
                
                // Exit
                System.exit(0);
                
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
    }
    
}
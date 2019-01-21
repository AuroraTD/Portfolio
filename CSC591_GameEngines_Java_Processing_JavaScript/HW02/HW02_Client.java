package HW02;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW02_Client extends PApplet implements Runnable {

    // Object properties
    private static HW02_ObjectCharacter     oMyCharacterObject;
    private static ObjectOutputStream       oStreamOut;
    private static ObjectInputStream        oStreamIn;
    private static Socket                   oSocket;
    private static boolean                  bQuit;
    private static boolean                  bShutdownInProgress;
    private static boolean                  bServerShutdownBeingHandled;
    private static String                   sCharacterAsStringPrevious = "";
    private static String                   sCharacterAsStringCurrent = "";
    
    private static int                      nArgNetworkProtocol = 0;
    private static int                      nTotalIterations =      0;
    
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
    public HW02_Client (String sType) {
        this.sThreadType = sType;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW02_Server_Async constructor
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
    public HW02_Client () {
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
     * ARGUMENTS:       0 - Network protocol (information sent across network)
     *                      0 means game objects
     *                      1 means use .writeReplace() and .readResolve()
     *                      Default = 0
     *                      
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings({ "unchecked", "unused" })
    public static void main (String[] args) {
        try {
            
            // Declare variables
            HW02_Client oClientInstance;
            CopyOnWriteArrayList<HW02_ObjectGame> aoReceivedObjects;
            int i;
            
            // Check arguments
            if (args.length > 1) {
                System.out.println("\nUsage: java -classpath .:./core.jar HW03_Client [network protocol]");
            }
            else if (args.length > 0) {
                nArgNetworkProtocol = Integer.parseInt(args[0]);
                HW02_ObjectGame.setNetworkProtocol(nArgNetworkProtocol);
            }

            // Assume the user does not want to quit until we know otherwise
            bQuit = false;
            bShutdownInProgress = false;
            bServerShutdownBeingHandled = false;
            
            // Create a socket to the server, and create streams on that socket
            oSocket = null;
            try {
                oSocket = new Socket("127.0.0.1", HW02_Utility.getPortNumber());
            }
            catch (ConnectException oError) {
                System.out.println("Game server is not running!");
                bQuit = true;
            }
            if (bQuit != true) {
                
                // Create streams on the socket
                oStreamOut = new ObjectOutputStream(oSocket.getOutputStream());
                oStreamIn = new ObjectInputStream(oSocket.getInputStream());
                
                /* Wait to hear back from the server about my character object
                 * This is the first object the server will send me
                 * The server may also send additional game objects
                 * and those should be processed in the normal way
                 */
                aoReceivedObjects = (CopyOnWriteArrayList<HW02_ObjectGame>) oStreamIn.readObject();
                oMyCharacterObject = (HW02_ObjectCharacter) aoReceivedObjects.get(0);
                HW02_ObjectGame.setGameObjects(aoReceivedObjects);

                // Hello World AFTER we have a character object
                instructUser();
                
                /* Start 2 new threads, to write to and read from the server
                 * Do this only after all the collections have been populated,
                 * because the threads might need them
                 */
                oClientInstance = new HW02_Client("Write");
                (new Thread(oClientInstance)).start();
                oClientInstance = new HW02_Client("Read");
                (new Thread(oClientInstance)).start();
                
                /* Start a PApplet application and tell it to use our class
                 * This will run settings() and setup() and then will start running draw() continuously
                 * So this comes after communication is established with the server
                 */
                PApplet.main("HW03_Client");
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            CopyOnWriteArrayList<HW02_ObjectGame> aoObjectsChanged;
            HW02_ObjectGame oThisChangedObject;
            HW02_ObjectGame oLocalCopyOfChangedObject;
            int i;
            
            // WRITE TO SERVER
            if (this.sThreadType.equals("Write")) {
                
                // Write to the server "forever"
                while (bQuit != true) {
                    
                    // Wait to be woken up at the end of a game loop
                    synchronized (oStreamOut) {
                        oStreamOut.wait();
                    }
                    
                    /* WRITE to game server (if the server hasn't shut down)
                     *  https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
                     *  All game clients, on every game loop, 
                     *  send their character object to the server IF it has changed
                     */
                    synchronized (HW02_Client.class) {
                        if (bQuit == false && bShutdownInProgress == false && bServerShutdownBeingHandled == false) {
                            try {
                                /* Write character object to server
                                 * https://stackoverflow.com/questions/41058548/why-an-object-doesnt-change-when-i-send-it-through-writeobject-method
                                 */
                                oStreamOut.reset();
                                oStreamOut.writeObject(oMyCharacterObject);
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
                     *  Client needs to react appropriately to
                     *      - New game objects
                     *      - Removed game objects (via a removal flag)
                     *      - Changed game objects
                     */
                    if (oObjectReadFromServer != null) {
                        
                        aoObjectsChanged = (CopyOnWriteArrayList<HW02_ObjectGame>) oObjectReadFromServer;
                        
                        synchronized (HW02_Client.class) {
                            
                            for (i = 0; i < aoObjectsChanged.size(); i++) {

                                // Error checking (server should trust client to handle their own character object
                                if (aoObjectsChanged.get(i).getGUID() == oMyCharacterObject.getGUID()) {
                                    throw new Exception("Server should not update client about client's own character object");
                                }
                                
                                // Removed Object
                                oThisChangedObject = aoObjectsChanged.get(i);
                                if (oThisChangedObject.getRemovalFlag() == true) {
                                    HW02_ObjectGame.removeObjectByGUID(oThisChangedObject.getGUID());
                                }
                                else {
                                    
                                    // New Object
                                    oLocalCopyOfChangedObject = HW02_ObjectGame.getObjectByGUID(oThisChangedObject.getGUID());
                                    if (oLocalCopyOfChangedObject == null) {
                                        HW02_ObjectGame.addToGameObjects(oThisChangedObject);
                                    }
                                    
                                    // Changed Object
                                    else {
                                        HW02_ObjectGame.replaceObjectByGUID(oThisChangedObject.getGUID(), oThisChangedObject);
                                    }
                                    
                                }
                                
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
            HW02_Utility.handleError(oError);
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
            System.out.println("\nWelcome to SPP (simplest possible platformer) 2.0!");
            System.out.println("\nYou are player # " + oMyCharacterObject.getPlayerID() + ".");
            System.out.println("\nYour avatar is the BLUE one.");
            System.out.println("\nIf you reach the TOP, you win a point!");
            System.out.println("\nIf you fall to the BOTTOM, you lose a point ('the floor is lava')!");
            System.out.println("\nOptions (click the window to give it focus first):");
            System.out.println("\tLeft Arrow: Move Left");
            System.out.println("\tRight Arrow: Move Right");
            System.out.println("\tSpace Bar: Jump");
            System.out.println("\t'Q': Quit");
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            size(HW02_Utility.getWindowSize(), HW02_Utility.getWindowSize());
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            frameRate(60);
            
            // Set default shape fill color (light gray)
            fill(200);
            
            // Set the window title
            surface.setTitle("Player " + oMyCharacterObject.getPlayerID());
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            CopyOnWriteArrayList<HW02_ObjectRenderable> aoObjectsRenderable;
            Object oObjectReadFromServer;
            HW02_ObjectRenderable oThisRenderableObject;
            int nIndexOfMyCharacterObject;
            int i;
            boolean bSpecialColor;
            
            // Quitting?
            if (bQuit == true) {
                shutDown();
            }
            else {
                
                /* Set the background color of the window (black)
                 * It is common to call background() near the beginning of the draw() loop to clear the contents of the window. 
                 * Since pixels drawn to the window are cumulative, omitting background() may result in unintended results.
                 */
                background(HW02_Color.black());
                
                // Draw a border around the window
                stroke(HW02_Color.white());
                fill(HW02_Color.black());
                rect(0, 0, HW02_Utility.getWindowSize()-1, HW02_Utility.getWindowSize()-1, 0);
                
                // Move the local character object if needed
                synchronized (HW02_Client.class) {
                    oMyCharacterObject.handleJumpStatus();
                    oMyCharacterObject.handlePlatformAdjacency();
                    oMyCharacterObject.updateLocation();
                }
                
                /* Draw all renderable game objects
                 * Synchronize here so that we don't end up 
                 * trying to draw stuff while that stuff is being replaced
                 */
                synchronized (HW02_Client.class) {
                    
                    aoObjectsRenderable = HW02_ObjectRenderable.getRenderableObjects();
                    for (i = 0; i < aoObjectsRenderable.size(); i++) {
                        
                        // Get this object
                        oThisRenderableObject = aoObjectsRenderable.get(i);
                        
                        /* Before we can use any Processing methods, must give game objects a window
                         * Objects are received from the server, but the client has its own window to draw in
                         */
                        oThisRenderableObject.setApplet(this);
                        
                        // Make sure overriding display methods are actually called
                        if (oThisRenderableObject instanceof HW02_ObjectScoreboard) {
                            ((HW02_ObjectScoreboard) oThisRenderableObject).display(false);
                        }
                        else if (oThisRenderableObject instanceof HW02_ObjectCharacter) {
                            bSpecialColor = (oThisRenderableObject.getGUID() == oMyCharacterObject.getGUID());
                            ((HW02_ObjectCharacter) oThisRenderableObject).display(bSpecialColor);
                        }
                        else {
                            oThisRenderableObject.display(false);
                        }
                        
                    }
                    aoObjectsRenderable = null;
                    
                }
                
                /* Wake up the write thread to tell the server where we are now
                 * Synchronizing on the output stream so that specifically the write thread can be woken
                 * Only do this if something actually changed, to minimize traffic
                 * Every time the client updates the server, 
                 *  the server has to update all other clients, 
                 *  and all other clients have to do work based on this update
                 */
                sCharacterAsStringCurrent = oMyCharacterObject.toString();
                if (sCharacterAsStringCurrent.equals(sCharacterAsStringPrevious) == false) {
                    synchronized (oStreamOut) {
                        oStreamOut.notify();
                    }
                }
                sCharacterAsStringPrevious = new String(sCharacterAsStringCurrent);
                
                // Take a screenshot
                if (nTotalIterations == 0) {
                    nTotalIterations++;
                    saveFrame(new Random().nextInt(10000) + ".png");
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            }
            /* JUMP is an instant action that happens on key press
             * Holding down the key may result in this getting called multiple times
             * and we do not want that behavior so we guard against it here
             */
            else if (key == ' ' && oMyCharacterObject.getJumpAllowed() == true) {
                oMyCharacterObject.startJump();
                oMyCharacterObject.setJumpAllowed(false);
            }
            // LEFT is a continuous action that happens as long as the key is depressed
            else if (keyCode == LEFT) {
                oMyCharacterObject.setHorizontalDirection(-1);
            }
            // RIGHT is a continuous action that happens as long as the key is depressed
            else if (keyCode == RIGHT) {
                oMyCharacterObject.setHorizontalDirection(1);
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
                oMyCharacterObject.setJumpAllowed(true);
            }
            // LEFT is a continuous action that happens as long as the key is depressed
            else if (keyCode == LEFT) {
                oMyCharacterObject.setHorizontalDirection(0);
            }
            // RIGHT is a continuous action that happens as long as the key is depressed
            else if (keyCode == RIGHT) {
                oMyCharacterObject.setHorizontalDirection(0);
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            HW02_Utility.handleError(oError);
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
                catch (SocketException oError) {
                    // Ignore
                }
                
                // Goodbye Cruel World
                System.out.println("Player " + oMyCharacterObject.getPlayerID() + " is leaving the game!");
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
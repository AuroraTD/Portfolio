package HW01;
/*************************************************************************************************************
 * FILE:            HW01_Server_Async.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Explore the basics of constructing a multi-threaded network server.
 *                  ASYNCHRONOUS version.
 *                  The server application initializes the game world,
 *                      and keeps track of where all clients' player objects are within the world.
 *                  Data is sent to and received from the game clients:
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
 *                  https://moodle-courses1819.wolfware.ncsu.edu/pluginfile.php/773435/mod_resource/content/1/SampleServer.java
 *
 * TO COMPILE:      javac -classpath .:./core.jar HW01_Server_Async.java
 * 
 * TO RUN:          java -classpath .:./core.jar HW01_Server_Async
 *************************************************************************************************************/

// IMPORTS
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Math;
import processing.core.PApplet;

// CLASS DEFINITION
public class HW01_Server_Async extends PApplet implements Runnable {

    //  Object properties
    private static CopyOnWriteArrayList<HW01_RenderableObject>  aoGameObjects;
    private static CopyOnWriteArrayList<HW01_PlayerObject>      aoPlayerObjects;
    private static CopyOnWriteArrayList<ObjectOutputStream>     aoStreamsOut;
    private static CopyOnWriteArrayList<ObjectInputStream>      aoStreamsIn;
    private static ServerSocket                                 oServerSocket;
    private static int                                          nNextGUID;
    private static boolean                                      bQuit;
    private static final int                                    N_NUM_OBSTACLES = 3;
    private static final int                                    B_DEBUG = 0;
    private static final int                                    N_DEBUG_WAIT_MS = 1000;
    String sThreadType;
    int nThreadClientGUID;
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_Server_Async constructor
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
     *                  nGUID - The GUID of the client that the .run() method is concerned with.
     *                          Not used in the case of "Accept".
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW01_Server_Async (String sType, int nGUID) {
        this.sThreadType = sType;
        this.nThreadClientGUID = nGUID;
    }
    
    /*********************************************************************************************************
     * FUNCTION:        HW01_Server_Async constructor
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     A second constructor, this exists to accommodate the call:
     *                  PApplet.main("HW01_Server_Async");
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW01_Server_Async () {
        // Nothing to do here, this exists to accommodate the call: PApplet.main("HW01_Server_Async");
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
    @SuppressWarnings("unused")
    public void run () {
        try {
            
            // Declare variables
            HW01_Server_Async oServerInstanceRead;
            HW01_Server_Async oServerInstanceWrite;
            Socket oSocket = null;
            HW01_PlayerObject oNewPlayerObject;
            HW01_PlayerObject oUpdatedPlayerObject;
            boolean bClientStillExistsRead;
            boolean bClientStillExistsWrite;
            int nPlayerObjectIndex;
            int nX_px;
            int i;
            
            // ACCEPT NEW CLIENTS
            if (this.sThreadType.equals("Accept")) {
                
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
                        
                        /* Perform all work needed for newly connected client
                         *  Synchronizing on the class to be very explicit for readers of the code, 
                         *  and to control access to items used across multiple threads
                         */
                        synchronized (HW01_Server_Async.class) {
                            
                            // Add this connection to our list of connections
                            aoStreamsOut.add(new ObjectOutputStream(oSocket.getOutputStream()));
                            aoStreamsIn.add(new ObjectInputStream(oSocket.getInputStream()));
                            
                            // Print to terminal that we have a new client
                            System.out.println("Client " + nNextGUID + " has joined the game!");
                            
                            // Create a new player object for the newly connected client
                            nX_px = 0;
                            oNewPlayerObject = new HW01_PlayerObject(
                                // Give the new player object a unique ID
                                nNextGUID, 
                                // Give the player an x-axis location (start on the left)
                                nX_px, 
                                // All players are on the ground (will update Y position soon to reflect this properly)
                                0,
                                // Do not pass a window to draw in, server doesn't draw objects
                                null
                            );
                            
                            /* Put player object on the ground
                             * (0,0) is TOP LEFT of the renderable object
                             * (0,0) is TOP LEFT of the drawing window
                             */
                            oNewPlayerObject.setPositionY(HW01_Utility.getWindowSize() - oNewPlayerObject.getHeight());
                            
                            /* Move the new player object until it has no collisions
                             *  This assumes that we won't ever have so many clients 
                             *      that there just isn't room for them all
                             *  This is one of the simplifying assumptions made for the time scale of a HW assignment
                             */
                            while (oNewPlayerObject.doesObjectCollide(aoGameObjects) == true) {
                                nX_px += 5;
                                oNewPlayerObject.setPositionX(nX_px);
                            }
                            
                            /* Add the player object to our collections
                             * How do existing clients find out about the new client?
                             * Because we wake up the "Write" thread for every other client
                             */
                            aoGameObjects.add(oNewPlayerObject);
                            aoPlayerObjects.add(oNewPlayerObject);
                            
                            /* Tell the new client about all the game world objects
                             *  This sends as a list so that the client can just read one object (the list)
                             */
                            aoStreamsOut.get(aoStreamsOut.size()-1).writeObject(aoGameObjects);
                            aoStreamsOut.get(aoStreamsOut.size()-1).flush();
                            
                            // Print debug info
                            if (B_DEBUG >= 1) {
                                System.out.println("Server has written " + aoGameObjects.size() + " game objects to client");
                                Thread.sleep(N_DEBUG_WAIT_MS);
                            }
                            
                            /* Start 2 new threads, to read from and write to our new client
                             * Do this only after all the collections have been populated,
                             * because the threads might need them
                             */
                            oServerInstanceRead = new HW01_Server_Async("Read", nNextGUID);
                            (new Thread(oServerInstanceRead)).start();
                            oServerInstanceWrite = new HW01_Server_Async("Write", nNextGUID);
                            (new Thread(oServerInstanceWrite)).start();
                            
                            // Everyone else needs to know about this new player
                            nPlayerObjectIndex = HW01_Utility.getIndexOfGUID(aoPlayerObjects, nNextGUID);
                            for (i = 0; i < aoStreamsOut.size(); i++) {
                                if (i != nPlayerObjectIndex) {
                                    // Synchronizing on one specific output stream so that this specific write thread can be woken
                                    synchronized (aoStreamsOut.get(i)) {
                                        aoStreamsOut.get(i).notify();
                                    }
                                }
                            }
                            
                            // Update the GUID tracker
                            nNextGUID++;
                            
                        }
                        
                    }

                }
                
            }
            
            // READ FROM CLIENT
            else if (this.sThreadType.equals("Read")) {
                
                // Read from this one client "forever"
                bClientStillExistsRead = true;
                while (bQuit != true && bClientStillExistsRead == true) {
                    
                    /* Figure out which input stream we're reading from
                     * Input streams come and go and change index in our collection as clients connect and disconnect
                     * We want to get the one input stream that we were born for
                     * If it isn't there, die a natural death
                     */
                    nPlayerObjectIndex = HW01_Utility.getIndexOfGUID(aoPlayerObjects, this.nThreadClientGUID);
                    if (nPlayerObjectIndex >= 0) {
                        
                        /* Wait for this client to let us know about player object change (if they haven't quit)
                         *  https://stackoverflow.com/questions/30559373/socket-isclosed-returns-false-after-client-disconnected
                         */
                        oUpdatedPlayerObject = null;
                        try {
                            
                            // Read player object
                            oUpdatedPlayerObject = (HW01_PlayerObject) aoStreamsIn.get(nPlayerObjectIndex).readObject();
                            
                            // Print debug info
                            if (B_DEBUG >= 1) {
                                System.out.println("Server has read client's player object");
                                Thread.sleep(N_DEBUG_WAIT_MS);
                            }
                            
                        }
                        catch (Throwable oError) {
                            bClientStillExistsRead = false;
                            handleClientDeparture(this.nThreadClientGUID);
                        }
                        
                        /* Act on success or failure of read
                         *  All game clients, on every game loop, should send their player object so we know where they are
                         *  Synchronizing on the class to be very explicit for readers of the code, 
                         *  and to control access to items used across multiple threads
                         */
                        synchronized (HW01_Server_Async.class) {
                            
                            // Client has sent an update
                            if (bClientStillExistsRead == true && oUpdatedPlayerObject != null) {
                                
                                // Get player index again, it might have changed while we were waiting
                                nPlayerObjectIndex = HW01_Utility.getIndexOfGUID(aoPlayerObjects, this.nThreadClientGUID);
                                if (nPlayerObjectIndex >= 0) {
                                    
                                    // Update our own copy of the player object
                                    aoPlayerObjects.get(nPlayerObjectIndex).setJumping(oUpdatedPlayerObject.getJumping());
                                    aoPlayerObjects.get(nPlayerObjectIndex).setMovementDirection(oUpdatedPlayerObject.getMovementDirection());
                                    aoPlayerObjects.get(nPlayerObjectIndex).setPositionX(oUpdatedPlayerObject.getPositionX());
                                    aoPlayerObjects.get(nPlayerObjectIndex).setPositionY(oUpdatedPlayerObject.getPositionY());
                                    
                                }
                                else {
                                    bClientStillExistsRead = false;
                                    handleClientDeparture(this.nThreadClientGUID);
                                }
                                
                            }
                            
                            // Either way, everyone else needs to know about this changed or lost player
                            for (i = 0; i < aoStreamsOut.size(); i++) {
                                if (i != nPlayerObjectIndex || oUpdatedPlayerObject == null) {
                                    // Synchronizing on one specific output stream so that this specific write thread can be woken
                                    synchronized (aoStreamsOut.get(i)) {
                                        aoStreamsOut.get(i).notify();
                                    }
                                }
                            }
                            
                        }
                        
                    }
                    else {
                        bClientStillExistsRead = false;
                        handleClientDeparture(this.nThreadClientGUID);
                    }
                    
                }
                
            }
            
            // WRITE TO CLIENT
            else if (this.sThreadType.equals("Write")) {
                
                // Write to this one client "forever"
                bClientStillExistsWrite = true;
                while (bQuit != true && bClientStillExistsWrite) {
                    
                    /* Figure out which output stream we're writing to
                     * Output streams come and go and change index in our collection as clients connect and disconnect
                     * We want to get the one output stream that we were born for
                     * If it isn't there, die a natural death
                     */
                    nPlayerObjectIndex = HW01_Utility.getIndexOfGUID(aoPlayerObjects, this.nThreadClientGUID);
                    if (nPlayerObjectIndex >= 0) {
                        
                        /* Wait to be woken up
                         * Synchronizing on one specific output stream so that this specific write thread can be woken
                         */
                        synchronized (aoStreamsOut.get(nPlayerObjectIndex)) {
                            aoStreamsOut.get(nPlayerObjectIndex).wait();
                        }

                        /* Tell this client where all the players are 
                         *  Synchronizing on the class to be very explicit for readers of the code, 
                         *  and to control access to items used across multiple threads
                         */
                        synchronized (HW01_Server_Async.class) {
                            
                            // Get player index again, it might have changed while we were waiting
                            nPlayerObjectIndex = HW01_Utility.getIndexOfGUID(aoPlayerObjects, this.nThreadClientGUID);
                            
                            /* Write player objects to this client (if they haven't quit)
                             *  The non-player objects (in this simple version of a game) never change
                             *  This sends as a list so that the client can just read one object (the list)
                             * https://stackoverflow.com/questions/41058548/why-an-object-doesnt-change-when-i-send-it-through-writeobject-method
                             */
                            if (nPlayerObjectIndex >= 0) {
                                
                                try {
                                    aoStreamsOut.get(nPlayerObjectIndex).reset();
                                    aoStreamsOut.get(nPlayerObjectIndex).writeObject(aoPlayerObjects);
                                    aoStreamsOut.get(nPlayerObjectIndex).flush();
                                }
                                catch (Throwable oError) {
                                    bClientStillExistsWrite = false;
                                    handleClientDeparture(this.nThreadClientGUID);
                                }
                                
                            }
                            else {
                                // Make sure we don't loop again
                                bClientStillExistsWrite = false;
                                handleClientDeparture(this.nThreadClientGUID);
                            }
                            
                            // Print debug info
                            if (B_DEBUG >= 1) {
                                System.out.println("Server has written object to client: " + aoPlayerObjects.toString());
                                System.out.println("Server has written " + aoPlayerObjects.size() + " player objects to client");
                                Thread.sleep(N_DEBUG_WAIT_MS);
                            }
                            
                        }
                        
                    }
                    else {
                        bClientStillExistsWrite = false;
                        handleClientDeparture(this.nThreadClientGUID);
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
     *                      Create game world objects
     *                      Start thread to listen for new client connections
     *                      Start game loop (draw function of Processing API)
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void main (String[] args) {
        try {
            
            // Declare constants
            final int N_MAX_WIDTH_PX = (int) Math.floor(HW01_Utility.getWindowSize() / (N_NUM_OBSTACLES * 4));
            final int N_MAX_HEIGHT_PX = (int) Math.floor(HW01_Utility.getWindowSize() / 5);
            
            // Declare variables
            HW01_Server_Async oServerInstanceAccept;
            int nX_px;
            int nWidth_px;
            int nHeight_px;
            int i;
            Random oRandomizer;
            
            // Check arguments
            if (args.length != 0) {
                System.out.println("\nUsage: java HW01_Server_Async");
            }
            else {
                
                // Hello World
                System.out.println("\nGame server is running!");
                System.out.println("Options (click the window to give it focus first):");
                System.out.println("\t'Q': Quit");
                
                // Assume the user does not want to quit until we know otherwise
                bQuit = false;
                
                // Start GUID tracker
                nNextGUID = 1;
                
                // Initialize thread-safe collections
                aoStreamsIn = new CopyOnWriteArrayList<ObjectInputStream>();
                aoStreamsOut = new CopyOnWriteArrayList<ObjectOutputStream>();
                aoGameObjects = new CopyOnWriteArrayList<HW01_RenderableObject>();
                aoPlayerObjects = new CopyOnWriteArrayList<HW01_PlayerObject>();
                
                // Get randomizer
                oRandomizer = new Random();
                
                // Create initial game world objects (a few obstacles)
                for (i = 0; i < N_NUM_OBSTACLES; i++) {
                    
                    /* Randomize width and height
                     * https://docs.oracle.com/javase/8/docs/api/java/util/Random.html#nextInt--
                     */
                    nWidth_px = oRandomizer.nextInt(N_MAX_WIDTH_PX);
                    nHeight_px = oRandomizer.nextInt(N_MAX_HEIGHT_PX);
                    
                    // Roughly space the obstacles out along the x-axis
                    nX_px = (int) Math.floor(HW01_Utility.getWindowSize() / N_NUM_OBSTACLES) * i;
                    
                    // Create an obstacle and add it to our collection of game objects
                    aoGameObjects.add(new HW01_WorldObject(
                        // Give the new obstacle a unique ID
                        nNextGUID, 
                        // Give the obstacle an x-axis location
                        nX_px, 
                        /* All obstacles are on the ground
                         *  (0,0) is TOP LEFT of the renderable object
                         *  (0,0) is TOP LEFT of the drawing window
                         */
                        HW01_Utility.getWindowSize() - nHeight_px, 
                        // Give the obstacle a width
                        nWidth_px, 
                        // Give the obstacle a height
                        nHeight_px, 
                        // Do not pass a window to draw in, server doesn't draw objects
                        null
                    ));
                    
                    // Update the GUID tracker
                    nNextGUID++;
                    
                }
                
                // Create a server socket
                oServerSocket = new ServerSocket(HW01_Utility.getPortNumber());

                // Start a thread to listen for new client connections
                oServerInstanceAccept = new HW01_Server_Async("Accept", -1);
                (new Thread(oServerInstanceAccept)).start();
                
                /* Start a PApplet application and tell it to use our class
                 * This will run settings() and setup() and then will start running draw() continuously
                 * So this comes after communication is established with the server
                 */
                PApplet.main("HW01_Server_Async");
                
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
            size(0, 0);
            
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
        // Nothing actually needed here, not drawing on server
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
     *                      For now, this is to make detection of key presses easy.
     *                      In future, this could be used to render game objects to the screen from the server
     *                      (could be helpful for debugging).
     *
     * ARGUMENTS:       None
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void draw () {
        try {
            if (bQuit == true) {
                shutDown();
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
            }
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
        
    }
    
    /*********************************************************************************************************
     * FUNCTION:        handleClientDeparture
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Handle the case that a client leaves the game.
     *                  Not using a synchronized block in here.
     *                  If the client has left the game, 
     *                      it's objects should be removed from collections immediately.
     *                  Other threads should detect this.
     *
     * ARGUMENTS:       nGUID - The GUID of the client's player object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public void handleClientDeparture (int nGUID) {
        try {
            
            // Declare variables
            int nPlayerObjectIndex;
            int nGameObjectIndex;
           
            /* Get indexes
             * Player and stream collections are all aligned with one another
             */
            nPlayerObjectIndex = HW01_Utility.getIndexOfGUID(aoPlayerObjects, nGUID);
            nGameObjectIndex = HW01_Utility.getIndexOfGUID(aoGameObjects, nGUID);
            
            // Close streams
            try {
                aoStreamsOut.get(nPlayerObjectIndex).close();
                aoStreamsIn.get(nPlayerObjectIndex).close();
            }
            catch (Throwable oError) {
                // Ignore
            }
            
            // Remove client player object from ALL collections where it resides
            if (nGameObjectIndex >= 0) {
                aoGameObjects.remove(nGameObjectIndex);
            }
            if (nPlayerObjectIndex >= 0) {
                aoPlayerObjects.remove(nPlayerObjectIndex);
                aoStreamsOut.remove(nPlayerObjectIndex);
                aoStreamsIn.remove(nPlayerObjectIndex);
            }
            
            /* IF we are the first to know about it tell the user what's going on
             * Read and write threads could independently discover that a client has left the game
             */
            if (nPlayerObjectIndex >= 0 && nGameObjectIndex >= 0) {
                // Goodbye cruel world
                System.out.println("Client " + nGUID + " has left the game!");
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
    public void shutDown () {
        try {
            
            // Declare variables
            int i;
            
            // Goodbye Cruel World
            System.out.println("Game server is shutting down!");
            
            // Close streams
            for (i = 0; i < aoStreamsOut.size(); i++) {
                aoStreamsOut.get(i).close();
            }
            for (i = 0; i < aoStreamsIn.size(); i++) {
                aoStreamsIn.get(i).close();
            }
            
            // Close the socket
            oServerSocket.close();
            
            // Exit
            System.exit(0);
            
        }
        catch (Throwable oError) {
            HW01_Utility.handleError(oError);
        }
    }

}
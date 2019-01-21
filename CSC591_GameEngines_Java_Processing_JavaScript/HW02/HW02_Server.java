package HW02;
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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import processing.core.PApplet;


// CLASS DEFINITION
public class HW02_Server extends PApplet implements Runnable {

    //  Object properties
    private static ConcurrentHashMap<Integer, ObjectOutputStream>                       oStreamsOut;
    private static ConcurrentHashMap<Integer, ObjectInputStream>                        oStreamsIn;
    private static ConcurrentHashMap<Integer, CopyOnWriteArrayList<HW02_ObjectGame>>    oObjectsToWrite;
    private static ServerSocket                                                         oServerSocket;
    private static boolean                                                              bQuit;
    
    private static int                                                                  nArgIterations =        0;
    private static int                                                                  nArgNetworkProtocol =   0;
    private static int                                                                  nArgPlatformsStatic =   3;
    private static int                                                                  nArgPlatformsMoving =   6;
    private static int                                                                  nClients =              0;
    private static int                                                                  nTotalIterations =      0;
    private static int                                                                  nMeasuredIterations =   0;
    private static long                                                                 nTimeStartApplication_ms;
    private static long                                                                 nTimeStartLoop_ms;
    private static long                                                                 nTimeStartLoopMeas_ms;
    private static long                                                                 nTimeStopLoopMeas_ms;
    
    String                                                                              sThreadType;
    
    int                                                                                 nThreadClientGUID;
    
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
     *                  nGUID - The GUID of the client that the .run() method is concerned with.
     *                          Not used in the case of "Accept".
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public HW02_Server (String sType, int nGUID) {
        this.sThreadType = sType;
        this.nThreadClientGUID = nGUID;
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
    public HW02_Server () {
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
     * ARGUMENTS:       0 - Number of game loop iterations to run performance testing on
     *                      If and only if this number is > 0,
     *                      the program will print out performance results and shut down 
     *                      after the specified number of iterations.
     *                      Default = 0
     *                  1 - Network protocol (information sent across network)
     *                      0 means game objects
     *                      1 means use .writeReplace() and .readResolve()
     *                      Default = 0
     *                  2 - Number of static platforms to include in the game world
     *                      Default = 3
     *                  3 - Number of moving platforms to include in the game world
     *                      Default = 6
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    public static void main (String[] args) {
        try {
            
            // Declare variables
            HW02_Server oServerInstanceAccept;
            
            // Check arguments
            if (args.length > 4) {
                System.out.println("\nUsage: java -classpath .:./core.jar HW03_Server [# iterations for performance test] [network protocol] [# static platforms] [# moving platforms]");
            }
            if (args.length > 0) {
                nArgIterations = Integer.parseInt(args[0]);
            }
            if (args.length > 1) {
                nArgNetworkProtocol = Integer.parseInt(args[1]);
                HW02_ObjectGame.setNetworkProtocol(nArgNetworkProtocol);
            }
            if (args.length > 2) {
                nArgPlatformsStatic = Integer.parseInt(args[2]);
            }
            if (args.length > 3) {
                nArgPlatformsMoving = Integer.parseInt(args[3]);
            }

            // Start performance measurement
            nTimeStartApplication_ms = System.currentTimeMillis();
            
            // Hello World
            System.out.println("\nGame server is running!");
            if (nArgIterations > 0) {
                System.out.println("Will run performance test with " + nArgIterations + " iterations");
            }
            else {
                System.out.println("Normal gameplay (no performance test)");
            }
            System.out.println(
                "Network Protocol: " + 
                (nArgNetworkProtocol == 0 ? "Send Game Objects" : "Use .writeReplace() and .readResolve()")
            );
            System.out.println("Static Platforms: " + nArgPlatformsStatic);
            System.out.println("Moving Platforms: " + nArgPlatformsMoving);
            System.out.println("\nOptions (click the window to give it focus first):");
            System.out.println("\t'Q': Quit");
            
            // Assume the user does not want to quit until we know otherwise
            bQuit = false;
            
            // Initialize thread-safe collections
            oStreamsOut =       new ConcurrentHashMap<Integer, ObjectOutputStream>();
            oStreamsIn =        new ConcurrentHashMap<Integer, ObjectInputStream>();
            oObjectsToWrite =   new ConcurrentHashMap<Integer, CopyOnWriteArrayList<HW02_ObjectGame>>();
            
            // Add scoreboard
            new HW02_ObjectScoreboard(-1, null);
            
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
            oServerSocket = new ServerSocket(HW02_Utility.setPortNumber());

            // Start a thread to listen for new client connections
            oServerInstanceAccept = new HW02_Server("Accept", -1);
            (new Thread(oServerInstanceAccept)).start();
            
            /* Start a PApplet application and tell it to use our class
             * This will run settings() and setup() and then will start running draw() continuously
             * So this comes after communication is established with the server
             */
            PApplet.main("HW03_Server");
            
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
                threadReadFromClient(this.nThreadClientGUID); 
            }
            
            // WRITE TO CLIENT
            else if (this.sThreadType.equals("Write")) {
                threadWriteToClient(this.nThreadClientGUID);
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
            ConcurrentHashMap<Integer, HW02_ObjectGame> oObjectsGame;
            HW02_Server oServerInstanceRead;
            HW02_Server oServerInstanceWrite;
            Socket oSocket = null;
            HW02_ObjectCharacter oNewObjectCharacter;
            int nNewClientGUID;
            
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
                    oNewObjectCharacter = new HW02_ObjectCharacter(
                        -1,
                        -1,
                        // Give the player an x-axis location (will move to spawn point later)
                        0, 
                        // Give the player a y-axis location (will move to spawn point later)
                        0,
                        // Do not pass a window to draw in, server doesn't draw objects
                        null
                    );
                    
                    // Print to terminal that we have a new client
                    System.out.println("Player " + oNewObjectCharacter.getPlayerID() + " has joined the game!");
                    
                    // Add this connection to our list of connections
                    nNewClientGUID = oNewObjectCharacter.getGUID();
                    oStreamsOut.put(nNewClientGUID, new ObjectOutputStream(oSocket.getOutputStream()));
                    oStreamsIn.put(nNewClientGUID, new ObjectInputStream(oSocket.getInputStream()));
                    
                    // Move the new character object to a spawn point
                    oNewObjectCharacter.spawn();
                    
                    // Start 2 new threads, to read from and write to our new client
                    oServerInstanceRead = new HW02_Server("Read", nNewClientGUID);
                    (new Thread(oServerInstanceRead)).start();
                    oServerInstanceWrite = new HW02_Server("Write", nNewClientGUID);
                    (new Thread(oServerInstanceWrite)).start();
                    
                    /* Tell the client about their character object and then all other game objects
                     * The first object we send, the client interprets as their character object
                     */
                    oObjectsToWrite.put(nNewClientGUID, new CopyOnWriteArrayList<HW02_ObjectGame>());
                    
                    /* Synchronizing between adding objects to write, and write-clear of those objects
                     *  Otherwise, we could write out objects,
                     *      then add another object,
                     *      then clear objects,
                     *      thus failing to write an object we want to write
                     */
                    synchronized (HW02_Server.class) {
                        oObjectsToWrite.get(nNewClientGUID).add(oNewObjectCharacter);
                        oObjectsGame = HW02_ObjectGame.getGameObjects();
                        for (ConcurrentHashMap.Entry<Integer, HW02_ObjectGame> oEntry : oObjectsGame.entrySet()) {
                            if (oEntry.getKey() != nNewClientGUID) {
                                oObjectsToWrite.get(nNewClientGUID).add(oEntry.getValue());
                            }
                        }
                        oObjectsGame = null;
                    }
                    
                    // Notify write thread to go ahead
                    synchronized (oStreamsOut.get(nNewClientGUID)) {
                        oStreamsOut.get(nNewClientGUID).notify();
                    }

                    // Every other client needs to know about this new character object
                    notifyClientsAboutOneObject(nNewClientGUID, oNewObjectCharacter);
                    
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
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        threadReadFromClient
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Read from a client
     *
     * ARGUMENTS:       nThreadClientGUID - The GUID of the client's character object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void threadReadFromClient (int nThreadClientGUID) {
        try {
            
            // Declare variables
            HW02_ObjectCharacter oUpdatedObjectCharacter;
            boolean bClientStillExistsRead;
            
            // Read from this one client "forever"
            bClientStillExistsRead = true;
            while (bQuit != true && bClientStillExistsRead == true) {
                
                /* Figure out which input stream we're reading from
                 * Input streams come and go as clients connect and disconnect
                 * We want to get the one input stream that we were born for
                 * If it isn't there, die a natural death
                 */
                if (oStreamsIn.containsKey(nThreadClientGUID)) {
                    
                    /* Wait for this client to let us know about character object change (if they haven't quit)
                     *  https://stackoverflow.com/questions/30559373/socket-isclosed-returns-false-after-client-disconnected
                     */
                    oUpdatedObjectCharacter = null;
                    try {
                        // Read character object
                        oUpdatedObjectCharacter = (HW02_ObjectCharacter) oStreamsIn.get(nThreadClientGUID).readObject();
                    }
                    catch (Throwable oError) {
                        bClientStillExistsRead = false;
                        handleClientDeparture(nThreadClientGUID);
                    }
                    
                    // Client has sent an update
                    if (bClientStillExistsRead == true && oUpdatedObjectCharacter != null) {
                        
                        // Check for existence of client again, it might have changed while we were waiting
                        if (oStreamsIn.containsKey(nThreadClientGUID)) {
                            
                            // Update our own copy of the character object
                            HW02_ObjectGame.replaceObjectByGUID(nThreadClientGUID, oUpdatedObjectCharacter);
                            
                            // Everyone else needs to know that this client updated
                            notifyClientsAboutOneObject(nThreadClientGUID, oUpdatedObjectCharacter);
                            
                        }
                        else {
                            bClientStillExistsRead = false;
                            handleClientDeparture(nThreadClientGUID);
                        }
                        
                    }
                    
                }
                else {
                    bClientStillExistsRead = false;
                    handleClientDeparture(nThreadClientGUID);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        threadWriteToClient
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Write to a client
     *
     * ARGUMENTS:       nThreadClientGUID - The GUID of the client's character object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private void threadWriteToClient (int nThreadClientGUID) {
        try {
            
            // Declare variables
            boolean bClientStillExistsWrite;
            
            // Write to this one client "forever"
            bClientStillExistsWrite = true;
            while (bQuit != true && bClientStillExistsWrite) {
                
                /* Figure out which output stream we're writing to
                 * Output streams come and go as clients connect and disconnect
                 * We want to get the one output stream that we were born for
                 * If it isn't there, die a natural death
                 */
                if (oStreamsOut.containsKey(nThreadClientGUID)) {
                    
                    /* Wait to be woken up
                     * Synchronizing because it is required for wait
                     */
                    synchronized (oStreamsOut.get(nThreadClientGUID)) {
                        oStreamsOut.get(nThreadClientGUID).wait();
                    }
                    
                    /* Synchronizing between adding objects to write, and write-clear of those objects
                     *  Otherwise, we could write out objects,
                     *      then add another object,
                     *      then clear objects,
                     *      thus failing to write an object we want to write
                     */
                    synchronized (HW02_Server.class) {
                        
                        /* Write all changed game objects to this client (if they haven't quit)
                         * https://stackoverflow.com/questions/41058548/why-an-object-doesnt-change-when-i-send-it-through-writeobject-method
                         */
                        if (oStreamsOut.containsKey(nThreadClientGUID)) {
                                
                            try {
                                
                                // Write
                                oStreamsOut.get(nThreadClientGUID).reset();
                                oStreamsOut.get(nThreadClientGUID).writeObject(oObjectsToWrite.get(nThreadClientGUID));
                                oStreamsOut.get(nThreadClientGUID).flush();
                                
                                // Clear collection so we don't repeat ourselves next time
                                oObjectsToWrite.get(nThreadClientGUID).clear();

                            }
                            catch (Throwable oError) {
                                bClientStillExistsWrite = false;
                                handleClientDeparture(nThreadClientGUID);
                            }
                            
                        }
                        else {
                            // Make sure we don't loop again
                            bClientStillExistsWrite = false;
                            handleClientDeparture(nThreadClientGUID);
                        }
                        
                    }
                    
                }
                else {
                    bClientStillExistsWrite = false;
                    handleClientDeparture(nThreadClientGUID);
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        notifyClients
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Notify clients (wake up the write thread for clients)
     *
     * ARGUMENTS:       nExcludedGUID - The GUID of a client to exclude from the notification (or -1)
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void notifyClients (int nExcludedGUID) {
        try {
            
            for (ConcurrentHashMap.Entry<Integer, ObjectOutputStream> oEntry : oStreamsOut.entrySet()) {
                if (nExcludedGUID == -1 || oEntry.getKey() != nExcludedGUID) {
                    // Synchronizing because it is required for notify
                    synchronized (oEntry.getValue()) {
                        oEntry.getValue().notify();
                    }
                }
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        notifyClientsAboutOneObject
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Notify clients of the current status of one game object
     *
     * ARGUMENTS:       nExcludedGUID -     The GUID of a client to exclude from the notification (or -1)
     *                  oObjectToWrite -    A game object to write to clients
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    static void notifyClientsAboutOneObject (int nExcludedGUID, HW02_ObjectGame oObjectToWrite) {
        try {
            
            // Check to make sure that we actually have some clients
            if (oObjectsToWrite.isEmpty() == false) {
                
                // Populate info to write
                for (ConcurrentHashMap.Entry<Integer, CopyOnWriteArrayList<HW02_ObjectGame>> oEntry : oObjectsToWrite.entrySet()) {
                    if (nExcludedGUID == -1 || oEntry.getKey() != nExcludedGUID) {
                        /* Synchronizing between adding objects to write, and write-clear of those objects
                         *  Otherwise, we could write out objects,
                         *      then add another object,
                         *      then clear objects,
                         *      thus failing to write an object we want to write
                         */
                        synchronized (HW02_Server.class) {
                            oEntry.getValue().add(oObjectToWrite);
                        }
                    }
                }
                
                // Write to these clients
                notifyClients(nExcludedGUID);
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
        }
    }
    
    /*********************************************************************************************************
     * FUNCTION:        notifyClientsAboutManyObjects
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Notify clients of the current status of multiple game objects
     *
     * ARGUMENTS:       nExcludedGUID -     The GUID of a client to exclude from the notification (or -1)
     *                  aoObjectsToWrite -  Game objects to write to clients
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    @SuppressWarnings("unchecked")
    static void notifyClientsAboutManyObjects(int nExcludedGUID, CopyOnWriteArrayList<?> aoObjectsToWrite) {
        try {
            
            // Check to make sure that we actually have some clients
            if (oObjectsToWrite.isEmpty() == false) {
                
                for (ConcurrentHashMap.Entry<Integer, CopyOnWriteArrayList<HW02_ObjectGame>> oEntry : oObjectsToWrite.entrySet()) {
                    if (nExcludedGUID == -1 || oEntry.getKey() != nExcludedGUID) {
                        /* Synchronizing between adding objects to write, and write-clear of those objects
                         *  Otherwise, we could write out objects,
                         *      then add another object,
                         *      then clear objects,
                         *      thus failing to write an object we want to write
                         */
                        synchronized (HW02_Server.class) {
                            oEntry.getValue().addAll((CopyOnWriteArrayList<HW02_ObjectGame>) aoObjectsToWrite);
                        }
                    }
                }
                
                // Write to these clients
                notifyClients(nExcludedGUID);
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            new HW02_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                0, 
                // Length
                HW02_Utility.getWindowSize(), 
                // Vertical
                false
            );
            
            // Create boundary on window (right)
            new HW02_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                HW02_Utility.getWindowSize() - HW02_ObjectBoundary.getStaticSkinnyDimension(),
                // Y
                0, 
                // Length
                HW02_Utility.getWindowSize(), 
                // Vertical
                true
            );
            
            // Create boundary on window (bottom)
            new HW02_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                HW02_Utility.getWindowSize() - HW02_ObjectBoundary.getStaticSkinnyDimension(),
                // Length
                HW02_Utility.getWindowSize(), 
                // Vertical
                false
            );
            
            // Create boundary on window (left)
            new HW02_ObjectBoundary(
                // Get auto GUID
                -1,
                // X
                0, 
                // Y
                0, 
                // Length
                HW02_Utility.getWindowSize(), 
                // Vertical
                true
            );
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
     *                  Character could also get pushed onto some other platform
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
            
            final int N_WINDOW_AREA_PX = HW02_Utility.getWindowSize() * HW02_Utility.getWindowSize();
            final int N_PLATFORM_AREA_PX = (int) ( ((double) N_WINDOW_AREA_PX) / 5);
            final int N_PLATFORMS_TOTAL_PX = nArgPlatformsStatic + nArgPlatformsMoving;
            
            final int N_MAX_LENGTH_GUESS = max(2, (int) Math.sqrt( ((double) N_PLATFORM_AREA_PX) / ((double) N_PLATFORMS_TOTAL_PX) ));
            
            final int N_MIN_WIDTH_STATIC_PX = (nArgPlatformsStatic < 10 ? (HW02_ObjectCharacter.getStaticHeight() * 3) : 1);
            final int N_MIN_WIDTH_MOVING_PX = 1;
            
            final int N_MAX_WIDTH_STATIC_PX = max(N_MAX_LENGTH_GUESS, N_MIN_WIDTH_STATIC_PX) + 1;
            final int N_MAX_WIDTH_MOVING_PC = max(N_MAX_LENGTH_GUESS, N_MIN_WIDTH_MOVING_PX) + 1;
            
            final int N_MIN_HEIGHT_STATIC_PX = 1;
            final int N_MIN_HEIGHT_MOVING_PX = 1;
            
            final int N_MAX_HEIGHT_STATIC_PX = max(N_MAX_LENGTH_GUESS, N_MIN_HEIGHT_STATIC_PX) + 1;
            final int N_MAX_HEIGHT_MOVING_PX = max(N_MAX_LENGTH_GUESS, N_MIN_HEIGHT_MOVING_PX) + 1;
            
            // Declare variables
            Random oRandomizer;
            HW02_ObjectPlatformStatic oPlatformStatic;
            HW02_ObjectPlatformH oPlatformH;
            HW02_ObjectPlatformV oPlatformV;
            int nX_px;
            int nY_px;
            int i;
            
            // Get randomizer
            oRandomizer = new Random();
            
            // STATIC
            for (i = 0; i < nArgPlatformsStatic; i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW02_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformStatic = new HW02_ObjectPlatformStatic(
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
                    oPlatformStatic.setPositionX(oRandomizer.nextInt(HW02_Utility.getWindowSize()));
                    oPlatformStatic.setPositionY(getRandomPlatformY());
                }
                
            }
            
            // HORIZONTAL
            for (i = 0; i < (nArgPlatformsMoving / 2); i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW02_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformH = new HW02_ObjectPlatformH(
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
                    oPlatformH.setPositionX(oRandomizer.nextInt(HW02_Utility.getWindowSize()));
                    oPlatformH.setPositionY(getRandomPlatformY());
                }
                
            }
            
            // VERTICAL
            for (i = 0; i < (nArgPlatformsMoving / 2) + (nArgPlatformsMoving % 2); i++) {
                
                // Randomize location
                nX_px = oRandomizer.nextInt(HW02_Utility.getWindowSize());
                nY_px = getRandomPlatformY();
                
                // Create object
                oPlatformV = new HW02_ObjectPlatformV(
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
                    oPlatformV.setPositionX(oRandomizer.nextInt(HW02_Utility.getWindowSize()));
                    oPlatformV.setPositionY(getRandomPlatformY());
                }
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            final int N_CHARACTER_HEIGHT_PX = HW02_ObjectCharacter.getStaticHeight();
            final int N_SCOREBOARD_HEIGHT_PX = HW02_ObjectScoreboard.getStaticHeight();
            final int N_MIN_Y_PX = (N_CHARACTER_HEIGHT_PX * 4) + N_SCOREBOARD_HEIGHT_PX;
            
            // Declare variables
            Random oRandomizer;
            
            // Get randomizer
            oRandomizer = new Random();
            
            // Calculate
            nY_px = N_MIN_Y_PX + oRandomizer.nextInt(HW02_Utility.getWindowSize() - N_MIN_Y_PX);
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            CopyOnWriteArrayList<HW02_ObjectPlatformStatic> aoObjectsPlatformStatic;
            int i;
            
            // Create spawn points
            aoObjectsPlatformStatic = HW02_ObjectPlatformStatic.getStaticPlatformObjects();
            for (i = 0; i < aoObjectsPlatformStatic.size(); i++) {
                
                // Add a spawn point on TOP of each static platform
                new HW02_ObjectSpawnPoint(
                    // Get auto GUID
                    -1,
                    aoObjectsPlatformStatic.get(i).getPositionX(),
                    aoObjectsPlatformStatic.get(i).getPositionY() - 1 - HW02_ObjectCharacter.getStaticHeight()
                );
                
            }
            
            // Free
            aoObjectsPlatformStatic = null;
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            new HW02_ObjectDeathZone(
                // Get auto GUID
                -1,
                0,
                HW02_Utility.getWindowSize() - 1,
                HW02_Utility.getWindowSize(),
                1
            );
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            new HW02_ObjectWinZone(
                // Get auto GUID
                -1,
                0,
                HW02_ObjectScoreboard.getStaticHeight(),
                HW02_Utility.getWindowSize(),
                1
            );
            
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
            size(0, 0);
            
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
        
        /* If running a performance test, we want to run as fast as possible 
         * (so we can see how fast that is)
         */
        if (nArgIterations > 0) {
            frameRate(10000);
        }
        else {
            frameRate(60);
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
            CopyOnWriteArrayList<HW02_ObjectPlatformH> aoObjectsPlatformH;
            CopyOnWriteArrayList<HW02_ObjectPlatformV> aoObjectsPlatformV;
            int i;
            
            // Start performance measurement
            if (nTotalIterations == 0) {
                nTimeStartLoop_ms = System.currentTimeMillis();
            }
            
            // Detect key presses
            if (bQuit == true) {
                shutDown();
            }
            else {
                
                // Move platforms (horizontal)
                aoObjectsPlatformH = HW02_ObjectPlatformH.getHorizontalPlatformObjects();
                for (i = 0; i < aoObjectsPlatformH.size(); i++) {
                    aoObjectsPlatformH.get(i).updateLocation();
                }
                
                // Move platforms (vertical)
                aoObjectsPlatformV = HW02_ObjectPlatformV.getVerticalPlatformObjects();
                for (i = 0; i < aoObjectsPlatformV.size(); i++) {
                    aoObjectsPlatformV.get(i).updateLocation();
                }
                
                // Send updated platform locations to all clients
                notifyClientsAboutManyObjects(-1, aoObjectsPlatformH);
                notifyClientsAboutManyObjects(-1, aoObjectsPlatformV);
                
                // Free
                aoObjectsPlatformH = null;
                aoObjectsPlatformV = null;
                
                // Detect start and end of performance run
                nTotalIterations++;
                if (nClients > 0) {
                    if (nMeasuredIterations == 0) {
                        nTimeStartLoopMeas_ms = System.currentTimeMillis();
                    }
                    nMeasuredIterations++;
                    if (nArgIterations > 0 && nMeasuredIterations >= nArgIterations) {
                        nTimeStopLoopMeas_ms = System.currentTimeMillis();
                        bQuit = true;
                    }
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
            HW02_Utility.handleError(oError);
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
     * ARGUMENTS:       nGUID - The GUID of the client's character object
     * 
     * RETURNS:         None
     *********************************************************************************************************/
    private synchronized void handleClientDeparture (int nGUID) {
        try {
            
            // Declare variables
            HW02_ObjectGame oGameObjectToRemove;
            
            // Find the game object for this client
            oGameObjectToRemove = HW02_ObjectGame.getObjectByGUID(nGUID);
            
            /* IF we are the first to know about it
             * (read and write threads could independently discover that a client has left the game)
             */
            if (oGameObjectToRemove != null && oGameObjectToRemove.getRemovalFlag() == false) {
                
                // Goodbye cruel world
                System.out.println(
                    "Player " + 
                    ((HW02_ObjectCharacter) oGameObjectToRemove).getPlayerID() + 
                    " has left the game!"
                );
                
                // Tell everybody else that this character object is out of the game
                oGameObjectToRemove.setRemovalFlag();
                notifyClientsAboutOneObject(nGUID, oGameObjectToRemove);
                
                // Close streams
                try {
                    oStreamsOut.get(nGUID).close();
                    oStreamsIn.get(nGUID).close();
                }
                catch (Throwable oError) {
                    // Ignore
                }
                
                // Remove client character object from ALL collections where it resides
                HW02_ObjectGame.removeObjectByGUID(nGUID);
                oStreamsOut.remove(nGUID);
                oStreamsIn.remove(nGUID);
                
            }
            
        }
        catch (Throwable oError) {
            HW02_Utility.handleError(oError);
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
            final int N_GAME_OBJECTS =          HW02_ObjectGame.getGameObjects().size();
            final int N_MS_PER_SECOND =         1000;
            final double N_INIT_TIME_S =        ((double) (nTimeStartLoop_ms - nTimeStartApplication_ms)) / N_MS_PER_SECOND;
            final double N_CLIENT_WAIT_TIME_S = ((double) (nTimeStartLoopMeas_ms - nTimeStartLoop_ms)) / N_MS_PER_SECOND;
            final double N_TOTAL_LOOP_TIME_S =  ((double) (nTimeStopLoopMeas_ms - nTimeStartLoopMeas_ms)) / N_MS_PER_SECOND;
    
            // Print results
            System.out.println("");
            System.out.println("PERFORMANCE TEST RESULTS");
            System.out.println("Network Protocol:\t" + (nArgNetworkProtocol == 0 ? "Send Game Objects" : "Use .writeReplace() and .readResolve()"));
            System.out.println("# Static Platforms:\t" + Integer.toString(nArgPlatformsStatic));
            System.out.println("# Moving Platforms:\t" + Integer.toString(nArgPlatformsMoving));
            System.out.println("# Clients:\t" + Integer.toString(nClients));
            System.out.println("# Total Game Objects:\t" + Integer.toString(N_GAME_OBJECTS));
            System.out.println("Requested # measured iterations:\t" + Integer.toString(nArgIterations));
            System.out.println("Actual # measured iterations:\t" + Integer.toString(nMeasuredIterations));
            System.out.println("Time to Initialize (s):\t" + Double.toString(N_INIT_TIME_S));
            System.out.println("Time to Wait for Clients (s):\t" + Double.toString(N_CLIENT_WAIT_TIME_S));
            System.out.println("Time to Run Game Loop (s):\t" + Double.toString(N_TOTAL_LOOP_TIME_S));
            System.out.println("Time per Game Loop Iteration (s):\t" + Double.toString(N_TOTAL_LOOP_TIME_S / nMeasuredIterations));
            System.out.println("");
            
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
            if (nArgIterations > 0) {
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
            HW02_Utility.handleError(oError);
        }
        
        // Exit no matter what
        this.exit();
        System.exit(0);
        
    }

}
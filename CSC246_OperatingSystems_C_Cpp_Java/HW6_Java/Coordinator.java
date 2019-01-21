/**
 * 
 * @author Aurora Tiffany-Davis (attiffan)
 * 
 * 4/14/17
 * CSC 246
 * Homework 6, Q1
 * 
 * Compile command: javac Coordinator.java
 * Tested in: remote.eos.ncsu.edu
 * 
 * Simulate multiple riders who wait in line to use multiple bumper cars,
 * and vice versa (returned cars wait for the next rider)
 *
 */

public class Coordinator {
	
	// Shared variables
	static CarBuffer carBB;
	static RiderBuffer riderBB;
	private static final Object carLock = new Object();

	// Car Buffer Class
	public static class CarBuffer {

		// Fields
	    private int[] waitingCars;
	    private int carCountInLine;
	    private int carCountTotal;
	    private int enQ;
	    private int deQ;
	    
		/** 
		 * Constructor
		 * @param numberCars The number of cars at the amusement park
		 */
	    public CarBuffer(int numberCars) {
	    	// Set fields
	    	waitingCars = new int[numberCars];
	    	carCountInLine = 0;
	    	carCountTotal = numberCars;
	    	enQ = 0;
	    	deQ = 0;
	    	// Initially, the buffer is filled
	    	// (representing the bumper cars in line waiting for riders)
	    	parkAllCars(numberCars);
	    }
	    
		/** 
		 * Park All Cars
		 * The bounded buffer contains the car identifiers
		 * (as ints from 1 to number of bumper cars). 
		 * @param numberCars The number of cars at the amusement park
		 * @return 
		 */
	    private void parkAllCars(int numberCars) {
	    	int i;
	        for (i = 0; i < numberCars; i++) {
	        	returnCar(i+1);
	        }
	    }

		/** 
		 * Return Car
		 * @param carID The ID of the car to park
		 */
	    public synchronized void returnCar(int carID) {
	        if (carCountInLine == carCountTotal) {
	        	throw new IllegalStateException();
	        }
	        else {
	        	// Add car to buffer
	        	waitingCars[enQ] = carID;
	        	// Update buffer tracking info
	        	enQ = (enQ+1) % carCountTotal;
		        carCountInLine++;
		        // Notify threads that a car is available
		        synchronized (carLock) {
		        	carLock.notifyAll();
		        }
	        }
	    }

		/** 
		 * Get Car
		 * @return carID The ID of the car to drive
		 */
	    public synchronized int getCar() {
	        if (carCountInLine == 0) {
	        	throw new IllegalStateException();
	        }
	        else {
	        	// Get car ID
		        int carID = waitingCars[deQ];
		        // Update buffer tracking info
		        waitingCars[deQ] = 0;
		        deQ = (deQ+1) % carCountTotal;
		        carCountInLine--;
		        // Return car ID
		        return carID;
	        }
	    }
	    
		/** 
		 * Is Car Available
		 * @return True if there is at least one car in the buffer
		 */
	    public boolean isCarAvailable() {
	    	return carCountInLine > 0;
	    }

	}
	
	// Rider Buffer Class
	public static class RiderBuffer {

		// Fields
	    private Rider[] waitingRiders;
	    private int riderCountInLine;
	    private int riderCountTotal;
	    private int enQ;
	    private int deQ;
	    
		/** 
		 * Constructor
		 * @param numberRiders The number of riders at the amusement park
		 */
	    public RiderBuffer(int numberRiders) {
	    	// Set fields
	    	waitingRiders = new Rider[numberRiders];
	    	riderCountInLine = 0;
	    	riderCountTotal = numberRiders;
	    	enQ = 0;
	    	deQ = 0;
	    }

		/** 
		 * Enqueue Rider
		 * @param rider The rider thread to enqueue in the buffer
		 */
	    public synchronized void enQueueRider(Rider rider) {
	        if (riderCountInLine == riderCountTotal) {
	        	throw new IllegalStateException();
	        }
	        else {
	        	// Add car to buffer
	        	waitingRiders[enQ] = rider;
	        	// Update buffer tracking info
	        	enQ = (enQ+1) % riderCountTotal;
	        	riderCountInLine++;
	        }
	    }

		/** 
		 * Dequeue rider
		 * @return nextRider The rider thread to dequeue from the buffer
		 */
	    public synchronized Rider deQueueRider() {
	        if (riderCountInLine == 0) {
	        	throw new IllegalStateException();
	        }
	        else {
	        	// Get car ID
		        Rider nextRider = waitingRiders[deQ];
		        // Update buffer tracking info
		        deQ = (deQ+1) % riderCountTotal;
		        riderCountInLine--;
		        // Return car ID
		        return nextRider;
	        }
	    }
	    
		/** 
		 * Peek to see which is the next thread in line
		 * @return The rider thread at the head of the line
		 */
	    public Rider peekRider() {
	        if (riderCountInLine == 0) {
	        	throw new IllegalStateException();
	        }
	        else {
		        return waitingRiders[deQ];
	        }
	    }

	}
	
	/** 
	 * Get in Line
	 * Each time a rider wants to get in line, it calls this method. 
	 * This method contains a critical section that determines if a car is available 
	 * (i.e. is there a 'car' identifier currently in the buffer). 
	 * If not, the thread is blocked. 
	 * If there is a car, the car id is returned for the rider thread.
	 * @return carID The ID of the car to drive
	 * @throws InterruptedException 
	 */
    public static synchronized int getInLine() throws InterruptedException {
    	// Declare variables
    	int carID;
    	// Get in line for a car
    	riderBB.enQueueRider((Rider)Thread.currentThread());
    	// Critical section
    	synchronized (carLock) {
    		// Go back to sleep if no cars are available, or someone else is ahead of you in line
            while (!carBB.isCarAvailable() || riderBB.peekRider() != (Rider)Thread.currentThread()) {
            	carLock.wait();
            }
            riderBB.deQueueRider();
            carID = carBB.getCar();
    	}
        // Return
        return carID;
    }

	/** 
	 * MAIN function
	 * Simulate multiple riders who wait in line to use multiple bumper cars,
	 * and vice versa (returned cars wait for the next rider)
	 * @param args Arguments
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// Declare variables
		int numCars;
		int numRiders;
		int simTimeSeconds;
		int i;
		Rider [] thr;
		// Check arguments
		// The first is the number of bumper cars
		// The second is the number of riders
		// The third is the number of seconds for the simulation
		if(args.length != 3){
			System.out.println("Please provide 3 args: # bumper cars, # riders, # seconds for simulation");
			System.exit(0);
		}
		else {
			// Read in arguments
			numCars = Integer.parseInt(args[0]);
			numRiders = Integer.parseInt(args[1]);
			simTimeSeconds = Integer.parseInt(args[2]);
			// Create the bumper car bounded buffer
			carBB = new CarBuffer(numCars);
			// Create the rider bounded buffer
			riderBB = new RiderBuffer(numRiders);
			// Create rider threads
			thr = new Rider[numRiders];
			for (i = 0; i < numRiders; i++) {
				thr[i] = new Rider();
			}
			// Start rider threads
			for (i = 0; i < numRiders; i++) {
				thr[i].start();
			}
			// Run simulation for specified length of time
			Thread.sleep(simTimeSeconds*1000);
			System.exit(0);
		}
	}

}

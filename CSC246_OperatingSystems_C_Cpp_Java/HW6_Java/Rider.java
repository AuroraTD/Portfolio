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
 */

	// Rider thread class
	public class Rider extends Thread{

		// Shared variables
		private int id;
		public static int numStartedRiders = 0;
		private static final Object idLock = new Object();
		
		/** 
		 * Constructor
		 * @param riderID The ID of the rider thread
		 */
		public Rider () {
			// NOT setting the real rider ID here, will do that once the rider is reported
			// This is really just for convenience, so that the first rider to get a car will be "1"
			id  = 0;
		}
		
		/** 
		 * Set Rider ID
		 * @param riderID The ID of the rider thread
		 */
		public void setRiderID (int riderID) {
			// Setting the real rider ID here, instead of when thread is created
			// This is really just for convenience, so that the first rider to be reported will be "1"
			id  = riderID;
		}
		
		/** 
		 * Get Rider ID
		 * @return The ID of the rider thread
		 */
		public int getRiderID () {
			return id;
		}
		
		/** 
		 * Run thread
		 */
		public void run () {
			// Declare variables
			int currentCar;
			// Try
			try {
				// Each rider walks, waits, rides, returns "forever"
				while (true) {
		    		// Set rider ID if you don't already have one
					synchronized (idLock) {
			    		if (getRiderID() == 0) {
			    			numStartedRiders++;
			    			setRiderID(Rider.numStartedRiders);
			    		}
						System.out.println("Rider " + id + " is walking around the park.");
					}
					// Walk around for awhile
					Sleeper.walkAround();
					// Get in line to request a bumper car
					currentCar = Coordinator.getInLine();
					System.out.println("Rider " + id + " is now riding in car " + currentCar + ".");
					// Ride around for awhile
					Sleeper.rideTime();
					// Return the car
					Coordinator.carBB.returnCar(currentCar);
					System.out.println("Rider " + id + " returned car " + currentCar + ".");
				}
			} catch (InterruptedException e) {
				// ignore
			}
		}
		
	}
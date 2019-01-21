/**
 * 
 * @author Aurora Tiffany-Davis (attiffan)
 * 
 * 4/6/17
 * CSC 246
 * Homework 5, Q1
 * 
 * Compile command: javac DiningThread.java
 *
 */

public class DiningThread extends Thread{

	private static final int NUM_DINERS = 4;
	private Barrier barrier;
	private String task;
	private int id; 
	
	public DiningThread (Barrier b, String work, int threadid) {
		barrier = b;
		task = work;
		id  = threadid;
	}
	
	public void run () {
		// Work on given task
		System.out.println("I am thread " + id + ", working on Task " + task + ", and waiting to eat.");
		// Wait for all diners to arrive
		barrier.waitForOthers();
		// Start eating
		System.out.println("I am thread " + id + " and I am eating.");
	}
	
	public static void main (String [] args)throws InterruptedException {
	
		// Declare variables
		int i;
		DiningThread [] thr = new DiningThread[NUM_DINERS];
		String taskLetter;
		
		// 4 threads must be synchronized
		Barrier b = new Barrier (NUM_DINERS);
		
		// create DiningThread objects
		for (i = 0; i < NUM_DINERS ; i++) {
			switch (i) {
			case 0:
				taskLetter = "A";
				break;
			case 1:
				taskLetter = "B";
				break;
			case 2:
				taskLetter = "C";
				break;
			case 3:
				taskLetter = "D";
				break;
			default:
				taskLetter = "INVALID";
				break;
			}
			thr[i] = new DiningThread(b, taskLetter, i+1);
		}
		
		// start them
		for (i = 0; i < NUM_DINERS; i++) {
			thr[i].start();
		}
		
		// join them
		for (i = 0; i < NUM_DINERS; i++) {
			thr[i].join();
		}
	
	} 
}
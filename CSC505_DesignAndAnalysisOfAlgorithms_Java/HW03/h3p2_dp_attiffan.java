/**
 * 
 * @author Aurora Tiffany-Davis (attiffan)
 * 
 * 10/30/17
 * CSC 505
 * Homework 3
 * Problem 2 (a)
 * 
 * Problem Description (from "Introduction to Algorithms", 3rd edition, page 371-372):
 * Given a chain <A1, A2, ..., An> of n matrices, 
 * where for i = 1, 2, ..., n, matrix Ai has dimension p(i-1) x p(i), 
 * fully parenthesize the product A1A2...An 
 * in a way that minimizes the number of scalar multiplications.
 * 
 * Do this via dynamic programming, based upon the pseudo-code given in "Introduction to Algorithms", 3rd edition, page 375.
 *
 */

public class h3p2_dp_attiffan {

	// Start recursive call counter at 0 (dynamic programming approach uses no recursion)
	private static long iCounterRecursiveCalls = 0;
	// Establish 2d array to track minimum multiplications (double so that we can use positive infinity)
	private static double[][] adMinMultiplications;
	// Establish 2d array to track solutions (places to split matrix chain from Ai to Aj for lowest # scalar multiplications)
	private static int[][] aiSolutions;
	
	/** 
	 * Parenthesization function
	 * 
	 * @param p		Sequence of dimensions of matrices in matrix chain ("1 3 5 2" means 1x3 matrix, 3x5 matrix, 5x2 matrix)
	 */
	private static void parenthesizeDynamic (int[] p) {
		
		// Declare variables
		int iNumMatrices = p.length - 1;
		int iChainLength;
		int i, j, k, q;
		
		// Establish the diagonal of the min multiplications array as all zeros
		for (i = 0; i < iNumMatrices; i++) {
			adMinMultiplications[i][i] = 0;
		}
		
		// Consider possible matrix chain lengths from 2 to the entire original chain
		for (iChainLength = 2; iChainLength <= iNumMatrices; iChainLength++) {
			// Fill in the min multiplications array above the diagonal with meaningful information
			for (i = 1; i <= iNumMatrices - iChainLength + 1; i++) {
				j = i + iChainLength - 1;
				adMinMultiplications[i-1][j-1] = Double.POSITIVE_INFINITY;
				// Check several possibilities for the split point in the matrix chain, and choose the one with min scalar multiplications
				for (k = i; k <= j-1; k++) {
					q = ((int) adMinMultiplications[i-1][k-1]) + ((int) adMinMultiplications[k][j-1]) + p[i-1]*p[k]*p[j];
					if (q < adMinMultiplications[i-1][j-1]) {
						adMinMultiplications[i-1][j-1] = q;
						aiSolutions[i-1][j-1] = k;
					}
				}
			}
		}
		
	}
	
	/** 
	 * Print string describing optimal parenthesization
	 * Also, update iCounterScalarMultiplications
	 * 
	 * @param sDescriptionIn	String describing optimal parenthesization (under construction)
	 * @param i					First matrix index to consider (1-indexed rather than 0-indexed)
	 * @param j					Last matrix index to consider (1-indexed rather than 0-indexed)
	 */
	private static void printOptimalParenthesization (int i, int j) {
		
		// Base case
		if (i == j) {
			System.out.print("A" + i);
		}
		
		// Recursion
		else {
			System.out.print("(");
			printOptimalParenthesization(i, aiSolutions[i-1][j-1]);
			printOptimalParenthesization(aiSolutions[i-1][j-1] + 1, j);
			System.out.print(")");
		}
		
	}
	
	/** 
	 * Print 2d array holding minimum multiplications (used for debug)
	 */
	private static void printMinMultiplicationArray () {
		
		// Declare variables
		int i, j;
		int iDimension = aiSolutions[0].length;
		
		// Print
		System.out.println("");
		for (i = 0; i < iDimension; i++) {
			for (j = 0; j < iDimension; j++) {
				System.out.print(Integer.toString(aiSolutions[i][j]));
				if (j < iDimension - 1) {
					System.out.print("\t");
				}
			}
			System.out.print("\n");
		}
		System.out.println("");
		
	}

	/** 
	 * Print 2d array holding solution (used for debug)
	 */
	private static void printSolutionArray () {
		
		// Declare variables
		int i, j;
		int iDimension = adMinMultiplications[0].length;
		
		// Print
		System.out.println("");
		for (i = 0; i < iDimension; i++) {
			for (j = 0; j < iDimension; j++) {
				System.out.print(Double.toString(adMinMultiplications[i][j]));
				if (j < iDimension - 1) {
					System.out.print("\t");
				}
			}
			System.out.print("\n");
		}
		System.out.println("");
		
	}
	
	/** 
	 * MAIN function
	 * 
	 * @param args Sequence of dimensions of matrices in matrix chain ("1 3 5 2" means 1x3 matrix, 3x5 matrix, 5x2 matrix)
	 */
	public static void main(String[] args) {
		
		// Check arguments
		if (args.length < 1) {
			System.out.println("usage: h3p2_recursive_attiffan [sequence of dimension]");
		}
		else {
			
			// Declare variables
			int[] aiSequenceOfDimension = new int[args.length];
			int iNumMatrices = args.length - 1;
			int i;
			int iCounterScalarMultiplications;
			long lStartTime_ns;
			long lEndTime_ns;
			long lRunTime_ns;
			String sSequenceOfDimension = "";
			
			// Initialize the 2d arrays we use to track solution
			adMinMultiplications = new double[iNumMatrices][iNumMatrices];
			aiSolutions = new int[iNumMatrices][iNumMatrices];
			
			// Populate array and comma-separated string (used later) for sequence of dimension
			for (i = 0; i < args.length; i++) {
				aiSequenceOfDimension[i] = Integer.parseInt(args[i]);
				sSequenceOfDimension += args[i];
				if (i < args.length - 1) {
					sSequenceOfDimension += ", ";
				}
			}
			
			// Get start time
			lStartTime_ns = System.nanoTime();
			
			// Parenthesize matrix chain
			parenthesizeDynamic(aiSequenceOfDimension);
			
			// Find out how many scalar multiplications are needed
			iCounterScalarMultiplications = (int) adMinMultiplications[0][iNumMatrices-1];
			
			// Get end time and run time
			lEndTime_ns = System.nanoTime();
			lRunTime_ns = lEndTime_ns - lStartTime_ns;
			
			//Used during debug
			//printMinMultiplicationArray();
			//printSolutionArray();
			
			/* Print results (separated by tabs)
			 * Algorithm type
			 * Number of matrices
			 * Comma-separated sequence of dimension
			 * Parenthesization
			 * Run time expressed in nanoseconds
			 * Number of recursive calls
			 * Number of scalar multiplications
			 */
			System.out.print("Dynamic Programming ");
			System.out.print("\t");
			System.out.print(iNumMatrices);
			System.out.print("\t");
			System.out.print(sSequenceOfDimension);
			System.out.print("\t");
			printOptimalParenthesization(1,iNumMatrices);
			System.out.print("\t");
			System.out.print(lRunTime_ns);
			System.out.print("\t");
			System.out.print(iCounterRecursiveCalls);
			System.out.print("\t");
			System.out.print(iCounterScalarMultiplications);
			System.out.print("\n");
			
		}
		
	}
	
}

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
 * Do this via recursion, based upon the pseudo-code given in "Introduction to Algorithms", 3rd edition, page 385.
 *
 */

public class h3p2_recursive_attiffan {
	
	// Start recursive call counter at -1 (first call is not a recursive call)
	private static long iCounterRecursiveCalls = -1;
	// Establish 2d array to track minimum multiplications (double so that we can use positive infinity)
	private static double[][] adMinMultiplications;
	// Establish 2d array to track solutions (places to split matrix chain from Ai to Aj for lowest # scalar multiplications)
	private static int[][] aiSolutions;
	
	/** 
	 * Parenthesization function
	 * 
	 * @param p		Sequence of dimensions of matrices in matrix chain ("1 3 5 2" means 1x3 matrix, 3x5 matrix, 5x2 matrix)
	 * @param i		First matrix index to consider (1-indexed rather than 0-indexed)
	 * @param j		Last matrix index to consider (1-indexed rather than 0-indexed)
	 * @return min	Minimum number of scalar multiplications needed to multiply the matrix chain from Ai to Aj
	 */
	private static int parenthesizeRecursive (int[] p, int i, int j) {
		
		// Declare variables
		int k;
		int q;
		
		// Count how many times we recurse
		iCounterRecursiveCalls++;
		
		// If indices match, then no multiplications are necessary (base case)
		if (i == j) {
			return 0;
		}
		
		// Start by assuming infinity multiplications, work down from there
		adMinMultiplications[i-1][j-1] = Double.POSITIVE_INFINITY;
		
		// Check each place at which we could split up the matrix chain from Ai to Aj
		for (k = i; k < j; k++) {
			q = 
				parenthesizeRecursive(p,i,k) + 
				parenthesizeRecursive(p,k+1,j) + 
				(p[i-1] * p[k] * p[j]);
			if (q < adMinMultiplications[i-1][j-1]) {
				// The minimum number of scalar multiplications needed to perform the multiplication at the split
				adMinMultiplications[i-1][j-1] = q;
				// The proper split point in the matrix chain (1-indexed rather than 0-indexed) if k = 2, split AFTER A2
				aiSolutions[i-1][j-1] = k;
			}
		}
		
		// Return
		return (int)adMinMultiplications[i-1][j-1];
		
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
			parenthesizeRecursive(aiSequenceOfDimension, 1, iNumMatrices);
			
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
			System.out.print("Recursion");
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

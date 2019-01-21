/**
 * 
 * @author Aurora Tiffany-Davis (attiffan)
 * 
 * 11/14/17
 * CSC 505
 * Homework 4
 * Problem 3 (b)
 * 
 * Problem Description:
 * Implement a modification of Dijkstra's SSSP (single source shortest paths) algorithm.
 * Modification runs (asymptotically) as fast as the original algorithm,
 * and assigns a binary value usp[u] to every vertex u in G,
 * so that usp[u]=1 if and only if there is a unique shortest path from s to u. 
 * By definition usp[s]=1. 
 *
 */

public class Dijkstra {
	
	// Globally used variables
	static int nNumVertices;
	static int nNumEdges;
	static int nSourceVertex;
	static int nNumVerticesInPQ;
	static int [][] anEdgeDescriptions;
	static double [][] anAdjacencyMatrix;
	static Vertex [] avVertexPQ;
	static String sAsteriskLine = "*************************************************";
	
	// Vertex class
	static class Vertex {
		
		// Fields
		private int nVertexNumber;
		private double nDistance;
		private int nPredecessor;
		private boolean bUniqueShortestPath;
		private boolean bShortestPathKnown;
		
		// Constructor
		public Vertex (int nNewVertexNumber) {
			// Assign the new vertex the given number
			nVertexNumber = nNewVertexNumber;
			// Vertices start out "infinity" distance from the source vertex
			nDistance = Double.POSITIVE_INFINITY;
			// Vertices start out with null predecessors (here flagged by -1)
			nPredecessor = -1;
			// Vertices start out with the assumption that there is a USP to get to them
			bUniqueShortestPath = true;
			// Vertices start out with shortest path from source not known
			bShortestPathKnown = false;
		}
		
		/** 
		 * Get the vertex number of a vertex
		 * 
		 * @return nVertexNumber	The number identifying the vertex
		 */
		public int getVertexNumber() {
			return nVertexNumber;
		}
		
		/** 
		 * Get the distance attribute of a vertex
		 * 
		 * @return nDistance	The distance from the source vertex to this vertex
		 */
		public double getDistance() {
			return nDistance;
		}
		
		/** 
		 * Set the distance attribute of a vertex
		 * 
		 * @param nDistanceToSet	The distance from the source vertex to this vertex
		 */
		public void setDistance(double nDistanceToSet) {
			nDistance = nDistanceToSet;
		}
		
		/** 
		 * Set the predecessor attribute of a vertex
		 * 
		 * @param nPredecessorToSet		The immediate predecessor of the vertex on a shortest path from the source
		 */
		public void setPredecessor(int nPredecessorToSet) {
			nPredecessor = nPredecessorToSet;
		}
		
		/** 
		 * CGet flag indicating whether there is a unique shortest path from source to the vertex
		 */
		public boolean getUSP() {
			return bUniqueShortestPath;
		}
		
		/** 
		 * Consider the vertex to have more than one shortest path from the source vertex
		 */
		public void setNonUnique() {
			bUniqueShortestPath = false;
		}
		
		/** 
		 * Consider the vertex to have only one shortest path from the source vertex
		 */
		public void setUnique() {
			bUniqueShortestPath = true;
		}
		
		/** 
		 * Get flag indicating whether the shortest path to the vertex is known
		 */
		public boolean getShortestPathKnown() {
			return bShortestPathKnown;
		}
		
		/** 
		 * Flag that the shortest path to the vertex is now known
		 */
		public void setShortestPathKnown() {
			bShortestPathKnown = true;
		}
		
		/** 
		 * Print vertex (used for debug)
		 */
		public void printVertex () {
			System.out.println("");
			System.out.println("\t" + "PRINT VERTEX");
			System.out.println("\t" + "nVertexNumber: " + nVertexNumber);
			System.out.println("\t" + "nDistance: " + nDistance);
			System.out.println("\t" + "nPredecessor: " + nPredecessor);
			System.out.println("\t" + "bUniqueShortestPath: " + bUniqueShortestPath);
			System.out.println("\t" + "bShortestPathKnown: " + bShortestPathKnown);
			System.out.println("");
		}
		
	}
	
	/** 
	 * Determine the single source shortest paths for a given graph and given source
	 * Return shortest path length to each vertex along with a flag indicating if that vertex has a unique shortest path
	 * 
	 * @param n 		number of vertices of G (vertices numbered 1..n)
	 * @param e			number of undirected edges of G
	 * @param mat		an e x 3 matrix that defines the edges of G
	 * @param s			the source vertex of Dijkstra’s algorithm
	 * 
	 * @return sssp		shortest path length to each vertex along with a flag indicating if that vertex has a unique shortest path
	 */
	public static int [][] Dijkstra_alg( int n, int e, int mat[][], int s) {
		
		// Declare variables
		Vertex vMinDistanceVertex;
		int i;
		double nAdjMatrixEntry;
		
		// Save inputs for use by multiple methods
		nNumVertices = n;
		nNumVerticesInPQ = n;
		nNumEdges = e;
		nSourceVertex = s;
		anEdgeDescriptions = mat;
		
		// Build adjacency matrix for the graph
		buildAdjacencyMatrix();
		
		// Initialize every vertex in the graph
		initialize();
		
		// Run Dijkstra's SSSP
		while (nNumVerticesInPQ > 0) {
			// Get the vertex with the smallest estimated distance from the source
			vMinDistanceVertex = extractMinDistanceVertex();
			
			// Relax all edges incident to this vertex
			for (i = 0; i < nNumVertices; i++) {
				nAdjMatrixEntry = anAdjacencyMatrix[vMinDistanceVertex.getVertexNumber()-1][i];
				if (nAdjMatrixEntry > 0 && nAdjMatrixEntry < Double.POSITIVE_INFINITY) {
					relax(vMinDistanceVertex.getVertexNumber(),i+1);
				}
			}
		}
		
		// Build result array
		return buildResults();
        
	}

	/** 
	 * Build an adjacency matrix for the graph
	 * i=j will be zero weight (same vertex)
	 * i!=j where i & j not directly connected will be +inf
	 * i!=j where i & j directly connected will be weight of connecting edge
	 */
	private static void buildAdjacencyMatrix () {
		
		// Declare variables
		int i;
		int j;
		int nSingleEdgeVertexA;
		int nSingleEdgeVertexB;
		int nSingleEdgeWeight;
		
		// Initialize adjacency matrix
		anAdjacencyMatrix = new double [nNumVertices][nNumVertices];
		
		/* Build adjacency matrix for the graph
		 * A vertex to itself has weight zero
		 * Two vertices not connected by an edge have weight infinity
		 * If mat = {{1, 2, 5}} this means there is one edge, connecting vertex 1 and vertex 2, having a weight of 5
		 */
		for (i = 0; i < nNumVertices; i++) {
			for (j = 0; j < nNumVertices; j++) {
				if (i == j) {
					anAdjacencyMatrix[i][j] = 0;
				}
				else {
					anAdjacencyMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (i = 0; i < nNumEdges; i++) {
			nSingleEdgeVertexA = anEdgeDescriptions[i][0];
			nSingleEdgeVertexB = anEdgeDescriptions[i][1];
			nSingleEdgeWeight = anEdgeDescriptions[i][2];
			anAdjacencyMatrix[nSingleEdgeVertexA-1][nSingleEdgeVertexB-1] = nSingleEdgeWeight;
			anAdjacencyMatrix[nSingleEdgeVertexB-1][nSingleEdgeVertexA-1] = nSingleEdgeWeight;
		}

	}
	
	/** 
	 * Initialize the "priority queue" of vertices
	 * Assign the new vertex the given number
	 * Vertices start out "infinity" distance from the source vertex
	 * Vertices start out with null predecessors (here flagged by -1)
	 * Vertices start out with the assumption that there is a USP (unique shortest path) to get to them
	 * Vertices start out with shortest path from source not known
	 */
	private static void initialize () {
		
		// Declare variables
		int i;
		
		// Initialize the array containing all vertex objects
		avVertexPQ = new Vertex[nNumVertices];
		
		// Initialize all vertices in the graph
		for (i = 0; i < nNumVertices; i++) {
			avVertexPQ[i] = new Vertex(i+1);
		}
		
		/* Set the source vertex to have distance-from-source zero (vertices are 1-indexed)
		 * Do not set source vertex to shortest path known quite yet
		 * This way it can be handled just like any other vertex in the algorithm
		 */
		avVertexPQ[nSourceVertex-1].setDistance(0);
		
	}
	
	/** 
	 * Get the vertex with the shortest estimated distance from the source vertex
	 * (among those that don't already have known distances and are thus still part of the conceptual priority queue)
	 * 
	 * @return vMinDistanceVertex	the vertex with the shortest estimated distance from the source vertex
	 */
	private static Vertex extractMinDistanceVertex () {

		// Declare variables
		int i;
		int nIndexOfMinVertex = 0;
		double nMinDistance = Double.POSITIVE_INFINITY;
		
		/* Get the vertex with the minimum distance
		 * Per https://piazza.com/class/j66rlu2rn16352?cid=250,
		 * "The pseudocode of Dijkstra algorithm pp. 658 uses a min- priority queue to extract vertex with minimum shortest path length. 
		 * You are requested not to use any library for this implementation (there are many packages available in python). 
		 * You can use any basic array manipulation (including adjacency matrix) to mimic the operation of a priority queue."
		 * Here I am mimicking the operation of a PQ
		 * simply by getting returning the vertex object from the basic array which has the smallest estimated distance from the source vertex.
		 * As I didn't think that array manipulation was the goal of this homework (but rather understanding Dijkstra),
		 * I have not taken the extra time to get the min distance efficiently.
		 * This runs in linear time in number of vertices.
		 */
		for (i = 0; i < nNumVertices; i++) {
			if (avVertexPQ[i].getDistance() < nMinDistance && avVertexPQ[i].getShortestPathKnown() == false) {
				nMinDistance = avVertexPQ[i].getDistance();
				nIndexOfMinVertex = i;
			}
		}
		
		// Remove from PQ (conceptually) and return
		avVertexPQ[nIndexOfMinVertex].setShortestPathKnown();
		nNumVerticesInPQ--;
		return avVertexPQ[nIndexOfMinVertex];
		
	}
	
	/** 
	 * Relax a single edge by determining if there is a shorter distance from the source to the end of the edge than the one already known
	 * 
	 * @param nU 		number identifying the start vertex of the edge (1-indexed)
	 * @param nV		number identifying the end vertex of the edge (1-indexed)
	 */
	private static void relax (int nU, int nV) {

		// Declare variables
		double nIndirectDistanceToV;
		
		// Check for non-unique shortest path to this vertex
		nIndirectDistanceToV = avVertexPQ[nU-1].getDistance() + anAdjacencyMatrix[nU-1][nV-1];
		if (nIndirectDistanceToV == avVertexPQ[nV-1].getDistance()) {
			avVertexPQ[nV-1].setNonUnique();
		}
		
		// Perform relaxation
		else if (nIndirectDistanceToV < avVertexPQ[nV-1].getDistance()) {
			avVertexPQ[nV-1].setUnique();
			avVertexPQ[nV-1].setDistance(nIndirectDistanceToV);
			avVertexPQ[nV-1].setPredecessor(nU);
		}
		
	}
	
	/** 
	 * Build and return a 2d array describing the results of the modified Dijkstra SSSP
	 * 
	 * @return anResults 	2d array of the following format (example)
	 *    					[
	 *    						[0, 1], // Shortest path weight source to vertex 1 is 0, usp is 1
	 *    						[8, 1], // Shortest path weight source to vertex 2 is 8, usp is 1
	 *    						[6, 1], // Shortest path weight source to vertex 3 is 6, usp is 1
	 *    						[5, 1], // Shortest path weight source to vertex 4 is 5, usp is 1
	 *    						[3, 1] 	// Shortest path weight source to vertex 5 is 3, usp is 1
	 *    					]
	 */
	private static int[][] buildResults () {
		
		// Declare variables
		int i;
		int [][] anResults;
		Vertex vResultVertex;
		
		// Build results
		anResults = new int [nNumVertices][2];
		for (i = 0; i < nNumVertices; i++) {
			vResultVertex = avVertexPQ[i];
			anResults[i][0] = (int) vResultVertex.getDistance();
			anResults[i][1] = vResultVertex.getUSP() == true ? 1 : 0;
		}

		// Return
		return anResults;
		
	}
	
	/** 
	 * Print adjacency matrix (used for debug)
	 */
	public static void printAdjacencyMatrix () {
		int i, j;
		System.out.println("");
		System.out.println(sAsteriskLine);
		System.out.println("PRINT ADJACENCY MATRIX");
		for (i = 0; i < nNumVertices; i++) {
			for (j = 0; j < nNumVertices; j++) {
				System.out.print(anAdjacencyMatrix[i][j]);
				System.out.print("\t");
			}
			System.out.print("\n");
		}
		System.out.println(sAsteriskLine);
		System.out.println("");
	}
	
	/** 
	 * Print all vertices (used for debug)
	 */
	public static void printAllVertices () {
		
		// Declare variables
		int i;
		
		// Print all vertices
		System.out.println("");
		System.out.println(sAsteriskLine);
		System.out.println("PRINT ALL VERTICES");
		for (i = 0; i < avVertexPQ.length; i++) {
			avVertexPQ[i].printVertex();
		}
		System.out.println(sAsteriskLine);
		System.out.println("");
		
	}
	
	/** 
	 * Print results (used for debug)
	 * 
	 * @param anResults		2d array of results
	 */
	public static void printResults (int[][] anResults) {
		
		// Declare variables
		int i;
		
		// Print all vertices
		System.out.println("");
		System.out.println(sAsteriskLine);
		System.out.println("PRINT RESULTS");
		for (i = 0; i < anResults.length; i++) {
			System.out.println("{" + anResults[i][0] + ", " + anResults[i][1] + "}");
		}
		System.out.println(sAsteriskLine);
		System.out.println("");
		
	}
	
}

import java.util.Stack;
import java.util.Queue;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Aurora Tiffany-Davis (attiffan)
 * 
 * 09/01/17
 * CSC 520
 * Homework 1
 * Problem 5
 * 
 * Search Romania
 * 
 * Implements the Depth-First Search and Breadth-First Search algorithms on a representation of a map of Romanian cities.
 * Keeps track of nodes expanded and is able to compute the length of this list.
 * 
 * A few extra notes about this implementation:
 * 	Paths, not nodes, are stored on the data structure. 
 * 	When a path comes off the data structure, its terminal city is marked as visited.
 * 	Visited cities are not considered during path expansion.
 * 	During path expansion, cities are considered in alphabetical order (BFS / Queue) or reverse alphabetical order (DFS / stack).
 *
 */


public class SearchRomania {
	
	// Establish graph as array of cities
	// Simple arrays are used because the search space is completely known beforehand
	// May be more elegant to keep graph as an array of roads / edges
	// But the approach used here makes searching real easy
	// Cities are in the array in alphabetical order
	static City[] aGraph = {
		new City("arad", new String [] {"sibiu", "timisoara", "zerind"}),
		new City("bucharest", new String [] {"fagaras", "giurgiu", "pitesti", "urziceni"}),
		new City("craiova", new String [] {"dobreta", "pitesti", "rimnicu_vilcea"}),
		new City("dobreta", new String [] {"craiova", "mehadia"}),
		new City("eforie", new String [] {"hirsova"}),
		new City("fagaras", new String [] {"bucharest", "sibiu"}),
		new City("giurgiu", new String [] {"bucharest"}),
		new City("hirsova", new String [] {"eforie", "urziceni"}),
		new City("iasi", new String [] {"neamt", "vaslui"}),
		new City("lugoj", new String [] {"mehadia", "timisoara"}),
		new City("mehadia", new String [] {"dobreta", "lugoj"}),
		new City("neamt", new String [] {"iasi"}),
		new City("rimnicu_vilcea", new String [] {"craiova", "pitesti", "sibiu"}),
		new City("oradea", new String [] {"sibiu", "zerind"}),
		new City("pitesti", new String [] {"bucharest", "craiova", "rimnicu_vilcea"}),
		new City("sibiu", new String [] {"arad", "fagaras", "oradea", "rimnicu_vilcea"}),
		new City("timisoara", new String [] {"arad", "lugoj"}),
		new City("urziceni", new String [] {"bucharest", "hirsova", "vaslui"}),
		new City("vaslui", new String [] {"iasi", "urziceni"}),
		new City("zerind", new String [] {"arad", "oradea"})
	};
	
	// Simple city class
	static class City {
		
		// Fields
		private String sCityName;
		private String[] aAdjacentCities;
		private boolean bVisited;
		
		// Constructor
		public City(String sNewCityName, String[] aNewAdjacencyList) {
			sCityName = sNewCityName;
			aAdjacentCities = aNewAdjacencyList;
			bVisited = false;
		}
		
		/** 
		 * Get city name
		 */
		public String getCityName() {
			return sCityName;
		}
		
		/** 
		 * Get adjacent cities
		 */
		public String[] getAdjacentCities() {
			return aAdjacentCities;
		}
		
		/** 
		 * Set visited flag to true
		 */
		public void setVisitedFlag() {
			bVisited = true;
		}
		
		/** 
		 * Get visited flag
		 */
		public boolean getVisitedFlag() {
			return bVisited;
		}
		
	}
	
	/** 
	 * Find the index of a city given by name in the graph
	 * @param sCityName 	The name of a city
	 * @return iCityIndex 	The index of the city within the graph array (-1 if not found)
	 */
	public static int getCityIndex(String sCityName) {
		
		int iCityIndex = -1;
		int i;
		
		for (i = 0; i < aGraph.length; i++) {
			if (aGraph[i].getCityName().equals(sCityName) == true) {
				iCityIndex = i;
				break;
			}
		}
		
		return iCityIndex;

	}
	
	/** 
	 * Mark the terminal city in the given path as visited, and return that terminal city
	 * @param vPath 				The given path
	 * @return cTerminalCity 		The terminal city in the given path
	 */
	public static City markTerminalCityAsVisitedAndReturn(List<City> vPath) {

		// Declare variables
		City cTerminalCity;
		
		// Get and mark
		cTerminalCity = vPath.get(vPath.size()-1);
		cTerminalCity.setVisitedFlag();
		
	    // Return
		return cTerminalCity;
		
	}
	
	/** 
	 * Create a new path, given an existing path and a successor city
	 * @param vPath 				An existing path
	 * @param cSuccessorCity 		A Successor City
	 * @return vNewPath 			A new path with the successor added
	 */
	public static List<City> createNewPath(List<City> vPath, City cSuccessorCity) {
		
		// Declare variables
		List<City> vNewPath;
		
	    // Create new path
		vNewPath = new LinkedList<City>();
		for(City cCityInExistingPath : vPath) {
			vNewPath.add(cCityInExistingPath);
        }
		vNewPath.add(cSuccessorCity);
		
	    // Return
		return vNewPath;
		
	}
	
	/** 
	 * Get a city, given the name of that city
	 * @param sSCityName 		The name of a city
	 * @return cCity 			The city itself
	 * @return vPathUnderConsideration 	A path from the source city to the destination city
	 */
	public static City getCityFromName(String sSCityName) {
		
		// Declare variables
		int iIndexOfCity;
		City cCity;
		
		// Get city from name
		iIndexOfCity = getCityIndex(sSCityName);
    	cCity = aGraph[iIndexOfCity];
		
	    // Return 
		return cCity;

	}
	
	/** 
	 * Perform a depth first search from one city to another
	 * @param sSourceCityName 			The name of the source city
	 * @param sDestinationCityName 		The name of the destination city
	 * @return vPathUnderConsideration 	A path from the source city to the destination city
	 * 									OR in case of failure, return empty path
	 */
	public static List<City> depthFirstSearch(String sSourceCityName, String sDestinationCityName) {
		
		// Declare variables
		Stack<List<City>> vStackOfPathsDFS;
		List<City> vPathUnderConsideration;
		List<City> vNewPath;
		City cTerminalCity;
		City cSourceCity;
		City cSuccessorCandidate;
		String [] aAdjacentCities;
		String sSuccessorCityName;
		int i;
		int nExpandedNodes = 0;
		
		// Initialize a stack of paths with the one-node path consisting of the initial state
		vStackOfPathsDFS = new Stack<List<City>>();
		cSourceCity = aGraph[getCityIndex(sSourceCityName)];
		vStackOfPathsDFS.push(new LinkedList<City>(Arrays.asList(cSourceCity)));
		
		// While (stack not empty)
		while (vStackOfPathsDFS.size() > 0) {
			
			// Pop top path (and mark terminal city as visited)
			vPathUnderConsideration = vStackOfPathsDFS.pop();
			cTerminalCity = markTerminalCityAsVisitedAndReturn(vPathUnderConsideration);
			
			// If last node on path matches goal, return path
			if (cTerminalCity.getCityName().equals(sDestinationCityName) == true) {
				
				// Print total number of expanded nodes
				printTotalExpandedNodes(nExpandedNodes);
				
				// Return
				return vPathUnderConsideration;
			}
			
			// Else extend the path by one node in all possible ways, by generating successors of terminal city
			// (ignore already visited cities)
			else {
				// Path expansion
				aAdjacentCities = cTerminalCity.getAdjacentCities();
				// Stack: put cities on in reverse alphabetical order, so that they come off in alphabetical order
				for (i = aAdjacentCities.length - 1 ; i >= 0; i--) {
					sSuccessorCityName = aAdjacentCities[i];
	            	cSuccessorCandidate = getCityFromName(sSuccessorCityName);
	            	if (cSuccessorCandidate.getVisitedFlag() == false) {
	            		
	        		    // Push successor paths on top of stack
	            		vNewPath = createNewPath(vPathUnderConsideration, cSuccessorCandidate);
	            		vStackOfPathsDFS.push(vNewPath);
	            		
	            		// Print node now and track count for printing that later
	            		System.out.println("Expanded Node: " + sSuccessorCityName);
	            		nExpandedNodes++;
	            		
	            	}
				}
			}
			
		}
		
	    // Return FAIL (empty path)
		return new LinkedList<City>();

	}
	
	/** 
	 * Perform a breadth first search from one city to another
	 * @param sSourceCityName 			The name of the source city
	 * @param sDestinationCityName 		The name of the destination city
	 * @return vPathUnderConsideration 	A path from the source city to the destination city
	 * 									OR in case of failure, return empty path
	 */
	public static List<City> breadthFirstSearch(String sSourceCityName, String sDestinationCityName) {
		
		// Declare variables
		Queue<List<City>> vQueueOfPathsBFS;
		List<City> vPathUnderConsideration;
		List<City> vNewPath;
		City cTerminalCity;
		City cSourceCity;
		City cSuccessorCandidate;
		String [] aAdjacentCities;
		String sSuccessorCityName;
		int i;
		int nExpandedNodes = 0;
		
		// Initialize a queue of paths with the one-node path consisting of the initial state
		vQueueOfPathsBFS = new LinkedList<List<City>>();
		cSourceCity = aGraph[getCityIndex(sSourceCityName)];
		vQueueOfPathsBFS.add(new LinkedList<City>(Arrays.asList(cSourceCity)));
		
		// While (queue not empty)
		while (vQueueOfPathsBFS.size() > 0) {
			
			// Remove path on front of queue (and mark terminal city as visited)
			vPathUnderConsideration = vQueueOfPathsBFS.remove();
			cTerminalCity = markTerminalCityAsVisitedAndReturn(vPathUnderConsideration);
			
			// If last node on path matches goal, return path
			if (cTerminalCity.getCityName().equals(sDestinationCityName) == true) {
				
				// Print total number of expanded nodes
				printTotalExpandedNodes(nExpandedNodes);
				
				// Return
				return vPathUnderConsideration;
			}
			
			// Else extend the path by one node in all possible ways, by generating successors of terminal city
			// (ignore already visited cities)
			else {
				// Path expansion
				aAdjacentCities = cTerminalCity.getAdjacentCities();
				// Queue: put cities on in alphabetical order, so that they come off in alphabetical order
				for (i = 0 ; i < aAdjacentCities.length; i++) {
					sSuccessorCityName = aAdjacentCities[i];
					cSuccessorCandidate = getCityFromName(sSuccessorCityName);
	            	if (cSuccessorCandidate.getVisitedFlag() == false) {
	            		
	        		    // Insert successor paths on rear of queue
	            		vNewPath = createNewPath(vPathUnderConsideration, cSuccessorCandidate);
	            		vQueueOfPathsBFS.add(vNewPath);
	            		
	            		// Print node now and track count for printing that later
	            		System.out.println("Expanded Node: " + sSuccessorCityName);
	            		nExpandedNodes++;
	            		
	            	}
				}
			}
			
		}
		
	    // Return FAIL (empty path)
		return new LinkedList<City>();
		
	}
	
	/** 
	 * Print a path between cities
	 * @param vPath		A path from city to city, to print
	 */
	public static void printPath(List<City> vPath) {
		
		System.out.println("");
		System.out.println("Path (" + (vPath.size()-1) + " hops):");
		System.out.println("");
		for(City cCity : vPath) {
			System.out.println(cCity.getCityName());
        }
		System.out.println("");

	}
	
	/** 
	 * Print total number of expanded nodes
	 * @param nExpandedNodes		Number to print
	 */
	public static void printTotalExpandedNodes(int nExpandedNodes) {
		
		System.out.println("");
		System.out.println("Total number of expanded nodes: " + nExpandedNodes);
		System.out.println("");

	}
	
	/** 
	 * Print program usage reminder
	 */
	public static void printUsageReminder() {
		
		System.out.println("");
		System.out.println("Usage: SearchRomania searchtype srccityname destcityname");
		System.out.println("");

	}

	/** 
	 * MAIN function
	 * 
	 * @param args Arguments	Search type ("DFS" or "BFS"),  source city name,  destination city name 
	 */
	public static void main(String[] args) {
		
		// Declare variables
		String sSearchType;
		String sSourceCityName;
		String sDestinationCityName;
		List<City> cPathFromSourceToDestination;
		
		// Check arguments
		if (args.length != 3) {
			printUsageReminder();
		}
		else {
			
			// Read arguments
			sSearchType = args[0];
			sSourceCityName = args[1];
			sDestinationCityName = args[2];
			
			// Check arguments in more detail
			if (sSearchType.equals("DFS") == false  && sSearchType.equals("BFS") == false) {
				printUsageReminder();
				System.out.println("Search type must be 'DFS' or 'BFS' (search type read in as '" + args[0] + "')");
				System.out.println("");
			}
			else  if (getCityIndex(sSourceCityName) < 0) {
				printUsageReminder();
				System.out.println("'" + sSourceCityName + "' is not in the search space");
				System.out.println("");
			}
			else if (getCityIndex(sDestinationCityName) < 0) {
				printUsageReminder();
				System.out.println("'" + sDestinationCityName + "' is not in the search space");
				System.out.println("");
			}
			else {
				
				// Print blank line for pretty spacing
				System.out.println("");
				
				// At least have an empty path, if all else fails
				cPathFromSourceToDestination = new LinkedList<City>();
				
				// DFS
				if (sSearchType.equals("DFS") == true) {
					cPathFromSourceToDestination = depthFirstSearch(sSourceCityName, sDestinationCityName);
				}
				
				// BFS
				else if (sSearchType.equals("BFS") == true) {
					cPathFromSourceToDestination = breadthFirstSearch(sSourceCityName, sDestinationCityName);
				}
				
				// Print the path we have found, or complain
				if (cPathFromSourceToDestination.isEmpty() == true) {
					System.out.println("");
					System.out.println(
						"Error: No path found from '" + 
						sSourceCityName + 
						"' to '" + 
						sDestinationCityName + 
						"'"
					);
					System.out.println("");
				}
				else {
					printPath(cPathFromSourceToDestination);
				}
				
			}
			
		}
		
	}

}

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;


/**
 * 
 * @author Aurora Tiffany-Davis (attiffan)
 * 
 * 09/23/17
 * CSC 520
 * Homework 2
 * Problem 1
 * 
 * Search USA
 * 
 * Implements three kinds of informed search on a graph representing a US network packet switching map
 * 		A*
 * 		Greedy Best First
 * 		Uniform
 *
 */

public class SearchUSA {
	
	// Globally used variables
	static Graph graphUSA = new Graph(getCitiesUSA(), getRoadsUSA());
	
	static String sSearchType;
	
	static String sDebug;
	
	// CLASSES

	// Graph class
	static class Graph {
		
		// Fields
		private static List<City> vCities;
		private static List<Road> vRoads;
		
		// Constructor
		public Graph (List<City> vNewCities, List<Road> vNewRoads) {
			vCities = vNewCities;
			vRoads = vNewRoads;
		}
		
		/** 
		 * Get a city, given the name of that city
		 * @param sSCityName 		The name of a city
		 * @return cCity 			The city itself
		 */
		public City getCityFromName (String sSCityName) {
			
			// Declare variables
			int iIndexOfCity;
			City cCity;
			
			// Get city from name
			iIndexOfCity = getCityIndex(sSCityName);
			if (iIndexOfCity >= 0) {
				cCity = vCities.get(iIndexOfCity);
			}
			else {
				System.out.println("'" + sSCityName + "' is not in the search space");
				printAllCities();
				System.out.println("");
				cCity = new City("",0,0);
			}
			
		    // Return 
			return cCity;

		}
		
		/** 
		 * Find the index of a city given by name in the graph
		 * @param sCityName 	The name of a city
		 * @return iCityIndex 	The index of the city within the graph array (-1 if not found)
		 */
		public int getCityIndex (String sCityName) {
			
			int iCityIndex = -1;
			int i;
			
			for (i = 0; i < vCities.size(); i++) {
				if (vCities.get(i).getCityName().equals(sCityName) == true) {
					iCityIndex = i;
					break;
				}
			}
			
			return iCityIndex;

		}
		
		/** 
		 * Get roads incident to the given city
		 * @param cCity 			A city in the graph
		 * @return vIncidentRoads 	A list of roads incident to the given city
		 */
		public List<Road> getRoadsIncidentToCity (City cCity) {
			
			// Declare variables
			int i;
			List<Road> vIncidentRoads;
			Road rRoadUnderConsideration;
			
			// Go through all the roads to see if they are incident to the given city
			vIncidentRoads = new LinkedList<Road>();
			for (i = 0; i < vRoads.size(); i++) {
				rRoadUnderConsideration = vRoads.get(i);
				if (rRoadUnderConsideration.sCityName1.equals(cCity.getCityName()) == true) {
					vIncidentRoads.add(rRoadUnderConsideration);
				}
				else if (rRoadUnderConsideration.sCityName2.equals(cCity.getCityName()) == true) {
					vIncidentRoads.add(rRoadUnderConsideration);
				}
			}
			
			// Return
			return vIncidentRoads;

		}
		
		/** 
		 * Get city successors from a given path
		 * Do not include any cities that are currently in the path
		 * @param pPath 				A path (has list of cities and list of roads)
		 * @return vSuccessorCities		A list of successor cities
		 */
		public List<City> getSuccessorCities (Path pPath) {
			
			// Declare variables
			int i;
			City cPossibleSuccessor;
			List<City> vSuccessorCities;
			List<Road> vIncidentRoads;
			Road rRoadUnderConsideration;
			
			// Go through all incident roads getting city successors
			vSuccessorCities = new LinkedList<City>();
			vIncidentRoads = getRoadsIncidentToCity(pPath.getTerminalCity());
			for (i = 0; i < vIncidentRoads.size(); i++) {
				rRoadUnderConsideration = vIncidentRoads.get(i);
				if (rRoadUnderConsideration.sCityName1.equals(pPath.getTerminalCity().getCityName()) == true) {
					cPossibleSuccessor = getCityFromName(rRoadUnderConsideration.sCityName2);
				}
				else {
					cPossibleSuccessor = getCityFromName(rRoadUnderConsideration.sCityName1);
				}
				if (pPath.isCityInPath(cPossibleSuccessor) == false) {
					vSuccessorCities.add(cPossibleSuccessor);
				}
			}
			
			// Return
			return vSuccessorCities;

		}
		
	}
	
	// City class
	static class City {
		
		// Fields
		private String sCityName;
		private double nLatitude;
		private double nLongitude;
		private boolean bExplored;
		
		// Constructor
		public City(String sNewCityName, double nNewLat, double nNewLong) {
			sCityName = sNewCityName;
			nLatitude = nNewLat;
			nLongitude = nNewLong;
			bExplored = false;
		}
		
		/** 
		 * Get city name
		 */
		public String getCityName() {
			return sCityName;
		}
		
		/** 
		 * Get city latitude
		 */
		public double getLat() {
			return nLatitude;
		}
		
		/** 
		 * Get city longitude
		 */
		public double getLong() {
			return nLongitude;
		}
		
		/** 
		 * Set explored flag
		 */
		public void setExplored() {
			bExplored = true;
		}
		
		/** 
		 * Get explored flag
		 */
		public boolean getExplored() {
			return bExplored;
		}
		
		/** 
		 * Print city (used for debug)
		 */
		public void printCity() {
			System.out.println("");
			System.out.println("\t" + "PRINT CITY");
			System.out.println("\t" + "sCityName: " + sCityName);
			System.out.println("\t" + "nLatitude: " + nLatitude);
			System.out.println("\t" + "nLongitude: " + nLongitude);
			System.out.println("");
		}
		
	}
	
	// Road class
	static class Road {
		
		// Fields
		private String sCityName1;
		private String sCityName2;
		private int nDistance;
		
		// Constructor
		public Road (String sNewCityName1, String sNewCityName2, int nNewDistance) {
			sCityName1 = sNewCityName1;
			sCityName2 = sNewCityName2;
			nDistance = nNewDistance;
		}
		
		/** 
		 * Get road city names
		 */
		public String[] getCityNames () {
			return new String [] {sCityName1, sCityName2};
		}
		
		/** 
		 * Get road distance
		 */
		public int getDistance () {
			return nDistance;
		}
		
		/** 
		 * Print road (used for debug)
		 */
		public void printRoad () {
			System.out.println("");
			System.out.println("\t" + "PRINT ROAD");
			System.out.println("\t" + "sCityName1: " + sCityName1);
			System.out.println("\t" + "sCityName2: " + sCityName2);
			System.out.println("\t" + "nDistance: " + nDistance);
			System.out.println("");
		}
		
	}
	
	// Path class
	static class Path {
		
		// Fields
		private List<City> vCitiesInPath;
		private List<Road> vRoadsInPath;
		private int nPriority;
		private City cDestinationCity;
		
		// Constructor (for brand new path)
		public Path (String sSourceCityName, String sDestinationCityName) {
			vCitiesInPath = new LinkedList<City>();
			vCitiesInPath.add(graphUSA.getCityFromName(sSourceCityName));
			vRoadsInPath = new LinkedList<Road>();
			// Destination city is used in priority calculation, so populate that first
			cDestinationCity = graphUSA.getCityFromName(sDestinationCityName);
			nPriority = calculatePriority();
			
		}
		
		// Constructor (for path built from existing path)
		public Path (Path pExistingPath, City cNewCity) {
			
			// Declare variables
			List<Road> vRoadsIncidentToNewCity;
			int i;
			String sNewCityName;
			String sTerminalCityName;
			String sRoadCityName1;
			String sRoadCityName2;
			
			// Copy over existing information (don't simply copy the reference / pointer)
			vCitiesInPath = new LinkedList<City>();
			for (i = 0; i < pExistingPath.vCitiesInPath.size(); i++) {
				vCitiesInPath.add(pExistingPath.vCitiesInPath.get(i));
			}
			vRoadsInPath = new LinkedList<Road>();
			for (i = 0; i < pExistingPath.vRoadsInPath.size(); i++) {
				vRoadsInPath.add(pExistingPath.vRoadsInPath.get(i));
			}
			nPriority = pExistingPath.nPriority;
			cDestinationCity = pExistingPath.cDestinationCity;
			
			// Add new city (update cities)
			vCitiesInPath.add(cNewCity);
			
			// Add new city (update roads)
			vRoadsIncidentToNewCity = graphUSA.getRoadsIncidentToCity(cNewCity);
			for (i = 0; i < vRoadsIncidentToNewCity.size(); i++) {
				sRoadCityName1 = vRoadsIncidentToNewCity.get(i).sCityName1;
				sRoadCityName2 = vRoadsIncidentToNewCity.get(i).sCityName2;
				sNewCityName = cNewCity.getCityName();
				sTerminalCityName = pExistingPath.getTerminalCity().getCityName();
				if (sRoadCityName1.equals(sNewCityName) == true && sRoadCityName2.equals(sTerminalCityName) == true) {
					vRoadsInPath.add(vRoadsIncidentToNewCity.get(i));
					break;
				}
				else if (sRoadCityName1.equals(sTerminalCityName) == true && sRoadCityName2.equals(sNewCityName) == true) {
					vRoadsInPath.add(vRoadsIncidentToNewCity.get(i));
					break;
				}
			}
			
			// Add new city (update priority)
			nPriority = calculatePriority();
			
		}
		
		/** 
		 * Get cities in path
		 * 
		 * @return vCitiesInPath	A list of all the cities in the path
		 */
		public List<City> getCitiesInPath () {
			return vCitiesInPath;
		}
		
		/** 
		 * Determine if the given city is already in the path
		 * 
		 * @param cCity				A city
		 * @return bCityInPath		True if the city is already in the path
		 */
		public boolean isCityInPath (City cCity) {
			
			// Declare variables
			int i;
			boolean bCityInPath;
			
			// Calculate path cost
			bCityInPath = false;
			for (i = 0 ; i < vCitiesInPath.size(); i++) {
				if (vCitiesInPath.get(i).getCityName().equals(cCity.getCityName()) == true) {
					bCityInPath = true;
					break;
				}
			}
			
			// Return
			return bCityInPath;
			
		}
		
		/** 
		 * Get terminal city in path
		 * 
		 * @return cTerminalCity	The terminal city in the path
		 */
		public City getTerminalCity () {
			return vCitiesInPath.get(vCitiesInPath.size()-1);
		}
		
		/** 
		 * Get roads in path
		 */
		public List<Road> getRoadsInPath () {
			return vRoadsInPath;
		}
		
		/** 
		 * Get priority
		 */
		public int getPriority () {
			return nPriority;
		}
		
		/** 
		 * Update priority
		 * 		A*: using SUM OF PATH COST FROM ROOT (g) AND ESTIMATED REMAINING DISTANCE TO GOAL (h-hat) as the priority
		 * 		Greedy: using ESTIMATED REMAINING DISTANCE TO GOAL as the priority
		 * 		Uniform: ordered by PATH-COST
		 * 
		 * @return nPriority;
		 */
		private int calculatePriority () {
			
			if (sSearchType.equals("astar") == true) {
				nPriority = calculatePathCost() + calculateHeuristicCost();
			}
			else if (sSearchType.equals("greedy") == true) {
				nPriority = calculateHeuristicCost();
			}
			else if (sSearchType.equals("uniform") == true) {
				nPriority = calculatePathCost();
			}
			
			return nPriority;
			
		}
		
		/** 
		 * Get path cost (actual distance along the path to the terminal city)
		 * 
		 * @return nPathCost	The total distance of all the roads in the path
		 */
		public int calculatePathCost () {
			
			// Declare variables
			int i;
			int nPathCost;
			
			// Calculate path cost
			nPathCost = 0;
			for (i = 0 ; i < vRoadsInPath.size(); i++) {
				nPathCost += vRoadsInPath.get(i).nDistance;
			}
			
			// Return
			return nPathCost;
			
		}
		
		/** 
		 * Get heuristic cost (estimated distance from the terminal city to the goal)
		 * 
		 * Method is public for debugging purposes
		 * 
		 * From https://people.engr.ncsu.edu/bahler/520/wrap/heuristic.pl:
		 * Value is sqrt((69.5 * (Lat1 - Lat2)) ^ 2 + (69.5 * cos((Lat1 + Lat2)/360 * pi) * (Long1 - Long2)) ^ 2).
		 * 
		 * @return nHeuristicCost	The estimated distance from the terminal city in the path to the goal city
		 */
		public int calculateHeuristicCost () {
	
			// Declare variables
			int nHeuristicCost;
			double nBase1;
			double nBase2;
			double nTerm1;
			double nTerm2;
			City cTerminalCity;
			
			// Calculate Heuristic Cost
			cTerminalCity = getTerminalCity();
			nBase1 = 69.5 * (cTerminalCity.getLat() - cDestinationCity.getLat());
			nTerm1 = Math.pow(nBase1,2);
			nBase2 = 69.5 * Math.cos((cTerminalCity.getLat() + cDestinationCity.getLat())/360 * Math.PI) * (cTerminalCity.getLong() - cDestinationCity.getLong());
			nTerm2 = Math.pow(nBase2,2);
			nHeuristicCost = (int) Math.round(Math.sqrt(nTerm1 + nTerm2));
			
			// Return
			return nHeuristicCost;
			
		}
		
		/** 
		 * Print path (used for debug)
		 */
		public void printPath() {
			
			// Declare variables
			int i;
			
			// Print
			System.out.println("");
			System.out.println("PRINT PATH");
			for (i = 0; i < vCitiesInPath.size(); i++) {
				vCitiesInPath.get(i).printCity();
			}
			for (i = 0; i < vRoadsInPath.size(); i++) {
				vRoadsInPath.get(i).printRoad();
			}
			System.out.println("\t" + "cDestinationCity: " + cDestinationCity.getCityName());
			System.out.println("\t" + "nPriority: " + nPriority);
			System.out.println("");
		}
		
	}
	
	// Heap class
	static class Heap {
		
		// Fields
		private PriorityQueue<Path> pq;
		
		// Constructor
		public Heap (Path pInitialPath) {
			
			// Initialize heap
			pq = new PriorityQueue<Path>(1, new CityComparator());
			pq.add(pInitialPath);
		}
		
		// Methods
		
		/** 
		 * Remove all paths with given terminal city except for the one of minimum cost
		 * @param hHeapOfPaths 			A priority queue of paths
		 * @param cTerminalCity			The terminal city to use when removing redundant paths
		 */
		public void removeRedundantPaths (City cTerminalCity) {
			
			// Declare variables
			Path [] aHeapAsArray;
			Path pDataStructPath;
			double nMinimumCost;
			int i;
			boolean bFoundMinPath;

			// Find minimum cost
			nMinimumCost = Double.POSITIVE_INFINITY;
			aHeapAsArray = pq.toArray(new Path[0]);
			for (i = 0; i < aHeapAsArray.length; i++) {
				pDataStructPath = aHeapAsArray[i];
				if (pDataStructPath.getTerminalCity().getCityName().equals(cTerminalCity.getCityName()) == true) {					
					if (pDataStructPath.nPriority < nMinimumCost) {
						nMinimumCost = pDataStructPath.nPriority;
					}
				}
			}
			
			// Remove all paths with given terminal city except for the one of minimum cost
			bFoundMinPath = false;
			for (i = 0; i < aHeapAsArray.length; i++) {
				pDataStructPath = aHeapAsArray[i];
				if (pDataStructPath.getTerminalCity().getCityName().equals(cTerminalCity.getCityName()) == true) {
					if (pDataStructPath.nPriority == nMinimumCost) {
						if (bFoundMinPath == false) {
							bFoundMinPath = true;
						}
						else {
							pq.remove(pDataStructPath);
						}
					}
					else {
						pq.remove(pDataStructPath);
					}
				}
			}
			
		}
		
		/** 
		 * Print heap (used for debug)
		 */
		public void printHeap () {
			
			// Declare variables
			Path [] aHeapAsArray;
			int i;

			// Print
			System.out.println("");
			System.out.println("PRINT HEAP");
			aHeapAsArray = pq.toArray(new Path[0]);
			for (i = 0; i < aHeapAsArray.length; i++) {
				aHeapAsArray[i].printPath();
			}
			System.out.println("");

		}
		
	}
	
	/* Comparator (for priority queues) class
	 * Used for reheapifying priority queues of paths
	 */
	static class CityComparator implements Comparator<Path>
	{
	    @Override
	    public int compare(Path x, Path y) {
	        // The path with the lowest priority is the best
	        return x.nPriority - y.nPriority;
	    }
	}
	
	// METHODS

	/** 
	 * Return a list of cities in the USA
	 * @return vCitiesUSA	A list of cities in the USA
	 */
	public static List<City> getCitiesUSA() {
		
		List<City> vCitiesUSA = new LinkedList<City>();

		vCitiesUSA.add(new City("albanyGA", 31.58, 84.17));
		vCitiesUSA.add(new City("albanyNY", 42.66, 73.78));
		vCitiesUSA.add(new City("albuquerque", 35.11, 106.61));
		vCitiesUSA.add(new City("atlanta", 33.76, 84.40));
		vCitiesUSA.add(new City("augusta", 33.43, 82.02));
		vCitiesUSA.add(new City("austin", 30.30, 97.75));
		vCitiesUSA.add(new City("bakersfield", 35.36, 119.03));
		vCitiesUSA.add(new City("baltimore", 39.31, 76.62));
		vCitiesUSA.add(new City("batonRouge", 30.46, 91.14));
		vCitiesUSA.add(new City("beaumont", 30.08, 94.13));
		vCitiesUSA.add(new City("boise", 43.61, 116.24));
		vCitiesUSA.add(new City("boston", 42.32, 71.09));
		vCitiesUSA.add(new City("buffalo", 42.90, 78.85));
		vCitiesUSA.add(new City("calgary", 51.00, 114.00));
		vCitiesUSA.add(new City("charlotte", 35.21, 80.83));
		vCitiesUSA.add(new City("chattanooga", 35.05, 85.27));
		vCitiesUSA.add(new City("chicago", 41.84, 87.68));
		vCitiesUSA.add(new City("cincinnati", 39.14, 84.50));
		vCitiesUSA.add(new City("cleveland", 41.48, 81.67));
		vCitiesUSA.add(new City("coloradoSprings", 38.86, 104.79));
		vCitiesUSA.add(new City("columbus", 39.99, 82.99));
		vCitiesUSA.add(new City("dallas", 32.80, 96.79));
		vCitiesUSA.add(new City("dayton", 39.76, 84.20));
		vCitiesUSA.add(new City("daytonaBeach", 29.21, 81.04));
		vCitiesUSA.add(new City("denver", 39.73, 104.97));
		vCitiesUSA.add(new City("desMoines", 41.59, 93.62));
		vCitiesUSA.add(new City("elPaso", 31.79, 106.42));
		vCitiesUSA.add(new City("eugene", 44.06, 123.11));
		vCitiesUSA.add(new City("europe", 48.87, -2.33));
		vCitiesUSA.add(new City("ftWorth", 32.74, 97.33));
		vCitiesUSA.add(new City("fresno", 36.78, 119.79));
		vCitiesUSA.add(new City("grandJunction",39.08, 108.56));
		vCitiesUSA.add(new City("greenBay", 44.51, 88.02));
		vCitiesUSA.add(new City("greensboro", 36.08, 79.82));
		vCitiesUSA.add(new City("houston", 29.76, 95.38));
		vCitiesUSA.add(new City("indianapolis", 39.79, 86.15));
		vCitiesUSA.add(new City("jacksonville", 30.32, 81.66));
		vCitiesUSA.add(new City("japan", 35.68, 220.23));
		vCitiesUSA.add(new City("kansasCity", 39.08, 94.56));
		vCitiesUSA.add(new City("keyWest", 24.56, 81.78));
		vCitiesUSA.add(new City("lafayette", 30.21, 92.03));
		vCitiesUSA.add(new City("lakeCity", 30.19, 82.64));
		vCitiesUSA.add(new City("laredo", 27.52, 99.49));
		vCitiesUSA.add(new City("lasVegas", 36.19, 115.22));
		vCitiesUSA.add(new City("lincoln", 40.81, 96.68));
		vCitiesUSA.add(new City("littleRock", 34.74, 92.33));
		vCitiesUSA.add(new City("losAngeles", 34.03, 118.17));
		vCitiesUSA.add(new City("macon", 32.83, 83.65));
		vCitiesUSA.add(new City("medford", 42.33, 122.86));
		vCitiesUSA.add(new City("memphis", 35.12, 89.97));
		vCitiesUSA.add(new City("mexia", 31.68, 96.48));
		vCitiesUSA.add(new City("mexico", 19.40, 99.12));
		vCitiesUSA.add(new City("miami", 25.79, 80.22));
		vCitiesUSA.add(new City("midland", 43.62, 84.23));
		vCitiesUSA.add(new City("milwaukee", 43.05, 87.96));
		vCitiesUSA.add(new City("minneapolis", 44.96, 93.27));
		vCitiesUSA.add(new City("modesto", 37.66, 120.99));
		vCitiesUSA.add(new City("montreal", 45.50, 73.67));
		vCitiesUSA.add(new City("nashville", 36.15, 86.76));
		vCitiesUSA.add(new City("newHaven", 41.31, 72.92));
		vCitiesUSA.add(new City("newOrleans", 29.97, 90.06));
		vCitiesUSA.add(new City("newYork", 40.70, 73.92));
		vCitiesUSA.add(new City("norfolk", 36.89, 76.26));
		vCitiesUSA.add(new City("oakland", 37.80, 122.23));
		vCitiesUSA.add(new City("oklahomaCity", 35.48, 97.53));
		vCitiesUSA.add(new City("omaha", 41.26, 96.01));
		vCitiesUSA.add(new City("orlando", 28.53, 81.38));
		vCitiesUSA.add(new City("ottawa", 45.42, 75.69));
		vCitiesUSA.add(new City("pensacola", 30.44, 87.21));
		vCitiesUSA.add(new City("philadelphia", 40.72, 76.12));
		vCitiesUSA.add(new City("phoenix", 33.53, 112.08));
		vCitiesUSA.add(new City("pittsburgh", 40.40, 79.84));
		vCitiesUSA.add(new City("pointReyes", 38.07, 122.81));
		vCitiesUSA.add(new City("portland", 45.52, 122.64));
		vCitiesUSA.add(new City("providence", 41.80, 71.36));
		vCitiesUSA.add(new City("provo", 40.24, 111.66));
		vCitiesUSA.add(new City("raleigh", 35.82, 78.64));
		vCitiesUSA.add(new City("redding", 40.58, 122.37));
		vCitiesUSA.add(new City("reno", 39.53, 119.82));
		vCitiesUSA.add(new City("richmond", 37.54, 77.46));
		vCitiesUSA.add(new City("rochester", 43.17, 77.61));
		vCitiesUSA.add(new City("sacramento", 38.56, 121.47));
		vCitiesUSA.add(new City("salem", 44.93, 123.03));
		vCitiesUSA.add(new City("salinas", 36.68, 121.64));
		vCitiesUSA.add(new City("saltLakeCity", 40.75, 111.89));
		vCitiesUSA.add(new City("sanAntonio", 29.45, 98.51));
		vCitiesUSA.add(new City("sanDiego", 32.78, 117.15));
		vCitiesUSA.add(new City("sanFrancisco", 37.76, 122.44));
		vCitiesUSA.add(new City("sanJose", 37.30, 121.87));
		vCitiesUSA.add(new City("sanLuisObispo",35.27, 120.66));
		vCitiesUSA.add(new City("santaFe", 35.67, 105.96));
		vCitiesUSA.add(new City("saultSteMarie",46.49, 84.35));
		vCitiesUSA.add(new City("savannah", 32.05, 81.10));
		vCitiesUSA.add(new City("seattle", 47.63, 122.33));
		vCitiesUSA.add(new City("stLouis", 38.63, 90.24));
		vCitiesUSA.add(new City("stamford", 41.07, 73.54));
		vCitiesUSA.add(new City("stockton", 37.98, 121.30));
		vCitiesUSA.add(new City("tallahassee", 30.45, 84.27));
		vCitiesUSA.add(new City("tampa", 27.97, 82.46));
		vCitiesUSA.add(new City("thunderBay", 48.38, 89.25));
		vCitiesUSA.add(new City("toledo", 41.67, 83.58));
		vCitiesUSA.add(new City("toronto", 43.65, 79.38));
		vCitiesUSA.add(new City("tucson", 32.21, 110.92));
		vCitiesUSA.add(new City("tulsa", 36.13, 95.94));
		vCitiesUSA.add(new City("uk1", 51.30, 0.00));
		/* This city seems redundant
		 *  per https://piazza.com/class/j6npc14ixrk250?cid=47
		 *  "do not discard anything so that all the results become consistent"
		 */
		vCitiesUSA.add(new City("uk2", 51.30, 0.00));
		vCitiesUSA.add(new City("vancouver", 49.25, 123.10));
		vCitiesUSA.add(new City("washington", 38.91, 77.01));
		vCitiesUSA.add(new City("westPalmBeach",26.71, 80.05));
		vCitiesUSA.add(new City("wichita", 37.69, 97.34));
		vCitiesUSA.add(new City("winnipeg", 49.90, 97.13));
		vCitiesUSA.add(new City("yuma", 32.69, 114.62));
		
		return vCitiesUSA;

	}
	
	/* 
	 * Print all cities in the graph (used for debug)
	 */
	public static void printAllCities () {
		
		// Declare variables
		int i;
		List<City> vAllCities;
		
		// Print
		System.out.println("");
		vAllCities = getCitiesUSA();
		for (i = 0; i < vAllCities.size(); i++) {
			System.out.print(vAllCities.get(i).getCityName());
			if (i < vAllCities.size()-1) {
				System.out.print(", ");
			}
		}
		System.out.println("");
		
	}
	
	/** 
	 * Return a list of roads in the USA
	 * @return vRoadsUSA	A list of roads in the USA
	 */
	public static List<Road> getRoadsUSA() {
		
		List<Road> vRoadsUSA = new LinkedList<Road>();

		vRoadsUSA.add(new Road("albanyNY", "montreal", 226));
		vRoadsUSA.add(new Road("albanyNY", "boston", 166));
		vRoadsUSA.add(new Road("albanyNY", "rochester", 148));
		vRoadsUSA.add(new Road("albanyGA", "tallahassee", 120));
		vRoadsUSA.add(new Road("albanyGA", "macon", 106));
		vRoadsUSA.add(new Road("albuquerque", "elPaso", 267));
		vRoadsUSA.add(new Road("albuquerque", "santaFe", 61));
		vRoadsUSA.add(new Road("atlanta", "macon", 82));
		vRoadsUSA.add(new Road("atlanta", "chattanooga", 117));
		vRoadsUSA.add(new Road("augusta", "charlotte", 161));
		vRoadsUSA.add(new Road("augusta", "savannah", 131));
		vRoadsUSA.add(new Road("austin", "houston", 186));
		vRoadsUSA.add(new Road("austin", "sanAntonio", 79));
		vRoadsUSA.add(new Road("bakersfield", "losAngeles", 112));
		vRoadsUSA.add(new Road("bakersfield", "fresno", 107));
		vRoadsUSA.add(new Road("baltimore", "philadelphia", 102));
		vRoadsUSA.add(new Road("baltimore", "washington", 45));
		vRoadsUSA.add(new Road("batonRouge", "lafayette", 50));
		vRoadsUSA.add(new Road("batonRouge", "newOrleans", 80));
		vRoadsUSA.add(new Road("beaumont", "houston", 69));
		vRoadsUSA.add(new Road("beaumont", "lafayette", 122));
		vRoadsUSA.add(new Road("boise", "saltLakeCity", 349));
		vRoadsUSA.add(new Road("boise", "portland", 428));
		vRoadsUSA.add(new Road("boston", "providence", 51));
		vRoadsUSA.add(new Road("buffalo", "toronto", 105));
		vRoadsUSA.add(new Road("buffalo", "rochester", 64));
		vRoadsUSA.add(new Road("buffalo", "cleveland", 191));
		vRoadsUSA.add(new Road("buffalo", "toronto", 105));
		vRoadsUSA.add(new Road("buffalo", "rochester", 164));
		vRoadsUSA.add(new Road("buffalo", "cleveland", 191));
		vRoadsUSA.add(new Road("calgary", "vancouver", 605));
		vRoadsUSA.add(new Road("calgary", "winnipeg", 829));
		vRoadsUSA.add(new Road("charlotte", "greensboro", 91));
		vRoadsUSA.add(new Road("chattanooga", "nashville", 129));
		vRoadsUSA.add(new Road("chicago", "milwaukee", 90));
		vRoadsUSA.add(new Road("chicago", "midland", 279));
		vRoadsUSA.add(new Road("cincinnati", "indianapolis", 110));
		vRoadsUSA.add(new Road("cincinnati", "dayton", 56));
		vRoadsUSA.add(new Road("cleveland", "pittsburgh", 157));
		vRoadsUSA.add(new Road("cleveland", "columbus", 142));
		vRoadsUSA.add(new Road("coloradoSprings", "denver", 70));
		vRoadsUSA.add(new Road("coloradoSprings", "santaFe", 316));
		vRoadsUSA.add(new Road("columbus", "dayton", 72));
		vRoadsUSA.add(new Road("dallas", "denver", 792));
		vRoadsUSA.add(new Road("dallas", "mexia", 83));
		vRoadsUSA.add(new Road("daytonaBeach", "jacksonville", 92));
		vRoadsUSA.add(new Road("daytonaBeach", "orlando", 54));
		vRoadsUSA.add(new Road("denver", "wichita", 523));
		vRoadsUSA.add(new Road("denver", "grandJunction", 246));
		vRoadsUSA.add(new Road("desMoines", "omaha", 135));
		vRoadsUSA.add(new Road("desMoines", "minneapolis", 246));
		vRoadsUSA.add(new Road("elPaso", "sanAntonio", 580));
		vRoadsUSA.add(new Road("elPaso", "tucson", 320));
		vRoadsUSA.add(new Road("eugene", "salem", 63));
		vRoadsUSA.add(new Road("eugene", "medford", 165));
		vRoadsUSA.add(new Road("europe", "philadelphia", 3939));
		vRoadsUSA.add(new Road("ftWorth", "oklahomaCity", 209));
		vRoadsUSA.add(new Road("fresno", "modesto", 109));
		vRoadsUSA.add(new Road("grandJunction", "provo", 220));
		vRoadsUSA.add(new Road("greenBay", "minneapolis", 304));
		vRoadsUSA.add(new Road("greenBay", "milwaukee", 117));
		vRoadsUSA.add(new Road("greensboro", "raleigh", 74));
		vRoadsUSA.add(new Road("houston", "mexia", 165));
		vRoadsUSA.add(new Road("indianapolis", "stLouis", 246));
		vRoadsUSA.add(new Road("jacksonville", "savannah", 140));
		vRoadsUSA.add(new Road("jacksonville", "lakeCity", 113));
		vRoadsUSA.add(new Road("japan", "pointReyes", 5131));
		vRoadsUSA.add(new Road("japan", "sanLuisObispo", 5451));
		vRoadsUSA.add(new Road("kansasCity", "tulsa", 249));
		vRoadsUSA.add(new Road("kansasCity", "stLouis", 256));
		vRoadsUSA.add(new Road("kansasCity", "wichita", 190));
		vRoadsUSA.add(new Road("keyWest", "tampa", 446));
		vRoadsUSA.add(new Road("lakeCity", "tampa", 169));
		vRoadsUSA.add(new Road("lakeCity", "tallahassee", 104));
		vRoadsUSA.add(new Road("laredo", "sanAntonio", 154));
		vRoadsUSA.add(new Road("laredo", "mexico", 741));
		vRoadsUSA.add(new Road("lasVegas", "losAngeles", 275));
		vRoadsUSA.add(new Road("lasVegas", "saltLakeCity", 486));
		vRoadsUSA.add(new Road("lincoln", "wichita", 277));
		vRoadsUSA.add(new Road("lincoln", "omaha", 58));
		vRoadsUSA.add(new Road("littleRock", "memphis", 137));
		vRoadsUSA.add(new Road("littleRock", "tulsa", 276));
		vRoadsUSA.add(new Road("losAngeles", "sanDiego", 124));
		vRoadsUSA.add(new Road("losAngeles", "sanLuisObispo", 182));
		vRoadsUSA.add(new Road("medford", "redding", 150));
		vRoadsUSA.add(new Road("memphis", "nashville", 210));
		vRoadsUSA.add(new Road("miami", "westPalmBeach", 67));
		vRoadsUSA.add(new Road("midland", "toledo", 82));
		vRoadsUSA.add(new Road("minneapolis", "winnipeg", 463));
		vRoadsUSA.add(new Road("modesto", "stockton", 29));
		vRoadsUSA.add(new Road("montreal", "ottawa", 132));
		vRoadsUSA.add(new Road("newHaven", "providence", 110));
		vRoadsUSA.add(new Road("newHaven", "stamford", 92));
		vRoadsUSA.add(new Road("newOrleans", "pensacola", 268));
		vRoadsUSA.add(new Road("newYork", "philadelphia", 101));
		vRoadsUSA.add(new Road("norfolk", "richmond", 92));
		vRoadsUSA.add(new Road("norfolk", "raleigh", 174));
		vRoadsUSA.add(new Road("oakland", "sanFrancisco", 8));
		vRoadsUSA.add(new Road("oakland", "sanJose", 42));
		vRoadsUSA.add(new Road("oklahomaCity", "tulsa", 105));
		vRoadsUSA.add(new Road("orlando", "westPalmBeach", 168));
		vRoadsUSA.add(new Road("orlando", "tampa", 84));
		vRoadsUSA.add(new Road("ottawa", "toronto", 269));
		vRoadsUSA.add(new Road("pensacola", "tallahassee", 120));
		vRoadsUSA.add(new Road("philadelphia", "pittsburgh", 319));
		/* This road is redundant
		 *  per https://piazza.com/class/j6npc14ixrk250?cid=47
		 *  "do not discard anything so that all the results become consistent"
		 */
		vRoadsUSA.add(new Road("philadelphia", "newYork", 101));
		vRoadsUSA.add(new Road("philadelphia", "uk1", 3548));
		/* This road seems redundant
		 *  per https://piazza.com/class/j6npc14ixrk250?cid=47
		 *  "do not discard anything so that all the results become consistent"
		 */
		vRoadsUSA.add(new Road("philadelphia", "uk2", 3548));
		vRoadsUSA.add(new Road("phoenix", "tucson", 117));
		vRoadsUSA.add(new Road("phoenix", "yuma", 178));
		vRoadsUSA.add(new Road("pointReyes", "redding", 215));
		vRoadsUSA.add(new Road("pointReyes", "sacramento", 115));
		vRoadsUSA.add(new Road("portland", "seattle", 174));
		vRoadsUSA.add(new Road("portland", "salem", 47));
		vRoadsUSA.add(new Road("reno", "saltLakeCity", 520));
		vRoadsUSA.add(new Road("reno", "sacramento", 133));
		vRoadsUSA.add(new Road("richmond", "washington", 105));
		vRoadsUSA.add(new Road("sacramento", "sanFrancisco", 95));
		vRoadsUSA.add(new Road("sacramento", "stockton", 51));
		vRoadsUSA.add(new Road("salinas", "sanJose", 31));
		vRoadsUSA.add(new Road("salinas", "sanLuisObispo", 137));
		vRoadsUSA.add(new Road("sanDiego", "yuma", 172));
		vRoadsUSA.add(new Road("saultSteMarie", "thunderBay", 442));
		vRoadsUSA.add(new Road("saultSteMarie", "toronto", 436));
		vRoadsUSA.add(new Road("seattle", "vancouver", 115));
		vRoadsUSA.add(new Road("thunderBay", "winnipeg", 440));
		
		return vRoadsUSA;

	}


	/** 
	 * Perform an A* search from one city to another
	 * Print out search results
	 * 
	 * A* description from web notes: https://people.engr.ncsu.edu/bahler/520/wrap/search3.php:
	 * 
	 * Initialize a priority queue of paths with the one-node path consisting of the initial state
	 * While (queue not empty)
	 * 		Remove path at root (which will be of min cost)
	 * 		If last node on path matches goal, return path
	 * 		Else extend the path by one node in all possible ways, by generating successors of the last node on the path
	 * 		Foreach successor path succ
	 * 			Update path cost (g) to succ
	 * 			Heuristically estimate remaining distance (h-hat) to goal from last node on succ
	 * 			Insert succ on queue and re-heapify using SUM OF PATH COST FROM ROOT (g) AND ESTIMATED REMAINING DISTANCE TO GOAL (h-hat) as the priority
	 * 		If two or more paths reach the same node, delete all paths except the one of min cost
	 * Return FAIL
	 * 
	 * Per https://piazza.com/class/j6npc14ixrk250?cid=65:
	 * Uniform Cost Search is just A*, with a constant heuristic of 0.
	 * 
	 * @param sSourceCityName 			The name of the source city
	 * @param sDestinationCityName 		The name of the destination city
	 */
	public static void searchAStarUniform (String sSourceCityName, String sDestinationCityName) {
		
		// Declare variables
		Heap heap;
		Path pPathToExpand;
		Path pNewPath;
		List<City> vExpandedNodes;
		List<City> vSuccessorCities;
		City cPathToExpandTerminalCity;
		City cSuccessorCity;
		int i;
		
		// Initialize a priority queue of paths with the one-node path consisting of the initial state
		heap = new Heap(new Path(sSourceCityName, sDestinationCityName));

		// Initialize some bookkeeping that'll be needed at the end
		vExpandedNodes = new LinkedList<City>();
		
		// While (queue not empty)
		while (heap.pq.size() > 0) {
			
			// Remove path at root (which will be of min cost)
			pPathToExpand = heap.pq.poll();
			
			// If last node on path matches goal, return path
			cPathToExpandTerminalCity = pPathToExpand.getTerminalCity();
			if (cPathToExpandTerminalCity.sCityName.equals(sDestinationCityName) == true) {
				printSearchResults(vExpandedNodes, pPathToExpand);
				return;
			}
			
			// Else extend the path by one node in all possible ways, by generating successors of the last node on the path
			else {
				
				vSuccessorCities = graphUSA.getSuccessorCities(pPathToExpand);
				
				// A node is said to be expanded when it is taken off a data structure and its successors generated
				vExpandedNodes.add(cPathToExpandTerminalCity);
				
				// For each successor path succ
				for (i = 0; i < vSuccessorCities.size(); i++) {
					
					/* Create the actual successor path (from successor city)
					 * This also updates path cost (g) and remaining distance (h-hat) to goal from last node (if A*)
					 * Or just the path cost (g) (if Uniform)
					 */
					cSuccessorCity = vSuccessorCities.get(i);
					pNewPath = new Path(pPathToExpand, cSuccessorCity);
					
					// Insert succ on queue and re-heapify
					heap.pq.add(pNewPath);
					
					// If two or more paths reach the same node, delete all paths except the one of min cost
					heap.removeRedundantPaths(cSuccessorCity);
					
				}
				
			}
			
		}
		
		// Return FAIL
		printSearchFailure(sSourceCityName, sDestinationCityName);

	}
	
	/** 
	 * Perform a greedy search from one city to another
	 * Print out search results
	 * 
	 * Greedy Search description from web notes: https://people.engr.ncsu.edu/bahler/520/wrap/search3.php
	 * 
	 * Initialize a priority queue of paths with the one-node path consisting of the initial state
	 * While (queue not empty)
	 * 		Remove path at root (which will be of min cost)
	 * 		If last node on path matches goal, return path
	 * 		Else extend the path by one node in all possible ways, by generating successors of the last node on the path
	 * 		Foreach successor path succ
	 * 			Heuristically estimate remaining distance (h-hat) to goal from last node on succ
	 * 			Insert succ on queue and re-heapify using ESTIMATED REMAINING DISTANCE TO GOAL as the priority
	 * Return FAIL
	 * 
	 * @param sSourceCityName 			The name of the source city
	 * @param sDestinationCityName 		The name of the destination city
	 */
	public static void searchGreedy (String sSourceCityName, String sDestinationCityName) {
		
		// Declare variables
		Heap heap;
		Path pPathToExpand;
		Path pNewPath;
		List<City> vExpandedNodes;
		List<City> vSuccessorCities;
		City cPathToExpandTerminalCity;
		City cSuccessorCity;
		int i;
		
		// Initialize a priority queue of paths with the one-node path consisting of the initial state
		heap = new Heap(new Path(sSourceCityName, sDestinationCityName));

		// Initialize some bookkeeping that'll be needed at the end
		vExpandedNodes = new LinkedList<City>();
		
		// While (queue not empty)
		while (heap.pq.size() > 0) {
			
			// Remove path at root (which will be of min cost)
			pPathToExpand = heap.pq.poll();
			
			// If last node on path matches goal, return path
			cPathToExpandTerminalCity = pPathToExpand.getTerminalCity();
			if (cPathToExpandTerminalCity.sCityName.equals(sDestinationCityName) == true) {
				printSearchResults(vExpandedNodes, pPathToExpand);
				return;
			}
			
			// Else extend the path by one node in all possible ways, by generating successors of the last node on the path
			else {
				
				vSuccessorCities = graphUSA.getSuccessorCities(pPathToExpand);
				
				// A node is said to be expanded when it is taken off a data structure and its successors generated
				vExpandedNodes.add(cPathToExpandTerminalCity);
				
				// For each successor path succ
				for (i = 0; i < vSuccessorCities.size(); i++) {
					
					/* Create the actual successor path (from successor city)
					 * This also heuristically estimates remaining distance (h-hat) to goal from last node on succ
					 */
					cSuccessorCity = vSuccessorCities.get(i);
					pNewPath = new Path(pPathToExpand, cSuccessorCity);
					
					// Insert succ on queue and re-heapify
					heap.pq.add(pNewPath);
					
				}
				
			}
			
		}
		
		// Return FAIL
		printSearchFailure(sSourceCityName, sDestinationCityName);

	}

	
	/** 
	 * Print search results
	 * 		A comma separated list of expanded nodes (the closed list);
	 * 		The number of nodes expanded;
	 * 		A comma-separated list of nodes in the solution path;
	 * 		The number of nodes in the path;
	 * 		The total distance from A to B in the solution path.
	 * @param vExpandedNodes	List of expanded city nodes
	 * @param vSolutionPath		A path (contains list of cities and list of roads)
	 * @param sSourceCityName	Starting city name
	 */
	public static void printSearchResults (List<City> vExpandedNodes, Path vSolutionPath) {
		
		// Declare variables
		int i;
		int nNumExpandedNodes;
		int nNumPathNodes;
		int nTotalSolutionPathDistance;
		String sCityName;
		List<City> vCitiesInPath;
		
		if (sDebug == "None") {
			
			// Print empty line for readability
			System.out.println("");
			
			// Print comma separated list of expanded nodes
			System.out.println("Expanded Nodes:");
			System.out.println("");
			nNumExpandedNodes = vExpandedNodes.size();
			for (i = 0 ; i < nNumExpandedNodes; i++) {
				System.out.print(vExpandedNodes.get(i).sCityName);
				if (i < nNumExpandedNodes - 1) {
					System.out.print(", ");
				}
				else {
					System.out.print("\n");
				}
			}
			
			// Print empty line for readability
			System.out.println("");
			
			// Print number of nodes expanded
			System.out.println("# Expanded Nodes: " + nNumExpandedNodes);
			
			// Print empty line for readability
			System.out.println("");
			
			// Print comma-separated list of nodes in the solution path
			System.out.println("Nodes in Solution Path: ");
			System.out.println("");
			vCitiesInPath = vSolutionPath.getCitiesInPath();
			nNumPathNodes = vCitiesInPath.size();
			for (i = 0 ; i < nNumPathNodes; i++) {
				sCityName = vCitiesInPath.get(i).getCityName();
				System.out.print(sCityName);
				if (i < nNumPathNodes - 1) {
					System.out.print(", ");
				}
				else {
					System.out.print("\n");
				}
			}
			
			// Print empty line for readability
			System.out.println("");
			
			// Print number of nodes in the path
			System.out.println("# Nodes in Path: " + nNumPathNodes);
			
			// Print empty line for readability
			System.out.println("");
			
			// Print total distance from A to B in the solution path
			nTotalSolutionPathDistance = vSolutionPath.calculatePathCost();
			System.out.println("Total Distance of Solution Path: " + nTotalSolutionPathDistance);
			
			// Print empty lines for readability
			System.out.println("");
			System.out.println("");
			System.out.println("");
			
		}
		else if (sDebug.equals("numPath") == true) {
			vCitiesInPath = vSolutionPath.getCitiesInPath();
			nNumPathNodes = vCitiesInPath.size();
			System.out.print("\t" + nNumPathNodes);
		}
		else if (sDebug.equals("numExpansion") == true) {
			nNumExpandedNodes = vExpandedNodes.size();
			System.out.print("\t" + nNumExpandedNodes);
		}

	}
	
	
	/** 
	 * Print search failure
	 * 
	 * @param sSourceCityName			The name of the source / start city
	 * @param sDestinationCityName		The name of the destination / goal city
	 */
	public static void printSearchFailure (String sSourceCityName, String sDestinationCityName) {
		
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
	
	
	/** 
	 * Print program usage reminder
	 */
	public static void printUsageReminder () {
		
		System.out.println("");
		System.out.println("Usage: SearchUSA searchtype srccityname destcityname");
		System.out.println("");

	}
	
	// MAIN METHOD

	/** 
	 * MAIN method
	 * 
	 * @param args Arguments	Search type ("astar", "greedy", or "uniform"),  source city name,  destination city name
	 * 							Additional OPTIONAL argument "numPath" or "numExpansion"
	 */
	public static void main (String[] args) {

		// Declare variables
		String sSourceCityName;
		String sDestinationCityName;
		
		// Check arguments
		if (args.length < 3) {
			printUsageReminder();
		}
		else {
			
			// Read arguments
			sSearchType = args[0];
			sSourceCityName = args[1];
			sDestinationCityName = args[2];
			
			// Check arguments in more detail
			if (args.length > 3) {
				sDebug = args[3];
			}
			else {
				sDebug = "None";
			}
			if (sSearchType.equals("astar") == false  && sSearchType.equals("greedy") == false && sSearchType.equals("uniform") == false) {
				printUsageReminder();
				System.out.println("Search type must be 'astar', 'greedy', or 'uniform' (search type read in as '" + args[0] + "')");
				System.out.println("");
			}
			else  if (graphUSA.getCityIndex(sSourceCityName) < 0) {
				printUsageReminder();
				System.out.println("'" + sSourceCityName + "' is not in the search space");
				printAllCities();
				System.out.println("");
			}
			else if (graphUSA.getCityIndex(sDestinationCityName) < 0) {
				printUsageReminder();
				System.out.println("'" + sDestinationCityName + "' is not in the search space");
				printAllCities();
				System.out.println("");
			}
			else {
				
				// A*
				if (sSearchType.equals("astar") == true || sSearchType.equals("uniform")) {
					/* Per https://piazza.com/class/j6npc14ixrk250?cid=65:
					 * Uniform Cost Search is just A*, with a constant heuristic of 0.
					 */
					searchAStarUniform(sSourceCityName, sDestinationCityName);
				}
				
				// Greedy
				else if (sSearchType.equals("greedy") == true) {
					searchGreedy(sSourceCityName, sDestinationCityName);
				}
				
			}
			
		}
		
	}

}
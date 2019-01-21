
/**
 * 
 * CSC 540
 * 
 * Wolf Inns
 * Hotel Management Database System
 * 
 * Team C
 * Abhay Soni                   (asoni3)
 * Aurora Tiffany-Davis         (attiffan)
 * Manjusha Trilochan Awasthi   (mawasth)
 * Samantha Scoggins            (smscoggi)
 * 
 * Compilation:
 * -    add mysql
 * -    javac WolfInns.java
 * 
 * Running:
 * -    java WolfInns
 * 
 * Operation:
 * -    When the application starts, it will populate the DB with the demo data.
 * -    Thereafter, interaction is enabled by means of menus
 *      -   FRONTDESK Menu
 *          -   Perform hotel staff operations such as checking guests in / out, entering service records, etc.
 *      -   REPORTS Menu
 *          -   Perform reporting operations such as revenue, occupancy, and staff reports.
 *          -   Perform extra reporting operations such as reporting on all hotels, all rooms, etc.
 *      -   MANAGE Menu
 *          -   Perform management operations such as adding / updating / deleting hotels, staff, etc.
 * -    Each menu shows each available command, along with a brief description of what the command accomplishes.
 * -    If you wish to close the application, each menu includes a QUIT option.
 * 
 * Organization:
 * 
 * -    "main" function
 *      -    Welcomes the user
 *      -    States available commands
 *      -    Listens to and acts on user commands
 *      -    Closes resources upon "QUIT"
 * 
 * -    "startup_..." functions
 *      -    Perform startup work such as connecting to the DBMS, creating tables
 * 
 * -    "populate_..." functions
 *      -    Populate tables with demo data
 * 
 * -    "user_..." functions
 *      -    Interact with the user to get details about their query / update needs
 * 
 * -    "db_..." functions
 *      -   Interact with the database to run queries and updates
 *      
 * -    "support_..." functions
 *      -   Perform calculations and checks that may be needed by any other functions
 * 
 * -    "error_handler" function
 *      -   Generalized error handler intended to give human-understandable feedback if something goes wrong
 *      
 * -    Large source code file
 *      -   The discerning reader will notice that our application is written in one single file which is > 7k lines long.
 *      -   Our priority during development was on functionality and reliability, such that designing a file hierarchy fell by the wayside.
 *      -   We took to heart the guidance from course instructors that assessment would be based on functionality, 
 *          and that "extra" work would yield no extra points.
 *      -   Certainly if this code were to be supported long-term we would have made a different choice here.
 * 
 * Design:
 * 
 * -    Prepared Statements
 *      -    We used prepared statements for most SQL interactions with the database.
 *      -    Although efficiency is not a requirement of the project, we wanted to explore this topic as a best practice.
 *      -    Our initial intention was to used prepared statements for all such interactions, but time did not allow for that.
 * 
 * -    Constraints
 *      -    We implemented uniqueness, primary key, and foreign key constraints 
 *           as noted in project report 2 and as implied by project assumptions.
 *      -    We did not implement CHECK constraints as these are not fully supported by the version of MariaDB suggested for use in the project.
 *      -    We did not implement ASSERTIONS as these are not fully supported by the version of MariaDB suggested for use in the project.
 *      -    We implemented constraints needed for logical operation on the data, 
 *           which otherwise might have been implemented via CHECK or ASSERTION, through two means:
 *          -   In SQL where practical
 *          -   In application code where SQL was not practical
 *           In some cases we are covered by checks at both the SQL and application levels
 *           This is a result of the development evolution and we are comfortable with extra protection
 * 
 * -    Transactions
 *      -    For safety, our intention was to implement transactions wherever there are multiple DB modification steps within one function.
 *      -    Functions are scoped in a logical way, such that they should represent actions on the database that should be handled atomically.
 * 
 * -    User Friendliness
 *      -    We have made some effort to have a user-friendly interface.
 *      -    There are cases where the application gives the user hints.
 *      -    One example is showing a list of all service types before asking which service type the user wants to change the cost of.
 *      -    Although the team members will be the users during the demo, these hints / helps are intended to help us:
 *          -   Move through code development and debug smoothly
 *          -   Move through the demo smoothly
 *      -    We are certainly not claiming that this application is user-friendly enough for mass market adoption,
 *           it just has a few of the rough edges sanded down.
 * 
 * -    Room Availability
 *      -    As indicated by our design as documented in project report 2, 
 *           we calculate room availability by examining check-in / check-out records.
 *      -    During phase 3 of the project we became aware via 
 *           https://classic.wolfware.ncsu.edu/wrap-bin/mesgboard/csc:540::001:1:2018?task=ST&Forum=13&Topic=7
 *           that "A room can be unavailable for a variety of reasons - for instance, for being renovated".
 *      -    We have not re-designed our system to accommodate a room which is unoccupied nonetheless being considered unavailable.
 * 
 * -    Beyond-demo-data table population
 *      -   Service Types
 *          -   "Catering" is included as a service type although it is not found in the demo data.
 *          -   Catering is a service mentioned in the project narrative.
 *          -   Presidential suites must have dedicated catering staff again as noted in the project narrative.
 *          -   We designed our system with catering in mind as an offered service.
 *      -   Staff
 *          -   Two staff members are included beyond what is found in the demo data in order to 
 *              have dedicated staff available to serve the presidential suite which is found in the demo data.
 *              -   As noted in the project narrative, "At time of check-in, the presidential suite is assigned dedicated ... staff."
 *              -   For this reason, a presidential suite is considered unavailable if there are no staff that we can dedicate to it.
 *              -   Yet, the suite is noted as available in the demo data.
 *              -   The added staff were necessary, given our project design & assumptions, to resolve this conflict.
 *      -   Stays
 *          -   One stay is included beyond what is found in the demo data in order to 
 *              have one room considered unavailable, as is noted in the demo data.
 *      -   Because our changes to the demo data are purely additions, and there are no alterations or deletions,
 *          we are comfortable that we will be best able to explain and defend the behavior of our application
 *          with these changes in place.
 * 
 *  -    Other
 *      -    Other design decisions are best understood by reviewing our team's project report 2, and project assumptions.
 * 
 */

// Imports
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;

// WolfInns class
public class WolfInns {
    
    // DECLARATIONS
    
    // Declare constants - commands
    
    private static final String CMD_MAIN =                                  "MAIN";
    private static final String CMD_QUIT =                                  "QUIT";
    private static final String CMD_FRONTDESK =                             "FRONTDESK";
    private static final String CMD_REPORTS =                               "REPORTS";
    private static final String CMD_MANAGE =                                "MANAGE";
    
    private static final String CMD_FRONTDESK_AVAILABLE =                   "AVAILABILITY";
    private static final String CMD_FRONTDESK_CHECKIN =                     "CHECKIN";
    private static final String CMD_FRONTDESK_CHECKOUT =                    "CHECKOUT";
    private static final String CMD_FRONTDESK_ENTER_SERVICE =               "ENTERSERVICERECORD";
    private static final String CMD_FRONTDESK_UPDATE_SERVICE =              "UPDATESERVICERECORD";
    
    private static final String CMD_REPORT_REVENUE =                        "REVENUE";
    private static final String CMD_REPORT_HOTELS =                         "HOTELS";
    private static final String CMD_REPORT_ROOMS =                          "ROOMS";
    private static final String CMD_REPORT_STAFF =                          "STAFF";
    private static final String CMD_REPORT_CUSTOMERS =                      "CUSTOMERS";
    private static final String CMD_REPORT_STAYS =                          "STAYS";
    private static final String CMD_REPORT_SERVICES =                       "SERVICES";
    private static final String CMD_REPORT_PROVIDED =                       "PROVIDED";
    
    private static final String CMD_REPORT_OCCUPANCY_BY_HOTEL =             "OCCUPANCYBYHOTEL";
    private static final String CMD_REPORT_OCCUPANCY_BY_ROOM_TYPE =         "OCCUPANCYBYROOMTYPE";
    private static final String CMD_REPORT_OCCUPANCY_BY_DATE_RANGE =        "OCCUPANCYBYDATERANGE";
    private static final String CMD_REPORT_OCCUPANCY_BY_CITY =              "OCCUPANCYBYCITY";
    private static final String CMD_REPORT_TOTAL_OCCUPANCY =                "TOTALOCCUPANCY";
    private static final String CMD_REPORT_PERCENTAGE_OF_ROOMS_OCCUPIED =   "PERCENTAGEOFROOMSOCCUPIED";
    
    private static final String CMD_REPORT_STAFF_GROUPED_BY_ROLE =          "STAFFGROUPEDBYROLE";
    private static final String CMD_REPORT_STAFF_SERVING_DURING_STAY =      "STAFFSERVINGDURINGSTAY";
    
    private static final String CMD_MANAGE_HOTEL_ADD =                      "ADDHOTEL";
    private static final String CMD_MANAGE_HOTEL_UPDATE =                   "UPDATEHOTEL";
    private static final String CMD_MANAGE_HOTEL_DELETE =                   "DELETEHOTEL";
    private static final String CMD_MANAGE_STAFF_ADD =                      "ADDSTAFF";
    private static final String CMD_MANAGE_STAFF_UPDATE =                   "UPDATESTAFF";
    private static final String CMD_MANAGE_STAFF_DELETE =                   "DELETESTAFF";
    
    private static final String CMD_MANAGE_ROOM_ADD =                       "ADDROOM";
    private static final String CMD_MANAGE_ROOM_UPDATE =                    "UPDATEROOM";
    private static final String CMD_MANAGE_ROOM_DELETE =                    "DELETEROOM"; 
    
    private static final String CMD_MANAGE_CUSTOMER_ADD =                   "ADDCUSTOMER";
    private static final String CMD_MANAGE_CUSTOMER_UPDATE =                "UPDATECUSTOMER";
    private static final String CMD_MANAGE_CUSTOMER_DELETE =                "DELETECUSTOMER";

    private static final String CMD_MANAGE_SERVICE_COST_UPDATE =            "UPDATESERVICECOST";
    
    // Declare constants - connection parameters
    private static final String JDBC_URL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/smscoggi";
    private static final String JDBC_USER = "smscoggi";
    private static final String JDBC_PASSWORD = "200157888";
    
    // Declare variables - high level
    private static Connection jdbc_connection;
    private static Statement jdbc_statement;
    private static ResultSet jdbc_result;
    private static String currentMenu;
    
    // Declare variables - prepared statements - HOTELS
    private static PreparedStatement jdbcPrep_insertNewHotel;
    private static PreparedStatement jdbcPrep_updateNewHotelManager;
    private static PreparedStatement jdbcPrep_udpateHotelName;
    private static PreparedStatement jdbcPrep_updateHotelStreetAddress;
    private static PreparedStatement jdbcPrep_updateHotelCity;
    private static PreparedStatement jdbcPrep_udpateHotelState;
    private static PreparedStatement jdbcPrep_updateHotelZip;
    private static PreparedStatement jdbcPrep_updateHotelPhoneNum;
    private static PreparedStatement jdbcPrep_updateHotelManagerID;
    private static PreparedStatement jdbcPrep_demoteOldManager;
    private static PreparedStatement jdbcPrep_promoteNewManager;
    private static PreparedStatement jdbcPrep_getNewestHotelID;
    private static PreparedStatement jdbcPrep_getHotelSummaryForAddress;
    private static PreparedStatement jdbcPrep_getHotelSummaryForPhoneNumber;
    private static PreparedStatement jdbcPrep_getHotelSummaryForStaffMember;
    private static PreparedStatement jdbcPrep_getHotelByID;
    private static PreparedStatement jdbcPrep_deleteHotel;  
    
    // Declare variables - prepared statements - ROOMS
    private static PreparedStatement jdbcPrep_insertNewRoom;
    private static PreparedStatement jdbcPrep_roomUpdateCategory;
    private static PreparedStatement jdbcPrep_roomUpdateMaxOccupancy;
    private static PreparedStatement jdbcPrep_roomUpdateNightlyRate;
    private static PreparedStatement jdbcPrep_roomUpdateDRSStaff;
    private static PreparedStatement jdbcPrep_roomUpdateDCStaff;
    private static PreparedStatement jdbcPrep_roomDelete;
    private static PreparedStatement jdbcPrep_isValidRoomNumber; 
    private static PreparedStatement jdbcPrep_isRoomCurrentlyOccupied;
    private static PreparedStatement jdbcPrep_isValidHotelID;
    private static PreparedStatement jdbcPrep_getRoomByHotelIDRoomNum;
    private static PreparedStatement jdbcPrep_getOccupiedRoomsInHotel; 
    private static PreparedStatement jdbcPrep_getOneExampleRoom;
    private static PreparedStatement jdbcPrep_assignDedicatedStaff;
    private static PreparedStatement jdbcPrep_releaseDedicatedStaff;    
    
    // Declare variables - prepared statements - STAFF
    private static PreparedStatement jdbcPrep_insertNewStaff;
    private static PreparedStatement jdbcPrep_getNewestStaffID;
    private static PreparedStatement jdbcPrep_updateStaffName;
    private static PreparedStatement jdbcPrep_updateStaffDOB;
    private static PreparedStatement jdbcPrep_updateStaffJobTitle;
    private static PreparedStatement jdbcPrep_updateStaffDepartment;
    private static PreparedStatement jdbcPrep_updateStaffPhoneNum;
    private static PreparedStatement jdbcPrep_updateStaffAddress;
    private static PreparedStatement jdbcPrep_updateStaffHotelID;
    private static PreparedStatement jdbcPrep_getStaffByID;
    private static PreparedStatement jdbcPrep_deleteStaff;
    private static PreparedStatement jdbcPrep_getFirstAvailableCateringStaff;
    private static PreparedStatement jdbcPrep_getFirstAvailableRoomServiceStaff;
    private static PreparedStatement jdbcPrep_getDedicatedStaffMembers;
    private static PreparedStatement jdbcPrep_getJobTitlebyID;
    // Declare variables - prepared statements - CUSTOMERS
    private static PreparedStatement jdbcPrep_insertNewCustomer;
    private static PreparedStatement jdbcPrep_customerUpdateSSN;
    private static PreparedStatement jdbcPrep_customerUpdateName;
    private static PreparedStatement jdbcPrep_customerUpdateDateOfBirth;
    private static PreparedStatement jdbcPrep_customerUpdatePhoneNumber;
    private static PreparedStatement jdbcPrep_customerUpdateEmail;
    private static PreparedStatement jdbcPrep_customerDelete; 
    private static PreparedStatement jdbcPrep_getCustomerByID; 
    private static PreparedStatement jdbcPrep_isValidCustomer; 
    private static PreparedStatement jdbcPrep_isCustomerCurrentlyStaying;
    
    // Declare variables - prepared statements - STAYS
    private static PreparedStatement jdbcPrep_assignRoom;
    private static PreparedStatement jdbcPrep_getNewestStay;
    private static PreparedStatement jdbcPrep_addStayNoSafetyChecks;
    private static PreparedStatement jdbcPrep_getSummaryOfStay;
    private static PreparedStatement jdbcPrep_getStayByRoomAndHotel;
    private static PreparedStatement jdbcPrep_getItemizedReceipt;
    private static PreparedStatement jdbcPrep_updateAmountOwed;
    private static PreparedStatement jdbcPrep_getStayIdForOccupiedRoom;
    private static PreparedStatement jdbcPrep_updateCheckOutTimeAndEndDate; 
    private static PreparedStatement jdbcPrep_isValidStayID;
    
    // Declare variables - prepared statements - SERVICES
    private static PreparedStatement jdbcPrep_getEligibleStaffForService;
    private static PreparedStatement jdbcPrep_insertNewServiceType;
    private static PreparedStatement jdbcPrep_insertNewServiceRecord;
    private static PreparedStatement jdbcPrep_udpateServiceRecord;
    private static PreparedStatement jdbcPrep_getNewestServiceRecord;
    private static PreparedStatement jdbcPrep_getServiceNameAndStaffByID;
    private static PreparedStatement jdbcPrep_getValidServiceNames;
    private static PreparedStatement jdbcPrep_getServiceRecordByID;
    private static PreparedStatement jdbcPrep_updateServiceCost;

    // Declare variables - prepared statements - TABLES
    private static PreparedStatement jdbcPrep_reportTableRooms;
    private static PreparedStatement jdbcPrep_reportTableStaff;
    private static PreparedStatement jdbcPrep_reportTableStays;
    
    // Declare variables - prepared statements - REPORTS
    private static PreparedStatement jdbcPrep_reportOccupancyByHotel;
    private static PreparedStatement jdbcPrep_reportOccupancyByRoomType;
    private static PreparedStatement jdbcPrep_reportOccupancyByDateRange;
    private static PreparedStatement jdbcPrep_reportOccupancyByCity;
    private static PreparedStatement jdbcPrep_reportTotalOccupancy;
    private static PreparedStatement jdbcPrep_reportPercentageOfRoomsOccupied;
    private static PreparedStatement jdbcPrep_reportStaffGroupedByRole;
    private static PreparedStatement jdbcPrep_reportStaffServingDuringStay;
    private static PreparedStatement jdbcPrep_reportHotelRevenueByDateRange;
    
    /* Why is the scanner outside of any method?
     * See https://stackoverflow.com/questions/13042008/java-util-nosuchelementexception-scanner-reading-user-input
     */
    private static Scanner scanner;

    // STARTUP METHODS
    
    /** 
     * Print available commands
     * 
     * Arguments -  menu -  The menu we are currently in (determines available commands).
     *                      For example main, reports, etc.
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Add ability to print entire Provided table.
     *                  03/09/18 -  ATTD -  Add ability to delete a hotel.
     *                  03/11/18 -  ATTD -  Add ability to report revenue.
     *                  03/11/18 -  ATTD -  Add ability to generate bill for customer stay.
     *                  03/12/18 -  ATTD -  Add ability to delete a staff member.
     *                  03/23/18 -  ATTD -  Add ability to update basic information about a hotel.
											Use new general error handler. 
     *                  03/24/18 -  MTA  -  Added ability to support Manage task for Room i.e add, update and delete room 
     *                  03/24/18 -  ATTD -  Add ability to insert new staff member.
     *                  03/26/18 -  ATTD -  Add ability to update basic info about a staff member.
     *                  03/27/18 -  MTA  -  Add ability to add, update and delete customer.
     *                  03/27/18 -  ATTD -  Add ability to check room availability.
     *                  04/01/18 -  ATTD -  Add ability to assign a room to a customer.
     *                  04/05/18 -  ATTD -  Create streamlined checkout (generate receipt & bill, release room).
     *                  04/06/18 -  ATTD -  Add ability to enter a new service record.
     *                  04/06/18 -  ATTD -  Add ability to update a service record.
     *                  04/07/18 -  AS   -  Add ability to update cost of service.
     *                  04/09/18 -  ATTD -  Added 'Quit'functionality to all submenus
     */
    public static void startup_printAvailableCommands(String menu) {
        
        try {
            
            System.out.println("");
            System.out.println(menu + " Menu available commands:");
            System.out.println("");
            
            switch (menu) {
                case CMD_MAIN:
                    System.out.println("'" + CMD_FRONTDESK + "'");
                    System.out.println("\t- perform front-desk tasks");
                    System.out.println("'" + CMD_REPORTS + "'");
                    System.out.println("\t- run reports");
                    System.out.println("'" + CMD_MANAGE + "'");
                    System.out.println("\t- manage the hotel chain (add hotels, etc)");
                    System.out.println("'" + CMD_QUIT + "'");
                    System.out.println("\t- exit the program");
                    System.out.println("");
                    break;
                case CMD_FRONTDESK:
                    System.out.println("'" + CMD_FRONTDESK_CHECKOUT + "'");
                    System.out.println("\t- check customer out (generate receipt & bill, release room)");
                    System.out.println("'" + CMD_FRONTDESK_AVAILABLE + "'");
                    System.out.println("\t- check room availability");
                    System.out.println("'" + CMD_FRONTDESK_CHECKIN + "'");
                    System.out.println("\t- check a customer into a room");
                    System.out.println("'" + CMD_FRONTDESK_ENTER_SERVICE + "'");
                    System.out.println("\t- enter a service record");
                    System.out.println("'" + CMD_FRONTDESK_UPDATE_SERVICE + "'");
                    System.out.println("\t- update a service record");
                    System.out.println("'" + CMD_MAIN + "'");
                    System.out.println("\t- go back to the main menu");
                    System.out.println("'" + CMD_QUIT + "'");
                    System.out.println("\t- exit the program");
                    System.out.println("");
                    break;
                case CMD_REPORTS:
                    System.out.println("'" + CMD_REPORT_REVENUE + "'");
                    System.out.println("\t- run report on a hotel's revenue during a given date range");
                    System.out.println("'" + CMD_REPORT_HOTELS + "'");
                    System.out.println("\t- run report on hotels");
                    System.out.println("'" + CMD_REPORT_ROOMS + "'");
                    System.out.println("\t- run report on rooms");
                    System.out.println("'" + CMD_REPORT_STAFF + "'");
                    System.out.println("\t- run report on staff");
                    System.out.println("'" + CMD_REPORT_CUSTOMERS + "'");
                    System.out.println("\t- run report on customers");
                    System.out.println("'" + CMD_REPORT_STAYS + "'");
                    System.out.println("\t- run report on stays");
                    System.out.println("'" + CMD_REPORT_SERVICES + "'");
                    System.out.println("\t- run report on service types");
                    System.out.println("'" + CMD_REPORT_PROVIDED + "'");
                    System.out.println("\t- run report on services provided to guests");
                    System.out.println("'" + CMD_REPORT_OCCUPANCY_BY_HOTEL + "'");
                    System.out.println("\t- run report on occupancy by hotel"); 
                    System.out.println("'" + CMD_REPORT_OCCUPANCY_BY_ROOM_TYPE + "'");
                    System.out.println("\t- run report on occupancy by room type");  
                    System.out.println("'" + CMD_REPORT_OCCUPANCY_BY_DATE_RANGE + "'");
                    System.out.println("\t- run report on occupancy by date range"); 
                    System.out.println("'" + CMD_REPORT_OCCUPANCY_BY_CITY + "'");
                    System.out.println("\t- run report on occupancy by city");  
                    System.out.println("'" + CMD_REPORT_TOTAL_OCCUPANCY + "'");
                    System.out.println("\t- run report on total occupancy");  
                    System.out.println("'" + CMD_REPORT_PERCENTAGE_OF_ROOMS_OCCUPIED + "'");
                    System.out.println("\t- run report on percentage of rooms occupied");                     
                    System.out.println("'" + CMD_REPORT_STAFF_GROUPED_BY_ROLE + "'");
                    System.out.println("\t- run report on staff grouped by role");
                    System.out.println("'" + CMD_REPORT_STAFF_SERVING_DURING_STAY + "'");
                    System.out.println("\t- run report on staff serving the customer during the stay");                    
                    System.out.println("'" + CMD_MAIN + "'");
                    System.out.println("\t- go back to the main menu");
                    System.out.println("'" + CMD_QUIT + "'");
                    System.out.println("\t- exit the program");
                    System.out.println("");
                    break;
                case CMD_MANAGE:
                    System.out.println("'" + CMD_MANAGE_HOTEL_ADD + "'");
                    System.out.println("\t- add a hotel");
                    System.out.println("'" + CMD_MANAGE_HOTEL_UPDATE + "'");
                    System.out.println("\t- update information about a hotel");
                    System.out.println("'" + CMD_MANAGE_HOTEL_DELETE + "'");
                    System.out.println("\t- delete a hotel");
                    System.out.println("'" + CMD_MANAGE_STAFF_ADD + "'");
                    System.out.println("\t- add a staff member");
                    System.out.println("'" + CMD_MANAGE_STAFF_UPDATE + "'");
                    System.out.println("\t- update information about a staff member");
                    System.out.println("'" + CMD_MANAGE_STAFF_DELETE + "'");
                    System.out.println("\t- delete a staff member");
                    
                    System.out.println("'" + CMD_MANAGE_ROOM_ADD + "'");
                    System.out.println("\t- add a room");
                    System.out.println("'" + CMD_MANAGE_ROOM_UPDATE + "'");
                    System.out.println("\t- update details of the room");
                    System.out.println("'" + CMD_MANAGE_ROOM_DELETE + "'");
                    System.out.println("\t- delete a room");
                    
                    System.out.println("'" + CMD_MANAGE_CUSTOMER_ADD + "'");
                    System.out.println("\t- add a customer");
                    System.out.println("'" + CMD_MANAGE_CUSTOMER_UPDATE + "'");
                    System.out.println("\t- update details of the customer");
                    System.out.println("'" + CMD_MANAGE_CUSTOMER_DELETE + "'");
                    System.out.println("\t- delete a customer");
                    
                    System.out.println("'" + CMD_MANAGE_SERVICE_COST_UPDATE + "'");
                    System.out.println("\t- update cost of a service");
                    
                    System.out.println("'" + CMD_MAIN + "'");
                    System.out.println("\t- go back to the main menu");
                    System.out.println("'" + CMD_QUIT + "'");
                    System.out.println("\t- exit the program");
                    System.out.println("");
                    break;
                default:
                    break;
            }
            

        
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Establish a connection to the database
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     */
    public static void startup_connectToDatabase() {
        
        try {
            
            // Get JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");
            
            // Initialize JDBC stuff to null
            jdbc_connection = null;
            jdbc_statement = null;
            jdbc_result = null;
            
            // Establish connection
            jdbc_connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            jdbc_statement = jdbc_connection.createStatement();
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Create prepared statements
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/20/18 -  ATTD -  Created method.
     *                  03/21/18 -  ATTD -  Fix off-by-one error in updating new hotel manager.
     *                                      Add more prepared statements to use when inserting new hotel.
     *                  03/23/18 -  ATTD -  Add support for updating basic information about a hotel.
     *                                      Use new general error handler.
     *                  03/24/18 -  ATTD -  Add support for deleting a hotel.
     *                  03/24/18 -  ATTD -  Add support for adding a new staff member.
     *                  03/26/18 -  ATTD -  Add ability to update basic info about a staff member.
     *                  03/27/18 -  ATTD -  Add prepared statement for updating staff hotel ID by staff ID range.
     *                                      Use prepared statement to delete staff.
     *                                      Add ability to report one example room.
     *                  04/01/18 -  ATTD -  Add ability to assign a room to a customer.
     *                  04/02/18 -  ATTD -  Do not assign room if number of guests exceeds maximum occupancy.
     *                  04/04/18 -  ATTD -  Add prepared statement for mass population of Stays table.
     *                  04/04/18 -  ATTD -  Add attribute for hotel zip code, per demo data.
     *                                      Do not allow insertion of new staff member, if that staff member
     *                                          is supposed to be the manager of a hotel which ALREADY has a manager.
     *                                      Make customer ID the primary key, and SSN just another attribute, per demo data.
     *                                      Change "front desk representative" job title to "front desk staff", to match demo data.
     *                                      Remove prepared statement to update hotel ID over a range of staff IDs,
     *                                          no longer makes sense with tables populated with demo data.
     *                  04/05/18 -  ATTD -  Add prepared statements related to customer billing (itemized receipt, bill).
     *                                      Add prepared statements for table reporting.
     *                  04/06/18 -  ATTD -  Add ability to enter a new service record.
     *                  04/06/18 -  ATTD -  Add ability to update a service record.
     *                  04/07/18 -  ATTD -  Debug ability to update a service record.
     *                  04/07/18 -  AS   -  Add ability to update cost of a service.
     *                  04/08/18 -  ATTD -  Fix bug keeping dedicated staff from being assigned to presidential suite.
     *                  04/10/18 -  ATTD -  When reporting room availability, take into account for the presidential suite
     *                                      the need to have staff available to dedicate to the suite.
     *                  04/11/18 -  ATTD -  Use prepared statement to populate service types table.
     */
    public static void startup_createPreparedStatements() {
        
        try {
            
            // Declare variables
            String reusedSQLVar;

            /* Insert new hotel
             * Indices to use when calling this prepared statement:
             * 1 - name
             * 2 - street address
             * 3 - city
             * 4 - state
             * 5 - zip code
             * 6 - phone number
             * 7 - manager ID
             * 8 - manager ID (again)
             */
            reusedSQLVar = 
                "INSERT INTO Hotels (Name, StreetAddress, City, State, Zip, PhoneNum, ManagerID) " + 
                "SELECT ?, ?, ?, ?, ?, ?, ? " + 
                "FROM Staff " + 
                "WHERE " + 
                "Staff.ID = ? AND " + 
                "Staff.ID NOT IN (SELECT DCStaff FROM Rooms WHERE DCStaff IS NOT NULL) AND " + 
                "Staff.ID NOT IN (SELECT DRSStaff FROM Rooms WHERE DRSStaff IS NOT NULL);";
            jdbcPrep_insertNewHotel = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update new hotel manager
             * to give new manager correct job title and hotel assignment
             * intended to be called in same transaction as insertion of new hotel
             * therefore the insertion is not yet committed
             * therefore max ID needs incremented
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET JobTitle = 'Manager', HotelID = (SELECT MAX(ID) FROM Hotels) " + 
                "WHERE " + 
                "ID = (SELECT ManagerID FROM Hotels WHERE ID = (SELECT MAX(ID) FROM Hotels));";
            jdbcPrep_updateNewHotelManager = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update hotel name
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel name
             * 2 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Hotels " + 
                "SET Name = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_udpateHotelName = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Update hotel street address
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel street address
             * 2 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Hotels " + 
                "SET StreetAddress = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateHotelStreetAddress = jdbc_connection.prepareStatement(reusedSQLVar); 

            /* Update hotel city
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel city
             * 2 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Hotels " + 
                "SET City = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateHotelCity = jdbc_connection.prepareStatement(reusedSQLVar); 

            /* Update hotel state
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel state
             * 2 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Hotels " + 
                "SET State = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_udpateHotelState = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update hotel zip code
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel zip code
             * 2 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Hotels " + 
                "SET Zip = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateHotelZip = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update hotel phone number
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel phone number
             * 2 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Hotels " + 
                "SET PhoneNum = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateHotelPhoneNum = jdbc_connection.prepareStatement(reusedSQLVar); 
            
            /* Update hotel managerID
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel manager ID
             * 2 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Hotels " + 
                "SET ManagerID = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateHotelManagerID = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get one example room, just to show the user what the attributes to filter on are
             * Don't filter on DCStaff or DRSStaff, that doesn't really make sense for the front desk rep
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = 
                "SELECT RoomNum, HotelID, Category, MaxOcc, NightlyRate from Rooms LIMIT 1;";
            jdbcPrep_getOneExampleRoom = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Demote the old manager of a given hotel to front desk staff
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET JobTitle = 'Front Desk Staff' " + 
                "WHERE JobTitle = 'Manager' AND HotelID = ?;";
            jdbcPrep_demoteOldManager = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Promote a staff member to management of a hotel
             * Indices to use when calling this prepared statement: 
             * 1 -  hotel ID
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET JobTitle = 'Manager', HotelID = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_promoteNewManager = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Get the ID of the newest hotel in the DB
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = "SELECT MAX(ID) FROM Hotels;";
            jdbcPrep_getNewestHotelID = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get a summary of info about the hotel which has a particular street address, city, state
             * Indices to use when calling this prepared statement:
             * 1 -  street address
             * 2 -  city
             * 3 -  state
             */
            reusedSQLVar = 
                "SELECT " + 
                "StreetAddress, City, State, Zip, ID AS HotelID, Name AS HotelName " + 
                "FROM Hotels WHERE StreetAddress = ? AND City = ? AND State = ?;";
            jdbcPrep_getHotelSummaryForAddress = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get a summary of info about the hotel which has a particular phone number
             * Indices to use when calling this prepared statement:
             * 1 -  phone number
             */
            reusedSQLVar = 
                "SELECT " + 
                "PhoneNum AS PhoneNumber, ID AS HotelID, Name AS HotelName " + 
                "FROM Hotels WHERE PhoneNum = ?;";
            jdbcPrep_getHotelSummaryForPhoneNumber = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get a summary of info about the hotel to which a staff member is assigned
             * Indices to use when calling this prepared statement:
             * 1 -  staff ID
             */
            reusedSQLVar = 
                "SELECT " + 
                "Staff.ID AS StaffID, Staff.Name AS StaffName, Hotels.ID AS HotelID, Hotels.Name AS HotelName " + 
                "FROM Staff, Hotels WHERE Staff.ID = ?" + 
                " AND Staff.HotelID = Hotels.ID;";
            jdbcPrep_getHotelSummaryForStaffMember = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get all values of a given tuple (by ID) from the Hotels table
             * Indices to use when calling this prepared statement:
             * 1 -  ID
             */
            reusedSQLVar = 
                "SELECT * FROM Hotels WHERE ID = ?;";
            jdbcPrep_getHotelByID = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Delete a hotel (by ID)
             * Any hotel, room, customer, staff member, or service type associated with a current guest stay may not be deleted
             * Indices to use when calling this prepared statement:
             * 1 -  ID
             */
            reusedSQLVar = 
                "DELETE FROM Hotels WHERE ID = ? AND ID NOT IN " + 
                "(SELECT HotelID FROM Stays WHERE CheckOutTime IS NULL OR EndDate IS NULL);";
            jdbcPrep_deleteHotel = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Insert new staff member
             * Do not allow insertion of new staff member, if that staff member
             * is supposed to be the manager of a hotel which ALREADY has a manager
             * The way we've structured the hotel-manager two-way link,
             * we assign the manager on the hotel side before we assign the hotel on the manager side
             * So this means it is NEVER appropriate to insert a manager and give them an assigned hotel in the same SQL statement
             * Handle this restriction at the application level rather than at the SQL level (just easier that way)
             * Indices to use when calling this prepared statement:
             * 1 - name
             * 2 - date of birth
             * 3 - job title
             * 4 - department
             * 5 - phone number
             * 6 - address
             * 7 - hotel ID
             */
            reusedSQLVar = 
                "INSERT INTO Staff (Name, DOB, JobTitle, Dep, PhoneNum, Address, HotelID) " + 
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
            jdbcPrep_insertNewStaff = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get the ID of the newest staff member in the DB
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = "SELECT MAX(ID) FROM Staff;";
            jdbcPrep_getNewestStaffID = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Update staff member name
             * Indices to use when calling this prepared statement: 
             * 1 -  staff name
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET Name = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateStaffName = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update staff member date of birth
             * Indices to use when calling this prepared statement: 
             * 1 -  staff date of birth
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET DOB = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateStaffDOB = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update staff member job title
             * Any staff member currently dedicated to serving a presidential suite may not have their job title changed
             * Since we set DCStaff and DRSStaff to NULL when a room is released, we needn't look at the Stays table
             * Indices to use when calling this prepared statement: 
             * 1 -  staff job title
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET JobTitle = ? " + 
                "WHERE ID = ? AND " + 
                "ID NOT IN (SELECT DCStaff FROM Rooms WHERE DCStaff IS NOT NULL) AND " + 
                "ID NOT IN (SELECT DRSStaff FROM Rooms WHERE DRSStaff IS NOT NULL);";
            jdbcPrep_updateStaffJobTitle = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update staff member department
             * Indices to use when calling this prepared statement: 
             * 1 -  staff department
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET Dep = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateStaffDepartment = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update staff member phone number
             * Indices to use when calling this prepared statement: 
             * 1 -  staff phone number
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET PhoneNum = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateStaffPhoneNum = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update staff member address
             * Indices to use when calling this prepared statement: 
             * 1 -  staff address
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET Address = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateStaffAddress = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update staff member assigned hotel ID
             * Indices to use when calling this prepared statement: 
             * 1 -  staff hotel ID
             * 2 -  staff ID
             */
            reusedSQLVar = 
                "UPDATE Staff " + 
                "SET HotelID = ? " + 
                "WHERE ID = ?;";
            jdbcPrep_updateStaffHotelID = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Get all values of a given tuple (by ID) from the Staff table
             * Indices to use when calling this prepared statement:
             * 1 -  ID
             */
            reusedSQLVar = 
                "SELECT * FROM Staff WHERE ID = ?;";
            jdbcPrep_getStaffByID = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Delete a staff member (by ID)
             * Any hotel, room, customer, staff member, or service type associated with a current guest stay may not be deleted
             * Since we set DCStaff and DRSStaff to NULL when a room is released, we needn't look at the Stays table
             * Indices to use when calling this prepared statement:
             * 1 -  ID
             */
            reusedSQLVar = 
                "DELETE FROM Staff " + 
                "WHERE ID = ? AND " + 
                "ID NOT IN (SELECT DCStaff FROM Rooms WHERE DCStaff IS NOT NULL) AND " + 
                "ID NOT IN (SELECT DRSStaff FROM Rooms WHERE DRSStaff IS NOT NULL);";
            jdbcPrep_deleteStaff = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Add room
            reusedSQLVar = "INSERT INTO Rooms (RoomNum, HotelID, Category, MaxOcc, NightlyRate) VALUES (? , ?, ?, ?, ?); ";
        	jdbcPrep_insertNewRoom = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update room category
            reusedSQLVar = "UPDATE Rooms SET Category = ? WHERE RoomNum = ? AND HotelID = ?;";
        	jdbcPrep_roomUpdateCategory = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update room max occupancy
            reusedSQLVar = "UPDATE Rooms SET MaxOcc = ? WHERE RoomNum = ? AND HotelID = ?;";
        	jdbcPrep_roomUpdateMaxOccupancy = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update room nightly rate
            reusedSQLVar = "UPDATE Rooms SET NightlyRate = ? WHERE RoomNum = ? AND HotelID = ?;";
        	jdbcPrep_roomUpdateNightlyRate = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update room Dedicated Room Staff
            reusedSQLVar = "UPDATE Rooms SET DRSStaff = ? WHERE RoomNum = ? AND HotelID = ?;";
        	jdbcPrep_roomUpdateDRSStaff = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update room Dedicated Catering Staff
            reusedSQLVar = "UPDATE Rooms SET DCStaff = ? WHERE RoomNum = ? AND HotelID = ?;";
        	jdbcPrep_roomUpdateDCStaff = jdbc_connection.prepareStatement(reusedSQLVar);
        	 
        	// Delete room
        	reusedSQLVar = "DELETE FROM Rooms WHERE RoomNum = ? AND HotelID = ?; ";
        	jdbcPrep_roomDelete = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Check if room number belongs to hotel 
        	reusedSQLVar = "SELECT COUNT(*) AS CNT FROM Rooms WHERE HotelID = ? AND RoomNum = ? ;";			 
			jdbcPrep_isValidRoomNumber = jdbc_connection.prepareStatement(reusedSQLVar); 
			
			/* Check if room is currently occupied
			 * Indices to use when calling this prepared statement:
             * 1 -  hotel ID
             * 2 -  room number
			 */
			reusedSQLVar = "SELECT COUNT(*) AS CNT FROM Stays WHERE HotelID = ? AND RoomNum = ? AND (CheckOutTime IS NULL OR EndDate IS NULL);";			 
			jdbcPrep_isRoomCurrentlyOccupied = jdbc_connection.prepareStatement(reusedSQLVar); 

			// Check if hotel exists in the database
			reusedSQLVar = "SELECT COUNT(*) AS CNT FROM Hotels WHERE ID = ? ;"; 
			jdbcPrep_isValidHotelID = jdbc_connection.prepareStatement(reusedSQLVar); 
			
			// Report room details by room number and hotel id
			reusedSQLVar = "SELECT * FROM Rooms WHERE RoomNum = ? AND HotelID = ?; ";
        	jdbcPrep_getRoomByHotelIDRoomNum = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	/* Report occupied rooms in one specific hotel
             * Indices to use when calling this prepared statement:
             * 1 -  hotel ID
        	 */
            reusedSQLVar = 
                "SELECT * FROM Rooms WHERE HotelID = ? AND " + 
                "EXISTS (SELECT * FROM Stays WHERE Stays.HotelID = Rooms.HotelID AND Stays.RoomNum = Rooms.RoomNum AND EndDate IS NULL);";
            jdbcPrep_getOccupiedRoomsInHotel = jdbc_connection.prepareStatement(reusedSQLVar);
			 
            // Add customer
            reusedSQLVar = "INSERT INTO Customers (SSN, Name, DOB, PhoneNum, Email) VALUES (? , ?, ?, ?, ?); ";
        	jdbcPrep_insertNewCustomer = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Report customer by ID
        	reusedSQLVar = "SELECT * FROM Customers WHERE ID = ?";
        	jdbcPrep_getCustomerByID = jdbc_connection.prepareStatement(reusedSQLVar);
        	
            // Update Customer SSN
            reusedSQLVar = "UPDATE Customers SET SSN = ? WHERE ID = ?; ";
            jdbcPrep_customerUpdateSSN = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update Customer Name
        	reusedSQLVar = "UPDATE Customers SET Name = ? WHERE ID = ?; ";
        	jdbcPrep_customerUpdateName = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update Customer Date Of Birth
        	reusedSQLVar = "UPDATE Customers SET DOB = ? WHERE ID = ?; ";
        	jdbcPrep_customerUpdateDateOfBirth = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update Customer Phone Number
        	reusedSQLVar = "UPDATE Customers SET PhoneNum = ? WHERE ID = ?; ";
        	jdbcPrep_customerUpdatePhoneNumber = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Update Customer Email
        	reusedSQLVar = "UPDATE Customers SET Email = ? WHERE ID = ?; ";
        	jdbcPrep_customerUpdateEmail = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Check if customer exists in the database
        	reusedSQLVar = "SELECT COUNT(*) AS CNT FROM Customers WHERE ID = ?";
        	jdbcPrep_isValidCustomer = jdbc_connection.prepareStatement(reusedSQLVar);  
        	
        	// Delete customer
        	reusedSQLVar = "DELETE FROM Customers WHERE ID = ?; ";
        	jdbcPrep_customerDelete = jdbc_connection.prepareStatement(reusedSQLVar);
        	
        	// Check if customer is associated with current(ongoing) stay
        	reusedSQLVar = "SELECT COUNT(*) AS CNT FROM Stays WHERE CustomerID = ? AND (CheckOutTime IS NULL OR EndDate IS NULL);";
        	jdbcPrep_isCustomerCurrentlyStaying = jdbc_connection.prepareStatement(reusedSQLVar);
        	
            /* Assign a room to a customer
             * Enforce maximum occupancy of the room
             * Enforce the presence of further billing information in the case that the customer chooses to pay with a credit card.  
             * This is enforced by using a SELECT statement to produce values to be inserted.  
             * If the WHERE clause evaluates to false then no values will be produced, and thus the insertion will not take place.
             * Indices to use when calling this prepared statement:
             * 1 -  Customer ID
             * 2 -  Number of guests
             * 3 -  Payment method
             * 4 -  Card type
             * 5 -  Card number
             * 6 -  Billing address
             * 7 -  Room number
             * 8 -  Hotel ID
             * 9 -  Payment method (again)
             * 10 - Card type (again)
             * 11 - Card number (again)
             * 12 - Billing address (again)
             * 13 - Number of guests (again)
             */
            reusedSQLVar = 
                "INSERT INTO Stays " + 
                "(StartDate, CheckInTime, RoomNum, HotelID, CustomerID, NumGuests, PaymentMethod, CardType, CardNumber, BillingAddress) " + 
                "SELECT CURDATE(), CURTIME(), RoomNum, HotelID, ?, ?, ?, ?, ?, ? " + 
                "FROM Rooms WHERE " + 
                "RoomNum = ? AND HotelID = ? AND " + 
                "(? <> 'CARD' OR (? IS NOT NULL AND ? IS NOT NULL AND ? IS NOT NULL)) AND " +
                "MaxOcc >= ?;";
            jdbcPrep_assignRoom = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Get first available catering staff in a hotel
             * Indices to use when calling this prepared statement:
             * 1 -  Hotel ID
             */
            reusedSQLVar = 
                "SELECT MIN(ID) FROM (SELECT ID FROM Staff WHERE JobTitle = 'Catering' AND HotelID = ? AND " + 
                "ID NOT IN (SELECT DCStaff FROM Rooms WHERE DCStaff IS NOT NULL)) AS X;";
            jdbcPrep_getFirstAvailableCateringStaff = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get first available room service staff in a hotel
             * Indices to use when calling this prepared statement:
             * 1 -  Hotel ID
             */
            reusedSQLVar = 
                "SELECT MIN(ID) FROM (SELECT ID FROM Staff WHERE JobTitle = 'Room Service' AND HotelID = ? AND " + 
                "ID NOT IN (SELECT DCStaff FROM Rooms WHERE DCStaff IS NOT NULL)) AS X;";
            jdbcPrep_getFirstAvailableRoomServiceStaff = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Assign dedicated staff to a presidential suite
             * Get first available room service staff in the hotel
             * Get first available catering staff in the hotel
             * Make sure there is actually a customer stay assigned for this room
             * Indices to use when calling this prepared statement:
             * 1 -  room service staff
             * 2 -  catering staff
             * 3 -  Room number
             * 4 -  Hotel ID
             * 5 -  Room number (again)
             * 6 -  Hotel ID (again)
             */
            reusedSQLVar = 
                "UPDATE Rooms SET " + 
                "DRSStaff = ?, " + 
                "DCStaff = ? " +
                "WHERE RoomNum = ? AND HotelID = ? AND EXISTS(SELECT * FROM Stays WHERE RoomNum = ? AND HotelID = ? AND EndDate IS NULL);";
            jdbcPrep_assignDedicatedStaff = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Release the dedicated staff when releasing room
            reusedSQLVar = "UPDATE Rooms SET DCStaff = NULL, DRSStaff = NULL WHERE HotelID = ? AND RoomNum = ?;";
            jdbcPrep_releaseDedicatedStaff = jdbc_connection.prepareStatement(reusedSQLVar); 
            
            /* Get the newest stay in the DB
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = "SELECT * FROM Stays WHERE ID >= ALL (SELECT ID FROM Stays);";
            jdbcPrep_getNewestStay = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Insert a new stay (in a dumb, no-safety-check way suitable for mass population)
             * Indices to use when calling this prepared statement:
             * 1 -  start date
             * 2 -  check in time
             * 3 -  room number
             * 4 -  hotel ID
             * 5 -  customer ID
             * 6 -  number of guests
             * 7 -  check out time
             * 8 -  end date
             * 9 -  payment method
             * 10 - card type
             * 11 - card number
             * 12 - billing address
             */
            reusedSQLVar = 
                "INSERT INTO Stays " + 
                "(StartDate, CheckInTime, RoomNum, HotelID, CustomerID, NumGuests, CheckOutTime, EndDate, PaymentMethod, CardType, CardNumber, BillingAddress) " + 
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            jdbcPrep_addStayNoSafetyChecks = jdbc_connection.prepareStatement(reusedSQLVar);
                                  
            // Get the Stay ID for given room
            reusedSQLVar = "SELECT ID FROM Stays WHERE HotelID = ? AND RoomNum = ?;";
            jdbcPrep_getStayIdForOccupiedRoom = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get a brief, user-friendly summary of a single customer stay
             * Indices to use when calling this prepared statement:
             * 1 -  stay ID
             */
            reusedSQLVar = 
                "SELECT " + 
                "(SELECT Name FROM Customers WHERE Customers.ID = Stays.CustomerID) AS Customer, StartDate, EndDate, " + 
                "(SELECT Name FROM Hotels WHERE Hotels.ID = Stays.HotelID) AS Hotel, RoomNum, PaymentMethod, CardType " + 
                "FROM Stays WHERE ID = ?";
            jdbcPrep_getSummaryOfStay = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get stay ID by room number and hotel ID
             * Indices to use when calling this prepared statement:
             * 1 -  room number
             * 2 -  hotel ID
             */
            reusedSQLVar = "SELECT ID FROM Stays WHERE Stays.RoomNum = ? AND Stays.HotelID = ? AND EndDate IS NULL;";
            jdbcPrep_getStayByRoomAndHotel = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Generate an itemized receipt for a stay
             * This produces the union of two relations
             * First, a relation showing costs incurred based on nights stayed in a particular room
             * Second, a relation showing costs incurred by getting extra services (catering, etc)
             * Indices to use when calling this prepared statement:
             * 1 -  stay ID
             * 2 -  stay ID (again)
             */
            reusedSQLVar = 
                "(" + 
                    "SELECT 'NIGHT' AS Item, Nights AS Qty, NightlyRate AS ItemCost, Nights * NightlyRate AS TotalCost " + 
                    "FROM (" + 
                        "SELECT DATEDIFF(EndDate, StartDate) AS Nights, NightlyRate " + 
                        "FROM (" + 
                            "SELECT StartDate, EndDate, NightlyRate " + 
                            "FROM Rooms NATURAL JOIN (" + 
                                "SELECT StartDate, EndDate, RoomNum, HotelID " + 
                                "FROM Stays " + 
                                "WHERE ID = ?" + 
                            ") AS A " + 
                        ") AS B " + 
                    ") AS C " + 
                ") " + 
                "UNION " + 
                "(" + 
                    "SELECT Name AS Item, Cost AS ItemCost, COUNT(*) AS Qty, COUNT(*) * Cost AS TotalCost " + 
                    "FROM (" + 
                        "ServiceTypes NATURAL JOIN (" + 
                            "SELECT StayID, ServiceName AS Name " + 
                            "FROM Provided " + 
                            "WHERE StayID = ?" + 
                        ") AS ServicesProvided " + 
                    ") GROUP BY Item" + 
                ")";
            jdbcPrep_getItemizedReceipt = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update the amount owed for a stay
             * Indices to use when calling this prepared statement: 
             * 1 -  amount owed
             * 2 -  stay ID
             */
            reusedSQLVar = 
                "UPDATE Stays SET AmountOwed = ? WHERE ID = ?;";
            jdbcPrep_updateAmountOwed = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Special printing for Rooms table
             * Also print out whether the room is available.
             * This is to more easily demonstrate that the demo data was populated correctly.
             * While you're at it, print ordered by hotel ID because the output makes more sense that way.
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = 
                "SELECT HotelID, RoomNum, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff, " + 
                "IF" + 
                    "(NOT EXISTS(SELECT * FROM Stays WHERE Stays.RoomNum = Rooms.RoomNum AND Stays.HotelID = Rooms.HotelID AND EndDate IS NULL) AND " + 
                    "(Rooms.Category <> 'Presidential' OR ( " + 
                    "EXISTS (SELECT ID FROM Staff WHERE Staff.JobTitle = 'Catering' AND Staff.HotelID = Rooms.HotelID AND " + 
                    "ID NOT IN (SELECT DCStaff FROM Rooms WHERE DCStaff IS NOT NULL)) AND " +
                    "EXISTS (SELECT ID FROM Staff WHERE Staff.JobTitle = 'Room Service' AND Staff.HotelID = Rooms.HotelID AND " + 
                    "ID NOT IN (SELECT DRSStaff FROM Rooms WHERE DRSStaff IS NOT NULL))))" +
                ", 'Yes', 'No') AS Available " + 
                "FROM Rooms ORDER BY HotelID, RoomNum";
            jdbcPrep_reportTableRooms = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Special printing for Staff table
             * Also print out staff member's age.
             * This is to more easily demonstrate that the demo data was populated correctly.
             * While you're at it, print ordered by hotel ID because the output makes more sense that way.
             */ 
            reusedSQLVar = 
                "SELECT HotelID, ID, Name, DOB, FLOOR(DATEDIFF(CURDATE(), DOB) / 365) AS Age, JobTitle, Dep, PhoneNum, Address " + 
                "FROM Staff ORDER BY HotelID;";
            jdbcPrep_reportTableStaff = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Special printing for Stays table
             * Also print out customer's SSN.
             * This is to more easily demonstrate that the demo data was populated correctly.
             * While you're at it, print ordered by hotel ID because the output makes more sense that way.
             */
            reusedSQLVar = 
                "SELECT ID, StartDate, CheckInTime, EndDate, CheckOutTime, RoomNum, HotelID, CustomerID, " + 
                "(SELECT SSN FROM Customers WHERE Customers.ID = Stays.CustomerID) AS CustomerSSN, " + 
                "NumGuests, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress " + 
                "FROM Stays;";
            jdbcPrep_reportTableStays = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Update the CheckOutTime and EndDate while releasing the room
            reusedSQLVar = "UPDATE Stays SET CheckOutTime = CURTIME(), EndDate = CURDATE() WHERE ID = ?;";
            jdbcPrep_updateCheckOutTimeAndEndDate = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Check if Stay id exists in the Stays table
            reusedSQLVar = "SELECT COUNT(*) AS CNT FROM Stays WHERE ID = ?;";
            jdbcPrep_isValidStayID = jdbc_connection.prepareStatement(reusedSQLVar);
                        
            // Report Occupancy By Hotel
            reusedSQLVar = "SELECT " +
            				"HotelID, " +
            				"count(*) AS TotalRooms, " + 
            				"COALESCE(SUM(OccupiedFlag),0) AS OccupiedRooms, " + 
            				"count(*) - COALESCE(SUM(OccupiedFlag),0) AS AvailableRooms " +
            				"FROM (" +
            				"Rooms NATURAL LEFT OUTER JOIN " + 
            				"( " +
            				"SELECT RoomNum, HotelID, 1 AS OccupiedFlag " +
            				"FROM Stays AS X " + 
            				"WHERE CheckOutTime IS NULL OR EndDate IS NULL " +
            				") AS Y) " +
            				"GROUP BY HotelID;";
            jdbcPrep_reportOccupancyByHotel = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Report Occupancy By Room Type
            reusedSQLVar = "SELECT Category AS RoomType, count(*) AS TotalRooms, SUM(OccupiedFlag) AS Occupancy, " +
            				"count(*) - SUM(OccupiedFlag) AS Availability " +
            				"FROM (" + 
            				"SELECT RoomNum, HotelID, Category, IF( " +
            				"EXISTS( " +
            				"SELECT * " +
            				"FROM Stays " +
            				"WHERE Stays.RoomNum = Rooms.RoomNum AND Stays.HotelID = Rooms.HotelID AND ( " +
            				"CheckOutTime IS NULL OR " +
            				"EndDate IS NULL " +
            				") " +
            				"), " +
            				"1, " +
            				"0 " +
            				") AS OccupiedFlag " +
            				"FROM Rooms " +
            				") AS X " +
            				"GROUP BY Category; ";
            jdbcPrep_reportOccupancyByRoomType = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Report Occupancy By Date Range
            reusedSQLVar = "SELECT SUM(OccupiedFlag) AS RoomsOccupied " +
            				"FROM ( " +
            				"SELECT RoomNum, HotelID, IF (count(*) > 0, 1, 0) AS OccupiedFlag " + 
            				"FROM Stays WHERE " +
            				"StartDate <= ? AND " + 
            				"CURDATE() >= ? AND " +
            				"(EndDate >= ? OR EndDate IS NULL) " +
            				" GROUP BY RoomNum, HotelID " +
            				") AS X; ";
            jdbcPrep_reportOccupancyByDateRange = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Report Occupancy By City
            reusedSQLVar = "SELECT " +
            				"City, " +
            				"count(*) AS TotalRooms, " +
            				"COALESCE(SUM(OccupiedFlag),0) AS OccupiedRooms, " +
            				"count(*) - COALESCE(SUM(OccupiedFlag),0) AS AvailableRooms " +
            				"FROM ( " +
            				"Rooms NATURAL LEFT OUTER JOIN  " +
            				"( " +
            				"SELECT RoomNum, HotelID, 1 AS OccupiedFlag " +
            				"FROM Stays AS X  " +
            				"WHERE CheckOutTime IS NULL OR EndDate IS NULL " +
            				") AS Y NATURAL LEFT OUTER JOIN " +
            				"(SELECT ID AS HotelID, City FROM Hotels) AS Z" +
            				") " +
            				"GROUP BY City; ";
            jdbcPrep_reportOccupancyByCity = jdbc_connection.prepareStatement(reusedSQLVar);                        

            // Report Total Occupancy
            reusedSQLVar = "SELECT count(*) AS TotalOccupancy " +
            				"FROM Stays " +
            				"WHERE " +
            				"CheckOutTime IS NULL OR " +
            				"EndDate IS NULL; ";
            jdbcPrep_reportTotalOccupancy = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Report Percentage Of Rooms Occupied
            reusedSQLVar = "SELECT (SUM(OccupiedFlag) / count(*)) * 100 AS PercentOccupied " +
            				"FROM ( " +
            				"Rooms NATURAL LEFT OUTER JOIN " +
            				"( " +
            				"SELECT RoomNum, HotelID, 1 AS OccupiedFlag " +
            				"FROM Stays AS X " +
            				"WHERE CheckOutTime IS NULL OR EndDate IS NULL " +
            				") AS Y); ";
            jdbcPrep_reportPercentageOfRoomsOccupied = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Report Staff grouped by role
            reusedSQLVar = "SELECT * FROM Staff ORDER BY JobTitle; ";
            jdbcPrep_reportStaffGroupedByRole = jdbc_connection.prepareStatement(reusedSQLVar);
            
            // Report Staff serving the customer during stay
            reusedSQLVar = "SELECT Provided.ID, ServiceName, StaffID, Name, JobTitle " + 
            				"FROM Provided, Staff " +
            				"WHERE Provided.StaffID = Staff.ID AND StayID = ?;" ;
            jdbcPrep_reportStaffServingDuringStay = jdbc_connection.prepareStatement(reusedSQLVar); 
            
            // Report Hotel revenue during given date range
            reusedSQLVar = "SELECT SUM(AmountOwed) AS Revenue " + 
            				"FROM Stays " +
            				"WHERE ( " +
            				"HotelID = ? AND " +
            				"EndDate >= ? " +
            				"AND EndDate <= ? );" ;
            jdbcPrep_reportHotelRevenueByDateRange = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get a bit of info about all staff eligible to provide a given service for a given room in a given hotel
             * Must have correct job title
             * Must be serving the hotel
             * Must not be dedicated to a different room
             * Indices to use when calling this prepared statement:
             * 1 -  service name
             * 2 -  service name (again)
             * 3 -  service name (again)
             * 4 -  hotel ID
             * 5 -  room number
             */
            reusedSQLVar = 
                "SELECT ID, Name, JobTitle FROM Staff WHERE " +
                "(? = Staff.JobTitle OR ? = 'Phone' OR ? = 'Special Request' OR Staff.JobTitle = 'Manager') AND " + 
                "Staff.HotelID = ? AND " + 
                "NOT EXISTS (SELECT * FROM Rooms WHERE Rooms.RoomNum <> ? AND (DRSStaff = Staff.ID OR DCStaff = Staff.ID));";
            jdbcPrep_getEligibleStaffForService = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Insert new service type
             * Indices to use when calling this prepared statement:
             * 1 -  name of service
             * 2 -  cost of service
             */
            reusedSQLVar = 
                "INSERT INTO ServiceTypes (Name, Cost) " + 
                "VALUES (?, ?);";
            jdbcPrep_insertNewServiceType = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Insert new service record for stay
             * Staff member must have correct job title
             * Staff member must be serving the hotel
             * Staff member must not be dedicated to a different room
             * Indices to use when calling this prepared statement:
             * 1 -  stay ID
             * 2 -  staff ID
             * 3 -  name of service
             */
            reusedSQLVar = 
                "INSERT INTO Provided (StayID, StaffID, ServiceName) " + 
                "SELECT Stays.ID, Staff.ID, ServiceTypes.Name " + 
                "FROM Stays, Staff, ServiceTypes WHERE " + 
                "Stays.ID = ? AND " + 
                "Staff.ID = ? AND " + 
                "ServiceTypes.Name = ? AND (" + 
                "Staff.JobTitle = ServiceTypes.Name OR " + 
                "ServiceTypes.Name = 'Phone' OR " + 
                "ServiceTypes.Name = 'Special Request' OR " + 
                "Staff.JobTitle = 'Manager') AND " + 
                "Staff.HotelID = Stays.HotelID AND " + 
                "NOT EXISTS (SELECT * FROM Rooms WHERE " + 
                "RoomNum <> Stays.RoomNum AND (DRSStaff = Staff.ID OR DCStaff = Staff.ID)" + 
                ");";
            jdbcPrep_insertNewServiceRecord = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Update a service record for stay
             * This update assumes that the application level checks that the name of service and staff ID are appropriate!
             * Indices to use when calling this prepared statement:
             * 1 -  staff ID
             * 2 -  name of service
             * 3 -  service record ID
             */
            reusedSQLVar = 
                "UPDATE Provided SET StaffID = ?, ServiceName = ? WHERE ID = ?;";
            jdbcPrep_udpateServiceRecord = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get the newest service record in the DB
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = "SELECT * FROM Provided WHERE ID >= ALL (SELECT ID FROM Provided);";
            jdbcPrep_getNewestServiceRecord = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get the basic info (service & staff) for a service record, by ID
             * Indices to use when calling this prepared statement:
             * 1 -  service record ID
             */
            reusedSQLVar = "Select ServiceName, StaffID FROM Provided WHERE ID = ?";
            jdbcPrep_getServiceNameAndStaffByID = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get valid service names
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = "Select Name FROM ServiceTypes";
            jdbcPrep_getValidServiceNames = jdbc_connection.prepareStatement(reusedSQLVar);
            
            /* Get service record, by ID
             * Indices to use when calling this prepared statement: 
             * 1 -  service record ID
             */
            reusedSQLVar = "Select * FROM Provided WHERE ID = ?";
            jdbcPrep_getServiceRecordByID = jdbc_connection.prepareStatement(reusedSQLVar);

            /* Update the cost of a service
               Indices to use when calling this prepared statement:
               1 -  service name
               2 -  Cost of service
            */
            reusedSQLVar = "Update ServiceTypes SET Cost = ? WHERE Name = ?";
            jdbcPrep_updateServiceCost = jdbc_connection.prepareStatement(reusedSQLVar);

            /**
             * Get currently dedicated staff members (Room Service and Catering Staff)
             * Indices to use when calling this prepared statement: n/a
             */
            reusedSQLVar = "SELECT DRSStaff as StaffID FROM Rooms "+
                "WHERE DRSStaff IS NOT NULL "+
                "UNION "+
                "SELECT DCStaff as StaffID FROM Rooms "+
                "WHERE DCStaff IS NOT NULL";
            jdbcPrep_getDedicatedStaffMembers = jdbc_connection.prepareStatement(reusedSQLVar);

            /**
             * Support Query to get job title of staff by ID
             * Indices: StaffID
             */
            reusedSQLVar = "SELECT JobTitle from Staff WHERE ID = ?";
            jdbcPrep_getJobTitlebyID = jdbc_connection.prepareStatement(reusedSQLVar);
                   
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }

    /** 
     * Drop database tables, if they exist
     * (to support running program many times)
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     */
    public static void startup_dropExistingTables() {

        try {
            
            // Declare variables
            DatabaseMetaData metaData;
            String tableName;

            /* Find out what tables already exist
             * https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html
             */
            metaData = jdbc_connection.getMetaData();
            jdbc_result = metaData.getTables(null, null, "%", null);
            
            // Go through and delete each existing table
            while (jdbc_result.next()) {
                // Get table name
                tableName = jdbc_result.getString(3);
                /* Drop disable foreign key checks to avoid complaint
                 * https://stackoverflow.com/questions/4120482/foreign-key-problem-in-jdbc
                 */
                jdbc_statement.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
                // Drop table
                jdbc_statement.executeUpdate("DROP TABLE " + tableName);
                // Re-establish normal foreign key checks
                jdbc_statement.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Create database tables
     * 
     * Note:    CHECK    
     *              Per https://dev.mysql.com/doc/refman/5.7/en/create-table.html,
     *              "The CHECK clause is parsed but ignored by all storage engines",
     *          ASSERTION
     *              Per https://stackoverflow.com/questions/34769321/unexplainable-mysql-error-when-trying-to-create-assertion
     *              "This list does not include CREATE ASSERTION, so MariaDB does not support this functionality"
     *          So unfortunately there are some data entry error checks that we must perform
     *          in the application rather than letting the DBMS do it for us
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Changed state to CHAR(2).
     *                  03/09/18 -  ATTD -  Added on delete rules for foreign keys.
     *                  03/11/18 -  ATTD -  Added amount owed to Stays relation.
     *                  03/12/18 -  ATTD -  Changed Provided table to set staff ID to NULL when staff member is deleted.
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/14/18 -  ATTD -  Changed service type names to enum, to match project assumptions.
     *                  03/17/18 -  ATTD -  Billing address IS allowed be NULL (when payment method is not card) per team discussion.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/24/18 -  MTA -   Name the primary key constraints.
     *                  04/04/18 -  ATTD -  Add attribute for hotel zip code, per demo data.
     *                                      Start staff auto increment at 100, per demo data.
     *                                      Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static void startup_createTables() {
        
        try {

            // Drop all tables that already exist, so that we may run repeatedly
            startup_dropExistingTables();
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
            
                /* Create table: Customers
                 * phone number to be entered as 10 digit int ex: 9993335555
                 * requires "BIGINT" instead of just "INT"
                 * SSN to be entered as 9 digit int ex: 100101000
                 * requires "BIGINT" instead of just "INT"
                 */
                jdbc_statement.executeUpdate("CREATE TABLE Customers ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "SSN BIGINT NOT NULL,"+
                    "Name VARCHAR(255) NOT NULL,"+
                    "DOB DATE NOT NULL,"+
                    "PhoneNum BIGINT NOT NULL,"+
                    "Email VARCHAR(255) NOT NULL,"+
                    "CONSTRAINT PK_CUSTOMERS PRIMARY KEY (ID)"+
                ");");
                jdbc_statement.executeUpdate("ALTER TABLE Customers AUTO_INCREMENT = 1001;");
    
                // Create table: ServiceTypes
                jdbc_statement.executeUpdate("CREATE TABLE ServiceTypes ("+
                    "Name ENUM('Phone','Dry Cleaning','Gym','Room Service','Catering','Special Request') NOT NULL,"+
                    "Cost INT NOT NULL,"+
                    "CONSTRAINT PK_SERVICE_TYPES PRIMARY KEY (Name)"+
                ");");
    
                /* Create table: Staff
                 * phone number to be entered as 10 digit int ex: 9993335555
                 * requires "BIGINT" instead of just "INT"
                 */
                jdbc_statement.executeUpdate("CREATE TABLE Staff ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "Name VARCHAR(255) NOT NULL,"+
                    "DOB DATE NOT NULL,"+
                    "JobTitle VARCHAR(255),"+
                    "Dep VARCHAR(255) NOT NULL,"+
                    "PhoneNum BIGINT NOT NULL,"+
                    "Address VARCHAR(255) NOT NULL,"+
                    "HotelID INT,"+
                    "CONSTRAINT PK_STAFF PRIMARY KEY(ID)"+
                ");");
                jdbc_statement.executeUpdate("ALTER TABLE Staff AUTO_INCREMENT = 100;");
    
                /* Create table: Hotels
                 * this is done after Staff table is created
                 * because manager ID references Staff table
                 * phone number to be entered as 10 digit int ex: 9993335555
                 * requires "BIGINT" instead of just "INT"
                 * As noted in https://classic.wolfware.ncsu.edu/wrap-bin/mesgboard/csc:540::001:1:2018?task=ST&Forum=13&Topic=8,
                 * hotel IDs actually start at 1, NOT at 1001 as is noted in the originally posted demo data
                 */
                jdbc_statement.executeUpdate("CREATE TABLE Hotels ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "Name VARCHAR(255) NOT NULL,"+
                    "StreetAddress VARCHAR(255) NOT NULL,"+
                    "City VARCHAR(255) NOT NULL,"+
                    "State CHAR(2) NOT NULL,"+
                    "Zip INT NOT NULL,"+
                    "PhoneNum BIGINT Not Null,"+
                    "ManagerID INT Not Null,"+
                    "CONSTRAINT PK_HOTELS Primary Key(ID),"+
                    "CONSTRAINT UC_HACS UNIQUE (StreetAddress, City, State),"+
                    "CONSTRAINT UC_HPN UNIQUE (PhoneNum),"+
                    "CONSTRAINT UC_HMID UNIQUE (ManagerID),"+
                    /* If a manager is deleted from the system and not replaced in the same transaction, no choice but to delete hotel
                     * A hotel cannot be without a manager
                     */
                    "CONSTRAINT FK_HMID FOREIGN KEY (ManagerID) REFERENCES Staff(ID) ON DELETE CASCADE"+
                ");");
    
                /* Alter table: Staff
                 * needs to happen after Hotels table is created
                 * because hotel ID references Hotels table
                 */
                jdbc_statement.executeUpdate("ALTER TABLE Staff "+
                    "ADD CONSTRAINT FK_STAFFHID "+
                     /* If a hotel is deleted, no need to delete the staff that work there,
                      * NULL is allowed (currently unassigned staff)
                      */
                    "FOREIGN KEY (HotelID) REFERENCES Hotels(ID) ON DELETE SET NULL;"
                ); 
    
                // Create table: Rooms
                jdbc_statement.executeUpdate("CREATE TABLE Rooms ("+
                    "RoomNum INT NOT NULL,"+
                    "HotelID INT NOT NULL,"+
                    "Category VARCHAR(255) NOT NULL,"+
                    "MaxOcc INT NOT NULL,"+
                    "NightlyRate DOUBLE NOT NULL,"+
                    "DRSStaff INT,"+
                    "DCStaff INT,"+
                    "CONSTRAINT PK_ROOMS PRIMARY KEY(RoomNum,HotelID),"+
                    // If a hotel is deleted, then the rooms within it should also be deleted
                    "CONSTRAINT FK_ROOMHID FOREIGN KEY (HotelID) REFERENCES Hotels(ID) ON DELETE CASCADE,"+
                    /* If a staff member dedicated to a room is deleted by the end of a transaction
                     * then something has probably gone wrong, because that staff member should have been replaced
                     * to maintain continuous service
                     * Nonetheless, not appropriate to delete the room in this case
                     * NULL is allowed
                    */
                    "CONSTRAINT FK_ROOMDRSID FOREIGN KEY (DRSStaff) REFERENCES Staff(ID) ON DELETE SET NULL,"+
                    "CONSTRAINT FK_ROOMDCID FOREIGN KEY (DCStaff) REFERENCES Staff(ID) ON DELETE SET NULL"+
                ");");
    
                // Create table: Stays
                jdbc_statement.executeUpdate("CREATE TABLE Stays ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "StartDate DATE NOT NULL,"+
                    "CheckInTime TIME NOT NULL,"+
                    "RoomNum INT NOT NULL,"+
                    "HotelID INT NOT NULL,"+
                    "CustomerID INT NOT NULL,"+
                    "NumGuests INT NOT NULL,"+
                    "CheckOutTime TIME,"+
                    "EndDate DATE,"+
                    "AmountOwed DOUBLE,"+
                    "PaymentMethod ENUM('CASH','CARD') NOT NULL,"+
                    "CardType ENUM('VISA','MASTERCARD','HOTEL'),"+
                    "CardNumber BIGINT,"+
                    "BillingAddress VARCHAR(255),"+
                    "CONSTRAINT PK_STAYS PRIMARY KEY(ID),"+
                    "CONSTRAINT UC_STAYKEY UNIQUE (StartDate, CheckInTime,RoomNum, HotelID),"+
                    /* If a room is deleted, then the stay no longer makes sense and should be deleted
                     * Need to handle room/hotel together as a single foreign key
                     * Because a foreign key is supposed to point to a unique tuple
                     * And room number by itself is not unique
                    */
                    "CONSTRAINT FK_STAYRID FOREIGN KEY (RoomNum, HotelID) REFERENCES Rooms(RoomNum, HotelID) ON DELETE CASCADE,"+
                    // If a customer is deleted, then the stay no longer makes sense and should be deleted
                    "CONSTRAINT FK_STAYCID FOREIGN KEY (CustomerID) REFERENCES Customers(ID) ON DELETE CASCADE"+
                ");");
    
                // Create table: Provided
                jdbc_statement.executeUpdate("CREATE TABLE Provided ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "StayID INT NOT NULL,"+
                    "StaffID INT,"+
                    "ServiceName ENUM('Phone','Dry Cleaning','Gym','Room Service','Catering','Special Request') NOT NULL,"+
                    "CONSTRAINT PK_PROVIDED PRIMARY KEY(ID),"+
                    // If a stay is deleted, then the service provided record no longer makes sense and should be deleted
                    "CONSTRAINT FK_PROVSTAYID FOREIGN KEY (StayID) REFERENCES Stays(ID) ON DELETE CASCADE,"+
                    // If a staff member is deleted, then the service provided record still makes sense but has staff ID as NULL
                    "CONSTRAINT FK_PROVSTAFFID FOREIGN KEY (StaffID) REFERENCES Staff(ID) ON DELETE SET NULL,"+
                    // If a service type is deleted, then the service provided record no longer makes sense and should be deleted
                    "CONSTRAINT FK_PROVSERV FOREIGN KEY (ServiceName) REFERENCES ServiceTypes(Name) ON DELETE CASCADE"+
                ");");
                
                // If success, commit
                jdbc_connection.commit();
                
                System.out.println("Tables created successfully!");
            
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    // TABLE POPULATION METHODS
    
    /** 
     * Populate Customers Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/04/18 -  ATTD -  Populate Customers table with demo data.
     */
    public static void populate_Customers() {
        
        try {
            
            /* Start transaction
             * In this function, we add several customer tuples to the Customers table
             * If there is a problem with any tuple, 
             * we feel it is safest to tell the user there is a problem, and leave the table empty
             */
            jdbc_connection.setAutoCommit(false);
            
            try {
            
                /* Populating data for Customers
                 * Signature for method called here:
                 * String ssn, 
                 * String name, 
                 * String dob, 
                 * String phoneNumber, 
                 * String email, 
                 * boolean reportSuccess
                 */
            	db_manageCustomerAdd("5939846", "David",    "1980-01-30", "123", "david@gmail.com",     false); 
            	db_manageCustomerAdd("7778352", "Sarah",    "1971-01-30", "456", "sarah@gmail.com",     false);
            	db_manageCustomerAdd("8589430", "Joseph",   "1987-01-30", "789", "joseph@gmail.com",    false);
            	db_manageCustomerAdd("4409328", "Lucy",     "1985-01-30", "213", "lucy@gmail.com",      false);  

                // If success, commit
                jdbc_connection.commit();
                
                // Tell the user that the table is loaded
                System.out.println("Customers table loaded!");
    		
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Populate ServiceTypes Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/14/18 -  ATTD -  Changed service type names to match job titles, to make queries easier.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/04/18 -  ATTD -  Populate ServiceTypes table with demo data.
     *                  04/11/18 -  ATTD -  Use prepared statement to populate service types table.
     */
    public static void populate_ServiceTypes() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
            
                /* Populating data for ServiceTypes
                 * Indices to use when calling this prepared statement:
                 * 1 -  name of service
                 * 2 -  cost of service
                 */

                jdbcPrep_insertNewServiceType.setString(1, "Phone");
                jdbcPrep_insertNewServiceType.setInt(2, 5);
                jdbcPrep_insertNewServiceType.executeUpdate();
                
                jdbcPrep_insertNewServiceType.setString(1, "Dry Cleaning");
                jdbcPrep_insertNewServiceType.setInt(2, 16);
                jdbcPrep_insertNewServiceType.executeUpdate();

                jdbcPrep_insertNewServiceType.setString(1, "Gym");
                jdbcPrep_insertNewServiceType.setInt(2, 15);
                jdbcPrep_insertNewServiceType.executeUpdate();
                
                jdbcPrep_insertNewServiceType.setString(1, "Room Service");
                jdbcPrep_insertNewServiceType.setInt(2, 10);
                jdbcPrep_insertNewServiceType.executeUpdate();
  
                jdbcPrep_insertNewServiceType.setString(1, "Special Request");
                jdbcPrep_insertNewServiceType.setInt(2, 20);
                jdbcPrep_insertNewServiceType.executeUpdate();

    			// Populate one extra service type that is not in demo data but is in project narrative
                jdbcPrep_insertNewServiceType.setString(1, "Catering");
                jdbcPrep_insertNewServiceType.setInt(2, 50);
                jdbcPrep_insertNewServiceType.executeUpdate();
                
                // If success, commit
                jdbc_connection.commit();
    			
    			System.out.println("ServiceTypes table loaded!");

            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Populate Staff Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Removed explicit setting of ID (this is auto incremented).
     *                  03/10/18 -  ATTD -  Removed explicit setting of hotel ID to null.
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/16/18 -  ATTD -  Changing departments to emphasize their meaninglessness.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/24/18 -  ATTD -  Call insert new staff member method, rather than having SQL directly in this method.
     *                  04/04/18 -  ATTD -  Populate Staff table with demo data.
     *                  04/12/18 -  ATTD -  Removed 3 "extra" staff members that were not in demo data and were not necessary after all.
     */
    public static void populate_Staff() { 
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
            
                /* Populating data for Staff
                 * Signature for method called here:
                 * String name, 
                 * String dob, 
                 * String jobTitle, 
                 * String department, 
                 * long phoneNum, 
                 * String address, 
                 * int hotelID, 
                 * boolean reportSuccess
                 */
                db_manageStaffAdd ("Mary",      "1978-01-01", "Manager",            "Management",   654L, "90 ABC St , Raleigh NC 27",      0, false);
                db_manageStaffAdd ("John",      "1973-01-01", "Manager",            "Management",   564L, "1798 XYZ St , Rochester NY 54",  0, false);
                db_manageStaffAdd ("Carol",     "1963-01-01", "Manager",            "Management",   546L, "351 MH St , Greensboro NC 27",   0, false);
                db_manageStaffAdd ("Emma",      "1963-01-01", "Front Desk Staff",   "Management",   546L, "49 ABC St , Raleigh NC 27",      0, false);
                db_manageStaffAdd ("Ava",       "1963-01-01", "Catering",           "Catering",     777L, "425 RG St , Raleigh NC 27",      0, false);
                db_manageStaffAdd ("Peter",     "1966-01-01", "Manager",            "Management",   724L, "475 RG St , Raleigh NC 27",      0, false);
                db_manageStaffAdd ("Olivia",    "1991-01-01", "Front Desk Staff",   "Management",   799L, "325 PD St , Raleigh NC 27",      0, false);
                
                /* Populate with additional staff beyond what is in the demo data, in order to have dedicated staff available for presidential suite
                 * Room 1, hotel 4, which is in Raleigh
                 * Note that staff IDs are auto-incremented starting at 100
                 */
                db_manageStaffAdd ("Suzy",      "1960-01-01", "Room Service",       "Room Service", 9198675309L, "123 Super St, Raleigh NC 27612",  0, false);
                db_manageStaffAdd ("Edward",    "1961-01-01", "Catering",           "Catering",     9195551234L, "123 Rad Rd, Raleigh NC 27612",    0, false);
        		
                // If success, commit
                jdbc_connection.commit();
             
        		System.out.println("Staff table loaded!");
    		
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Populate Hotels Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Calling method to insert hotels (which also update's new manager's staff info).
     *                  03/11/18 -  ATTD -  Removed 9th hotel.
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/04/18 -  ATTD -  Add attribute for hotel zip code, per demo data.
     *                                      Updated manager IDs to start at 100, per demo data.
     *                                      Populate Hotels table with demo data.
     */
    public static void populate_Hotels() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
            
                /* Populating data for Hotels
                 * Signature of method called here:
                 * String hotelName, 
                 * String streetAddress, 
                 * String city, 
                 * String state, 
                 * int zip, 
                 * long phoneNum, 
                 * int managerID, 
                 * boolean reportSuccess
                 */
                db_manageHotelAdd("Hotel A", "21 ABC St", "Raleigh", "NC", 27, 919L, 100, false);
                db_manageHotelAdd("Hotel B", "25 XYZ St", "Rochester", "NY", 54, 718L, 101, false);
        		db_manageHotelAdd("Hotel C", "29 PQR St", "Greensboro", "NC", 27, 984L, 102, false);
        		db_manageHotelAdd("Hotel D", "28 GHW St", "Raleigh", "NC", 32, 920L, 105, false);
        		
                // If success, commit
                jdbc_connection.commit();
                
        		System.out.println("Hotels table loaded!");
            
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Update Staff Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  MTA -   Created method.
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/27/18 -  ATTD -  Use prepared statement.
     *                  04/04/18 -  ATTD -  Updated staff IDs to start at 100, per demo data.
     *                                      Populate Staff table with demo data.
     *                  04/12/18 -  ATTD -  Removed 3 "extra" staff members that were not in demo data and were not necessary after all.
     */
    public static void populate_updateHotelIdForStaff() {
    	
    	 try {
             
             // Start transaction
             jdbc_connection.setAutoCommit(false);
             
             try {
                 
                 /* Update staff member assigned hotel ID
                  * Indices to use when calling this prepared statement: 
                  * 1 -  staff hotel ID
                  * 2 -  staff ID
                  */
                 
                 jdbcPrep_updateStaffHotelID.setInt(1, 1);
                 jdbcPrep_updateStaffHotelID.setInt(2, 100);
                 jdbcPrep_updateStaffHotelID.executeUpdate();
                 
                 jdbcPrep_updateStaffHotelID.setInt(1, 2);
                 jdbcPrep_updateStaffHotelID.setInt(2, 101);
                 jdbcPrep_updateStaffHotelID.executeUpdate();

                 jdbcPrep_updateStaffHotelID.setInt(1, 3);
                 jdbcPrep_updateStaffHotelID.setInt(2, 102);
                 jdbcPrep_updateStaffHotelID.executeUpdate();
                 
                 jdbcPrep_updateStaffHotelID.setInt(1, 1);
                 jdbcPrep_updateStaffHotelID.setInt(2, 103);
                 jdbcPrep_updateStaffHotelID.executeUpdate();
                 
                 jdbcPrep_updateStaffHotelID.setInt(1, 1);
                 jdbcPrep_updateStaffHotelID.setInt(2, 104);
                 jdbcPrep_updateStaffHotelID.executeUpdate();

                 jdbcPrep_updateStaffHotelID.setInt(1, 4);
                 jdbcPrep_updateStaffHotelID.setInt(2, 105);
                 jdbcPrep_updateStaffHotelID.executeUpdate();

                 jdbcPrep_updateStaffHotelID.setInt(1, 4);
                 jdbcPrep_updateStaffHotelID.setInt(2, 106);
                 jdbcPrep_updateStaffHotelID.executeUpdate();
                 
                 jdbcPrep_updateStaffHotelID.setInt(1, 4);
                 jdbcPrep_updateStaffHotelID.setInt(2, 107);
                 jdbcPrep_updateStaffHotelID.executeUpdate();
                 
                 jdbcPrep_updateStaffHotelID.setInt(1, 4);
                 jdbcPrep_updateStaffHotelID.setInt(2, 108);
                 jdbcPrep_updateStaffHotelID.executeUpdate();

                 // If success, commit
                 jdbc_connection.commit();
    			
                 System.out.println("Hotel Id's updated for Staff!");
             
             }
             catch (Throwable err) {
                 
                 // Handle error
                 error_handler(err);
                 // Roll back the entire transaction
                 jdbc_connection.rollback();
                 
             }
             finally {
                 // Restore normal auto-commit mode
                 jdbc_connection.setAutoCommit(true);
             }
             
         }
         catch (Throwable err) {
             error_handler(err);
         }
    }
        
    /** 
     * Populate Rooms Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/11/18 -  ATTD -  Changed dedicated presidential suite staff for hotel 9 to avoid staff conflicts.
     *                  03/11/18 -  ATTD -  Removed hotel 9.
     *                  03/12/18 -  ATTD -  Do not set DRSStaff and DCStaff to NULL explicitly (no need to).
     *                                      Do not set DRSStaff and DCStaff to non-NULL, for rooms not currently occupied.
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/24/18 -  MTA -   Using prepared statements to populate the data.
     *                  04/04/18 -  ATTD -  Updated staff IDs to start at 100, per demo data.
     *                                      Changing room categories to match those given in demo data.
     *                                      Populate Rooms table with demo data.
     *                  04/05/18 -  ATTD -  Remove code to give presidential suite dedicated staff.
     *                                      The room in question is not occupied at the time of application start-up!
     */
    public static void populate_Rooms() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
            
                /* Populating data for Rooms
                 * Signature for method called here:
                 * int roomNumber, 
                 * int hotelId, 
                 * String category, 
                 * int maxOccupancy, 
                 * int nightlyRate, 
                 * boolean reportSuccess
                 */
        		db_manageRoomAdd(1, 1, "Economy",      1, 100,     false);
        		db_manageRoomAdd(2, 1, "Deluxe",       2, 200,     false);
        		db_manageRoomAdd(3, 2, "Economy",      1, 100,     false);
        		db_manageRoomAdd(2, 3, "Executive",    3, 1000,    false);
        		db_manageRoomAdd(1, 4, "Presidential", 4, 5000,    false);
        		db_manageRoomAdd(5, 1, "Deluxe",       2, 200,     false);
        		
                // If success, commit
                jdbc_connection.commit();
    
                System.out.println("Rooms Table loaded!");
            
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Populate Stays Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Removed explicit setting of ID (this is auto incremented).
     *                  03/11/18 -  ATTD -  Added amount owed to Stays relation.
     *                  03/11/18 -  ATTD -  Removed hotel 9.
     *                                      It was added to demonstrate that we can have NULL values for check out time, end date.
     *                                      This demonstration is instead added into some of the existing stays for hotels 1-8.
     *                                      The reason for this change is just to keep the amount of data to a reasonably low level 
     *                                      to help us think through queries and updates more quickly.
     *                  03/11/18 -  ATTD -  Do not set amount owed here (risk of calculating wrong when we calculate by hand).
     *                                      Instead, set by running billing report on the stay.
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/17/18 -  ATTD -  Billing address IS allowed be NULL (when payment method is not card) per team discussion.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/04/18 -  ATTD -  Use prepared statement method to populate Stays table.
     *                                      Populate Stays table with demo data.
     */
    public static void populate_Stays() {
        
        try {
            
            /* Signature of method we're calling here:
             * String startDate,
             * String checkInTime,
             * int roomNum, 
             * int hotelID, 
             * int customerID,
             * int numGuests, 
             * String checkOutTime,     // blank for ongoing stay
             * String endDate,          // blank for ongoing stay
             * String paymentMethod, 
             * String cardType,         // blank if not paying with card
             * long cardNumber,         // -1 if not paying with card
             * String billingAddress    // blank if not paying with card
             */
            db_populateInsertStay(
                "2017-05-10", 
                "15:17:00", 
                1, 
                1, 
                1001,
                1, 
                "10:22:00", 
                "2017-05-13", 
                "CARD", 
                "VISA", 
                1052L, 
                "980 TRT St , Raleigh NC"
            );
            db_populateInsertStay(
                "2017-05-10", 
                "16:11:00", 
                2, 
                1, 
                1002, 
                2, 
                "09:27:00", 
                "2017-05-13", 
                "CARD", 
                "HOTEL", 
                3020L, 
                "7720 MHT St , Greensboro NC"
            );
            db_populateInsertStay(
                "2016-05-10", 
                "15:45:00", 
                3, 
                2, 
                1003, 
                1, 
                "11:10:00", 
                "2016-05-14", 
                "CARD", 
                "VISA", 
                2497L, 
                "231 DRY St , Rochester NY 78"
            );
            db_populateInsertStay(
                "2018-05-10", 
                "14:30:00", 
                2, 
                3, 
                1004, 
                2, 
                "10:00:00", 
                "2018-05-12", 
                "CASH", 
                "", 
                -1, 
                "24 BST Dr , Dallas TX 14"
            );
            
            /* Populate with additional stay beyond what is in the demo data, in order to have "Room#4" unavailable as noted in demo data
             * Room Number 2
             * Hotel 3
             * Executive (so, no dedicated staff are required)
             */
            db_populateInsertStay(
                "2018-04-04", 
                "15:59:00", 
                2, 
                3, 
                1004, 
                2, 
                "", 
                "", 
                "CASH", 
                "", 
                -1, 
                "24 BST Dr , Dallas TX 14"
            );
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Populate Provided Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Removed explicit setting of ID (this is auto incremented).
     *                  03/11/18 -  ATTD -  Added another gym stay to stay ID 1, to more fully exercise ability to produce itemized receipt
     *                                      (itemized receipt needs to sum costs for all instances of the same service type).
     *                  03/12/18 -  ATTD -  Corrected JDBC transaction code (add try-catch).
     *                  03/14/18 -  ATTD -  Changed service type names to match job titles, to make queries easier.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/04/18 -  ATTD -  Update staff IDs to start at 100, per demo data.
     *                                      Populate Provided table with demo data.
     *                  04/06/18 -  ATTD -  Use new method which in turn uses prepared statement.
     *                  04/12/18 -  ATTD -  Removed 3 "extra" staff members that were not in demo data and were not necessary after all.
     *                                      As a result, moved their provided services to the applicable demo data hotel manager.
     */
    public static void populate_Provided() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
            
                /* Populating data for Provided
                 * Signature for this method:
                 * int stayID, 
                 * int staffID, 
                 * String serviceName, 
                 * boolean reportSuccess
                 */
                db_frontDeskEnterService(1, 100, "Dry Cleaning",    false);
                db_frontDeskEnterService(1, 100, "Gym",             false);
                db_frontDeskEnterService(2, 100, "Gym",             false);
                db_frontDeskEnterService(3, 101, "Room Service",    false);
                db_frontDeskEnterService(4, 102, "Phone",           false);

                // If success, commit
                jdbc_connection.commit();
                
        		System.out.println("Provided table loaded!");
    		
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Update Stays Table with amount owed for each stay that has actually ended
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/11/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     */
    public static void populate_updateAmountOwedForStays() {
        
        try {
            
            // Declare variables (this method calls another which uses our global jdbc result, so we need to disambiguate)
            ResultSet local_jdbc_result;
            int stayID;

            // Find the stays that have actually ended (no transaction needed for a query)
            local_jdbc_result = jdbc_statement.executeQuery("SELECT ID FROM Stays WHERE EndDate IS NOT NULL");
            
            // Go through and update amount owed for all stays that have actually ended
            while (local_jdbc_result.next()) {
                stayID = local_jdbc_result.getInt(1);
                db_frontDeskItemizedReceipt(stayID, false);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    // USER-INTERACTION METHODS: FRONT DESK MENU
    
    /** 
     * Front Desk task: Check availability of rooms based on a wide range of characteristics
     * 
     * Normally a "user_" method would just get info from the user, then call a "db_" method to interact with the DB, using prepared statement(s).
     * However, this filter query is so variable that it seemed nuts to try to create all the needed prepared statements.
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/27/18 -  ATTD -  Created method.
     *                  04/08/18 -  ATTD -  Major oversight in initial creation of this method.
     *                                      Was filtering by chosen attributes but NOT by availability!
     *                  04/09/18 -  ATTD -  Another major oversight.
     *                                      Did not consider presidential suite as unavailable if no staff to dedicate to it!
     *                  04/10/18 -  ATTD -  Removed some redundant code.
     */
    public static void user_frontDeskCheckAvailability() {
 
        try {
            
            // Declare local variables
            ArrayList<String> filters = new ArrayList<>();
            String attributeToFilter = "";
            String filterAttrToApply = "";
            String valueToFilter = "";
            String filterValToApply = "";
            String sqlToExecute = "";
            int numRoomsAvailable = 0;
            int i;
            boolean userWantsToStop = false;
            boolean valueIsNumeric = false;
            
            // Print example room so user has some context
            System.out.println("\nExample Room (showing filter options):\n");
            jdbc_result = jdbcPrep_getOneExampleRoom.executeQuery();
            support_printQueryResultSet(jdbc_result);
            
            // Keep filtering results until the user wants to stop
            while (userWantsToStop == false) {
                
                // Print # available rooms in the Wolf Inns chain given existing filtering (need count but later will need all results)
                sqlToExecute = 
                        "SELECT count(*) FROM Rooms WHERE NOT EXISTS " + 
                        "(SELECT * FROM Stays WHERE Stays.RoomNum = Rooms.RoomNum AND Stays.HotelID = Rooms.HotelID AND EndDate IS NULL) AND " +
                        "(Rooms.Category <> 'Presidential' OR ( " + 
                        "EXISTS (SELECT ID FROM Staff WHERE Staff.JobTitle = 'Catering' AND Staff.HotelID = Rooms.HotelID AND " + 
                        "ID NOT IN (SELECT DCStaff FROM Rooms WHERE DCStaff IS NOT NULL)) AND " +
                        "EXISTS (SELECT ID FROM Staff WHERE Staff.JobTitle = 'Room Service' AND Staff.HotelID = Rooms.HotelID AND " + 
                        "ID NOT IN (SELECT DRSStaff FROM Rooms WHERE DRSStaff IS NOT NULL))))";
                for (i = 0; i < filters.size(); i++) {
                    // Prepare to append the filter part of the query
                    sqlToExecute += " AND ";
                    // Each element of filters is of the form attr:value
                    filterAttrToApply = filters.get(i).split(":")[0];
                    filterValToApply = filters.get(i).split(":")[1];
                    // Deal with special case Maximum Occupancy
                    if (filterAttrToApply.equalsIgnoreCase("MaxOcc")) {
                        sqlToExecute += filterAttrToApply + " >= " + filterValToApply;
                    }
                    // Deal with special case Nightly Rate
                    else if (filterAttrToApply.equalsIgnoreCase("NightlyRate")) {
                        sqlToExecute += filterAttrToApply + " <= " + filterValToApply;
                    }
                    // Deal with non-special cases (if string, must include quotes!)
                    else {
                        valueIsNumeric = true;
                        try {
                            Double.parseDouble(filterValToApply);
                        }
                        catch (NumberFormatException e) {
                            valueIsNumeric = false;
                        }
                        if (valueIsNumeric) {
                            sqlToExecute += filterAttrToApply + " = " + filterValToApply;
                        }
                        else {
                            sqlToExecute += filterAttrToApply + " = '" + filterValToApply + "'";
                        }
                    }
                }
                sqlToExecute += ";";
                jdbc_result = jdbc_statement.executeQuery(sqlToExecute);
                if (jdbc_result.next()) {
                    numRoomsAvailable = jdbc_result.getInt(1);
                }
                System.out.println("\nRooms Available: " + numRoomsAvailable + "\n");
                
                if (numRoomsAvailable == 0) {
                    userWantsToStop = true;
                }
                else {
                    
                    // Get name of attribute they want to filter on
                    System.out.print("\nEnter the name of the attribute you wish to ADD a filter for (or press <Enter> to stop)\n> ");
                    attributeToFilter = scanner.nextLine();
                    if (support_isValueSane("AnyAttr", attributeToFilter)) {
                        // Get value they want to change the attribute to
                        if (attributeToFilter.equals("MaxOcc")) {
                            System.out.print("\nFor maximum occupancy, filtering will include any rooms with your desired occupancy or above");
                        }
                        else if (attributeToFilter.equals("NightlyRate")) {
                            System.out.print("\nFor nightly rate, filtering will include any rooms with your desired rate or below");
                        }
                        System.out.print("\nEnter the value you wish to apply as a filter (or press <Enter> to stop)\n> ");
                        valueToFilter = scanner.nextLine();
                        if (support_isValueSane(attributeToFilter, valueToFilter)) {
                            // Add this filter to the growing list
                            filters.add(attributeToFilter + ":" + valueToFilter);
                        }
                        else {
                            userWantsToStop = true;
                        }
                    }
                    else {
                        userWantsToStop = true;
                    }
                    
                }
                
            }
            // Report full info about rooms that satisfied all the filters
            sqlToExecute = sqlToExecute.replace("count(*)", "*");
            jdbc_result = jdbc_statement.executeQuery(sqlToExecute);
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Front Desk task: Assign a room to a customer
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/01/18 -  ATTD -  Created method.
     *                  04/02/18 -  ATTD -  Print out customers to help user pick a valid SSN.
     *                  04/03/18 -  ATTD -  Debug assigning a room to a customer.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static void user_frontDeskCheckIn() {
        
        try {
            
            // Declare local variables
            String roomNum = "";
            String hotelID = "";
            String numGuests = "";
            String customerID = "";
            String paymentMethod = "";
            String cardType = "";
            String cardNumber = "";
            String billingAddress = "";
            
            // Get room number
            System.out.print("\nEnter the room number\n> ");
            roomNum = scanner.nextLine();
            if (support_isValueSane("RoomNum", roomNum)) {
                // Get hotel ID
                System.out.print("\nEnter the hotel ID\n> ");
                hotelID = scanner.nextLine();
                if (support_isValueSane("HotelID", hotelID)) {
                    // Make sure room is actually available
                    if (support_isValidHotelID(Integer.parseInt(hotelID)) == false) {
                        System.out.println("\nCannot assign room " + roomNum + " in hotel " + hotelID + ", because this is not a valid WolfInns hotel\n");
                    }
                    else if (support_isValidRoomForHotel(Integer.parseInt(hotelID), Integer.parseInt(roomNum)) == false) {
                        System.out.println("\nCannot assign room " + roomNum + " in hotel " + hotelID + ", because this is not a valid room for this hotel\n");
                    }
                    else if (support_isRoomCurrentlyOccupied(Integer.parseInt(hotelID), Integer.parseInt(roomNum)) == true) {
                        System.out.println("\nCannot assign room " + roomNum + " in hotel " + hotelID + ", because it is already occupied\n");
                    }
                    else {
                        // Get number of guests
                        System.out.print("\nEnter the number of guests staying in this room\n> ");
                        numGuests = scanner.nextLine();
                        if (support_isValueSane("NumGuests", numGuests)) {
                            // Get customer ID (print out all customers to help user pick a valid ID)
                            user_reportEntireTable("Customers");
                            System.out.print("\nEnter the customer's ID\n> ");
                            customerID = scanner.nextLine();
                            if (support_isValueSane("CustomerID", customerID)) {
                                // Get payment method
                                System.out.print("\nEnter the payment method\n> ");
                                paymentMethod = scanner.nextLine();
                                if (support_isValueSane("PaymentMethod", paymentMethod)) {
                                    // Get billing information IF paying with a card
                                    if (paymentMethod.equalsIgnoreCase("CARD")) {
                                        // Get card type
                                        System.out.print("\nEnter the credit card type\n> ");
                                        cardType = scanner.nextLine();
                                        if (support_isValueSane("CardType", cardType)) {
                                            // Get card number
                                            System.out.print("\nEnter the credit card number\n> ");
                                            cardNumber = scanner.nextLine();
                                            if (support_isValueSane("CardNumber", cardNumber)) {
                                                // Get billing address
                                                System.out.print("\nEnter the billing address\n> ");
                                                billingAddress = scanner.nextLine();
                                                if (support_isValueSane("BillingAddress", billingAddress)) {
                                                    // Okay, at this point everything else I can think of can be caught by a Java exception or a SQL exception
                                                    db_frontDeskAssignRoom(
                                                        Integer.parseInt(roomNum), 
                                                        Integer.parseInt(hotelID), 
                                                        Integer.parseInt(customerID), 
                                                        Integer.parseInt(numGuests), 
                                                        paymentMethod, 
                                                        cardType, 
                                                        Long.parseLong(cardNumber), 
                                                        billingAddress, 
                                                        true
                                                    );
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        db_frontDeskAssignRoom(
                                            Integer.parseInt(roomNum), 
                                            Integer.parseInt(hotelID), 
                                            Integer.parseInt(customerID), 
                                            Integer.parseInt(numGuests), 
                                            paymentMethod, 
                                            "", 
                                            -1, 
                                            "", 
                                            true
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }

    /** 
     * Front desk task: Enter a new service record for a customer stay
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/06/18 -  ATTD -  Created method.
     */
    public static void user_frontDeskEnterService() {
        
        try {

            // Declare variables
            String hotelID;
            String roomNum;
            String serviceName;
            String staffID;
            int stayID;
            
            // Print hotels to console so user has some context
            user_reportEntireTable("Hotels");
            
            // Get hotel ID
            System.out.print("\nEnter the hotel ID for the stay you wish to enter a new service record for\n> ");
            hotelID = scanner.nextLine();
            if (support_isValueSane("ID", hotelID)) {
                
                // Are there any occupied rooms in this hotel?
                jdbcPrep_getOccupiedRoomsInHotel.setInt(1, Integer.parseInt(hotelID));
                jdbc_result = jdbcPrep_getOccupiedRoomsInHotel.executeQuery();
                if (jdbc_result.next()) {
                    
                    // Print all occupied rooms in that hotel so user has some context
                    System.out.println("\nOccupied rooms in this hotel:\n");
                    jdbc_result.beforeFirst();
                    support_printQueryResultSet(jdbc_result);
                    
                    // Get room number
                    System.out.print("\nEnter the room number for the stay you wish to enter a new service record for\n> ");
                    roomNum = scanner.nextLine();
                    if (support_isValueSane("RoomNum", roomNum)) {

                        // Get stay ID based on room number and hotel
                        jdbcPrep_getStayByRoomAndHotel.setInt(1, Integer.parseInt(roomNum));
                        jdbcPrep_getStayByRoomAndHotel.setInt(2, Integer.parseInt(hotelID));
                        jdbc_result = jdbcPrep_getStayByRoomAndHotel.executeQuery();
                        
                        if (jdbc_result.next()) {
                            
                            stayID = jdbc_result.getInt(1);
                            
                            // Print all available services so user has some context
                            System.out.println("\nAvailable Services:\n");
                            user_reportEntireTable("ServiceTypes");
                            
                            // Get service type
                            System.out.print("\nEnter the name of the service provided to the guest\n> ");
                            serviceName = scanner.nextLine();
                            if (support_isValueSane("ServiceName", serviceName)) {
                                
                                /* Print  a bit of info about all staff eligible to provide a given service for a given room in a given hotel
                                 * Must have correct job title
                                 * Must be serving the hotel
                                 * Must not be dedicated to a different room
                                 * Indices to use when calling this prepared statement:
                                 * 1 -  service name
                                 * 2 -  service name (again)
                                 * 3 -  service name (again)
                                 * 4 -  hotel ID
                                 * 5 -  room number
                                 */
                                System.out.print("\nStaff eligible to provide this service:\n\n");
                                jdbcPrep_getEligibleStaffForService.setString(1, serviceName);
                                jdbcPrep_getEligibleStaffForService.setString(2, serviceName);
                                jdbcPrep_getEligibleStaffForService.setString(3, serviceName);
                                jdbcPrep_getEligibleStaffForService.setInt(4, Integer.parseInt(hotelID));
                                jdbcPrep_getEligibleStaffForService.setInt(5, Integer.parseInt(roomNum));
                                jdbc_result = jdbcPrep_getEligibleStaffForService.executeQuery();
                                support_printQueryResultSet(jdbc_result);
                                
                                // Get staff ID
                                System.out.print("\nEnter the staff ID for the staff member providing the service to the guest\n> ");
                                staffID = scanner.nextLine();
                                if (support_isValueSane("StaffID", staffID)) {
                                    
                                    // Call method that interacts with the DB
                                    db_frontDeskEnterService(stayID, Integer.parseInt(staffID), serviceName, true);
                                    
                                }

                            }
                            
                        }
                        else {
                            System.out.println("\nThis is not a valid hotel-room combination (cannot proceed)\n");
                        }

                    }
                    
                }
                else {
                    System.out.println("\nThere are no occupied rooms in this hotel!\n");
                }
                
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Front desk task: Update a service record for a customer stay
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/06/18 -  ATTD -  Created method.
     *                  04/07/18 -  ATTD -  Debug method.
     */
    public static void user_frontDeskUpdateService() {
        
        try {

            // Declare variables
            ArrayList<String> validServiceNames = new ArrayList<>();
            ArrayList<String> eligibleStaffIDs = new ArrayList<>();
            String hotelID;
            String roomNum;
            String oldServiceName;
            String oldStaffID;
            String newServiceName;
            String newStaffID;
            String attributeToChange;
            String serviceRecordID;
            int stayID;
            
            // Print hotels to console so user has some context
            user_reportEntireTable("Hotels");
            
            // Get hotel ID
            System.out.print("\nEnter the hotel ID for the stay you wish to update a service record for\n> ");
            hotelID = scanner.nextLine();
            if (support_isValueSane("ID", hotelID)) {
                
                // Are there any occupied rooms in this hotel?
                jdbcPrep_getOccupiedRoomsInHotel.setInt(1, Integer.parseInt(hotelID));
                jdbc_result = jdbcPrep_getOccupiedRoomsInHotel.executeQuery();
                if (jdbc_result.next()) {
                    
                    // Print all occupied rooms in that hotel so user has some context
                    System.out.println("\nOccupied rooms in this hotel:\n");
                    jdbc_result.beforeFirst();
                    support_printQueryResultSet(jdbc_result);
                    
                    // Get room number
                    System.out.print("\nEnter the room number for the stay you wish to update a service record for\n> ");
                    roomNum = scanner.nextLine();
                    if (support_isValueSane("RoomNum", roomNum)) {

                        // Get stay ID based on room number and hotel
                        jdbcPrep_getStayByRoomAndHotel.setInt(1, Integer.parseInt(roomNum));
                        jdbcPrep_getStayByRoomAndHotel.setInt(2, Integer.parseInt(hotelID));
                        jdbc_result = jdbcPrep_getStayByRoomAndHotel.executeQuery();
                        if (jdbc_result.next()) {
                            
                            stayID = jdbc_result.getInt(1);

                            // Make sure there is actually a service record to update
                            jdbcPrep_reportStaffServingDuringStay.setInt(1, stayID); 
                            jdbc_result = jdbcPrep_reportStaffServingDuringStay.executeQuery();
                            if (jdbc_result.next()) {

                             // Print all service records for this stay so user has some context
                                System.out.println("\nServices provided during this stay:\n");
                                jdbc_result.beforeFirst();
                                support_printQueryResultSet(jdbc_result);
                                
                                // Get service record ID to update
                                System.out.println("\nEnter the ID of the service record you wish to update:\n> ");
                                serviceRecordID = scanner.nextLine();
                                if (support_isValueSane("ID", serviceRecordID)) {
                                    
                                    // Remember OLD service name and staff ID
                                    jdbcPrep_getServiceNameAndStaffByID.setInt(1, Integer.parseInt(serviceRecordID));
                                    jdbc_result = jdbcPrep_getServiceNameAndStaffByID.executeQuery();
                                    jdbc_result.next();
                                    oldServiceName = jdbc_result.getString(1);
                                    oldStaffID = jdbc_result.getString(2);
                                    
                                    // Find out what they want to update (only choices are service name and staff member)
                                    System.out.println("\nEnter 'Service' to change the service provided, or enter 'Staff' to change the staff member who provided the service:\n> ");
                                    attributeToChange = scanner.nextLine();
                                    if (attributeToChange.equalsIgnoreCase("Service")) {
                                        
                                        // Print all available services so user has some context
                                        System.out.println("\nAvailable Services:\n");
                                        user_reportEntireTable("ServiceTypes");
                                        
                                        // Get service type
                                        System.out.print("\nEnter the name of the service provided to the guest\n> ");
                                        newServiceName = scanner.nextLine();
                                        if (support_isValueSane("ServiceName", newServiceName)) {
                                            // To radically simplify update SQL, check here to make sure the user put in a good service name
                                            jdbc_result = jdbcPrep_getValidServiceNames.executeQuery();
                                            while (jdbc_result.next()) {
                                                validServiceNames.add(jdbc_result.getString(1));
                                            }
                                            if (validServiceNames.contains(newServiceName)) {
                                                // Call method that interacts with the DB
                                                db_frontDeskUpdateService(Integer.parseInt(serviceRecordID), Integer.parseInt(oldStaffID), newServiceName);
                                            }
                                            else {
                                                System.out.print("\nThis is not a valid service name (cannot proceed)\n");
                                            }
                                        }
                                        
                                    }
                                    else  if(attributeToChange.equalsIgnoreCase("Staff")) {
                                        
                                        /* Print  a bit of info about all staff eligible to provide a given service for a given room in a given hotel
                                         * Must have correct job title
                                         * Must be serving the hotel
                                         * Must not be dedicated to a different room
                                         * Indices to use when calling this prepared statement:
                                         * 1 -  service name
                                         * 2 -  service name (again)
                                         * 3 -  service name (again)
                                         * 4 -  hotel ID
                                         * 5 -  room number
                                         */
                                        System.out.print("\nStaff eligible to provide this service:\n\n");
                                        jdbcPrep_getEligibleStaffForService.setString(1, oldServiceName);
                                        jdbcPrep_getEligibleStaffForService.setString(2, oldServiceName);
                                        jdbcPrep_getEligibleStaffForService.setString(3, oldServiceName);
                                        jdbcPrep_getEligibleStaffForService.setInt(4, Integer.parseInt(hotelID));
                                        jdbcPrep_getEligibleStaffForService.setInt(5, Integer.parseInt(roomNum));
                                        jdbc_result = jdbcPrep_getEligibleStaffForService.executeQuery();
                                        support_printQueryResultSet(jdbc_result);
                                        
                                        // Get staff ID
                                        System.out.print("\nEnter the staff ID for the staff member providing the service to the guest\n> ");
                                        newStaffID = scanner.nextLine();
                                        if (support_isValueSane("StaffID", newStaffID)) {
                                            // To radically simplify update SQL, check here to make sure the user put in a good staff ID
                                            jdbc_result.beforeFirst();
                                            while (jdbc_result.next()) {
                                                eligibleStaffIDs.add(jdbc_result.getString(1));
                                            }
                                            if (eligibleStaffIDs.contains(newStaffID)) {
                                                // Call method that interacts with the DB
                                                db_frontDeskUpdateService(Integer.parseInt(serviceRecordID), Integer.parseInt(newStaffID), oldServiceName);
                                            }
                                            else {
                                                System.out.print("\nThis is not an eligible staff member (cannot proceed)\n");
                                            }
                                        }
                                        
                                    }
                                    else {
                                        System.out.print("\nThis is not a valid option (cannot proceed)\n");
                                    }
                                    
                                }
                                
                            }
                            else {
                                System.out.println("\nFor this stay, there are no service records to update!\n");
                            }
                            
                            

                            
                        }
                        else {
                            System.out.println("\nThis is not a valid hotel-room combination (cannot proceed)\n");
                        }

                    }
                    
                }
                else {
                    System.out.println("\nThere are no occupied rooms in this hotel!\n");
                }
                
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Front desk task: Check a customer out (generate itemized receipt & bill, release room)
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/11/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/05/18 -  ATTD -  Improve user friendliness (help user find a stay ID to bill for).
     *                                      Roll in release of room (written by Manjusha) for streamlined check-out process.
     */
    public static void user_frontDeskCheckOut() {
        
        try {

            // Declare variables
            String hotelID;
            String roomNum;
            int stayID;
            
            // Print hotels to console so user has some context
            user_reportEntireTable("Hotels");
            
            // Get hotel ID
            System.out.print("\nEnter the hotel ID for the hotel you wish to check a customer out of\n> ");
            hotelID = scanner.nextLine();
            if (support_isValueSane("ID", hotelID)) {
                
                // Are there any occupied rooms in this hotel?
                jdbcPrep_getOccupiedRoomsInHotel.setInt(1, Integer.parseInt(hotelID));
                jdbc_result = jdbcPrep_getOccupiedRoomsInHotel.executeQuery();
                if (jdbc_result.next()) {
                    
                    // Print all occupied rooms in that hotel so user has some context
                    System.out.println("\nOccupied rooms in this hotel:\n");
                    jdbc_result.beforeFirst();
                    support_printQueryResultSet(jdbc_result);
                    
                    // Get room number
                    System.out.print("\nEnter the room number you wish to check a customer out of\n> ");
                    roomNum = scanner.nextLine();
                    if (support_isValueSane("RoomNum", roomNum)) {

                        /* Get stay ID based on room number and hotel BEFORE releasing room
                         * (could be several stays with same room / hotel, we want unreleased stay)
                         */
                        jdbcPrep_getStayByRoomAndHotel.setInt(1, Integer.parseInt(roomNum));
                        jdbcPrep_getStayByRoomAndHotel.setInt(2, Integer.parseInt(hotelID));
                        jdbc_result = jdbcPrep_getStayByRoomAndHotel.executeQuery();
                        if (jdbc_result.next()) {
                            
                            stayID = jdbc_result.getInt(1);
                            
                            // AFTER getting the stay ID (needs room to be UNreleased), release the room so another customer can use it
                            db_frontDeskReleaseRoom(hotelID, roomNum);
                            
                            // Create itemized receipt & bill, print all info to console
                            db_frontDeskItemizedReceipt(stayID, true);
                            
                        }
                        else {
                            System.out.println("\nThis is not a valid hotel-room combination (cannot proceed)\n");
                        }

                    }
                    
                }
                else {
                    System.out.println("\nThere are no occupied rooms in this hotel!\n");
                }
                
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    // USER INTERACTION METHODS: REPORTS MENU
    
    /** 
     * Report task: Report revenue earned by a hotel over a date range
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/11/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new attribute / value sanity checking method.
     *                                      Use new general error handler.
     */
    public static void user_reportHotelRevenue() {

        try {
            
            // Declare local variables
            int hotelID;
            String hotelIdAsString = "";
            String startDate = "";
            String endDate = "";
            
            // Get name
            System.out.print("\nEnter the hotel ID\n> ");
            hotelIdAsString = scanner.nextLine();
            if (support_isValueSane("ID", hotelIdAsString)) {
                hotelID = Integer.parseInt(hotelIdAsString);
                // Get start date
                System.out.print("\nEnter the start date\n> ");
                startDate = scanner.nextLine();
                if (support_isValueSane("StartDate", startDate)) {
                    // Get end date
                    System.out.print("\nEnter the end date\n> ");
                    endDate = scanner.nextLine();
                    if (support_isValueSane("EndDate", endDate)) {
                        // Call method to actually interact with the DB
                        db_reportHotelRevenue(hotelID, startDate, endDate);
                    }
                }
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
        
    /** 
     * Report task: Report occupancy by hotel
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportOccupancyByHotel() {

        try {
                        
        	jdbc_result = jdbcPrep_reportOccupancyByHotel.executeQuery();
			 
            // Print result
            System.out.println("\nReporting occupancy By Hotel:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
                        
    }
    
    /** 
     * Report task: Report occupancy by room type
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportOccupancyByRoomType() {

        try {
                        
        	jdbc_result = jdbcPrep_reportOccupancyByRoomType.executeQuery();
			 
            // Print result
            System.out.println("\nReporting occupancy By Room Type:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
                        
    }
    
    /** 
     * Report task: Report occupancy by date range
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportOccupancyByDateRange() {
  
    	try { 
    		  
    		String startDate = support_getValidDataFromUser("REPORT_BY_DATE_RANGE", "StartDate", "Enter the start date (in format YYYY-MM-DD)");
			if (!startDate.equalsIgnoreCase("<QUIT>")) {
				
				String endDate = support_getValidDataFromUser("REPORT_BY_DATE_RANGE", "EndDate", "Enter the end date (in format YYYY-MM-DD)", startDate);
				if (!endDate.equalsIgnoreCase("<QUIT>")) { 
					
					// Get the report data and display the results
					db_reportOccupancyByDateRange(startDate, endDate);
					
				}                		                	
			}                                	
    	}		 
        catch (Throwable err) {
            error_handler(err);
        }
         
    }
    
    /** 
     * Report task: Report occupancy by city
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportOccupancyByCity() {
  
    	try {
            
        	jdbc_result = jdbcPrep_reportOccupancyByCity.executeQuery();
			 
            // Print result
            System.out.println("\nReporting occupancy By City:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
       
    }
        
    /** 
     * Report task: Report total occupancy
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportTotalOccupancy() {
  
    	try {
            
        	jdbc_result = jdbcPrep_reportTotalOccupancy.executeQuery();
			 
            // Print result
            System.out.println("\nReporting total occupancy:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
       
    }
    
    /** 
     * Report task: Report percentage of rooms occupied
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportPercentageOfRoomsOccupied() {
  
    	try {
            
        	jdbc_result = jdbcPrep_reportPercentageOfRoomsOccupied.executeQuery();
			 
            // Print result
            System.out.println("\nReporting percentage of rooms occupied:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
       
    }
    
    /** 
     * Report task: Report staff grouped by role
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportStaffGroupedByRole() {
  
    	try {
            
        	jdbc_result = jdbcPrep_reportStaffGroupedByRole.executeQuery();
			 
            // Print result
            System.out.println("\nReporting staff grouped by their role:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
       
    }
    
    /** 
     * Report task: Report staff serving the customer during stay
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void user_reportStaffServingDuringStay() {
  
    	try { 
  		  
    		String stayId = support_getValidDataFromUser("REPORT_STAFF_BY_STAY", "StayID", "Enter the stay id");
			if (!stayId.equalsIgnoreCase("<QUIT>")) {
				
				// Get the report data and display the results
				db_reportStaffServingDuringStay(stayId);
				               		                	
			}                                	
    	}		 
        catch (Throwable err) {
            error_handler(err);
        }
       
    }
    
    /** 
     * Report task: Report all results from a given table
     * 
     * Arguments -  tableName - The table to print out
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/04/18 -  ATTD -  For Rooms table, also print out whether the room is available.
     *                                      For Staff table, also print out staff member's age.
     *                                      For Stays table, also print out customer's SSN.
     *                                      This is to more easily demonstrate that the demo data was populated correctly.
     */
    public static void user_reportEntireTable(String tableName) {

        try {

            // Report entire table (no transaction needed for a query)
            System.out.println("\nEntries in the " + tableName + " table:\n");
            
            /* Special printing for Rooms table
             * Also print out whether the room is available.
             * This is to more easily demonstrate that the demo data was populated correctly.
             * While you're at it, print ordered by hotel ID because the output makes more sense that way.
             */  
            if (tableName.equalsIgnoreCase("Rooms")) {
                jdbc_result = jdbcPrep_reportTableRooms.executeQuery();
            }
            /* Special printing for Staff table
             * Also print out staff member's age.
             * This is to more easily demonstrate that the demo data was populated correctly.
             * While you're at it, print ordered by hotel ID because the output makes more sense that way.
             */  
            else if (tableName.equalsIgnoreCase("Staff")) {
                jdbc_result = jdbcPrep_reportTableStaff.executeQuery();
            }
            /* Special printing for Stays table
             * Also print out customer's SSN.
             * This is to more easily demonstrate that the demo data was populated correctly.
             * While you're at it, print ordered by hotel ID because the output makes more sense that way.
             */  
            else if (tableName.equalsIgnoreCase("Stays")) {
                jdbc_result = jdbcPrep_reportTableStays.executeQuery();
            }
            // Other tables have no special printing needs
            else {
                jdbc_result = jdbc_statement.executeQuery("SELECT * FROM " + tableName);
            }
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    // USER INTERACTION METHODS: MANAGE MENU
    
    /** 
     * Management task: Add a new hotel
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/08/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new attribute / value sanity checking method.
     *                                      Use new general error handler.
     *                  04/04/18 -  ATTD -  Add attribute for hotel zip code, per demo data.
     *                  04/13/18 -  ATTD -  Give user some help in finding staff to promote to manager of new hotel.
     */
    public static void user_manageHotelAdd() {

        try {
            
            // Declare local variables
            String hotelName = "";
            String streetAddress = "";
            String city = "";
            String state = "";
            String zipAsString = "";
            String phoneNumAsString = "";
            String managerIdAsString = "";
            long phoneNum = 0;
            int managerID = 0;
            int zip = 0;
            
            // Get name
            System.out.print("\nEnter the hotel name\n> ");
            hotelName = scanner.nextLine();
            if (support_isValueSane("Name", hotelName)) {
                // Get street address
                System.out.print("\nEnter the hotel's street address\n> ");
                streetAddress = scanner.nextLine();
                if (support_isValueSane("StreetAddress", streetAddress)) {
                    // Get city
                    System.out.print("\nEnter the hotel's city\n> ");
                    city = scanner.nextLine();
                    if (support_isValueSane("City", city)) {
                        // Get state
                        System.out.print("\nEnter the hotel's state\n> ");
                        state = scanner.nextLine();
                        if (support_isValueSane("State", state)) {
                            // Get zip code
                            System.out.print("\nEnter the hotel's zip code\n> ");
                            zipAsString = scanner.nextLine();
                            if (support_isValueSane("Zip", zipAsString)) {
                                zip = Integer.parseInt(zipAsString);
                                // Get phone number
                                System.out.print("\nEnter the hotel's phone number\n> ");
                                phoneNumAsString = scanner.nextLine();
                                if (support_isValueSane("PhoneNum", phoneNumAsString)) {
                                    phoneNum = Long.parseLong(phoneNumAsString);
                                    // Get manager (first, print staff members to give the user some context)
                                    user_reportEntireTable("Staff");
                                    System.out.print("\nEnter the hotel's manager's staff ID\n> ");
                                    managerIdAsString = scanner.nextLine();
                                    if (support_isValueSane("ManagerID", managerIdAsString)) {
                                        managerID = Integer.parseInt(managerIdAsString);
                                        // Okay, at this point everything else I can think of can be caught by a Java exception or a SQL exception
                                        db_manageHotelAdd(hotelName, streetAddress, city, state, zip, phoneNum, managerID, true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Management task: Change information about a hotel
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/23/18 -  ATTD -  Created method.
     *                  03/17/18 -  ATTD -  Fix copy-paste error keeping proposed values from being sanity-checked.
     *                  03/28/18 -  ATTD -  Stop immediately if invalid hotel ID is entered.
     *                  04/13/18 -  ATTD -  If the user wants to change the manager ID, give them some context.
     */
    public static void user_manageHotelUpdate() {

        try {

            // Declare local variables
            String hotelIdAsString = "";
            String attributeToChange = "";
            String valueToChangeTo;
            int hotelID = 0;
            boolean userWantsToStop = false;
            boolean hotelFound = false;
            
            // Print hotels to console so user has some context
            user_reportEntireTable("Hotels");
            
            // Get hotel ID
            System.out.print("\nEnter the hotel ID for the hotel you wish to make changes for\n> ");
            hotelIdAsString = scanner.nextLine();
            if (support_isValueSane("ID", hotelIdAsString)) {
                hotelID = Integer.parseInt(hotelIdAsString);
                // Print just that hotel to console so user has some context
                hotelFound = db_manageShowHotelByID(hotelID);
                if (hotelFound) {
                    
                    // Keep updating values until the user wants to stop
                    while (userWantsToStop == false) {
                        // Get name of attribute they want to change
                        System.out.print("\nEnter the name of the attribute you wish to change (or press <Enter> to stop)\n> ");
                        attributeToChange = scanner.nextLine();
                        if (support_isValueSane("AnyAttr", attributeToChange)) {
                            // If the user wants to change the manager ID, give them some context
                            if (attributeToChange.equalsIgnoreCase("ManagerID")) {
                                user_reportEntireTable("Staff");
                            }
                            // Get value they want to change the attribute to
                            System.out.print("\nEnter the value you wish to change this attribute to (or press <Enter> to stop)\n> ");
                            valueToChangeTo = scanner.nextLine();
                            if (support_isValueSane(attributeToChange, valueToChangeTo)) {
                                // Okay, at this point everything else I can think of can be caught by a Java exception or a SQL exception
                                db_manageHotelUpdate(hotelID, attributeToChange, valueToChangeTo);
                            }
                            else {
                                userWantsToStop = true;
                            }
                        }
                        else {
                            userWantsToStop = true;
                        }
                    }
                    // Report results of all the updates
                    db_manageShowHotelByID(hotelID);
                    
                }

            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Management task: Delete a hotel
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/09/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/24/18 -  ATTD -  Provide more context for user.
     *                                      Change hotel ID from long to int.
     */
    public static void user_manageHotelDelete() {

        try {
            
            // Declare local variables
            int hotelID = 0;
            
            // Print hotels to console so user has some context
            user_reportEntireTable("Hotels");
            
            // Get ID of hotel to delete
            System.out.print("\nEnter the hotel ID for the hotel you wish to delete\n> ");
            hotelID = Integer.parseInt(scanner.nextLine());

            // Call method to actually interact with the DB
            db_manageHotelDelete(hotelID, true);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /**
     * Management task: Add a room
     * 
     * Arguments -  None
     * Returns -    None
     * 
     * Modifications:   03/24/18 -  MTA -   Added functionality to add new room
     * 				    03/25/18 -  MTA -   Fix the category type
     *                  03/28/18 -  ATTD -  Use same field names as in tables themselves
     *                                      - for developer ease
     *                                      - because "support_isValueSane" method uses table attribute names
     *                  04/04/18 -  ATTD -  Changing room categories to match those given in demo data.
     *                  04/13/18 -  ATTD -  Give the user some context for picking a hotel ID.
     */
    public static void user_manageRoomAdd() {
    	
    	try { 
    		
    	    user_reportEntireTable("Hotels");
    		String hotelId = support_getValidDataFromUser("ADD_ROOM", "HotelId", "Enter the hotel id for which you are adding new room");    	
    		if (!hotelId.equalsIgnoreCase("<QUIT>")) { 
    			
    			String roomNumber = support_getValidDataFromUser("ADD_ROOM", "RoomNum", "Enter the room number", hotelId); 
    			if (!roomNumber.equalsIgnoreCase("<QUIT>")) { 
    				
    				String category = support_getValidDataFromUser(
	    		        "ADD_ROOM",
	    		        "Category", 
	    		        "Enter the room's category.\nAvailable options are 'Economy', 'Deluxe', 'Executive', 'Presidential'"
	    	        );
    				if (!category.equalsIgnoreCase("<QUIT>")) { 
    					
    					String maxOccupancy = support_getValidDataFromUser("ADD_ROOM","MaxOcc", "Enter the room's maximum occupancy"); 
    					if (!maxOccupancy.equalsIgnoreCase("<QUIT>")) { 
    						
    						String nightlyRate = support_getValidDataFromUser("ADD_ROOM", "NightlyRate", "Enter the room's nightly rate"); 
    						if (!nightlyRate.equalsIgnoreCase("<QUIT>")) { 
    							
    							db_manageRoomAdd(Integer.parseInt(roomNumber), Integer.parseInt(hotelId), category, Integer.parseInt(maxOccupancy), Integer.parseInt(nightlyRate), true);
    						}        	                
    					}    					    	        	
    				}	                   	        	
    			}        		        	
    		} 
        }
        catch (Throwable err) {
            error_handler(err);
        }
    } 
    
    /**
     * Management task: Update room details
     * 
     * Modifications:   03/24/18 -  MTA -   Added functionality to update room details
     *                  03/28/18 -  ATTD -  Use same field names as in tables themselves
     *                                      - for developer ease
     *                                      - because "support_isValueSane" method uses table attribute names
     *                  04/04/18 -  ATTD -  Changing room categories to match those given in demo data.
     *                  04/13/18 -  ATTD -  Do not force room category to be upper case.
     */
    public static void user_manageRoomUpdate() {
    	try {
    		boolean userWantsToStop = false; 
    		 
            // Print hotels to console so user has some context
            user_reportEntireTable("Rooms");
            
            String hotelId = support_getValidDataFromUser("UPDATE_ROOM", "HotelId", "Enter the hotel ID for the room you wish to make changes for");
            if (!hotelId.equalsIgnoreCase("<QUIT>")) { 
            	
            	String roomNumber = support_getValidDataFromUser("UPDATE_ROOM", "RoomNum", "Enter the room number you wish to make changes for", hotelId);
            	if (!roomNumber.equalsIgnoreCase("<QUIT>")) { 
            	
            		db_manageShowRoomByHotelIdRoomNum(Integer.parseInt(hotelId), Integer.parseInt(roomNumber));
                    
                    while(!userWantsToStop) { 
                    	
                    	// Get the attribute the user wants to update
                        System.out.print("\nChoose the attribute you wish to change\n1. Room Category\n2. Max Occupancy\n3. Nightly Rate\n4. Exit\n> ");
                        int attributeToChange = Integer.parseInt(scanner.nextLine());
                    	
                    	switch(attributeToChange){
        	             	case 1:
        	             		String category = support_getValidDataFromUser(
                     		        "UPDATE_ROOM",
                     		        "Category", 
                     		        "Enter the new value for room's category.\nAvailable options are 'Economy', 'Deluxe', 'Executive', 'Presidential'"
                 		        );
        	             		if (!category.equalsIgnoreCase("<QUIT>")) { 
        	             			db_manageRoomUpdate(Integer.parseInt(roomNumber), Integer.parseInt(hotelId), "Category", category, true);
        	             		} else
        	             			userWantsToStop = true;
        	             		break;
        	             	case 2:
        	             		String maxOccupancy = support_getValidDataFromUser("UPDATE_ROOM","MaxOcc", "Enter the new value for room's maximum occupancy");
        	             		if (!maxOccupancy.equalsIgnoreCase("<QUIT>")) {
        	             			db_manageRoomUpdate(Integer.parseInt(roomNumber), Integer.parseInt(hotelId), "MaxOcc", maxOccupancy, true);
        	             		}
        	             		else
        	             			userWantsToStop = true;
        	             		break;
        	             	case 3:
        	             		String nightlyRate = support_getValidDataFromUser("UPDATE_ROOM", "NightlyRate", "Enter the new value for room's nightly rate");
        	             		if (!nightlyRate.equalsIgnoreCase("<QUIT>")) {
        	             			db_manageRoomUpdate(Integer.parseInt(roomNumber), Integer.parseInt(hotelId), "NightlyRate", nightlyRate, true);
        	             		}
        	             		else
        	             			userWantsToStop = true;
        	             		break;
        	             	case 4:
        	             		userWantsToStop = true;
        	             		break;
        	             	default: System.out.println("Please choose a number between 1 to 4"); 
        	            } 
                    } 
                        
                    // Report results of all the updates
                    db_manageShowRoomByHotelIdRoomNum(Integer.parseInt(hotelId), Integer.parseInt(roomNumber));
            	}                               
            }            
        }
        catch (Throwable err) {
            error_handler(err);
        }
    }
    
    /**
     * Management task: Delete a room
     * 
     * Modifications:   03/24/18 -  MTA -   Added functionality to delete room
     *                  03/28/18 -  ATTD -  Use same field names as in tables themselves
     *                                      - for developer ease
     *                                      - because "support_isValueSane" method uses table attribute names
     */
    public static void user_manageRoomDelete() {
    	
    	try { 
             
            // Print hotels to console so user has some context
            user_reportEntireTable("Rooms");
            
            // Get hotel ID and room number to be deleted
            String hotelId = support_getValidDataFromUser("DELETE_ROOM", "HotelId", "Enter the hotel ID for the room you wish to delete"); 
            if (!hotelId.equalsIgnoreCase("<QUIT>")) {
            	
            	String roomNumber = support_getValidDataFromUser("DELETE_ROOM", "RoomNum", "Enter the room number you wish to delete", hotelId);
            	if (!roomNumber.equalsIgnoreCase("<QUIT>")) {
            		
            		// Call method to actually interact with the DB
                    db_manageRoomDelete(Integer.parseInt(hotelId), Integer.parseInt(roomNumber), true);
            	}                
            }    		           
        }
        catch (Throwable err) {
            error_handler(err);
        }
    	
    } 
    
    /**
     * Management task: Add new customer
     * 
     * Modifications:   03/27/18 -  MTA -   Added method
     *                  03/28/18 -  ATTD -  Use same field names as in tables themselves
     *                                      - for developer ease
     *                                      - because "support_isValueSane" method uses table attribute names
     */
    public static void user_manageCustomerAdd() {
    	
    	try { 
  		  
    		String ssn = support_getValidDataFromUser("ADD_CUSTOMER", "SSN", "Enter the customer's SSN");
    		if (!ssn.equalsIgnoreCase("<QUIT>")) {
    			
    			String name = support_getValidDataFromUser("ADD_CUSTOMER", "Name", "Enter the customer's name"); 
    			if (!name.equalsIgnoreCase("<QUIT>")) {
    				
    				String dob = support_getValidDataFromUser("ADD_CUSTOMER", "DOB", "Enter the customer's Date Of Birth (in format YYYY-MM-DD)");
    				if (!dob.equalsIgnoreCase("<QUIT>")) {
    					
    					String phoneNumber = support_getValidDataFromUser("ADD_CUSTOMER", "PhoneNum", "Enter the customer's phone number"); 
    					if (!phoneNumber.equalsIgnoreCase("<QUIT>")) {
    						
    						String email = support_getValidDataFromUser("ADD_CUSTOMER", "Email", "Enter the customer's email");                             
    						if (!email.equalsIgnoreCase("<QUIT>")) {
    							db_manageCustomerAdd(ssn, name, dob, phoneNumber, email, true);
    						}                            
    					}                		                	
    				}                                	
    			}        		        	
    		}    		          
        }
        catch (Throwable err) {
            error_handler(err);
        }
    	
    } 
    
    /**
     * Management task: Update customer
     * 
     * Modifications:   03/27/18 -  MTA -   Added method.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static void user_manageCustomerUpdate() {
    	
    	try { 

    		boolean userWantsToStop = false; 
    		 
            // Print all customers to console so user has some context
            user_reportEntireTable("Customers");
            
            String customerID = support_getValidDataFromUser("UPDATE_CUSTOMER", "ID", "Enter the ID for the customer you wish to make changes for");
            if (!customerID.equalsIgnoreCase("<QUIT>")) {
            	
            	db_manageShowCustomerByID(customerID);
                
                while(!userWantsToStop) { 
                	
                	// Get the attribute the user wants to update
                    System.out.print("\nChoose the attribute you wish to change\n1. SSN\n2. Name\n3. Date Of Birth\n4. Phone Number\n5. Email\n6. Exit\n> ");
                    int attributeToChange = Integer.parseInt(scanner.nextLine());
                	
                	switch(attributeToChange){
                        case 1:
                            String customerSSN = support_getValidDataFromUser("UPDATE_CUSTOMER","SSN", "Enter the new value for customer's SSN");
                            if (!customerSSN.equalsIgnoreCase("<QUIT>")) {
                            	db_manageCustomerUpdate(customerID, "SSN", customerSSN, true);
                            } else
                            	userWantsToStop = true;
                            break;
    	             	case 2:
    	             		String customerName = support_getValidDataFromUser("UPDATE_CUSTOMER","Name", "Enter the new value for customer's name");
    	             		if (!customerName.equalsIgnoreCase("<QUIT>")) {
    	             			db_manageCustomerUpdate(customerID, "Name", customerName, true);
    	             		} else
    	             			userWantsToStop = true;
    	             		break;
    	             	case 3:
    	             		String dob = support_getValidDataFromUser("UPDATE_CUSTOMER","DOB", "Enter the new value for customer's date of birth");
    	             		if (!dob.equalsIgnoreCase("<QUIT>")) {
    	             			db_manageCustomerUpdate(customerID, "DOB", dob, true);
    	             		} else 
    	             			userWantsToStop = true;
    	             		break;
    	             	case 4:
    	             		String phoneNumber = support_getValidDataFromUser("UPDATE_CUSTOMER", "PhoneNum", "Enter the new value for customer's phone number");
    	             		if (!phoneNumber.equalsIgnoreCase("<QUIT>")) {
    	             			db_manageCustomerUpdate(customerID, "PhoneNum", phoneNumber, true);
    	             		} else
    	             			userWantsToStop = true;
    	             		break;
    	             	case 5:
    	             		String email = support_getValidDataFromUser("UPDATE_CUSTOMER", "Email", "Enter the new value for customer's email");
    	             		if (!email.equalsIgnoreCase("<QUIT>")) {
    	             			db_manageCustomerUpdate(customerID, "Email", email, true);
    	             		} else
    	             			userWantsToStop = true;
    	             		break;
    	             	case 6:
    	             		userWantsToStop = true;
    	             		break;
    	             	default: System.out.println("Please choose a number between 1 and 6"); 
    	            } 
                } 
                    
                // Report results of all the updates
                db_manageShowCustomerByID(customerID); 
                       
            }                          
        }
        catch (Throwable err) {
            error_handler(err);
        }
    	
    } 
    
    /**
     * Management task: Delete a customer
     * 
     * Modifications:   03/27/18 -  MTA -   Added method.
     *                  04/04/18 -  ATTD -  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static void user_manageCustomerDelete() {
    	
    	try { 
            
            // Print all customers to console so user has some context
            user_reportEntireTable("Customers");
            
            // Get ID of the customer to be deleted
            String ID = support_getValidDataFromUser("DELETE_CUSTOMER", "ID", "Enter the ID for the customer you wish to delete"); 
            if (!ID.equalsIgnoreCase("<QUIT>")) {
            	// Call method to actually interact with the DB
                db_manageCustomerDelete(ID, true); 
            }
                   
        }
        catch (Throwable err) {
            error_handler(err);
        }
    	
    } 
    
    /** 
     * Management task: Add a new staff member
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/24/18 -  ATTD -  Created method.
     *                  04/13/18 -  ATTD -  Give the user some context to help them pick a hotel ID for the new staff member.
     */
    public static void user_manageStaffAdd() {

        try {
            
            // Declare local variables
            String name = "";
            String dob = "";
            String jobTitle = "";
            String department = "";
            String phoneNumAsString = "";
            String address = "";
            String hotelIdAsString = "";
            long phoneNum = 0;
            int hotelID = 0;
            
            // Get name
            System.out.print("\nEnter the new staff member's name\n> ");
            name = scanner.nextLine();
            if (support_isValueSane("Name", name)) {
                // Get date of birth
                System.out.print("\nEnter the new staff member's date of birth\n> ");
                dob = scanner.nextLine();
                if (support_isValueSane("DOB", dob)) {
                    // Get job title
                    System.out.print("\nEnter the new staff member's job title\n> ");
                    jobTitle = scanner.nextLine();
                    if (support_isValueSane("JobTitle", jobTitle)) {
                        if(jobTitle.equalsIgnoreCase("Manager")) {
                            System.out.println("You cannot enter a new manager who will not initally be associated with any hotel");
                        }
                        else
                        {
                            // Get department
                            System.out.print("\nEnter the new staff member's department\n> ");
                            department = scanner.nextLine();
                            if (support_isValueSane("Dep", department)) {
                                // Get phone number
                                System.out.print("\nEnter the new staff member's phone number\n> ");
                                phoneNumAsString = scanner.nextLine();
                                if (support_isValueSane("PhoneNum", phoneNumAsString)) {
                                    phoneNum = Long.parseLong(phoneNumAsString);
                                    // Get address
                                    System.out.print("\nEnter the new staff member's full address\n> ");
                                    address = scanner.nextLine();
                                    if (support_isValueSane("Address", address)) {
                                        // Give the user some context
                                        user_reportEntireTable("Hotels");
                                        // Get hotel ID
                                        System.out.print("\nEnter the new staff member's hotel ID (or press <Enter> if they are not assigned to any particular hotel)\n> ");
                                        hotelIdAsString = scanner.nextLine();
                                        hotelID = hotelIdAsString.length() == 0 ? 0 : Integer.parseInt(hotelIdAsString);
                                        // Okay, at this point everything else I can think of can be caught by a Java exception or a SQL exception
                                        db_manageStaffAdd(name, dob, jobTitle, department, phoneNum, address, hotelID, true);
                                    }
                                }
                            }
                        }
                        
                    }
                }
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Management task: Change information about a staff member
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/26/18 -  ATTD -  Created method.
     *                  03/28/18 -  ATTD -  Stop immediately if invalid staff ID entered.
     */
    public static void user_manageStaffUpdate() {

        try {

            // Declare local variables
            String staffIdAsString = "";
            String attributeToChange = "";
            String valueToChangeTo;
            int staffID = 0;
            boolean userWantsToStop = false;
            boolean staffFound = false;
            String JobTitle = "";

            ResultSet rs = jdbcPrep_getDedicatedStaffMembers.executeQuery();
            HashSet<Integer> hash = new HashSet<Integer>();
            while(rs.next())
            {
                hash.add(rs.getInt("StaffID"));
            }
            // Print staff to console so user has some context
            user_reportEntireTable("Staff");
            
            // Get hotel ID
            System.out.print("\nEnter the staff ID for the staff member you wish to make changes for\n> ");
            staffIdAsString = scanner.nextLine();
            if (support_isValueSane("ID", staffIdAsString)) {
                staffID = Integer.parseInt(staffIdAsString);
                jdbcPrep_getJobTitlebyID.setInt(1, staffID);
                ResultSet rs2 = jdbcPrep_getJobTitlebyID.executeQuery();
                while(rs2.next())
                {
                    JobTitle = rs2.getString("JobTitle");
                }
                // Print just that staff member to console so user has some context
                staffFound = db_manageShowStaffByID(staffID);
                if (staffFound) {
                    
                    // Keep updating values until the user wants to stop
                    while (userWantsToStop == false) {
                        // Get name of attribute they want to change
                        System.out.print("\nEnter the name of the attribute you wish to change (or press <Enter> to stop)\n> ");
                        attributeToChange = scanner.nextLine();
                        if(hash.contains(staffID) && (attributeToChange.equalsIgnoreCase("JobTitle") || attributeToChange.equalsIgnoreCase("HotelID")))
                        {
                            System.out.println("You cannot change HotelID and Job Title of staff currently dedicated to a presidential suite");
                        }
                        else if (support_isValueSane("AnyAttr", attributeToChange)) {
                            // Get value they want to change the attribute to
                            System.out.print("\nEnter the value you wish to change this attribute to (or press <Enter> to stop)\n> ");
                            valueToChangeTo = scanner.nextLine();
                            if (support_isValueSane(attributeToChange, valueToChangeTo)) {
                                if(attributeToChange.equalsIgnoreCase("JobTitle") && valueToChangeTo.equalsIgnoreCase("Manager"))
                                {
                                    System.out.println(
                                        "\nYou cannot directly promote a staff member to manager (because every manager must manage a hotel)." + 
                                        "\nTo do this, you may update the hotel info and change manager ID."
                                    );
                                }
                                else
                                {
                                    if(JobTitle.equals("Manager") && (attributeToChange.equalsIgnoreCase("JobTitle") || attributeToChange.equals("HotelID")))
                                    {
                                        System.out.println(
                                            "\nYou cannot update Job Title and Hotel ID of an exisiting manager from here." + 
                                            "\nYou need to update existing manager of the hotel from updatehotel section."
                                        );
                                    }
                                    else
                                    {
                                        // Okay, at this point everything else I can think of can be caught by a Java exception or a SQL exception
                                        db_manageStaffUpdate(staffID, attributeToChange, valueToChangeTo);
                                    }
                                    
                                }
                                
                            }
                            else {
                                userWantsToStop = true;
                            }
                        }
                        else {
                            userWantsToStop = true;
                        }
                    }
                    // Report results of all the updates
                    db_manageShowStaffByID(staffID);
                    
                }

            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Management task: Delete a staff member
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/12/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/27/18 -  ATTD -  Provide more context for user.
     *                                      Change staff ID from long to int.
     */
    public static void user_manageStaffDelete() {

        try {
            
            // Declare local variables
            int staffID = 0;
            
            // Print staff members to console so user has some context
            user_reportEntireTable("Staff");
            
            // Get ID of staff members to delete
            System.out.print("\nEnter the staff ID for the staff member you wish to delete\n> ");
            staffID = Integer.parseInt(scanner.nextLine());

            // Call method to actually interact with the DB
            db_manageStaffDelete(staffID, true);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }

    /** 
     * Management task: Update the cost of a service
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/07/18 -  AS -    Created method.
     *                  04/07/18 -  ATTD -  Slight tweak (print service costs table before and after).
     */
    public static void user_manageUpdateServiceCost() {
        try {
            int newCost = 0;
            /*
            Query: "Update ServiceTypes SET Cost = ? WHERE Name = ?";
            1: Cost
            2: ServiceName
            */
            int flag = 0;
            while(flag == 0)
            {
                user_reportEntireTable("ServiceTypes");
                String enteredServiceName = "";
                System.out.println("\nEnter the name of the service for which you wish to update cost\n> ");
                enteredServiceName = scanner.nextLine();
                if (
                    enteredServiceName.equalsIgnoreCase("Gym") ||
                    enteredServiceName.equalsIgnoreCase("Phone") ||
                    enteredServiceName.equalsIgnoreCase("Room Service") ||
                    enteredServiceName.equalsIgnoreCase("Dry Cleaning") ||
                    enteredServiceName.equalsIgnoreCase("Special Request") ||
                    enteredServiceName.equalsIgnoreCase("Catering")
                )
                {
                    System.out.println("\nPlease enter the new cost for " + enteredServiceName + " service\n> ");
                    String temp = scanner.nextLine();
                    newCost = Integer.parseInt(temp);
                    jdbcPrep_updateServiceCost.setInt(1, newCost);
                    jdbcPrep_updateServiceCost.setString(2, enteredServiceName);
                    jdbcPrep_updateServiceCost.executeUpdate();
                    flag = 1;
                }
                else
                {
                    System.out.println("\nPlease enter the correct input\n");
                }
            }
            user_reportEntireTable("ServiceTypes");
        }
        catch(Throwable err) {
            error_handler(err);
        }
    }
    
    // DATABASE INTERACTION METHODS: POPULATION OF TABLES
    
    /** 
     * DB Update: Insert Stay (with no user interaction, safety checks) - for mass population
     * 
     * Arguments -  startDate -         The start date of the guest stay
     *              checkInTime -       The start time of the guest stay
     *              roomNum -           The room the customer will stay in
     *              hotelID -           The hotel the customer will stay in
     *              customerID -        The customer's ID
     *              numGuests -         The number of guests staying in the room
     *              checkOutTime -      The end time of the guest stay (or blank string if stay is ongoing)
     *              endDate -           The end date of the guest stay (or blank string if stay is ongoing)
     *              paymentMethod -     The method of payment (card, etc)
     *              cardType -          The type of card (or blank string if not paying with card)
     *              cardNumber -        The card number (or -1 if not paying with card)
     *              billingAddress -    The billing address (or blank string if not paying with card)
     * Return -     None
     * 
     * Modifications:   04/04/18 -  ATTD -  Created method.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static void db_populateInsertStay (
        String startDate,
        String checkInTime,
        int roomNum, 
        int hotelID, 
        int customerID,
        int numGuests, 
        String checkOutTime,
        String endDate,
        String paymentMethod, 
        String cardType, 
        long cardNumber, 
        String billingAddress
    ) {

        try {
            
            
            /* Insert a new stay (in a dumb, no-safety-check way suitable for mass population)
             * Indices to use when calling this prepared statement:
             * 1 -  start date
             * 2 -  check in time
             * 3 -  room number
             * 4 -  hotel ID
             * 5 -  customer ID
             * 6 -  number of guests
             * 7 -  check out time
             * 8 -  end date
             * 9 -  payment method
             * 10 - card type
             * 11 - card number
             * 12 - billing address
             */
            jdbcPrep_addStayNoSafetyChecks.setDate(1, java.sql.Date.valueOf(startDate));
            jdbcPrep_addStayNoSafetyChecks.setTime(2, java.sql.Time.valueOf(checkInTime));
            jdbcPrep_addStayNoSafetyChecks.setInt(3, roomNum);
            jdbcPrep_addStayNoSafetyChecks.setInt(4, hotelID);
            jdbcPrep_addStayNoSafetyChecks.setInt(5, customerID);
            jdbcPrep_addStayNoSafetyChecks.setInt(6, numGuests);
            if (checkOutTime.length() > 0 && endDate.length() > 0) {
                jdbcPrep_addStayNoSafetyChecks.setTime(7, java.sql.Time.valueOf(checkOutTime));
                jdbcPrep_addStayNoSafetyChecks.setDate(8, java.sql.Date.valueOf(endDate));
            }
            else {
                jdbcPrep_addStayNoSafetyChecks.setNull(7, java.sql.Types.TIME);
                jdbcPrep_addStayNoSafetyChecks.setNull(8, java.sql.Types.DATE);
            }
            jdbcPrep_addStayNoSafetyChecks.setString(9, paymentMethod);
            if (paymentMethod.equals("CARD")) {
                jdbcPrep_addStayNoSafetyChecks.setString(10, cardType);
                jdbcPrep_addStayNoSafetyChecks.setLong(11, cardNumber);
                jdbcPrep_addStayNoSafetyChecks.setString(12, billingAddress);
            }
            else {
                jdbcPrep_addStayNoSafetyChecks.setNull(10, java.sql.Types.VARCHAR);
                jdbcPrep_addStayNoSafetyChecks.setNull(11, java.sql.Types.BIGINT);
                jdbcPrep_addStayNoSafetyChecks.setNull(12, java.sql.Types.VARCHAR);
            }
            jdbcPrep_addStayNoSafetyChecks.executeUpdate();
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
   
    // DATABASE INTERACTION METHODS: FRONT DESK MENU
    
    /** 
     * DB Update: Insert Stay
     * 
     * Arguments -  roomNum -           The room the customer will stay in
     *              hotelID -           The hotel the customer will stay in
     *              customerID -        The customer's ID
     *              numGuests -         The number of guests staying in the room
     *              paymentMethod -     The method of payment (card, etc).
     *              cardType -          The type of card (or blank string if not paying with card)
     *              cardNumber -        The card number (or -1 if not paying with card)
     *              billingAddress -    The billing address (or blank string if not paying with card)
     *              reportSuccess -     True if we should print success message to console (should be false for mass population of hotels)
     * Return -     None
     * 
     * Modifications:   04/01/18 -  ATTD -  Created method.
     *                  04/02/18 -  ATTD -  Do not assign room if number of guests exceeds maximum occupancy.
     *                  04/04/18 -  ATTD -  Debug assigning a room to a customer.
     *                  04/04/18 -  ATTD -  Changing room categories to match those given in demo data.
     *                                      Make customer ID the primary key, and SSN just another attribute, per demo data.
     *                  04/08/18 -  ATTD -  Fix bug keeping dedicated staff from being assigned to presidential suite.
     *                  04/09/18 -  ATTD -  Fixed Defect preventing room to be assigned when PaymentMethod=card 
     */
    public static void db_frontDeskAssignRoom (
        int roomNum, 
        int hotelID, 
        int customerID,
        int numGuests, 
        String paymentMethod, 
        String cardType, 
        long cardNumber, 
        String billingAddress, 
        boolean reportSuccess
    ) {
        
        // Declare variables
        int oldStayID = 0;
        int newStayID = 0;
        int roomServiceStaffID = 0;
        int cateringStaffID = 0;
        String roomType = "";
        String userGuidance = "";
        
        try {
               
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {

                // Get the (OLD) newest stay ID
                jdbc_result = jdbcPrep_getNewestStay.executeQuery();
                jdbc_result.next();
                oldStayID = jdbc_result.getInt(1);

                /* Insert new stay, using prepared statement
                 * If the payment method is not "CARD", pass NULL for card type, card number, and billing address
                 * Indices to use when calling this prepared statement:
                 * 1 -  Customer ID
                 * 2 -  Number of guests
                 * 3 -  Payment method
                 * 4 -  Card type
                 * 5 -  Card number
                 * 6 -  Billing address
                 * 7 -  Room number
                 * 8 -  Hotel ID
                 * 9 -  Payment method (again)
                 * 10 - Card type (again)
                 * 11 - Card number (again)
                 * 12 - Billing address (again)
                 * 13 - Number of guests (again)
                 */
                jdbcPrep_assignRoom.setInt(1, customerID);
                jdbcPrep_assignRoom.setInt(2, numGuests);
                jdbcPrep_assignRoom.setInt(13, numGuests);
                jdbcPrep_assignRoom.setString(3, paymentMethod);
                jdbcPrep_assignRoom.setString(9, paymentMethod);
                if (paymentMethod.equalsIgnoreCase("CARD")) {
                    jdbcPrep_assignRoom.setString(4, cardType);
                    jdbcPrep_assignRoom.setString(10, cardType);
                    jdbcPrep_assignRoom.setLong(5, cardNumber);
                    jdbcPrep_assignRoom.setLong(11, cardNumber);
                    jdbcPrep_assignRoom.setString(6, billingAddress);
                    jdbcPrep_assignRoom.setString(12, billingAddress);
                }
                else {
                    jdbcPrep_assignRoom.setNull(4, java.sql.Types.VARCHAR);
                    jdbcPrep_assignRoom.setNull(10, java.sql.Types.VARCHAR);
                    jdbcPrep_assignRoom.setNull(5, java.sql.Types.BIGINT);
                    jdbcPrep_assignRoom.setNull(11, java.sql.Types.BIGINT);
                    jdbcPrep_assignRoom.setNull(6, java.sql.Types.VARCHAR);
                    jdbcPrep_assignRoom.setNull(12, java.sql.Types.VARCHAR);
                }
                jdbcPrep_assignRoom.setInt(7, roomNum);
                jdbcPrep_assignRoom.setInt(8, hotelID);
                jdbcPrep_assignRoom.executeUpdate();
                
                /* Assign dedicated staff to a presidential suite (if that's the room type)
                 * Indices to use when calling this prepared statement:
                 * 1 -  Hotel ID
                 * 2 -  Room number
                 * 3 -  Hotel ID (again)
                 * 4 -  Room number (again)
                 * 5 -  Hotel ID (again)
                 */
                jdbcPrep_getRoomByHotelIDRoomNum.setInt(1, roomNum);
                jdbcPrep_getRoomByHotelIDRoomNum.setInt(2, hotelID);
                jdbc_result = jdbcPrep_getRoomByHotelIDRoomNum.executeQuery();
                if (jdbc_result.next()) {
                    roomType = jdbc_result.getString("Category");     
                }
                if (roomType.equals("Presidential")) {
                    
                    /* Get first available room service staff in a hotel
                     * Indices to use when calling this prepared statement:
                     * 1 -  Hotel ID
                     */
                    jdbcPrep_getFirstAvailableRoomServiceStaff.setInt(1, hotelID);
                    jdbc_result = jdbcPrep_getFirstAvailableRoomServiceStaff.executeQuery();
                    if (jdbc_result.next()) {
                        roomServiceStaffID = jdbc_result.getInt(1);     
                    }
                    
                    /* Get first available room service staff in a hotel
                     * Indices to use when calling this prepared statement:
                     * 1 -  Hotel ID
                     */
                    jdbcPrep_getFirstAvailableCateringStaff.setInt(1, hotelID);
                    jdbc_result = jdbcPrep_getFirstAvailableCateringStaff.executeQuery();
                    if (jdbc_result.next()) {
                        cateringStaffID = jdbc_result.getInt(1);     
                    }

                    /* Assign dedicated staff to a presidential suite
                     * Get first available room service staff in the hotel
                     * Get first available catering staff in the hotel
                     * Make sure there is actually a customer stay assigned for this room
                     * Indices to use when calling this prepared statement:
                     * 1 -  room service staff
                     * 2 -  catering staff
                     * 3 -  Room number
                     * 4 -  Hotel ID
                     * 5 -  Room number (again)
                     * 6 -  Hotel ID (again)
                     */
                    jdbcPrep_assignDedicatedStaff.setInt(1, roomServiceStaffID);
                    jdbcPrep_assignDedicatedStaff.setInt(2, cateringStaffID);
                    jdbcPrep_assignDedicatedStaff.setInt(3, roomNum);
                    jdbcPrep_assignDedicatedStaff.setInt(4, hotelID);
                    jdbcPrep_assignDedicatedStaff.setInt(5, roomNum);
                    jdbcPrep_assignDedicatedStaff.setInt(6, hotelID);
                    jdbcPrep_assignDedicatedStaff.executeUpdate();
                }

              
                // If success, commit
                jdbc_connection.commit();

                
                // Then, tell the user about the success or failure
                jdbc_result = jdbcPrep_getNewestStay.executeQuery();
                jdbc_result.next();
                newStayID = jdbc_result.getInt(1);
                if (oldStayID < newStayID) {
                    if (reportSuccess) {
                        System.out.println("\nRoom Assigned!\n");
                        jdbc_result.beforeFirst();
                        support_printQueryResultSet(jdbc_result);
                    }
                }
                else {
                    userGuidance = 
                       "Room NOT Assigned " + 
                       "(this can happen if the number of guests is too high, or if it's a Presidential Suite there may be a lack of available staff)";
                    System.out.println("\n" + userGuidance + "\n");
                }
                
            }
            catch (Throwable err) {
                // Handle error
                error_handler(err);
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }

        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * DB Update: Insert new service record
     * 
     * Arguments -  stayID -            The ID of the stay the service is provided for.
     *              staffID -           The ID of the staff member providing the service.
     *              serviceName -       The name of the service provided.
     *              reportSuccess -     True if we should print success message to console (should be false for mass population of service records)
     * Return -     None
     * 
     * Modifications:   04/06/18 -  ATTD -  Created method.
     */
    public static void db_frontDeskEnterService(int stayID, int staffID, String serviceName, boolean reportSuccess) {
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
                
                // Declare variables
                int oldServiceRecordID;
                int newServiceRecordID;
                String userGuidance;
                
                // Get the (OLD) newest service record ID
                jdbc_result = jdbcPrep_getNewestServiceRecord.executeQuery();
                if (jdbc_result.next()) {
                    oldServiceRecordID = jdbc_result.getInt(1);
                }
                else {
                    oldServiceRecordID = -1;
                }
                
                /* Insert new service record for stay
                 * Staff member must have correct job title
                 * Staff member must be serving the hotel
                 * Staff member must not be dedicated to a different room
                 * Indices to use when calling this prepared statement:
                 * 1 -  stay ID
                 * 2 -  staff ID
                 * 3 -  name of service
                 */
                jdbcPrep_insertNewServiceRecord.setInt(1, stayID);
                jdbcPrep_insertNewServiceRecord.setInt(2, staffID);
                jdbcPrep_insertNewServiceRecord.setString(3, serviceName);
                jdbcPrep_insertNewServiceRecord.executeUpdate();

                // If success, commit
                jdbc_connection.commit();
                
                // Then, tell the user about the success or failure
                jdbc_result = jdbcPrep_getNewestServiceRecord.executeQuery();
                jdbc_result.next();
                newServiceRecordID = jdbc_result.getInt(1);
                if (oldServiceRecordID < newServiceRecordID) {
                    if (reportSuccess) {
                        System.out.println("\nService Record Entered!\n");
                        jdbc_result.beforeFirst();
                        support_printQueryResultSet(jdbc_result);
                    }
                }
                else {
                    userGuidance = 
                       "Service Record NOT Entered " + 
                       "(this can happen if the staff member is not eligible to provide the service for this stay)";
                    System.out.println("\n" + userGuidance + "\n");
                }
                
            }
            catch (Throwable err) {
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback();
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
  
        }
        catch (Throwable err) {
            error_handler(err);
        }
    }
    
    /** 
     * DB Update: Update a service record
     * 
     * Arguments -  serviceRecordID -   The ID of the service record.
     *              staffID -           The ID of the staff member providing the service.
     *              serviceName -       The name of the service provided.
     * Return -     None
     * 
     * Modifications:   04/06/18 -  ATTD -  Created method.
     *                  04/07/18 -  ATTD -  Debug method.
     */
    public static void db_frontDeskUpdateService(int serviceRecordID, int staffID, String serviceName) {
        try {
            
            /* Update a service record for stay
             * Staff member must have correct job title
             * Staff member must be serving the hotel
             * Staff member must not be dedicated to a different room
             * Indices to use when calling this prepared statement:
             * 1 -  staff ID
             * 2 -  name of service
             * 3 -  service record ID
             */
            jdbcPrep_udpateServiceRecord.setInt(1, staffID);
            jdbcPrep_udpateServiceRecord.setString(2, serviceName);
            jdbcPrep_udpateServiceRecord.setInt(3, serviceRecordID);
            jdbcPrep_udpateServiceRecord.executeUpdate();

            System.out.println("\nService Record Updated!\n");
            jdbcPrep_getServiceRecordByID.setInt(1, serviceRecordID);
            jdbc_result = jdbcPrep_getServiceRecordByID.executeQuery();
            support_printQueryResultSet(jdbc_result);
  
        }
        catch (Throwable err) {
            error_handler(err);
        }
    }
    
    /** 
     * DB Update: Release Room
     * 
     * Arguments -  hotelID -           The hotel for the room to be released
     *              roomNum -           The room number of the room to be released
     * Return -     None
     * 
     * Modifications:   04/04/18 -  MTA -   Added method.
     *                  04/05/18 -  ATTD -  Remove unused reportSuccess argument.
     */
    public static void db_frontDeskReleaseRoom (String hotelID, String roomNum) {
          
        try {
               
            /* Start transaction
             * In this function, we first update the check out time and end date in the Stays table
             * Then we release the dedicated staff assigned to the room in the Rooms table
             * Either operation by itself does not make sense
             * We want both operations to succeed together, or fail together
             */
            jdbc_connection.setAutoCommit(false);
            
            try {
        
                jdbcPrep_getStayIdForOccupiedRoom.setLong(1, Long.parseLong(hotelID));
                jdbcPrep_getStayIdForOccupiedRoom.setLong(2, Long.parseLong(roomNum)); 
                 
                // Get the Stay ID for given room
                ResultSet rs = jdbcPrep_getStayIdForOccupiedRoom.executeQuery();
                int stayId = 0; 
                while (rs.next()) {
                    stayId = rs.getInt("ID");   
                }
                
                // Now update the CheckOutTime and EndDate to current date               
                jdbcPrep_updateCheckOutTimeAndEndDate.setInt(1, stayId);
                jdbcPrep_updateCheckOutTimeAndEndDate.executeUpdate();
                
                // Now release the dedicated staff assigned to the room              
                jdbcPrep_releaseDedicatedStaff.setLong(1, Long.parseLong(hotelID));
                jdbcPrep_releaseDedicatedStaff.setLong(2, Long.parseLong(roomNum));
                jdbcPrep_releaseDedicatedStaff.executeUpdate(); 

                // Once both actions (Updating Checkout and EndDate for Stay & Releasing the dedicated staff)
                // are successful, commit the transaction
                jdbc_connection.commit();
                
                // Tell the user the room was released
                System.out.println("\nThe room has been successfully released!");
                
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }

        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * DB Query: Stay bill and itemized receipt
     * 
     * Note:    Since we want to produce an itemized receipt, and then pull from it the total amount owed by the customer, it would be awesome if we could use the VIEW feature
     *          (sort of a stored query that can be operated on like a table).
     *          Per http://www.mysqltutorial.org/mysql-views.aspx, "You cannot use subqueries in the FROM clause of the SELECT statement that defines the view before MySQL 5.7.7"
     *          Per https://classic.wolfware.ncsu.edu/wrap-bin/mesgboard/csc:540::001:1:2018?task=ST&Forum=8&Topic=7, "We are running version 5.5.57 it seems"
     * 
     * Arguments -  stayID -        The ID of the stay for which we wish to generate a bill and an itemized receipt
     *              reportResults - True if we wish to report the itemized receipt and total amount owed
     *                              (false for mass calculation of amounts owed on stays)
     * Return -     None
     * 
     * Modifications:   03/11/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/05/18 -  ATTD -  Make print-out more user-friendly.
     *                                      Allow for generating a receipt for a stay that is active (assume stay ends on current date).
     *                                      This adds flexibility in that this can be called before or after the room is released.
     */
    public static void db_frontDeskItemizedReceipt (int stayID, boolean reportResults) {

        try {
            
            // Declare variables
            NumberFormat currency;
            boolean discountApplies = false;
            double lineItemCost = 0d;
            double amountOwedBeforeDiscount = 0d;
            double amountOwedAfterDiscount = 0d;
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
                
                /* Pull out info that tells us whether the customer will get a discount
                 * Columns in result:
                 * 1 -  customer
                 * 2 -  start date
                 * 3 -  end date
                 * 4 -  hotel
                 * 5 -  room
                 * 6 -  payment method
                 * 7 -  card type
                 */
                jdbcPrep_getSummaryOfStay.setInt(1,stayID);
                jdbc_result = jdbcPrep_getSummaryOfStay.executeQuery();
                jdbc_result.next();
                discountApplies = jdbc_result.getString(6).equalsIgnoreCase("CARD") && jdbc_result.getString(7).equalsIgnoreCase("HOTEL");
                
                // If reporting to screen, print a summary of the stay for context
                if (reportResults) {
                    System.out.println("\nYour Stay:\n");
                    jdbc_result.beforeFirst();
                    support_printQueryResultSet(jdbc_result);
                }

                // Generate an itemized receipt for the stay
                jdbcPrep_getItemizedReceipt.setInt(1, stayID);
                jdbcPrep_getItemizedReceipt.setInt(2, stayID);
                jdbc_result = jdbcPrep_getItemizedReceipt.executeQuery();
                
                // Print the itemized receipt
                if (reportResults) {
                    System.out.println("\nItemized Receipt:\n");
                    support_printQueryResultSet(jdbc_result);
                }
                
                // Calculate the total amount owed, both before and after the possible hotel credit card discount
                jdbc_result.beforeFirst();
                while (jdbc_result.next()) {
                    // Total cost of a line item is in column 4
                    lineItemCost = Double.parseDouble(jdbc_result.getString(4));
                    amountOwedBeforeDiscount += lineItemCost;
                }
                amountOwedAfterDiscount = discountApplies ? amountOwedBeforeDiscount * 0.95 : amountOwedBeforeDiscount;
                
                // Update the stay with the total amount owed
                jdbcPrep_updateAmountOwed.setDouble(1, amountOwedAfterDiscount);
                jdbcPrep_updateAmountOwed.setInt(2, stayID);
                jdbcPrep_updateAmountOwed.executeUpdate();
                
                // If success, commit
                jdbc_connection.commit();
                
                // Print the total amount owed
                if (reportResults) {
                    currency = NumberFormat.getCurrencyInstance();
                    System.out.println("\nTotal Amount Owed Before Discount: " +        currency.format(amountOwedBeforeDiscount));
                    System.out.println("\nWolfInns Credit Card Discount Applied: " +    (amountOwedBeforeDiscount == amountOwedAfterDiscount ? "0%" : "5%"));
                    System.out.println("\nTotal Amount Owed After Discount: " +         currency.format(amountOwedAfterDiscount) + "\n");
                }
                
            }
            catch (Throwable err) {
                // Handle error
                error_handler(err);
                // Roll back the entire transaction
                jdbc_connection.rollback(); 
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    // DATABASE INTERACTION METHODS: REPORTS MENU
    
    /** 
     * Report task: Report staff serving customer during stay
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void db_reportStaffServingDuringStay(String stayId) {
  
        try {
                        
            jdbcPrep_reportStaffServingDuringStay.setInt(1, Integer.parseInt(stayId)); 
            jdbc_result = jdbcPrep_reportStaffServingDuringStay.executeQuery();
                                     
            // Print result
            System.out.println("\nReporting staff serving customer during their stay:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
         
    }
        
    /** 
     * Report task: Report occupancy by date range
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   04/05/18 -  MTA -  Added method.
     */
    public static void db_reportOccupancyByDateRange(String startDate, String endDate) {
  
        try {
                        
            jdbcPrep_reportOccupancyByDateRange.setDate(1, java.sql.Date.valueOf(endDate));
            jdbcPrep_reportOccupancyByDateRange.setDate(2, java.sql.Date.valueOf(startDate));
            jdbcPrep_reportOccupancyByDateRange.setDate(3, java.sql.Date.valueOf(startDate));
            jdbc_result = jdbcPrep_reportOccupancyByDateRange.executeQuery();
                                     
            // Print result
            System.out.println("\nReporting occupancy from " + startDate + " to " + endDate);
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
         
    }

    /** 
     * DB Query: Hotel Revenue
     * 
     * Arguments -  hotelID -       The ID of the hotel
     *              queryStartDate -     The start date of the date range we want revenue for
     *              queryEndDate -       The end date of the date range we want revenue for
     * Return -     None
     * 
     * Modifications:   03/09/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     */
    public static void db_reportHotelRevenue (int hotelID, String queryStartDate, String queryEndDate) {

        try {
            
            // Declare variables
            double revenue = 0.0;
            NumberFormat currency;

            /* Get revenue for one hotel from a date range
             * Revenue is earned when the guest checks OUT
             * So we always look at the end date for the customer's stay
             * No transaction needed for a query
             */            
            try {
                
                jdbcPrep_reportHotelRevenueByDateRange.setInt(1, hotelID);
                jdbcPrep_reportHotelRevenueByDateRange.setDate(2, java.sql.Date.valueOf(queryStartDate));
                jdbcPrep_reportHotelRevenueByDateRange.setDate(3, java.sql.Date.valueOf(queryEndDate));
                jdbc_result = jdbcPrep_reportHotelRevenueByDateRange.executeQuery();
                                         
                // Print report
                ResultSet rs = jdbcPrep_reportHotelRevenueByDateRange.executeQuery(); 
                 
                while (rs.next()) {
                    revenue = rs.getDouble("Revenue");  
                }
                // Print report
                currency = NumberFormat.getCurrencyInstance();
                System.out.println("\nRevenue earned: " + currency.format(revenue) + "\n");
                
            }
            catch (Throwable err) {
                error_handler(err);
            }
                  
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
   
    // DATABASE INTERACTION METHODS: MANAGE MENU
    
    /** 
     * Report task: Report all values of a single tuple of the Hotels table, by ID
     * 
     * Arguments -  id -        The ID of the tuple.
     * Return -     success -   True if the hotel was found.
     * 
     * Modifications:   03/23/18 -  ATTD -  Created method.
     *                  03/28/18 -  ATTD -  Return whether query was successful (hotel was found).
     */
    public static boolean db_manageShowHotelByID(int id) {

        // Declare variables
        boolean success = false;
        
        try {

            // Get entire tuple from table
            jdbcPrep_getHotelByID.setInt(1, id);
            jdbc_result = jdbcPrep_getHotelByID.executeQuery();
            
            // Was the hotel found?
            if (jdbc_result.next()) {
                success = true;
                jdbc_result.beforeFirst();
            }
            
            // Print result
            System.out.println("\nHotel Information:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
        return success;
        
    }
    
    /** 
     * Report task: Report all values of a single tuple of the Staff table, by ID
     * 
     * Arguments -  id -        The ID of the tuple.
     * Return -     success -   True if the staff member was found.
     * 
     * Modifications:   03/26/18 -  ATTD -  Created method.
     *                  03/28/18 -  ATTD -  Return whether query was successful (staff was found).
     */
    public static boolean db_manageShowStaffByID(int id) {

        // Declare variables
        boolean success = false;
        
        try {
            
            // Get entire tuple from table
            jdbcPrep_getStaffByID.setInt(1, id);
            jdbc_result = jdbcPrep_getStaffByID.executeQuery();
            
            // Was the staff member found?
            if (jdbc_result.next()) {
                success = true;
                jdbc_result.beforeFirst();
            }
            
            // Print result
            System.out.println("\nStaff Member Information:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
        return success;
        
    }
    
    /** 
     * Report task:   Report all values of a single tuple of the Rooms table, by hotelId and RoomNum
     * 
     * Arguments -    hotelId       - Hotel to which the room belongs
     *                roomNumber    - Room number  
     * Return -       None
     * 
     * Modifications: 03/25/18 -  MTA -  Created method.
     */
    public static void db_manageShowRoomByHotelIdRoomNum(int hotelId, int roomNum) {

        try {
             
            jdbcPrep_getRoomByHotelIDRoomNum.setInt(1, roomNum);
            jdbcPrep_getRoomByHotelIDRoomNum.setInt(2, hotelId);
            jdbc_result = jdbcPrep_getRoomByHotelIDRoomNum.executeQuery();
            
            // Print result
            System.out.println("\nInformation:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Report task:   Report all values of a single tuple of the Customers table, by customer ID
     * 
     * Arguments -    customerID - Customer ID
     * Return -       None
     * 
     * Modifications:   03/27/18 -  MTA -  Created method.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static void db_manageShowCustomerByID(String ID) {

        try {
             
            jdbcPrep_getCustomerByID.setInt(1, Integer.parseInt(ID)); 
            jdbc_result = jdbcPrep_getCustomerByID.executeQuery();
            
            // Print result
            System.out.println("\nInformation:\n");
            support_printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            error_handler(err);
        } 
    }

    /** 
     * DB Update: Insert Hotel
     * 
     * Note:    This does NOT support chain reaction of moving managers between hotels.
     *          Manager of new hotel CANNOT be existing manager of another hotel.
     * 
     * Arguments -  hotelName -     The name of the hotel to insert
     *              streetAddress - The street address of the hotel to insert
     *              city -          The city in which the hotel is located
     *              state -         The state in which the hotel is located
     *              zip -           The zip code of the hotel
     *              phoneNum -      The phone number of the hotel
     *              managerID -     The staff ID of the person to promote to manager of the new hotel
     *              reportSuccess - True if we should print success message to console (should be false for mass population of hotels)
     * Return -     None
     * 
     * Modifications:   03/09/18 -  ATTD -  Created method.
     *                  03/12/18 -  ATTD -  Add missing JDBC commit statement.
     *                  03/16/18 -  ATTD -  Changing departments to emphasize their meaninglessness.
     *                  03/20/18 -  ATTD -  Switch to using prepared statements.
     *                  03/21/18 -  ATTD -  Debug inserting new hotel with prepared statements.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/04/18 -  ATTD -  Add attribute for hotel zip code, per demo data.
     */
    public static void db_manageHotelAdd (String hotelName, String streetAddress, String city, String state, int zip, long phoneNum, int managerID, boolean reportSuccess) {
        
        // Declare variables
        int previousNewestHotelID;
        int newHotelID;
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
                
                // Keep track of whether the hotel was actually added, by making sure the newest hotel ID changes
                jdbc_result = jdbcPrep_getNewestHotelID.executeQuery();
                jdbc_result.next();
                previousNewestHotelID = jdbc_result.getInt(1);

                /* Insert new hotel, using prepared statement
                 * Indices to use when calling this prepared statement:
                 * 1 - name
                 * 2 - street address
                 * 3 - city
                 * 4 - state
                 * 5 - zip code
                 * 6 - phone number
                 * 7 - manager ID
                 * 8 - manager ID (again)
                 */
                jdbcPrep_insertNewHotel.setString(1, hotelName);
                jdbcPrep_insertNewHotel.setString(2, streetAddress);
                jdbcPrep_insertNewHotel.setString(3, city);
                jdbcPrep_insertNewHotel.setString(4, state);
                jdbcPrep_insertNewHotel.setInt(5, zip);
                jdbcPrep_insertNewHotel.setLong(6, phoneNum);
                jdbcPrep_insertNewHotel.setInt(7, managerID);
                jdbcPrep_insertNewHotel.setInt(8, managerID); 
                jdbcPrep_insertNewHotel.executeUpdate();
                
                // Update new hotel's manager (job title and hotel assignment), using prepared statement
                jdbcPrep_updateNewHotelManager.executeUpdate();

                // If success, commit
                jdbc_connection.commit();
                
                // Then, tell the user about the failure or success
                jdbc_result = jdbcPrep_getNewestHotelID.executeQuery();
                jdbc_result.next();
                newHotelID = jdbc_result.getInt(1);
                if (previousNewestHotelID == newHotelID) {
                    System.out.println("\n'" + hotelName + "' hotel was NOT added (this can happen if e.g. an invalid manager ID was provided)\n");
                }
                else if (reportSuccess) {
                    System.out.println("\n'" + hotelName + "' hotel added (hotel ID: " + newHotelID + ")!\n");
                }
                
            }
            catch (Throwable err) {
                
                // Handle error
                error_handler(err);
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * DB Update: Change Hotel Information
     * 
     * Arguments -  hotelID -           The ID of the hotel to update
     *              attributeToChange - The attribute to update
     *              valueToChangeTo -   The value to update this attribute to (as a string)
     * Return -     None
     * 
     * Modifications:   03/23/18 -  ATTD -  Created method.
     *                  04/04/18 -  ATTD -  Add attribute for hotel zip code, per demo data.
     *                                      Change "front desk representative" job title to "front desk staff", to match demo data.
     *                  04/13/18 -  ATTD -  Make attribute as entered by user case-insensitive.
     */
    public static void db_manageHotelUpdate (int hotelID, String attributeToChange, String valueToChangeTo) {
        
        try {

            // Safeguard - make sure changes will commit
            jdbc_connection.setAutoCommit(true);
            
            // Update hotel info, using prepared statement
            switch (attributeToChange.toUpperCase()) {
                case "NAME":
                    jdbcPrep_udpateHotelName.setString(1, valueToChangeTo);
                    jdbcPrep_udpateHotelName.setInt(2, hotelID);
                    jdbcPrep_udpateHotelName.executeUpdate();
                    break;
                case "STREETADDRESS":
                    jdbcPrep_updateHotelStreetAddress.setString(1, valueToChangeTo);
                    jdbcPrep_updateHotelStreetAddress.setInt(2, hotelID);
                    jdbcPrep_updateHotelStreetAddress.executeUpdate();
                    break;
                case "CITY":
                    jdbcPrep_updateHotelCity.setString(1, valueToChangeTo);
                    jdbcPrep_updateHotelCity.setInt(2, hotelID);
                    jdbcPrep_updateHotelCity.executeUpdate();
                    break;
                case "STATE":
                    jdbcPrep_udpateHotelState.setString(1, valueToChangeTo);
                    jdbcPrep_udpateHotelState.setInt(2, hotelID);
                    jdbcPrep_udpateHotelState.executeUpdate();
                    break;
                case "ZIP":
                    jdbcPrep_updateHotelZip.setString(1, valueToChangeTo);
                    jdbcPrep_updateHotelZip.setInt(2, hotelID);
                    jdbcPrep_updateHotelZip.executeUpdate();
                    break;
                case "PHONENUM":
                    jdbcPrep_updateHotelPhoneNum.setLong(1, Long.parseLong(valueToChangeTo));
                    jdbcPrep_updateHotelPhoneNum.setInt(2, hotelID);
                    jdbcPrep_updateHotelPhoneNum.executeUpdate();
                    break;
                case "MANAGERID":
                    /* This one is a special case
                     * 1 -  Demote old manager to front desk staff
                     * 2 -  Actually update the manager ID of the hotel
                     * 3 -  Update new manager to have the correct job title and hotel assignment
                     */
                    jdbc_connection.setAutoCommit(false);
                    try {
                        // Demote old manager
                        jdbcPrep_demoteOldManager.setInt(1, hotelID);
                        jdbcPrep_demoteOldManager.executeUpdate();
                        // Update hotel manager ID
                        jdbcPrep_updateHotelManagerID.setLong(1, Integer.parseInt(valueToChangeTo));
                        jdbcPrep_updateHotelManagerID.setInt(2, hotelID);
                        jdbcPrep_updateHotelManagerID.executeUpdate();
                        // Promote new manager
                        jdbcPrep_promoteNewManager.setInt(1, hotelID);
                        jdbcPrep_promoteNewManager.setInt(2, Integer.parseInt(valueToChangeTo));
                        jdbcPrep_promoteNewManager.executeUpdate();
                        // If success, commit
                        jdbc_connection.commit();
                    }
                    catch (Throwable err) {
                        // Handle error
                        error_handler(err);
                        // Roll back the entire transaction
                        jdbc_connection.rollback(); 
                    }
                    finally {
                        // Restore normal auto-commit mode
                        jdbc_connection.setAutoCommit(true);
                    }
                    break;
                default:
                    System.out.println(
                        "\nCannot update the '" + attributeToChange + "' attribute of a hotel, because this is not a recognized attribute for Wolf Inns hotels\n"
                    );
                    break;
            }
            
        }
        catch (Throwable err) {
            error_handler(err);            
        }
        
    }
    
    /** 
     * DB Update: Delete Hotel
     * 
     * Why does this method exist at all when it is so dead simple?
     * 1. To keep with the pattern of isolating methods that directly interact with the DBMS, 
     * from those that interact with the user (readability of code)
     * 2. In case in the future we find some need for mass deletes.
     * 
     * Arguments -  hotelID -       The ID of the hotel
     *              reportSuccess - True if we should print success message to console (should be false for mass deletion of hotels)
     * Return -     None
     * 
     * Modifications:   03/09/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/24/18 -  ATTD -  Use prepared statement.
     *                                      Do not claim success unless the hotel actually got deleted.
     */
    public static void db_manageHotelDelete (int hotelID, boolean reportSuccess) {

        try {

            /* Remove the hotel from the Hotels table
             * No need to explicitly set up a transaction, because only one SQL command is needed
             */
            jdbcPrep_deleteHotel.setInt(1, hotelID);
            jdbcPrep_deleteHotel.executeUpdate();
            
            // Did the deletion succeed?
            jdbcPrep_getHotelByID.setInt(1, hotelID);
            jdbc_result = jdbcPrep_getHotelByID.executeQuery();
            if (jdbc_result.next()) {
                // Always complain about a failure (never fail silently)
                System.out.println(
                    "\nHotel ID " + 
                    hotelID + 
                    " was NOT deleted (this can happen if a customer is currently staying in the hotel)\n"
                );
            }
            else {
                // Tell the user about the success, if we're supposed to
                if (reportSuccess) {
                    System.out.println("\nHotel ID " + hotelID + " deleted!\n");
                }
            }
            
        }
        catch (Throwable err) {
             error_handler(err);
        }
        
    }
    
    /** 
     * DB Update: Insert Staff Member
     * 
     * Arguments -  name -          The name of the new staff member
     *              dob -           The date of birth of the new staff member
     *              jobTitle -      The job title of the new staff member
     *              department -    The department of the new staff member
     *              phoneNum -      The phone number of the new staff member
     *              address -       The address of the new staff member
     *              hotelID -       The ID of the hotel to which the staff member is assigned (OR zero if not assigned to any hotel)
     *              reportSuccess - True if we should print success message to console (should be false for mass population of hotels)
     * Return -     None
     * 
     * Modifications:   03/24/18 -  ATTD -  Created method.
     *                  04/04/18 -  ATTD -  Handle manager-hotel two-way link.
     */
    public static void db_manageStaffAdd (String name, String dob, String jobTitle, String department, long phoneNum, String address, int hotelID, boolean reportSuccess) {
        
        // Declare variables
        int newStaffID;
        
        try {
            
            /* Handle manager-hotel two-way link
             * Do not allow insertion of new staff member, if that staff member
             * is supposed to be the manager of a hotel which ALREADY has a manager
             * The way we've structured the hotel-manager two-way link,
             * we assign the manager on the hotel side before we assign the hotel on the manager side
             * So this means it is NEVER appropriate to insert a manager and give them an assigned hotel in the same SQL statement
             * Handle this restriction at the application level rather than at the SQL level (just easier that way)
             */
            if (jobTitle.equals("Manager") && hotelID != 0) {
                System.out.println(
                    "\n'" + 
                    name + 
                    "' staff member NOT added " + 
                    "(to add a manager, must first add that manager with no hotel ID, then create hotel referencing the manager, finally assign manager to hotel)" + 
                    "\n"
                );
            }
            else {
                
                // Start transaction
                jdbc_connection.setAutoCommit(false);
                
                try {
    
                    /* Insert new staff member
                     * 
                     * If a zero is passed for hotel ID,
                     * that means the new staff member isn't to be assigned to any particular hotel,
                     * so set hotel ID in the tuple to NULL
                     * 
                     * Indices to use when calling this prepared statement:
                     * 1 - name
                     * 2 - date of birth
                     * 3 - job title
                     * 4 - department
                     * 5 - phone number
                     * 6 - address
                     * 7 - hotel ID
                     */
                    jdbcPrep_insertNewStaff.setString(1, name);
                    jdbcPrep_insertNewStaff.setString(2, dob);
                    jdbcPrep_insertNewStaff.setString(3, jobTitle);
                    jdbcPrep_insertNewStaff.setString(4, department);
                    jdbcPrep_insertNewStaff.setLong(5, phoneNum);
                    jdbcPrep_insertNewStaff.setString(6, address);
                    if (hotelID == 0) {
                        jdbcPrep_insertNewStaff.setNull(7, java.sql.Types.INTEGER);
                    }
                    else {
                        jdbcPrep_insertNewStaff.setInt(7, hotelID); 
                    }
                    jdbcPrep_insertNewStaff.executeUpdate();
    
                    // If success, commit
                    jdbc_connection.commit();
                    
                    // Then, tell the user about the success
                    if (reportSuccess) {
                        jdbc_result = jdbcPrep_getNewestStaffID.executeQuery();
                        jdbc_result.next();
                        newStaffID = jdbc_result.getInt(1);
                        System.out.println("\n'" + name + "' staff member added (staff ID: " + newStaffID + ")!\n");
                    }
                    
                }
                catch (Throwable err) {
                    
                    // Handle error
                    error_handler(err);
                    
                    // Roll back the entire transaction
                    jdbc_connection.rollback();
                    
                }
                finally {
                    // Restore normal auto-commit mode
                    jdbc_connection.setAutoCommit(true);
                }
            
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * DB Update: Change Staff Information
     * 
     * Arguments -  staffID -           The ID of the staff member to update
     *              attributeToChange - The attribute to update
     *              valueToChangeTo -   The value to update this attribute to (as a string)
     * Return -     None
     * 
     * Modifications:   03/27/18 -  ATTD -  Created method.
     *                  04/13/18 -  ATTD -  Make attribute, as entered by user, case-insensitive.
     */
    public static void db_manageStaffUpdate (int staffID, String attributeToChange, String valueToChangeTo) {
        
        try {

            // Update hotel info, using prepared statement
            switch (attributeToChange.toUpperCase()) {
                case "NAME":
                    jdbcPrep_updateStaffName.setString(1, valueToChangeTo);
                    jdbcPrep_updateStaffName.setInt(2, staffID);
                    jdbcPrep_updateStaffName.executeUpdate();
                    break;
                case "DOB":
                    jdbcPrep_updateStaffDOB.setDate(1, java.sql.Date.valueOf(valueToChangeTo));
                    jdbcPrep_updateStaffDOB.setInt(2, staffID);
                    jdbcPrep_updateStaffDOB.executeUpdate();
                    break;
                case "JOBTITLE":
                    jdbcPrep_updateStaffJobTitle.setString(1, valueToChangeTo);
                    jdbcPrep_updateStaffJobTitle.setInt(2, staffID);
                    jdbcPrep_updateStaffJobTitle.executeUpdate();
                    break;
                case "DEP":
                    jdbcPrep_updateStaffDepartment.setString(1, valueToChangeTo);
                    jdbcPrep_updateStaffDepartment.setInt(2, staffID);
                    jdbcPrep_updateStaffDepartment.executeUpdate();
                    break;
                case "PHONENUM":
                    jdbcPrep_updateStaffPhoneNum.setLong(1, Long.parseLong(valueToChangeTo));
                    jdbcPrep_updateStaffPhoneNum.setInt(2, staffID);
                    jdbcPrep_updateStaffPhoneNum.executeUpdate();
                    break;
                case "ADDRESS":
                    jdbcPrep_updateStaffAddress.setString(1, valueToChangeTo);
                    jdbcPrep_updateStaffAddress.setInt(2, staffID);
                    jdbcPrep_updateStaffAddress.executeUpdate();
                    break;
                case "HOTELID":
                    jdbcPrep_updateStaffHotelID.setInt(1, Integer.parseInt(valueToChangeTo));
                    jdbcPrep_updateStaffHotelID.setInt(2, staffID);
                    jdbcPrep_updateStaffHotelID.executeUpdate();
                    break;
                default:
                    System.out.println(
                        "\nCannot update the '" + attributeToChange + "' attribute of a staff member, because this is not a recognized attribute for Wolf Inns staff\n"
                    );
                    break;
            }
            
        }
        catch (Throwable err) {
            error_handler(err);            
        }
        
    }
    
    /** 
     * DB Update: Delete Staff Member
     * 
     * Arguments -  staffID -       The ID of the staff member
     *              reportSuccess - True if we should print success message to console (should be false for mass deletion of staff members)
     * Return -     None
     * 
     * Modifications:   03/12/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  03/24/18 -  ATTD -  Use prepared statement.
     *                                      Do not claim success unless the staff member actually got deleted.
     */
    public static void db_manageStaffDelete (int staffID, boolean reportSuccess) {

        try {
            
            /* Remove the staff member from the Staff table
             * No need to explicitly set up a transaction, because only one SQL command is needed
             */
            jdbcPrep_deleteStaff.setInt(1, staffID);
            jdbcPrep_deleteStaff.executeUpdate();
            
            // Did the deletion succeed?
            jdbcPrep_getStaffByID.setInt(1, staffID);
            jdbc_result = jdbcPrep_getStaffByID.executeQuery();
            if (jdbc_result.next()) {
                // Always complain about a failure (never fail silently)
                System.out.println(
                    "\nStaff ID " + 
                    staffID + 
                    " was NOT deleted " + 
                    "(this can happen if the staff member is currently dedicated to serving a room in which a guest is staying)\n"
                );
            }
            else {
                // Tell the user about the success, if we're supposed to
                if (reportSuccess) {
                    System.out.println("\nStaff ID " + staffID + " deleted!\n");
                }
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    } 
    
    /** 
     * DB Update: Add new room
     * 
     * Arguments -  roomNumber    - Room number 
     *              hotelId       - Hotel to which the room belongs
     *              category      - Room Category ('Economy', 'Deluxe', 'Executive', 'Presidential')
     *              maxOccupancy  - Max Occupancy for the room
     *              nightlyRate   - Nightly rate for the room
     *              reportSuccess - True if need to print success message after method completes
     * Return -     None
     * 
     * Modifications:   03/24/18 -  MTA -  Added method. 
     * 					03/25/18 -  MTA -  Updated method signature.
     *                  04/04/18 -  ATTD -  Changing room categories to match those given in demo data.
     */
    public static void db_manageRoomAdd(int roomNumber, int hotelId, String category, int maxOccupancy, int nightlyRate, boolean reportSuccess){
    
        try {

            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
            	           
            	jdbcPrep_insertNewRoom.setInt(1, roomNumber);
            	jdbcPrep_insertNewRoom.setInt(2, hotelId);
            	jdbcPrep_insertNewRoom.setString(3, category);
            	jdbcPrep_insertNewRoom.setInt(4, maxOccupancy);
            	jdbcPrep_insertNewRoom.setInt(5, nightlyRate);  
                 
            	jdbcPrep_insertNewRoom.executeUpdate();

                // If success, commit
                jdbc_connection.commit();
                
                if (reportSuccess)
                {
                	System.out.println("\nRoom has been successfully added to the database! \n");
                } 
            } 
            catch (Throwable ex) {
                
                // Handle pk violation
            	if (ex.getMessage() != null && ex.getMessage().matches("(.*)Duplicate entry(.*)for key 'PRIMARY'(.*)")) { 
            		error_handler(ex, "PK_ROOMS");
            	} else {
            		error_handler(ex);	
            	} 
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
    }
  
    /** 
     * DB Update: Add new customer
     * 
     * Arguments -  ssn           - Customer Social Security Number
     *              name          - Customer Name
     *              dob      	  - Customer Date Of Birth
     *              phoneNumber   - Customer Phone Number
     *              email         - Customer email
     *              reportSuccess - True if need to print success message after method completes
     * Return -     None
     * 
     * Modifications:   03/27/18 -  MTA -  Added method. 
     */
    public static void db_manageCustomerAdd(String ssn, String name, String dob, String phoneNumber, String email, boolean reportSuccess){
    
        try {

            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {             	
            	
            	jdbcPrep_insertNewCustomer.setLong(1, Long.parseLong(ssn));
            	jdbcPrep_insertNewCustomer.setString(2, name);
            	jdbcPrep_insertNewCustomer.setDate(3, java.sql.Date.valueOf(dob));
            	jdbcPrep_insertNewCustomer.setLong(4, Long.parseLong(phoneNumber));
            	jdbcPrep_insertNewCustomer.setString(5, email);  
                 
            	jdbcPrep_insertNewCustomer.executeUpdate();

                // If success, commit
                jdbc_connection.commit();
                
                if (reportSuccess)
                {
                	System.out.println("\nCustomer has been successfully added to the database! \n");
                } 
            
            } 
            catch (Throwable ex) {
                
                // Handle pk violation
            	if (ex.getMessage() != null && ex.getMessage().matches("(.*)Duplicate entry(.*)for key 'PRIMARY'(.*)")) { 
            		error_handler(ex, "PK_CUSTOMERS");
            	} else {
            		error_handler(ex);	
            	} 
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true); 
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
    }
    
    /** 
	 * DB Update: Update customer details
	 * 
	 * Arguments -  ID -               Customer ID 
	 *              columnName -       Name of the column for Rooms table that is being updated 
	 *              columnValue -      Value of column for Rooms table that is being updated 
	 *              reportSuccess -    True if need to print success message after method completes 
	 * Return -     None
	 * 
	 * Modifications:  03/28/18 -  MTA -   Added method. 
	 *                 04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
	 */
	public static void db_manageCustomerUpdate(String ID, String columnName, String columnValue, boolean reportSuccess){
	
	    try {
	
	        // Start transaction
	        jdbc_connection.setAutoCommit(false);
	        
	        try {
	        	
	        	switch (columnName) {
                    case "SSN":
                        jdbcPrep_customerUpdateSSN.setString(1, columnValue); 
                        jdbcPrep_customerUpdateSSN.setInt(2, Integer.parseInt(ID)); 
                        jdbcPrep_customerUpdateSSN.executeUpdate();
                        break;
    				case "Name":
    					jdbcPrep_customerUpdateName.setString(1, columnValue); 
    					jdbcPrep_customerUpdateName.setInt(2, Integer.parseInt(ID)); 
    					jdbcPrep_customerUpdateName.executeUpdate();
    					break;
    				case "DOB":
    					jdbcPrep_customerUpdateDateOfBirth.setString(1, columnValue); 
    					jdbcPrep_customerUpdateDateOfBirth.setInt(2, Integer.parseInt(ID)); 
    					jdbcPrep_customerUpdateDateOfBirth.executeUpdate();
    					break;
    				case "PhoneNum":
    					jdbcPrep_customerUpdatePhoneNumber.setString(1, columnValue); 
    					jdbcPrep_customerUpdatePhoneNumber.setInt(2, Integer.parseInt(ID)); 
    					jdbcPrep_customerUpdatePhoneNumber.executeUpdate();
    					break;
    				case "Email":
    					jdbcPrep_customerUpdateEmail.setString(1, columnValue); 
    					jdbcPrep_customerUpdateEmail.setInt(2, Integer.parseInt(ID)); 
    					jdbcPrep_customerUpdateEmail.executeUpdate();
    					break;
    				default:
    					System.out.println(
                            "\nCannot update the '" + columnName + "' attribute of customer, because this is not a recognized attribute for Wolf Inns Customers\n"
                        );
    					break;
				}
	        	 
	            // If success, commit
	            jdbc_connection.commit();
	            
	            if (reportSuccess)
	            {
	            	System.out.println("\nCustomer details were successfully updated! \n");        	
	            } 
	        } 
	        catch (Throwable ex) {
	             
	        	error_handler(ex);	
	        	 
	            // Roll back the entire transaction
	            jdbc_connection.rollback();
	            
	        }
	        finally {
	            // Restore normal auto-commit mode
	            jdbc_connection.setAutoCommit(true); 
	        }
	        
	    }
	    catch (Throwable err) {
	        error_handler(err);
	    }
	}
    
	/** 
	 * DB Update: Update room details
	 * 
	 * Arguments -  roomNumber    - Room number 
	 *              hotelId       - Hotel to which the room belongs
	 *              columnName    - Name of the column for Rooms table that is being updated 
	 *              columnValue   - Value of column for Rooms table that is being updated 
	 *              reportSuccess - True if need to print success message after method completes 
	 * Return -     None
	 * 
	 * Modifications:   03/25/18 -  MTA -  Added method. 
	 */
	public static void db_manageRoomUpdate(int roomNumber, int hotelId, String columnName, String columnValue, boolean reportSuccess){
	
	    try {
	
	        // Start transaction
	        jdbc_connection.setAutoCommit(false);
	        
	        try {
	        	
	        	switch (columnName) {
				case "Category":
					jdbcPrep_roomUpdateCategory.setString(1, columnValue); 
					jdbcPrep_roomUpdateCategory.setInt(2, roomNumber);
					jdbcPrep_roomUpdateCategory.setInt(3, hotelId); 
					jdbcPrep_roomUpdateCategory.executeUpdate();
					break;

				case "MaxOcc":
					jdbcPrep_roomUpdateMaxOccupancy.setString(1, columnValue); 
					jdbcPrep_roomUpdateMaxOccupancy.setInt(2, roomNumber);
					jdbcPrep_roomUpdateMaxOccupancy.setInt(3, hotelId); 
					jdbcPrep_roomUpdateMaxOccupancy.executeUpdate();
					break;
					
				case "NightlyRate":
					jdbcPrep_roomUpdateNightlyRate.setString(1, columnValue); 
					jdbcPrep_roomUpdateNightlyRate.setInt(2, roomNumber);
					jdbcPrep_roomUpdateNightlyRate.setInt(3, hotelId); 
					jdbcPrep_roomUpdateNightlyRate.executeUpdate();
					break;
					
				case "DRSStaff":
					jdbcPrep_roomUpdateDRSStaff.setString(1, columnValue); 
					jdbcPrep_roomUpdateDRSStaff.setInt(2, roomNumber);
					jdbcPrep_roomUpdateDRSStaff.setInt(3, hotelId); 
					jdbcPrep_roomUpdateDRSStaff.executeUpdate();
					break;
					
				case "DCStaff":
					jdbcPrep_roomUpdateDCStaff.setString(1, columnValue); 
					jdbcPrep_roomUpdateDCStaff.setInt(2, roomNumber);
					jdbcPrep_roomUpdateDCStaff.setInt(3, hotelId); 
					jdbcPrep_roomUpdateDCStaff.executeUpdate();
					break;

				default:
					System.out.println(
	                    "\nCannot update the '" + columnName + "' attribute of room, because this is not a recognized attribute for Wolf Inns Rooms\n"
	                );
					break;
				}
	
	            // If success, commit
	            jdbc_connection.commit();
	            
	            if (reportSuccess)
	            {
	            	System.out.println("\nRoom details were successfully updated! \n");        	
	            } 
	        } 
	        catch (Throwable ex) {
	        	
	        	error_handler(ex);	
	        	 
	            // Roll back the entire transaction
	            jdbc_connection.rollback();
	            
	        }
	        finally {
	            // Restore normal auto-commit mode
	            jdbc_connection.setAutoCommit(true); 
	        }
	        
	    }
	    catch (Throwable err) {
	        error_handler(err);
	    }
	}
    
    /** 
     * DB Update: Delete room
     * 
     * Arguments -  hotelId       - Hotel to which the room belongs
     * 				roomNumber    - Room number  
     *              reportSuccess - True if need to print success message after method completes
     * Return -     None
     * 
     * Modifications:   03/25/18 -  MTA -  Added method. 
     */
    public static void db_manageRoomDelete(int hotelId, int roomNumber, boolean reportSuccess) {
    	try {

            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {                      
  
            	jdbcPrep_roomDelete.setInt(1, roomNumber);
            	jdbcPrep_roomDelete.setInt(2, hotelId); 
            	
            	jdbcPrep_roomDelete.executeUpdate();

                // If success, commit
                jdbc_connection.commit();
                
                if (reportSuccess)
                {
                	System.out.println("\nRoom has been successfully deleted from the database! \n");        	
                } 
            } 
            catch (Throwable ex) {
                
                error_handler(ex);	 
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true); 
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
    }
    
    /** 
     * DB Update: Delete customer
     * 
     * Arguments -  ID -            Customer ID
     *              reportSuccess - True if need to print success message after method completes
     * Return -     None
     * 
     * Modifications:   03/28/18 -  MTA -   Added method.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static void db_manageCustomerDelete(String ID, boolean reportSuccess) {
    	try {

            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {                      
  
            	jdbcPrep_customerDelete.setInt(1, Integer.parseInt(ID)); 
            	
            	jdbcPrep_customerDelete.executeUpdate();

                // If success, commit
                jdbc_connection.commit();
                
                if (reportSuccess)
                {
                	System.out.println("\nCustomer has been successfully deleted from the database! \n");        	
                } 
            } 
            catch (Throwable ex) {
                
                error_handler(ex);	 
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true); 
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
    }
  
    // SUPPORT METHODS
    
    /** 
     * DB Check: Check if room number is associated with given hotel
     * 
     * Arguments -  roomNumber    - Room number
     *              hotelId       - Hotel Id 
     * Return -     boolean       - True if the room number is associated with given hotel
      * 
     * Modifications:   03/24/18 -  MTA -  Added method. 
     */
    public static boolean support_isValidRoomForHotel(int hotelId, int roomNumber){  
    	
    	try {      		
	        try {				
				 jdbcPrep_isValidRoomNumber.setInt(1, hotelId);
				 jdbcPrep_isValidRoomNumber.setInt(2, roomNumber);   
				 
				 ResultSet rs = jdbcPrep_isValidRoomNumber.executeQuery();
				 int cnt = 0;
				 
				 while (rs.next()) {
					cnt = rs.getInt("CNT"); 	
				 }
				 
				 if (cnt > 0) { 
					 return true;
				 }   
				 
	        }
	        catch (Throwable err) {
	            error_handler(err);
	        }  
    	} 
    	catch (Throwable err) { 
    		error_handler(err); 
        }
        
        return false; 
    }
    
    /** 
     * DB Check: Check if given hotel id exists in the database
     * 
     * Arguments -  hotelId       - Hotel Id  
     * Return -     boolean       - True if the hotel id exists
     * 
     * Modifications:   03/25/18 -  MTA -  Added method. 
     */
    public static boolean support_isValidHotelID(int hotelId) {
    	try {   	        
    		try {  				 
				 jdbcPrep_isValidHotelID.setInt(1, hotelId); 
				 
				 ResultSet rs = jdbcPrep_isValidHotelID.executeQuery();
				 int cnt = 0;
				 
				 while (rs.next()) {
					cnt = rs.getInt("CNT"); 	
				 }
				 
				 if (cnt > 0) { 
					 return true;
				 }   
				 
	        }
	        catch (Throwable err) {
	            error_handler(err);
	        }  
    	} 
    	catch (Throwable err) { 
    		error_handler(err); 
        }
        
        return false; 
    }
    
    /** 
     * DB Check: Check if room number for given hotel is currently occupied
     * 
     * Arguments -  roomNumber    - Room number
     *              hotelId       - Hotel Id 
     * Return -     boolean       - True if the room number for given hotel is currently occupied by guests
     * 
     * Modifications:   03/25/18 -  MTA -  Added method. 
     */
    public static boolean support_isRoomCurrentlyOccupied(int hotelId, int roomNumber){  
    	
    	try {      		
	        try {	        				 
				 jdbcPrep_isRoomCurrentlyOccupied.setInt(1, hotelId);
				 jdbcPrep_isRoomCurrentlyOccupied.setInt(2, roomNumber);   
				 
				 ResultSet rs = jdbcPrep_isRoomCurrentlyOccupied.executeQuery();
				 int cnt = 0;
				 
				 while (rs.next()) {
					cnt = rs.getInt("CNT"); 	
				 }
				 
				 if (cnt > 0) { 
					 return true;
				 }   
				 
	        }
	        catch (Throwable err) {
	            error_handler(err);
	        } 
    	} 
    	catch (Throwable err) { 
    		error_handler(err); 
        }
        
        return false; 
    }
    
    /** 
     * DB Check: Check if customer exists in Customers table
     * 
     * Arguments -  ID -        Customer ID  
     * Return -     boolean -   True if the customer is present in Customers table
      * 
     * Modifications:   03/28/18 -  MTA -   Added method.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     *                  04/05/18 -  ATTD -  Remove extraneous try-catch.
     */
    public static boolean support_isValidCustomer(String ID){  
    	
        try {               
            jdbcPrep_isValidCustomer.setInt(1, Integer.parseInt(ID)); 
             
             ResultSet rs = jdbcPrep_isValidCustomer.executeQuery();
             int cnt = 0;
             
             while (rs.next()) {
                cnt = rs.getInt("CNT");     
             }
             
             if (cnt == 1) { 
                 return true;
             }   
             
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
        return false; 
    }
    
    /** 
     * DB Check: Check if customer is associated with current guest stay 
     * 
     * Arguments -  ID -        Customer ID              
     * Return -     boolean-    True if the customer is associated with current guest stay 
     * 
     * Modifications:   03/28/18 -  MTA -   Added method.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     */
    public static boolean support_isCustomerCurrentlyStaying(String ID){  
    	
    	try {      		
	        try {	        				 
				 jdbcPrep_isCustomerCurrentlyStaying.setInt(1, Integer.parseInt(ID)); 
				 
				 ResultSet rs = jdbcPrep_isCustomerCurrentlyStaying.executeQuery();
				 int cnt = 0;
				 
				 while (rs.next()) {
					cnt = rs.getInt("CNT"); 	
				 }
				 
				 if (cnt > 0) { 
					 return true;
				 }   
				 
	        }
	        catch (Throwable err) {
	            error_handler(err);
	        } 
    	} 
    	catch (Throwable err) { 
    		error_handler(err); 
        }
        
        return false; 
    }
    
    /** 
     * DB Check: Check if given stay id exists in the database
     * 
     * Arguments -  stayId       - Stay Id  
     * Return -     boolean       - True if the stay id exists
     * 
     * Modifications:   04/05/18 -  MTA -  Added method. 
     */
    public static boolean support_isValidStayID(int stayId) {
    	try {   	        
    		try {  				 
    			jdbcPrep_isValidStayID.setInt(1, stayId); 
				 
				 ResultSet rs = jdbcPrep_isValidStayID.executeQuery();
				 int cnt = 0;
				 
				 while (rs.next()) {
					cnt = rs.getInt("CNT"); 	
				 }
				 
				 if (cnt > 0) { 
					 return true;
				 }   
				 
	        }
	        catch (Throwable err) {
	            error_handler(err);
	        }  
    	} 
    	catch (Throwable err) { 
    		error_handler(err); 
        }
        
        return false; 
    }

    /**
     * Task: Helper method to get data from user
     * 
     * Arguments: operation - Operation the user is performing (ADD_ROOM / UPDATE_ROOM / DELETE_ROOM)  
     *            fieldName - Name of the field (used while checking if entered data is sane)
     *            message - The message asking user to enter the data 
     *            params(Optional) - Extra parameters needed to validate the sanity 
     * 
     * Returns: Valid user entered data 
     * 
     * Modifications:   03/24/18 -  MTA -   Added method
     *                  03/28/18 -  ATTD -  Use same field names as in tables themselves
     *                                      - for developer ease
     *                                      - because "support_isValueSane" method uses table attribute names
     *                  04/04/18 -  ATTD -  Fix bug causing add customer to never accept any SSN.
     *                                          Make customer ID the primary key, and SSN just another attribute, per demo data.
     *                  04/05/18 -  MTA -   Added validations for Start and End Date fields while generating report by date range.
     *                  04/13/18 -  ATTD -  Add ">" prompt when asking user for data.
     */
    public static String support_getValidDataFromUser (String operation, String fieldName, String message, String...params ){
        
        boolean isValid = false;
        int attempt = 0;
        String value = "";
        
        // Repeat till user enters a correct field value
        while(!isValid) {
            // Ask user to enter/re-enter the data
            String messagePrefix = (attempt == 0) ? "\n" : "\nRe-";
            String messagePostfix = (attempt == 0) ? "\n> " : "Else press q to go back to previous menu\n> ";
            System.out.println(messagePrefix + message);
            System.out.print(messagePostfix);
            value = scanner.nextLine(); 
            
            if (value.equalsIgnoreCase("q")) {
                return "<QUIT>";
            }
            
            if (fieldName.equalsIgnoreCase("HotelId")) {
                boolean isSane = support_isValueSane(fieldName, value); 
                if (isSane) {  
                    // Extra checks for Hotel Id when deleting room:
                    // 1. Check if the entered hotel id is valid
                    boolean support_isValidHotelID = support_isValidHotelID(Integer.parseInt(value));
                    if (support_isValidHotelID) { 
                        isValid = true;  
                    } else {
                        System.out.println("ERROR: The entered hotel id does not exist in database");
                    }  
                }  
            } 
            
            else if (fieldName.equalsIgnoreCase("RoomNum") && (operation.equals("DELETE_ROOM") || operation.equals("UPDATE_ROOM") || operation.equals("RELEASE_ROOM"))) {
                boolean isSane = support_isValueSane(fieldName, value); 
                if (isSane) {  
                    // Extra checks for Room Number when deleting/updating room :
                    // 1. Check if the entered room number is present for given hotel
                    boolean isAssociated = support_isValidRoomForHotel(Integer.parseInt(params[0]), Integer.parseInt(value));
                    if (isAssociated) {  
                        boolean isRoomOccupied = support_isRoomCurrentlyOccupied(Integer.parseInt(params[0]), Integer.parseInt(value)); 
                        // 2. If updating/deleting the room, make sure there are no guests staying in this room currently 
                        if (operation.equals("DELETE_ROOM") || operation.equals("UPDATE_ROOM")) {
                            if (!isRoomOccupied) {
                                isValid = true;  
                            } else { 
                                System.out.println("ERROR: The room is currently occupied, hence cannot be modified"); 
                            }   
                        // 2. If releasing the room, make sure it is currently occupied
                        } else if (operation.equals("RELEASE_ROOM")){
                            if (isRoomOccupied) {
                                isValid = true;  
                            } else { 
                                System.out.println("ERROR: The room is currently not occupied, hence cannot be released"); 
                            }   
                        } 
                    } else { 
                        System.out.println("ERROR: The room number is not associated with this hotel");   
                    }  
                }  
            } 
            
            else if (fieldName.equalsIgnoreCase("RoomNum") && operation.equals("ADD_ROOM")) {
                 
                boolean isSane = support_isValueSane(fieldName, value); 
                if (isSane) { 
                    // Extra checks for Room Number when adding room:
                    // 1.check if the entered room number is not already present for given hotel
                    boolean isAssociated = support_isValidRoomForHotel(Integer.parseInt(params[0]), Integer.parseInt(value));
                    if (!isAssociated) { 
                        isValid = true; 
                    } else {
                        System.out.println("ERROR: This room number is already associated with different room in this hotel");  
                    }
                }     
            }  
            
            else if (fieldName.equalsIgnoreCase("ID")) {
             
                boolean isSane = support_isValueSane(fieldName, value);
                if (operation.equals("UPDATE_CUSTOMER") || operation.equals("DELETE_CUSTOMER")) {
                    if (isSane) {
                        // Extra checks for ID when updating / deleting customer info:
                        // 1.check if the entered ID is valid i.e exists in Customers table
                        boolean isExistingCustomer = support_isValidCustomer(value);
                        if (isExistingCustomer) {  
                            if (operation.equals("DELETE_CUSTOMER")) {
                                // Extra checks for ID when deleting customer info:
                                // 2. Check if customer is currently staying in any hotel
                                boolean isCurrentlyStaying = support_isCustomerCurrentlyStaying(value);
                                if (!isCurrentlyStaying) {
                                    isValid = true; 
                                } else {
                                    System.out.println("ERROR: The customer is associated with a current guest stay"); 
                                }                               
                            } else {
                                isValid = true; 
                            }                           
                        } else {
                            System.out.println("ERROR: The entered ID does not exist in our database");  
                        }
                    }  
                }
                else {
                    isValid = isSane;
                }
                   
            } 
            
            else if (fieldName.equalsIgnoreCase("StartDate")) {
                boolean isSane = support_isValueSane(fieldName, value); 
                 
                if (isSane) {  
                    // Extra checks for Start Date when reporting occupancy by date range :
                    // 1. Check if the Start Date is less than current date
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
                    Date currentDate,startDate;
                    try {
                        currentDate = new Date();
                        startDate = df.parse(value);
                        boolean isValidStartDate = startDate.before(currentDate);
                        if (isValidStartDate) { 
                            isValid = true;  
                        } else {
                            System.out.println("ERROR: Entered start date must be less than the current date");
                        }  
                    } catch (ParseException e) {
                        System.out.println("Start Date must be entered in the format 'YYYY-MM-DD'");
                        e.printStackTrace();
                    }  
                }  
            } 
            
            else if (fieldName.equalsIgnoreCase("EndDate")) {
                boolean isSane = support_isValueSane(fieldName, value); 
                 
                if (isSane) {  
                    // Extra checks for End Date when reporting occupancy by date range :                   
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
                    Date startDate, endDate;
                    try { 
                        endDate = df.parse(value);
                        startDate = df.parse(params[0]);
                        // 1. End Date is after Start Date
                        if (endDate.after(startDate)) { 
                            isValid = true;  
                        } else {
                            System.out.println("ERROR: Entered end date must be greater than the start date");
                        }  
                    } catch (ParseException e) {
                        System.out.println("End Date must be entered in the format 'YYYY-MM-DD'");
                        e.printStackTrace();
                    }  
                }  
            }  
            
            else if (fieldName.equalsIgnoreCase("StayID")) {
                boolean isSane = support_isValueSane(fieldName, value); 
                if (isSane) {  
                    // Extra checks for Stay id when reporting Staff serving customer during stay:
                    // 1. Check if the Stay id is valid
                    boolean support_isValidStayID = support_isValidStayID(Integer.parseInt(value));
                    if (support_isValidStayID) { 
                        isValid = true;  
                    } else {
                        System.out.println("ERROR: The entered stay id does not exist in database");
                    }  
                }  
            }
            
            else {
                isValid = support_isValueSane(fieldName, value);
            }
            
            attempt++;
        } 
        
        // Now the data is valid, return it
        return value;
    }
    
    /** 
     * Print query result set
     * Modified from, but inspired by: https://coderwall.com/p/609ppa/printing-the-result-of-resultset
     * 
     * Arguments -  resultSetToPrint -  The result set to print
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Made printout slightly prettier.
     *                  03/08/18 -  ATTD -  At the end, print number of records in result set.
     *                  03/21/18 -  ATTD -  Print column label instead of column name.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     *                  04/01/18 -  ATTD -  Make the result set print out correctly regardless of contents
     *                                      (at the expense of speed - will run slower now, but still seems quite fast enough).
     */
    public static void support_printQueryResultSet(ResultSet resultSetToPrint) {
        
        try {
            
            // Declare variables
            ArrayList<Integer> columnWidths = new ArrayList<>();
            ResultSetMetaData metaData;
            String columnName;
            String tupleValue;
            int columnWidth;
            int numColumns;
            int numTuples;
            int i;

            // Is there anything useful in the result set?
            if (jdbc_result.next()) {
                
                // Get metadata
                metaData = jdbc_result.getMetaData();
                numColumns = metaData.getColumnCount();
                numTuples = 0;
                do {
                    numTuples++;
                }
                while (jdbc_result.next());
                jdbc_result.beforeFirst();
                
                // Figure out how many chars to use for each column
                for (i = 1; i <= numColumns; i++) {
                    columnName = metaData.getColumnLabel(i);
                    columnWidth = columnName.length();
                    while (jdbc_result.next()) {
                        tupleValue = jdbc_result.getString(i);
                        // Tuple value could be null, if so that's 4 chars to print ("NULL")
                        if (tupleValue == null) {
                            if (4 > columnWidth) {
                                columnWidth = 4;
                            }
                        }
                        else if (tupleValue.length() > columnWidth) {
                            columnWidth = tupleValue.length();
                        }
                    }
                    columnWidths.add(columnWidth + 1);
                    jdbc_result.beforeFirst();
                }

                /* Print column headers
                 * use column label instead of column name,
                 * otherwise you will not see the effect of aliasing
                 */
                for (i = 1; i <= numColumns; i++) {
                    columnName = metaData.getColumnLabel(i);
                    System.out.print(support_padRight(columnName, columnWidths.get(i-1)));
                }
                System.out.println("");
                System.out.println("");
                
                // Go through the result set tuple by tuple
                while (jdbc_result.next()) {
                    for (i = 1; i <= numColumns; i++) {
                        tupleValue = jdbc_result.getString(i);
                        System.out.print(support_padRight(tupleValue, columnWidths.get(i-1)));
                    }
                    System.out.print("\n");
                }
                
                // Print number of records found
                System.out.println("");
                System.out.println("(" + numTuples + " entries)");
                System.out.println("");
                
            }
            else {
                // Tell the user that the result set is empty
                System.out.println("(no results)\n");
            }
            
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
    }
    
    /** 
     * Pad a string with space characters to reach a given number of total characters
     * https://stackoverflow.com/questions/388461/how-can-i-pad-a-string-in-java/391978#391978
     * 
     * Arguments -  stringIn -          The result set to print
     *              numDesiredChars -   The desired number of characters in the padded result
     * Return -     stringOut -         The padded string
     * 
     * Modifications:   03/08/18 -  ATTD -  Created method.
     *                  03/23/18 -  ATTD -  Use new general error handler.
     */
    public static String support_padRight(String stringIn, int numDesiredChars) {
        
        // Declare variables
        String stringOut = stringIn;
        
        try {
            // Pad string
            stringOut = String.format("%1$-" + numDesiredChars + "s", stringIn); 
        }
        catch (Throwable err) {
            error_handler(err);
        }
        
        return stringOut;
         
    }
    
    /** 
     * Given a value, and an indication of its intended meaning, determine if it's "sane"
     * This method exists in part because the version of MariaDB suggested for use in this project
     * supports neither CHECK nor ASSERTION!
     * 
     * Arguments -  attributeName - The name of the attribute ("PhoneNum", "Address", etc)
     *              proposedValue - The proposed value for the attribute (as a string)
     * Return -     okaySoFar -     True if the value seems sane at first glance
     * 
     * Modifications:   03/23/18 -  ATTD -  Created method.
     *                  03/24/18 -  MTA -   Added validations for fields when adding a new room.
     *                  03/27/18 -  MTA -   Added validations for fields when adding a new customer.
     *                  03/28/18 -  ATTD -  Combined / removed redundancies for checks:
     *                                      - Customer date of bBirth
     *                                      - Customer email
     *                                      - Customer phone number
     *                                      - Customer name
     *                                      - Customer SSN
     *                                      Used "equalsIgnoreCase" more widely (better than "equals").
     *                  04/04/18 -  ATTD -  Allow 3-digit phone numbers per demo data.
     *                                      Allow 4-digit SSNs per demo data.
     *                                      Changing room categories to match those given in demo data.
     *                  04/08/18 -  MTA -  Added validations for assigning rooms to fix defects
     */
    public static boolean support_isValueSane(String attributeName, String proposedValue) {
        
        // Declare local variables
        boolean okaySoFar = true;
        
        try {
            
            if (okaySoFar && attributeName.length() == 0) {
                System.out.println("Attribute not entered (cannot proceed)\n");
                okaySoFar = false;
            }
            if (okaySoFar && proposedValue.length() == 0) {
                System.out.println("Value not entered (cannot proceed)\n");
                okaySoFar = false;
            }
            /* Check for malformed state
             * DBMS seems to accept a malformed date with no complaints
             * even though we define as CHAR(2)
             */
            if (okaySoFar && attributeName.equalsIgnoreCase("State") && proposedValue.length() != 2) {
                System.out.println("State '" + proposedValue + "' malformed, should have 2 letters (cannot proceed)\n");
                okaySoFar = false;
            }
            // Check for malformed phone number
            if (okaySoFar && attributeName.equalsIgnoreCase("PhoneNum")) {
                try {
                    if (Long.parseLong(proposedValue) <= 0) {
                        System.out.println("Phone number '" + proposedValue + "' malformed, should be positive (cannot proceed)\n");
                        okaySoFar = false;
                    } else if (proposedValue.length() < 3) {
                        System.out.println("Phone number '" + proposedValue + "' malformed, should have at least 3 digits (cannot proceed)\n");
                        okaySoFar = false;
                    }  
                }
                catch(NumberFormatException nfe) {
                   System.out.println("Phone number '" + proposedValue + "' malformed, should be a number (cannot proceed)\n");
                   okaySoFar = false;
               }  
            }
            // Check for malformed SSN
            if (okaySoFar && attributeName.contains("SSN")) {
                try {
                    if (Long.parseLong(proposedValue) <= 0) {
                        System.out.println("SSN '" + proposedValue + "' malformed, should be positive (cannot proceed)\n");
                        okaySoFar = false;
                    } else if (proposedValue.length() < 4) {
                        System.out.println("SSN '" + proposedValue + "' malformed, should have at least 4 digits (cannot proceed)\n");
                        okaySoFar = false;
                    }  
                }
                catch(NumberFormatException nfe) {
                   System.out.println("SSN '" + proposedValue + "' malformed, should be a number (cannot proceed)\n");
                   okaySoFar = false;
               }  
            }
            /* Check for malformed date
             * DBMS seems to accept a malformed date with no complaints
             * https://stackoverflow.com/questions/2149680/regex-date-format-validation-on-java
             */
            if (okaySoFar && (attributeName.contains("Date") || attributeName.equalsIgnoreCase("DOB") ) && proposedValue.matches("\\d{4}-\\d{2}-\\d{2}") == false) {
                System.out.println("Date must be entered in the format 'YYYY-MM-DD' (cannot proceed)\n");
                okaySoFar = false;
            }
            /* Check for "bad" manager
             * Don't know of a way to have DBMS check that manager isn't dedicated to a presidential suite (ASSERTION not supported)
             */
            if (okaySoFar && attributeName.equalsIgnoreCase("ManagerID")) {
                jdbc_result = jdbc_statement.executeQuery(
                    "SELECT Staff.ID, Staff.Name, Rooms.RoomNum, Rooms.hotelID " + 
                    "FROM Staff, Rooms " + 
                    "WHERE Staff.ID = " + 
                    Integer.parseInt(proposedValue) + 
                    " AND (Rooms.DRSStaff = " + Integer.parseInt(proposedValue) + " OR Rooms.DCStaff = " + Integer.parseInt(proposedValue) + ")");
                
                if (jdbc_result.next()) {
                    System.out.println("\nThis manager cannot be used, because they are already dedicated to serving a presidential suite\n");
                    jdbc_result.beforeFirst();
                    support_printQueryResultSet(jdbc_result);
                    okaySoFar = false;
                }
            }
            
            /* ******************************** VALIDATIONS FOR ADDING NEW ROOM ************************************* */
           
            // Check if entered hotel id for room is valid ( i.e non-negative number) 
            try{
                 if (attributeName.equalsIgnoreCase("HotelId") && Integer.parseInt(proposedValue) <= 0) {
                     System.out.println("\nERROR: Hotel ID should be a positive number");
                     okaySoFar = false;
                 }  
            } catch(NumberFormatException nfe) {
                System.out.println("\nERROR: Hotel ID should be a number");
                okaySoFar = false;
            }
            
            // Check if entered room number is valid ( i.e non-negative number)
            try{
                 if (attributeName.equalsIgnoreCase("RoomNum") && Integer.parseInt(proposedValue) <= 0) {
                     System.out.println("\nERROR: Room Number should be a positive number");
                     okaySoFar = false;
                 }  
            } catch(NumberFormatException nfe) {
                System.out.println("\nERROR: Room number should be a number");
                okaySoFar = false;
            }  
            
            // Check if entered room category is valid ( i.e 'Economy', 'Deluxe', 'Executive Suite', 'Presidential Suite' )
            if (
                attributeName.equalsIgnoreCase("Category") && 
                 !(
                     proposedValue.equalsIgnoreCase("Economy") || 
                     proposedValue.equalsIgnoreCase("Deluxe") || 
                     proposedValue.equalsIgnoreCase("Executive") || 
                     proposedValue.equalsIgnoreCase("Presidential")
                 )
             ) {
                    System.out.println("\nERROR: Allowed values for room category are 'Economy', 'Deluxe', 'Executive', 'Presidential' ");
                    okaySoFar = false; 
            } 
            
            // Check if entered room max occupancy is valid ( i.e non-negative number)
            try{
                 if (attributeName.equalsIgnoreCase("MaxOcc")) {
                     if (Integer.parseInt(proposedValue) <= 0) {
                         System.out.println("\nERROR: Room Max Occupancy should be a positive number");
                         okaySoFar = false;     
                     } 
                     if (Integer.parseInt(proposedValue) > 4) {
                         System.out.println("\nERROR: Maximum allowed value for Room Occupancy is 4");
                         okaySoFar = false;     
                     } 
                 }
            } catch(NumberFormatException nfe) {
                System.out.println("\nERROR: Room Max Occupancy should be a number");
                okaySoFar = false;
            }
             
            // Check if entered Room Nightly rate is valid ( i.e non-negative number)
            try{
                 if (attributeName.equalsIgnoreCase("NightlyRate") && Integer.parseInt(proposedValue) <= 0) {
                     System.out.println("\nERROR: Room Nightly rate should be a positive number");
                     okaySoFar = false;
                 }
            } catch(NumberFormatException nfe) {
                System.out.println("\nERROR: Room Nightly rate should be a number");
                okaySoFar = false;
            } 
           // ---------Validation for assigning room-------------
            ///check for assigning room if paymentmethod values are valid
            if (
                attributeName.equalsIgnoreCase("PaymentMethod") && 
                 !(
                     proposedValue.equalsIgnoreCase("Card") || 
                     proposedValue.equalsIgnoreCase("Cash") 
                 )
             ) {
                    System.out.println("\nERROR: Allowed values for room category are 'Card' or 'Cash' ");
                    okaySoFar = false; 
            } 

            ///check for assigning room if cardtype values are valid
            if (
                attributeName.equalsIgnoreCase("CardType") && 
                 !(
                     proposedValue.equalsIgnoreCase("Visa") || 
                     proposedValue.equalsIgnoreCase("Hotel") ||
                     proposedValue.equalsIgnoreCase("Mastercard")
                 )
             ) {
                    System.out.println("\nERROR: Allowed values for room category are 'Visa', 'Mastercard', or 'Hotel' ");
                    okaySoFar = false; 
            } 
            ////check that the cardnumber is a integer
            try{
                if (attributeName.equalsIgnoreCase("CardNumber") && Long.parseLong(proposedValue) <= 0) {
                    System.out.println("\nERROR: Room Nightly rate should be a positive number");
                    okaySoFar = false;
                }
           } catch(NumberFormatException nfe) {
               System.out.println("\nERROR: Room Nightly rate should be a number");
               okaySoFar = false;
           } 
           ////////check for customerID 
           if (okaySoFar && attributeName.equalsIgnoreCase("CustomerID")) {
                jdbc_result = jdbc_statement.executeQuery(
                    "SELECT ID " + 
                    "FROM Customers " + 
                    "WHERE ID = " + 
                    Integer.parseInt(proposedValue) + ";");
            
                if (!jdbc_result.next()) {
                    System.out.println("\nThis is not a valid CustomerID\n");
                    okaySoFar = false;
                }
            }
            
             
        }
        catch (Throwable err) {
            error_handler(err);
            okaySoFar = false;
        }

        // Return
        return okaySoFar;
        
    }
    
    /** 
     * General error handler
     * Turn obscure error stack into human-understandable feedback
     * 
     * Arguments -  err -   An error object.
     *           -  pkViolation - Name of the primary key constraint being violated
     * Return -     None
     * 
     * Modifications:   03/23/18 -  ATTD -  Created method.
     *                  03/24/18 -  MTA -   Handle primary key violation.
     *                  03/28/18 -  ATTD -  Handle unknown column in WHERE clause.
     *                  04/04/18 -  ATTD -  Make customer ID the primary key, and SSN just another attribute, per demo data.
     *                  04/09/18 -  ATTD -  Changed the FK_ROOMDCID and FK_ROOMDRSID messages to reflect better message inclusive to when 
     *                                          assigning presidential suite fails due to these errors (could not find staffID=0)
     */
    public static void error_handler(Throwable err, String... pkViolation) {
        
        // Declare variables
        String errorMessage;
        
        try {
            
            // Handle specific errors
            errorMessage = err.toString();
            
            // HOTELS constraint violated
            if (errorMessage.contains("UC_HACS")) {
                System.out.println(
                    "\nCannot use this address / city / state for the hotel, because it is already used for another hotel\n"
                );
            }
            else if (errorMessage.contains("UC_HPN")) {
                System.out.println(
                    "\nCannot use this phone number for the hotel, because it is already used for another hotel\n"
                );
            }
            else if (errorMessage.contains("UC_HMID")) {
                System.out.println(
                    "\nCannot use this manager for the hotel, because they are already managing another hotel\n"
                );
            }
            else if (errorMessage.contains("FK_HMID")) {
                System.out.println(
                        "\nCannot use this manager for the hotel, because they are not registered as a Wolf Inns staff member\n"
                    );
            }
            else if (errorMessage.contains("PK_HOTELS")) {
                System.out.println(
                    "\nCannot use this ID for the hotel, because it is already used for another hotel\n"
                );
            }
            
            // STAFF constraint violated
            else if (errorMessage.contains("FK_STAFFHID")) {
                System.out.println(
                    "\nCannot assign the staff member to this hotel, because it is not registered as a Wolf Inns hotel\n"
                );
            }
            else if (errorMessage.contains("PK_STAFF")) {
                System.out.println(
                    "\nCannot use this ID for the Staff, because it is already used for another staff member\n"
                );
            }
            
            // ROOMS constraint violated
            else if (errorMessage.contains("FK_ROOMHID")) {
                System.out.println(
                    "\nCannot assign the room to this hotel, because it is not registered as a Wolf Inns hotel\n"
                );
            }
            else if (errorMessage.contains("FK_ROOMDRSID")) {
                System.out.println(
                    "\nCould not assign dedicated catering Staff, this prevents successful assignment of the Presidential Suite.\n"
                );
            }
            else if (errorMessage.contains("FK_ROOMDCID")) {
                System.out.println(
                    "\nCould not assign dedicated catering Staff, this prevents successful assignment of the Presidential Suite.\n"
                );
            }
            else if (errorMessage.contains("PK_ROOMS") || (pkViolation.length > 0 && pkViolation[0].contains("PK_ROOMS"))) {
            	System.out.println(
                    "\nCannot use this room number for the hotel, because it is already used for another room\n"
                );
            }
            
            // STAYS constraint violated
            else if (errorMessage.contains("UC_STAYKEY")) {
                System.out.println(
                    "\nCannot use this combination of start date / check in time / room number / hotel ID the stay, because it is already used for another stay\n"
                );
            }
            else if (errorMessage.contains("FK_STAYRID")) {
                System.out.println(
                    "\nCannot use this combination of room number / hotel ID the stay, because it is not registered as a Wolf Inns hotel room\n"
                );
            }
            else if (errorMessage.contains("FK_STAYCID")) {
                System.out.println(
                    "\nCannot use this customer ID the stay, because there is no registered Wolf Inns customer with that ID\n"
                );
            }
            else if (errorMessage.contains("PK_STAYS")) {
            	System.out.println(
                    "\nCannot use this ID for the stay, because it is already used for another stay\n"
                );
            }
                        
            // PROVIDED constraint violated
            else if (errorMessage.contains("FK_PROVSTAYID")) {
                System.out.println(
                    "\nCannot use this stay ID for the provided service, because it is not registered as a Wolf Inns customer stay\n"
                );
            }
            else if (errorMessage.contains("FK_PROVSTAFFID")) {
                System.out.println(
                    "\nCannot use this staff member for the provided service, because they are not registered as a Wolf Inns staff member\n"
                );
            }
            else if (errorMessage.contains("FK_PROVSERV")) {
                System.out.println(
                    "\nCannot use this service name for the provided service, because it is not registered as a Wolf Inns available service\n"
                );
            }
            else if (errorMessage.contains("PK_PROVIDED")) {
                System.out.println(
                    "\nCannot use this ID for the service provided, because it is already used for another provided service\n"
                );
            }
            
            // Customer constraint violated
            else if (errorMessage.contains("PK_CUSTOMERS") || (pkViolation.length > 0 && pkViolation[0].contains("PK_CUSTOMERS"))) {
                System.out.println(
                    "\nCannot use this ID for the customer, because it is already used for another customer\n"
                );
            }
            
            // SERVICETYPES constraint violated
            else if (errorMessage.contains("PK_SERVICE_TYPES")) {
                System.out.println(
                    "\nCannot use this name for the service type, because it is already used for another service type\n"
                );
            }
            
            // Used bad column (non-existent attribute)
            else if (errorMessage.contains("Unknown column")) {
                System.out.println(
                    "\nCannot use this attribute - it does not exist (check spelling)\n"
                );
            }
            
            // Number format error
            else if (errorMessage.contains("NumberFormatException")) {
                System.out.println("Cannot use this value because it is not a number\n");
            }
            
            // Illegal Argument Exception
            else if (errorMessage.contains("IllegalArgumentException")) {
                System.out.println("Cannot use this value because it it not valid\n");
            }
            
            // Primary Key Violation
            else if (errorMessage.contains("SQLIntegrityConstraintViolationException")) {
                System.out.println("Primary Key Constraint violated\n");
            }            
            
            // Don't know what happened, best we can do is print the stack trace as-is
            else {
                err.printStackTrace();
            }
            
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        
    }
    
    // MAIN
    
    /* MAIN function
     * 
     * Welcomes the user
     * States available commands
     * Listens to and acts on user commands
     * Closes resources upon "QUIT"
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Add ability to print entire Provided table.
     *                  03/08/18 -  ATTD -  Add sub-menus (report, etc) off of main menu.
     *                  03/09/18 -  ATTD -  Add ability to delete a hotel.
     *                  03/11/18 -  ATTD -  Add ability to report revenue.
     *                  03/11/18 -  ATTD -  Add ability to generate bill for customer stay.
     *                  03/12/18 -  ATTD -  Add ability to delete staff member.
     *                  03/20/18 -  ATTD -  Add call to new method for creating prepared statements.
     *                  03/21/18 -  ATTD -  Close more resources during clean-up.
     *                  03/23/18 -  ATTD -  Add ability to update basic information about a hotel.
     *                                      Use new general error handler.
     *                  03/24/18 -  MTA -   Add ability to add room.
     *                  03/24/18 -  ATTD -  Close prepared statement for deleting a hotel.
     *                  03/24/18 -  ATTD -  Add ability to insert new staff member.
     *                  03/26/18 -  ATTD -  Add ability to update basic info about a staff member.
     *                  03/27/18 -  ATTD -  Use prepared statement to delete staff.
     *                                      Add ability to check if rooms are available.
     *                  03/27/18 -  MTA -   Add ability to add, update and delete customer.
     *                  04/01/18 -  ATTD -  Add ability to assign a room to a customer.
     *                  04/04/18 -  ATTD -  Add attribute for hotel zip code, per demo data.
     *                                      Make customer ID the primary key, and SSN just another attribute, per demo data.
     *                                      Remove prepared statement to update hotel ID over a range of staff IDs,
     *                                          no longer makes sense with tables populated with demo data.
     *                  04/05/18 -  ATTD -  Create streamlined checkout (generate receipt & bill, release room).
     *                  04/06/18 -  ATTD -  Add ability to enter a new service record.
     *                  04/06/18 -  ATTD -  Add ability to update a service record.
     *                  04/07/18 -  ATTD -  Debug ability to update a service record.
     *                  04/08/18 -  ATTD -  Fix bug keeping dedicated staff from being assigned to presidential suite.
     *                  04/09/18 -  ATTD -  Added 'Quit'functionality to all submenus
     */
    public static void main(String[] args) {
        
        try {
        
            // Declare local variables
            boolean quit = false;
            String command;
            
            // Print welcome
            System.out.println("\nWelcome to Wolf Inns Hotel Management System");
            
            // Connect to database
            System.out.println("\nConnecting to database...");
            startup_connectToDatabase();
            
            // Create prepared statements
            System.out.println("\nCreating prepared statements...");
            startup_createPreparedStatements();
            
            // Create tables
            System.out.println("\nCreating tables...");
            startup_createTables();
            
            // Populate tables
            System.out.println("\nPopulating tables...");
            populate_Customers();
            populate_ServiceTypes();
            populate_Staff();
            populate_Hotels();
            populate_updateHotelIdForStaff();
            populate_Rooms();
            populate_Stays();
            populate_Provided();
            populate_updateAmountOwedForStays();
            
            // Print available commands
            startup_printAvailableCommands(CMD_MAIN);
            
            // Watch for user input
            currentMenu = CMD_MAIN;
            scanner = new Scanner(System.in);
            while (quit == false) {
                System.out.print("> ");
                command = scanner.nextLine();
                switch (currentMenu) {
                    case CMD_MAIN:
                        // Check user's input (case insensitively)
                        switch (command.toUpperCase()) {
                            case CMD_FRONTDESK:
                                // Tell the user their options in this new menu
                                startup_printAvailableCommands(CMD_FRONTDESK);
                                // Remember what menu we're in
                                currentMenu = CMD_FRONTDESK;
                            break;
                            case CMD_REPORTS:
                                // Tell the user their options in this new menu
                                startup_printAvailableCommands(CMD_REPORTS);
                                // Remember what menu we're in
                                currentMenu = CMD_REPORTS;
                                break;
                            case CMD_MANAGE:
                                // Tell the user their options in this new menu
                                startup_printAvailableCommands(CMD_MANAGE);
                                // Remember what menu we're in
                                currentMenu = CMD_MANAGE;
                                break;
                            case CMD_QUIT:
                                quit = true;
                                break;
                            default:
                                // Remind the user about what commands are available
                                System.out.println("\nCommand not recognized");
                                startup_printAvailableCommands(CMD_MAIN);
                                break;
                        }
                        break;
                    case CMD_FRONTDESK:
                        // Check user's input (case insensitively)
                        switch (command.toUpperCase()) {
                            case CMD_FRONTDESK_CHECKOUT:
                                user_frontDeskCheckOut();
                                break;
                            case CMD_FRONTDESK_AVAILABLE:
                                user_frontDeskCheckAvailability();
                                break;
                            case CMD_FRONTDESK_CHECKIN:
                                user_frontDeskCheckIn();
                                break;
                            case CMD_FRONTDESK_ENTER_SERVICE:
                                user_frontDeskEnterService();
                                break;
                            case CMD_FRONTDESK_UPDATE_SERVICE:
                                user_frontDeskUpdateService();
                                break;
                            case CMD_MAIN:
                                // Tell the user their options in this new menu
                                startup_printAvailableCommands(CMD_MAIN);
                                // Remember what menu we're in
                                currentMenu = CMD_MAIN;
                                break;
                            case CMD_QUIT:
                                quit = true;
                                break;
                            default:
                                // Remind the user about what commands are available
                                System.out.println("\nCommand not recognized");
                                startup_printAvailableCommands(CMD_FRONTDESK);
                                break;
                        }
                        break;
                    case CMD_REPORTS:
                        // Check user's input (case insensitively)
                        switch (command.toUpperCase()) {
                            case CMD_REPORT_REVENUE:
                                user_reportHotelRevenue();
                            break;
                            case CMD_REPORT_HOTELS:
                                user_reportEntireTable("Hotels");
                                break;
                            case CMD_REPORT_ROOMS:
                                user_reportEntireTable("Rooms");
                                break;
                            case CMD_REPORT_STAFF:
                                user_reportEntireTable("Staff");
                                break;
                            case CMD_REPORT_CUSTOMERS:
                                user_reportEntireTable("Customers");
                                break;
                            case CMD_REPORT_STAYS:
                                user_reportEntireTable("Stays");
                                break;
                            case CMD_REPORT_SERVICES:
                                user_reportEntireTable("ServiceTypes");
                                break;
                            case CMD_REPORT_PROVIDED:
                                user_reportEntireTable("Provided");
                                break;                           
                            case CMD_REPORT_OCCUPANCY_BY_HOTEL:
                                user_reportOccupancyByHotel();
                                break;
                            case CMD_REPORT_OCCUPANCY_BY_ROOM_TYPE:
                            	user_reportOccupancyByRoomType();
                            	break;
                            case CMD_REPORT_OCCUPANCY_BY_DATE_RANGE:
                            	user_reportOccupancyByDateRange();
                            	break;
                            case CMD_REPORT_OCCUPANCY_BY_CITY:
                            	user_reportOccupancyByCity();
                            	break;
                            case CMD_REPORT_TOTAL_OCCUPANCY:
                            	user_reportTotalOccupancy();
                            	break;
                            case CMD_REPORT_PERCENTAGE_OF_ROOMS_OCCUPIED:
                            	user_reportPercentageOfRoomsOccupied();
                            	break;
                            case CMD_REPORT_STAFF_GROUPED_BY_ROLE:
                            	user_reportStaffGroupedByRole();
                            	break;
                            case CMD_REPORT_STAFF_SERVING_DURING_STAY:
                            	user_reportStaffServingDuringStay();
                            	break;
                            case CMD_MAIN:
                                // Tell the user their options in this new menu
                                startup_printAvailableCommands(CMD_MAIN);
                                // Remember what menu we're in
                                currentMenu = CMD_MAIN;
                                break;
                            case CMD_QUIT:
                                quit = true;
                                break;
                            default:
                                // Remind the user about what commands are available
                                System.out.println("\nCommand not recognized");
                                startup_printAvailableCommands(CMD_REPORTS);
                                break;
                        }
                        break;
                    case CMD_MANAGE:
                        // Check user's input (case insensitively)
                        switch (command.toUpperCase()) {
                        case CMD_MANAGE_HOTEL_ADD:
                            user_manageHotelAdd();
                            break;
                        case CMD_MANAGE_HOTEL_UPDATE:
                            user_manageHotelUpdate();
                            break;
                        case CMD_MANAGE_HOTEL_DELETE:
                            user_manageHotelDelete();
                            break;
                        case CMD_MANAGE_STAFF_ADD:
                            user_manageStaffAdd();
                            break;
                        case CMD_MANAGE_STAFF_UPDATE:
                            user_manageStaffUpdate();
                            break;
                        case CMD_MANAGE_STAFF_DELETE:
                            user_manageStaffDelete();
                            break;
                        case CMD_MANAGE_ROOM_ADD:
                        	user_manageRoomAdd();
                            break;
                        case CMD_MANAGE_ROOM_UPDATE:
                        	user_manageRoomUpdate();
                            break;
                        case CMD_MANAGE_ROOM_DELETE:
                        	user_manageRoomDelete();
                            break;
                        case CMD_MANAGE_CUSTOMER_ADD:
                        	user_manageCustomerAdd();
                        	break;
                        case CMD_MANAGE_CUSTOMER_UPDATE:
                        	user_manageCustomerUpdate();
                        	break;
                        case CMD_MANAGE_CUSTOMER_DELETE:
                        	user_manageCustomerDelete();
                            break;
                        case CMD_MANAGE_SERVICE_COST_UPDATE:
                            user_manageUpdateServiceCost();
                            break;
                        case CMD_MAIN:
                            // Tell the user their options in this new menu
                            startup_printAvailableCommands(CMD_MAIN);
                            // Remember what menu we're in
                            currentMenu = CMD_MAIN;
                            break;
                        case CMD_QUIT:
                            quit = true;
                            break;
                        default:
                            // Remind the user about what commands are available
                            System.out.println("\nCommand not recognized");
                            startup_printAvailableCommands(CMD_MANAGE);
                            break;
                        }
                        break;
                    default:
                        break;
                }
            }
            
            // Clean up
            scanner.close();
            jdbc_statement.close();
            jdbc_result.close();
            // Hotels
            jdbcPrep_insertNewHotel.close();
            jdbcPrep_updateNewHotelManager.close();
            jdbcPrep_udpateHotelName.close();
            jdbcPrep_updateHotelStreetAddress.close();
            jdbcPrep_updateHotelCity.close();
            jdbcPrep_udpateHotelState.close();
            jdbcPrep_updateHotelZip.close();
            jdbcPrep_updateHotelPhoneNum.close();
            jdbcPrep_updateHotelManagerID.close();
            jdbcPrep_demoteOldManager.close();
            jdbcPrep_promoteNewManager.close();
            jdbcPrep_getNewestHotelID.close();
            jdbcPrep_getHotelSummaryForAddress.close();
            jdbcPrep_getHotelSummaryForPhoneNumber.close();
            jdbcPrep_getHotelSummaryForStaffMember.close();
            jdbcPrep_getHotelByID.close(); 
            jdbcPrep_deleteHotel.close();
            // Staff
            jdbcPrep_insertNewStaff.close();
            jdbcPrep_getNewestStaffID.close(); 
            jdbcPrep_updateStaffName.close(); 
            jdbcPrep_updateStaffDOB.close(); 
            jdbcPrep_updateStaffJobTitle.close(); 
            jdbcPrep_updateStaffDepartment.close(); 
            jdbcPrep_updateStaffPhoneNum.close(); 
            jdbcPrep_updateStaffAddress.close(); 
            jdbcPrep_updateStaffHotelID.close();
            jdbcPrep_getStaffByID.close();
            jdbcPrep_deleteStaff.close();
            jdbcPrep_getFirstAvailableCateringStaff.close();
            jdbcPrep_getFirstAvailableRoomServiceStaff.close();
            jdbcPrep_getDedicatedStaffMembers.close();
            // Rooms
            jdbcPrep_insertNewRoom.close(); 
            jdbcPrep_roomUpdateCategory.close();
            jdbcPrep_roomUpdateMaxOccupancy.close();
            jdbcPrep_roomUpdateNightlyRate.close();
            jdbcPrep_roomUpdateDCStaff.close();
            jdbcPrep_roomUpdateDRSStaff.close();
            jdbcPrep_roomDelete.close();
            jdbcPrep_isValidRoomNumber.close();
            jdbcPrep_isRoomCurrentlyOccupied.close();
            jdbcPrep_isValidHotelID.close();
            jdbcPrep_getRoomByHotelIDRoomNum.close();
            jdbcPrep_getOccupiedRoomsInHotel.close();
            jdbcPrep_getOneExampleRoom.close();
            jdbcPrep_assignDedicatedStaff.close();
            jdbcPrep_releaseDedicatedStaff.close();
            // Customers
            jdbcPrep_insertNewCustomer.close();
            jdbcPrep_customerUpdateSSN.close();
            jdbcPrep_customerUpdateName.close();
            jdbcPrep_customerUpdateDateOfBirth.close();
            jdbcPrep_customerUpdatePhoneNumber.close();
            jdbcPrep_customerUpdateEmail.close();
            jdbcPrep_customerDelete.close();
            jdbcPrep_getCustomerByID.close();
            jdbcPrep_isValidCustomer.close();                 
            jdbcPrep_isCustomerCurrentlyStaying.close();
            // Stays
            jdbcPrep_assignRoom.close();
            jdbcPrep_getNewestStay.close();
            jdbcPrep_addStayNoSafetyChecks.close();
            jdbcPrep_getSummaryOfStay.close();
            jdbcPrep_getStayByRoomAndHotel.close();
            jdbcPrep_getItemizedReceipt.close();
            jdbcPrep_updateAmountOwed.close();
            jdbcPrep_isValidStayID.close();
            // Services
            jdbcPrep_getEligibleStaffForService.close();
            jdbcPrep_insertNewServiceType.close();
            jdbcPrep_insertNewServiceRecord.close();
            jdbcPrep_udpateServiceRecord.close();
            jdbcPrep_getNewestServiceRecord.close();
            jdbcPrep_getServiceNameAndStaffByID.close();
            jdbcPrep_getValidServiceNames.close();
            jdbcPrep_getServiceRecordByID.close();
            jdbcPrep_updateServiceCost.close();
            // Table reporting
            jdbcPrep_reportTableRooms.close();
            jdbcPrep_reportTableStaff.close();
            jdbcPrep_reportTableStays.close();
            jdbcPrep_getStayIdForOccupiedRoom.close();
            jdbcPrep_updateCheckOutTimeAndEndDate.close();
            // Reports
            jdbcPrep_reportOccupancyByHotel.close();
            jdbcPrep_reportOccupancyByRoomType.close();
            jdbcPrep_reportOccupancyByDateRange.close();
            jdbcPrep_reportOccupancyByCity.close();
            jdbcPrep_reportTotalOccupancy.close();
            jdbcPrep_reportPercentageOfRoomsOccupied.close();
            jdbcPrep_reportStaffGroupedByRole.close();
            jdbcPrep_reportStaffServingDuringStay.close();
            jdbcPrep_reportHotelRevenueByDateRange.close();
            // Connection
            jdbc_connection.close();
        
        }
        catch (Throwable err) {
            error_handler(err);
        }

    }

}

import java.sql.*;


public class project540loadtables {

    private static final String jdbcURL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/smscoggi"; // Using SERVICE_NAME
    private static final String user = "smscoggi";
    private static final String password = "200157888";



    public static void main(String[] args) {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection connection = null;
            Statement statement = null;
            ResultSet result = null;
            try {

                connection = DriverManager.getConnection(jdbcURL, user, password);
                statement = connection.createStatement();

                ////sample cats create table
                // statement.executeUpdate("CREATE TABLE CATS (CNAME VARCHAR(20), " +
                // "TYPE VARCHAR(30), AGE INTEGER, WEIGHT FLOAT, SEX CHAR(1))");
                ////sample insert cats data
                // statement.executeUpdate("INSERT INTO CATS VALUES ('Oscar', 'Egyptian Mau'," +
                // " 3, 23.4, 'F')");
                ///sql for dropping table-
                ///DROP TABLE #temptable;

               /* CREATE TABLE Orders (
                    OrderID int NOT NULL,
                    OrderNumber int NOT NULL,
                    PersonID int,
                    PRIMARY KEY (OrderID),                              ///sample primary key setup
                    CONSTRAINT FK_PersonOrder FOREIGN KEY (PersonID)    ////sample fk setup + naming
                    REFERENCES Persons(PersonID)
                );*/

                /*CREATE TABLE Persons (
                    ID int NOT NULL,                                    ////not null set up
                    LastName varchar(255) NOT NULL,
                    FirstName varchar(255),
                    Age int,
                    CONSTRAINT UC_Person UNIQUE (ID,LastName)           ///setup for unique sets that would form fd's
                ); */

                /*CREATE TABLE Persons (
                    ID int NOT NULL AUTO_INCREMENT,                     ////setup for autoincrement field
                    LastName varchar(255) NOT NULL,
                    FirstName varchar(255),
                    Age int,
                    PRIMARY KEY (ID)
                );*/

                Boolean DropTables=false;
                try{ 
                    ///this is to set the following SQL statements so they don't commit to the database upon 
                    ///execution completion. 
                 connection.setAutoCommit(false);



                statement.executeUpdate("Drop Table Provided");
                statement.executeUpdate("Drop Table Stays");
                statement.executeUpdate("Drop Table ServiceTypes");
                statement.executeUpdate("Drop Table Rooms");
                statement.executeUpdate("Drop Table Customers");
                statement.executeUpdate("Alter Table Staff Drop Foreign Key FK_STAFFHID");
                statement.executeUpdate("Drop Table Hotels");
                statement.executeUpdate("Drop Table Staff");

                connection.commit();
                connection.setAutoCommit(true);
                DropTables=true;
                }catch(SQLException e ){
                    DropTables=false;
                    e.printStackTrace();
                    if (connection != null) {////This section is is to rollback everything that was commited
                        ////during the last commit since something failed.
                        try {
                            System.err.print("Transaction is being rolled back");
                            connection.rollback();
                        } catch(SQLException excep) {
                            e.printStackTrace();
                        }
                    }}


            if(DropTables){
                try{ 
                    ///this is to set the following SQL statements so they don't commit to the database upon 
                    ///execution completion. 
                 connection.setAutoCommit(false);

                statement.executeUpdate("CREATE TABLE Customers ("+
                    "SSN INT NOT NULL,"+
                    "Name VARCHAR(255) NOT NULL,"+
                    "DOB DATE NOT NULL,"+
                    "PhoneNum INT NOT NULL,"+
                    "Email VARCHAR(255) NOT NULL,"+
                    "PRIMARY KEY (SSN)"+
                ")");    /// phone number to be entered as 10 digit int ex: 9993335555

                statement.executeUpdate("CREATE TABLE ServiceTypes ("+
                    "Name VARCHAR(255) NOT NULL,"+
                    "Cost INT NOT NULL,"+
                    "PRIMARY KEY (Name)"+
                ")");

                statement.executeUpdate("CREATE TABLE Staff ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "Name VARCHAR(225) NOT NULL,"+
                    "DOB DATE NOT NULL,"+
                    "JobTitle VARCHAR(225),"+
                    "Dep VARCHAR(225) NOT NULL,"+
                    "PhoneNum INT NOT NULL,"+
                    "Address VARCHAR(225) NOT NULL,"+
                    "HotelID INT,"+
                    "PRIMARY KEY(ID)"+
                ")");

                statement.executeUpdate("CREATE TABLE Hotels ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "Name VARCHAR(225) NOT NULL,"+
                    "StreetAddress VARCHAR(225) NOT NULL,"+
                    "City VARCHAR(225) NOT NULL,"+
                    "State VARCHAR(225) NOT NULL,"+
                    "PhoneNum INT Not Null,"+
                    "ManagerID INT Not Null,"+
                    "Primary Key(ID),"+
                    "CONSTRAINT UC_HACS UNIQUE (StreetAddress, City, State),"+
                    "CONSTRAINT UC_HPN UNIQUE (PhoneNum),"+
                    "CONSTRAINT UC_HMID UNIQUE (ManagerID),"+
                    "CONSTRAINT FK_HMID FOREIGN KEY (ManagerID) REFERENCES Staff(ID)"+
                ")");


                statement.executeUpdate("ALTER TABLE Staff "+
                    "ADD CONSTRAINT FK_STAFFHID "+
                    "FOREIGN KEY (HotelID) "+
                    "REFERENCES Hotels(ID)"
                );/// needs to happen after hotel table is created

                statement.executeUpdate("CREATE TABLE Rooms ("+
                    "RoomNum INT NOT NULL,"+
                    "HotelID INT NOT NULL,"+
                    "Category VARCHAR(225) NOT NULL,"+
                    "MaxOcc INT NOT NULL,"+
                    "NightlyRate DOUBLE NOT NULL,"+
                    "DRSStaff INT,"+
                    "DCStaff INT,"+
                    "PRIMARY KEY(RoomNum,HotelID),"+
                    "CONSTRAINT FK_ROOMHID FOREIGN KEY (HotelID) REFERENCES Hotels(ID),"+
                    "CONSTRAINT FK_ROOMDRSID FOREIGN KEY (DRSStaff) REFERENCES Staff(ID),"+
                    "CONSTRAINT FK_ROOMDCID FOREIGN KEY (DCStaff) REFERENCES Staff(ID)"+
                ")");

                statement.executeUpdate("CREATE TABLE Stays ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "StartDate DATE NOT NULL,"+
                    "CheckInTime TIME NOT NULL,"+
                    "RoomNum INT NOT NULL,"+
                    "HotelID INT NOT NULL,"+
                    "CustomerSSN INT NOT NULL,"+
                    "NumGuests INT NOT NULL,"+
                    "CheckOutTime TIME,"+
                    "EndDate DATE,"+
                    "PaymentMethod ENUM('CASH','CARD') NOT NULL,"+
                    "CardType ENUM('VISA','MASTERCARD','HOTEL'),"+
                    "CardNumber INT,"+
                    "BillingAddress VARCHAR(255) NOT NULL,"+
                    "PRIMARY KEY(ID),"+
                    "CONSTRAINT UC_STAYKEY UNIQUE (StartDate, CheckInTime,RoomNum, HotelID),"+
                    "CONSTRAINT FK_STAYHID FOREIGN KEY (HotelID) REFERENCES Rooms(HotelID),"+
                    "CONSTRAINT FK_STAYRID FOREIGN KEY (RoomNum) REFERENCES Rooms(RoomNum),"+
                    "CONSTRAINT FK_STAYCSSN FOREIGN KEY (CustomerSSN) REFERENCES Customers(SSN)"+
                ")");

                statement.executeUpdate("CREATE TABLE Provided ("+
                    "ID INT NOT NULL AUTO_INCREMENT,"+
                    "StayID INT NOT NULL,"+
                    "StaffID INT NOT NULL,"+
                    "ServiceName VARCHAR(255) NOT NULL,"+
                    "PRIMARY KEY(ID),"+
                    "CONSTRAINT FK_PROVSTAYID FOREIGN KEY (StayID) REFERENCES Stays(ID),"+
                    "CONSTRAINT FK_PROVSTAFFID FOREIGN KEY (StaffID) REFERENCES Staff(ID),"+
                    "CONSTRAINT FK_PROVSERV FOREIGN KEY (ServiceName) REFERENCES ServiceTypes(Name)"+
                ")");  


                connection.commit();
                connection.setAutoCommit(true);

                }catch(SQLException e ){
                    e.printStackTrace();
                    if (connection != null) {////This section is is to rollback everything that was commited
                        ////during the last commit since something failed.
                        try {
                            System.err.print("Transaction is being rolled back");
                            connection.rollback();
                        } catch(SQLException excep) {
                            e.printStackTrace();
                        }
                    }

                }}







            } 
            finally {
                //close(result);
                //close(statement);
                //close(connection);
            }
        } 
        catch(Throwable oops) {
                oops.printStackTrace();
        }
    }
}
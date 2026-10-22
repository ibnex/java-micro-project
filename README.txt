VEHICLE PARKING AND SLOT MANAGEMENT SYSTEM

This is a simple Java Swing and MySQL college micro project. It has five
parking slots. The first free slot is automatically assigned to a vehicle.
The table displays currently parked vehicles. Releasing a vehicle calculates
the fee at Rs. 20 per hour, rounding up, with a minimum fee of Rs. 20.

PROJECT FILES
src\Main.java       Complete Java Swing application and JDBC code
database.sql        Database and table creation script
run.bat             Compiles and starts the application

WINDOWS SETUP
1. Install a JDK (Java Development Kit), not only a JRE. Open PowerShell and
   run:
       java -version
       javac -version
   Both commands should show a version. If javac is not found, install a JDK
   and add its bin folder to the Windows PATH.

2. Install MySQL Server and make sure the MySQL service is running. MySQL
   commonly uses port 3306, but check the port in MySQL Workbench or the
   MySQL Server configuration. If your port is different, change 3306 in
   the URL in src\Main.java.

3. Open MySQL Workbench, connect to your server, open database.sql, and
   execute the complete script. It creates the vehicle_parking database,
   two tables, and five empty slots.

4. Download MySQL Connector/J from the official MySQL website. Copy the
   downloaded file named similar to mysql-connector-j-8.x.x.jar into this
   VehicleParkingSystem folder (beside run.bat).

5. Open src\Main.java and replace:
       YOUR_MYSQL_PASSWORD
   with the actual password of the MySQL root user. Keep the quotation marks.
   If your MySQL username is not root, also change USERNAME in that file.

6. Double-click run.bat. It finds the Connector/J JAR, compiles Main.java,
   and starts the application.

POWER SHELL ALTERNATIVE
Open PowerShell in the VehicleParkingSystem folder and run:
    $jar = (Get-ChildItem .\mysql-connector-j-*.jar).FullName
    New-Item -ItemType Directory -Force .\bin
    javac -cp $jar -d .\bin .\src\Main.java
    java -cp ".\bin;$jar" Main

If compilation fails, check that the JDK and Connector/J JAR are installed.
If a database error appears, check that MySQL is running, the database.sql
script was executed, the port is correct, and the password in Main.java is
correct. The program has not been tested against your local MySQL password
or server settings.

HOW THE PROGRAM WORKS
The Park Vehicle button checks the registration number, finds the first slot
where occupied is false, inserts an active parking record, and marks that
slot occupied. The Release Vehicle button uses the selected table row,
calculates the time difference, updates the record with an exit time and
fee, and makes the slot available again.

FIVE SIMPLE VIVA QUESTIONS AND ANSWERS

1. Why is JDBC used?
   JDBC connects the Java program to MySQL and lets Java execute SQL queries.

2. What does the occupied column mean?
   It stores whether a slot is currently being used: TRUE means occupied and
   FALSE means available.

3. How is the first available slot selected?
   The program selects free slots in slot_id order and uses the first result.

4. How is the parking fee calculated?
   The time is rounded up to a complete hour. Each hour costs Rs. 20, and
   even less than one hour costs the minimum Rs. 20.

5. Why is active stored in parking_records?
   TRUE identifies a currently parked vehicle. After release it becomes FALSE,
   while the record remains saved as parking history.

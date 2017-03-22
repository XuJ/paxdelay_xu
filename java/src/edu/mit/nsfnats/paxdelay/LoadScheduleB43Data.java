//SUKITJANUPARP
//create table airline_inventories

package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LoadScheduleB43Data {
	// JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   
	   //SUKITJANUPARP
	   //input link to database here
	   static final String DB_URL = "jdbc:mysql://localhost:3306/paxdelay?allowMultiQueries=true";
	   
	   //  Database credentials
	   //SUKITJANUPARP
	   //input username and password here
	   static final String USER = "anunya";
	   static final String PASS = "paxdelay";
	   
	   //SUKITJANUPARP
	   //change the value of int year if want to run the data from other year
	   static int year = 2007;
	   
	   public static void main(String[] args) {
	   Connection conn = null;
	   Statement stmt = null;
	   try{
	      //STEP 2: Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");

	      //STEP 3: Open a connection
	      System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(DB_URL,USER,PASS);

	      //STEP 4: Execute a query
	      System.out.println("Creating statement...");
	      stmt = conn.createStatement();
	      ArrayList<String> sql = new ArrayList<String>();
	      sql.add("drop table if exists tmp_load_airline_inventories\n");
	      sql.add("create table tmp_load_airline_inventories\n" + 
	      		"(\n" + 
	      		"carrier_name varchar(100),\n" + 
	      		"year_of_first_delivery varchar(100),\n" + 
	      		"unique_carrier_name varchar(100),\n" + 
	      		"airline_id varchar(100),\n" + 
	      		"unique_carrier varchar(100),\n" + 
	      		"\n" + 
	      		"	carrier			char(6) not null,\n" + 
	      		"	year		char(4) not null,\n" + 
	      		"	serial_number	varchar(12) not null,\n" + 
	      		"	tail_number		varchar(7) not null,\n" + 
	      		"	aircraft_status	char(1) not null,\n" + 
	      		"	operating_status	char(1) not null,\n" + 
	      		"	number_of_seats		numeric(3, 0),\n" + 
	      		"	manufacturer	varchar(50) not null,\n" + 
	      		"	model			varchar(16) not null,\n" + 
	      		"	capacity_in_pounds	numeric(6, 0),\n" + 
	      		"	acquisition_date	varchar(10) not null\n" + 
	      		")\n" + 
	      		"ENGINE = MyISAM;");
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      //SUKITJANUPARP
	      //input path to the csv file here
	      String filename = "/mdsg/bts_raw_csv/SCHEDULE_B43_"+year+".csv";
	      stmt.execute("LOAD DATA LOCAL INFILE '"+filename+"'\n" + 
	      		"INTO TABLE tmp_load_airline_inventories\n" + 
	      		"FIELDS TERMINATED BY ','\n" + 
	      		"LINES TERMINATED BY '\\n'\n" + 
	      		"IGNORE 1 LINES\n" + 
	      		"(year,\n" + 
	      		"carrier,\n" + 
	      		"carrier_name,\n" + 
	      		"year_of_first_delivery,\n" + 
	      		"unique_carrier_name,\n" + 
	      		"serial_number,\n" + 
	      		"tail_number,\n" + 
	      		"aircraft_status,\n" + 
	      		"operating_status,\n" + 
	      		"number_of_seats,\n" + 
	      		"manufacturer,\n" + 
	      		"model,\n" + 
	      		"capacity_in_pounds,\n" + 
	      		"acquisition_date,\n" + 
	      		"airline_id,\n" + 
	      		"unique_carrier\n" + 
	      		");");
		   
	      sql.add("insert into airline_inventories\n" + 
	      		"	(carrier, year,	serial_number, tail_number,	aircraft_status, operating_status, number_of_seats, manufacturer, model, capacity_in_pounds, acquisition_date)\n" + 
	      		"select carrier, year, serial_number, tail_number,	aircraft_status, operating_status, number_of_seats, manufacturer, model, capacity_in_pounds, STR_TO_DATE(acquisition_date,'%Y-%m-%d') as acquisition_date\n" + 
	      		"from tmp_load_airline_inventories");
	      sql.add("drop table if exists tmp_load_airline_inventories;");
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	      }
	      stmt.executeBatch();

	      stmt.close();
	      conn.close();
	   }catch(SQLException se){
	      //Handle errors for JDBC
	      se.printStackTrace();
	   }catch(Exception e){
	      //Handle errors for Class.forName
	      e.printStackTrace();
	   }finally{
	      //finally block used to close resources
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	      }// nothing we can do
	      try{
	         if(conn!=null)
	            conn.close();
	      }catch(SQLException se){
	         se.printStackTrace();
	      }//end finally try
	   }//end try
	   
	   //SUKITJANUPARP
	   //the message "Goodbye!" will be shown in the console after all queries finished
	   System.out.println("Goodbye!");
	}//end main
}

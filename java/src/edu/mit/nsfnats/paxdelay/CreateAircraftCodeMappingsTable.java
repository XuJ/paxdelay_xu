//SUKITJANUPARP
//create table aircraft_code_mappings
//constant input: /mdsg/paxdelay_general_Xu/AircraftCodeMappings.csv


package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateAircraftCodeMappingsTable {
	// JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   
	   //SUKITJANUPARP
	   //input link to database here
	   static final String DB_URL = "jdbc:mysql://localhost:3306/paxdelay?allowMultiQueries=true";
	  
	   //  Database credentials
	   //SUKITJANUPARP
	   //input username and password here
	   static final String USER = "root";
	   static final String PASS = "paxdelay";
	   
	   public static void main(String[] args) {
	   long startTime = System.nanoTime();
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
	      
	      sql.add("drop table if exists aircraft_code_mappings");
	      sql.add("create table aircraft_code_mappings\n" + 
	      		"(\n" + 
	      		"	iata_code		char(3) not null,\n" + 
	      		"	icao_code		varchar(4),\n" + 
	      		"	manufacturer_and_model	varchar(75) not null,\n" + 
	      		"	inventory_manufacturer	varchar(20),\n" + 
	      		"	inventory_model	varchar(15),\n" + 
	      		"	wake_category	char(1)\n" + 
	      		")\n" + 
	      		"ENGINE = MyISAM");

	    
	     
	     for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	   
	      
	     
	     
	      //rs.close();
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
	   long endTime = System.nanoTime();
	   long duration = (endTime - startTime)/1000000/1000/60;
	   System.out.println("That took " + duration + " minutes ");
	}//end main
}

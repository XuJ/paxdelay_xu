//SUKITJANUPARP
//create table unique_carrier_routes

package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateUniqueCarrierRoutesTable {
	// JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  

	   //SUKITJANUPARP
	   //input link to database here
	   static final String DB_URL = "jdbc:mysql://localhost:3306/paxdelay_xu?allowMultiQueries=true";
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
	      
	      sql.add("drop table if exists unique_carrier_routes");
	      sql.add("create table unique_carrier_routes\n" + 
	      		"(\n" + 
	      		"  year numeric(4) not null,\n" + 
	      		"  num_flights numeric(1) not null,\n" + 
	      		"  first_operating_carrier char(6) not null,\n" + 
	      		"  second_operating_carrier char(6),\n" + 
	      		"  origin char(3) not null,\n" + 
	      		"  connection char(3),\n" + 
	      		"  destination char(3) not null\n" + 
	      		")");
	      sql.add("insert into unique_carrier_routes\n" + 
	      		"select distinct year, num_flights,\n" + 
	      		"  first_operating_carrier, second_operating_carrier,\n" + 
	      		"  origin, connection, destination\n" + 
	      		"from route_demands\n" + 
	      		"where exists\n" + 
	      		"(\n" + 
	      		" select code from asqp_carriers\n" + 
	      		" where code = first_operating_carrier\n" + 
	      		")\n" + 
	      		"and (num_flights = 1 or exists\n" + 
	      		"  (\n" + 
	      		"   select code from asqp_carriers\n" + 
	      		"   where code = second_operating_carrier\n" + 
	      		"  )\n" + 
	      		")");
	      sql.add("create index idx_carrier_routes_c1yocc2d\n" + 
	      		"  on unique_carrier_routes(first_operating_carrier, year, origin, connection,  second_operating_carrier, destination)");
	      sql.add("create index idx_unique_carrier_routes\n" + 
	      		"  on unique_carrier_routes(year, origin, connection, destination, first_operating_carrier, second_operating_carrier)");
	      sql.add("create index idx_unique_carrier_routes_c1oc\n" +
                        "  on unique_carrier_routes(first_operating_carrier, origin, connection)");
 	      sql.add("create index idx_unique_carrier_routes_yc1\n" +
                        "  on unique_carrier_routes(year, first_operating_carrier)");


	     
	     for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	   
	      

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

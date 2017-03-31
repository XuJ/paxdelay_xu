//SUKITJANUPARP
//create table t100_db1b_route_demands
//XuJiao
//That took 1 minute 
//another input: db1b_route_demands from CreateDB1BRouteDemandsTable.java
//records: 3,432,652 (MIT: 3,436,912)

package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateT100DB1BRouteDemandsTable {

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
	      
		//Move the indexing to CreateDB1BT100SegmentComparisons.java
/*      
		//If running for the second time, remember to comment these:
	      sql.add("create index idx_db1b_t100_qcod\n" + 
	      		"  on db1b_t100_segment_comparisons(quarter, carrier, origin, destination)");
	      sql.add("create index idx_db1b_t100_qmcod\n" + 
	      		"  on db1b_t100_segment_comparisons(quarter, month, carrier, origin, destination)");
*/	      
      	      sql.add("drop table if exists t100_db1b_route_demands");
	      sql.add("create table t100_db1b_route_demands\n" + 
	      		"(\n" + 
	      		"  year numeric(4) not null,\n" + 
	      		"  quarter int not null,\n" + 
	      		"  month int(2) not null,\n" + 
	      		"  num_flights int(11) not null,\n" + 
	      		"  origin char(3) not null,\n" + 
	      		"  connection char(3),\n" + 
	      		"  destination char(3) not null,\n" + 
	      		"  first_operating_carrier varchar(3) not null,\n" + 
	      		"  second_operating_carrier varchar(3),\n" + 
	      		"  passengers decimal(6,0) not null\n" + 
	      		")");
	      sql.add("insert into t100_db1b_route_demands\n" + 
	      		"select drd.year, drd.quarter, dtc.month, 1,\n" + 
	      		"  drd.origin, null, drd.destination,\n" + 
	      		"  drd.first_operating_carrier, null,\n" + 
	      		"  dtc.scaling_factor * drd.passengers\n" + 
	      		"from db1b_route_demands drd\n" + 
	      		"join db1b_t100_segment_comparisons dtc\n" + 
	      		"on dtc.year = drd.year\n" + 
	      		"  and dtc.quarter = drd.quarter\n" + 
	      		"  and dtc.carrier = drd.first_operating_carrier\n" + 
	      		"  and dtc.origin = drd.origin\n" + 
	      		"  and dtc.destination = drd.destination\n" + 
	      		"where drd.num_flights = 1");
	      sql.add("insert into t100_db1b_route_demands\n" + 
	      		"select drd.year, drd.quarter, dtc1.month, 2,\n" + 
	      		"  drd.origin, drd.connection, drd.destination,\n" + 
	      		"  drd.first_operating_carrier, drd.second_operating_carrier,\n" + 
	      		"  least(dtc1.scaling_factor, dtc2.scaling_factor) * drd.passengers\n" + 
	      		"from db1b_route_demands drd\n" + 
	      		"join db1b_t100_segment_comparisons dtc1\n" + 
	      		"on dtc1.year = drd.year\n" + 
	      		"  and dtc1.quarter = drd.quarter\n" + 
	      		"  and dtc1.carrier = drd.first_operating_carrier\n" + 
	      		"  and dtc1.origin = drd.origin\n" + 
	      		"  and dtc1.destination = drd.connection\n" + 
	      		"join db1b_t100_segment_comparisons dtc2\n" + 
	      		"on dtc2.year = drd.year\n" + 
	      		"  and dtc2.quarter = drd.quarter\n" + 
	      		"  and dtc2.month = dtc1.month\n" + 
	      		"  and dtc2.carrier = drd.second_operating_carrier\n" + 
	      		"  and dtc2.origin = drd.connection\n" + 
	      		"  and dtc2.destination = drd.destination\n" + 
	      		"where drd.num_flights = 2");
	      
	      
	      
	      
	     
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

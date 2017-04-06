//SUKITJANUPARP
//create table db1b_t100_segment_comparisons
//XuJiao
//That took 0 minute 
//Records: 118,574 (MIT: 118,892)
//Move indexing here from CreateT100DB1BRouteDemandsTable.java

package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateDB1BT100SegmentComparisons {
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
	           
	      sql.add("drop table if exists db1b_t100_segment_comparisons");
	      sql.add("create table db1b_t100_segment_comparisons\n" + 
	      		"(\n" + 
	      		"  year decimal(4,0) not null,\n" + 
	      		"  quarter int(11) not null,\n" + 
	      		"  month int(2) not null,\n" + 
	      		"  carrier varchar(3) not null,\n" + 
	      		"  origin char(3) not null,\n" + 
	      		"  destination char(3) not null,\n" + 
	      		"  db1b_passengers decimal(6,0) not null,\n" +
	      		"  t100_passegners decimal (6,0) not null,\n" +
	      		"  scaling_factor decimal(10, 4)\n" + 
	      		")\n");
	      
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      
	      //Fix the error in three union creation by XuJ 04/07/17
	      sql.add("drop table if exists temp_three_union\n");
	      sql.add("create table temp_three_union\n" + 
	      		"select \n" + 
	      		"	year, \n" + 
	      		"	quarter, \n" + 
	      		"	first_operating_carrier as carrier,\n" + 
	      		"	origin, \n" + 
	      		"	destination, \n" + 
	      		"	sum(passengers) as passengers\n" + 
	      		"from db1b_route_demands\n" + 
	      		"where num_flights = 1\n" + 
	      		"group by year, quarter, first_operating_carrier, origin, destination\n" + 
	      		"\n" + 
	      		"union		\n" + 
	      		"select \n" + 
	      		"	year, \n" + 
	      		"	quarter, \n" + 
	      		"	first_operating_carrier as carrier,\n" + 
	      		"	origin, \n" + 
	      		//"	connection, \n" + ; XuJ, 04/07/17
	      		"	connection as destination, \n" +
	      		"	sum(passengers) as passengers\n" + 
	      		"from db1b_route_demands\n" + 
	      		"where num_flights = 2\n" + 
	      		//"group by year, quarter, first_operating_carrier, origin, destination\n" + ; XuJ, 04/07/17
	      		"group by year, quarter, first_operating_carrier, origin, connection\n" + 
	      		"\n" + 
	      		"union	\n" + 
	      		"select \n" + 
	      		"	year, \n" + 
	      		"	quarter, \n" + 
	      		"	second_operating_carrier as carrier,\n" + 
	      		//"	connection, \n" + XuJ, 04/07/17
	      		"	connection as origin, \n" + 
	      		"	destination, \n" + 
	      		"	sum(passengers) as passengers\n" + 
	      		"from db1b_route_demands\n" + 
	      		"where num_flights = 2\n" + 
	      		//"group by year, quarter, second_operating_carrier, origin, destination\n"); XuJ, 04/07/17
	  			"group by year, quarter, second_operating_carrier, connection, destination\n");
	      //748,700
	      //748,252 XuJiao
	      
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      sql.add("drop table if exists temp_db1b_1");
	      sql.add("create table temp_db1b_1\n" + 
	      		"select \n" + 
	      		"	year, \n" + 
	      		"	quarter, \n" + 
	      		"	carrier, \n" + 
	      		"	origin, \n" + 
	      		"	destination,\n" + 
	      		"	sum(passengers) as passengers\n" + 
	      		"from temp_three_union \n" + 
	      		"group by year, quarter, carrier, origin, destination\n");
	      //102,785
	      //102,715 XuJiao
	      
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey ei");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      
	      sql.add("create index idx_temp_db1_1\n" + 
	      		" on temp_db1b_1(quarter, carrier, origin, destination)\n");
	      sql.add("drop table if exists temp_t100_1");
	      sql.add("create table temp_t100_1\n" + 
	      		"select \n" + 
	      		"	year, \n" + 
	      		"	quarter, \n" + 
	      		"	month, \n" + 
	      		"	carrier, \n" + 
	      		"	origin, \n" + 
	      		"	destination,\n" + 
	      		"	sum(passengers) as passengers\n" + 
	      		"from t100_segments\n" + 
	    		"group by year, quarter, month, carrier, origin, destination\n");
	      //237,560
	      //299,235 XuJiao
	      
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      
	      sql.add("create index idx_temp_db1_2\n" + 
	      		" on temp_t100_1(quarter, carrier, origin, destination)\n");
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      
	      sql.add("insert into db1b_t100_segment_comparisons\n" + 
	      		"select \n" + 
	      		"	db1b.year, \n" + 
	      		"	db1b.quarter, \n" + 
	      		"	t100.month,\n" + 
	      		"	db1b.carrier, \n" + 
	      		"	db1b.origin, \n" + 
	      		"	db1b.destination,\n" + 
	      		"   db1b.passengers,\n" +
	      		"   t100.passengers,\n" +
	      		"	t100.passengers / db1b.passengers as scaling_factor\n" + 
	      		"from temp_db1b_1 db1b\n" + 
	      		"join temp_t100_1 t100\n" + 
	      		"on db1b.year = t100.year\n" + 
	      		"	and db1b.quarter 	= t100.quarter\n" + 
	      		"	and db1b.carrier 	= t100.carrier\n" + 
	      		"	and db1b.origin 	= t100.origin\n" + 
	      		"	and db1b.destination 	= t100.destination\n");
	      sql.add("drop table temp_three_union\n");
	      sql.add("drop table temp_db1b_1\n");
	      sql.add("drop table temp_t100_1\n");
//	      sql.add("select count(*) from db1b_t100_segment_comparisons;\n");
	      
	     for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();

//	      Add indexes for creating t100_db1b_route_demands table
              sql.add("create index idx_db1b_t100_qcod\n" +
                        "  on db1b_t100_segment_comparisons(quarter, carrier, origin, destination)");
              sql.add("create index idx_db1b_t100_qmcod\n" +
                        "  on db1b_t100_segment_comparisons(quarter, month, carrier, origin, destination)");

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

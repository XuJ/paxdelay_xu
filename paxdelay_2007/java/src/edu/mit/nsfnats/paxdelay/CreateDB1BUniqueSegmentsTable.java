//SUKITJANUPARP
//create table db1b_unique_carrier_segments
//XuJiao
//That took 0 minutes 
//This is not independent, run LoadDB1BCoupons.java first
//Records: 156,168 (MIT: 156,209)


package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateDB1BUniqueSegmentsTable {
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
	      
	      sql.add("drop table if exists db1b_unique_carrier_segments\n");
	      sql.add("create table db1b_unique_carrier_segments\n" + 
	      		"(\n" + 
	      		"  id integer not null auto_increment, primary key (id)\n" + 
	      		") \n" + 
	      		"select	quarter as quarter, \n" + 
	      		"				ticketing_carrier as ticketing_carrier, \n" + 
	      		"				operating_carrier as operating_carrier,\n" + 
	      		"				origin as origin, \n" + 
	      		"				destination as destination\n" + 
	      		"from db1b_coupons\n" + 
	      		"group by quarter, ticketing_carrier, operating_carrier, origin, destination\n");
	      sql.add("create unique index idx_db1b_us_qcod \n" + 
	      		"	on db1b_unique_carrier_segments(quarter, ticketing_carrier, operating_carrier, origin, destination)\n" + 
	      		"	using btree\n");
	      
	      
	     
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

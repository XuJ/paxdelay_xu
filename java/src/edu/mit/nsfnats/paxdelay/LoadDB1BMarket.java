//SUKITJANUPARP
//create db1b_markets table
//XuJiao
//That took 3 minutes 
//input: /mdsg/bts_raw_csv/DB1B_MARKETS_20(06~15)_QX.csv
//Records: 20,778,751 (MIT: 20,778,751)

package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LoadDB1BMarket {
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
	   
	   //SUKITJANUPARP
	   //change the value of int year if want to run the data from other year
	   static int year = 2007;
	   
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
	      
	      
	      sql.add("drop table if exists db1b_markets\n");
	      sql.add("create table db1b_markets\n" + 
	      		"(\n" + 
	      		"	itinerary_id	numeric(16, 0) not null,\n" + 
	      		"	market_id	numeric(16, 0) not null,\n" + 
	      		"	number_coupons	numeric(2, 0) not null,\n" + 
	      		"	year	numeric(4) not null,\n" + 
	      		"	quarter	int not null,\n" + 
	      		"	origin	char(3) not null,\n" + 
	      		"	destination	char(3) not null,\n" + 
	      		"	reporting_carrier	varchar(3) not null,\n" +
	      		"	ticketing_carrier	varchar(3) not null,\n" + 
	      		"	operating_carrier	varchar(3) not null,\n" + 
	      		"	passengers	numeric(4, 0) not null,\n" + 
	      		"   fare numeric(7, 2) not null,\n"+
	      	    "   miles_flown numeric(5) not null,\n"+
	      	    "   nonstop_miles numeric(5, 0) not null\n"+ 
	      		")\n" + 
	      		"ENGINE = MyISAM\n" + 
	      		"partition by list (quarter)\n" + 
	      		"(	partition p_q1 values in (1),\n" + 
	      		"	partition p_q2 values in (2),\n" + 
	      		"	partition p_q3 values in (3),\n" + 
	      		"	partition p_q4 values in (4)\n" + 
	      		");");
	      
	     
	     for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	   
	      
	      sql.add("drop table if exists tmp_load_db1b_markets\n");
	      sql.add("create table tmp_load_db1b_markets\n" + 
	      		"(\n" + 
	      		"	itinerary_id	numeric(16, 0) not null,\n" + 
	      		"	market_id	numeric(16, 0) not null,\n" + 
	      		"	number_coupons	numeric(2, 0) not null,\n" + 
	      		"	year	numeric(4) not null,\n" + 
	      		"	quarter	int not null,\n" + 
	      		"   temp_o_id varchar(10) not null,\n" +
	      		"   temp_o_sq_id varchar(10) not null,\n" +
	      		"   temp_o_mk_id varchar(10) not null,\n" +
	      		"	origin	char(3) not null,\n" + 
	      		"   temp_d_id varchar(10) not null,\n" +
	      		"   temp_d_sq_id varchar(10) not null,\n" +
	      		"   temp_d_mk_id varchar(10) not null,\n" +
	      		"	destination	char(3) not null,\n" + 
	      		"	reporting_carrier	varchar(3) not null,\n" +
	      		"	ticketing_carrier	varchar(3) not null,\n" + 
	      		"	operating_carrier	varchar(3) not null,\n" + 
	      		"   bulk_fare varchar(10) not null,\n" +
	      		"	passengers	numeric(4, 0) not null,\n" + 
	      		"   fare numeric(7, 2) not null,\n"+
	      	    "   miles_flown numeric(5) not null,\n"+
	      	    "   nonstop_miles numeric(5, 0) not null\n"+ 
	      		")");
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      
		   for(int i = 1;i<=4;i++){
			   //SUKITJANUPARP
			   //input path to the csv file here
			   String filename = "/mdsg/bts_raw_csv/DB1B_MARKETS_"+year+"_Q"+i+".csv";
			   stmt.execute("LOAD DATA LOCAL INFILE '"+filename+"'\n" + 
			   		"		INTO TABLE tmp_load_db1b_markets\n" +
			   		"		FIELDS TERMINATED BY ','\n" + 
			   		"		OPTIONALLY ENCLOSED BY '\"'\n" + 
			   		"		LINES TERMINATED BY '\\n'\n" + 
			   		"		IGNORE 1 LINES\n" + 
			   		"		(\n" + 
			   		"	itinerary_id,\n" + 
		      		"	market_id	,\n" + 
		      		"	number_coupons	,\n" + 
		      		"	year	,\n" + 
		      		"	quarter	,\n" + 
		      		"   temp_o_id ,\n" +
		      		"   temp_o_sq_id ,\n" +
		      		"   temp_o_mk_id ,\n" +
		      		"	origin	,\n" + 
		      		"   temp_d_id ,\n" +
		      		"   temp_d_sq_id ,\n" +
		      		"   temp_d_mk_id ,\n" +
		      		"	destination	,\n" + 
		      		"	reporting_carrier	,\n" +
		      		"	ticketing_carrier	,\n" + 
		      		"	operating_carrier	,\n" + 
		      		"   bulk_fare ,\n" +
		      		"	passengers	,\n" + 
		      		"   fare ,\n"+
		      	    "   miles_flown ,\n"+
		      	    "   nonstop_miles \n"+ 
			   		"		)");
			   System.out.println("done");
		   }
		   
	      sql.add("insert into db1b_markets\n" +
	    		  "		(\n" + 
	    		  "	itinerary_id,\n" + 
		      		"	market_id	,\n" + 
		      		"	number_coupons	,\n" + 
		      		"	year	,\n" + 
		      		"	quarter	,\n" + 
		      		"	origin	,\n" + 
		      		"	destination	,\n" + 
		      		"	reporting_carrier	,\n" +
		      		"	ticketing_carrier	,\n" + 
		      		"	operating_carrier	,\n" + 
		      		"	passengers	,\n" + 
		      		"   fare ,\n"+
		      	    "   miles_flown ,\n"+
		      	    "   nonstop_miles \n"+ 
			   		"		)" +
	      		"\n" + 
	      		"select  "+
	      		"	itinerary_id,\n" + 
	      		"	market_id	,\n" + 
	      		"	number_coupons	,\n" + 
	      		"	year	,\n" + 
	      		"	quarter	,\n" + 
	      		"	origin	,\n" + 
	      		"	destination	,\n" + 
	      		"	reporting_carrier	,\n" +
	      		"	ticketing_carrier	,\n" + 
	      		"	operating_carrier	,\n" + 
	      		"	passengers	,\n" + 
	      		"   fare ,\n"+
	      	    "   miles_flown ,\n"+
	      	    "   nonstop_miles \n"+ 
	      		"from tmp_load_db1b_markets");
	      sql.add("drop table if exists tmp_load_db1b_markets\n");
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
	   long endTime = System.nanoTime();
	   long duration = (endTime - startTime)/1000000/1000/60;
	   System.out.println("That took " + duration + " minutes ");
	}//end main
}

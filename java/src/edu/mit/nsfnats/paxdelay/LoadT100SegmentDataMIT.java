//XuJiao
//That took 0 minutes

package edu.mit.nsfnats.paxdelay;

import java.sql.*;
import java.util.ArrayList;

public class LoadT100SegmentDataMIT {
	// JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost:3306/test?allowMultiQueries=true";
	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "paxdelay";
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
	      sql.add("drop table if exists t100_segments_MIT\n");
	      sql.add("create table if not exists t100_segments_MIT\n"+
	    		  "(\n" + 
		      		"	departures_scheduled	numeric(4, 0) not null,\n" + 
		      		"	departures_performed	numeric(4, 0) not null,\n" + 
		      		"	payload	numeric(10, 0) not null,\n" + 
		      		"	seats	numeric(6, 0) not null,\n" + 
		      		"	passengers	numeric(6, 0) not null,\n" + 
		      		"	freight	numeric(8, 0) not null,\n" + 
		      		"	mail	numeric(8, 0) not null,\n" + 
		      		"	distance	numeric(4, 0) not null,\n" + 
		      		"	ramp_to_ramp	numeric(6, 0) not null,\n" + 
		      		"	air_time	numeric(6, 0) not null,\n" + 
		      		"	unique_carrier	varchar(6) not null,\n" + 
		      		"	airline_id	numeric(6, 0) not null,\n" + 
		      		"	unique_carrier_name	varchar(100) not null,\n" + 
		      		"	unique_carrier_entity	varchar(6) not null,\n" + 
		      		"	region	char(1) not null,\n" + 
		      		"	carrier	char(6) not null,\n" + 
		      		"	carrier_name	varchar(100) not null,\n" + 
		      		"	carrier_group	numeric(2, 0) not null,\n" + 
		      		"	carrier_group_new	numeric(2, 0) not null,\n" + 
		      		"	origin	char(3) not null,\n" + 
		      		"	origin_city_name	varchar(50) not null,\n" + 
//		      		"	origin_airport_id int,\n" + 
//		      		"	origin_airport_seq_id int,\n" + 
		      		"	origin_city_code	numeric(6, 0) not null,\n" + 
		      		"	origin_state	char(2) not null,\n" + 
		      		"	origin_state_fips	numeric(2, 0) not null,\n" + 
		      		"	origin_state_name	varchar(50) not null,\n" + 
//		      		"	origin_country	  char(2),\n" + 
//		      		"	origin_country_name    varchar(50),\n" + 
		      		"	origin_wac	numeric(4, 0) not null,\n" + 
		      		"	destination	char(3) not null,\n" + 
		      		"	destination_city_name	varchar(50) not null,\n" + 
//		      		"	dest_airport_id int,\n" + 
//		      		"	dest_airport_seq_id int,\n" + 
		      		"	destination_city_code	numeric(6, 0) not null,\n" + 
		      		"	destination_state	char(2) not null,\n" + 
		      		"	destination_state_fips	numeric(2, 0) not null,\n" + 
		      		"	destination_state_name	varchar(50) not null,\n" + 
//		      		"	destination_country    char(2),\n" + 
//		      		"	destination_country_name	varchar(50),\n" + 
		      		"	destination_wac	numeric(4, 0) not null,\n" + 
		      		"	aircraft_group	numeric(2, 0) not null,\n" + 
		      		"	aircraft_type	numeric(4, 0) not null,\n" + 
		      		"	aircraft_config	numeric(1, 0) not null,\n" + 
		      		"	year	numeric(4) not null,\n" + 
		      		"	quarter	numeric(1, 0) not null,\n" + 
		      		"	month	numeric(2, 0) not null,\n" + 
		      		"	distance_group	numeric(2, 0) not null,\n\n" + 
					"	service_class	char(1) not null\n" + 
		      		")\n"+
					"ENGINE = MyISAM");

	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	      
	      String filename = "/mdsg/paxdelay_general_Xu/bts_raw_csv/T100_SEGMENTS_2007_MIT.csv";
	      stmt.execute("LOAD DATA LOCAL INFILE '"+filename+"'\n" + 
	      		"INTO TABLE t100_segments_MIT\n" + 
	      		"FIELDS TERMINATED BY ','\n" + 
	      		"OPTIONALLY ENCLOSED BY '\"'\n" + 
	      		"LINES TERMINATED BY '\\n'\n" + 
	      		"IGNORE 1 LINES\n" + 
	      		"(departures_scheduled,\n" + 
	      		"departures_performed,\n" + 
	      		"payload,\n" + 
	      		"seats,\n" + 
	      		"passengers,\n" + 
	      		"freight,\n" + 
	      		"mail,\n" + 
	      		"distance,\n" + 
	      		"ramp_to_ramp,\n" + 
	      		"air_time,\n" + 
	      		"unique_carrier,\n" + 
	      		"airline_id,\n" + 
	      		"unique_carrier_name,\n" + 
	      		"unique_carrier_entity,\n" + 
	      		"region,\n" + 
	      		"carrier,\n" + 
	      		"carrier_name,\n" + 
	      		"carrier_group,\n" + 
	      		"carrier_group_new,\n" + 
//	      		"origin_airport_id,\n" + 
//	      		"origin_airport_seq_id,\n" + 
				"origin,\n" + 
				"origin_city_name,\n" + 
				"origin_city_code,\n" + 
	      		"origin_state,\n" + 
	      		"origin_state_fips,\n" + 
	      		"origin_state_name,\n" + 
//	      		"origin_country,\n" + 
//	      		"origin_country_name,\n" + 
	      		"origin_wac,\n" + 
//	      		"dest_airport_id,\n" + 
//	      		"dest_airport_seq_id,\n" + 
				"destination,\n" + 
				"destination_city_name,\n" + 
				"destination_city_code,\n" + 
	      		"destination_state,\n" + 
	      		"destination_state_fips,\n" + 
	      		"destination_state_name,\n" + 
//	      		"destination_country,\n" + 
//	      		"destination_country_name,\n" + 
	      		"destination_wac,\n" + 
	      		"aircraft_group,\n" + 
	      		"aircraft_type,\n" + 
	      		"aircraft_config,\n" + 
	      		"year,\n" + 
	      		"quarter,\n" + 
	      		"month,\n" + 
	      		"distance_group,\n" + 
	      		"service_class);");
		   
	      
	      sql.add("update t100_segments_MIT\n" + 
	      		"set carrier = 'US'\n" + 
	      		"where carrier = 'HP'");
	      sql.add("create index idx_t100_segments_MIT_cym\n" + 
	      		"	on t100_segments_MIT(carrier, year, month)\n" + 
	      		"	using btree");
	      sql.add("create index idx_t100_segments_MIT_cymod\n" + 
	      		"  on t100_segments_MIT(carrier, year, month, origin, destination)\n" + 
	      		"	using btree\n");
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	      }
	      stmt.executeBatch();
////	      STEP 5: Extract data from result set
//	      while(rs.next()){
//	         //Retrieve by column name
//	         int id  = rs.getInt("id");
//	         int age = rs.getInt("age");
//	         String first = rs.getString("first");
//	         String last = rs.getString("last");
//
//	         //Display values
//	         System.out.print("ID: " + id);
//	         System.out.print(", Age: " + age);
//	         System.out.print(", First: " + first);
//	         System.out.println(", Last: " + last);
//	      }
////	      STEP 6: Clean-up environment
//	      rs.close();
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
	   System.out.println("Goodbye!");
	   long endTime = System.nanoTime();
	   long duration = (endTime - startTime)/1000000/1000/60;
	   System.out.println("That took " + duration + " minutes ");
	}//end main
}

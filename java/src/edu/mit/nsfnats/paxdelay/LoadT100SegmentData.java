package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LoadT100SegmentData {
	// JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost:3306/paxdelay?allowMultiQueries=true";
	   //  Database credentials
	   static final String USER = "saris";
	   static final String PASS = "paxdelay";
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
	      String filename = "/mdsg/bts_raw_csv/T100_SEGMENTS_"+year+".csv";
	      stmt.execute("LOAD DATA LOCAL INFILE '"+filename+"'\n" + 
	      		"INTO TABLE t100_segments\n" + 
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
	      		"origin_airport_id,\n" + 
	      		"origin_airport_seq_id,\n" + 
	      		"origin_city_code,\n" + 
	      		"origin,\n" + 
	      		"origin_city_name,\n" + 
	      		"origin_state,\n" + 
	      		"origin_state_fips,\n" + 
	      		"origin_state_name,\n" + 
	      		"origin_country,\n" + 
	      		"origin_country_name,\n" + 
	      		"origin_wac,\n" + 
	      		"dest_airport_id,\n" + 
	      		"dest_airport_seq_id,\n" + 
	      		"destination_city_code,\n" + 
	      		"destination,\n" + 
	      		"destination_city_name,\n" + 
	      		"destination_state,\n" + 
	      		"destination_state_fips,\n" + 
	      		"destination_state_name,\n" + 
	      		"destination_country,\n" + 
	      		"destination_country_name,\n" + 
	      		"destination_wac,\n" + 
	      		"aircraft_group,\n" + 
	      		"aircraft_type,\n" + 
	      		"aircraft_config,\n" + 
	      		"year,\n" + 
	      		"quarter,\n" + 
	      		"month,\n" + 
	      		"distance_group,\n" + 
	      		"service_class);");
		   
	      sql.add("update t100_segments\n" + 
	      		"set carrier = 'US'\n" + 
	      		"where carrier = 'HP'");
	      sql.add("create index idx_t100_segments_cym\n" + 
	      		"	on t100_segments(carrier, year, month)\n" + 
	      		"	using btree;");
	      sql.add("create index idx_t100_segments_cymod\n" + 
	      		"  on t100_segments(carrier, year, month, origin, destination)\n" + 
	      		"	using btree;\n");
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	      }
	      stmt.executeBatch();
	      //STEP 5: Extract data from result set
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
	      //STEP 6: Clean-up environment
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
	   System.out.println("Goodbye!");
	}//end main
}

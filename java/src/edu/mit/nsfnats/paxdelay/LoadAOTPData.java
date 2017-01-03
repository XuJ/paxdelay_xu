package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LoadAOTPData {
	// JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost:3306/paxdelay?allowMultiQueries=true";
	   //  Database credentials
	   static final String USER = "saris";
	   static final String PASS = "paxdelay";
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
	      sql.add("drop table if exists tmp_load_aotp\n");
	      sql.add("create table tmp_load_aotp\n" + 
	      		"( \n" + 
	      		"	year	numeric(4) not null, \n" + 
	      		"	quarter	int not null,\n" + 
	      		"	month	numeric(2, 0) not null,\n" + 
	      		"	day_of_month	numeric(2, 0) not null,\n" + 
	      		"	day_of_week	numeric(1, 0) not null,\n" + 
	      		"	flight_date	char(10) not null,\n" + 
	      		"	unique_carrier	char(2) not null,\n" + 
	      		"	airline_id	numeric(10, 0) not null,\n" + 
	      		"	carrier	char(6) not null,\n" + 
	      		"	tail_number	varchar(10),\n" + 
	      		"	flight_number	varchar(6) not null,\n" + 
	      		"		origin_airportid int,\n" + 
	      		"		origin_airportseqid int,\n" + 
	      		"		origin_citymarketid int,\n" + 
	      		"	origin	varchar(5) not null,\n" + 
	      		"	origin_city_name	varchar(50),\n" + 
	      		"	origin_state	char(2),\n" + 
	      		"	origin_state_fips	varchar(4),\n" + 
	      		"	origin_state_name	varchar(25),\n" + 
	      		"	origin_wac	numeric(4, 0),\n" + 
	      		"		dest_airportid int,\n" + 
	      		"		dest_airportseqid int,\n" + 
	      		"		dest_citymarketid int,\n" + 
	      		"	destination	varchar(5) not null,\n" + 
	      		"	destination_city_name	varchar(50),\n" + 
	      		"	destination_state	char(2),\n" + 
	      		"	destination_state_fips	numeric(2, 0),\n" + 
	      		"	destination_state_name	varchar(25),\n" + 
	      		"	destination_wac	numeric(4, 0),\n" + 
	      		"	planned_departure_time	char(4) not null,\n" + 
	      		"	actual_departure_time	char(4),\n" + 
	      		"	departure_offset	numeric(4, 0),\n" + 
	      		"	departure_delay	numeric(4, 0),\n" + 
	      		"	departure_delay_15	numeric(2, 0),\n" + 
	      		"	departure_delay_group	numeric(2, 0),\n" + 
	      		"	departure_time_block	char(9),\n" + 
	      		"	taxi_out_duration	numeric(4, 0),\n" + 
	      		"	wheels_off_time	char(4),\n" + 
	      		"	wheels_on_time	char(4),\n" + 
	      		"	taxi_in_duration	numeric(4, 0),\n" + 
	      		"	planned_arrival_time	char(4) not null,\n" + 
	      		"	actual_arrival_time	char(4),\n" + 
	      		"	arrival_offset	numeric(4, 0),\n" + 
	      		"	arrival_delay	numeric(4, 0),\n" + 
	      		"	arrival_delay_15	numeric(2, 0),\n" + 
	      		"	arrival_delay_group	numeric(2, 0),\n" + 
	      		"	arrival_time_block	char(9),\n" + 
	      		"	cancelled	numeric(1, 0) not null,\n" + 
	      		"	cancellation_code	char(1),\n" + 
	      		"	diverted	numeric(1, 0) not null,\n" + 
	      		"	planned_elapsed_time	numeric(4, 0),\n" + 
	      		"	actual_elapsed_time	numeric(4, 0),\n" + 
	      		"	in_air_duration	numeric(4, 0),\n" + 
	      		"	number_flights	numeric(1, 0),\n" + 
	      		"	flight_distance	numeric(5, 0),\n" + 
	      		"	distance_group	numeric(2, 0),\n" + 
	      		"	carrier_delay	numeric(4, 0) default 0.00 not null,\n" + 
	      		"	weather_delay	numeric(4, 0) default 0.00 not null,\n" + 
	      		"	nas_delay	numeric(4, 0) default 0.00 not null,\n" + 
	      		"	security_delay	numeric(4, 0) default 0.00 not null,\n" + 
	      		"	late_aircraft_delay	numeric(4, 0) default 0.00 not null\n" + 
	      		") \n" + 
	      		"ENGINE = MyISAM\n" + 
	      		"partition by list (quarter)\n" + 
	      		"(	partition p_q1 values in (1),\n" + 
	      		"	partition p_q2 values in (2),\n" + 
	      		"	partition p_q3 values in (3),\n" + 
	      		"	partition p_q4 values in (4)\n" + 
	      		")");
	      for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
		   for(int i = 1;i<=12;i++){
			   String filename = "/mdsg/bts_raw_csv/AOTP_2007_"+i+".csv";
			   stmt.execute("LOAD DATA LOCAL INFILE '"+filename+"'\n" + 
			   		"		INTO TABLE tmp_load_aotp\n" +
			   		"		FIELDS TERMINATED BY ','\n" + 
			   		"		OPTIONALLY ENCLOSED BY '\"'\n" + 
			   		"		LINES TERMINATED BY '\\n'\n" + 
			   		"		IGNORE 1 LINES\n" + 
			   		"		(\n" + 
			   		"			year,\n" + 
			   		"			quarter,\n" + 
			   		"			month,\n" + 
			   		"			day_of_month,\n" + 
			   		"			day_of_week,\n" + 
			   		"			flight_date,\n" + 
			   		"			unique_carrier,\n" + 
			   		"			airline_id,\n" + 
			   		"			carrier,\n" + 
			   		"			tail_number,\n" + 
			   		"			flight_number,\n" + 
			   		"				origin_airportid,\n" + 
			   		"				origin_airportseqid,\n" + 
			   		"				origin_citymarketid,\n" + 
			   		"			origin,\n" + 
			   		"			origin_city_name,\n" + 
			   		"			origin_state,\n" + 
			   		"			origin_state_fips,\n" + 
			   		"			origin_state_name,\n" + 
			   		"			origin_wac,\n" + 
			   		"				dest_airportid,\n" + 
			   		"				dest_airportseqid,\n" + 
			   		"				dest_citymarketid,\n" + 
			   		"			destination,\n" + 
			   		"			destination_city_name,\n" + 
			   		"			destination_state,\n" + 
			   		"			destination_state_fips,\n" + 
			   		"			destination_state_name,\n" + 
			   		"			destination_wac,\n" + 
			   		"			planned_departure_time,\n" + 
			   		"			actual_departure_time,\n" + 
			   		"			departure_offset,\n" + 
			   		"			departure_delay,\n" + 
			   		"			departure_delay_15,\n" + 
			   		"			departure_delay_group,\n" + 
			   		"			departure_time_block,\n" + 
			   		"			taxi_out_duration,\n" + 
			   		"			wheels_off_time,\n" + 
			   		"			wheels_on_time,\n" + 
			   		"			taxi_in_duration,\n" + 
			   		"			planned_arrival_time,\n" + 
			   		"			actual_arrival_time,\n" + 
			   		"			arrival_offset,\n" + 
			   		"			arrival_delay,\n" + 
			   		"			arrival_delay_15,\n" + 
			   		"			arrival_delay_group,\n" + 
			   		"			arrival_time_block,\n" + 
			   		"			cancelled,\n" + 
			   		"			cancellation_code,\n" + 
			   		"			diverted,\n" + 
			   		"			planned_elapsed_time,\n" + 
			   		"			actual_elapsed_time,\n" + 
			   		"			in_air_duration,\n" + 
			   		"			number_flights,\n" + 
			   		"			flight_distance,\n" + 
			   		"			distance_group,\n" + 
			   		"			carrier_delay,\n" + 
			   		"			weather_delay,\n" + 
			   		"			nas_delay,\n" + 
			   		"			security_delay,\n" + 
			   		"			late_aircraft_delay\n" + 
			   		"		);");
		   }
		   
	      sql.add("insert into aotp\n" + 
	      		"(year, quarter, month, day_of_month, day_of_week, flight_date, unique_carrier, airline_id, carrier, tail_number, flight_number, origin, origin_city_name, origin_state, origin_state_fips, origin_state_name,\n" + 
	      		"origin_wac, destination, destination_city_name, destination_state, destination_state_fips, destination_state_name, destination_wac, planned_departure_time, actual_departure_time, departure_offset,\n" + 
	      		"departure_delay, departure_delay_15, departure_delay_group, departure_time_block, taxi_out_duration, wheels_off_time, wheels_on_time, taxi_in_duration, planned_arrival_time, actual_arrival_time,\n" + 
	      		"arrival_offset, arrival_delay, arrival_delay_15, arrival_delay_group, arrival_time_block, cancelled, cancellation_code, diverted, planned_elapsed_time, actual_elapsed_time, in_air_duration,\n" + 
	      		"number_flights, flight_distance, distance_group, carrier_delay, weather_delay, nas_delay, security_delay, late_aircraft_delay)\n" + 
	      		"\n" + 
	      		"select  year, quarter, month, day_of_month, day_of_week, \n" + 
	      		"STR_TO_DATE(flight_date,'%Y-%m-%d') as flight_date, \n" + 
	      		"unique_carrier, airline_id, carrier, tail_number, flight_number, origin, origin_city_name, origin_state, origin_state_fips, origin_state_name,\n" + 
	      		"origin_wac, destination, destination_city_name, destination_state, destination_state_fips, destination_state_name, destination_wac, planned_departure_time, actual_departure_time, departure_offset,\n" + 
	      		"departure_delay, departure_delay_15, departure_delay_group, departure_time_block, taxi_out_duration, wheels_off_time, wheels_on_time, taxi_in_duration, planned_arrival_time, actual_arrival_time,\n" + 
	      		"arrival_offset, arrival_delay, arrival_delay_15, arrival_delay_group, arrival_time_block, cancelled, cancellation_code, diverted, planned_elapsed_time, actual_elapsed_time, in_air_duration,\n" + 
	      		"number_flights, flight_distance, distance_group, carrier_delay, weather_delay, nas_delay, security_delay, late_aircraft_delay\n" + 
	      		"from tmp_load_aotp");
	      sql.add("drop table if exists tmp_load_aotp;");
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

//SUKITJANUPARP
//create table db1b_ticketed_route_demands, db1b_route_demands
//XuJiao
//That took 12 minutes
//db1b_ticketed_route_demands: 1,603,147 (MIT: 1,604,797)
//db1b_route_demands: 1,497,798 (MIT: 1,499,404)

package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateDB1BRouteDemandsTable {
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
	      
	      sql.add("drop table if exists temp_db1b_route_demands\n");
	      sql.add("create table temp_db1b_route_demands\n" + 
	      		"select  gc.year, gc.quarter,	gc.market_id,\n" + 
	      		"gc.passengers, gc.distance,\n" + 
	      		"gc.total_ucs_id, \n" + 
	      		"sum(gc.ucs_id_max) as last_ucs_id, \n" + 
	      		"sum(gc.ucs_id_min) as first_ucs_id,\n" + 
	      		"gc.num_flights\n" + 
	      		"	from (\n" + 
	      		"			select  j.year, j.quarter,	j.market_id,  \n" + 
	      		"			avg(j.passengers) as passengers,\n" + 
	      		"			sum(j.distance) as distance,\n" + 
	      		"			sum(j.ucs_id) as total_ucs_id,\n" + 
	      		"			convert(SUBSTRING_INDEX(group_concat(j.ucs_id order by j.sequence_number desc),',',1 ),decimal) ucs_id_max,\n" + 
	      		"			convert(SUBSTRING_INDEX(group_concat(j.ucs_id order by j.sequence_number),',',1 ),decimal) ucs_id_min,\n" + 
	      		"			count(j.sequence_number) as num_flights\n" + 
	      		"			from (\n" + 
	      		"					select \n" + 
	      		"					db1b.year as year,\n" + 
	      		"					db1b.quarter as quarter,\n" + 
	      		"					db1b.market_id as market_id,\n" + 
	      		"					db1b.sequence_number as sequence_number,\n" + 
	      		"					db1b.passengers as passengers,\n" + 
	      		"					db1b.distance as distance,\n" + 
	      		"					ucs.id as ucs_id\n" + 
	      		"					from db1b_unique_carrier_segments ucs\n" + 
	      		"					join db1b_coupons db1b\n" + 
	      		"						on db1b.quarter = ucs.quarter\n" + 
	      		"						and db1b.ticketing_carrier = ucs.ticketing_carrier\n" + 
	      		"						and db1b.operating_carrier = ucs.operating_carrier\n" + 
	      		"						and db1b.origin = ucs.origin\n" + 
	      		"						and db1b.destination = ucs.destination\n" + 
	      		"				) j\n" + 
	      		"		group by j.year, j.quarter,	j.market_id\n" + 
	      		"		having count(j.sequence_number) <= 3\n" + 
	      		"	) gc\n" + 
	      		"	group by  gc.year, gc.quarter,	gc.market_id\n");
	      sql.add("create index idx_temp_db1b_rd_yqucs\n" + 
	      		"  on temp_db1b_route_demands(year, quarter, first_ucs_id, last_ucs_id)\n");
	      sql.add("drop table if exists db1b_ticketed_route_demands\n");
	      sql.add("create table db1b_ticketed_route_demands\n" + 
	      		"(\n" + 
	      		"  year numeric(4) not null,\n" + 
	      		"  quarter int not null,\n" + 
	      		"  first_ticketing_carrier varchar(3) not null,\n" + 
	      		"  second_ticketing_carrier varchar(3),\n" + 
	      		"  num_flights int not null,\n" + 
	      		"  origin char(3) not null,\n" + 
	      		"  connection char(3),\n" + 
	      		"  destination char(3) not null,\n" + 
	      		"  first_operating_carrier varchar(3) not null,\n" + 
	      		"  second_operating_carrier varchar(3) ,\n" + 
	      		"  passengers numeric(4) not null\n" + 
	      		")\n" + 
	      		"ENGINE = MyISAM\n");
	      sql.add("insert into db1b_ticketed_route_demands\n" + 
	      		"(\n" + 
	      		" year, quarter, first_ticketing_carrier, second_ticketing_carrier, num_flights, origin, connection, destination,\n" + 
	      		"  first_operating_carrier, second_operating_carrier, passengers\n" + 
	      		")\n"+
	            "select tin.year, tin.quarter, first.ticketing_carrier, null, 1,\n" + 
	      		"  first.origin, null, first.destination, first.operating_carrier, \n" + 
	      		"	null, tin.passengers\n" + 
	      		"from db1b_unique_carrier_segments first\n" + 
	      		"join \n" + 
	      		"(select trd.year, trd.quarter,\n" + 
	      		"   trd.first_ucs_id,\n" + 
	      		"   sum(trd.passengers) as passengers\n" + 
	      		" from temp_db1b_route_demands trd\n" + 
	      		" where trd.num_flights = 1\n" + 
	      		" group by trd.year, trd.quarter,\n" + 
	      		"   trd.first_ucs_id) tin\n" + 
	      		"on tin.first_ucs_id = first.id\n" + 
	      		"union all\n" + 
	      		"select tin.year, tin.quarter,\n" + 
	      		"  first.ticketing_carrier, second.ticketing_carrier, 2,\n" + 
	      		"  first.origin, first.destination, second.destination, \n" + 
	      		"  first.operating_carrier, second.operating_carrier,\n" + 
	      		"  tin.passengers\n" + 
	      		
	      		"from\n" + 
	      		"(select trd.year, trd.quarter,\n" + 
	      		"   trd.first_ucs_id, trd.last_ucs_id,\n" + 
	      		"   sum(trd.passengers) as passengers\n" + 
	      		" from temp_db1b_route_demands trd\n" + 
	      		" where trd.num_flights = 2\n" + 
	      		" group by trd.year, trd.quarter,\n" + 
	      		"   trd.first_ucs_id, trd.last_ucs_id) tin\n" + 
	      		"join db1b_unique_carrier_segments first\n" + 
	      		"  on first.id = tin.first_ucs_id\n" + 
	      		"join db1b_unique_carrier_segments second\n" + 
	      		"  on second.id = tin.last_ucs_id\n" + 
	      		"  and second.origin = first.destination\n");
	      sql.add("drop table if exists db1b_route_demands");
	      sql.add("create table db1b_route_demands\n" + 
	      		"(\n" + 
	      		"  year numeric(4) not null,\n" + 
	      		"  quarter int not null,\n" + 
	      		"  num_flights int not null,\n" + 
	      		"  origin char(3) not null,\n" + 
	      		"  connection char(3),\n" + 
	      		"  destination char(3) not null,\n" + 
	      		"  first_operating_carrier varchar(3) not null,\n" + 
	      		"  second_operating_carrier varchar(3),\n" + 
	      		"  passengers numeric(4) not null\n" + 
	      		")\n" + 
	      		"ENGINE = MyISAM\n");
	      sql.add("insert into db1b_route_demands\n" + 
	      		"(year, quarter, num_flights, origin, connection, destination, first_operating_carrier, second_operating_carrier, passengers)\n" + 
	      		"select trd.year, trd.quarter, trd.num_flights,\n" + 
	      		"  trd.origin, trd.connection, trd.destination,\n" + 
	      		"  trd.first_operating_carrier, trd.second_operating_carrier,\n" + 
	      		"  sum(trd.passengers) as passengers\n" + 
	      		"from db1b_ticketed_route_demands trd\n" + 
	      		"group by trd.year, trd.quarter, trd.num_flights,\n" + 
	      		"  trd.origin, trd.connection, trd.destination,\n" + 
	      		"  trd.first_operating_carrier, trd.second_operating_carrier\n");
	      //General indices for querying route demands
	      sql.add("create index idx_db1b_route_demands_c1yqodnf\n" + 
	      		"  on db1b_route_demands(year, quarter, num_flights, first_operating_carrier, origin, destination)\n");
	      sql.add("create index idx_db1b_route_demands_c1c2yqodc\n" + 
	      		"  on db1b_route_demands(year, quarter, first_operating_carrier, second_operating_carrier, origin, destination, connection)\n");
	      //The following two indices are used by PAP
	      sql.add("create index idx_db1b_route_demands_c1yq\n" + 
	      		"  on db1b_route_demands(first_operating_carrier, year, quarter)");
	      sql.add("create index idx_db1b_route_demands_c2yq\n" + 
	      		"  on db1b_route_demands(second_operating_carrier, year, quarter)\n");
	      //The following index is used by itinerary generation
	      sql.add("create index idx_db1b_route_demands_c1qy\n" + 
	      		"  on db1b_route_demands(first_operating_carrier, quarter, year)\n");
	      sql.add("drop table if exists temp_db1b_route_demands\n");
	      
	      
	      System.out.println(sql);
	      
	     
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

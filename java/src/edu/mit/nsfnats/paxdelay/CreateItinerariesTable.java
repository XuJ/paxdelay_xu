package edu.mit.nsfnats.paxdelay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateItinerariesTable {
	// JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost:3306/paxdelay?allowMultiQueries=true";
	   //  Database credentials
	   static final String USER = "root";
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
	      
	      sql.add("drop table if exists itineraries");
	      sql.add("create table itineraries\n" + 
	      		"(\n" + 
	      		"  id integer not null auto_increment, primary key (id),\n" + 
	      		"  year numeric(4) not null,\n" + 
	      		"  quarter int not null,\n" + 
	      		"  month numeric(2) not null,\n" + 
	      		"  day_of_month numeric(2) not null,\n" + 
	      		"  day_of_week numeric(1) not null,\n" + 
	      		"  hour_of_day numeric(2) not null,\n" + 
	      		"  minutes_of_hour numeric(2) not null,\n" + 
	      		"  num_flights numeric(1) not null,\n" + 
	      		"  multi_carrier_flag numeric(1) not null,\n" + 
	      		"  first_operating_carrier char(6) not null,\n" + 
	      		"  second_operating_carrier char(6), \n" + 
	      		"  origin char(3) not null,\n" + 
	      		"  connection char(3),\n" + 
	      		"  destination char(3) not null,\n" + 
	      		"\n" + 
	      		"  planned_departure_time datetime not null,\n" + 
	      		"  planned_departure_tz char(25),\n" + 
	      		"  planned_departure_local_hour numeric(2),\n" + 
	      		"\n" + 
	      		"\n" + 
	      		"  planned_arrival_time datetime not null,\n" + 
	      		"  planned_arrival_tz char(25),\n" + 
	      		"  planned_arrival_local_hour numeric(2),\n" + 
	      		"\n" + 
	      		"  layover_duration numeric(6),\n" + 
	      		"  first_flight_id numeric(12) not null,\n" + 
	      		"  second_flight_id numeric(12)\n" + 
	      		")");
	      sql.add("insert into itineraries\n" + 
	      		"(\n" + 
	      		"  year, quarter, month, day_of_month, day_of_week, hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag, first_operating_carrier, second_operating_carrier, \n" + 
	      		"  origin, connection, destination,\n" + 
	      		"  planned_departure_time,\n" + 
	      		"  planned_departure_tz,\n" + 
	      		"  planned_departure_local_hour,\n" + 
	      		"\n" + 
	      		"  planned_arrival_time,\n" + 
	      		"  planned_arrival_tz,\n" + 
	      		"  planned_arrival_local_hour,\n" + 
	      		"\n" + 
	      		"  layover_duration, first_flight_id, second_flight_id\n" + 
	      		")\n" + 
	      		"select \n" + 
	      		"	ft.year as year,\n" + 
	      		"	ft.quarter as quarter,\n" + 
	      		"	ft.month as month,\n" + 
	      		"	ft.day_of_month as day_of_month,\n" + 
	      		"	ft.day_of_week as day_of_week,\n" + 
	      		"	ft.hour_of_day,\n" + 
	      		"	ft.minutes_of_hour as minutes_of_hour,\n" + 
	      		"	1 as num_flights, \n" + 
	      		"	0 as multi_carrier_flag,\n" + 
	      		"	ft.carrier as first_operating_carrier,\n" + 
	      		"	null as second_operating_carrier,\n" + 
	      		"	ft.origin as origin,\n" + 
	      		"	null as connection,\n" + 
	      		"	ft.destination as destination,\n" + 
	      		"	ft.planned_departure_time as planned_departure_time,\n" + 
	      		"	ft.planned_departure_tz as planned_departure_tz,\n" + 
	      		"	ft.planned_departure_local_hour as planned_departure_local_hour,\n" + 
	      		" \n" + 
	      		"	ft.planned_arrival_time as planned_arrival_time,\n" + 
	      		"	ft.planned_arrival_tz as planned_arrival_tz,\n" + 
	      		"	ft.planned_arrival_local_hour as planned_arrival_local_hour,\n" + 
	      		"	null as layover_duration,\n" + 
	      		"	ft.id as first_flight_id,\n" + 
	      		"	null as second_flight_id\n" + 
	      		"from temp_itineraries ti\n" + 
	      		"join flights ft \n" + 
	      		"on ft.id = ti.first_flight_id\n" + 
	      		"where ti.num_flights = 1");
	      sql.add("insert into itineraries\n" + 
	      		"(  year, quarter, month, day_of_month, day_of_week, hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag, first_operating_carrier, second_operating_carrier, \n" + 
	      		"  origin, connection, destination,\n" + 
	      		"  planned_departure_time,\n" + 
	      		"  planned_departure_tz,\n" + 
	      		"  planned_departure_local_hour,\n" + 
	      		"\n" + 
	      		"  planned_arrival_time,\n" + 
	      		"  planned_arrival_tz,\n" + 
	      		"  planned_arrival_local_hour,\n" + 
	      		"\n" + 
	      		"  layover_duration, first_flight_id, second_flight_id\n" + 
	      		")\n" + 
	      		"select \n" + 
	      		"	ft1.year as year,\n" + 
	      		"	ft1.quarter as quarter,\n" + 
	      		"	ft1.month as month,\n" + 
	      		"	ft1.day_of_month as day_of_month,\n" + 
	      		"	ft1.day_of_week as day_of_week,\n" + 
	      		"	ft1.hour_of_day as hour_of_day,\n" + 
	      		"	ft1.minutes_of_hour as minutes_of_hour,\n" + 
	      		"	2 as num_flights, \n" + 
	      		"\n" + 
	      		"	case when ti.num_flights = 2 and ft1.carrier <> ft2.carrier\n" + 
	      		"		then 1\n" + 
	      		"		else 0\n" + 
	      		"	end as multi_carrier_flag,\n" + 
	      		"\n" + 
	      		"	ft1.carrier as first_operating_carrier,\n" + 
	      		"	ft2.carrier as second_operating_carrier, \n" + 
	      		"	ft1.origin as origin,\n" + 
	      		"	ft1.destination as connection,\n" + 
	      		"	ft2.destination as destination, \n" + 
	      		"\n" + 
	      		"	ft1.planned_departure_time as planned_departure_time,\n" + 
	      		"	ft1.planned_departure_tz as planned_departure_tz,\n" + 
	      		"	ft1.planned_departure_local_hour as planned_departure_local_hour,\n" + 
	      		"\n" + 
	      		"	ft2.planned_arrival_time as planned_arrival_time,\n" + 
	      		"	ft2.planned_arrival_tz as planned_arrival_tz,\n" + 
	      		"	ft2.planned_arrival_local_hour as planned_arrival_local_hour,\n" + 
	      		"\n" + 
	      		"	TIMESTAMPDIFF(minute, ft2.planned_departure_time, ft1.planned_arrival_time) as layover_duration,\n" + 
	      		"	ft1.id as first_flight_id, \n" + 
	      		"	ft2.id as second_flight_id\n" + 
	      		"from temp_itineraries ti\n" + 
	      		"join flights ft1 on ft1.id = ti.first_flight_id\n" + 
	      		"join flights ft2 on ft2.id = ti.second_flight_id\n" + 
	      		"where ti.num_flights = 2");
	      sql.add("drop table if exists itineraries_status;\n" + 
	      		"create table itineraries_status (\n" + 
	      		"	iteration_number int(11) auto_increment, primary key (iteration_number),\n" + 
	      		"	start_time datetime\n" + 
	      		")");
	      sql.add("drop procedure if exists populate_itineraries");
	      sql.add("SHOW PROCEDURE STATUS");
	      sql.add("delimiter $$\n" + 
	      		"\n" + 
	      		"create procedure populate_itineraries()\n" + 
	      		"begin\n" + 
	      		"    declare done int default 0;\n" + 
	      		"\n" + 
	      		"    declare op_year numeric(4);\n" + 
	      		"    declare op_quarter int;\n" + 
	      		"    declare op_carrier char(6);\n" + 
	      		"    \n" + 
	      		"    -- 277 rows\n" + 
	      		"    declare rdcursor cursor for     select rd.year, rd.quarter, rd.first_operating_carrier\n" + 
	      		"                        from route_demands rd\n" + 
	      		"                        group by rd.year, rd.quarter, rd.first_operating_carrier\n" + 
	      		"                        order by rd.year, rd.quarter, rd.first_operating_carrier;\n" + 
	      		"\n" + 
	      		"    declare continue handler for not found set done = 1;\n" + 
	      		"\n" + 
	      		"    open rdcursor;\n" + 
	      		"\n" + 
	      		"    cursor_loop:LOOP\n" + 
	      		"        fetch rdcursor into op_year, op_quarter, op_carrier;\n" + 
	      		"        if done then leave cursor_loop; end if;\n" + 
	      		"\n" + 
	      		"        drop table if exists temp_iti_1;\n" + 
	      		"        drop table if exists temp_iti_2;\n" + 
	      		"        drop table if exists temp_iti_ft1ucr;\n" + 
	      		"        \n" + 
	      		"-- Log iteration start time\n" + 
	      		"        insert into itineraries_status\n" + 
	      		"        values (null, now());        \n" + 
	      		"         \n" + 
	      		"--STEP 1\n" + 
	      		"-- Create the non-stop itineraries\n" + 
	      		"        create table temp_iti_1\n" + 
	      		"        select \n" + 
	      		"                ft.year, ft.quarter, ft.month, ft.day_of_month, ft.day_of_week,\n" + 
	      		"                ft.hour_of_day, ft.minutes_of_hour, 1, 0, \n" + 
	      		"                ft.carrier, ft.origin, ft.destination, \n" + 
	      		"                \n" + 
	      		"                ft.planned_departure_time   as planned_departure_time,\n" + 
	      		"                ft.planned_departure_tz         as planned_departure_tz,\n" + 
	      		"                ft.planned_departure_local_hour as planned_departure_local_hour,\n" + 
	      		"                     \n" + 
	      		"                ft.planned_arrival_time     as planned_arrival_time,\n" + 
	      		"                ft.planned_arrival_tz           as planned_arrival_tz,\n" + 
	      		"                ft.planned_arrival_local_hour   as planned_arrival_local_hour,\n" + 
	      		"                \n" + 
	      		"                ft.id\n" + 
	      		"          \n" + 
	      		"        from flights ft\n" + 
	      		"        where ft.year = op_year and ft.quarter = op_quarter  and ft.carrier = op_carrier; \n" + 
	      		"            \n" + 
	      		"        insert into itineraries (\n" + 
	      		"                year, quarter, month, day_of_month, day_of_week,\n" + 
	      		"                hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag,\n" + 
	      		"                first_operating_carrier, origin, destination, \n" + 
	      		"                   \n" + 
	      		"                planned_departure_time,\n" + 
	      		"                planned_departure_tz,\n" + 
	      		"                planned_departure_local_hour,\n" + 
	      		"                \n" + 
	      		"                planned_arrival_time,\n" + 
	      		"                planned_arrival_tz,\n" + 
	      		"                planned_arrival_local_hour,\n" + 
	      		"                   \n" + 
	      		"                first_flight_id)\n" + 
	      		"        select \n" + 
	      		"                ft.year, ft.quarter, ft.month, ft.day_of_month, ft.day_of_week,\n" + 
	      		"                ft.hour_of_day, ft.minutes_of_hour, 1, 0, \n" + 
	      		"                ft.carrier, ft.origin, ft.destination, \n" + 
	      		"                \n" + 
	      		"                ft.planned_departure_time,\n" + 
	      		"                ft.planned_departure_tz,\n" + 
	      		"                ft.planned_departure_local_hour,\n" + 
	      		"                 \n" + 
	      		"                ft.planned_arrival_time,\n" + 
	      		"                ft.planned_arrival_tz,\n" + 
	      		"                ft.planned_arrival_local_hour,\n" + 
	      		"                \n" + 
	      		"                ft.id as first_flight_id\n" + 
	      		"        from temp_iti_1 ft;\n" + 
	      		"--!STEP 1\n" + 
	      		"\n" + 
	      		"--STEP 2\n" + 
	      		"-- Create the one stop itineraries\n" + 
	      		"        create table temp_iti_ft1ucr\n" + 
	      		"        select \n" + 
	      		"                ft1.id                           as ft1_id,\n" + 
	      		"                ft1.year                         as ft1_year,\n" + 
	      		"                ft1.quarter                      as ft1_quarter,\n" + 
	      		"                ft1.month                        as ft1_month,\n" + 
	      		"                ft1.day_of_month                 as ft1_day_of_month,\n" + 
	      		"                ft1.day_of_week                  as ft1_day_of_week,\n" + 
	      		"                ft1.hour_of_day                  as ft1_hour_of_day,\n" + 
	      		"                ft1.minutes_of_hour              as ft1_minutes_of_hour,\n" + 
	      		"                ft1.carrier                      as ft1_carrier,\n" + 
	      		"                ft1.planned_departure_time   as ft1_planned_departure_time,\n" + 
	      		"                ft1.planned_departure_tz         as ft1_planned_departure_tz,\n" + 
	      		"                ft1.planned_departure_local_hour as ft1_planned_departure_local_hour,\n" + 
	      		"                ft1.planned_arrival_time     as ft1_planned_arrival_time,\n" + 
	      		"                \n" + 
	      		"                ucr.second_operating_carrier     as ucr_second_operating_carrier,\n" + 
	      		"                ucr.origin                       as ucr_origin,\n" + 
	      		"                ucr.connection                   as ucr_connection,\n" + 
	      		"                ucr.destination                  as ucr_destination\n" + 
	      		"        from flights ft1\n" + 
	      		"        join unique_carrier_routes ucr \n" + 
	      		"                on ucr.first_operating_carrier   = ft1.carrier \n" + 
	      		"                and ucr.origin                   = ft1.origin \n" + 
	      		"                and ucr.connection               = ft1.destination\n" + 
	      		"        where ft1.year                           = op_year \n" + 
	      		"          and ft1.quarter                        = op_quarter \n" + 
	      		"          and ft1.carrier                        = op_carrier \n" + 
	      		"          and ucr.year                           = op_year \n" + 
	      		"          and ucr.first_operating_carrier        = op_carrier; \n" + 
	      		"                \n" + 
	      		"        alter table temp_iti_ft1ucr\n" + 
	      		"        add column ft1_planned_arrival_time_add30 datetime,\n" + 
	      		"        add column ft1_planned_arrival_time_add300 datetime;\n" + 
	      		"        \n" + 
	      		"        update temp_iti_ft1ucr\n" + 
	      		"        set ft1_planned_arrival_time_add30  = date_add(ft1_planned_arrival_time, interval 30 minute),\n" + 
	      		"            ft1_planned_arrival_time_add300 = date_add(ft1_planned_arrival_time, interval 300 minute);\n" + 
	      		"            \n" + 
	      		"        create index idx_temp_iti\n" + 
	      		"         on temp_iti_ft1ucr(\n" + 
	      		"                ft1_carrier, \n" + 
	      		"                ft1_day_of_month,\n" + 
	      		"                ft1_day_of_week,\n" + 
	      		"                ft1_hour_of_day,\n" + 
	      		"                ft1_id,\n" + 
	      		"                ft1_month, \n" + 
	      		"                ft1_planned_arrival_time,\n" + 
	      		"                ft1_planned_arrival_time_add30,\n" + 
	      		"                ft1_planned_arrival_time_add300,\n" + 
	      		"                ft1_planned_departure_time,\n" + 
	      		"                ft1_planned_departure_tz,\n" + 
	      		"                ft1_quarter,\n" + 
	      		"                ucr_connection,\n" + 
	      		"                ucr_destination,\n" + 
	      		"                ucr_origin,\n" + 
	      		"                ucr_second_operating_carrier);\n" + 
	      		"                \n" + 
	      		"        create table temp_iti_2\n" + 
	      		"        select \n" + 
	      		"--  year(ttt.ft1_planned_departure_time) as year, \n" + 
	      		"                null as year, \n" + 
	      		"                ttt.ft1_quarter, \n" + 
	      		"                ttt.ft1_month, \n" + 
	      		"                ttt.ft1_day_of_month, \n" + 
	      		"                ttt.ft1_day_of_week,\n" + 
	      		"                ttt.ft1_hour_of_day, \n" + 
	      		"--  minute(ttt.ft1_planned_departure_time) as minutes_of_hour, \n" + 
	      		"                null as minutes_of_hour, \n" + 
	      		"                2,\n" + 
	      		"                case when (ft2.day_of_month - ttt.ft1_day_of_month) = 0 then 0 else 1 end as multi_carrier_flag,\n" + 
	      		"                \n" + 
	      		"                ttt.ft1_carrier, \n" + 
	      		"                ft2.carrier,\n" + 
	      		"                ttt.ucr_origin, \n" + 
	      		"                ttt.ucr_connection, \n" + 
	      		"                ttt.ucr_destination,\n" + 
	      		"                \n" + 
	      		"                ttt.ft1_planned_departure_time as planned_departure_time,\n" + 
	      		"                ttt.ft1_planned_departure_tz as planned_departure_tz,\n" + 
	      		"--  ttt.ft1_planned_departure_local_hour as planned_departure_local_hour,\n" + 
	      		"                null as planned_departure_local_hour,\n" + 
	      		"                \n" + 
	      		"                ft2.planned_arrival_time as planned_arrival_time,\n" + 
	      		"                ft2.planned_arrival_tz as planned_arrival_tz,\n" + 
	      		"--  ft2.planned_arrival_local_hour as planned_arrival_local_hour,\n" + 
	      		"                null as planned_arrival_local_hour,\n" + 
	      		"                \n" + 
	      		"                TIMESTAMPDIFF(minute, ft2.planned_departure_time, ttt.ft1_planned_arrival_time) as layover_duration,\n" + 
	      		"                \n" + 
	      		"                ttt.ft1_id, \n" + 
	      		"                ft2.id as second_flight_id\n" + 
	      		"        from flights ft2\n" + 
	      		"        join temp_iti_ft1ucr ttt\n" + 
	      		"                on ft2.carrier                     = ttt.ucr_second_operating_carrier \n" + 
	      		"                and ft2.origin                     = ttt.ucr_connection  \n" + 
	      		"                and ft2.destination                = ttt.ucr_destination\n" + 
	      		"                and ft2.planned_departure_time >= ttt.ft1_planned_arrival_time_add30\n" + 
	      		"                and ft2.planned_departure_time <= ttt.ft1_planned_arrival_time_add300;          \n" + 
	      		"                \n" + 
	      		"        insert into itineraries (\n" + 
	      		"                year, \n" + 
	      		"                quarter, \n" + 
	      		"                month, \n" + 
	      		"                day_of_month, \n" + 
	      		"                day_of_week,\n" + 
	      		"                \n" + 
	      		"                hour_of_day, \n" + 
	      		"                minutes_of_hour, \n" + 
	      		"                num_flights,\n" + 
	      		"                multi_carrier_flag,\n" + 
	      		"                \n" + 
	      		"                first_operating_carrier, \n" + 
	      		"                second_operating_carrier,\n" + 
	      		"                origin, \n" + 
	      		"                connection, \n" + 
	      		"                destination,\n" + 
	      		"                \n" + 
	      		"                planned_departure_time,\n" + 
	      		"                planned_departure_tz,\n" + 
	      		"                planned_departure_local_hour,\n" + 
	      		"                \n" + 
	      		"                planned_arrival_time,\n" + 
	      		"                planned_arrival_tz,\n" + 
	      		"                planned_arrival_local_hour,       \n" + 
	      		"                \n" + 
	      		"                layover_duration,\n" + 
	      		"                first_flight_id, \n" + 
	      		"                second_flight_id)\n" + 
	      		"        select \n" + 
	      		"                ft.year,\n" + 
	      		"                ft.ft1_quarter,\n" + 
	      		"                ft.ft1_month,\n" + 
	      		"                ft.ft1_day_of_month,\n" + 
	      		"                \n" + 
	      		"                ft.ft1_day_of_week,\n" + 
	      		"                ft.ft1_hour_of_day,\n" + 
	      		"                ft.minutes_of_hour,\n" + 
	      		"                2,\n" + 
	      		"                ft.multi_carrier_flag,\n" + 
	      		"                \n" + 
	      		"                ft.ft1_carrier,\n" + 
	      		"                ft.carrier,\n" + 
	      		"                ft.ucr_origin,\n" + 
	      		"                ft.ucr_connection,\n" + 
	      		"                ft.ucr_destination,\n" + 
	      		"                \n" + 
	      		"                ft.planned_departure_time,\n" + 
	      		"                ft.planned_departure_tz,\n" + 
	      		"                ft.planned_departure_local_hour,\n" + 
	      		"                \n" + 
	      		"                ft.planned_arrival_time,\n" + 
	      		"                ft.planned_arrival_tz,\n" + 
	      		"                ft.planned_arrival_local_hour,\n" + 
	      		"                \n" + 
	      		"                ft.layover_duration,\n" + 
	      		"                ft.ft1_id,\n" + 
	      		"                ft.second_flight_id\n" + 
	      		"        from temp_iti_2 ft;\n" + 
	      		"--!STEP 2\n" + 
	      		"          \n" + 
	      		"        drop table if exists temp_iti_1;\n" + 
	      		"        drop table if exists temp_iti_2;\n" + 
	      		"        drop table if exists temp_iti_ft1ucr;\n" + 
	      		"  \n" + 
	      		"    end loop;\n" + 
	      		"    \n" + 
	      		"    close rdcursor;\n" + 
	      		"end$$\n" + 
	      		"\n" + 
	      		"delimiter ");
	      sql.add("call populate_itineraries()");
	      sql.add("drop procedure if exists populate_itineraries");
	      sql.add("create index idx_itineraries_ft1ft2\n" + 
	      		"  on itineraries(first_flight_id, second_flight_id)");
	      sql.add("create index idx_itineraries_fod \n" + 
	      		"  on itineraries(month, first_operating_carrier, origin, destination)");
	      sql.add("create index idx_itineraries_c1c2\n" + 
	      		"  on itineraries(first_operating_carrier, second_operating_carrier)");
	      sql.add("create index idx_itineraries_c1c2ym\n" + 
	      		"  on itineraries(first_operating_carrier, second_operating_carrier, year, month)");
	      sql.add("create index bmx_itineraries_c1ymmc\n" + 
	      		"  on itineraries(first_operating_carrier, year, month, multi_carrier_flag)");
	      sql.add("create index idx_itineraries_c1c2ymdm\n" + 
	      		"  on itineraries(first_operating_carrier, second_operating_carrier, year, month, day_of_month)");
	      sql.add("create index bmx_itineraries_ymdm\n" + 
	      		"  on itineraries(year, month, day_of_month)");
	      
	      
	     
	     
	     for(Object s:sql){
	    	  stmt.addBatch(s.toString());
	    	  System.out.println("hey");
	      }
	      stmt.executeBatch();
	      stmt.clearBatch();
	      sql.clear();
	   
	      
	     
	      
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

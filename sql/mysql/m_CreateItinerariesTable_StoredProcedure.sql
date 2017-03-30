drop table if exists itineraries;
-- OPHASWONGSE
-- Remove suffix "_UTC" from all column
-- Change data type of planned_departure_tz and planned_arrival_tz from char(15) to char(25)
-- XuJiao
-- This file uses stored procedure populate_itineraries() to create itineraries table
-- That took 3 days

create table itineraries
(
  id integer not null auto_increment, primary key (id),
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  day_of_week numeric(1) not null,
  hour_of_day numeric(2) not null,
  minutes_of_hour numeric(2) not null,
  num_flights numeric(1) not null,
  multi_carrier_flag numeric(1) not null,
  first_operating_carrier char(6) not null,
  second_operating_carrier char(6), 
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,

  planned_departure_time datetime not null,
  planned_departure_tz char(25),
  planned_departure_local_hour numeric(2),


  planned_arrival_time datetime not null,
  planned_arrival_tz char(25),
  planned_arrival_local_hour numeric(2),

  layover_duration numeric(4),
  first_flight_id numeric(12) not null,
  second_flight_id numeric(12)
);


-- The non-stop and one-stop insert statements from flights and temp_itineraries
-- are identical to the strored procedure populate_itineraries ()

/*
-- Insert all non-stop itineraries
insert into itineraries
(
  year, quarter, month, day_of_month, day_of_week, hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag, first_operating_carrier, second_operating_carrier, 
  origin, connection, destination,
  planned_departure_time,
  planned_departure_tz,
  planned_departure_local_hour,

  planned_arrival_time,
  planned_arrival_tz,
  planned_arrival_local_hour,

  layover_duration, first_flight_id, second_flight_id
)
select 
	ft.year as year,
	ft.quarter as quarter,
	ft.month as month,
	ft.day_of_month as day_of_month,
	ft.day_of_week as day_of_week,
	ft.hour_of_day,
	ft.minutes_of_hour as minutes_of_hour,
	1 as num_flights, 
	0 as multi_carrier_flag,
	ft.carrier as first_operating_carrier,
	null as second_operating_carrier,
	ft.origin as origin,
	null as connection,
	ft.destination as destination,
	ft.planned_departure_time as planned_departure_time,
	ft.planned_departure_tz as planned_departure_tz,
	ft.planned_departure_local_hour as planned_departure_local_hour,
 
	ft.planned_arrival_time as planned_arrival_time,
	ft.planned_arrival_tz as planned_arrival_tz,
	ft.planned_arrival_local_hour as planned_arrival_local_hour,
	null as layover_duration,
	ft.id as first_flight_id,
	null as second_flight_id
from temp_itineraries ti
join flights ft 
on ft.id = ti.first_flight_id
where ti.num_flights = 1;
-- 4467804


-- Insert all one stop itineraries
insert into itineraries
(  year, quarter, month, day_of_month, day_of_week, hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag, first_operating_carrier, second_operating_carrier, 
  origin, connection, destination,
  planned_departure_time,
  planned_departure_tz,
  planned_departure_local_hour,

  planned_arrival_time,
  planned_arrival_tz,
  planned_arrival_local_hour,

  layover_duration, first_flight_id, second_flight_id
)
select 
	ft1.year as year,
	ft1.quarter as quarter,
	ft1.month as month,
	ft1.day_of_month as day_of_month,
	ft1.day_of_week as day_of_week,
	ft1.hour_of_day as hour_of_day,
	ft1.minutes_of_hour as minutes_of_hour,
	2 as num_flights, 

	case when ti.num_flights = 2 and ft1.carrier <> ft2.carrier
		then 1
		else 0
	end as multi_carrier_flag,

	ft1.carrier as first_operating_carrier,
	ft2.carrier as second_operating_carrier, 
	ft1.origin as origin,
	ft1.destination as connection,
	ft2.destination as destination, 

	ft1.planned_departure_time as planned_departure_time,
	ft1.planned_departure_tz as planned_departure_tz,
	ft1.planned_departure_local_hour as planned_departure_local_hour,

	ft2.planned_arrival_time as planned_arrival_time,
	ft2.planned_arrival_tz as planned_arrival_tz,
	ft2.planned_arrival_local_hour as planned_arrival_local_hour,

	TIMESTAMPDIFF(minute, ft2.planned_departure_time, ft1.planned_arrival_time) as layover_duration,
	ft1.id as first_flight_id, 
	ft2.id as second_flight_id
from temp_itineraries ti
join flights ft1 on ft1.id = ti.first_flight_id
join flights ft2 on ft2.id = ti.second_flight_id
where ti.num_flights = 2;
-- 161848110
*/

-- Status tracking table
drop table if exists itineraries_status;
create table itineraries_status (
	iteration_number int(11) auto_increment, primary key (iteration_number),
	start_time datetime
);

-- PROCEDURE
-- It is extremely recommended to use command line to run this query!
drop procedure if exists populate_itineraries;

-- SHOW PROCEDURE STATUS;

delimiter $$

create procedure populate_itineraries()
begin
    declare done int default 0;

    declare op_year numeric(4);
    declare op_quarter int;
    declare op_carrier char(6);
    
    -- 277 rows
    declare rdcursor cursor for     select rd.year, rd.quarter, rd.first_operating_carrier
                        from route_demands rd
                        group by rd.year, rd.quarter, rd.first_operating_carrier
                        order by rd.year, rd.quarter, rd.first_operating_carrier;

    declare continue handler for not found set done = 1;

    open rdcursor;

    cursor_loop:LOOP
        fetch rdcursor into op_year, op_quarter, op_carrier;
        if done then leave cursor_loop; end if;

        drop table if exists temp_iti_1;
        drop table if exists temp_iti_2;
        drop table if exists temp_iti_ft1ucr;
        
-- Log iteration start time
        insert into itineraries_status
        values (null, now());        
         
-- STEP 1
-- Create the non-stop itineraries
        create table temp_iti_1
        select 
                ft.year, ft.quarter, ft.month, ft.day_of_month, ft.day_of_week,
                ft.hour_of_day, ft.minutes_of_hour, 1, 0, 
                ft.carrier, ft.origin, ft.destination, 
                
                ft.planned_departure_time   /*as planned_departure_time*/,
                ft.planned_departure_tz         /*as planned_departure_tz*/,
                ft.planned_departure_local_hour /*as planned_departure_local_hour*/,                     
                ft.planned_arrival_time     /*as planned_arrival_time*/,
                ft.planned_arrival_tz           /*as planned_arrival_tz*/,
                ft.planned_arrival_local_hour   /*as planned_arrival_local_hour*/,
                
                ft.id
          
        from flights ft
        where ft.year = op_year and ft.quarter = op_quarter  and ft.carrier = op_carrier; 
            
        insert into itineraries (
                year, quarter, month, day_of_month, day_of_week,
                hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag,
                first_operating_carrier, origin, destination, 
                   
                planned_departure_time,
                planned_departure_tz,
                planned_departure_local_hour,
                
                planned_arrival_time,
                planned_arrival_tz,
                planned_arrival_local_hour,
                   
                first_flight_id)
        select 
                ti1.year, ti1.quarter, ti1.month, ti1.day_of_month, ti1.day_of_week,
                ti1.hour_of_day, ti1.minutes_of_hour, 1, 0, 
                ti1.carrier, ti1.origin, ti1.destination, 
                
                ti1.planned_departure_time,
                ti1.planned_departure_tz,
                ti1.planned_departure_local_hour,
                 
                ti1.planned_arrival_time,
                ti1.planned_arrival_tz,
                ti1.planned_arrival_local_hour,
                
                ti1.id as first_flight_id
        from temp_iti_1 ti1;
-- !STEP 1

-- STEP 2
-- Create the one stop itineraries
        create table temp_iti_ft1ucr
        select 
                ft1.id                           as ft1_id,
                ft1.year                         as ft1_year,
                ft1.quarter                      as ft1_quarter,
                ft1.month                        as ft1_month,
                ft1.day_of_month                 as ft1_day_of_month,
                ft1.day_of_week                  as ft1_day_of_week,
                ft1.hour_of_day                  as ft1_hour_of_day,
                ft1.minutes_of_hour              as ft1_minutes_of_hour,
                ft1.carrier                      as ft1_carrier,
                ft1.planned_departure_time   as ft1_planned_departure_time,
                ft1.planned_departure_tz         as ft1_planned_departure_tz,
                ft1.planned_departure_local_hour as ft1_planned_departure_local_hour,
                ft1.planned_arrival_time     as ft1_planned_arrival_time,
                ucr.second_operating_carrier     as ucr_second_operating_carrier,
                ucr.origin                       as ucr_origin,
                ucr.connection                   as ucr_connection,
                ucr.destination                  as ucr_destination
        from flights ft1
        join unique_carrier_routes ucr 
                on ucr.first_operating_carrier   = ft1.carrier 
                and ucr.origin                   = ft1.origin 
                and ucr.connection               = ft1.destination
        where ft1.year                           = op_year 
          and ft1.quarter                        = op_quarter 
          and ft1.carrier                        = op_carrier 
          and ucr.year                           = op_year 
          and ucr.first_operating_carrier        = op_carrier; 
                
        alter table temp_iti_ft1ucr
        add column ft1_planned_arrival_time_add30 datetime,
        add column ft1_planned_arrival_time_add300 datetime;
        
        update temp_iti_ft1ucr
        set ft1_planned_arrival_time_add30  = date_add(ft1_planned_arrival_time, interval 30 minute),
            ft1_planned_arrival_time_add300 = date_add(ft1_planned_arrival_time, interval 300 minute);
            
        /* create index idx_temp_iti
         on temp_iti_ft1ucr(
                ft1_carrier, 
                ft1_day_of_month,
                ft1_day_of_week,
                ft1_hour_of_day,
                ft1_id,
                ft1_month, 
                ft1_planned_arrival_time,
                ft1_planned_arrival_time_add30,
                ft1_planned_arrival_time_add300,
                ft1_planned_departure_time,
                ft1_planned_departure_tz,
                ft1_quarter,
                ucr_connection,
                ucr_destination,
                ucr_origin,
                ucr_second_operating_carrier); */

	create index idx_temp_iti
         on temp_iti_ft1ucr
		(
                ucr_second_operating_carrier,
		ucr_connection,
                ucr_destination,
                ft1_planned_arrival_time_add30,
                ft1_planned_arrival_time_add300
                );
                
        create table temp_iti_2
        select 
 year(ttt.ft1_planned_departure_time) as year, 
                -- null as year, 
                ttt.ft1_quarter, 
                ttt.ft1_month, 
                ttt.ft1_day_of_month, 
                ttt.ft1_day_of_week,
                ttt.ft1_hour_of_day, 
 minute(ttt.ft1_planned_departure_time) as minutes_of_hour, 
                -- null as minutes_of_hour, 
                2,
                case when (ft2.day_of_month - ttt.ft1_day_of_month) = 0 then 0 else 1 end as multi_carrier_flag,
                
                ttt.ft1_carrier, 
                ft2.carrier,
                ttt.ucr_origin, 
                ttt.ucr_connection, 
                ttt.ucr_destination,
                
                ttt.ft1_planned_departure_time as planned_departure_time,
                ttt.ft1_planned_departure_tz as planned_departure_tz,
 ttt.ft1_planned_departure_local_hour as planned_departure_local_hour,
                -- null as planned_departure_local_hour,
                
                ft2.planned_arrival_time as planned_arrival_time,
                ft2.planned_arrival_tz as planned_arrival_tz,
 ft2.planned_arrival_local_hour as planned_arrival_local_hour,
                -- null as planned_arrival_local_hour,
                
                TIMESTAMPDIFF(minute, ft2.planned_departure_time, ttt.ft1_planned_arrival_time) as layover_duration,
                
                ttt.ft1_id, 
                ft2.id as second_flight_id
        from flights ft2
        join temp_iti_ft1ucr ttt
                on ft2.carrier                     = ttt.ucr_second_operating_carrier 
                and ft2.origin                     = ttt.ucr_connection  
                and ft2.destination                = ttt.ucr_destination
                and ft2.planned_departure_time >= ttt.ft1_planned_arrival_time_add30
                and ft2.planned_departure_time <= ttt.ft1_planned_arrival_time_add300;          
                
        insert into itineraries (
                year, 
                quarter, 
                month, 
                day_of_month, 
                day_of_week,
                
                hour_of_day, 
                minutes_of_hour, 
                num_flights,
                multi_carrier_flag,
                
                first_operating_carrier, 
                second_operating_carrier,
                origin, 
                connection, 
                destination,
                
                planned_departure_time,
                planned_departure_tz,
                planned_departure_local_hour,
                
                planned_arrival_time,
                planned_arrival_tz,
                planned_arrival_local_hour,       
                
                layover_duration,
                first_flight_id, 
                second_flight_id)
        select 
                ft.year,
                ft.ft1_quarter,
                ft.ft1_month,
                ft.ft1_day_of_month,
                
                ft.ft1_day_of_week,
                ft.ft1_hour_of_day,
                ft.minutes_of_hour,
                2,
                ft.multi_carrier_flag,
                
                ft.ft1_carrier,
                ft.carrier,
                ft.ucr_origin,
                ft.ucr_connection,
                ft.ucr_destination,
                
                ft.planned_departure_time,
                ft.planned_departure_tz,
                ft.planned_departure_local_hour,
                
                ft.planned_arrival_time,
                ft.planned_arrival_tz,
                ft.planned_arrival_local_hour,
                
                ft.layover_duration,
                ft.ft1_id,
                ft.second_flight_id
        from temp_iti_2 ft;
-- !STEP 2
          
        drop table if exists temp_iti_1;
        drop table if exists temp_iti_2;
        drop table if exists temp_iti_ft1ucr;
  
    end loop;
    
    close rdcursor;
end$$

delimiter ;

call populate_itineraries();

drop procedure if exists populate_itineraries;
 
-- !PROCEDURE

-- General indices for querying itineraries
create index idx_itineraries_ft1ft2
  on itineraries(first_flight_id, second_flight_id);
  
create index idx_itineraries_fod 
  on itineraries(month, first_operating_carrier, origin, destination);  

create index idx_itineraries_c1c2
  on itineraries(first_operating_carrier, second_operating_carrier);

create index idx_itineraries_c1c2ym
  on itineraries(first_operating_carrier, second_operating_carrier, year, month);

-- The following index is used for passenger allocation
create index bmx_itineraries_c1ymmc
  on itineraries(first_operating_carrier, year, month, multi_carrier_flag);

-- The following index is used for passenger delay calculation
create index idx_itineraries_c1c2ymdm
  on itineraries(first_operating_carrier, second_operating_carrier, year, month, day_of_month);

create index bmx_itineraries_ymdm
  on itineraries(year, month, day_of_month);
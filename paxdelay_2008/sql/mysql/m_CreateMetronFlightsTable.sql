drop table if exists metron_flights;

create table metron_flights
(
  id integer not null auto_increment, primary key (id),
  metron_id numeric(12) not null,
  flight_id numeric(12),
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  day_of_week numeric(1) not null,
  hour_of_day numeric(2) not null,
  minutes_of_hour numeric(2) not null,
  carrier char(6) not null,
  tail_number char(10),
  origin char(3) not null,
  destination char(3) not null,
  
  planned_departure_time_UTC datetime,
  planned_departure_tz char(19),
  planned_departure_local_hour numeric(2),
  
  planned_wheels_off_UTC datetime,
  planned_wheels_off_tz char(19),
  planned_wheels_off_local_hour numeric(19),
  
  planned_wheels_on_UTC datetime,
  planned_wheels_on_tz char(19),
  planned_wheels_on_local_hour numeric(2),
  
  planned_arrival_time_UTC datetime,
  planned_arrival_tz char(19),
  planned_arrival_local_hour numeric(2),
  
  cancelled_flag numeric(1) not null,
  icao_aircraft_code char(4) not null,
  seating_capacity numeric(4)   
);

drop table if exists temp_t100_metron;
create table temp_t100_metron
(
  select distinct year, quarter, month, carrier, origin, destination
  from t100_segments
);

create index idx_temp_t100_m
on temp_t100_metron(carrier, origin, destination, year, quarter, month);

insert into metron_flights
(
	metron_id, 
	flight_id, 
	year, 
	quarter, 
	month, 
	day_of_month, 
	day_of_week, 
	hour_of_day, 
	minutes_of_hour, 
	carrier, 
	tail_number, 
	origin, 
	destination, 
		planned_departure_time_UTC, 	planned_departure_tz, 	planned_departure_local_hour, 
		planned_wheels_off_UTC, 	planned_wheels_off_tz, 	planned_wheels_off_local_hour, 
		planned_wheels_on_UTC, 		planned_wheels_on_tz, 	planned_wheels_on_local_hour,
		planned_arrival_time_UTC, 	planned_arrival_tz, 	planned_arrival_local_hour, 
	cancelled_flag, icao_aircraft_code, seating_capacity
)
select 
	tmf.metron_id, 
	tmf.database_id,
	
	year(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)),
	quarter(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)),
	month(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)),
	day(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)),
	dayofweek(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)),
	hour(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)),
	minute(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)),
	tmf.carrier, 
	tmf.tail_number,
	tmf.origin, 
	tmf.destination,
	
	tmf.departure_time_UTC as planned_departure_time_UTC,
	ori.timezone_region as planned_departure_tz,
	hour(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', ori.timezone_region)) as planned_departure_local_hour,
	
	tmf.wheels_off_time_UTC as planned_wheels_off_UTC,
	ori.timezone_region as planned_wheels_off_tz,
	hour(CONVERT_TZ(tmf.wheels_off_time_UTC, '+00:00', ori.timezone_region)) as planned_wheels_local_hour,
	
	tmf.wheels_on_time_UTC as planned_wheels_on_UTC,
	des.timezone_region as planned_wheels_on_tz,
	hour(CONVERT_TZ(tmf.wheels_on_time_UTC, '+00:00', des.timezone_region)) as planned_wheels_on_local_hour,

	tmf.arrival_time_UTC as planned_arrival_time_UTC,
	des.timezone_region as planned_arrival_tz,
	hour(CONVERT_TZ(tmf.arrival_time_UTC, '+00:00', des.timezone_region)) as planned_arrival_local_hour,

	0,
	tmf.aircraft_type,
	tmf.seating_capacity
from temp_metron_flights tmf
join airports ori 
	on ori.code = tmf.origin
join airports des 
	on des.code = tmf.destination
join temp_t100_metron t100 
	on t100.carrier = tmf.carrier 
	and t100.origin = tmf.origin 
	and t100.destination = tmf.destination 
	and t100.year = year(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)) 
	and t100.quarter = quarter(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz)) 
	and t100.month = month(CONVERT_TZ(tmf.departure_time_UTC, '+00:00', departure_tz))
	;
-- 16,100

drop table if exists temp_t100_metron;
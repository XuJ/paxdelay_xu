-- OPHASWONGSE
-- Modified to exclude the use of offerings table
-- XuJiao
-- That took 27 minutes
-- Records: 7,455,428 (MIT: 7,455,428)

drop table if exists temp_flights_aotp;

-- set sql_mode = '';

create table temp_flights_aotp
(
  id integer not null auto_increment, primary key (id)
)
select 
	ot.year as year, 
	ot.quarter as quarter, 
	ot.month as month, 
	ot.day_of_month as day_of_month,
	hour(STR_TO_DATE(ot.planned_departure_time, '%H%i')) as hour_of_day,
	minute(STR_TO_DATE(ot.planned_departure_time, '%H%i')) as minutes_of_hour,
	ot.carrier as carrier,
	ot.tail_number as tail_number,
	ot.flight_number as flight_number,
	ot.origin as origin,
	ot.destination as destination,

-- planned_departure_time

	convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), orig.timezone_region, 'UTC') as planned_departure_time_UTC,
	orig.timezone_region as planned_departure_tz,
	hour(STR_TO_DATE(ot.planned_departure_time, '%H%i')) as planned_departure_local_hour,

-- planned_arrival_time
	case when ot.planned_elapsed_time is null 
			then 	case when greatest(	convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC'), 
										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC')) = 
										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC') 
				then date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC'), interval 1 day)
				else convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC')
				end
			else convert_tz(date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region, dest.timezone_region), 
												interval ot.planned_elapsed_time minute), dest.timezone_region, 'UTC')
			end as planned_arrival_time_UTC,

	case when ot.planned_elapsed_time is null 
			then null
			else dest.timezone_region 
			end as planned_arrival_tz,

	case when ot.planned_elapsed_time is null 
			then 	case when greatest(	convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC'), 
										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC')) = 
										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC') 
				then hour(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')), interval 1 day))
				else hour(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')))
				end
			else hour(date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region, dest.timezone_region), 
												interval ot.planned_elapsed_time minute))
			end as planned_arrival_local_hour,

-- actual_departure_time
	case when ot.actual_departure_time is null
				then null
				else date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC'), interval ot.departure_offset minute)
				end as actual_departure_time_UTC,

	case when ot.actual_departure_time is null
				then null
				else orig.timezone_region
				end as actual_departure_tz,

	case when ot.actual_departure_time is null
				then null
				else hour(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset minute))
				end as actual_departure_local_hour,

-- actual_arrival_time
	case when ot.actual_arrival_time is null 
			then null
			else convert_tz(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.planned_elapsed_time + ot.arrival_offset minute),
												orig.timezone_region, dest.timezone_region),dest.timezone_region,'UTC')
			end as actual_arrival_time_UTC,

	case when ot.actual_arrival_time is null 
			then null
			else dest.timezone_region
			end as actual_arrival_tz ,

	case when ot.actual_arrival_time is null 
			then null
			else hour(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.planned_elapsed_time + ot.arrival_offset minute),
												orig.timezone_region, dest.timezone_region))
			end as actual_arrival_local_hour,

-- wheels_off_time
	case when ot.wheels_off_time is null
			then null
			else convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration minute),orig.timezone_region, 'UTC')
			end as wheels_off_time_UTC,

	case when ot.wheels_off_time is null
			then null
			else orig.timezone_region
			end as wheels_off_tz,

	case when ot.wheels_off_time is null
			then null
			else hour(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration minute))
			end as wheels_off_local_hour,


-- wheels_on_time 
	case when ot.wheels_on_time is null
			then null
			else convert_tz(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration + ot.in_air_duration minute ),orig.timezone_region, dest.timezone_region), dest.timezone_region, 'UTC')
			end as wheels_on_time_UTC,

	case when ot.wheels_on_time is null
			then null
			else dest.timezone_region
			end as wheels_on_tz,

	case when ot.wheels_on_time is null
			then null
			else hour(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration + ot.in_air_duration minute ),orig.timezone_region, dest.timezone_region))
			end as wheels_on_local_hour,

  ot.cancelled as cancelled_flag,
	ot.diverted as diverted_flag,
	ot.number_flights as num_flights,
	ot.flight_distance as flight_distance,
  ot.carrier_delay as carrier_delay ,
	ot.weather_delay as weather_delay,
	ot.nas_delay as nas_delay,
	ot.security_delay as security_delay,
  ot.late_aircraft_delay as late_aircraft_delay
from aotp ot
join airports orig on orig.code = ot.origin
join airports dest on dest.code = ot.destination;

delete 
from temp_flights_aotp 
using temp_flights_aotp
join (select max(id) as max_id, count(id)
				from temp_flights_aotp
				group by carrier, flight_number, origin, destination, planned_departure_time_UTC
				having count(id) > 1) t on id = t.max_id;



create index idx_temp_ftaotp_fncod
  on temp_flights_aotp(flight_number, carrier, origin, destination)
  using btree;
  
alter table temp_flights_aotp
add planned_departure_time_local datetime;
alter table temp_flights_aotp
add exponent_planned_departure_time_local integer;

update temp_flights_aotp 
set planned_departure_time_local = convert_tz(planned_departure_time_utc,'+00:00',planned_departure_tz);
update temp_flights_aotp 
set exponent_planned_departure_time_local = power(2, dayofweek(planned_departure_time_local)-1);

drop table if exists temp_flights_aotp_mini;
create table temp_flights_aotp_mini 
select id,
hour_of_day,
minutes_of_hour,
carrier,
tail_number,
flight_number,
origin,
destination,
planned_departure_time_utc,
planned_departure_time_local,
planned_departure_tz,
planned_departure_local_hour,
exponent_planned_departure_time_local
from temp_flights_aotp;

create index idx_temp_ft_mini
  on temp_flights_aotp_mini(id, carrier, flight_number, origin, destination, exponent_planned_departure_time_local,
  planned_departure_time_local, hour_of_day, minutes_of_hour);  
  
drop table if exists temp_flights_no_seats;

create table temp_flights_no_seats
select 
	tf.id as id,
	tf.year as year,
	tf.quarter as quarter,
	tf.month as month,
	tf.day_of_month as day_of_month,
  dayofweek(tf.planned_departure_time_UTC) as day_of_week, 
  tf.hour_of_day as hour_of_day,
	tf.minutes_of_hour as minutes_of_hour,
  tf.carrier as carrier,
	tf.tail_number as tail_number, 
  tf.flight_number as flight_number,
	tf.origin as origin,
	tf.destination as destination,
	tf.planned_departure_time_UTC as planned_departure_time_UTC,
	tf.planned_departure_tz as planned_departure_tz,
	tf.planned_departure_local_hour as planned_departure_local_hour,

  tf.planned_arrival_time_UTC as planned_arrival_time_UTC,
  tf.planned_arrival_tz as planned_arrival_tz,
  tf.planned_arrival_local_hour as planned_arrival_local_hour,

	tf.actual_departure_time_UTC as actual_departure_time_UTC,
	tf.actual_departure_tz as actual_departure_tz,
	tf.actual_departure_local_hour as actual_departure_local_hour,

	tf.actual_arrival_time_UTC as actual_arrival_time_UTC,
	tf.actual_arrival_tz as actual_arrival_tz,
	tf.actual_arrival_local_hour as actual_arrival_local_hour,

  tf.wheels_off_time_UTC as wheels_off_time_UTC,
  tf.wheels_off_tz as wheels_off_tz,
  tf.wheels_off_local_hour as wheels_off_local_hour,

	tf.wheels_on_time_UTC as wheels_on_time_UTC,
	tf.wheels_on_tz as wheels_on_tz,
	tf.wheels_on_local_hour as wheels_on_local_hour,

	tf.cancelled_flag as cancelled_flag,
  tf.diverted_flag as diverted_flag,
	tf.num_flights as num_flights,
	tf.flight_distance as flight_distance,
	tf.carrier_delay as carrier_delay,
  tf.weather_delay as weather_delay,
	tf.nas_delay as nas_delay,
	tf.security_delay as security_delay,
	tf.late_aircraft_delay as late_aircraft_delay
from temp_flights_aotp tf;

drop table if exists temp_t100_flight_seats;

create table temp_t100_flight_seats
select 
	fto.id as flight_id,
	t100s.seats_mean as seats,
	t100s.seats_coeff_var as coeff_var
from t100_seats t100s
join temp_flights_no_seats fto
  on year(CONVERT_TZ(fto.planned_departure_time_UTC,'+00:00',fto.planned_departure_tz)) = t100s.year
  and month(CONVERT_TZ(fto.planned_departure_time_UTC,'+00:00',fto.planned_departure_tz)) = t100s.month
  and fto.carrier = t100s.carrier
  and fto.origin = t100s.origin
  and fto.destination = t100s.destination;

create unique index idx_temp_t100s_fid
  on temp_t100_flight_seats(flight_id)
  using btree;

-- create table temp_flight_seats
drop table if exists temp_flight_seats;

create table temp_flight_seats
select 
	t100fs.flight_id as flight_id,
	t100fs.seats as seats
from temp_t100_flight_seats t100fs
where t100fs.coeff_var <= 0.025;

create unique index idx_temp_fs_fid
  on temp_flight_seats(flight_id)
  using btree;
  
drop table if exists flights_no_seats;

create table flights_no_seats
(
  id integer not null auto_increment, primary key (id),
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  day_of_week numeric(1) not null,
  hour_of_day numeric(2) not null,
  minutes_of_hour numeric(2) not null,
  carrier char(6) not null,
  tail_number varchar(10),
  flight_number char(4) not null,
  origin char(3) not null,
  destination char(3) not null,

  planned_departure_time_UTC datetime not null,
  planned_departure_tz char(19),
  planned_departure_local_hour numeric(2),

  planned_arrival_time_UTC datetime not null,
  planned_arrival_tz char(19),
  planned_arrival_local_hour numeric(2),

  actual_departure_time_UTC datetime,
  actual_departure_tz char(19),
  actual_departure_local_hour numeric(2),

  actual_arrival_time_UTC datetime,
  actual_arrival_tz char(19),
  actual_arrival_local_hour numeric(2),

  wheels_off_time_UTC datetime,
  wheels_off_tz char(19),
  wheels_off_time_local_hour numeric(2),

  wheels_on_time_UTC datetime,
  wheels_on_tz char(19),
  wheels_on_time_local_hour numeric(2),

  cancelled_flag numeric(1) not null,
  diverted_flag numeric(1) not null,
  num_flights numeric(1) not null,
  flight_distance numeric(5) not null,
  carrier_delay numeric(4) not null,
  weather_delay numeric(4) not null,
  nas_delay numeric(4) not null,
  security_delay numeric(4) not null,
  late_aircraft_delay numeric(4) not null,
  seating_capacity numeric(4)
);

insert into flights_no_seats
select tf.id, tf.year, tf.quarter, tf.month, tf.day_of_month, tf.day_of_week,
  tf.hour_of_day, tf.minutes_of_hour, tf.carrier, tf.tail_number,
  tf.flight_number,tf.origin, tf.destination,

  tf.planned_departure_time_UTC as planned_departure_time_UTC, 
  tf.planned_departure_tz as planned_departure_tz, 
  tf.planned_departure_local_hour as planned_departure_local_hour, 

	tf.planned_arrival_time_UTC as planned_arrival_time_UTC,  
	tf.planned_arrival_tz as planned_arrival_tz,  
	tf.planned_arrival_local_hour as planned_arrival_time_local_hour,  

	tf.actual_departure_time_UTC as actual_departure_time_UTC,
	tf.actual_departure_tz as actual_departure_tz,
	tf.actual_departure_local_hour as actual_departure_local_hour,

  tf.actual_arrival_time_UTC as actual_arrival_time_UTC, 
  tf.actual_arrival_tz as actual_arrival_tz, 
  tf.actual_arrival_local_hour as actual_arrival_local_hour, 

	tf.wheels_off_time_UTC as wheels_off_time_UTC, 
	tf.wheels_off_tz as wheels_off_tz, 
	tf.wheels_off_local_hour as wheels_off_local_hour, 

	tf.wheels_on_time_UTC as wheels_on_time_UTC, 
	tf.wheels_on_tz as wheels_on_tz, 
	tf.wheels_on_local_hour as wheels_on_local_hour, 

	tf.cancelled_flag,
  tf.diverted_flag, tf.num_flights, tf.flight_distance, tf.carrier_delay,
  tf.weather_delay, tf.nas_delay, tf.security_delay, tf.late_aircraft_delay,tfs.seats
from temp_flights_no_seats tf
left join temp_flight_seats tfs 
on tfs.flight_id = tf.id;

-- General index for searching for flights
create index idx_flights_cymdmodh
on flights_no_seats(carrier, year, month, day_of_month, origin, destination, hour_of_day);

create index idx_flights_icfodepphm
  on flights_no_seats(id, carrier, flight_number, origin, destination, exponent_planned_departure_time_local,
  planned_departure_time_local, hour_of_day, minutes_of_hour);

-- The following index is used to support PAP
create index idx_flights_cymdm
on flights_no_seats(carrier, year, month, day_of_month);

-- The following two indices are used to support itinerary generation
create index idx_flights_c
on flights_no_seats(carrier);

create index idx_flights_ict
on flights_no_seats(id, carrier, tail_number);

create index idx_flights_coddt
on flights_no_seats(carrier, origin, destination, planned_departure_time_UTC);

create index idx_flights_ymdm
on flights_no_seats(year, month, day_of_month);

-- The following indices are used to support airline validation
create index idx_flights_cyq
on flights_no_seats(carrier, year, quarter);

create index idx_flights_fncyqod
on flights_no_seats(flight_number, carrier, year, quarter, origin, destination);

create index idx_flights_ymdmodh
on flights_no_seats(year, month, day_of_month, origin, destination, hour_of_day);

-- create unique index idx_flights_yqmdmfncod
--  on flights_no_seats(year, quarter, month, day_of_month, flight_number, carrier, origin, destination);

create index idx_flights_fncoddt
on flights_no_seats(flight_number, carrier, origin, destination, planned_departure_time_UTC);

update flights_no_seats
set seating_capacity = null;


-- Drop the temporary tables
drop table if exists temp_flights_aotp;
drop table if exists temp_flights_aotp_mini;
drop table if exists temp_flight_offerings;
drop table if exists temp_flights_no_seats;
drop table if exists temp_iata_flight_seats;
drop table if exists temp_t100_flight_seats;
drop table if exists temp_flight_seats;
-- -- Statements from file CreateFlightsTable.sql 
-- drop table if exists temp_flights_aotp;

-- create table temp_flights_aotp
-- (
--   id integer not null auto_increment, primary key (id)
-- )
-- select 
-- 	ot.year as year, 
-- 	ot.quarter as quarter, 
-- 	ot.month as month, 
-- 	ot.day_of_month as day_of_month,
-- 	hour(STR_TO_DATE(ot.planned_departure_time, '%H%i')) as hour_of_day,
-- 	minute(STR_TO_DATE(ot.planned_departure_time, '%H%i')) as minutes_of_hour,
-- 	ot.carrier as carrier,
-- 	ot.tail_number as tail_number,
-- 	ot.flight_number as flight_number,
-- 	ot.origin as origin,
-- 	ot.destination as destination,

-- -- planned_departure_time

-- 	convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), orig.timezone_region, 'UTC') as planned_departure_time_UTC,
-- 	orig.timezone_region as planned_departure_tz,
-- 	hour(STR_TO_DATE(ot.planned_departure_time, '%H%i')) as planned_departure_local_hour,

-- -- planned_arrival_time
-- 	case when ot.planned_elapsed_time is null 
-- 			then 	case when greatest(	convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC'), 
-- 										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC')) = 
-- 										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC') 
-- 				then date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC'), interval 1 day)
-- 				else convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC')
-- 				end
-- 			else convert_tz(date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region, dest.timezone_region), 
-- 												interval ot.planned_elapsed_time minute), dest.timezone_region, 'UTC')
-- 			end as planned_arrival_time_UTC,

-- 	case when ot.planned_elapsed_time is null 
-- 			then null
-- 			else dest.timezone_region 
-- 			end as planned_arrival_tz,

-- 	case when ot.planned_elapsed_time is null 
-- 			then 	case when greatest(	convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC'), 
-- 										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')),dest.timezone_region,'UTC')) = 
-- 										convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC') 
-- 				then hour(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')), interval 1 day))
-- 				else hour(addtime(ot.flight_date, STR_TO_DATE(ot.planned_arrival_time, '%H%i')))
-- 				end
-- 			else hour(date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region, dest.timezone_region), 
-- 												interval ot.planned_elapsed_time minute))
-- 			end as planned_arrival_local_hour,

-- -- actual_departure_time
-- 	case when ot.actual_departure_time is null
-- 				then null
-- 				else date_add(convert_tz(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')),orig.timezone_region,'UTC'), interval ot.departure_offset minute)
-- 				end as actual_departure_time_UTC,

-- 	case when ot.actual_departure_time is null
-- 				then null
-- 				else orig.timezone_region
-- 				end as actual_departure_tz,

-- 	case when ot.actual_departure_time is null
-- 				then null
-- 				else hour(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset minute))
-- 				end as actual_departure_local_hour,

-- -- actual_arrival_time
-- 	case when ot.actual_arrival_time is null 
-- 			then null
-- 			else convert_tz(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.planned_elapsed_time + ot.arrival_offset minute),
-- 												orig.timezone_region, dest.timezone_region),dest.timezone_region,'UTC')
-- 			end as actual_arrival_time_UTC,

-- 	case when ot.actual_arrival_time is null 
-- 			then null
-- 			else dest.timezone_region
-- 			end as actual_arrival_tz ,

-- 	case when ot.actual_arrival_time is null 
-- 			then null
-- 			else hour(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.planned_elapsed_time + ot.arrival_offset minute),
-- 												orig.timezone_region, dest.timezone_region))
-- 			end as actual_arrival_local_hour,

-- -- wheels_off_time
-- 	case when ot.wheels_off_time is null
-- 			then null
-- 			else convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration minute),orig.timezone_region, 'UTC')
-- 			end as wheels_off_time_UTC,

-- 	case when ot.wheels_off_time is null
-- 			then null
-- 			else orig.timezone_region
-- 			end as wheels_off_tz,

-- 	case when ot.wheels_off_time is null
-- 			then null
-- 			else hour(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration minute))
-- 			end as wheels_off_local_hour,


-- -- wheels_on_time 
-- 	case when ot.wheels_on_time is null
-- 			then null
-- 			else convert_tz(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration + ot.in_air_duration minute ),orig.timezone_region, dest.timezone_region), dest.timezone_region, 'UTC')
-- 			end as wheels_on_time_UTC,

-- 	case when ot.wheels_on_time is null
-- 			then null
-- 			else dest.timezone_region
-- 			end as wheels_on_tz,

-- 	case when ot.wheels_on_time is null
-- 			then null
-- 			else hour(convert_tz(date_add(addtime(ot.flight_date, STR_TO_DATE(ot.planned_departure_time, '%H%i')), interval ot.departure_offset + ot.taxi_out_duration + ot.in_air_duration minute ),orig.timezone_region, dest.timezone_region))
-- 			end as wheels_on_local_hour,

--   ot.cancelled as cancelled_flag,
-- 	ot.diverted as diverted_flag,
-- 	ot.number_flights as num_flights,
-- 	ot.flight_distance as flight_distance,
--   ot.carrier_delay as carrier_delay ,
-- 	ot.weather_delay as weather_delay,
-- 	ot.nas_delay as nas_delay,
-- 	ot.security_delay as security_delay,
--   ot.late_aircraft_delay as late_aircraft_delay
-- from aotp ot
-- join airports orig on orig.code = ot.origin
-- join airports dest on dest.code = ot.destination;

-- -- There are 30 duplicate flights that need to be cleaned up
-- -- We will just delete the one with the higher flight ID
-- delete 
-- from temp_flights_aotp 
-- using temp_flights_aotp
-- join (select max(id) as max_id, count(id)
-- 				from temp_flights_aotp
-- 				group by carrier, flight_number, origin, destination, planned_departure_time_UTC
-- 				having count(id) > 1) t on id = t.max_id;



-- create index idx_temp_ftaotp_fncod
--   on temp_flights_aotp(flight_number, carrier, origin, destination)
--   using btree;


-- -- create table temp_flight_offerings
-- -- 1a. optimizing performance offerings
-- alter table offerings
-- add flag_multiplication integer;

-- update offerings set flag_multiplication = 
--   (sunday_flag + 
--   2 * monday_flag + 
--   4 * tuesday_flag + 
--   8 * wednesday_flag + 
--   16 * thursday_flag + 
--   32 * friday_flag + 
--   64 * saturday_flag)
-- ;

-- -- 1b. optimizing performance temp_flights_aotp
-- alter table temp_flights_aotp
-- add planned_departure_time_local datetime;
-- alter table temp_flights_aotp
-- add exponent_planned_departure_time_local integer;

-- update temp_flights_aotp 
-- set planned_departure_time_local = convert_tz(planned_departure_time_utc,'+00:00',planned_departure_tz);
-- update temp_flights_aotp 
-- set exponent_planned_departure_time_local = power(2, dayofweek(planned_departure_time_local)-1);

-- -- 2a. minimizing tables for better performance
-- drop table offerings_mini;
-- create table offerings_mini 
-- select id,
-- carrier,
-- flight_number,
-- origin,
-- destination,
-- effective_start,
-- effective_start_tz,
-- effective_end,
-- effective_end_tz,
-- published_departure_hour,
-- published_departure_minutes,
-- flag_multiplication
-- from offerings;

-- -- 2b. minimizing tables for better performance
-- drop table temp_flights_aotp_mini;
-- create table temp_flights_aotp_mini 
-- select id,
-- hour_of_day,
-- minutes_of_hour,
-- carrier,
-- tail_number,
-- flight_number,
-- origin,
-- destination,
-- planned_departure_time_utc,
-- planned_departure_time_local,
-- planned_departure_tz,
-- planned_departure_local_hour,
-- exponent_planned_departure_time_local
-- from temp_flights_aotp;

-- -- 3a creating indexes
-- create index idx_temp_off_mini
--   on offerings_mini(carrier, flight_number, origin, destination);
  
-- -- 3b creating indexes
-- create index idx_temp_ft_mini
--   on temp_flights_aotp_mini(id, carrier, flight_number, origin, destination, exponent_planned_departure_time_local,
--   planned_departure_time_local, hour_of_day, minutes_of_hour);  
  
-- -- creation
-- drop table temp_flight_offerings;
-- create table temp_flight_offerings
-- select 
-- 	ft.id as flight_id, 
-- 	min(off.id) as offering_id
-- from temp_flights_aotp_mini ft
-- join offerings_mini off
--   on 
--   ft.carrier = off.carrier
--   and ft.flight_number = off.flight_number
--   and ft.origin = off.origin
--   and ft.destination = off.destination
--   and off.flag_multiplication & ft.exponent_planned_departure_time_local != 0
--   and date(ft.planned_departure_time_local) >= date(off.effective_start)
--   and date(ft.planned_departure_time_local) <= date(off.effective_end)
--   and ft.hour_of_day = off.published_departure_hour
--   and ft.minutes_of_hour = off.published_departure_minutes
-- group by ft.id;
-- -- 1590566

-- create unique index idx_temp_tfo_fid
--   on temp_flight_offerings(flight_id)
--   using btree;
-- -- !create table temp_flight_offerings

-- -- insertion 1
-- create table temp_flight_offerings_1a
-- select 
--   ft.id as flight_id, 
--   min(off.id) as offering_id
-- from temp_flights_aotp_mini ft
-- join offerings_mini off
--   on 
--   ft.carrier = off.carrier
--   and ft.flight_number = off.flight_number
--   and ft.origin = off.origin
--   and ft.destination = off.destination
--   and off.flag_multiplication & ft.exponent_planned_departure_time_local != 0
--   and date(ft.planned_departure_time_local) >= date(off.effective_start)
--   and date(ft.planned_departure_time_local) <= date(off.effective_end)
-- group by ft.id;

-- create table temp_flight_offerings_1
-- select 
--   ft.flight_id, 
--   ft.offering_id
-- from temp_flight_offerings_1a ft  
-- left join temp_flight_offerings  tfo  on tfo.flight_id = ft.flight_id
-- where tfo.flight_id is null;

-- insert into temp_flight_offerings
-- select
--   ft.flight_id, 
--   ft.offering_id
-- from temp_flight_offerings_1 ft;

-- drop table temp_flight_offerings_1a;
-- drop table temp_flight_offerings_1;
-- -- 3074285
-- -- !insertion 1

-- -- insertion 2
-- create table temp_flight_offerings_2a
-- select 
--   ft.id as flight_id, 
--   min(off.id) as offering_id
-- from temp_flights_aotp_mini ft
-- join offerings_mini off
--   on 
--   ft.carrier = off.carrier
--   and ft.flight_number = off.flight_number
--   and ft.origin = off.origin
--   and ft.destination = off.destination
--   and off.flag_multiplication & ft.exponent_planned_departure_time_local != 0
-- group by ft.id;

-- create table temp_flight_offerings_2
-- select 
--   ft.flight_id, 
--   ft.offering_id
-- from temp_flight_offerings_2a ft  
-- left join temp_flight_offerings  tfo  on tfo.flight_id = ft.flight_id
-- where tfo.flight_id is null;

-- insert into temp_flight_offerings
-- select
--   ft.flight_id, 
--   ft.offering_id
-- from temp_flight_offerings_2 ft;

-- drop table temp_flight_offerings_2a;
-- drop table temp_flight_offerings_2;
-- -- 3302655
-- -- !insertion 2

-- -- create table temp_flights_no_seats
-- drop table if exists temp_flights_no_seats;

-- create table temp_flights_no_seats
-- select 
-- 	tf.id as id,
-- 	tf.year as year,
-- 	tf.quarter as quarter,
-- 	tf.month as month,
-- 	tf.day_of_month as day_of_month,
--   day(tf.planned_departure_time_UTC) as day_of_week, 
--   tf.hour_of_day as hour_of_day,
-- 	tf.minutes_of_hour as minutes_of_hour,
--   tf.carrier as carrier,
-- 	tf.tail_number as tail_number, 
--   tf.flight_number as flight_number,
-- 	tf.origin as origin,
-- 	tf.destination as destination,
-- 	tf.planned_departure_time_UTC as planned_departure_time_UTC,
-- 	tf.planned_departure_tz as planned_departure_tz,
-- 	tf.planned_departure_local_hour as planned_departure_local_hour,

--   tf.planned_arrival_time_UTC as planned_arrival_time_UTC,
--   tf.planned_arrival_tz as planned_arrival_tz,
--   tf.planned_arrival_local_hour as planned_arrival_local_hour,

-- 	tf.actual_departure_time_UTC as actual_departure_time_UTC,
-- 	tf.actual_departure_tz as actual_departure_tz,
-- 	tf.actual_departure_local_hour as actual_departure_local_hour,

-- 	tf.actual_arrival_time_UTC as actual_arrival_time_UTC,
-- 	tf.actual_arrival_tz as actual_arrival_tz,
-- 	tf.actual_arrival_local_hour as actual_arrival_local_hour,

--   tf.wheels_off_time_UTC as wheels_off_time_UTC,
--   tf.wheels_off_tz as wheels_off_tz,
--   tf.wheels_off_local_hour as wheels_off_local_hour,

-- 	tf.wheels_on_time_UTC as wheels_on_time_UTC,
-- 	tf.wheels_on_tz as wheels_on_tz,
-- 	tf.wheels_on_local_hour as wheels_on_local_hour,

-- 	tf.cancelled_flag as cancelled_flag,
--   tf.diverted_flag as diverted_flag,
-- 	tf.num_flights as num_flights,
-- 	tf.flight_distance as flight_distance,
-- 	tf.carrier_delay as carrier_delay,
--   tf.weather_delay as weather_delay,
-- 	tf.nas_delay as nas_delay,
-- 	tf.security_delay as security_delay,
-- 	tf.late_aircraft_delay as late_aircraft_delay,
--   off.id as offering_id,
-- 	off.iata_aircraft_code as iata_aircraft_code
-- from temp_flights_aotp tf
-- left join temp_flight_offerings tfo on tfo.flight_id = tf.id
-- left join offerings off on off.id = tfo.offering_id;



-- -- create table temp_iata_flight_seats
-- drop table if exists temp_iata_flight_seats;

-- create table temp_iata_flight_seats
-- select 
-- 	fto.id as flight_id,
-- 	iata.seats as seats
-- from carrier_iata_seats iata
-- join temp_flights_no_seats fto on fto.carrier = iata.carrier and fto.iata_aircraft_code = iata.aircraft_code;


-- create unique index idx_temp_iata_fid
--   on temp_iata_flight_seats(flight_id)
--   using btree;

-- -- create table temp_t100_flight_seats
-- drop table if exists temp_t100_flight_seats;

-- create table temp_t100_flight_seats
-- select 
-- 	fto.id as flight_id,
-- 	t100s.seats_mean as seats,
-- 	t100s.seats_coeff_var as coeff_var
-- from t100_seats t100s
-- join temp_flights_no_seats fto
--   on year(CONVERT_TZ(fto.planned_departure_time_UTC,'+00:00',fto.planned_departure_tz)) = t100s.year
--   and month(CONVERT_TZ(fto.planned_departure_time_UTC,'+00:00',fto.planned_departure_tz)) = t100s.month
--   and fto.carrier = t100s.carrier
--   and fto.origin = t100s.origin
--   and fto.destination = t100s.destination;

-- create unique index idx_temp_t100s_fid
--   on temp_t100_flight_seats(flight_id)
--   using btree;

-- -- create table temp_flight_seats
-- drop table if exists temp_flight_seats;

-- create table temp_flight_seats
-- select 
-- 	t100fs.flight_id as flight_id,
-- 	t100fs.seats as seats
-- from temp_t100_flight_seats t100fs
-- where t100fs.coeff_var <= 0.025;

-- create unique index idx_temp_fs_fid
--   on temp_flight_seats(flight_id)
--   using btree;
  
-- insert into temp_flight_seats
-- select iata.flight_id, iata.seats
-- from temp_iata_flight_seats iata
-- left join temp_flight_seats tfs
--   on tfs.flight_id = iata.flight_id
-- where tfs.flight_id is null;

-- -- create table flights_no_seats
-- drop table if exists flights_no_seats;

-- create table flights_no_seats
-- (
--   id integer not null auto_increment, primary key (id),
--   year numeric(4) not null,
--   quarter int not null,
--   month numeric(2) not null,
--   day_of_month numeric(2) not null,
--   day_of_week numeric(1) not null,
--   hour_of_day numeric(2) not null,
--   minutes_of_hour numeric(2) not null,
--   carrier char(6) not null,
--   tail_number varchar(10),
--   flight_number char(4) not null,
--   origin char(3) not null,
--   destination char(3) not null,

--   planned_departure_time_UTC datetime not null,
--   planned_departure_tz char(19),
--   planned_departure_local_hour numeric(2),

--   planned_arrival_time_UTC datetime not null,
--   planned_arrival_tz char(19),
--   planned_arrival_local_hour numeric(2),

--   actual_departure_time_UTC datetime,
--   actual_departure_tz char(19),
--   actual_departure_local_hour numeric(2),

--   actual_arrival_time_UTC datetime,
--   actual_arrival_tz char(19),
--   actual_arrival_local_hour numeric(2),

--   wheels_off_time_UTC datetime,
--   wheels_off_tz char(19),
--   wheels_off_time_local_hour numeric(2),

--   wheels_on_time_UTC datetime,
--   wheels_on_tz char(19),
--   wheels_on_time_local_hour numeric(2),

--   cancelled_flag numeric(1) not null,
--   diverted_flag numeric(1) not null,
--   num_flights numeric(1) not null,
--   flight_distance numeric(5) not null,
--   carrier_delay numeric(4) not null,
--   weather_delay numeric(4) not null,
--   nas_delay numeric(4) not null,
--   security_delay numeric(4) not null,
--   late_aircraft_delay numeric(4) not null,
--   iata_aircraft_code char(4),
--   seating_capacity numeric(4)
-- );

-- insert into flights_no_seats
-- select tf.id, tf.year, tf.quarter, tf.month, tf.day_of_month, tf.day_of_week,
--   tf.hour_of_day, tf.minutes_of_hour, tf.carrier, tf.tail_number,
--   tf.flight_number,tf.origin, tf.destination,

--   tf.planned_departure_time_UTC as planned_departure_time_UTC, 
--   tf.planned_departure_tz as planned_departure_tz, 
--   tf.planned_departure_local_hour as planned_departure_local_hour, 

-- 	tf.planned_arrival_time_UTC as planned_arrival_time_UTC,  
-- 	tf.planned_arrival_tz as planned_arrival_tz,  
-- 	tf.planned_arrival_local_hour as planned_arrival_time_local_hour,  

-- 	tf.actual_departure_time_UTC as actual_departure_time_UTC,
-- 	tf.actual_departure_tz as actual_departure_tz,
-- 	tf.actual_departure_local_hour as actual_departure_local_hour,

--   tf.actual_arrival_time_UTC as actual_arrival_time_UTC, 
--   tf.actual_arrival_tz as actual_arrival_tz, 
--   tf.actual_arrival_local_hour as actual_arrival_local_hour, 

-- 	tf.wheels_off_time_UTC as wheels_off_time_UTC, 
-- 	tf.wheels_off_tz as wheels_off_tz, 
-- 	tf.wheels_off_local_hour as wheels_off_local_hour, 

-- 	tf.wheels_on_time_UTC as wheels_on_time_UTC, 
-- 	tf.wheels_on_tz as wheels_on_tz, 
-- 	tf.wheels_on_local_hour as wheels_on_local_hour, 

-- 	tf.cancelled_flag,
--   tf.diverted_flag, tf.num_flights, tf.flight_distance, tf.carrier_delay,
--   tf.weather_delay, tf.nas_delay, tf.security_delay, tf.late_aircraft_delay,
--   tf.iata_aircraft_code, tfs.seats
-- from temp_flights_no_seats tf
-- left join temp_flight_seats tfs 
-- on tfs.flight_id = tf.id;

-- -- General index for searching for flights
-- create index idx_flights_cymdmodh
-- on flights_no_seats(carrier, year, month, day_of_month, origin, destination, hour_of_day);

-- -- The following index is used to support PAP
-- create index idx_flights_cymdm
-- on flights_no_seats(carrier, year, month, day_of_month);

-- -- The following two indices are used to support itinerary generation
-- create index idx_flights_c
-- on flights_no_seats(carrier);

-- create index idx_flights_ict
-- on flights_no_seats(id, carrier, tail_number);

-- create index idx_flights_coddt
-- on flights_no_seats(carrier, origin, destination, planned_departure_time_UTC);

-- create index idx_flights_ymdm
-- on flights_no_seats(year, month, day_of_month);

-- -- The following indices are used to support airline validation
-- create index idx_flights_cyq
-- on flights_no_seats(carrier, year, quarter);

-- create index idx_flights_fncyqod
-- on flights_no_seats(flight_number, carrier, year, quarter, origin, destination);

-- create index idx_flights_ymdmodh
-- on flights_no_seats(year, month, day_of_month, origin, destination, hour_of_day);

-- create unique index idx_flights_yqmdmfncod
--  on flights_no_seats(year, quarter, month, day_of_month, flight_number, carrier, origin, destination);

-- create index idx_flights_fncoddt
-- on flights_no_seats(flight_number, carrier, origin, destination, planned_departure_time_UTC);

-- update flights_no_seats
-- set iata_aircraft_code = null,
--   seating_capacity = null;

-- --! create table flights_no_seats

-- -- Drop the temporary tables
-- drop table if exists temp_flights_aotp;
-- drop table if exists temp_flights_aotp_mini;
-- drop table if exists temp_flight_offerings;
-- drop table if exists temp_flights_no_seats;
-- drop table if exists temp_iata_flight_seats;
-- drop table if exists temp_t100_flight_seats;
-- drop table if exists temp_flight_seats;

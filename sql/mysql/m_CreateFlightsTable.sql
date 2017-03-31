-- OPHASWONGSE
-- Modified to use only needed information
-- XuJiao
-- That took 117 minutes
-- Records: 7,455,428  (MIT: 7,455,428)
-- Drop two other temp tables: temp_t100_flights_seats, temp_flight_seats
-- Move indexing to other files

alter table flights_no_seats convert to character set latin1 collate latin1_general_cs;
alter table airline_inventories convert to character set latin1 collate latin1_general_cs;

/*create index idx_ai_ct
 on airline_inventories(carrier, tail_number);*/

alter table flights_no_seats add planned_departure_time_local 	datetime;
alter table flights_no_seats add year_local 					integer;
alter table flights_no_seats add month_local 					integer;
alter table flights_no_seats add dayofmonth_local 				integer;
alter table flights_no_seats add h24_local 						integer;
alter table flights_no_seats add mi_local 						integer;

update flights_no_seats set planned_departure_time_local 	= CONVERT_TZ(planned_departure_time_UTC,'+00:00',planned_departure_tz);
update flights_no_seats set year_local 						= year(planned_departure_time_local);
update flights_no_seats set month_local 					= month(planned_departure_time_local);
update flights_no_seats set dayofmonth_local 				= dayofmonth(planned_departure_time_local);
update flights_no_seats set h24_local 						= hour(planned_departure_time_local);
update flights_no_seats set mi_local 						= minute(planned_departure_time_local);

alter table flights_no_seats
add exponent_planned_departure_time_local integer;

update flights_no_seats 
set exponent_planned_departure_time_local = power(2, dayofweek(planned_departure_time_local)-1);

/*create index idx_flights_icfodepphm
  on flights_no_seats(id, carrier, flight_number, origin, destination, exponent_planned_departure_time_local,
  planned_departure_time_local, hour_of_day, minutes_of_hour);*/

-- alter table t100_seats convert to character set latin1 collate latin1_general_cs;
-- 
-- drop table if exists temp_t100_flight_seats;
-- create table temp_t100_flight_seats
-- select 
-- fto.id as flight_id, t100s.seats_mean as seats, t100s.seats_coeff_var as coeff_var
-- from t100_seats t100s
-- join flights_no_seats fto
--   on fto.year_local = t100s.year
--   and fto.month_local = t100s.month
--   and fto.carrier = t100s.carrier
--   and fto.origin = t100s.origin
--   and fto.destination = t100s.destination; 
-- -- 7455217
-- 
-- 
-- drop table if exists temp_flight_seats;
-- create table temp_flight_seats
-- select distinct fns.id as flight_id,
--   ai.number_of_seats
-- from flights_no_seats fns
-- join airline_inventories ai
--   on ai.carrier = fns.carrier
--   and ai.tail_number = fns.tail_number
-- where number_of_seats is not null;

alter table t100_seats convert to character set latin1 collate latin1_general_cs;

drop table if exists temp_t100_flight_seats;
create table temp_t100_flight_seats
select 
fto.id as flight_id, t100s.seats_mean as seats, t100s.seats_coeff_var as coeff_var
from t100_seats t100s
join flights_no_seats fto
  on fto.year_local = t100s.year
  and fto.month_local = t100s.month
  and fto.carrier = t100s.carrier
  and fto.origin = t100s.origin
  and fto.destination = t100s.destination; 


drop table if exists temp_flight_seats;
create table temp_flight_seats
select distinct fns.id as flight_id,
  ai.number_of_seats
from flights_no_seats fns
join airline_inventories ai
  on ai.carrier = fns.carrier
  and ai.tail_number = fns.tail_number
where number_of_seats is not null;


create index idx_temp_t100_isc
  on temp_t100_flight_seats(flight_id, seats, coeff_var);
  
create index idx_temp_fs_i
  on temp_flight_seats(flight_id);

insert into temp_flight_seats
select t100fs.flight_id, t100fs.seats
from temp_t100_flight_seats t100fs
left join temp_flight_seats tfs on tfs.flight_id = t100fs.flight_id
where t100fs.coeff_var <= 0.02 and tfs.flight_id is null;

drop table if exists flights;
create table flights
(
  id integer not null,
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

  planned_departure_time datetime not null,
  planned_departure_tz char(19),
  planned_departure_local_hour numeric(2),

  planned_arrival_time datetime not null,
  planned_arrival_tz char(19),
  planned_arrival_local_hour numeric(2),

  actual_departure_time datetime,
  actual_departure_tz char(19),
  actual_departure_local_hour numeric(2),

  actual_arrival_time datetime,
  actual_arrival_tz char(19),
  actual_arrival_local_hour numeric(2),

  wheels_off_time datetime,
  wheels_off_tz char(19),
  wheels_off_time_local_hour numeric(2),

  wheels_on_time datetime,
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

  seating_capacity numeric(3)
);

insert into flights
select tf.id, tf.year, tf.quarter, tf.month, tf.day_of_month, tf.day_of_week,
  tf.hour_of_day, tf.minutes_of_hour, tf.carrier, tf.tail_number,
  tf.flight_number,tf.origin, tf.destination,

  tf.planned_departure_time_UTC as planned_departure_time, 
  tf.planned_departure_tz as planned_departure_tz, 
  tf.planned_departure_local_hour as planned_departure_local_hour, 

	tf.planned_arrival_time_UTC as planned_arrival_time,  
	tf.planned_arrival_tz as planned_arrival_tz,  
	tf.planned_arrival_local_hour as planned_arrival_time_local_hour,  

	tf.actual_departure_time_UTC as actual_departure_time,
	tf.actual_departure_tz as actual_departure_tz,
	tf.actual_departure_local_hour as actual_departure_local_hour,

  tf.actual_arrival_time_UTC as actual_arrival_time, 
  tf.actual_arrival_tz as actual_arrival_tz, 
  tf.actual_arrival_local_hour as actual_arrival_local_hour, 

	tf.wheels_off_time_UTC as wheels_off_time, 
	tf.wheels_off_tz as wheels_off_tz, 
	tf.wheels_off_time_local_hour as wheels_off_time_local_hour, 

	tf.wheels_on_time_UTC as wheels_on_time, 
	tf.wheels_on_tz as wheels_on_tz, 
	tf.wheels_on_time_local_hour as wheels_on_time_local_hour, 

	tf.cancelled_flag,
  tf.diverted_flag, tf.num_flights, tf.flight_distance, tf.carrier_delay,
  tf.weather_delay, tf.nas_delay, tf.security_delay, 
tf.late_aircraft_delay,

tfs.number_of_seats
from flights_no_seats tf
left join temp_flight_seats tfs
  on tfs.flight_id = tf.id;
  
create index idx_flights_2_coddt
on flights(carrier, origin, destination, planned_departure_time);

create index idx_flights_3_coddtt
on flights(carrier, origin, destination, planned_departure_time, planned_arrival_time);

create index idx_flights_4_id 
on flights(id);

create index idx_flights_5_yqc 
on flights(year,quarter,carrier);

select dups.flight_id, tfs.number_of_seats
from temp_flight_seats tfs
join
(select tfs.flight_id, count(*) as num_dups
 from temp_flight_seats tfs
 group by tfs.flight_id
 having count(*) > 1
) dups
on dups.flight_id = tfs.flight_id
order by dups.flight_id;


delete
from flights
where planned_arrival_time is null;


drop table temp_t100_flight_seats;
drop table temp_flight_seats;

-- -- Statements from file UpdateFlightsTable.sql 

-- -- create temp_flight_icao

-- -- enables case sensitive search
-- alter table flights_no_seats convert to character set latin1 collate latin1_general_cs;
-- alter table airline_inventories convert to character set latin1 collate latin1_general_cs;

-- -- additional table
-- create table temp_map
-- select distinct
--   inventory_manufacturer as manufacturer,
--   inventory_model as model,
--   icao_code
--  from aircraft_code_mappings
--  where inventory_manufacturer is not null
--    and inventory_model is not null
--    and inventory_manufacturer !="" 
--    and inventory_model !="";
-- -- 58   
-- alter table temp_map convert to character set latin1 collate latin1_general_cs;

-- -- to speedup the statement
-- create index idx_temp_map
--  on temp_map(manufacturer, model);

-- create index idx_ai_ct
--  on airline_inventories(carrier, tail_number);

-- create table temp_flight_icao
-- select 
-- 	distinct fns.id as flight_id, 
-- 	map.icao_code as icao_aircraft_code
-- from flights_no_seats fns
-- join airline_inventories ai
--   on ai.carrier = fns.carrier
--   and ai.tail_number = fns.tail_number
-- join temp_map map
--   on map.manufacturer = ai.manufacturer
--   and map.model = ai.model;
-- -- 5210995 / 7455428
	
-- drop table if exists temp_flight_icao_additional;
-- drop table if exists temp_flight_icao_step1;
-- drop table if exists temp_map;
-- -- !create temp_flight_icao

-- -- temp_flight_icao (insert #1)
-- alter table etms_flights convert to character set latin1 collate latin1_general_cs;

-- create index idx_temp_ficao
-- on temp_flight_icao(flight_id);

-- -- mini
-- create table etms_flights_mini
-- select iata_aircraft_code, carrier, flight_number, origin, destination, planned_departure_time_UTC, planned_departure_tz
-- from etms_flights;

-- alter table etms_flights_mini add planned_departure_time_local 	datetime;
-- alter table etms_flights_mini add year_local 					integer;
-- alter table etms_flights_mini add month_local 					integer;
-- alter table etms_flights_mini add dayofmonth_local 				integer;
-- alter table etms_flights_mini add h24_local 					integer;
-- alter table etms_flights_mini add mi_local 						integer;

-- update etms_flights_mini set planned_departure_time_local 	= CONVERT_TZ(planned_departure_time_UTC,'+00:00',planned_departure_tz);
-- update etms_flights_mini set year_local 					= year(planned_departure_time_local);
-- update etms_flights_mini set month_local 					= month(planned_departure_time_local);
-- update etms_flights_mini set dayofmonth_local 				= dayofmonth(planned_departure_time_local);
-- update etms_flights_mini set h24_local 						= hour(planned_departure_time_local);
-- update etms_flights_mini set mi_local 						= minute(planned_departure_time_local);

-- create index idx_etms_ccfodymd
-- on etms_flights_mini(iata_aircraft_code, carrier, flight_number, origin, destination, year_local, month_local, dayofmonth_local);
-- create index idx_etms_ccodymdhm
-- on etms_flights_mini(iata_aircraft_code, carrier, origin, destination, year_local, month_local, dayofmonth_local, h24_local, mi_local);
-- create index idx_etms_ccodymdp
-- on etms_flights_mini(iata_aircraft_code, carrier, origin, destination, year_local, month_local, dayofmonth_local, planned_departure_time_UTC);
-- -- !mini

-- -- LEVEL 2
-- -- Issue #3. In Oracle 0204 != 204, so we use LPAD function here.
-- create table temp_nodups
-- select 
-- 	fns.id as flight_id, 
-- 	et.iata_aircraft_code as iata_aircraft_code
-- from flights_no_seats fns
-- join etms_flights_mini et
-- 	on et.carrier 						= fns.carrier  
-- 	and cast(et.flight_number as char) 	= cast(LPAD(fns.flight_number,4,'0')  as char)
-- 	and et.origin 						= fns.origin 
-- 	and et.destination 					= fns.destination
-- 	and et.year_local 					= fns.year
-- 	and et.month_local 					= fns.month
-- 	and et.dayofmonth_local 			= fns.day_of_month
-- left join temp_flight_icao tfi 
-- 	on tfi.flight_id = fns.id
-- where 
-- 	(tfi.flight_id is NULL OR tfi.flight_id = "")
-- 	and 	et.iata_aircraft_code is not null 	
-- 	and 	et.iata_aircraft_code != ""
-- group by fns.id
-- having count(distinct et.iata_aircraft_code) = 1;

-- create index idx_etms_ccfodymd
-- on temp_nodups(flight_id, iata_aircraft_code);
-- --1215898
-- -- !LEVEL 2

-- -- LEVEL 1
-- create table temp_sm 
-- select 
-- 	distinct fns.id as flight_id, 
-- 	fns.carrier,  
-- 	nodups.iata_aircraft_code
-- from flights_no_seats fns
-- join temp_nodups nodups 
-- 	on nodups.flight_id = fns.id
-- join etms_flights_mini et1 
-- 	on et1.carrier 						= fns.carrier
-- 	and cast(et1.flight_number as char) = cast(LPAD(fns.flight_number,4,'0')  as char)
-- 	and et1.origin 						= fns.origin
-- 	and et1.destination 				= fns.destination
-- 	and et1.year_local 					= fns.year
-- 	and et1.month_local 				= fns.month
-- 	and et1.dayofmonth_local 			= fns.day_of_month
-- where 	et1.iata_aircraft_code is not null
--     and et1.iata_aircraft_code != "";
-- --1215898
-- -- !LEVEL 1

-- ---- BEGIN
-- alter table fixed_carrier_icao_codes convert to character set latin1 collate latin1_general_cs;

-- insert into temp_flight_icao
-- (flight_id, icao_aircraft_code)
-- select 
-- 	sm.flight_id, 
-- 	case when fx.fixed_icao_aircraft_code is null 
-- 		then sm.iata_aircraft_code
-- 		else fx.fixed_icao_aircraft_code
-- 	end as icao_aircraft_code
-- from temp_sm sm
-- left join fixed_carrier_icao_codes fx 
-- on fx.carrier = sm.carrier 
-- and fx.incorrect_icao_aircraft_code = sm.iata_aircraft_code;
-- -- 6426893
-- -- !temp_flight_icao (insert #1)

-- -- temp_flight_icao (insert #2)
-- alter table flights_no_seats add planned_departure_time_local 	datetime;
-- alter table flights_no_seats add year_local 					integer;
-- alter table flights_no_seats add month_local 					integer;
-- alter table flights_no_seats add dayofmonth_local 				integer;
-- alter table flights_no_seats add h24_local 						integer;
-- alter table flights_no_seats add mi_local 						integer;

-- update flights_no_seats set planned_departure_time_local 	= CONVERT_TZ(planned_departure_time_UTC,'+00:00',planned_departure_tz);
-- update flights_no_seats set year_local 						= year(planned_departure_time_local);
-- update flights_no_seats set month_local 					= month(planned_departure_time_local);
-- update flights_no_seats set dayofmonth_local 				= dayofmonth(planned_departure_time_local);
-- update flights_no_seats set h24_local 						= hour(planned_departure_time_local);
-- update flights_no_seats set mi_local 						= minute(planned_departure_time_local);


-- -- LEVEL 2
-- drop table if exists temp_nodups;
-- create table temp_nodups
-- select 
-- 	fns.id as flight_id, 
-- 	et.iata_aircraft_code as iata_aircraft_code
-- from flights_no_seats fns
-- join etms_flights_mini et
-- 	on et.carrier 		= fns.carrier  
-- 	and et.origin 		= fns.origin 
-- 	and et.destination 	= fns.destination
-- 	and et.year_local 	= fns.year
-- 	and et.month_local 	= fns.month
-- 	and et.dayofmonth_local = fns.day_of_month
-- 	and et.h24_local 	= fns.hour_of_day      
-- 	and et.mi_local 	= fns.minutes_of_hour
-- left join temp_flight_icao tfi 
-- 	on tfi.flight_id = fns.id
-- where 
-- 	(tfi.flight_id is NULL OR tfi.flight_id = "")
-- 	and 	et.iata_aircraft_code is not null 	
-- 	and 	et.iata_aircraft_code != ""
-- group by fns.id
-- having count(distinct et.iata_aircraft_code) = 1;

-- create index idx_etms_ccfodymd
-- on temp_nodups(flight_id, iata_aircraft_code);
-- --851558
-- -- !LEVEL 2

-- -- LEVEL 1
-- drop table if exists temp_sm;
-- create table temp_sm 
-- select 
-- 	distinct fns.id as flight_id, 
-- 	fns.carrier,  
-- 	nodups.iata_aircraft_code
-- from flights_no_seats fns
-- join temp_nodups nodups 
-- 	on nodups.flight_id = fns.id
-- join etms_flights_mini et1 
-- 	on et1.carrier 		 = fns.carrier
-- 	and et1.origin 		 = fns.origin
-- 	and et1.destination 	 = fns.destination
-- 	and et1.year_local 	 = fns.year
-- 	and et1.month_local 	 = fns.month
-- 	and et1.dayofmonth_local = fns.day_of_month
-- 	and et1.h24_local 	 = fns.hour_of_day
-- 	and et1.mi_local 	 = fns.minutes_of_hour
-- where 	et1.iata_aircraft_code is not null
--     and et1.iata_aircraft_code != "";
-- --851558
-- -- !LEVEL 1

-- ---- BEGIN
-- insert into temp_flight_icao
-- (flight_id, icao_aircraft_code)
-- select 
-- 	sm.flight_id, 
-- 	case when fx.fixed_icao_aircraft_code is null 
-- 		then sm.iata_aircraft_code
-- 		else fx.fixed_icao_aircraft_code
-- 	end as icao_aircraft_code
-- from temp_sm sm
-- left join fixed_carrier_icao_codes fx 
-- on fx.carrier = sm.carrier 
-- and fx.incorrect_icao_aircraft_code = sm.iata_aircraft_code;
-- -- 7278451
-- -- !temp_flight_icao (insert #2)


-- -- Consider expanding to 30 / 60 minutes in either direction
-- -- temp_flight_icao (insert #3)
-- -- LEVEL 2
-- drop table if exists temp_nodups;
-- create table temp_nodups
-- select 
-- 	fns.id as flight_id, 
-- 	et.iata_aircraft_code as iata_aircraft_code
-- from flights_no_seats fns
-- join etms_flights_mini et
-- 	on et.carrier 		= fns.carrier  
-- 	and et.origin 		= fns.origin 
-- 	and et.destination 	= fns.destination
-- 	and et.year_local 	= fns.year
-- 	and et.month_local 	= fns.month
-- 	and et.dayofmonth_local = fns.day_of_month	
-- 	and et.planned_departure_time_UTC >= DATE_SUB(fns.planned_departure_time_UTC,INTERVAL 20 MINUTE)
-- 	and et.planned_departure_time_UTC <= DATE_ADD(fns.planned_departure_time_UTC,INTERVAL 20 MINUTE)
-- left join temp_flight_icao tfi 
-- 	on tfi.flight_id = fns.id
-- where 
-- 	(tfi.flight_id is NULL OR tfi.flight_id = "")
-- 	and 	et.iata_aircraft_code is not null 	
-- 	and 	et.iata_aircraft_code != ""
-- group by fns.id
-- having count(distinct et.iata_aircraft_code) = 1;

-- create index idx_etms_ccfodymd
-- on temp_nodups(flight_id, iata_aircraft_code);
-- --8625
-- -- !LEVEL 2

-- -- LEVEL 1
-- drop table if exists temp_sm;
-- create table temp_sm 
-- select 
-- 	distinct fns.id as flight_id, 
-- 	fns.carrier,  
-- 	nodups.iata_aircraft_code
-- from flights_no_seats fns
-- join temp_nodups nodups 
-- 	on nodups.flight_id = fns.id
-- join etms_flights_mini et1 
-- 	on et1.carrier 		 = fns.carrier
-- 	and et1.origin 		 = fns.origin
-- 	and et1.destination 	 = fns.destination
-- 	and et1.year_local 	 = fns.year
-- 	and et1.month_local 	 = fns.month
-- 	and et1.dayofmonth_local = fns.day_of_month
-- 	and et1.planned_departure_time_UTC >= DATE_SUB(fns.planned_departure_time_UTC,INTERVAL 20 MINUTE)
-- 	and et1.planned_departure_time_UTC <= DATE_ADD(fns.planned_departure_time_UTC,INTERVAL 20 MINUTE)
-- where 	et1.iata_aircraft_code is not null
--     and et1.iata_aircraft_code != "";
-- --8625
-- -- !LEVEL 1

-- ---- BEGIN
-- insert into temp_flight_icao
-- (flight_id, icao_aircraft_code)
-- select 
-- 	sm.flight_id, 
-- 	case when fx.fixed_icao_aircraft_code is null 
-- 		then sm.iata_aircraft_code
-- 		else fx.fixed_icao_aircraft_code
-- 	end as icao_aircraft_code
-- from temp_sm sm
-- left join fixed_carrier_icao_codes fx 
-- on fx.carrier = sm.carrier 
-- and fx.incorrect_icao_aircraft_code = sm.iata_aircraft_code;
-- -- 7287076
-- -- !temp_flight_icao (insert #3)

-- -- complex insert #1
-- -- 1a. changing collation
-- alter table offerings convert to character set latin1 collate latin1_general_cs;
-- alter table offerings_mini convert to character set latin1 collate latin1_general_cs;
-- alter table aircraft_code_mappings convert to character set latin1 collate latin1_general_cs;

-- -- 1b. optimizing performance flights_no_seats
-- alter table flights_no_seats
-- add exponent_planned_departure_time_local integer;

-- update flights_no_seats 
-- set exponent_planned_departure_time_local = power(2, dayofweek(planned_departure_time_local)-1);

-- create index idx_flights_icfodepphm
--   on flights_no_seats(id, carrier, flight_number, origin, destination, exponent_planned_departure_time_local,
--   planned_departure_time_local, hour_of_day, minutes_of_hour);

-- -- LEVEL1
-- -- issue #3
-- drop table if exists temp_offin;
-- create table temp_offin
-- select 
-- 		ft.id as flight_id, 
-- 		min(off.id) as offering_id
-- 	from flights_no_seats ft
-- 	join offerings_mini off
-- 		on ft.carrier 					= off.carrier
-- 		and cast(LPAD(ft.flight_number,4,'0')  as char) = cast(off.flight_number as char)
-- 		and ft.flight_number 				= off.flight_number
-- 		and ft.origin 					= off.origin
-- 		and ft.destination 				= off.destination
-- 		and off.flag_multiplication & ft.exponent_planned_departure_time_local != 0
-- 		and date(ft.planned_departure_time_local) 	>= date(off.effective_start)
-- 		and date(ft.planned_departure_time_local) 	<= date(off.effective_end)
-- 		and ft.hour_of_day 				= off.published_departure_hour
-- 		and ft.minutes_of_hour 				= off.published_departure_minutes
-- 	left join temp_flight_icao tfi
-- 		on tfi.flight_id = ft.id
-- 	where tfi.flight_id is NULL or tfi.flight_id = ""
-- 	group by ft.id;
-- --13828
-- -- !LEVEL1

-- insert into temp_flight_icao
-- select 
-- 	offin.flight_id, 
-- 	acm.icao_code
-- from
-- 	temp_offin offin
-- join offerings off
--   on off.id = offin.offering_id
-- join aircraft_code_mappings acm
--   on acm.iata_code = off.iata_aircraft_code;
-- -- 7300649
-- -- !complex insert #1


-- -- complex insert #2 (without last two and's)
-- -- LEVEL1
-- -- issue #3
-- drop table if exists temp_offin;
-- create table temp_offin
-- select 
-- 		ft.id as flight_id, 
-- 		min(off.id) as offering_id
-- 	from flights_no_seats ft
-- 	join offerings_mini off
-- 		on ft.carrier 					= off.carrier
-- 		and cast(LPAD(ft.flight_number,4,'0')  as char) = cast(off.flight_number as char)
-- 		and ft.flight_number 				= off.flight_number
-- 		and ft.origin 					= off.origin
-- 		and ft.destination 				= off.destination
-- 		and off.flag_multiplication & ft.exponent_planned_departure_time_local != 0
-- 		and date(ft.planned_departure_time_local) 	>= date(off.effective_start)
-- 		and date(ft.planned_departure_time_local) 	<= date(off.effective_end)
-- 	left join temp_flight_icao tfi
-- 		on tfi.flight_id = ft.id
-- 	where tfi.flight_id is NULL or tfi.flight_id = ""
-- 	group by ft.id;
-- select count(*) from temp_offin;
-- --5944
-- -- !LEVEL1

-- insert into temp_flight_icao
-- select 
-- 	offin.flight_id, 
-- 	acm.icao_code
-- from
-- 	temp_offin offin
-- join offerings off
--   on off.id = offin.offering_id
-- join aircraft_code_mappings acm
--   on acm.iata_code = off.iata_aircraft_code;
-- -- 7 305 951
-- -- !complex insert #2


-- -- complex insert #3 (without last two and's)
-- -- LEVEL1
-- -- issue #3
-- drop table if exists temp_offin;
-- create table temp_offin
-- select 
-- 		ft.id as flight_id, 
-- 		min(off.id) as offering_id
-- 	from flights_no_seats ft
-- 	join offerings_mini off
-- 		on ft.carrier 					= off.carrier
-- 		and cast(LPAD(ft.flight_number,4,'0')  as char) = cast(off.flight_number as char)
-- 		and ft.flight_number 				= off.flight_number
-- 		and ft.origin 					= off.origin
-- 		and ft.destination 				= off.destination
-- 		and off.flag_multiplication & ft.exponent_planned_departure_time_local != 0
-- 	left join temp_flight_icao tfi
-- 		on tfi.flight_id = ft.id
-- 	where tfi.flight_id is NULL or tfi.flight_id = ""
-- 	group by ft.id;
-- --2477
-- -- !LEVEL1

-- insert into temp_flight_icao
-- select 
-- 	offin.flight_id, 
-- 	acm.icao_code
-- from
-- 	temp_offin offin
-- join offerings off
--   on off.id = offin.offering_id
-- join aircraft_code_mappings acm
--   on acm.iata_code = off.iata_aircraft_code;
  
-- drop table temp_offin;
-- drop table temp_sm;
-- drop table temp_nodups;
-- -- 7307899
-- -- !complex insert #3


-- alter table t100_seats convert to character set latin1 collate latin1_general_cs;

-- drop table if exists temp_t100_flight_seats;
-- create table temp_t100_flight_seats
-- select 
-- fto.id as flight_id, t100s.seats_mean as seats, t100s.seats_coeff_var as coeff_var
-- from t100_seats t100s
-- join flights_no_seats fto
--   on fto.year_local = t100s.year
--   and fto.month_local = t100s.month
--   and fto.carrier = t100s.carrier
--   and fto.origin = t100s.origin
--   and fto.destination = t100s.destination; 
-- -- 7455217


-- drop table if exists temp_flight_seats;
-- create table temp_flight_seats
-- select distinct fns.id as flight_id,
--   ai.number_of_seats
-- from flights_no_seats fns
-- join airline_inventories ai
--   on ai.carrier = fns.carrier
--   and ai.tail_number = fns.tail_number
-- where number_of_seats is not null;
-- -- expected value: 5,558,544
-- -- oracle value: 5,556,261
-- -- mysql value: 5,558,544


-- create index idx_temp_t100_isc
--   on temp_t100_flight_seats(flight_id, seats, coeff_var);
  
-- create index idx_temp_fs_i
--   on temp_flight_seats(flight_id);

-- insert into temp_flight_seats
-- select t100fs.flight_id, t100fs.seats
-- from temp_t100_flight_seats t100fs
-- left join temp_flight_seats tfs on tfs.flight_id = t100fs.flight_id
-- where t100fs.coeff_var <= 0.02 and tfs.flight_id is null;
-- -- expected value: 6 703 157
-- -- oracle value: 6 744 581
-- -- mysql value: 6 746 780

-- alter table carrier_icao_seats convert to character set latin1 collate latin1_general_cs;

-- insert into temp_flight_seats
-- select fns.id, cis.number_of_seats
-- from flights_no_seats fns
-- join temp_flight_icao tfi on tfi.flight_id = fns.id
-- join carrier_icao_seats cis on cis.carrier = fns.carrier and cis.icao_aircraft_code = tfi.icao_aircraft_code
-- left join temp_flight_seats tfs on tfs.flight_id = fns.id
-- where tfs.flight_id is null;
-- -- expected value: 7,346,287
-- -- oracle value: 7,342,643
-- -- mysql value: 7,344,844


-- -- create flights table
-- -- id integer not null auto_increment, primary key (id),
-- create table flights
-- (
--   id integer not null,
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
  
--   icao_aircraft_code char(4),
--   seating_capacity numeric(3)
-- );

-- insert into flights
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
-- 	tf.wheels_off_time_local_hour as wheels_off_time_local_hour, 

-- 	tf.wheels_on_time_UTC as wheels_on_time_UTC, 
-- 	tf.wheels_on_tz as wheels_on_tz, 
-- 	tf.wheels_on_time_local_hour as wheels_on_time_local_hour, 

-- 	tf.cancelled_flag,
--   tf.diverted_flag, tf.num_flights, tf.flight_distance, tf.carrier_delay,
--   tf.weather_delay, tf.nas_delay, tf.security_delay, 
-- tf.late_aircraft_delay,

--   tfi.icao_aircraft_code, 
-- tfs.number_of_seats
-- from flights_no_seats tf
-- left join temp_flight_icao tfi
--   on tfi.flight_id = tf.id
-- left join temp_flight_seats tfs
--   on tfs.flight_id = tf.id;
-- -- 7,455,428

-- create index idx_flights_2_coddt
--   on flights(carrier, origin, destination, planned_departure_time_UTC);
  
-- -- additional scripts
-- select dups.flight_id, tfi.icao_aircraft_code
-- from temp_flight_icao tfi
-- join
-- (select tfi.flight_id, count(*) as num_dups
--  from temp_flight_icao tfi
--  group by tfi.flight_id
--  having count(*) > 1
-- ) dups
-- on tfi.flight_id = dups.flight_id
-- order by dups.flight_id;

-- select dups.flight_id, tfs.number_of_seats
-- from temp_flight_seats tfs
-- join
-- (select tfs.flight_id, count(*) as num_dups
--  from temp_flight_seats tfs
--  group by tfs.flight_id
--  having count(*) > 1
-- ) dups
-- on dups.flight_id = tfs.flight_id
-- order by dups.flight_id;

-- select map.manufacturer, map.model, count(*)
-- from
-- (select distinct
--   inventory_manufacturer as manufacturer,
--   inventory_model as model,
--   icao_code
--  from aircraft_code_mappings
--  where inventory_manufacturer is not null
--    and inventory_model is not null) map
-- group by map.manufacturer, map.model
-- having count(*) > 1;

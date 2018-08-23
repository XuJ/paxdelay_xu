-- XuJiao052717
-- create flights_MIT table with flights table format to debug

drop table if exists flights_MIT;

create table flights_MIT 
(
	id integer not null primary key,
	year numeric(4) not null,
	quarter int not null,
	month numeric(2) not null,
	day_of_month numeric(2) not null,
	day_of_week numeric(1) not null,
	hour_of_day numeric(2) not null,
	minutes_of_hour numeric(2) not null,
	carrier char(2) not null,
	tail_number varchar(10),
	flight_number char(4) not null,
	origin char(3) not null,
	destination char(3) not null,
	planned_departure_time varchar(60) not null,
	planned_arrival_time varchar(60) not null,
	actual_departure_time varchar(60),
	actual_arrival_time varchar(60),
	wheels_off_time varchar(60),
	wheels_on_time varchar(60),
	cancelled_flag numeric(1) not null,
	diverted_flag numeric(1) not null,
	num_flights numeric(1) not null,
	flight_distance numeric(5) not null,
	carrier_delay numeric(4) not null,
	weather_delay numeric(4) not null,
	nas_delay numeric(4) not null,
	security_delay numeric(4) not null,
	late_aircraft_delay numeric(4) not null,
	icao_airport_code varchar(4),
	seating_capacity numeric(3)
);


LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/bts_raw_csv/flights_MIT.csv'
INTO TABLE flights_MIT 
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
	id,
	year,
	quarter,
	month,
	day_of_month,
	day_of_week,
	hour_of_day,
	minutes_of_hour,
	carrier,
	@vtail_number,
	flight_number,
	origin,
	destination,
	planned_departure_time,
	planned_arrival_time,
	@vactual_departure_time,
	@vactual_arrival_time,
	@vwheels_off_time,
	@vwheels_on_time,
	cancelled_flag,
	diverted_flag,
	num_flights,
	flight_distance,
	carrier_delay,
	weather_delay,
	nas_delay,
	security_delay,
	late_aircraft_delay,
	@vicao_airport_code,
	@vseating_capacity
)
set
	tail_number = nullif(@vtail_number,''),
	actual_departure_time = nullif(@vactual_departure_time,''),
	actual_arrival_time = nullif(@vactual_arrival_time,''),
	wheels_off_time = nullif(@vwheels_off_time,''),
	wheels_on_time = nullif(@vwheels_on_time,''),
	icao_airport_code = nullif(@vicao_airport_code,''),
	seating_capacity = nullif(@vseating_capacity,'');


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

  convert_tz(str_to_date(concat(substr(tf.planned_departure_time,1,18),substr(tf.planned_departure_time,29,3)),'%d-%b-%y %h.%i.%s %p'),substr(tf.planned_departure_time from 33),'UTC') as planned_departure_time,
  substr(tf.planned_departure_time from 33) as planned_departure_tz,
  hour(str_to_date(concat(substr(tf.planned_departure_time,11,8),substr(tf.planned_departure_time,29,3)),'%h.%i.%s %p')) as planned_departure_local_hour,

        convert_tz(str_to_date(concat(substr(tf.planned_arrival_time,1,18),substr(tf.planned_arrival_time,29,3)),'%d-%b-%y %h.%i.%s %p'),substr(tf.planned_arrival_time from 33),'UTC') as planned_arrival_time,
        substr(tf.planned_arrival_time from 33) as planned_arrival_tz,
        hour(str_to_date(concat(substr(tf.planned_arrival_time,11,8),substr(tf.planned_arrival_time,29,3)),'%h.%i.%s %p')) as planned_arrival_time_local_hour,

  convert_tz(str_to_date(concat(substr(tf.actual_departure_time,1,18),substr(tf.actual_departure_time,29,3)),'%d-%b-%y %h.%i.%s %p'),substr(tf.actual_departure_time from 33),'UTC') as actual_departure_time,
  substr(tf.actual_departure_time from 33) as actual_departure_tz,
  hour(str_to_date(concat(substr(tf.actual_departure_time,11,8),substr(tf.actual_departure_time,29,3)),'%h.%i.%s %p')) as actual_departure_time_local_hour,

  convert_tz(str_to_date(concat(substr(tf.actual_arrival_time,1,18),substr(tf.actual_arrival_time,29,3)),'%d-%b-%y %h.%i.%s %p'),substr(tf.actual_arrival_time from 33),'UTC') as actual_arrival_time,
  substr(tf.actual_arrival_time from 33) as actual_arrival_tz,
  hour(str_to_date(concat(substr(tf.actual_arrival_time,11,8),substr(tf.actual_arrival_time,29,3)),'%h.%i.%s %p')) as actual_arrival_local_hour,

        convert_tz(str_to_date(concat(substr(tf.wheels_off_time,1,18),substr(tf.wheels_off_time,29,3)),'%d-%b-%y %h.%i.%s %p'),substr(tf.wheels_off_time from 33),'UTC') as wheels_off_time,
        substr(tf.wheels_off_time from 33) as wheels_off_tz,
        hour(str_to_date(concat(substr(tf.wheels_off_time,11,8),substr(tf.wheels_off_time,29,3)),'%h.%i.%s %p')) as wheels_off_time_local_hour,

        convert_tz(str_to_date(concat(substr(tf.wheels_on_time,1,18),substr(tf.wheels_on_time,29,3)),'%d-%b-%y %h.%i.%s %p'),substr(tf.wheels_on_time from 33),'UTC') as wheels_on_time,
        substr(tf.wheels_on_time from 33) as wheels_on_tz,
        hour(str_to_date(concat(substr(tf.wheels_on_time,11,8),substr(tf.wheels_on_time,29,3)),'%h.%i.%s %p')) as wheels_on_time_local_hour,

        tf.cancelled_flag,
  tf.diverted_flag, tf.num_flights, tf.flight_distance, tf.carrier_delay,
  tf.weather_delay, tf.nas_delay, tf.security_delay,
tf.late_aircraft_delay,

tf.seating_capacity
from flights_MIT tf;

create index idx_flights_2_coddt
on flights(carrier, origin, destination, planned_departure_time);

create index idx_flights_3_coddtt
on flights(carrier, origin, destination, planned_departure_time, planned_arrival_time);

create index idx_flights_4_id
on flights(id);

create index idx_flights_5_yqc
on flights(year,quarter,carrier);


delete
from flights
where planned_arrival_time is null;

-- drop table if exists flights_MIT;

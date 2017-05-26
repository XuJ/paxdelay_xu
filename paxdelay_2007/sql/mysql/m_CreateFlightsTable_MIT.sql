-- XuJiao052017
-- create flights_MIT table

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

create index idx_flights_c_MIT
on flights_MIT(carrier);

create index idx_flights_cod_MIT
on flights_MIT(carrier, origin, destination);

create index idx_flights_coddt_MIT
on flights_MIT(carrier, origin, destination, planned_departure_time);

create index idx_flights_cymdm_MIT
on flights_MIT(carrier, year, month, day_of_month);

create index idx_flights_cymod_MIT
on flights_MIT(carrier, year, month, origin, destination);

create index idx_flights_ym_MIT
on flights_MIT(year, month);

create index idx_flights_ymdm_MIT
on flights_MIT(year, month, day_of_month);


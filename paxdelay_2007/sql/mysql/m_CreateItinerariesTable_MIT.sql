-- XuJiao052017
-- create itineraries_MIT table

drop table if exists itineraries_MIT;

create table itineraries_MIT 
(
	id integer not null primary key,
	year numeric(4) not null,
	quarter int not null,
	month numeric(2) not null,
	day_of_month numeric(2) not null,
	day_of_week numeric(1) not null,
	hour_of_day numeric(2) not null,
	minutes_of_hour numeric(2) not null,
	num_flights numeric(1) not null,
	multi_carrier_flag numeric(1) not null,
	first_operating_carrier char(2) not null,
	second_operating_carrier char(2),
	origin char(3) not null,
	connection char(3),
	destination char(3) not null,
	planned_departure_time varchar(60) not null,
	planned_arrival_time varchar(60) not null,
	layover_duration numeric(4),
	first_flight_id numeric(12) not null,
	second_flight_id numeric(12)
);


LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/bts_raw_csv/itineraries_MIT.csv'
INTO TABLE itineraries_MIT 
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
	num_flights,
	multi_carrier_flag,
	first_operating_carrier,
	@vsecond_operating_carrier,
	origin,
	@vconnection,
	destination,
	planned_departure_time,
	planned_arrival_time,
	@vlayover_duration,
	first_flight_id,
	@vsecond_flight_id
)
set
	second_operating_carrier = nullif(@vsecond_operating_carrier,''),
	connection = nullif(@vconnection,''),
	layover_duration = nullif(@vlayover_duration,''),
	second_flight_id = nullif(@vsecond_flight_id,'');

create index bmx_itineraries_c1ymmc_MIT
on itineraries_MIT(first_operating_carrier, year, month, multi_carrier_flag);

create index bmx_itineraries_ft1ft2_MIT
on itineraries_MIT(first_flight_id, second_flight_id);

create index bmx_itineraries_ymdm_MIT
on itineraries_MIT(year, month, day_of_month);

create index bmx_itineraries_c1c2_MIT
on itineraries_MIT(first_operating_carrier, second_operating_carrier);

create index bmx_itineraries_c1c2ym_MIT
on itineraries_MIT(first_operating_carrier, second_operating_carrier, year, month);

create index bmx_itineraries_c1c2ymdm_MIT
on itineraries_MIT(first_operating_carrier, second_operating_carrier, year, month, day_of_month);



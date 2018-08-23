-- XuJiao052017
-- create t100_segments_MIT table

drop table if exists t100_segments_MIT;

create table t100_segments_MIT 
(
	departures_scheduled	numeric(4, 0) not null, 
	departures_performed	numeric(4, 0) not null, 
	payload	numeric(10, 0) not null, 
	seats	numeric(6, 0) not null, 
	passengers	numeric(6, 0) not null, 
	freight	numeric(8, 0) not null, 
	mail	numeric(8, 0) not null, 
	distance	numeric(4, 0) not null, 
	ramp_to_ramp	numeric(6, 0) not null, 
	air_time	numeric(6, 0) not null, 
	unique_carrier	varchar(6) not null, 
	airline_id	numeric(6, 0) not null, 
	unique_carrier_name	varchar(100) not null, 
	unique_carrier_entity	varchar(6) not null, 
	region	char(1) not null, 
	carrier	char(6) not null, 
	carrier_name	varchar(100) not null, 
	carrier_group	numeric(2, 0) not null, 
	carrier_group_new	numeric(2, 0) not null, 
	origin	char(3) not null, 
	origin_city_name	varchar(50) not null, 
	origin_city_code	numeric(6, 0) not null, 
	origin_state	char(2) not null, 
	origin_state_fips	numeric(2, 0) not null, 
	origin_state_name	varchar(50) not null, 
	origin_wac	numeric(4, 0) not null, 
	destination	char(3) not null, 
	destination_city_name	varchar(50) not null, 
	destination_city_code	numeric(6, 0) not null, 
	destination_state	char(2) not null, 
	destination_state_fips	numeric(2, 0) not null, 
	destination_state_name	varchar(50) not null, 
	destination_wac	numeric(4, 0) not null, 
	aircraft_group	numeric(2, 0) not null, 
	aircraft_type	numeric(4, 0) not null, 
	aircraft_config	numeric(1, 0) not null, 
	year	numeric(4) not null, 
	quarter	numeric(1, 0) not null, 
	month	numeric(2, 0) not null, 
	distance_group	numeric(2, 0) not null, 
	service_class	char(1) not null 	
);
LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/bts_raw_csv/t100_segments_MIT.csv'
INTO TABLE t100_segments_MIT 
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
	departures_scheduled, 
	departures_performed, 
	payload, 
	seats, 
	passengers, 
	freight, 
	mail, 
	distance, 
	ramp_to_ramp, 
	air_time, 
	unique_carrier, 
	airline_id, 
	unique_carrier_name, 
	unique_carrier_entity, 
	region, 
	carrier, 
	carrier_name, 
	carrier_group, 
	carrier_group_new, 
	origin, 
	origin_city_name, 
	origin_city_code, 
	origin_state, 
	origin_state_fips, 
	origin_state_name, 
	origin_wac, 
	destination, 
	destination_city_name, 
	destination_city_code, 
	destination_state, 
	destination_state_fips, 
	destination_state_name, 
	destination_wac, 
	aircraft_group, 
	aircraft_type, 
	aircraft_config, 
	year, 
	quarter, 
	month, 
	distance_group, 
	service_class 
);

create index bm_idx_t100_segments_cym_MIT
on t100_segments_MIT(carrier, year, month);

create index bm_idx_t100_segments_cymod_MIT
on t100_segments_MIT(carrier, year, month, origin, destination);

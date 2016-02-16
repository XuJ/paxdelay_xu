drop table if exists t100_segments;

-- These are extra columns for non pax2006 instances.
-- Used with zipped csv files from http://www.transtats.bts.gov (which are with all the fields). 
---------------------------------------------------- 
-- ORIGIN_AIRPORT_ID 		- origin_airport_id
-- ORIGIN_AIRPORT_SEQ_ID 	- origin_airport_seq_id
-- DEST_AIRPORT_ID 			- dest_airport_id
-- DEST_AIRPORT_SEQ_ID 		- dest_airport_seq_id
---------------------------------------------------- 


create table t100_segments
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
	
			origin_airport_id int,
			origin_airport_seq_id int,
	origin_city_code	numeric(6, 0) not null,
	origin_state	char(2) not null,
	origin_state_fips	numeric(2, 0) not null,
	origin_state_name	varchar(50) not null,
	origin_wac	numeric(4, 0) not null,
	destination	char(3) not null,
	destination_city_name	varchar(50) not null,
	
			dest_airport_id int,
			dest_airport_seq_id int,
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
)
ENGINE = MyISAM;



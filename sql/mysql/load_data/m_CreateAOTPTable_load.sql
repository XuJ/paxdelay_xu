drop table if exists tmp_load_aotp;

-- These are extra columns for non pax2006 instances.
-- Used with zipped csv files from http://www.transtats.bts.gov (which are with all the fields). 
-------------------------------- 
--		OriginAirportID       --
--		OriginAirportSeqID    --
--		OriginCityMarketID    --
--                            --
--		DestAirportID		  --
--		DestAirportSeqID	  --
--		DestCityMarketID	  --
--------------------------------

create table tmp_load_aotp
( 
	year	numeric(4) not null, 
	quarter	int not null,
	month	numeric(2, 0) not null,
	day_of_month	numeric(2, 0) not null,
	day_of_week	numeric(1, 0) not null,
	flight_date	char(10) not null,
	unique_carrier	char(2) not null,
	airline_id	numeric(10, 0) not null,
	carrier	char(6) not null,
	tail_number	varchar(10),
	flight_number	varchar(6) not null,
		origin_airportid int,
		origin_airportseqid int,
		origin_citymarketid int,
	origin	varchar(5) not null,
	origin_city_name	varchar(50),
	origin_state	char(2),
	origin_state_fips	varchar(4),
	origin_state_name	varchar(25),
	origin_wac	numeric(4, 0),
		dest_airportid int,
		dest_airportseqid int,
		dest_citymarketid int,
	destination	varchar(5) not null,
	destination_city_name	varchar(50),
	destination_state	char(2),
	destination_state_fips	numeric(2, 0),
	destination_state_name	varchar(25),
	destination_wac	numeric(4, 0),
	planned_departure_time	char(4) not null,
	actual_departure_time	char(4),
	departure_offset	numeric(4, 0),
	departure_delay	numeric(4, 0),
	departure_delay_15	numeric(2, 0),
	departure_delay_group	numeric(2, 0),
	departure_time_block	char(9),
	taxi_out_duration	numeric(4, 0),
	wheels_off_time	char(4),
	wheels_on_time	char(4),
	taxi_in_duration	numeric(4, 0),
	planned_arrival_time	char(4) not null,
	actual_arrival_time	char(4),
	arrival_offset	numeric(4, 0),
	arrival_delay	numeric(4, 0),
	arrival_delay_15	numeric(2, 0),
	arrival_delay_group	numeric(2, 0),
	arrival_time_block	char(9),
	cancelled	numeric(1, 0) not null,
	cancellation_code	char(1),
	diverted	numeric(1, 0) not null,
	planned_elapsed_time	numeric(4, 0),
	actual_elapsed_time	numeric(4, 0),
	in_air_duration	numeric(4, 0),
	number_flights	numeric(1, 0),
	flight_distance	numeric(5, 0),
	distance_group	numeric(2, 0),
	carrier_delay	numeric(4, 0) default 0.00 not null,
	weather_delay	numeric(4, 0) default 0.00 not null,
	nas_delay	numeric(4, 0) default 0.00 not null,
	security_delay	numeric(4, 0) default 0.00 not null,
	late_aircraft_delay	numeric(4, 0) default 0.00 not null
) 
ENGINE = MyISAM
partition by list (quarter)
(	partition p_q1 values in (1),
	partition p_q2 values in (2),
	partition p_q3 values in (3),
	partition p_q4 values in (4)
);





drop procedure if exists loopYear;
DELIMITER $$
CREATE PROCEDURE loopYear()
BEGIN

	DECLARE counts INT;
	DECLARE maxNum INT;
	DECLARE fileName varchar(40);
	DECLARE command varchar(2000);
	SET counts=1;
	SET maxNum=12;
	SET fileName='/mdsg/bts_raw_csv/AOTP_2007_#.csv';
	WHILE  counts<=maxNum DO
		SET fileName = REPLACE(fileName, '#', CONCAT('',counts))

		SET command= CONCAT("LOAD DATA LOCAL INFILE ",fileName,"INTO TABLE tmp_load_aotp
		FIELDS TERMINATED BY ','
		OPTIONALLY ENCLOSED BY '"'
		LINES TERMINATED BY '\n'
		IGNORE 1 LINES
		(
			year,
			quarter,
			month,
			day_of_month,
			day_of_week,
			flight_date,
			unique_carrier,
			airline_id,
			carrier,
			tail_number,
			flight_number,
				origin_airportid,
				origin_airportseqid,
				origin_citymarketid,
			origin,
			origin_city_name,
			origin_state,
			origin_state_fips,
			origin_state_name,
			origin_wac,
				dest_airportid,
				dest_airportseqid,
				dest_citymarketid,
			destination,
			destination_city_name,
			destination_state,
			destination_state_fips,
			destination_state_name,
			destination_wac,
			planned_departure_time,
			actual_departure_time,
			departure_offset,
			departure_delay,
			departure_delay_15,
			departure_delay_group,
			departure_time_block,
			taxi_out_duration,
			wheels_off_time,
			wheels_on_time,
			taxi_in_duration,
			planned_arrival_time,
			actual_arrival_time,
			arrival_offset,
			arrival_delay,
			arrival_delay_15,
			arrival_delay_group,
			arrival_time_block,
			cancelled,
			cancellation_code,
			diverted,
			planned_elapsed_time,
			actual_elapsed_time,
			in_air_duration,
			number_flights,
			flight_distance,
			distance_group,
			carrier_delay,
			weather_delay,
			nas_delay,
			security_delay,
			late_aircraft_delay
		)");
		prepare s1 from command;
		execute s1;deallocate prepare s1;
		SET counts=counts+1;
		SET fileName='/mdsg/bts_raw_csv/AOTP_2007_#.csv';
	END WHILE;
END$$
DELIMITER ;

CALL loopYear();





insert into aotp
(year, quarter, month, day_of_month, day_of_week, flight_date, unique_carrier, airline_id, carrier, tail_number, flight_number, origin, origin_city_name, origin_state, origin_state_fips, origin_state_name,
origin_wac, destination, destination_city_name, destination_state, destination_state_fips, destination_state_name, destination_wac, planned_departure_time, actual_departure_time, departure_offset,
departure_delay, departure_delay_15, departure_delay_group, departure_time_block, taxi_out_duration, wheels_off_time, wheels_on_time, taxi_in_duration, planned_arrival_time, actual_arrival_time,
arrival_offset, arrival_delay, arrival_delay_15, arrival_delay_group, arrival_time_block, cancelled, cancellation_code, diverted, planned_elapsed_time, actual_elapsed_time, in_air_duration,
number_flights, flight_distance, distance_group, carrier_delay, weather_delay, nas_delay, security_delay, late_aircraft_delay)

select  year, quarter, month, day_of_month, day_of_week, 
STR_TO_DATE(flight_date,'%Y-%m-%d') as flight_date, 
unique_carrier, airline_id, carrier, tail_number, flight_number, origin, origin_city_name, origin_state, origin_state_fips, origin_state_name,
origin_wac, destination, destination_city_name, destination_state, destination_state_fips, destination_state_name, destination_wac, planned_departure_time, actual_departure_time, departure_offset,
departure_delay, departure_delay_15, departure_delay_group, departure_time_block, taxi_out_duration, wheels_off_time, wheels_on_time, taxi_in_duration, planned_arrival_time, actual_arrival_time,
arrival_offset, arrival_delay, arrival_delay_15, arrival_delay_group, arrival_time_block, cancelled, cancellation_code, diverted, planned_elapsed_time, actual_elapsed_time, in_air_duration,
number_flights, flight_distance, distance_group, carrier_delay, weather_delay, nas_delay, security_delay, late_aircraft_delay
from tmp_load_aotp;

drop table if exists tmp_load_aotp;

create index idx_aotp_ymdmcod
  on aotp(year, month, day_of_month, carrier, origin, destination, tail_number)
	using btree;



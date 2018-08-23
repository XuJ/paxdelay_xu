-- OPHASWONGSE
-- Add column id(Auto-Increment)
drop table if exists aotp;

create table aotp
( 
	id int not null AUTO_INCREMENT,
	year	numeric(4) not null, 
	quarter	int not null,
	month	numeric(2, 0) not null,
	day_of_month	numeric(2, 0) not null,
	day_of_week	numeric(1, 0) not null,
	flight_date	date not null,
	unique_carrier	char(2) not null,
	airline_id	numeric(10, 0) not null,
	carrier	char(6) not null,
	tail_number	varchar(10),
	flight_number	varchar(6) not null,
	origin	varchar(5) not null,
	origin_city_name	varchar(50),
	origin_state	char(2),
	origin_state_fips	varchar(4),
	origin_state_name	varchar(25),
	origin_wac	numeric(4, 0),
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
	late_aircraft_delay	numeric(4, 0) default 0.00 not null,
	primary key(id, quarter)
) 
ENGINE = MyISAM
partition by list (quarter)
(	partition p_q1 values in (1),
	partition p_q2 values in (2),
	partition p_q3 values in (3),
	partition p_q4 values in (4)
);



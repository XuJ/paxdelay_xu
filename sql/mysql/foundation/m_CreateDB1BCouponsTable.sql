drop table if exists db1b_coupons;


-- These are extra columns for non pax2006 instances.
-- Used with zipped csv files from http://www.transtats.bts.gov (which are with all the fields). 
----------------------------------------------------
--		OriginAirportSeqID (origin_airport_seqid) --
--      DestAirportSeqID   (dest_airport_seqid)   --
----------------------------------------------------

create table db1b_coupons
(
	itinerary_id	numeric(16, 0) not null,
	market_id	numeric(16, 0) not null,
	sequence_number	numeric(2, 0) not null,
	number_coupons	numeric(2, 0) not null,
	year	numeric(4) not null,
	quarter	int not null,
	origin	char(3) not null,
	number_origin_airports	numeric(2, 0) not null,
	
		origin_airport_seqid int,
	origin_city_code	numeric(6, 0) not null,
	origin_country_code	varchar(3) not null,
	origin_state_fips	numeric(2, 0) not null,
	origin_state	char(2) not null,
	origin_state_name	varchar(50) not null,
	origin_wac	numeric(4, 0) not null,
	destination	char(3) not null,
	number_destination_airports	numeric(2, 0) not null,
	
		dest_airport_seqid int,
	destination_city_code	numeric(6, 0) not null,
	destination_country_code	varchar(3) not null,
	destination_state_fips	numeric(2, 0) not null,
	destination_state	char(2) not null,
	destination_state_name	varchar(50) not null,
	destination_wac	numeric(4, 0) not null,
	break_code	char(1),
	coupon_type	char(1) not null,
	ticketing_carrier	varchar(3) not null,
	operating_carrier	varchar(3) not null,
	reporting_carrier	varchar(3) not null,
	passengers	numeric(4, 0) not null,
	fare_class	char(1),
	distance	numeric(4, 0) not null,
	distance_group	numeric(2, 0) not null,
	gateway_flag	numeric(1, 0) not null,
	itinerary_geography_type	numeric(1, 0) not null,
	coupon_geography_type	numeric(1, 0) not null
)
ENGINE = MyISAM
partition by list (quarter)
(	partition p_q1 values in (1),
	partition p_q2 values in (2),
	partition p_q3 values in (3),
	partition p_q4 values in (4)
);
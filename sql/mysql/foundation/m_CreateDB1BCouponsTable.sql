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
	destination	char(3) not null,
	number_destination_airports	numeric(2, 0) not null,
	ticketing_carrier	varchar(3) not null,
	operating_carrier	varchar(3) not null,
	reporting_carrier	varchar(3) not null,
	passengers	numeric(4, 0) not null,
	fare_class	char(1),
)
ENGINE = MyISAM
partition by list (quarter)
(	partition p_q1 values in (1),
	partition p_q2 values in (2),
	partition p_q3 values in (3),
	partition p_q4 values in (4)
);
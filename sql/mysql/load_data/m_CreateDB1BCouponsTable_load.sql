-- These are extra columns for non pax2006 instances.
-- Used with zipped csv files from http://www.transtats.bts.gov (which are with all the fields). 
----------------------------------------------------
--		OriginAirportSeqID (origin_airport_seqid) --
--      DestAirportSeqID   (dest_airport_seqid)   --
----------------------------------------------------

LOAD DATA INFILE '/export/mysql/import/Origin_and_Destination_Survey_DB1BCoupon_2007_1.csv'
INTO TABLE db1b_coupons
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(itinerary_id,
market_id,
sequence_number,
number_coupons,
year,
number_origin_airports,
	origin_airport_seqid,
origin_city_code,
quarter,
origin,
origin_country_code,
origin_state_fips,
origin_state,
origin_state_name,
origin_wac,
number_destination_airports,
	dest_airport_seqid,
destination_city_code,
destination,
destination_country_code,
destination_state_fips,
destination_state,
destination_state_name,
destination_wac,
break_code,
coupon_type,
ticketing_carrier,
operating_carrier,
reporting_carrier,
passengers,
fare_class,
distance,
distance_group,
gateway_flag,
itinerary_geography_type,
coupon_geography_type);

update db1b_coupons
set ticketing_carrier = 'US'
where ticketing_carrier = 'HP';

update db1b_coupons
set operating_carrier = 'US'
where operating_carrier = 'HP';

update db1b_coupons
set reporting_carrier = 'US'
where reporting_carrier = 'HP';


create unique index idx_db1b_qmidseq
  on db1b_coupons(quarter, market_id, sequence_number);

create index idx_db1b_yqcod
  on db1b_coupons(year, quarter, operating_carrier, origin, destination);

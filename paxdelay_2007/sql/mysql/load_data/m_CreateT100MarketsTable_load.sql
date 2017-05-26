LOAD DATA INFILE '/export/mysql/import/T100D_Market_US_Carrier_2007.csv'
INTO TABLE t100_markets
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
	passengers,
	freight,
	mail,
	distance,
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
	year,
	quarter,
	month,
	distance_group,
	service_class
);
-- 275,786

update t100_markets
set carrier = 'us'
where carrier = 'hp';

create index idx_t100_markets_ymcod
  on t100_markets(year, month, carrier, origin, destination);
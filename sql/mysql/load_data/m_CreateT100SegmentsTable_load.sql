LOAD DATA INFILE '/export/mysql/import/T100D_Segment_US_Carrier_2007.csv'
INTO TABLE t100_segments
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(departures_scheduled,
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
origin_airport_id,
origin_airport_seq_id,
origin_city_code,
origin,
origin_city_name,
origin_state,
origin_state_fips,
origin_state_name,
origin_wac,
dest_airport_id,
dest_airport_seq_id,
destination_city_code,
destination,
destination_city_name,
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
service_class);


update t100_segments
set carrier = 'US'
where carrier = 'HP';


create index idx_t100_segments_cym
	on t100_segments(carrier, year, month)
	using btree;


create index idx_t100_segments_cymod
  on t100_segments(carrier, year, month, origin, destination)
	using btree;


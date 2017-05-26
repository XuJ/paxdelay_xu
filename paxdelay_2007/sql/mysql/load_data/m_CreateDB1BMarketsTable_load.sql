LOAD DATA INFILE '/export/mysql/import/schaan/Origin_and_Destination_Survey_DB1BMarket_2007_1.csv'
INTO TABLE db1b_markets
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
itinerary_id,
market_id,
number_coupons,
year,
quarter,
origin,
number_origin_airports,
origin_city_code,
origin_country_code,
origin_state_fips,
origin_state,
origin_state_name,
origin_wac,
destination,
number_destination_airports,
destination_city_code,
destination_country_code,
destination_state_fips,
destination_state,
destination_state_name,
destination_wac,
airport_group,
airport_wac_group,
ticketing_carrier_change,
ticketing_carrier_group,
operating_carrier_change,
operating_carrier_group,
reporting_carrier,
ticketing_carrier,
operating_carrier,
bulk_fare_flag,
passengers,
fare,
distance,
distance_group,
miles_flown,
nonstop_miles,
itinerary_geography_type,
market_geography_type
);

LOAD DATA INFILE '/export/mysql/import/schaan/Origin_and_Destination_Survey_DB1BMarket_2007_2.csv'
INTO TABLE db1b_markets
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
itinerary_id,
market_id,
number_coupons,
year,
quarter,
origin,
number_origin_airports,
origin_city_code,
origin_country_code,
origin_state_fips,
origin_state,
origin_state_name,
origin_wac,
destination,
number_destination_airports,
destination_city_code,
destination_country_code,
destination_state_fips,
destination_state,
destination_state_name,
destination_wac,
airport_group,
airport_wac_group,
ticketing_carrier_change,
ticketing_carrier_group,
operating_carrier_change,
operating_carrier_group,
reporting_carrier,
ticketing_carrier,
operating_carrier,
bulk_fare_flag,
passengers,
fare,
distance,
distance_group,
miles_flown,
nonstop_miles,
itinerary_geography_type,
market_geography_type
);

LOAD DATA INFILE '/export/mysql/import/schaan/Origin_and_Destination_Survey_DB1BMarket_2007_3.csv'
INTO TABLE db1b_markets
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
itinerary_id,
market_id,
number_coupons,
year,
quarter,
origin,
number_origin_airports,
origin_city_code,
origin_country_code,
origin_state_fips,
origin_state,
origin_state_name,
origin_wac,
destination,
number_destination_airports,
destination_city_code,
destination_country_code,
destination_state_fips,
destination_state,
destination_state_name,
destination_wac,
airport_group,
airport_wac_group,
ticketing_carrier_change,
ticketing_carrier_group,
operating_carrier_change,
operating_carrier_group,
reporting_carrier,
ticketing_carrier,
operating_carrier,
bulk_fare_flag,
passengers,
fare,
distance,
distance_group,
miles_flown,
nonstop_miles,
itinerary_geography_type,
market_geography_type
);

LOAD DATA INFILE '/export/mysql/import/schaan/Origin_and_Destination_Survey_DB1BMarket_2007_4.csv'
INTO TABLE db1b_markets
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
itinerary_id,
market_id,
number_coupons,
year,
quarter,
origin,
number_origin_airports,
origin_city_code,
origin_country_code,
origin_state_fips,
origin_state,
origin_state_name,
origin_wac,
destination,
number_destination_airports,
destination_city_code,
destination_country_code,
destination_state_fips,
destination_state,
destination_state_name,
destination_wac,
airport_group,
airport_wac_group,
ticketing_carrier_change,
ticketing_carrier_group,
operating_carrier_change,
operating_carrier_group,
reporting_carrier,
ticketing_carrier,
operating_carrier,
bulk_fare_flag,
passengers,
fare,
distance,
distance_group,
miles_flown,
nonstop_miles,
itinerary_geography_type,
market_geography_type
);

-- 20,778,751

update db1b_markets
set ticketing_carrier = 'US'
where ticketing_carrier = 'HP';

update db1b_markets
set operating_carrier = 'US'
where operating_carrier = 'HP';

update db1b_markets
set reporting_carrier = 'US'
where reporting_carrier = 'HP';

-- Wait to create the indices until after the table is populated
create index idx_db1b_markets_yqcod
  on db1b_markets(year, quarter, operating_carrier, origin, destination);
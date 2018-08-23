options
(
  skip = 1,
  direct = true
)
load data
infile 'Origin_and_Destination_Survey_DB1BMarket_2007_1.csv'
infile 'Origin_and_Destination_Survey_DB1BMarket_2007_2.csv'
infile 'Origin_and_Destination_Survey_DB1BMarket_2007_3.csv'
infile 'Origin_and_Destination_Survey_DB1BMarket_2007_4.csv'
replace
into table paxdelay.db1b_markets
fields terminated by ',' optionally enclosed by '"'
(
  itinerary_id integer external,
  market_id integer external,
  number_coupons integer external,
  year integer external,
  quarter integer external,
  origin char,
  number_origin_airports integer external,
  origin_city_code integer external,
  origin_country_code char,
  origin_state_fips integer external,
  origin_state char,
  origin_state_name char,
  origin_wac integer external,
  destination char,
  number_destination_airports integer external,
  destination_city_code integer external,
  destination_country_code char,
  destination_state_fips integer external,
  destination_state char,
  destination_state_name char,
  destination_wac integer external,
  airport_group char,
  airport_wac_group char,
  ticketing_carrier_change integer external,
  ticketing_carrier_group char,
  operating_carrier_change integer external,
  operating_carrier_group char,
  reporting_carrier char,
  ticketing_carrier char,
  operating_carrier char,
  bulk_fare_flag integer external,
  passengers integer external,
  fare float external,
  distance integer external,
  distance_group integer external,
  miles_flown integer external,
  nonstop_miles integer external,
  itinerary_geography_type integer external,
  market_geography_type integer external
)

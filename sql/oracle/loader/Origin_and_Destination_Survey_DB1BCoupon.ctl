options
(
  skip = 1,
  direct = true
)
load data
infile 'Origin_and_Destination_Survey_DB1BCoupon_2007_1.csv'
infile 'Origin_and_Destination_Survey_DB1BCoupon_2007_2.csv'
infile 'Origin_and_Destination_Survey_DB1BCoupon_2007_3.csv'
infile 'Origin_and_Destination_Survey_DB1BCoupon_2007_4.csv'
replace
into table paxdelay.db1b_coupons
fields terminated by ',' optionally enclosed by '"'
(
  itinerary_id integer external,
  market_id integer external,
  sequence_number integer external,
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
  break_code char,
  coupon_type char,
  ticketing_carrier char,
  operating_carrier char,
  reporting_carrier char,
  passengers integer external,
  fare_class char,
  distance integer external,
  distance_group integer external,
  gateway_flag integer external,
  itinerary_geography_type integer external,
  coupon_geography_type integer external
)

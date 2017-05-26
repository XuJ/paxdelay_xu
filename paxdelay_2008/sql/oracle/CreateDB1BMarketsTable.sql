create table db1b_markets
(
  itinerary_id number(16, 0) not null,
  market_id number(16, 0) not null,
  number_coupons number(6, 0) not null,
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  origin char(3) not null,
  number_origin_airports number(2, 0) not null,
  origin_city_code number(6, 0) not null,
  origin_country_code varchar2(3) not null,
  origin_state_fips number(2, 0) not null,
  origin_state char(2) not null,
  origin_state_name varchar2(50) not null,
  origin_wac number(4, 0) not null,
  destination char(3) not null,
  number_destination_airports number(2, 0) not null,
  destination_city_code number(6, 0) not null,
  destination_country_code varchar2(3) not null,
  destination_state_fips number(2, 0) not null,
  destination_state char(2) not null,
  destination_state_name varchar2(50) not null,
  destination_wac number(4, 0) not null,
  airport_group varchar2(51) not null,
  airport_wac_group varchar2(38) not null,
  ticketing_carrier_change number(1, 0) not null,
  ticketing_carrier_group varchar2(35) not null,
  operating_carrier_change number(1, 0) not null,
  operating_carrier_group varchar2(35) not null,
  reporting_carrier char(2) not null,
  ticketing_carrier char(2) not null,
  operating_carrier char(2) not null,
  bulk_fare_flag number(1, 0) not null,
  passengers number(4, 0) not null,
  fare number(7, 2) not null,
  distance number(5, 0) not null,
  distance_group number(2, 0) not null,
  miles_flown number(5, 0) not null,
  nonstop_miles number(5, 0) not null,
  itinerary_geography_type number(1, 0) not null,
  market_geography_type number(1, 0) not null
) partition by list (quarter)
(partition p_q1 values (1),
  partition p_q2 values (2),
  partition p_q3 values (3),
  partition p_q4 values (4)
);

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
  on db1b_markets(year, quarter, operating_carrier, origin, destination)
  local
  tablespace users;


create table db1b_coupons_2008
(
  itinerary_id number(16, 0) not null,
  market_id number(16, 0) not null,
  sequence_number number(2, 0) not null,
  number_coupons number(2, 0) not null,
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
  break_code char(1),
  coupon_type char(1) not null,
  ticketing_carrier varchar2(3) not null,
  operating_carrier varchar2(3) not null,
  reporting_carrier varchar2(3) not null,
  passengers number(4, 0) not null,
  fare_class char(1),
  distance number(4, 0) not null,
  distance_group number(2, 0) not null,
  gateway_flag number(1, 0) not null,
  itinerary_geography_type number(1, 0) not null,
  coupon_geography_type number(1, 0) not null
) partition by list (quarter)
(partition p_q1 values (1),
  partition p_q2 values (2),
  partition p_q3 values (3),
  partition p_q4 values (4)
);

update db1b_coupons_2008
set ticketing_carrier = 'US'
where ticketing_carrier = 'HP';

update db1b_coupons_2008
set operating_carrier = 'US'
where operating_carrier = 'HP';

update db1b_coupons_2008
set reporting_carrier = 'US'
where reporting_carrier = 'HP';

-- Wait to create the indices until after the table is populated
create unique index idx_db1b_08_qmidseq
  on db1b_coupons_2008(quarter, market_id, sequence_number)
  local
  tablespace users;

create index idx_db1b_08_yqcod
  on db1b_coupons_2008(year, quarter, operating_carrier, origin, destination)
  local
  tablespace users;


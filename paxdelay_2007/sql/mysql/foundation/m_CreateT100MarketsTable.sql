drop table if exists t100_markets;

create table t100_markets
(
  passengers numeric(6) not null,
  freight numeric(8) not null,
  mail numeric(8) not null,
  distance numeric(4) not null,
  unique_carrier char(6) not null,
  airline_id numeric(6) not null,
  unique_carrier_name char(100) not null,
  unique_carrier_entity char(6) not null,
  region char(1) not null,
  carrier char(3) not null,
  carrier_name char(100) not null,
  carrier_group numeric(2) not null,
  carrier_group_new numeric(2) not null,
  origin char(3) not null,
  origin_city_name char(50) not null,
  origin_city_code numeric(6) not null,
  origin_state char(2) not null,
  origin_state_fips numeric(2) not null,
  origin_state_name char(50) not null,
  origin_wac numeric(4) not null,
  destination char(3) not null,
  destination_city_name char(50) not null,
  destination_city_code numeric(6) not null, 
  destination_state char(2) not null,
  destination_state_fips numeric(2) not null,
  destination_state_name char(50) not null,
  destination_wac numeric(4) not null,
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  distance_group numeric(2) not null,
  service_class char(1) not null
);
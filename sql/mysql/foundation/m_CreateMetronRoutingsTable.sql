drop table if exists metron_routings;

create table metron_routings
(
  carrier char(6) not null,
  icao_aircraft_code char(4) not null,
  fleet_index numeric(4) not null,
  flight_order numeric(2) not null,
  flight_id numeric(12) not null,
  metron_id numeric(12) not null
);
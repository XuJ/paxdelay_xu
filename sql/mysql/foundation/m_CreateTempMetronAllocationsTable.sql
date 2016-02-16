drop table if exists temp_metron_allocations;

create table temp_metron_allocations
(
  first_carrier char(6) not null,
  second_carrier char(6),
  first_flight_id numeric(12) not null,
  second_flight_id numeric(12),
  passengers numeric(4) not null
);
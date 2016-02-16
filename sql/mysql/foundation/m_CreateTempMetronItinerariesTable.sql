drop table if exists temp_metron_itineraries;

create table temp_metron_itineraries
(
  num_flights numeric(1) not null,
  first_flight_id numeric(12) not null,
  second_flight_id numeric(12)
);
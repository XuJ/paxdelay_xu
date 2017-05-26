drop table if exists temp_passenger_delays;

create table temp_passenger_delays
(
  num_passengers numeric(8, 4) not null,
  original_first_flight_id numeric(12) not null,
  original_second_flight_id numeric(12),
  first_disruption_cause numeric(1) not null,
  first_disruption_hour numeric(2),
  num_disruptions numeric(2) not null,
  disruption_origin_sequence varchar(40),
  disruption_cause_sequence varchar(20),
  last_flown_flight_id numeric(12),
  trip_delay numeric(8, 4) not null
);
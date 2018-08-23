drop table if exists temp_itinerary_comparisons;
create table temp_itinerary_comparisons
(
  num_flights numeric(1) not null,
  first_flight_id numeric(12) not null,
  second_flight_id numeric(12),
  allocated_passengers numeric(8, 4),
  airline_passengers numeric(8, 4)
);
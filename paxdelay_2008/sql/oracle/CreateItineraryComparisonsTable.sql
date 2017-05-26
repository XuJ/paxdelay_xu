create table temp_itinerary_comparisons
(
  num_flights number(1, 0) not null,
  first_flight_id number(12, 0) not null,
  second_flight_id number(12, 0),
  allocated_passengers number(8, 4),
  airline_passengers number(8, 4)
);

create table itinerary_comparisons
(
  allocation_method varchar2(20) not null,
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  day_of_month number(2, 0) not null,
  day_of_week number(1, 0) not null,
  hour_of_day number(2, 0) not null,
  num_flights number(1, 0) not null,
  first_flight_id number(12, 0) not null,
  first_carrier char(2) not null,
  first_flight_number char(4) not null,
  first_departure_time timestamp with time zone not null,
  first_arrival_time timestamp with time zone not null,
  first_seating_capacity number(4, 0),
  second_flight_id number(12, 0),
  second_carrier char(2),
  second_flight_number char(4),
  second_departure_time timestamp with time zone,
  second_arrival_time timestamp with time zone,
  second_seating_capacity number(4, 0),
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  layover_duration number(4, 0),
  allocated_passengers number(8, 4),
  airline_passengers number(8, 4)
);

insert into itinerary_comparisons
select 'Scaled_1.25_1110', ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  ft1.day_of_week, ft1.hour_of_day, tic.num_flights,
  ft1.id, ft1.carrier, ft1.flight_number,
  ft1.planned_departure_time, ft1.planned_arrival_time,
  ft1.seating_capacity,
  ft2.id, ft2.carrier, ft2.flight_number,
  ft2.planned_departure_time, ft2.planned_arrival_time,
  ft2.seating_capacity,
  ft1.origin, decode(tic.num_flights, 1, null, ft1.destination), 
  decode(tic.num_flights, 1, ft1.destination, ft2.destination),
  decode(tic.num_flights, 1, null,
    (extract(day from (ft2.planned_departure_time - ft1.planned_arrival_time))) * 24 * 60 +
      (extract(hour from (ft2.planned_departure_time - ft1.planned_arrival_time))) * 60 +
      (extract(minute from (ft2.planned_departure_time - ft1.planned_arrival_time)))),
  tic.allocated_passengers, tic.airline_passengers
from temp_itinerary_comparisons tic
join flights ft1
  on ft1.id = tic.first_flight_id
left join flights ft2
  on ft2.id = tic.second_flight_id;


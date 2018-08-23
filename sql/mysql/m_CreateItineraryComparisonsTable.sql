drop table if exists itinerary_comparisons;

create table itinerary_comparisons
(
  allocation_method varchar(20) not null,
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  day_of_week numeric(1) not null,
  hour_of_day numeric(2) not null,
  num_flights numeric(1) not null,
  first_flight_id numeric(12) not null,
  first_carrier char(2) not null,
  first_flight_numeric char(4) not null,

  first_departure_time_UTC date not null,
  first_departure_tz char(19),
  first_departure_local_hour numeric(2),

  first_arrival_time_UTC date not null,
  first_arrival_tz char (19),
  first_arrival_local_hour numeric(2),

  first_seating_capacity numeric(4),
  second_flight_id numeric(12),
  second_carrier char(2),
  second_flight_numeric char(4),

  second_departure_time_UTC date,
  second_departure_tz char(19),
  second_departure_local_hour numeric(2),

  second_arrival_time_UTC date,
  second_arrival_tz char(19),
  second_arrival_local_hoour numeric(2),

  second_seating_capacity numeric(4),
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  layover_duration numeric(4),
  allocated_passengers numeric(8, 4),
  airline_passengers numeric(8, 4)
);

insert into itinerary_comparisons
(
  allocation_method, year, quarter, month, day_of_month, day_of_week, hour_of_day, num_flights, first_flight_id, first_carrier, first_flight_numeric,

  first_departure_time_UTC, first_departure_tz, first_departure_local_hour, 
	first_arrival_time_UTC, first_arrival_tz, first_arrival_local_hour, 

  first_seating_capacity, second_flight_id, second_carrier, second_flight_numeric,

  second_departure_time_UTC, second_departure_tz, second_departure_local_hour,
  second_arrival_time_UTC, second_arrival_tz, second_arrival_local_hoour, 

  second_seating_capacity, origin, connection, destination, layover_duration, allocated_passengers, airline_passengers
)
select 
	'Scaled_1.25_1110' as allocation_method,
	ft1.year as year,
	ft1.quarter as quarter,
	ft1.month as month,
	ft1.day_of_month as day_of_month,
	ft1.day_of_week as day_of_week,
	ft1.hour_of_day as hour_of_day,
	tic.num_flights as num_flights,
	ft1.id as first_flight_id,
	ft1.carrier as first_carrier,
	ft1.flight_number as first_flight_numeric,
	ft1.planned_departure_time_UTC as first_departure_time_UTC, ft1.planned_departure_tz as first_arrival_tz,ft1.planned_departure_local_hour as first_arrival_local_hour,
	ft1.planned_arrival_time_UTC as first_arrival_time_UTC, ft1.planned_arrival_tz as first_arrival_tz,  ft1.planned_arrival_local_hour as first_arrival_local_hour,
	ft1.seating_capacity as first_seating_capacity,
	ft2.id as second_flight_id,
	ft2.carrier as second_carrier,
	ft2.flight_number as second_flight_numeric,
	ft2.planned_departure_time_UTC as second_departure_time_UTC, ft2.planned_departure_tz as second_departure_tz,ft2.planned_departure_local_hour as second_departure_local_hour,
	ft2.planned_arrival_time_UTC as second_arrival_time_UTC, ft2.planned_arrival_tz as second_arrival_tz, ft2.planned_arrival_local_hour as second_arrival_local_hoour,
	ft2.seating_capacity as second_seating_capacity,
	ft1.origin as origin, 

	case when tic.num_flights = 1
				then null
				else ft1.destination
			end as connection,

	case when tic.num_flights = 1
				then ft1.destination
				else ft2.destination
			end as destination,

	case when tic.num_flights = 1
				then null
				else TIMESTAMPDIFF(minute, ft2.planned_departure_time_UTC, ft1.planned_arrival_time_UTC)
			end as layover_duration,

	tic.allocated_passengers as allocated_passengers,
	tic.airline_passengers as airline_passengers
from temp_itinerary_comparisons tic
join flights ft1 on ft1.id = tic.first_flight_id
left join flights ft2 on ft2.id = tic.second_flight_id;
-- 1142085

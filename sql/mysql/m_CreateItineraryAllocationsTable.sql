-- XuJiao
-- It took 13 min 52 sec for one month
-- Record: 27,938,722 for one month

drop table if exists itinerary_allocations;

create table itinerary_allocations
(
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  num_flights numeric(1) not null,
  first_carrier char(6) not null,
  second_carrier char(6),
  origin char(3) not null,
  destination char(3) not null,

  planned_departure_time datetime not null,
  planned_departure_tz char(25),
  planned_departure_local_hour numeric(2),

  planned_arrival_time datetime not null,
  planned_arrival_tz char(25),
  planned_arrival_local_hour numeric(2),

  first_flight_id numeric(12) not null,
  second_flight_id numeric(12),
  passengers numeric(4) not null
)
partition by list (quarter)
(	partition p_q1 values in (1),
	partition p_q2 values in (2),
	partition p_q3 values in (3),
	partition p_q4 values in (4)
);


insert into itinerary_allocations
select ft1.year as year, ft1.quarter as quarter, ft1.month as month, ft1.day_of_month as day_of_month,
  1 as num_flights, tia.first_carrier as first_carrier, null as second_carrier, 
  ft1.origin as origin, ft1.destination as destination,

  ft1.planned_departure_time as planned_departure_time, ft1.planned_departure_tz as planned_departure_tz, ft1.planned_departure_local_hour as planned_departure_local_hour,
  ft1.planned_arrival_time as planned_arrival_time, ft1.planned_arrival_tz as planned_arrival_tz, ft1.planned_arrival_local_hour as planned_arrival_local_hour,

  tia.first_flight_id as first_flight_id, null as second_flight_id,
  tia.passengers as passengers
from temp_itinerary_allocations tia
join flights ft1 on ft1.id = tia.first_flight_id
where tia.second_flight_id is null
union all
select ft1.year as year, ft1.quarter as quarter, ft1.month as month, ft1.day_of_month as day_of_month,
  2 as num_flights, tia.first_carrier as first_carrier, tia.second_carrier as second_carrier,
  ft1.origin as origin, ft2.destination as destination, 

  ft1.planned_departure_time as planned_departure_time, ft1.planned_departure_tz as planned_departure_tz, ft1.planned_departure_local_hour as planned_departure_local_hour,
  ft2.planned_arrival_time as planned_arrival_time, ft2.planned_arrival_tz as planned_arrival_tz, ft2.planned_arrival_local_hour as planned_arrival_local_hour,
 
  tia.first_flight_id as first_flight_id, tia.second_flight_id as second_flight_id,
  tia.passengers as passengers
from temp_itinerary_allocations tia
join flights ft1 on ft1.id = tia.first_flight_id
join flights ft2 on ft2.id = tia.second_flight_id;
-- 117036

-- The following index is used to support delay calculation
create index idx_itin_allocs_ymdm
  on itinerary_allocations(year, month, day_of_month);

create index idx_itin_allocs_c1c2ymdm
  on itinerary_allocations(first_carrier, second_carrier, year, month, day_of_month);

create index idx_itin_allocs_ft1ft2
  on itinerary_allocations(first_flight_id, second_flight_id);


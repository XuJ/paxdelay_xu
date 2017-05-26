drop table if exists continental_allocations;

create table continental_allocations
(
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  num_flights numeric(1) not null,
  first_carrier char(2) not null,
  second_carrier char(2),
  origin char(3) not null,
  destination char(3) not null,

  planned_departure_time_UTC date not null,
	planned_departure_tz char(19),
	planned_departure_local_hour numeric(2),

  planned_arrival_time_UTC date not null,
	planned_arrival_tz char(19),
	planned_arrival_local_hour numeric(2),

  first_flight_id numeric(12) not null,
  second_flight_id numeric(12),
  passengers numeric(4) not null
);

insert  into continental_allocations
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  1, cai.first_carrier, null, 
  ft1.origin, ft1.destination,

	ft1.planned_departure_time_UTC,
	ft1.planned_departure_tz,
	ft1.planned_departure_local_hour,

	ft1.planned_arrival_time_UTC,
	ft1.planned_arrival_tz,
	ft1.planned_arrival_local_hour,

  cai.first_flight_id, null,
  cai.passengers
from airline_itineraries cai
join flights ft1 on ft1.id = cai.first_flight_id
where 
	cai.num_flights = 1 
	and cai.first_flight_id is not null 
	and ft1.carrier in ('CO', 'XE')
union all
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  1, cai.first_carrier, null,
  ft1.origin, ft1.destination,
	ft1.planned_departure_time_UTC,
	ft1.planned_departure_tz,
	ft1.planned_departure_local_hour,

	ft1.planned_arrival_time_UTC,
	ft1.planned_arrival_tz,
	ft1.planned_arrival_local_hour,
  cai.first_flight_id, null,
  cai.passengers
from airline_itineraries cai
join flights ft1 on ft1.id = cai.first_flight_id
join flights ft2 on ft2.id = cai.second_flight_id
where 
	cai.num_flights = 2 
	and cai.first_flight_id is not null 
	and cai.second_flight_id is not null 
	and ft1.carrier in ('CO', 'XE') 
	and ft2.carrier not in ('CO', 'XE')
union all
select ft2.year, ft2.quarter, ft2.month, ft2.day_of_month,
  1, cai.second_carrier, null,
  ft2.origin, ft2.destination,
	ft2.planned_departure_time_UTC,
	ft2.planned_departure_tz,
	ft2.planned_departure_local_hour,

	ft2.planned_arrival_time_UTC,
	ft2.planned_arrival_tz,
	ft2.planned_arrival_local_hour,
  cai.second_flight_id, null,
  cai.passengers
from airline_itineraries cai
join flights ft1 on ft1.id = cai.first_flight_id
join flights ft2 on ft2.id = cai.second_flight_id
where cai.num_flights = 2 and cai.first_flight_id is not null and cai.second_flight_id is not null and ft1.carrier not in ('CO', 'XE') and ft2.carrier in ('CO', 'XE')
union all
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  2, cai.first_carrier, cai.second_carrier,
  ft1.origin, ft2.destination, 
	ft1.planned_departure_time_UTC,
	ft1.planned_departure_tz,
	ft1.planned_departure_local_hour,

	ft2.planned_arrival_time_UTC,
	ft2.planned_arrival_tz,
	ft2.planned_arrival_local_hour,
  cai.first_flight_id, cai.second_flight_id,
  cai.passengers
from airline_itineraries cai
join flights ft1 on ft1.id = cai.first_flight_id
join flights ft2 on ft2.id = cai.second_flight_id
where 
	cai.num_flights = 2 
	and cai.first_flight_id is not null 
	and cai.second_flight_id is not null 
	and ft1.carrier in ('CO', 'XE') 
	and ft2.carrier in ('CO', 'XE');

-- The following index is used to support delay calculation
create index idx_contin_allocs_yqmdm
  on continental_allocations(year, quarter, month, day_of_month);

create index idx_contin_allocs_c1c2yqmdm
  on continental_allocations(first_carrier, second_carrier, year, quarter, month, day_of_month);

create index idx_contin_allocs_ft1ft2
  on continental_allocations(first_flight_id, second_flight_id);

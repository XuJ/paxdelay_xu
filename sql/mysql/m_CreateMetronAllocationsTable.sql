drop table if exists metron_allocations;

create table metron_allocations
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

  planned_departure_time_UTC date,
  planned_departure_tz char(19),
  planned_departure_local_hour numeric(2),

  planned_arrival_time_UTC date,
  planned_arrival_tz char(19),
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

insert into metron_allocations
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  1, tia.first_carrier, null, 
  ft1.origin, ft1.destination,
  ft1.planned_departure_time_UTC,
  ft1.planned_departure_tz,
  ft1.planned_departure_local_hour,

  ft1.planned_arrival_time_UTC,
  ft1.planned_arrival_tz,
  ft1.planned_arrival_local_hour,

  tia.first_flight_id, null,
  tia.passengers
from temp_metron_allocations tia
join metron_flights ft1 
	on ft1.id = tia.first_flight_id
where tia.second_flight_id is null 
	or tia.second_flight_id = ""
union all
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  2, tia.first_carrier, tia.second_carrier,
  ft1.origin, ft2.destination, 
  ft1.planned_departure_time_UTC,
  ft1.planned_departure_tz,
  ft1.planned_departure_local_hour,

  ft2.planned_arrival_time_UTC,
  ft2.planned_arrival_tz,
  ft2.planned_arrival_local_hour,
  tia.first_flight_id, tia.second_flight_id,
  tia.passengers
from temp_metron_allocations tia
join metron_flights ft1 
	on ft1.id = tia.first_flight_id
join metron_flights ft2 
	on ft2.id = tia.second_flight_id;
-- 132,704

-- The following index is used to support delay calculation
create index idx_metitin_allocs_ymdm
  on metron_allocations(year, month, day_of_month);

create index idx_metitin_allocs_c1c2ymdm
  on metron_allocations(first_carrier, second_carrier, year, month, day_of_month);

create index idx_metitin_allocs_ft1ft2
  on metron_allocations(first_flight_id, second_flight_id);
drop table temp_itinerary_allocations;

create table temp_itinerary_allocations
(
  first_carrier char(2) not null,
  second_carrier char(2),
  first_flight_id number(12, 0) not null,
  second_flight_id number(12, 0),
  passengers number(4, 0) not null
);

create table itinerary_allocations
(
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  day_of_month number(2, 0) not null,
  num_flights number(1, 0) not null,
  first_carrier char(2) not null,
  second_carrier char(2),
  origin char(3) not null,
  destination char(3) not null,
  planned_departure_time timestamp with time zone not null,
  planned_arrival_time timestamp with time zone not null,
  first_flight_id number(12, 0) not null,
  second_flight_id number(12, 0),
  passengers number(4, 0) not null
)
partition by list (quarter)
(partition p_q1 values (1),
  partition p_q2 values (2),
  partition p_q3 values (3),
  partition p_q4 values (4)
);

insert /*+ append */ into itinerary_allocations
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  1, tia.first_carrier, null, 
  ft1.origin, ft1.destination,
  ft1.planned_departure_time, ft1.planned_arrival_time,
  tia.first_flight_id, null,
  tia.passengers
from temp_itinerary_allocations tia
join flights ft1
  on ft1.id = tia.first_flight_id
where tia.second_flight_id is null
union all
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  2, tia.first_carrier, tia.second_carrier,
  ft1.origin, ft2.destination, 
  ft1.planned_departure_time, ft2.planned_arrival_time,
  tia.first_flight_id, tia.second_flight_id,
  tia.passengers
from temp_itinerary_allocations tia
join flights ft1
  on ft1.id = tia.first_flight_id
join flights ft2
  on ft2.id = tia.second_flight_id;

commit;

-- The following index is used to support delay calculation
create bitmap index bmx_itin_allocs_ymdm
  on itinerary_allocations(year, month, day_of_month)
  local
  tablespace users;

create index idx_itin_allocs_c1c2ymdm
  on itinerary_allocations(first_carrier, second_carrier, 
    year, month, day_of_month)
  local
  tablespace users;

create index idx_itin_allocs_ft1ft2
  on itinerary_allocations(first_flight_id, second_flight_id)
  tablespace users;

commit;

-- Rename table and indices to create multiple samples
alter table itinerary_allocations
rename to itinerary_allocations_62;

alter index bmx_itin_allocs_ymdm
rename to bmx_itin_allocs_62_ymdm;

alter index idx_itin_allocs_c1c2ymdm
rename to idx_itin_allocs_62_c1c2ymdm;

alter index idx_itin_allocs_ft1ft2
rename to idx_itin_allocs_62_ft1ft2;

alter table itinerary_allocations
rename to randomized_allocations;

alter index bmx_itin_allocs_ymdm
rename to bmx_random_allocs_6_ymdm;

alter index idx_itin_allocs_c1c2ymdm
rename to idx_random_allocs_6_c1c2ymdm;

alter index idx_itin_allocs_ft1ft2
rename to idx_random_allocs_6_ft1ft2;

commit;


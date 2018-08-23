create table temp_route_demands
(
  year not null,
  quarter not null,
  market_id not null,
  passengers not null,
  distance not null,
  first_ucs_id not null,
  last_ucs_id not null,
  total_ucs_id not null,
  num_flights not null
) as
select 
  db1b.year, db1b.quarter, db1b.market_id,
    avg(db1b.passengers),
    sum(db1b.distance),
    sum(ucs.id) keep (dense_rank first order by db1b.sequence_number),
    sum(ucs.id) keep (dense_rank last order by db1b.sequence_number),
    sum(ucs.id),
    count(db1b.sequence_number)
from db1b_unique_carrier_segments ucs
join db1b_coupons db1b
  on db1b.quarter = ucs.quarter
  and db1b.ticketing_carrier = ucs.ticketing_carrier
  and db1b.operating_carrier = ucs.operating_carrier
  and db1b.origin = ucs.origin
  and db1b.destination = ucs.destination
group by db1b.year, db1b.quarter, db1b.market_id
having count(db1b.sequence_number) <= 3;

create index idx_temp_rd_qucs
  on temp_route_demands(quarter, first_ucs_id, last_ucs_id)
  tablespace users;

-- Multiply the number of passengers by 10 to account for the
-- 10% sampling that occurs in DB1B
create table ticketed_route_demands
(
  year not null,
  quarter not null,
  first_ticketing_carrier not null,
  second_ticketing_carrier,
  num_flights not null,
  origin not null,
  connection,
  destination not null,
  first_operating_carrier not null,
  second_operating_carrier,
  passengers not null
)
as
select tin.year, tin.quarter, 
  first.ticketing_carrier, null, 1,
  first.origin, null, first.destination, 
  first.operating_carrier, null,
  tin.passengers
from db1b_unique_carrier_segments first
join 
(select trd.year, trd.quarter,
   trd.first_ucs_id,
   10 * sum(trd.passengers) as passengers
 from temp_route_demands trd
 where trd.num_flights = 1
 group by trd.year, trd.quarter,
   trd.first_ucs_id) tin
on tin.first_ucs_id = first.id
union all
select tin.year, tin.quarter,
  first.ticketing_carrier, second.ticketing_carrier, 2,
  first.origin, first.destination, second.destination, 
  first.operating_carrier, second.operating_carrier,
  tin.passengers
from
(select trd.year, trd.quarter,
   trd.first_ucs_id, trd.last_ucs_id,
   10 * sum(trd.passengers) as passengers
 from temp_route_demands trd
 where trd.num_flights = 2
 group by trd.year, trd.quarter,
   trd.first_ucs_id, trd.last_ucs_id) tin
join db1b_unique_carrier_segments first
  on first.id = tin.first_ucs_id
join db1b_unique_carrier_segments second
  on second.id = tin.last_ucs_id
  and second.origin = first.destination;

create table route_demands
(
  year not null,
  quarter not null,
  num_flights not null,
  origin not null,
  connection,
  destination not null,
  first_operating_carrier not null,
  second_operating_carrier,
  passengers not null
)
as
select trd.year, trd.quarter, trd.num_flights,
  trd.origin, trd.connection, trd.destination,
  trd.first_operating_carrier, trd.second_operating_carrier,
  sum(trd.passengers) as passengers
from ticketed_route_demands trd
group by trd.year, trd.quarter, trd.num_flights,
  trd.origin, trd.connection, trd.destination,
  trd.first_operating_carrier, trd.second_operating_carrier;

-- General indices for querying route demands
create index idx_route_demands_c1yqodnf
  on route_demands(year, quarter, num_flights,
    first_operating_carrier, origin, destination)
  tablespace users;

create index idx_route_demands_c1c2yqodc
  on route_demands(year, quarter, 
    first_operating_carrier, second_operating_carrier,
    origin, destination, connection)
  tablespace users;

-- The following two indices are used by PAP
create bitmap index bm_idx_route_demands_c1yq
  on route_demands(first_operating_carrier, year, quarter)
  tablespace users;

create bitmap index bm_idx_route_demands_c2yq
  on route_demands(second_operating_carrier, year, quarter)
  tablespace users;

-- The following index is used by itinerary generation
create index idx_route_demands_c1qy
  on route_demands(first_operating_carrier, quarter, year)
  tablespace users;

drop table temp_route_demands;

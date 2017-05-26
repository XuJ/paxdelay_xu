-- Creation of the co_db1b_route_demands table (ported from CreateCODB1BRouteDemandsTable.sql)
drop table if exists co_db1b_route_demands;

create table co_db1b_route_demands
(
  year decimal(4,0) not null,
  quarter int(11) not null,
  month int(2) not null,
  num_flights int(11) not null,
  origin char(3) binary not null,
  connection char(3) binary null,
  destination char(3) binary not null,
  first_operating_carrier varchar(3) binary not null,
  second_operating_carrier varchar(3) binary null,
  passengers decimal(4,0) not null
)
engine = innodb; 

insert into co_db1b_route_demands
select drd.year, drd.quarter, das.month, 1,
  drd.origin, null, drd.destination,
  drd.first_operating_carrier, null,
  das.scaling_factor * drd.passengers
from db1b_route_demands drd
join db1b_co_segment_factors das
on das.year = drd.year
  and das.quarter = drd.quarter
  and das.carrier = drd.first_operating_carrier
  and das.origin = drd.origin
  and das.destination = drd.destination
where drd.num_flights = 1;
-- 770

insert into co_db1b_route_demands
select drd.year, drd.quarter, das1.month, 2,
  drd.origin, drd.connection, drd.destination,
  drd.first_operating_carrier, drd.second_operating_carrier,
  least(das1.scaling_factor, das2.scaling_factor) * drd.passengers
from db1b_route_demands drd
join db1b_co_segment_factors das1
on das1.year = drd.year
  and das1.quarter = drd.quarter
  and das1.carrier = drd.first_operating_carrier
  and das1.origin = drd.origin
  and das1.destination = drd.connection
join db1b_co_segment_factors das2
on das2.year = drd.year
  and das2.quarter = drd.quarter
  and das2.month = das1.month
  and das2.carrier = drd.second_operating_carrier
  and das2.origin = drd.connection
  and das2.destination = drd.destination
where drd.num_flights = 2;
-- 10,394
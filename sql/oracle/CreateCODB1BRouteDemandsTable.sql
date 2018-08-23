drop table co_db1b_route_demands;

create table co_db1b_route_demands
(
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  num_flights number(1, 0) not null,
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  first_operating_carrier varchar2(3) not null,
  second_operating_carrier varchar2(3),
  passengers number(10, 4) not null
);

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

commit;

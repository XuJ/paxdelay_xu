drop table t100_db1b_route_demands;

create table t100_db1b_route_demands
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

insert into t100_db1b_route_demands
select drd.year, drd.quarter, dtc.month, 1,
  drd.origin, null, drd.destination,
  drd.first_operating_carrier, null,
  dtc.scaling_factor * drd.passengers
from db1b_route_demands drd
join db1b_t100_segment_comparisons dtc
on dtc.year = drd.year
  and dtc.quarter = drd.quarter
  and dtc.carrier = drd.first_operating_carrier
  and dtc.origin = drd.origin
  and dtc.destination = drd.destination
where drd.num_flights = 1;

insert into t100_db1b_route_demands
select drd.year, drd.quarter, dtc1.month, 2,
  drd.origin, drd.connection, drd.destination,
  drd.first_operating_carrier, drd.second_operating_carrier,
  least(dtc1.scaling_factor, dtc2.scaling_factor) * drd.passengers
from db1b_route_demands drd
join db1b_t100_segment_comparisons dtc1
on dtc1.year = drd.year
  and dtc1.quarter = drd.quarter
  and dtc1.carrier = drd.first_operating_carrier
  and dtc1.origin = drd.origin
  and dtc1.destination = drd.connection
join db1b_t100_segment_comparisons dtc2
on dtc2.year = drd.year
  and dtc2.quarter = drd.quarter
  and dtc2.month = dtc1.month
  and dtc2.carrier = drd.second_operating_carrier
  and dtc2.origin = drd.connection
  and dtc2.destination = drd.destination
where drd.num_flights = 2;

commit;

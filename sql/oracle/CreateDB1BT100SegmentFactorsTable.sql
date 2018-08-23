drop table db1b_t100_segment_factors;

create table db1b_t100_segment_factors
(
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  carrier varchar2(3) not null,
  origin char(3) not null,
  destination char(3) not null,
  db1b_passengers number(6, 0) not null,
  t100_passengers number(6, 0) not null,
  scaling_factor number(8, 4) not null
);

insert into db1b_t100_segment_factors
select dseg.year, dseg.quarter, t100.month,
  dseg.carrier, dseg.origin, dseg.destination,
  dseg.passengers, t100.passengers,
  decode(dseg.passengers, 0, 0,
    t100.passengers / dseg.passengers)
from
(
 select year, quarter, carrier, origin, destination,
   sum(passengers) as passengers
 from
 (
  select year, quarter, first_operating_carrier as carrier,
    origin, destination, passengers
  from db1b_route_demands
  where num_flights = 1
  union all
  select year, quarter, first_operating_carrier as carrier,
    origin, connection as destination, passengers
  from db1b_route_demands
  where num_flights = 2
  union all
  select year, quarter, second_operating_carrier as carrier,
    connection as origin, destination, passengers
  from db1b_route_demands
  where num_flights = 2
 )
 group by year, quarter, carrier, origin, destination
) dseg
join
(
 select year, quarter, month, carrier, origin, destination,
   sum(passengers) as passengers
 from t100_segments
 group by year, quarter, month, carrier, origin, destination
) t100
on t100.year = dseg.year
  and t100.quarter = dseg.quarter
  and t100.carrier = dseg.carrier
  and t100.origin = dseg.origin
  and t100.destination = dseg.destination;

-- For testing purposes, the results should be about 4.0
select sum(t100_passengers) / sum(db1b_passengers)
from db1b_t100_segment_factors;


drop table if exists db1b_co_segment_factors;

create table db1b_co_segment_factors
(
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  carrier char(6) not null,
  origin char(3) not null,
  destination char(3) not null,
  db1b_passengers numeric(6) not null,
  continental_passengers numeric(6) not null,
  scaling_factor numeric(8, 4) not null
);

insert into db1b_co_segment_factors
select dseg.year, dseg.quarter, co.month,
  dseg.carrier, dseg.origin, dseg.destination,
  dseg.passengers, co.passengers,
	case when dseg.passengers = 0 then 0 else co.passengers / dseg.passengers end
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
 ) t 
 group by t.year, t.quarter, t.carrier, t.origin, t.destination
) dseg
join continental_segments co
on co.year = dseg.year
  and co.quarter = dseg.quarter
  and co.carrier = dseg.carrier
  and co.origin = dseg.origin
  and co.destination = dseg.destination;
-- 771

-- For testing purposes, the results should be about 3.3
select sum(continental_passengers) / sum(db1b_passengers)
from db1b_co_segment_factors;


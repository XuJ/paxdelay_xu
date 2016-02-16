drop table scaled_segment_comparisons;

create table scaled_segment_comparisons
(
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  carrier varchar2(3) not null,
  origin char(3) not null,
  destination char(3) not null,
  scaled_db1b_passengers number(6, 0) not null,
  t100_passengers number(6, 0) not null
);

insert into scaled_segment_comparisons
select sseg.year, sseg.quarter, sseg.month,
  sseg.carrier, sseg.origin, sseg.destination,
  sseg.passengers, t100.passengers
from
(
 select year, quarter, month, carrier, origin, destination,
   sum(passengers) as passengers
 from
 (
  select year, quarter, month, first_operating_carrier as carrier,
    origin, destination, passengers
  from scaled_db1b_route_demands
  where num_flights = 1
  union all
  select year, quarter, month, first_operating_carrier as carrier,
    origin, connection as destination, passengers
  from scaled_db1b_route_demands
  where num_flights = 2
  union all
  select year, quarter, month, second_operating_carrier as carrier,
    connection as origin, destination, passengers
  from scaled_db1b_route_demands
  where num_flights = 2
 )
 group by year, quarter, month, carrier, origin, destination
) sseg
join
(
 select year, quarter, month, carrier, origin, destination,
   sum(passengers) as passengers
 from t100_segments
 group by year, quarter, month, carrier, origin, destination
) t100
on t100.year = sseg.year
  and t100.quarter = sseg.quarter
  and t100.month = sseg.month
  and t100.carrier = sseg.carrier
  and t100.origin = sseg.origin
  and t100.destination = sseg.destination;

select sum(t100_passengers) / sum(scaled_db1b_passengers)
from scaled_segment_comparisons;

select max(abs(t100_passengers - scaled_db1b_passengers) / t100_passengers)
from scaled_segment_comparisons
where t100_passengers > 0;

select count(*)
from scaled_segment_comparisons;

select count(*)
from scaled_segment_comparisons
where abs(t100_passengers - scaled_db1b_passengers) / t100_passengers > 0.50
and t100_passengers > 0;


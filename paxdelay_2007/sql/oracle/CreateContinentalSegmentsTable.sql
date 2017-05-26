create table continental_segments
(
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  carrier varchar2(3) not null,
  origin char(3) not null,
  destination char(3) not null,
  passengers number(6, 0) not null
);

insert into continental_segments
select ca.year, ca.quarter, ca.month,
  ca.carrier, ca.origin, ca.destination,
  sum(ca.passengers)
from (
 select f.year, f.quarter, f.month,
   f.carrier, f.origin, f.destination, 
   c.passengers
 from continental_allocations c
 join flights f
 on f.id = c.first_flight_id
 where c.num_flights = 1
 union all
 select f.year, f.quarter, f.month,
   f.carrier, f.origin, f.destination,
   c.passengers
 from continental_allocations c
 join flights f
 on f.id = c.first_flight_id
 where c.num_flights = 2
 union all
 select f.year, f.quarter, f.month,
   f.carrier, f.origin, f.destination,
   c.passengers
 from continental_allocations c
 join flights f
 on f.id = c.second_flight_id
 where c.num_flights = 2
) ca
group by ca.year, ca.quarter, ca.month,
ca.carrier, ca.origin, ca.destination;

commit;

create bitmap index bm_idx_continental_segments_cym
  on continental_segments(carrier, year, month)
  tablespace users;

commit;


create table temp_ca
select 
	f.year, 
	f.quarter, 
	f.month,
	f.carrier, 
	f.origin, 
	f.destination, 
	c.passengers
from continental_allocations c
join flights f 
	on f.id = c.first_flight_id
where c.num_flights = 1
union all
select 
	f.year, 
	f.quarter, 
	f.month,
	f.carrier, 
	f.origin, 
	f.destination,
	c.passengers
from continental_allocations c
join flights f 
	on f.id = c.first_flight_id
where c.num_flights = 2
union all
select 
	f.year, 
	f.quarter, 
	f.month,
	f.carrier, 
	f.origin, 
	f.destination,
	c.passengers
from continental_allocations c
join flights f 
	on f.id = c.second_flight_id
where c.num_flights = 2;
-- 1,205,070

drop table if exists continental_segments;
create table continental_segments
(
	year numeric(4) not null,
	quarter int not null,
	month numeric(2) not null,
	carrier char(6) not null,
	origin char(3) not null,
	destination char(3) not null,
	passengers numeric(6) not null
);

insert into continental_segments
select 
	ca.year, 
	ca.quarter, 
	ca.month,
	ca.carrier, 
	ca.origin, 
	ca.destination,
	sum(ca.passengers)
from temp_ca ca
group by ca.year, ca.quarter, ca.month, ca.carrier, ca.origin, ca.destination;

create index idx_continental_segments_cym
  on continental_segments(carrier, year, month);

drop table temp_ca;
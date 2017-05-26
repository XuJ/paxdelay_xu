drop table if exists db1b_t100_segment_factors;

create table db1b_t100_segment_factors
(
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  carrier char(6) not null,
  origin char(3) not null,
  destination char(3) not null,
  db1b_passengers numeric(6) not null,
  t100_passengers numeric(6) not null,
  scaling_factor numeric(8, 4) not null
);

create table temp_t
select 	year, 
	quarter, 
	first_operating_carrier as carrier, 
	origin, 
	destination, 
	passengers
from db1b_route_demands
where num_flights = 1
union all
select 	year, 
	quarter, 
	first_operating_carrier as carrier, 
	origin, 
	connection as destination, 
	passengers
from db1b_route_demands
where num_flights = 2
union all
select 	year, 
	quarter, 
	second_operating_carrier as carrier,  
	connection as origin, 
	destination, 
	passengers
from db1b_route_demands
where num_flights = 2;
  
create table temp_t100
select 	year, 
	quarter, 
	month, 
	carrier, 
	origin, 
	destination, 
	sum(passengers) as passengers
from t100_segments
group by year, quarter, month, carrier, origin, destination;

create table temp_dseg
select year, quarter, carrier, origin, destination, sum(passengers) as passengers
	 from temp_t t 
	 group by t.year, t.quarter, t.carrier, t.origin, t.destination;
	
create index idx_temp_t100
  on temp_t100(quarter, carrier, origin, destination);
  
create index idx_temp_dseg
  on temp_dseg(quarter, carrier, origin, destination);  
	
insert into db1b_t100_segment_factors
select dseg.year, dseg.quarter, t100.month,
  dseg.carrier, dseg.origin, dseg.destination,
  dseg.passengers, t100.passengers,
	case when dseg.passengers = 0 then 0 else t100.passengers / dseg.passengers end
from temp_dseg dseg
join temp_t100 t100
on t100.year = dseg.year
  and t100.quarter = dseg.quarter
  and t100.carrier = dseg.carrier
  and t100.origin = dseg.origin
  and t100.destination = dseg.destination;
-- 119058
  
drop table temp_t;
drop table temp_t100;
drop table temp_dseg;

-- For testing purposes, the results should be about 4.0
select sum(t100_passengers) / sum(db1b_passengers)
from db1b_t100_segment_factors;
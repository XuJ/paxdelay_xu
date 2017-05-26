drop table if exists db1b_t100_segment_comparisons;
create table db1b_t100_segment_comparisons
(
  year decimal(4,0) not null,
  quarter int(11) not null,
  month int(2) not null,
  carrier varchar(3) not null,
  origin char(3) not null,
  destination char(3) not null,
  scaling_factor decimal(8, 4)
);

drop table if exists temp_three_union;
create table temp_three_union
select 
	year, 
	quarter, 
	first_operating_carrier as carrier,
	origin, 
	destination, 
	sum(passengers) as passengers
from db1b_route_demands
where num_flights = 1
group by year, quarter, first_operating_carrier, origin, destination

union		
select 
	year, 
	quarter, 
	first_operating_carrier as carrier,
	origin, 
	connection, 
	sum(passengers) as passengers
from db1b_route_demands
where num_flights = 2
group by year, quarter, first_operating_carrier, origin, destination

union	
select 
	year, 
	quarter, 
	second_operating_carrier as carrier,
	connection, 
	destination, 
	sum(passengers) as passengers
from db1b_route_demands
where num_flights = 2
group by year, quarter, second_operating_carrier, origin, destination;
--748,700

drop table if exists temp_db1b_1;
create table temp_db1b_1
select 
	year, 
	quarter, 
	carrier, 
	origin, 
	destination,
	sum(passengers) as passengers
from temp_three_union 
group by year, quarter, carrier, origin, destination;
--102,785

create index idx_temp_db1_1
 on temp_db1b_1(quarter, carrier, origin, destination);

drop table if exists temp_t100_1;
create table temp_t100_1
select 
	year, 
	quarter, 
	month, 
	carrier, 
	origin, 
	destination,
	sum(passengers) as passengers
from t100_segments
group by year, quarter, month, carrier, origin, destination;
--237,560

create index idx_temp_db1_2
 on temp_t100_1(quarter, carrier, origin, destination);

insert into db1b_t100_segment_comparisons
select 
	db1b.year, 
	db1b.quarter, 
	t100.month,
	db1b.carrier, 
	db1b.origin, 
	db1b.destination,
	t100.passengers / db1b.passengers as scaling_factor
from temp_db1b_1 db1b
join temp_t100_1 t100
on db1b.year = t100.year
	and db1b.quarter 	= t100.quarter
	and db1b.carrier 	= t100.carrier
	and db1b.origin 	= t100.origin
	and db1b.destination 	= t100.destination;

drop table temp_three_union;
drop table temp_db1b_1;
drop table temp_t100_1;

select count(*) from db1b_t100_segment_comparisons;	
drop table if exists temp_route_demands;

create table temp_route_demands
select  gc.year, gc.quarter,	gc.market_id,
gc.passengers, gc.distance,
gc.total_ucs_id, 
sum(gc.ucs_id_max) as last_ucs_id, 
sum(gc.ucs_id_min) as first_ucs_id,
gc.num_flights
	from (
			select  j.year, j.quarter,	j.market_id,  
			avg(j.passengers) as passengers,
			sum(j.distance) as distance,
			sum(j.ucs_id) as total_ucs_id,
			convert(SUBSTRING_INDEX(group_concat(j.ucs_id order by j.sequence_number desc),',',1 ),decimal) ucs_id_max,
			convert(SUBSTRING_INDEX(group_concat(j.ucs_id order by j.sequence_number),',',1 ),decimal) ucs_id_min,
			count(j.sequence_number) as num_flights
			from (
					select 
					db1b.year as year,
					db1b.quarter as quarter,
					db1b.market_id as market_id,
					db1b.sequence_number as sequence_number,
					db1b.passengers as passengers,
					db1b.distance as distance,
					ucs.id as ucs_id
					from db1b_unique_carrier_segments ucs
					join db1b_coupons db1b
						on db1b.quarter = ucs.quarter
						and db1b.ticketing_carrier = ucs.ticketing_carrier
						and db1b.operating_carrier = ucs.operating_carrier
						and db1b.origin = ucs.origin
						and db1b.destination = ucs.destination
				) j
		group by j.year, j.quarter,	j.market_id
		having count(j.sequence_number) <= 3
	) gc
	group by  gc.year, gc.quarter,	gc.market_id;

create index idx_temp_rd_yqucs
  on temp_route_demands(year, quarter, first_ucs_id, last_ucs_id);
-- 20,671,839

drop table if exists ticketed_route_demands;
-- Multiply the number of passengers by 10 to account for the
-- 10% sampling that occurs in DB1B
create table ticketed_route_demands
(
  year numeric(4) not null,
  quarter int not null,
  first_ticketing_carrier varchar(3) not null,
  second_ticketing_carrier varchar(3),
  num_flights int not null,
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  first_operating_carrier varchar(3) not null,
  second_operating_carrier varchar(3) ,
  passengers numeric(4) not null
)
ENGINE = MyISAM;

insert into ticketed_route_demands
(
 year, quarter, first_ticketing_carrier, second_ticketing_carrier, num_flights, origin, connection, destination,
  first_operating_carrier, second_operating_carrier, passengers
)
select tin.year, tin.quarter, first.ticketing_carrier, null, 1,
  first.origin, null, first.destination, first.operating_carrier, 
	null, tin.passengers
from db1b_unique_carrier_segments first
join 
(select trd.year, trd.quarter,
   trd.first_ucs_id,
   10 * sum(trd.passengers) as passengers
 from temp_route_demands trd
 where trd.num_flights = 1
 group by trd.year, trd.quarter,
   trd.first_ucs_id) tin
on tin.first_ucs_id = first.id
union all
select tin.year, tin.quarter,
  first.ticketing_carrier, second.ticketing_carrier, 2,
  first.origin, first.destination, second.destination, 
  first.operating_carrier, second.operating_carrier,
  tin.passengers
from
(select trd.year, trd.quarter,
   trd.first_ucs_id, trd.last_ucs_id,
   10 * sum(trd.passengers) as passengers
 from temp_route_demands trd
 where trd.num_flights = 2
 group by trd.year, trd.quarter,
   trd.first_ucs_id, trd.last_ucs_id) tin
join db1b_unique_carrier_segments first
  on first.id = tin.first_ucs_id
join db1b_unique_carrier_segments second
  on second.id = tin.last_ucs_id
  and second.origin = first.destination;
-- 1,604,797


drop table if exists route_demands;

create table route_demands
(
  year numeric(4) not null,
  quarter int not null,
  num_flights int not null,
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  first_operating_carrier varchar(3) not null,
  second_operating_carrier varchar(3),
  passengers numeric(4) not null
)
ENGINE = MyISAM;


insert into route_demands
(year, quarter, num_flights, origin, connection, destination, first_operating_carrier, second_operating_carrier, passengers)
select trd.year, trd.quarter, trd.num_flights,
  trd.origin, trd.connection, trd.destination,
  trd.first_operating_carrier, trd.second_operating_carrier,
  sum(trd.passengers) as passengers
from ticketed_route_demands trd
group by trd.year, trd.quarter, trd.num_flights,
  trd.origin, trd.connection, trd.destination,
  trd.first_operating_carrier, trd.second_operating_carrier;
-- 1,499,404

-- General indices for querying route demands
create index idx_route_demands_c1yqodnf
  on route_demands(year, quarter, num_flights, first_operating_carrier, origin, destination);

create index idx_route_demands_c1c2yqodc
  on route_demands(year, quarter, 
    first_operating_carrier, second_operating_carrier, origin, destination, connection);

-- The following two indices are used by PAP
create index idx_route_demands_c1yq
  on route_demands(first_operating_carrier, year, quarter);

create index idx_route_demands_c2yq
  on route_demands(second_operating_carrier, year, quarter);

-- The following index is used by itinerary generation
create index idx_route_demands_c1qy
  on route_demands(first_operating_carrier, quarter, year);

drop table if exists temp_route_demands;
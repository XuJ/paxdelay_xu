drop table if exists carrier_operations;

create table carrier_operations
(
  carrier char(6) not null,
  airport char(3) not null,
  number_operations numeric(8) not null,
  percent_operations numeric(5,4) not null
);

insert into carrier_operations
(
  carrier,
  airport,
  number_operations,
  percent_operations
)
select 
	ops.carrier as carrier,
	arr.airport as airport,
	(arr.arrivals + dep.departures) as number_operations,
	(arr.arrivals + dep.departures) / ops.operations as percent_operations
from 
	(select carrier,(2 * count(*)) as operations
	 from flights
	 group by carrier) ops
join
	(select carrier, origin as airport, count(*) as arrivals
	 from flights
	 group by carrier, origin) arr on arr.carrier = ops.carrier
join
	(select carrier, destination as airport, count(*) as departures
	 from flights
	 group by carrier, destination) dep on dep.carrier = ops.carrier and dep.airport = arr.airport;
-- 1,786
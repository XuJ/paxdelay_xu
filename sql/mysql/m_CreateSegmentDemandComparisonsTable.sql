drop table if exists segment_demand_comparisons;

create table segment_demand_comparisons
(
  carrier char(6) not null,
  origin char(3) not null,
  destination char(3) not null,
  year numeric(4) not null,
  month numeric(2) not null,
  t100_demand numeric(8) not null,
  t100_capacity numeric(8) not null,
  proprietary_demand numeric(8)
);

create table temp_100
select t100.carrier, t100.origin, t100.destination,
   t100.year, t100.month, sum(t100.passengers) as passengers,
   sum(t100.seats) as capacity
 from t100_segments t100
 where t100.carrier = 'CO' and t100.year = 2007 and t100.quarter = 4
 group by t100.carrier, t100.origin, t100.destination, t100.year, t100.month;

create table temp_it
select cof.carrier, cof.origin, cof.destination,
    month(coi.departure_date) as month,
    coi.number_flown as passengers
  from continental_flight_legs cof
  join continental_itineraries coi on coi.itinerary_id = cof.itinerary_id
  where cof.carrier = 'CO';
 
insert into segment_demand_comparisons
(carrier, origin, destination, year, month, t100_demand, t100_capacity, proprietary_demand)
select t100.carrier, t100.origin, t100.destination,
  t100.year, t100.month, t100.passengers as t100_demand, t100.capacity as t100_capacity,
  it.passengers as proprietary_demand
from temp_100 t100
join
	(
	 select it.carrier, it.origin, it.destination,
	   it.month, sum(it.passengers) as passengers
	 from temp_it it
	 group by it.carrier, it.origin, it.destination, it.month
	) it
on it.carrier = t100.carrier 
and it.origin = t100.origin 
and it.destination = t100.destination 
and it.month = t100.month;
-- 837

drop table temp_100;
drop table temp_it;

select it.*
from
(
 select it.carrier, it.origin, it.destination,
   it.month, sum(it.passengers) as passengers
 from
 (
  select cof.carrier, cof.origin, cof.destination,
		month(coi.departure_date) as month,
    coi.number_flown as passengers
  from continental_flight_legs cof
  join continental_itineraries coi on coi.itinerary_id = cof.itinerary_id
  where cof.carrier = 'CO'
 ) it
 group by it.carrier, it.origin, it.destination, it.month
) it limit 10;
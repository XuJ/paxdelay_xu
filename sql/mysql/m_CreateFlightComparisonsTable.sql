drop table if exists flight_comparisons;

create table flight_comparisons
(
  allocation_method varchar(20) not null,
  flight_id numeric(12) not null,
  number_passengers numeric(8, 4) not null,
  seating_capacity numeric(4),
  number_connecting numeric(8, 4) not null
);

insert into flight_comparisons
select 'Proprietary', ai.flight_id,
  sum(ai.number_passengers),
  ai.seating_capacity,
  sum(ai.number_connecting)
from
(
 select ai.first_flight_id as flight_id,
   f.seating_capacity,
   sum(ai.passengers) as number_passengers,
		sum(case when ai.num_flights = 2 then ai.passengers else 0 end) as number_connecting
 from airline_itineraries ai
 join flights f on f.id = ai.first_flight_id
 where ai.first_carrier = 'CO' and ai.first_flight_id is not null
 group by ai.first_flight_id, f.seating_capacity
 union all
 select ai.second_flight_id as flight_id,
   f.seating_capacity as seating_capacity,
   sum(ai.passengers) as number_passengers,
   0 as number_connecting
 from airline_itineraries ai
 join flights f on f.id = ai.second_flight_id
 where ai.second_carrier = 'C0' and ai.second_flight_id is not null
 group by ai.second_flight_id, f.seating_capacity
) ai
group by ai.flight_id, ai.seating_capacity;
-- 80,006

delete from flight_comparisons
where allocation_method != 'Proprietary';

insert into flight_comparisons
select ic.allocation_method,  ic.flight_id,
  sum(ic.number_passengers),
  ic.seating_capacity,
  sum(ic.number_connecting)
from
(
 select ic.allocation_method, 
   ic.first_flight_id as flight_id,
   ic.first_seating_capacity as seating_capacity,
   sum(ic.allocated_passengers) as number_passengers,
		sum(case when ic.num_flights = 2 then ic.allocated_passengers else 0 end) as number_connecting
 from itinerary_comparisons ic
 where ic.first_carrier = 'CO' and ic.first_flight_id is not null and ic.allocated_passengers is not null
 group by ic.allocation_method, ic.first_flight_id, ic.first_seating_capacity
 union all
 select ic.allocation_method, 
   ic.second_flight_id as flight_id,
   ic.second_seating_capacity as seating_capacity,
   sum(ic.allocated_passengers) as number_passengers,
   0 as number_connecting
 from itinerary_comparisons ic
 where ic.second_carrier = 'CO' and ic.second_flight_id is not null and ic.allocated_passengers is not null
 group by ic.allocation_method, ic.second_flight_id, ic.second_seating_capacity
) ic
group by ic.allocation_method, ic.flight_id, ic.seating_capacity;
-- 107,679
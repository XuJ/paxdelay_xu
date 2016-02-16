create table segment_demand_comparisons
(
  carrier varchar2(3) not null,
  origin char(3) not null,
  destination char(3) not null,
  year number(4, 0) not null,
  month number(2, 0) not null,
  t100_demand number(8, 0) not null,
  t100_capacity number(8, 0) not null,
  proprietary_demand number(8, 0)
);

insert into segment_demand_comparisons
select t100.carrier, t100.origin, t100.destination,
  t100.year, t100.month, t100.passengers, t100.capacity,
  it.passengers
from
(
 select t100.carrier, t100.origin, t100.destination,
   t100.year, t100.month, sum(t100.passengers) as passengers,
   sum(t100.seats) as capacity
 from t100_segments t100
 where t100.carrier = 'CO' and t100.year = 2007
   and t100.quarter = 4
 group by t100.carrier, t100.origin, t100.destination,
   t100.year, t100.month
) t100
join
(
 select it.carrier, it.origin, it.destination,
   it.month, sum(it.passengers) as passengers
 from
 (
  select cof.carrier, cof.origin, cof.destination,
    to_number(to_char(to_date(coi.departure_date, 'MM/DD/YYYY'), 'MM'), '00')
      as month,
    coi.number_flown as passengers
  from continental_flight_legs cof
  join continental_itineraries coi
    on coi.itinerary_id = cof.itinerary_id
  where cof.carrier = 'CO'
 ) it
 group by it.carrier, it.origin, it.destination,
   it.month
) it
on it.carrier = t100.carrier
  and it.origin = t100.origin
  and it.destination = t100.destination
  and it.month = t100.month;

select it.*
from
(
 select it.carrier, it.origin, it.destination,
   it.month, sum(it.passengers) as passengers
 from
 (
  select cof.carrier, cof.origin, cof.destination,
    to_number(to_char(to_date(coi.departure_date, 'MM/DD/YYYY'), 'MM'), '00')
      as month,
    coi.number_flown as passengers
  from continental_flight_legs cof
  join continental_itineraries coi
    on coi.itinerary_id = cof.itinerary_id
  where cof.carrier = 'CO'
 ) it
 group by it.carrier, it.origin, it.destination,
   it.month
) it
where rownum <= 10;


select t100.carrier, t100.origin, t100.destination,
  t100.year, t100.month, t100.passengers
from
(
 select t100.carrier, t100.origin, t100.destination,
   t100.year, t100.month, sum(t100.passengers) as passengers
 from t100
 where t100.carrier = 'CO' and t100.year = 2007
   and t100.quarter = 3
 group by t100.carrier, t100.origin, t100.destination,
   t100.year, t100.month
) t100
join
(
 select ct.carrier as carrier,
   cf.origin, cf.destination,
   decode(ai.num_flights, 2, ai.connection, ai.destination), 
   ai.year, ai.month
   sum(ai.passengers) as passengers
 from airline_itineraries ai
 where ai.carrier = 'CO' and ai.year = 2007
   and ai.quarter = 3
 group by ai.first_flight_carrier, ai.num_flights, ai.origin, 
   decode(ai.num_flights, 2, ai.connection, ai.destination),
   ai.year, ai.month
 union all
 select ai.second_flight_carrier as carrier,
   ai.origin, ai.destination, ai.year, ai.month
   sum(ai.passengers) as passengers
 from airline_itineraries ai
 where ai.carrier = 'CO' and ai.year = 2007
   and ai.quarter = 3
 group by ai.first_flight_carrier, ai.origin, ai.destination
   ai.year, ai.month

)
) ai

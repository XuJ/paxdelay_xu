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

select *
from segment_demand_comparisons
order by (proprietary_demand - t100_demand) desc;

select f.id, f.tail_number, f.carrier, f.origin, f.destination,
  to_char(f.planned_departure_time, 'MM/DD/YYYY HH24:MI')
    as planned_departure,
  to_char(f.planned_arrival_time, 'MM/DD/YYYY HH24:MI')
    as planned_arrival,
  f.icao_aircraft_code, f.seating_capacity, fc.number_passengers
from flight_comparisons fc
join flights f
  on f.id = fc.flight_id
where fc.allocation_method = 'Proprietary'
  and fc.number_passengers > f.seating_capacity
order by (fc.number_passengers - f.seating_capacity) desc;
    

to_char(planned_departure_time, 'MM/DD/YYYY HH24:MI')
  as planned_departure,
  to_char(planned_arrival_time, 'MM/DD/YYYY HH24:MI')
  as planned_arrival
from flights
where rownum <= 10;

select carrier_demands.passengers, t100_demands.passengers,
  t100_demands.carrier, t100_demands.origin, t100_demands.destination
from
(
 select sum(t100.passengers) as passengers, 
   t100.carrier, t100.origin, t100.destination
 from t100_segments t100
 where t100.year = 2007
   and t100.carrier = 'CO'
   and t100.quarter = 4
   and t100.month = 10
 group by t100.carrier, t100.origin, t100.destination
) t100_demands
join
(
select sum(tot.passengers) as passengers, tot.carrier,
  tot.origin, tot.destination
from
(
select sum(ait.passengers) as passengers, 
  ait.first_carrier as carrier,
  ait.origin, ait.destination
from airline_itineraries ait
join
(
 select sum(t100.passengers) as passengers, 
   t100.carrier, t100.origin, t100.destination
 from t100_segments t100
 where t100.year = 2007
   and t100.carrier = 'CO'
   and t100.quarter = 4
   and t100.month = 10
 group by t100.carrier, t100.origin, t100.destination
) t100_demands
on t100_demands.carrier = ait.first_carrier
  and t100_demands.origin = ait.origin
  and t100_demands.destination = ait.destination
where ait.num_flights = 1
  and ait.first_carrier = 'CO'
  and ait.year = 2007
  and ait.quarter = 4
  and ait.month = 10
group by ait.first_carrier, ait.origin, ait.destination
union all
select sum(ait.passengers), ait.first_carrier,
  ait.origin, ait.connection
from airline_itineraries ait
join
(
 select sum(t100.passengers) as passengers, 
   t100.carrier, t100.origin, t100.destination
 from t100_segments t100
 where t100.year = 2007
   and t100.carrier = 'CO'
   and t100.quarter = 4
   and t100.month = 10
 group by t100.carrier, t100.origin, t100.destination
) t100_demands
on t100_demands.carrier = ait.first_carrier
  and t100_demands.origin = ait.origin
  and t100_demands.destination = ait.connection
where ait.num_flights = 2
  and ait.first_carrier = 'CO'
  and ait.year = 2007
  and ait.quarter = 4
  and ait.month = 10
group by ait.first_carrier, ait.origin, ait.connection
union all
select sum(ait.passengers), ait.second_carrier,
  ait.connection, ait.destination
from airline_itineraries ait
join
(
 select sum(t100.passengers) as passengers, 
   t100.carrier, t100.origin, t100.destination
 from t100_segments t100
 where t100.year = 2007
   and t100.carrier = 'CO'
   and t100.quarter = 4
   and t100.month = 10
 group by t100.carrier, t100.origin, t100.destination
) t100_demands
on t100_demands.carrier = ait.second_carrier
  and t100_demands.origin = ait.connection
  and t100_demands.destination = ait.destination
where ait.num_flights = 2
  and ait.second_carrier = 'CO'
  and ait.year = 2007
  and ait.quarter = 4
  and ait.month = 10
group by ait.second_carrier, ait.connection, ait.destination
) tot
group by tot.carrier, tot.origin, tot.destination
) carrier_demands
on carrier_demands.carrier = t100_demands.carrier
  and carrier_demands.origin = t100_demands.origin
  and carrier_demands.destination = t100_demands.destination
  
-- Number of October flights based on Continental data
select count(*)
from
(
select distinct coi.departure_date, cof.departure_time
from continental_flight_legs cof
join continental_itineraries coi
  on coi.itinerary_id = cof.itinerary_id
where to_number(to_char(to_date(coi.departure_date, 'MM/DD/YYYY'), 'MM'), '00') = 10
  and cof.carrier = 'CO'
  and cof.origin = 'AUS'
  and cof.destination = 'IAH'
)

-- Number of October flights based on T100 data
select sum(departures_performed)
from t100_segments
where carrier = 'CO'
  and origin = 'AUS'
  and destination = 'IAH'
  and year = 2007
  and quarter = 4
  and month = 10;

from continental_flight_legs
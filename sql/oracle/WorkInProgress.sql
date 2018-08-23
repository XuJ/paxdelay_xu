select t100.carrier, sum(t100.passengers) as num_passengers
from t100_segments t100
group by t100.carrier
order by sum(t100.passengers) desc;

select count(*)
from temp_route_demands;

select sum(passengers)
from route_demands
where num_flights = 1
  and first_operating_carrier = '--';


create table temp_itinerary_first_legs
as
select f.year, f.quarter, f.month, f.day_of_month, f.day_of_week,
  f.hour_of_day, f.minutes_of_hour, r.first_operating_carrier,
  r.second_operating_carrier, r.origin, r.connection, r.destination,
  f.planned_departure_time, f.planned_arrival_time, f.id
from route_demands r
join flights f
  on f.year = 2007 and f.quarter = 4
  and f.carrier = r.first_operating_carrier
  and f.origin = r.origin
  and f.destination = r.connection
where r.num_flights = 2 and r.year = 2007
  and r.quarter = 4 
  and r.first_operating_carrier = 'DL';

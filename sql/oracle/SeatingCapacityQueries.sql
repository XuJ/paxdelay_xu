select ft.carrier, ft.iata_aircraft_code, count(*) as num_flights
from flights ft
group by ft.carrier, ft.iata_aircraft_code;

select t100.carrier, t100.aircraft_type,
  ceil(sum(t100.seats) / sum(t100.departures_performed)) as seats
from t100_segments t100
where t100.departures_performed > 0
  and t100.service_class = 'F'
  and t100.carrier in
    ('WN', 'AA', 'DL', 'UA', 'NW', 'US', 'CO', 'FL', 'OO', 'B6', 
     'MQ', 'AS', 'XE', 'HP', 'YV', 'EV', 'F9', '9E', 'OH', 'RP',
     'HA', 'QX', 'NK', 'ZW', '16')
group by t100.carrier, t100.aircraft_type;

select count(*)
from t100_segments
where departures_performed > 0
  and passengers = 0
  and carrier in 
   ('WN', 'AA', 'DL', 'UA', 'NW', 'US', 'CO', 'FL', 'OO', 'B6', 
     'MQ', 'AS', 'XE', 'HP', 'YV', 'EV', 'F9', '9E', 'OH', 'RP',
     'HA', 'QX', 'NK', 'ZW', '16')

select *
from t100_segments
where carrier = 'XE' and origin = 'CRP'
and destination = 'IAH' and year = 2007
and quarter = 2 and month = 4;

select t100.carrier, t100.aircraft_type,
  t100.origin, t100.destination, t100.year,
  t100.quarter, t100.month, count(*)
from t100_segments t100
where t100.departures_performed > 0
  and t100.service_class = 'F'
  and carrier in
   ('WN', 'AA', 'DL', 'UA', 'NW', 'US', 'CO', 'FL', 'OO', 'B6', 
     'MQ', 'AS', 'XE', 'HP', 'YV', 'EV', 'F9', '9E', 'OH', 'RP',
     'HA', 'QX', 'NK', 'ZW', '16')  
group by t100.carrier, t100.aircraft_type,
  t100.origin, t100.destination, t100.year,
  t100.quarter, t100.month
having count(*) > 1
order by count(*) desc;

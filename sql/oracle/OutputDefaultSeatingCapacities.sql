SET TERMOUT OFF

SET NEWPAGE 0
SET SPACE 0
SET LINESIZE 165
SET PAGESIZE 0
SET ECHO OFF
SET FEEDBACK OFF
SET VERIFY OFF
SET HEADING OFF
SET MARKUP HTML OFF SPOOL OFF
SET COLSEP '|'

SPOOL '/home/dfearing/workspace/data/paxdelay/aircrafts/DefaultSeatingCapacities.txt'

CLEAR COLUMNS
CLEAR BREAKS
CLEAR COMPUTES

COLUMN carrier HEADING CAR FORMAT A3
COLUMN origin HEADING ORI FORMAT A3
COLUMN destination HEADING DST FORMAT A3
COLUMN year HEADING YEAR FORMAT 0000
COLUMN quarter HEADING QTR FORMAT 9
COLUMN month HEADING MON FORMAT 99
COLUMN num_aircraft_types HEADING NUM FORMAT 99
COLUMN seats_mean HEADING MEAN FORMAT 999
COLUMN aircraft_types HEADING AIRCRAFT_TYPES FORMAT A40
COLUMN seating_capacities HEADING SEATING_CAPACITIES FORMAT A40
COLUMN departures_performed HEADING DEPARTURES_PERFORMED FORMAT A40

select tout.carrier, tout.origin, 
  tout.destination, tout.year,
  tout.quarter, tout.month,
  count(tout.aircraft_type) as num_aircraft_types,
  ceil(sum(tout.departures_performed * tin.seats) / 
    sum(tout.departures_performed)) as seats_mean,
  wm_concat(tout.aircraft_type) as aircraft_types,
  wm_concat(tin.seats) as seating_capacities,
  wm_concat(tout.departures_performed) as departures_performed
from 
(select t100.carrier, t100.origin, t100.destination, t100.aircraft_type,
  t100.year, t100.quarter, t100.month,
  sum(t100.departures_performed) as departures_performed
  from t100_segments t100
  where t100.departures_performed > 0
    and t100.service_class = 'F'
    and t100.carrier in
      ('WN', 'AA', 'DL', 'UA', 'NW', 'US', 'CO', 'FL', 'OO', 'B6', 
       'MQ', 'AS', 'XE', 'HP', 'YV', 'EV', 'F9', '9E', 'OH', 'RP',
       'HA', 'QX', 'NK', 'ZW', '16')
  group by t100.carrier, t100.origin, t100.destination, 
    t100.aircraft_type, t100.year, t100.quarter, t100.month) tout
join
(select t100.carrier, t100.aircraft_type,
  ceil(sum(t100.seats) / sum(t100.departures_performed)) as seats
  from t100_segments t100
  where t100.departures_performed > 0
    and t100.service_class = 'F'
    and t100.carrier in
      ('WN', 'AA', 'DL', 'UA', 'NW', 'US', 'CO', 'FL', 'OO', 'B6', 
       'MQ', 'AS', 'XE', 'HP', 'YV', 'EV', 'F9', '9E', 'OH', 'RP',
       'HA', 'QX', 'NK', 'ZW', '16')
  group by t100.carrier, t100.aircraft_type) tin
on tin.carrier = tout.carrier
and tin.aircraft_type = tout.aircraft_type
group by tout.carrier, tout.origin, tout.destination,
  tout.year, tout.quarter, tout.month;

SPOOL OFF

SET TERMOUT ON

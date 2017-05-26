SET TERMOUT OFF

SET NEWPAGE 0
SET SPACE 0
SET LINESIZE 170
SET PAGESIZE 0
SET ECHO OFF
SET FEEDBACK OFF
SET VERIFY OFF
SET HEADING OFF
SET MARKUP HTML OFF SPOOL OFF
SET COLSEP ' '

SPOOL '/home/dfearing/workspace/data/paxdelay/PotentialItineraries.txt'

CLEAR COLUMNS
CLEAR BREAKS
CLEAR COMPUTES

COLUMN itinerary_id HEADING ID FORMAT 999999999999
COLUMN scenario HEADING SCENARIO FORMAT A25
COLUMN carrier HEADING CAR FORMAT A3
COLUMN origin HEADING ORI FORMAT A3
COLUMN connection HEADING CON FORMAT A3
COLUMN departure FORMAT A46
COLUMN arrival FORMAT A46
COLUMN destination HEADING DST FORMAT A3
COLUMN layover_duration HEADING LAY FORMAT 9999
COLUMN first_flight_ID HEADING FLIGHT_ID_1 FORMAT 999999999999
COLUMN second_flight_ID HEADING FLIGHT_ID_2 FORMAT 999999999999

select 'Starting non-stop itineraries at ' || to_char(sysdate, 'HH24:MI:SS') from dual;

select itinerary_id_seq.nextval as itinerary_id, 'base_scenario' as scenario, 
  ft.carrier, ft.origin, null as connection, ft.destination, 
  to_char(ft.planned_departure_time, 'MM-DD-YYYY HH24:MI TZR') as departure, 
  to_char(ft.planned_arrival_time, 'MM-DD-YYYY HH24:MI TZR') as arrival, 
  null as layover_duration,
  ft.id as first_flight_id, null as second_flight_id
from flights ft
where ft.carrier = 'UA';
--where ft.carrier in
--('WN','AA','DL','UA','NW','US','CO','FL','OO','B6',
--'MQ','AS','XE','HP','YV','EV','F9','9E','OH','RP',
--'HA','QX','NK','ZW','16');

select 'Completed non-stop itineraries at ' || to_char(sysdate, 'HH24:MI:SS') from dual;
select 'Starting one-stop itineraries at ' || to_char(sysdate, 'HH24:MI:SS') from dual;

select itinerary_id_seq.nextval as itinerary_id, 'base_scenario',
  ft1.carrier, ft1.origin, ft1.destination as connection, ft2.destination,
  to_char(ft1.planned_departure_time, 'MM-DD-YYYY HH24:MI TZR') as departure,
  to_char(ft2.planned_arrival_time, 'MM-DD-YYYY HH24:MI TZR') as arrival,
  extract(hour from (ft2.planned_departure_time - ft1.planned_arrival_time)) * 60 +
    extract(minute from(ft2.planned_departure_time - ft1.planned_arrival_time)) 
      as layover_duration,
  ft1.id as first_flight_id, ft2.id as second_flight_id
from flights ft1
join flights ft2
  on ft1.carrier = ft2.carrier
  and ft1.destination = ft2.origin
  and ft1.planned_arrival_time + numtodsinterval(45, 'MINUTE') <=
    ft2.planned_departure_time
  and ft1.planned_arrival_time + numtodsinterval(150, 'MINUTE') >=
    ft2.planned_arrival_time
where ft1.carrier = 'UA';

select 'Completed one-stop itineraries at ' || to_char(sysdate, 'HH24:MI:SS') from dual;

SPOOL OFF

SET TERMOUT ON

insert into flights
  (id, carrier, tail_number, flight_number, origin, destination,
   planned_departure_time, planned_arrival_time, 
   actual_departure_time, actual_arrival_time,
   wheels_off_time, wheels_on_time,
   cancelled_flag, diverted_flag, number_flights, flight_distance,
   carrier_delay, weather_delay, nas_delay, security_delay,
   late_aircraft_delay)
select
  flight_id_seq.nextval, ot.carrier, ot.tail_number, ot.flight_number,
  ot.origin, ot.destination,
  to_timestamp_tz(concat(ot.flight_date, 
    concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
    concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
    concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:Mi TZR'),
  (to_timestamp_tz(concat(ot.flight_date, 
    concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
    concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
    concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:Mi TZR') +
   decode(ot.planned_elapsed_time, null,
   numtodsinterval(to_number(ot.planned_arrival_time) -
     to_number(ot.planned_departure_time), 'MINUTE'),
   numtodsinterval(ot.planned_elapsed_time, 'MINUTE')))
    at time zone dest.timezone_region,
  decode(ot.actual_departure_time, null, null,
    to_timestamp_tz(concat(ot.flight_date, 
      concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
      concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
      concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:Mi TZR') +
    numtodsinterval(ot.departure_offset, 'MINUTE')),
  decode(ot.actual_arrival_time, null, null,
    (to_timestamp_tz(concat(ot.flight_date, 
      concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
      concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
      concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:Mi TZR') +
    numtodsinterval(ot.planned_elapsed_time, 'MINUTE') +
    numtodsinterval(ot.arrival_offset, 'MINUTE'))
      at time zone dest.timezone_region),
  decode(ot.wheels_off_time, null, null,
    to_timestamp_tz(concat(ot.flight_date, 
      concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
      concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
      concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:Mi TZR') +
    numtodsinterval(ot.departure_offset, 'MINUTE') +
    numtodsinterval(ot.taxi_out_duration, 'MINUTE')),
  decode(ot.wheels_off_time, null, null,
    (to_timestamp_tz(concat(ot.flight_date, 
      concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
      concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
      concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:Mi TZR') +
    numtodsinterval(ot.departure_offset, 'MINUTE') +
    numtodsinterval(ot.taxi_out_duration, 'MINUTE') +
    numtodsinterval(ot.in_air_duration, 'MINUTE'))
      at time zone dest.timezone_region),
  ot.cancelled, ot.diverted, ot.number_flights, ot.flight_distance,
  ot.carrier_delay, ot.weather_delay, ot.nas_delay, ot.security_delay,
  ot.late_aircraft_delay
from aotp ot
join airports orig
  on orig.code = ot.origin
join airports dest
  on dest.code = ot.destination;

select ft.carrier, ft.flight_number, ft.origin, ft.destination,
  ft.planned_departure_time, min(ft.id), max(ft.id), count(*)
from flights ft
group by ft.carrier, ft.flight_number, ft.origin, ft.destination,
  ft.planned_departure_time
having count(*) > 1
order by min(ft.id);

select ft.*
from flights ft
where ft.id in
(354460,354461,
356372,356373,
962548,962549,
4075075,4075076,
4075077,4075078,
4075079,4075080,
4075081,4075082,
4075083,4075084,
4075085,4075086,
4075087,4075088,
4075089,4075090,
4075091,4075092,
4075093,4075094,
4075095,4075096,
4075097,4075098,
4075099,4075100,
4075101,4075102,
4075103,4075104,
4075105,4075106,
4075107,4075108,
4075109,4075110,
4719865,4719866,
4728085,4728086,
4728087,4728088,
5349590,5349591,
5962966,5962967,
5964029,5964030,
6580347,6580348,
7192933,7192934,
7192935,7192936)
order by ft.id;

delete
from flights ft
where ft.id in
(354461,
356373,
962548,
4075076,
4075078,
4075080,
4075082,
4075084,
4075086,
4075088,
4075090,
4075092,
4075094,
4075096,
4075098,
4075100,
4075102,
4075104,
4075106,
4075108,
4075110,
4719866,
4728086,
4728088,
5349591,
5962967,
5964030,
6580348,
7192934,
7192936)

create index idx_flights_codt
  on flights(carrier, origin, destination, planned_departure_time)
  tablespace users;

create index idx_flights_fnc
  on flights (flight_number, carrier)
  tablespace users;
  
create index idx_flights_cot
  on flights(carrier, origin, planned_departure_time)
  tablespace users;

create index idx_flights_cdt
  on flights(carrier, destination, planned_arrival_time)
  tablespace users;
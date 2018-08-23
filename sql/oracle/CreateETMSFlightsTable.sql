create table etms_flights
(
  carrier char(2) not null,
  flight_number varchar(4),
  origin char(3) not null,
  destination char(3) not null,
  iata_aircraft_code char(4),
  planned_departure_time timestamp with time zone,
  planned_arrival_time timestamp with time zone,
  actual_departure_time timestamp with time zone,
  actual_arrival_time timestamp with time zone
);

insert into etms_flights
select fc.iata_code, 
  decode(length(et.aircraft_id), 3, null,
    substr(et.aircraft_id, 4, length(et.aircraft_id) - 3)),
  et.origin, et.destination, et.iata_aircraft_code,
  decode(et.planned_departure_time_local, null, null,
    to_timestamp_tz(concat(et.planned_departure_time_local,
      concat(' ', orig.timezone_region)), 'MM/DD/YYYY HH24:MI:SS TZR')),
  decode(et.planned_arrival_time_local, null, null,
    to_timestamp_tz(concat(et.planned_arrival_time_local,
      concat(' ', dest.timezone_region)), 'MM/DD/YYYY HH24:MI:SS TZR')),
  decode(et.actual_departure_time_local, null, null,
    to_timestamp_tz(concat(et.actual_departure_time_local,
      concat(' ', orig.timezone_region)), 'MM/DD/YYYY HH24:MI:SS TZR')),
  decode(et.actual_arrival_time_local, null, null,
    to_timestamp_tz(concat(et.actual_arrival_time_local,
      concat(' ', dest.timezone_region)), 'MM/DD/YYYY HH24:MI:SS TZR'))
from etms et
join flight_carriers fc
  on fc.icao_code = substr(et.aircraft_id, 1, 3)
join airports orig
  on orig.code = et.origin
join airports dest
  on dest.code = et.destination;

select to_timestamp_tz(concat(et.planned_departure_time_local,
  concat(' ', orig.timezone_region)), 'MM/DD/YYYY HH24:MI:SS TZR')
from etms et
join airports orig
  on orig.code = et.origin
where et.planned_departure_time_local is not null
and et.actual_departure_time_local is null;

select count(*) from etms where planned_departure_time_local is null
and planned_departure_time_gmt is not null;

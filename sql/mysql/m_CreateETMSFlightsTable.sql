drop table if exists etms_flights;

create table etms_flights
select fc.iata_code as carrier,
			case when length(et.aircraft_id) = 3 then NULL
				else substring(et.aircraft_id, 4, length(et.aircraft_id) - 3)
			end as flight_number,
			et.origin as origin,
			et.destination as destination,
			et.iata_aircraft_code as iata_aircraft_code,
			
			et.planned_departure_time_gmt as planned_departure_time_UTC,
			orig.timezone_region as planned_departure_tz,
			hour(et.planned_departure_time_local) as planned_departure_local_hour,

			et.planned_arrival_time_gmt as planned_arrival_time_UTC,
			dest.timezone_region as planned_arrival_tz,
			hour(et.planned_arrival_time_local) as planned_arrival_local_hour,

			et.actual_departure_time_gmt as actual_departure_time_UTC,
			orig.timezone_region as actual_departure_tz,
			hour(et.actual_departure_time_local) as actual_departure_local_hour,

			et.actual_arrival_time_gmt as actual_arrival_time_UTC,
			dest.timezone_region as actual_arrival_tz,
			hour(et.actual_arrival_time_local) as actual_arrival_local_hour

from etms et
join flight_carriers fc on fc.icao_code = et.icao_aircraft_code
join airports orig on orig.code = et.origin
join airports dest on dest.code = et.destination;

create index idx_etms_flights_cfnoddt
  on etms_flights(carrier, flight_number, origin, destination, planned_departure_time_UTC);

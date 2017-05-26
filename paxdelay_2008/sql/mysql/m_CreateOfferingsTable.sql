drop table if exists offerings;

create table offerings
(
id integer not null auto_increment, primary key (id)
)
select 	
	iv.record_id as innovata_id,
  iv.carrier as carrier, 
	iv.flight_number as flight_number,
  iv.origin as origin, iv.destination as destination,
  iv.sunday_flag as sunday_flag, 
	iv.monday_flag as monday_flag, 
	iv.tuesday_flag as tuesday_flag, 
	iv.wednesday_flag as wednesday_flag,
  iv.thursday_flag as thursday_flag, 
	iv.friday_flag as friday_flag, 
	iv.saturday_flag as saturday_flag,
  iv.aircraft_code as iata_aircraft_code, 
	iv.number_stops as number_stops,
  iv.stop_codes as stop_code_list, 
	iv.aircraft_code_list as aircraft_code_list,

	addtime(iv.effective_start, SEC_TO_TIME(0)) as effective_start,
	orig.timezone_region as effective_start_tz,
	(addtime(iv.effective_end, SEC_TO_TIME((23 * 60 + 59) * 60 + 59))) as effective_end,
	orig.timezone_region as effective_end_tz,

	hour(iv.published_departure_time) as published_departure_hour,
	minute(iv.published_departure_time) as published_departure_minutes,
	hour(iv.published_arrival_time) as published_arrival_hour,
	minute(iv.published_arrival_time) as published_arrival_minutes

from innovata iv
join (select distinct(carrier) as code from aotp) carriers on iv.carrier = carriers.code
join airports orig  on iv.origin = orig.code
join airports dest  on iv.destination = dest.code
where iv.origin_country = 'US' and iv.destination_country = 'US';

create index idx_offerings_composit
on offerings(carrier, flight_number, origin, destination, sunday_flag, monday_flag, tuesday_flag, wednesday_flag, thursday_flag, friday_flag, saturday_flag, 
							effective_start, effective_end, published_departure_hour, published_departure_minutes);

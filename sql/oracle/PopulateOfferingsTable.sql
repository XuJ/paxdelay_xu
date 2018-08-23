insert into offerings
  (id, innovata_id, carrier, flight_number, effective_start, effective_end,
   origin, destination, published_departure_hour, published_departure_minutes, 
   published_arrival_hour, published_arrival_minutes, sunday_flag, monday_flag, 
   tuesday_flag, wednesday_flag, thursday_flag, friday_flag, saturday_flag,
   iata_aircraft_code, number_stops, stop_code_list, aircraft_code_list)
select 
  offering_id_seq.nextval, iv.record_id,
  iv.carrier, iv.flight_number,
  to_timestamp_tz(concat(iv.effective_start,
    concat(' ', orig.timezone_region)), 'DD-MM-YYYY TZR'),
  (to_timestamp_tz(concat(iv.effective_end,
    concat(' ', orig.timezone_region)), 'DD-MM-YYYY TZR')) + 
      numtodsinterval((23 * 60 + 59) * 60 + 59, 'SECOND'),
  iv.origin, iv.destination,
  extract(hour from to_timestamp(iv.published_departure_time, 'HH24:MI:SS')),
  extract(minute from to_timestamp(iv.published_departure_time, 'HH24:MI:SS')),
  extract(hour from to_timestamp(iv.published_arrival_time, 'HH24:MI:SS')),
  extract(minute from to_timestamp(iv.published_arrival_time, 'HH24:MI:SS')),
  iv.sunday_flag, iv.monday_flag, iv.tuesday_flag, iv.wednesday_flag,
  iv.thursday_flag, iv.friday_flag, iv.saturday_flag,
  iv.aircraft_code, iv.number_stops,
  iv.stop_codes, iv.aircraft_code_list
from innovata iv
join (select distinct(carrier) as code from aotp) carriers
  on iv.carrier = carriers.code
join airports orig
  on iv.origin = orig.code
join airports dest
  on iv.destination = dest.code
where iv.origin_country = 'US' and iv.destination_country = 'US';

create index idx_offerings_fncod
  on offerings(flight_number, carrier, origin, destination)
  tablespace users;

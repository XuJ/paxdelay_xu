create table
  temp_best_match
as
select ft.id as flight_id, min(off.id) as offering_id,
  count(*) as number_offerings
from flights ft
join offerings off
  on ft.carrier = off.carrier
  and ft.flight_number = off.flight_number
  and ft.origin = off.origin
  and ft.destination = off.destination
  and to_timestamp(to_char(ft.planned_departure_time, 'HH24:MI'), 'HH24:MI') =
    to_timestamp(concat(to_char(off.published_departure_hour, '00'),
      concat(':', to_char(off.published_departure_minutes, '00'))), 'HH24:MI')
  and ft.planned_departure_time >= off.effective_start
  and ft.planned_departure_time <= off.effective_end
  and bitand(
    (off.sunday_flag + 2 * off.monday_flag + 4 * off.tuesday_flag
    + 8 * off.wednesday_flag + 16 * off.thursday_flag + 32 * off.friday_flag
    + 64 * off.saturday_flag),
    (power(2, to_number(to_char(ft.planned_departure_time, 'D')) - 1))) != 0
group by ft.id;

create table
  temp_ignore_just_time
as
select ft.id as flight_id, min(off.id) as offering_id,
  count(*) as number_offerings
from flights ft
join offerings off
  on ft.carrier = off.carrier
  and ft.flight_number = off.flight_number
  and ft.origin = off.origin
  and ft.destination = off.destination
  and ft.planned_departure_time >= off.effective_start
  and ft.planned_departure_time <= off.effective_end
  and bitand(
    (off.sunday_flag + 2 * off.monday_flag + 4 * off.tuesday_flag
    + 8 * off.wednesday_flag + 16 * off.thursday_flag + 32 * off.friday_flag
    + 64 * off.saturday_flag),
    (power(2, to_number(to_char(ft.planned_departure_time, 'D')) - 1))) != 0
group by ft.id;

create table
  temp_ignore_effectivity_time
as
select ft.id as flight_id, min(off.id) as offering_id,
  count(*) as number_offerings
from flights ft
join offerings off
  on ft.carrier = off.carrier
  and ft.flight_number = off.flight_number
  and ft.origin = off.origin
  and ft.destination = off.destination
  and bitand(
    (off.sunday_flag + 2 * off.monday_flag + 4 * off.tuesday_flag
    + 8 * off.wednesday_flag + 16 * off.thursday_flag + 32 * off.friday_flag
    + 64 * off.saturday_flag),
    (power(2, to_number(to_char(ft.planned_departure_time, 'D')) - 1))) != 0
group by ft.id;

create table
  temp_ignore_dept_date_time
as
select ft.id as flight_id, min(off.id) as offering_id,
  count(*) as number_offerings
from flights ft
join offerings off
  on ft.carrier = off.carrier
  and ft.flight_number = off.flight_number
  and ft.origin = off.origin
  and ft.destination = off.destination
group by ft.id;

drop table temp_flight_offerings;

create table
  temp_flight_offerings
as
  select t.flight_id as flight_id, t.offering_id as offering_id
  from temp_best_match t

select count(*) from temp_flight_offerings;

select count(*) from temp_ignore_just_time;

insert into
  temp_flight_offerings
select t.flight_id, t.offering_id
from temp_ignore_just_time t
left join temp_flight_offerings tfo
  on tfo.flight_id = t.flight_id
where tfo.offering_id is null;

select count(*) from temp_flight_offerings;

select count(*) from temp_ignore_effectivity_time;

insert into
  temp_flight_offerings
select t.flight_id, t.offering_id
from temp_ignore_effectivity_time t
left join temp_flight_offerings tfo
  on tfo.flight_id = t.flight_id
where tfo.offering_id is null;

select count(*) from temp_flight_offerings;

select count(*) from temp_ignore_dept_date_time;

insert into
  temp_flight_offerings
select t.flight_id, t.offering_id
from temp_ignore_dept_date_time t
left join temp_flight_offerings tfo
  on tfo.flight_id = t.flight_id
where tfo.offering_id is null;

select count(*) from temp_flight_offerings;

delete from
  temp_flight_offerings
where offering_id is null;

select count(*) from temp_flight_offerings;

create index idx_tfo_fid_offid
  on temp_flight_offerings(flight_id, offering_id)
  tablespace users;

alter table flights
drop constraint fk_flights_offering;

insert into flights
 (id, carrier, offering_id, iata_aircraft_code, tail_number,
  flight_number, origin, destination, planned_departure_time,
  actual_departure_time, wheels_off_time, wheels_on_time,
  actual_arrival_time, planned_arrival_time, cancelled_flag,
  diverted_flag, number_flights, flight_distance, carrier_delay,
  weather_delay, nas_delay, security_delay, late_aircraft_delay)
select flight_id_seq.nextval,
  fto.carrier, off.id, off.iata_aircraft_code, fto.tail_number,
  fto.flight_number, fto.origin, fto.destination, fto.planned_departure_time,
  fto.actual_departure_time, fto.wheels_off_time, fto.wheels_on_time,
  fto.actual_arrival_time, fto.planned_arrival_time, fto.cancelled_flag,
  fto.diverted_flag, fto.number_flights, fto.flight_distance,
  fto.carrier_delay, fto.weather_delay, fto.nas_delay, fto.security_delay,
  fto.late_aircraft_delay
from flights_old fto
join temp_flight_offerings tfo
  on tfo.flight_id = fto.id
left join offerings off
  on off.id = tfo.offering_id;

alter table flights
add constraint fk_flights_offerings
  foreign key (offering_id)
  references offerings(id);

select count(*)
from flights
where offering_id is not null;



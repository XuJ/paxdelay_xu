alter table flights
rename to flights_no_seats;

update flights_no_seats
set iata_aircraft_code = null,
  seating_capacity = null;

-- Need to ensure there are no duplicate inventory listings
select ai.carrier, ai.tail_number
from
(select distinct carrier, tail_number,
  manufacturer, model
 from airline_inventories) ai
group by ai.carrier, ai.tail_number
having count(*) > 1;

create table temp_flight_icao
as
select distinct fns.id as flight_id, 
  map.icao_code as icao_aircraft_code
from flights_no_seats fns
join airline_inventories ai
  on ai.carrier = fns.carrier
  and ai.tail_number = fns.tail_number
join
(select distinct
  inventory_manufacturer as manufacturer,
  inventory_model as model,
  icao_code
 from aircraft_code_mappings
 where inventory_manufacturer is not null
   and inventory_model is not null
) map
  on map.manufacturer = ai.manufacturer
  and map.model = ai.model;
-- 5210995 / 7455428

insert into temp_flight_icao
select sm.flight_id, 
  decode(fx.fixed_icao_aircraft_code, null,
    sm.icao_aircraft_code, fx.fixed_icao_aircraft_code)
from 
(select distinct fns.id as flight_id, fns.carrier,
  et.icao_aircraft_code
 from flights_no_seats fns
 join
 (select fns.id as flight_id
  from flights_no_seats fns
  join etms_flights et
    on et.carrier = fns.carrier
    and et.flight_number = fns.flight_number
    and et.origin = fns.origin
    and et.destination = fns.destination
    and to_char(et.planned_departure_time, 'YYYY') =
      fns.year
    and to_char(et.planned_departure_time, 'MM') =
      fns.month
    and to_char(et.planned_departure_time, 'DD') =
      fns.day_of_month
  left join temp_flight_icao tfi
    on tfi.flight_id = fns.id
  where tfi.flight_id is null
    and et.icao_aircraft_code is not null
  group by fns.id
  having count(distinct et.icao_aircraft_code) = 1
 ) nodups
   on nodups.flight_id = fns.id
 join etms_flights et
   on et.carrier = fns.carrier
   and et.flight_number = fns.flight_number
   and et.origin = fns.origin
   and et.destination = fns.destination
   and to_char(et.planned_departure_time, 'YYYY') =
     fns.year
   and to_char(et.planned_departure_time, 'MM') =
     fns.month
   and to_char(et.planned_departure_time, 'DD') =
     fns.day_of_month
 where et.icao_aircraft_code is not null
) sm
left join fixed_carrier_icao_codes fx
  on fx.carrier = sm.carrier
  and fx.incorrect_icao_aircraft_code = sm.icao_aircraft_code;
-- 6426893 / 7455428

insert into temp_flight_icao
select sm.flight_id,
  decode(fx.fixed_icao_aircraft_code, null,
    sm.icao_aircraft_code, fx.fixed_icao_aircraft_code)
from
(select distinct fns.id as flight_id, fns.carrier,
  et.icao_aircraft_code
 from flights_no_seats fns
 join
 (select fns.id as flight_id
  from flights_no_seats fns
  join etms_flights et
    on et.carrier = fns.carrier
    and et.origin = fns.origin
    and et.destination = fns.destination
    and to_char(et.planned_departure_time, 'YYYY') =
      fns.year
    and to_char(et.planned_departure_time, 'MM') =
      fns.month
    and to_char(et.planned_departure_time, 'DD') =
      fns.day_of_month
    and to_char(et.planned_departure_time, 'HH24') =
      fns.hour_of_day
    and to_char(et.planned_departure_time, 'MI') =
      fns.minutes_of_hour
  left join temp_flight_icao tfi
    on tfi.flight_id = fns.id
  where tfi.flight_id is null
    and et.icao_aircraft_code is not null
  group by fns.id
  having count(distinct et.icao_aircraft_code) = 1
 ) nodups
   on nodups.flight_id = fns.id
 join etms_flights et
   on et.carrier = fns.carrier
   and et.origin = fns.origin
   and et.destination = fns.destination
   and to_char(et.planned_departure_time, 'YYYY') =
     fns.year
   and to_char(et.planned_departure_time, 'MM') =
     fns.month
   and to_char(et.planned_departure_time, 'DD') =
     fns.day_of_month
   and to_char(et.planned_departure_time, 'HH24') =
     fns.hour_of_day
   and to_char(et.planned_departure_time, 'MI') =
     fns.minutes_of_hour
 where et.icao_aircraft_code is not null
) sm
left join fixed_carrier_icao_codes fx
  on fx.carrier = sm.carrier
  and incorrect_icao_aircraft_code = sm.icao_aircraft_code;
-- 7278451 / 7455428

-- Consider expanding to 30 / 60 minutes in either direction
insert into temp_flight_icao
select sm.flight_id,
  decode(fx.fixed_icao_aircraft_code, null,
    sm.icao_aircraft_code, fx.fixed_icao_aircraft_code)
from
(select distinct fns.id as flight_id, fns.carrier,
  et.icao_aircraft_code
 from flights_no_seats fns
 join
 (select fns.id as flight_id
  from flights_no_seats fns
  join etms_flights et
    on et.carrier = fns.carrier
    and et.origin = fns.origin
    and et.destination = fns.destination
    and to_char(et.planned_departure_time, 'YYYY') =
      fns.year
    and to_char(et.planned_departure_time, 'MM') =
      fns.month
    and to_char(et.planned_departure_time, 'DD') =
      fns.day_of_month
    and et.planned_departure_time >=
      fns.planned_departure_time - numtodsinterval(20, 'MINUTE')
    and et.planned_departure_time <=
      fns.planned_departure_time + numtodsinterval(20, 'MINUTE')
  left join temp_flight_icao tfi
    on tfi.flight_id = fns.id
  where tfi.flight_id is null
    and et.icao_aircraft_code is not null
  group by fns.id
  having count(distinct et.icao_aircraft_code) = 1
 ) nodups
   on nodups.flight_id = fns.id
 join etms_flights et
   on et.carrier = fns.carrier
   and et.origin = fns.origin
   and et.destination = fns.destination
   and to_char(et.planned_departure_time, 'YYYY') =
     fns.year
   and to_char(et.planned_departure_time, 'MM') =
     fns.month
   and to_char(et.planned_departure_time, 'DD') =
     fns.day_of_month
   and et.planned_departure_time >=
     fns.planned_departure_time - numtodsinterval(20, 'MINUTE')
   and et.planned_departure_time <=
     fns.planned_departure_time + numtodsinterval(20, 'MINUTE')
  where et.icao_aircraft_code is not null
) sm
left join fixed_carrier_icao_codes fx
  on fx.carrier = sm.carrier
  and incorrect_icao_aircraft_code = sm.icao_aircraft_code;
-- 7287076 / 7455428

insert into temp_flight_icao
select offin.flight_id, acm.icao_code
from
(select ft.id as flight_id, min(off.id) as offering_id
 from flights_no_seats ft
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
   and ft.planned_departure_time >= off.effective_start
   and ft.planned_departure_time <= off.effective_end
   and ft.hour_of_day = off.published_departure_hour
   and ft.minutes_of_hour = off.published_departure_minutes
 left join temp_flight_icao tfi
   on tfi.flight_id = ft.id
 where tfi.flight_id is null
 group by ft.id) offin
join offerings off
  on off.id = offin.offering_id
join aircraft_code_mappings acm
  on acm.iata_code = off.iata_aircraft_code;
-- 7300649 / 7455428

insert into temp_flight_icao
select offin.flight_id, acm.icao_code
from
(select ft.id as flight_id, min(off.id) as offering_id
 from flights_no_seats ft
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
   and ft.planned_departure_time >= off.effective_start
   and ft.planned_departure_time <= off.effective_end
 left join temp_flight_icao tfi
   on tfi.flight_id = ft.id
 where tfi.flight_id is null
 group by ft.id) offin
join offerings off
  on off.id = offin.offering_id
join aircraft_code_mappings acm
  on acm.iata_code = off.iata_aircraft_code;
-- 7305951 / 7455428

insert into temp_flight_icao
select offin.flight_id, acm.icao_code
from
(select ft.id as flight_id, min(off.id) as offering_id
 from flights_no_seats ft
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
 left join temp_flight_icao tfi
   on tfi.flight_id = ft.id
 where tfi.flight_id is null
 group by ft.id) offin
join offerings off
  on off.id = offin.offering_id
join aircraft_code_mappings acm
  on acm.iata_code = off.iata_aircraft_code;
-- 7307899 / 7455428
   
create table temp_flight_seats
as
select distinct fns.id as flight_id,
  ai.number_of_seats
from flights_no_seats fns
join airline_inventories ai
  on ai.carrier = fns.carrier
  and ai.tail_number = fns.tail_number
where number_of_seats is not null;
-- 5558544 / 7455428

create table temp_t100_flight_seats
(
  flight_id not null,
  seats not null,
  coeff_var not null
)
as
select fto.id, t100s.seats_mean, t100s.seats_coeff_var
from t100_seats t100s
join flights_no_seats fto
  on to_number(to_char(fto.planned_departure_time, 'YYYY')) = t100s.year
  and to_number(to_char(fto.planned_departure_time, 'MM')) = t100s.month
  and fto.carrier = t100s.carrier
  and fto.origin = t100s.origin
  and fto.destination = t100s.destination;

insert into temp_flight_seats
select t100fs.flight_id, t100fs.seats
from temp_t100_flight_seats t100fs
left join temp_flight_seats tfs
  on tfs.flight_id = t100fs.flight_id
where t100fs.coeff_var <= 0.02
  and tfs.flight_id is null;
-- 6703157 / 7455428

insert into temp_flight_seats
select fns.id, cis.number_of_seats
from flights_no_seats fns
join temp_flight_icao tfi
  on tfi.flight_id = fns.id
join carrier_icao_seats cis
  on cis.carrier = fns.carrier
  and cis.icao_aircraft_code = tfi.icao_aircraft_code
left join temp_flight_seats tfs
  on tfs.flight_id = fns.id
where tfs.flight_id is null;
-- 7346287 / 7455428

create table flights
(
  id number not null primary key,
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  day_of_month number(2, 0) not null,
  day_of_week number(1, 0) not null,
  hour_of_day number(2, 0) not null,
  minutes_of_hour number(2, 0) not null,
  carrier char(2) not null,
  tail_number varchar2(10),
  flight_number char(4) not null,
  origin char(3) not null,
  destination char(3) not null,
  planned_departure_time timestamp with time zone not null,
  planned_arrival_time timestamp with time zone not null,
  actual_departure_time timestamp with time zone,
  actual_arrival_time timestamp with time zone,
  wheels_off_time timestamp with time zone,
  wheels_on_time timestamp with time zone,
  cancelled_flag number(1, 0) not null,
  diverted_flag number(1, 0) not null,
  num_flights number(1, 0) not null,
  flight_distance number(5, 0) not null,
  carrier_delay number(4, 0) not null,
  weather_delay number(4, 0) not null,
  nas_delay number(4, 0) not null,
  security_delay number(4, 0) not null,
  late_aircraft_delay number(4, 0) not null,
  icao_aircraft_code varchar2(4),
  seating_capacity number(3, 0)
)
partition by list (quarter)
(partition p_q1 values (1),
  partition p_q2 values (2),
  partition p_q3 values (3),
  partition p_q4 values (4)
);

select dups.flight_id, tfi.icao_aircraft_code
from temp_flight_icao tfi
join
(select tfi.flight_id, count(*) as num_dups
 from temp_flight_icao tfi
 group by tfi.flight_id
 having count(*) > 1
) dups
on tfi.flight_id = dups.flight_id
where rownum <= 10
order by dups.flight_id;

select dups.flight_id, tfs.number_of_seats
from temp_flight_seats tfs
join
(select tfs.flight_id, count(*) as num_dups
 from temp_flight_seats tfs
 group by tfs.flight_id
 having count(*) > 1
) dups
on dups.flight_id = tfs.flight_id
where rownum <= 10
order by dups.flight_id;

insert into flights
select tf.id, tf.year, tf.quarter, tf.month, tf.day_of_month, tf.day_of_week,
  tf.hour_of_day, tf.minutes_of_hour,
  tf.carrier, tf.tail_number,
  trim(to_char(to_number(tf.flight_number), '0000')),
  tf.origin, tf.destination,
  tf.planned_departure_time, tf.planned_arrival_time, tf.actual_departure_time,
  tf.actual_arrival_time, tf.wheels_off_time, tf.wheels_on_time, tf.cancelled_flag,
  tf.diverted_flag, tf.num_flights, tf.flight_distance, tf.carrier_delay,
  tf.weather_delay, tf.nas_delay, tf.security_delay, tf.late_aircraft_delay,
  tfi.icao_aircraft_code, tfs.number_of_seats
from flights_no_seats tf
left join temp_flight_icao tfi
  on tfi.flight_id = tf.id
left join temp_flight_seats tfs
  on tfs.flight_id = tf.id;

-- General index for searching for flights
create index idx_flights_cymdmodh
  on flights(carrier, year, month, day_of_month, origin, destination, hour_of_day)
  local
  tablespace users;

-- The following index is used to support PAP
create bitmap index bm_idx_flights_cymdm
  on flights(carrier, year, month, day_of_month)
  local
  tablespace users;

-- The following two indices are used to support itinerary generation
create index idx_flights_c
  on flights(carrier)
  local
  tablespace users;

create index idx_flights_coddt
  on flights(carrier, origin, destination, planned_departure_time)
  tablespace users;

-- The following indices are used to support airline validation
-- create index idx_flights_cyq
--   on flights(carrier, year, quarter)
--   tablespace users;
--
-- create index idx_flights_fncoddt
--   on flights(flight_number, carrier, origin, destination,
--     planned_departure_time)
--   tablespace users;
--
-- create index idx_flights_ymdmodh
--   on flights(year, month, day_of_month, origin, destination, hour_of_day)
--   local
--   tablespace users;

-- create unique index idx_flights_yqmdmfncod
--   on flights(year, quarter, month, day_of_month,
--     flight_number, carrier, origin, destination)
--   local
--   tablespace users;

select map.manufacturer, map.model, count(*)
from
(select distinct
  inventory_manufacturer as manufacturer,
  inventory_model as model,
  icao_code
 from aircraft_code_mappings
 where inventory_manufacturer is not null
   and inventory_model is not null) map
group by map.manufacturer, map.model
having count(*) > 1;

select count(*)
from carrier_iata_seats;

select distinct ft.carrier, tfi.icao_aircraft_code
from flights_no_seats ft
join temp_flight_icao tfi
  on tfi.flight_id = ft.id;

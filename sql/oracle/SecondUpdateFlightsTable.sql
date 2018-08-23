drop table temp_flight_seats;
drop table temp_t100_flight_seats;

create table temp_flight_seats
as
select distinct fns.id as flight_id,
  ai.number_of_seats
from flights fns
join airline_inventories ai
  on ai.carrier = fns.carrier
  and ai.tail_number = fns.tail_number
where number_of_seats is not null;
-- 5556261 / 7455428

create table temp_t100_flight_seats
(
  flight_id not null,
  seats not null,
  coeff_var not null
)
as
select fto.id, t100s.seats_mean, t100s.seats_coeff_var
from t100_seats t100s
join flights fto
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
-- 6744581 / 7455428

insert into temp_flight_seats
select fns.id, cis.number_of_seats
from flights fns
join temp_flight_icao tfi
  on tfi.flight_id = fns.id
join carrier_icao_seats cis
  on cis.carrier = fns.carrier
  and cis.icao_aircraft_code = tfi.icao_aircraft_code
left join temp_flight_seats tfs
  on tfs.flight_id = fns.id
where tfs.flight_id is null;
-- 7342643 / 7455428

create table flights_new_seats
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

insert into flights_new_seats
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
from flights tf
left join temp_flight_icao tfi
  on tfi.flight_id = tf.id
left join temp_flight_seats tfs
  on tfs.flight_id = tf.id;

-- General index for searching for flights
create index idx_flights_cymdm
  on flights_new_seats(carrier, year, month, day_of_month)
  local
  tablespace users;

create index idx_flights_cymod
  on flights_new_seats(carrier, year, month, origin, destination)
  local
  tablespace users;

create index idx_flights_ym
  on flights_new_seats(year, month)
  local
  tablespace users;

create index idx_flights_ymdm
  on flights_new_seats(year, month, day_of_month)
  local
  tablespace users;

-- The following two indices are used to support itinerary generation
create index idx_flights_c
  on flights_new_seats(carrier)
  local
  tablespace users;

create index idx_flights_coddt
  on flights_new_seats(carrier, origin, destination, planned_departure_time)
  tablespace users;


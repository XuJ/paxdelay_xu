declare
  seq_exists PLS_INTEGER;
  table_exists PLS_INTEGER;
begin

select count(*) into seq_exists 
  from user_sequences
  where sequence_name = 'FLIGHT_ID_SEQ';
if seq_exists = 1 then
  execute immediate 'drop sequence flight_id_seq';
end if;

select count(*) into table_exists 
  from user_tables
  where table_name = 'FLIGHTS';
if table_exists = 1 then
  execute immediate 'drop table flights';
end if;

end;

create sequence flight_id_seq
  start with 1
  increment by 1
  nomaxvalue;

create table temp_flights_aotp
(
  id not null primary key,
  year not null,
  quarter not null,
  month not null,
  day_of_month not null,
  hour_of_day not null,
  minutes_of_hour not null,
  carrier not null,
  tail_number,
  flight_number not null,
  origin not null,
  destination not null,
  planned_departure_time not null,
  planned_arrival_time not null,
  actual_departure_time,
  actual_arrival_time,
  wheels_off_time,
  wheels_on_time,
  cancelled_flag not null,
  diverted_flag not null,
  num_flights not null,
  flight_distance not null,
  carrier_delay not null,
  weather_delay not null,
  nas_delay not null,
  security_delay not null,
  late_aircraft_delay not null
)
as
select
  flight_id_seq.nextval,
  ot.year, ot.quarter, ot.month, ot.day_of_month,
  to_number(substr(ot.planned_departure_time, 1, 2), '00'),
  to_number(substr(ot.planned_departure_time, 3, 2), '00'),
  ot.carrier, ot.tail_number, ot.flight_number,
  ot.origin, ot.destination,
  to_timestamp_tz(concat(ot.flight_date, 
    concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
    concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
    concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:MI TZR'),
   decode(ot.planned_elapsed_time, null,
     decode(
       greatest(
         to_timestamp_tz(
           concat(substr(ot.planned_departure_time, 1, 2), 
           concat(':', concat(substr(ot.planned_departure_time, 3, 2),
           orig.timezone_region))), 'HH24:MI TZR'),
         to_timestamp_tz(
           concat(substr(ot.planned_arrival_time, 1, 2), 
           concat(':', concat(substr(ot.planned_arrival_time, 3, 2),
           dest.timezone_region))), 'HH24:MI TZR')),
       to_timestamp_tz(
         concat(substr(ot.planned_departure_time, 1, 2), 
         concat(':', concat(substr(ot.planned_departure_time, 3, 2),
         orig.timezone_region))), 'HH24:MI TZR'),
       (to_timestamp_tz(concat(ot.flight_date, 
          concat(' ', concat(substr(ot.planned_arrival_time, 1, 2), 
          concat(':', concat(substr(ot.planned_arrival_time, 3, 2), 
          concat(' ', trim(dest.timezone_region))))))), 'YYYY-MM-DD HH24:MI TZR') +
         numtodsinterval(1, 'DAY')),
       to_timestamp_tz(concat(ot.flight_date,
          concat(' ', concat(substr(ot.planned_arrival_time, 1, 2), 
          concat(':', concat(substr(ot.planned_arrival_time, 3, 2), 
          concat(' ', trim(dest.timezone_region))))))), 'YYYY-MM-DD HH24:MI TZR')),
    (to_timestamp_tz(concat(ot.flight_date, 
       concat(' ', concat(substr(ot.planned_departure_time, 1, 2), 
       concat(':', concat(substr(ot.planned_departure_time, 3, 2), 
       concat(' ', trim(orig.timezone_region))))))), 'YYYY-MM-DD HH24:MI TZR') +
     numtodsinterval(ot.planned_elapsed_time, 'MINUTE'))
     at time zone dest.timezone_region),
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
  decode(ot.wheels_on_time, null, null,
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

-- There are 30 duplicate flights that need to be cleaned up
-- We will just delete the one with the higher flight ID
delete from temp_flights_aotp tf
where tf.id in
(select max(tf.id)
 from temp_flights_aotp tf
 group by tf.carrier, tf.flight_number, tf.origin, tf.destination,
   tf.planned_departure_time
 having count(*) > 1);
 
create index idx_temp_ftaotp_fncod
  on temp_flights_aotp(flight_number, carrier, origin, destination)
  tablespace users;

create table
  temp_flight_offerings
as
select ft.id as flight_id, min(off.id) as offering_id
from temp_flights_aotp ft
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
group by ft.id;

create unique index idx_temp_tfo_fid
  on temp_flight_offerings(flight_id)
  tablespace users;

insert into temp_flight_offerings
select ft.id as flight_id, min(off.id) as offering_id
from temp_flights_aotp ft
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
left join temp_flight_offerings  tfo
  on tfo.flight_id = ft.id
where tfo.flight_id is null
group by ft.id;

insert into temp_flight_offerings
select ft.id as flight_id, min(off.id) as offering_id
from temp_flights_aotp ft
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
left join temp_flight_offerings tfo
  on tfo.flight_id = ft.id
where tfo.flight_id is null
group by ft.id;

create table temp_flights_no_seats
(
  id not null primary key,
  year not null,
  quarter not null,
  month not null,
  day_of_month not null,
  day_of_week not null,
  hour_of_day not null,
  minutes_of_hour not null,
  carrier not null,
  tail_number,
  flight_number not null,
  origin not null,
  destination not null,
  planned_departure_time not null,
  planned_arrival_time not null,
  actual_departure_time,
  actual_arrival_time,
  wheels_off_time,
  wheels_on_time,
  cancelled_flag not null,
  diverted_flag not null,
  num_flights not null,
  flight_distance not null,
  carrier_delay not null,
  weather_delay not null,
  nas_delay not null,
  security_delay not null,
  late_aircraft_delay not null,
  offering_id,
  iata_aircraft_code
)
as
select tf.id, tf.year, tf.quarter, tf.month, tf.day_of_month,
  to_number(to_char(tf.planned_departure_time, 'D'), 0), 
  tf.hour_of_day, tf.minutes_of_hour,
  tf.carrier, tf.tail_number, 
  tf.flight_number, tf.origin, tf.destination, tf.planned_departure_time,
  tf.planned_arrival_time, tf.actual_departure_time, tf.actual_arrival_time,
  tf.wheels_off_time, tf.wheels_on_time, tf.cancelled_flag,
  tf.diverted_flag, tf.num_flights, tf.flight_distance, tf.carrier_delay,
  tf.weather_delay, tf.nas_delay, tf.security_delay, tf.late_aircraft_delay,
  off.id, off.iata_aircraft_code
from temp_flights_aotp tf
left join temp_flight_offerings tfo
  on tfo.flight_id = tf.id
left join offerings off
  on off.id = tfo.offering_id;

create table temp_iata_flight_seats
(
  flight_id not null,
  seats not null
)
as
select fto.id, iata.seats
from carrier_iata_seats iata
join temp_flights_no_seats fto
  on fto.carrier = iata.carrier
  and fto.iata_aircraft_code = iata.aircraft_code;

create unique index idx_temp_iata_fid
  on temp_iata_flight_seats(flight_id)
  tablespace users;

create table temp_t100_flight_seats
(
  flight_id not null,
  seats not null,
  coeff_var not null
)
as
select fto.id, t100s.seats_mean, t100s.seats_coeff_var
from t100_seats t100s
join temp_flights_no_seats fto
  on to_number(to_char(fto.planned_departure_time, 'YYYY')) = t100s.year
  and to_number(to_char(fto.planned_departure_time, 'MM')) = t100s.month
  and fto.carrier = t100s.carrier
  and fto.origin = t100s.origin
  and fto.destination = t100s.destination;

create unique index idx_temp_t100s_fid
  on temp_t100_flight_seats(flight_id)
  tablespace users;

create table temp_flight_seats
(
  flight_id not null,
  seats not null
)
as
select t100fs.flight_id, t100fs.seats
from temp_t100_flight_seats t100fs
where t100fs.coeff_var <= 0.025;

create unique index idx_temp_fs_fid
  on temp_flight_seats(flight_id)
  tablespace users;

insert into temp_flight_seats
select iata.flight_id, iata.seats
from temp_iata_flight_seats iata
left join temp_flight_seats tfs
  on tfs.flight_id = iata.flight_id
where tfs.flight_id is null;

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
  offering_id number(10, 0),
  iata_aircraft_code char(3),
  seating_capacity number(4, 0),
  constraint fk_flights_offering
    foreign key (offering_id)
    references offerings(id)
)
partition by list (quarter)
(partition p_q1 values (1),
  partition p_q2 values (2),
  partition p_q3 values (3),
  partition p_q4 values (4)
);

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
  tf.offering_id, tf.iata_aircraft_code, tfs.seats
from temp_flights_no_seats tf
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
-- The following indices are used to support passenger allocation
create index idx_flights_ym
  on flights(year, month)
  local
  tablespace users;

create index idx_flights_ymdm
  on flights(year, month, day_of_month)
  local
  tablespace users;

-- The following indices are used to support airline validation
create index idx_flights_cyq
  on flights(carrier, year, quarter)
  tablespace users;

create index idx_flights_fncyqod
  on flights(flight_number, carrier, year, quarter, origin, destination)
  local
  tablespace users;

create index idx_flights_ymdmodh
  on flights(year, month, day_of_month, origin, destination, hour_of_day)
  local
  tablespace users;

create unique index idx_flights_yqmdmfncod
  on flights(year, quarter, month, day_of_month, 
    flight_number, carrier, origin, destination)
  local
  tablespace users;

create index idx_flights_fncoddt
  on flights(flight_number, carrier, origin, destination,
    planned_departure_time)
  tablespace users;

/*********************************************
 * Used to update table without rebuilding it
alter table flights 
  rename column flight_number to flight_number_string;

alter table flights
  add (flight_number char(4) default '0000' not null);

update flights
  set flight_number = trim(to_char(to_number(flight_number_string), '0000'));

alter table flights
  drop column flight_number_string;
*********************************************/

-- Drop the temporary tables
drop table temp_flights_aotp;
drop table temp_flight_offerings;
drop table temp_flights_no_seats;
drop table temp_iata_flight_seats;
drop table temp_t100_flight_seats;
drop table temp_flight_seats;

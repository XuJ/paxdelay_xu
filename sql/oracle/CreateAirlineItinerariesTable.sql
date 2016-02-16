select count(*)
from flights ft
where length(translate(trim(flight_number), '0123456789', '')) >= 0;

select count(*)
from continental_flight_legs
where length(translate(trim(flight_number), '0123456789', '')) >= 0;

create table temp_airline_itineraries
(
  itinerary_id primary key not null,
  year not null,
  month not null,
  day_of_month not null,
  num_flights not null,
  origin not null,
  destination not null,
  planned_departure_time not null,
  passengers not null
)
as
select coi.itinerary_id,
  to_number(to_char(to_date(coi.departure_date, 'MM/DD/YYYY'), 'YYYY'), '0000'),
  to_number(to_char(to_date(coi.departure_date, 'MM/DD/YYYY'), 'MM'), '00'),
  to_number(to_char(to_date(coi.departure_date, 'MM/DD/YYYY'), 'DD'), '00'),
  coi.num_flights, coi.origin, coi.destination,
  to_timestamp_tz(concat(coi.departure_date, 
    concat(' ', 
    concat(coi.departure_time, 
    concat(' ', ori.timezone_region)))), 
    'MM/DD/YYYY HH24:MI TZR'),
  coi.number_flown
from continental_itineraries coi
join airports ori
  on ori.code = coi.origin;

create table temp_airline_legs
(
  itinerary_id not null,
  num_flights not null,
  itinerary_sequence not null,
  carrier not null,
  flight_number not null,
  planned_departure_time not null,
  origin not null,
  destination not null
)
as
select cof.itinerary_id,
  cof.num_flights, cof.itinerary_sequence,
  cof.carrier, cof.flight_number,
  decode(cof.itinerary_sequence, 1, tai.planned_departure_time,
    decode(greatest(tai.planned_departure_time,
      to_timestamp_tz(concat(to_char(tai.planned_departure_time, 'MM/DD/YYYY'),
        concat(' ',
        concat(cof.departure_time, ori.timezone_region))),
        'MM/DD/YYYY HH24:MI TZR')), 
      tai.planned_departure_time,
      to_timestamp_tz(concat(to_char(tai.planned_departure_time, 'MM/DD/YYYY'),
        concat(' ',
        concat(cof.departure_time, ori.timezone_region))),
        'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(1, 'DAY'),
      to_timestamp_tz(concat(to_char(tai.planned_departure_time, 'MM/DD/YYYY'),
        concat(' ',
        concat(cof.departure_time, ori.timezone_region))),
        'MM/DD/YYYY HH24:MI TZR'))),
  cof.origin, cof.destination
from continental_flight_legs cof
join temp_airline_itineraries tai
  on tai.itinerary_id = cof.itinerary_id
join airports ori
  on ori.code = cof.origin;

create unique index idx_temp_airline_legs_iidis
  on temp_airline_legs(itinerary_id, itinerary_sequence)
  tablespace users;

alter table temp_airline_legs
  add (year number(4, 0) default 0 not null,
       quarter number(1, 0) default 0 not null,
       month number(2, 0) default 0 not null,
       day_of_month number(2, 0) default 0 not null);

update temp_airline_legs tal
  set tal.year = to_number(to_char(tal.planned_departure_time, 'YYYY')),
      tal.quarter = to_number(to_char(tal.planned_departure_time, 'Q')),
      tal.month = to_number(to_char(tal.planned_departure_time, 'MM')),
      tal.day_of_month = to_number(to_char(tal.planned_departure_time, 'DD'));

create index temp_airline_legs_c
  on temp_airline_legs(carrier)
  tablespace users;

create index temp_airline_legs_fncyqod
  on temp_airline_legs(flight_number, carrier, year, quarter,
    origin, destination);

create table temp_leg_matches
as
select tal.itinerary_id, tal.itinerary_sequence,
  ft.id as flight_id, tal.origin, tal.destination,
  tal.carrier as continental_carrier,
  tal.flight_number as continental_flight_number,
  ft.carrier as flight_carrier,
  ft.flight_number as flight_flight_number,
  tal.planned_departure_time as continental_departure_time,
  ft.planned_departure_time as flight_departure_time,
  abs(extract(hour from (tal.planned_departure_time -
    ft.planned_departure_time))) * 60 +
    abs(extract(minute from (tal.planned_departure_time -
      ft.planned_departure_time))) as minute_offset
from temp_airline_legs tal
join flights ft
  on ft.origin = tal.origin
  and ft.destination = tal.destination
  and ft.carrier = tal.carrier
  and ft.flight_number = tal.flight_number
  and ft.planned_departure_time >=
    (tal.planned_departure_time - numtodsinterval(150, 'MINUTE'))
  and ft.planned_departure_time <=
    (tal.planned_departure_time + numtodsinterval(150, 'MINUTE'));

create unique index idx_temp_leg_matches_iidis
  on temp_leg_matches(itinerary_id, itinerary_sequence)
  tablespace users;

insert into temp_leg_matches
select tal.itinerary_id, tal.itinerary_sequence,
  ft.id as flight_id, tal.origin, tal.destination,
  tal.carrier as continental_carrier,
  tal.flight_number as continental_flight_number,
  ft.carrier as flight_carrier,
  ft.flight_number as flight_flight_number,
  tal.planned_departure_time as continental_departure_time,
  ft.planned_departure_time as flight_departure_time,
  abs(extract(hour from (tal.planned_departure_time -
    ft.planned_departure_time))) * 60 +
    abs(extract(minute from (tal.planned_departure_time -
      ft.planned_departure_time))) as minute_offset
from temp_airline_legs tal
join flights ft
  on ft.origin = tal.origin
  and ft.destination = tal.destination
  and ft.flight_number = tal.flight_number
  and ft.planned_departure_time >=
    (tal.planned_departure_time - numtodsinterval(150, 'MINUTE'))
  and ft.planned_departure_time <=
    (tal.planned_departure_time + numtodsinterval(150, 'MINUTE'))
left join temp_leg_matches tlm
  on tlm.itinerary_id = tal.itinerary_id
  and tlm.itinerary_sequence = tal.itinerary_sequence
where tlm.itinerary_id is null;

select count(*)
from flights ft
where carrier = 'CO' and quarter = '4';

select count(*)
from flights ft
join
(select distinct tlm.flight_id
 from temp_leg_matches tlm) pin
on ft.id = pin.flight_id
where carrier = 'CO' and quarter = '4';

create table temp_potential_leg_matches
as
select tal.itinerary_id, tal.itinerary_sequence,
  ft.id as flight_id, tal.origin, tal.destination,
  tal.carrier as continental_carrier,
  tal.flight_number as continental_flight_number,
  ft.carrier as flight_carrier,
  ft.flight_number as flight_flight_number,
  tal.planned_departure_time as continental_departure_time,
  ft.planned_departure_time as flight_departure_time,
  abs(extract(hour from (tal.planned_departure_time -
    ft.planned_departure_time))) * 60 +
    abs(extract(minute from (tal.planned_departure_time -
      ft.planned_departure_time))) as minute_offset
from temp_airline_legs tal
left join flights ft
  on ft.origin = tal.origin
  and ft.destination = tal.destination
  and ft.planned_departure_time >=
    (tal.planned_departure_time - numtodsinterval(480, 'MINUTE'))
  and ft.planned_departure_time <=
    (tal.planned_departure_time + numtodsinterval(480, 'MINUTE'))
left join temp_leg_matches tlm
  on tlm.itinerary_id = tal.itinerary_id
  and tlm.itinerary_sequence = tal.itinerary_sequence
where tlm.itinerary_id is null;

select itinerary_id, itinerary_sequence
from temp_leg_matches
group by itinerary_id, itinerary_sequence
having count(*) > 1;

-- The number of itinerary legs with matching flights
select count(*)
from
(select tlm.itinerary_id, tlm.itinerary_sequence
 from temp_leg_matches tlm
 group by tlm.itinerary_id, tlm.itinerary_sequence);

-- The number of itinerary legs with potential origin 
-- and destination matches within 8 hours of the departure time
select count(*)
from
(select tpm.itinerary_id, tpm.itinerary_sequence
 from temp_potential_leg_matches tpm
 group by tpm.itinerary_id, tpm.itinerary_sequence);

-- The number of distinct flights from the itinerary legs
-- with potential origin and destination matches within
-- 8 hours of the departure time
select count(*)
from
(select tpm.continental_carrier, tpm.continental_flight_number,
   tpm.origin, tpm.destination, tpm.continental_departure_time
 from temp_potential_leg_matches tpm
 group by tpm.continental_carrier, tpm.continental_flight_number,
   tpm.origin, tpm.destination, tpm.continental_departure_time);

create index idx_temp_leg_matches_fid
  on temp_leg_matches(flight_id);

create table temp_leg_flight_joins
as
select
  tal.itinerary_id, tal.itinerary_sequence,
  tal.origin, tal.destination,
  ft.id as flight_id,
  ft.carrier,
  tal.carrier as airline_carrier,
  ft.flight_number,
  tal.flight_number as airline_flight_number,
  ft.planned_departure_time,
  tal.planned_departure_time as airline_departure_time,
  ft.planned_arrival_time
from temp_airline_legs tal
left join temp_leg_matches tlm
  on tlm.itinerary_id = tal.itinerary_id
  and tlm.itinerary_sequence = tal.itinerary_sequence
left join flights ft
  on ft.id = tlm.flight_id;

-- Should return no rows
select itinerary_id, itinerary_sequence
from temp_leg_flight_joins
group by itinerary_id, itinerary_sequence
having count(*) > 1;

create sequence temp_unique_leg_flight_id
  start with 1
  increment by 1
  nomaxvalue;

create table temp_unique_leg_flights
as
select
  temp_unique_leg_flight_id.nextval as id,
  tlf.origin, tlf.destination,
  tlf.flight_id,
  tlf.carrier, tlf.airline_carrier,
  tlf.flight_number, tlf.airline_flight_number,
  tlf.planned_departure_time, tlf.airline_departure_time,
  tlf.planned_arrival_time
from 
(select tlf.origin, tlf.destination, 
   tlf.flight_id,
   tlf.carrier, tlf.airline_carrier,
   tlf.flight_number, tlf.airline_flight_number,
   tlf.planned_departure_time, tlf.airline_departure_time,
   tlf.planned_arrival_time
 from temp_leg_flight_joins tlf
 group by tlf.origin, tlf.destination, tlf.flight_id,
   tlf.carrier, tlf.airline_carrier,
   tlf.flight_number, tlf.airline_flight_number,
   tlf.planned_departure_time, tlf.airline_departure_time,
   tlf.planned_arrival_time) tlf;

drop sequence temp_unique_leg_flight_id;

create sequence airline_itineraries_id_seq
  start with 1
  increment by 1
  nomaxvalue;

create table temp_airline_legs_merged
as
select airline_itineraries_id_seq.nextval as itinerary_id,
  tai.year, tai.quarter, tai.month, tai.day_of_month,
  tai.passengers, tai.num_flights, tai.first_flight_id,
  decode(tai.num_flights, 2, tai.last_flight_id, 
    3, tai.sum_flight_ids - tai.first_flight_id - tai.last_flight_id, null)
      as second_flight_id,
  decode(tai.num_flights, 3, tai.last_flight_id, null)
    as third_flight_id
from
(select tai.year, tai.quarter, tai.month, tai.day_of_month,
  tai.num_flights, tai.first_flight_id, tai.last_flight_id,
  tai.sum_flight_ids, sum(tai.passengers) as passengers
 from
 (select tai.year, floor((tai.month + 2) / 3) as quarter,
   tai.month, tai.day_of_month,
   avg(tai.passengers) as passengers,
   sum(ulf.id) keep (dense_rank first order by tal.itinerary_sequence) as first_flight_id,
   sum(ulf.id) keep (dense_rank last order by tal.itinerary_sequence) as last_flight_id,
   sum(ulf.id) as sum_flight_ids,
   count(tal.itinerary_sequence) as num_flights
  from temp_airline_itineraries tai
  join temp_airline_legs tal
    on tal.itinerary_id = tai.itinerary_id
  join temp_unique_leg_flights ulf
    on ulf.airline_carrier = tal.carrier
    and ulf.airline_flight_number = tal.flight_number
    and ulf.origin = tal.origin
    and ulf.destination = tal.destination
    and ulf.airline_departure_time = tal.planned_departure_time
  group by tai.year, tai.month, tai.day_of_month, tai.itinerary_id
  having count(tal.itinerary_sequence) <= 3) tai
 group by tai.year, tai.quarter, tai.month, tai.day_of_month,
   tai.num_flights, tai.first_flight_id, tai.last_flight_id,
   tai.sum_flight_ids) tai;

create table airline_itineraries
(
  itinerary_id number(12, 0) not null,
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  day_of_month number(2, 0) not null,
  day_of_week number(1, 0) not null,
  passengers number(4, 0) not null,
  num_flights number(1, 0) not null,
  first_carrier char(2) not null,
  first_flight_number varchar2(6) not null,
  first_departure_time timestamp with time zone not null,
  first_arrival_time timestamp with time zone,
  first_flight_id number(12, 0),
  second_carrier char(2),
  second_flight_number varchar2(6),
  second_departure_time timestamp with time zone,
  second_arrival_time timestamp with time zone,
  second_flight_id number(12, 0),
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  trip_duration number(4, 0),
  layover_duration number(4, 0),
  multi_day_flag number(1, 0)
);

insert into airline_itineraries
select alm.itinerary_id, alm.year, alm.quarter, alm.month,
  alm.day_of_month, 0, alm.passengers, 1,
  decode(first.carrier, null, first.airline_carrier, first.carrier),
  decode(first.flight_number, null, first.airline_flight_number, first.flight_number),
  decode(first.planned_departure_time, null, first.airline_departure_time, 
    first.planned_departure_time),
  first.planned_arrival_time,
  first.flight_id,
  null, null, null, null, null,
  first.origin, null, first.destination,
  null, null, 0
from temp_airline_legs_merged alm
join temp_unique_leg_flights first
  on first.id = alm.first_flight_id
where alm.num_flights = 1
union all
select alm.itinerary_id, alm.year, alm.quarter, alm.month,
  alm.day_of_month, 0, alm.passengers, 2,
  decode(first.carrier, null, first.airline_carrier, first.carrier),
  decode(first.flight_number, null, first.airline_flight_number, first.flight_number),
  decode(first.planned_departure_time, null, first.airline_departure_time,
    first.planned_departure_time),
  first.planned_arrival_time,
  first.flight_id,
  decode(second.carrier, null, second.airline_carrier, second.carrier),
  decode(second.flight_number, null, second.airline_flight_number, second.flight_number),
  decode(second.planned_departure_time, null, second.airline_departure_time,
    second.planned_departure_time),
  second.planned_arrival_time,
  second.flight_id,
  first.origin, first.destination, second.destination,
  null, null, 0
from temp_airline_legs_merged alm
join temp_unique_leg_flights first
  on first.id = alm.first_flight_id
join temp_unique_leg_flights second
  on second.id = alm.second_flight_id
where alm.num_flights = 2;

update airline_itineraries
  set day_of_week = trim(to_number(to_char(first_departure_time, 'D'), '0'));

create table temp_americawest_itineraries
(
  itinerary_id primary key not null,
  year not null,
  month not null,
  day_of_month not null,
  day_of_week not null,
  num_flights not null,
  origin not null,
  destination not null,
  planned_departure_time not null,
  passengers not null
)
as
select amw.itinerary_id,
  to_number(to_char(to_date(amw.departure_date, 'MM/DD/YYYY'), 'YYYY'), '0000'),
  to_number(to_char(to_date(amw.departure_date, 'MM/DD/YYYY'), 'MM'), '00'),
  to_number(to_char(to_date(amw.departure_date, 'MM/DD/YYYY'), 'DD'), '00'),
  to_number(to_char(to_date(amw.departure_date, 'MM/DD/YYYY'), 'D'), '0'),
  amw.num_flights, amw.origin, amw.destination,
  to_timestamp_tz(concat(amw.departure_date,
    concat(' ',
    concat(to_char(decode(amw.next_day_flag, 1, amw.hour_of_day - 24, amw.hour_of_day), '00'),
    concat(':',
    concat(to_char(amw.minutes_of_hour, '00'),
    concat(' ', ori.timezone_region)))))),
    'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(amw.next_day_flag, 'DAY'),
  amw.passengers
from 
(
 select itinerary_id, departure_date, num_flights,
   origin, destination, 
   to_number(substr(departure_time, 1, 
     instr(departure_time, ':', 1, 1) - 1)) as hour_of_day,
   to_number(substr(departure_time, 
     instr(departure_time, ':', 1, 1) + 1, 2)) as minutes_of_hour,
   floor(to_number(substr(departure_time, 1,
     instr(departure_time, ':', 1, 1) - 1)) / 24) as next_day_flag,
   passengers
 from americawest_itineraries
) amw
join airports ori
  on ori.code = amw.origin
join airports dest
  on dest.code = amw.destination;

create table temp_americawest_legs
(
  itinerary_id not null,
  num_flights not null,
  itinerary_sequence not null,
  carrier not null,
  flight_number not null,
  planned_departure_time not null,
  planned_arrival_time not null,
  origin not null,
  destination not null
)
as
select amf.itinerary_id,
  amf.num_flights, amf.itinerary_sequence,
  amf.carrier, amf.flight_number,
  to_timestamp_tz(concat(amf.departure_date,
    concat(' ',
    concat(to_char(decode(amf.next_day_departure, 1, amf.departure_hour - 24, 
      amf.departure_hour), '00'),
    concat(':',
    concat(to_char(amf.departure_minutes, '00'),
    concat(' ', ori.timezone_region)))))),
    'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(amf.next_day_departure, 'DAY'),
  decode(
    greatest(
      to_timestamp_tz(concat(amf.departure_date,
        concat(' ',
        concat(to_char(decode(amf.next_day_departure, 1, amf.departure_hour - 24,
          amf.departure_hour), '00'),
        concat(':',
        concat(to_char(amf.departure_minutes, '00'),
        concat(' ', ori.timezone_region)))))),
        'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(amf.next_day_departure, 'DAY'),
     to_timestamp_tz(concat(amf.departure_date,
       concat(' ',
       concat(to_char(decode(amf.next_day_arrival, 1, amf.arrival_hour - 24,
         amf.arrival_hour), '00'),
       concat(':',
       concat(to_char(amf.arrival_minutes, '00'),
       concat(' ', dest.timezone_region)))))),
       'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(amf.next_day_arrival, 'DAY')),
    to_timestamp_tz(concat(amf.departure_date,
      concat(' ',
      concat(to_char(decode(amf.next_day_departure, 1, amf.departure_hour - 24,
        amf.departure_hour), '00'),
      concat(':',
      concat(to_char(amf.departure_minutes, '00'),
      concat(' ', ori.timezone_region)))))),
     'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(amf.next_day_departure, 'DAY'),
   to_timestamp_tz(concat(amf.departure_date,
     concat(' ',
     concat(to_char(decode(amf.next_day_arrival, 1, amf.arrival_hour - 24,
       amf.arrival_hour), '00'),
     concat(':',
     concat(to_char(amf.arrival_minutes, '00'),
     concat(' ', dest.timezone_region)))))),
     'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(amf.next_day_arrival, 'DAY') +
     numtodsinterval(1, 'DAY'),
   to_timestamp_tz(concat(amf.departure_date,
     concat(' ',
     concat(to_char(decode(amf.next_day_arrival, 1, amf.arrival_hour - 24,
       amf.arrival_hour), '00'),
     concat(':',
     concat(to_char(amf.arrival_minutes, '00'),
     concat(' ', dest.timezone_region)))))),
     'MM/DD/YYYY HH24:MI TZR') + numtodsinterval(amf.next_day_arrival, 'DAY')),
  amf.origin, amf.destination
from 
(
 select itinerary_id, num_flights, itinerary_sequence,
   carrier, flight_number, departure_date,
   to_number(substr(departure_time, 1,
     instr(departure_time, ':', 1, 1) - 1)) as departure_hour,
   to_number(substr(departure_time,
     instr(departure_time, ':', 1, 1) + 1, 2)) as departure_minutes,
   floor(to_number(substr(departure_time, 1,
     instr(departure_time, ':', 1, 1) - 1)) / 24) as next_day_departure,
   to_number(substr(arrival_time, 1,
     instr(arrival_time, ':', 1, 1) - 1)) as arrival_hour,
   to_number(substr(arrival_time,
     instr(arrival_time, ':', 1, 1) + 1, 2)) as arrival_minutes,
   floor(to_number(substr(arrival_time, 1,
     instr(arrival_time, ':', 1, 1) - 1)) / 24) as next_day_arrival,
   origin, destination
 from americawest_flight_legs
) amf
join temp_americawest_itineraries tai
  on tai.itinerary_id = tal.itinerary_id
join airports ori
  on ori.code = amf.origin
join airports dest
  on dest.code = amf.destination;

delete from temp_americawest_itineraries tai
where tai.itinerary_id in
(
 select tai.itinerary_id
 from temp_americawest_itineraries tai
 join temp_americawest_legs tal
   on tal.itinerary_id = tai.itinerary_id
 group by tai.itinerary_id, tai.num_flights
 having tai.num_flights != count(tal.itinerary_sequence)
);

delete from temp_americawest_legs tal
where not exists
(
 select * from temp_americawest_itineraries
 where itinerary_id = tal.itinerary_id
);

create sequence temp_unique_flight_leg_id
  start with 1
  increment by 1
  nomaxvalue;

create table temp_unique_americawest_legs
as
select
  temp_unique_flight_leg_id.nextval as id,
  tal.origin, tal.destination,
  tal.carrier, tal.flight_number, 
  tal.planned_departure_time, tal.planned_arrival_time
from
(
 select tal.origin, tal.destination,
   tal.carrier, tal.flight_number, 
   tal.planned_departure_time, tal.planned_arrival_time
 from temp_americawest_legs tal
 group by tal.origin, tal.destination, 
   tal.carrier, tal.flight_number, 
   tal.planned_departure_time, tal.planned_arrival_time
) tal;

drop sequence temp_unique_flight_leg_id;

create table temp_americawest_legs_merged
as
select airline_itineraries_id_seq.nextval as itinerary_id,
  tal.year, tal.quarter, tal.month, tal.day_of_month,
  tal.day_of_week, tal.passengers, tal.num_flights, tal.first_flight_id,
  decode(tal.num_flights, 2, tal.last_flight_id,
    3, tal.sum_flight_ids - tal.first_flight_id - tal.last_flight_id, null)
      as second_flight_id,
  decode(tal.num_flights, 3, tal.last_flight_id, null)
    as third_flight_id
from
(
 select tin.year, tin.quarter, tin.month, tin.day_of_month,
  tin.day_of_week, tin.num_flights, tin.first_flight_id, tin.last_flight_id,
  tin.sum_flight_ids, sum(tin.passengers) as passengers
 from
 (
  select tai.year, floor((tai.month + 2) / 3) as quarter,
   tai.month, tai.day_of_month, tai.day_of_week,
   avg(tai.passengers) as passengers,
   sum(ual.id) keep (dense_rank first order by tal.itinerary_sequence) as first_flight_id,
   sum(ual.id) keep (dense_rank last order by tal.itinerary_sequence) as last_flight_id,
   sum(ual.id) as sum_flight_ids,
   count(tal.itinerary_sequence) as num_flights
  from temp_americawest_itineraries tai
  join temp_americawest_legs tal
    on tal.itinerary_id = tai.itinerary_id
  join temp_unique_americawest_legs ual
    on ual.carrier = tal.carrier
    and ual.flight_number = tal.flight_number
    and ual.origin = tal.origin
    and ual.destination = tal.destination
    and ual.planned_departure_time = tal.planned_departure_time
    and ual.planned_arrival_time = tal.planned_arrival_time
  group by tai.itinerary_id, tai.year, tai.month, tai.day_of_month, tai.day_of_week
  having count(tal.itinerary_sequence) <= 3
 ) tin
 group by tin.year, tin.quarter, tin.month, tin.day_of_month,
   tin.day_of_week, tin.num_flights, tin.first_flight_id, tin.last_flight_id,
   tin.sum_flight_ids
) tal;

insert into airline_itineraries
select alm.itinerary_id, alm.year, alm.quarter, alm.month, 
  alm.day_of_month, alm.passengers, 1, 
  first.carrier, first.flight_number,
  first.planned_departure_time, first.planned_arrival_time,
  null, null, null, null, null, null,
  first.origin, null, first.destination,
  alm.day_of_week, null, null, 0
from temp_americawest_legs_merged alm
join temp_unique_americawest_legs first
  on first.id = alm.first_flight_id
where alm.num_flights = 1;

insert into airline_itineraries
select alm.itinerary_id, alm.year, alm.quarter, alm.month,
  alm.day_of_month, alm.passengers, 2,
  first.carrier, first.flight_number,
  first.planned_departure_time, first.planned_arrival_time,
  null,
  second.carrier, second.flight_number,
  second.planned_departure_time, second.planned_arrival_time,
  null,
  first.origin, first.destination, second.destination,
  alm.day_of_week, null, null, 0
from temp_americawest_legs_merged alm
join temp_unique_americawest_legs first
  on first.id = alm.first_flight_id
join temp_unique_americawest_legs second
  on second.id = alm.second_flight_id
where alm.num_flights = 2;

update airline_itineraries
  set trip_duration =
    (extract(day from (first_arrival_time - first_departure_time))) * 24 * 60 +
      (extract(hour from (first_arrival_time - first_departure_time))) * 60 +
      (extract(minute from (first_arrival_time - first_departure_time)))
where num_flights = 1 and first_arrival_time is not null
  and trip_duration is null;

update airline_itineraries
  set trip_duration =
    (extract(day from (second_arrival_time - first_departure_time))) * 24 * 60 +
      (extract(hour from (second_arrival_time - first_departure_time))) * 60 +
      (extract(minute from (second_arrival_time - first_departure_time)))
where num_flights = 2 and second_arrival_time is not null
  and trip_duration is null;

update airline_itineraries
set layover_duration =
  (extract(day from (second_departure_time - first_arrival_time))) * 24 * 60 +
    (extract(hour from (second_departure_time - first_arrival_time))) * 60 +
    (extract(minute from (second_departure_time - first_arrival_time)))
where num_flights = 2 and first_arrival_time is not null
  and layover_duration is null;

update airline_itineraries
set multi_day_flag = 1
where to_char(first_departure_time, 'DD') !=
  to_char(second_departure_time, 'DD')
  and multi_day_flag = 0;

create index idx_airline_itins_c1c2yq
  on airline_itineraries(first_carrier, second_carrier,
    year, quarter);

create index idx_airline_itins_nfft1
  on airline_itineraries(num_flights, first_flight_id);

create bitmap index bmx_airline_itins_nfft1ft2
  on airline_itineraries(num_flights, first_flight_id, second_flight_id);

select count(*)
from airline_itineraries
where num_flights = 1
and first_flight_id is not null;

select sum(passengers)
from airline_itineraries
where num_flights = 1
and first_flight_id is not null;

select count(*)
from airline_itineraries
where num_flights = 2
  and first_flight_id is not null
  and second_flight_id is not null;

select sum(passengers)
from airline_itineraries
where num_flights = 2
  and first_flight_id is not null
  and second_flight_id is not null;

select count(*)
from airline_itineraries
where num_flights = 2
  and first_flight_id is not null
  and second_flight_id is not null
  and layover_duration <= 300;

select sum(passengers)
from airline_itineraries
where num_flights = 2
  and first_flight_id is not null
  and second_flight_id is not null
  and layover_duration <= 300;

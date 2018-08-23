create table temp_itineraries
(
  num_flights number(1, 0) not null,
  first_flight_id number(12, 0) not null,
  second_flight_id number(12, 0)
);

drop table itineraries;

drop sequence itinerary_id_seq;

create sequence itinerary_id_seq
  start with 1
  increment by 1
  nomaxvalue;

create table itineraries
(
  id number(12, 0) not null primary key,
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  day_of_month number(2, 0) not null,
  day_of_week number(1, 0) not null,
  hour_of_day number(2, 0) not null,
  minutes_of_hour number(2, 0) not null,
  num_flights number(1, 0) not null,
  multi_carrier_flag number(1, 0) not null,
  first_operating_carrier char(2) not null,
  second_operating_carrier char(2), 
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  planned_departure_time timestamp with time zone not null,
  planned_arrival_time timestamp with time zone not null,
  layover_duration number(4, 0),
  first_flight_id number(12, 0) not null,
  second_flight_id number(12, 0)
)
partition by list (quarter)
(partition p_q1 values (1),
  partition p_q2 values (2),
  partition p_q3 values (3),
  partition p_q4 values (4)
);

-- Insert all non-stop itineraries
insert /*+ append */ into itineraries
select itinerary_id_seq.nextval,
  ft.year, ft.quarter, ft.month, ft.day_of_month,
  ft.day_of_week, ft.hour_of_day, ft.minutes_of_hour, 1, 
  0, ft.carrier, null, ft.origin, null, ft.destination,
  ft.planned_departure_time, ft.planned_arrival_time,
  null, ft.id, null
from temp_itineraries ti
join flights ft
  on ft.id = ti.first_flight_id
where ti.num_flights = 1;

-- Insert all one stop itineraries
insert /*+ append */ into itineraries
select itinerary_id_seq.nextval,
  ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  ft1.day_of_week, ft1.hour_of_day, ft1.minutes_of_hour, 2, 
  decode(num_flights, 2, 
    decode(second_operating_carrier, first_operating_carrier, 0, 1), 0),
  decode(ft1.day_of_month - ft2.day_of_month, 0, 0, 1),
  ft1.carrier, ft2.carrier, ft1.origin, ft1.destination,
  ft2.destination, ft1.planned_departure_time, ft2.planned_arrival_time,
  extract(hour from (ft2.planned_departure_time - ft1.planned_arrival_time)) * 60 +
    extract(minute from(ft2.planned_departure_time - ft1.planned_arrival_time)),
  ft1.id, ft2.id
from temp_itineraries ti
join flights ft1
  on ft1.id = ti.first_flight_id
join flights ft2
  on ft2.id = ti.second_flight_id
where ti.num_flights = 2;

-- General indices for querying itineraries
create bitmap index bmx_itineraries_ft1ft2
  on itineraries(first_flight_id, second_flight_id)
  local
  tablespace users;

create index idx_itineraries_c1c2
  on itineraries(first_operating_carrier, second_operating_carrier)
  local
  tablespace users;

create index idx_itineraries_c1c2ym
  on itineraries(first_operating_carrier, second_operating_carrier, year, month)
  local
  tablespace users;

-- The following index is used for passenger allocation
create bitmap index bmx_itineraries_c1ymmc
  on itineraries(first_operating_carrier, year, month, multi_carrier_flag)
  local
  tablespace users;

-- The following index is used for passenger delay calculation
create index idx_itineraries_c1c2ymdm
  on itineraries(first_operating_carrier, second_operating_carrier, year, month, day_of_month)
  local
  tablespace users;

create bitmap index bmx_itineraries_ymdm
  on itineraries(year, month, day_of_month)
  local
  tablespace users;

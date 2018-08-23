drop table temp_passenger_delays;

create table temp_passenger_delays
(
  num_passengers number(8, 4) not null,
  original_first_flight_id number(12, 0) not null,
  original_second_flight_id number(12, 0),
  first_disruption_cause number(1, 0) not null,
  first_disruption_hour number(2, 0),
  num_disruptions number(2, 0) not null,
  disruption_origin_sequence varchar2(40),
  disruption_cause_sequence varchar2(20),
  last_flown_flight_id number(12, 0),
  trip_delay number(8, 4) not null
);

create table passenger_delays
(
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  day_of_month number(2, 0) not null,
  planned_num_flights number(1, 0) not null,
  planned_multi_carrier number(1, 0) not null,
  planned_first_flight_id number(12, 0) not null,
  planned_second_flight_id number(12, 0),
  planned_first_carrier char(2) not null,
  planned_second_carrier char(2),
  planned_origin char(3) not null,
  planned_connection char(3),
  planned_destination char(3) not null,
  planned_departure_time timestamp with time zone not null,
  planned_arrival_time timestamp with time zone not null,
  num_passengers number(8, 4) not null,
  num_disruptions number(1, 0) not null,
  first_disruption_cause number(1, 0) not null,
  first_disruption_time timestamp with time zone,
  first_disruption_hour number(2, 0),
  disruption_origin_sequence varchar2(40),
  disruption_cause_sequence varchar2(20),
  last_flown_flight_id number(12, 0),
  trip_delay number(8, 4) not null
)
partition by list (quarter)
(partition p_q1 values (1),
  partition p_q2 values (2),
  partition p_q3 values (3),
  partition p_q4 values (4)
);

-- Insert all of the non-stop and one stop itineraries
insert /*+ append */ into passenger_delays
select ft.year, ft.quarter, ft.month, ft.day_of_month, 1, 0,
  ft.id, null, ft.carrier, null, ft.origin, null, ft.destination,
  ft.planned_departure_time, ft.planned_arrival_time,
  tpd.num_passengers, tpd.num_disruptions, tpd.first_disruption_cause,
  decode(tpd.first_disruption_cause, 2, ft.planned_departure_time, null),
  tpd.first_disruption_hour,
  tpd.disruption_origin_sequence, tpd.disruption_cause_sequence,
  tpd.last_flown_flight_id, tpd.trip_delay
from temp_passenger_delays tpd
join flights ft
  on ft.id = tpd.original_first_flight_id
where tpd.original_second_flight_id is null
union all
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month, 2,
  decode(ft2.carrier, ft1.carrier, 0, 1),
  ft1.id, ft2.id, ft1.carrier, ft2.carrier, ft1.origin, ft1.destination,
  ft2.destination, ft1.planned_departure_time, ft2.planned_arrival_time,
  tpd.num_passengers, tpd.num_disruptions, tpd.first_disruption_cause,
  decode(tpd.first_disruption_cause, 1, ft1.actual_arrival_time, 2, 
    decode(ft1.cancelled_flag + ft1.diverted_flag, 0,
      greatest(ft1.actual_arrival_time, ft2.planned_departure_time), 
      ft1.planned_departure_time), null),
  tpd.first_disruption_hour,
  tpd.disruption_origin_sequence, tpd.disruption_cause_sequence,
  tpd.last_flown_flight_id, tpd.trip_delay
from temp_passenger_delays tpd
join flights ft1
  on ft1.id = tpd.original_first_flight_id
join flights ft2
  on ft2.id = tpd.original_second_flight_id
where tpd.original_second_flight_id is not null;

commit;

create index idx_passenger_delays_c1c2ymdm
  on passenger_delays(planned_first_carrier, planned_second_carrier,
    year, month, day_of_month)
  local
  tablespace users;

create bitmap index bmx_passenger_delays_ymdm
  on passenger_delays(year, month, day_of_month)
  local
  tablespace users;

create index idx_passenger_delays_c1mc
  on passenger_delays(planned_first_carrier, planned_multi_carrier)
  tablespace users;

create bitmap index bmx_passenger_delays_c1ymdmmc
  on passenger_delays(planned_first_carrier, year, month,
    day_of_month, planned_multi_carrier)
  local
  tablespace users;

create index idx_passenger_delays_ft1ft2
  on passenger_delays(planned_first_flight_id, planned_second_flight_id)
  tablespace users;

commit;

-- Rename table and indices to create multiple samples
alter table passenger_delays
rename to validation_delays_618;

alter index idx_passenger_delays_c1c2ymdm
rename to idx_valid_delays_618_c1c2ymdm;

alter index bmx_passenger_delays_ymdm
rename to bmx_valid_delays_618_ymdm;

alter index idx_passenger_delays_c1mc
rename to idx_valid_delays_618_c1mc;

alter index bmx_passenger_delays_c1ymdmmc
rename to bmx_valid_delays_618_c1ymdmmc;

alter index idx_passenger_delays_ft1ft2
rename to idx_valid_delays_618_ft1ft2;

commit;


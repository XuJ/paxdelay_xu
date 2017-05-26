drop table etms;

create table etms
(
  aircraft_id varchar2(8) not null,
  iata_aircraft_code char(4),
  origin varchar2(4) not null,
  destination varchar2(4) not null,
  planned_departure_time_gmt varchar2(19),
  planned_arrival_time_gmt varchar2(19),
  actual_departure_time_gmt varchar2(19) not null,
  actual_arrival_time_gmt varchar2(19),
  planned_departure_time_local varchar2(19),
  planned_arrival_time_local varchar2(19),
  actual_departure_time_local varchar2(19) not null,
  actual_arrival_time_local varchar2(19),
  departure_flag number(1, 0),
  arrival_flag number(1, 0),
  flew_flag number(1, 0),
  mg_flag char(1)
);

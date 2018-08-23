create table pax_delay_analysis
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
  planned_layover_duration number(4, 0),
  actual_layover_duration number(4, 0),
  planned_departure_time timestamp with time zone not null,
  planned_connection_time timestamp with time zone,
  actual_connection_time timestamp with time zone,
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

commit;

insert /*+ append */ into pax_delay_analysis
select pd.year, pd.quarter, pd.month, pd.day_of_month,
  pd.planned_num_flights, pd.planned_multi_carrier,
  pd.planned_first_flight_id, pd.planned_second_flight_id, 
  pd.planned_first_carrier, pd.planned_second_carrier,
  pd.planned_origin, pd.planned_connection, pd.planned_destination,
  decode(ft2.planned_departure_time, null, null,
    extract(day from 
        ft2.planned_departure_time - ft1.planned_arrival_time) * 24 * 60 +
      extract(hour from 
          ft2.planned_departure_time - ft1.planned_arrival_time) * 60 +
        extract(minute from
            ft2.planned_departure_time - ft1.planned_arrival_time))
    as planned_layover_duration,
  decode(ft2.actual_departure_time, null, null,
    decode(ft1.actual_arrival_time, null, null,
      extract(day from
          ft2.actual_departure_time - ft1.actual_arrival_time) * 24 * 60 +
        extract(hour from
            ft2.actual_departure_time - ft1.actual_arrival_time) * 60 +
          extract(minute from
              ft2.actual_departure_time - ft1.actual_arrival_time)))
    as actual_layover_duration,
  pd.planned_departure_time, 
  ft2.planned_departure_time as planned_connection_time,
  ft2.actual_departure_time as actual_connection_time,
  pd.planned_arrival_time,
  pd.num_passengers, pd.num_disruptions,
  pd.first_disruption_cause, pd.first_disruption_time,
  pd.first_disruption_hour, pd.disruption_origin_sequence,
  pd.disruption_cause_sequence, pd.last_flown_flight_id,
  pd.trip_delay
from passenger_delays_618 pd
join flights ft1
  on ft1.id = pd.planned_first_flight_id
left join flights ft2
  on ft2.id = pd.planned_second_flight_id
order by pd.year, pd.quarter, pd.month,
  pd.planned_first_carrier, pd.planned_second_carrier;

commit;

create index idx_pdelay_analysis_c1c2ymdm
  on pax_delay_analysis(planned_first_carrier, planned_second_carrier,
    year, month, day_of_month)
  local
  tablespace users;

create bitmap index bmx_pdelay_analysis_ymdm
  on pax_delay_analysis(year, month, day_of_month)
  local
  tablespace users;

create index idx_pdelay_analysis_c1mc
  on pax_delay_analysis(planned_first_carrier, planned_multi_carrier)
  tablespace users;

create bitmap index bmx_pdelay_analysis_c1ymdmmc
  on pax_delay_analysis(planned_first_carrier, year, month,
    day_of_month, planned_multi_carrier)
  local
  tablespace users;

create index idx_pdelay_analysis_ft1ft2
  on pax_delay_analysis(planned_first_flight_id, planned_second_flight_id)
  tablespace users;

create index idx_pdelay_analysis_c1cmld
  on pax_delay_analysis(planned_first_carrier, planned_connection, month,
    planned_layover_duration)
  tablespace users;

commit;

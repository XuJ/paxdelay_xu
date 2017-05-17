-- XuJiao052017
-- create pax_delay_analysis_MIT table

drop table if exists pax_delay_analysis_MIT;

create table pax_delay_analysis_MIT 
(
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  planned_num_flights numeric(1) not null,
  planned_multi_carrier numeric(1) not null,
  planned_first_flight_id numeric(12) not null,
  planned_second_flight_id numeric(12),
  planned_first_carrier char(6) not null,
  planned_second_carrier char(6),
  planned_origin char(3) not null,
  planned_connection char(3),
  planned_destination char(3) not null,
  planned_layover_duration numeric(4),
  actual_layover_duration numeric(4),
  planned_departure_time varchar(60) not null,
  planned_connection_time varchar(60),
  actual_connection_time varchar(60),
  planned_arrival_time varchar(60) not null,
  num_passengers numeric(8, 4) not null,
  num_disruptions numeric(1) not null,
  first_disruption_cause numeric(1) not null,
  first_disruption_time varchar(40),
  first_disruption_hour numeric(2),
  disruption_origin_sequence varchar(40),
  disruption_cause_sequence varchar(20),
  last_flown_flight_id numeric(12),
  trip_delay numeric(8, 4) not null,
  random_num float 
) 
partition by list (quarter)
(       partition p_q1 values in (1),
        partition p_q2 values in (2),
        partition p_q3 values in (3),
        partition p_q4 values in (4)
);


LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/bts_raw_csv/pax_delay_analysis_MIT.csv'
INTO TABLE pax_delay_analysis_MIT 
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  year,
  quarter,
  month,
  day_of_month,
  planned_num_flights,
  planned_multi_carrier,
  planned_first_flight_id,
  @vplanned_second_flight_id,
  planned_first_carrier,
  @vplanned_second_carrier,
  planned_origin,
  @vplanned_connection,
  planned_destination,
  @vplanned_layover_duration,
  @vactual_layover_duration,
  planned_departure_time,
  @vplanned_connection_time,
  @vactual_connection_time,
  planned_arrival_time,
  num_passengers,
  num_disruptions,
  first_disruption_cause,
  @vfirst_disruption_time,
  @vfirst_disruption_hour,
  @vdisruption_origin_sequence,
  @vdisruption_cause_sequence,
  @vlast_flown_flight_id,
  trip_delay,
  @vrandom_num
)

set 
planned_second_flight_id = nullif(@vplanned_second_flight_id,''),
planned_second_carrier = nullif(@vplanned_second_carrier,''),
planned_connection = nullif(@vplanned_connection,''),
planned_layover_duration = nullif(@vplanned_layover_duration,''),
actual_layover_duration = nullif(@vactual_layover_duration,''),
planned_connection_time = nullif(@vplanned_connection_time,''),
actual_connection_time = nullif(@vactual_connection_time,''),
first_disruption_time = nullif(@vfirst_disruption_time,''),
first_disruption_hour = nullif(@vfirst_disruption_hour,''),
disruption_origin_sequence = nullif(@vdisruption_origin_sequence,''),
disruption_cause_sequence = nullif(@vdisruption_cause_sequence,''),
last_flown_flight_id = nullif(@vlast_flown_flight_id,''),
random_num = nullif(@vrandom_num,'');


-- XuJ 051217: change oracle timestamp with timezone into mysql datetime and timezone (maybe need to convert_tz to UTC)
-- select str_to_date(concat(substr('10-JAN-07 07.55.00.000000000 PM AMERICA/CHICAGO',1,18),substr('10-JAN-07 07.55.00.000000000 PM AMERICA/CHICAGO',29,3)),'%d-%b-%y %h.%i.%s %p');
-- select substr('10-JAN-07 07.55.00.000000000 PM AMERICA/CHICAGO' from 33);
-- XuJ 051217: may not need this conversion, for in DisruptionFeaturesGenerator.java only need hour

create index idx_pdelay_analysis_c1c2ymdm_MIT 
  on pax_delay_analysis_MIT(planned_first_carrier, planned_second_carrier, year, month, day_of_month);

create index idx_pdelay_analysis_ymdm_MIT 
  on pax_delay_analysis_MIT(year, month, day_of_month);

create index idx_pdelay_analysis_c1mc_MIT 
  on pax_delay_analysis_MIT(planned_first_carrier, planned_multi_carrier);

create index idx_pdelay_analysis_c1ymdmmc_MIT 
  on pax_delay_analysis_MIT(planned_first_carrier, year, month, day_of_month, planned_multi_carrier);

create index idx_pdelay_analysis_ft1ft2_MIT 
  on pax_delay_analysis_MIT(planned_first_flight_id, planned_second_flight_id);

create index idx_pdelay_analysis_c1cmld_MIT 
  on pax_delay_analysis_MIT(planned_first_carrier, planned_connection, month, planned_layover_duration);


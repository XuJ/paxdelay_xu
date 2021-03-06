drop table if exists continental_delays;
create table continental_delays (
year int(4) not null,
quarter int(1) not null,
month int(2) not null,
day_of_month int(2) not null,
planned_num_flights int(1) not null,
planned_multi_carrier int(1) not null,
planned_first_flight_id bigint(12) not null,
planned_second_flight_id bigint(12) null,
planned_first_carrier char(2) binary not null,
planned_second_carrier char(2) binary null,
planned_origin char(3) binary not null,
planned_connection char(3) binary null,
planned_destination char(3) binary not null,
planned_departure_time datetime not null,
planned_arrival_time datetime not null,
num_passengers decimal(8, 4) not null,
num_disruptions int(1) not null,
first_disruption_cause int(1) not null,
first_disruption_time datetime null,
first_disruption_hour int(2) null,
disruption_origin_sequence varchar(40) binary null,
disruption_cause_sequence varchar(20) binary null,
last_flown_flight_id bigint(12) null,
trip_delay decimal(8, 4) not null,
index bmx_cont_delays_c1ymdmmc (planned_first_carrier(2), year, month, day_of_month, planned_multi_carrier),
index bmx_cont_delays_ymdm (year, month, day_of_month),
index idx_cont_delays_c1c2ymdm (planned_first_carrier(2), planned_second_carrier(2), year, month, day_of_month),
index idx_cont_delays_c1mc (planned_first_carrier(2), planned_multi_carrier),
index idx_cont_delays_ft1ft2 (planned_first_flight_id, planned_second_flight_id)
)
engine = MyISAM;
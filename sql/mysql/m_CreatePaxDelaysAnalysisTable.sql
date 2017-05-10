-- XuJiao022017
-- Remove all _UTC
-- That took 63 min

drop table if exists pax_delay_analysis;

create table pax_delay_analysis
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

	-- XuJ 051017: change time format from date to datetime
	-- planned_departure_time date,
	planned_departure_time datetime,
	planned_departure_tz char(19),
	planned_departure_local_hour numeric(2),

	-- planned_connection_time date,
	planned_connection_time datetime,
	planned_connection_tz char(19),	
	planned_connection_local_hour numeric(2),

	-- actual_connection_time date,
	actual_connection_time datetime,
	actual_connection_tz char(19),
	actual_connection_local_hour numeric(2),

	-- planned_arrival_time date,
	planned_arrival_time datetime,
	planned_arrival_tz char(19),
	planned_arrival_local_hour numeric(2),

  num_passengers numeric(8, 4) not null,
  num_disruptions numeric(1) not null,
  first_disruption_cause numeric(1) not null,

	-- first_disruption_time date,
	first_disruption_time datetime,
  first_disruption_hour numeric(2),
  disruption_origin_sequence varchar(40),
  disruption_cause_sequence varchar(20),
  last_flown_flight_id numeric(12),
  trip_delay numeric(8, 4) not null
)
partition by list (quarter)
(	partition p_q1 values in (1),
	partition p_q2 values in (2),
	partition p_q3 values in (3),
	partition p_q4 values in (4)
);


insert into pax_delay_analysis
select pd.year, pd.quarter, pd.month, pd.day_of_month,
  pd.planned_num_flights, pd.planned_multi_carrier,
  pd.planned_first_flight_id, pd.planned_second_flight_id, 
  pd.planned_first_carrier, pd.planned_second_carrier,
  pd.planned_origin, pd.planned_connection, pd.planned_destination,

	case when ft2.planned_departure_time is null then null
		-- XuJ 05/01/2017: Change the sequence of times so that layover_duration is positive
		-- else TIMESTAMPDIFF(minute, ft2.planned_departure_time, ft1.planned_arrival_time)
		else TIMESTAMPDIFF(minute, ft1.planned_arrival_time, ft2.planned_departure_time)
		end as planned_layover_duration,

	case when ft2.actual_departure_time is null then null
			-- else TIMESTAMPDIFF(minute, ft2.actual_departure_time, ft1.actual_arrival_time)
			else TIMESTAMPDIFF(minute, ft1.actual_arrival_time, ft2.actual_departure_time)
			end as actual_layover_duration,

	pd.planned_departure_time as planned_departure_time,
	pd.planned_departure_tz as planned_departure_tz,
	pd.planned_departure_local_hour as planned_departure_local_hour ,

	ft2.planned_departure_time as planned_connection_time,
	ft2.planned_departure_tz as planned_connection_tz,
	ft2.planned_departure_local_hour as planned_connection_local_hour,


	ft2.actual_departure_time as actual_connection_time,
	ft2.actual_departure_tz as actual_connection_tz,
	ft2.actual_departure_local_hour as actual_connection_local_hour,

	pd.planned_arrival_time as planned_arrival_time,
	pd.planned_arrival_tz as planned_arrival_tz,
	pd.planned_arrival_local_hour as planned_arrival_local_hour,

  pd.num_passengers, pd.num_disruptions,
  pd.first_disruption_cause, pd.first_disruption_time,
  pd.first_disruption_hour, pd.disruption_origin_sequence,
  pd.disruption_cause_sequence, pd.last_flown_flight_id,
  pd.trip_delay
from passenger_delays pd
join flights ft1 on ft1.id = pd.planned_first_flight_id
left join flights ft2 on ft2.id = pd.planned_second_flight_id
order by pd.year, pd.quarter, pd.month, pd.planned_first_carrier, pd.planned_second_carrier;


create index idx_pdelay_analysis_c1c2ymdm
  on pax_delay_analysis(planned_first_carrier, planned_second_carrier, year, month, day_of_month);

create index idx_pdelay_analysis_ymdm
  on pax_delay_analysis(year, month, day_of_month);

create index idx_pdelay_analysis_c1mc
  on pax_delay_analysis(planned_first_carrier, planned_multi_carrier);

create index idx_pdelay_analysis_c1ymdmmc
  on pax_delay_analysis(planned_first_carrier, year, month, day_of_month, planned_multi_carrier);

create index idx_pdelay_analysis_ft1ft2
  on pax_delay_analysis(planned_first_flight_id, planned_second_flight_id);

create index idx_pdelay_analysis_c1cmld
  on pax_delay_analysis(planned_first_carrier, planned_connection, month, planned_layover_duration);


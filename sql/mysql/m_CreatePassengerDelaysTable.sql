-- XuJiao 041217
-- That took 70 min
-- Records: 81,254,021

drop table if exists passenger_delays;

create table passenger_delays
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

  planned_departure_time date not null,
  planned_departure_tz char(15),
  planned_departure_local_hour numeric(2),

  planned_arrival_time date not null,
  planned_arrival_tz char(15),
  planned_arrival_local_hour numeric(2),

  num_passengers numeric(8, 4) not null,
  num_disruptions numeric(1) not null,
  first_disruption_cause numeric(1) not null,

  first_disruption_time date,

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

-- Insert all of the non-stop and one stop itineraries
insert into passenger_delays
select 
	ft.year as year, 
	ft.quarter as quarter, 
	ft.month as month, 
	ft.day_of_month as day_of_month, 
	1 as planned_num_flights, 
	0 as planned_multi_carrier,
	ft.id as planned_first_flight_id, 
	null as planned_second_flight_id, 
	ft.carrier as planned_first_carrier, 
	null as planned_second_carrier, 
	ft.origin as planned_origin, 
	null as planned_connection, 
	ft.destination as planned_destination,

	ft.planned_departure_time as planned_departure_time, 
	ft.planned_departure_tz as planned_departure_tz, 
	ft.planned_departure_local_hour as planned_departure_local_hour,
	
	ft.planned_arrival_time as planned_arrival_time, 
	ft.planned_arrival_tz as planned_arrival_tz, 
	ft.planned_arrival_local_hour as planned_arrival_local_hour,

	tpd.num_passengers as num_passengers, 
	tpd.num_disruptions as num_disruptions, 
	tpd.first_disruption_cause as first_disruption_cause,

	case when tpd.first_disruption_cause = 2
		then ft.planned_departure_time
		else null
		end as first_disruption_time,

	tpd.first_disruption_hour as first_disruption_hour,
	tpd.disruption_origin_sequence as disruption_origin_sequence, 
	tpd.disruption_cause_sequence as disruption_cause_sequence,
	tpd.last_flown_flight_id as last_flown_flight_id, 
	tpd.trip_delay as trip_delay
from temp_passenger_delays tpd
join flights ft 
	on ft.id = tpd.original_first_flight_id
where tpd.original_second_flight_id is null
	or tpd.original_second_flight_id = ""
union all
select ft1.year as year, 
	ft1.quarter as quarter, 
	ft1.month as month, 
	ft1.day_of_month as day_of_month, 
	2 as planned_num_flights,
	case when ft2.carrier = ft1.carrier
		then 0
		else 1
		end as planned_multi_carrier,

	ft1.id as planned_first_flight_id, 
	ft2.id as planned_second_flight_id, 
	ft1.carrier as planned_first_carrier, 
	ft2.carrier as planned_second_carrier, 
	ft1.origin as planned_origin, 
	ft1.destination as planned_connection,
	ft2.destination as planned_destination, 

	ft1.planned_departure_time as planned_departure_time, 
	ft1.planned_departure_tz as planned_departure_tz, 
	ft1.planned_departure_local_hour as planned_departure_local_hour,
	
	ft2.planned_arrival_time as planned_arrival_time, 
	ft2.planned_arrival_tz as planned_arrival_tz, 
	ft2.planned_arrival_local_hour as planned_arrival_local_hour,

	tpd.num_passengers as num_passengers, 
	tpd.num_disruptions as num_disruptions, 
	tpd.first_disruption_cause as first_disruption_cause,

	case	when tpd.first_disruption_cause = 1 
			then ft1.actual_arrival_time
		when tpd.first_disruption_cause = 2 
			then case when (ft1.cancelled_flag + ft1.diverted_flag) = 0 
					then greatest(ft1.actual_arrival_time, ft2.planned_departure_time)
					else ft1.planned_departure_time
				end
			else null
	end as first_disruption_time,
  tpd.first_disruption_hour as first_disruption_hour,
  tpd.disruption_origin_sequence as disruption_origin_sequence, tpd.disruption_cause_sequence as disruption_cause_sequence,
  tpd.last_flown_flight_id as last_flown_flight_id, tpd.trip_delay as trip_delay
from temp_passenger_delays tpd
join flights ft1 
	on ft1.id = tpd.original_first_flight_id
join flights ft2 
	on ft2.id = tpd.original_second_flight_id
where tpd.original_second_flight_id is not null
	and tpd.original_second_flight_id != "";
-- 1,015,085


create index idx_passenger_delays_c1c2ymdm
  on passenger_delays(planned_first_carrier, planned_second_carrier, year, month, day_of_month);

create index idx_passenger_delays_ymdm
  on passenger_delays(year, month, day_of_month);

create index idx_passenger_delays_c1mc
  on passenger_delays(planned_first_carrier, planned_multi_carrier);

create index idx_passenger_delays_c1ymdmmc
  on passenger_delays(planned_first_carrier, year, month, day_of_month, planned_multi_carrier);

create index idx_passenger_delays_ft1ft2
  on passenger_delays(planned_first_flight_id, planned_second_flight_id);

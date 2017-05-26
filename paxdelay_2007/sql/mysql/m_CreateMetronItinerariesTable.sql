drop table if exists metron_itineraries;

create table metron_itineraries
(
  id integer not null auto_increment, primary key (id),
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  day_of_week numeric(1) not null,
  hour_of_day numeric(2) not null,
  minutes_of_hour numeric(2) not null,
  num_flights numeric(1) not null,
  multi_carrier_flag numeric(1) not null,
  first_operating_carrier char(6) not null,
  second_operating_carrier char(6), 
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  planned_departure_time_UTC date,
  planned_departure_tz char(19),
  planned_departure_local_hour numeric(2),

  planned_arrival_time_UTC date,
  planned_arrival_tz char(19),
  planned_arrival_local_hour numeric(2),
  layover_duration numeric(4),
  first_flight_id numeric(12) not null,
  second_flight_id numeric(12)
);

-- Insert all non-stop metron_itineraries
insert  into metron_itineraries
(
year, quarter, month, day_of_month, day_of_week, hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag, first_operating_carrier, second_operating_carrier, origin, connection,
destination, planned_departure_time_UTC, planned_departure_tz, planned_departure_local_hour, planned_arrival_time_UTC, planned_arrival_tz, planned_arrival_local_hour,
layover_duration, first_flight_id, second_flight_id)
select 
  ft.year, ft.quarter, ft.month, ft.day_of_month,
  ft.day_of_week, ft.hour_of_day, ft.minutes_of_hour, 1, 
  0, ft.carrier, null, ft.origin, null, ft.destination,
  ft.planned_departure_time_UTC,
  ft.planned_departure_tz,
  ft.planned_departure_local_hour,

  ft.planned_arrival_time_UTC,
  ft.planned_arrival_tz,
  ft.planned_arrival_local_hour,
  null, ft.id, null
from temp_metron_itineraries ti
join metron_flights ft on ft.id = ti.first_flight_id 
where ti.num_flights = 1;

-- Insert all one stop metron_itineraries
insert  into metron_itineraries
(
year, quarter, month, day_of_month, day_of_week, hour_of_day, minutes_of_hour, num_flights, multi_carrier_flag, first_operating_carrier, second_operating_carrier, origin, connection,
destination, planned_departure_time_UTC, planned_departure_tz, planned_departure_local_hour, planned_arrival_time_UTC, planned_arrival_tz, planned_arrival_local_hour,
layover_duration, first_flight_id, second_flight_id)
select 
  ft1.year, ft1.quarter, ft1.month, ft1.day_of_month,
  ft1.day_of_week, ft1.hour_of_day, ft1.minutes_of_hour, 2, 
		case when num_flights = 2 
			then case when ft2.carrier = ft1.carrier then 0 else 1 end
			else 0
		end, 

  ft1.carrier, ft2.carrier, ft1.origin, ft1.destination,
  ft2.destination, 
  ft1.planned_departure_time_UTC,
  ft1.planned_departure_tz,
  ft1.planned_departure_local_hour,

  ft2.planned_arrival_time_UTC,
  ft2.planned_arrival_tz,
  ft2.planned_arrival_local_hour,
	TIMESTAMPDIFF(minute, ft2.planned_departure_time_UTC,  ft1.planned_arrival_time_UTC),
  ft1.id, ft2.id
from temp_metron_itineraries ti
join metron_flights ft1 on ft1.id = ti.first_flight_id
join metron_flights ft2 on ft2.id = ti.second_flight_id
where ti.num_flights = 2;
-- 489,831

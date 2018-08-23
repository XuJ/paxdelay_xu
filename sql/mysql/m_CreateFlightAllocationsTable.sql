drop table if exists flight_disruptions;

create table flight_disruptions
(
  flight_id numeric(12, 3) not null,
  year numeric(4) not null,
  quarter int not null,
  month numeric(4) not null,
  day_of_month numeric(4) not null,
  carrier char(2) not null,
  flight_numeric char(4) not null,
  cancelled_flag numeric(1) not null,
  total_delay numeric(4),
  seating_capacity numeric(3),
  nonstop_passengers numeric(3) not null,
  first_leg_passengers numeric(3) not null,
  second_leg_passengers numeric(3) not null,
  missed_connections_after numeric(3),
  missed_connections_before numeric(3)
);

insert into flight_disruptions
select ft.id, ft.year, ft.quarter, ft.month,
  ft.day_of_month, ft.carrier, ft.flight_number,
  ft.cancelled_flag,

	case when ft.cancelled_flag = 1 then null
			else greatest(0, TIMESTAMPDIFF(minute, ft.actual_arrival_time_UTC, ft.planned_arrival_time_UTC))
		end as total_delay,

  ft.seating_capacity,
	
  ifnull(nonstop.passengers, 0),
  ifnull(leg1.passengers, 0),
  ifnull(leg2.passengers, 0),

	case when ft.cancelled_flag =1 then null
			else ifnull(leg1.missed_connections, 0)	
		end as missed_connections_after,

	case when ft.cancelled_flag =1 then null
			else ifnull(leg2.missed_connections, 0)	
		end as missed_connections_before
  
from flights ft
left join
(select id.first_flight_id as flight_id, 
   sum(id.passengers) as passengers
 from itinerary_disruptions id
 where id.num_flights = 1
 group by id.first_flight_id) nonstop on nonstop.flight_id = ft.id
left join
(select id.first_flight_id as flight_id,
   sum(id.passengers) as passengers,
   sum(case when id.missed_connection = 1 then id.passengers else 0 end)  as missed_connections
 from itinerary_disruptions id
 where id.num_flights = 2
 group by id.first_flight_id) leg1 on leg1.flight_id = ft.id
left join 
(select id.second_flight_id as flight_id,
   sum(id.passengers) as passengers,
   sum(case when id.missed_connection = 1 then id.missed_connection else 0	end ) as missed_connections
 from itinerary_disruptions id
 where id.num_flights = 2
 group by id.second_flight_id) leg2
on leg2.flight_id = ft.id;


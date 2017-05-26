drop table if exists itinerary_disruptions;

create table itinerary_disruptions
(
  num_flights numeric(1) not null,
  first_flight_id numeric(12) not null,
  first_flight_cancelled numeric(1) not null,
  second_flight_id numeric(12),
  second_flight_cancelled numeric(1),
  passengers numeric(3) not null,
  missed_connection numeric(1)
);

insert into itinerary_disruptions
select ia.num_flights,
  ia.first_flight_id, ft1.cancelled_flag,
  ia.second_flight_id, ft2.cancelled_flag,
  ia.passengers,

	case when ia.num_flights = 1 then null
		when ft1.cancelled_flag = 1 then null
		when ft2.cancelled_flag = 1 then null
		when least(date_add(ft1.actual_arrival_time_UTC, interval 14 minute), ft2.actual_departure_time_UTC) = ft2.actual_departure_time_UTC then 1
		else 0
	end as missed_connection
from itinerary_allocations ia
join flights ft1 on ft1.id = ia.first_flight_id
left join flights ft2 on ft2.id = ia.second_flight_id;

create index idx_itin_disrupts_ft1
  on itinerary_disruptions(first_flight_id);

create index idx_itin_disrupts_ft2
  on itinerary_disruptions(second_flight_id);

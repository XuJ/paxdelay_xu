drop table flight_disruptions;

create table flight_disruptions
(
  flight_id number(12, 3) not null,
  year number(4, 0) not null,
  quarter number(4, 0) not null,
  month number(4, 0) not null,
  day_of_month number(4, 0) not null,
  carrier char(2) not null,
  flight_number char(4) not null,
  cancelled_flag number(1, 0) not null,
  total_delay number(4, 0),
  seating_capacity number(3, 0),
  nonstop_passengers number(3, 0) not null,
  first_leg_passengers number(3, 0) not null,
  second_leg_passengers number(3, 0) not null,
  missed_connections_after number(3, 0),
  missed_connections_before number(3, 0)
);

insert into flight_disruptions
select ft.id, ft.year, ft.quarter, ft.month,
  ft.day_of_month, ft.carrier, ft.flight_number,
  ft.cancelled_flag,
  decode(ft.cancelled_flag, 1, null,
    greatest(0, 
      extract(day from actual_arrival_time - planned_arrival_time) * 60 * 24 +
        extract(hour from actual_arrival_time - planned_arrival_time) * 60 +
          extract(minute from actual_arrival_time - planned_arrival_time)))
    as total_delay,
  ft.seating_capacity,
  nvl(nonstop.passengers, 0),
  nvl(leg1.passengers, 0),
  nvl(leg2.passengers, 0),
  decode(ft.cancelled_flag, 1, null, nvl(leg1.missed_connections, 0)),
  decode(ft.cancelled_flag, 1, null, nvl(leg2.missed_connections, 0))
from flights ft
left join
(select id.first_flight_id as flight_id, 
   sum(id.passengers) as passengers
 from itinerary_disruptions id
 where id.num_flights = 1
 group by id.first_flight_id) nonstop
on nonstop.flight_id = ft.id
left join
(select id.first_flight_id as flight_id,
   sum(id.passengers) as passengers,
   sum(decode(id.missed_connection, 1, id.passengers, 0))
     as missed_connections
 from itinerary_disruptions id
 where id.num_flights = 2
 group by id.first_flight_id) leg1
on leg1.flight_id = ft.id
left join 
(select id.second_flight_id as flight_id,
   sum(id.passengers) as passengers,
   sum(decode(id.missed_connection, 1, id.passengers, 0))
     as missed_connections
 from itinerary_disruptions id
 where id.num_flights = 2
 group by id.second_flight_id) leg2
on leg2.flight_id = ft.id;

commit;

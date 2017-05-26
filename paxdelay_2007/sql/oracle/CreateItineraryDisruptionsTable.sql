drop table itinerary_disruptions;

create table itinerary_disruptions
(
  num_flights number(1, 0) not null,
  first_flight_id number(12, 0) not null,
  first_flight_cancelled number(1, 0) not null,
  second_flight_id number(12, 0),
  second_flight_cancelled number(1, 0),
  passengers number(3, 0) not null,
  missed_connection number(1, 0)
);

insert into itinerary_disruptions
select ia.num_flights,
  ia.first_flight_id, ft1.cancelled_flag,
  ia.second_flight_id, ft2.cancelled_flag,
  ia.passengers,
  decode(ia.num_flights, 1, null,
    decode(ft1.cancelled_flag, 1, null,
      decode(ft2.cancelled_flag, 1, null,
        decode(least(ft1.actual_arrival_time + interval '14' minute,
          ft2.actual_departure_time), ft2.actual_departure_time, 1, 0))))
    as missed_connection
from itinerary_allocations_61 ia
join flights ft1
on ft1.id = ia.first_flight_id
left join flights ft2
on ft2.id = ia.second_flight_id;

commit;

create index idx_itin_disrupts_ft1
  on itinerary_disruptions(first_flight_id)
  tablespace users;

create index idx_itin_disrupts_ft2
  on itinerary_disruptions(second_flight_id)
  tablespace users;

commit;

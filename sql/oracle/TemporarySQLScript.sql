create bitmap index bmx_itinerary_alloc_ft1ft2
  on itinerary_allocations(first_flight_id, second_flight_id)
  local
  tablespace users;

commit;

insert into passenger_delays
select ft.year, ft.quarter, ft.month, ft.day_of_month, 1,
  ft.id, null, ft.carrier, null, ft.origin, null, ft.destination,
  ft.planned_departure_time, ft.planned_arrival_time,
  tpd.num_passengers, tpd.trip_delay
from temp_passenger_delays tpd
join flights ft
  on ft.id = tpd.original_first_flight_id
where tpd.original_second_flight_id is null
union all
select ft1.year, ft1.quarter, ft1.month, ft1.day_of_month, 2,
  ft1.id, ft2.id, ft1.carrier, ft2.carrier, ft1.origin, ft1.destination,
  ft2.destination, ft1.planned_departure_time, ft2.planned_arrival_time,
  tpd.num_passengers, tpd.trip_delay
from temp_passenger_delays tpd
join flights ft1
  on ft1.id = tpd.original_first_flight_id
join flights ft2
  on ft2.id = tpd.original_second_flight_id
where tpd.original_second_flight_id is not null;

commit;

create index idx_passenger_delays_c1c2ymdm
  on passenger_delays(planned_first_carrier, planned_second_carrier,
    year, month, day_of_month)
  local
  tablespace users;

commit;

create bitmap index bmx_passenger_delays_ft1ft2
  on passenger_delays(planned_first_flight_id, planned_second_flight_id)
  local
  tablespace users;

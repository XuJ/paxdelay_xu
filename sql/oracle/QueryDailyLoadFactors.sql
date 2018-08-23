select ft.carrier, ft.month, ft.day_of_month,
  sum(al.passengers) / sum(ft.seating_capacity) as load_factor
from
(select flight_id, sum(passengers) as passengers
 from
 (select first_flight_id as flight_id, passengers
  from validation_allocations_6
  union all
  select second_flight_id as flight_id, passengers
  from validation_allocations_6
  where second_flight_id is not null
 )
 group by flight_id
) al
join flights ft
on ft.id = al.flight_id
where ft.seating_capacity is not null
group by ft.carrier, ft.month, ft.day_of_month;
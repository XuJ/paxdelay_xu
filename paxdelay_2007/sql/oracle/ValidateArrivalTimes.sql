-- Validate planned arrival times
select ft.id, ft.planned_departure_time, 
ft.planned_arrival_time, ot.planned_elapsed_time,
  extract(day from (ft.planned_arrival_time - ft.planned_departure_time)) * 24 * 60 +
    extract(hour from (ft.planned_arrival_time - ft.planned_departure_time)) * 60 +
    extract(minute from (ft.planned_arrival_time - ft.planned_departure_time))
  as calculated_elapsed_time
from flights ft
join aotp ot
  on ot.carrier = ft.carrier
  and ot.flight_number = ft.flight_number
  and ot.origin = ft.origin
  and ot.destination = ft.destination
  and ot.year = ft.year
  and ot.quarter = ft.quarter
  and ot.month = ft.month
  and ot.day_of_month = ft.day_of_month
  and concat(substr(ot.planned_departure_time, 1, 2),
    concat(':', substr(ot.planned_departure_time, 3, 2))) = 
    to_char(ft.planned_departure_time, 'HH24:MI')
where ot.planned_elapsed_time is not null
  and ot.planned_elapsed_time !=
  extract(day from (ft.planned_arrival_time - ft.planned_departure_time)) * 24 * 60 +
    extract(hour from (ft.planned_arrival_time - ft.planned_departure_time)) * 60 +
      extract(minute from (ft.planned_arrival_time - ft.planned_departure_time));

-- Validate actual departure times
select ft.id, ft.planned_departure_time,
ft.actual_departure_time, ot.actual_departure_time, ot.departure_offset,
  extract(day from (ft.actual_departure_time - ft.planned_departure_time)) * 24 * 60 +
    extract(hour from (ft.actual_departure_time - ft.planned_departure_time)) * 60 +
    extract(minute from (ft.actual_departure_time - ft.planned_departure_time))
  as calculated_departure_offset
from flights ft
join aotp ot
  on ot.carrier = ft.carrier
  and ot.flight_number = ft.flight_number
  and ot.origin = ft.origin
  and ot.destination = ft.destination
  and ot.year = ft.year
  and ot.quarter = ft.quarter
  and ot.month = ft.month
  and ot.day_of_month = ft.day_of_month
  and concat(substr(ot.planned_departure_time, 1, 2),
    concat(':', substr(ot.planned_departure_time, 3, 2))) =
    to_char(ft.planned_departure_time, 'HH24:MI')
where ot.actual_departure_time is not null
  and ot.departure_offset !=
  extract(day from (ft.actual_departure_time - ft.planned_departure_time)) * 24 * 60 +
    extract(hour from (ft.actual_departure_time - ft.planned_departure_time)) * 60 +
      extract(minute from (ft.actual_departure_time - ft.planned_departure_time));

-- Validate actual arrival times
select ft.id, ft.planned_departure_time, 
ft.actual_arrival_time, ot.actual_arrival_time,
ot.planned_elapsed_time + ot.arrival_offset
  extract(day from (ft.actual_arrival_time - ft.planned_departure_time)) * 24 * 60 +
    extract(hour from (ft.actual_arrival_time - ft.planned_departure_time)) * 60 +
    extract(minute from (ft.actual_arrival_time - ft.planned_departure_time))
  as calculated_departure_offset
from flights ft
join aotp ot
  on ot.carrier = ft.carrier
  and ot.flight_number = ft.flight_number
  and ot.origin = ft.origin
  and ot.destination = ft.destination
  and ot.year = ft.year
  and ot.quarter = ft.quarter
  and ot.month = ft.month
  and ot.day_of_month = ft.day_of_month
  and concat(substr(ot.planned_departure_time, 1, 2),
    concat(':', substr(ot.planned_departure_time, 3, 2))) =
    to_char(ft.planned_departure_time, 'HH24:MI')
where ot.departure_offset is not null
  and ot.departure_offset !=
  extract(day from (ft.actual_arrival_time - ft.planned_departure_time)) * 24 * 60 +
    extract(hour from (ft.actual_arrival_time - ft.planned_departure_time)) * 60 +
      extract(minute from (ft.actual_arrival_time - ft.planned_departure_time));


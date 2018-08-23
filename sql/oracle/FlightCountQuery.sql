select count(*), hour_of_day
from flights
where to_number(to_char(planned_arrival_time, 'HH24')) + 
  to_number(to_char(planned_arrival_time, 'MI')) / 60 -
  hour_of_day - minutes_of_hour / 60
  + decode(to_number(to_char(planned_arrival_time, 'DD')), day_of_month, 0, 24) 
  > 6.0
group by hour_of_day
order by hour_of_day;
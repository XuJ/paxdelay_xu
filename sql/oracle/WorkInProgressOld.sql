select count(*)
from t100_segments;

select t.carrier, t.origin, t.destination, t.year,
t.quarter, t.month, t.num_aircrafts, t.seat_deviation
from
(select t100.carrier, t100.origin, t100.destination, t100.year,
 t100.quarter, t100.month,
  count(distinct(t100.aircraft_type)) as num_aircrafts,
  stddev(t100.seats / t100.departures_performed) as seat_deviation
from t100_segments t100
where t100.departures_performed > 0
group by t100.carrier, t100.origin, t100.destination, t100.year,
t100.quarter, t100.month) t
where t.num_aircrafts > 1
and rownum <= 1000
order by t.seat_deviation desc;

select t100.seats / t100.departures_performed, t100.* from t100_segments t100
where t100.carrier = '16'
and t100.origin = 'SAV' and t100.destination = 'CLT'
and t100.year = 2007 and t100.quarter = 1
and t100.month = 1;

select ceil(sum(t100.seats) / sum(t100.departures_performed)) from t100_segments t100
where t100.carrier = '16'
and t100.origin = 'SAV' and t100.destination = 'CLT'
and t100.year = 2007 and t100.quarter = 1
and t100.month = 1
group by t100.carrier, t100.origin, t100.destination, t100.year,
t100.quarter, t100.month;

select count(*) from flights;

select count(*)
from t100_segments;

select tout.carrier, tout.origin, tout.destination, tout.year,
tout.quarter, tout.month, tout.num_aircraft_types, 
tout.aircraft_types, tout.seating_capacities, tout.departures_performed,
tout.seats_mean, tout.seats_deviation
from
(select t100.carrier, t100.origin, t100.destination, t100.year,
   t100.quarter, t100.month, avg(tin.seats_mean) as seats_mean,
  count(distinct(t100.aircraft_type)) as num_aircraft_types,
  wm_concat(t100.aircraft_type) as aircraft_types,
  wm_concat(to_char(t100.seats / t100.departures_performed)) as seating_capacities,
  wm_concat(t100.departures_performed) as departures_performed,
  sqrt(sum(t100.departures_performed * 
    power((t100.seats / t100.departures_performed) - tin.seats_mean, 2)) /
    power(sum(t100.departures_performed), 2)) as seats_deviation
from t100_segments t100
join (select t100.carrier, t100.origin, t100.destination, t100.year,
   t100.quarter, t100.month,
    ceil(sum(t100.seats) / sum(t100.departures_performed)) as seats_mean
  from t100_segments t100
  where t100.departures_performed > 0
  group by t100.carrier, t100.origin, t100.destination, t100.year,
    t100.quarter, t100.month) tin
on t100.carrier = tin.carrier and t100.origin = tin.origin 
  and t100.destination = tin.destination and t100.year = tin.year
  and t100.quarter = tin.quarter and t100.month = tin.month
group by t100.carrier, t100.origin, t100.destination, t100.year,
  t100.quarter, t100.month) tout
where tout.num_aircraft_types > 1
and rownum <= 1000;
order by t.seat_deviation desc;

select t100.seats / t100.departures_performed, t100.* from t100_segments t100
where t100.carrier = '16'
and t100.origin = 'SAV' and t100.destination = 'CLT'
and t100.year = 2007 and t100.quarter = 1
and t100.month = 1;

select ceil(sum(t100.seats) / sum(t100.departures_performed)) from t100_segments t100
where t100.carrier = '16'
and t100.origin = 'SAV' and t100.destination = 'CLT'
and t100.year = 2007 and t100.quarter = 1
and t100.month = 1
group by t100.carrier, t100.origin, t100.destination, t100.year,
t100.quarter, t100.month;
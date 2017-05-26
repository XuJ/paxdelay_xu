create table t100_seats
(
  year number(4, 0) not null,
  quarter number(1, 0) not null,
  month number(2, 0) not null,
  carrier varchar2(3) not null,
  origin char(3) not null,
  destination char(3) not null,
  departures_performed number(6, 0) not null,
  num_aircraft_types number(2, 0) not null,
  seats_mean number(6, 3) not null,
  seats_squared_mean number(9, 3) not null,
  seats_std_dev number(6, 3) not null,
  seats_coeff_var number(6, 5) not null
);

insert into t100_seats
select t100.year, t100.quarter, t100.month, t100.carrier,
  t100.origin, t100.destination, 
  t100.departures_performed, t100.num_aircraft_types,
  t100.seats_mean, t100.seats_squared_mean,
  sqrt(t100.seats_variance) as seats_std_dev,
  decode(t100.seats_mean, 0, 0, sqrt(t100.seats_variance) / t100.seats_mean)
from
(select t100.year, t100.quarter, t100.month, t100.carrier,
   t100.origin, t100.destination, t100.departures_performed,
   t100.num_aircraft_types, t100.seats_mean,
   t100.seats_squared_mean,
   decode(t100.departures_performed, 1, 0,
     round((t100.seats_squared_mean - power(t100.seats_mean, 2)) *
       t100.departures_performed / (t100.departures_performed - 1), 3))
     as seats_variance
 from
 (select t100.year, t100.quarter, t100.month, t100.carrier,
    t100.origin, t100.destination, count(*) as num_aircraft_types,
    sum(t100.seats) / sum(t100.departures_performed) as seats_mean,
    sum(power(t100.seats, 2) / t100.departures_performed) / 
      sum(t100.departures_performed) as seats_squared_mean,
    sum(t100.departures_performed) as departures_performed
  from t100_segments t100
  where t100.departures_performed > 0
  group by t100.year, t100.quarter, t100.month, t100.carrier,
    t100.origin, t100.destination
 ) t100
) t100;

create bitmap index bm_idx_t100_seats_cymod
  on t100_seats(carrier, year, month, origin, destination);

create bitmap index bm_idx_t100_seats_cym
  on t100_seats(carrier, year, month);

update t100_seats
set carrier = 'US'
where carrier = 'HP';

select carrier, origin, destination, year, quarter, month
from t100_seats
group by carrier, origin, destination, year, quarter, month
having count(*) > 2;


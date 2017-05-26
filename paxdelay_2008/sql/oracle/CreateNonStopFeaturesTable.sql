create table non_stop_features
as
select it.month, 
  it.first_operating_carrier as carrier,
  it.origin, it.destination,
  count(distinct it.planned_departure_time) 
    as num_itineraries,
  sum(seg.passengers) / sum(seg.seats) as load_factor
from itineraries it
join 
(
 select month, carrier, origin, destination, 
   sum(passengers) as passengers,
   sum(seats) as seats
 from t100_segments
 where year = 2007
   and seats > 0
 group by month, carrier, origin, destination
) seg
  on seg.month = it.month
  and seg.carrier = it.first_operating_carrier
  and seg.origin = it.origin
  and seg.destination = it.destination
where it.year = 2007
  and it.num_flights = 1
group by it.month, it.first_operating_carrier,
  it.origin, it.destination
order by month, first_operating_carrier;

commit;

create index idx_non_stop_features_m
  on non_stop_features(month)
  tablespace users;

commit;


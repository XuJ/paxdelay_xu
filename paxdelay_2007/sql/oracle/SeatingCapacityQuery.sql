select iv.*, cap.seats
from innovata iv
join
(select carrier, aircraft_type,
 ceil(sum(seats) / sum(departures_performed)) as seats
 from t100_segments
 group by carrier, aircraft_type) cap
on iv.carrier = cap.carrier
where iv.aircraft_code = '319'
and cap.aircraft_type = '698'
and rownum <= 1000;

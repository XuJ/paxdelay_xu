select t100.origin, t100.destination, 
  sum (t100.ramp_to_ramp) / sum(t100.departures_performed)
    as ramp_to_ramp
from t100_segments t100
group by t100.origin, t100.destination
having sum(t100.departures_performed) > 0;
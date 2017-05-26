select 'Generated',
  sum(decode(num_flights, 2, 2, 0) * passengers) as one_stop_segments,
  sum(num_flights * passengers) as total_segments
from itinerary_allocations
union all
select 'Continental', 
  sum(decode(num_flights, 2, 2, 0) * passengers) as one_stop_segments,
  sum(num_flights * passengers) as total_segments
from continental_allocations;

select 'Generated', sum(num_passengers * trip_delay),
  sum(decode(first_disruption_cause, 0, 1, 0) * num_passengers * trip_delay)
    as flight_delays,
  sum(decode(first_disruption_cause, 1, 1, 0) * num_passengers * trip_delay)
    as missed_connections,
  sum(decode(first_disruption_cause, 2, 1, 0) * num_passengers * trip_delay)
    as cancellations
from validation_delays_3
union all
select 'Continental', sum(num_passengers * trip_delay),
  sum(decode(first_disruption_cause, 0, 1, 0) * num_passengers * trip_delay)
    as flight_delays,
  sum(decode(first_disruption_cause, 1, 1, 0) * num_passengers * trip_delay)
    as missed_connections,
  sum(decode(first_disruption_cause, 2, 1, 0) * num_passengers * trip_delay)
    as cancellations
from continental_delays_3;
select month,
  sum(num_passengers)
    as passengers,
  sum(trip_delay * num_passengers)
    as total_passenger_delay,
  sum(decode(first_disruption_cause, 0, num_passengers, 0))
    as nondisrupted_passengers,
  sum(decode(first_disruption_cause, 0, trip_delay * num_passengers, 0))
    as total_nondisrupted_delay,
  sum(decode(first_disruption_cause, 0, 0, num_passengers))
    as disrupted_passengers,
  sum(decode(first_disruption_cause, 0, 0, trip_delay * num_passengers))
    as total_disruption_delay,
  sum(decode(first_disruption_cause, 1, num_passengers, 0))
    as misconnection_passengers,
  sum(decode(first_disruption_cause, 1, trip_delay * num_passengers, 0))
    as total_misconnection_delay,
  sum(decode(first_disruption_cause, 2, num_passengers, 0))
    as cancellation_passengers,
  sum(decode(first_disruption_cause, 2, trip_delay * num_passengers, 0))
    as total_cancellation_delay
from passenger_delays
group by month
order by month;
  
    
  
  
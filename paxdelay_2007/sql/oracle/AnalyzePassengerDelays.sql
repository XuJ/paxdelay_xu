select planned_first_carrier, year, month, day_of_month,
  sum(num_passengers) as number_passengers,
  sum(decode(first_disruption_cause, 0, 
    decode(greatest(trip_delay, 15.0), trip_delay, num_passengers, 0), 0)) as delayed_15_passengers,
  sum(decode(first_disruption_cause, 1, num_passengers, 0)) as missed_connection_passengers,
  sum(decode(first_disruption_cause, 2, num_passengers, 0)) as cancellation_passengers,
  sum(decode(first_disruption_cause, 0, num_passengers * trip_delay, 0)) as flight_delays, 
  sum(decode(first_disruption_cause, 1, num_passengers * trip_delay, 0)) as missed_connection_delays,
  sum(decode(first_disruption_cause, 2, num_passengers * trip_delay, 0)) as cancellation_delays 
from passenger_delays_2
where planned_multi_carrier = 0
  and quarter in (3, 4)
group by planned_first_carrier, year, month, day_of_month
order by year, month, day_of_month, planned_first_carrier;

select year, month, day_of_month,
  sum(num_passengers) as number_passengers,
  sum(decode(first_disruption_cause, 0, 
    decode(greatest(trip_delay, 15.0), trip_delay, num_passengers, 0), 0)) as delayed_15_passengers,
  sum(decode(first_disruption_cause, 1, num_passengers, 0)) as missed_connection_passengers,
  sum(decode(first_disruption_cause, 2, num_passengers, 0)) as cancellation_passengers,
  sum(decode(first_disruption_cause, 0, num_passengers * trip_delay, 0)) as flight_delays, 
  sum(decode(first_disruption_cause, 1, num_passengers * trip_delay, 0)) as missed_connection_delays,
  sum(decode(first_disruption_cause, 2, num_passengers * trip_delay, 0)) as cancellation_delays 
from passenger_delays_2
group by year, month, day_of_month
order by year, month, day_of_month;
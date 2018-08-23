SET TERMOUT OFF

SET NEWPAGE 0
SET SPACE 0
SET LINESIZE 320
SET PAGESIZE 0
SET ECHO OFF
SET FEEDBACK OFF
SET VERIFY OFF
SET HEADING OFF
SET MARKUP HTML OFF SPOOL OFF
SET COLSEP ','

SPOOL '/home/dfearing/workspace/output/paxdelay/analysis/PassengerDelayDestinationStatistics.csv'

CLEAR COLUMNS
CLEAR BREAKS
CLEAR COMPUTES

select ft.carrier, ft.month, ft.destination, 
  ft.flights, ft.cancelled_flights,
  ft.total_flight_delay, pd.passengers,
  pd.one_stop_passengers, pd.total_passenger_delay,
  pd.cancelled_passengers, pd.total_cancelled_delay,
  pd.misconnected_passengers, pd.total_misconnected_delay
from
(select planned_destination as airport_code
 from passenger_delays_618
 group by planned_destination
 having sum(num_passengers) > 3000000) included
join
(select nvl(planned_second_carrier, planned_first_carrier)
     as planned_last_carrier, 
   month, planned_destination,
   sum(num_passengers) as passengers,
   sum(decode(planned_num_flights, 2, num_passengers, 0)) 
     as one_stop_passengers,
   sum(num_passengers * trip_delay)
     as total_passenger_delay,
   sum(decode(first_disruption_cause, 2, num_passengers, 0))
     as cancelled_passengers,
   sum(decode(first_disruption_cause, 1, num_passengers, 0))
     as misconnected_passengers,
   sum(decode(first_disruption_cause, 2, 
     num_passengers * trip_delay, 0))
     as total_cancelled_delay,
   sum(decode(first_disruption_cause, 1, 
     num_passengers * trip_delay, 0))
     as total_misconnected_delay
 from passenger_delays_618
 group by nvl(planned_second_carrier, planned_first_carrier),
   month, planned_destination) pd
on pd.planned_destination = included.airport_code
join  
(select carrier, month, destination,
  count(*) as flights,
  sum(cancelled_flag + diverted_flag)
    as cancelled_flights,
  sum(decode(cancelled_flag + diverted_flag, 0,
    greatest(0, 
      extract(day from actual_arrival_time - planned_arrival_time) * 24 * 60 +
      extract(hour from actual_arrival_time - planned_arrival_time) * 60 +
      extract(minute from actual_arrival_time - planned_arrival_time)), 0))
    as total_flight_delay
 from flights
 group by carrier, month, destination) ft
on ft.carrier = pd.planned_last_carrier
  and ft.month = pd.month
  and ft.destination = pd.planned_destination
order by pd.total_passenger_delay / pd.passengers desc;

SPOOL OFF

SET TERMOUT ON

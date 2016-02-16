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

SPOOL '/home/dfearing/workspace/output/paxdelay/analysis/PassengerDelayConnectionStatistics.csv'

CLEAR COLUMNS
CLEAR BREAKS
CLEAR COMPUTES

select ftIn.carrier, ftOut.carrier, 
  ftIn.month, pd.planned_connection, 
  ftIn.flights, ftIn.cancelled_flights,
  ftIn.total_flight_delay, 
  ftOut.flights, ftOut.cancelled_flights,
  ftOut.total_flight_delay,
  pd.passengers, pd.total_passenger_delay,
  pd.cancelled_passengers, pd.total_cancelled_delay,
  pd.misconnected_passengers, pd.total_misconnected_delay
from
(select planned_connection as airport_code
 from passenger_delays_618
 where planned_num_flights = 2
 group by planned_connection
 having sum(num_passengers) > 75000) included
join
(select planned_first_carrier, planned_second_carrier,
   month, planned_connection,
   sum(num_passengers) as passengers,
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
 where planned_num_flights = 2
 group by planned_first_carrier, planned_second_carrier,
   month, planned_connection) pd
on pd.planned_connection = included.airport_code
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
 group by carrier, month, destination) ftIn
on ftIn.carrier = pd.planned_first_carrier
  and ftIn.month = pd.month
  and ftIn.destination = pd.planned_connection
join
(select carrier, month, origin,
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
 group by carrier, month, origin) ftOut
on ftOut.carrier = pd.planned_second_carrier
  and ftOut.month = pd.month
  and ftOut.origin = pd.planned_connection
order by pd.total_passenger_delay / pd.passengers desc;

SPOOL OFF

SET TERMOUT ON

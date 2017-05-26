SET TERMOUT OFF

SET NEWPAGE 0
SET SPACE 0
SET LINESIZE 165
SET PAGESIZE 0
SET ECHO OFF
SET FEEDBACK OFF
SET VERIFY OFF
SET HEADING OFF
SET MARKUP HTML OFF SPOOL OFF
SET COLSEP ','

SPOOL '/home/dfearing/workspace/output/paxdelay/allocation/ContinentalLoadFactors.txt'

CLEAR COLUMNS
CLEAR BREAKS
CLEAR COMPUTES

select ft.id, least(ia.passengers * 1.30, ft.seating_capacity) as passengers, 
  ft.seating_capacity
from
(
 select flight_id, sum(passengers) as passengers
 from
 (
  select first_flight_id as flight_id, passengers
  from continental_allocations
  where year = 2007
    and quarter = 4
    and first_carrier in ('CO', 'XE')
    and num_flights = 1
  union all
  select first_flight_id as flight_id, passengers
  from continental_allocations
  where year = 2007
    and quarter = 4
    and first_carrier in ('CO', 'XE')
    and second_carrier in ('CO', 'XE')
    and num_flights = 2
  union all
  select second_flight_id as flight_id, passengers
  from continental_allocations
  where year = 2007
    and quarter = 4
    and first_carrier in ('CO', 'XE')
    and second_carrier in ('CO', 'XE')
    and num_flights = 2
 )
 group by flight_id
) ia
join flights ft
  on ft.id = ia.flight_id
where ft.seating_capacity is not null;

SPOOL OFF

SET TERMOUT ON

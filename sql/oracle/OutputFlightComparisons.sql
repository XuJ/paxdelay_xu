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

SPOOL '/home/dfearing/workspace/output/paxdelay/allocation/FlightAssignmentComparisons.txt'

CLEAR COLUMNS
CLEAR BREAKS
CLEAR COMPUTES

select fc.* 
from flight_comparisons fc
join flights f
  on f.id = fc.flight_id
where f.month = 10
  and f.day_of_month >= 7
  and f.day_of_month < 14;

SPOOL OFF

SET TERMOUT ON

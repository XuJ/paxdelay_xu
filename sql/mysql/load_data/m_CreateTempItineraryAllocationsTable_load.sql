-- XuJiao
-- It took 12 min
-- Record 80,929,023
LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/Allocation_Output/ItineraryLoad_AllData.csv'
INTO TABLE temp_itinerary_allocations
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  first_carrier,
  @vsecond_carrier,
  first_flight_id,
  @vsecond_flight_id,
  passengers
)
-- 042117 XuJ: Change blank value to null value (more efficient method).
set 
second_carrier = nullif(@vsecond_carrier,''),
second_flight_id = nullif(@vsecond_flight_id,'');

-- XuJ: 04/01/17. Change blank value to null value.
/*
update temp_itinerary_allocations set second_carrier = NULL where second_carrier = '';
update temp_itinerary_allocations set second_flight_id = NULL where second_flight_id = '';
*/
create index idx_ita_ftid
  on temp_itinerary_allocations(first_flight_id, second_flight_id);


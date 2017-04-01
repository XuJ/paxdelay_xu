-- XuJiao
-- It took 12 min
-- Record 82,777,269
LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/Allocation_Output/ItineraryLoad_AllData.csv'
INTO TABLE temp_itinerary_allocations
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  first_carrier,
  second_carrier,
  first_flight_id,
  second_flight_id,
  passengers
);
-- XuJ: 04/01/17. Change blank value to null value.
update temp_itinerary_allocations set second_carrier = NULL where second_carrier = '';
update temp_itinerary_allocations set second_flight_id = NULL where second_flight_id = '';

create index idx_ita_ftid
  on temp_itinerary_allocations(first_flight_id, second_flight_id);


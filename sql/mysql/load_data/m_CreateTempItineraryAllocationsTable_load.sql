LOAD DATA INFILE '/export/mysql/import/ItineraryLoad_AllData.csv'
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
-- 132704
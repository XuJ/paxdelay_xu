LOAD DATA INFILE '/export/mysql/import/MetronItineraries_20070423.csv'
INTO TABLE temp_metron_itineraries
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  num_flights,
  first_flight_id,
  second_flight_id
);

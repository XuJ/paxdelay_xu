LOAD DATA INFILE '/export/mysql/import/schaan/GeneratedItineraries.csv'
INTO TABLE temp_itineraries
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(num_flights,
first_flight_id,
second_flight_id);

create index idx_temp_itineraries
  on temp_itineraries(num_flights, first_flight_id, second_flight_id);
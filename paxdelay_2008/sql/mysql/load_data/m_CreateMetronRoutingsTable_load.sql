LOAD DATA INFILE '/export/mysql/import/EstimatedAircraftRoutings.csv'
INTO TABLE metron_routings
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  carrier,
  icao_aircraft_code,
  fleet_index,
  flight_order,
  flight_id,
  metron_id
);
-- 16,100

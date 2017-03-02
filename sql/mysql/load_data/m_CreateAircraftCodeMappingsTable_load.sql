LOAD DATA INFILE '/export/mysql/import/AircraftCodeMappings.csv'
INTO TABLE aircraft_code_mappings
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(iata_code,
icao_code,
manufacturer_and_model,
inventory_manufacturer,
inventory_model,
wake_category);

LOAD DATA INFILE '/export/mysql/import/AircraftCodeMappings.csv'
INTO TABLE aircraft_code_mappings
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(iata_code,
@vicao_code,
manufacturer_and_model,
@vinventory_manufacturer,
@vinventory_model,
@vwake_category)
set 
icao_code = nullif(@vicao_code,''),
inventory_manufacturer = nullif(@vinventory_manufacturer,''),
inventory_model = nullif(@vinventory_model,''),
wake_category = nullif(@vwake_category,'');

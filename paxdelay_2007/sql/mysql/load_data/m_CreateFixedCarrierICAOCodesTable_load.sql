LOAD DATA INFILE '/export/mysql/import/FixedCarrierICAOCodes.csv'
INTO TABLE fixed_carrier_icao_codes
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(carrier,
incorrect_icao_aircraft_code,
fixed_icao_aircraft_code);
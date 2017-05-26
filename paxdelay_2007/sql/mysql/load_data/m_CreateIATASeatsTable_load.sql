LOAD DATA INFILE '/export/mysql/import/CarrierAircraftCodePairsSeats.csv'
INTO TABLE carrier_iata_seats
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(carrier,
aircraft_code,
seats);

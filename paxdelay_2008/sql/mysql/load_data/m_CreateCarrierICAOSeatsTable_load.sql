LOAD DATA INFILE '/export/mysql/import/CarrierICAOCodeSeats.csv'
INTO TABLE carrier_icao_seats
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(carrier,
icao_aircraft_code,
number_of_seats);
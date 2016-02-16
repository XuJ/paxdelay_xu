OPTIONS
(
  SKIP = 1,
  DIRECT = TRUE
)
LOAD DATA
INFILE 'CarrierAircraftCodePairsSeats.csv' "str '\r\n'"
REPLACE
INTO TABLE PAXDELAY.CARRIER_IATA_SEATS
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
(
  carrier char,
  aircraft_code char,
  seats integer external
)

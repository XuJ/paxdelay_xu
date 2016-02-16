options
(
  skip = 1,
  direct = true
)
load data
infile 'FlightCarrierData.csv' "str '\r\n'"
replace
into table paxdelay.flight_carriers
fields terminated by ',' optionally enclosed by '"'
(
  iata_code char,
  icao_code char,
  name char
)

options
(
  skip = 1,
  direct = true
)
load data
infile 'FixedCarrierICAOCodes.csv' "str '\r\n'"
replace
into table paxdelay.fixed_carrier_icao_codes
fields terminated by ',' optionally enclosed by '"'
(
  carrier char,
  incorrect_icao_aircraft_code char,
  fixed_icao_aircraft_code char
)

options
(
  skip = 1,
  direct = true
)
load data
infile 'CarrierICAOCodeSeats.csv' "str '\r\n'"
replace
into table paxdelay.carrier_icao_seats
fields terminated by ',' optionally enclosed by '"'
(
  carrier char,
  icao_aircraft_code char,
  number_of_seats integer external
)

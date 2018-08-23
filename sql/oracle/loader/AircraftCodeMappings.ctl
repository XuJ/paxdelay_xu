options
(
  skip = 1,
  direct = true
)
load data
infile 'AircraftCodeMappings.csv' "str '\r\n'"
replace
into table paxdelay.aircraft_code_mappings
fields terminated by ',' optionally enclosed by '"'
trailing nullcols
(
  iata_code char,
  icao_code char,
  manufacturer_and_model char,
  inventory_manufacturer char,
  inventory_model char,
  wake_category char
)

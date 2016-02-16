options
(
  skip = 1,
  direct = true
)
load data
infile 'ETMS_2007.csv' "str '\r\n'"
replace
into table paxdelay.etms
fields terminated by ',' optionally enclosed by '"'
trailing nullcols
(
  aircraft_id char,
  iata_aircraft_code char,
  origin char,
  destination char,
  planned_departure_time_gmt char,
  planned_arrival_time_gmt char,
  actual_departure_time_gmt char,
  actual_arrival_time_gmt char,
  planned_departure_time_local char,
  planned_arrival_time_local char,
  actual_departure_time_local char,
  actual_arrival_time_local char,
  departure_flag integer external,
  arrival_flag integer external,
  flew_flag integer external,
  mg_flag integer external
)

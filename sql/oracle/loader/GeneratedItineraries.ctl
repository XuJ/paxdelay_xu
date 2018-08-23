options
(
  skip = 1,
  direct = TRUE
)
load data
infile 'GeneratedItineraries.csv'
replace
into table paxdelay.temp_itineraries
fields terminated by ',' optionally enclosed by '"'
trailing nullcols
(
  num_flights integer external,
  first_flight_id integer external,
  second_flight_id integer external
)

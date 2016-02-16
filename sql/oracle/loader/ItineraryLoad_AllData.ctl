options
(
  skip = 1,
  direct = true
)
load data
infile 'ItineraryLoad_AllData.csv'
replace
into table paxdelay.temp_itinerary_allocations
fields terminated by ',' optionally enclosed by '"'
trailing nullcols
(
  first_carrier char,
  second_carrier char,
  first_flight_id integer external,
  second_flight_id integer external,
  passengers integer external
)

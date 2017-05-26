options
(
  skip = 1,
  direct = TRUE
)
load data
infile 'October/Scaled_1.25_20091110/ItineraryValidation.csv'
replace
into table paxdelay.temp_itinerary_comparisons
fields terminated by ',' optionally enclosed by '"'
trailing nullcols
(
  num_flights integer external,
  first_flight_id integer external,
  second_flight_id integer external,
  allocated_passengers float external,
  airline_passengers float external
)

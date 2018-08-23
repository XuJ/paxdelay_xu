options
(
  skip = 1,
  direct = true
)
load data
infile 'ContinentalFlightLegs.csv'
replace
into table paxdelay.continental_flight_legs
fields terminated by ',' optionally enclosed by '"'
(
  itinerary_id integer external,
  num_flights integer external,
  itinerary_sequence integer external,
  carrier char,
  flight_number char,
  departure_time char,
  origin char,
  destination char
)

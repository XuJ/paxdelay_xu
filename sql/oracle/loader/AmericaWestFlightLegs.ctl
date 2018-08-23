options
(
  skip = 1,
  direct = true
)
load data
infile 'AmericaWestFlightLegs.txt'
replace
into table paxdelay.americawest_flight_legs
fields terminated by '\t' optionally enclosed by '"'
(
  itinerary_id integer external,
  ticket_number integer external,
  num_flights integer external,
  itinerary_sequence integer external,
  carrier char,
  flight_number char,
  departure_date char,
  departure_time char,
  arrival_time char,
  origin char,
  destination char,
  fare_class char,
  fare float external
)

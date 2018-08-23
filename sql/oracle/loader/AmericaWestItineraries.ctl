options
(
  skip = 1,
  direct = true
)
load data
infile 'AmericaWestItineraries.txt'
replace
into table paxdelay.americawest_itineraries
fields terminated by '\t' optionally enclosed by '"'
(
  itinerary_id integer external,
  ticket_number integer external,
  num_flights integer external,
  origin char,
  destination char,
  departure_time char,
  departure_date char,
  itinerary_fare float external,
  passengers integer external
)

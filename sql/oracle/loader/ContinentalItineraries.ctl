options
(
  skip = 1,
  direct = true
)
load data
infile 'ContinentalItineraries.csv'
replace
into table paxdelay.continental_itineraries
fields terminated by ',' optionally enclosed by '"'
(
  itinerary_id integer external,
  num_flights integer external,
  origin char,
  destination char,
  departure_time char,
  day_of_week integer external,
  departure_date char,
  number_samples integer external,
  number_flown integer external,
  number_no_show integer external,
  no_show_average float external,
  show_average float external
)

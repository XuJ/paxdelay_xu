create table continental_itineraries
(
  itinerary_id number(12, 0) primary key not null,
  num_flights number(2, 0) not null,
  origin char(3) not null,
  destination char(3) not null,
  departure_time char(5) not null,
  day_of_week number(1, 0) not null,
  departure_date char(10) not null,
  number_samples number(4, 0) not null,
  number_flown number(4, 0) not null,
  number_no_show number(4, 0) not null,
  no_show_average number(4, 2) not null,
  show_average number(4, 2) not null
);

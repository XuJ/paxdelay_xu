drop table americawest_itineraries;

create table americawest_itineraries
(
  itinerary_id number(12, 0) not null,
  ticket_number number(13, 0) not null,
  num_flights number(2, 0) not null,
  origin char(3) not null,
  destination char(3) not null,
  departure_time char(5) not null,
  departure_date char(10) not null,
  itinerary_fare number(6, 2) not null,
  passengers number(4, 0) not null
);

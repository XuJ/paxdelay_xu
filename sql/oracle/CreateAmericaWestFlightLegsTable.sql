drop table americawest_flight_legs;

create table americawest_flight_legs
(
  itinerary_id number(12, 0) not null,
  ticket_number number(13, 0) not null,
  num_flights number(2, 0) not null,
  itinerary_sequence number(2, 0) not null,
  carrier char(2) not null,
  flight_number varchar2(6) not null,
  departure_date char(10) not null,
  departure_time char(5) not null,
  arrival_time char(5) not null,
  origin char(3) not null,
  destination char(3) not null,
  fare_class char(1) not null,
  fare number(6, 2) not null
);

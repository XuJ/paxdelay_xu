create table continental_flight_legs
(
  itinerary_id number(12, 0) not null,
  num_flights number(2, 0) not null,
  itinerary_sequence number(2, 0) not null,
  carrier varchar2(3) not null,
  flight_number varchar2(6) not null,
  departure_time char(5) not null,
  origin char(3) not null,
  destination char(3) not null
);

create unique index idx_co_legs_iidis
  on continental_flight_legs(itinerary_id, itinerary_sequence);

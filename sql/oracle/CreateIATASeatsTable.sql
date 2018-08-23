create table carrier_iata_seats
(
  carrier varchar2(3) not null,
  aircraft_code char(3) not null,
  seats number(3, 0) not null
);

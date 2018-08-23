create table carrier_icao_seats
(
  carrier char(2) not null,
  icao_aircraft_code varchar2(4) not null,
  number_of_seats number(3, 0) not null
);

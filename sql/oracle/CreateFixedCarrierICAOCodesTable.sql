create table fixed_carrier_icao_codes
(
  carrier char(2) not null,
  incorrect_icao_aircraft_code varchar2(4) not null,
  fixed_icao_aircraft_code varchar2(4) not null
);

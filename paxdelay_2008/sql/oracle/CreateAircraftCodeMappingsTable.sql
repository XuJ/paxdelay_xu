drop table aircraft_code_mappings;

create table aircraft_code_mappings
(
  iata_code char(3) not null,
  icao_code varchar2(4),
  manufacturer_and_model varchar2(75) not null,
  inventory_manufacturer varchar2(20),
  inventory_model varchar2(15),
  wake_category char(1)
);

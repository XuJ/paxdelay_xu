drop table airline_inventories;

create table airline_inventories
(
  carrier varchar2(3) not null,
  first_year number(2, 0) not null,
  serial_number varchar2(12) not null,
  tail_number varchar2(7) not null,
  aircraft_status char(1) not null,
  operating_status char(1) not null,
  number_of_seats number(3, 0),
  manufacturer varchar2(50) not null,
  model varchar2(25) not null,
  capacity_in_pounds number(6, 0),
  acquisition_date varchar2(10) not null
);

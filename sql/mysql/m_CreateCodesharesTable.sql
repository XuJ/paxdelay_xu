drop table if exists codeshares;

create table codeshares
(
  offering_id numeric(10) not null,
  carrier char(3) not null,
  flight_number char(6)
);
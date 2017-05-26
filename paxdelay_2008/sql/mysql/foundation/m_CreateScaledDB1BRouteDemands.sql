drop table if exists scaled_db1b_route_demands;

create table scaled_db1b_route_demands
(
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  first_operating_carrier varchar(3) not null,
  second_operating_carrier varchar(3) ,
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  passengers numeric(4) not null,
  num_flights int not null
)
ENGINE = MyISAM;
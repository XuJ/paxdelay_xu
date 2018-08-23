create table unique_carrier_routes
(
  year number(4, 0) not null,
  num_flights number(1, 0) not null,
  first_operating_carrier char(2) not null,
  second_operating_carrier char(2),
  origin char(3) not null,
  connection char(3),
  destination char(3) not null
);

insert into unique_carrier_routes
select distinct year, num_flights,
  first_operating_carrier, second_operating_carrier,
  origin, connection, destination
from route_demands
where exists
(
 select code from asqp_carriers
 where code = first_operating_carrier
)
and (num_flights = 1 or exists
  (
   select code from asqp_carriers
   where code = second_operating_carrier
  )
);

create index idx_carrier_routes_c1yocc2d
  on unique_carrier_routes(first_operating_carrier,
    year, origin, connection, 
    second_operating_carrier, destination)
  tablespace users;

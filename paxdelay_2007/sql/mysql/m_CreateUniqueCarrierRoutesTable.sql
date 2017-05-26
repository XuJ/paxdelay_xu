
drop table if exists unique_carrier_routes;

create table unique_carrier_routes
(
  year numeric(4) not null,
  num_flights numeric(1) not null,
  first_operating_carrier char(6) not null,
  second_operating_carrier char(6),
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
-- 386,487

create index idx_carrier_routes_c1yocc2d
  on unique_carrier_routes(first_operating_carrier, year, origin, connection,  second_operating_carrier, destination);

create index idx_unique_carrier_routes
  on unique_carrier_routes(year, origin, connection, destination, first_operating_carrier, second_operating_carrier);
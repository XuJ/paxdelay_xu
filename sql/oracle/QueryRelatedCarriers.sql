select rd.quarter, rd.primary_carrier, rd.secondary_carrier,
  rd.passengers
from
(
 select rd.quarter, rd.primary_carrier, rd.secondary_carrier, 
   sum(rd.passengers) as passengers
 from
 (
  select rd.quarter, rd.first_operating_carrier as primary_carrier,
    rd.second_operating_carrier as secondary_carrier,
    sum(rd.passengers) as passengers
  from route_demands rd
  where rd.num_flights = 2
  group by rd.quarter, rd.first_operating_carrier,
    rd.second_operating_carrier
  union all
  select rd.quarter, rd.second_operating_carrier as primary_carrier,
    rd.first_operating_carrier as secondary_carrier,
    sum(rd.passengers) as passengers
  from route_demands rd
  where rd.num_flights = 2
  group by rd.quarter, rd.first_operating_carrier,
    rd.second_operating_carrier
 ) rd
 where exists
 (
   select code
   from asqp_carriers
   where code = rd.primary_carrier
 ) 
 and exists
 (
   select code
   from asqp_carriers
   where code = rd.secondary_carrier
 )
 group by rd.quarter, rd.primary_carrier, rd.secondary_carrier
) rd
order by rd.quarter, rd.primary_carrier, rd.passengers desc;
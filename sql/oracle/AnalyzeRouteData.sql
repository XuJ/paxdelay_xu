select sum(tin.passengers)
from
(select db1b.market_id, avg(db1b.passengers) as passengers
from db1b_coupons db1b
group by db1b.quarter, db1b.market_id) tin

select sum(trd.passengers)
from temp_route_demands trd;

select sum(trd.passengers)
from temp_route_demands trd
where trd.num_flights <= 2;

select sum(trd.passengers)
from temp_route_demands trd
where trd.num_flights <= 1;

select sum(passengers)
from route_demands rd
where
  (rd.ticketing_carrier != rd.first_operating_carrier
   or (rd.num_flights = 2 and 
         rd.ticketing_carrier != rd.second_operating_carrier));

select sum(passengers)
from route_demands rd
where rd.num_flights = 2 and
 (rd.first_operating_carrier != rd.second_operating_carrier);

select sum(passengers)
from temp_route_demands trd
where trd.num_flights <= 2;

select sum(passengers)
from route_demands 
where num_flights <= 2;
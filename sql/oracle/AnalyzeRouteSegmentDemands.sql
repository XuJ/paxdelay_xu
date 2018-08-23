-- Number of flights in ASQP
select count(*)
from flights;

-- Number of flights without matching T100 demand
select count(distinct ft.id)
from flights ft
left join
(select distinct t100.year, t100.quarter,
   t100.month, t100.carrier, t100.origin, t100.destination
 from t100_segments t100) t100
on t100.year = ft.year
  and t100.quarter = ft.quarter
  and t100.month = ft.month
  and t100.carrier = ft.carrier
  and t100.origin = ft.origin
  and t100.destination = ft.destination
where t100.carrier is null;

-- Number of flights without matching DB1B demand
select count(*)
from flights ft
left join
(select distinct rd.year, rd.quarter, 
   rd.first_operating_carrier as carrier, rd.origin, rd.destination
 from route_demands rd
 where rd.num_flights = 1
 union
 select distinct rd.year, rd.quarter,
   rd.first_operating_carrier, rd.origin, rd.connection
 from route_demands rd
 where rd.num_flights = 2
 union
 select distinct rd.year, rd.quarter,
   rd.second_operating_carrier, rd.connection, rd.connection
 from route_demands rd
 where rd.num_flights = 2) rs
on rs.year = ft.year
  and rs.quarter = ft.quarter
  and rs.carrier = ft.carrier
  and rs.origin = ft.origin
  and rs.destination = ft.destination
where rs.carrier is null;

-- Number of flights with out matching T100 or DB1B demand
select count(*)
from flights ft
left join
(select distinct rd.year, rd.quarter, 
   rd.first_operating_carrier as carrier, rd.origin, rd.destination
 from route_demands rd
 where rd.num_flights = 1
 union
 select distinct rd.year, rd.quarter,
   rd.first_operating_carrier, rd.origin, rd.connection
 from route_demands rd
 where rd.num_flights = 2
 union
 select distinct rd.year, rd.quarter,
   rd.second_operating_carrier, rd.connection, rd.connection
 from route_demands rd
 where rd.num_flights = 2) rs
on rs.year = ft.year
  and rs.quarter = ft.quarter
  and rs.carrier = ft.carrier
  and rs.origin = ft.origin
  and rs.destination = ft.destination
left join t100_segments t100
on t100.year = ft.year
  and t100.quarter = ft.quarter
  and t100.month = ft.month
  and t100.carrier = ft.carrier
  and t100.origin = ft.origin
  and t100.destination = ft.destination
where rs.carrier is null and t100.carrier is null;

-- Number of unmatched T100 flights with swapped US / HP carriers
select count(distinct usft.id) 
from
(select ft.id, ft.year, ft.quarter, ft.month, ft.carrier, 
   ft.origin, ft.destination
 from flights ft
 left join
 (select distinct t100.year, t100.quarter,
    t100.month, t100.carrier, t100.origin, t100.destination
  from t100_segments t100) t100
 on t100.year = ft.year
   and t100.quarter = ft.quarter
   and t100.month = ft.month
   and t100.carrier = ft.carrier
   and t100.origin = ft.origin
   and t100.destination = ft.destination
 where t100.carrier is null and ft.carrier = 'US') usft
join t100_segments t100
  on t100.year = usft.year
  and t100.quarter = usft.quarter
  and t100.month = usft.month
  and t100.origin = usft.origin
  and t100.destination = usft.destination
where t100.carrier = 'HP';

select count(distinct usft.id)
from
(select ft.id, ft.year, ft.quarter, ft.month, ft.carrier, 
    ft.origin, ft.destination
 from flights ft
 left join
 (select distinct rd.year, rd.quarter, 
    rd.first_operating_carrier as carrier, rd.origin, rd.destination
  from route_demands rd
  where rd.num_flights = 1
  union
  select distinct rd.year, rd.quarter,
    rd.first_operating_carrier, rd.origin, rd.connection
  from route_demands rd
  where rd.num_flights = 2
  union
  select distinct rd.year, rd.quarter,
    rd.second_operating_carrier, rd.connection, rd.connection
  from route_demands rd
  where rd.num_flights = 2) rs
 on rs.year = ft.year
   and rs.quarter = ft.quarter
   and rs.carrier = ft.carrier
   and rs.origin = ft.origin
   and rs.destination = ft.destination
 where rs.carrier is null and ft.carrier = 'US') usft
join
(select distinct rd.year, rd.quarter, 
   rd.first_operating_carrier as carrier, rd.origin, rd.destination
 from route_demands rd
 where rd.num_flights = 1
 union
 select distinct rd.year, rd.quarter,
   rd.first_operating_carrier, rd.origin, rd.connection
 from route_demands rd
 where rd.num_flights = 2
 union
 select distinct rd.year, rd.quarter,
   rd.second_operating_carrier, rd.connection, rd.connection
 from route_demands rd
 where rd.num_flights = 2) rs
on rs.year = usft.year
  and rs.quarter = usft.quarter
  and rs.origin = usft.origin
  and rs.destination = usft.destination
where rs.carrier = 'HP';


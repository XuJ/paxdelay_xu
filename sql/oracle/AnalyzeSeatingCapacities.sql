select count(*)
from flights ft;

select count(*)
from flights ft
join 
(select distinct carrier, tail_number
from airline_inventories) ai
on ai.carrier = ft.carrier
and ai.tail_number = ft.tail_number;

select count(*)
from flights ft
join 
(select distinct carrier, tail_number
from airline_inventories
where number_of_seats is not null) ai
on ai.carrier = ft.carrier
and ai.tail_number = ft.tail_number;

select count(*)
from flights ft
left join
(select distinct carrier, tail_number
from airline_inventories
where number_of_seats is not null) ai
on ai.carrier = ft.carrier
and ai.tail_number = ft.tail_number
where ai.carrier is null
and ft.seating_capacity is null

select count(*)
from etms;

select count(*)
from etms et
join flight_carriers fc
on substr(et.aircraft_id, 1, 3) = fc.icao_code;

select sum(num_aircraft)
from
(select carrier, manufacturer, model,
 max(number_of_seats), avg(number_of_seats), min(number_of_seats), 
 count(*) as num_aircraft
 from airline_inventories ai
 group by carrier, manufacturer, model
 having max(number_of_seats) = min(number_of_seats));
 
select distinct ft.carrier, tfi.iata_aircraft_code
from flights_no_seats ft
join temp_flight_iata tfi
  on tfi.flight_id = ft.id;

select ft.carrier, tfi.icao_aircraft_code, count(*) as aircraft_count
from flights_no_seats ft
join temp_flight_icao tfi
  on tfi.flight_id = ft.id
group by ft.carrier, tfi.icao_aircraft_code;

select tfi.icao_aircraft_code,
  ai.manufacturer, ai.model, ai.number_of_seats,
  count(*) as aircraft_count
from flights_no_seats ft
join temp_flight_icao tfi
  on tfi.flight_id = ft.id
left join airline_inventories ai
  on ai.carrier = ft.carrier
  and ai.tail_number = ft.tail_number
where ft.carrier = 'NW'
group by tfi.icao_aircraft_code,
  ai.manufacturer, ai.model, ai.number_of_seats
order by tfi.icao_aircraft_code;

select distinct ai.manufacturer, ai.model
from flights_no_seats ft
join airline_inventories ai
  on ai.carrier = ft.carrier
  and ai.tail_number = ft.tail_number
where ft.carrier = '9E';

select count(*)
from flights_no_seats ft
where ft.tail_number is null
 and ft.carrier = 'AA';

select ft.tail_number
from flights_no_seats ft
left join airline_inventories ai
  on ai.carrier = ft.carrier
  and ai.tail_number = ft.tail_number
where ft.carrier = 'CO'
  and ft.tail_number is not null
  and ai.carrier is null
  and rownum <= 10;

create table temp_missing_inventories
as
select ft.carrier, tfi.icao_aircraft_code,
  count(*) as number_missing
from flights_no_seats ft
join temp_flight_icao tfi
  on tfi.flight_id = ft.id
left join airline_inventories ai
  on ai.carrier = ft.carrier
  and ai.tail_number = ft.tail_number
where ai.carrier is null
  and tfi.icao_aircraft_code is not null
group by ft.carrier, tfi.icao_aircraft_code;

select *
from temp_missing_inventories
order by carrier, icao_aircraft_code;

select tmi.carrier, tmi.icao_aircraft_code,
  acm.icao_code, ai.number_of_seats, tmi.number_missing,
  count(*) as number_matching
from flights_no_seats ft
join temp_flight_icao tfi
  on tfi.flight_id = ft.id
join temp_missing_inventories tmi
  on tmi.carrier = ft.carrier
  and tmi.icao_aircraft_code = tfi.icao_aircraft_code
join airline_inventories ai
  on ai.carrier = ft.carrier
  and ai.tail_number = ft.tail_number
join aircraft_code_mappings acm
  on acm.inventory_manufacturer = ai.manufacturer
  and acm.inventory_model = ai.model
group by tmi.carrier, tmi.icao_aircraft_code,
  acm.icao_code, ai.number_of_seats, tmi.number_missing
order by tmi.carrier, tmi.icao_aircraft_code;

select ai.carrier, acm.icao_code, 
  ai.number_of_seats, count(*) as number_aircraft
from airline_inventories ai
join aircraft_code_mappings acm
  on acm.inventory_manufacturer = ai.manufacturer
  and acm.inventory_model = ai.model
group by ai.carrier, acm.icao_code,
  ai.number_of_seats
order by ai.carrier, acm.icao_code;
 
group by minv.carrier, minv.icao_aircraft_code,
  acm.icao_code, ai.number_of_seats;
  
select count(*)
from flights_no_seats ft
join airline_inventories ai
  on ft.carrier = ai.carrier
  and ft.tail_number = ai.tail_number;

select distinct ft.tail_number
from flights ft
where ft.seating_capacity is null
and ft.tail_number is not null
and ft.carrier = 'CO';

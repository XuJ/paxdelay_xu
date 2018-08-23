select max(to_number(to_char(to_date(amw.departure_time, 'HH24:MI'), 'HH24'), '00'))
from americawest_itineraries amw;

select departure_time
from americawest_itineraries amw
where length(translate(substr(amw.departure_time, 1, 
instr(amw.departure_time, ':', 1, 1) - 1), ' +-.0123456789',' ')) > 0;

select max(to_number(substr(amw.departure_time, 1, 
instr(amw.departure_time, ':', 1, 1) - 1)))
from americawest_itineraries amw;

select departure_time
from americawest_itineraries amw
where instr(amw.departure_time, ':', 1, 1) = 3 
  and to_number(substr(amw.departure_time, 1, 2)) > '23';
  
select *
from americawest_flight_legs amw
join americawest_itineraries ami
  on ami.itinerary_id = amw.itinerary_id
where instr(amw.departure_time, ':', 1, 1) = 3 
  and to_number(substr(amw.departure_time, 1, 2)) > '23'
order by amw.itinerary_id, amw.itinerary_sequence;

select count(*)
from
(
 select amw.itinerary_id, count(*)
 from americawest_flight_legs amw
 group by amw.itinerary_id
 having count(*) > 2
);

select max(to_number(substr(arrival_time, 1, 
  instr(arrival_time, ':', 1, 1) - 1)))
from americawest_flight_legs;

select count(*)
from temp_americawest_legs tal
left join temp_unique_americawest_legs ual
  on ual.carrier = tal.carrier
  and ual.flight_number = tal.flight_number
  and ual.origin = tal.origin
  and ual.destination = tal.destination
  and ual.planned_departure_time = tal.planned_departure_time
  and ual.planned_arrival_time = tal.planned_arrival_time
where ual.id is null;

select tai.origin, tai.destination
from temp_americawest_itineraries tai
left join temp_americawest_legs tal
  on tal.itinerary_id = tai.itinerary_id
where tal.itinerary_id is null
  and rownum <= 10;
  
delete from temp_americawest_itineraries tai
where not exists
(
 select * from airports where code = tai.destination
);

delete from temp_americawest_itineraries tai
where tai.itinerary_id in
(
 select tai.itinerary_id
 from temp_americawest_itineraries tai
 join temp_americawest_legs tal
   on tal.itinerary_id = tai.itinerary_id
 group by tai.itinerary_id, tai.num_flights
 having tai.num_flights != count(tal.itinerary_sequence)
);

delete from temp_americawest_legs tal
where not exists
(
 select * from temp_americawest_itineraries
 where itinerary_id = tal.itinerary_id
);

select count(*)
from
(
  select itinerary_id
  from temp_americawest_legs
  group by itinerary_id
  having count(itinerary_sequence) <= 3
);

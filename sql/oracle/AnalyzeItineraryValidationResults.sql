select *
from itinerary_comparisons
where allocation_method = 'Scaled_2.00_1109'
  and allocated_passengers is not null
  and airline_passengers is not null
  and num_flights = 1
order by abs(nvl(allocated_passengers, 0) - nvl(airline_passengers, 0)) desc;

select (sum(abs(nvl(allocated_passengers, 0) - nvl(airline_passengers, 0))) -
  (sum(airline_passengers) - sum(allocated_passengers))) /
  sum(nvl(allocated_passengers, 0))
from itinerary_comparisons
where allocation_method = 'Scaled_2.00_1109'
  and allocated_passengers is not null
  and airline_passengers is not null;

select sum(abs(nvl(allocated_passengers, 0) - nvl(airline_passengers, 0))) /
  sum(nvl(airline_passengers, 0))
from itinerary_comparisons
where allocation_method = 'Scaled_2.00_1109'
  and allocated_passengers is not null
  and airline_passengers is not null;

select sum(nvl(allocated_passengers, 0))
from itinerary_comparisons
where allocation_method = 'AutomatedChoice'
--  and allocated_passengers is not null
--  and airline_passengers is not null
  and num_flights = 2
  and month = 10;

select sum(nvl(airline_passengers, 0))
from itinerary_comparisons
where allocation_method = 'Scaled_2.00_1109'
--  and allocated_passengers is not null
--  and airline_passengers is not null
  and day_of_month >= 7
  and day_of_month < 14;

-- Number of passengers based on flights
select sum(nvl(fc.number_passengers, 0))
from flight_comparisons fc
join flights f
  on f.id = fc.flight_id
where fc.allocation_method = 'Scaled_2.00_1109'
  and f.year = 2007
  and f.month = 10
  and f.carrier = 'CO';

select sum(nvl(number_passengers, 0))
from flight_comparisons
where allocation_method = 'AutomatedChoice';

select sum(nvl(allocated_passengers, 0))
from itinerary_comparisons
where num_flights = 2
  and allocation_method = 'AutomatedChoice'
  and month = 10
  and day_of_month >= 7
  and day_of_month < 14;

-- Total number of flight-based passengers
select sum(num_flights * nvl(allocated_passengers, 0))
from itinerary_comparisons
where allocation_method = 'Scaled_1.25_1108'
  and month = 10;
  
select sum(num_flights * nvl(airline_passengers, 0))
from itinerary_comparisons
where allocation_method = 'Scaled_1.25_1108'
  and month = 10;  

-- Number of one stop passengers
select sum(nvl(allocated_passengers, 0))
from itinerary_comparisons
where num_flights = 2
  and allocation_method = 'Scaled_2.00_1109'
  and first_carrier = 'CO'
  and second_carrier = 'XE'
  and month = 10;

select sum(nvl(airline_passengers, 0))
from itinerary_comparisons
where num_flights = 2
  and allocation_method = 'Scaled_2.00_1109'
  and first_carrier = 'CO'
  and second_carrier = 'XE'
  and month = 10;

select sum(nvl(allocated_passengers, 0)) * 0.8
from itinerary_comparisons
where num_flights = 2
  and first_carrier = 'CO'
  and second_carrier = 'XE'
  and allocation_method = 'Scaled_1.25_1108'
  and month = 10;

select sum(nvl(airline_passengers, 0))
from itinerary_comparisons
where num_flights = 2
  and first_carrier = 'CO'
  and second_carrier = 'XE'
  and allocation_method = 'Scaled_1.25_1108'
  and month = 10;

select sum(nvl(airline_passengers, 0))
from itinerary_comparisons
where num_flights = 1
  and first_carrier = 'CO'
  and allocation_method = 'AutomatedChoice'
  and month = 10;

select sum(nvl(allocated_passengers, 0))
from itinerary_comparisons
where allocation_method = 'Scaled_1.25_1110'
  and num_flights = 2
  and month = 10;

select sum(passengers)
from airline_itineraries
where year = 2007
  and quarter = 4
  and num_flights = 2
  and first_carrier = 'CO'
  and second_carrier = 'CO';
  
select distinct allocation_method
from itinerary_comparisons;

-- Percentage of connecting passengers
select sum(nvl(fc.number_connecting, 0)) / sum(nvl(fc.number_passengers, 0))
from flight_comparisons fc
join flights f
  on f.id = fc.flight_id
where fc.allocation_method = 'Proprietary'
  and f.year = 2007
  and f.month = 10
  and f.carrier = 'CO';

select sum(nvl(number_connecting, 0)) / sum(nvl(number_passengers, 0))
from flight_comparisons
where allocation_method = 'AutomatedChoice';

select sum(decode(first_carrier, 'CO', 
    nvl(allocated_passengers, 0), 0)) +
  sum(decode(second_carrier, 'CO',
    nvl(allocated_passengers, 0), 0))
from itinerary_comparisons
where allocation_method = 'AutomatedChoice';

select sum(passengers)
from t100_segments
where carrier = 'CO'
and year = 2007
and quarter = 4
and month = 10;

from itineraries
where first_operating_carrier = 'F9';
  
select * from flights where id = 9019100;

select *
from itinerary_comparisons
where allocation_method = 'MeanChoice'
and rownum <= 10;

select sum(firstFlight.passengers)
from db1b_coupons firstFlight
join db1b_coupons secondFlight
on secondFlight.market_id = firstFlight.market_id
  and secondFlight.sequence_number = firstFlight.sequence_number + 1
join
(
 select market_id, 
   min(sequence_number) as firstSequenceNumber,
   max(sequence_number) as lastSequenceNumber
 from db1b_coupons
 where year = 2007
   and quarter = 4
 group by market_id 
) flightSequence
on flightSequence.market_id = firstFlight.market_id
  and flightSequence.firstSequenceNumber = firstFlight.sequence_number  
  and flightSequence.lastSequenceNumber = secondFlight.sequence_number
where firstFlight.operating_carrier = 'CO'
  and secondFlight.operating_carrier = 'XE';

-- Non-stop DB1B demand for the 4th quarter = 7926215
select sum(passengers)
from db1b_coupons firstFlight
join
(
 select market_id, 
   min(sequence_number) as firstSequenceNumber,
   max(sequence_number) as lastSequenceNumber
 from db1b_coupons
 where year = 2007
   and quarter = 4
 group by market_id 
) flightSequence
on flightSequence.market_id = firstFlight.market_id
  and flightSequence.firstSequenceNumber = firstFlight.sequence_number
where flightSequence.lastSequenceNumber = flightSequence.firstSequenceNumber;

-- One stop DB1B demand for the 4th quarter = 3367770
select sum(passengers)
from db1b_coupons firstFlight
join
(
 select market_id, 
   min(sequence_number) as firstSequenceNumber,
   max(sequence_number) as lastSequenceNumber
 from db1b_coupons
 where year = 2007
   and quarter = 4
 group by market_id 
) flightSequence
on flightSequence.market_id = firstFlight.market_id
  and flightSequence.firstSequenceNumber = firstFlight.sequence_number
where flightSequence.lastSequenceNumber = flightSequence.firstSequenceNumber + 1;

-- Two or more stop DB1B demand for the 4th quarter = 282703
select sum(passengers)
from db1b_coupons firstFlight
join
(
 select market_id, 
   min(sequence_number) as firstSequenceNumber,
   max(sequence_number) as lastSequenceNumber
 from db1b_coupons
 where year = 2007
   and quarter = 4
 group by market_id 
) flightSequence
on flightSequence.market_id = firstFlight.market_id
  and flightSequence.firstSequenceNumber = firstFlight.sequence_number
where flightSequence.lastSequenceNumber > flightSequence.firstSequenceNumber + 1;

select sum(passengers)
from route_demands
where year = 2007
  and quarter = 4
  and num_flights = 2
  and first_operating_carrier = 'CO'
  and second_operating_carrier = 'XE';

select sum(allocated_passengers), sum(airline_passengers)
from itinerary_comparisons
where allocation_method = 'Optimization'
  and allocated_passengers is not null
  and airline_passengers is not null;

select sum(decode(allocated_passengers, null, 0, allocated_passengers))
from itinerary_comparisons;

select sum(passengers)
from route_demands
where year = 2007
  and quarter = 4
  and num_flights = 2
  and first_operating_carrier = 'F9'
  and not exists(
    select * from asqp_carriers
    where code = second_operating_carrier);

select sum(passengers)
from route_demands
where year = 2007
  and quarter = 4
  and num_flights = 2
  and second_operating_carrier = 'F9'
  and not exists(
    select * from asqp_carriers
    where code = first_operating_carrier);
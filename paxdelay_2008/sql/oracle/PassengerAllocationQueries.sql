--------------------------------------------------

-- SELECTING ITINERARIES:

select id, first_flight_id, second_flight,
  origin, destination, connection,
  first_operating_carrier, second_operating_carrier,
  hour_of_day
from itineraries
where year = 2007 and month = 10 and day_of_month = 14
  and first_operating_carrier = 'CO'
  and (second_operating_carrier is null 'CO' or
    second_operating_carrier = 'CO')
  and multi_day_flag = 0;

-- SHOULD BE REPLACED WITH:

select id, first_flight_id, second_flight_id,
  origin, destination, connection,
  first_operating_carrier, second_operating_carrier,
  hour_of_day
from itineraries
where year = 2007 and quarter = 4
  and month = 10 and day_of_month = 14
  and num_flights = 1
  and first_operating_carrier = 'CO'
union all
select id, first_flight_id, second_flight_id,
  origin, destination, connection,
  first_operating_carrier, second_operating_carrier,
  hour_of_day
from itineraries
where year = 2007 and quarter = 4
  and month = 10 and day_of_month = 14
  and first_operating_carrier = 'CO'
  and second_operating_carrier = 'CO'
  and multi_day_flag = 0;

--------------------------------------------------

-- SELECTING DEFAULT SEATING CAPACITIES:

select origin, destination, seats_mean, seats_std_dev
from t100_seats
where year = 2007 and month = 10
and carrier = 'CO';

-- IS FINE.

--------------------------------------------------

-- SELECTING FLIGHT SEATING CAPACITIES:

select id, origin, destination, seating_capacity
from flights
where year = 2007 and month = 10 and day = 14
and carrier = 'CO';

-- SHOULD BE REPLACED WITH:

select id, origin, destination, seating_capacity
from flights
where year = 2007 and quarter = 4
and month = 10 and day = 14
and carrier = 'CO';

--------------------------------------------------

-- SELECTING ROUTE DEMANDS:

select origin, destination, connection, passengers,
  first_operating_carrier, second_operating_carrier
from route_demands
where (first_operating_carrier = 'CO'
  or second_operating_carrier = 'CO')
and year = 2007 and quarter = 4;

-- IS FINE

--------------------------------------------------

-- SELECTING SEGMENT DEMANDS:

select origin, destination, sum(passengers) as passengers
from t100_segments
where unique_carrier = 'CO'
and year = 2007 and month = 10
group by origin, destination;

-- SHOULD BE REPLACED BY:

select origin, destination, sum(passengers) as passengers
from t100_segments
where carrier = 'CO'
and year = 2007 and month = 10
group by origin, destination;

--------------------------------------------------

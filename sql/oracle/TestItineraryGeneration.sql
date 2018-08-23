    select 'Starting non-stop itineraries for CO at '||to_char(sysdate, 'HH24:MI:SS')
    from dual;

    -- Create the non-stop itineraries
    insert into itineraries
      (id, year, quarter, month, num_flights,
       first_operating_carrier, origin, destination, 
       planned_departure_time, planned_arrival_time,
       first_flight_id)
    select itinerary_id_seq.nextval, 
      ft.year, ft.quarter, ft.month, rd.num_flights,
      rd.first_operating_carrier,
      ft.origin, ft.destination, 
      ft.planned_departure_time, ft.planned_arrival_time, 
      ft.id
    from route_demands rd
    join flights ft
      on ft.year = rd.year and ft.quarter = rd.quarter
      and ft.carrier = rd.first_operating_carrier
      and ft.origin = rd.origin
      and ft.destination = rd.destination
    where rd.num_flights = 1 and rd.first_operating_carrier = 'CO'
      and rd.year = 2007 and rd.quarter = 4;

    select 'Completed non-stop itineraries for UA at '||to_char(sysdate, 'HH24:MI:SS')
    from dual;

    select 'Starting one-stop itineraries for UA at '||to_char(sysdate, 'HH24:MI:SS')
    as start_time from dual;

    insert into itineraries
      (id, year, quarter, month, num_flights,
       first_operating_carrier, second_operating_carrier,
       origin, connection, destination,
       planned_departure_time, planned_arrival_time, layover_duration,
       first_flight_id, second_flight_id)
    select itinerary_id_seq.nextval, 
      ft1.year, ft1.quarter, ft1.month, rd.num_flights,
      rd.first_operating_carrier, rd.second_operating_carrier,
      ft1.origin, ft1.destination, ft2.destination,
      ft1.planned_departure_time, ft2.planned_arrival_time,
      extract(hour from (ft2.planned_departure_time - ft1.planned_arrival_time)) * 60 +
        extract(minute from(ft2.planned_departure_time - ft1.planned_arrival_time)),
      ft1.id, ft2.id
    from route_demands rd
    join flights ft1
      on ft1.year = rd.year and ft1.quarter = rd.quarter
      and ft1.carrier = rd.first_operating_carrier
      and ft1.origin = rd.origin
      and ft1.destination = rd.connection
    join flights ft2
      on ft2.carrier = rd.second_operating_carrier
      and ft2.origin = ft1.destination 
      and ft2.destination = rd.destination
      and ft2.planned_departure_time >= 
        ft1.planned_arrival_time + numtodsinterval(45, 'MINUTE')
      and ft2.planned_departure_time <=
        ft1.planned_arrival_time + numtodsinterval(150, 'MINUTE')
    where rd.num_flights = 2 and rd.first_operating_carrier = 'CO'
      and rd.year = 2007 and rd.quarter = 4;

    select 'Completed one-stop itineraries for CO at '||to_char(sysdate, 'HH24:MI:SS')
    as end_time from dual;

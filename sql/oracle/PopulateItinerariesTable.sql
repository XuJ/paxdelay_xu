declare
  op_year route_demands.year%type;
  op_quarter route_demands.quarter%type;
  op_carrier route_demands.first_operating_carrier%type;
  cursor rdcursor is
    select rd.year, rd.quarter, 
      rd.first_operating_carrier
    from route_demands rd
    group by rd.year, rd.quarter,
      rd.first_operating_carrier
    order by rd.year, rd.quarter,
      rd.first_operating_carrier;
begin
  open rdcursor;
  loop
    fetch rdcursor into op_year, op_quarter, op_carrier;
    exit when rdcursor%notfound;

--    select 'Creating non-stop itineraries for'||op_carrier||' '||to_char(op_year)||
--     'Q'||to_char(op_quarter)||' at '||to_char(sysdate, 'HH24:MI:SS') from dual;

    -- Create the non-stop itineraries
    insert /*+ append */ into itineraries
      (id, year, quarter, month, day_of_month, day_of_week,
       hour_of_day, minutes_of_hour,
       num_flights, multi_day_flag,
       first_operating_carrier, origin, destination, 
       planned_departure_time, planned_arrival_time,
       first_flight_id)
    select itinerary_id_seq.nextval, 
      ft.year, ft.quarter, ft.month, ft.day_of_month, ft.day_of_week,
      ft.hour_of_day, ft.minutes_of_hour, 1, 0, 
      ft.carrier, ft.origin, ft.destination, 
      ft.planned_departure_time, ft.planned_arrival_time, 
      ft.id
    from flights ft
    where ft.year = op_year and ft.quarter = op_quarter
      and ft.carrier = op_carrier;

--    select 'Completed non-stop itineraries for'||op_carrier||' '||to_char(op_year)||
--     'Q'||to_char(op_quarter)||' at '||to_char(sysdate, 'HH24:MI:SS') from dual;

--    select 'Creating one-stop itineraries for'||op_carrier||' '||to_char(op_year)||
--     'Q'||to_char(op_quarter)||' at '||to_char(sysdate, 'HH24:MI:SS') from dual;
    commit;

    -- Create the one stop itineraries
    insert /*+ append */ into itineraries
      (id, year, quarter, month, day_of_month, day_of_week,
       hour_of_day, minutes_of_hour,
       num_flights, multi_day_flag,
       first_operating_carrier, second_operating_carrier,
       origin, connection, destination,
       planned_departure_time, planned_arrival_time, layover_duration,
       first_flight_id, second_flight_id)
    select itinerary_id_seq.nextval,
      ft1.year, ft1.quarter, ft1.month, ft1.day_of_month, ft1.day_of_week,
      ft1.hour_of_day, ft1.minutes_of_hour, 2,
      decode(ft2.day_of_month - ft1.day_of_month, 0, 0, 1),
      ft1.carrier, ft2.carrier,
      ucr.origin, ucr.connection, ucr.destination,
      ft1.planned_departure_time, ft2.planned_arrival_time,
      extract(hour from (ft2.planned_departure_time - ft1.planned_arrival_time)) * 60 +
        extract(minute from(ft2.planned_departure_time - ft1.planned_arrival_time)),
      ft1.id, ft2.id
    from flights ft1
    join unique_carrier_routes ucr
      on ucr.first_operating_carrier = ft1.carrier
      and ucr.origin = ft1.origin
      and ucr.connection = ft1.destination
    join flights ft2
      on ft2.carrier = ucr.second_operating_carrier
      and ft2.origin = ucr.connection
      and ft2.destination = ucr.destination
      and ft2.planned_departure_time >=
        ft1.planned_arrival_time + numtodsinterval(30, 'MINUTE')
      and ft2.planned_departure_time <=
        ft1.planned_arrival_time + numtodsinterval(300, 'MINUTE')
    where ft1.year = op_year and ft1.quarter = op_quarter
      and ft1.carrier = op_carrier and ucr.year = op_year
      and ucr.first_operating_carrier = op_carrier;

  commit;
--    select 'Completed one-stop itineraries for'||op_carrier||' '||to_char(op_year)||
--     'Q'||to_char(op_quarter)||' at '||to_char(sysdate, 'HH24:MI:SS') from dual;
  end loop;
  close rdcursor;
end;
/

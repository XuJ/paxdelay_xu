create or replace
package paxdelay_pkg as
  procedure create_itineraries(flight_carrier in flights.carrier%type,
    scenario in itineraries.scenario_name%type,
    minimum_layover in number, maximum_layover in number);
end paxdelay_pkg;

create or replace
package body paxdelay_pkg as

  procedure create_itineraries(
    flight_carrier in flights.carrier%type,
    scenario in itineraries.scenario_name%type,
    minimum_layover in number,
    maximum_layover in number) as
  begin  
    -- Remove old itineraries using this name, carrier, and day
    delete from itineraries it
    where it.scenario_name = scenario
      and it.carrier = flight_carrier;

    -- Create the non-stop itineraries
    insert into itineraries
      (id, scenario_name, carrier, origin, destination, 
       planned_departure_time, planned_arrival_time,
       first_flight_id)
    select itinerary_id_seq.nextval, scenario, ft.carrier,
      ft.origin, ft.destination, 
      ft.planned_departure_time, ft.planned_arrival_time, 
      ft.id
    from flights ft
    where ft.carrier = flight_carrier;

    -- Create the one-stop itineraries
    insert into itineraries
      (id, scenario_name, carrier, origin, connection, destination,
       planned_departure_time, planned_arrival_time, layover_duration,
       first_flight_id, second_flight_id)
    select itinerary_id_seq.nextval, scenario, ft1.carrier,
      ft1.origin, ft1.destination, ft2.destination,
      ft1.planned_departure_time, ft2.planned_arrival_time,
      extract(hour from (ft2.planned_departure_time - ft1.planned_arrival_time)) * 60 +
        extract(minute from(ft2.planned_departure_time - ft1.planned_arrival_time)),
      ft1.id, ft2.id
    from flights ft1
    join flights ft2
      on ft1.carrier = ft2.carrier
      and ft1.destination = ft2.origin
      and ft1.planned_arrival_time + numtodsinterval(minimum_layover, 'MINUTE') <=
        ft2.planned_departure_time
      and ft1.planned_arrival_time + numtodsinterval(maximum_layover, 'MINUTE') >=
        ft2.planned_arrival_time
    where ft1.carrier = flight_carrier;
  end create_itineraries;

end paxdelay_pkg;

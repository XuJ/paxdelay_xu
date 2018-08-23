-- AlexTableCreation
use paxdelay_2015;
select code from asqp_carriers order by code;
select count(*) as flights from flights group by carrier order by carrier;
select avg(arrival_delay) as avg_flight_delay from aotp group by carrier order by carrier;
select avg(arrival_delay) as avg_flight_delay from aotp;
select planned_first_carrier, planned_second_carrier from pax_delay_analysis group by planned_first_carrier, planned_second_carrier order by planned_first_carrier, planned_second_carrier;
select sum(num_passengers) as pax from pax_delay_analysis group by planned_first_carrier, planned_second_carrier order by planned_first_carrier, planned_second_carrier;
select sum(num_passengers) as number_of_disrupted_passengers from pax_delay_analysis where first_disruption_cause <> 0 group by planned_first_carrier, planned_second_carrier order by planned_first_carrier, planned_second_carrier;
select sum(num_passengers*trip_delay) as sum_of_pax_delay from pax_delay_analysis group by planned_first_carrier, planned_second_carrier order by planned_first_carrier, planned_second_carrier;
select sum(num_passengers*trip_delay) as sum_of_pax_delay_due_to_cancellation from pax_delay_analysis where first_disruption_cause = 2 group by planned_first_carrier, planned_second_carrier order by planned_first_carrier, planned_second_carrier;
select sum(num_passengers*trip_delay) as sum_of_pax_delay_due_to_missed_connection from pax_delay_analysis where first_disruption_cause = 1 group by planned_first_carrier, planned_second_carrier order by planned_first_carrier, planned_second_carrier;
select planned_first_carrier, planned_connection from pax_delay_analysis group by planned_first_carrier, planned_connection order by planned_first_carrier, planned_connection;
select sum(num_passengers) as pax_2 from pax_delay_analysis group by planned_first_carrier, planned_connection order by planned_first_carrier, planned_connection;
select sum(num_passengers) as number_of_disrupted_passengers_2 from pax_delay_analysis where first_disruption_cause <> 0 group by planned_first_carrier, planned_connection order by planned_first_carrier, planned_connection;
select sum(num_passengers*trip_delay) as sum_of_pax_delay_2 from pax_delay_analysis group by planned_first_carrier, planned_connection order by planned_first_carrier, planned_connection;
select sum(num_passengers*trip_delay) as sum_of_pax_delay_due_to_cancellation_2 from pax_delay_analysis where first_disruption_cause = 2 group by planned_first_carrier, planned_connection order by planned_first_carrier, planned_connection;
select sum(num_passengers*trip_delay) as sum_of_pax_delay_due_to_missed_connection_2 from pax_delay_analysis where first_disruption_cause = 1 group by planned_first_carrier, planned_connection order by planned_first_carrier, planned_connection;

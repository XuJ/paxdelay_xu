-- Table5Creation
use paxdelay_2016;
select count(*) as flights from flights group by carrier order by carrier;
select avg(arrival_delay) as avg_flight_delay from aotp group by carrier order by carrier;
select avg(arrival_delay) as avg_flight_delay from aotp;
select sum(num_passengers) as pax from pax_delay_analysis group by planned_first_carrier order by planned_first_carrier;
select sum(num_passengers) as number_of_disrupted_passengers from pax_delay_analysis where first_disruption_cause <> 0 group by planned_first_carrier order by planned_first_carrier;
select sum(num_passengers*trip_delay) as sum_of_pax_delay from pax_delay_analysis group by planned_first_carrier order by planned_first_carrier;
select sum(num_passengers*trip_delay) as sum_of_pax_delay_due_to_cancellation from pax_delay_analysis where first_disruption_cause = 2 group by planned_first_carrier order by planned_first_carrier;
select sum(num_passengers*trip_delay) as sum_of_pax_delay_due_to_missed_connection from pax_delay_analysis where first_disruption_cause = 1 group by planned_first_carrier order by planned_first_carrier;

-- Flights
use paxdelay;
source /mdsg/paxdelay_general_Xu/sql/mysql/foundation/m_CreateAirportsTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/load_data/m_CreateAirportsTable_load.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/m_CreateT100SeatsTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/m_CreateFlightsNoSeatsTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/m_CreateFlightsTable.sql;


-- Itineraries
use paxdelay;
source /mdsg/paxdelay_general_Xu/sql/mysql/m_CreateItinerariesTable_ItineraryGenerator.sql;


-- ItineraryAllocations
use paxdelay;
source /mdsg/paxdelay_general_Xu/sql/mysql/foundation/m_CreateTempItineraryAllocationsTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/load_data/m_CreateTempItineraryAllocationsTable_load.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/m_CreateItineraryAllocationsTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/foundation/m_CreateRelatedCarriersTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/load_data/m_CreateRelatedCarriersTable_load.sql;


-- PaxDelayCalculation
use paxdelay;
source /mdsg/paxdelay_general_Xu/sql/mysql/foundation/m_CreateTempPassengerDelaysTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/load_data/m_CreateTempPassengerDelaysTable_load.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/m_CreatePassengerDelaysTable.sql;
source /mdsg/paxdelay_general_Xu/sql/mysql/m_CreatePaxDelaysAnalysisTable.sql;


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


-- Backup
create database paxdelay_2016;
use paxdelay_2016;
source /mdsg/mysql_backup/paxdelay_2016.sql
drop database paxdelay;
create database paxdelay;


-- XuJiao 041217 
-- Add index
-- That took 11 min
LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/ProcessedItineraryDelays.csv'
INTO TABLE temp_passenger_delays
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
num_passengers,
original_first_flight_id,
original_second_flight_id,
first_disruption_cause,
first_disruption_hour,
num_disruptions,
disruption_origin_sequence,
disruption_cause_sequence,
last_flown_flight_id,
trip_delay
);

-- 041217 XuJ: Create index
create index idx_temp_passenger_delays_ft1ft2 
	on temp_passenger_delays(original_first_flight_id, original_second_flight_id);

-- 041217 XuJ: No such issue
/*
-- Issue #5
-- flights id: 1... 7,455,458
-- temp_passenger_delays id: 8,687,278... 10,424,410
-- difference is 2,987,606
update temp_passenger_delays set original_first_flight_id = 
	case when original_first_flight_id = "" 
		then ''
		else original_first_flight_id - 2987606
	end;
	
update temp_passenger_delays set original_second_flight_id = 	
	case when original_second_flight_id = "" 
		then ""
		else original_second_flight_id - 2987606
	end;
	
update temp_passenger_delays set last_flown_flight_id = 	
	case when last_flown_flight_id = "" 
		then ""
		else last_flown_flight_id - 2987606
	end;
*/


LOAD DATA INFILE '/export/mysql/import/MetronFlights_20070423.csv'
INTO TABLE temp_metron_flights
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  metron_id,
  database_id,
  carrier,
  origin,
  destination,
  tail_number,
  aircraft_type,
  seating_capacity,
  departure_time,
  wheels_off_time,
  wheels_on_time,
  arrival_time
);
-- 16173

-- Function to extract timezone
-- SELECT SPLIT_STR(string, delimiter, position)
CREATE FUNCTION SPLIT_STR(
  x VARCHAR(255),
  delim VARCHAR(12),
  pos INT
)
RETURNS VARCHAR(255)
RETURN REPLACE(SUBSTRING(SUBSTRING_INDEX(x, delim, pos),
       LENGTH(SUBSTRING_INDEX(x, delim, pos -1)) + 1),
       delim, '');


-- Populating _time_UTC, _tz, _local_hour fields
update temp_metron_flights set
	departure_time_UTC		= CONVERT_TZ(STR_TO_DATE(departure_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(departure_time, ' ', 4), '+00:00'),
	departure_tz			= SPLIT_STR(departure_time, ' ', 4),
	departure_local_hour	= hour(CONVERT_TZ(STR_TO_DATE(departure_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(departure_time, ' ', 4), '+00:00')),
	
	arrival_time_UTC		= CONVERT_TZ(STR_TO_DATE(arrival_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(arrival_time, ' ', 4), '+00:00'),
	arrival_tz				= SPLIT_STR(arrival_time, ' ', 4),
	arrival_local_hour		= hour(CONVERT_TZ(STR_TO_DATE(arrival_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(arrival_time, ' ', 4), '+00:00')),
	
	wheels_off_time_UTC		= CONVERT_TZ(STR_TO_DATE(wheels_off_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(wheels_off_time, ' ', 4), '+00:00'),
	wheels_off_tz			= SPLIT_STR(wheels_off_time, ' ', 4),
	wheels_off_local_hour	= hour(CONVERT_TZ(STR_TO_DATE(wheels_off_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(wheels_off_time, ' ', 4), '+00:00')),
	
	wheels_on_time_UTC		= CONVERT_TZ(STR_TO_DATE(wheels_on_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(wheels_on_time, ' ', 4), '+00:00'),
	wheels_on_tz			= SPLIT_STR(wheels_on_time, ' ', 4),
	wheels_on_local_hour	= hour(CONVERT_TZ(STR_TO_DATE(wheels_on_time,'%d-%b-%y %h.%i.%s %p'), SPLIT_STR(wheels_on_time, ' ', 4), '+00:00'));



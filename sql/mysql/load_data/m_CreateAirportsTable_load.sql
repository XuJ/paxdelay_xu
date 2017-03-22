-- XuJiao
-- change the AirportTimeZones file which includes the state and timezone as well


LOAD DATA LOCAL INFILE '/mdsg/AirportTimeZones_MIT.csv'
INTO TABLE airports
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(code,
city,
state,
timezone_region);

-- Clean the table to make it readable by convert_tz function
-- 
-- update airports set timezone_region = 'America/New_York' where timezone_region = 'EST';
-- update airports set timezone_region = 'America/Chicago' where timezone_region = 'CST';
-- update airports set timezone_region = 'America/Denver' where timezone_region = 'MST';
-- update airports set timezone_region = 'America/Los_Angeles' where timezone_region = 'PST';
-- delete from airports where timezone_region = '"VA'; 
-- delete from airports where timezone_region = ''; 

create index idx_airports_code
  on airports(code)
	using btree;
	
create index idx_airports_tzc
  on airports(timezone_region, code);
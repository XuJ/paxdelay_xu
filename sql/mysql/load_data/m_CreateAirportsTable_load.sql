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

create index idx_airports_code
  on airports(code)
	using btree;
	
create index idx_airports_tzc
  on airports(timezone_region, code);

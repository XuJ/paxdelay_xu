LOAD DATA INFILE '/export/mysql/import/airportTimeZones.csv'
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
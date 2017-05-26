-- http://openflights.org
--Airline ID 	Unique OpenFlights identifier for this airline.
--Name 		Name of the airline.
--Alias 	Alias of the airline. For example, All Nippon Airways is commonly known as "ANA".
--IATA 		2-letter IATA code, if available.
--ICAO 		3-letter ICAO code, if available.
--Callsign 	Airline callsign.
--Country 	Country or territory where airline is incorporated.
--Active 	"Y" if the airline is or has until recently been operational, "N" if it is defunct. (This is only a rough indication and should not be taken as 100% accurate.

create table temp_flight_carriers
(
 airlineid int(11),
 name varchar(255),
 alias varchar(255),
 iata_code	char(2),
 icao_code	char(3),
 callsign varchar(255),
 country varchar(255),
 active char(1)
);

LOAD DATA INFILE '/export/mysql/import/schaan/pax2007/FlightCarrierDataFull.csv'
INTO TABLE temp_flight_carriers
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
 airlineid,
 name,
 alias,
 iata_code,
 icao_code,
 callsign,
 country,
 active
);

insert into flight_carriers
(
  iata_code,
  icao_code,
  name)
select
  iata_code,
  icao_code,
  name
from temp_flight_carriers;

create index idx_flight_carriers_icao_code
  on flight_carriers(icao_code)
	using btree;

drop table temp_flight_carriers;
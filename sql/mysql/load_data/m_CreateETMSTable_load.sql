drop table if exists tmp_load_etms;

create table tmp_load_etms
(
	aircraft_id	varchar(8) not null,
	iata_aircraft_code	char(4),
	origin		varchar(4) not null,
	destination	varchar(4) not null,
	planned_departure_time_gmt	varchar(39),
	planned_arrival_time_gmt	varchar(39),
	actual_departure_time_gmt	varchar(39) not null,
	actual_arrival_time_gmt		varchar(39),
	planned_departure_time_local	varchar(39),
	planned_arrival_time_local	varchar(39),
	actual_departure_time_local	varchar(39) not null,
	actual_arrival_time_local	varchar(39),
	departure_flag	numeric(1, 0),
	arrival_flag	numeric(1, 0),
	flew_flag		numeric(1, 0),
	mg_flag			char(1)
)
ENGINE = MyISAM;



LOAD DATA INFILE '/export/mysql/import/schaan/pax2006/ETMS_data.csv'
INTO TABLE tmp_load_etms
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(aircraft_id,
iata_aircraft_code,
origin,
destination,
planned_departure_time_gmt,
planned_arrival_time_gmt,
actual_departure_time_gmt,
actual_arrival_time_gmt,
departure_flag,
arrival_flag,
flew_flag,
mg_flag);

-- removed local dates
--STR_TO_DATE(planned_departure_time_local,'%m/%d/%Y %H:%i:%S') as planned_departure_time_local, 
--STR_TO_DATE(planned_arrival_time_local,'%m/%d/%Y %H:%i:%S') as planned_arrival_time_local, 
--STR_TO_DATE(actual_departure_time_local,'%m/%d/%Y %H:%i:%S') as actual_departure_time_local, 
--STR_TO_DATE(actual_arrival_time_local,'%m/%d/%Y %H:%i:%S') as actual_arrival_time_local, 
insert into etms
(aircraft_id, icao_aircraft_code, iata_aircraft_code, origin, destination, planned_departure_time_gmt, planned_arrival_time_gmt, actual_departure_time_gmt, actual_arrival_time_gmt, planned_departure_time_local, 
	planned_arrival_time_local, actual_departure_time_local, actual_arrival_time_local, departure_flag, arrival_flag, flew_flag, mg_flag)
select aircraft_id, substring(aircraft_id, 1, 3) as icao_aircraft_code, iata_aircraft_code, origin, destination, 

case when planned_departure_time_gmt = "" then null
else STR_TO_DATE(REPLACE(planned_departure_time_gmt,'.000000000',''),'%d-%b-%y %h.%i.%s %p')
end as planned_departure_time_gmt,

case when planned_arrival_time_gmt = "" then null
else STR_TO_DATE(REPLACE(planned_arrival_time_gmt,'.000000000',''),'%d-%b-%y %h.%i.%s %p')
end as planned_arrival_time_gmt,

case when actual_departure_time_gmt = "" then null
else STR_TO_DATE(REPLACE(actual_departure_time_gmt,'.000000000',''),'%d-%b-%y %h.%i.%s %p')
end as actual_departure_time_gmt,

case when actual_arrival_time_gmt = "" then null
else STR_TO_DATE(REPLACE(actual_arrival_time_gmt,'.000000000',''),'%d-%b-%y %h.%i.%s %p')
end as actual_arrival_time_gmt,

null,
null,
null,
null,
departure_flag, arrival_flag, flew_flag, mg_flag
from tmp_load_etms;

-- Needed for etms_flights table creation
create index idx_etms
  on etms(actual_arrival_time_gmt,
	actual_arrival_time_local,
	actual_departure_time_gmt,
	actual_departure_time_local,
	aircraft_id,
	destination,
	iata_aircraft_code,
	icao_aircraft_code,
	origin,
	planned_arrival_time_gmt,
	planned_arrival_time_local,
	planned_departure_time_gmt,
	planned_departure_time_local
);

drop table if exists tmp_load_etms;
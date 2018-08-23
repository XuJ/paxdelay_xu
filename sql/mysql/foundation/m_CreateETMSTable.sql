drop table if exists etms;

-- due to pax2006 data file format we do not use local time fields
create table etms
(
	aircraft_id	varchar(8) not null,
	icao_aircraft_code char(3),
	iata_aircraft_code	char(4),
	origin		varchar(4) not null,
	destination	varchar(4) not null,
	planned_departure_time_gmt	datetime,
	planned_arrival_time_gmt	datetime,
	actual_departure_time_gmt	datetime not null,
	actual_arrival_time_gmt		datetime,
	planned_departure_time_local	datetime,
	planned_arrival_time_local	datetime,
	actual_departure_time_local	datetime,
	actual_arrival_time_local	datetime,
	departure_flag	numeric(1, 0),
	arrival_flag	numeric(1, 0),
	flew_flag		numeric(1, 0),
	mg_flag			char(1)
)
ENGINE = MyISAM;


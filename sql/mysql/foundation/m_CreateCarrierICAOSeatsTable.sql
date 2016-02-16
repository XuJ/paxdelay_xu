drop table if exists carrier_icao_seats;

create table carrier_icao_seats
(
	carrier	char(6) not null,
	icao_aircraft_code	varchar(4) not null,
	number_of_seats	numeric(3, 0) not null
)
ENGINE = MyISAM;


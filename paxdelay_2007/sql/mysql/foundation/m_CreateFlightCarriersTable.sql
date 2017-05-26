drop table if exists flight_carriers;

create table flight_carriers
(
	iata_code	char(2) not null,
	icao_code	char(3) not null,
	name		varchar(30) not null
)
ENGINE = MyISAM;

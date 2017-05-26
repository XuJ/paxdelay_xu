drop table if exists carrier_iata_seats;

create table carrier_iata_seats
(
	carrier	char(6) not null,
	aircraft_code	char(3) not null,
	seats	numeric(3, 0) not null
)
ENGINE = MyISAM;


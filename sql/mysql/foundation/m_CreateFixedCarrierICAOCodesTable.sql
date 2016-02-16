drop table if exists fixed_carrier_icao_codes;

create table fixed_carrier_icao_codes
(
	carrier			char(6) not null,
	incorrect_icao_aircraft_code	varchar(4) not null,
	fixed_icao_aircraft_code		varchar(4) not null
)
ENGINE = MyISAM;

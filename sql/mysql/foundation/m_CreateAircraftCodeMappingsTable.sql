drop table if exists aircraft_code_mappings;

create table aircraft_code_mappings
(
	iata_code		char(3) not null,
	icao_code		varchar(4),
	manufacturer_and_model	varchar(75) not null,
	inventory_manufacturer	varchar(20),
	inventory_model	varchar(15),
	wake_category	char(1)
)
ENGINE = MyISAM;

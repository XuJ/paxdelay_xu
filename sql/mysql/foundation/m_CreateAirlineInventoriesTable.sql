drop table if exists airline_inventories;

create table airline_inventories
(
	carrier			char(6) not null,
	year		numeric(4) not null,
	serial_number	varchar(12) not null,
	tail_number		varchar(7) not null,
	aircraft_status	char(1) not null,
	operating_status	char(1) not null,
	number_of_seats		numeric(3, 0),
	manufacturer	varchar(50) not null,
	model			varchar(16) not null,
	capacity_in_pounds	numeric(6, 0),
	acquisition_date	date not null
)
ENGINE = MyISAM;

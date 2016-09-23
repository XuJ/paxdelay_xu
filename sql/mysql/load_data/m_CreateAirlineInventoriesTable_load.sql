drop table if exists tmp_load_airline_inventories;

create table tmp_load_airline_inventories
(
carrier_name varchar(100),
year_of_first_delivery varchar(100),
unique_carrier_name varchar(100),
airline_id varchar(100),
unique_carrier varchar(100),

	carrier			char(6) not null,
	year		char(4) not null,
	serial_number	varchar(12) not null,
	tail_number		varchar(7) not null,
	aircraft_status	char(1) not null,
	operating_status	char(1) not null,
	number_of_seats		numeric(3, 0),
	manufacturer	varchar(50) not null,
	model			varchar(16) not null,
	capacity_in_pounds	numeric(6, 0),
	acquisition_date	varchar(10) not null
)
ENGINE = MyISAM;


LOAD DATA INFILE '/export/mysql/import/airline_inventory.csv'
INTO TABLE tmp_load_airline_inventories
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(year,
carrier,
carrier_name,
year_of_first_delivery,
unique_carrier_name,
serial_number,
tail_number,
aircraft_status,
operating_status,
number_of_seats,
manufacturer,
model,
capacity_in_pounds,
acquisition_date,
airline_id,
unique_carrier
);

insert into airline_inventories
	(carrier, year,	serial_number, tail_number,	aircraft_status, operating_status, number_of_seats, manufacturer, model, capacity_in_pounds, acquisition_date)
select carrier, year, serial_number, tail_number,	aircraft_status, operating_status, number_of_seats, manufacturer, model, capacity_in_pounds, STR_TO_DATE(acquisition_date,'%Y-%m-%d') as acquisition_date
from tmp_load_airline_inventories;

drop table if exists tmp_load_airline_inventories;
drop table if exists tmp_load_americawest_flight_legs;

create table tmp_load_americawest_flight_legs
(
	itinerary_id	numeric(12, 0) not null,
	ticket_number	numeric(13, 0) not null,
	num_flights		numeric(2, 0) not null,
	itinerary_sequence	numeric(2, 0) not null,
	carrier			char(6) not null,
	flight_number	varchar(6) not null,
	departure_date	char(10) not null,
	departure_time	char(5) not null,
	arrival_time	char(5) not null,
	origin			char(3) not null,
	destination		char(3) not null,
	fare_class		char(1) not null,
	fare			numeric(6, 2) not null
)
ENGINE = MyISAM;


LOAD DATA INFILE '/export/mysql/import/AmericaWestFlightLegs.txt'
INTO TABLE tmp_load_americawest_flight_legs
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(itinerary_id,
ticket_number,
num_flights,
itinerary_sequence,
carrier,
flight_number,
departure_date,
departure_time,
arrival_time,
origin,
destination,
fare_class,
fare);

insert into americawest_flight_legs
(itinerary_id, ticket_number, num_flights, itinerary_sequence, carrier, flight_number, departure_date, departure_time, arrival_time, origin, destination, fare_class, fare)
select itinerary_id, ticket_number, num_flights, itinerary_sequence, carrier, flight_number, STR_TO_DATE(departure_date,'%m/%d/%Y') as departure_date, departure_time, arrival_time, origin, destination, fare_class, fare
from tmp_load_americawest_flight_legs;

drop table if exists tmp_load_americawest_flight_legs;

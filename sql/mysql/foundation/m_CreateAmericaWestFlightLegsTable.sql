drop table if exists americawest_flight_legs;

create table americawest_flight_legs
(
	itinerary_id	numeric(12, 0) not null,
	ticket_number	numeric(13, 0) not null,
	num_flights		numeric(2, 0) not null,
	itinerary_sequence	numeric(2, 0) not null,
	carrier			char(6) not null,
	flight_number	varchar(6) not null,
	departure_date	date not null,
	departure_time	char(5) not null,
	arrival_time	char(5) not null,
	origin			char(3) not null,
	destination		char(3) not null,
	fare_class		char(1) not null,
	fare			numeric(6, 2) not null
)
ENGINE = MyISAM;


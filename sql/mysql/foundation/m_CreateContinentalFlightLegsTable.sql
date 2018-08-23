drop table if exists continental_flight_legs;

create table continental_flight_legs
(
	itinerary_id	numeric(12, 0) not null,
	num_flights		numeric(2, 0) not null,
	itinerary_sequence	numeric(2, 0) not null,
	carrier			char(6) not null,
	flight_number	varchar(6) not null,
	departure_time	char(5) not null,
	origin			char(3) not null,
	destination		char(3) not null
)
ENGINE = MyISAM;


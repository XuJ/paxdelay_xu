drop table if exists americawest_itineraries;

create table americawest_itineraries
(
	itinerary_id	numeric(12, 0) not null,
	ticket_number	numeric(13, 0) not null,
	num_flights		numeric(2, 0) not null,
	origin			char(3) not null,
	destination		char(3) not null,
	departure_time	char(5) not null,
	departure_date	date not null,
	itinerary_fare	numeric(6, 2) not null,
	passengers		numeric(4, 0) not null
)
ENGINE = MyISAM;


drop table if exists tmp_load_americawest_itineraries;

create table tmp_load_americawest_itineraries
(
	itinerary_id	numeric(12, 0) not null,
	ticket_number	numeric(13, 0) not null,
	num_flights		numeric(2, 0) not null,
	origin			char(3) not null,
	destination		char(3) not null,
	departure_time	char(5) not null,
	departure_date	char(10) not null,
	itinerary_fare	numeric(6, 2) not null,
	passengers		numeric(4, 0) not null
)
ENGINE = MyISAM;



LOAD DATA INFILE '/export/mysql/import/AmericaWestItineraries.txt'
INTO TABLE tmp_load_americawest_itineraries
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(itinerary_id,
ticket_number,
num_flights,
origin,
destination,
departure_time,
departure_date,
itinerary_fare,
passengers);

insert into americawest_itineraries
(itinerary_id, ticket_number, num_flights, origin, destination, departure_time, departure_date, itinerary_fare, passengers)
select itinerary_id, ticket_number, num_flights, origin, destination, departure_time, STR_TO_DATE(departure_date,'%m/%d/%Y') as departure_date, itinerary_fare, passengers
from tmp_load_americawest_itineraries;

drop table if exists tmp_load_americawest_itineraries;
drop table if exists tmp_continental_itineraries;

create table tmp_continental_itineraries
(
	itinerary_id numeric(12, 0) primary key not null,
	num_flights numeric(2, 0) not null,
	origin char(3) not null,
	destination char(3) not null,
	departure_time char(5) not null,
	day_of_week numeric(1, 0) not null,
	departure_date char(10) not null,
	number_samples numeric(4, 0) not null,
	number_flown numeric(4, 0) not null,
	number_no_show numeric(4, 0) not null,
	no_show_average numeric(4, 2) not null,
	show_average numeric(4, 2) not null
)
ENGINE = MyISAM;



LOAD DATA INFILE '/export/mysql/import/ContinentalItineraries.csv'
INTO TABLE tmp_continental_itineraries
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(itinerary_id,
num_flights,
origin,
destination,
departure_time,
day_of_week,
departure_date,
number_samples,
number_flown,
number_no_show,
no_show_average,
show_average);

insert into continental_itineraries
(itinerary_id, num_flights, origin, destination, departure_time, day_of_week, departure_date, number_samples, number_flown, number_no_show, no_show_average, show_average)
select itinerary_id, num_flights, origin, destination, departure_time, day_of_week, STR_TO_DATE(departure_date,'%m/%d/%Y') as departure_date, number_samples, number_flown, number_no_show, no_show_average, show_average
from tmp_continental_itineraries;

drop table if exists tmp_continental_itineraries;
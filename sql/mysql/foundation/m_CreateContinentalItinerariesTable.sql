drop table if exists continental_itineraries;

create table continental_itineraries
(
	itinerary_id numeric(12, 0) primary key not null,
	num_flights numeric(2, 0) not null,
	origin char(3) not null,
	destination char(3) not null,
	departure_time char(5) not null,
	day_of_week numeric(1, 0) not null,
	departure_date date not null,
	number_samples numeric(4, 0) not null,
	number_flown numeric(4, 0) not null,
	number_no_show numeric(4, 0) not null,
	no_show_average numeric(4, 2) not null,
	show_average numeric(4, 2) not null
)
ENGINE = MyISAM;


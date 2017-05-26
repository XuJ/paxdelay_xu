drop table if exists temp_metron_flights;

create table temp_metron_flights
(
  metron_id numeric(12) not null,
  database_id numeric(12),
  carrier char(2) not null,
  origin char(3) not null,
  destination char(3) not null,
  tail_number char(10),
  aircraft_type char(4) not null,
  seating_capacity numeric(4),
	departure_time varchar(255),
	arrival_time varchar(255),
	wheels_off_time varchar(255),
	wheels_on_time varchar(255),
	
	departure_time_UTC datetime not null,
	departure_tz char(15),
	departure_local_hour numeric(2),
	
	arrival_time_UTC datetime not null,
	arrival_tz char(15),
	arrival_local_hour numeric(2),
	
	wheels_off_time_UTC datetime not null,
	wheels_off_tz char(15),
	wheels_off_local_hour numeric(2),
	
	wheels_on_time_UTC datetime not null,
	wheels_on_tz char(15),
	wheels_on_local_hour numeric(2)
);
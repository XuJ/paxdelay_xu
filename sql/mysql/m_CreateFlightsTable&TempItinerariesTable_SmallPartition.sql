-- use paxdelay_monthly;
-- set sql_mode='';

delete from flights;
delete from temp_itineraries;

insert into flights select * from test2.flights limit 1000000;
insert into temp_itineraries select * from test2.temp_itineraries where num_flights =1 limit 500000;
insert into temp_itineraries select * from test2.temp_itineraries where num_flights =2 limit 500000;

-- select count(*) from flights;
-- select distinct num_flights, count(*) from temp_itineraries group by num_flights;
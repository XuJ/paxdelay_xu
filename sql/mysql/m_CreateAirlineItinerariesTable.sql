
drop table if exists temp_airline_itineraries;

create table temp_airline_itineraries
select coi.itinerary_id as itinerary_id,
	year(coi.departure_date) as year,
	month(coi.departure_date) as month,
	day(coi.departure_date) as day_of_month,
  coi.num_flights as num_flights, 
	coi.origin as origin,
	coi.destination as destination,

	convert_tz(addtime(coi.departure_date, STR_TO_DATE(coi.departure_time, '%H:%i')), ori.timezone_region, 'UTC') as planned_departure_time_UTC,
	ori.timezone_region as planned_departure_tz,
	hour(STR_TO_DATE(coi.departure_time, '%H:%i')) as planned_departure_local_hour,

  coi.number_flown as passengers
from continental_itineraries coi
join airports ori on ori.code = coi.origin;
-- 1,989,461

drop table if exists temp_airline_legs;

create table temp_airline_legs
select cof.itinerary_id as itinerary_id,
  cof.num_flights as num_flights, cof.itinerary_sequence as itinerary_sequence,
  cof.carrier as carrier, cof.flight_number as flight_number,
  cof.origin as origin, cof.destination as destination,
	
case 	when cof.itinerary_sequence = 1 then tai.planned_departure_time_UTC
	when greatest(
		time(CONVERT_TZ(tai.planned_departure_time_UTC,'+00:00',ori.timezone_region)),
		CAST(cof.departure_time as time)) = tai.planned_departure_time_UTC
	then 
		TIMESTAMPADD(day,1,
		CONVERT_TZ(
			cast(
				cast(date(CONVERT_TZ(tai.planned_departure_time_UTC,'+00:00',ori.timezone_region)) as datetime) 
				+ 
				cast(cof.departure_time as time) 
			as datetime),
			ori.timezone_region,
			'+00:00'
		))
	else 
		CONVERT_TZ(
			cast(
				cast(date(CONVERT_TZ(tai.planned_departure_time_UTC,'+00:00',ori.timezone_region)) as datetime) 
				+ 
				cast(cof.departure_time as time) 
			as datetime),
			ori.timezone_region,
			'+00:00'
		)
end as planned_departure_time_UTC

from continental_flight_legs cof
join temp_airline_itineraries tai on tai.itinerary_id = cof.itinerary_id
join airports ori on ori.code = cof.origin;
-- 3,946,416


alter table temp_airline_legs
  add (year numeric(4) default 0 not null,
       quarter int default 0 not null,
       month numeric(2) default 0 not null,
       day_of_month numeric(2) default 0 not null);


update temp_airline_legs tal
  set tal.year = year(tal.planned_departure_time_UTC),
      tal.quarter = quarter(tal.planned_departure_time_UTC),
      tal.month = month(tal.planned_departure_time_UTC),
      tal.day_of_month = day(tal.planned_departure_time_UTC);

alter table temp_airline_legs
add (planned_departure_time_UTC_sub150 datetime,
     planned_departure_time_UTC_add150 datetime);

update temp_airline_legs tal
set planned_departure_time_UTC_sub150 = date_sub(tal.planned_departure_time_UTC, interval 150 minute ),
    planned_departure_time_UTC_add150 = date_add(tal.planned_departure_time_UTC, interval 150 minute ); 
    
alter table temp_airline_legs
add (planned_departure_time_UTC_sub480 datetime,
     planned_departure_time_UTC_add480 datetime);

update temp_airline_legs tal
set planned_departure_time_UTC_sub480 = date_sub(tal.planned_departure_time_UTC, interval 480 minute ),
    planned_departure_time_UTC_add480 = date_add(tal.planned_departure_time_UTC, interval 480 minute ); 

create unique index idx_temp_airline_legs_iidis
  on temp_airline_legs(itinerary_id, itinerary_sequence);

create index temp_airline_legs_c
  on temp_airline_legs(carrier);

create index temp_airline_legs_fncyqod
  on temp_airline_legs(flight_number, carrier, year, quarter, origin, destination);

create index temp_airline_legs_odcfpp
  on temp_airline_legs(origin, destination, carrier, flight_number, 
                       planned_departure_time_UTC_sub, planned_departure_time_UTC_add); 
                       
create index idx_temp_airline_legs_3
  on temp_airline_legs(origin, destination, flight_number, planned_departure_time_UTC_sub150, planned_departure_time_UTC_add150, itinerary_id, itinerary_sequence);  

create index idx_temp_airline_legs_4
  on temp_airline_legs(origin, destination, planned_departure_time_UTC_sub480, planned_departure_time_UTC_add480, itinerary_id, itinerary_sequence);
  
create index idx_flights2_1
 on flights (carrier, destination, flight_number, id, origin, planned_departure_time_UTC);
  					   
update flights set flight_number = LPAD(flight_number,4,'0');
--update temp_airline_legs set flight_number = cast(flight_number as char);

drop table if exists temp_leg_matches;
create table temp_leg_matches
select 
  tal.itinerary_id, 
  tal.itinerary_sequence,
  tal.origin, 
  tal.destination,
  tal.carrier as continental_carrier,
  tal.flight_number as continental_flight_number,
  tal.planned_departure_time_UTC as continental_departure_time_UTC,
  
  ft.carrier as flight_carrier,
  ft.flight_number as flight_flight_number,
  ft.id as flight_id, 
  ft.planned_departure_time_UTC as flight_departure_time_UTC,	
  timestampdiff(minute, tal.planned_departure_time_UTC, ft.planned_departure_time_UTC) as minute_offset
from temp_airline_legs tal
join flights ft
  on ft.origin = tal.origin
  and ft.destination = tal.destination
  and ft.carrier = tal.carrier
  and ft.flight_number = cast(tal.flight_number as char)
  and ft.planned_departure_time_UTC between tal.planned_departure_time_UTC_sub150 and tal.planned_departure_time_UTC_add150;	
-- 1,966,409

create unique index idx_temp_leg_matches_iidis
  on temp_leg_matches(itinerary_id, itinerary_sequence);
  
-- Insert #1
create table temp_talft
select 
	tal.itinerary_id, 
	tal.itinerary_sequence,
	ft.id 							as flight_id, 
	tal.origin, 
	tal.destination,
	tal.carrier 					as continental_carrier,
	tal.flight_number 				as continental_flight_number,
	ft.carrier 						as flight_carrier,
	ft.flight_number 				as flight_flight_number,
	tal.planned_departure_time_UTC 	as continental_departure_time_UTC,
	ft.planned_departure_time_UTC 	as flight_departure_time_UTC,
	timestampdiff(minute, tal.planned_departure_time_UTC, ft.planned_departure_time_UTC) 	as minute_offset
from temp_airline_legs tal
join flights ft
  on ft.origin 						= tal.origin
  and ft.destination 				= tal.destination
  and ft.flight_number 				= tal.flight_number
  and ft.planned_departure_time_UTC between tal.planned_departure_time_UTC_sub150 and tal.planned_departure_time_UTC_add150;

create table temp_leg_matches_2
select tal.*
from temp_talft tal
left join temp_leg_matches tlm
  on tlm.itinerary_id 		= tal.itinerary_id 
  and tlm.itinerary_sequence 	= tal.itinerary_sequence
where tlm.itinerary_id is null;

insert into temp_leg_matches
select * from temp_leg_matches_2;

drop table temp_leg_matches_2;
drop table temp_talft;
-- !Insert #1
-- 2,997,410

select count(*)
from flights ft
where carrier = 'CO' and quarter = '4';
-- 80850

select count(*)
from flights ft
join
(select distinct tlm.flight_id
 from temp_leg_matches tlm) pin
on ft.id = pin.flight_id
where carrier = 'CO' and quarter = '4';
-- 80762

drop table if exists temp_potential_leg_matches;
create table temp_potential_leg_matches
select 
  tal.itinerary_id, 
  tal.itinerary_sequence,
  ft.id                         as flight_id, 
  tal.origin, 
  tal.destination,
  tal.carrier                    as continental_carrier,
  tal.flight_number              as continental_flight_number,
  ft.carrier                     as flight_carrier,
  ft.flight_number               as flight_flight_number,
  tal.planned_departure_time_UTC as continental_departure_time_UTC,
  ft.planned_departure_time_UTC  as flight_departure_time_UTC,
  timestampdiff(minute, tal.planned_departure_time_UTC, ft.planned_departure_time_UTC) as minute_offset
from temp_airline_legs tal
left join flights ft
  on ft.origin                = tal.origin
  and ft.destination          = tal.destination
  and ft.planned_departure_time_UTC between tal.planned_departure_time_UTC_sub480 and tal.planned_departure_time_UTC_add480
left join temp_leg_matches tlm
  on tlm.itinerary_id        = tal.itinerary_id 
  and tlm.itinerary_sequence = tal.itinerary_sequence
where tlm.itinerary_id is null;

select itinerary_id, itinerary_sequence
from temp_leg_matches
group by itinerary_id, itinerary_sequence
having count(*) > 1;
-- 1,671,562

-- The number of itinerary legs with matching flights
select count(*)
from
(select tlm.itinerary_id, tlm.itinerary_sequence
 from temp_leg_matches tlm
 group by tlm.itinerary_id, tlm.itinerary_sequence) t;

-- The number of itinerary legs with potential origin 
-- and destination matches within 8 hours of the departure time
select count(*)
from
(select tpm.itinerary_id, tpm.itinerary_sequence
 from temp_potential_leg_matches tpm
 group by tpm.itinerary_id, tpm.itinerary_sequence) t;
-- 949,006
 
-- The number of distinct flights from the itinerary legs
-- with potential origin and destination matches within
-- 8 hours of the departure time
select count(*)
from
(select tpm.continental_carrier, tpm.continental_flight_number,
   tpm.origin, tpm.destination, tpm.continental_departure_time_UTC
 from temp_potential_leg_matches tpm
 group by tpm.continental_carrier, tpm.continental_flight_number,
   tpm.origin, tpm.destination, tpm.continental_departure_time_UTC) t;
-- 138,521

create index idx_temp_leg_matches_fid
  on temp_leg_matches(flight_id);

drop table if exists temp_leg_flight_joins;
create table temp_leg_flight_joins
select
  tal.itinerary_id, tal.itinerary_sequence,
  tal.origin, tal.destination,
  ft.id as flight_id,
  ft.carrier,
  tal.carrier as airline_carrier,
  ft.flight_number,
  tal.flight_number as airline_flight_number,
  ft.planned_departure_time_UTC,
	ft.planned_departure_tz,
	ft.planned_departure_local_hour,
  tal.planned_departure_time_UTC as airline_departure_time_UTC,
  ft.planned_arrival_time_UTC,
	ft.planned_arrival_tz,
	ft.planned_arrival_local_hour
from temp_airline_legs tal
left join temp_leg_matches tlm
  on tlm.itinerary_id = tal.itinerary_id and tlm.itinerary_sequence = tal.itinerary_sequence
left join flights ft on ft.id = tlm.flight_id;
-- 3,946,416

-- Should return no rows
select itinerary_id, itinerary_sequence
from temp_leg_flight_joins
group by itinerary_id, itinerary_sequence
having count(*) > 1;


drop table if exists temp_unique_leg_flights;
create table temp_unique_leg_flights
(
	id integer not null auto_increment, primary key (id)
)
select
  tlf.origin, tlf.destination,
  tlf.flight_id,
  tlf.carrier, tlf.airline_carrier,
  tlf.flight_number, tlf.airline_flight_number,
  tlf.planned_departure_time_UTC, tlf.airline_departure_time_UTC,
  tlf.planned_arrival_time_UTC
from 
(select tlf.origin, tlf.destination, 
   tlf.flight_id,
   tlf.carrier, tlf.airline_carrier,
   tlf.flight_number, tlf.airline_flight_number,
   tlf.planned_departure_time_UTC,
	 tlf.airline_departure_time_UTC,
   tlf.planned_arrival_time_UTC
 from temp_leg_flight_joins tlf
 group by tlf.origin, tlf.destination, tlf.flight_id,
   tlf.carrier, tlf.airline_carrier,
   tlf.flight_number, tlf.airline_flight_number,
   tlf.planned_departure_time_UTC, tlf.airline_departure_time_UTC,
   tlf.planned_arrival_time_UTC) tlf;
-- 437,064

create table temp_amw
select 
 	itinerary_id, 
 	departure_date, 
 	num_flights,
 	origin, destination,
	convert(substring_index(departure_time, ':',1), signed) 			as hour_of_day,
	convert(substring_index(departure_time, ':',-1), signed) 			as minutes_of_hour,
	floor(convert(substring_index(departure_time, ':',1), signed) / 24) as next_day_flag,
	passengers
from americawest_itineraries;
-- 5,087,090

drop table if exists temp_americawest_itineraries;
create table temp_americawest_itineraries
select 
	amw.itinerary_id 				as itinerary_id,
	year(amw.departure_date) 		as year,
	month(amw.departure_date) 		as month,
	dayofmonth(amw.departure_date) 	as day_of_month,
	dayofweek(amw.departure_date) 	as day_of_week,
	amw.num_flights 				as num_flights, 
	amw.origin 						as origin, 
	amw.destination 				as destination,
	date_add(convert_tz(addtime(amw.departure_date, maketime(case when amw.next_day_flag = 1 then  amw.hour_of_day - 24 else amw.hour_of_day end, amw.minutes_of_hour, 0)), 
	ori.timezone_region, 'UTC'), interval amw.next_day_flag day) as planned_departure_time_UTC,
  	amw.passengers
from temp_amw amw
join airports ori on ori.code = amw.origin;
drop table temp_amw;
-- 4,685,816


create table temp_amf
select itinerary_id, num_flights, itinerary_sequence,
   carrier, flight_number, departure_date,
		convert(substring_index(departure_time, ':',1), signed) as departure_hour,
		convert(substring_index(departure_time, ':',-1), signed) as departure_minutes,
		floor(convert(substring_index(departure_time, ':',1), signed) / 24) as next_day_departure,
		convert(substring_index(arrival_time, ':',1), signed) as arrival_hour,
		convert(substring_index(arrival_time, ':',-1), signed) as arrival_minutes,
		floor(convert(substring_index(arrival_time, ':',1), signed) / 24) as next_day_arrival,
   origin, destination
from americawest_flight_legs;

create index idx_tmp_amf
  on temp_amf (itinerary_id, origin, destination);

create index idx_tmp_tai
  on temp_americawest_itineraries (itinerary_id);   
  
drop table if exists temp_americawest_legs;
create table temp_americawest_legs
select 
	amf.itinerary_id,
	amf.num_flights, 
	amf.itinerary_sequence,
	amf.carrier, 
	amf.flight_number,
	date_add(convert_tz(addtime(amf.departure_date, maketime(case when amf.next_day_departure = 1 then  amf.departure_hour - 24 else amf.departure_hour end, amf.departure_minutes, 0)), 
		ori.timezone_region, 'UTC'), interval amf.next_day_departure day) as planned_departure_time_UTC,
	case when greatest(date_add(convert_tz(addtime(amf.departure_date, maketime(case when amf.next_day_departure = 1 then  amf.departure_hour - 24 else amf.departure_hour end, amf.departure_minutes, 0)), 
		ori.timezone_region, 'UTC'), interval amf.next_day_departure day),
		date_add(convert_tz(addtime(amf.departure_date, maketime(case when amf.next_day_arrival = 1 then  amf.arrival_hour - 24 else amf.arrival_hour end, amf.arrival_minutes, 0)), 
		dest.timezone_region, 'UTC'), interval amf.next_day_arrival day)) = 
		date_add(convert_tz(addtime(amf.departure_date, maketime(case when amf.next_day_departure = 1 then  amf.departure_hour - 24 else amf.departure_hour end, amf.departure_minutes, 0)), 
		ori.timezone_region, 'UTC'), interval amf.next_day_departure day)
		then 	date_add(convert_tz(addtime(amf.departure_date, maketime(case when amf.next_day_arrival = 1 then  amf.arrival_hour - 24 else amf.arrival_hour end, amf.arrival_minutes, 0)), 
		dest.timezone_region, 'UTC'), interval 1 + amf.next_day_arrival day)
		else	date_add(convert_tz(addtime(amf.departure_date, maketime(case when amf.next_day_arrival = 1 then  amf.arrival_hour - 24 else amf.arrival_hour end, amf.arrival_minutes, 0)), 
		dest.timezone_region, 'UTC'), interval amf.next_day_arrival day)
		end as planned_arrival_time_UTC,
	amf.origin, 
	amf.destination
from temp_amf amf
join temp_americawest_itineraries tai 
	on tai.itinerary_id = amf.itinerary_id
join airports ori 
	on ori.code = amf.origin
join airports dest 
	on dest.code = amf.destination;
	
drop table temp_amf;
-- 6,562,161

-- drop #1
create table temp_dd
select tai.itinerary_id
from temp_americawest_itineraries tai
join temp_americawest_legs tal 
on tal.itinerary_id = tai.itinerary_id
group by tai.itinerary_id, tai.num_flights
having tai.num_flights != count(tal.itinerary_sequence);

delete from temp_americawest_itineraries 
where itinerary_id in
(
	select itinerary_id
	from temp_dd
);
drop table temp_dd;
-- 15
-- !drop #1

-- drop #2
delete from temp_americawest_legs tal
where not exists
(
 select * from temp_americawest_itineraries
 where itinerary_id = tal.itinerary_id
);
-- 23 (o)
-- !drop #2

drop table if exists temp_unique_americawest_legs;
create table temp_unique_americawest_legs
(
	id integer not null auto_increment, primary key (id)
)
select
	tal.origin, 
	tal.destination,
	tal.carrier, 
	tal.flight_number, 
	tal.planned_departure_time_UTC, 
	tal.planned_arrival_time_UTC
from
(
 select 
	tal.origin, 
	tal.destination,
	tal.carrier, 
	tal.flight_number, 
	tal.planned_departure_time_UTC, 
	tal.planned_arrival_time_UTC
 from temp_americawest_legs tal
 group by tal.origin, 
	tal.destination, 
	tal.carrier, 
	tal.flight_number, 
	tal.planned_departure_time_UTC, 
	tal.planned_arrival_time_UTC
) tal;
-- 113,807

-- temp_americawest_legs_merged
create index idx_temp_ual_1
  on temp_unique_americawest_legs(carrier, flight_number, origin, destination, planned_departure_time_utc, planned_arrival_time_UTC);

create index idx_temp_tai_1
  on temp_americawest_legs(carrier, flight_number, origin, destination, planned_departure_time_utc, planned_arrival_time_UTC);

create table temp_j	
select 	tai.year as year, 
	floor((tai.month + 2) / 3) as quarter, 
	tai.month as month, 
	tai.day_of_month as day_of_month, 
	tai.day_of_week as day_of_week,
	tai.passengers as passengers, 
	ual.id as ual_id, 
	tal.itinerary_sequence as itinerary_sequence, 
	tai.itinerary_id as itinerary_id
from temp_americawest_itineraries tai
join temp_americawest_legs tal  
	on tal.itinerary_id = tai.itinerary_id
join temp_unique_americawest_legs ual 
	on ual.carrier 				= tal.carrier 
	and ual.flight_number 			= tal.flight_number 
	and ual.origin 				= tal.origin 
	and ual.destination 			= tal.destination
	and ual.planned_departure_time_UTC 	= tal.planned_departure_time_UTC 
	and ual.planned_arrival_time_UTC 	= tal.planned_arrival_time_UTC;
-- 6,562,138

create table temp_gc
select j.year as year, j.quarter  as quarter, j.month as month, j.day_of_month as day_of_month, j.day_of_week as day_of_week,
	avg(j.passengers) as passengers,
	count(j.itinerary_sequence) as num_flights,
	sum(j.ual_id) as total_ual_id,
	convert(SUBSTRING_INDEX(group_concat(j.ual_id order by j.itinerary_sequence desc),',',1 ),decimal) ual_id_max,
	convert(SUBSTRING_INDEX(group_concat(j.ual_id order by j.itinerary_sequence),',',1 ),decimal) ual_id_min,
	j.itinerary_id as itinerary_id
from temp_j j
group by j.itinerary_id, j.year, j.month, j.day_of_month, j.day_of_week
having count(j.itinerary_sequence) <= 3;
-- 4,684,603

create table temp_tin
select 
	gc.year as year, 
	gc.quarter as quarter, 
	gc.month as month, 
	gc.day_of_month as day_of_month, 
	gc.day_of_week as day_of_week,
	gc.passengers as passengers, 
	gc.total_ual_id as sum_flight_ids,
	sum(gc.ual_id_max) as last_flight_id,
	sum(gc.ual_id_min) as first_flight_id,
	gc.num_flights
from temp_gc gc
group by gc.itinerary_id, gc.year, gc.month, gc.day_of_month, gc.day_of_week;
-- 4,684,603

create table temp_tin_2
select 
  tin.year, 
  tin.quarter, 
  tin.month, 
  tin.day_of_month,
  tin.day_of_week, 
  tin.num_flights, 
  tin.first_flight_id, 
  tin.last_flight_id,
  tin.sum_flight_ids, 
  sum(tin.passengers) as passengers
from temp_tin tin
group by tin.year, tin.quarter, tin.month, tin.day_of_month, tin.day_of_week, tin.num_flights, tin.first_flight_id, tin.last_flight_id, tin.sum_flight_ids;
-- 701,228

drop table temp_americawest_legs_merged;
create table temp_americawest_legs_merged
(
  id integer not null auto_increment, primary key (id)
)
select 
  tal.year, 
  tal.quarter, 
  tal.month, 
  tal.day_of_month,
  tal.day_of_week, 
  tal.passengers, 
  tal.num_flights, 
  tal.first_flight_id,
  case tal.num_flights 
    when 2 then tal.last_flight_id
    when 3 then tal.sum_flight_ids - tal.first_flight_id - tal.last_flight_id
	else null
    end as second_flight_id,
  case when tal.num_flights = 3 then tal.last_flight_id else null end as third_flight_id
from temp_tin_2 tal;
-- 701,228

drop table temp_j;
drop table temp_gc;
drop table temp_tin;
drop table temp_tin_2;
--! temp_americawest_legs_merged

-- temp_airline_legs_merged
create index idx_temp_tai_1
  on temp_airline_itineraries(itinerary_id);

create table temp_j
select 	
	tai.year 			as year, 
	floor((tai.month + 2) / 3) 	as quarter, 
	tai.month 			as month, 
	tai.day_of_month 		as day_of_month, 
	tai.passengers 			as passengers,
	ulf.id 				as ulf_id,
	tal.itinerary_sequence,
	tai.itinerary_id 		as itinerary_id
from temp_airline_itineraries tai
join temp_airline_legs tal 
	on tal.itinerary_id 			= tai.itinerary_id
join temp_unique_leg_flights ulf
	on ulf.airline_carrier 			= tal.carrier
	and ulf.airline_flight_number 		= tal.flight_number
	and ulf.origin 				= tal.origin
	and ulf.destination 			= tal.destination
	and ulf.airline_departure_time_UTC 	= tal.planned_departure_time_UTC;
--3,946,416
	
create table temp_gc
select 
	j.year, 
	j.quarter, 
	j.month, 
	j.day_of_month, 
	avg(j.passengers) 		as passengers,
	sum(j.ulf_id) 			as total_ulf_id,
	count(j.itinerary_sequence) 	as num_flights,
	j.itinerary_id,
	convert(SUBSTRING_INDEX(group_concat(j.ulf_id order by j.itinerary_sequence desc),',',1 ),decimal) ulf_id_max,
	convert(SUBSTRING_INDEX(group_concat(j.ulf_id order by j.itinerary_sequence),',',1 ),decimal) ulf_id_min
from temp_j j
group by j.year, j.quarter, j.month, j.day_of_month, j.itinerary_id	
having count(j.itinerary_sequence) <= 3;
--1,977,249

create table temp_tai
select 
	gc.year, 
	gc.quarter, 
	gc.month, 
	gc.day_of_month, 
	gc.passengers, 
	gc.total_ulf_id as sum_flight_ids, 
	gc.num_flights,
	sum(gc.ulf_id_max) as last_flight_id,
	sum(gc.ulf_id_min) as first_flight_id
from temp_gc gc
group by gc.year, gc.quarter, gc.month, gc.day_of_month, gc.itinerary_id;
--1,977,249

drop table if exists temp_airline_legs_merged;
create table temp_airline_legs_merged
(
	id integer not null auto_increment, 
	primary key (id)
)
select 
	tai.year, 
	tai.quarter, 
	tai.month, 
	tai.day_of_month,
	tai.passengers, 
	tai.num_flights, 
	tai.first_flight_id,
	case tai.num_flights
		when 2 then tai.last_flight_id
		when 3 then tai.sum_flight_ids - tai.first_flight_id - tai.last_flight_id
		else null
	end as second_flight_id,
	case when tai.num_flights = 3 then tai.last_flight_id else null end as third_flight_id
from temp_tai tai;
--1,977,249

drop table temp_j;
drop table temp_gc;
drop table temp_tai;
-- !temp_airline_legs_merged

create table airline_itineraries
(
  itinerary_id numeric(12) not null,
  year numeric(4) not null,
  quarter int not null,
  month numeric(2) not null,
  day_of_month numeric(2) not null,
  day_of_week numeric(1) not null,
  passengers numeric(4) not null,
  num_flights numeric(1) not null,
  first_carrier char(2) not null,
  first_flight_number varchar(6) not null,

  first_departure_time_UTC date not null,
	first_departure_tz char(19),
	first_departure_local_hour numeric(2),

  first_arrival_time_UTC date,
	first_arrival_tz char(19),
	first_arrival_local_hour numeric(2),

  first_flight_id numeric(12),
  second_carrier char(2),
  second_flight_number varchar(6),

  second_departure_time_UTC date,
	second_departure_tz char(19),
	second_departure_local_hour numeric(2),

  second_arrival_time_UTC date,
	second_arrival_tz char(19),
	second_arrival_local_hour numeric(2),

  second_flight_id numeric(12),
  origin char(3) not null,
  connection char(3),
  destination char(3) not null,
  trip_duration numeric(4),
  layover_duration numeric(4),
  multi_day_flag numeric(1)
);

-- insert #1
insert into airline_itineraries
(
	itinerary_id, 
	year, 
	quarter, 
	month, 
	day_of_month, 
	day_of_week, 
	passengers, 
	num_flights,  

	first_carrier,
	first_flight_number,
	first_departure_time_UTC,
	first_arrival_time_UTC,
	first_flight_id,

	second_carrier,
	second_flight_number,
	second_departure_time_UTC,
	second_arrival_time_UTC,
	second_flight_id,

	origin,
	connection,
	destination,
	trip_duration,
	layover_duration,
	multi_day_flag
)
select 
	alm.id 			as itinerary_id, 
	alm.year 		as year, 
	alm.quarter 		as quarter, 
	alm.month 		as month,
	alm.day_of_month 	as day_of_month, 
	0 			as day_of_week, 
	alm.passengers 		as passengers, 
	1 			as num_flights,
	case when first.carrier is null then first.airline_carrier else first.carrier end 							as first_carrier,
	case when first.carrier is null then first.airline_flight_number else first.flight_number end 						as first_flight_number,
	case when first.planned_departure_time_UTC is null then first.airline_departure_time_UTC else first.planned_departure_time_UTC end 	as first_departure_time_UTC,
	first.planned_arrival_time_UTC as first_arrival_time_UTC,
	first.flight_id 	as first_flight_id,
	null 			as second_carrier, 
	null 			as second_flight_number, 
	null 			as second_departure_time_UTC, 
	null 			as second_arrival_time_UTC, 
	null 			as second_flight_id,
	first.origin 		as origin, 
	null 			as connection, 
	first.destination 	as destination,
	null 			as trip_duration, 
	null 			as layover_duration, 
	0 as multi_day_flag
from temp_airline_legs_merged alm
join temp_unique_leg_flights first 
	on first.id 		= alm.first_flight_id
where alm.num_flights = 1
union all
select 
	alm.id 			as itinerary_id, 
	alm.year 		as year, 
	alm.quarter 		as quarter, 
	alm.month 		as month,
	alm.day_of_month 	as day_of_month, 
	0 			as day_of_week, 
	alm.passengers 		as passengers, 
	2 			as num_flights,
	case when first.carrier is null then first.airline_carrier else first.carrier end 							as first_carrier,
	case when first.flight_number is null then first.airline_flight_number else first.flight_number end 					as first_flight_number,
	case when first.planned_departure_time_UTC is null then first.airline_departure_time_UTC else first.planned_departure_time_UTC end 	as first_departure_time_UTC,
	first.planned_arrival_time_UTC as first_arrival_time_UTC,
	first.flight_id 	as first_flight_id,
	case when second.carrier is null then second.airline_carrier else second.carrier end 							as second_carrier,
	case when second.flight_number is null then second.airline_flight_number else second.flight_number end 					as second_flight_number,
	case when second.planned_departure_time_UTC is null then second.airline_departure_time_UTC else second.planned_departure_time_UTC end 	as second_departure_time_UTC,
	second.planned_arrival_time_UTC as second_arrival_time_UTC,
	second.flight_id 	as second_flight_id,
	first.origin 		as origin, 
	first.destination 	as connection, 
	second.destination 	as destination,
	null 			as trip_duration, 
	null 			as layover_duration, 
	0 			as multi_day_flag
from temp_airline_legs_merged alm
join temp_unique_leg_flights first 
	on first.id 		= alm.first_flight_id
join temp_unique_leg_flights second 
	on second.id 		= alm.second_flight_id
where alm.num_flights = 2;
-- !insert #1
--1,832,711

-- insert #2
insert into airline_itineraries
(
	itinerary_id, 
	year, 
	quarter, 
	month, 
	day_of_month, 
	day_of_week, 
	passengers, 
	num_flights,  

	first_carrier,
	first_flight_number,
	first_departure_time_UTC,
	first_arrival_time_UTC,
	first_flight_id,

	second_carrier,
	second_flight_number,
	second_departure_time_UTC,
	second_arrival_time_UTC,
	second_flight_id,

	origin,
	connection,
	destination,
	trip_duration,
	layover_duration,
	multi_day_flag
)
select 
	alm.id, 
	alm.year, 
	alm.quarter, 
	alm.month, 
	alm.day_of_month, 
	alm.passengers, 
	1, 
	first.carrier, 
	first.flight_number,
	first.planned_departure_time_UTC, 
	first.planned_arrival_time_UTC,

	null, 
	null, 
	null, 
	null, 
	null, 
	null,

	first.origin, 
	null, 
	first.destination,

	alm.day_of_week, 
	null, 
	null, 
	0
from temp_americawest_legs_merged alm
join temp_unique_americawest_legs first 
	on first.id 	= alm.first_flight_id
where alm.num_flights = 1;
-- !insert #2
--1,924,598

-- insert #3
insert into airline_itineraries
(
	itinerary_id, 
	year, 
	quarter, 
	month, 
	day_of_month, 
	day_of_week, 
	passengers, 
	num_flights,  

	first_carrier,
	first_flight_number,
	first_departure_time_UTC,
	first_arrival_time_UTC,
	first_flight_id,

	second_carrier,
	second_flight_number,
	second_departure_time_UTC,
	second_arrival_time_UTC,
	second_flight_id,

	origin,
	connection,
	destination,
	trip_duration,
	layover_duration,
	multi_day_flag
)
select 
	alm.id, 
	alm.year, 
	alm.quarter, 
	alm.month,
	alm.day_of_month, 
	alm.passengers, 
	2,
	first.carrier, 
	first.flight_number,
	first.planned_departure_time_UTC, 
	first.planned_arrival_time_UTC,
	null,
	second.carrier, 
	second.flight_number,
	second.planned_departure_time_UTC, 
	second.planned_arrival_time_UTC,
	null,
	first.origin, 
	first.destination, 
	second.destination,
	alm.day_of_week, 
	null, 
	null, 
	0
from temp_americawest_legs_merged alm
join temp_unique_americawest_legs first 
	on first.id 	= alm.first_flight_id
join temp_unique_americawest_legs second 
	on second.id 	= alm.second_flight_id
where alm.num_flights = 2;
-- !insert #3
--2,524,825

-- updates
update airline_itineraries
set day_of_week = dayofweek(first_departure_time_UTC);

update airline_itineraries
set trip_duration = timestampdiff(minute, first_arrival_time_UTC, first_departure_time_UTC)
where 	num_flights = 1 
	and first_arrival_time_UTC is not null 
	and trip_duration is null;

update airline_itineraries
set trip_duration = timestampdiff(minute, second_arrival_time_UTC, first_departure_time_UTC)
where	num_flights = 2 
	and second_arrival_time_UTC is not null 
	and trip_duration is null;

update airline_itineraries
set layover_duration = timestampdiff(minute, second_departure_time_UTC, first_arrival_time_UTC)
where	num_flights = 2 
	and first_arrival_time_UTC is not null 
	and layover_duration is null;

update airline_itineraries
set multi_day_flag = 1
where 	day(first_departure_time_UTC) != day(second_departure_time_UTC) 
	and multi_day_flag = 0;
-- !updates

-- indexes
create index idx_airline_itins_c1c2yq
  on airline_itineraries(first_carrier, second_carrier, year, quarter);

create index idx_airline_itins_nfft1
  on airline_itineraries(num_flights, first_flight_id);

create index bmx_airline_itins_nfft1ft2
  on airline_itineraries(num_flights, first_flight_id, second_flight_id);
-- !indexes

-- additional 
select count(*)
from airline_itineraries
where num_flights = 1
and first_flight_id is not null;
-- 166,183

select sum(passengers)
from airline_itineraries
where num_flights = 1
and first_flight_id is not null;
-- 5,299,123

select count(*)
from airline_itineraries
where num_flights = 2 and first_flight_id is not null and second_flight_id is not null;
-- 902,493

select sum(passengers)
from airline_itineraries
where num_flights = 2
  and first_flight_id is not null
  and second_flight_id is not null;
-- 2,007,539
  
select count(*)
from airline_itineraries
where num_flights = 2
  and first_flight_id is not null
  and second_flight_id is not null
  and layover_duration <= 300;
-- 675,754
  
select sum(passengers)
from airline_itineraries
where num_flights = 2
  and first_flight_id is not null
  and second_flight_id is not null
  and layover_duration <= 300;
-- 1,562,938
-- !additional
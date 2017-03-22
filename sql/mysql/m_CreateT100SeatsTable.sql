-- use paxdelay_xu;
-- XuJiao
-- That took 0 min

-- set sql_mode="";

drop table if exists temp_1;
create table temp_1
(
seats_mean decimal(65,30),
seats_squared_mean decimal(65,30)
)
select t100.year, t100.quarter, t100.month, t100.carrier,
    t100.origin, t100.destination, count(*) as num_aircraft_types,
    sum(t100.seats) / sum(t100.departures_performed) as seats_mean,
    
    sum(power(t100.seats, 2) / t100.departures_performed) / sum(t100.departures_performed) as seats_squared_mean,
     sum(power(t100.seats, 2) / t100.departures_performed) as temp_power,
    
    sum(t100.departures_performed) as departures_performed
  from t100_segments t100
  where t100.departures_performed > 0
  group by t100.year, t100.quarter, t100.month, t100.carrier,
    t100.origin, t100.destination;

drop table if exists temp_2;
create table temp_2
select t100.year, t100.quarter, t100.month, t100.carrier,
   t100.origin, t100.destination, t100.departures_performed,
   t100.num_aircraft_types, t100.seats_mean,
   t100.seats_squared_mean,
		case when t100.departures_performed = 1 then 0
			else round((t100.seats_squared_mean - power(t100.seats_mean, 2)) * t100.departures_performed / (t100.departures_performed - 1), 3)
		end as seats_variance
 from
 temp_1 t100;


DROP TABLE IF EXISTS t100_seats;
create table t100_seats
(
  year numeric(4, 0) not null,
  quarter numeric(1, 0) not null,
  month numeric(2, 0) not null,
  carrier varchar(3) not null,
  origin char(3) not null,
  destination char(3) not null,
  departures_performed numeric(6, 0) not null,
  num_aircraft_types numeric(2, 0) not null,
  seats_mean numeric(6, 3) not null,
  seats_squared_mean numeric(9, 3) not null,
  seats_std_dev numeric(6, 3) ,
  seats_coeff_var decimal(65,30) 
);

insert into t100_seats
select 
	t100.year,
	t100.quarter,
	t100.month,
	t100.carrier,
	t100.origin,
	t100.destination, 
	t100.departures_performed,
	t100.num_aircraft_types,
	t100.seats_mean, t100.seats_squared_mean,
	sqrt(t100.seats_variance) as seats_std_dev,
	case 	when t100.seats_mean = 0 then 0
		else sqrt(t100.seats_variance) / t100.seats_mean
	end
from
temp_2 t100;

update t100_seats
set carrier = 'US'
where carrier = 'HP';


create index idx_t100_seats_cymod
  on t100_seats(carrier, year, month, origin, destination);

create index idx_t100_seats_cym
  on t100_seats(carrier, year, month);


-- select carrier, origin, destination, year, quarter, month
-- from t100_seats
-- group by carrier, origin, destination, year, quarter, month
-- having count(*) > 2;

drop table if exists temp_1;
drop table if exists temp_2;
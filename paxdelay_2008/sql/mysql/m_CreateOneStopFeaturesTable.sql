drop table if exists one_stop_features;

create table one_stop_features
(
  month numeric(2) not null,
  first_carrier char(6) not null,
  second_carrier char(6) not null,
  origin char(3) not null,
  destination char(3) not null,
  num_itineraries numeric(7) not null
);

-- Status tracking table
drop table if exists one_stop_features_status;
create table one_stop_features_status (
	iteration_number int(11) auto_increment, primary key (iteration_number),
	start_time datetime
);

--- PROCEDURE
--- It is extremely reccomended to use command line to run this query!
drop procedure if exists populate_one_stop_features;

SHOW PROCEDURE STATUS;

delimiter $$ 

create procedure populate_one_stop_features()
begin
    declare done int default 0;
    declare seg_year int;
    declare seg_quarter int;
    declare seg_month int;
    declare month_cursor cursor for select distinct t100.year, t100.quarter, t100.month
                                    from t100_segments t100
                                    order by t100.year, t100.quarter, t100.month;
    declare continue handler for not found set done = 1;

    open month_cursor;

cursor_loop:LOOP
    fetch month_cursor into seg_year, seg_quarter, seg_month;
    if done then leave cursor_loop; end if;

-- Log iteration start time
insert into one_stop_features_status
values (null, now());     
    
drop table if exists temp_one_stop_features_1;
drop table if exists temp_one_stop_features_2;

create table temp_one_stop_features_1
select *
from t100_segments
where year = seg_year 
    and quarter = seg_quarter 
    and month = seg_month 
    and seats > 0;

create table temp_one_stop_features_2
select *
from t100_segments
where year = seg_year 
    and quarter = seg_quarter 
    and month = seg_month 
    and seats > 0;

insert into one_stop_features
    (month, 
    first_carrier, 
    second_carrier, 
    origin, 
    destination, 
    num_itineraries)
select 
    seg_month as month, 
    it.first_operating_carrier as first_carrier,
    it.second_operating_carrier as second_carrier,
    it.origin as origin, 
    it.destination as destination,
    count(distinct it.planned_departure_time_UTC) as num_itineraries
from itineraries it
where it.year = seg_year 
    and it.quarter = seg_quarter 
    and it.month = seg_month 
    and it.num_flights = 2
    and exists
    (
        select *
        from temp_one_stop_features_1
        where carrier = it.first_operating_carrier 
            and origin = it.origin
            and destination = it.connection
    )
    and exists
    (
        select *
        from temp_one_stop_features_2
        where carrier = it.second_operating_carrier 
            and origin = it.connection
            and destination = it.destination
    )
group by it.first_operating_carrier, it.second_operating_carrier, it.origin, it.destination;

drop table temp_one_stop_features_1;
drop table temp_one_stop_features_2;

end LOOP;
    
close month_cursor;
end$$

delimiter ;

call populate_one_stop_features();

drop procedure if exists populate_one_stop_features;

---
--- !PROCEDURE

create index idx_one_stop_features_m
  on one_stop_features(month);
drop table if exists db1b_unique_carrier_segments;

create table db1b_unique_carrier_segments
(
  id integer not null auto_increment, primary key (id)
) 
select	quarter as quarter, 
				ticketing_carrier as ticketing_carrier, 
				operating_carrier as operating_carrier,
				origin as origin, 
				destination as destination
from db1b_coupons
group by quarter, ticketing_carrier, operating_carrier, origin, destination;

create unique index idx_db1b_us_qcod 
	on db1b_unique_carrier_segments(quarter, ticketing_carrier, operating_carrier, origin, destination)
	using btree;
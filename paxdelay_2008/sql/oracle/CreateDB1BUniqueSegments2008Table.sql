create sequence db1b_unique_segments_08_id_seq
  start with 1
  increment by 1
  nomaxvalue;

create table db1b_unique_segments_2008
(
  id primary key not null,
  quarter not null,
  ticketing_carrier not null,
  operating_carrier not null,
  origin not null,
  destination not null
) as
select 
  db1b_unique_segments_08_id_seq.nextval, tin.quarter, 
  tin.ticketing_carrier, tin.operating_carrier, tin.origin, tin.destination
from
(select db1b.quarter, 
   db1b.ticketing_carrier, db1b.operating_carrier,
   db1b.origin, db1b.destination
 from db1b_coupons_2008 db1b
 group by db1b.quarter,
   db1b.ticketing_carrier, db1b.operating_carrier, 
   db1b.origin, db1b.destination) tin;

create unique index idx_db1b_us_08_qcod
  on db1b_unique_segments_2008(quarter, ticketing_carrier, 
    operating_carrier, origin, destination)
  tablespace users;

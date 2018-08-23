declare seq_exists PLS_INTEGER;
declare table_exists PLS_INTEGER;
begin

select count(*) into seq_exists 
  from user_sequences
  where sequence_name = 'DB1B_UNIQUE_SEGMENTS_ID_SEQ';
if seq_exists = 1 then
  execute immediate 'drop sequence db1b_unique_segments_id_seq';
end if;

select count(*) into table_exists 
  from user_tables
  where table_name = 'DB1B_UNIQUE_CARRIER_SEGMENTS';
if table_exists = 1 then
  execute immediate 'drop table db1b_unique_carrier_segments';
end if;

end;

create sequence db1b_unique_segments_id_seq
  start with 1
  increment by 1
  nomaxvalue;

create table db1b_unique_carrier_segments
(
  id primary key not null,
  quarter not null,
  ticketing_carrier not null,
  operating_carrier not null,
  origin not null,
  destination not null
) as
select 
  db1b_unique_segments_id_seq.nextval, tin.quarter, 
  tin.ticketing_carrier, tin.operating_carrier, tin.origin, tin.destination
from
(select db1b.quarter, 
   db1b.ticketing_carrier, db1b.operating_carrier,
   db1b.origin, db1b.destination
 from db1b_coupons db1b
 group by db1b.quarter,
   db1b.ticketing_carrier, db1b.operating_carrier, 
   db1b.origin, db1b.destination) tin;

create unique index idx_db1b_ucs
  on db1b_unique_carrier_segments(quarter, ticketing_carrier, operating_carrier,
      origin, destination);

declare table_exists PLS_INTEGER;
begin
select count(*) into table_exists 
  from user_tables
  where table_name = 'CODESHARES';
if table_exists = 1 then
  execute immediate 'drop table codeshares';
end if;
end;

create table codeshares
(
  offering_id number(10, 0) not null,
  carrier varchar2(3) not null,
  flight_number varchar2(6),
  constraint fk_offering_id
    foreign key (offering_id)
    references offerings(id)
);
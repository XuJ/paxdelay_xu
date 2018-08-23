declare seq_exists PLS_INTEGER;
begin
  select count(*) into seq_exists 
    from user_sequences
    where sequence_name = 'OFFERING_ID_SEQ';
  if seq_exists = 1 then
    execute immediate 'drop sequence offering_id_seq';
  end if;
end;

declare table_exists PLS_INTEGER;
begin
  select count(*) into table_exists 
    from user_tables
    where table_name = 'OFFERINGS';
  if table_exists = 1 then
    execute immediate 'drop table offerings';
  end if;
end;

create sequence offering_id_seq
  start with 1
  increment by 1
  nomaxvalue;

create table offerings
(
  id number(10, 0) not null primary key,
  innovata_id number(10, 0) not null,
  carrier varchar2(3) not null,
  flight_number varchar2(6) not null,
  effective_start timestamp with time zone not null,
  effective_end timestamp with time zone not null,
  origin char(3) not null,
  destination char(3) not null,
  published_departure_hour number(2, 0) not null,
  published_departure_minutes number(2, 0) not  null,
  published_arrival_hour number(2, 0) not null,
  published_arrival_minutes number(2, 0) not null,
  sunday_flag number(1, 0) not null,
  monday_flag number(1, 0) not null,
  tuesday_flag number(1, 0) not null,
  wednesday_flag number(1, 0) not null,
  thursday_flag number(1, 0) not null,
  friday_flag number(1, 0) not null,
  saturday_flag number(1, 0) not null,
  iata_aircraft_code char(3) not null,
  number_stops number(1, 0) not null,
  stop_code_list varchar2(50),
  aircraft_code_list varchar2(50) not null,
  constraint fk_innovata_id
    foreign key (innovata_id)
    references innovata(record_id)
);

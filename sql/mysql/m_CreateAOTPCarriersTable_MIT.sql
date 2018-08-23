-- XuJiao052017
-- create asqp_carriers_MIT table

drop table if exists asqp_carriers_MIT;

create table asqp_carriers_MIT 
(
	code char(2) not null
);


LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/bts_raw_csv/asqp_carriers_MIT.csv'
INTO TABLE asqp_carriers_MIT 
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
	code
);


-- XuJiao052017
-- create related_carriers_MIT table

drop table if exists related_carriers_MIT;

create table related_carriers_MIT 
(
	primary_carrier char(2) not null,
	secondary_carrier char(2) not null
	
);


LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/bts_raw_csv/related_carriers_MIT.csv'
INTO TABLE related_carriers_MIT 
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
	primary_carrier,
	secondary_carrier
);


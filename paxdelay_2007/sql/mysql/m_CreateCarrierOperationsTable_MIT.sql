-- XuJiao052017
-- create carrier_operations_MIT table

drop table if exists carrier_operations_MIT;

create table carrier_operations_MIT 
(
	carrier char(2) not null,
	airport char(3) not null,
	number_operations numeric(8,0) not null,
	percent_operations numeric(5,4) not null
);


LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/bts_raw_csv/carrier_operations_MIT.csv'
INTO TABLE carrier_operations_MIT 
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
	carrier,
	airport,
	number_operations,
	percent_operations
);


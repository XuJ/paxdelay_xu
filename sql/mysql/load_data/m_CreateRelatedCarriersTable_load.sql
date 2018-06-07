-- 052217 XuJ: use related_carriers.R in /mdsg/ to create related_carriers_20xx.csv in /mdsg/bts_raw_csv/
LOAD DATA LOCAL INFILE '/mdsg/bts_raw_csv/related_carriers_2016.csv'
INTO TABLE related_carriers
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  primary_carrier,
  secondary_carrier
);
-- 14

-- 042717 XuJ: Change the character set which will be used in PassengerDelayCalculator.java
ALTER TABLE airline_inventories CONVERT TO CHARACTER SET latin1 COLLATE 'latin1_swedish_ci';

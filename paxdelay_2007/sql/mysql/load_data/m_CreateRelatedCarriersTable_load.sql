-- Add RelatedCarriers.csv in the root directory 021417
LOAD DATA LOCAL INFILE '/mdsg/paxdelay_general_Xu/RelatedCarriers.csv'
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

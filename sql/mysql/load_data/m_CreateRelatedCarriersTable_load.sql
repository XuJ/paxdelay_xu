LOAD DATA INFILE '/export/mysql/import/RelatedCarriers.csv'
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
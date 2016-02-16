LOAD DATA INFILE '/export/mysql/import/DelayBuckets.csv'
INTO TABLE delay_buckets
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  bucket_minimum,
  bucket_maximum
);
options
(
  skip = 1,
  direct = true
)
load data
infile 'DelayBuckets.csv'
replace
into table paxdelay.delay_buckets
fields terminated by ',' optionally enclosed by '"'
(
  bucket_minimum integer external,
  bucket_maximum integer external
)

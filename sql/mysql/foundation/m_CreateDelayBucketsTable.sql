drop table if exists delay_buckets;

create table delay_buckets
(
  bucket_minimum numeric(4) not null,
  bucket_maximum numeric(4) not null
);


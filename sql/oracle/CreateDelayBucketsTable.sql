drop table delay_buckets;

create table delay_buckets
(
  bucket_minimum number(4, 0) not null,
  bucket_maximum number(4, 0) not null
);

commit;


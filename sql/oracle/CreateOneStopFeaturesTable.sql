drop table one_stop_features;

create table one_stop_features
(
  month number(2, 0) not null,
  first_carrier char(2) not null,
  second_carrier char(2) not null,
  origin char(3) not null,
  destination char(3) not null,
  num_itineraries number(7, 0) not null
);

commit;

create index idx_one_stop_features_m
  on one_stop_features(month)
  tablespace users;

commit;


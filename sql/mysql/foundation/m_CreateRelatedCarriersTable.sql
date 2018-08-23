drop table if exists related_carriers;

create table related_carriers
(
  primary_carrier char(2) not null,
  secondary_carrier char(2) not null
);

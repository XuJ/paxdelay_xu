drop table if exists db1b_markets;

create table db1b_markets
(
  itinerary_id numeric(16) not null,
  market_id numeric(16) not null,
  number_coupons numeric(6) not null,
  year numeric(4) not null,
  quarter int not null,
  origin char(3) not null,
  destination char(3) not null,
  reporting_carrier char(6) not null,
  ticketing_carrier char(6) not null,
  operating_carrier char(6) not null,
  bulk_fare_flag numeric(1) not null,
  passengers numeric(4) not null,
  fare numeric(7, 2) not null,
  miles_flown numeric(5) not null,
  nonstop_miles numeric(5, 0) not null,
)
partition by list (quarter)
(	partition p_q1 values in (1),
	partition p_q2 values in (2),
	partition p_q3 values in (3),
	partition p_q4 values in (4)
);
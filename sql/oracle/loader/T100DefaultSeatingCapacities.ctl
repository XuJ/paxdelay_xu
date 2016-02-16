options
(
  skip = 1,
  direct = TRUE
)
load data
infile 'T100DefaultSeatingCapacities.csv' "str '\r\n'"
replace
into table paxdelay.t100_seats
fields terminated by ',' optionally enclosed by '"'
(
  carrier char,
  origin char,
  destination char,
  year integer external,
  quarter integer external,
  month integer external,
  num_aircraft_types integer external,
  seats_mean float external,
  seats_std_dev float external,
  seats_coeff_var float external
)

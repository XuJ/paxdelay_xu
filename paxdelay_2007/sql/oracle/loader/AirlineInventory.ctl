options
(
  skip = 1,
  direct = true
)
load data
infile 'airline_inventory.csv' "str '\r\n'"
replace
into table paxdelay.airline_inventories
fields terminated by ',' optionally enclosed by '"'
(
  carrier char,
  first_year integer external,
  serial_number char,
  tail_number char,
  aircraft_status char,
  operating_status char,
  number_of_seats integer external
    "decode(:number_of_seats, 0, null, :number_of_seats)",
  manufacturer char,
  model char,
  capacity_in_pounds integer external
    "decode(:capacity_in_pounds, 0, null, :capacity_in_pounds)",
  acquisition_date char
    "trim(:acquisition_date)"
)

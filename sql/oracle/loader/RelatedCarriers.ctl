options
(
  skip = 1,
  direct = true
)
load data
infile 'RelatedCarriers.csv' "str '\r\n'"
replace
into table paxdelay.related_carriers
fields terminated by ',' optionally enclosed by '"'
(
  primary_carrier char,
  secondary_carrier char
)

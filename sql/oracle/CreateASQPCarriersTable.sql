create table asqp_carriers
(
  code not null
)
as
select distinct carrier
from flights;

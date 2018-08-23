drop table if exists asqp_carriers;

create table asqp_carriers
select distinct carrier as code
from flights;
-- 20

alter table asqp_carriers convert to character set latin1 collate latin1_general_cs;
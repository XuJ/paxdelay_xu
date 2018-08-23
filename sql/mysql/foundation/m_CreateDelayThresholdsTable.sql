drop table if exists delay_thresholds;
create table delay_thresholds (
  minutes_of_delay int(4) not null
)
engine = MyISAM;
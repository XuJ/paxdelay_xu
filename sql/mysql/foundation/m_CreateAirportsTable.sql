use paxdelay;
drop table if exists airports;

create table airports
(
	code		char(3) not null,
	city		varchar(50) not null,
	state		char(2) not null,
	timezone_region	varchar(20) not null
)
ENGINE = MyISAM;

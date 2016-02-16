drop table if exists scenario_flight_schedule;

drop table if exists scenario_parameter_sets;


/*==============================================================*/
/* Table: Scenario_Flight_Schedule                              */
/*==============================================================*/
create table scenario_flight_schedule
(
   scenario_id          char(36) not null,
   scenario_name        char(100),
   stage_name           char(100),
   airline              char(6),
   flight               char(20),
   flight_step          int,
   last_flight_step     int,
   resource             char(100),
   is_constrained       bool,
   scheduled_arrival_time datetime,
   scheduled_arrival_interval int,
   scheduled_order      int,
   controlled_arrival_time datetime,
   controlled_arrival_interval int,
   controlled_order     int,
   latest_arrival_time  datetime,
   latest_arrival_interval int,
   arrival_duration     int,
   maximum_rbs_arrival_time datetime,
   maximum_rbs_interval int,
   configuration_name   char(36)
);


/*==============================================================*/
/* Table: Scenario_Parameter_Sets                               */
/*==============================================================*/
create table scenario_parameter_sets
(
   configuration_name   char(36) not null,
   parameter_name       char(255),
   parameter_value      char(255)
);
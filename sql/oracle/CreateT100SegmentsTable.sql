CREATE TABLE T100_SEGMENTS
(
  "DEPARTURES_SCHEDULED" NUMBER(4, 0) NOT NULL,
  "DEPARTURES_PERFORMED" NUMBER(4, 0) NOT NULL,
  "PAYLOAD" NUMBER(10, 0) NOT NULL,
  "SEATS" NUMBER(6, 0) NOT NULL,
  "PASSENGERS" NUMBER(6, 0) NOT NULL,
  "FREIGHT" NUMBER(8, 0) NOT NULL,
  "MAIL" NUMBER(8, 0) NOT NULL,
  "DISTANCE" NUMBER(4, 0) NOT NULL,
  "RAMP_TO_RAMP" NUMBER(6, 0) NOT NULL,
  "AIR_TIME" NUMBER(6, 0) NOT NULL,
  "UNIQUE_CARRIER" VARCHAR2(6) NOT NULL,
  "AIRLINE_ID" NUMBER(6, 0) NOT NULL,
  "UNIQUE_CARRIER_NAME" VARCHAR2(100) NOT NULL,
  "UNIQUE_CARRIER_ENTITY" VARCHAR(6) NOT NULL,
  "REGION" CHAR(1) NOT NULL,
  "CARRIER" VARCHAR2(3) NOT NULL,
  "CARRIER_NAME" VARCHAR2(100) NOT NULL,
  "CARRIER_GROUP" NUMBER(2, 0) NOT NULL,
  "CARRIER_GROUP_NEW" NUMBER(2, 0) NOT NULL,
  "ORIGIN" CHAR(3) NOT NULL,
  "ORIGIN_CITY_NAME" VARCHAR2(50) NOT NULL,
  "ORIGIN_CITY_CODE" NUMBER(6, 0) NOT NULL,
  "ORIGIN_STATE" CHAR(2) NOT NULL,
  "ORIGIN_STATE_FIPS" NUMBER(2, 0) NOT NULL,
  "ORIGIN_STATE_NAME" VARCHAR2(50) NOT NULL,
  "ORIGIN_WAC" NUMBER(4, 0) NOT NULL,
  "DESTINATION" CHAR(3) NOT NULL,
  "DESTINATION_CITY_NAME" VARCHAR2(50) NOT NULL,
  "DESTINATION_CITY_CODE" NUMBER(6, 0) NOT NULL, 
  "DESTINATION_STATE" CHAR(2) NOT NULL,
  "DESTINATION_STATE_FIPS" NUMBER(2, 0) NOT NULL,
  "DESTINATION_STATE_NAME" VARCHAR2(50) NOT NULL,
  "DESTINATION_WAC" NUMBER(4, 0) NOT NULL,
  "AIRCRAFT_GROUP" NUMBER(2, 0) NOT NULL,
  "AIRCRAFT_TYPE" NUMBER(4, 0) NOT NULL,
  "AIRCRAFT_CONFIG" NUMBER(1, 0) NOT NULL,
  "YEAR" NUMBER(4, 0) NOT NULL,
  "QUARTER" NUMBER(1, 0) NOT NULL,
  "MONTH" NUMBER(2, 0) NOT NULL,
  "DISTANCE_GROUP" NUMBER(2, 0) NOT NULL,
  "SERVICE_CLASS" CHAR(1) NOT NULL
);

create bitmap index bm_idx_t100_segments_cym
  on t100_segments(carrier, year, month)
  tablespace users;

create bitmap index bm_idx_t100_segments_cymod
  on t100_segments(carrier, year, month, origin, destination)
  tablespace users;

update t100_segments
set carrier = 'US'
where carrier = 'HP';

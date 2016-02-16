LOAD DATA INFILE '/export/mysql/import/ContinentalFlightLegs.csv'
INTO TABLE continental_flight_legs
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(itinerary_id,
num_flights,
itinerary_sequence,
carrier,
flight_number,
departure_time,
origin,
destination);


create unique index idx_co_legs_iidis
  on continental_flight_legs(itinerary_id, itinerary_sequence);

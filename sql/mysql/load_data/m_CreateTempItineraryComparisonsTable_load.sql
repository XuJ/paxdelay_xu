LOAD DATA INFILE '/export/mysql/import/ItineraryValidation.csv'
INTO TABLE temp_itinerary_comparisons
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(
  num_flights,
  first_flight_id,
  second_flight_id,
  allocated_passengers,
  airline_passengers
);

-- Issue #5
-- flights id: 1 7455458
-- temp_itinerary_comparisons id: 2987607 10443064
-- difference is 2987606
update temp_itinerary_comparisons set first_flight_id = first_flight_id - 2987606;
update temp_itinerary_comparisons set second_flight_id = second_flight_id - 2987606;
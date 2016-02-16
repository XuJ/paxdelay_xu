package edu.mit.nsfnats.paxdelay.allocation;

import java.util.Properties;
import java.util.Random;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;

public interface ItineraryUtilityCalculator {
	public static final String PROPERTY_ALLOCATION_PARAMETERS_DIRECTORY = "ALLOCATION_PARAMETERS_DIRECTORY";
	public static final String PROPERTY_ALLOCATION_PARAMETERS_FILENAME = "ALLOCATION_PARAMETERS_FILENAME";

	public void initialize(Properties properties) throws InvalidFormatException;
	
	// Assumes initialize has already been called
	public void resetParameters(Random randomEngine);
	
	public double calculateUtility(AllocationItinerary itinerary);
}

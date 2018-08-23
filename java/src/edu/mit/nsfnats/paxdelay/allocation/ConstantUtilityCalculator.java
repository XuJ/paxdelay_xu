package edu.mit.nsfnats.paxdelay.allocation;

import java.util.Properties;
import java.util.Random;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;

public class ConstantUtilityCalculator implements ItineraryUtilityCalculator {

	@Override
	public double calculateUtility(AllocationItinerary itinerary) {
		// Assume constant utility for all itineraries, leading to
		// a completely random allocation
		return 0;
	}

	@Override
	public void initialize(Properties properties) throws InvalidFormatException {
		// Do nothing in this case
	}

	@Override
	public void resetParameters(Random randomEngine) {
		// Do nothing in this case
	}
}

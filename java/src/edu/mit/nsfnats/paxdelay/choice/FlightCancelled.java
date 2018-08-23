package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public class FlightCancelled extends AlternativeFeature {

	public void initialize(Properties properties) {
		// Do nothing
	}

	// The following two methods should only be called after
	// all itineraries have been processed
	public String[] getColumnHeaders() {
		return new String[] { "Flight_Cancelled" };
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		return new FeatureValue(this, new String[] { itinerary
				.hasCancelledFlight() ? "1" : "0" });
	}
}

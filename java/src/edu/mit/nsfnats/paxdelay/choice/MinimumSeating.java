package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public class MinimumSeating extends AlternativeFeature {

	public void initialize(Properties properties) {
		// Do nothing...
	}

	// The following two methods should only be called after
	// all itineraries have been processed
	public String[] getColumnHeaders() {
		return new String[]{"Minimum_Seating"};
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		FlightInformation[] flightInfos = itinerary.getFlightInfos();
		double seatingCapacity = flightInfos[0].getSeatingCapacity();
		for (int i = 1; i < flightInfos.length; ++i) {
			seatingCapacity = Math.min(seatingCapacity, 
					flightInfos[i].getSeatingCapacity());
		}
		return new FeatureValue(this,
				new String[]{String.format("%1$4.2f", seatingCapacity)});
	}
}

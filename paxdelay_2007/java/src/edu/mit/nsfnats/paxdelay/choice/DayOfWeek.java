package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public class DayOfWeek extends AlternativeFeature {
	String[] m_columnHeaders;

	public DayOfWeek() {
		m_columnHeaders = new String[] { "Day_of_Week" };
	}

	public void initialize(Properties properties) {
		// Do nothing...
	}

	// The following two methods should only be called after
	// all itineraries have been processed
	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		return new FeatureValue(this, new String[] { Integer.toString(itinerary
				.getDayOfWeek()) });
	}
}

package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public class TimeOfDay extends AlternativeFeature {

	String[] m_columnHeaders;

	public TimeOfDay() {
		m_columnHeaders = new String[] { "Hour_of_Day" };
	}

	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		int departureHour = itinerary.getLocalDepartureHour();
		int departureMinutes = itinerary.getLocalDepartureMinutes();
		double departureHourFraction = departureHour + departureMinutes / 60.0;
		return new FeatureValue(this, new String[] { String.format("%1$04.2f",
				departureHourFraction) });
	}

	public void initialize(Properties properties) {
		// Do nothing...
	}
}

package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public class NumberFlights extends ChoiceSetFeature {

	String[] m_columnHeaders;

	public NumberFlights() {
		m_columnHeaders = new String[] { "Number_Flights" };
	}

	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		return new FeatureValue(this, new String[] { Integer.toString(itinerary
				.getNumFlights()) });
	}

	public void initialize(Properties properties) {
		// Do nothing...
	}
}

package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public class ConnectionTime extends AlternativeFeature {
	String[] m_columnHeaders;

	public ConnectionTime() {
		m_columnHeaders = new String[] { "Layover_Minutes" };
	}

	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		if (itinerary.getNumFlights() == 1) {
			return new FeatureValue(this,
					new String[] { ObservationSet.DEFAULT_MISSING_VALUE });
		}

		int connectionTime = itinerary.getConnectionTime();
		return new FeatureValue(this, new String[] { Integer
				.toString(connectionTime) });
	}

	public void initialize(Properties properties) {
		// Do nothing...
	}
}

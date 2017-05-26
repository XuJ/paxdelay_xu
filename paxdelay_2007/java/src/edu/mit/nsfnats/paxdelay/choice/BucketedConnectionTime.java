package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

import edu.mit.nsfnats.paxdelay.util.PropertiesReader;

public class BucketedConnectionTime extends AlternativeFeature {

	public static final String PROPERTY_IDEAL_CONNECTION_TIME = 
		BucketedConnectionTime.class.getName() + ".IDEAL_CONNECTION_TIME";

	public static final int DEFAULT_IDEAL_CONNECTION_TIME = 60;

	int m_idealConnectionTime;
	String[] m_columnHeaders;

	public BucketedConnectionTime() {
		m_columnHeaders = new String[] { "Layover_Under", "Layover_Over" };
	}

	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		if (itinerary.getNumFlights() == 1) {
			return new FeatureValue(this, new String[] { "0", "0" });
		}

		int connectionTime = itinerary.getConnectionTime();
		int connectionUnder = Math.max(0, m_idealConnectionTime
				- connectionTime);
		int connectionOver = Math
				.max(0, connectionTime - m_idealConnectionTime);

		return new FeatureValue(this, new String[] {
				Integer.toString(connectionUnder),
				Integer.toString(connectionOver) });
	}

	public void initialize(Properties properties) {
		m_idealConnectionTime = PropertiesReader.readInt(properties,
				PROPERTY_IDEAL_CONNECTION_TIME, DEFAULT_IDEAL_CONNECTION_TIME);
	}
}

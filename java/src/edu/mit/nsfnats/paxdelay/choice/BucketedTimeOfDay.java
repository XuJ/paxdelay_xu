package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public class BucketedTimeOfDay extends AlternativeFeature {

	int[] m_hourThresholds;
	String[] m_columnHeaders;

	public BucketedTimeOfDay() {
		m_hourThresholds = new int[] { 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21,
				23 };
		int numThresholds = m_hourThresholds.length;
		m_columnHeaders = new String[numThresholds];
		for (int i = 0; i < numThresholds; ++i) {
			m_columnHeaders[i] = formatColumnHeader(m_hourThresholds[i]);
		}
	}

	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}

	protected static String formatColumnHeader(int startHour) {
		return "Hour_" + String.format("%1$02d", startHour);
	}

	public FeatureValue getValue(AirlineItinerary itinerary) {
		int numThresholds = m_hourThresholds.length;
		int departureHour = itinerary.getLocalDepartureHour();
		int departureMinutes = itinerary.getLocalDepartureMinutes();
		double departureHourFraction = departureHour + departureMinutes / 60.0;
		int index = 0;
		for (; index < numThresholds; ++index) {
			if (departureHourFraction <= m_hourThresholds[index]) {
				break;
			}
		}
		String[] values = new String[numThresholds];
		for (int i = 0; i < numThresholds; ++i) {
			values[i] = "0.0";
		}

		int prevIndex, nextIndex;
		if (index == 0 || index == numThresholds) {
			prevIndex = numThresholds - 1;
			nextIndex = 0;
		} else {
			prevIndex = index - 1;
			nextIndex = index;
		}

		int prevHour = m_hourThresholds[prevIndex];
		int nextHour = m_hourThresholds[nextIndex];

		double minutesToPrev = ((departureHourFraction - prevHour + 24) % 24) * 60.0;
		double minutesToNext = ((nextHour - departureHourFraction + 24) % 24) * 60.0;
		double minutesInWindow = ((nextHour - prevHour + 24) % 24) * 60.0;
		values[prevIndex] = String.format("%1$03.2f", minutesToNext
				/ minutesInWindow);
		values[nextIndex] = String.format("%1$03.2f", minutesToPrev
				/ minutesInWindow);

		return new FeatureValue(this, values);
	}

	public void initialize(Properties properties) {
		// TODO: Define the hour thresholds based on the properties files
	}
}

package edu.mit.nsfnats.paxdelay;

public class Itinerary {

	int m_numFlights;
	String[] m_flightCarriers;
	long[] m_flightIDs;
	double m_numPassengers;

	public Itinerary(String[] flightCarriers, long[] flightIDs) {
		m_numFlights = flightIDs.length;
		m_flightCarriers = flightCarriers;
		m_flightIDs = flightIDs;
		// Use a negative value to indicate unknown
		m_numPassengers = -1.0;
	}

	public int getNumFlights() {
		return m_numFlights;
	}

	public String[] getFlightCarriers() {
		return m_flightCarriers;
	}

	public long[] getFlightIDs() {
		return m_flightIDs;
	}

	public double getNumPassengers() {
		return m_numPassengers;
	}

	public void setNumPassengers(double passengers) {
		m_numPassengers = passengers;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Itinerary)) {
			return false;
		}
		Itinerary that = (Itinerary) obj;
		if (m_numFlights != that.getNumFlights()) {
			return false;
		}
		String[] thatFlightCarriers = that.getFlightCarriers();
		for (int i = 0; i < m_numFlights; ++i) {
			if (!m_flightCarriers[i].equals(thatFlightCarriers[i])) {
				return false;
			}
		}
		long[] thatFlightIDs = that.getFlightIDs();
		for (int i = 0; i < m_numFlights; ++i) {
			if (m_flightIDs[i] != thatFlightIDs[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		StringBuffer itineraryKey = new StringBuffer().append(m_numFlights)
				.append(":");
		for (int i = 0; i < m_numFlights; ++i) {
			if (i > 0) {
				itineraryKey.append(";");
			}
			itineraryKey.append(m_flightCarriers[i]).append(".");
			itineraryKey.append(m_flightIDs[i]);
		}
		return itineraryKey.toString().hashCode();
	}
}

package edu.mit.nsfnats.paxdelay.choice;

public class CarrierFlightPath {

	String m_carrier;
	String m_origin;
	String m_destination;

	public CarrierFlightPath(String carrier, String origin, String destination) {
		m_carrier = carrier;
		m_origin = origin;
		m_destination = destination;
	}

	public String getCarrier() {
		return m_carrier;
	}

	public String getOrigin() {
		return m_origin;
	}

	public String getDestination() {
		return m_destination;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof CarrierFlightPath)) {
			return false;
		}
		CarrierFlightPath thatFlightPath = (CarrierFlightPath) that;
		if (m_carrier.equals(thatFlightPath.m_carrier)
				&& m_origin.equals(thatFlightPath.m_origin)
				&& m_destination.equals(thatFlightPath.m_destination)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		String hashKey = getHashKey();
		return hashKey.hashCode();
	}

	protected String getHashKey() {
		StringBuffer flightPathKey = new StringBuffer();
		flightPathKey.append(m_carrier).append("_");
		flightPathKey.append(m_origin).append("_");
		flightPathKey.append(m_destination);
		return flightPathKey.toString();
	}
}

package edu.mit.nsfnats.paxdelay.choice;

public class AirlineItinerary {
	int m_numFlights;
	CarrierFlightPath[] m_flightPaths;
	FlightInformation[] m_flightInfos;

	int m_year;
	// SQL week of year, January 1 - January 7 = 1, ...
	int m_dayOfYear;
	// SQL day of week, Sunday = 1, Monday = 2, ...
	int m_dayOfWeek;

	int m_localDepartureHour;
	int m_localDepartureMinutes;

	// Connection time in minutes (0 for a non-stop flight)
	int m_connectionTime;

	int m_numPassengers;

	public AirlineItinerary(CarrierFlightPath[] flightPaths, 
			FlightInformation[] flightInfos, int year,
			int dayOfYear, int dayOfWeek, int localDepartureHour,
			int localDepartureMinutes, int connectionTime,
			int numPassengers) {
		m_numFlights = flightPaths.length;
		m_flightPaths = flightPaths;
		m_flightInfos = flightInfos;
		m_localDepartureHour = localDepartureHour;
		m_localDepartureMinutes = localDepartureMinutes;
		m_dayOfYear = dayOfYear;
		m_dayOfWeek = dayOfWeek;
		m_connectionTime = connectionTime;
		m_numPassengers = numPassengers;
	}

	public int getNumFlights() {
		return m_numFlights;
	}

	public CarrierFlightPath[] getFlightPaths() {
		return m_flightPaths;
	}
	
	public FlightInformation[] getFlightInfos() {
		return m_flightInfos;
	}

	public int getYear() {
		return m_year;
	}

	public int getDayOfYear() {
		return m_dayOfYear;
	}

	public int getDayOfWeek() {
		return m_dayOfWeek;
	}

	public int getLocalDepartureHour() {
		return m_localDepartureHour;
	}

	public int getLocalDepartureMinutes() {
		return m_localDepartureMinutes;
	}

	public int getConnectionTime() {
		return m_connectionTime;
	}

	public boolean hasCancelledFlight() {
		for (int i = 0; i < m_flightInfos.length; ++i) {
			if (m_flightInfos[i].isCancelled()) {
				return true;
			}
		}
		return false;
	}

	public int getNumPassengers() {
		return m_numPassengers;
	}
	
	public String getFlightSequence() {
		StringBuffer flightSequence = new StringBuffer();
		for (int i = 0; i < m_flightInfos.length; ++i) {
			if (i != 0) {
				flightSequence.append(";");
			}
			flightSequence.append(m_flightInfos[i].m_flightID);
		}
		return flightSequence.toString();
	}
}

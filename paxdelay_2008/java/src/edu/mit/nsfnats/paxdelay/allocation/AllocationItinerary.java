package edu.mit.nsfnats.paxdelay.allocation;

public class AllocationItinerary {
	int m_itineraryID;
	InternalRoute m_route;
	InternalFlight m_firstFlight;
	InternalFlight m_secondFlight;
	
	int m_departureDayOfWeek;
	int m_departureHour;
	int m_departureMinutes;
	int m_connectionTime;
	boolean m_cancelledFlag;
	double m_minimumSeats;
	int m_originTimeZoneOffset;
	int m_destinationTimeZoneOffset;

	double m_estimatedWeight;
	double m_numberPassengersAllocated;

	public AllocationItinerary(int itineraryID, InternalRoute route,
			InternalFlight firstFlight, InternalFlight secondFlight) {
		m_itineraryID = itineraryID;
		m_route = route;
		m_firstFlight = firstFlight;
		m_secondFlight = secondFlight;
	}

	public InternalFlight getFirstFlight() {
		return m_firstFlight;
	}

	public InternalFlight getSecondFlight() {
		return m_secondFlight;
	}

	public void setDepartureDayOfWeek(int dayOfWeek) {
		m_departureDayOfWeek = dayOfWeek;
	}

	public void setDepartureHour(int hourOfDay) {
		m_departureHour = hourOfDay;
	}

	public void setDepartureMinutes(int minutesOfHour) {
		m_departureMinutes = minutesOfHour;
	}

	public void setConnectionTime(int connectionTime) {
		m_connectionTime = connectionTime;
	}

	public void setCancelledFlag(boolean cancelledFlag) {
		m_cancelledFlag = cancelledFlag;
	}

	public void setMinimumSeats(double minimumSeats) {
		m_minimumSeats = minimumSeats;
	}

	public void setOriginTimeZoneOffset(int originTimeZoneOffset) {
		m_originTimeZoneOffset = originTimeZoneOffset;
	}

	public void setDestinationTimeZoneOffset(int destinationTimeZoneOffset) {
		m_destinationTimeZoneOffset = destinationTimeZoneOffset;
	}
	
	public void setEstimatedWeight(double weight) {
		m_estimatedWeight = weight;
	}

	public double getEstimatedWeight() {
		return m_estimatedWeight;
	}

	public void allocatePassengers(double numberPassengers) {
		m_numberPassengersAllocated += numberPassengers;

		m_firstFlight.allocatePassengers(numberPassengers);
		if (m_secondFlight != null) {
			m_secondFlight.allocatePassengers(numberPassengers);
		}
		m_route.allocatePassengers(numberPassengers);
	}

	public double getNumberPassengersAllocated() {
		return m_numberPassengersAllocated;
	}

	public boolean containsFullSegment() {
		return m_route.containsFullSegment();
	}

	public boolean containsFullFlight() {
		return m_firstFlight.isFull()
				|| (m_secondFlight != null && m_secondFlight.isFull());
	}

	public double getEstimatedProbability() {
		return m_estimatedWeight / m_route.getTotalEstimatedWeight();
	}
}
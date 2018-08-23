package edu.mit.nsfnats.paxdelay.choice;

public class FlightInformation {
	int m_flightID;
	boolean m_cancelled;
	double m_seatingCapacity;
	int m_duration;

	public FlightInformation(int flightID) {
		m_flightID = flightID;
	}

	public boolean isCancelled() {
		return m_cancelled;
	}

	public void setCancelled(boolean cancelled) {
		m_cancelled = cancelled;
	}

	public double getSeatingCapacity() {
		return m_seatingCapacity;
	}

	public void setSeatingCapacity(double seatingCapacity) {
		m_seatingCapacity = seatingCapacity;
	}

	public int getDuration() {
		return m_duration;
	}

	public void setDuration(int duration) {
		m_duration = duration;
	}
}

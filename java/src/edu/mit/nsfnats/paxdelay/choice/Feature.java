package edu.mit.nsfnats.paxdelay.choice;

import java.util.Properties;

public interface Feature {
	public void initialize(Properties properties);

	// The following two methods should only be called after
	// all itineraries have been processed
	public String[] getColumnHeaders();

}

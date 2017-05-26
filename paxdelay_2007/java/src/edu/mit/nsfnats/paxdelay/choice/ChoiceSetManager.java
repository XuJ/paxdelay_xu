package edu.mit.nsfnats.paxdelay.choice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class ChoiceSetManager {

	int m_nextChoiceSetID;
	Map<ChoiceSetKey, ChoiceSet> m_choiceSetMap;

	public ChoiceSetManager() {
		m_nextChoiceSetID = 0;
		m_choiceSetMap = new HashMap<ChoiceSetKey, ChoiceSet>();
	}

	public void initialize(Properties properties) {
		// Do nothing for now...
	}

	public ChoiceSet getChoiceSet(AirlineItinerary itinerary, boolean createChoiceSet) {
		ChoiceSetKey key = new ChoiceSetKey(itinerary);
		ChoiceSet choiceSet = m_choiceSetMap.get(key);
		if (choiceSet == null && createChoiceSet) {
			choiceSet = new ChoiceSet(m_nextChoiceSetID);
			m_nextChoiceSetID += 1;
			m_choiceSetMap.put(key, choiceSet);
		}
		return choiceSet;
	}

	public int getMaximumNumChoices() {
		int maximumNumChoices = 0;
		Iterator<ChoiceSet> choiceSetIterator = m_choiceSetMap.values()
				.iterator();
		while (choiceSetIterator.hasNext()) {
			ChoiceSet choiceSet = choiceSetIterator.next();
			maximumNumChoices = Math.max(maximumNumChoices, choiceSet
					.numChoices());
		}
		return maximumNumChoices;
	}
}

class ChoiceSetKey {
	int m_weekOfYear;
	CarrierFlightPath[] m_flightPaths;

	ChoiceSetKey(AirlineItinerary itinerary) {
		// The first week will be 0 (corresponding to days 1 - 7)
		m_weekOfYear = (itinerary.getDayOfYear() - 1) / 7;
		m_flightPaths = itinerary.getFlightPaths();
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof ChoiceSetKey)) {
			return false;
		}
		ChoiceSetKey thatKey = (ChoiceSetKey) that;
		if (m_weekOfYear != thatKey.m_weekOfYear) {
			return false;
		}
		if (m_flightPaths.length != thatKey.m_flightPaths.length) {
			return false;
		}
		for (int i = 0; i < m_flightPaths.length; ++i) {
			if (!m_flightPaths[i].equals(thatKey.m_flightPaths[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return getHashKey().hashCode();
	}

	public String getHashKey() {
		StringBuffer hashKey = new StringBuffer();
		hashKey.append("W").append(m_weekOfYear).append("-");
		for (int i = 0; i < m_flightPaths.length; ++i) {
			if (i > 0) {
				hashKey.append(";");
			}
			hashKey.append(m_flightPaths[i].getHashKey());
		}
		return hashKey.toString();
	}
}

package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.jet.random.sampling.RandomSampler;

public class ChoiceSet {

	int m_uniqueID;
	Map<String, Integer> m_alternativeChoiceIDMap;
	List<AirlineItinerary> m_itineraryList;

	protected ChoiceSet(int uniqueID) {
		m_uniqueID = uniqueID;
		m_alternativeChoiceIDMap = new HashMap<String, Integer>();
		m_itineraryList = new ArrayList<AirlineItinerary>();
	}

	public int addChoice(AirlineItinerary itinerary) {
		String choiceKey = itinerary.getFlightSequence();
		Integer choiceID = m_alternativeChoiceIDMap.get(choiceKey);
		if (choiceID == null) {
			choiceID = new Integer(m_itineraryList.size());
			m_alternativeChoiceIDMap.put(choiceKey, choiceID);
			m_itineraryList.add(itinerary);
		}
		return choiceID.intValue();
	}

	public int getUniqueID() {
		return m_uniqueID;
	}

	public int numChoices() {
		return m_itineraryList.size();
	}
	
	public AirlineItinerary getItinerary(int index) {
		return m_itineraryList.get(index);
	}

	// INVARIANT: Sample is returned in sorted order ascending
	protected int sampleChoices(int[] sample, int originalChoice) {
		int numChoices = numChoices();
		// Subtract 1 to save a spot for the original choice
		int rawSampleSize = sample.length - 1;
		long[] rawSample = new long[rawSampleSize];
		RandomSampler.sample(rawSampleSize, numChoices - 1, rawSampleSize, 0,
				rawSample, 0, null);
		int choiceIndex = -1;
		for (int i = 0; i < rawSample.length; ++i) {
			if (rawSample[i] < originalChoice) {
				sample[i] = (int) rawSample[i];
			} else {
				if (choiceIndex < 0) {
					sample[i] = originalChoice;
					choiceIndex = i;
				}
				sample[i + 1] = (int) rawSample[i] + 1;
			}
		}
		if (choiceIndex < 0) {
			// Set the last element of the sample to the original choice
			sample[rawSampleSize] = originalChoice;
			choiceIndex = rawSampleSize;
		}
		return choiceIndex;
	}
}

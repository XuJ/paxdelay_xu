package edu.mit.nsfnats.paxdelay.choice;

public abstract class AlternativeFeature implements Feature {
	
	public FeatureValue[] getValues(AirlineItinerary[] alternatives) {
		FeatureValue[] featureValues = new FeatureValue[alternatives.length];
		for (int i = 0; i < alternatives.length; ++i) {
			featureValues[i] = getValue(alternatives[i]);
		}
		return featureValues;
	}
	
	protected abstract FeatureValue getValue(AirlineItinerary itinerary);
}

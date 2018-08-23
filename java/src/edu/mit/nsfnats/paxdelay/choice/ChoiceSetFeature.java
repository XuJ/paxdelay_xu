package edu.mit.nsfnats.paxdelay.choice;

public abstract class ChoiceSetFeature implements Feature {

	public FeatureValue getValue(ChoiceSet choiceSet, int choiceIndex) {
		return getValue(choiceSet.getItinerary(choiceIndex));
	}
	
	protected abstract FeatureValue getValue(AirlineItinerary itinerary);
}

package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;

public class FeatureSet {

	public static final String PROPERTY_ALTERNATIVE_FEATURE_CLASS = "ALTERNATIVE_FEATURE_CLASS";
	public static final String PROPERTY_CHOICE_SET_FEATURE_CLASS = "CHOICE_SET_FEATURE_CLASS";

	ChoiceSetFeature[] m_choiceSetFeatures;
	AlternativeFeature[] m_alternativeFeatures;

	public void initialize(Properties properties) throws InvalidFormatException {
		m_choiceSetFeatures = loadChoiceSetFeatures(properties,
				PROPERTY_CHOICE_SET_FEATURE_CLASS);
		m_alternativeFeatures = loadAlternativeFeatures(properties,
				PROPERTY_ALTERNATIVE_FEATURE_CLASS);
	}

	@SuppressWarnings("unchecked")
	public ChoiceSetFeature[] loadChoiceSetFeatures(Properties properties, String baseKey)
			throws InvalidFormatException {
		String[] featureClasses = PropertiesReader.readStrings(properties,
				baseKey);
		ChoiceSetFeature[] features = new ChoiceSetFeature[featureClasses.length];
		for (int i = 0; i < featureClasses.length; ++i) {
			Class<ChoiceSetFeature> clazz = null;
			try {
				clazz = (Class<ChoiceSetFeature>) Class.forName(featureClasses[i]);
			} catch (ClassNotFoundException e) {
				throw new InvalidFormatException("Choice set feature " + featureClasses[i]
						+ " does not exist", e);
			}
			try {
				features[i] = clazz.newInstance();
			} catch (InstantiationException e) {
				throw new InvalidFormatException(
						"Unable to instantiate feature " + featureClasses[i], e);
			} catch (IllegalAccessException e) {
				throw new InvalidFormatException(
						"Unable to access constructor on feaature "
								+ featureClasses[i], e);
			}
			features[i].initialize(properties);
		}
		return features;
	}

	@SuppressWarnings("unchecked")
	public AlternativeFeature[] loadAlternativeFeatures(Properties properties, String baseKey)
			throws InvalidFormatException {
		String[] featureClasses = PropertiesReader.readStrings(properties,
				baseKey);
		AlternativeFeature[] features = new AlternativeFeature[featureClasses.length];
		for (int i = 0; i < featureClasses.length; ++i) {
			Class<AlternativeFeature> clazz = null;
			try {
				clazz = (Class<AlternativeFeature>) Class.forName(featureClasses[i]);
			} catch (ClassNotFoundException e) {
				throw new InvalidFormatException("Alternative feature " + featureClasses[i]
						+ " does not exist", e);
			}
			try {
				features[i] = clazz.newInstance();
			} catch (InstantiationException e) {
				throw new InvalidFormatException(
						"Unable to instantiate feature " + featureClasses[i], e);
			} catch (IllegalAccessException e) {
				throw new InvalidFormatException(
						"Unable to access constructor on feaature "
								+ featureClasses[i], e);
			}
			features[i].initialize(properties);
		}
		return features;
	}

	public FeatureValue[] getChoiceSetFeatureValues(ChoiceSet choiceSet, int choiceIndex) {
		FeatureValue[] values = new FeatureValue[m_choiceSetFeatures.length];
		for (int i = 0; i < m_choiceSetFeatures.length; ++i) {
			values[i] = m_choiceSetFeatures[i].getValue(choiceSet, choiceIndex);
		}
		return values;
	}

	public FeatureValue[][] getAlternativeFeatureValues(ChoiceSet choiceSet, int[] alternatives) {
		AirlineItinerary[] alternativeItineraries;
		if (alternatives == null) {
			alternativeItineraries = new AirlineItinerary[choiceSet.numChoices()];
			for (int i = 0; i < alternativeItineraries.length; ++i) {
				alternativeItineraries[i] = choiceSet.getItinerary(i); 
			}
		}
		else {
			alternativeItineraries = new AirlineItinerary[alternatives.length];
			for (int i = 0; i < alternativeItineraries.length; ++i) {
				alternativeItineraries[i] = choiceSet.getItinerary(alternatives[i]); 
			}
		}
		FeatureValue[][] alternativeValues = 
			new FeatureValue[alternativeItineraries.length][m_alternativeFeatures.length];
		for (int i = 0; i < m_alternativeFeatures.length; ++i) {
			FeatureValue[] values = m_alternativeFeatures[i].getValues(alternativeItineraries);
			for (int j = 0; j < alternativeItineraries.length; ++j) {
				alternativeValues[j][i] = values[j];
			}
		}
		return alternativeValues;
	}

	public String[] getChoiceSetColumnHeaders() {
		return getColumnHeaders(m_choiceSetFeatures);
	}

	public String[] getAlternativeColumnHeaders() {
		return getColumnHeaders(m_alternativeFeatures);
	}

	protected String[] getColumnHeaders(Feature[] features) {
		List<String> columnHeaderList = new ArrayList<String>();
		for (int i = 0; i < features.length; ++i) {
			String[] columnHeaders = features[i].getColumnHeaders();
			for (int j = 0; j < columnHeaders.length; ++j) {
				columnHeaderList.add(columnHeaders[j]);
			}
		}
		String[] columnHeaderArray = new String[columnHeaderList.size()];
		columnHeaderList.toArray(columnHeaderArray);
		return columnHeaderArray;
	}
}

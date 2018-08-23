package edu.mit.nsfnats.paxdelay.choice;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import edu.mit.nsfnats.paxdelay.util.RandomValuesComparator;

public class ObservationSet {

	public static final String PROPERTY_MAXIMUM_NUMBER_CHOICES = "MAXIMUM_NUMBER_CHOICES";
	public static final int DEFAULT_MAXIMUM_NUMBER_CHOICES = -1;

	public static final String PROPERTY_OUTPUT_FIELD_SEPARATOR = "OUTPUT_FIELD_SEPARATOR";
	public static final String DEFAULT_OUTPUT_FIELD_SEPARATOR = "\t";
	public static String OUTPUT_FIELD_SEPARATOR;

	public static final String PROPERTY_DEFAULT_MISSING_VALUE = "DEFAULT_MISSING_VALUE";
	public static final String DEFAULT_DEFAULT_MISSING_VALUE = "99999.9";
	public static String DEFAULT_MISSING_VALUE;

	public static final String PROPERTY_RANDOMLY_PERMUTE_OBSERVATIONS = "RANDOMLY_PERMUTE_OBSERVATIONS";
	public static final boolean DEFAULT_RANDOMLY_PERMUTE_OBSERVATIONS = false;

	int m_maxNumChoices;
	ChoiceSetManager m_choiceSetManager;
	FeatureSet m_featureSet;
	boolean m_randomlyPermuteObservations;

	Map<Observation, Observation> m_distinctObservations;
	List<Observation> m_observationList;

	public ObservationSet() {
		m_choiceSetManager = new ChoiceSetManager();
		m_featureSet = new FeatureSet();
		m_distinctObservations = new HashMap<Observation, Observation>();
		m_observationList = new ArrayList<Observation>();
	}

	public void initialize(Properties properties) throws InvalidFormatException {
		OUTPUT_FIELD_SEPARATOR = properties
				.getProperty(PROPERTY_OUTPUT_FIELD_SEPARATOR,
						DEFAULT_OUTPUT_FIELD_SEPARATOR);
		DEFAULT_MISSING_VALUE = properties.getProperty(
				PROPERTY_DEFAULT_MISSING_VALUE, DEFAULT_DEFAULT_MISSING_VALUE);
		m_maxNumChoices = PropertiesReader
				.readInt(properties, PROPERTY_MAXIMUM_NUMBER_CHOICES,
						DEFAULT_MAXIMUM_NUMBER_CHOICES);
		m_randomlyPermuteObservations = PropertiesReader.readBoolean(
				properties, PROPERTY_RANDOMLY_PERMUTE_OBSERVATIONS,
				DEFAULT_RANDOMLY_PERMUTE_OBSERVATIONS);
		m_choiceSetManager.initialize(properties);
		m_featureSet.initialize(properties);
	}

	public void processAirlineItinerary(AirlineItinerary itinerary, boolean createChoiceSet) {
		ChoiceSet choiceSet = 
			m_choiceSetManager.getChoiceSet(itinerary, createChoiceSet);
		if (choiceSet == null) {
			return;
		}
		int choiceIndex = choiceSet.addChoice(itinerary);

		Observation observation = new Observation(choiceSet, choiceIndex);
		int numPassengers = itinerary.getNumPassengers();

		Observation distinctObservation = 
			m_distinctObservations.get(observation);
		if (distinctObservation == null) {
			observation.setPassengerCount(numPassengers);
			m_distinctObservations.put(observation, observation);
			m_observationList.add(observation);
		} else {
			distinctObservation.incrementPassengerCount(numPassengers);
		}
	}

	public void writeColumnHeaders(PrintWriter writer) {
		writer.print("Choice_Set_ID");
		writer.print(OUTPUT_FIELD_SEPARATOR);
		writer.print("Passenger_Count");
		writer.print(OUTPUT_FIELD_SEPARATOR);
		writer.print("Choice");
		writer.print(OUTPUT_FIELD_SEPARATOR);
		writer.print("Number_Choices");

		String[] observationColumnHeaders = m_featureSet
				.getChoiceSetColumnHeaders();
		for (int i = 0; i < observationColumnHeaders.length; ++i) {
			writer.print(OUTPUT_FIELD_SEPARATOR);
			writer.print(observationColumnHeaders[i]);
		}

		String[] alternativeColumnHeaders = m_featureSet
				.getAlternativeColumnHeaders();
		if (m_maxNumChoices < 1) {
			m_maxNumChoices = m_choiceSetManager.getMaximumNumChoices();
		}
		for (int i = 0; i < m_maxNumChoices; ++i) {
			for (int j = 0; j < alternativeColumnHeaders.length; ++j) {
				writer.print(OUTPUT_FIELD_SEPARATOR);
				writer.print(alternativeColumnHeaders[j] + "_" + i);
			}
		}
		writer.println();
		writer.flush();
	}

	public void writeChoiceData(PrintWriter writer) {
		if (m_maxNumChoices < 1) {
			m_maxNumChoices = m_choiceSetManager.getMaximumNumChoices();
		}
		Observation[] observations = new Observation[m_observationList.size()];
		m_observationList.toArray(observations);
		if (m_randomlyPermuteObservations) {
			randomlyPermuteObservations(observations);
		}
		for (int i = 0; i < observations.length; ++i) {
			observations[i].writeChoiceData(writer, m_featureSet, m_maxNumChoices);
		}
		writer.flush();
	}

	public void randomlyPermuteObservations(Observation[] observations) {
		int numObservations = observations.length;
		Integer[] initialIndices = new Integer[numObservations];
		for (int i = 0; i < initialIndices.length; ++i) {
			initialIndices[i] = new Integer(i);
		}
		Arrays
				.sort(initialIndices, new RandomValuesComparator(
						numObservations));
		Observation[] tempObservations = observations.clone();
		for (int i = 0; i < observations.length; ++i) {
			observations[i] = tempObservations[initialIndices[i].intValue()];
		}
	}
}

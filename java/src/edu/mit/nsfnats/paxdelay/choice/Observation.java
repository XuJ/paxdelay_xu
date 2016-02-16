package edu.mit.nsfnats.paxdelay.choice;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Observation {
	ChoiceSet m_choiceSet;
	int m_choiceIndex;
	int m_passengerCount;

	public Observation(ChoiceSet choiceSet, int choiceIndex) {
		m_choiceSet = choiceSet;
		m_choiceIndex = choiceIndex;
	}

	public int getPassengerCount() {
		return m_passengerCount;
	}

	public void setPassengerCount(int count) {
		m_passengerCount = count;
	}

	public void incrementPassengerCount(int increment) {
		m_passengerCount += increment;
	}

	public ChoiceSet getChoiceSet() {
		return m_choiceSet;
	}

	public int getChoiceIndex() {
		return m_choiceIndex;
	}
	
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof Observation)) {
			return false;
		}
		Observation thatObservation = (Observation) that;
		if (!m_choiceSet.equals(thatObservation.m_choiceSet)) {
			return false;
		}
		if (m_choiceIndex != thatObservation.m_choiceIndex) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		String hashKey = getHashKey();
		return hashKey.hashCode();
	}

	protected String getHashKey() {
		StringBuffer observationKey = new StringBuffer(m_choiceSet.getUniqueID());
		observationKey.append(":").append(m_choiceIndex);
		return observationKey.toString();
	}

	public void writeChoiceData(PrintWriter writer, FeatureSet featureSet, int maxNumChoices) {
		int numPassengers = getPassengerCount();


		// We do not need to write any choice data for paths with negative
		// passenger counts, since the data are already aggregated
		if (numPassengers <= 0) {
			return;
		}
		ChoiceSet choiceSet = getChoiceSet();
		int choiceIndex = getChoiceIndex();
		FeatureValue[] choiceSetFeatureValues = 
			featureSet.getChoiceSetFeatureValues(choiceSet, choiceIndex);
		int numChoices = choiceSet.numChoices();
		if (numChoices <= 1) {
			return;
		}
		if (numChoices <= maxNumChoices) {
			
			FeatureValue[][] alternativeFeatureValues = 
				featureSet.getAlternativeFeatureValues(choiceSet, null);

			writer.print(choiceSet.getUniqueID());
			writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
			writer.print(getPassengerCount());
			writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
			writer.print(choiceIndex);
			writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
			writer.print(numChoices);

			writeFeatureValues(writer, choiceSetFeatureValues);

			int i;
			int numColumns = 0;
			for (i = 0; i < alternativeFeatureValues.length; ++i) {
				numColumns = writeFeatureValues(writer, alternativeFeatureValues[i]);
			}
			for (; i < maxNumChoices; ++i) {
				for (int j = 0; j < numColumns; ++j) {
					writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
					writer.print(ObservationSet.DEFAULT_MISSING_VALUE);
				}
			}
			writer.println();
		} else {
			int numSamples = numPassengers;
			int originalChoiceIndex = getChoiceIndex();
			int[][] choiceSetSamples = new int[numSamples][maxNumChoices];
			int[] sampledChoiceIndices = new int[numSamples];
			for (int i = 0; i < numSamples; ++i) {
				sampledChoiceIndices[i] = choiceSet.sampleChoices(
						choiceSetSamples[i], originalChoiceIndex);
			}
			Map<ChoiceSetSample, ChoiceSetSample> distinctSampleMap = new HashMap<ChoiceSetSample, ChoiceSetSample>();
			for (int i = 0; i < numSamples; ++i) {
				ChoiceSetSample sample = new ChoiceSetSample(
						choiceSetSamples[i], sampledChoiceIndices[i]);
				ChoiceSetSample distinctSample = distinctSampleMap.get(sample);
				if (distinctSample == null) {
					distinctSample = sample;
					distinctSampleMap.put(distinctSample, distinctSample);
				} else {
					distinctSample.incrementSampleCount();
				}
			}
			Iterator<ChoiceSetSample> sampleIter = distinctSampleMap.values()
					.iterator();
			while (sampleIter.hasNext()) {
				ChoiceSetSample sample = sampleIter.next();

				writer.print(choiceSet.getUniqueID());
				writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
				writer.print(sample.getSampleCount());

				writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
				writer.print(sample.getSampledChoiceIndex());
				// In the case of sampling, the size of the resulting
				// choice set will always be maxNumChoices
				writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
				writer.print(maxNumChoices);

				writeFeatureValues(writer, choiceSetFeatureValues);

				int[] sampledChoices = sample.getSampledChoices();
				FeatureValue[][] alternativeFeatureValues = 
					featureSet.getAlternativeFeatureValues(choiceSet, sampledChoices);
				for (int i = 0; i < alternativeFeatureValues.length; ++i) {
					writeFeatureValues(writer, alternativeFeatureValues[i]);
				}
				writer.println();
			}
		}
	}

	protected int writeFeatureValues(PrintWriter writer,
			FeatureValue[] featureValues) {
		int numColumns = 0;
		for (int i = 0; i < featureValues.length; ++i) {
			String[] values = featureValues[i].getValues();
			numColumns += values.length;
			for (int j = 0; j < values.length; ++j) {
				writer.print(ObservationSet.OUTPUT_FIELD_SEPARATOR);
				writer.print(values[j]);
			}
		}
		return numColumns;
	}
}

class ChoiceSetSample {
	int[] m_sampledChoices;
	int m_sampledChoiceIndex;
	int m_numSamples;

	ChoiceSetSample(int[] sampledChoices, int sampledChoiceIndex) {
		m_sampledChoices = sampledChoices;
		m_sampledChoiceIndex = sampledChoiceIndex;
		m_numSamples = 1;
	}

	public int[] getSampledChoices() {
		return m_sampledChoices;
	}

	public int getSampledChoiceIndex() {
		return m_sampledChoiceIndex;
	}

	public void incrementSampleCount() {
		m_numSamples += 1;
	}

	public int getSampleCount() {
		return m_numSamples;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof ChoiceSetSample)) {
			return false;
		}
		ChoiceSetSample thatSample = (ChoiceSetSample) that;
		if (m_sampledChoices.length != thatSample.m_sampledChoices.length) {
			return false;
		}
		for (int i = 0; i < m_sampledChoices.length; ++i) {
			if (m_sampledChoices[i] != thatSample.m_sampledChoices[i]) {
				return false;
			}
		}
		return m_sampledChoiceIndex == thatSample.m_sampledChoiceIndex;
	}

	@Override
	public int hashCode() {
		return getHashKey().hashCode();
	}

	protected String getHashKey() {
		StringBuffer hashKey = new StringBuffer();
		hashKey.append("C").append(m_sampledChoiceIndex).append(":");
		for (int i = 0; i < m_sampledChoices.length; ++i) {
			if (i > 0) {
				hashKey.append(";");
			}
			hashKey.append(m_sampledChoices[i]);
		}
		return hashKey.toString();
	}
}

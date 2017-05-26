package edu.mit.nsfnats.paxdelay.allocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;

public class RandomEffectsUtilityCalculator implements
		ItineraryUtilityCalculator {
	public static final String DEFAULT_ALLOCATION_PARAMETERS_FILENAME = "RandomEffectParameters.txt";

	String m_parametersDirectory;
	String m_parametersFilename;

	RandomEffect[] m_dayRandomEffects;
	RandomEffect[] m_timeRandomEffects;
	RandomEffect m_cancellationRandomEffect;
	RandomEffect m_minimumSeatsRandomEffect;
	RandomEffect[] m_connectionTimeRandomEffects;

	double[] m_dayCoefficients;
	double[] m_timeCoefficients;
	double m_cancellationCoefficient;
	double m_minimumSeatsCoefficient;
	double[] m_connectionTimeCoefficients;
	
	@Override
	public void initialize(Properties properties) throws InvalidFormatException {
		m_parametersDirectory = properties
				.getProperty(PROPERTY_ALLOCATION_PARAMETERS_DIRECTORY);
		m_parametersFilename = properties.getProperty(
				PROPERTY_ALLOCATION_PARAMETERS_FILENAME,
				DEFAULT_ALLOCATION_PARAMETERS_FILENAME);
		
		readChoiceParameters();
	}

	protected void readChoiceParameters() throws InvalidFormatException {
		String parameterFile = m_parametersDirectory + File.separator
				+ m_parametersFilename;
		Map<String, RandomEffect> parameterMap = new HashMap<String, RandomEffect>();
		try {
			FileReader fr = new FileReader(new File(parameterFile));
			BufferedReader in = new BufferedReader(fr);
			String line = in.readLine();
			line = in.readLine(); // To discard the header row
			while (line != null) {
				String[] temp = line.split("\t");
				parameterMap.put(temp[0], 
						new RandomEffect(Double.parseDouble(temp[1]),
								Double.parseDouble(temp[2])));
				line = in.readLine();
			}
			in.close();
			fr.close();
		} catch (FileNotFoundException e) {
			throw new InvalidFormatException("Unable to find discrete choice parameter file "
					+ parameterFile, e);
		} catch (IOException e) {
			throw new InvalidFormatException("Unable to read discrete choice parameters from "
					+ parameterFile, e);
		}
		m_dayRandomEffects = new RandomEffect[7];
		m_dayCoefficients = new double[7];
		// The first day is not specified, so the coefficient
		// will remain equal to zero (the default value)
		for (int dayIndex = 1; dayIndex < 7; ++dayIndex) {
			m_dayRandomEffects[dayIndex] = parameterMap.get("BETA_DW" + Integer.toString(dayIndex + 1));
		}
		m_timeRandomEffects = new RandomEffect[6];
		m_timeCoefficients = new double[6];
		// The first time block is not specified, so the coefficient
		// will remain equal to zero (the default value)
		for (int timeIndex = 1; timeIndex < 6; ++timeIndex) {
			m_timeRandomEffects[timeIndex] = parameterMap.get("BETA_HR" + String.format("%1$02d", timeIndex * 4 + 1));
		}
		m_cancellationRandomEffect = parameterMap.get("BETA_CANCEL");
		m_minimumSeatsRandomEffect = parameterMap.get("BETA_LN_SEATS");
		m_connectionTimeRandomEffects = new RandomEffect[3];
		m_connectionTimeCoefficients = new double[3];
		m_connectionTimeRandomEffects[0] = parameterMap.get("BETA_CNCT30");
		m_connectionTimeRandomEffects[1] = parameterMap.get("BETA_CNCT45");
		m_connectionTimeRandomEffects[2] = parameterMap.get("BETA_CNCT75");
	}
	
	@Override
	public void resetParameters(Random randomEngine) {
		for (int i = 0; i < m_dayCoefficients.length; ++i) {
			m_dayCoefficients[i] =
				getCoefficientValue(m_dayRandomEffects[i], randomEngine);
		}
		// The first time block is not specified, so the coefficient
		// will remain equal to zero (the default value)
		for (int i = 0; i < m_timeCoefficients.length; ++i) {
			m_timeCoefficients[i] =
				getCoefficientValue(m_timeRandomEffects[i], randomEngine);
		}
		m_cancellationCoefficient = 
			getCoefficientValue(m_cancellationRandomEffect, randomEngine);
		m_minimumSeatsCoefficient = 
			getCoefficientValue(m_minimumSeatsRandomEffect, randomEngine);
		for (int i = 0; i < m_connectionTimeCoefficients.length; ++i) {
			m_connectionTimeCoefficients[i] =
				getCoefficientValue(m_connectionTimeRandomEffects[i], randomEngine);
		}
	}
	
	protected double getCoefficientValue(RandomEffect effect, Random randomEngine) {
		if (effect == null) {
			return 0.0;
		}
		double coefficient = randomEngine.nextGaussian();
		coefficient *= effect.getStandardDeviation();
		coefficient += effect.getMean();
		return coefficient;
	}

	@Override
	public double calculateUtility(AllocationItinerary itinerary) {
		int dayIndex = itinerary.m_departureDayOfWeek - 1;
		int timeIndex = (int) Math.floor((itinerary.m_departureHour - 1.0) / 4.0);
		if (timeIndex < 0) {
			dayIndex = (dayIndex + 6) % 7;
			timeIndex = 5;
		}
		double utility = m_dayCoefficients[dayIndex];
		utility += m_dayCoefficients[timeIndex];
		if (itinerary.m_cancelledFlag) {
			utility += m_cancellationCoefficient;
		}
		utility += m_minimumSeatsCoefficient * Math.log(itinerary.m_minimumSeats);
		utility += m_connectionTimeCoefficients[0] * Math.min(Math.max(itinerary.m_connectionTime - 30, 0), 15);
		utility += m_connectionTimeCoefficients[1] * Math.min(Math.max(itinerary.m_connectionTime - 45, 0), 15);
		utility += m_connectionTimeCoefficients[2] * Math.max(itinerary.m_connectionTime - 75, 0);
		return utility;
	}
}

class RandomEffect {
	double m_mean;
	double m_standardDeviation;
	
	RandomEffect(double mean, double standardDeviation) {
		m_mean = mean;
		m_standardDeviation = standardDeviation;
	}

	public double getMean() {
		return m_mean;
	}

	public double getStandardDeviation() {
		return m_standardDeviation;
	}
}

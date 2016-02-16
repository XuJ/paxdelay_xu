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

public class FixedEffectsUtilityCalculator implements ItineraryUtilityCalculator {
	public static final String DEFAULT_ALLOCATION_PARAMETERS_FILENAME = "FixedEffectParameters.txt";

	String m_parametersDirectory;
	String m_parametersFilename;
	
	double[] m_dayCoefficients;
	double[] m_timeCoefficients;
	double[][] m_dayTimeCoefficients;
	double m_cancellationCoefficient;
	double m_minimumSeatsCoefficient;
	double[] m_connectionTimeCoefficients;
	double[][] m_deltaZoneTimeCoefficients;
	
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
		Map<String, Double> parameterMap = new HashMap<String, Double>();
		try {
			FileReader fr = new FileReader(new File(parameterFile));
			BufferedReader in = new BufferedReader(fr);
			String line = in.readLine();
			line = in.readLine(); // To discard the header row
			while (line != null) {
				String[] temp = line.split("\t");
				parameterMap.put(temp[0], Double.parseDouble(temp[1]));
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
		m_dayCoefficients = new double[7];
		// The first day is not specified, so the coefficient
		// will remain equal to zero (the default value)
		for (int dayIndex = 1; dayIndex < 7; ++dayIndex) {
			m_dayCoefficients[dayIndex] = parameterMap.get("BETA_DW" + Integer.toString(dayIndex + 1));
		}
		m_timeCoefficients = new double[6];
		// The first hour block is not specified, so the coefficient
		// will remain equal to zero (the default value)
		for (int timeIndex = 1; timeIndex < 6; ++timeIndex) {
			m_timeCoefficients[timeIndex] = parameterMap.get("BETA_HR" + String.format("%1$02d", timeIndex * 4 + 1));
		}
		m_dayTimeCoefficients = new double[7][6];
		// Neither the first day nor the first hour block are specified,
		// so those coefficients for those combinations 
		// will remain equal to zero (the default value)
		for (int dayIndex = 1; dayIndex < 7; ++dayIndex) {
			for (int timeIndex = 1; timeIndex < 6; ++timeIndex) {
				StringBuffer parameterName = new StringBuffer("BETA_DW");
				parameterName.append(dayIndex + 1);
				parameterName.append("_HR");
				parameterName.append(String.format("%1$02d", timeIndex * 4 + 1));
				Double parameter = parameterMap.get(parameterName.toString());
				m_dayTimeCoefficients[dayIndex][timeIndex] = parameter.doubleValue();
			}
		}
		m_cancellationCoefficient = parameterMap.get("BETA_CANCEL");
		m_minimumSeatsCoefficient = parameterMap.get("BETA_LN_SEATS");
		m_connectionTimeCoefficients = new double[3];
		m_connectionTimeCoefficients[0] = parameterMap.get("BETA_CNCT30");
		m_connectionTimeCoefficients[1] = parameterMap.get("BETA_CNCT45");
		m_connectionTimeCoefficients[2] = parameterMap.get("BETA_CNCT75");
		m_deltaZoneTimeCoefficients = new double[3][6];
		// Neither the first delta time zone category nor the first hour
		// block are specified, so the coefficients for those combinations
		// will remain equal to zero (the default value)
		String[] zoneIndicators = new String[]{"N/A", "ZN(1)", "ZN1"};
		for (int zoneIndex = 1; zoneIndex < 3; ++zoneIndex) {
			for (int timeIndex = 1; timeIndex < 6; ++ timeIndex) {
				StringBuffer parameterName = new StringBuffer("BETA_");
				parameterName.append(zoneIndicators[zoneIndex]);
				parameterName.append("_HR");
				parameterName.append(String.format("%1$02d", timeIndex * 4 + 1));
				Double parameter = parameterMap.get(parameterName.toString());
				m_deltaZoneTimeCoefficients[zoneIndex][timeIndex] = parameter.doubleValue();
			}
		}
	}
	
	@Override
	public void resetParameters(Random randomEngine) {
		// Do nothing when we are using a fixed effects regression function
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
		utility += m_dayTimeCoefficients[dayIndex][timeIndex];
		if (itinerary.m_cancelledFlag) {
			utility += m_cancellationCoefficient;
		}
		utility += m_minimumSeatsCoefficient * Math.log(itinerary.m_minimumSeats);
		utility += m_connectionTimeCoefficients[0] * Math.min(Math.max(itinerary.m_connectionTime - 30, 0), 15);
		utility += m_connectionTimeCoefficients[1] * Math.min(Math.max(itinerary.m_connectionTime - 45, 0), 15);
		utility += m_connectionTimeCoefficients[2] * Math.max(itinerary.m_connectionTime - 75, 0);
		
		int zoneDifference = itinerary.m_destinationTimeZoneOffset - itinerary.m_originTimeZoneOffset;
		int zoneIndex = 0;
		if (zoneDifference > -2 && zoneDifference < 2) {
			zoneIndex = 1;
		} else if (zoneDifference >= 2) {
			zoneIndex = 2;
		}
		utility += m_deltaZoneTimeCoefficients[zoneIndex][timeIndex];
		return utility;
	}
}

package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PathSize extends AlternativeFeature {
	public static Logger logger = Logger.getLogger(PathSize.class);

	String[] m_columnHeaders;

	public PathSize() {
		m_columnHeaders = new String[] { "Path_Size" };
	}
	
	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}
	
	public FeatureValue[] getValues(AirlineItinerary[] alternatives) {
		double[] pathLength = new double[alternatives.length];
		double shortestPathLength = Double.MAX_VALUE;
		Map<Integer, List<Integer>> flightItineraryMap = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < alternatives.length; ++i) {
			// Calculate the total travel time for each alternative
			// and keep track of which alternatives share flights
			FlightInformation[] flightInfos = alternatives[i].getFlightInfos();
			for (int j = 0; j < flightInfos.length; ++j) {
				pathLength[i] += flightInfos[j].getDuration();
				List<Integer> itineraryList = flightItineraryMap.get(flightInfos[j].m_flightID);
				if (itineraryList == null) {
					itineraryList = new ArrayList<Integer>();
					flightItineraryMap.put(flightInfos[j].m_flightID, itineraryList);
				}
				itineraryList.add(i);
			}
			if (flightInfos.length > 1) {
				pathLength[i] += alternatives[i].getConnectionTime();
			}
			shortestPathLength = Math.min(shortestPathLength, pathLength[i]);
		}
		
		FeatureValue[] featureValues = new FeatureValue[alternatives.length];
		for (int i = 0; i < alternatives.length; ++i) {
			double pathSize = 0.0;
			FlightInformation[] flightInfos = alternatives[i].getFlightInfos();
			for (int j = 0; j < flightInfos.length; ++j) {
				List<Integer> itineraryList = flightItineraryMap.get(flightInfos[j].m_flightID);
				Iterator<Integer> itineraryIterator = itineraryList.iterator();
				double adjacencySum = 0.0;
				while (itineraryIterator.hasNext()) {
					int itineraryIndex = itineraryIterator.next().intValue();
					adjacencySum += shortestPathLength / pathLength[itineraryIndex];
				}
				pathSize += flightInfos[j].getDuration() / adjacencySum;
			}
			pathSize = pathSize / pathLength[i];
			featureValues[i] = new FeatureValue(this, 
					new String[] {String.format("%1$4.2f", pathSize)});
		}
		return featureValues;
	}
	
	@Override
	public void initialize(Properties properties) {
		// Do nothing...
	}
	
	protected FeatureValue getValue(AirlineItinerary itinerary) {
		// Throw an exception is someone tries to access this method
		throw new RuntimeException("PathSize.getValue(AirlineItinerary) is unsupported");
	}
}
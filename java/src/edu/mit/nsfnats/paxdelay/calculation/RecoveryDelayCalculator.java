package edu.mit.nsfnats.paxdelay.calculation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;

public class RecoveryDelayCalculator {
	static Logger logger = Logger.getLogger(RecoveryDelayCalculator.class);
	
	public static final int DEFAULT_FETCH_SIZE = 5000;	
	public static final String NEWLINE = "\n";

	Connection m_dbConnection;
	
	Map<String, String[]> m_relatedCarriersMap;
	Map<NonStopKey, NonStopItineraryData> m_nonStopDataMap;
	Map<OneStopKey, OneStopItineraryData> m_oneStopDataMap;
	
	public static double s_betaIntercept = 364.779024;
	public static double[] s_betaDisruptionHour = 
		new double[]{
		// Disruption_Hour_0
		0.0,
		// Disruption_Hour_1
		0.0,
		// Disruption_Hour_2
		0.0,
		// Disruption_Hour_3
		0.0,
		// Disruption_Hour_4
		0.0,
		// Disruption_Hour_5
		0.0,
		// Disruption_Hour_6
		24.371468,
		// Disruption_Hour_7
		48.086419,
		// Disruption_Hour_8
		73.548398,
		// Disruption_Hour_9
		90.561277,
		// Disruption_Hour_10
		126.401627,
		// Disruption_Hour_11
		147.073001,
		// Disruption_Hour_12
		189.274011,
		// Disruption_Hour_13
		277.073323,
		// Disruption_Hour_14
		300.611096,
		// Disruption_Hour_15
		329.230028,
		// Disruption_Hour_16
		360.160575,
		// Disruption_Hour_17
		404.016285,
		// Disruption_Hour_18
		463.027636,
		// Disruption_Hour_19
		500.307790,
		// Disruption_Hour_20
		515.658773,
		// Disruption_Hour_21
		536.100252,
		// Disruption_Hour_22
		464.915563,
		// Disruption_Hour_23
		464.915563		
	};
	
	public static double s_betaNonStopEmptyAlternatives =  -115.459013;
	public static double s_betaOvernightTimeUntilMorning = 58.954715;
	public static double s_betaIsCancellation = 184.933415;
	public static double s_betaStopsRemaining =  -73.136230;
	public static double s_betaOneStopItineraries = -1.085016;
	public static double s_betaStopsRemaining_OneStopItineraries = -6.565951;
	
	public RecoveryDelayCalculator(Connection dbConnection) {
		m_dbConnection = dbConnection;
		
		m_relatedCarriersMap = new HashMap<String, String[]>();
		m_nonStopDataMap = new HashMap<NonStopKey, NonStopItineraryData>();
		m_oneStopDataMap = new HashMap<OneStopKey, OneStopItineraryData>();
	}
	
	public void initialize(int year, int month) 
	throws InvalidFormatException {
		
		queryRelatedCarriers();
		queryNonStopFeatures(year, month);
		queryOneStopFeatures(year, month);
	}
	
	public void queryRelatedCarriers() throws InvalidFormatException {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select asqp.code as primary_carrier,").append(NEWLINE);
			query.append("  rc.secondary_carrier").append(NEWLINE);
			query.append("from asqp_carriers asqp").append(NEWLINE);
			query.append("left join related_carriers rc").append(NEWLINE);
			query.append("  on rc.primary_carrier = asqp.code").append(NEWLINE);
			query.append("order by asqp.code, rc.secondary_carrier");			
			
			logger.trace("Related carriers query:");
			logger.trace(query.toString());
			
			String currentCarrier = null;
			List<String> secondaryCarrierList = null;
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String primaryCarrier = rset.getString("primary_carrier");
				String secondaryCarrier = rset.getString("secondary_carrier");
				if (!primaryCarrier.equals(currentCarrier)) {
					if (currentCarrier != null) {
						String[] secondaryCarriers = new String[secondaryCarrierList.size()];
						secondaryCarrierList.toArray(secondaryCarriers);
						m_relatedCarriersMap.put(currentCarrier, secondaryCarriers);
					}
					currentCarrier = primaryCarrier;
					secondaryCarrierList = new ArrayList<String> ();
				}
				if (secondaryCarrier != null) {
					secondaryCarrierList.add(secondaryCarrier);
				}
			}
			// Make sure to add the last carrier...
			if (currentCarrier != null) {
				String[] secondaryCarriers = new String[secondaryCarrierList.size()];
				secondaryCarrierList.toArray(secondaryCarriers);
				m_relatedCarriersMap.put(currentCarrier, secondaryCarriers);
			}
		} catch (SQLException e) {
			throw new InvalidFormatException("Unable to load related carriers", e);
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
					logger.error("Unable to close SQL result set", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("Unable to close SQL statment", e);
				}
			}
		}		
	}
	
	public void queryNonStopFeatures(int year, int month) throws InvalidFormatException {
		Calendar calendar = new GregorianCalendar(year, month - 1, 1);
		double daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();		
			StringBuffer query = new StringBuffer();
			query.append("select carrier, origin, destination,").append(NEWLINE);
			query.append("  num_itineraries, load_factor").append(NEWLINE);
			query.append("from non_stop_features").append(NEWLINE);
			query.append("where month = ").append(month);
			
			logger.trace("Non-stop itinerary data query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				double numItineraries = rset.getDouble("num_itineraries");
				double loadFactor = rset.getDouble("load_factor");
				
				NonStopKey nonStopKey = new NonStopKey(carrier, origin, destination);
				NonStopItineraryData nonStopData = new NonStopItineraryData(nonStopKey);
				nonStopData.setNumItineraries(numItineraries / daysInMonth);
				nonStopData.setLoadFactor(loadFactor);
				m_nonStopDataMap.put(nonStopKey, nonStopData);
			}
		} catch (SQLException e) {
			throw new InvalidFormatException("Unable to load non-stop itinerary data", e);
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
					logger.error("Unable to close SQL result set", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("Unable to close SQL statment", e);
				}
			}
		}	
	}
	
	public void queryOneStopFeatures(int year, int month) throws InvalidFormatException {
		Calendar calendar = new GregorianCalendar(year, month - 1, 1);
		double daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();		
			StringBuffer query = new StringBuffer();
			query.append("select first_carrier, second_carrier,").append(NEWLINE);
			query.append("  origin, destination, num_itineraries").append(NEWLINE);
			query.append("from one_stop_features").append(NEWLINE);
			query.append("where month = ").append(month);
			
			logger.trace("One-stop itinerary data query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String firstCarrier = rset.getString("first_carrier");
				String secondCarrier = rset.getString("second_carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				double numItineraries = rset.getDouble("num_itineraries");
				
				OneStopKey oneStopKey = new OneStopKey(firstCarrier, secondCarrier,
						origin, destination);
				OneStopItineraryData oneStopData = new OneStopItineraryData(oneStopKey);
				oneStopData.setNumItineraries(numItineraries / daysInMonth);
				m_oneStopDataMap.put(oneStopKey, oneStopData);
			}
		} catch (SQLException e) {
			throw new InvalidFormatException("Unable to load one-stop itinerary data", e);
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
					logger.error("Unable to close SQL result set", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("Unable to close SQL statment", e);
				}
			}
		}
	}
	
	public int calculateCancellationDelay(String[] carriers, String disruptionOrigin,
			String destination, int disruptionHour, int numStopsRemaining) {
		String[] recoveryCarriers = getRecoveryCarriers(carriers);
		NonStopItineraryData nonStopData =
			getNonStopItineraryData(recoveryCarriers, disruptionOrigin, destination);
		OneStopItineraryData oneStopData =
			getOneStopItineraryData(recoveryCarriers, disruptionOrigin, destination);
		
		double recoveryDelay =
			calculateRecoveryDelay(nonStopData, oneStopData, disruptionHour, true, 
					numStopsRemaining);
		
		return (int) Math.round (recoveryDelay);
	}
	
	public int calculateMissedConnectionDelay(String[] carriers, String disruptionOrigin,
			String destination, int disruptionHour) {
		String[] recoveryCarriers = getRecoveryCarriers(carriers);
		NonStopItineraryData nonStopData =
			getNonStopItineraryData(recoveryCarriers, disruptionOrigin, destination);
		OneStopItineraryData oneStopData =
			getOneStopItineraryData(recoveryCarriers, disruptionOrigin, destination);
		double recoveryDelay =
			calculateRecoveryDelay(nonStopData, oneStopData, disruptionHour, false, 0);

		return (int) Math.round (recoveryDelay);
	}
	
	
	public String[] getRecoveryCarriers(String[] carriers) {
		Set<String> processedCarriers = new HashSet<String>();
		Set<String> recoveryCarriersSet = new HashSet<String>();
		for (int i = 0; i < carriers.length; ++i) {
			if (processedCarriers.add(carriers[i])) {
				String[] secondaryCarriers = m_relatedCarriersMap.get(carriers[i]);
				recoveryCarriersSet.add(carriers[i]);
				for (int j = 0; j < secondaryCarriers.length; ++j) {
					recoveryCarriersSet.add(secondaryCarriers[j]);
				}
			}
		}
		String[] recoveryCarriers = new String[recoveryCarriersSet.size()];
		recoveryCarriersSet.toArray(recoveryCarriers);
		Arrays.sort(recoveryCarriers);
		return recoveryCarriers;
	}
	
	public NonStopItineraryData getNonStopItineraryData(String[] carriers, String origin,
			String destination) {
		NonStopItineraryData data = 
			new NonStopItineraryData(new NonStopKey(origin, destination));
		for (int i = 0; i < carriers.length; ++i) {
			NonStopKey carrierKey = new NonStopKey(carriers[i], origin, destination);
			NonStopItineraryData carrierData = m_nonStopDataMap.get(carrierKey);
			if (carrierData != null) {
				data.addItineraryData(carrierData);
			}
		}
		return data;
	}
	
	public OneStopItineraryData getOneStopItineraryData(String[] carriers, String origin,
			String destination) {
		OneStopItineraryData data = 
			new OneStopItineraryData(new OneStopKey(origin, destination));
		for (int i = 0; i < carriers.length; ++i) {
			for (int j = i; j < carriers.length; ++j) {
				OneStopKey carrierKey = 
					new OneStopKey(carriers[i], carriers[j], origin, destination);
				OneStopItineraryData carrierData = m_oneStopDataMap.get(carrierKey);
				data.addItineraryData(carrierData);
			}
		}
		return data;		
	}

	protected double calculateRecoveryDelay(NonStopItineraryData nonStopData, 
			OneStopItineraryData oneStopData, int disruptionHour, boolean isCancellation,
			int numStopsRemaining) {
		double delay = s_betaIntercept;
		delay += s_betaDisruptionHour[disruptionHour];
		double nonStopEmptyAlternatives = nonStopData.getNumItineraries() *
			(1 - nonStopData.getLoadFactor());
		delay += s_betaNonStopEmptyAlternatives * nonStopEmptyAlternatives;
		if (disruptionHour < 5) {
			delay += s_betaOvernightTimeUntilMorning * (5 - disruptionHour);
		}
		if (isCancellation) {
			delay += s_betaIsCancellation;
		}
		delay += s_betaStopsRemaining * numStopsRemaining;
		double numOneStopItineraries = oneStopData.getNumItineraries();
		delay += s_betaOneStopItineraries * numOneStopItineraries;
		delay += s_betaStopsRemaining_OneStopItineraries *
			numStopsRemaining * numOneStopItineraries;
		// We should never predict a negative delay regardless of the model
		return Math.max(0.0, delay);
	}

	protected Statement createStatement()
	throws InvalidFormatException {
		Statement stmt = null;
		try {
			stmt = m_dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(DEFAULT_FETCH_SIZE);
		} catch (SQLException e) {
			throw new InvalidFormatException("Unable to create database statement", e);
		}
		return stmt;
	}

	protected static int getQuarterForMonth(int month) {
		return (int) Math.floor((month + 2) / 3.0);
	}	
}

class NonStopKey {
	String m_carrier;
	String m_origin;
	String m_destination;

	// Constructor for the key representing all carriers
	public NonStopKey(String origin, String destination) {
		m_origin = origin;
		m_destination = destination;
	}

	public NonStopKey(String carrier, String origin, String destination) {
		this(origin, destination);
		
		m_carrier = carrier;
	}

	public String getCarrier() {
		return m_carrier;
	}

	public String getOrigin() {
		return m_origin;
	}

	public String getDestination() {
		return m_destination;
	}
	
	public String toString() {
		StringBuffer keyString = new StringBuffer();
		if (m_carrier != null) {
			keyString.append(m_carrier).append(":");
		}
		keyString.append(m_origin).append("_");
		keyString.append(m_destination);
		return keyString.toString();
	}
	
	public boolean equals(Object that) {
		if (!(that instanceof NonStopKey)) {
			return false;
		}
		return toString().equals(((NonStopKey) that).toString());
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
}

class NonStopItineraryData {
	NonStopKey m_itineraryKey;
	
	double m_numItineraries;
	double m_loadFactor;
	
	public NonStopItineraryData(NonStopKey itineraryKey) {
		m_itineraryKey = itineraryKey;
	}

	public double getNumItineraries() {
		return m_numItineraries;
	}

	public void setNumItineraries(double numItineraries) {
		m_numItineraries = numItineraries;
	}
	
	public double getLoadFactor() {
		return m_loadFactor;
	}
	
	public void setLoadFactor(double loadFactor) {
		m_loadFactor = loadFactor;
	}
	
	public void addItineraryData(NonStopItineraryData data) {
		if (data != null) {
			double numNewItineraries = data.getNumItineraries();
			m_loadFactor = ((m_numItineraries * m_loadFactor) + 
					(numNewItineraries * data.getLoadFactor())) / 
					(m_numItineraries + numNewItineraries);
			m_numItineraries += numNewItineraries;
		}
	}
}

class OneStopKey {
	String m_firstCarrier;
	String m_secondCarrier;
	String m_origin;
	String m_destination;
	
	// Constructor for the key representing all carriers
	public OneStopKey(String origin, String destination) {
		m_origin = origin;
		m_destination = destination;
	}
	
	public OneStopKey(String oneCarrier, String twoCarrier, String origin, String destination) {
		this(origin, destination);
		
		if (oneCarrier.compareTo(twoCarrier) <= 0) {
			m_firstCarrier = oneCarrier;
			m_secondCarrier = twoCarrier;
		}
		else {
			m_firstCarrier = twoCarrier;
			m_secondCarrier = oneCarrier;
		}
	}

	public String getFirstCarrier() {
		return m_firstCarrier;
	}

	public String getSecondCarrier() {
		return m_secondCarrier;
	}

	public String getOrigin() {
		return m_origin;
	}

	public String getDestination() {
		return m_destination;
	}
	
	public String toString() {
		StringBuffer keyString = new StringBuffer();
		if (m_firstCarrier != null && m_secondCarrier != null) {
			keyString.append(m_firstCarrier).append("_");
			keyString.append(m_secondCarrier).append(":");
		}
		keyString.append(m_origin).append("_");
		keyString.append(m_destination);
		return keyString.toString();
	}
	
	public boolean equals(Object that) {
		if (!(that instanceof OneStopKey)) {
			return false;
		}
		return toString().equals(((OneStopKey) that).toString());
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
}

class OneStopItineraryData {
	OneStopKey m_itineraryKey;
	
	double m_numItineraries;
	
	public OneStopItineraryData(OneStopKey itineraryKey) {
		m_itineraryKey = itineraryKey;
	}

	public double getNumItineraries() {
		return m_numItineraries;
	}

	public void setNumItineraries(double numItineraries) {
		m_numItineraries = numItineraries;
	}

	public OneStopKey getItineraryKey() {
		return m_itineraryKey;
	}
	
	public void addItineraryData(OneStopItineraryData data) {
		if (data != null) {
			m_numItineraries += data.getNumItineraries();
		}
	}
}

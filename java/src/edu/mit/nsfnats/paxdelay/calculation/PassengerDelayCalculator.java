//Xu Jiao 02202017
//That took 499 minutes 

package edu.mit.nsfnats.paxdelay.calculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import oracle.jdbc.pool.OracleDataSource;

public class PassengerDelayCalculator {
	public static Logger logger = Logger
			.getLogger(PassengerDelayCalculator.class);

	public static final String PROPERTY_JDBC_URL = "JDBC_URL";
	public static final String PROPERTY_DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String PROPERTY_DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public static int DEFAULT_FETCH_SIZE = 5000;

	public static final String PROPERTY_OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";
	public static final String DEFAULT_OUTPUT_FILENAME = "ProcessedItineraryDelays.csv";
	public static final String CSV_FIELD_SEPARATOR = ",";

	public static final String PROPERTY_YEAR = "YEAR";
	public static final String PROPERTY_FIRST_DATE = "FIRST_DATE";
	public static final String PROPERTY_LAST_DATE = "LAST_DATE";
	public static final int DEFAULT_YEAR = 2007;

	public static final String PROPERTY_ITINERARIES_TABLE = "ITINERARIES_TABLE";
	public static final String DEFAULT_ITINERARIES_TABLE = "itineraries";
	public static final String PROPERTY_ITINERARY_ALLOCATIONS_TABLE = "ITINERARY_ALLOCATIONS_TABLE";
	public static final String DEFAULT_ITINERARY_ALLOCATIONS_TABLE = "itinerary_allocations";
	public static final String PROPERTY_FLIGHTS_TABLE = "FLIGHTS_TABLE";
	public static final String DEFAULT_FLIGHTS_TABLE = "flights";

	public static final String PROPERTY_CARRIER_PREFIX = "CARRIER";

	public static final String PROPERTY_DEVIATION_CAPACITY_COEFFICIENT = "DEVIATION_CAPACITY_COEFFICIENT";
	public static final double DEFAULT_DEVIATION_CAPACITY_COEFFICIENT = 1.0;

	public static final double MAXIMUM_DEFAULT_SEATING_CAPACITY = 999.0;

	public static final String NEWLINE = "\n";

	public static final String PROPERTY_MINIMUM_CONNECTION_THRESHOLD = "MINIMUM_CONNECTION_THRESHOLD";
	public static final String PROPERTY_MINIMUM_RECOVERY_TURN_TIME = "MINIMUM_RECOVERY_TURN_TIME";
	public static final double DEFAULT_MINIMUM_CONNECTION_THRESHOLD = 15.0;
	public static final int DEFAULT_MINIMUM_RECOVERY_TURN_TIME = 45;

	public static final String PROPERTY_MAXIMUM_DAYTIME_DELAY = "MAXIMUM_DAYTIME_DELAY";
	public static final String PROPERTY_MAXIMUM_EVENING_DELAY = "MAXIMUM_EVENING_DELAY";
	public static final int DEFAULT_MAXIMUM_DAYTIME_DELAY = 480;
	public static final int DEFAULT_MAXIMUM_EVENING_DELAY = 960;

	public static final String PROPERTY_ATTEMPT_ALL_CARRIERS = "ATTEMPT_ALL_CARRIERS";
	public static final boolean DEFAULT_ATTEMPT_ALL_CARRIERS = true;

	public static final int DISRUPTION_NONE = 0;
	public static final int DISRUPTION_DUE_TO_MISSED_CONNECTION = 1;
	public static final int DISRUPTION_DUE_TO_CANCELLATION = 2;

	public static final String ALL_CARRIERS_KEY = "**";

	int m_year;
	Date m_firstDate;
	int m_firstMonth;
	int m_firstDay;
	Date m_lastDate;
	int m_lastMonth;
	int m_lastDay;
	String[] m_carriers;

	String m_flightsTable;
	String m_itinerariesTable;
	String m_itineraryAllocationsTable;

	String m_jdbcURL;
	String m_dbUsername;
	String m_dbPassword;

	OracleDataSource m_datasource;
	Connection m_dbConnection;

	String m_outputDirectory;
	
	// Maps used for calculating default seating capacities
	Map<String, SegmentSeats> m_segmentSeatsMap;
	Map<String, CarrierSeats> m_carrierSeatsMap;
	Map<String, AircraftFleetSeats> m_aircraftFleetSeatsMap;

	Map<String, String[]> m_relatedCarriersMap;
	Map<Integer, InternalFlight> m_idFlightMap;
	Map<String, InternalItinerary[]> m_carrierODItinerariesMap;
	
	InternalFlight[][] m_dailyFlights;

	double m_deviationCapacityCoefficient;
	double m_minimumConnectionThreshold;
	int m_minimumRecoveryTurnTime;
	int m_maximumDaytimeDelay;
	int m_maximumEveningDelay;
	boolean m_attemptAllCarriers;

	PrintWriter m_fileWriter;

	public PassengerDelayCalculator(String outputDirectory) {
		m_outputDirectory = outputDirectory;
		m_idFlightMap = new HashMap<Integer, InternalFlight>();
		m_segmentSeatsMap = new HashMap<String, SegmentSeats>();
		m_carrierSeatsMap = new HashMap<String, CarrierSeats>();
		m_aircraftFleetSeatsMap = new HashMap<String, AircraftFleetSeats>();
		m_relatedCarriersMap = new HashMap<String, String[]>();
		m_carrierODItinerariesMap = new HashMap<String, InternalItinerary[]>();

		// Maintain the current day plus two extra days worth of flights
		m_dailyFlights = new InternalFlight[3][];
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.nanoTime();
//		if (args.length != 3) {
//			System.err
//					.println("Usage: java edu.mit.nsfnats.paxdelay.calculation.PassengerDelayCaculator "
//							+ "<logger_properties_file> <calculator_properties_file> <output_directory");
//			System.exit(-1);
//		}
//		Properties loggerProperties = null;
//		try {
//			loggerProperties = PropertiesReader.loadProperties(args[0]);
//		} catch (FileNotFoundException e) {
//			exit("Logger properties file not found.", e, -1);
//		} catch (IOException e) {
//			exit("Received IO exception while reading logger properties file.",
//					e, -1);
//		}
//		PropertyConfigurator.configure(loggerProperties);
//
//		Properties calculatorProperties = null;
//		try {
//			calculatorProperties = PropertiesReader.loadProperties(args[1]);
//		} catch (FileNotFoundException e) {
//			exit("Delay calculator properties file not found.", e, -1);
//		} catch (IOException e) {
//			exit(
//					"Received IO exception while reading delay calculator properties file.",
//					e, -1);
//		}
//		String outputDirectory = args[2];
//		logger
//				.info("Beginning PassengerDelayCalculator.main(Properties properties) execution");
//		main(calculatorProperties, outputDirectory);
//		logger
//				.info("Execution of PassengerDelayCalculator.main(Properties properties) complete");
		
		Properties loggerProperties = null;
		try {
			loggerProperties = PropertiesReader.loadProperties("resources/config/desktop/DefaultLogger.properties");
		} catch (FileNotFoundException e) {
			exit("Logger properties file not found.", e, -1);
		} catch (IOException e) {
			exit("Received IO exception while reading logger properties file.",
					e, -1);
		}
		PropertyConfigurator.configure(loggerProperties);

		Properties calculatorProperties = null;
		try {
			calculatorProperties = PropertiesReader.loadProperties("resources/config/desktop/PassengerDelayCalculatorTest.properties");
		} catch (FileNotFoundException e) {
			exit("Delay calculator properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading delay calculator properties file.",
					e, -1);
		}
		String outputDirectory = "/mdsg/paxdelay_general_Xu/";
		logger
				.info("Beginning PassengerDelayCalculator.main(Properties properties) execution");
		main(calculatorProperties, outputDirectory);
		logger
				.info("Execution of PassengerDelayCalculator.main(Properties properties) complete");
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000/1000/60;
		System.out.println("That took " + duration + " minutes ");
	}

	public static void main(Properties properties, String outputDirectory) {
		PassengerDelayCalculator pdc = new PassengerDelayCalculator(outputDirectory);
		try {
			pdc.initialize(properties);
		} catch (InvalidFormatException e) {
			exit(
					"Invalid format specified in delay calculator properties file",
					e, -1);
		}
		try {
			pdc.connectToDatabase();
			logger.trace("Connected to database");

			pdc.loadRelatedCarriers();

			pdc.loadDefaultSeatingCapacities();
			
			pdc.loadAircraftFleetTypes();

			pdc.writeOutputHeader();

			pdc.calculatePassengerDelays();

		} finally {
			pdc.disconnectFromDatabase();
			logger.trace("Disconnected from database");
		}
	}

	public void initialize(Properties properties) throws InvalidFormatException {
		File directory = new File(m_outputDirectory);
		if (!directory.exists() && !directory.mkdir()) {
			throw new InvalidFormatException("Unable to create output directory " + 
					m_outputDirectory);
		}
		m_jdbcURL = properties.getProperty(PROPERTY_JDBC_URL);
		m_dbUsername = properties.getProperty(PROPERTY_DATABASE_USERNAME);
		m_dbPassword = properties.getProperty(PROPERTY_DATABASE_PASSWORD);

		m_year = PropertiesReader.readInt(properties, PROPERTY_YEAR,
				DEFAULT_YEAR);
		try {
			Date tempDate = PropertiesReader.readDate(properties,
					PROPERTY_FIRST_DATE);
			m_firstDate = new Date(tempDate.getTime());
			Calendar firstCalendar = GregorianCalendar.getInstance();
			firstCalendar.setTime(m_firstDate);
			m_firstMonth = firstCalendar.get(Calendar.MONTH) + 1;
			m_firstDay = firstCalendar.get(Calendar.DAY_OF_MONTH);			
		} catch (ParseException e) {
			throw new InvalidFormatException(
					"Invalid format for first date string: " + m_firstDate, e);
		}
		try {
			Date tempDate = PropertiesReader.readDate(properties,
					PROPERTY_LAST_DATE);
			m_lastDate = new Date(tempDate.getTime());			
			Calendar lastCalendar = GregorianCalendar.getInstance();
			lastCalendar.setTime(m_lastDate);
			m_lastMonth = lastCalendar.get(Calendar.MONTH) + 1;
			m_lastDay = lastCalendar.get(Calendar.DAY_OF_MONTH);			
		} catch (ParseException e) {
			throw new InvalidFormatException(
					"Invalid format for last date string: " + m_lastDate, e);
		}

		m_carriers = PropertiesReader.readStrings(properties,
				PROPERTY_CARRIER_PREFIX);
		m_flightsTable = properties.getProperty(
				PROPERTY_FLIGHTS_TABLE, DEFAULT_FLIGHTS_TABLE);
		m_itinerariesTable = properties.getProperty(
				PROPERTY_ITINERARIES_TABLE, DEFAULT_ITINERARIES_TABLE);
		m_itineraryAllocationsTable = properties.getProperty(
				PROPERTY_ITINERARY_ALLOCATIONS_TABLE,
				DEFAULT_ITINERARY_ALLOCATIONS_TABLE);

		try {
			String filename = m_outputDirectory + File.separator
					+ DEFAULT_OUTPUT_FILENAME;
			m_fileWriter = new PrintWriter(new File(filename));
		} catch (IOException e) {
			exit("Unable to load output file for writing", e, -1);
		}

		m_deviationCapacityCoefficient = PropertiesReader.readDouble(
				properties, PROPERTY_DEVIATION_CAPACITY_COEFFICIENT,
				DEFAULT_DEVIATION_CAPACITY_COEFFICIENT);

		m_minimumConnectionThreshold = PropertiesReader.readDouble(properties,
				PROPERTY_MINIMUM_CONNECTION_THRESHOLD,
				DEFAULT_MINIMUM_CONNECTION_THRESHOLD);
		m_minimumRecoveryTurnTime = PropertiesReader.readInt(properties,
				PROPERTY_MINIMUM_RECOVERY_TURN_TIME,
				DEFAULT_MINIMUM_RECOVERY_TURN_TIME);

		m_maximumDaytimeDelay = PropertiesReader.readInt(properties,
				PROPERTY_MAXIMUM_DAYTIME_DELAY, DEFAULT_MAXIMUM_DAYTIME_DELAY);
		m_maximumEveningDelay = PropertiesReader.readInt(properties,
				PROPERTY_MAXIMUM_EVENING_DELAY, DEFAULT_MAXIMUM_EVENING_DELAY);
		m_attemptAllCarriers = PropertiesReader.readBoolean(properties,
				PROPERTY_ATTEMPT_ALL_CARRIERS, DEFAULT_ATTEMPT_ALL_CARRIERS);
	}

	public void connectToDatabase() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
//			m_datasource = new OracleDataSource();
//			m_datasource.setURL(m_jdbcURL);
//			m_dbConnection = m_datasource.getConnection(m_dbUsername,
//					m_dbPassword);
			m_dbConnection = DriverManager.getConnection(m_jdbcURL,m_dbUsername,
					m_dbPassword);
		} catch (SQLException e) {
			exit("Unable to connect to database " + m_jdbcURL
					+ " using username " + m_dbUsername + " and password "
					+ m_dbPassword, e, -1);
		}
	}

	public void loadRelatedCarriers() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select primary_carrier, secondary_carrier").append(
					NEWLINE);
			query.append("from related_carriers");

			logger.trace("Related carriers query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			Map<String, List<String>> relatedCarrierMap = new HashMap<String, List<String>>();
			while (rset.next()) {
				String primaryCarrier = rset.getString("primary_carrier");
				String secondaryCarrier = rset.getString("secondary_carrier");

				List<String> secondaryList = relatedCarrierMap
						.get(primaryCarrier);
				if (secondaryList == null) {
					secondaryList = new ArrayList<String>();
					relatedCarrierMap.put(primaryCarrier, secondaryList);
				}
				secondaryList.add(secondaryCarrier);
			}
			Iterator<Entry<String, List<String>>> entryIter = relatedCarrierMap
					.entrySet().iterator();
			while (entryIter.hasNext()) {
				Entry<String, List<String>> thisEntry = entryIter.next();
				String primaryCarrier = thisEntry.getKey();
				List<String> secondaryList = thisEntry.getValue();
				String[] secondaryCarriers = new String[secondaryList.size()];
				secondaryList.toArray(secondaryCarriers);
				m_relatedCarriersMap.put(primaryCarrier, secondaryCarriers);
			}
		} catch (SQLException e) {
			exit("Unable to load related carriers", e, -1);
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

	public void loadDefaultSeatingCapacities() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select carrier, origin, destination,")
					.append(NEWLINE);
			query.append("  seats_mean, seats_std_dev, departures_performed").append(NEWLINE);
			query.append("from paxdelay.t100_seats").append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and month >= ").append(m_firstMonth).append(NEWLINE);
			query.append("  and month <= ").append(m_lastMonth).append(NEWLINE);
			query.append("  and carrier in").append(NEWLINE);
			query.append("    ").append(getCarrierSetString());

			logger.trace("Default seating capacity query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");

				double mean = rset.getDouble("seats_mean");
				double stdDev = rset.getDouble("seats_std_dev");
				double defaultCapacity = mean + stdDev
						* m_deviationCapacityCoefficient;
				int departures = rset.getInt("departures_performed");

				SegmentSeats segment = new SegmentSeats(carrier, origin, destination);
				String segmentKey = getSegmentKey(carrier, origin, destination);
				m_segmentSeatsMap.put(segmentKey, segment);
				CarrierSeats carrierSeats = m_carrierSeatsMap.get(carrier);
				if (carrierSeats == null) {
					carrierSeats = new CarrierSeats(carrier);
					m_carrierSeatsMap.put(carrier, carrierSeats);
				}
				carrierSeats.addDefaultSeatingCapacity(defaultCapacity, departures);
				
			}
		} catch (SQLException e) {
			exit("Unable to load default flight seating capacities", e, -1);
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
		logger.trace("Number of segment default seating capacities = "
				+ m_segmentSeatsMap.size());
	}

	public void loadAircraftFleetTypes() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
//			04122017 XuJ: executeQuery() cannot handle sql alter table syntax
//			query.append("ALTER TABLE airline_inventories CONVERT TO CHARACTER SET latin1 COLLATE 'latin1_swedish_ci';").append(NEWLINE);
			query.append("select distinct ai.carrier, acm.icao_code,").append(NEWLINE);
			query.append("  ai.number_of_seats").append(NEWLINE);
			query.append("from airline_inventories ai").append(NEWLINE);
			query.append("join aircraft_code_mappings acm").append(NEWLINE);
			query.append("  on acm.inventory_manufacturer = ai.manufacturer").append(NEWLINE);
			query.append("  and acm.inventory_model = ai.model").append(NEWLINE);
			query.append("join asqp_carriers asqp").append(NEWLINE);
			query.append("  on asqp.code = ai.carrier").append(NEWLINE);
			query.append("where ai.number_of_seats is not null").append(NEWLINE);
			query.append("order by ai.carrier, acm.icao_code");
			

//			System.out.println(query);
			
			logger.trace("Aircraft fleet types query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());

	
			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String aircraftType = rset.getString("icao_code");
				int numSeats = rset.getInt("number_of_seats");
				
				String aircraftFleetKey = getAircraftFleetKey(carrier, aircraftType);
				AircraftFleetSeats aircraftFleet = m_aircraftFleetSeatsMap.get(aircraftFleetKey);
				if (aircraftFleet == null) {
					aircraftFleet = new AircraftFleetSeats(carrier, aircraftType);
					m_aircraftFleetSeatsMap.put(aircraftFleetKey, aircraftFleet);
				}
				aircraftFleet.registerSeatingCapacity(numSeats);
			}
		} catch (SQLException e) {
			exit("Unable to load aircraft fleet types", e, -1);
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
	
	public void writeOutputHeader() {
		m_fileWriter.print("Number_Passengers");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Original_First_Flight");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Original_Second_Flight");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("First_Disruption_Cause");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("First_Disruption_Hour");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Number_Disruptions");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Disruption_Origin_Sequence");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Disruption_Cause_Sequence");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Last_Flight_Flown");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Trip_Delay");
		m_fileWriter.println();
		m_fileWriter.flush();
	}

	public void calculatePassengerDelays() {
		Calendar itineraryCalendar = GregorianCalendar.getInstance();
		itineraryCalendar.setTime(m_firstDate);
		Calendar flightCalendar = GregorianCalendar.getInstance();
		flightCalendar.setTime(m_firstDate);
		
		// Load the first day worth of flight data
		int month = itineraryCalendar.get(Calendar.MONTH) + 1;
		int day = itineraryCalendar.get(Calendar.DAY_OF_MONTH);
		m_dailyFlights[0] = loadFlightData(month, day);
		// We need two days worth of flight data in order to
		// successfully load multiple day itineraries
		flightCalendar.add(Calendar.DAY_OF_MONTH, 1);
		if (flightCalendar.get(Calendar.YEAR) == m_year) {
			m_dailyFlights[1] = loadFlightData(flightCalendar
					.get(Calendar.MONTH) + 1, flightCalendar
					.get(Calendar.DAY_OF_MONTH));
		}
		// Load the first day worth of itinerary data
		loadItineraryData(month, day, month, day);

		while (!itineraryCalendar.getTime().after(m_lastDate)) {
			// Keep track of the month and day to process
			month = itineraryCalendar.get(Calendar.MONTH) + 1;
			day = itineraryCalendar.get(Calendar.DAY_OF_MONTH);

			// First load the flight data for the following day
			// (i.e. the processing day plus two)
			flightCalendar.add(Calendar.DAY_OF_MONTH, 1);
			if (flightCalendar.get(Calendar.YEAR) == m_year) {
				m_dailyFlights[2] = loadFlightData(flightCalendar
						.get(Calendar.MONTH) + 1, flightCalendar
						.get(Calendar.DAY_OF_MONTH));
			}
			// Then load the itinerary data for the following day
			// for the purpose of recovering passenger itineraries
			itineraryCalendar.add(Calendar.DAY_OF_MONTH, 1);
			if (itineraryCalendar.get(Calendar.YEAR) == m_year) {
				loadItineraryData(itineraryCalendar.get(Calendar.MONTH) + 1,
						itineraryCalendar.get(Calendar.DAY_OF_MONTH), month, day);
			}
			InternalItinerary[] processedItineraries = processItineraryDisruptions();
			writeProcessedItineraries(processedItineraries);

			logger.info("Processed " + processedItineraries.length
					+ " itineraries for month " + month + " day " + day);
			
			incrementFlightDate();

			// If we are tracing, perform regular garbage detection to detect
			// any unprocessed passengers
			if (logger.isTraceEnabled()) {
				System.gc();
			}
		}
		// Process any remaining itineraries (corresponding to the next two days)
		if (m_dailyFlights[0] != null) {
			InternalItinerary[] processedItineraries = processItineraryDisruptions();
			writeProcessedItineraries(processedItineraries);
			logger.info("Processed " + processedItineraries.length
					+ " remaining itineraries from last day + 1");

			incrementFlightDate();
			if (m_dailyFlights[0] != null) {
				processedItineraries = processItineraryDisruptions();
				writeProcessedItineraries(processedItineraries);
				logger.info("Processed " + processedItineraries.length
						+ " remaining itineraries from last day + 2");
			}
		}
	}

	public InternalFlight[] loadFlightData(int month, int day) {
		List<InternalFlight> flightList = new ArrayList<InternalFlight>();
	
		loadFlightDataInternal(month, day, flightList);
		
		InternalFlight[] flights = new InternalFlight[flightList.size()];
		flightList.toArray(flights);

		// Sort the updated list by time of disruption
		Arrays.sort(flights, new Comparator<InternalFlight>() {
			public int compare(InternalFlight a, InternalFlight b) {
				return a.getDisruptionTime().compareTo(b.getDisruptionTime());
			}
		});
		logger.trace("Number of flights for month " + month + " day " + day
				+ " = " + flights.length);
		return flights;
	}
	
	public void loadFlightDataInternal(int month, int day, 
			List<InternalFlight> updatedFlightList) {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			int quarter = getQuarterForMonth(month);
			StringBuffer disruptionClause = new StringBuffer();
			//OPHASWONGSE Convert query to MySQL
//			disruptionClause.append(
//					"  decode(cancelled_flag, 1, planned_departure_time,")
//					.append(NEWLINE);
//			disruptionClause.append(
//					"    decode(diverted_flag, 1, planned_departure_time,")
//					.append(NEWLINE);
//			disruptionClause.append(
//					"      nvl(actual_arrival_time, planned_arrival_time)))")
//					.append(NEWLINE);
			disruptionClause.append(
					"  case cancelled_flag when 1 then planned_departure_time")
					.append(NEWLINE);
			disruptionClause.append(
					"      else case diverted_flag when 1 then planned_departure_time")
					.append(NEWLINE);
			disruptionClause.append(
					"      else ifnull(actual_arrival_time, planned_arrival_time)")
					.append(NEWLINE);
			disruptionClause.append(
					"      end end")
					.append(NEWLINE);

			StringBuffer query = new StringBuffer();
			query.append("select id, carrier, origin, destination,").append(NEWLINE);
			query.append("  seating_capacity,").append(NEWLINE);
			query.append("  cancelled_flag, diverted_flag,").append(NEWLINE);
			query.append("  planned_departure_time,").append(NEWLINE);
			query.append("  planned_arrival_time,").append(NEWLINE);
			query.append("  actual_departure_time,").append(NEWLINE);
			query.append("  actual_arrival_time,").append(NEWLINE);
			query.append(disruptionClause);
			query.append("  as disruption_time,").append(NEWLINE);
//			query.append("  decode(cancelled_flag, 1,").append(NEWLINE);
//			query.append(
//					"    to_number(to_char(planned_departure_time, 'HH24')),")
//					.append(NEWLINE);
//			query.append("    decode(diverted_flag, 1,").append(NEWLINE);
//			query
//					.append(
//							"      to_number(to_char(planned_departure_time, 'HH24')),")
//					.append(NEWLINE);
//			query
//					.append(
//							"      to_number(to_char(nvl(actual_arrival_time, planned_arrival_time), 'HH24'))))")
//					.append(NEWLINE);
//			query.append("    as local_disruption_hour").append(NEWLINE);
			
			// XuJ 051817 local_disruption_hour should be local hour instead of UTC hour because all the flight time are in UTC timezone
//			query.append(" case cancelled_flag when 1 then  cast(date_format(planned_departure_time, 'HH24') as unsigned)").append(NEWLINE);
//			query.append(" else case diverted_flag when 1 then cast(date_format(planned_departure_time, 'HH24') as unsigned)").append(NEWLINE);
//			query.append("     else cast(date_format(ifnull(actual_arrival_time, planned_arrival_time), 'HH24') as unsigned)").append(NEWLINE);
//			query.append("     end").append(NEWLINE);
//			query.append(" end as local_disruption_hour").append(NEWLINE);
			query.append(" case cancelled_flag when 1 then  planned_departure_local_hour").append(NEWLINE);
			query.append(" else case diverted_flag when 1 then planned_departure_local_hour").append(NEWLINE);
			query.append("     else ifnull(actual_arrival_local_hour, planned_arrival_local_hour)").append(NEWLINE);
			query.append("     end").append(NEWLINE);
			query.append(" end as local_disruption_hour").append(NEWLINE);
			query.append("from paxdelay.").append(m_flightsTable).append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month).append(NEWLINE);
			query.append("  and day_of_month = ").append(day).append(NEWLINE);
			query.append("  and carrier in").append(NEWLINE);
			query.append("    ").append("(select * from asqp_carriers)").append(NEWLINE);
			query.append("order by").append(NEWLINE);
			query.append(disruptionClause);

			logger.trace("Flight data query:");
			logger.trace(query.toString());
//			System.out.println(query);
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {
				int flightID = rset.getInt("id");
				String carrier = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
//				String aircraftType = rset.getString("icao_aircraft_code");
				double seatingCapacity = rset.getDouble("seating_capacity");
				if (seatingCapacity < 1) {
					seatingCapacity = estimateDefaultSeatingCapacity(flightID, carrier,
							origin, destination);
				}
				boolean isCancelled = rset.getBoolean("cancelled_flag");
				boolean isDiverted = rset.getBoolean("diverted_flag");
				// Treat diversions as cancellations since we don't know the
				// diversion airport
				isCancelled = isCancelled || isDiverted;

				Timestamp plannedDeparture = rset
						.getTimestamp("planned_departure_time");
				Timestamp plannedArrival = rset
						.getTimestamp("planned_arrival_time");
				Timestamp actualDeparture = rset
						.getTimestamp("actual_departure_time");
				Timestamp actualArrival = rset
						.getTimestamp("actual_arrival_time");
				// If the flight is not cancelled and we don't know its actual
				// arrival time
				// we will assume that the flight arrived on time
				if (!isCancelled && actualArrival == null) {
					actualArrival = plannedArrival;
				}
				Timestamp disruptionTime = rset.getTimestamp("disruption_time");
				int localDisruptionHour = rset.getInt("local_disruption_hour");

				InternalFlight flight = new InternalFlight(flightID, carrier,
						month, day, origin, destination,
						plannedDeparture, plannedArrival,
						isCancelled, actualDeparture, actualArrival,
						disruptionTime, localDisruptionHour, seatingCapacity);

				m_idFlightMap.put(new Integer(flightID), flight);
				updatedFlightList.add(flight);
			}
		} catch (SQLException e) {
			exit("Unable to load flight data for month " + month + " day "
					+ day, e, -1);
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
	
	public void incrementFlightDate() {
		// Remove current day flights from ID map and clear the corresponding itinerary lists
		for (int i = 0; i < m_dailyFlights[0].length; ++i) {
			int flightID = m_dailyFlights[0][i].getFlightID();
			m_idFlightMap.remove(flightID);
			m_dailyFlights[0][i].clearItineraryList();
		}
		// Move the other lists up for processing
		m_dailyFlights[0] = m_dailyFlights[1];
		m_dailyFlights[1] = m_dailyFlights[2];
		m_dailyFlights[2] = null;
	}
	//OPHASWONGSE
	//Delete aircraft type from parameter 
	public double estimateDefaultSeatingCapacity(int flightID, String carrier,
			String origin, String destination) {
		double seatsEstimate = 0.0;
		SegmentSeats segmentSeats = m_segmentSeatsMap.get(getSegmentKey(carrier,
				origin, destination));
		if (segmentSeats != null && segmentSeats.getDefaultSeatingCapacity() > 0) {
			seatsEstimate = segmentSeats.getDefaultSeatingCapacity();
		} else {
			CarrierSeats carrierSeats = m_carrierSeatsMap.get(carrier);
			seatsEstimate = carrierSeats.getDefaultSeatingCapacity();
		}
		if (seatsEstimate < 1) {
			logger.warn("Default seating capacity missing for flight ID " + flightID);
			seatsEstimate = MAXIMUM_DEFAULT_SEATING_CAPACITY;
		}
		
//		AircraftFleetSeats aircraftFleetSeats = 
//			m_aircraftFleetSeatsMap.get(getAircraftFleetKey(carrier, aircraftType));
//		if (aircraftFleetSeats != null) {
//			// If we are estimating the seating capacity from T-100, we need
//			// to ensure that the seating capacity estimate is between the 
//			// minimum and maximum seating capacity for the fleet type
//			seatsEstimate = Math.max(seatsEstimate, 
//					aircraftFleetSeats.getMinimumSeatingCapacity());
//			seatsEstimate = Math.min(seatsEstimate, 
//					aircraftFleetSeats.getMaximumSeatingCapacity());
//		}
		return seatsEstimate;
	}

	public void loadItineraryData(int monthToLoad, int dayToLoad, 
			int monthToProcess, int dayToProcess) {
		// First, load all of the itineraries with passengers
		loadPassengerItineraryData(monthToLoad, dayToLoad);
		// Next, load all of the alternative itineraries for recovery
		loadAlternativesItineraryData(monthToLoad, dayToLoad, monthToProcess, dayToProcess);
	}

	public void loadPassengerItineraryData(int month, int day) {
		int numItineraries = 0;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();

			int quarter = getQuarterForMonth(month);
			StringBuffer query = new StringBuffer();
			query.append("select first_flight_id, second_flight_id,").append(
					NEWLINE);
			query.append("  first_carrier, second_carrier,").append(NEWLINE);
			query.append("  origin, destination, passengers,").append(NEWLINE);
			query.append("  planned_departure_time,").append(NEWLINE);
			query.append("  planned_arrival_time").append(NEWLINE);
			query.append("from ").append(m_itineraryAllocationsTable).append(
					NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month).append(NEWLINE);
			query.append("  and day_of_month = ").append(day).append(NEWLINE);
			query.append("  and first_carrier in").append(NEWLINE);
			query.append("    ").append(getCarrierSetString()).append(NEWLINE);
			query.append("  and (second_carrier is null or second_carrier in")
					.append(NEWLINE);
			query.append("    ").append(getCarrierSetString()).append(")");

			logger.trace("Passenger itinerary data query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {

				int firstFlightID = rset.getInt("first_flight_id");
				int secondFlightID = rset.getInt("second_flight_id");
				String firstCarrier = rset.getString("first_carrier");
				String secondCarrier = rset.getString("second_carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				double passengers = rset.getDouble("passengers");
				Timestamp plannedDeparture = rset
						.getTimestamp("planned_departure_time");
				Timestamp plannedArrival = rset
						.getTimestamp("planned_arrival_time");

				InternalItinerary itinerary = new InternalItinerary(
						firstCarrier, secondCarrier, origin, destination,
						plannedDeparture, plannedArrival, passengers);

				InternalFlight firstFlight = m_idFlightMap.get(firstFlightID);
				if (firstFlight == null) {
					logger.warn("Unable to load first flight " + firstFlightID
							+ " in itinerary for month " + month + " day "
							+ day);
					continue;
				}
				InternalFlight secondFlight = null;
				if (secondCarrier != null) {
					secondFlight = m_idFlightMap.get(secondFlightID);
					if (secondFlight == null) {
						logger.warn("Unable to load second flight "
								+ secondFlightID + " in itinerary for  month "
								+ month + " day " + day);
						continue;
					}
				}
				itinerary.setFlightSequence(firstFlight, secondFlight);
				// Add the itinerary to each of the flights in order to process
				// disruptions
				firstFlight.addItinerary(itinerary);
				if (secondFlight != null) {
					secondFlight.addItinerary(itinerary);
				}

				++numItineraries;
				if (numItineraries % 50000 == 0) {
					logger.trace("Loaded " + numItineraries
							+ " passenger itineraries for month " + month
							+ " day " + day);
				}
			}
		} catch (SQLException e) {
			exit("Unable to load passenger itinerary data for month " + month
					+ " day " + day, e, -1);
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
		logger.trace("Number of passenger itineraries for month " + month
				+ " day " + day + " = " + numItineraries);
	}

	public void loadAlternativesItineraryData(int monthToLoad, int dayToLoad, 
			int monthToProcess, int dayToProcess) {
		int numItineraries = 0;
		Statement stmt = null;
		ResultSet rset = null;
		Map<String, InternalItinerary[]> carrierODItinerariesMap = 
			new HashMap<String, InternalItinerary[]>();
		try {
			stmt = createStatement();

			int quarter = getQuarterForMonth(monthToLoad);
			StringBuffer query = new StringBuffer();
			query.append("select first_flight_id, second_flight_id,").append(
					NEWLINE);
			query
					.append(
							"  first_operating_carrier, second_operating_carrier,")
					.append(NEWLINE);
			query.append("  origin, destination,").append(NEWLINE);
			query.append("  planned_departure_time,").append(NEWLINE);
			query.append("  planned_arrival_time").append(NEWLINE);
			query.append("from paxdelay.").append(m_itinerariesTable).append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(monthToLoad).append(NEWLINE);
			query.append("  and day_of_month = ").append(dayToLoad).append(NEWLINE);
			query.append("  and first_operating_carrier in").append(NEWLINE);
			query.append("    ").append(getCarrierSetString()).append(NEWLINE);
			query
					.append(
							"  and (second_operating_carrier is null or second_operating_carrier in")
					.append(NEWLINE);
			query.append("    ").append(getCarrierSetString()).append(")")
					.append(NEWLINE);
			query.append("order by origin, destination, planned_arrival_time");

			logger.trace("Alternatives itinerary data query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			String prevCarrierODKey = null;
			List<InternalItinerary> itineraryList = null;
			while (rset.next()) {

				int firstFlightID = rset.getInt("first_flight_id");
				int secondFlightID = rset.getInt("second_flight_id");
				String firstCarrier = rset.getString("first_operating_carrier");
				String secondCarrier = rset
						.getString("second_operating_carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				Timestamp plannedDeparture = rset
						.getTimestamp("planned_departure_time");
				Timestamp plannedArrival = rset
						.getTimestamp("planned_arrival_time");

				InternalItinerary itinerary = new InternalItinerary(
						firstCarrier, secondCarrier, origin, destination,
						plannedDeparture, plannedArrival);

				InternalFlight firstFlight = m_idFlightMap.get(firstFlightID);
				if (firstFlight == null) {
					logger.warn("Unable to load first flight " + firstFlightID
							+ " in itinerary for month " + monthToLoad + " day "
							+ dayToLoad);
					continue;
				}
				InternalFlight secondFlight = null;
				if (secondCarrier != null) {
					secondFlight = m_idFlightMap.get(secondFlightID);
					if (secondFlight == null) {
						logger.warn("Unable to load second flight "
								+ secondFlightID + " in itinerary for  month "
								+ monthToLoad + " day " + dayToLoad);
						continue;
					}
				}
				itinerary.setFlightSequence(firstFlight, secondFlight);

				// Create a new itinerary list if the all
				// carriers-origin-destination key changes
				String allCarriersODKey = getCarrierODKey(null, null, origin,
						destination);
				if (!allCarriersODKey.equals(prevCarrierODKey)) {
					if (itineraryList != null) {
						addAllCarriersItineraryList(carrierODItinerariesMap,
								prevCarrierODKey, itineraryList);
					}
					itineraryList = new ArrayList<InternalItinerary>();
					prevCarrierODKey = allCarriersODKey;
				}
				itineraryList.add(itinerary);
				++numItineraries;
				if (numItineraries % 50000 == 0) {
					logger
							.trace("Loaded " + numItineraries
									+ " itineraries for month " + monthToLoad
									+ " day " + dayToLoad);
				}
			}
			// Need to add the last list of all carrier-origin-destination
			// itineraries
			if (itineraryList != null) {
				addAllCarriersItineraryList(carrierODItinerariesMap,
						prevCarrierODKey, itineraryList);
			}
		} catch (SQLException e) {
			exit("Unable to load alternatives itinerary data for month "
					+ monthToLoad + " day " + dayToLoad, e, -1);
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
		// Merge the newly loaded itineraries with the itineraries from
		// the previous iteration
		Set<String> carrierODKeySet = new HashSet<String>();
		carrierODKeySet.addAll(carrierODItinerariesMap.keySet());
		carrierODKeySet.addAll(m_carrierODItinerariesMap.keySet());

		Map<String, InternalItinerary[]> updatedCarrierODItinerariesMap = new HashMap<String, InternalItinerary[]>();
		Iterator<String> carrierODKeysIter = carrierODKeySet.iterator();
		while (carrierODKeysIter.hasNext()) {
			String carrierODKey = carrierODKeysIter.next();

			InternalItinerary[] prevItineraries = m_carrierODItinerariesMap
					.get(carrierODKey);
			InternalItinerary[] currItineraries = carrierODItinerariesMap
					.get(carrierODKey);

			InternalItinerary[] newItineraries = mergeSortAndFilter(
					prevItineraries, currItineraries, monthToProcess, dayToProcess);
			if (newItineraries.length > 0) {
				updatedCarrierODItinerariesMap
						.put(carrierODKey, newItineraries);
			}
		}
		m_carrierODItinerariesMap = updatedCarrierODItinerariesMap;

		logger.trace("Number of alternatives itineraries for month " + monthToLoad
				+ " day " + dayToLoad + " = " + numItineraries);
	}

	protected void addAllCarriersItineraryList(
			Map<String, InternalItinerary[]> carrierODItinerariesMap,
			String allCarriersODKey, List<InternalItinerary> itineraryList) {
		InternalItinerary[] itineraries = new InternalItinerary[itineraryList
				.size()];
		itineraryList.toArray(itineraries);
		carrierODItinerariesMap.put(allCarriersODKey, itineraries);
		InternalItinerary[] resortedItineraries = itineraries.clone();
		Arrays.sort(resortedItineraries, new Comparator<InternalItinerary>() {
			public int compare(InternalItinerary a, InternalItinerary b) {
				String aKey = getCarrierODKey(a.getFirstCarrier(), a
						.getSecondCarrier(), a.getOrigin(), a.getDestination());
				String bKey = getCarrierODKey(b.getFirstCarrier(), b
						.getSecondCarrier(), b.getOrigin(), b.getDestination());
				int compareValue = aKey.compareTo(bKey);
				if (compareValue != 0) {
					return compareValue;
				} else {
					return a.getPlannedArrival().compareTo(
							b.getPlannedArrival());
				}
			}
		});
		int index = 0;
		int lastIndex = 0;
		String prevKey = getCarrierODKey(resortedItineraries[0]
				.getFirstCarrier(), resortedItineraries[0].getSecondCarrier(),
				resortedItineraries[0].getOrigin(), resortedItineraries[0]
						.getDestination());
		for (; index < resortedItineraries.length; ++index) {
			String key = getCarrierODKey(resortedItineraries[index]
					.getFirstCarrier(), resortedItineraries[index]
					.getSecondCarrier(),
					resortedItineraries[index].getOrigin(),
					resortedItineraries[index].getDestination());
			if (!key.equals(prevKey)) {
				InternalItinerary[] carrierItineraries = new InternalItinerary[index
						- lastIndex];
				for (int i = 0; i < carrierItineraries.length; ++i) {
					carrierItineraries[i] = resortedItineraries[lastIndex + i];
				}
				carrierODItinerariesMap.put(prevKey, carrierItineraries);
				lastIndex = index;
				prevKey = key;
			}
		}
		// We need to add the last array of itineraries to the map
		InternalItinerary[] carrierItineraries = new InternalItinerary[index
				- lastIndex];
		for (int i = 0; i < carrierItineraries.length; ++i) {
			carrierItineraries[i] = resortedItineraries[lastIndex + i];
		}
		carrierODItinerariesMap.put(prevKey, carrierItineraries);
	}

	public InternalItinerary[] mergeSortAndFilter(
			InternalItinerary[] prevItineraries,
			InternalItinerary[] currItineraries, 
			int monthToProcess, int dayToProcess) {
		if (prevItineraries == null) {
			prevItineraries = new InternalItinerary[0];
		}
		if (currItineraries == null) {
			currItineraries = new InternalItinerary[0];
		}
		Iterator<InternalItinerary> itinerariesIter = new SortedArrayIterator(
				new InternalItinerary[][] { prevItineraries, currItineraries });
		List<InternalItinerary> mergedList = new ArrayList<InternalItinerary>();
		while (itinerariesIter.hasNext()) {
			InternalItinerary nextItinerary = itinerariesIter.next();
			InternalFlight firstFlight = nextItinerary.getFirstFlight();
			if (firstFlight.getMonth() > monthToProcess ||
					(firstFlight.getMonth() == monthToProcess && 
							firstFlight.getDay() >= dayToProcess)) {
				mergedList.add(nextItinerary);
			}
		}
		InternalItinerary[] mergedItineraries = new InternalItinerary[mergedList
				.size()];
		mergedList.toArray(mergedItineraries);
		return mergedItineraries;
	}

	public InternalItinerary[] processItineraryDisruptions() {
		List<InternalItinerary> processedList = new ArrayList<InternalItinerary>();
		for (int i = 0; i < m_dailyFlights[0].length; ++i) {
			InternalFlight currentFlight = m_dailyFlights[0][i];
			Date currentDisruptionTime = currentFlight.getDisruptionTime();
			int localDisruptionHour = currentFlight.getLocalDisruptionHour();
			if (currentFlight.isCancelled()) {
				// Process the flight cancellation disruptions
				Iterator<InternalItinerary> itinerariesIter = currentFlight
						.getItineraryIterator();
				while (itinerariesIter.hasNext()) {
					InternalItinerary itinerary = itinerariesIter.next();
					// There is no need to process itineraries without
					// passengers
					if (itinerary.getPassengers() <= 0.0) {
						continue;
					}
					InternalFlight secondFlight = itinerary.getSecondFlight();
					String disruptionOrigin = currentFlight.getOrigin();
					if (currentFlight.equals(secondFlight)) {
						InternalFlight firstFlight = itinerary.getFirstFlight();
						// If the disruption time for the first flight is after
						// the
						// current disruption time, we will recover the
						// itinerary
						// when processing the disruption for the first flight
						if (!currentDisruptionTime.after(firstFlight
								.getDisruptionTime())) {
							continue;
						}
					} else if (secondFlight != null) {
						secondFlight.removeItinerary(itinerary);
					}
					recoverItinerary(itinerary, disruptionOrigin,
							currentDisruptionTime, localDisruptionHour,
							DISRUPTION_DUE_TO_CANCELLATION, 
							processedList);
				}
			} else {
				Date thisArrival = currentFlight.getActualArrival();
				// If we don't know the actual arrival time and the flight was
				// not cancelled, use the planned arrival time instead
				if (thisArrival == null) {
					thisArrival = currentFlight.getPlannedArrival();
				}
				long actualArrivalLong = thisArrival.getTime();

				Iterator<InternalItinerary> itinerariesIter = currentFlight
						.getItineraryIterator();
				while (itinerariesIter.hasNext()) {
					InternalItinerary itinerary = itinerariesIter.next();
					// There is no need to process itineraries without
					// passengers
					if (itinerary.getPassengers() <= 0.0) {
						continue;
					}
					if (itinerary.getDestination().equals(
							currentFlight.getDestination())) {
						InternalFlight firstFlight = itinerary.getFirstFlight();
						InternalFlight secondFlight = itinerary
								.getSecondFlight();
						// Otherwise, the second flight arrives before the
						// first, which will be picked
						// up as missed connection when we process the
						// disruption for the first flight
						if (secondFlight == null
								|| currentDisruptionTime.after(firstFlight
										.getDisruptionTime())) {
							itinerary.setActualArrival(thisArrival);
							processedList.add(itinerary);
						}
					} else {
						InternalFlight secondFlight = itinerary
								.getSecondFlight();
						if (secondFlight.isCancelled()) {
							// If the first flight's arrival occurs after
							// the second flight's disruption time, we will recover
							// the passenger at the time of the first flight's arrival
							if (!currentDisruptionTime.before(secondFlight
									.getDisruptionTime())) {
								String disruptionOrigin = currentFlight
										.getDestination();
								recoverItinerary(itinerary, disruptionOrigin,
										currentDisruptionTime, localDisruptionHour,
										DISRUPTION_DUE_TO_CANCELLATION,
										processedList);
							}
						} else {
							Date nextDeparture = secondFlight
									.getActualDeparture();
							// If we don't know the actual departure time and
							// the flight
							// was not canceled, use the planned departure
							// instead
							if (nextDeparture == null) {
								nextDeparture = secondFlight
										.getPlannedDeparture();
							}
							long nextDepartureLong = nextDeparture.getTime();

							double connectionMinutes = (nextDepartureLong - actualArrivalLong)
									/ (1000.0 * 60.0);
							if (connectionMinutes < m_minimumConnectionThreshold) {
								// Process the missed connection disruption
								secondFlight.removeItinerary(itinerary);
								String disruptionOrigin = currentFlight
										.getDestination();
								recoverItinerary(itinerary, disruptionOrigin,
										currentDisruptionTime, localDisruptionHour, 
										DISRUPTION_DUE_TO_MISSED_CONNECTION,
										processedList);
							}
						}
					}
				}
			}
		}
		InternalItinerary[] processedItineraries = new InternalItinerary[processedList
				.size()];
		processedList.toArray(processedItineraries);
		return processedItineraries;
	}

	public int getMaximumDelay(int disruptionHour) {
		// Between 5:00am and 5:00pm, we set the maximum delay to the daytime
		// maximum
		if (disruptionHour >= 5 && disruptionHour < 17) {
			return m_maximumDaytimeDelay;
		}
		// At 5:00pm or later, we set the maximum delay to the evening maximum
		return m_maximumEveningDelay;
	}
	
	// By default we set the delay to the maximum delay if we are unable to
	// find a recovery alternative
	public int getDefaultDelay(Date disruptionTime, InternalItinerary itinerary, 
			String disruptionOrigin, int causeOfDisruption, int disruptionHour) {
		return getMaximumDelay(disruptionHour);
	}
	

	public void recoverItinerary(InternalItinerary itinerary,
			String disruptionOrigin, Date disruptionTime,
			int localDisruptionHour, int causeOfDisruption, 
			List<InternalItinerary> processedItineraries) {
		// The number of disrupted passengers
		double disruptedPassengers = itinerary.getPassengers();
		
		// To calculate the maximum delay when there is disruption chaining
		// we use the first (local) disruption hour
		int firstDisruptionHour = localDisruptionHour;
		if (itinerary instanceof RecoveryItinerary) {
			firstDisruptionHour = ((RecoveryItinerary) itinerary).getFirstDisruptionHour();
		}
		int maximumDelay = getMaximumDelay(firstDisruptionHour);
		int defaultDelay = getDefaultDelay(disruptionTime, itinerary, disruptionOrigin, 
				causeOfDisruption, firstDisruptionHour);

		// Calculate the earliest departure and latest arrival
		// for the recovery itineraries
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(disruptionTime);
		calendar.add(Calendar.MINUTE, m_minimumRecoveryTurnTime);
		Date earliestDeparture = calendar.getTime();
		calendar.setTime(itinerary.getPlannedArrival());
		calendar.add(Calendar.MINUTE, maximumDelay);
		Date latestArrival = calendar.getTime();

		// Retrieve the list of alternatives for this itinerary's passengers
		Iterator<InternalItinerary> alternativesIter = getAlternativeItineraryIterator(
				itinerary.getFirstCarrier(), itinerary.getSecondCarrier(),
				disruptionOrigin, itinerary.getDestination());
		// Recover this itinerary's passengers on these alternatives
		double remainingPassengers = disruptedPassengers;
		while (remainingPassengers > 0.0 && alternativesIter.hasNext()) {
			InternalItinerary alternative = alternativesIter.next();
			// Since the itinerary alternatives are ordered by planned
			// arrival time, once we hit one that arrives too late based
			// on the maximum delay (i.e. latest arrival), we can stop
			if (alternative.getPlannedArrival().after(latestArrival)) {
				break;
			}
			// If there is enough time available to recover the passenger
			// before the itinerary departs
			if (alternative.getPlannedDeparture().after(earliestDeparture)) {
				double seatsAvailable = alternative.getSeatsAvailable();
				if (seatsAvailable > 0.0) {
					double numberRecovered = Math.min(remainingPassengers,
							seatsAvailable);
					// Will be automatically added to the appropriate flight
					// lists
					new RecoveryItinerary(itinerary, alternative,
							numberRecovered, disruptionTime, localDisruptionHour,
							causeOfDisruption);
					remainingPassengers -= numberRecovered;
					// Don't add the itinerary to the processed itineraries list
					// until
					// we process the flights from the recovery itinerary
				}
			}
		}
		// If the attempt all carriers flag is set, we will try to recover
		// the remaining passengers on an unrelated carrier
		if (remainingPassengers > 0 && m_attemptAllCarriers) {
			String allCarriersODKey = getCarrierODKey(null, null,
					disruptionOrigin, itinerary.getDestination());
			InternalItinerary[] alternatives = m_carrierODItinerariesMap
					.get(allCarriersODKey);
			for (int i = 0; remainingPassengers > 0.0 && alternatives != null
					&& i < alternatives.length; ++i) {
				// Since the itinerary alternatives are ordered by planned
				// arrival time, once we hit one that arrives too late based
				// on the maximum delay (i.e. latest arrival), we can stop
				if (alternatives[i].getPlannedArrival().after(latestArrival)) {
					break;
				}
				// If there is enough time available to recover the passenger
				// before the itinerary departs
				if (alternatives[i].getPlannedDeparture().after(
						earliestDeparture)) {
					double seatsAvailable = alternatives[i].getSeatsAvailable();
					if (seatsAvailable > 0.0) {
						double numberRecovered = Math.min(remainingPassengers,
								seatsAvailable);
						// Will be automatically added to the appropriate flight
						// lists
						new RecoveryItinerary(itinerary, alternatives[i],
								numberRecovered, disruptionTime, localDisruptionHour,
								causeOfDisruption);
						remainingPassengers -= numberRecovered;
						// Don't add the itinerary to the processed itineraries
						// list until
						// we process the flights from the recovery itinerary
					}
				}
			}
		}
		// Last, if there are no available itineraries, cap the itinerary delay
		// at the maximum delay value based on the time of disruption
		if (remainingPassengers > 0.0) {
			InternalItinerary unknownRecovery = new RecoveryItinerary(
					itinerary, disruptionOrigin, remainingPassengers,
					disruptionTime, localDisruptionHour, causeOfDisruption, defaultDelay);
			processedItineraries.add(unknownRecovery);
		}
	}

	// Returns a list of potential alternative itineraries ordered
	// by the planned arrival time of the alternative
	public Iterator<InternalItinerary> getAlternativeItineraryIterator(
			String firstCarrier, String secondCarrier, String origin,
			String destination) {
		List<InternalItinerary[]> optionsList = new ArrayList<InternalItinerary[]>();
		String carrierKey = getCarrierODKey(firstCarrier, secondCarrier,
				origin, destination);
		InternalItinerary[] options = m_carrierODItinerariesMap.get(carrierKey);
		// We need to check for null to cover the case where there is
		// a disruption in the middle of the passenger's itinerary
		if (options != null) {
			optionsList.add(options);
		}

		// If the original itinerary is multiple carrier, then we should check
		// if either of the carriers involved offers alternatives individually.
		// This implicitly covers the case where the disruption occurs in the
		// middle of a multiple carrier itinerary.
		if (secondCarrier != null && !secondCarrier.equals(firstCarrier)) {
			// See if the first carrier offers any single carrier itineraries
			String firstKey = getCarrierODKey(firstCarrier, null, origin,
					destination);
			InternalItinerary[] optionsFirst = m_carrierODItinerariesMap
					.get(firstKey);
			if (optionsFirst != null) {
				optionsList.add(optionsFirst);
			}
			// See if the second carrier offers any single carrier itineraries
			String secondKey = getCarrierODKey(secondCarrier, null, origin,
					destination);
			InternalItinerary[] optionsSecond = m_carrierODItinerariesMap
					.get(secondKey);
			if (optionsSecond != null) {
				optionsList.add(optionsSecond);
			}
		}
		// We should also check sub-contracted carrier and parent carriers
		// for each of the two carriers specified to see if there are
		// single or multiple carrier itineraries available
		addRelatedCarrierOptions(firstCarrier, secondCarrier, origin,
				destination, optionsList);
		if (secondCarrier != null && !secondCarrier.equals(firstCarrier)) {
			addRelatedCarrierOptions(secondCarrier, firstCarrier, origin,
					destination, optionsList);
		}
		InternalItinerary[][] optionsArrays = new InternalItinerary[optionsList
				.size()][];
		optionsList.toArray(optionsArrays);

		return new SortedArrayIterator(optionsArrays);
	}

	public void addRelatedCarrierOptions(String carrier, String excludeCarrier,
			String origin, String destination,
			List<InternalItinerary[]> optionsList) {
		String[] relatedCarriers = m_relatedCarriersMap.get(carrier);
		// If there are no related carriers, there will be no entry
		// in the related carriers map
		if (relatedCarriers == null) {
			return;
		}
		for (int i = 0; i < relatedCarriers.length; ++i) {
			// Skip the excluded carrier
			if (excludeCarrier != null
					&& relatedCarriers[i].equals(excludeCarrier)) {
				continue;
			}
			// Add single carrier itineraries
			String relatedKey = getCarrierODKey(relatedCarriers[i], null,
					origin, destination);
			InternalItinerary[] relatedOptions = m_carrierODItinerariesMap
					.get(relatedKey);
			if (relatedOptions != null) {
				optionsList.add(relatedOptions);
			}
			// Add multiple carrier itineraries
			String pairedKey = getCarrierODKey(carrier, relatedCarriers[i],
					origin, destination);
			relatedOptions = m_carrierODItinerariesMap.get(pairedKey);
			if (relatedOptions != null) {
				optionsList.add(relatedOptions);
			}
			// Note that we do not consider the pairwise closure of related
			// carriers
			// as that would dramatically increase the set of options to
			// consider
		}
	}

	public void writeProcessedItineraries(
			InternalItinerary[] processedItineraries) {
		for (int i = 0; i < processedItineraries.length; ++i) {
			InternalFlight firstFlight = processedItineraries[i].getFirstFlight();
			if (processedItineraries[i] instanceof RecoveryItinerary) {
				RecoveryItinerary recoveryItinerary = (RecoveryItinerary) processedItineraries[i];
				InternalItinerary originalItinerary = recoveryItinerary.getOriginalItinerary();
				while (originalItinerary instanceof RecoveryItinerary) {
					recoveryItinerary = (RecoveryItinerary) originalItinerary;
					originalItinerary = recoveryItinerary.getOriginalItinerary();
				}
				firstFlight = originalItinerary.getFirstFlight();
			}
			if (firstFlight.getMonth() < m_lastMonth || 
					(firstFlight.getMonth() == m_lastMonth && firstFlight.getDay() <= m_lastDay)) {
				writeProcessedItinerary(processedItineraries[i]);
			}
		}
		m_fileWriter.flush();
	}
	
	public void writeProcessedItinerary(InternalItinerary itinerary) {
		itinerary.writeItinerary(m_fileWriter);		
	}

	public void disconnectFromDatabase() {
		try {
			m_dbConnection.close();
//			m_datasource.close();
		} catch (SQLException e) {
			logger.fatal("Unable to disconnect from database", e);
		}
	}

	protected static void exit(String message, Exception e, int code) {
		logger.fatal(message, e);
		System.exit(code);
	}

	protected Statement createStatement() {
		Statement stmt = null;
		try {
			stmt = m_dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(DEFAULT_FETCH_SIZE);
		} catch (SQLException e) {
			exit("Unable to create database statement", e, -1);
		}
		return stmt;
	}

	protected static String getSegmentKey(String carrier, String origin,
			String destination) {
		StringBuffer key = new StringBuffer(carrier);
		key.append(".").append(origin).append(".").append(destination);
		return key.toString();
	}
	
	protected static String getAircraftFleetKey(String carrier, String aircraftType) {
		return carrier + aircraftType;
	}
	

	protected String getCarrierSetString() {
		StringBuffer carrierSetString = new StringBuffer("(");
		for (int i = 0; i < m_carriers.length; ++i) {
			if (i > 0) {
				carrierSetString.append(",");
			}
			carrierSetString.append("'").append(m_carriers[i]).append("'");
		}
		carrierSetString.append(")");
		return carrierSetString.toString();
	}

	protected static int getQuarterForMonth(int month) {
		return (int) Math.floor((month + 2) / 3.0);
	}

	// The key determines who is responsible for the itinerary. Thus, for a
	// multiple
	// carrier itinerary we want CO, XE to be the same as XE, CO. Thus, the key
	// returned
	// does not depend on which carrier is specified first and which is
	// specified second.
	protected static String getCarrierODKey(String firstCarrier,
			String secondCarrier, String origin, String destination) {
		StringBuffer carrierODKey = new StringBuffer();
		if (firstCarrier == null) {
			if (secondCarrier != null) {
				carrierODKey.append(secondCarrier);
			} else {
				carrierODKey.append(ALL_CARRIERS_KEY);
			}
		} else if (secondCarrier != null && !secondCarrier.equals(firstCarrier)) {
			if (firstCarrier.compareTo(secondCarrier) < 0) {
				carrierODKey.append(firstCarrier);
				carrierODKey.append("_").append(secondCarrier);
			} else {
				carrierODKey.append(secondCarrier);
				carrierODKey.append("_").append(firstCarrier);
			}
		} else {
			carrierODKey.append(firstCarrier);
		}
		carrierODKey.append(".").append(origin);
		carrierODKey.append(".").append(destination);
		return carrierODKey.toString();
	}

}

class InternalFlight {

	int m_flightID;
	String m_carrier;
	int m_month;
	int m_day;
	String m_origin;
	String m_destination;
	Date m_plannedDeparture;
	Date m_plannedArrival;
	boolean m_isCancelled;
	Date m_actualDeparture;
	Date m_actualArrival;
	Date m_disruptionTime;
	int m_localDisruptionHour;

	double m_seatingCapacity;
	double m_passengers;
	List<InternalItinerary> m_itineraryList;

	public InternalFlight(int flightID, String carrier,
			int month, int day,
			String origin, String destination, 
			Date plannedDeparture, Date plannedArrival,
			boolean isCancelled,
			Date actualDeparture, Date actualArrival,
			Date disruptionTime, int localDisruptionHour,
			double seatingCapacity) {
		m_flightID = flightID;
		m_carrier = carrier;
		m_month = month;
		m_day = day;
		m_origin = origin;
		m_destination = destination;
		m_plannedDeparture = plannedDeparture;
		m_plannedArrival = plannedArrival;
		m_isCancelled = isCancelled;
		m_actualDeparture = actualDeparture;
		m_actualArrival = actualArrival;
		m_disruptionTime = disruptionTime;
		m_localDisruptionHour = localDisruptionHour;

		m_seatingCapacity = seatingCapacity;
		m_passengers = 0;
		m_itineraryList = new ArrayList<InternalItinerary>();
	}

	public int getFlightID() {
		return m_flightID;
	}

	public String getCarrier() {
		return m_carrier;
	}
	
	public int getMonth() {
		return m_month;
	}
	
	public int getDay() {
		return m_day;
	}

	public String getOrigin() {
		return m_origin;
	}

	public String getDestination() {
		return m_destination;
	}

	public Date getPlannedDeparture() {
		return m_plannedDeparture;
	}

	public Date getPlannedArrival() {
		return m_plannedArrival;
	}

	public boolean isCancelled() {
		return m_isCancelled;
	}

	public Date getActualDeparture() {
		return m_actualDeparture;
	}

	public Date getActualArrival() {
		return m_actualArrival;
	}

	public Date getDisruptionTime() {
		return m_disruptionTime;
	}

	public int getLocalDisruptionHour() {
		return m_localDisruptionHour;
	}

	public void addItinerary(InternalItinerary itinerary) {
		m_itineraryList.add(itinerary);
		m_passengers += itinerary.getPassengers();
	}

	public void removeItinerary(InternalItinerary itinerary) {
		if (m_itineraryList != null) {
			m_itineraryList.remove(itinerary);
		}
		m_passengers -= itinerary.getPassengers();
	}

	public Iterator<InternalItinerary> getItineraryIterator() {
		return m_itineraryList.iterator();
	}

	public double getSeatsAvailable() {
		return m_seatingCapacity - m_passengers;
	}

	public void clearItineraryList() {
		m_itineraryList = null;
	}
}

class InternalItinerary {
	public static Logger logger = Logger
			.getLogger(PassengerDelayCalculator.class);

	String m_firstCarrier;
	String m_secondCarrier;
	String m_origin;
	String m_destination;
	Date m_plannedDeparture;
	Date m_plannedArrival;
	Date m_actualArrival;
	double m_passengers;
	double m_delay;
	double m_numProcessed;
	double m_numWritten;

	InternalFlight m_firstFlight;
	InternalFlight m_secondFlight;

	public InternalItinerary(String firstCarrier, String secondCarrier,
			String origin, String destination, Date plannedDeparture,
			Date plannedArrival) {
		this(firstCarrier, secondCarrier, origin, destination,
				plannedDeparture, plannedArrival, 0.0);
	}

	public InternalItinerary(String firstCarrier, String secondCarrier,
			String origin, String destination, Date plannedDeparture,
			Date plannedArrival, double passengers) {
		m_firstCarrier = firstCarrier;
		m_secondCarrier = secondCarrier;
		m_origin = origin;
		m_destination = destination;
		m_plannedDeparture = plannedDeparture;
		m_plannedArrival = plannedArrival;
		m_passengers = passengers;
		m_delay = 0.0;
		m_numProcessed = 0.0;
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

	public Date getPlannedDeparture() {
		return m_plannedDeparture;
	}

	public Date getPlannedArrival() {
		return m_plannedArrival;
	}

	public Date getActualArrival() {
		return m_actualArrival;
	}

	public void setActualArrival(Date actualArrival) {
		m_actualArrival = actualArrival;
		long plannedArrivalLong = m_plannedArrival.getTime();
		long actualArrivalLong = m_actualArrival.getTime();
		setDelay(Math.max((actualArrivalLong - plannedArrivalLong)
				/ (1000 * 60), 0.0));
	}

	public double getPassengers() {
		return m_passengers;
	}

	public void setDelay(double delay) {
		m_delay = delay;
		markProcessed(m_passengers);
	}

	public double getDelay() {
		return m_delay;
	}

	public void setFlightSequence(InternalFlight firstFlight,
			InternalFlight secondFlight) {
		m_firstFlight = firstFlight;
		m_secondFlight = secondFlight;
	}

	public InternalFlight getFirstFlight() {
		return m_firstFlight;
	}

	public InternalFlight getSecondFlight() {
		return m_secondFlight;
	}

	public double getSeatsAvailable() {
		double seatsAvailable = m_firstFlight.getSeatsAvailable();
		if (m_secondFlight != null) {
			seatsAvailable = Math.min(seatsAvailable, m_secondFlight
					.getSeatsAvailable());
		}
		return seatsAvailable;
	}

	protected void markProcessed(double numPassengers) {
		m_numProcessed += numPassengers;
		if (m_numProcessed > m_passengers) {
			logger.warn("Processing " + numPassengers + " passengers causes "
					+ m_numProcessed + " passengers processed to exceed the "
					+ m_passengers + " passengers");
		}
	}

	protected void markWritten(double numPassengers) {
		m_numWritten += numPassengers;
		if (m_numWritten > m_passengers) {
			logger.warn("Writing " + numPassengers + " passengers causes "
					+ m_numWritten + " passengers written to exceed the "
					+ m_passengers + " passengers");
		}
	}

	public void writeItinerary(PrintWriter writer) {
		double numPassengers = getPassengers();
		// Number_Passengers
		writer.print(numPassengers);
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Original_First_Flight_ID
		writer.print(m_firstFlight.getFlightID());
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Original_Second_Flight_ID
		if (m_secondFlight != null) {
			writer.print(m_secondFlight.getFlightID());
		}
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// First_Disruption_Cause
		writer.print(PassengerDelayCalculator.DISRUPTION_NONE);
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// First_Disruption_Hour
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Number_Disruptions
		writer.print(0);
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Disruption_Origin_Sequence
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Disruption_Cause_Sequence
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Last_Flight_Flown
		if (m_secondFlight != null) {
			writer.print(m_secondFlight.getFlightID());
		}
		else {
			writer.print(m_firstFlight.getFlightID());
		}
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Trip_Delay
		writer.print(getDelay());
		writer.println();
		markWritten(numPassengers);
	}

	protected void finalize() throws Throwable {
		if (m_numProcessed < m_passengers) {
			logger.warn("Itinerary " + this + " has "
					+ (m_passengers - m_numProcessed)
					+ " unprocessed passengers out of " + m_passengers);
		}
		if (m_numWritten < m_passengers) {
			logger.warn("Itinerary " + this + " has "
					+ (m_passengers - m_numWritten)
					+ " unwritten passengers out of " + m_passengers);
		}
	}

	public String toString() {
		StringBuffer itineraryString = new StringBuffer();
		itineraryString.append("[").append(m_firstFlight.getFlightID());
		itineraryString.append(", ");
		if (m_secondFlight != null) {
			itineraryString.append(m_secondFlight.getFlightID());
		}
		itineraryString.append("]");
		return itineraryString.toString();
	}
}

class RecoveryItinerary extends InternalItinerary {
	InternalItinerary m_originalItinerary;

	Date m_disruptionTime;
	int m_firstDisruptionHour;
	int m_numberDisruptions;
	int m_causeOfDisruption;

	public RecoveryItinerary(InternalItinerary originalItinerary,
			InternalItinerary recoveryItinerary, double numberPassengers,
			Date disruptionTime, int localDisruptionHour, int causeOfDisruption) {

		this(originalItinerary, recoveryItinerary.getFirstCarrier(),
				recoveryItinerary.getSecondCarrier(), recoveryItinerary
						.getOrigin(), numberPassengers, disruptionTime,
				localDisruptionHour, causeOfDisruption);

		InternalFlight firstFlight = recoveryItinerary.getFirstFlight();
		InternalFlight secondFlight = recoveryItinerary.getSecondFlight();
		setFlightSequence(firstFlight, secondFlight);

		// Since we will need to process this itinerary, add it to each
		// of the flights
		firstFlight.addItinerary(this);
		if (secondFlight != null) {
			secondFlight.addItinerary(this);
		}
	}

	public RecoveryItinerary(InternalItinerary originalItinerary,
			String disruptionOrigin, double numberPassengers,
			Date disruptionTime, int localDisruptionHour,
			int causeOfDisruption, double delay) {

		this(originalItinerary, null, null, disruptionOrigin, numberPassengers,
				disruptionTime, localDisruptionHour, causeOfDisruption);

		setDelay(delay);
	}

	protected RecoveryItinerary(InternalItinerary originalItinerary,
			String recoveryFirstCarrier, String recoverySecondCarrier,
			String recoveryOrigin, double passengers, Date disruptionTime,
			int localDisruptionHour, int causeOfDisruption) {

		super(recoveryFirstCarrier, recoverySecondCarrier, recoveryOrigin,
				originalItinerary.getDestination(), originalItinerary
						.getPlannedDeparture(), originalItinerary
						.getPlannedArrival(), passengers);

		m_originalItinerary = originalItinerary;
		m_numberDisruptions = 1;
		if (m_originalItinerary instanceof RecoveryItinerary) {
			RecoveryItinerary parent = (RecoveryItinerary) m_originalItinerary;
			m_numberDisruptions += parent.getNumberDisruptions();
			m_firstDisruptionHour = parent.getFirstDisruptionHour();
		}
		else {
			m_firstDisruptionHour = localDisruptionHour;
		}
		m_disruptionTime = disruptionTime;
		m_causeOfDisruption = causeOfDisruption;
	}

	public InternalItinerary getOriginalItinerary() {
		return m_originalItinerary;
	}

	protected void markProcessed(double numPassengers) {
		super.markProcessed(numPassengers);
		if (m_originalItinerary != null) {
			m_originalItinerary.markProcessed(numPassengers);
		}
	}

	protected void markWritten(double numPassengers) {
		super.markWritten(numPassengers);
		if (m_originalItinerary != null) {
			m_originalItinerary.markWritten(numPassengers);
		}
	}

	public int getNumberDisruptions() {
		return m_numberDisruptions;
	}

	public Date getDisruptionTime() {
		return m_disruptionTime;
	}
	
	public int getFirstDisruptionHour() {
		return m_firstDisruptionHour;
	}

	public int getCauseOfDisruption() {
		return m_causeOfDisruption;
	}

	public void writeItinerary(PrintWriter writer) {
		StringBuffer originSequence = new StringBuffer();
		StringBuffer causeSequence = new StringBuffer();
		InternalItinerary[] itinerarySequence = getRecoverySequence();
		for (int i = 1; i < itinerarySequence.length; ++i) {
			RecoveryItinerary recoveryItinerary = (RecoveryItinerary) itinerarySequence[i];
			if (i > 1) {
				originSequence.append(";");
				causeSequence.append(";");
			}
			originSequence.append(recoveryItinerary.getOrigin());
			causeSequence.append(recoveryItinerary.getCauseOfDisruption());
		}
		double numPassengers = getPassengers();
		// Number_Passengers
		writer.print(numPassengers);
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Original_First_Flight_ID
		writer.print(itinerarySequence[0].getFirstFlight().getFlightID());
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Original_Second_Flight_ID
		InternalFlight secondFlight = itinerarySequence[0].getSecondFlight();
		if (secondFlight != null) {
			writer.print(secondFlight.getFlightID());
		}
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// First_Disruption_Cause
		writer.print(((RecoveryItinerary) itinerarySequence[1])
				.getCauseOfDisruption());
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// First_Disruption_Hour
		writer.print(getFirstDisruptionHour());
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Number_Disruptions
		writer.print(getNumberDisruptions());
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Disruption_Origin_Sequence
		writer.print(originSequence.toString());
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Disruption_Cause_Sequence
		writer.print(causeSequence.toString());
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Last_Flight_Flown
		// If the trip delay has been defaulted to the maximum value, the last flight
		// flown will equal null
		InternalItinerary lastItinerary = itinerarySequence[itinerarySequence.length - 1];
		if (lastItinerary.getSecondFlight() != null) {
			writer.print(lastItinerary.getSecondFlight().getFlightID());
		}
		else if (lastItinerary.getFirstFlight() != null) {
			writer.print(lastItinerary.getFirstFlight().getFlightID());
		}
		writer.print(PassengerDelayCalculator.CSV_FIELD_SEPARATOR);
		// Trip_Delay		
		writer.print(getDelay());
		writer.println();
		markWritten(numPassengers);
	}

	protected InternalItinerary[] getRecoverySequence() {
		InternalItinerary[] itinerarySequence = new InternalItinerary[getNumberDisruptions() + 1];
		int i = itinerarySequence.length - 1;
		itinerarySequence[i--] = this;
		for (; i >= 0; --i) {
			itinerarySequence[i] = ((RecoveryItinerary) itinerarySequence[i + 1])
					.getOriginalItinerary();
		}
		return itinerarySequence;
	}
}

class SortedArrayIterator implements Iterator<InternalItinerary> {
	InternalItinerary[][] m_itineraryArrays;
	int[] m_indices;

	public SortedArrayIterator(InternalItinerary[][] itineraries) {
		m_itineraryArrays = itineraries;
		m_indices = new int[itineraries.length];
		resetIndices();
	}

	protected void resetIndices() {
		for (int i = 0; i < m_indices.length; ++i) {
			m_indices[i] = 0;
		}
	}

	public InternalItinerary next() {
		InternalItinerary[] itinerariesToCompare = new InternalItinerary[m_indices.length];
		for (int i = 0; i < m_indices.length; ++i) {
			if (m_indices[i] < m_itineraryArrays[i].length) {
				itinerariesToCompare[i] = m_itineraryArrays[i][m_indices[i]];
			}
		}
		int minIndex = getMinimumIndex(itinerariesToCompare);
		++m_indices[minIndex];
		return itinerariesToCompare[minIndex];
	}

	protected int getMinimumIndex(InternalItinerary[] itineraries) {
		int minIndex = -1;
		int currIndex = 0;
		for (; currIndex < itineraries.length; ++currIndex) {
			if (itineraries[currIndex] != null) {
				minIndex = currIndex;
				break;
			}
		}
		for (; currIndex < itineraries.length; ++currIndex) {
			if (itineraries[currIndex] != null
					&& itineraries[currIndex].getPlannedArrival().before(
							itineraries[minIndex].getPlannedArrival())) {
				minIndex = currIndex;
			}
		}
		return minIndex;
	}

	public boolean hasNext() {
		for (int i = 0; i < m_indices.length; ++i) {
			if (m_indices[i] < m_itineraryArrays[i].length) {
				return true;
			}
		}
		return false;
	}

	public void remove() {
		throw new java.lang.UnsupportedOperationException(
				"SortedArrayIterator.remove() unsupported");
	}
}

class CarrierSeats {
	String m_carrier;
	
	double m_defaultSeats;
	int m_departures;

	public CarrierSeats(String carrier) {
		m_carrier = carrier;
	}

	public String getCarrier() {
		return m_carrier;
	}
	
	public void resetDefaultSeatingCapacity() {
		m_defaultSeats = 0.0;
		m_departures = 0;
	}
	
	public void addDefaultSeatingCapacity(double mean, int departures) {
		m_defaultSeats = (m_defaultSeats * m_departures + mean * departures) /
			(m_departures + departures);
		m_departures = m_departures + departures;
	}
	
	public double getDefaultSeatingCapacity() {
		return m_defaultSeats;
	}
}

class SegmentSeats {
	String m_carrier;
	String m_origin;
	String m_destination;
	double m_defaultSeats;

	public SegmentSeats(String carrier, String origin, String destination) {
		m_carrier = carrier;
		m_origin = origin;
		m_destination = destination;
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

	public void setDefaultSeatingCapacity(double seats) {
		m_defaultSeats = seats;
	}

	public double getDefaultSeatingCapacity() {
		return m_defaultSeats;
	}
}

class AircraftFleetSeats {
	String m_carrier;
	String m_aircraftType;
	
	int m_minimumSeatingCapacity;
	int m_maximumSeatingCapacity;
	
	public AircraftFleetSeats(String carrier, String aircraftType) {
		m_carrier = carrier;
		m_aircraftType = aircraftType;
		
		m_minimumSeatingCapacity = Integer.MAX_VALUE;
		m_maximumSeatingCapacity = Integer.MIN_VALUE;
	}

	public String getCarrier() {
		return m_carrier;
	}

	public String getAircraftType() {
		return m_aircraftType;
	}

	public int getMinimumSeatingCapacity() {
		return m_minimumSeatingCapacity;
	}

	public int getMaximumSeatingCapacity() {
		return m_maximumSeatingCapacity;
	}
	
	public void registerSeatingCapacity(int seatingCapacity) {
		m_minimumSeatingCapacity = Math.min(m_minimumSeatingCapacity, seatingCapacity);
		m_maximumSeatingCapacity = Math.max(m_maximumSeatingCapacity, seatingCapacity);
	}
}

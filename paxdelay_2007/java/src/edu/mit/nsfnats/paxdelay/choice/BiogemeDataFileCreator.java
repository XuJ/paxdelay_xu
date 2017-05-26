package edu.mit.nsfnats.paxdelay.choice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;

public class BiogemeDataFileCreator {
	public static Logger logger = Logger
			.getLogger(BiogemeDataFileCreator.class);

	public static final String PROPERTY_JDBC_URL = "JDBC_URL";
	public static final String PROPERTY_DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String PROPERTY_DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public static int DEFAULT_FETCH_SIZE = 50000;

	public static final String PROPERTY_BIOGEME_DATA_DIRECTORY = "BIOGEME_DATA_DIRECTORY";
	public static final String PROPERTY_BIOGEME_DATA_FILENAME = "BIOGEME_DATA_FILENAME";

	public static final String PROPERTY_YEAR_PREFIX = "YEAR";
	public static final String PROPERTY_QUARTER_PREFIX = "QUARTER";
	public static final String PROPERTY_CARRIER_PREFIX = "CARRIER";

	public static final String PROPERTY_MAXIMUM_CONNECTION_TIME = "MAXIMUM_CONNECTION_TIME";
	public static final int DEFAULT_MAXIMUM_CONNECTION_TIME = 300;
	public static final String PROPERTY_INCLUDE_GENERATED_ITINERARIES = "INCLUDE_GENERATED_ITINERARIES";
	public static final boolean DEFAULT_INCLUDE_GENERATED_ITINERARIES = false;
	// Minimum connection time for including generated itineraries
	public static final String PROPERTY_MINIMUM_CONNECTION_TIME = "MINIMUM_CONNECTION_TIME";
	public static final int DEFAULT_MINIMUM_CONNECTION_TIME = 30;

	public static final String NEWLINE = "\n";

	ObservationSet m_observationSet;
	Set<Integer> m_includedFlights;

	Map<String, Double> m_defaultCarrierCapacities;
	Map<String, Map<String, Double>> m_defaultSegmentCapacities;
	Map<Integer, FlightInformation> m_flightInfo;

	String m_jdbcURL;
	String m_dbUsername;
	String m_dbPassword;

	OracleDataSource m_datasource;
	Connection m_dbConnection;

	String m_biogemeDataDirectory;
	String m_biogemeDataFilename;

	String[] m_carriers;
	int[] m_years;
	int[] m_quarters;

	boolean m_includeGeneratedItineraries;
	int m_minimumConnectionTime;
	int m_maximumConnectionTime;	

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err
					.println("Usage: java edu.mit.nsfnats.paxdelay.choice.BiogemeDataFileCreator <logger_properties_file> <data_file_properties_file>");
			System.exit(-1);
		}
		Properties loggerProperties = null;
		try {
			loggerProperties = PropertiesReader.loadProperties(args[0]);
		} catch (FileNotFoundException e) {
			exit("Logger properties file not found.", e, -1);
		} catch (IOException e) {
			exit("Received IO exception while reading logger properties file.",
					e, -1);
		}
		PropertyConfigurator.configure(loggerProperties);

		Properties dataProperties = null;
		try {
			dataProperties = PropertiesReader.loadProperties(args[1]);
		} catch (FileNotFoundException e) {
			exit("Data properties file not found.", e, -1);
		} catch (IOException e) {
			exit("Received IO exception while reading data properties file.",
					e, -1);
		}
		main(dataProperties);
	}

	public static void main(Properties properties) {
		BiogemeDataFileCreator fileCreator = new BiogemeDataFileCreator();
		try {
			fileCreator.initialize(properties);
		} catch (InvalidFormatException e) {
			exit("Invalid format specified in data properties file", e, -1);
		}

		try {
			fileCreator.connectToDatabase();
			fileCreator.queryDefaulSeatingCapacities();
			fileCreator.queryFlightInfo();
			fileCreator.queryAirlineItineraries();
			fileCreator.queryGeneratedItineraries();
		} finally {
			fileCreator.disconnectFromDatabase();
			logger.trace("Disconnected from database");
		}

		try {
			fileCreator.writeBiogemeDataFile();
		} catch (FileNotFoundException e) {
			exit("Unable to write to Biogeme data file", e, -1);
		}
	}

	protected static void exit(String message, Exception e, int code) {
		logger.fatal(message, e);
		System.exit(code);
	}

	public void initialize(Properties properties) throws InvalidFormatException {
		m_jdbcURL = properties.getProperty(PROPERTY_JDBC_URL);
		m_dbUsername = properties.getProperty(PROPERTY_DATABASE_USERNAME);
		m_dbPassword = properties.getProperty(PROPERTY_DATABASE_PASSWORD);

		m_years = PropertiesReader.readInts(properties, PROPERTY_YEAR_PREFIX);
		m_quarters = PropertiesReader.readInts(properties,
				PROPERTY_QUARTER_PREFIX);
		m_carriers = PropertiesReader.readStrings(properties,
				PROPERTY_CARRIER_PREFIX);

		m_observationSet = new ObservationSet();
		m_observationSet.initialize(properties);

		m_biogemeDataDirectory = properties
				.getProperty(PROPERTY_BIOGEME_DATA_DIRECTORY);
		File validationDirectory = new File(m_biogemeDataDirectory);
		if (!validationDirectory.exists()) {
			validationDirectory.mkdir();
		}
		m_biogemeDataFilename = properties
				.getProperty(PROPERTY_BIOGEME_DATA_FILENAME);

		m_maximumConnectionTime = PropertiesReader.readInt(properties, 
				PROPERTY_MAXIMUM_CONNECTION_TIME,
				DEFAULT_MAXIMUM_CONNECTION_TIME);
		m_includeGeneratedItineraries = PropertiesReader.readBoolean(
				properties, PROPERTY_INCLUDE_GENERATED_ITINERARIES,
				DEFAULT_INCLUDE_GENERATED_ITINERARIES);
		m_minimumConnectionTime = PropertiesReader.readInt(properties,
				PROPERTY_MINIMUM_CONNECTION_TIME,
				DEFAULT_MINIMUM_CONNECTION_TIME);
		m_includedFlights = new HashSet<Integer>();
		m_defaultCarrierCapacities = new HashMap<String, Double>();
		m_defaultSegmentCapacities = new HashMap<String, Map<String, Double>>();
		m_flightInfo = new HashMap<Integer, FlightInformation>();
	}

	public void queryDefaulSeatingCapacities() {
		Statement statement = null;
		ResultSet results = null;

		try {
			statement = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select month, carrier, origin, destination,").append(NEWLINE);
			query.append("  departures_performed, seats_mean").append(NEWLINE);
			query.append("from t100_seats").append(NEWLINE);
			query.append("where carrier in").append(NEWLINE);
			query.append("  ").append(getCarrierClause()).append(NEWLINE);
		
			logger.trace("Default seating capacity query:");
			logger.trace(query.toString());

			Map<String, Integer> carrierDepartures = new HashMap<String, Integer>();
			results = statement.executeQuery(query.toString());
			while (results.next()) {
				int month = results.getInt("month");
				String carrier = results.getString("carrier");
				String origin = results.getString("origin");
				String destination = results.getString("destination");
				int departures = results.getInt("departures_performed");
				double seats = results.getDouble("seats_mean");
				
				String carrierKey = getCarrierKey(month, carrier);
				Map<String, Double> segmentCapacities = m_defaultSegmentCapacities.get(carrierKey);
				if (segmentCapacities == null) {
					segmentCapacities = new HashMap<String, Double>();
					m_defaultSegmentCapacities.put(carrierKey, segmentCapacities);
				}
				String segmentKey = getSegmentKey(origin, destination);
				segmentCapacities.put(segmentKey, seats);
				
				// Set the carrier default equal to the average across all matching segments
				Double carrierDefault = m_defaultCarrierCapacities.get(carrierKey);
				if (carrierDefault != null) {
					int priorDepartures = carrierDepartures.get(carrierKey);
					seats = (carrierDefault.doubleValue() * priorDepartures + seats * departures) /
						(priorDepartures + departures);
					departures = priorDepartures + departures;
				}
				m_defaultCarrierCapacities.put(carrierKey, seats);
				carrierDepartures.put(carrierKey, departures);
			}
		} catch (SQLException e) {
			logger.error("Received unexpected SQL exception " + e.toString());
			StackTraceElement[] traceElements = e.getStackTrace();
			for (int i = 0; i < traceElements.length; ++i) {
				logger.error(traceElements[i]);
			}
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Exception e) {
					logger.error("Unable to close results set due to exception"
							+ e.toString());
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					logger.error("Unable to close statement due to exception"
							+ e.toString());
				}
			}
		}
	}
	
	public void queryFlightInfo() {
		Statement statement = null;
		ResultSet results = null;

		try {
			statement = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select id, month, carrier, origin, destination,").append(NEWLINE);
			query.append("  cancelled_flag, seating_capacity,").append(NEWLINE);
			query.append("  extract(day from planned_arrival_time - planned_departure_time) * 24 * 60").append(NEWLINE);
			query.append("    + extract(hour from planned_arrival_time - planned_departure_time) * 60").append(NEWLINE);
			query.append("    + extract(minute from planned_arrival_time - planned_departure_time)").append(NEWLINE);
			query.append("  as duration").append(NEWLINE);
			query.append("from flights").append(NEWLINE);
			query.append("where carrier in").append(NEWLINE);
			query.append("  ").append(getCarrierClause()).append(NEWLINE);			

			logger.trace("Flight information query:");
			logger.trace(query.toString());

			results = statement.executeQuery(query.toString());
			while (results.next()) {
				int flightID = results.getInt("id");
				FlightInformation info = new FlightInformation(flightID);
				int seatingCapacity = results.getInt("seating_capacity");
				int duration = results.getInt("duration");
				if (seatingCapacity == 0) {
					int month = results.getInt("month");
					String carrier = results.getString("carrier");
					String origin = results.getString("origin");
					String destination = results.getString("destination");
					String carrierKey = getCarrierKey(month, carrier);
					String segmentKey = getSegmentKey(origin, destination);
					Map<String, Double> segmentCapacities = m_defaultSegmentCapacities.get(carrierKey);
					Double defaultCapacity = segmentCapacities.get(segmentKey);
					if (defaultCapacity == null) {
						defaultCapacity = m_defaultCarrierCapacities.get(carrierKey);
					}
					seatingCapacity = (int) Math.round(defaultCapacity.doubleValue());
				}
				info.setDuration(duration);
				info.setSeatingCapacity(seatingCapacity);
				info.setCancelled(results.getBoolean("cancelled_flag"));
				m_flightInfo.put(flightID, info);
			}

		} catch (SQLException e) {
			logger.error("Received unexpected SQL exception " + e.toString());
			StackTraceElement[] traceElements = e.getStackTrace();
			for (int i = 0; i < traceElements.length; ++i) {
				logger.error(traceElements[i]);
			}
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Exception e) {
					logger.error("Unable to close results set due to exception"
							+ e.toString());
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					logger.error("Unable to close statement due to exception"
							+ e.toString());
				}
			}
		}
	}

	public void queryAirlineItineraries() {
		Statement statement = null;
		ResultSet results = null;

		try {
			statement = createStatement();

			StringBuffer query = new StringBuffer();
			query.append("select itinerary_id, num_flights,").append(NEWLINE);
			query.append("  first_carrier, second_carrier,").append(NEWLINE);
			query.append("  first_flight_id, second_flight_id,")
					.append(NEWLINE);
			query.append("  origin, connection, destination,").append(NEWLINE);
			query.append("  year, day_of_week,").append(NEWLINE);
			query
					.append(
							"  to_number(to_char(first_departure_time, 'DDD')) as day_of_year,")
					.append(NEWLINE);
			query
					.append(
							"  to_number(to_char(first_departure_time, 'HH24')) as departure_hour,")
					.append(NEWLINE);
			query
					.append(
							"  to_number(to_char(first_departure_time, 'MI')) as departure_minutes,")
					.append(NEWLINE);
			query.append("  layover_duration, passengers").append(NEWLINE);
			query.append("from paxdelay.airline_itineraries").append(NEWLINE);
			query.append("where trip_duration is not null").append(NEWLINE);
			query.append("  and first_carrier in ").append(getCarrierClause())
					.append(NEWLINE);
			query.append("  and (num_flights = 1").append(NEWLINE);
			query.append("    or (num_flights = 2 ").append(NEWLINE);
			query.append("      and layover_duration is not null").append(
					NEWLINE);
			query.append("      and second_carrier in ").append(
					getCarrierClause()).append("))").append(NEWLINE);
			query.append("  and (").append(NEWLINE);
			for (int i = 0; i < m_years.length; ++i) {
				if (i > 0) {
					query.append("    or").append(NEWLINE);
				}
				query.append("    (year =  ").append(m_years[i]);
				query.append(" and quarter = ").append(m_quarters[i]);
				query.append(")").append(NEWLINE);
			}
			query.append("  )").append(NEWLINE);

			logger.trace("Airline itineraries query:");
			logger.trace(query.toString());

			results = statement.executeQuery(query.toString());
			int count = 0;
			while (results.next()) {
				int numFlights = results.getInt("num_flights");
				String firstCarrier = results.getString("first_carrier");
				String origin = results.getString("origin");
				String destination = results.getString("destination");

				CarrierFlightPath[] flightPaths = null;
				if (numFlights == 1) {
					flightPaths = new CarrierFlightPath[] { new CarrierFlightPath(
							firstCarrier, origin, destination) };
				} else {
					String secondCarrier = results.getString("second_carrier");
					String connection = results.getString("connection");
					flightPaths = new CarrierFlightPath[] {
							new CarrierFlightPath(firstCarrier, origin,
									connection),
							new CarrierFlightPath(secondCarrier, connection,
									destination) };
				}

				int year = results.getInt("year");
				int dayOfYear = results.getInt("day_of_year");
				int dayOfWeek = results.getInt("day_of_week");
				int localDepartureHour = results.getInt("departure_hour");
				int localDepartureMinutes = results.getInt("departure_minutes");
				int connectionTime = results.getInt("layover_duration");
				// Skip all itineraries with connection times shorter than
				// the specified minimum connection time or longer than
				// the specified maximum connection time
				if (numFlights == 2 && (connectionTime < m_minimumConnectionTime || 
						connectionTime > m_maximumConnectionTime)) {
					continue;
				}
				int numPassengers = results.getInt("passengers");

				FlightInformation[] flightInfos = null;
				int firstFlightID = results.getInt("first_flight_id");
				int secondFlightID = results.getInt("second_flight_id");
				FlightInformation firstFlightInfo = m_flightInfo.get(firstFlightID);
				if (secondFlightID == 0) {
					flightInfos = new FlightInformation[]{firstFlightInfo};
				}
				else {
					FlightInformation secondFlightInfo = m_flightInfo.get(secondFlightID);
					flightInfos = new FlightInformation[]{firstFlightInfo, secondFlightInfo};					
				}

				AirlineItinerary itinerary = new AirlineItinerary(flightPaths, flightInfos,
						year, dayOfYear, dayOfWeek, localDepartureHour,
						localDepartureMinutes, connectionTime, numPassengers);

				m_includedFlights.add(firstFlightID);
				if (secondFlightID != 0) {
					m_includedFlights.add(secondFlightID);
				}

				m_observationSet.processAirlineItinerary(itinerary, true);
				count++;
				if (count % 100000 == 0) {
					logger.trace("Processed " + count + " airline itineraries");
				}
			}
		} catch (SQLException e) {
			logger.error("Received unexpected SQL exception " + e.toString());
			StackTraceElement[] traceElements = e.getStackTrace();
			for (int i = 0; i < traceElements.length; ++i) {
				logger.error(traceElements[i]);
			}
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Exception e) {
					logger.error("Unable to close results set due to exception"
							+ e.toString());
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					logger.error("Unable to close statement due to exception"
							+ e.toString());
				}
			}
		}
	}

	public void queryGeneratedItineraries() {
		// We only add the generated itineraries if the appropriate flag is set
		if (!m_includeGeneratedItineraries) {
			return;
		}
		Statement statement = null;
		ResultSet results = null;

		try {
			statement = createStatement();

			StringBuffer query = new StringBuffer();
			query
					.append(
							"select id, first_operating_carrier, second_operating_carrier,")
					.append(NEWLINE);
			query.append("  first_flight_id, second_flight_id,")
					.append(NEWLINE);
			query.append("  origin, connection, destination,").append(NEWLINE);
			query.append("  year, day_of_week,").append(NEWLINE);
			query
					.append(
							"  to_number(to_char(planned_departure_time, 'DDD')) as day_of_year,")
					.append(NEWLINE);
			query
					.append(
							"  to_number(to_char(planned_departure_time, 'HH24')) as departure_hour,")
					.append(NEWLINE);
			query
					.append(
							"  to_number(to_char(planned_departure_time, 'MI')) as departure_minutes,")
					.append(NEWLINE);
			query.append("  layover_duration").append(NEWLINE);
			query.append("from paxdelay.itineraries").append(NEWLINE);
			query.append("where num_flights = 2").append(NEWLINE);
			query.append("  and first_operating_carrier in ").append(
					getCarrierClause()).append(NEWLINE);
			query.append("  and second_operating_carrier in ").append(
					getCarrierClause()).append(NEWLINE);
			query.append("  and (").append(NEWLINE);
			for (int i = 0; i < m_years.length; ++i) {
				if (i > 0) {
					query.append("    or").append(NEWLINE);
				}
				query.append("    (year =  ").append(m_years[i]);
				query.append(" and quarter = ").append(m_quarters[i]);
				query.append(")").append(NEWLINE);
			}
			query.append("  )").append(NEWLINE);

			logger.trace("Generated itineraries query:");
			logger.trace(query.toString());

			results = statement.executeQuery(query.toString());
			int count = 0;
			while (results.next()) {
				// Only add generated itineraries if the flight IDs show up
				// in the airline itinerary data
				int firstFlightID = results.getInt("first_flight_id");
				int secondFlightID = results.getInt("second_flight_id");
				if (!m_includedFlights.contains(firstFlightID)) {
					continue;
				}
				if (secondFlightID != 0
						&& !m_includedFlights.contains(secondFlightID)) {
					continue;
				}

				String firstCarrier = results
						.getString("first_operating_carrier");
				String origin = results.getString("origin");
				String destination = results.getString("destination");

				String secondCarrier = results
						.getString("second_operating_carrier");
				String connection = results.getString("connection");
				CarrierFlightPath[] flightPaths = new CarrierFlightPath[] {
						new CarrierFlightPath(firstCarrier, origin, connection),
						new CarrierFlightPath(secondCarrier, connection,
								destination) };

				int year = results.getInt("year");
				int dayOfYear = results.getInt("day_of_year");
				int dayOfWeek = results.getInt("day_of_week");
				int localDepartureHour = results.getInt("departure_hour");
				int localDepartureMinutes = results.getInt("departure_minutes");
				int connectionTime = results.getInt("layover_duration");
				// Skip all generated itineraries with a connection time longer
				// than the specified maximum connection time.  For generated
				// itineraries, we also skip itineraries where the connection time
				// is less than the minimum connection time.
				if (connectionTime > m_maximumConnectionTime || 
						connectionTime < m_minimumConnectionTime) {
					continue;
				}
				int numPassengers = 0;

				FlightInformation[] flightInfos = null;
				FlightInformation firstFlightInfo = m_flightInfo.get(firstFlightID);
				if (secondFlightID == 0) {
					flightInfos = new FlightInformation[]{firstFlightInfo};
				}
				else {
					FlightInformation secondFlightInfo = m_flightInfo.get(secondFlightID);
					flightInfos = new FlightInformation[]{firstFlightInfo, secondFlightInfo};					
				}

				AirlineItinerary itinerary = new AirlineItinerary(flightPaths, flightInfos,
						year, dayOfYear, dayOfWeek, localDepartureHour,
						localDepartureMinutes, connectionTime, numPassengers);

				m_observationSet.processAirlineItinerary(itinerary, false);
				count++;
				if (count % 100000 == 0) {
					logger.trace("Processed " + count
							+ " generated itineraries");
				}
			}
		} catch (SQLException e) {
			logger.error("Received unexpected SQL exception " + e.toString());
			StackTraceElement[] traceElements = e.getStackTrace();
			for (int i = 0; i < traceElements.length; ++i) {
				logger.error(traceElements[i]);
			}
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Exception e) {
					logger.error("Unable to close results set due to exception"
							+ e.toString());
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					logger.error("Unable to close statement due to exception"
							+ e.toString());
				}
			}
		}
	}

	public void writeBiogemeDataFile() throws FileNotFoundException {
		StringBuffer outputFilename = new StringBuffer(m_biogemeDataDirectory);
		outputFilename.append(File.separator);
		outputFilename.append(m_biogemeDataFilename);

		PrintWriter dataWriter = new PrintWriter(outputFilename.toString());

		m_observationSet.writeColumnHeaders(dataWriter);
		m_observationSet.writeChoiceData(dataWriter);
	}
	
	protected String getCarrierKey(int month, String carrier) {
		StringBuffer key = new StringBuffer();
		key.append(carrier).append("(").append(month).append(")");
		return key.toString();
	}

	protected String getSegmentKey(String origin, String destination) {
		StringBuffer key = new StringBuffer();
		key.append(origin).append("_").append(destination);
		return key.toString();
	}
	
	protected String getCarrierClause() {
		StringBuffer clause = new StringBuffer();
		clause.append("(");
		for (int i = 0; i < m_carriers.length; ++i) {
			if (i > 0) {
				clause.append(", ");
			}
			clause.append("'").append(m_carriers[i]).append("'");
		}
		clause.append(")");
		return clause.toString();
	}

	protected void connectToDatabase() {
		try {
			m_datasource = new OracleDataSource();
			m_datasource.setURL(m_jdbcURL);
			m_dbConnection = m_datasource.getConnection(m_dbUsername,
					m_dbPassword);
		} catch (SQLException e) {
			exit("Unable to connect to database " + m_jdbcURL
					+ " using username " + m_dbUsername + " and password "
					+ m_dbPassword, e, -1);
		}
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

	protected void disconnectFromDatabase() {
		try {
			m_dbConnection.close();
			m_datasource.close();
		} catch (SQLException e) {
			logger.fatal("Unable to disconnect from database", e);
		}
	}
}

package edu.mit.nsfnats.paxdelay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.util.FileProcessor;
import edu.mit.nsfnats.paxdelay.util.LineProcessor;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;

public class AllocationValidator {
	static Logger logger = Logger.getLogger(AllocationValidator.class);

	public static final String PROPERTY_YEAR = "YEAR";
	public static final String PROPERTY_QUARTER = "QUARTER";
	public static final String PROPERTY_CARRIER = "CARRIER";
	public static final String PROPERTY_FIRST_DATE = "FIRST_DATE";
	public static final String PROPERTY_LAST_DATE = "LAST_DATE";
	public static final String PROPERTY_ALLOCATION_DIRECTORY = "ALLOCATION_DIRECTORY";
	public static final String PROPERTY_ALLOCATION_FILENAME_PREFIX = "ALLOCATION_FILENAME";
	public static final String PROPERTY_IS_OPTIMIZATION_ALLOCATION = "IS_OPTIMIZATION_ALLOCATION";
	public static final String PROPERTY_VALIDATION_OUTPUT_DIRECTORY = "VALIDATION_OUTPUT_DIRECTORY";
	public static final String PROPERTY_VALIDATION_OUTPUT_FILENAME = "VALIDATION_OUTPUT_FILENAME";
	public static final String OUTPUT_FIELD_SEPARATOR = ",";

	public static final String PROPERTY_JDBC_URL = "JDBC_URL";
	public static final String PROPERTY_DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String PROPERTY_DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public static final String AIRLINE_ITINERARIES_QUERY = "select itinerary_id,\n"
			+ "  first_carrier, second_carrier,\n"
			+ "  first_flight_id, second_flight_id, passengers\n"
			+ "from paxdelay.airline_itineraries\n"
			+ "where year = ? and quarter = ?\n"
			+ "  and (first_carrier = ? or second_carrier = ?)\n"
			+ "  and to_date(concat(month, concat('/', \n"
			+ "    concat(day_of_month, concat('/', year)))), \n"
			+ "      'MM/DD/YYYY') >= ? \n"
			+ "  and to_date(concat(month, concat('/', \n"
			+ "    concat(day_of_month, concat('/', year)))), \n"
			+ "      'MM/DD/YYYY') <= ?\n" + "order by num_flights";

	Map<Itinerary, Itinerary[]> m_itineraryValidationMap;
	int m_year;
	int m_quarter;
	// Flight carrier whose date is being used for validation
	String m_carrier;
	// First and last dates for validation, inclusive
	Date m_firstDate;
	Date m_lastDate;
	// Root directory and filename in which allocation results reside
	String m_allocationDirectory;
	String[] m_allocationFilenames;
	boolean m_isOptimizationAllocation;

	String m_validationOutputDirectory;
	String m_validationOutputFilename;

	String m_jdbcURL;
	String m_dbUsername;
	String m_dbPassword;

	public AllocationValidator() {
		m_itineraryValidationMap = new HashMap<Itinerary, Itinerary[]>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err
					.println("Usage: java edu.mit.nsfnats.paxdelay.AllocationValidator <logger_properties_file> <validation_properties_file>");
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

		Properties validationProperties = null;
		try {
			validationProperties = PropertiesReader.loadProperties(args[1]);
		} catch (FileNotFoundException e) {
			exit("Itineraries properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading itineraries properties file.",
					e, -1);
		}
		main(validationProperties);
	}

	public static void main(Properties validationProperties) {
		AllocationValidator validator = new AllocationValidator();

		try {
			validator.initialize(validationProperties);
		} catch (InvalidFormatException e) {
			exit("Invalid format specified in validation properties file", e,
					-1);
		}

		try {
			validator.readAllocationFiles();
		} catch (InvalidFormatException e) {
			exit("Unable to read allocation files", e, -1);
		}

		validator.queryAirlineItineraries();
		try {
			validator.writeValidationResults();
		} catch (FileNotFoundException e) {
			exit("Unable to write to validation output file", e, -1);
		}
	}

	protected static void exit(String message, Exception e, int code) {
		System.err.println(message);
		System.err.println(e);
		System.exit(code);
	}

	public void initialize(Properties properties) throws InvalidFormatException {
		m_year = PropertiesReader.readInt(properties, PROPERTY_YEAR);
		m_quarter = PropertiesReader.readInt(properties, PROPERTY_QUARTER);
		m_carrier = properties.getProperty(PROPERTY_CARRIER);
		try {
			java.util.Date tempDate = PropertiesReader.readDate(properties,
					PROPERTY_FIRST_DATE);
			m_firstDate = new Date(tempDate.getTime());
		} catch (ParseException e) {
			throw new InvalidFormatException(
					"Invalid format for first date string: " + m_firstDate, e);
		}
		try {
			java.util.Date tempDate = PropertiesReader.readDate(properties,
					PROPERTY_LAST_DATE);
			m_lastDate = new Date(tempDate.getTime());
		} catch (ParseException e) {
			throw new InvalidFormatException(
					"Invalid format for last date string: " + m_lastDate, e);
		}
		m_allocationDirectory = properties
				.getProperty(PROPERTY_ALLOCATION_DIRECTORY);
		m_allocationFilenames = PropertiesReader.readStrings(properties,
				PROPERTY_ALLOCATION_FILENAME_PREFIX);
		m_isOptimizationAllocation = PropertiesReader.readBoolean(properties,
				PROPERTY_IS_OPTIMIZATION_ALLOCATION);

		m_jdbcURL = properties.getProperty(PROPERTY_JDBC_URL);
		m_dbUsername = properties.getProperty(PROPERTY_DATABASE_USERNAME);
		m_dbPassword = properties.getProperty(PROPERTY_DATABASE_PASSWORD);

		m_validationOutputDirectory = properties
				.getProperty(PROPERTY_VALIDATION_OUTPUT_DIRECTORY);
		File validationDirectory = new File(m_validationOutputDirectory);
		if (!validationDirectory.exists()) {
			validationDirectory.mkdir();
		}
		m_validationOutputFilename = properties
				.getProperty(PROPERTY_VALIDATION_OUTPUT_FILENAME);
	}

	public void readAllocationFiles() throws InvalidFormatException {
		FileProcessor allocationFileProcessor = new FileProcessor(
				new AllocationLineProcessor());
		if (m_isOptimizationAllocation) {
			Calendar current = Calendar.getInstance();
			current.setTime(m_firstDate);
			for (; !current.getTime().after(m_lastDate); current.add(
					Calendar.DAY_OF_MONTH, 1)) {
				String month = current.getDisplayName(Calendar.MONTH,
						Calendar.LONG, Locale.US);
				String day = Integer.toString(current
						.get(Calendar.DAY_OF_MONTH));

				StringBuffer currentFileBuffer = new StringBuffer(
						m_allocationDirectory);
				currentFileBuffer.append(File.separator).append(m_carrier)
						.append(File.separator);
				currentFileBuffer.append(month).append(File.separator);
				currentFileBuffer.append(day).append(File.separator);
				currentFileBuffer.append(m_allocationFilenames[0]);
				try {
					allocationFileProcessor.processFile(currentFileBuffer
							.toString());
				} catch (FileNotFoundException e) {
					throw new InvalidFormatException(
							"Unable to find allocation file: "
									+ currentFileBuffer, e);
				}
			}
		} else {
			for (int i = 0; i < m_allocationFilenames.length; ++i) {
				StringBuffer currentFileBuffer = new StringBuffer(
						m_allocationDirectory);
				currentFileBuffer.append(File.separator);
				currentFileBuffer.append(m_allocationFilenames[i]);
				try {
					allocationFileProcessor.processFile(currentFileBuffer
							.toString());
				} catch (FileNotFoundException e) {
					throw new InvalidFormatException(
							"Unable to find allocation file: "
									+ currentFileBuffer, e);
				}
			}
		}
	}

	class AllocationLineProcessor extends LineProcessor {
		public AllocationLineProcessor() {
			super("\t", true);
		}

		@Override
		protected void processLine() throws InvalidFormatException {
			String firstCarrier = getNextValue();
			String secondCarrier = getNextValue();
			// Only process the itineraries that contain the carrier of interest
			if (!m_carrier.equals(firstCarrier)
					&& !m_carrier.equals(secondCarrier)) {
				return;
			}
			long firstFlightID = getLongNextValue();
			long secondFlightID = getLongNextValue(0);
			double passengers = getDoubleNextValue();

			String[] flightCarriers = null;
			long[] flightIDs = null;
			if (secondFlightID > 0) {
				flightCarriers = new String[] { firstCarrier, secondCarrier };
				flightIDs = new long[] { firstFlightID, secondFlightID };
			} else {
				flightCarriers = new String[] { firstCarrier };
				flightIDs = new long[] { firstFlightID };
			}
			Itinerary itinerary = new Itinerary(flightCarriers, flightIDs);
			itinerary.setNumPassengers(passengers);

			Itinerary[] validationPair = m_itineraryValidationMap
					.get(itinerary);
			if (validationPair == null) {
				validationPair = new Itinerary[2];
				m_itineraryValidationMap.put(itinerary, validationPair);
			} else {
				logger
						.error("Duplicate itineraries detected in allocation file!");
			}
			validationPair[0] = itinerary;
		}
	}

	public void queryAirlineItineraries() {
		OracleDataSource dataSource = null;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet results = null;

		try {
			dataSource = new OracleDataSource();
			dataSource.setURL(m_jdbcURL);
			connection = dataSource.getConnection(m_dbUsername, m_dbPassword);

			statement = connection.prepareStatement(AIRLINE_ITINERARIES_QUERY);
			statement.setInt(1, m_year);
			statement.setInt(2, m_quarter);
			statement.setString(3, m_carrier);
			statement.setString(4, m_carrier);
			statement.setDate(5, m_firstDate);
			statement.setDate(6, m_lastDate);

			logger.trace(statement);
			results = statement.executeQuery();
			while (results.next()) {
				long itineraryID = results.getLong("ITINERARY_ID");
				String firstCarrier = results.getString("FIRST_CARRIER");
				String secondCarrier = results.getString("SECOND_CARRIER");
				long firstFlightID = results.getLong("FIRST_FLIGHT_ID");
				long secondFlightID = results.getLong("SECOND_FLIGHT_ID");
				int passengers = results.getInt("PASSENGERS");

				if (firstFlightID == 0 && secondFlightID == 0) {
					continue;
				}

				if (firstFlightID > 0 && secondFlightID > 0) {
					processAirlineItinerary(itineraryID, new String[] {
							firstCarrier, secondCarrier }, new long[] {
							firstFlightID, secondFlightID }, passengers);
				} else if (firstFlightID > 0 && secondFlightID == 0) {
					processAirlineItinerary(itineraryID,
							new String[] { firstCarrier },
							new long[] { firstFlightID }, passengers);
				}
			}
		} catch (SQLException e) {
			logger.error("Received unexpected SQL exception " + e.toString());
			logger.error(e.getStackTrace());
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
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					logger
							.error("Unable to close database connection due to exception "
									+ e.toString());
				}
			}
			if (dataSource != null) {
				try {
					dataSource.close();
				} catch (Exception e) {
					logger
							.error("Unable to close data source due to exception "
									+ e.toString());
				}
			}
		}
	}

	public void processAirlineItinerary(long itineraryID,
			String[] flightCarriers, long[] flightIDs, double passengers) {
		Itinerary newItinerary = new Itinerary(flightCarriers, flightIDs);
		Itinerary[] validationPair = m_itineraryValidationMap.get(newItinerary);
		if (validationPair == null) {
			validationPair = new Itinerary[2];
			m_itineraryValidationMap.put(newItinerary, validationPair);
		}
		if (validationPair[1] != null) {
			// Airline itineraries are not guaranteed to be unique
			double initialPassengers = validationPair[1].getNumPassengers();
			validationPair[1].setNumPassengers(initialPassengers + passengers);
		} else {
			validationPair[1] = newItinerary;
			validationPair[1].setNumPassengers(passengers);
		}
	}

	public void writeValidationResults() throws FileNotFoundException {
		StringBuffer outputFilename = new StringBuffer(
				m_validationOutputDirectory);
		outputFilename.append(File.separator);
		outputFilename.append(m_validationOutputFilename);

		PrintWriter validationWriter = new PrintWriter(outputFilename
				.toString());
		try
		{
			// First, we need to write the header for the file
			validationWriter.print("Num_Flights");
			validationWriter.print(OUTPUT_FIELD_SEPARATOR);
			validationWriter.print("First_Flight_ID");
			validationWriter.print(OUTPUT_FIELD_SEPARATOR);
			validationWriter.print("Second_Flight_ID");
			validationWriter.print(OUTPUT_FIELD_SEPARATOR);
			validationWriter.print("Allocated_Passengers");
			validationWriter.print(OUTPUT_FIELD_SEPARATOR);
			validationWriter.print("Airline_Passengers");
			validationWriter.println();

			Set<Map.Entry<Itinerary, Itinerary[]>> compSet = m_itineraryValidationMap
					.entrySet();
			Iterator<Map.Entry<Itinerary, Itinerary[]>> compSetIter = compSet
					.iterator();
			while (compSetIter.hasNext()) {
				Map.Entry<Itinerary, Itinerary[]> entry = compSetIter.next();
				Itinerary itineraryKey = entry.getKey();
				Itinerary[] itineraryValues = entry.getValue();

				long[] flightIDs = itineraryKey.getFlightIDs();
				validationWriter.print(flightIDs.length);
				validationWriter.print(OUTPUT_FIELD_SEPARATOR);
				validationWriter.print(flightIDs[0]);
				validationWriter.print(OUTPUT_FIELD_SEPARATOR);
				if (flightIDs.length >= 2) {
					validationWriter.print(flightIDs[1]);
				}
				validationWriter.print(OUTPUT_FIELD_SEPARATOR);
				if (itineraryValues[0] != null) {
					validationWriter.print(itineraryValues[0].getNumPassengers());
				}
				validationWriter.print(OUTPUT_FIELD_SEPARATOR);
				if (itineraryValues[1] != null) {
					validationWriter.print(itineraryValues[1].getNumPassengers());
				}
				validationWriter.println();
			}
			validationWriter.flush();
		}
		finally {
			validationWriter.close();
		}
	}
}

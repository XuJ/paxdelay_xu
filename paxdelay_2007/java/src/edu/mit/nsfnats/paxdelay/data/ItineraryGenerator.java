//SUKITJANUPARP
//create GeneratedItineraries.csv
//change value in the properties file before running (ItineraryGeneratorTest and DefaultLogger)
//XuJiao
//That took 27 minutes
//Change the name of the output file and comment the maximum_connections_time
// and maximum_number_connections in the property files
//Console outputs are located in ~/paxdelay_general_Xu/Itinerary_Generator_Console_Log.txt


package edu.mit.nsfnats.paxdelay.data;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import oracle.jdbc.pool.OracleDataSource;

public class ItineraryGenerator {
	public static Logger logger = Logger.getLogger(ItineraryGenerator.class);

	public static final String PROPERTY_JDBC_URL = ""
			+ "JDBC_URL";
	public static final String PROPERTY_DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String PROPERTY_DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public static final String PROPERTY_FLIGHTS_TABLE = "FLIGHTS_TABLE";
	public static final String DEFAULT_FLIGHTS_TABLE = "flights";
	
	public static int DEFAULT_FETCH_SIZE = 5000;

	public static final String PROPERTY_OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";
	public static final String PROPERTY_OUTPUT_FILENAME = "OUTPUT_FILENAME";
	public static final String DEFAULT_OUTPUT_FILENAME = "GeneratedItineraries.csv";
	public static final String CSV_FIELD_SEPARATOR = ",";

	public static final String PROPERTY_YEAR = "YEAR";
	public static final String PROPERTY_FIRST_DATE = "FIRST_DATE";
	public static final String PROPERTY_LAST_DATE = "LAST_DATE";
	public static final int DEFAULT_YEAR = 2007;

	public static final String NEWLINE = "\n";

	// TODO: Set the minimum connection time based on the carrier and hub
	public static final String PROPERTY_MINIMUM_CONNECTION_TIME = "MINIMUM_CONNECTION_TIME";
	public static final int DEFAULT_MINIMUM_CONNECTION_TIME = 30;
	public static final String PROPERTY_MAXIMUM_CONNECTION_TIME = "MAXIMUM_CONNECTION_TIME";
	public static final int DEFAULT_MAXIMUM_CONNECTION_TIME = 300;
	public static final String PROPERTY_MAXIMUM_NUMBER_CONNECTIONS = "MAXIMUM_NUMBER_CONNECTIONS";
	public static final int DEFAULT_MAXIMUM_NUMBER_CONNECTIONS = 2;

	int m_year;
	Date m_firstDate;
	Date m_lastDate;
	String[] m_carriers;

	String m_jdbcURL;
	String m_dbUsername;
	String m_dbPassword;
	
	String m_flightsTable;

	OracleDataSource m_datasource;
	Connection m_dbConnection;

	String m_outputDirectory;
	PrintWriter m_fileWriter;

	Map<String, String[]> m_validConnectionsMap;
	Map<String, InternalFlight[]> m_carrierSegmentFlights;

	InternalFlight[] m_flights;
	int m_currentFlightIndex;
	Timestamp m_currentArrivalTime;

	int m_minimumConnectionTime;
	int m_maximumConnectionTime;
	int m_maximumNumberConnections;

	public static final String PROPERTY_CARRIER_PREFIX = "CARRIER";

	public ItineraryGenerator() {
		m_validConnectionsMap = new HashMap<String, String[]>();
		m_carrierSegmentFlights = new HashMap<String, InternalFlight[]>();

		m_flights = new InternalFlight[0];
		m_currentFlightIndex = 0;
	}

	public static void main(String[] args) {
		
		long startTime = System.nanoTime();
		
//		if (args.length != 2) {
//			System.err
//					.println("Usage: java edu.mit.nsfnats.paxdelay.data.ItineraryGenerator "
//							+ "<logger_properties_file> <generator_properties_file>");
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
		
//		Properties generatorProperties = null;
//		try {
////			generatorProperties = PropertiesReader.loadProperties(args[1]);
//			generatorProperties = PropertiesReader.loadProperties("ItineraryGeneratorTest.properties");
//		} catch (FileNotFoundException e) {
//			exit("Itinerary generator properties file not found.", e, -1);
//		} catch (IOException e) {
//			exit(
//					"Received IO exception while reading itinerary generator properties file.",
//					e, -1);
//		}
//		logger
//				.info("Beginning ItineraryGenerator.main(Properties properties) execution");
//		main(generatorProperties);
//		logger
//				.info("Execution of ItineraryGenerator.main(Properties properties) complete");
//	}
		
		
		
		
		
		
		Properties loggerProperties = null;
		try {
			loggerProperties = PropertiesReader.loadProperties("resources/config/desktop/DebugLogger.properties");
		} catch (FileNotFoundException e) {
			exit("Logger properties file not found.", e, -1);
		} catch (IOException e) {
			exit("Received IO exception while reading logger properties file.",
					e, -1);
		}
		PropertyConfigurator.configure(loggerProperties);

		Properties generatorProperties = null;
		try {
			generatorProperties = PropertiesReader.loadProperties("resources/config/desktop/ItineraryGeneratorTest.properties");
		} catch (FileNotFoundException e) {
			exit("Itinerary generator properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading itinerary generator properties file.",
					e, -1);
		}
		logger
				.info("Beginning ItineraryGenerator.main(Properties properties) execution");
		main(generatorProperties);
		logger
				.info("Execution of ItineraryGenerator.main(Properties properties) complete");
		
		   
			long endTime = System.nanoTime();
			long duration = (endTime - startTime)/1000000/1000/60;
			System.out.println("That took " + duration + " minutes ");
	}

	public static void main(Properties properties) {
		ItineraryGenerator generator = new ItineraryGenerator();
		try {
//			XuJ 051017: remove properties printing
//			System.out.println("properties "+properties);
			generator.initialize(properties);
		} catch (InvalidFormatException e) {
			exit(
					"Invalid format specified in itinerary generator properties file",
					e, -1);
		}

		try {
			generator.connectToDatabase();

			generator.loadValidConnections();

			generator.writeOutputHeader();

			generator.generateItineraries();
		} finally {
			generator.disconnectFromDatabase();
			logger.trace("Disconnected from database");
		}
	}

	public void initialize(Properties properties) throws InvalidFormatException {
		m_jdbcURL = properties.getProperty(PROPERTY_JDBC_URL);
//		System.out.println("url: "+m_jdbcURL);
		m_dbUsername = properties.getProperty(PROPERTY_DATABASE_USERNAME);
//		System.out.println("username: "+m_dbUsername);
		m_dbPassword = properties.getProperty(PROPERTY_DATABASE_PASSWORD);

		m_flightsTable = properties.getProperty(PROPERTY_FLIGHTS_TABLE, DEFAULT_FLIGHTS_TABLE);
		
		m_year = PropertiesReader.readInt(properties, PROPERTY_YEAR,
				DEFAULT_YEAR);
		try {
			Date tempDate = PropertiesReader.readDate(properties,
					PROPERTY_FIRST_DATE);
			m_firstDate = new Date(tempDate.getTime());
		} catch (ParseException e) {
			throw new InvalidFormatException(
					"Invalid format for first date string: " + m_firstDate, e);
		}
		try {
			Date tempDate = PropertiesReader.readDate(properties,
					PROPERTY_LAST_DATE);
			m_lastDate = new Date(tempDate.getTime());
		} catch (ParseException e) {
			throw new InvalidFormatException(
					"Invalid format for last date string: " + m_lastDate, e);
		}

		m_carriers = PropertiesReader.readStrings(properties,
				PROPERTY_CARRIER_PREFIX);
		m_outputDirectory = properties.getProperty(PROPERTY_OUTPUT_DIRECTORY);
        System.out.println("output_directory: "+m_outputDirectory);
//        System.out.println(PROPERTY_OUTPUT_DIRECTORY);
		try {
			String filename = m_outputDirectory + File.separator
					+ properties.getProperty(PROPERTY_OUTPUT_FILENAME, 
							DEFAULT_OUTPUT_FILENAME);
			m_fileWriter = new PrintWriter(new File(filename));
		} catch (IOException e) {
			exit("Unable to load output file for writing", e, -1);
		}

		m_minimumConnectionTime = PropertiesReader.readInt(properties,
				PROPERTY_MINIMUM_CONNECTION_TIME,
				DEFAULT_MINIMUM_CONNECTION_TIME);

		m_maximumConnectionTime = PropertiesReader.readInt(properties,
				PROPERTY_MAXIMUM_CONNECTION_TIME,
				DEFAULT_MAXIMUM_CONNECTION_TIME);

		m_maximumNumberConnections = PropertiesReader.readInt(properties,
				PROPERTY_MAXIMUM_NUMBER_CONNECTIONS,
				DEFAULT_MAXIMUM_NUMBER_CONNECTIONS);
	}

	public void loadValidConnections() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query
					.append(
							"select first_operating_carrier, second_operating_carrier,")
					.append(NEWLINE);
			query.append("  origin, connection, destination").append(NEWLINE);
			query.append("from unique_carrier_routes").append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and num_flights = 2").append(NEWLINE);
			query.append("order by first_operating_carrier, origin, connection");
			//XuJ 050917 comment unnecessary print
//			logger.trace("Valid connections query:");
//			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());
			String prevKey = null;
			List<String> connectionList = null;
			while (rset.next()) {
				String firstCarrier = rset.getString("first_operating_carrier");
				String secondCarrier = rset
						.getString("second_operating_carrier");
				String origin = rset.getString("origin");
				String connection = rset.getString("connection");
				String destination = rset.getString("destination");

				String key = getCarrierSegmentKey(firstCarrier, origin,
						connection);
				if (!key.equals(prevKey)) {
					if (connectionList != null) {
						String[] connections = new String[connectionList.size()];
						connectionList.toArray(connections);
						m_validConnectionsMap.put(prevKey, connections);
					}
					connectionList = new ArrayList<String>();
					prevKey = key;
				}
				String connectionKey = getCarrierSegmentKey(secondCarrier,
						connection, destination);
				connectionList.add(connectionKey);
			}
			// We still need to add the last connection list
			String[] connections = new String[connectionList.size()];
			connectionList.toArray(connections);
			m_validConnectionsMap.put(prevKey, connections);
		} catch (SQLException e) {
			exit("Unable to load list of valid connections", e, -1);
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
		m_fileWriter.print("Num_Flights");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("First_Flight_ID");
		m_fileWriter.print(CSV_FIELD_SEPARATOR);
		m_fileWriter.print("Second_Flight_ID");
		m_fileWriter.println();
		m_fileWriter.flush();
	}

	public void generateItineraries() {
		Calendar flightCalendar = Calendar.getInstance();
		flightCalendar.setTime(m_firstDate);

		// Load the first day worth of flight data
		int month = flightCalendar.get(Calendar.MONTH) + 1;
		int day = flightCalendar.get(Calendar.DAY_OF_MONTH);
		Timestamp lastArrivalTime = loadFlightData(month, day);
		Timestamp nextLastArrivalTime = null;
		while (!flightCalendar.getTime().after(m_lastDate)) {
			// We need two days worth of flight data in order to
			// create multiple day itineraries
			flightCalendar.add(Calendar.DAY_OF_MONTH, 1);
			if (flightCalendar.get(Calendar.YEAR) == m_year) {
				nextLastArrivalTime = loadFlightData(flightCalendar
						.get(Calendar.MONTH) + 1, flightCalendar
						.get(Calendar.DAY_OF_MONTH));
			}
			InternalItinerary[] itineraries = buildFeasibleItineraries(lastArrivalTime);
			writeFeasibleItineraries(itineraries);

			logger.info("Generated " + itineraries.length
					+ " feasible itineraries for month " + month + " day "
					+ day);

			lastArrivalTime = nextLastArrivalTime;
			month = flightCalendar.get(Calendar.MONTH) + 1;
			day = flightCalendar.get(Calendar.DAY_OF_MONTH);
		}
		// Since we are sorting the flights by planned arrival time
		// there is no need to process the remaining flights
	}

	public Timestamp loadFlightData(int month, int day) {
		int numFlights = 0;
		int numFlightsMissingCapacity = 0;
		List<InternalFlight> updatedFlightList = new ArrayList<InternalFlight>();
		for (int i = m_currentFlightIndex; i < m_flights.length; ++i) {
			updatedFlightList.add(m_flights[i]);
		}
		Timestamp lastPlannedArrival = new Timestamp(0);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			int quarter = getQuarterForMonth(month);
			StringBuffer query = new StringBuffer();
			query.append("select id, carrier, origin, destination,").append(
					NEWLINE);
			query.append("  planned_departure_time, planned_arrival_time")
					.append(NEWLINE);
			//Remove the paxdelay as we are in that database already
			//query.append("from paxdelay.").append(m_flightsTable).append(NEWLINE);
			query.append("from ").append(m_flightsTable).append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month).append(NEWLINE);
			query.append("  and day_of_month = ").append(day).append(NEWLINE);
			query.append("  and carrier in").append(NEWLINE);
			query.append("    ").append(getCarrierSetString()).append(NEWLINE);
			query.append("order by planned_arrival_time");
			
			//XuJ 050817 comment this query print
//			logger.trace("Flight data query:");
//			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {

				int flightID = rset.getInt("id");
				String carrier = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");

				Timestamp plannedDeparture = rset
						.getTimestamp("planned_departure_time");
				Timestamp plannedArrival = rset
						.getTimestamp("planned_arrival_time");
				if (plannedArrival.after(lastPlannedArrival)) {
					lastPlannedArrival = plannedArrival;
				}

				InternalFlight flight = new InternalFlight(flightID, carrier,
						origin, destination, plannedDeparture, plannedArrival);

				updatedFlightList.add(flight);
				++numFlights;
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
		m_flights = new InternalFlight[updatedFlightList.size()];
		updatedFlightList.toArray(m_flights);

		// Sort the updated list by the planned arrival time
		Arrays.sort(m_flights, new Comparator<InternalFlight>() {
			public int compare(InternalFlight a, InternalFlight b) {
				return a.getPlannedArrival().compareTo(b.getPlannedArrival());
			}
		});
		m_currentFlightIndex = 0;

		// Recreate the map of carrier segment keys to flights
		m_carrierSegmentFlights = new HashMap<String, InternalFlight[]>();
		InternalFlight[] resortedFlights = m_flights.clone();
		Arrays.sort(resortedFlights, new Comparator<InternalFlight>() {
			public int compare(InternalFlight a, InternalFlight b) {
				int compareValue = a.getCarrier().compareTo(b.getCarrier());
				if (compareValue != 0) {
					return compareValue;
				}
				compareValue = a.getOrigin().compareTo(b.getOrigin());
				if (compareValue != 0) {
					return compareValue;
				}
				compareValue = a.getDestination().compareTo(b.getDestination());
				if (compareValue != 0) {
					return compareValue;
				}
				return a.getPlannedDeparture().compareTo(
						b.getPlannedDeparture());
			}
		});
		int index = 0;
		int lastIndex = 0;
		String prevKey = getCarrierSegmentKey(resortedFlights[0].getCarrier(),
				resortedFlights[0].getOrigin(), resortedFlights[0]
						.getDestination());
		for (; index < resortedFlights.length; ++index) {
			String key = getCarrierSegmentKey(resortedFlights[index]
					.getCarrier(), resortedFlights[index].getOrigin(),
					resortedFlights[index].getDestination());
			if (!key.equals(prevKey)) {
				InternalFlight[] segmentOptions = new InternalFlight[index
						- lastIndex];
				for (int i = 0; i < segmentOptions.length; ++i) {
					segmentOptions[i] = resortedFlights[lastIndex + i];
				}
				m_carrierSegmentFlights.put(prevKey, segmentOptions);
				prevKey = key;
				lastIndex = index;
			}
		}
		// We need to add the last set of flight options to the map
		InternalFlight[] segmentOptions = new InternalFlight[index - lastIndex];
		for (int i = 0; i < segmentOptions.length; ++i) {
			segmentOptions[i] = resortedFlights[lastIndex + i];
		}
		m_carrierSegmentFlights.put(prevKey, segmentOptions);

		logger.trace("Number of flights for month " + month + " day " + day
				+ " = " + numFlights);
		logger.trace("Number of flight missing capacity for month " + month
				+ " day " + day + " = " + numFlightsMissingCapacity);

		return lastPlannedArrival;
	}

	public InternalItinerary[] buildFeasibleItineraries(
			Timestamp lastArrivalTime) {
		List<InternalItinerary> itineraryList = new ArrayList<InternalItinerary>();
		System.out.println("size "+m_flights.length+" index: "+m_currentFlightIndex);
		m_currentFlightIndex = 0;
		InternalFlight currentFlight = m_flights[m_currentFlightIndex];
		m_currentArrivalTime = currentFlight.getPlannedArrival();
		while (!m_currentArrivalTime.after(lastArrivalTime)) {
			// Add a non stop itinerary for each flight in ASQP
			itineraryList.add(new InternalItinerary(currentFlight));

			// Add the feasible one stop itineraries where this flight is first
			addOneStopItineraries(currentFlight, itineraryList);

			++m_currentFlightIndex;
			// Make sure we don't scroll past the end of the flights array
			if (m_currentFlightIndex >= m_flights.length) {
				break;
			}
			
			currentFlight = m_flights[m_currentFlightIndex];
			m_currentArrivalTime = currentFlight.getPlannedArrival();
		}
		InternalItinerary[] itineraries = new InternalItinerary[itineraryList
				.size()];
		itineraryList.toArray(itineraries);
		return itineraries;
	}

	public void addOneStopItineraries(InternalFlight flight,
			List<InternalItinerary> itineraryList) {

		// Calculate the earliest departure for connections
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(flight.getPlannedArrival());
		calendar.add(Calendar.MINUTE, m_minimumConnectionTime);
		Date earliestDeparture = calendar.getTime();
		// Calculate the latest departure for connections
		calendar.setTime(flight.getPlannedArrival());
		calendar.add(Calendar.MINUTE, m_maximumConnectionTime);
		Date latestDeparture = calendar.getTime();

		// Retrieve the list of potential carrier segment connections for this
		// flight
		String key = getCarrierSegmentKey(flight.getCarrier(), flight
				.getOrigin(), flight.getDestination());
		String[] connectionKeys = m_validConnectionsMap.get(key);
		// If there are no valid connections, then there is nothing to add
		if (connectionKeys == null) {
			return;
		}
		// Otherwise, for each valid connection build itineraries based on
		// the matching feasible flights
		for (int i = 0; i < connectionKeys.length; ++i) {
			InternalFlight[] connections = m_carrierSegmentFlights
					.get(connectionKeys[i]);
			// If there are no matching flights, move onto the next valid
			// connection
			if (connections == null) {
				continue;
			}
			int numConnections = 0;
			for (int j = 0; j < connections.length; ++j) {
				// Once we pass the latest departure time, we don't need to
				// continue
				// searching the list since the flights are ordered by planned
				// departure
				if (connections[j].getPlannedDeparture().after(latestDeparture)) {
					break;
				}

				// If the connecting flight departs between the earliest
				// departure
				// and the latest departure, we should create a new itinerary
				if (!connections[j].getPlannedDeparture().before(
						earliestDeparture)) {
					// Add the feasible itinerary to the list of generated
					// itineraries
					itineraryList.add(new InternalItinerary(flight,
							connections[j]));
					++numConnections;
					// We only create up to the maximum number of connections
					if (numConnections >= m_maximumNumberConnections) {
						break;
					}
				}
			}
		}
	}

	public void writeFeasibleItineraries(InternalItinerary[] itineraries) {
		for (int i = 0; i < itineraries.length; ++i) {
			itineraries[i].writeItinerary(m_fileWriter);
		}
		m_fileWriter.flush();
	}

	protected static void exit(String message, Exception e, int code) {
		logger.fatal(message, e);
		System.exit(code);
	}

	protected void connectToDatabase() {
		//SUKITJANUPARP
		//changed from oraqle to mysql
		
//		try {
//			m_datasource = new OracleDataSource();
//			m_datasource.setURL(m_jdbcURL);
//			m_dbConnection = m_datasource.getConnection(m_dbUsername,
//					m_dbPassword);
//		} catch (SQLException e) {
//			exit("Unable to connect to database " + m_jdbcURL
//					+ " using username " + m_dbUsername + " and password "
//					+ m_dbPassword, e, -1);
//		}
		
		Connection conn = null;
//		   Statement stmt = null;
		   try{
		      //STEP 2: Register JDBC driver
		      try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		      //STEP 3: Open a connection
		      System.out.println("Connecting to database...");
		      System.out.println(m_jdbcURL);
		      conn = DriverManager.getConnection(m_jdbcURL,m_dbUsername,m_dbPassword);
              
		      m_dbConnection=conn;
		     } catch (SQLException e) {
				exit("Unable to connect to database " + m_jdbcURL
						+ " using username " + m_dbUsername + " and password "
						+ m_dbPassword, e, -1);
			}
		      
//		      //STEP 4: Execute a query
//		      System.out.println("Creating statement...");
//		      stmt = conn.createStatement();
//		      ArrayList<String> sql = new ArrayList<String>();
//		      
//		      sql.add("drop table if exists asqp_carriers");
//		      sql.add("create table asqp_carriers\n" + 
//		      		"select distinct carrier as code\n" + 
//		      		"from aotp");
//		      sql.add("alter table asqp_carriers convert to character set latin1 collate latin1_general_cs");
//		    
//		     
//		     for(Object s:sql){
//		    	  stmt.addBatch(s.toString());
//		    	  System.out.println("hey");
//		      }
//		      stmt.executeBatch();
//		      stmt.clearBatch();
//		      sql.clear();
//		   
		      
		     
		      
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
			//m_datasource.close();
		} catch (SQLException e) {
			logger.fatal("Unable to disconnect from database", e);
		}
	}

	protected String getCarrierSegmentKey(String carrier, String origin,
			String destination) {
		StringBuffer key = new StringBuffer();
		key.append(carrier).append(".");
		key.append(origin).append(".").append(destination);
		return key.toString();
	}

	protected static int getQuarterForMonth(int month) {
		return (int) Math.floor((month + 2) / 3.0);
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
}

class InternalFlight {

	int m_flightID;
	String m_carrier;
	String m_origin;
	String m_destination;
	Timestamp m_plannedDeparture;
	Timestamp m_plannedArrival;

	public InternalFlight(int flightID, String carrier, String origin,
			String destination, Timestamp plannedDeparture,
			Timestamp plannedArrival) {
		m_flightID = flightID;
		m_carrier = carrier;
		m_origin = origin;
		m_destination = destination;
		m_plannedDeparture = plannedDeparture;
		m_plannedArrival = plannedArrival;
	}

	public int getFlightID() {
		return m_flightID;
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

	public Timestamp getPlannedDeparture() {
		return m_plannedDeparture;
	}

	public Timestamp getPlannedArrival() {
		return m_plannedArrival;
	}

}

class InternalItinerary {

	int m_numFlights;
	InternalFlight m_firstFlight;
	InternalFlight m_secondFlight;

	public InternalItinerary(InternalFlight flight) {
		m_numFlights = 1;
		m_firstFlight = flight;
		//SUKITJANUPARP
		//changed from m_secondFlight = null
		m_secondFlight = new InternalFlight(0, "", "", "", new Timestamp(0), new Timestamp(0));
	}

	public InternalItinerary(InternalFlight firstFlight,
			InternalFlight secondFlight) {
		m_numFlights = 2;
		m_firstFlight = firstFlight;
		m_secondFlight = secondFlight;
	}

	public void writeItinerary(PrintWriter writer) {
		writer.print(m_numFlights);
		writer.print(ItineraryGenerator.CSV_FIELD_SEPARATOR);
		writer.print(m_firstFlight.getFlightID());
		writer.print(ItineraryGenerator.CSV_FIELD_SEPARATOR);
		if (m_secondFlight != null) {
			writer.print(m_secondFlight.getFlightID());
		}
		writer.println();
	}
}

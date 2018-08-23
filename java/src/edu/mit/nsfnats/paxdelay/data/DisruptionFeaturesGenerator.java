package edu.mit.nsfnats.paxdelay.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
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
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;

public class DisruptionFeaturesGenerator {
	public static Logger logger = Logger.getLogger(DisruptionFeaturesGenerator.class);

	public static final String PROPERTY_JDBC_URL = "JDBC_URL";
	public static final String PROPERTY_DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String PROPERTY_DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public static final String PROPERTY_PASSENGER_DELAYS_TABLE = "PASSENGER_DELAYS_TABLE";
//	public static final String DEFAULT_PASSENGER_DELAYS_TABLE = "passenger_delays";
	public static final String DEFAULT_PASSENGER_DELAYS_TABLE = "pax_delay_analysis_MIT";
	
	public static final int DEFAULT_FETCH_SIZE = 10000;
	public static final String NEWLINE = "\n";
	
	public static final String PROPERTY_OUTPUT_FILENAME = "OUTPUT_FILENAME";
	public static final String DEFAULT_OUTPUT_FILENAME = "PassengerDisruptionFeatures.csv";
	public static final String CSV_FIELD_DELIMITER = ",";
	
	public static final String PROPERTY_YEAR = "YEAR";
	public static final int DEFAULT_YEAR = 2007;
	public static final String PROPERTY_FIRST_MONTH = "FIRST_MONTH";
	public static final int DEFAULT_FIRST_MONTH = 1;
	public static final String PROPERTY_LAST_MONTH = "LAST_MONTH";
	public static final int DEFAULT_LAST_MONTH = 12;
	
	int m_year;
	int m_firstMonth;
	int m_lastMonth;

	String m_jdbcURL;
	String m_dbUsername;
	String m_dbPassword;
	
	OracleDataSource m_datasource;
	Connection m_dbConnection;
	
	String m_passengerDelaysTable;
	
	String m_outputDirectory;
	String m_outputFilename;
	
	// Map of primary carriers to secondary carriers for recovery purposes
	Map<String, String[]> m_relatedCarriersMap;
	// Set of carrier hub airports (>= 10% of operations)
	Set<CarrierAirport> m_carrierHubsSet;
	// Set of carrier primary hubs (airport with most operations)
	Set<CarrierAirport> m_carrierPrimaryAirportSet;
	// Monthly storage for computing disruption features
	Map<NonStopKey, NonStopItineraryData> m_nonStopDataMap;
	Map<OneStopKey, OneStopItineraryData> m_oneStopDataMap;
	Map<CarrierAirport, PerformanceData> m_carrierAirportDataMap;
	
	public DisruptionFeaturesGenerator(String outputDirectory) {
		m_outputDirectory = outputDirectory;
		m_relatedCarriersMap = new HashMap<String, String[]>();
		m_carrierHubsSet = new HashSet<CarrierAirport>();
		m_carrierPrimaryAirportSet = new HashSet<CarrierAirport>();
	}
	
	public static void main(String[] args) {
		
		long startTime = System.nanoTime();
		
//		if (args.length != 3) {
//			System.err
//					.println("Usage: java edu.mit.nsfnats.paxdelay.data.ItineraryGenerator " +
//							" <logger_properties_file> <generator_properties_file>" +
//							" <outputDirectory>");
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
//		Properties generatorProperties = null;
//		try {
//			generatorProperties = PropertiesReader.loadProperties(args[1]);
//		} catch (FileNotFoundException e) {
//			exit("Itinerary generator properties file not found.", e, -1);
//		} catch (IOException e) {
//			exit(
//					"Received IO exception while reading itinerary generator properties file.",
//					e, -1);
//		}
//		String outputDirectory = args[2];
//		logger
//				.info("Beginning DisruptionFeaturesGenerator.main(...) execution");
//		main(generatorProperties, outputDirectory);
//		logger
//				.info("Execution of ItineraryGenerator.main(...) complete");
//		
		
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

		Properties generatorProperties = null;
		try {
			generatorProperties = PropertiesReader.loadProperties("resources/config/desktop/DisruptionFeaturesGenerator.properties");
		} catch (FileNotFoundException e) {
			exit("Itinerary generator properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading itinerary generator properties file.",
					e, -1);
		}
		String outputDirectory = "/mdsg/paxdelay_analysis/";
		logger
				.info("Beginning DisruptionFeaturesGenerator.main(...) execution");
		main(generatorProperties, outputDirectory);
		logger
				.info("Execution of ItineraryGenerator.main(...) complete");
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000/1000/60;
		System.out.println("That took " + duration + " minutes ");
	}
	
	public static void main(Properties properties, String outputDirectory) {
		DisruptionFeaturesGenerator generator = new DisruptionFeaturesGenerator(outputDirectory);
		try {
			generator.initialize(properties);
		}
		catch (InvalidFormatException e) {
			exit("Unable to initialize disruption features generator", e, -1);
		}
		try {
			generator.connectToDatabase();
			
			generator.queryCarrierHubs();
			
			generator.queryRelatedCarriers();
			try {
				generator.processItineraryDisruptions();
			}
			catch (InvalidFormatException e) {
				exit("Unable to process itinerary disruptions", e, -1);
			}
		}
		finally {
			generator.disconnectFromDatabase();
		}
	}
	
	public void initialize(Properties properties) throws InvalidFormatException {
		File outputDirectoryFile = new File(m_outputDirectory);
		if (!outputDirectoryFile.exists()) {
			outputDirectoryFile.mkdir();
		}
		else if(!outputDirectoryFile.isDirectory()) {
			throw new InvalidFormatException("Specified output location " + m_outputDirectory
					+ " is not a directory");
		}
		
		m_jdbcURL = properties.getProperty(PROPERTY_JDBC_URL);
		m_dbUsername = properties.getProperty(PROPERTY_DATABASE_USERNAME);
		m_dbPassword = properties.getProperty(PROPERTY_DATABASE_PASSWORD);

		m_passengerDelaysTable = properties.getProperty(PROPERTY_PASSENGER_DELAYS_TABLE, 
				DEFAULT_PASSENGER_DELAYS_TABLE);
		
		m_year = PropertiesReader.readInt(properties, PROPERTY_YEAR, DEFAULT_YEAR);
		m_firstMonth = PropertiesReader.readInt(properties, PROPERTY_FIRST_MONTH, 
				DEFAULT_FIRST_MONTH);
		m_lastMonth = PropertiesReader.readInt(properties, PROPERTY_LAST_MONTH, 
				DEFAULT_LAST_MONTH);
		
		m_outputFilename = properties.getProperty(PROPERTY_OUTPUT_FILENAME, 
				DEFAULT_OUTPUT_FILENAME);
	}
	
	public void queryRelatedCarriers() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select asqp.code as primary_carrier,").append(NEWLINE);
			query.append("  rc.secondary_carrier").append(NEWLINE);
			query.append("from asqp_carriers_MIT asqp").append(NEWLINE);
			query.append("left join related_carriers_MIT rc").append(NEWLINE);
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
	
	public void queryCarrierHubs() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select carrier, airport, percent_operations").append(NEWLINE);
			query.append("from carrier_operations_MIT").append(NEWLINE);
			query.append("order by carrier asc, percent_operations desc");
			
			logger.trace("Carrier hubs query:");
			logger.trace(query.toString());
			
			String currentCarrier = null;
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String airport = rset.getString("airport");
				double percentOps = rset.getDouble("percent_operations");
				
				if (!carrier.equals(currentCarrier)) {
					m_carrierPrimaryAirportSet.add(new CarrierAirport(carrier, airport));
				}
				currentCarrier = carrier;
				if (percentOps >= 0.10) {
					m_carrierHubsSet.add(new CarrierAirport(carrier, airport));
				}
			}
		} catch (SQLException e) {
			exit("Unable to load carrier hub airports", e, -1);
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

	
	public void processItineraryDisruptions() throws InvalidFormatException {
		FileOutputStream outputStream = null;
		File outputFile = new File(m_outputDirectory, m_outputFilename);
		try {
			outputStream = new FileOutputStream(outputFile);
		}
		catch (FileNotFoundException e) {
			throw new InvalidFormatException("Unable to open output stream for file " +
					m_outputDirectory + File.separator + m_outputFilename);
		}
		PrintWriter writer = new PrintWriter(outputStream);
		try {
			writeDisruptionFeaturesHeader(writer);
			
			for (int month = m_firstMonth; month <= m_lastMonth; ++month) {
				logger.info("Processing itinerary disruptions for month " + month + "...");
				resetMonthlyStorage();

				queryNonStopItineraryData(month);

				queryOneStopItineraryData(month);
				
				queryAirportDepartureDelays(month);

				processItineraryDisruptions(writer, month);
				logger.info("Finished processing itinerary disruptions for month " + month + ".");
			}
		}
		finally {
			writer.flush();
			try {
				outputStream.close();
			}
			catch (IOException e) {
				throw new InvalidFormatException("Unable to close output stream for file " +
						m_outputDirectory + File.separator + m_outputFilename);
			}
		}	
	}
	
	public void writeDisruptionFeaturesHeader(PrintWriter writer) {
		writer.print("Month");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Number_Passengers");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Disruption_Origin");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Disruption_Carrier");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Hub_Flag");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Primary_Airport_Flag");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Average_Departure_Delay");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Cancelation_Rate");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Destination");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Actual_Disruption_Hour");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Estimated_Disruption_Hour");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Number_Flights_Remaining");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("First_Disruption_Cause");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Trip_Delay");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Non_Stop_Itineraries");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Non_Stop_Load_Factor");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("One_Stop_Itineraries");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Fullest_One_Stop_Load_Factor");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("Emptiest_One_Stop_Load_Factor");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("All_Non_Stop_Itineraries");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("All_Non_Stop_Load_Factor");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("All_One_Stop_Itineraries");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("All_Fullest_One_Stop_Load_Factor");
		writer.print(CSV_FIELD_DELIMITER);
		writer.print("All_Emptiest_One_Stop_Load_Factor");
		writer.println();
	}
	
	public void resetMonthlyStorage() {
		m_nonStopDataMap = new HashMap<NonStopKey, NonStopItineraryData>();
		m_oneStopDataMap = new HashMap<OneStopKey, OneStopItineraryData>();
		m_carrierAirportDataMap = new HashMap<CarrierAirport, PerformanceData>();
	}
	
	public void queryNonStopItineraryData(int month) {
		int quarter = getQuarterForMonth(month);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();		
			StringBuffer query = new StringBuffer();
			query.append("select it.first_operating_carrier as carrier,").append(NEWLINE);
			query.append("  it.origin, it.destination,").append(NEWLINE);
			query.append("  count(distinct it.planned_departure_time) ").append(NEWLINE);
			query.append("    as non_stop_itineraries,").append(NEWLINE);
			query.append("  sum(seg.passengers) / sum(seg.seats) as load_factor")
				.append(NEWLINE);
			query.append("from itineraries_MIT it").append(NEWLINE);
			query.append("join ").append(NEWLINE);
			query.append("(").append(NEWLINE);
			query.append(" select carrier, origin, destination, ").append(NEWLINE);
			query.append("   sum(passengers) as passengers,").append(NEWLINE);
			query.append("   sum(seats) as seats").append(NEWLINE);
			query.append(" from t100_segments_MIT").append(NEWLINE);
			query.append(" where year = ").append(m_year).append(NEWLINE);
			query.append("   and quarter = ").append(quarter).append(NEWLINE);
			query.append("   and month = ").append(month).append(NEWLINE);
			query.append("   and seats > 0").append(NEWLINE);
			query.append(" group by carrier, origin, destination").append(NEWLINE);
			query.append("").append(NEWLINE);
			query.append(") seg").append(NEWLINE);
			query.append("  on seg.carrier = it.first_operating_carrier").append(NEWLINE);
			query.append("  and seg.origin = it.origin").append(NEWLINE);
			query.append("  and seg.destination = it.destination").append(NEWLINE);
			query.append("where it.year = ").append(m_year).append(NEWLINE);
			query.append("  and it.quarter = ").append(quarter).append(NEWLINE);
			query.append("  and it.month = ").append(month).append(NEWLINE);
			query.append("  and it.num_flights = 1").append(NEWLINE);
			query.append("group by it.first_operating_carrier,").append(NEWLINE);
			query.append("  it.origin, it.destination");
			
			logger.trace("Non-stop itinerary data query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				
				NonStopKey oneCarrierKey = new NonStopKey(carrier, origin, destination);
				NonStopItineraryData oneCarrierData = 
					new NonStopItineraryData(oneCarrierKey);
				m_nonStopDataMap.put(oneCarrierKey, oneCarrierData);
				oneCarrierData.setNumItineraries(rset.getInt("non_stop_itineraries"));
				oneCarrierData.setLoadFactor(rset.getDouble("load_factor"));
				
				NonStopKey allCarriersKey = new NonStopKey(origin, destination);
				NonStopItineraryData allCarriersData =
					m_nonStopDataMap.get(allCarriersKey);
				if (allCarriersData == null) {
					allCarriersData = new NonStopItineraryData(allCarriersKey);
					m_nonStopDataMap.put(allCarriersKey, allCarriersData);
				}
				allCarriersData.addItineraryData(oneCarrierData);
			}
		} catch (SQLException e) {
			exit("Unable to load non-stop itinerary data", e, -1);
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
	
	public void queryOneStopItineraryData(int month) {
		int quarter = getQuarterForMonth(month);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select it.first_operating_carrier as first_carrier,").append(NEWLINE);
			query.append("  it.second_operating_carrier as second_carrier,").append(NEWLINE);
			query.append("  it.origin, it.destination,").append(NEWLINE);
			query.append("  count(distinct it.planned_departure_time) ").append(NEWLINE);
			query.append("    as one_stop_itineraries,").append(NEWLINE);
			query.append("  sum(decode(sign((seg1.passengers / seg1.seats) - ").append(NEWLINE);
			query.append("    (seg2.passengers / seg2.seats)), -1, ").append(NEWLINE);
			query.append("      seg2.passengers / seg2.seats,").append(NEWLINE);
			query.append("      seg1.passengers / seg1.seats)) / count(*)").append(NEWLINE);
			query.append("    as fullest_load_factor,").append(NEWLINE);
			query.append("  sum(decode(sign((seg1.passengers / seg1.seats) - ").append(NEWLINE);
			query.append("    (seg2.passengers / seg2.seats)), -1, ").append(NEWLINE);
			query.append("      seg1.passengers / seg1.seats,").append(NEWLINE);
			query.append("      seg2.passengers / seg2.seats)) / count(*) ").append(NEWLINE);
			query.append("    as emptiest_load_factor").append(NEWLINE);
			query.append("from itineraries_MIT it").append(NEWLINE);
			query.append("join ").append(NEWLINE);
			query.append("(").append(NEWLINE);
			query.append(" select carrier, origin, destination, ").append(NEWLINE);
			query.append("   sum(passengers) as passengers,").append(NEWLINE);
			query.append("   sum(seats) as seats").append(NEWLINE);
			query.append(" from t100_segments_MIT").append(NEWLINE);
			query.append(" where year = ").append(m_year).append(NEWLINE);
			query.append("   and quarter = ").append(quarter).append(NEWLINE);
			query.append("   and month = ").append(month).append(NEWLINE);
			query.append("   and seats > 0").append(NEWLINE);
			query.append(" group by carrier, origin, destination").append(NEWLINE);
			query.append("").append(NEWLINE);
			query.append(") seg1").append(NEWLINE);
			query.append("  on seg1.carrier = it.first_operating_carrier").append(NEWLINE);
			query.append("  and seg1.origin = it.origin").append(NEWLINE);
			query.append("  and seg1.destination = it.connection").append(NEWLINE);
			query.append("join ").append(NEWLINE);
			query.append("(").append(NEWLINE);
			query.append(" select carrier, origin, destination, ").append(NEWLINE);
			query.append("   sum(passengers) as passengers,").append(NEWLINE);
			query.append("   sum(seats) as seats").append(NEWLINE);
			query.append(" from t100_segments_MIT").append(NEWLINE);
			query.append(" where year = ").append(m_year).append(NEWLINE);
			query.append("   and quarter = ").append(quarter).append(NEWLINE);
			query.append("   and month = ").append(month).append(NEWLINE);
			query.append("   and seats > 0").append(NEWLINE);
			query.append(" group by carrier, origin, destination").append(NEWLINE);
			query.append("").append(NEWLINE);
			query.append(") seg2").append(NEWLINE);
			query.append("  on seg2.carrier = it.second_operating_carrier").append(NEWLINE);
			query.append("  and seg2.origin = it.connection").append(NEWLINE);
			query.append("  and seg2.destination = it.destination").append(NEWLINE);
			query.append("where it.year = ").append(m_year).append(NEWLINE);
			query.append("  and it.quarter = ").append(quarter).append(NEWLINE);
			query.append("  and it.month = ").append(month).append(NEWLINE);
			query.append("  and it.num_flights = 2").append(NEWLINE);
			query.append("group by it.first_operating_carrier,").append(NEWLINE); 
			query.append("  it.second_operating_carrier,").append(NEWLINE);
			query.append("  it.origin, it.destination");
			
			logger.trace("One-stop itinerary data query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String firstCarrier = rset.getString("first_carrier");
				String secondCarrier = rset.getString("second_carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				
				OneStopKey twoCarrierKey = 
					new OneStopKey(firstCarrier, secondCarrier, origin, destination);
				OneStopItineraryData twoCarrierData = new OneStopItineraryData(twoCarrierKey);
				m_oneStopDataMap.put(twoCarrierKey, twoCarrierData);
				twoCarrierData.setNumItineraries(rset.getInt("one_stop_itineraries"));
				twoCarrierData.setFullestLoadFactor(
						rset.getDouble("fullest_load_factor"));
				twoCarrierData.setEmptiestLoadFactor(
						rset.getDouble("emptiest_load_factor"));
				
				OneStopKey allCarriersKey = new OneStopKey(origin, destination);
				OneStopItineraryData allCarriersData =
					m_oneStopDataMap.get(allCarriersKey);
				if (allCarriersData == null) {
					allCarriersData = new OneStopItineraryData(allCarriersKey);
					m_oneStopDataMap.put(allCarriersKey, allCarriersData);
				}
				allCarriersData.addItineraryData(twoCarrierData);
			}
		} catch (SQLException e) {
			exit("Unable to load one-stop itinerary data", e, -1);
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
	
	public void queryAirportDepartureDelays(int month) {
		int quarter = getQuarterForMonth(month);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select carrier, origin, sum(decode(diverted_flag + cancelled_flag, 0,")
				.append(NEWLINE);
			query.append("  extract(day from actual_arrival_time - planned_arrival_time) * 24 * 60 +")
				.append(NEWLINE);
			query.append("  extract(hour from actual_arrival_time - planned_arrival_time) * 60 +")
				.append(NEWLINE);
			query.append("  extract(minute from actual_arrival_time - planned_arrival_time), 0))")
				.append(NEWLINE);
			query.append("    as total_delay,")
				.append(NEWLINE);
			query.append("  sum(decode(diverted_flag + cancelled_flag, 0, 1, 0))")
				.append(NEWLINE);
			query.append("    as flown_flights,")
				.append(NEWLINE);
			query.append("  sum(decode(diverted_flag + cancelled_flag, 0, 0, 1)) / count(*)")
				.append(NEWLINE);
			query.append("    as cancelation_rate").append(NEWLINE);
			query.append("from flights_MIT").append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month).append(NEWLINE);
			query.append("group by carrier, origin");
			
			logger.trace("Airport departure delays query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String airport = rset.getString("origin");
				double totalDelay = rset.getDouble("total_delay");
				int flownFlights = rset.getInt("flown_flights");
				double cancelationRate = rset.getDouble("cancelation_rate");
				double averageDelay = 0.0;
				if (flownFlights > 0) {
					averageDelay = totalDelay / flownFlights;
				}
				
				CarrierAirport key = new CarrierAirport(carrier, airport);
				PerformanceData data = new PerformanceData(averageDelay, cancelationRate);
				m_carrierAirportDataMap.put(key, data);
			}
		} catch (SQLException e) {
			exit("Unable to load one-stop itinerary data", e, -1);
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
	
	public void processItineraryDisruptions(PrintWriter writer, int month) {
		Calendar calendar = new GregorianCalendar(m_year, month - 1, 1);
		double daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int quarter = getQuarterForMonth(month);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select pd.planned_first_carrier, pd.planned_second_carrier,")
				.append(NEWLINE);
			query.append("  pd.planned_origin, pd.planned_destination,").append(NEWLINE);
			query.append("  pd.planned_num_flights, pd.first_disruption_cause,")
				.append(NEWLINE);
			query.append("  pd.disruption_origin_sequence,").append(NEWLINE);
			query.append("  to_number(to_char(ft1.planned_departure_time, 'HH24'))")
				.append(NEWLINE);
			query.append("    as first_departure_hour, ").append(NEWLINE);
			query.append("  decode(ft2.planned_departure_time, null, null,").append(NEWLINE);
			query.append("    to_number(to_char(ft2.planned_departure_time, 'HH24')))")
				.append(NEWLINE);
			query.append("    as second_departure_hour,").append(NEWLINE);
			query.append("  pd.first_disruption_hour as disruption_hour,").append(NEWLINE);
			query.append("  pd.num_passengers, pd.trip_delay").append(NEWLINE);
			query.append("from pax_delay_analysis_MIT pd").append(NEWLINE);
			query.append("join flights_MIT ft1").append(NEWLINE);
			query.append("  on ft1.id = pd.planned_first_flight_id").append(NEWLINE);
			query.append("left join flights ft2").append(NEWLINE);
			query.append("  on ft2.id = pd.planned_second_flight_id").append(NEWLINE);
			query.append("where pd.year = ").append(m_year).append(NEWLINE);
			query.append("  and pd.quarter = ").append(quarter).append(NEWLINE);
			query.append("  and pd.month = ").append(month).append(NEWLINE);
			query.append("  and pd.first_disruption_cause > 0").append(NEWLINE);
			query.append("  and pd.last_flown_flight_id is not null");
			
			logger.trace("Itinerary disruptions query:");
			logger.trace(query.toString());

			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String firstCarrier = rset.getString("planned_first_carrier");
				String secondCarrier = rset.getString("planned_second_carrier");
				String origin = rset.getString("planned_origin");
				String destination = rset.getString("planned_destination");
				int numFlights = rset.getInt("planned_num_flights");
				int disruptionCause = rset.getInt("first_disruption_cause");
				String disruptionOriginSequence = rset.getString("disruption_origin_sequence");
				StringTokenizer tok = new StringTokenizer(disruptionOriginSequence, ";");
				String disruptionOrigin = tok.nextToken();
				String disruptionCarrier;
				int estimatedDisruptionHour;
				int numFlightsRemaining;
				if (origin.equals(disruptionOrigin)) {
					estimatedDisruptionHour = rset.getInt("first_departure_hour");
					numFlightsRemaining = numFlights;
					disruptionCarrier = firstCarrier;
				}
				else {
					estimatedDisruptionHour = rset.getInt("second_departure_hour");
					numFlightsRemaining = 1;
					disruptionCarrier = secondCarrier;
				}
				int hubFlag = 0;
				int primaryAirportFlag = 0;
				CarrierAirport disruptionKey = 
					new CarrierAirport(disruptionCarrier, disruptionOrigin);
				if (m_carrierHubsSet.contains(disruptionKey)) {
					hubFlag = 1;
				}
				if (m_carrierPrimaryAirportSet.contains(disruptionKey)) {
					primaryAirportFlag = 1;
				}
				PerformanceData performanceData = m_carrierAirportDataMap.get(disruptionKey);
				int actualDisruptionHour = rset.getInt("disruption_hour");
				int numPassengers = rset.getInt("num_passengers");
				int tripDelay = rset.getInt("trip_delay");
				String[] firstSecondaryCarriers = m_relatedCarriersMap.get(firstCarrier);
				Set<String> recoveryCarriersSet = new HashSet<String>();
				recoveryCarriersSet.add(firstCarrier);
				for (int i = 0; i < firstSecondaryCarriers.length; ++i) {
					recoveryCarriersSet.add(firstSecondaryCarriers[i]);
				}
				if (secondCarrier != null && !secondCarrier.equals(firstCarrier)) {
					String[] secondSecondaryCarriers = m_relatedCarriersMap.get(secondCarrier);
					recoveryCarriersSet.add(secondCarrier);
					for (int i = 0; i < secondSecondaryCarriers.length; ++i) {
						recoveryCarriersSet.add(secondSecondaryCarriers[i]);
					}
				}
				String[] recoveryCarriers = new String[recoveryCarriersSet.size()];
				recoveryCarriersSet.toArray(recoveryCarriers);
				Arrays.sort(recoveryCarriers);
				
				NonStopItineraryData recoveryCarriersNonStopData = null;
				for (int i = 0; i < recoveryCarriers.length; ++i) {
					NonStopKey oneCarrierKey = new NonStopKey(recoveryCarriers[i], 
							disruptionOrigin, destination);
					NonStopItineraryData data = m_nonStopDataMap.get(oneCarrierKey);
					if (data != null) {
						if (recoveryCarriersNonStopData == null) {
							recoveryCarriersNonStopData = new NonStopItineraryData(
									new NonStopKey(disruptionOrigin, destination));
						}
						recoveryCarriersNonStopData.addItineraryData(data);
					}
				}
				NonStopItineraryData allCarriersNonStopData =
					m_nonStopDataMap.get(new NonStopKey(disruptionOrigin, destination));
				
				OneStopItineraryData recoveryCarriersOneStopData = null;
				for (int i = 0; i < recoveryCarriers.length; ++i) {
					for (int j = i; j < recoveryCarriers.length; ++j) {
						OneStopKey twoCarrierKey = new OneStopKey(recoveryCarriers[i],
								recoveryCarriers[j], disruptionOrigin, destination);
						OneStopItineraryData data = m_oneStopDataMap.get(twoCarrierKey);
						if (data != null) {
							if (recoveryCarriersOneStopData == null) {
								recoveryCarriersOneStopData = new OneStopItineraryData(
										new OneStopKey(disruptionOrigin, destination));
							}
							recoveryCarriersOneStopData.addItineraryData(data);
						}
					}
				}
				OneStopItineraryData allCarriersOneStopData =
					m_oneStopDataMap.get(new OneStopKey(disruptionOrigin, destination));

				String doubleFormat = "%1$05.4f";
				// Now, write out the corresponding line in the data file
				writer.print(month);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(numPassengers);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(disruptionOrigin);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(disruptionCarrier);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(hubFlag);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(primaryAirportFlag);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(performanceData.getAverageDelay());
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(performanceData.getCancelationRate());
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(destination);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(actualDisruptionHour);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(estimatedDisruptionHour);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(numFlightsRemaining);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(disruptionCause);
				writer.print(CSV_FIELD_DELIMITER);
				writer.print(tripDelay);
				writer.print(CSV_FIELD_DELIMITER);
				if (recoveryCarriersNonStopData != null) {
					writer.print(String.format(doubleFormat,
							recoveryCarriersNonStopData.getNumItineraries() / daysInMonth));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (recoveryCarriersNonStopData != null) {
					writer.print(String.format(doubleFormat, 
							recoveryCarriersNonStopData.getLoadFactor()));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (recoveryCarriersOneStopData != null) {
					writer.print(String.format(doubleFormat,
							recoveryCarriersOneStopData.getNumItineraries() / daysInMonth));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (recoveryCarriersOneStopData != null) {
					writer.print(String.format(doubleFormat, 
							recoveryCarriersOneStopData.getFullestLoadFactor()));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (recoveryCarriersOneStopData != null) {
					writer.print(String.format(doubleFormat, 
							recoveryCarriersOneStopData.getEmptiestLoadFactor()));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (allCarriersNonStopData != null) {
					writer.print(String.format(doubleFormat,
							allCarriersNonStopData.getNumItineraries() / daysInMonth));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (allCarriersNonStopData != null) {
					writer.print(String.format(doubleFormat, 
							allCarriersNonStopData.getLoadFactor()));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (allCarriersOneStopData != null) {
					writer.print(String.format(doubleFormat,
							allCarriersOneStopData.getNumItineraries() / daysInMonth));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (allCarriersOneStopData != null) {
					writer.print(String.format(doubleFormat, 
							allCarriersOneStopData.getFullestLoadFactor()));
				} else {
					writer.print(0);
				}
				writer.print(CSV_FIELD_DELIMITER);
				if (allCarriersOneStopData != null) {
					writer.print(String.format(doubleFormat, 
							allCarriersOneStopData.getEmptiestLoadFactor()));
				} else {
					writer.print(0);
				}
				writer.println();
			}
		} catch (SQLException e) {
			exit("Unable to load itinerary disruption data", e, -1);
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
	
	protected static void exit(String message, Exception e, int code) {
		logger.fatal(message, e);
		System.exit(code);
	}

	protected void connectToDatabase() {
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

	protected static int getQuarterForMonth(int month) {
		return (int) Math.floor((month + 2) / 3.0);
	}	
}

class CarrierAirport {
	String m_carrier;
	String m_airport;
	
	public CarrierAirport(String carrier, String airport) {
		m_carrier = carrier;
		m_airport = airport;
	}

	public String getCarrier() {
		return m_carrier;
	}

	public String getAirport() {
		return m_airport;
	}
	
	@Override
	public boolean equals(Object that) {
		if (that instanceof CarrierAirport) {
			CarrierAirport thatCarrierAirport = (CarrierAirport) that;
			if (m_carrier.equals(thatCarrierAirport.getCarrier()) &&
					m_airport.equals(thatCarrierAirport.getAirport())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return m_carrier + "_" + m_airport;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
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
	
	int m_numItineraries;
	double m_loadFactor;
	
	public NonStopItineraryData(NonStopKey itineraryKey) {
		m_itineraryKey = itineraryKey;
	}

	public int getNumItineraries() {
		return m_numItineraries;
	}

	public void setNumItineraries(int numItineraries) {
		m_numItineraries = numItineraries;
	}
	
	public double getLoadFactor() {
		return m_loadFactor;
	}
	
	public void setLoadFactor(double loadFactor) {
		m_loadFactor = loadFactor;
	}
	
	public void addItineraryData(NonStopItineraryData data) {
		int numNewItineraries = data.getNumItineraries();
		m_loadFactor = ((m_numItineraries * m_loadFactor) + 
				(numNewItineraries * data.getLoadFactor())) / 
				(m_numItineraries + numNewItineraries);
		m_numItineraries += numNewItineraries;
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
	
	int m_numItineraries;
	double m_fullestLoadFactor;
	double m_emptiestLoadFactor;
	
	public OneStopItineraryData(OneStopKey itineraryKey) {
		m_itineraryKey = itineraryKey;
	}

	public int getNumItineraries() {
		return m_numItineraries;
	}

	public void setNumItineraries(int numItineraries) {
		m_numItineraries = numItineraries;
	}

	public double getFullestLoadFactor() {
		return m_fullestLoadFactor;
	}

	public void setFullestLoadFactor(double fullestLoadFactor) {
		m_fullestLoadFactor = fullestLoadFactor;
	}

	public double getEmptiestLoadFactor() {
		return m_emptiestLoadFactor;
	}

	public void setEmptiestLoadFactor(double emptiestLoadFactor) {
		m_emptiestLoadFactor = emptiestLoadFactor;
	}

	public OneStopKey getItineraryKey() {
		return m_itineraryKey;
	}
	
	public void addItineraryData(OneStopItineraryData data) {
		int numNewItineraries = data.getNumItineraries();
		m_fullestLoadFactor = ((m_numItineraries * m_fullestLoadFactor) +
				(numNewItineraries * data.getFullestLoadFactor())) /
				(m_numItineraries + numNewItineraries);
		m_emptiestLoadFactor = ((m_numItineraries * m_emptiestLoadFactor) +
				(numNewItineraries * data.getEmptiestLoadFactor())) /
				(m_numItineraries + numNewItineraries);
		m_numItineraries += numNewItineraries;
	}
}

class PerformanceData {
	
	double m_averageDelay;
	double m_cancelationRate;
	
	public PerformanceData(double averageDelay, double cancelationRate) {
		m_averageDelay = averageDelay;
		m_cancelationRate = cancelationRate;
	}

	public double getAverageDelay() {
		return m_averageDelay;
	}

	public double getCancelationRate() {
		return m_cancelationRate;
	}
}
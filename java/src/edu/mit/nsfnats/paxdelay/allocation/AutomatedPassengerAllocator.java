//SUKITJANUPARP 
//to generate .txt file that will be used in CreateItineraryLoad_Alldata.java
//need to add properties file in arguments before start running: AutomatedPassengerAllocator and DefaultLogger
//this code was modified from the original version by changing from sql syntax to mysql syntax
//XuJiao
//Change the default maximum connection time from 180 min to 300 min
//Ignore the LoadAircraftFleetType in the java and NUMBER_ALLOCATION_PARAMETERS in the properties
//Comment unnecessary printing and external files writing
//It took 270 min


package edu.mit.nsfnats.paxdelay.allocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import edu.mit.nsfnats.paxdelay.util.RandomValuesComparator;

import oracle.jdbc.pool.OracleDataSource;


public class AutomatedPassengerAllocator {
	public static Logger logger = Logger
			.getLogger(AutomatedPassengerAllocator.class);

	public static final String PROPERTY_JDBC_URL = "JDBC_URL";
	public static final String PROPERTY_DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String PROPERTY_DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public static final String PROPERTY_SCALED_DEMAND_TABLE = "SCALED_DEMAND_TABLE";
	public static final String PROPERTY_SEGMENT_DEMAND_TABLE = "SEGMENT_DEMAND_TABLE";
	public static final String PROPERTY_ONE_STOP_PERCENT_TABLE = "ONE_STOP_PERCENT_TABLE";
	public static final String PROPERTY_FLIGHTS_TABLE = "FLIGHTS_TABLE";
	public static final String PROPERTY_ITINERARIES_TABLE = "ITINERARIES_TABLE";
	public static final String DEFAULT_SCALED_DEMAND_TABLE = "t100_db1b_route_demands";
	public static final String DEFAULT_SEGMENT_DEMAND_TABLE = "t100_segments";
	public static final String DEFAULT_ONE_STOP_PERCENT_TABLE = "db1b_route_demands";
	public static final String DEFAULT_FLIGHTS_TABLE = "flights";
	public static final String DEFAULT_ITINERARIES_TABLE = "itineraries";

	public static int DEFAULT_FETCH_SIZE = 5000;

	public static final String PROPERTY_UTILITY_CALCULATOR_CLASS = "UTILITY_CALCULATOR_CLASS";
	public static final String DEFAULT_UTILITY_CALCULATOR_CLASS = "edu.mit.nsfnats.paxdelay.allocation.FixedEffectsUtilityCalculator";
	
	public static final String PROPERTY_ALLOCATION_RANDOM_SEED = "ALLOCATION_RANDOM_SEED";
	public static final String PROPERTY_ALLOCATION_OUTPUT_DIRECTORY = "ALLOCATION_OUTPUT_DIRECTORY";

	public static final String PROPERTY_ALLOCATION_YEAR = "ALLOCATION_YEAR";
	public static final String PROPERTY_ALLOCATION_FIRST_MONTH = "ALLOCATION_FIRST_MONTH";
	public static final String PROPERTY_ALLOCATION_LAST_MONTH = "ALLOCATION_LAST_MONTH";
	public static final int DEFAULT_ALLOCATION_YEAR = 2013;

	public static final String PROPERTY_ALLOCATION_CARRIER_PREFIX = "ALLOCATION_CARRIER";

	public static final String PROPERTY_DEVIATION_CAPACITY_COEFFICIENT = "DEVIATION_CAPACITY_COEFFICIENT";
	public static final double DEFAULT_DEVIATION_CAPACITY_COEFFICIENT = 1.0;

	public static final String PROPERTY_MAXIMUM_CONNECTION_TIME = "MAXIMUM_CONNECTION_TIME";
//	public static final int DEFAULT_MAXIMUM_CONNECTION_TIME = 180;
	public static final int DEFAULT_MAXIMUM_CONNECTION_TIME = 300;
	
	public static final double MAXIMUM_DEFAULT_SEATING_CAPACITY = 999.0;

	public static final String NEWLINE = "\n";

	public static final double EPSILON = 0.000001;

	// The next four are the only inputs necessary
	int m_year;
	int m_firstMonth;
	int m_lastMonth;

	String[] m_carriers;
	Map<String, InternalCarrier> m_ASQPCarrierMap;
	Set<String> m_ASQPCarrierCodes;
	Map<String, InternalAircraftFleet> m_aircraftFleetMap;

	List<InternalFlight> m_rolloverFlights;

	// Storage structures used for monthly passenger allocation
	Map<Integer, InternalFlight> m_idFlightMap;
	Map<String, InternalSegment> m_keySegmentMap;
	Map<String, InternalRoute> m_keyRouteMap;

	ItineraryUtilityCalculator m_utilityCalculator;
	String m_allocationOutputDirectory;

	String m_jdbcURL;
	String m_dbUsername;
	String m_dbPassword;

	String m_scaledRouteDemandTable;
	String m_segmentDemandTable;
	String m_oneStopPercentTable;
	String m_flightsTable;
	String m_itinerariesTable;

	OracleDataSource m_datasource;
	Connection m_dbConnection;

	double m_deviationCapacityCoefficient;
	double[] m_quarterlyOneStopScalingFactors;
	int m_maximumConnectionTime;

	Random m_randomNumberGenerator;

	int m_allocatedPassengers;
	int m_noRoutePassengers;
	int m_noSeatPassengers;

	public AutomatedPassengerAllocator() {
		m_randomNumberGenerator = new Random();

		m_allocatedPassengers = 0;
		m_noRoutePassengers = 0;
		m_noSeatPassengers = 0;

		m_ASQPCarrierMap = new HashMap<String, InternalCarrier>();
		m_rolloverFlights = new ArrayList<InternalFlight>();

		m_ASQPCarrierCodes = new HashSet<String>();
		m_aircraftFleetMap = new HashMap<String, InternalAircraftFleet>();
	}

	public static void main(String[] args) {
		long startTime = System.nanoTime();
//		if (args.length != 2) {
//			System.err
//					.println("Usage: java edu.mit.nsfnats.paxdelay.choice.AutomatedPassengerAllocator "
//							+ "<logger_properties_file> <allocation_properties_file>");
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
//		Properties allocationProperties = null;
//		try {
//			allocationProperties = PropertiesReader.loadProperties(args[1]);
//		} catch (FileNotFoundException e) {
//			exit("Allocation properties file not found.", e, -1);
//		} catch (IOException e) {
//			exit(
//					"Received IO exception while reading allocation properties file.",
//					e, -1);
//		}
//		main(allocationProperties);
		
		Properties loggerProperties = null;
		try {
			loggerProperties = PropertiesReader.loadProperties("/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/resources/config/desktop/DefaultLogger.properties");
		} catch (FileNotFoundException e) {
			exit("Logger properties file not found.", e, -1);
		} catch (IOException e) {
			exit("Received IO exception while reading logger properties file.",
					e, -1);
		}
		PropertyConfigurator.configure(loggerProperties);

		Properties allocationProperties = null;
		try {
			allocationProperties = PropertiesReader.loadProperties("/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/resources/config/desktop/AutomatedPassengerAllocator.properties");
		} catch (FileNotFoundException e) {
			exit("Allocation properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading allocation properties file.",
					e, -1);
		}
		main(allocationProperties);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000/1000/60;
		System.out.println("That took " + duration + " minutes ");
	}

	public static void main(Properties properties) {
		AutomatedPassengerAllocator pa = new AutomatedPassengerAllocator();
		try {
			pa.initialize(properties);
		} catch (InvalidFormatException e) {
			exit("Invalid format specified in allocation properties file", e,
					-1);
		}

		logger.info("Initializing annual data for allocation");
		pa.connectToDatabase();
		logger.trace("Connected to database");
		pa.loadASQPCarriers();
		logger.trace("Loaded list of ASQP carriers");
		//pa.loadAircraftFleetTypes();
		logger.trace("Loaded aircraft fleet types");
		pa.loadOneStopScalingFactors();
		logger.trace("Loaded one stop scaling factors");
		pa.allocatePassengersThroughSampling();
		logger.trace("Finished allocating all passengers");

		pa.disconnectFromDatabase();
	}
	
	@SuppressWarnings("unchecked")
	public void initialize(Properties properties) throws InvalidFormatException {
		m_jdbcURL = properties.getProperty(PROPERTY_JDBC_URL);
		m_dbUsername = properties.getProperty(PROPERTY_DATABASE_USERNAME);
		m_dbPassword = properties.getProperty(PROPERTY_DATABASE_PASSWORD);

		m_year = PropertiesReader.readInt(properties, PROPERTY_ALLOCATION_YEAR,
				DEFAULT_ALLOCATION_YEAR);
		m_carriers = PropertiesReader.readStrings(properties,
				PROPERTY_ALLOCATION_CARRIER_PREFIX);
		System.out.println("m_carriers**");
		m_firstMonth = PropertiesReader.readInt(properties,
				PROPERTY_ALLOCATION_FIRST_MONTH);
		m_lastMonth = PropertiesReader.readInt(properties,
				PROPERTY_ALLOCATION_LAST_MONTH);
		m_scaledRouteDemandTable = properties.getProperty(
				PROPERTY_SCALED_DEMAND_TABLE, DEFAULT_SCALED_DEMAND_TABLE);
		m_segmentDemandTable = properties.getProperty(
				PROPERTY_SEGMENT_DEMAND_TABLE, DEFAULT_SEGMENT_DEMAND_TABLE);
		m_oneStopPercentTable = properties
				.getProperty(PROPERTY_ONE_STOP_PERCENT_TABLE,
						DEFAULT_ONE_STOP_PERCENT_TABLE);
		m_flightsTable = properties.getProperty(PROPERTY_FLIGHTS_TABLE,
				DEFAULT_FLIGHTS_TABLE);
		m_itinerariesTable = properties.getProperty(PROPERTY_ITINERARIES_TABLE,
				DEFAULT_ITINERARIES_TABLE);
		System.out.println("m_segmentDemandTable= "+m_segmentDemandTable);
		String seedString = properties.getProperty(PROPERTY_ALLOCATION_RANDOM_SEED);
		if (seedString != null) {
			long allocationSeed = Long.parseLong(seedString);
			m_randomNumberGenerator.setSeed(allocationSeed);
		}

		m_allocationOutputDirectory = properties
				.getProperty(PROPERTY_ALLOCATION_OUTPUT_DIRECTORY);

		m_deviationCapacityCoefficient = PropertiesReader.readDouble(
				properties, PROPERTY_DEVIATION_CAPACITY_COEFFICIENT,
				DEFAULT_DEVIATION_CAPACITY_COEFFICIENT);

		m_maximumConnectionTime = PropertiesReader.readInt(properties,
				PROPERTY_MAXIMUM_CONNECTION_TIME,
				DEFAULT_MAXIMUM_CONNECTION_TIME);
		
		String utilityCalculatorClass = properties.getProperty(PROPERTY_UTILITY_CALCULATOR_CLASS,
				DEFAULT_UTILITY_CALCULATOR_CLASS);
		Class<ItineraryUtilityCalculator> clazz = null;
		try {
			clazz = (Class<ItineraryUtilityCalculator>) Class.forName(utilityCalculatorClass);
		} catch (ClassNotFoundException e) {
			throw new InvalidFormatException("Utility calculator " + utilityCalculatorClass
					+ " does not exist", e);
		}
		
		try {
			m_utilityCalculator = clazz.newInstance();
		}
		catch (IllegalAccessException e) {
			throw new InvalidFormatException("Unable to access default constructor on utility calculator " 
					+ utilityCalculatorClass, e);
		}
		catch (InstantiationException e) {
			throw new InvalidFormatException("Unable to instantiate utility calculator " 
					+ utilityCalculatorClass, e);
		}
		m_utilityCalculator.initialize(properties);
	}

	public void connectToDatabase() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}      
		try {
			//m_datasource = new OracleDataSource();
			//m_datasource.setURL(m_jdbcURL);
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

	public void loadASQPCarriers() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select code from asqp_carriers");
			logger.trace("ASQP carriers query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {
				String carrierCode = rset.getString("code");
				m_ASQPCarrierCodes.add(carrierCode);
				InternalCarrier carrier = new InternalCarrier(carrierCode);
				m_ASQPCarrierMap.put(carrierCode, carrier);		
			}
		} catch (SQLException e) {
			exit("Unable to load set of ASQP carriers", e, -1);
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
	
	public void loadAircraftFleetTypes() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
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
			
			logger.trace("Aircraft fleet types query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String aircraftType = rset.getString("icao_code");
				int numSeats = rset.getInt("number_of_seats");
				
				InternalAircraftFleet aircraftFleet =
					getInternalAircraftFleet(carrier, aircraftType);
				System.out.println("aircraftFleet: "+ aircraftFleet);
				if (aircraftFleet == null) {
					System.out.println("call createInternalAircraftFleet");
					aircraftFleet = createInternalAircraftFleet(carrier, aircraftType);
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

	public void loadOneStopScalingFactors() {
		Statement stmt = null;
		ResultSet rset = null;
		m_quarterlyOneStopScalingFactors = new double[4];
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select seg.quarter,").append(NEWLINE);
			query.append("  db1b.percent_one_stop * seg.passenger_segments")
					.append(NEWLINE);
			query.append(
					"    / scaled_db1b.one_stop_segments as scaling_factor")
					.append(NEWLINE);
			query.append("from (").append(NEWLINE);
			query.append(
					" select quarter, sum(passengers) as passenger_segments")
					.append(NEWLINE);
			query.append(" from ").append(m_segmentDemandTable)
					.append(NEWLINE);
			query.append(" group by quarter").append(NEWLINE);
			query.append(") seg").append(NEWLINE);
			query.append("join (").append(NEWLINE);
			query
					.append(
							" select quarter, sum(IF(num_flights= 2, 2, 0) * passengers)")
					.append(NEWLINE);
			query.append(
					"   / sum(num_flights * passengers) as percent_one_stop")
					.append(NEWLINE);
			query.append(" from ").append(m_oneStopPercentTable)
					.append(NEWLINE);
			query.append(" where passengers > 0").append(NEWLINE);
			query.append(" group by quarter").append(NEWLINE);
			query.append(") db1b").append(NEWLINE);
			query.append("on db1b.quarter = seg.quarter").append(NEWLINE);
			query.append("join (").append(NEWLINE);
			query.append(" select quarter,").append(NEWLINE);
			query.append(
					"   sum(num_flights * passengers) as one_stop_segments")
					.append(NEWLINE);
			query.append(" from ").append(m_scaledRouteDemandTable)
					.append(NEWLINE);
			query.append(" where num_flights > 1").append(NEWLINE);
			query.append(" group by quarter").append(NEWLINE);
			query.append(") scaled_db1b").append(NEWLINE);
			query.append("on scaled_db1b.quarter = seg.quarter")
					.append(NEWLINE);
			query.append("order by seg.quarter");
            //System.out.println(query);
			logger.trace("Scaling factors query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {
				int quarter = rset.getInt("quarter");
				double scalingFactor = rset.getDouble("scaling_factor");
				m_quarterlyOneStopScalingFactors[quarter - 1] = scalingFactor;
			}

			stmt.close();

		} catch (SQLException e) {
			exit("Unable to load quarterly DB1B scaling factors", e, -1);
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

	public void allocatePassengersThroughSampling() {
		for (int month = m_firstMonth; month <= m_lastMonth; ++month) {
			logger.info("Allocating passengers for month " + month);

			initializeMonthlyStorage();
			getDefaultSeatingCapacities(month);
			getFlightSeatingCapacities(month);
			loadSegmentDemands(month);

			// First, allocate passengers to the multiple carrier itineraries
			System.out.println("size: "+m_carriers.length);
			for (int i = 0; i < m_carriers.length; ++i) {
				String carrierCode = m_carriers[i];
				System.out.println(i+"----"+carrierCode+"###");
				InternalRouteSet routeSet = queryMonthlyItineraries(month,
						carrierCode, true);
				loadOneStopRouteDemands(month, carrierCode, true);
				int numPassengers = allocateRouteSetPassengers(routeSet, 
						Integer.toString(month), carrierCode, true);
				writePassengerAllocationFiles(routeSet, String.format("%1$02d", month), 
						carrierCode, true);
				m_allocatedPassengers += numPassengers;
			}

			// Next, allocate passengers to the single carrier itineraries
			for (int i = 0; i < m_carriers.length; ++i) {
				String carrierCode = m_carriers[i];
				System.out.println(i+"----"+carrierCode+"***");
				System.out.println("check "+queryMonthlyItineraries(month,
						carrierCode, false));
				InternalRouteSet routeSet = queryMonthlyItineraries(month,
						carrierCode, false);
				System.out.println("size routeset***"+routeSet.numRoutes());
				System.out.println("***"+routeSet);
				loadOneStopRouteDemands(month, carrierCode, false);
				int numPassengers = allocateRouteSetPassengers(routeSet, 
						Integer.toString(month), carrierCode, false);
				writePassengerAllocationFiles(routeSet, String.format("%1$02d", month), 
						carrierCode, false);
				m_allocatedPassengers += numPassengers;
			}
			logger.info("Completed allocating passengers for month " + month);
		}
		logger.info("Total passengers allocated = " + m_allocatedPassengers);
		logger.info("Passengers with no matching route = "
				+ m_noRoutePassengers);
		logger
				.info("Passengers with no seat available = "
						+ m_noSeatPassengers);
	}

	public void initializeMonthlyStorage() {
		m_idFlightMap = new HashMap<Integer, InternalFlight>(700000);
		m_keySegmentMap = new HashMap<String, InternalSegment>(50000);
		m_keyRouteMap = new HashMap<String, InternalRoute>(350000);
		Iterator<InternalCarrier> carrierIter = m_ASQPCarrierMap.values().iterator();
		while (carrierIter.hasNext()) {
			carrierIter.next().resetDefaultCapacity();
		}
	}

	public void getDefaultSeatingCapacities(int month) {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select carrier, origin, destination,").append(NEWLINE);
			query.append("  departures_performed, seats_mean,").append(NEWLINE);
			query.append("  seats_squared_mean, seats_std_dev").append(NEWLINE);
			query.append("from t100_seats").append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and month = ").append(month);

			logger.trace("Default seating capacity query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			while (rset.next()) {
				String carrierCode = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				InternalSegment segment = createInternalSegment(carrierCode,
						origin, destination);
				double mean = rset.getDouble("seats_mean");
				double squaredMean = rset.getDouble("seats_squared_mean");
				double stdDev = rset.getDouble("seats_std_dev");
				int departures = rset.getInt("departures_performed");
				segment.setDefaultCapacity(mean, stdDev);
				InternalCarrier carrier = m_ASQPCarrierMap.get(carrierCode);
				// We only need to track seating capacities for ASQP carriers
				if (carrier != null) {
					carrier.addDefaultCapacity(mean, squaredMean, departures);
				}
			}
		} catch (SQLException e) {
			exit("Unable to load default flight seating capacities for month "
					+ month, e, -1);
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
		logger.trace("Number of segment default seating capacities for month "
				+ month + " = " + m_keySegmentMap.size());
	}

	public void getFlightSeatingCapacities(int month) {
		int numFlightsMissingCapacity = 0;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			int quarter = getQuarterForMonth(month);
			StringBuffer query = new StringBuffer();
			query.append("select id, carrier, month, day_of_month,").append(
					NEWLINE);
			query.append("  origin, destination,  seating_capacity,").append(
					NEWLINE);
			query
					.append(
							"  DATE_FORMAT(planned_departure_time, '%h:%i:%s') as departure_time,")
					.append(NEWLINE);
			query
					.append(
							"  DATE_FORMAT(planned_arrival_time, '%h:%i:%s') as arrival_time,")
					.append(NEWLINE);
			query.append("  cancelled_flag").append(NEWLINE);
			query.append("from ").append(m_flightsTable).append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month);
			if (month < 12) {
				int nextQuarter = getQuarterForMonth(month + 1);
				query.append(NEWLINE);
				query.append("union all").append(NEWLINE);
				query.append("select id, carrier, month, day_of_month,")
						.append(NEWLINE);
				query.append("  origin, destination,  seating_capacity,")
						.append(NEWLINE);
				query
						.append(
								"  DATE_FORMAT(planned_departure_time, '%h:%i:%s') as departure_time,")
						.append(NEWLINE);
				query
						.append(
								"  DATE_FORMAT(planned_arrival_time, '%h:%i:%s') as arrival_time,")
						.append(NEWLINE);
				query.append("  cancelled_flag").append(NEWLINE);
				query.append("from ").append(m_flightsTable).append(NEWLINE);
				query.append("where year = ").append(m_year).append(NEWLINE);
				query.append("  and quarter = ").append(nextQuarter).append(
						NEWLINE);
				query.append("  and month = ").append(month + 1);
				query.append("  and day_of_month = 1");
			}
				// XuJ 060417: Add the last day of previous month, because there are some circumstances that the itineraries go from 02/01/10 to 01/31/10 and then 02/01/10
				// XuJ 060417: To decrease the coding difficulties, instead defining the last date of the month (28,29,30, or 31), add the date after 28th
			if (month > 1) {
				int previousQuarter = getQuarterForMonth(month - 1);
				query.append(NEWLINE);
				query.append("union all").append(NEWLINE);
				query.append("select id, carrier, month, day_of_month,")
						.append(NEWLINE);
				query.append("  origin, destination,  seating_capacity,")
						.append(NEWLINE);
				query
						.append(
								"  DATE_FORMAT(planned_departure_time, '%h:%i:%s') as departure_time,")
						.append(NEWLINE);
				query
						.append(
								"  DATE_FORMAT(planned_arrival_time, '%h:%i:%s') as arrival_time,")
						.append(NEWLINE);
				query.append("  cancelled_flag").append(NEWLINE);
				query.append("from ").append(m_flightsTable).append(NEWLINE);
				query.append("where year = ").append(m_year).append(NEWLINE);
				query.append("  and quarter = ").append(previousQuarter).append(
						NEWLINE);
				query.append("  and month = ").append(month - 1);
				query.append("  and day_of_month >= 28");
			}
			
			logger.trace("Flight seating capacities query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());

			int count = 0;
			List<InternalFlight> newRolloverFlights = new ArrayList<InternalFlight>();
			while (rset.next()) {
				int flightID = rset.getInt("id");
				String carrierCode = rset.getString("carrier");
				int flightMonth = rset.getInt("month");
				int dayOfMonth = rset.getInt("day_of_month");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				String departureTime = rset.getString("departure_time");
				String arrivalTime = rset.getString("arrival_time");
				boolean cancelledFlag = rset.getBoolean("cancelled_flag");

				InternalFlight flight = new InternalFlight(flightID,
						flightMonth, dayOfMonth, carrierCode, origin, destination,
						departureTime, arrivalTime, cancelledFlag);
				m_idFlightMap.put(flightID, flight);
				if (flightMonth == month + 1) {
					newRolloverFlights.add(flight);
				}

				//String aircraftType = rset.getString("icao_aircraft_code");
				String aircraftType = "";
				double seats = rset.getDouble("seating_capacity");
				double modelSeats = seats;
				double allocationSeats = seats;
				if (seats < 1) {
					InternalSegment segment = getInternalSegment(carrierCode,
							origin, destination);
					double seatsDeviation = 0.0;
					if (segment != null && segment.getSeatsMean() > 0) {
						seats = segment.getSeatsMean();
						seatsDeviation = segment.getSeatsDeviation();
					} else {
						InternalCarrier carrier = m_ASQPCarrierMap.get(carrierCode);
						seats = carrier.getSeatsMean();
						seatsDeviation = carrier.getSeatsDeviation();
					}
					if (seats < 1) {
						logger
							.error("Default seating capacity missing for flight ID "
								+ flightID);
						++numFlightsMissingCapacity;
						seats = MAXIMUM_DEFAULT_SEATING_CAPACITY;
						seatsDeviation = 0.0;
					}
					modelSeats = seats;
					allocationSeats = seats + m_deviationCapacityCoefficient * seatsDeviation;
					
					InternalAircraftFleet aircraftFleet = 
						getInternalAircraftFleet(carrierCode, aircraftType);
					if (aircraftFleet != null) {
						// If we are estimating the seating capacity from T-100, we need
						// to ensure that the model and allocation seats are between the 
						// minimum and maximum seating capacity for the fleet type
						modelSeats = Math.max(modelSeats, 
								aircraftFleet.getMinimumSeatingCapacity());
						modelSeats = Math.min(modelSeats, 
								aircraftFleet.getMaximumSeatingCapacity());
						
						allocationSeats = Math.max(allocationSeats, 
								aircraftFleet.getMinimumSeatingCapacity());
						allocationSeats = Math.min(allocationSeats, 
								aircraftFleet.getMaximumSeatingCapacity());
					}
				}
				flight.setModelCapacity(modelSeats);
				flight.setAllocationCapacity(allocationSeats);
				count++;
				if (count % 25000 == 0) {
					logger.trace("Obtained flight seating capacities for "
							+ count + " flights");
				}
			}

			// Passengers that rollover from the previous month should be
			// allocated
			// to the appropriate flights when processing the current month
			Iterator<InternalFlight> rolloverFlightIter = m_rolloverFlights
					.iterator();
			while (rolloverFlightIter.hasNext()) {
				InternalFlight rolloverFlight = rolloverFlightIter.next();

				InternalFlight flight = m_idFlightMap.get(rolloverFlight
						.getFlightID());
				flight.allocatePassengers(rolloverFlight
						.getNumberPassengersAllocated());
			}
			m_rolloverFlights = newRolloverFlights;
		} catch (SQLException e) {
			exit("Unable to load flight seating capacities for month " + month,
					e, -1);
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
		logger.info("Number of flights for month " + month + " = "
				+ m_idFlightMap.size());
		logger.info("Number of flight missing capacity for month " + month
				+ " = " + numFlightsMissingCapacity);
	}

	public void loadSegmentDemands(int month) {
		int numSegments = 0;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			int quarter = getQuarterForMonth(month);
			StringBuffer query = new StringBuffer();
			query.append("select carrier, origin, destination,")
					.append(NEWLINE);
			query.append("  sum(passengers) as passengers").append(NEWLINE);
			query.append("from ").append(m_segmentDemandTable).append(
					NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month).append(NEWLINE);
			query.append("group by carrier, origin, destination");

			logger.trace("Segment demand query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String carrier = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				InternalSegment segment = getInternalSegment(carrier, origin,
						destination);
				if (segment == null) {
					segment = createInternalSegment(carrier, origin,
							destination);
				}
				int passengers = rset.getInt("passengers");
				segment.setPassengerCapacity(passengers);
				numSegments++;
			}
		} catch (SQLException e) {
			exit("Unable to load segment demands for month " + month, e, -1);
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
		logger.info("Number of matching segments for month " + month + " = "
				+ numSegments);
	}

	public InternalRouteSet queryMonthlyItineraries(int month,
			String carrierCode, boolean multiCarrier) {
		List<InternalRoute> nonStopList = new ArrayList<InternalRoute>();
		List<InternalRoute> oneStopList = new ArrayList<InternalRoute>();
		int numItineraries = 0;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();

			int quarter = getQuarterForMonth(month);
			StringBuffer query = new StringBuffer();
			query
					.append(
							"select id, origin, connection, destination, first_operating_carrier,")
					.append(NEWLINE);
			query
					.append(
							"  second_operating_carrier, first_flight_id, second_flight_id,")
					.append(NEWLINE);
			query
					.append(
							"  day_of_week, hour_of_day, minutes_of_hour, layover_duration,")
					.append(NEWLINE);
			query.append(" HOUR(planned_departure_time) as origin_tz, ").append(NEWLINE);
			query.append(" HOUR(planned_arrival_time) as destination_tz ").append(NEWLINE);
			query.append("from ").append(m_itinerariesTable).append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month).append(NEWLINE);
			query.append("  and first_operating_carrier = '").append(
					carrierCode).append("'").append(NEWLINE);
			// If multiple carrier then we look for itineraries with the flag
			// set
			if (multiCarrier) {
				query.append("  and multi_carrier_flag = 1").append(NEWLINE);
			}
			// Otherwise, query for single carrier itineraries
			else {
				query.append("  and multi_carrier_flag = 0").append(NEWLINE);
			}
			query
					.append(
							"order by first_operating_carrier, second_operating_carrier,")
					.append(NEWLINE);
			query.append("  origin, connection, destination,").append(NEWLINE);
			query.append("  day_of_month, hour_of_day, minutes_of_hour");

			logger.trace("Itineraries query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());
			InternalRoute currentRoute = null;
			while (rset.next()) {
				int connectionTime = rset.getInt("layover_duration");
				// Only add one stop itineraries if the connection time
				// is less than the maximum connection time
				if (connectionTime > m_maximumConnectionTime) {
					continue;
				}

				String firstOperatingCarrier = rset
						.getString("first_operating_carrier");
				String secondOperatingCarrier = rset
						.getString("second_operating_carrier");
				String origin = rset.getString("origin");
				String connection = rset.getString("connection");
				String destination = rset.getString("destination");
				if (currentRoute == null
						|| !currentRoute.matchesRoute(firstOperatingCarrier,
								secondOperatingCarrier, origin, connection,
								destination)) {
					currentRoute = createInternalRoute(firstOperatingCarrier,
							secondOperatingCarrier, origin, connection,
							destination);
					if (connection == null) {
						nonStopList.add(currentRoute);
					} else {
						oneStopList.add(currentRoute);
					}
				}
				int itineraryID = rset.getInt("id");
				int firstFlightID = rset.getInt("first_flight_id");
				InternalFlight firstFlight = m_idFlightMap.get(firstFlightID);
				if (firstFlight == null) {
					logger
							.error("Unable to retrieve flight information for first flight ID "
									+ firstFlightID);
				}
				boolean cancelledFlag = firstFlight.isCancelledFlag();
				double numSeats = firstFlight.getModelCapacity();
				InternalFlight secondFlight = null;
				String secondFlightIDString = rset
						.getString("second_flight_id");
				if (secondFlightIDString != null) {
					int secondFlightID = Integer.parseInt(secondFlightIDString);
					secondFlight = m_idFlightMap.get(secondFlightID);
					if (secondFlight == null) {
						logger
								.error("Unable to retrieve flight information for second flight ID "
										+ secondFlightID);
					}
					cancelledFlag = cancelledFlag || secondFlight.isCancelledFlag();
					numSeats = Math.min(numSeats, secondFlight.getModelCapacity());
				}
				AllocationItinerary itinerary = new AllocationItinerary(
						itineraryID, currentRoute, firstFlight, secondFlight);
				currentRoute.addMatchingItinerary(itinerary);

				int dayOfWeek = rset.getInt("day_of_week");
				int hourOfDay = rset.getInt("hour_of_day");
				int minutesOfHour = rset.getInt("minutes_of_hour");
				int originOffset = rset.getInt("origin_tz");
				int destinationOffset = rset.getInt("destination_tz");
				
				itinerary.setDepartureDayOfWeek(dayOfWeek);
				itinerary.setDepartureHour(hourOfDay);
				itinerary.setDepartureMinutes(minutesOfHour);
				itinerary.setConnectionTime(connectionTime);
				itinerary.setCancelledFlag(cancelledFlag);
				itinerary.setMinimumSeats(numSeats);
				itinerary.setOriginTimeZoneOffset(originOffset);
				itinerary.setDestinationTimeZoneOffset(destinationOffset);

				numItineraries++;
				if (numItineraries % 25000 == 0) {
					logger.trace("Loaded "
							+ numItineraries
							+ " itineraries for "
							+ (carrierCode == null ? "multiple carriers"
									: carrierCode) + " in month " + month);
				}
			}
		} catch (SQLException e) {
			logger.error("Unable to load itineraries for month " + month, e);
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
		InternalRoute[] nonStopRoutes = new InternalRoute[nonStopList.size()];
		nonStopList.toArray(nonStopRoutes);
		InternalRoute[] oneStopRoutes = new InternalRoute[oneStopList.size()];
		oneStopList.toArray(oneStopRoutes);
		InternalRouteSet routeSet = new InternalRouteSet(nonStopRoutes,
				oneStopRoutes);

		logger.info("[" + carrierCode + ", " + month + "] Number of "
				+ (multiCarrier ? "multiple" : "single") + " carrier routes = "
				+ routeSet.numRoutes());
		logger.info("[" + carrierCode + ", " + month + "] Number of "
				+ (multiCarrier ? "multiple" : "single")
				+ " carrier itineraries = " + numItineraries);

		return new InternalRouteSet(nonStopRoutes, oneStopRoutes);
	}

	public void loadOneStopRouteDemands(int month, String carrierCode,
			boolean multiCarrier) {
		int scaledMonthlyPassengers = 0;
		int scaledNoRoutePassengers = 0;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();

			int quarter = getQuarterForMonth(month);
			StringBuffer query = new StringBuffer();
			query
					.append(
							"select first_operating_carrier, second_operating_carrier,")
					.append(NEWLINE);
			query.append("  origin, connection, destination, passengers")
					.append(NEWLINE);
			query.append("from ").append(m_scaledRouteDemandTable)
					.append(NEWLINE);
			query.append("where year = ").append(m_year).append(NEWLINE);
			query.append("  and quarter = ").append(quarter).append(NEWLINE);
			query.append("  and month = ").append(month).append(NEWLINE);
			query.append("  and num_flights = 2").append(NEWLINE);
			// If multiple carrier, then we look for routes with this carrier
			// first
			// or routes where this carrier is second and the first carrier is
			// not ASQP
			if (multiCarrier) {
				query.append("  and ((first_operating_carrier = '").append(
						carrierCode).append("'").append(NEWLINE);
				query
						.append(
								"      and second_operating_carrier != first_operating_carrier")
						.append(NEWLINE);
				query.append("      and exists(").append(NEWLINE);
				query.append("        select * from asqp_carriers")
						.append(NEWLINE);
				query.append("        where code = second_operating_carrier))")
						.append(NEWLINE);
				query.append("    or (second_operating_carrier = '").append(
						carrierCode).append("'").append(NEWLINE);
				query.append("      and not exists(").append(NEWLINE);
				query.append("        select * from asqp_carriers")
						.append(NEWLINE);
				query.append("        where code = first_operating_carrier)))")
						.append(NEWLINE);
			}
			// Otherwise, query for single carrier route demands
			else {
				query.append("  and first_operating_carrier = '").append(
						carrierCode).append("'").append(NEWLINE);
				query.append("  and second_operating_carrier = '").append(
						carrierCode).append("'").append(NEWLINE);
			}

			logger.trace("DB1B route demand query:");
			logger.trace(query.toString());
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				String firstCarrierCode = rset
						.getString("first_operating_carrier");
				String secondCarrierCode = rset
						.getString("second_operating_carrier");
				String origin = rset.getString("origin");
				String connection = rset.getString("connection");
				String destination = rset.getString("destination");
				int passengers = rset.getInt("passengers");

				InternalRoute route = getInternalRoute(firstCarrierCode,
						secondCarrierCode, origin, connection, destination);

				// Scale the passengers to keep the percentage of connections
				// reasonable based on the proprietary data
				int monthlyPassengers = (int) Math.round(passengers
						* m_quarterlyOneStopScalingFactors[quarter - 1]);

				scaledMonthlyPassengers += monthlyPassengers;

				// If one of the flights is not an ASQP flight or the route does
				// not have any matching itineraries, we can just ignore it
				// since
				// any stray passengers will be picked up based on the
				// excess segment demand
				if (route == null) {
					scaledNoRoutePassengers += monthlyPassengers;
					continue;
				}

				route.setPassengerDemand(monthlyPassengers);
			}
		} catch (SQLException e) {
			exit("Unable to load DB1B "
					+ (multiCarrier ? "multiple carrier" : "single carrier")
					+ " one stop route demands for carrier " + carrierCode, e,
					-1);
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

		m_noRoutePassengers += scaledNoRoutePassengers;
		double matchFraction = 1.0;
		if (scaledMonthlyPassengers > 0) {
			matchFraction = 1.0 - ((double) scaledNoRoutePassengers / (double) scaledMonthlyPassengers);
		}
		logger
				.info("[" + carrierCode + ", " + month + "] Total monthly "
						+ (multiCarrier ? "multiple" : "single")
						+ " carrier one stop route demand = "
						+ scaledMonthlyPassengers);
		String matchPercentString = String.format("%1$.2f",
				100.0 * matchFraction);
		logger.info("[" + carrierCode + ", " + month + "] Matched "
				+ matchPercentString + "% of monthly "
				+ (multiCarrier ? "multiple" : "single")
				+ " carrier one stop route demand");
	}

	public int allocateRouteSetPassengers(InternalRouteSet routeSet, String period,
			String carrierCode, boolean multiCarrier) {

		// First, we allocate the connecting passengers
		InternalRoute[] oneStopRoutes = routeSet.getOneStopRoutes();
		int oneStopPassengers = allocatePassengers(oneStopRoutes);

		// Next, we allocate all of the non-stop passengers
		// based on any remaining discrepancies with the segment demand
		int nonStopPassengers = 0;
		if (!multiCarrier) {
			InternalRoute[] nonStopRoutes = routeSet.getNonStopRoutes();
			for (int i = 0; i < nonStopRoutes.length; ++i) {
				InternalSegment segment = nonStopRoutes[i].getFirstSegment();
				// We only allocate non-stop passengers if there is a
				// matching segment in the database
				if (segment != null) {
					int passengerDemand = (int) Math.floor(segment
							.getPassengerCapacity()
							- segment.getNumberPassengersAllocated());
					nonStopRoutes[i].setPassengerDemand(passengerDemand);
				} else {
					nonStopRoutes[i].setPassengerDemand(0);
				}
			}
			nonStopPassengers = allocatePassengers(nonStopRoutes);
		}
		if (!multiCarrier) {
			logger
					.info("[" + carrierCode + ", " + period + "] Allocated "
							+ nonStopPassengers
							+ " non-stop single carrier passengers");
		}
		logger.info("[" + carrierCode + ", " + period + "] Allocated "
				+ oneStopPassengers + " one stop "
				+ (multiCarrier ? "multiple" : "single")
				+ " carrier passengers");
		return nonStopPassengers + oneStopPassengers;
	}

	public int allocatePassengers(InternalRoute[] routes) {
		int numPassengersAllocated = 0;
		int passengerCount = 0;
		for (int i = 0; i < routes.length; ++i) {
			passengerCount += routes[i].getPassengerDemand();
		}

		InternalRoute[] routeForPassengers = new InternalRoute[passengerCount];
		int startIndex = 0;
		for (int i = 0; i < routes.length; ++i) {
			int passengers = routes[i].getPassengerDemand();
			for (int j = startIndex; j < startIndex + passengers; ++j) {
				routeForPassengers[j] = routes[i];
			}
			startIndex += passengers;
		}
		randomlyPermuteObservations(routeForPassengers);

		for (int i = 0; i < routeForPassengers.length; ++i) {
			InternalRoute route = routeForPassengers[i];
			if (route.containsFullSegment()) {
				m_noSeatPassengers++;
				continue;
			}
			if (!route.hasMatchingItineraries()) {
				m_noRoutePassengers++;
				continue;
			}

			// Eliminate alternatives which have a flight full.  Calculate
			// and sum the weights associated with all other itineraries.
			AllocationItinerary[] itineraries = route.getMatchingItineraries();
			m_utilityCalculator.resetParameters(m_randomNumberGenerator);
			double totalWeight = 0.0;
			for (int j = 0; j < itineraries.length; ++j) {
				if (itineraries[j].containsFullFlight()) {
					itineraries[j].setEstimatedWeight(0.0);
				}
				else {
					double utility = m_utilityCalculator.calculateUtility(itineraries[j]);
					itineraries[j].setEstimatedWeight(Math.exp(utility));
					totalWeight += itineraries[j].getEstimatedWeight();
				}
			}
			route.setTotalEstimatedWeight(totalWeight);

			if (route.getTotalEstimatedWeight() < EPSILON) {
				m_noSeatPassengers++;
				continue;
			}

			// Because we have already eliminated unavailable itineraries
			// we should only need to try the assignment once
			double randomDouble = m_randomNumberGenerator.nextDouble();
			double probabilitySum = 0.0;
			int j = 0;
			for (; j < itineraries.length; ++j) {
				probabilitySum += itineraries[j].getEstimatedProbability();
				if (randomDouble <= probabilitySum) {
					itineraries[j].allocatePassengers(1.0);
					numPassengersAllocated++;
					break;
				}
			}
			if (j == itineraries.length) {
				logger.error("Unable to assign passenger to route "
						+ getRouteKey(route));
			}
		}
		return numPassengersAllocated;
	}

	protected String getPeriodFilePrefix(String period) {
		return "Month_" + period;
	}
	
	public void writePassengerAllocationFiles(InternalRouteSet routeSet,
			String period, String carrierCode, boolean multiCarrier) {

		StringBuffer identifierString = new StringBuffer();
		identifierString.append(getPeriodFilePrefix(period));
		if (multiCarrier) {
			identifierString.append("_Multiple_");
		} else {
			identifierString.append("_Single_");
		}
		identifierString.append(carrierCode);

		StringBuffer itineraryLoadFile = new StringBuffer(
				m_allocationOutputDirectory);
		itineraryLoadFile.append(File.separator);
		itineraryLoadFile.append("ItineraryLoad_");
		itineraryLoadFile.append(identifierString).append(".txt");
		
//		XuJ: 04/01/17. Because we don't need external files to create the database
//		StringBuffer itineraryExternalFile = new StringBuffer(
//				m_allocationOutputDirectory);
//		itineraryExternalFile.append(File.separator);
//		itineraryExternalFile.append("ItineraryExternal_");
//		itineraryExternalFile.append(identifierString).append(".txt");

		FileWriter lfw = null;
//		FileWriter efw = null;
		try {
			lfw = new FileWriter(itineraryLoadFile.toString());
//			efw = new FileWriter(itineraryExternalFile.toString());
		}
		catch (IOException e) {
			logger.fatal("Unable to load files to write "
					+ (multiCarrier ? "multiple carrier" : "single carrier")
					+ " allocation for carrier " + carrierCode + " to file", e);
			return;
		}
		try {
			lfw
					.write("First_Operating_Carrier\tSecond_Operating_Carrier\tFirst_Flight_ID\tSecond_Flight_ID\tPassengers\n");

//			efw
//					.write("First_Flight_ID\tFirst_Operating_Carrier\tFirst_Origin\tFirst_Destination\t");
//			efw
//					.write("First_Month\tFirst_Day\tFirst_Departure\tFirst_Arrival\tFirst_Capacity\t");
//			efw
//					.write("Second_Flight_ID\tSecond_Operating_Carrier\tSecond_Origin\tSecond_Destination\t");
//			efw
//					.write("Second_Month\tSecond_Day\tSecond_Departure\tSecond_Arrival\tSecond_Capacity\t");
//			efw.write("Number_Passengers\n");

			InternalRoute[] nonStopRoutes = routeSet.getNonStopRoutes();
			InternalRoute[] oneStopRoutes = routeSet.getOneStopRoutes();
			int i = 0;
			int j = 0;
			while (i < nonStopRoutes.length || j < oneStopRoutes.length) {
				InternalRoute route = null;
				if (i < nonStopRoutes.length) {
					route = nonStopRoutes[i];
					++i;
				} else {
					route = oneStopRoutes[j];
					++j;
				}
				AllocationItinerary[] itineraries = route
						.getMatchingItineraries();
				for (int k = 0; k < itineraries.length; ++k) {
					String firstCarrier = route.getFirstCarrierCode();
					String secondCarrier = route.getSecondCarrierCode();
					if (secondCarrier == null) {
						secondCarrier = "";
					}
					InternalFlight firstFlight = itineraries[k]
							.getFirstFlight();
					InternalFlight secondFlight = itineraries[k]
							.getSecondFlight();
					String firstFlightID = Integer.toString(firstFlight
							.getFlightID());
					String secondFlightID = "";
					if (secondFlight != null) {
						secondFlightID = Integer.toString(secondFlight
								.getFlightID());
					}
					double passengers = itineraries[k]
							.getNumberPassengersAllocated();
					String passengersString = Double.toString(passengers);

					// For the short database loading format, we only need to
					// write out itineraries that have passengers allocated
					if (passengers > 0.0) {
						lfw.write(firstCarrier);
						lfw.write("\t");
						lfw.write(secondCarrier);
						lfw.write("\t");
						lfw.write(firstFlightID);
						lfw.write("\t");
						lfw.write(secondFlightID);
						lfw.write("\t");
						lfw.write(passengersString);
						lfw.write("\n");
					}

					// Write the long format for external usage
//					efw.write(firstFlightID);
//					efw.write("\t");
//					efw.write(firstCarrier);
//					efw.write("\t");
//					efw.write(firstFlight.getOrigin());
//					efw.write("\t");
//					efw.write(firstFlight.getDestination());
//					efw.write("\t");
//					efw.write(String.format("%1$02d", firstFlight.getMonth()));
//					efw.write("\t");
//					efw.write(String.format("%1$02d", firstFlight
//							.getDayOfMonth()));
//					efw.write("\t");
//					efw.write(firstFlight.getDepartureTime());
//					efw.write("\t");
//					efw.write(firstFlight.getArrivalTime());
//					efw.write("\t");
//					efw
//							.write(Double.toString(firstFlight
//									.getAllocationCapacity()));
//					efw.write("\t");
//
//					if (secondFlight != null) {
//						efw.write(secondFlightID);
//						efw.write("\t");
//						efw.write(secondCarrier);
//						efw.write("\t");
//						efw.write(secondFlight.getOrigin());
//						efw.write("\t");
//						efw.write(secondFlight.getDestination());
//						efw.write("\t");
//						efw.write(String.format("%1$02d", secondFlight
//								.getMonth()));
//						efw.write("\t");
//						efw.write(String.format("%1$02d", secondFlight
//								.getDayOfMonth()));
//						efw.write("\t");
//						efw.write(secondFlight.getDepartureTime());
//						efw.write("\t");
//						efw.write(secondFlight.getArrivalTime());
//						efw.write("\t");
//						efw.write(Double.toString(secondFlight
//								.getAllocationCapacity()));
//						efw.write("\t");
//					} else {
//						// Flight ID
//						efw.write("\t");
//						// Carrier
//						efw.write("\t");
//						// Origin
//						efw.write("\t");
//						// Destination
//						efw.write("\t");
//						// Month
//						efw.write("\t");
//						// Day of Month
//						efw.write("\t");
//						// Departure Time
//						efw.write("\t");
//						// Arrival Time
//						efw.write("\t");
//						// Seating Capacity
//						efw.write("\t");
//					}
//					efw.write(passengersString);
//					efw.write("\n");
				}
			}
			lfw.flush();
//			efw.flush();
		} catch (IOException e) {
			logger.fatal("Unable to write "
					+ (multiCarrier ? "multiple carrier" : "single carrier")
					+ " allocation for carrier " + carrierCode + " to file", e);
		}
		finally {
			try {
				lfw.close();
//				efw.close();
			}
			catch (IOException e) {
				// Just eat the exception at this point...
			}
		}
	}

	public void randomlyPermuteObservations(Object[] obj) {
		int numObservations = obj.length;
		Integer[] initialIndices = new Integer[numObservations];
		for (int i = 0; i < initialIndices.length; ++i) {
			initialIndices[i] = new Integer(i);
		}
		Arrays
				.sort(initialIndices, new RandomValuesComparator(
						numObservations, this.m_randomNumberGenerator));
		Object[] tempObj = obj.clone();
		for (int i = 0; i < obj.length; ++i) {
			obj[i] = tempObj[initialIndices[i].intValue()];
		}
	}

	public void disconnectFromDatabase() {
		try {
			m_dbConnection.close();
			//m_datasource.close();
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

	protected InternalSegment createInternalSegment(String carrier,
			String origin, String destination) {
		InternalSegment newSegment = new InternalSegment(carrier, origin,
				destination);
		String segmentKey = getSegmentKey(carrier, origin, destination);
		m_keySegmentMap.put(segmentKey, newSegment);
		return newSegment;
	}

	protected InternalSegment getInternalSegment(String carrier, String origin,
			String destination) {
		String segmentKey = getSegmentKey(carrier, origin, destination);
		return m_keySegmentMap.get(segmentKey);
	}
	
	protected InternalAircraftFleet createInternalAircraftFleet(String carrier,
			String aircraftType) {
		InternalAircraftFleet aircraftFleet = new InternalAircraftFleet(carrier, aircraftType);
		String fleetKey = getAircraftFleetKey(carrier, aircraftType);
		System.out.println("###put###");
		System.out.println("key:"+fleetKey+" fleet"+ aircraftFleet);
		m_aircraftFleetMap.put(fleetKey, aircraftFleet);
		return aircraftFleet;
	}

	protected InternalAircraftFleet getInternalAircraftFleet(String carrier, 
			String aircraftType) {
		String fleetKey = getAircraftFleetKey(carrier, aircraftType);
		return m_aircraftFleetMap.get(fleetKey);
	}
	
	protected static String getAircraftFleetKey(String carrier, String aircraftType) {
		return carrier + aircraftType;
	}
	
	protected static String getRouteKey(InternalRoute route) {
		return getRouteKey(route.getFirstCarrierCode(), route
				.getSecondCarrierCode(), route.getOrigin(), route
				.getConnection(), route.getDestination());
	}

	protected static String getRouteKey(String firstCarrier,
			String secondCarrier, String origin, String connection,
			String destination) {
		StringBuffer key = new StringBuffer(firstCarrier);
		key.append(".").append(origin);
		if (connection == null) {
			key.append(".").append(destination);
		} else {
			key.append(".").append(connection);
			key.append(";").append(secondCarrier);
			key.append(".").append(connection);
			key.append(".").append(destination);
		}
		return key.toString();
	}

	protected InternalRoute createInternalRoute(String firstCarrierCode,
			String secondCarrierCode, String origin, String connection,
			String destination) {
		InternalRoute route = null;
		InternalSegment firstSegment = null;
		InternalSegment secondSegment = null;
		if (connection == null) {
			firstSegment = getInternalSegment(firstCarrierCode, origin,
					destination);
		} else {
			firstSegment = getInternalSegment(firstCarrierCode, origin,
					connection);
			secondSegment = getInternalSegment(secondCarrierCode, connection,
					destination);
		}
		route = new InternalRoute(firstCarrierCode, secondCarrierCode, origin,
				connection, destination, firstSegment, secondSegment);

		String key = getRouteKey(firstCarrierCode, secondCarrierCode, origin,
				connection, destination);
		m_keyRouteMap.put(key, route);
		return route;
	}

	protected InternalRoute getInternalRoute(String firstCarrier,
			String secondCarrier, String origin, String connection,
			String destination) {
		String key = getRouteKey(firstCarrier, secondCarrier, origin,
				connection, destination);
		return m_keyRouteMap.get(key);
	}

	protected static int getQuarterForMonth(int month) {
		return (int) Math.floor((month + 2) / 3.0);
	}
}

class InternalCarrier {
	String m_code;
	
	double m_seatsMean;
	double m_seatsSquaredMean;
	int m_departures;

	public InternalCarrier(String code) {
		m_code = code;
	}

	public String getCode() {
		return m_code;
	}
	
	public void resetDefaultCapacity() {
		m_seatsMean = 0.0;
		m_seatsSquaredMean = 0.0;
		m_departures = 0;
	}
	
	public void addDefaultCapacity(double mean, double squaredMean, int departures) {
		m_seatsMean = (m_seatsMean * m_departures + mean * departures) /
			(m_departures + departures);
		m_seatsSquaredMean = (m_seatsSquaredMean * m_departures + squaredMean * departures) /
			(m_departures + departures);
		m_departures = m_departures + departures;
	}
	
	public double getSeatsMean() {
		return m_seatsMean;
	}
	
	public double getSeatsDeviation() {
		if (m_departures <= 1) {
			return 0.0;
		}
		double seatsVariance = (m_seatsSquaredMean - Math.pow(m_seatsMean, 2.0)) * 
			m_departures / (m_departures - 1.0);
		return Math.sqrt(seatsVariance);
	}
}

class InternalSegment {
	String m_carrierCode;
	String m_origin;
	String m_destination;
	double m_seatsMean;
	double m_seatsDeviation;

	int m_passengerCapacity;
	double m_numberPassengersAllocated;
	int m_numberStrayPassengers;

	public InternalSegment(String carrierCode, String origin, String destination) {
		m_carrierCode = carrierCode;
		m_origin = origin;
		m_destination = destination;
	}

	public String getCarrierCode() {
		return m_carrierCode;
	}

	public String getOrigin() {
		return m_origin;
	}

	public String getDestination() {
		return m_destination;
	}

	public void setPassengerCapacity(int passengerCapacity) {
		m_passengerCapacity = passengerCapacity;
	}

	public int getPassengerCapacity() {
		return m_passengerCapacity;
	}
	
	public void setDefaultCapacity(double seatsMean, double seatsDeviation) {
		m_seatsMean = seatsMean;
		m_seatsDeviation = seatsDeviation;
	}

	public double getSeatsMean() {
		return m_seatsMean;
	}
	
	public double getSeatsDeviation() {
		return m_seatsDeviation;
	}

	public void allocatePassengers(double numberPassengers) {
		m_numberPassengersAllocated += numberPassengers;
	}

	public double getNumberPassengersAllocated() {
		return m_numberPassengersAllocated;
	}

	public void addStrayPassengers(int passengers) {
		m_numberStrayPassengers += passengers;
	}

	public int getStrayPassengers() {
		return m_numberStrayPassengers;
	}

	public boolean matchesSegment(String carrier, String origin,
			String destination) {
		return m_carrierCode.equals(carrier) && m_origin.equals(origin)
				&& m_destination.equals(destination);
	}

	public boolean isFull() {
		return Math.ceil(m_numberPassengersAllocated) >= m_passengerCapacity;
	}
}

class InternalAircraftFleet {
	String m_carrier;
	String m_aircraftType;
	
	int m_minimumSeatingCapacity;
	int m_maximumSeatingCapacity;
	
	public InternalAircraftFleet(String carrier, String aircraftType) {
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

class InternalFlight {
	int m_flightID;
	int m_month;
	int m_dayOfMonth;
	String m_carrierCode;
	String m_origin;
	String m_destination;
	String m_departureTime;
	String m_arrivalTime;
	boolean m_cancelledFlag;

	double m_modelCapacity;
	double m_allocationCapacity;
	double m_numberPassengersAllocated;

	public InternalFlight(int flightID, int month, int dayOfMonth,
			String carrierCode, String origin, String destination,
			String departureTime, String arrivalTime, boolean cancelledFlag) {
		m_flightID = flightID;
		m_month = month;
		m_dayOfMonth = dayOfMonth;
		m_carrierCode = carrierCode;
		m_origin = origin;
		m_destination = destination;
		m_departureTime = departureTime;
		m_arrivalTime = arrivalTime;
		m_cancelledFlag = cancelledFlag;
	}

	public int getFlightID() {
		return m_flightID;
	}

	public int getMonth() {
		return m_month;
	}

	public int getDayOfMonth() {
		return m_dayOfMonth;
	}

	public String getCarrierCode() {
		return m_carrierCode;
	}

	public String getOrigin() {
		return m_origin;
	}

	public String getDestination() {
		return m_destination;
	}

	public String getDepartureTime() {
		return m_departureTime;
	}

	public String getArrivalTime() {
		return m_arrivalTime;
	}

	public boolean isCancelledFlag() {
		return m_cancelledFlag;
	}

	public void setAllocationCapacity(double seatingCapacity) {
		m_allocationCapacity = seatingCapacity;
	}

	public double getAllocationCapacity() {
		return m_allocationCapacity;
	}
	
	public void setModelCapacity(double seatingCapacity) {
		m_modelCapacity = seatingCapacity;
	}
	
	public double getModelCapacity() {
		return m_modelCapacity;
	}

	public void allocatePassengers(double numberPassengers) {
		m_numberPassengersAllocated += numberPassengers;
	}

	public double getNumberPassengersAllocated() {
		return m_numberPassengersAllocated;
	}

	public boolean isFull() {
		return Math.ceil(m_numberPassengersAllocated) >= m_allocationCapacity;
	}
}

class InternalRoute {
	InternalSegment m_firstSegment;
	InternalSegment m_secondSegment;

	String m_firstCarrierCode;
	String m_secondCarrierCode;
	String m_origin;
	String m_connection;
	String m_destination;

	double m_totalEstimatedWeight;

	List<AllocationItinerary> m_matchingItineraries;
	AllocationItinerary[] m_matchingItinerariesCache;

	int m_passengerDemand;

	public InternalRoute(String firstCarrierCode, String secondCarrierCode,
			String origin, String connection, String destination,
			InternalSegment firstSegment, InternalSegment secondSegment) {
		m_firstCarrierCode = firstCarrierCode;
		m_secondCarrierCode = secondCarrierCode;
		m_origin = origin;
		m_connection = connection;
		m_destination = destination;

		m_firstSegment = firstSegment;
		m_secondSegment = secondSegment;

		m_matchingItineraries = new ArrayList<AllocationItinerary>();
	}

	public void addMatchingItinerary(AllocationItinerary itinerary) {
		m_matchingItineraries.add(itinerary);
	}

	public boolean hasMatchingItineraries() {
		return !m_matchingItineraries.isEmpty();
	}

	public AllocationItinerary[] getMatchingItineraries() {
		if (m_matchingItinerariesCache == null) {
			m_matchingItinerariesCache = new AllocationItinerary[m_matchingItineraries
					.size()];
			m_matchingItineraries.toArray(m_matchingItinerariesCache);
		}
		return m_matchingItinerariesCache;
	}

	public InternalSegment getFirstSegment() {
		return m_firstSegment;
	}

	public InternalSegment getSecondSegment() {
		return m_secondSegment;
	}

	public String getFirstCarrierCode() {
		return m_firstCarrierCode;
	}

	public String getSecondCarrierCode() {
		return m_secondCarrierCode;
	}

	public String getOrigin() {
		return m_origin;
	}

	public String getConnection() {
		return m_connection;
	}

	public String getDestination() {
		return m_destination;
	}

	public boolean matchesRoute(String firstOperatingCarrier,
			String secondOperatingCarrier, String origin, String connection,
			String destination) {
		if (!m_destination.equals(destination)) {
			return false;
		}
		if (m_connection == null) {
			if (connection != null) {
				return false;
			}
		} else {
			if (!m_connection.equals(connection)) {
				return false;
			}
		}
		if (!m_origin.equals(origin)) {
			return false;
		}
		if (m_secondCarrierCode == null) {
			if (secondOperatingCarrier != null) {
				return false;
			}
		} else {
			if (!m_secondCarrierCode.equals(secondOperatingCarrier)) {
				return false;
			}
		}
		if (!m_firstCarrierCode.equals(firstOperatingCarrier)) {
			return false;
		}
		return true;
	}

	public boolean isMultiCarrierRoute() {
		return m_secondSegment != null
				&& !getFirstCarrierCode().equals(getSecondCarrierCode());
	}

	public void setPassengerDemand(int passengerDemand) {
		m_passengerDemand = passengerDemand;
	}

	public int getPassengerDemand() {
		return m_passengerDemand;
	}

	public void scalePassengerDemand(double scalingFactor) {
		m_passengerDemand = (int) Math.round(m_passengerDemand * scalingFactor);
	}

	public boolean containsFullSegment() {
		return (m_firstSegment != null && m_firstSegment.isFull())
				|| (m_secondSegment != null && m_secondSegment.isFull());
	}

	public void allocatePassengers(double numberPassengers) {
		if (m_firstSegment != null) {
			m_firstSegment.allocatePassengers(numberPassengers);
		}
		if (m_secondSegment != null) {
			m_secondSegment.allocatePassengers(numberPassengers);
		}
	}

	public void setTotalEstimatedWeight(double totalWeight) {
		m_totalEstimatedWeight = totalWeight;
	}

	public double getTotalEstimatedWeight() {
		return m_totalEstimatedWeight;
	}
}

class InternalRouteSet {
	InternalRoute[] m_nonStopRoutes;
	InternalRoute[] m_oneStopRoutes;
	InternalRoute[] m_allRoutes;

	public InternalRouteSet(InternalRoute[] nonStopRoutes,
			InternalRoute[] oneStopRoutes) {
		m_nonStopRoutes = nonStopRoutes;
		m_oneStopRoutes = oneStopRoutes;
	}

	public InternalRoute[] getNonStopRoutes() {
		return m_nonStopRoutes;
	}

	public InternalRoute[] getOneStopRoutes() {
		return m_oneStopRoutes;
	}

	public int numRoutes() {
		return m_nonStopRoutes.length + m_oneStopRoutes.length;
	}
}

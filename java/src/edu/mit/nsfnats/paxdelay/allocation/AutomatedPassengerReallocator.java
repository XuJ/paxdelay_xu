package edu.mit.nsfnats.paxdelay.allocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import edu.mit.nsfnats.paxdelay.util.TimeFormat;

public class AutomatedPassengerReallocator extends AutomatedPassengerAllocator {
	public static final String PROPERTY_ALLOCATION_FIRST_DAY = "ALLOCATION_FIRST_DAY";
	// Start time specified in format HH24:MI
	public static final String PROPERTY_ALLOCATION_FIRST_TIME_UTC = "ALLOCATION_FIRST_TIME_UTC";
	
	public static final String PROPERTY_PASSENGER_ALLOCATIONS_TABLE = "PASSENGER_ALLOCATIONS_TABLE";
	public static final String DEFAULT_PASSENGER_ALLOCATIONS_TABLE = "itinerary_allocations";
	
	public static final String PROPERTY_ALLOCATION_FLIGHTS_TABLE = "ALLOCATION_FLIGHTS_TABLE";
	public static final String DEFAULT_ALLOCATION_FLIGHTS_TABLE = "flights";
	
	// Number of minutes on either side of the demand window to add a buffer
	public static final String PROPERTY_DEMAND_WINDOW_BUFFER_SIZE = "DEMAND_WINDOW_BUFFER_SIZE";
	public static final int DEFAULT_DEMAND_WINDOW_BUFFER_SIZE = 0;
	
	public static final String PROPERTY_SCALING_FACTOR = "SCALING_FACTOR";
	public static final double DEFAULT_SCALING_FACTOR = 1.0;
	
	String m_passengerAllocationsTable;
	String m_allocationFlightsTable;
	DateFormat m_oracleFormat;
	
	Date m_startDate;
	Date m_endDate;
	
	double m_scalingFactor;
	
	public AutomatedPassengerReallocator() {
		super();
		m_oracleFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss a zzz");
		m_oracleFormat.setTimeZone(TimeFormat.getUniversalTimeZone());
	}
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err
					.println("Usage: java edu.mit.nsfnats.paxdelay.choice.AutomatedPassengerReallocator "
							+ "<logger_properties_file> <allocation_properties_file>");
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

		Properties allocationProperties = null;
		try {
			allocationProperties = PropertiesReader.loadProperties(args[1]);
		} catch (FileNotFoundException e) {
			exit("Allocation properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading allocation properties file.",
					e, -1);
		}
		main(allocationProperties);
	}
	
	public static void main(Properties properties) {
		AutomatedPassengerReallocator pra = new AutomatedPassengerReallocator();
		try {
			pra.initialize(properties);
		} catch (InvalidFormatException e) {
			exit("Invalid format specified in allocation properties file", e,
					-1);
		}
		
		logger.info("Initializing annual data for allocation");
		pra.connectToDatabase();
		logger.trace("Connected to database");
		pra.loadASQPCarriers();
		logger.trace("Loaded list of ASQP carriers");
		pra.loadAircraftFleetTypes();
		logger.trace("Loaded aircraft fleet types");
		pra.allocatePassengersThroughSampling();
		logger.trace("Finished allocating all passengers");

		pra.disconnectFromDatabase();
	}
	
	public void initialize(Properties properties) throws InvalidFormatException {
		super.initialize(properties);
		
		m_passengerAllocationsTable = properties.getProperty(PROPERTY_PASSENGER_ALLOCATIONS_TABLE,
				DEFAULT_PASSENGER_ALLOCATIONS_TABLE);
		m_allocationFlightsTable = properties.getProperty(PROPERTY_ALLOCATION_FLIGHTS_TABLE,
				DEFAULT_ALLOCATION_FLIGHTS_TABLE);
		
		int demandWindowBufferSize = PropertiesReader.readInt(properties, 
				PROPERTY_DEMAND_WINDOW_BUFFER_SIZE, DEFAULT_DEMAND_WINDOW_BUFFER_SIZE);
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		int day = PropertiesReader.readInt(properties, PROPERTY_ALLOCATION_FIRST_DAY);
		String timeString = properties.getProperty(PROPERTY_ALLOCATION_FIRST_TIME_UTC);
		TimeFormat timeFormat = new TimeFormat();
		try {
			Date startDate = timeFormat.parse(timeString);
			calendar.setTime(startDate);
		}
		catch (ParseException e) {
			throw new InvalidFormatException("Incorrect format specified for time string " + timeString +
					", correct format is HH24:MI", e);
		}
		calendar.set(Calendar.YEAR, m_year);
		// Adjust for the fact that Java months start at 0
		calendar.set(Calendar.MONTH, m_firstMonth - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.add(Calendar.MINUTE, -1 * demandWindowBufferSize);
		m_startDate = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 12);
		calendar.add(Calendar.MINUTE, 2 * demandWindowBufferSize);
		m_endDate = calendar.getTime();
		
		m_scalingFactor = PropertiesReader.readDouble(properties, PROPERTY_SCALING_FACTOR,
				DEFAULT_SCALING_FACTOR);
	}
	
	public void allocatePassengersThroughSampling() {
		int month = m_firstMonth;
		
		DateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
		String startDateString = dayFormat.format(m_startDate);
		logger.info("Allocating passengers for date " + startDateString);

		initializeMonthlyStorage();
		getDefaultSeatingCapacities(month);
		getFlightSeatingCapacities(month);
		loadSegmentDemands(startDateString);

		// First, allocate passengers to the multiple carrier itineraries
		for (int i = 0; i < m_carriers.length; ++i) {
			String carrierCode = m_carriers[i];
			InternalRouteSet routeSet = queryItineraries(startDateString,
					carrierCode, true);
			loadOneStopRouteDemands(startDateString, carrierCode, true);
			int numPassengers = allocateRouteSetPassengers(routeSet,
					startDateString, carrierCode, true);
			writePassengerAllocationFiles(routeSet, startDateString, 
					carrierCode, true);
			m_allocatedPassengers += numPassengers;
		}

		// Next, allocate passengers to the single carrier itineraries
		for (int i = 0; i < m_carriers.length; ++i) {
			String carrierCode = m_carriers[i];
			InternalRouteSet routeSet = queryItineraries(startDateString,
					carrierCode, false);
			loadOneStopRouteDemands(startDateString, carrierCode, false);
			int numPassengers = allocateRouteSetPassengers(routeSet,
					startDateString, carrierCode, false);
			writePassengerAllocationFiles(routeSet, startDateString, 
					carrierCode, false);
			m_allocatedPassengers += numPassengers;
		}
		logger.info("Completed allocating passengers for date " + startDateString);
		logger.info("Total passengers allocated = " + m_allocatedPassengers);
		logger.info("Passengers with no matching route = "
				+ m_noRoutePassengers);
		logger
		.info("Passengers with no seat available = "
				+ m_noSeatPassengers);
	}

	
	public void loadSegmentDemands(String startDateString) {
		int numSegments = 0;
		String startTimeString = m_oracleFormat.format(m_startDate);
		String endTimeString = m_oracleFormat.format(m_endDate);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select carrier, origin, destination,").append(NEWLINE);
			query.append("  sum(passengers) as passengers").append(NEWLINE);
			query.append("from").append(NEWLINE);
			query.append("(").append(NEWLINE);
			query.append("  select ft.carrier as carrier,").append(NEWLINE);
			query.append("    ft.origin as origin,").append(NEWLINE);
			query.append("    ft.destination as destination,").append(NEWLINE);
			query.append("    sum(pa.passengers) as passengers").append(NEWLINE);
			query.append("  from ").append(m_passengerAllocationsTable).append(" pa ")
				.append(NEWLINE);
			query.append("  join ").append(m_allocationFlightsTable).append(" ft ").append(NEWLINE);
			query.append("    on ft.id = pa.first_flight_id").append(NEWLINE);
			query.append("  where pa.num_flights = 1").append(NEWLINE);
			query.append("    and ft.actual_arrival_time >= '").append(startTimeString)
				.append("'").append(NEWLINE);
			query.append("    and ft.planned_departure_time < '").append(endTimeString)
				.append("'").append(NEWLINE);
			query.append("  group by ft.carrier,").append(NEWLINE);
			query.append("    ft.origin, ft.destination").append(NEWLINE);
			query.append("  union all").append(NEWLINE);
			query.append("  select ft.carrier as carrier,").append(NEWLINE);
			query.append("    ft.origin as origin,").append(NEWLINE);
			query.append("    ft.destination as destination,").append(NEWLINE);
			query.append("    sum(pa.passengers)  as passengers").append(NEWLINE);
			query.append("  from ").append(m_passengerAllocationsTable).append(" pa ").append(NEWLINE);
			query.append("  join ").append(m_allocationFlightsTable).append(" ft ").append(NEWLINE);
			query.append("    on ft.id = pa.first_flight_id").append(NEWLINE);
			query.append("  where pa.num_flights = 2").append(NEWLINE);
			query.append("    and ft.actual_arrival_time >= '").append(startTimeString)
				.append("'").append(NEWLINE);
			query.append("    and ft.planned_departure_time < '").append(endTimeString)
				.append("'").append(NEWLINE);
			query.append("  group by ft.carrier,").append(NEWLINE);
			query.append("    ft.origin, ft.destination").append(NEWLINE);
			query.append("  union all").append(NEWLINE);
			query.append("  select ft.carrier as carrier,").append(NEWLINE);
			query.append("    ft.origin as origin,").append(NEWLINE);
			query.append("    ft.destination as destination,").append(NEWLINE);
			query.append("    sum(pa.passengers) as passengers").append(NEWLINE);
			query.append("  from ").append(m_passengerAllocationsTable).append(" pa ").append(NEWLINE);
			query.append("  join ").append(m_allocationFlightsTable).append(" ft ").append(NEWLINE);
			query.append("    on ft.id = pa.second_flight_id").append(NEWLINE);
			query.append("  where pa.num_flights = 2").append(NEWLINE);
			query.append("    and ft.actual_arrival_time >= '").append(startTimeString)
				.append("'").append(NEWLINE);
			query.append("    and ft.planned_departure_time < '").append(endTimeString)
				.append("'").append(NEWLINE);
			query.append("  group by ft.carrier,").append(NEWLINE);
			query.append("    ft.origin, ft.destination").append(NEWLINE);
			query.append(")").append(NEWLINE);
			query.append("group by carrier, origin, destination").append(NEWLINE);	

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
				// Scale the passengers to allow for load factor adjustments
				passengers = (int) Math.round(passengers * m_scalingFactor);
				segment.setPassengerCapacity(passengers);
				numSegments++;
			}
		} catch (SQLException e) {
			exit("Unable to load segment demands for date " + startDateString, e, -1);
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
		logger.info("Number of matching segments for date " + startDateString + " = "
				+ numSegments);
	}
	
	public InternalRouteSet queryItineraries(String startDateString,
			String carrierCode, boolean multiCarrier) {	
		List<InternalRoute> nonStopList = new ArrayList<InternalRoute>();
		List<InternalRoute> oneStopList = new ArrayList<InternalRoute>();
		int numItineraries = 0;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
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
			query.append("  extract(timezone_hour from planned_departure_time) as origin_tz, ").append(NEWLINE);
			query.append("  extract(timezone_hour from planned_arrival_time) as destination_tz ").append(NEWLINE);
			query.append("from paxdelay.").append(m_itinerariesTable).append(NEWLINE);
			query.append("where first_operating_carrier = '").append(
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
							.error("Unable to retrieve flight information for flight ID "
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
								.error("Unable to retrieve flight information for flight ID "
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
									: carrierCode) + " on date " + startDateString);
				}
			}
		} catch (SQLException e) {
			logger.error("Unable to load itineraries for date " + startDateString, e);
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

		logger.info("[" + carrierCode + ", " + startDateString + "] Number of "
				+ (multiCarrier ? "multiple" : "single") + " carrier routes = "
				+ routeSet.numRoutes());
		logger.info("[" + carrierCode + ", " + startDateString + "] Number of "
				+ (multiCarrier ? "multiple" : "single")
				+ " carrier itineraries = " + numItineraries);

		return new InternalRouteSet(nonStopRoutes, oneStopRoutes);
	}	
	
	public void loadOneStopRouteDemands(String startDateString,
			String carrierCode, boolean multiCarrier) {
		int totalPassengers = 0;
		int totalNoRoutePassengers = 0;
		String startTimeString = m_oracleFormat.format(m_startDate);
		String endTimeString = m_oracleFormat.format(m_endDate);		
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			StringBuffer query = new StringBuffer();
			query.append("select pa.first_carrier as first_operating_carrier,").append(NEWLINE);
			query.append("  pa.second_carrier as second_operating_carrier,").append(NEWLINE);
			query.append("  pa.origin as origin, ").append(NEWLINE);
			query.append("  ft1.destination as connection,").append(NEWLINE);
			query.append("  pa.destination as destination,").append(NEWLINE);
			query.append("  sum(pa.passengers) as passengers").append(NEWLINE);
			query.append("from ").append(m_passengerAllocationsTable).append(" pa ").append(NEWLINE);
			query.append("join ").append(m_allocationFlightsTable).append(" ft1 ").append(NEWLINE);
			query.append("  on ft1.id = pa.first_flight_id").append(NEWLINE);
			query.append("join ").append(m_allocationFlightsTable).append(" ft2 ").append(NEWLINE);
			query.append("  on ft2.id = pa.second_flight_id").append(NEWLINE);
			query.append("where pa.num_flights = 2").append(NEWLINE);
			query.append("  and ft1.actual_arrival_time >= '").append(startTimeString)
				.append("'").append(NEWLINE);
			query.append("  and ft2.planned_departure_time < '").append(endTimeString)
				.append("'").append(NEWLINE);
			// If multiple carrier, then we look for routes with this carrier
			// first
			// or routes where this carrier is second and the first carrier is
			// not ASQP
			if (multiCarrier) {
				query.append("  and pa.first_carrier = '").append(carrierCode)
					.append("'").append(NEWLINE);
				query.append("  and pa.second_carrier != pa.first_carrier")
					.append(NEWLINE);
			}
			// Otherwise, query for single carrier route demands
			else {
				query.append("  and pa.first_carrier = '").append(
						carrierCode).append("'").append(NEWLINE);
				query.append("  and pa.second_carrier = '").append(
						carrierCode).append("'").append(NEWLINE);
			}
			query.append("group by pa.first_carrier, pa.second_carrier,").append(NEWLINE);
			query.append("  pa.origin, ft1.destination, pa.destination");						

			logger.trace("Route demand query:");
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
				// Scale the passengers to allow for load factor adjustments
				passengers = (int) Math.round(passengers * m_scalingFactor);

				InternalRoute route = getInternalRoute(firstCarrierCode,
						secondCarrierCode, origin, connection, destination);
				totalPassengers += passengers;

				// If one of the flights is not an ASQP flight or the route does
				// not have any matching itineraries, we can just ignore it
				// since
				// any stray passengers will be picked up based on the
				// excess segment demand
				if (route == null) {
					totalNoRoutePassengers += passengers;
					continue;
				}

				route.setPassengerDemand(passengers);
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

		m_noRoutePassengers += totalNoRoutePassengers;
		double matchFraction = 1.0;
		if (totalPassengers > 0) {
			matchFraction = 1.0 - ((double) totalNoRoutePassengers / (double) totalPassengers);
		}
		logger
				.info("[" + carrierCode + ", " + startDateString + "] Total daily "
						+ (multiCarrier ? "multiple" : "single")
						+ " carrier one stop route demand = "
						+ totalPassengers);
		String matchPercentString = String.format("%1$.2f",
				100.0 * matchFraction);
		logger.info("[" + carrierCode + ", " + startDateString + "] Matched "
				+ matchPercentString + "% of daily "
				+ (multiCarrier ? "multiple" : "single")
				+ " carrier one stop route demand");
	}

	protected String getPeriodFilePrefix(String period) {
		return "Date_" + period;
	}
}

package edu.mit.nsfnats.paxdelay.choice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import edu.mit.nsfnats.paxdelay.util.PropertiesReader;

import oracle.jdbc.pool.OracleDataSource;

public class DeltaTime extends ChoiceSetFeature {
	public static Logger logger = Logger.getLogger(DeltaTime.class);
	public static final String PROPERTY_MINIMUM_LAYOVER_DURATION =
		DeltaTime.class.getName() + ".MINIMUM_LAYOVER_DURATION";
	public static final double DEFAULT_MINIMUM_LAYOVER_DURATION = 30.0;

	public static final String FLIGHT_TIMES_QUERY = "select origin, destination,\n"
			+ "  sum (ramp_to_ramp) / sum(departures_performed)\n"
			+ "    as ramp_to_ramp\n"
			+ "from t100_segments\n"
			+ "group by origin, destination\n"
			+ "having sum(departures_performed) > 0";

	public static final String AIRPORT_TIME_ZONES_QUERY = "select code, timezone_region\n"
			+ "from airports";

	String[] m_columnHeaders;
	Map<String, Double> m_flightTimes;
	Map<String, String> m_airportTimeZones;
	double m_minimumLayoverDuration;

	public DeltaTime() {
		m_columnHeaders = new String[] { "Orig_Offset", "Orig_DST", "Dest_Offset", "Dest_DST", 
				"Delta_Time_Zone", "Delta_Flight_Time" };
		m_flightTimes = new HashMap<String, Double>();
		m_airportTimeZones = new HashMap<String, String>();
	}

	public String[] getColumnHeaders() {
		return m_columnHeaders;
	}

	@Override
	public FeatureValue getValue(AirlineItinerary itinerary) {
		CarrierFlightPath[] flights = itinerary.getFlightPaths();
		double totalFlightTime = 0.0;
		for (int i = 0; i < flights.length; ++i) {
			String flightKey = getFlightKey(flights[i].getOrigin(), flights[i]
					.getDestination());
			Double flightTime = m_flightTimes.get(flightKey);
			if (flightTime == null) {
				logger.warn("Unable to determine flight time for flight "
						+ flightKey);
				return null;
			} else {
				totalFlightTime += flightTime.doubleValue();
			}
		}
		if (flights.length > 1) {
			totalFlightTime += (flights.length - 1) * m_minimumLayoverDuration;
		}
		String originTimeZoneString = m_airportTimeZones.get(flights[0]
				.getOrigin());
		String destinationTimeZoneString = m_airportTimeZones
				.get(flights[flights.length - 1].getDestination());
		TimeZone originTimeZone = TimeZone.getTimeZone(originTimeZoneString);
		TimeZone destinationTimeZone = TimeZone.getTimeZone(destinationTimeZoneString);
		
		String originOffsetString = String.format("%1$4.0f",
				(double)originTimeZone.getRawOffset() / (1000.0 * 60.0 * 60.0));
		String destinationOffsetString = String.format("%1$4.0f",
				(double)destinationTimeZone.getRawOffset() / (1000.0 * 60.0 * 60.0));
		
		Calendar calendar = new GregorianCalendar(originTimeZone);
		calendar.set(Calendar.YEAR, itinerary.getYear());
		calendar.set(Calendar.DAY_OF_YEAR, itinerary.getDayOfYear());
		calendar.set(Calendar.HOUR_OF_DAY, itinerary.getLocalDepartureHour());
		calendar.set(Calendar.MINUTE, itinerary.getLocalDepartureMinutes());
		
		String originDSTString = 
			originTimeZone.inDaylightTime(calendar.getTime()) ? "1" : "0";		
		
		int departureOffset = originTimeZone.getOffset(calendar.getTimeInMillis());
		calendar.add(Calendar.MINUTE, (int) Math.ceil(totalFlightTime));
		int arrivalOffset = destinationTimeZone.getOffset(calendar.getTimeInMillis());
		
		String destinationDSTString = 
			destinationTimeZone.inDaylightTime(calendar.getTime()) ? "1" : "0";
		
		double deltaTimeZone = 
			((double) arrivalOffset - (double) departureOffset) / (1000.0 * 60.0 * 60.0);
		
		double deltaFlightTime = deltaTimeZone + (totalFlightTime / 60);
		deltaFlightTime = (deltaFlightTime + 24.0) % 24.0;

		String deltaTimeZoneString = String.format("%1$4.0f", deltaTimeZone);
		String deltaFlightTimeString = String.format("%1$4.2f", deltaFlightTime);

		return new FeatureValue(this, new String[] {
				originOffsetString, originDSTString,
				destinationOffsetString, destinationDSTString, 
				deltaTimeZoneString, deltaFlightTimeString });
	}

	@Override
	public void initialize(Properties properties) {
		m_minimumLayoverDuration = PropertiesReader.readDouble(properties,
				PROPERTY_MINIMUM_LAYOVER_DURATION,
				DEFAULT_MINIMUM_LAYOVER_DURATION);

		String jdbcURL = properties
				.getProperty(BiogemeDataFileCreator.PROPERTY_JDBC_URL);
		String dbUsername = properties
				.getProperty(BiogemeDataFileCreator.PROPERTY_DATABASE_USERNAME);
		String dbPassword = properties
				.getProperty(BiogemeDataFileCreator.PROPERTY_DATABASE_PASSWORD);

		OracleDataSource dataSource = null;
		Connection dbConnection = null;

		try {
			dataSource = new OracleDataSource();
			dataSource.setURL(jdbcURL);
			dbConnection = dataSource.getConnection(dbUsername, dbPassword);

			loadFlightTimes(dbConnection);
			loadAirportTimeZones(dbConnection);
		} catch (SQLException e) {
			logger.error("Received unexpected SQL exception while initializing"
					+ " DeltaTime feature " + e.toString());
			logger.error(e.getStackTrace());
		} finally {
			if (dbConnection != null) {
				try {
					dbConnection.close();
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

	public void loadFlightTimes(Connection connection) throws SQLException {
		PreparedStatement statement = null;
		ResultSet results = null;
		try {
			statement = connection.prepareStatement(FLIGHT_TIMES_QUERY);
			logger.trace(statement);
			results = statement.executeQuery();
			while (results.next()) {
				String origin = results.getString("ORIGIN");
				String destination = results.getString("DESTINATION");
				double flightTime = results.getDouble("RAMP_TO_RAMP");

				String flightKey = getFlightKey(origin, destination);
				m_flightTimes.put(flightKey, new Double(flightTime));
			}
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Exception e) {
					logger
							.error("Unable to close flight times results set due to exception"
									+ e.toString());
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					logger
							.error("Unable to close flight times statement due to exception"
									+ e.toString());
				}
			}
		}
	}

	public void loadAirportTimeZones(Connection connection) throws SQLException {
		PreparedStatement statement = null;
		ResultSet results = null;
		try {
			statement = connection.prepareStatement(AIRPORT_TIME_ZONES_QUERY);
			logger.trace(statement);
			results = statement.executeQuery();
			while (results.next()) {
				String code = results.getString("CODE");
				String timeZone = results.getString("TIMEZONE_REGION");

				m_airportTimeZones.put(code, timeZone);
			}
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Exception e) {
					logger
							.error("Unable to close time zones results set due to exception"
									+ e.toString());
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					logger
							.error("Unable to close time zones statement due to exception"
									+ e.toString());
				}
			}
		}
	}

	protected String getFlightKey(String origin, String destination) {
		return origin + "_" + destination;
	}
}

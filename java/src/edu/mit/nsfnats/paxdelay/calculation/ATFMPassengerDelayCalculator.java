package edu.mit.nsfnats.paxdelay.calculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.FileProcessor;
import edu.mit.nsfnats.paxdelay.util.LineProcessor;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import edu.mit.nsfnats.paxdelay.util.TimeFormat;

public class ATFMPassengerDelayCalculator extends PassengerDelayCalculator {
	public static final String PROPERTY_ATFM_OUTPUT_DIRECTORY = "ATFM_OUTPUT_DIRECTORY";
	public static final String PROPERTY_ATFM_SOLUTION_FILENAME = "ATFM_SOLUTION_FILENAME";
	public static final String PROPERTY_ATFM_STATISTICS_FILENAME = "ATFM_STATISTICS_FILENAME";
	public static final String PROPERTY_ATFM_MODEL_INTERVAL_LENGTH = "ATFM_MODEL_INTERVAL_LENGTH";
	
	public static final String VALUE_TOKEN = "Value";
	public static final String TOKEN_SEPARATOR = ".";
	public static final String NUMERIC_EQUALITY_TOKEN = "=";
	
	public static final String HEADER_PREFIX = ">> ";
	public static final String HEADER_SUFFIX = " <<";
	
	String m_atfmSolutionFile;
	String m_atfmStatisticsFile;
	int m_modelIntervalLength;
	
	RecoveryDelayCalculator m_delayCalculator;
	int m_currentYear;
	int m_currentMonth;
	
	double m_totalPassengers;
	double m_cancelledPassengers;
	double m_missConnectedPassengers;
	
	double m_totalPassengerDelay;
	double m_cancelledPassengerDelay;
	double m_missConnectedPassengerDelay;
	
	Map<String, FlightScheduleAdjustment> m_scheduleAdjustmentMap;
	
	public ATFMPassengerDelayCalculator(String outputDirectory) {
		super(outputDirectory);
		
		m_scheduleAdjustmentMap = new HashMap<String, FlightScheduleAdjustment>();
	}
	
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println(
					"Usage: java edu.mit.nsfnats.paxdelay.calculation.PassengerDelayCaculator"
					+ " <logger_properties_file> <calculator_properties_file> <output_directory>");
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

		Properties calculatorProperties = null;
		try {
			calculatorProperties = PropertiesReader.loadProperties(args[1]);
		} catch (FileNotFoundException e) {
			exit("Delay calculator properties file not found", e, -1);
		} catch (IOException e) {
			exit("Received IO exception while reading delay calculator properties file",
					e, -1);
		}
		String outputDirectory = args[2];
		logger.info("Beginning ATFMPassengerDelayCalculator.main(Properties properties) execution");
		main(calculatorProperties, outputDirectory);
		logger.info("Execution of ATFMPassengerDelayCalculator.main(Properties properties) complete");
	}
	

	public static void main(Properties properties, String outputDirectory) {
		ATFMPassengerDelayCalculator pdc = new ATFMPassengerDelayCalculator(outputDirectory);
		try {
			pdc.initialize(properties);
		} catch (InvalidFormatException e) {
			exit("Invalid format specified in delay calculator properties file", e, -1);
		}
		try {
			pdc.connectToDatabase();
			logger.trace("Connected to database");

			pdc.loadRelatedCarriers();

			pdc.loadDefaultSeatingCapacities();
			
			pdc.loadAircraftFleetTypes();
			
			pdc.loadPlannedSchedule();

			pdc.writeOutputHeader();

			pdc.calculatePassengerDelays();
			
			pdc.writePassengerDelayStatistics();

		} finally {
			pdc.disconnectFromDatabase();
			logger.trace("Disconnected from database");
		}
	}
	
	public void initialize(Properties properties) throws InvalidFormatException {
		super.initialize(properties);
		
		String atfmOutputDirectory = properties.getProperty(PROPERTY_ATFM_OUTPUT_DIRECTORY);
		// If the ATFM output directory is not specified, we assume that the solutions
		// and statistics file reside in the output directory
		if (atfmOutputDirectory == null) {
			atfmOutputDirectory = m_outputDirectory;
		}
		
		String atfmSolutionFilename = properties.getProperty(PROPERTY_ATFM_SOLUTION_FILENAME);
		if (atfmSolutionFilename == null) {
			throw new InvalidFormatException("Must specify property " + 
					PROPERTY_ATFM_SOLUTION_FILENAME + " for ATFMPassengerDelayCalculator");
		}
		m_atfmSolutionFile = atfmOutputDirectory + File.separator + atfmSolutionFilename;
		File atfmSolutionFile = new File(m_atfmSolutionFile);
		if (!atfmSolutionFile.exists()) {
			throw new InvalidFormatException("ATFM solution file " + m_atfmSolutionFile +
				" does not exist");
		}
		
		String atfmStatisticsFilename = properties.getProperty(PROPERTY_ATFM_STATISTICS_FILENAME);
		if (atfmStatisticsFilename == null) {
			throw new InvalidFormatException("Must specify property " + 
					PROPERTY_ATFM_STATISTICS_FILENAME + " for ATFMPassengerDelayCalculator");
		}
		m_atfmStatisticsFile = atfmOutputDirectory + File.separator + atfmStatisticsFilename;
		File atfmStatisticsFile = new File(m_atfmStatisticsFile);
		if (!atfmStatisticsFile.exists()) {
			throw new InvalidFormatException("ATFM statistics file " + m_atfmStatisticsFile +
				" does not exist");
		}
		try {
			m_modelIntervalLength = PropertiesReader.readInt(properties, 
					PROPERTY_ATFM_MODEL_INTERVAL_LENGTH);
		}
		catch (NullPointerException e) {
			throw new InvalidFormatException("Must specify property " + 
					PROPERTY_ATFM_MODEL_INTERVAL_LENGTH + " for ATFMPassengerDelayCalculator");
		}
		catch (NumberFormatException e) {
			throw new InvalidFormatException("Property " + PROPERTY_ATFM_MODEL_INTERVAL_LENGTH + 
					" must be specified as an integral value for ATFMPassengerDelayCalculator");
		}
	}
	
	public void loadPlannedSchedule() {
		FileProcessor fileProcessor = new FileProcessor(new ModelSolutionLineProcessor());
		try {
			fileProcessor.processFile(m_atfmSolutionFile);
		}
		catch (FileNotFoundException e) {
			exit("Unable to load model solution file " + m_atfmSolutionFile +
					" due to FileNotFoundException", e, -1);
		}
		catch (InvalidFormatException e) {
			exit("Unable to process model solution file " + m_atfmSolutionFile +
					" due to InvalidFormatException", e, -1);
		}
	}
	
	// For this version, we load only the planned schedule and retrieve all
	// of the actual schedule details from the solution file
	@Override
	public void loadFlightDataInternal(int month, int day, 
			List<InternalFlight> updatedFlightList) {
		
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement();
			int quarter = getQuarterForMonth(month);

			StringBuffer query = new StringBuffer();
			query.append("select ft.id, ft.metron_id,").append(NEWLINE);
			query.append("  ft.carrier, ft.origin, ft.destination,").append(NEWLINE);
			query.append("  ft.icao_aircraft_code, ft.seating_capacity,").append(NEWLINE);
			query.append("  ft.planned_departure_time,").append(NEWLINE);
			query.append("  ft.planned_arrival_time,").append(NEWLINE);
			query.append("  orig.timezone_region as origin_time_zone,")
				.append(NEWLINE);
			query.append("  dest.timezone_region as destination_time_zone")
				.append(NEWLINE);
			query.append("from paxdelay.").append(m_flightsTable).append(" ft ").append(NEWLINE);
			query.append("join paxdelay.airports orig").append(NEWLINE);
			query.append("  on orig.code = ft.origin").append(NEWLINE);
			query.append("join paxdelay.airports dest").append(NEWLINE);
			query.append("  on dest.code = ft.destination").append(NEWLINE);
			query.append("where ft.year = ").append(m_year).append(NEWLINE);
			query.append("  and ft.quarter = ").append(quarter).append(NEWLINE);
			query.append("  and ft.month = ").append(month).append(NEWLINE);
			query.append("  and ft.day_of_month = ").append(day).append(NEWLINE);
			query.append("  and ft.carrier in").append(NEWLINE);
			query.append("    ").append(getCarrierSetString()).append(NEWLINE);
			query.append("order by ft.planned_departure_time");

			logger.trace("Flight data query:");
			logger.trace(query.toString());
			
			rset = stmt.executeQuery(query.toString());
			while (rset.next()) {
				int flightID = rset.getInt("id");
				String metronID = rset.getString("metron_id");
				String carrier = rset.getString("carrier");
				String origin = rset.getString("origin");
				String destination = rset.getString("destination");
				String aircraftType = rset.getString("icao_aircraft_code");
				double seatingCapacity = rset.getDouble("seating_capacity");
				if (seatingCapacity < 1) {
					//OPHASWONGSE
					//Remove aircraft type from parameter
					seatingCapacity = estimateDefaultSeatingCapacity(flightID, carrier,
							origin, destination);
				}

				Timestamp plannedDeparture = rset
						.getTimestamp("planned_departure_time");
				Timestamp plannedArrival = rset
						.getTimestamp("planned_arrival_time");
				
				String originTimeZone = rset.getString("origin_time_zone");
				String destinationTimeZone = rset.getString("destination_time_zone");
				
				FlightScheduleAdjustment scheduleAdjustment = 
					m_scheduleAdjustmentMap.get(metronID);
				
				boolean isCancelled = scheduleAdjustment == null ||
					scheduleAdjustment.isFlightCancelled();
				Date actualDeparture = null;
				Date actualArrival = null;
				Date disruptionTime = null;
				int localDisruptionHour = 0;
				if (isCancelled) {
					disruptionTime = plannedDeparture;
					Calendar calendar = 
						new GregorianCalendar(TimeZone.getTimeZone(originTimeZone));
					calendar.setTime(disruptionTime);
					localDisruptionHour = calendar.get(Calendar.HOUR_OF_DAY);
				}
				else {
					actualDeparture = 
						scheduleAdjustment.getActualDepartureTime(plannedDeparture);
					actualArrival = 
						scheduleAdjustment.getActualArrivalTime(plannedArrival);
					disruptionTime = actualArrival;
					Calendar calendar = 
						new GregorianCalendar(TimeZone.getTimeZone(destinationTimeZone));
					calendar.setTime(disruptionTime);
					localDisruptionHour = calendar.get(Calendar.HOUR_OF_DAY);
				}

				InternalFlight flight = new InternalFlight(flightID, carrier,
						month, day, origin, destination, plannedDeparture, plannedArrival,
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
	
	@Override
	public int getDefaultDelay(Date disruptionTime, InternalItinerary itinerary,
			String disruptionOrigin, int causeOfDisruption, int disruptionHour) {
		Calendar calendar = new GregorianCalendar(TimeFormat.getUniversalTimeZone());
		calendar.setTime(disruptionTime);
		int year = calendar.get(Calendar.YEAR);
		// Java months start at 0, so we need to adjust accordingly
		int month = calendar.get(Calendar.MONTH) + 1;
		RecoveryDelayCalculator delayCalculator = getRecoveryDelayCalculator(year, month);
		// While estimating the default delay, we will not exceed the maximum 
		// delay calculated by the superclass
		int maximumDelay = getMaximumDelay(disruptionHour);
		// If we are unable to initialize the recovery delay calculator, default
		// to using the default recovery delays from the superclass
		if (delayCalculator == null) {
			return super.getDefaultDelay(disruptionTime, itinerary, disruptionOrigin, 
					causeOfDisruption, disruptionHour);
		}
		String destination = itinerary.getDestination();
		InternalFlight firstFlight = itinerary.getFirstFlight();
		InternalFlight secondFlight = itinerary.getSecondFlight();
		String[] carriers;
		if (secondFlight != null) {
			carriers = new String[]{firstFlight.getCarrier(), secondFlight.getCarrier()};
		}
		else {
			carriers = new String[]{firstFlight.getCarrier()};			
		}
		double estimatedRecoveryDelay;
		// If the disruption is a flight cancellation
		if (causeOfDisruption == PassengerDelayCalculator.DISRUPTION_DUE_TO_CANCELLATION) {
			int numStopsRemaining = 0;
			if (secondFlight != null &&
					disruptionOrigin.equals(itinerary.getFirstFlight().getOrigin())) {
					numStopsRemaining = 1;
			}
			estimatedRecoveryDelay = 
					delayCalculator.calculateCancellationDelay(carriers, disruptionOrigin, 
							destination, disruptionHour, numStopsRemaining);
		}
		// Otherwise, the disruption is due to a missed connection...
		estimatedRecoveryDelay = 
				delayCalculator.calculateMissedConnectionDelay(carriers, disruptionOrigin, 
						destination, disruptionHour);
		
		// We do not exceed the maximum delay calculated by the superclass
		return Math.min(maximumDelay, (int) Math.round(estimatedRecoveryDelay));
	}
	
	@Override
	public void writeProcessedItinerary(InternalItinerary itinerary) {
		double numPassengers = itinerary.getPassengers();
		double tripDelay = itinerary.getDelay();
		m_totalPassengers += numPassengers;
		m_totalPassengerDelay += numPassengers * tripDelay;
		if (itinerary instanceof RecoveryItinerary) {
			RecoveryItinerary recoveryItinerary = (RecoveryItinerary)itinerary;
			InternalItinerary[] recoverySequence = recoveryItinerary.getRecoverySequence();
			int firstDisruptionCause = 
				((RecoveryItinerary) recoverySequence[1]).getCauseOfDisruption();
			if (firstDisruptionCause == DISRUPTION_DUE_TO_CANCELLATION) {
				m_cancelledPassengers += numPassengers;
				m_cancelledPassengerDelay += numPassengers * tripDelay;
			}
			else {
				m_missConnectedPassengers += numPassengers;
				m_missConnectedPassengerDelay += numPassengers * tripDelay;
			}
		}
		itinerary.writeItinerary(m_fileWriter);
	}
	
	public void writePassengerDelayStatistics() {
		FileOutputStream outputStream = null;
		File outputFile = new File(m_atfmStatisticsFile);
		try {
			outputStream = new FileOutputStream(outputFile, true);
		}
		catch (FileNotFoundException e) {
			exit("Unable to open output stream to append to file " +
					m_atfmStatisticsFile, e, -1);
		}
		PrintWriter writer = new PrintWriter(outputStream);
		writer.println();
		writeStatisticsHeader(writer, "Passenger Delay Statistics");
		try {
			writeStatisticsValue(writer, "Total Passengers", m_totalPassengers);
			writeStatisticsValue(writer, "Cancelled Passengers", m_cancelledPassengers);
			writeStatisticsValue(writer, "Missconnected Passengers", 
					m_missConnectedPassengers);


			writeStatisticsValue(writer, "Total Passenger Delay", m_totalPassengerDelay);
			writeStatisticsValue(writer, "Cancelled Passenger Delay", m_cancelledPassengerDelay);
			writeStatisticsValue(writer, "Missconnected Passenger Delay", 
					m_missConnectedPassengerDelay);

			writer.flush();
		}
		finally {
			try {
				outputStream.close();
			}
			catch (IOException e) {
				exit("Unable to close output stream for file " + m_atfmStatisticsFile, e, -1);				
			}
		}
	}

	public void writeStatisticsHeader(PrintWriter writer, String name) {
		writer.print(HEADER_PREFIX);
		writer.print(name);
		writer.println(HEADER_SUFFIX);
	}
	public void writeStatisticsValue(PrintWriter writer, String name,
			double value) {
		writer.print(VALUE_TOKEN);
		writer.print(TOKEN_SEPARATOR);
		writer.print(name);
		writer.print(" ");
		writer.print(NUMERIC_EQUALITY_TOKEN);
		writer.print(" ");
		writer.println(value);
	}
	
	public RecoveryDelayCalculator getRecoveryDelayCalculator(int year, int month) {
		if (m_delayCalculator == null || m_currentYear != year || m_currentMonth != month) {
			m_delayCalculator = new RecoveryDelayCalculator(m_dbConnection);
			try {
				m_delayCalculator.initialize(year, month);
			}
			catch (InvalidFormatException e) {
				logger.fatal("Unable to initialize recovery delay calculator for year = " + 
						year + " and month = " + month, e);
			}
			m_currentYear = year;
			m_currentMonth = month;
		}
		return m_delayCalculator;
	}
	
	class ModelSolutionLineProcessor extends LineProcessor
	{
		FlightScheduleAdjustment m_currentAdjustment;
		
		public ModelSolutionLineProcessor() {
			super(CSV_FIELD_SEPARATOR);
		}
		
		@Override
		public void processLine() throws InvalidFormatException {
			String metronID = getValue("Flight");
			int minimumStartInterval = getIntegerValue("Minimum Start");
			int scheduledStartInterval = getIntegerValue("Scheduled Start");
			boolean isFlightCancelled = scheduledStartInterval == 0;
			int flightDelay = 0;
			if (!isFlightCancelled) {
				flightDelay = m_modelIntervalLength * 
					(scheduledStartInterval - minimumStartInterval);
			}
			if (flightDelay < 0) {
				throw new InvalidFormatException("Model solution for flight " + metronID + 
						" includes scheduled start of " + scheduledStartInterval + 
						" before minimum start interval of " + minimumStartInterval);
			}
			if (m_currentAdjustment == null ||
					!m_currentAdjustment.getMetronID().equals(metronID)) {
				// Add the previous schedule adjustment before working on the new one
				if (m_currentAdjustment != null) {
					m_scheduleAdjustmentMap.put(m_currentAdjustment.getMetronID(), 
							m_currentAdjustment);
				}
				// The departure delay corresponds to the delay on the first flight step
				// in the flight's schedule
				m_currentAdjustment = 
					new FlightScheduleAdjustment(metronID, isFlightCancelled, flightDelay);
			}
			// The arrival delay corresponds to the delay on the last flight step in
			// the flight's schedule (which may be the same as the first flight step)
			m_currentAdjustment.setArrivalDelayMinutes(flightDelay);
		}
		
		@Override
		public void postProcess() throws InvalidFormatException {
			// We need to add the last schedule adjustment to the map
			if (m_currentAdjustment != null) {
				m_scheduleAdjustmentMap.put(m_currentAdjustment.getMetronID(), 
						m_currentAdjustment);				
			}
		}
	}	
}

class FlightScheduleAdjustment {
	String m_metronID;
	boolean m_isFlightCancelled;
	int m_departureDelayMinutes;
	int m_arrivalDelayMinutes;
	
	public FlightScheduleAdjustment(String metronID, boolean isFlightCancelled, 
			int departureDelayMinutes) {
		m_metronID = metronID;
		m_isFlightCancelled = isFlightCancelled;
		m_departureDelayMinutes = departureDelayMinutes;
	}

	public String getMetronID() {
		return m_metronID;
	}

	public boolean isFlightCancelled() {
		return m_isFlightCancelled;
	}

	public int getDepartureDelayMinutes() {
		return m_departureDelayMinutes;
	}
	
	public int getArrivalDelayMinutes() {
		return m_arrivalDelayMinutes;
	}
	
	public void setArrivalDelayMinutes(int arrivalDelayMinutes) {
		m_arrivalDelayMinutes = arrivalDelayMinutes;
	}
	
	public Date getActualDepartureTime(Date plannedDepartureTime) {
		if (m_isFlightCancelled) {
			return null;
		}
		Calendar calendar = new GregorianCalendar(TimeFormat.getUniversalTimeZone());
		calendar.setTime(plannedDepartureTime);
		calendar.add(Calendar.MINUTE, m_departureDelayMinutes);
		return calendar.getTime();
	}
	
	
	public Date getActualArrivalTime(Date plannedArrivalTime) {
		if (m_isFlightCancelled) {
			return null;
		}
		Calendar calendar = new GregorianCalendar(TimeFormat.getUniversalTimeZone());
		calendar.setTime(plannedArrivalTime);
		calendar.add(Calendar.MINUTE, m_arrivalDelayMinutes);
		return calendar.getTime();
	}
}

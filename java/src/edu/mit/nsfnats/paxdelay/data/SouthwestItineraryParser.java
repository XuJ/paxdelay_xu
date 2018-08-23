package edu.mit.nsfnats.paxdelay.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import edu.mit.nsfnats.paxdelay.util.FileProcessor;
import edu.mit.nsfnats.paxdelay.util.LineProcessor;

public class SouthwestItineraryParser {
	static Logger logger = Logger.getLogger(SouthwestItineraryParser.class);

	public static final String PROPERTY_DATA_DIRECTORY = "DATA_DIRECTORY";
	public static final String PROPERTY_SOUTHWEST_DATA_FILE = "SOUTHWEST_DATA_FILE";
	public static final String PROPERTY_OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";
	public static final String ITINERARIES_FILE = "SouthwestItineraries.csv";
	public static final String FLIGHT_LEGS_FILE = "SouthwestFlightLegs.csv";
	public static final String CSV_FIELD_SEPARATOR = ",";
	
	public static final String SOUTHWEST_CARRIER_CODE = "WN";

	public String m_outputDirectory;
	public int m_currentItineraryID;

	PrintWriter m_itinerariesWriter;
	PrintWriter m_flightLegsWriter;

	public SouthwestItineraryParser(String outputDirectory) {
		m_outputDirectory = outputDirectory;
		m_currentItineraryID = 1;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err
					.println("Usage: java edu.mit.nsfnats.paxdelay.data.SouthwestItineraryParser <logger_properties_file> <itineraries_properties_file>");
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

		Properties itineraryProperties = null;
		try {
			itineraryProperties = PropertiesReader.loadProperties(args[1]);
		} catch (FileNotFoundException e) {
			exit("Itineraries properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading itineraries properties file.",
					e, -1);
		}
		main(itineraryProperties);
	}

	public static void main(Properties itineraryProperties) {
		String outputDirectory = itineraryProperties
				.getProperty(PROPERTY_OUTPUT_DIRECTORY);

		SouthwestItineraryParser itineraryParser = new SouthwestItineraryParser(
				outputDirectory);
		try {
			itineraryParser.initialize();
		} catch (FileNotFoundException e) {
			exit("Unable to load output files for writing.", e, -1);
		}
		String dataDirectory = itineraryProperties
				.getProperty(PROPERTY_DATA_DIRECTORY);
		String southwestDataFile = itineraryProperties.getProperty(PROPERTY_SOUTHWEST_DATA_FILE);
		try {
			itineraryParser.processFile(dataDirectory, southwestDataFile);
		} catch (FileNotFoundException e) {
			exit("Unable to load Southwest data file.", e, -1);
		} catch (InvalidFormatException e) {
			exit(
					"Unable to process Southwest data file due to invalid file format.",
					e, -1);
		}
	}

	public void initialize() throws FileNotFoundException {
		StringBuffer itinerariesFilename = new StringBuffer(m_outputDirectory);
		itinerariesFilename.append(File.separator);
		itinerariesFilename.append(ITINERARIES_FILE);

		m_itinerariesWriter = new PrintWriter(itinerariesFilename.toString());

		StringBuffer flightLegsFilename = new StringBuffer(m_outputDirectory);
		flightLegsFilename.append(File.separator);
		flightLegsFilename.append(FLIGHT_LEGS_FILE);

		m_flightLegsWriter = new PrintWriter(flightLegsFilename.toString());
	}

	public void processFile(String dataDirectory, String southwestDataFile)
			throws FileNotFoundException, InvalidFormatException {
		m_itinerariesWriter.print("Itinerary_ID");
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Num_Flights");
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Origin");
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Destination");
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Departure_Time");
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Arrival_Time");
		// Number of passengers booked
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Number_Pax_Booked");
		// Number of passengers flown
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Number_Pax_Flown");
		m_itinerariesWriter.println();

		m_flightLegsWriter.print("Itinerary_ID");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Num_Flights");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Itinerary_Sequence");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Carrier");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Departure_Time");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Arrival_Time");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Origin");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Destination");
		m_flightLegsWriter.println();

		FileProcessor southwestProcessor = new FileProcessor(
				new SouthwestLineProcessor());
		logger.info("Processing file " + southwestDataFile + "...");
		southwestProcessor.processFile(dataDirectory,
				southwestDataFile);
		logger.info("...finished processing file "
				+ southwestDataFile + ".");

		m_itinerariesWriter.flush();
		m_flightLegsWriter.flush();
	}

	public int getNextItineraryID() {
		return m_currentItineraryID++;
	}

	protected static void exit(String message, Exception e, int code) {
		System.err.println(message);
		System.err.println(e);
		System.exit(code);
	}

	class SouthwestLineProcessor extends LineProcessor {
		Set<String> m_processedItineraries;

		public SouthwestLineProcessor() {
			super(",", true);
			m_processedItineraries = new HashSet<String>();
		}

		@Override
		protected void processLine() throws InvalidFormatException {

			// List of airports along itinerary path separated by spaces
			String airportList = getNextValue().trim();
			StringTokenizer airportTok = new StringTokenizer(airportList, " ");
			int numAirports = airportTok.countTokens();
			String[] airports = new String[numAirports];
			for (int i = 0; i < numAirports; ++i) {
				airports[i] = airportTok.nextToken();
			}
			// First leg departure time (HH:mm)
			String firstDepartureTime = getNextValue().trim();
			// Second leg departure date and time (MM/dd/YYYY HH:mm) 
			String secondDepartureDateTime = getNextValue().trim();
			// Itinerary arrival time
			String arrivalTime = getNextValue().trim();
			// Number of flight legs in the itinerary
			int numberLegs = 0;
			try {
				numberLegs = Integer.parseInt(getNextValue().trim());
			}
			catch (NumberFormatException e) {
				throw new InvalidFormatException("Expected integer for number of flight legs on line " 
						+ getCurrentLineNumber(), e);
			}
			// Burn the day of week values (String and numeric)
			getNextValue(); getNextValue();
			// Departure date (MM/dd/YYYY)
			String departureDay = getNextValue().trim();
			// Number of passengers booked on the itinerary
			int numberBooked = Integer.parseInt(getNextValue().trim());
			// Number of passengers flown on the itinerary
			int numberFlown = Integer.parseInt(getNextValue().trim());

			SouthwestLegData[] flightLegs = new SouthwestLegData[numberLegs];
			// Departure times in format 'MM/dd/YYYY HH:mm'
			DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy H:mm");
			String flightDepartureTimes[] = new String[numberLegs];
			Date firstDepartureDate = null;
			// If the time starts with 24, replace it with 0 and increment the day
			if (firstDepartureTime.substring(0, 2).equals("24")) {
				try {
					firstDepartureDate = dateFormat.parse(departureDay + " 0" + 
							firstDepartureTime.substring(2, firstDepartureTime.length()));
				}
				catch (ParseException e) {
					throw new InvalidFormatException("Invalid departure date/time format on line "
							+ getCurrentLineNumber());
				}
				Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
				calendar.setTime(firstDepartureDate);
				calendar.add(Calendar.DAY_OF_YEAR, 1);
				firstDepartureDate = calendar.getTime();
				flightDepartureTimes[0] = dateFormat.format(firstDepartureDate);
			} else {
				flightDepartureTimes[0] = departureDay + " " + firstDepartureTime;
			}
			if (numberLegs > 1) {
				if (!flightDepartureTimes[0].equals(secondDepartureDateTime)) {
					flightDepartureTimes[1] = secondDepartureDateTime;
				}
				else {
					flightDepartureTimes[1] = "";
				}
			}
			Date arrivalDate = null;
			try {
				arrivalDate = dateFormat.parse(departureDay + " " + arrivalTime);
			}
			catch (ParseException e) {
				throw new InvalidFormatException("Invalid arrival date/time format on line " 
						+ getCurrentLineNumber());
			}
			Date departureDate = null;
			try {
				departureDate = dateFormat.parse(flightDepartureTimes[0]);
			}
			catch (ParseException e) {
				throw new InvalidFormatException("Invalid departure date/time format on line " 
						+ getCurrentLineNumber());
			}
			if (arrivalDate.before(departureDate)) {
				Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
				calendar.setTime(arrivalDate);
				calendar.add(Calendar.DAY_OF_YEAR, 1);
				arrivalDate = calendar.getTime();
			}
			String flightArrivalTimes[] = new String[numberLegs];
			if (numberLegs == 1) {
				flightArrivalTimes[0] = dateFormat.format(arrivalDate);
			}
			else {
				flightArrivalTimes[0] = "";
				flightArrivalTimes[1] = dateFormat.format(arrivalDate);
			}
			for (int i = 0; i < numberLegs; ++i) {
				flightLegs[i] = new SouthwestLegData();
				flightLegs[i].m_carrier = SOUTHWEST_CARRIER_CODE;
				flightLegs[i].m_departureTime = flightDepartureTimes[i];
				flightLegs[i].m_arrivalTime = flightArrivalTimes[i];
				flightLegs[i].m_origin = airports[i];
				flightLegs[i].m_destination = airports[i + 1];
			}

			int itineraryID = getNextItineraryID();
			m_itinerariesWriter.print(itineraryID);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(numberLegs);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(airports[0]);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(airports[numberLegs]);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(flightDepartureTimes[0]);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(flightArrivalTimes[numberLegs - 1]);
			// Number of passengers booked
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(numberBooked);
			// Number of passengers flown
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(numberFlown);
			m_itinerariesWriter.println();

			for (int i = 0; i < flightLegs.length; ++i) {
				m_flightLegsWriter.print(itineraryID);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(numberLegs);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(i + 1);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_carrier);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_departureTime);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_arrivalTime);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_origin);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_destination);
				m_flightLegsWriter.println();
			}
		}
	}
}

class SouthwestLegData {
	String m_carrier;
	String m_departureTime;
	String m_arrivalTime;
	String m_origin;
	String m_destination;
}

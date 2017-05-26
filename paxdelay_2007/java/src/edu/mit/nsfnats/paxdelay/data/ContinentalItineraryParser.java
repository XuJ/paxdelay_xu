package edu.mit.nsfnats.paxdelay.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;
import edu.mit.nsfnats.paxdelay.util.FileProcessor;
import edu.mit.nsfnats.paxdelay.util.LineProcessor;

public class ContinentalItineraryParser {
	static Logger logger = Logger.getLogger(ContinentalItineraryParser.class);

	public static final String PROPERTY_DATA_DIRECTORY = "DATA_DIRECTORY";
	public static final String PROPERTY_CONTINENTAL_DATA_FILE = "CONTINENTAL_DATA_FILE";
	public static final String PROPERTY_OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";
	public static final String ITINERARIES_FILE = "ContinentalItineraries.csv";
	public static final String FLIGHT_LEGS_FILE = "ContinentalFlightLegs.csv";
	public static final String CSV_FIELD_SEPARATOR = ",";

	public String m_outputDirectory;
	public int m_currentItineraryID;

	PrintWriter m_itinerariesWriter;
	PrintWriter m_flightLegsWriter;

	public ContinentalItineraryParser(String outputDirectory) {
		m_outputDirectory = outputDirectory;
		m_currentItineraryID = 1;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err
					.println("Usage: java edu.mit.nsfnats.paxdelay.data.ContinentalItineraryParser <logger_properties_file> <itineraries_properties_file>");
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

		ContinentalItineraryParser itineraryParser = new ContinentalItineraryParser(
				outputDirectory);
		try {
			itineraryParser.initialize();
		} catch (FileNotFoundException e) {
			exit("Unable to load output files for writing.", e, -1);
		}
		String dataDirectory = itineraryProperties
				.getProperty(PROPERTY_DATA_DIRECTORY);
		String[] continentalDataFiles = PropertiesReader.readStrings(
				itineraryProperties, PROPERTY_CONTINENTAL_DATA_FILE);
		try {
			itineraryParser.processFiles(dataDirectory, continentalDataFiles);
		} catch (FileNotFoundException e) {
			exit("Unable to load Continental data file.", e, -1);
		} catch (InvalidFormatException e) {
			exit(
					"Unable to process Continental data files due to invalid file format.",
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

	public void processFiles(String dataDirectory, String[] continentalDataFiles)
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
		// Day of week field
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Day_of_Week");
		// Departure date field
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Departure_Date");
		// Number of samples field
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Number_of_Samples");
		// Number of passengers flown
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Number_Passengers_Flown");
		// Number of no show passengers
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Number_No_Show_Passengers");
		// Passenger no show average
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Passenger_No_Show_Average");
		// Passenger show average
		m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
		m_itinerariesWriter.print("Passenger_Show_Average");
		m_itinerariesWriter.println();

		m_flightLegsWriter.print("Itinerary_ID");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Num_Flights");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Itinerary_Sequence");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Carrier");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Flight_Number");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Departure_Time");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Origin");
		m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
		m_flightLegsWriter.print("Destination");
		m_flightLegsWriter.println();

		for (int i = 0; i < continentalDataFiles.length; ++i) {
			FileProcessor continentalProcessor = new FileProcessor(
					new ContinentalLineProcessor());
			logger.info("Processing file " + continentalDataFiles[i] + "...");
			continentalProcessor.processFile(dataDirectory,
					continentalDataFiles[i]);
			logger.info("...finished processing file "
					+ continentalDataFiles[i] + ".");
		}

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

	class ContinentalLineProcessor extends LineProcessor {
		Set<String> m_processedItineraries;

		public ContinentalLineProcessor() {
			super("\t", false);
			m_processedItineraries = new HashSet<String>();
		}

		@Override
		protected void processLine() throws InvalidFormatException {

			String airportList = getNextValue();
			StringTokenizer airportTok = new StringTokenizer(airportList, " ");
			int numAirports = airportTok.countTokens();
			String[] airports = new String[numAirports];
			for (int i = 0; i < numAirports; ++i) {
				airports[i] = airportTok.nextToken();
			}
			String pathDescription = getNextValue();
			int numFlights = Integer.parseInt(getNextValue());
			// Day number
			int dayOfWeek = Integer.parseInt(getNextValue());
			// Departure date
			String departureDate = getNextValue();
			// Burn the flight number
			getNextValue();
			// Number of samples
			int numberSample = Integer.parseInt(getNextValue());
			// Number of passengers flown
			int numberFlown = Integer.parseInt(getNextValue());
			// Number of no show passengers
			int numberNoShows = Integer.parseInt(getNextValue());
			// No show average
			String noShowAverage = getNextValue();
			// Show average
			String showAverage = getNextValue();

			StringBuffer itineraryDescription = new StringBuffer(departureDate);
			itineraryDescription.append(";").append(pathDescription);
			itineraryDescription.append(";").append(numberSample);
			itineraryDescription.append(",").append(numberFlown);
			itineraryDescription.append(",").append(numberNoShows);

			// If this itinerary has already been processed, we should ignore
			// the duplicate, since the only difference is the flight number
			if (!m_processedItineraries.add(itineraryDescription.toString())) {
				return;
			}

			// Parse the flight path string
			StringTokenizer pathTok = new StringTokenizer(pathDescription, ",");
			// Burn the day of the week
			pathTok.nextToken();
			FlightLegData[] flightLegs = new FlightLegData[numFlights];
			for (int i = 0; i < numFlights; ++i) {
				flightLegs[i] = new FlightLegData();
				String flightDescription = pathTok.nextToken();
				StringTokenizer flightTok = new StringTokenizer(
						flightDescription, " .");
				flightLegs[i].m_carrier = flightTok.nextToken();
				flightLegs[i].m_flightNumber = flightTok.nextToken();
				flightLegs[i].m_departureTime = flightTok.nextToken();
				flightLegs[i].m_origin = airports[i];
				flightLegs[i].m_destination = airports[i + 1];
			}

			int itineraryID = getNextItineraryID();
			m_itinerariesWriter.print(itineraryID);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(numFlights);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(airports[0]);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(airports[numFlights]);
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(flightLegs[0].m_departureTime);
			// Day of week field
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(dayOfWeek);
			// Departure date field
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(departureDate);
			// Number of samples field
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(numberSample);
			// Number of passengers flown
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(numberFlown);
			// Number of no show passengers
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(numberNoShows);
			// Passenger no show average
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(noShowAverage);
			// Passenger show average
			m_itinerariesWriter.print(CSV_FIELD_SEPARATOR);
			m_itinerariesWriter.print(showAverage);
			m_itinerariesWriter.println();

			for (int i = 0; i < flightLegs.length; ++i) {
				m_flightLegsWriter.print(itineraryID);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(numFlights);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(i + 1);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_carrier);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_flightNumber);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_departureTime);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_origin);
				m_flightLegsWriter.print(CSV_FIELD_SEPARATOR);
				m_flightLegsWriter.print(flightLegs[i].m_destination);
				m_flightLegsWriter.println();
			}
		}
	}
}

class FlightLegData {
	String m_carrier;
	String m_flightNumber;
	String m_departureTime;
	String m_origin;
	String m_destination;
}

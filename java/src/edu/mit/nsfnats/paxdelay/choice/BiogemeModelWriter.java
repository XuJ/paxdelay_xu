package edu.mit.nsfnats.paxdelay.choice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;
import edu.mit.nsfnats.paxdelay.util.PropertiesReader;

public class BiogemeModelWriter {
	public static Logger logger = Logger.getLogger(BiogemeModelWriter.class);

	public static final String PROPERTY_BIOGEME_MODEL_CLASS = "BIOGEME_MODEL_CLASS";
	public static final String PROPERTY_BIOGEME_MODEL_DIRECTORY = "BIOGEME_MODEL_DIRECTORY";
	public static final String PROPERTY_BIOGEME_MODEL_FILENAME = "BIOGEME_MODEL_FILENAME";
	public static final String PROPERTY_NUMBER_ALTERNATIVES = "NUMBER_ALTERNATIVES";

	public static final String VARIABLE_PREFIX = "BETA_";
	public static final String WHITESPACE_SEPARATOR = "  ";
	public static final String STARTING_VALUE = "0.0";
	public static final String MINIMUM_VALUE = "-10.0";
	public static final String MAXIMUM_VALUE = "10.0";

	String m_biogemeModelDirectory;
	String m_biogemeModelFilename;
	BiogemeModel m_biogemeModel;
	int m_numAlternatives;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err
					.println("Usage: java edu.mit.nsfnats.paxdelay.choice.BiogemeModelWriter <logger_properties_file> <model_properties_file>");
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

		Properties modelProperties = null;
		try {
			modelProperties = PropertiesReader.loadProperties(args[1]);
		} catch (FileNotFoundException e) {
			exit("Itineraries properties file not found.", e, -1);
		} catch (IOException e) {
			exit(
					"Received IO exception while reading itineraries properties file.",
					e, -1);
		}
		main(modelProperties);
	}

	public static void main(Properties properties) {
		BiogemeModelWriter modelWriter = new BiogemeModelWriter();
		try {
			modelWriter.initialize(properties);
		} catch (InvalidFormatException e) {
			exit("Invalid format specified in data properties file", e, -1);
		}
		try {
			modelWriter.writeBiogemeModelFile();
		} catch (FileNotFoundException e) {
			exit("Unable to write to Biogeme model file", e, -1);
		}
	}

	protected static void exit(String message, Exception e, int code) {
		System.err.println(message);
		System.err.println(e);
		System.exit(code);
	}

	@SuppressWarnings("unchecked")
	public void initialize(Properties properties) throws InvalidFormatException {
		m_biogemeModelDirectory = properties
				.getProperty(PROPERTY_BIOGEME_MODEL_DIRECTORY);
		File validationDirectory = new File(m_biogemeModelDirectory);
		if (!validationDirectory.exists()) {
			validationDirectory.mkdir();
		}
		m_biogemeModelFilename = properties
				.getProperty(PROPERTY_BIOGEME_MODEL_FILENAME);
		m_numAlternatives = PropertiesReader.readInt(properties,
				PROPERTY_NUMBER_ALTERNATIVES);

		String modelClass = properties
				.getProperty(PROPERTY_BIOGEME_MODEL_CLASS);
		if (modelClass == null) {
			throw new InvalidFormatException("No "
					+ PROPERTY_BIOGEME_MODEL_CLASS + " property specified");
		}
		Class<BiogemeModel> clazz = null;
		try {
			clazz = (Class<BiogemeModel>) Class.forName(modelClass);
		} catch (ClassNotFoundException e) {
			throw new InvalidFormatException("Biogeme model class "
					+ modelClass + " not found", e);
		}
		try {
			m_biogemeModel = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new InvalidFormatException(
					"Unable to instantion Biogeme model class " + modelClass, e);
		} catch (IllegalAccessException e) {
			throw new InvalidFormatException(
					"Unable to access default constructor for "
							+ "Biogeme model class " + modelClass, e);
		}
		m_biogemeModel.initialize(m_numAlternatives);
	}

	public void writeBiogemeModelFile() throws FileNotFoundException {
		StringBuffer outputFilename = new StringBuffer(m_biogemeModelDirectory);
		outputFilename.append(File.separator);
		outputFilename.append(m_biogemeModelFilename);

		PrintWriter writer = new PrintWriter(outputFilename.toString());

		// [Choice]
		writeChoiceSection(writer);
		writer.println();

		// [Beta]
		writeBetaSection(writer);
		writer.println();

		// [Utilities]
		writeUtilitiesSection(writer);
		writer.println();

		// [Expressions]
		writeExpressionsSection(writer);
		writer.println();

		// [Weight]
		writeWeightSection(writer);
		writer.println();

		// [Model]
		writeModelSection(writer);
		writer.println();

		// [Exclude]
		writeExcludeSection(writer);

		writer.flush();
	}

	public void writeChoiceSection(PrintWriter writer) {
		writer.println("[Choice]");
		// Choice column name
		writer.println("Choice");
	}

	public void writeBetaSection(PrintWriter writer) {
		ExpressionSequence[] primaryExprSeqs = m_biogemeModel
				.getPrimaryExpressionSequences();
		Map<String, String> variableSuffixMap = new HashMap<String, String>();
		List<String> variableSuffixList = new ArrayList<String>();
		for (int i = 0; i < primaryExprSeqs.length; ++i) {
			Expression[] exprs = primaryExprSeqs[i].getExpressions();
			for (int j = 0; j < exprs.length; ++j) {
				String name = exprs[j].getName();
				String value = variableSuffixMap.get(name);
				if (value == null) {
					variableSuffixMap.put(name, name);
					variableSuffixList.add(name);
				}
			}
			// Add a null value to place a blank line in the file
			variableSuffixList.add(null);
		}
		String[] variableSuffixes = new String[variableSuffixList.size()];
		variableSuffixList.toArray(variableSuffixes);
		writer.println("[Beta]");
		for (int i = 0; i < variableSuffixes.length; ++i) {
			if (variableSuffixes[i] == null) {
				writer.println();
				continue;
			}
			// Name
			writer.print(VARIABLE_PREFIX);
			writer.print(variableSuffixes[i]);
			// Start
			writer.print(WHITESPACE_SEPARATOR);
			writer.print(STARTING_VALUE);
			// Min
			writer.print(WHITESPACE_SEPARATOR);
			writer.print(MINIMUM_VALUE);
			// Max
			writer.print(WHITESPACE_SEPARATOR);
			writer.print(MAXIMUM_VALUE);
			// Fixed
			writer.print(WHITESPACE_SEPARATOR);
			writer.println(0);
		}
	}

	public void writeUtilitiesSection(PrintWriter writer) {
		ExpressionSequence availExprSeq = m_biogemeModel
				.getAvailabilityExpressionSequence();
		Expression[] availExprs = availExprSeq.getExpressions();
		ExpressionSequence[] primaryExprSeqs = m_biogemeModel
				.getPrimaryExpressionSequences();
		writer.println("[Utilities]");
		for (int i = 0; i < m_numAlternatives; ++i) {
			// Choice ID
			writer.print(i);
			// Name
			writer.print(WHITESPACE_SEPARATOR);
			writer.print("Choice_" + i);
			// Availability
			writer.print(WHITESPACE_SEPARATOR);
			writer.print(availExprs[i].getName() + "_" + i);
			// Linear utility function
			writer.print(WHITESPACE_SEPARATOR);
			boolean isFirst = true;
			for (int j = 0; j < primaryExprSeqs.length; ++j) {
				Expression[] expressions = primaryExprSeqs[j]
						.getExpressionsForAlternative(i);
				for (int k = 0; k < expressions.length; ++k) {
					if (!isFirst) {
						writer.print(" + ");
					}
					String name = expressions[k].getName();
					writer.print(VARIABLE_PREFIX);
					writer.print(name);
					writer.print(" * ");
					writer.print(name);
					writer.print("_");
					writer.print(i);

					isFirst = false;
				}
			}
			writer.println();
		}
	}

	public void writeExpressionsSection(PrintWriter writer) {

		writer.println("[Expressions]");
		ExpressionSequence availExprSeq = m_biogemeModel
				.getAvailabilityExpressionSequence();
		ExpressionSequence[] primaryExprSeqs = m_biogemeModel
				.getPrimaryExpressionSequences();
		availExprSeq.writeExpressions(writer);

		for (int i = 0; i < primaryExprSeqs.length; ++i) {
			primaryExprSeqs[i].writeExpressions(writer);
		}
	}

	public void writeWeightSection(PrintWriter writer) {
		writer.println("[Weight]");
		// Weight column name
		writer.println("Passenger_Count");
	}

	public void writeModelSection(PrintWriter writer) {
		writer.println("[Model]");
		// One of $MNL, $NL, $CNL, or $NGEV
		writer.println("$MNL");
	}

	public void writeExcludeSection(PrintWriter writer) {
		// Do nothing for now...
	}
}

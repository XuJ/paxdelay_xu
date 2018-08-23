package edu.mit.nsfnats.paxdelay.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;

public abstract class LineProcessor {
	// Sub-class is responsible for setting delimiter string during construction
	protected String m_delimiterString;
	protected Map<String, Integer> m_columnMap;
	protected String[] m_columns;

	protected String[] m_currentLineData;
	protected int m_currentLineNumber;
	protected int m_currentLineIndex;

	protected boolean m_includeEmpties;
	
	public LineProcessor(String delimiterString) {
		this(delimiterString, true);
	}
	
	public LineProcessor(String delimiterString, boolean includeEmpties) {
		m_delimiterString = delimiterString;
		m_columnMap = new HashMap<String, Integer>();
		m_currentLineIndex = 0;
		m_includeEmpties = includeEmpties;
	}

	protected int processHeader(BufferedReader reader, String filename)
			throws NoSuchElementException, InvalidFormatException {
		// If no columns have been specified, then in the default case,
		// we will try to read the column names off of the first
		// data line in the file
		return processColumnNames(reader, filename, 0);
	}

	protected int processColumnNames(BufferedReader reader, String filename,
			int lineNumber) throws NoSuchElementException,
			InvalidFormatException {
		try {
			String header = reader.readLine();
			++lineNumber;

			// Skip any blank lines (i.e. all whitespace) and allow comments
			// using #
			while (header != null
					&& (header.trim().length() == 0 || header.trim()
							.startsWith(FileProcessor.COMMENT_PREFIX_STRING))) {
				header = reader.readLine();
				++lineNumber;
			}

			processColumnNames(header, lineNumber);
		} catch (IOException e) {
			throw new InvalidFormatException(
					"Error reading column names for file " + filename, e);
		}

		return lineNumber;
	}
	
	protected void processColumnNames(String header, int lineNumber)
	throws NoSuchElementException, InvalidFormatException {
		CSVTokenizer headerParser = new CSVTokenizer(header, m_delimiterString,
				true);
		processColumnNames(headerParser, lineNumber);
	}

	protected void processColumnNames(CSVTokenizer headerProcessor,
			int lineNumber) throws NoSuchElementException,
			InvalidFormatException {
		List<String> columnList = new ArrayList<String>();
		for (int i = 0; headerProcessor.hasMoreTokens(); ++i) {
			String columnName = headerProcessor.nextToken().trim();
			m_columnMap.put(columnName, new Integer(i));
			columnList.add(columnName);
		}
		m_columns = new String[columnList.size()];
		columnList.toArray(m_columns);
	}

	protected int getColumnIndex(String columnName) {
		Integer column = m_columnMap.get(columnName);
		if (column == null) {
			return -1;
		}
		return column.intValue();
	}
	
	protected void processLine(String line, int lineNumber)
			throws NoSuchElementException, InvalidFormatException {
		CSVTokenizer lineParser = new CSVTokenizer(line, m_delimiterString,
				m_includeEmpties);

		m_currentLineNumber = lineNumber;
		List<String> lineDataList = new ArrayList<String>();
		while (lineParser.hasMoreTokens()) {
			lineDataList.add(lineParser.nextToken());
		}
		m_currentLineData = new String[lineDataList.size()];
		lineDataList.toArray(m_currentLineData);
		m_currentLineIndex = 0;

		processLine();
	}

	public String getColumnName(int columnIndex) {
		return m_columns[columnIndex];
	}
	
	public String getNextColumnName() {
		return m_columns[m_currentLineIndex + 1];
	}
	
	public boolean hasMoreValues() {
		return m_currentLineIndex < m_currentLineData.length;
	}

	public String getNextValue() throws NoSuchElementException,
			InvalidFormatException {
		int index = m_currentLineIndex++;
		return getValue(index);
	}

	public String getValue(String columnName) throws NoSuchElementException,
	InvalidFormatException {
		int index = getColumnIndex(columnName);
		return getValue(index);
	}

	public String getValue(int columnIndex) throws NoSuchElementException,
			InvalidFormatException {
		String value = null;
		try {
			value = m_currentLineData[columnIndex];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new NoSuchElementException("Index " + columnIndex
					+ " is invalid");
		}
		return value;
	}

	public int getIntegerNextValue() throws NoSuchElementException,
			InvalidFormatException {
		int index = m_currentLineIndex++;
		return getIntegerValue(index);
	}

	public int getIntegerValue(String columnName)
			throws NoSuchElementException, InvalidFormatException {
		int index = getColumnIndex(columnName);
		return getIntegerValue(index);
	}

	public int getIntegerValue(int columnIndex) throws NoSuchElementException,
			InvalidFormatException {
		String numberString = getValue(columnIndex);
		int number;
		try {
			number = Integer.parseInt(numberString);
		} catch (NumberFormatException e) {
			throw new InvalidFormatException("String in column " + columnIndex
					+ "(" + numberString
					+ ") is not an integer on line number "
					+ m_currentLineNumber);
		}
		return number;
	}

	public long getLongNextValue() throws NoSuchElementException,
			InvalidFormatException {
		int index = m_currentLineIndex++;
		return getLongValue(index);
	}

	public long getLongNextValue(long defaultValue)
			throws NoSuchElementException, InvalidFormatException {
		int index = m_currentLineIndex++;
		return getLongValue(index, new Long(defaultValue));
	}

	public long getLongValue(int columnIndex) throws NoSuchElementException,
			InvalidFormatException {
		return getLongValue(columnIndex, null);
	}

	public long getLongValue(int columnIndex, long defaultValue)
			throws NoSuchElementException, InvalidFormatException {
		return getLongValue(columnIndex, new Long(defaultValue));
	}

	public long getLongValue(int columnIndex, Long defaultValue)
			throws NoSuchElementException, InvalidFormatException {
		String numberString = getValue(columnIndex);
		long number;
		try {
			number = Long.parseLong(numberString);
		} catch (NumberFormatException e) {
			if (defaultValue != null) {
				return defaultValue;
			}
			throw new InvalidFormatException("String in column " + columnIndex
					+ "(" + numberString
					+ ") is not an integer on line number "
					+ m_currentLineNumber);
		}
		return number;
	}

	public double getDoubleNextValue() throws NoSuchElementException,
			InvalidFormatException {
		int index = m_currentLineIndex++;
		return getDoubleValue(index);
	}

	public double getDoubleValue(String columnName)
			throws NoSuchElementException, InvalidFormatException {
		int index = getColumnIndex(columnName);
		return getDoubleValue(index);
	}

	public double getDoubleValue(int columnIndex)
			throws NoSuchElementException, InvalidFormatException {
		String numberString = getValue(columnIndex);
		double number;
		try {
			number = NumberFormat.getInstance().parse(numberString)
					.doubleValue();
		} catch (ParseException e) {
			throw new InvalidFormatException("String in column " + columnIndex
					+ "(" + numberString + ") is not a number on line number "
					+ m_currentLineNumber);
		}
		return number;
	}

	public boolean getBooleanNextValue() throws NoSuchElementException,
			InvalidFormatException {
		int index = m_currentLineIndex++;
		return getBooleanValue(index);
	}

	public boolean getBooleanValue(String columnName)
			throws NoSuchElementException, InvalidFormatException {
		int index = getColumnIndex(columnName);
		return getBooleanValue(index);
	}

	public boolean getBooleanValue(int columnIndex)
			throws NoSuchElementException, InvalidFormatException {
		String booleanString = getValue(columnIndex);
		boolean value;
		if (booleanString.toUpperCase().equals("Y")
				|| booleanString.toUpperCase().equals("YES")
				|| booleanString.toUpperCase().equals("T")
				|| booleanString.toUpperCase().equals("TRUE")) {
			value = true;
		} else if (booleanString.toUpperCase().equals("N")
				|| booleanString.toUpperCase().equals("NO")
				|| booleanString.toUpperCase().equals("F")
				|| booleanString.toUpperCase().equals("FALSE")) {
			value = false;
		} else {
			throw new InvalidFormatException("String in column " + columnIndex
					+ "(" + booleanString
					+ ") is not a boolean value on line number "
					+ m_currentLineNumber);
		}
		return value;
	}

	public int getCurrentLineNumber() {
		return m_currentLineNumber;
	}

	protected abstract void processLine() throws InvalidFormatException;

	protected void postProcess() throws InvalidFormatException {
		// Default behavior is to do nothing
	}
}

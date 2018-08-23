package edu.mit.nsfnats.paxdelay.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.NoSuchElementException;

import edu.mit.nsfnats.paxdelay.InvalidFormatException;

public class FileProcessor {
	public static final String COMMENT_PREFIX_STRING = "#";

	LineProcessor m_lineProcessor;

	public FileProcessor(LineProcessor lineProcessor) {
		m_lineProcessor = lineProcessor;
	}

	public void processFile(String directory, String filename)
			throws FileNotFoundException, InvalidFormatException {
		processFile(directory + File.separator + filename);
	}

	public void processFile(String filename) throws FileNotFoundException,
			InvalidFormatException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				filename)));

		int lineNumber = m_lineProcessor.processHeader(reader, filename);

		try {
			String line = reader.readLine();
			++lineNumber;
			while (line != null) {
				// Skip any blank lines (i.e. all whitespace) and allow comments
				// using #
				if (line.trim().length() == 0
						|| line.trim().startsWith(COMMENT_PREFIX_STRING)) {
					line = reader.readLine();
					++lineNumber;
					continue;
				}
				try {
					m_lineProcessor.processLine(line, lineNumber);
				} catch (NoSuchElementException e) {
					throw new InvalidFormatException(
							"Insufficient information in file " + filename
									+ " row " + line + " on line " + lineNumber,
							e);
				}
				line = reader.readLine();
				++lineNumber;
			}
			m_lineProcessor.postProcess();
		} catch (IOException e) {
			throw new InvalidFormatException("Error reading file " + filename,
					e);
		}
	}
}

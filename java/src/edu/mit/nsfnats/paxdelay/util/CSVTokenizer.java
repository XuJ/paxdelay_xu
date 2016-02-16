package edu.mit.nsfnats.paxdelay.util;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;

public class CSVTokenizer {
	StringTokenizer m_stringTokenizer;
	String m_delimiterString;
	boolean m_lastTokenReturned;
	boolean m_includeEmpties;

	public CSVTokenizer(String row, String delimiterString,
			boolean includeEmpties) {
		m_stringTokenizer = new StringTokenizer(row, delimiterString,
				includeEmpties);
		m_delimiterString = delimiterString;
		m_lastTokenReturned = false;
		m_includeEmpties = includeEmpties;
	}

	public boolean hasMoreTokens() {
		return m_stringTokenizer.hasMoreTokens()
				|| (m_includeEmpties && !m_lastTokenReturned);
	}

	public String nextToken() {
		if (m_stringTokenizer.hasMoreTokens()) {
			String token = m_stringTokenizer.nextToken();
			if (m_includeEmpties) {
				if (m_delimiterString.indexOf(token) >= 0) {
					return "";
				}
				if (m_stringTokenizer.hasMoreTokens()) {
					// We need to burn a delimiter in this case
					m_stringTokenizer.nextToken();
				} else {
					// Otherwise, we already have the last token
					m_lastTokenReturned = true;
				}
			}
			return token;
		} else if (m_includeEmpties && !m_lastTokenReturned) {
			// In the case where the line ends with a delimiter
			m_lastTokenReturned = true;
			return "";
		}
		throw new NoSuchElementException();
	}
}

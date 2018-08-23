package edu.mit.nsfnats.paxdelay.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

public class PropertiesReader {
	public static final String PROPERTY_KEY_SEPARATOR = ".";

	public static Properties loadProperties(String propertiesFilename)
			throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(propertiesFilename));
		return properties;
	}

	protected static String[] getSortedKeys(Properties properties,
			String nameBase) {
		List<String> keyList = new ArrayList<String>();
		Iterator<String> nameEnum = properties.stringPropertyNames().iterator();
		while (nameEnum.hasNext()) {
			String name = nameEnum.next();
			if (name.startsWith(nameBase)) {
				keyList.add(name);
			}
		}
		String[] keys = new String[keyList.size()];
		keyList.toArray(keys);
		Arrays.sort(keys);
		return keys;
	}
	
	public static String[] getSortedSuffixes(Properties properties, String nameBase) {
		String[] sortedKeys = getSortedKeys(properties, nameBase);
		String[] sortedSuffixes = new String[sortedKeys.length];
		for (int i = 0; i < sortedSuffixes.length; ++i) {
			sortedSuffixes[i] = getKeySuffix(sortedKeys[i], nameBase);
		}
		return sortedSuffixes;
	}
	
	protected static String getKeySuffix(String key, String nameBase) {
		if (key.startsWith(nameBase + PROPERTY_KEY_SEPARATOR)) {
			int startIndex = nameBase.length() + 1;
			return key.substring(startIndex);
		}
		return null;
	}

	// Returns an ordered array of values where the key starts with nameBase
	public static String[] readStrings(Properties properties, String nameBase) {
		String[] keys = getSortedKeys(properties, nameBase);
		String[] values = new String[keys.length];
		for (int i = 0; i < values.length; ++i) {
			values[i] = properties.getProperty(keys[i]);
		}
		return values;
	}

	public static boolean readBoolean(Properties properties, String name) {
		return Boolean.parseBoolean(properties.getProperty(name).trim());
	}

	public static boolean readBoolean(Properties properties, String name,
			boolean defaultValue) {
		String value = properties.getProperty(name);
		if (value != null) {
			return Boolean.parseBoolean(value.trim());
		}
		return defaultValue;
	}

	public static double readDouble(Properties properties, String name) {
		return Double.parseDouble(properties.getProperty(name).trim());
	}

	public static double readDouble(Properties properties, String name,
			double defaultValue) {
		String value = properties.getProperty(name);
		if (value != null) {
			return Double.parseDouble(value.trim());
		}
		return defaultValue;
	}

	public static int readInt(Properties properties, String name) {
		return Integer.parseInt(properties.getProperty(name).trim());
	}

	public static int readInt(Properties properties, String name,
			int defaultValue) {
		String value = properties.getProperty(name);
		if (value != null) {
			return Integer.parseInt(value.trim());
		}
		return defaultValue;
	}

	public static int[] readInts(Properties properties, String nameBase) {
		String[] keys = getSortedKeys(properties, nameBase);
		int[] integers = new int[keys.length];
		for (int i = 0; i < integers.length; ++i) {
			integers[i] = readInt(properties, keys[i]);
		}
		return integers;
	}

	public static Date readDate(Properties properties, String name)
			throws ParseException {
		return readDate(properties, name, null);
	}

	public static Date readDate(Properties properties, String name,
			Date defaultValue) throws ParseException {
		String value = properties.getProperty(name);
		if (value != null) {
			DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
			return format.parse(value);
		}
		return defaultValue;
	}

}
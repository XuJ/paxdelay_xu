package edu.mit.nsfnats.paxdelay.util;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class TimeFormat {
	public static final String UNIVERSAL_TIME_ZONE = "UTC";
	public DateFormat m_timeFormat;
	
	public TimeFormat() {
		this(getUniversalTimeZone());
	}
	
	public TimeFormat(TimeZone timeZone) {
		m_timeFormat = new SimpleDateFormat("HH:mm");
		m_timeFormat.setTimeZone(timeZone);
	}

	public Date parse(String timeString) throws ParseException {
		return m_timeFormat.parse(timeString);
	}

	public String format(Date time) {
		// Add a millisecond to account for end intervals
		Calendar c = new GregorianCalendar(getUniversalTimeZone());
		c.setTime(time);
		c.add(Calendar.MILLISECOND, 1);

		return m_timeFormat.format(c.getTime());
	}

	public Date parseRelativeTime(Date startTime, String timeString,
			boolean isEndInterval) throws ParseException {
		Date relativeTime = parse(timeString);
		
		Calendar startCalendar = new GregorianCalendar(getUniversalTimeZone());
		startCalendar.setTime(startTime);

		Calendar endCalendar = new GregorianCalendar(getUniversalTimeZone());
		endCalendar.setTime(relativeTime);
		endCalendar.set(Calendar.YEAR, startCalendar.get(Calendar.YEAR));
		endCalendar.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH));
		endCalendar.set(Calendar.DAY_OF_MONTH, startCalendar.get(Calendar.DAY_OF_MONTH));

		// We need to check for the case where the time is after midnight and
		// add a day so that the time is correct relative to the model's start
		// time
		if (endCalendar.before(startCalendar)) {
			endCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		// If this is an end interval, we need to subtract a second to make sure
		// that we don't have overlapping intervals
		if (isEndInterval) {
			endCalendar.add(Calendar.MILLISECOND, -1);
		}
		relativeTime = endCalendar.getTime();

		return relativeTime;
	}
	
	public static TimeZone getUniversalTimeZone() {
		return TimeZone.getTimeZone(UNIVERSAL_TIME_ZONE);
	}
}

/*
 * This file is part of Transitime.org
 *
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.db.structs.Agency;
import org.transitclock.gtfs.DbConfig;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * UPDATED VERSION that is actually thread safe!
 * Uses LocalDateTime instead of Date
 *
 * Contains convenience methods for dealing with time issues.
 * <p>
 * Note: To use the proper timezone should set
 * <code> TimeZone.setDefault(TimeZone.getTimeZone(timeZoneStr));</code> before
 * this class is initialized. Otherwise the SimpleDateFormat objects will
 * wrongly use the system default timezone.
 *
 * @author lcaraballo
 *
 */
public class TimeV2 {

	private static final Logger logger = LoggerFactory.getLogger(TimeV2.class);

	private TimeV2() {
		throw new UnsupportedOperationException("This class cannot be instantiated");
	}

	private static BooleanConfigValue useMonthDayYearFormat =
			new BooleanConfigValue(
					"transitclock.utils.useMonthDayYearFormat",
					false,
					"Use the month-day-year date format instead of year-month-date.");

	private static final DateTimeFormatter dateFormatterYMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter dateFormatterMDY = DateTimeFormatter.ofPattern("MM-dd-yyyy");
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter timeFormat24 =  DateTimeFormatter.ofPattern("HH:mm:ss z");
	private static final DateTimeFormatter timeFormat24Msec = DateTimeFormatter.ofPattern("HH:mm:ss.SSS z");
	private static final DateTimeFormatter timeFormat24NoTimezone = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final DateTimeFormatter timeFormat24MinuteNoTimezone = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter timeFormat24MsecNoTimeZone = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");


	// Date Format with Msec and TimeZone
	private static final DateTimeFormatter readableDateFormat24MsecMDY =
			DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss.SSS z");
	private static final DateTimeFormatter readableDateFormat24MsecYMD =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z");
	private static DateTimeFormatter getReadableDateFormat24Msec() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24MsecMDY;
		}
		return readableDateFormat24MsecYMD;
	}

	// Date Format with Msec and NO TimeZone
	private static final DateTimeFormatter readableDateFormat24NoTimeZoneMsecMDY =
			DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss.SSS");
	private static final DateTimeFormatter readableDateFormat24NoTimeZoneMsecYMD =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static DateTimeFormatter getReadableDateFormat24NoTimeZoneMsec() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24NoTimeZoneMsecMDY;
		}
		return readableDateFormat24NoTimeZoneMsecYMD;
	}

	// Date Format with NO Msec and TimeZone
	private static final DateTimeFormatter readableDateFormat24MDY =
			DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss z");
	private static final DateTimeFormatter readableDateFormat24YMD =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
	private static DateTimeFormatter getReadableDateFormat24() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24MDY;
		}
		return readableDateFormat24YMD;
	}

	// Date Format with NO Msec and NO TimeZone
	private static final DateTimeFormatter readableDateFormat24NoTimeZoneNoMsecMDY =
			DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
	private static final DateTimeFormatter readableDateFormat24NoTimeZoneNoMsecYMD =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static DateTimeFormatter getReadableDateFormat24NoTimeZoneNoMsec() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24NoTimeZoneNoMsecMDY;
		}
		return readableDateFormat24NoTimeZoneNoMsecYMD;
	}

	// Date Format with NO Sec and NO TimeZone
	private static final DateTimeFormatter readableDateFormat24NoSecsMDY =
			DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
	private static final DateTimeFormatter readableDateFormat24NoSecsYMD =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static DateTimeFormatter getReadableDateFormat24NoSecs() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24NoSecsMDY;
		}
		return readableDateFormat24NoSecsYMD;
	}

	// Date Format with NO Time
	private static final DateTimeFormatter readableDateFormatMDY =
			DateTimeFormatter.ofPattern("MM-dd-yyyy");
	private static final DateTimeFormatter defaultDateFormatYMD =
			DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static DateTimeFormatter getReadableDateFormat() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormatMDY;
		}
		return defaultDateFormatYMD;
	}

	/*********************************
	 * Convert String to LocalDateTime
	 *********************************/
	public static LocalDateTime parse(String datetimeStr) throws DateTimeParseException {
		// First try with timezone and msec, the most complete form
		try {
			return LocalDateTime.parse(datetimeStr, getReadableDateFormat24Msec());
		} catch (DateTimeParseException e) {}

		// Got exception so try without timezone but still try msec
		try {
			return LocalDateTime.parse(datetimeStr, getReadableDateFormat24NoTimeZoneMsec());
		} catch (DateTimeParseException e) {}

		// Still not working so try without seconds but with timezone
		try {
			return LocalDateTime.parse(datetimeStr, getReadableDateFormat24());
		} catch (DateTimeParseException e) {}

		// Still not working so try without msecs and without timezone
		try {
			return LocalDateTime.parse(datetimeStr, getReadableDateFormat24NoTimeZoneNoMsec());
		} catch (DateTimeParseException e) {}

		// Still not working so try without seconds and without timezone
		try {
			return LocalDateTime.parse(datetimeStr, getReadableDateFormat24NoSecs());
		} catch (DateTimeParseException e) {}

		// Still not working so try date alone. This will ignore any time
		// specification so this attempt needs to be done after trying all
		// the other formats.
		try {
			return LocalDateTime.parse(datetimeStr, getReadableDateFormat());
		} catch (DateTimeParseException e) {}

		// As last resort try the default syntax. Will throw a DateTimeParseException if can't parse.
		return LocalDateTime.parse(datetimeStr);
	}

	/*********************************
	 * Convert String to LocalTime
	 *********************************/
	public static LocalTime parseTime(String timeStr) throws DateTimeParseException {
		// try format: HH:mm:ss
		try {
			return LocalTime.parse(timeStr, timeFormat24NoTimezone);
		} catch (DateTimeParseException e) {}

		// try format: HH:mm
		try {
			return LocalTime.parse(timeStr, timeFormat24MinuteNoTimezone);
		} catch (DateTimeParseException e) {}

		// try format: HH:mm:ss.SSS
		try {
			return LocalTime.parse(timeStr, timeFormat24MsecNoTimeZone);
		} catch (DateTimeParseException e) {}

		// try format: HH:mm:ss z
		try {
			return LocalTime.parse(timeStr, timeFormat24);
		} catch (DateTimeParseException e) {}

		// try format: HH:mm:ss.SSS z
		try {
			return LocalTime.parse(timeStr, timeFormat24Msec);
		} catch (DateTimeParseException e) {}

		// As last resort try the default syntax. Will throw a DateTimeParseException if can't parse.
		return LocalTime.parse(timeStr);
	}

	public static LocalTime parseTime(String timeStr, String defaultTime) throws DateTimeParseException {
		if(StringUtils.isEmpty(timeStr)){
			return parseTime(defaultTime);
		}
		return parseTime(timeStr);
	}

	/*********************************
	 * Convert String to LocalDate
	 *********************************/
	public static LocalDate parseDate(String dateStr) throws DateTimeParseException {
		// try format: yyyy-MM-dd AND MM-dd-yyyy
		try {
			return LocalDate.parse(dateStr, getReadableDateFormat());
		} catch (DateTimeParseException e) {}

		// As last resort try the default syntax. Will throw a DateTimeParseException if can't parse.
		return LocalDate.parse(dateStr);
	}

	/*********************************
	 * Convert LocalDateTime to String
	 *********************************/

	private static LocalDateTime convertLongToLocalDateTime(long epochTime){
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.systemDefault());
	}

	/**
	 * Returns epochTime as a string, including msec
	 * eg. MM-dd-yyyy HH:mm:ss.SSS z
	 * eg. yyyy-MM-dd HH:mm:ss.SSS z
	 *
	 * @param localDateTime
	 * @return
	 */
	public static String dateTimeStrMsec(LocalDateTime localDateTime) {
		return getReadableDateFormat24Msec().format(localDateTime);
	}

	/**
	 * Returns epochTime as a string, including sec
	 * eg. MM-dd-yyyy HH:mm:ss
	 * eg. yyyy-MM-dd HH:mm:ss
	 *
	 * @param localDateTime
	 * @return
	 */
	public static String dateTimeStrSec(LocalDateTime localDateTime) {
		return getReadableDateFormat24NoTimeZoneNoMsec().format(localDateTime);
	}

	/**
	 * Returns just the time string in format "HH:mm:ss z"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStr(long epochTime) {
		LocalDateTime localDateTime = convertLongToLocalDateTime(epochTime);
		return timeStr(localDateTime);
	}

	/**
	 * Returns just the time string in format "HH:mm:ss z"
	 *
	 * @param dateTime
	 * @return
	 */
	public static String timeStr(LocalDateTime dateTime) {
		return timeFormat24.format(dateTime);
	}

	/**
	 * Returns just the time string in format "HH:mm:ss"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStrNoTimeZone(long epochTime) {
		LocalDateTime localDateTime = convertLongToLocalDateTime(epochTime);
		return timeFormat24NoTimezone.format(localDateTime);
	}

	/**
	 * Returns just the time string in format "HH:mm:ss"
	 *
	 * @param localDateTime
	 * @return
	 */
	public static String timeStrNoTimeZone(LocalDateTime localDateTime) {
		return timeStrNoTimeZone(localDateTime);
	}

	/**
	 * Returns just the time string. Includes msec.
	 *
	 * @param localDateTime
	 * @return
	 */
	public static String timeStrMsec(LocalDateTime localDateTime) {
		return timeFormat24Msec.format(localDateTime);
	}

	/**
	 * Returns just the time string. Includes msec.
	 * e.g. "HH:mm:ss.SSS z"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsec(long epochTime) {
		LocalDateTime localDateTime = convertLongToLocalDateTime(epochTime);
		return timeFormat24Msec.format(localDateTime);
	}

	/**
	 * Returns just the time string. Includes msec but no timezone.
	 * e.g. "HH:mm:ss.SSS"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsecNoTimeZone(long epochTime) {
		LocalDateTime localDateTime = convertLongToLocalDateTime(epochTime);
		return timeFormat24MsecNoTimeZone.format(localDateTime);
	}


	/***************************************
	 * Convert LocalDateTime to Date String
	 **************************************/

	/**
	 * Returns just the date string in either YMD or MDY format.
	 * e.g. YMD -> "yyyy-MM-dd"
	 * e.g. MDY -> "MM-dd-yyyy"
	 *
	 * @param localDateTime
	 * @return
	 */
	public static String dateStr(LocalDateTime localDateTime) {
		if(useMonthDayYearFormat.getValue()) {
			return dateFormatterMDY.format(localDateTime);
		}
		return dateFormatterYMD.format(localDateTime);
	}

	/***************************************
	 * Convert LocalDate to Date String
	 **************************************/

	/**
	 * Returns the date string in either YMD or MDY format.
	 * e.g. YMD -> "yyyy-MM-dd"
	 * e.g. MDY -> "MM-dd-yyyy"
	 *
	 * @param localDate
	 * @return
	 */
	public static String dateStr(LocalDate localDate) {
		if(useMonthDayYearFormat.getValue()) {
			return dateFormatterMDY.format(localDate);
		}
		return dateFormatterYMD.format(localDate);
	}

	/***************************************
	 * Convert LocalTime to Time String
	 **************************************/

	/**
	 * Returns the time string in 24h format with no timezone.
	 * e.g. "HH:mm:ss"
	 *
	 * @param localTime
	 * @return
	 */
	public static String timeStr(LocalTime localTime) {
		return timeFormat24NoTimezone.format(localTime);
	}
}

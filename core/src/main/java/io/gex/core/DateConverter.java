package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateConverter {

    private final static LogWrapper logger = LogWrapper.create(DateConverter.class);

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT = "MM/dd/yyyy";

    public static LocalTime stringToLocalTime(String date, String format) throws GexException {
        return stringToLocalTime(date, DateTimeFormatter.ofPattern(format));
    }

    public static LocalTime stringToLocalTime(String date, DateTimeFormatter format) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return LocalTime.parse(date, format);
        } catch (DateTimeParseException e) {
            throw logger.logAndReturnException(e, LogType.PARSE_ERROR);
        }
    }

    public static LocalDate stringToLocalDate(String date, String format) throws GexException {
        return stringToLocalDate(date, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDate stringToLocalDate(String date, DateTimeFormatter format) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return LocalDate.parse(date, format);
        } catch (DateTimeParseException e) {
            throw logger.logAndReturnException(e, LogType.PARSE_ERROR);
        }
    }

    public static LocalDateTime stringToLocalDateTime(String date, String format) throws GexException {
        return stringToLocalDateTime(date, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime stringToLocalDateTime(String date, String format, ZoneId zoneId) throws GexException {
        return stringToLocalDateTime(date, DateTimeFormatter.ofPattern(format).withZone(zoneId));
    }

    public static LocalDateTime stringToLocalDateTime(String date, DateTimeFormatter format) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return ZonedDateTime.parse(date, format).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            throw logger.logAndReturnException(e, LogType.PARSE_ERROR);
        }
    }

    public static String localDateTimeToString(LocalDateTime dateTime, String format) {
        return localDateTimeToString(dateTime, DateTimeFormatter.ofPattern(format));
    }

    public static String localDateTimeToString(LocalDateTime dateTime, String format, ZoneId timeZone) {
        return localDateTimeToString(dateTime, DateTimeFormatter.ofPattern(format).withZone(timeZone));
    }

    public static String localDateTimeToString(LocalDateTime dateTime, DateTimeFormatter dateTimeFormatter) {
        logger.trace("Entered " + LogHelper.getMethodName());
        return dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).format(dateTimeFormatter) : null;
    }

    public static LocalDateTime utcSecondsToLocalDateTime(long seconds) {
        logger.trace("Entered " + LogHelper.getMethodName());
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
    }

    public static long localDateTimeToUtcSeconds(LocalDateTime dateTime) {
        logger.trace("Entered " + LogHelper.getMethodName());
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static LocalTime getStartOfDay() {
        return LocalTime.of(0, 0);
    }

    public static LocalTime getEndOfDay() {
        return LocalTime.of(23, 59, 59, 999999999);
    }
}

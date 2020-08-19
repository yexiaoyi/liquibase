package liquibase.util;

import liquibase.Scope;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class ISODateFormat {

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public String format(java.sql.Date date) {
        return dateFormat.format(date);
    }

    public String format(java.sql.Time date) {
        return timeFormat.format(date);
    }

    public String format(java.sql.Timestamp date) {
        StringBuilder sb = new StringBuilder(dateTimeFormat.format(date));
        int nanos = date.getNanos();
        if (nanos != 0) {
            String nanosString = String.format("%09d", nanos);
            int lastNotNullIndex = 8;
            for (; lastNotNullIndex > 0; lastNotNullIndex--) {
                if (nanosString.charAt(lastNotNullIndex) != '0') {
                    break;
                }
            }
            sb.append('.');
            sb.append(nanosString, 0, lastNotNullIndex + 1);
        }
        return sb.toString();
    }

    public String format(Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date) {
            return format(((java.sql.Date) date));
        } else if (date instanceof Time) {
            return format(((java.sql.Time) date));
        } else if (date instanceof java.sql.Timestamp) {
            return format(((java.sql.Timestamp) date));
        } else if (date instanceof java.util.Date) {
            return format(new java.sql.Timestamp(date.getTime()));
        } else {
            throw new RuntimeException("Unknown type: " + date.getClass().getName());
        }
    }

    public Temporal parseDateTime(String dateAsString) throws ParseException {
        if (dateAsString == null) {
            return null;
        }

        if (!dateAsString.contains("-")) {
            //just a time, no date
            return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(dateAsString));
        }

        if (!dateAsString.contains(":")) {
            //just a date, no time
            return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(dateAsString));
        }
        dateAsString = dateAsString.replaceFirst("(\\d) (\\d)", "$1T$2"); //replace spaces between date/time with T

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        TemporalAccessor parsed = formatter.parse(dateAsString);

        try {
            return ZonedDateTime.from(parsed);
        } catch (DateTimeException e) {
            //use local timezone
            return LocalDateTime.from(parsed);
        }
    }

    /**
     * @deprecated Should use {@link #parseDateTime(String)}.
     * Due to limitations in {@link java.util.Date, this loses timezone information}
     */
    public Date parse(String dateAsString) throws ParseException {
        if (dateAsString == null) {
            return null;
        }

        if (!dateAsString.contains("-")) {
            //just a time, no date
            return new java.sql.Time(timeFormat.parse(dateAsString).getTime());
        }

        if (!dateAsString.contains(":")) {
            //just a date, no time
            return new java.sql.Date(dateFormat.parse(dateAsString).getTime());
        }

        TemporalAccessor parsed = parseDateTime(dateAsString);

        try {
            final ZoneId ignored = ZoneId.from(parsed);
        } catch (DateTimeException e) {
            //add local timezone
            //have to use ZoneId.systemDefault() instead of Scope.get(zoneId) because the Timestamp object assumes systemDefault timezone
            parsed = ZonedDateTime.of(LocalDateTime.from(parsed), ZoneId.systemDefault());
        }

        final Timestamp timestamp = new Timestamp(Instant.from(parsed).toEpochMilli());
        timestamp.setNanos(parsed.get(ChronoField.NANO_OF_SECOND));
        return timestamp;

    }
}

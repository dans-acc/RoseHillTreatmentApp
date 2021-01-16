package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.support.annotation.NonNull;
import android.util.Log;
import com.takisoft.fix.support.v7.preference.DatePickerPreference;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtility
{

    private static final String BASIC_DATE_PATTERN = "dd/MM/yyyy";
    private static final String FANCY_DATE_PATTERN = "dd MMMM yyyy";
    private static final String DATABASE_DATE_PATTERN = "yyyy-MM-dd";
    private static final String YEAR_PATTERN = "yyyy";

    public static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.ofPattern(BASIC_DATE_PATTERN);
    public static final DateTimeFormatter FANCY_DATE_FORMATTER = DateTimeFormatter.ofPattern(FANCY_DATE_PATTERN);
    public static final DateTimeFormatter DATABASE_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATABASE_DATE_PATTERN);
    public static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern(YEAR_PATTERN);

    @NonNull
    public static LocalDate fromString(@NonNull final String string, @NonNull final DateTimeFormatter formatter)
            throws DateTimeParseException
    {
        return LocalDate.parse(string, formatter);
    }

    public static LocalDate fromUtilCalendar(final Calendar calendar)
    {
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toLocalDate();
    }

    @NonNull
    public static LocalDate fromEpochMilli(final long epochMilli)
    {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Remember that since we are converting from util date, we do not need to subtract 1.
    public static LocalDate fromUtilDate(final int year, final int month, final int dateOfMonth)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dateOfMonth, 0, 0);
        return fromUtilCalendar(calendar);
    }

    public static long toEpochMilli(@NonNull final LocalDate date)
    {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Date toUtilDate(@NonNull final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate fromUtilDate(@NonNull final Date date)
    {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(date.getTime()),
                ZoneId.systemDefault()
        ).toLocalDate();
    }

    // +1 month accounts for the month offset!
    public static LocalDate fromDateWrapper(final DatePickerPreference.DateWrapper dateWrapper)
    {
        return LocalDate.of(dateWrapper.year, dateWrapper.month + 1, dateWrapper.day);
    }

    public static boolean isWithin(final long epochMilli, final long fromEpochMilli, final long toEpochMilli)
    {
        return epochMilli >= fromEpochMilli && epochMilli <= toEpochMilli;
    }

    public static boolean isWithin(@NonNull final LocalDate date, @NonNull final LocalDate fromDate,
                                   @NonNull final LocalDate toDate)
    {
        return date.compareTo(fromDate) >= 0 && date.compareTo(toDate) <= 0;
    }

    public static Calendar toUtilCalendar(final LocalDate date)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtility.toUtilDate(date));
        return calendar;
    }

    @NonNull
    public static LocalDate getFirstWeekDate(@NonNull final LocalDate date)
    {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    @NonNull
    public static LocalDate getLastWeekDate(@NonNull final LocalDate date)
    {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    @NonNull
    public static LocalDate getFirstMonthDate(@NonNull final LocalDate date)
    {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    @NonNull
    public static LocalDate getLastMonthDate(@NonNull final LocalDate date)
    {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    @NonNull
    public static LocalDate roundMonth(@NonNull final LocalDate date)
    {
        final boolean round = ChronoUnit.DAYS.between(
                date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                date.with(TemporalAdjusters.firstDayOfNextMonth()))
                <= 3;
        return round ? date.with(TemporalAdjusters.firstDayOfNextMonth()) : date.with(TemporalAdjusters.firstDayOfMonth());
    }
}

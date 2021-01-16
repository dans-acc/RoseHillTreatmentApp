package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.support.annotation.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import android.util.Log;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;

public class TreatUtility
{

    public static int getTreatListInsertIndex(final List<Treat> treats, final Treat treat)
    {
        return TreatUtility.getTreatListInsertIndex(treats, treat.getNumber());
    }

    public static int getTreatListInsertIndex(final List<Treat> treats, final int number)
    {
        if (number == 0 || treats.isEmpty()) {
            return 0;
        } else if (treats.get(0).getNumber() < number) {
            return 0;
        } else if (treats.get(treats.size() - 1).getNumber() > number) {
            return treats.size();
        } else {
            int index = 0;
            while (treats.get(index).getNumber() > number) {
                ++index;
            }
            return index;
        }
    }

    public static UUID[] getTreatUUIDs(final Treat... treats)
    {
        return TreatUtility.getTreatUUIDs(Arrays.asList(treats));
    }

    public static UUID[] getTreatUUIDs(final List<Treat> treats)
    {
        if (treats == null) {
            return null;
        }

        final UUID[] uuids = new UUID[treats.size()];
        for (int i = 0; i < uuids.length; ++i) {
            uuids[i] = treats.get(i).getUUID();
            Log.wtf("Collected UUID", uuids[i].toString());
        }

        return uuids;
    }

    public static Treat getTreatByUUID(final List<Treat> treats,
                                       final UUID treatUUID)
    {
        for (final Treat treat : treats) {
            if (treat.getUUID().equals(treatUUID)) {
                return treat;
            }
        }
        return null;
    }

    public static List<Treat> getTreatsByUUID(final List<Treat> treatSrc,
                                              final UUID[] treatUUIDs)
    {
        return TreatUtility.getTreatsByUUID(treatSrc, Arrays.asList(treatUUIDs));
    }

    @SuppressWarnings("WeakerAccess")
    public static List<Treat> getTreatsByUUID(final List<Treat> treatSrc,
                                              final List<UUID> treatUUIDs)
    {
        final List<Treat> treats = new ArrayList<>(treatUUIDs.size());
        for (final Treat treat : treatSrc) {
            if (treatUUIDs.contains(treat.getUUID())) {
                treats.add(treat);
            }
        }
        return treats;
    }

    public static Week getWeekByDate(final List<Week> weeks, final LocalDate weekDate)
    {
        for (final Week week : weeks) {
            if (week.getDate().equals(weekDate)) {
                return week;
            }
        }
        return null;
    }

    public static long weekNumber(@NonNull final LocalDate fromWeek, @NonNull final LocalDate toWeek)
    {
        return weeksBetween(fromWeek, toWeek) + 1;
    }

    public static long weeksBetween(@NonNull final LocalDate fromWeek, @NonNull final LocalDate toWeek)
    {
        return ChronoUnit.WEEKS.between(
                fromWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                toWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        );
    }

    public static double getTotalWeekVolume(final Treat... treats)
    {
        return TreatUtility.getTotalWeekVolume(Arrays.asList(treats));
    }

    public static double getTotalWeekVolume(final List<Treat> treats)
    {
        double totalWeekVolume = 0;
        for (final Treat treat : treats) {
            totalWeekVolume += treat.getTotalTreatVolume();
        }
        return totalWeekVolume;
    }

    public static void sortTreatsIntoDescendingOrder(final List<Treat> treats)
    {
        if (treats == null) {
            return;
        }
        Collections.sort(treats, (x, y) -> Integer.compareUnsigned(y.getNumber(), x.getNumber()));
    }

    public static void sortWeeksIntoDescendingOrder(final List<Week> weeks)
    {
        if (weeks == null) {
            return;
        }
        Collections.sort(weeks, (x, y) -> y.getDate().compareTo(x.getDate()));
    }

    @NonNull
    public static List<Week> intoWeeks(final Treat... treats)
    {
        return TreatUtility.intoWeeks(Arrays.asList(treats));
    }

    public static List<Week> intoWeeks(final List<Treat> treats)
    {
        final List<Week> weeks = new ArrayList<>();
        if (treats.isEmpty()) {
            return weeks;
        } else if (treats.size() == 1) {
            final Week week = new Week(
                    treats.get(0).getWeekDate(),
                    treats
            );
            weeks.add(week);
            return weeks;
        }

        final Map<LocalDate, List<Treat>> weeklyTreatments = new HashMap<>();
        LocalDate week;
        List<Treat> treatments;
        for (final Treat treat : treats) {
            week = DateUtility.getFirstWeekDate(treat.getWeekDate());
            if (weeklyTreatments.containsKey(week)) {
                treatments = weeklyTreatments.get(week);
                if (treatments == null) {
                    throw new NullPointerException(String.format(
                            "Failed to obtain treatment list for week: %s.",
                            week.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ));
                }
                treatments.add(treat);
            } else {
                treatments = new ArrayList<>();
                treatments.add(treat);
                weeklyTreatments.put(week, treatments);
            }
        }

        for (final Map.Entry<LocalDate, List<Treat>> weekData : weeklyTreatments.entrySet()) {
            weeks.add(new Week(weekData.getKey(), weekData.getValue()));
        }
        return weeks;
    }

    @NonNull
    public static Map<LocalDate, Queue<Week>> intoMonthlyQueues(final Week[] weeks)
    {
        return TreatUtility.intoMonthlyQueues(Arrays.asList(weeks));
    }

    public static Map<LocalDate, Queue<Week>> intoMonthlyQueues(final List<Week> weeks)
    {
        final Map<LocalDate, Queue<Week>> monthlyQueues = new HashMap<>();
        LocalDate month = null;
        Queue<Week> queue = null;
        for (final Week week : weeks) {
            month = DateUtility.roundMonth(week.getDate());
            if (monthlyQueues.containsKey(month)) {
                queue = monthlyQueues.get(month);
                if (queue == null) {
                    throw new NullPointerException(String.format(
                            "Failed to obtain queue for month: %s whist adding week: %s.",
                            month.format(DateTimeFormatter.ofPattern("MMMM")),
                            week.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ));
                }
                queue.add(week);
            } else {
                queue = new LinkedList<>();
                queue.add(week);
                monthlyQueues.put(month, queue);
            }

        }
        return monthlyQueues;
    }
}

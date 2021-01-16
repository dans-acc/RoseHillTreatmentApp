package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.util.Log;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.TreatXLSWorkbookBuilder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WorkbookUtility
{

    private static final String XLS_FILE_EXTENSION = ".xls";

    private static final String YYYY_YYYY_WW_FILENAME_FORMAT = "%s_%s_%d";
    private static final String YYYY_YYYY_WW_WW_FILENAME_FORMAT = "%s_%s_%d_%d";

    public static WritableWorkbook buildWorkbook(final File targetDirectory,
                                                 final LocalDate startOfFinancialYear,
                                                 final LocalDate endOfFinancialYear,
                                                 final Week... weeks)
    {
        return WorkbookUtility.buildWorkbook(
                targetDirectory,
                startOfFinancialYear,
                endOfFinancialYear,
                new ArrayList<>(Arrays.asList(weeks))
        );
    }

    public static WritableWorkbook buildWorkbook(final File targetDirectory,
                                                 final LocalDate startOfFinancialYear,
                                                 final LocalDate endOfFinancialYear,
                                                 final List<Week> weeks)
    {
        // Sort the weeks so as to get the correct dates.
        if (weeks.size() > 1) {
            Collections.sort(weeks);
        }

        try {

            // Create a filename for the workbook.
            final File workbookFile = new File(targetDirectory, buildWorkbookFilename(
                    startOfFinancialYear,
                    endOfFinancialYear,
                    weeks
            ));

            Log.wtf("Workbook filename: ", workbookFile.getAbsolutePath());

            // The builder used for creating the workbook.
            final TreatXLSWorkbookBuilder workbookBuilder = new TreatXLSWorkbookBuilder(
                    startOfFinancialYear,
                    endOfFinancialYear,
                    workbookFile,
                    weeks
            ).setSortWeeks(false);

            // Build and return the workbook.
            return workbookBuilder.build();

            // Stop all exceptions from propagating.
        } catch (final IllegalArgumentException
                | IllegalStateException
                | NullPointerException
                | IOException
                | WriteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String buildWorkbookFilename(final LocalDate startOfFinancialYear,
                                               final LocalDate endOfFinancialYear,
                                               final Week... weeks)
    {
        return WorkbookUtility.buildWorkbookFilename(
                startOfFinancialYear,
                endOfFinancialYear,
                Arrays.asList(weeks)
        );
    }

    public static String buildWorkbookFilename(final LocalDate startOfFinancialYear,
                                               final LocalDate endOfFinancialYear,
                                               final List<Week> weeks)
    {
        final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
        final String workbookFilename;
        if (weeks.size() == 1) {
            workbookFilename = String.format(
                    Locale.UK,
                    YYYY_YYYY_WW_FILENAME_FORMAT,
                    startOfFinancialYear.format(yearFormatter),
                    endOfFinancialYear.format(yearFormatter),
                    TreatUtility.weekNumber(startOfFinancialYear, weeks.get(0).getDate())
            );
        } else {
            workbookFilename = String.format(
                    Locale.UK,
                    YYYY_YYYY_WW_WW_FILENAME_FORMAT,
                    startOfFinancialYear.format(yearFormatter),
                    endOfFinancialYear.format(yearFormatter),
                    TreatUtility.weekNumber(startOfFinancialYear, weeks.get(0).getDate()),
                    TreatUtility.weekNumber(startOfFinancialYear, weeks.get(weeks.size() - 1).getDate())
            );
        }
        return workbookFilename + XLS_FILE_EXTENSION;
    }

    public static boolean writeWorkbook(final WritableWorkbook workbook)
    {
        boolean successful = true;
        try {
            workbook.write();
        } catch (final IOException e) {
            e.printStackTrace();
            successful = false;
        } finally {
            try {
                workbook.close();
            } catch (final WriteException | IOException e) {
                e.printStackTrace();
            }
        }
        return successful;
    }

}

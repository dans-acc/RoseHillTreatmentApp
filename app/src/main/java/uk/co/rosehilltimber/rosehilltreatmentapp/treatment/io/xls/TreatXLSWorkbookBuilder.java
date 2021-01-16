package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.TreatFileFilter;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

public class TreatXLSWorkbookBuilder
{

    // Default workbook settings.
    private static final boolean DEFAULT_WORKBOOK_FILE_MUST_EXIST = false;
    private static final boolean DEFAULT_SORT_WEEKS = true;
    private static final boolean DEFAULT_USE_TEMPORARY_FILE_DURING_WRITE = false;

    // The colour changes.
    private static final int[] NEW_OLIVE_GREEN_COLOUR = {156, 186, 96};
    private static final int[] NEW_LIGHT_GREEN_COLOUR = {216, 227, 190};
    private static final int[] NEW_BROWN_COLOUR = {131, 101, 34};

    // The weeks being used to create the workbook.
    private List<Week> mWeeks;
    private boolean mSortWeeks;

    // The file into which the workbook is to be created, and the filter used.
    private File mWorkbookFile;
    private TreatFileFilter mTreatFileFilter;
    private boolean mUseTemporaryFileDuringWrite;

    // The start and end of the financial year.
    private LocalDate mStartOfFinancialYear;
    private LocalDate mEndOfFinancialYear;

    public TreatXLSWorkbookBuilder(@NonNull final LocalDate mStartOfFinancialYear,
                                   @NonNull final LocalDate mEndOfFinancialYear,
                                   @NonNull final File mWorkbookFile,
                                   @NonNull final List<Week> mWeeks)
    {
        // Init the required attributes of the builder.
        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;
        this.mWorkbookFile = mWorkbookFile;
        this.mWeeks = mWeeks;

        // Whether the weeks should be sorted by default.
        mSortWeeks = DEFAULT_SORT_WEEKS;

        // The default file filter.
        mTreatFileFilter = new TreatFileFilter(DEFAULT_WORKBOOK_FILE_MUST_EXIST);
        mUseTemporaryFileDuringWrite = DEFAULT_USE_TEMPORARY_FILE_DURING_WRITE;
    }

    public TreatXLSWorkbookBuilder(@NonNull final LocalDate mStartOfFinancialYear,
                                   @NonNull final LocalDate mEndOfFinancialYear,
                                   @NonNull final File mWorkbookFile,
                                   @NonNull final Week... mWeeks)
    {
        this (
                mStartOfFinancialYear,
                mEndOfFinancialYear,
                mWorkbookFile,
                Arrays.asList(mWeeks)
        );
    }

    @NonNull
    public List<Week> getWeeks()
    {
        return mWeeks;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setWeeks(@NonNull final List<Week> mWeeks)
    {
        this.mWeeks = mWeeks;
        return this;
    }

    public boolean sortsWeeks()
    {
        return mSortWeeks;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setSortWeeks(final boolean mSortWeeks)
    {
        this.mSortWeeks = mSortWeeks;
        return this;
    }

    @NonNull
    public File getWorkbookFile()
    {
        return mWorkbookFile;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setWorkbookFile(@NonNull final File mWorkbookFile)
    {
        this.mWorkbookFile = mWorkbookFile;
        return this;
    }

    @NonNull
    public TreatFileFilter getTreatmentFileFilter()
    {
        return mTreatFileFilter;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setTreatmentFileFilter(@NonNull final TreatFileFilter mTreatFileFilter)
    {
        this.mTreatFileFilter = mTreatFileFilter;
        return this;
    }

    public boolean usesTemporaryFileDuringWrite()
    {
        return mUseTemporaryFileDuringWrite;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setUseTemporaryFileDuringWrite(final boolean mUseTemporaryFileDuringWrite)
    {
        this.mUseTemporaryFileDuringWrite = mUseTemporaryFileDuringWrite;
        return this;
    }

    @NonNull
    public LocalDate getStartOfFinancialYear()
    {
        return mStartOfFinancialYear;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setStartOfFinancialYear(@NonNull final LocalDate mStartOfFinancialYear)
    {
        this.mStartOfFinancialYear = mStartOfFinancialYear;
        return this;
    }

    @NonNull
    public LocalDate getEndOfFinancialYear()
    {
        return mEndOfFinancialYear;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setEndOfFinancialYear(@NonNull final LocalDate mEndOfFinancialYear)
    {
        this.mEndOfFinancialYear = mEndOfFinancialYear;
        return this;
    }

    @NonNull
    public TreatXLSWorkbookBuilder setFinancialYear(@NonNull final LocalDate mStartOfFinancialYear, final LocalDate mEndOfFinancialYear)
    {
        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;
        return this;
    }

    @NonNull
    public WritableWorkbook build()
            throws IllegalArgumentException, NullPointerException, IOException, WriteException
    {
        // Check that the defined builder parameters are valid.
        if (mWeeks == null || mWeeks.size() == 0) {
            throw new IllegalArgumentException("Insufficient week data (0). The workbook could not be constructed.");
        } else if (mStartOfFinancialYear.isAfter(mEndOfFinancialYear)) {
            throw new IllegalArgumentException(String.format(
                    "The end of the financial year proceeds the start (days diff: %d).",
                    ChronoUnit.DAYS.between(mStartOfFinancialYear, mEndOfFinancialYear)
            ));
        } else if (!mTreatFileFilter.accept(mWorkbookFile)) {
            throw new IllegalArgumentException(String.format(
                    "The provided file (at: %s) does not conform to the filter implementation.",
                    mWorkbookFile.getPath()
            ));
        }

        // Build the workbook.
        return buildWorkbook();
    }

    @NonNull
    private WritableWorkbook buildWorkbook()
            throws IOException, WriteException
    {

        // Resolve memory issues by employing a temporary file.
        final WritableWorkbook workbook;
        if (mUseTemporaryFileDuringWrite) {
            final WorkbookSettings settings = new WorkbookSettings();
            settings.setUseTemporaryFileDuringWrite(true);
            workbook = Workbook.createWorkbook(mWorkbookFile, settings);
        } else {
            workbook = Workbook.createWorkbook(mWorkbookFile);
        }

        // Update the olive green colour.
        workbook.setColourRGB(
                Colour.OLIVE_GREEN,
                NEW_OLIVE_GREEN_COLOUR[0],
                NEW_OLIVE_GREEN_COLOUR[1],
                NEW_OLIVE_GREEN_COLOUR[2]
        );

        // Update the light green colour.
        workbook.setColourRGB(
                Colour.LIGHT_GREEN,
                NEW_LIGHT_GREEN_COLOUR[0],
                NEW_LIGHT_GREEN_COLOUR[1],
                NEW_LIGHT_GREEN_COLOUR[2]
        );

        // Update the brown workbook colour.
        workbook.setColourRGB(
                Colour.BROWN,
                NEW_BROWN_COLOUR[0],
                NEW_BROWN_COLOUR[1],
                NEW_BROWN_COLOUR[2]
        );

        // Depending on the contents, create the workbook appropriately.
        if (mWeeks.size() == 1) {
            buildMonthlySheets(
                    workbook,
                    DateUtility.roundMonth(mWeeks.get(0).getDate()),
                    new LinkedList<>(mWeeks)
            );
        } else {
            if (mSortWeeks) {
                Collections.sort(mWeeks);
            }
            buildMonthlySheets(workbook, TreatUtility.intoMonthlyQueues(mWeeks));
        }

        return workbook;
    }

    private void buildMonthlySheets(final WritableWorkbook workbook, final Map<LocalDate, Queue<Week>> monthlyQueues)
            throws IllegalArgumentException, IOException, WriteException
    {
        // Build a sheet for each of the months present within the queue.
        LocalDate month = null;
        Queue<Week> monthQueue = null;
        for (final Map.Entry<LocalDate, Queue<Week>> sheetData : monthlyQueues.entrySet()) {

            // Get the month and the week queue being built.
            month = sheetData.getKey();
            monthQueue = sheetData.getValue();

            // Build the month and week queue sheets.
            buildMonthlySheets(
                    new DetailedTreatXLSSheetBuilder(workbook, mStartOfFinancialYear, mEndOfFinancialYear, month, monthQueue),
                    new SummarisedTreatXLSSheetBuilder(workbook, mStartOfFinancialYear, mEndOfFinancialYear, month, monthQueue)
            );
        }
    }

    private void buildMonthlySheets(final WritableWorkbook workbook, final LocalDate month,
                                    final Queue<Week> monthQueue)
            throws IllegalArgumentException, IOException, WriteException
    {
        // Build a single sheet for the provided month.
        buildMonthlySheets(
                new DetailedTreatXLSSheetBuilder(workbook, mStartOfFinancialYear, mEndOfFinancialYear, month, monthQueue),
                new SummarisedTreatXLSSheetBuilder(workbook, mStartOfFinancialYear, mEndOfFinancialYear, month, monthQueue)
        );
    }

    private void buildMonthlySheets(final DetailedTreatXLSSheetBuilder detailedBuilder,
                                    final SummarisedTreatXLSSheetBuilder summarisedBuilder)
            throws IllegalArgumentException, IOException, WriteException
    {
        // Delegate the task of building to the sheet builders.
        detailedBuilder.build();
        summarisedBuilder.build();
    }
}

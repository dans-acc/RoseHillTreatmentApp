package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls;

import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.util.Queue;

import android.util.Log;
import jxl.CellView;

import jxl.write.*;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.formats.CommonTreatXLSFormats;

public abstract class TreatXLSSheetBuilder<T extends CommonTreatXLSFormats>
{

    // Default sheet settings.
    private static final boolean DEFAULT_AUTOSIZE_COLUMN_VIEWS = true;

    // The workbook for which the sheet is being created.
    private WritableWorkbook mWorkbook;
    private T mTreatXLSFormats;

    // Sheet date related information.
    private LocalDate mStartOfFinancialYear;
    private LocalDate mEndOfFinancialYear;

    // The month and the queue of weeks being written.
    private LocalDate mMonth;
    private Queue<Week> mMonthlyQueue;

    // Whether or not the sheet builder auto-sizes the columns.
    private boolean mAutosizeColumnViews;

    @SuppressWarnings("WeakerAccess")
    protected TreatXLSSheetBuilder(@NonNull final WritableWorkbook mWorkbook,
                                   @NonNull final T mTreatXLSFormats,
                                   @NonNull final LocalDate mStartOfFinancialYear,
                                   @NonNull final LocalDate mEndOfFinancialYear,
                                   @NonNull final LocalDate mMonth,
                                   @NonNull final Queue<Week> mMonthQueue)
            throws WriteException
    {
        // Init all required parameters.
        this.mWorkbook = mWorkbook;
        this.mTreatXLSFormats = mTreatXLSFormats;
        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;
        this.mMonth = mMonth;
        this.mMonthlyQueue = mMonthQueue;

        // Set to the default.
        mAutosizeColumnViews = DEFAULT_AUTOSIZE_COLUMN_VIEWS;
    }

    @NonNull
    public WritableWorkbook getWorkbook()
    {
        return mWorkbook;
    }

    @NonNull
    public TreatXLSSheetBuilder setWorkbook(@NonNull final WritableWorkbook mWorkbook)
    {
        this.mWorkbook = mWorkbook;
        return this;
    }

    @NonNull
    public T getTreatXLSFormats()
    {
        return mTreatXLSFormats;
    }

    @NonNull
    public TreatXLSSheetBuilder<T> setTreatXLSFormats(@NonNull final T mTreatXLSFormats)
    {
        this.mTreatXLSFormats = mTreatXLSFormats;
        return this;
    }

    @NonNull
    public LocalDate getStartOfFinancialYear()
    {
        return mStartOfFinancialYear;
    }

    @NonNull
    public TreatXLSSheetBuilder setStartOfFinancialYear(@NonNull final LocalDate mStartOfFinancialYear)
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
    public TreatXLSSheetBuilder setEndOfFinancialYear(@NonNull final LocalDate mEndOfFinancialYear)
    {
        this.mEndOfFinancialYear = mEndOfFinancialYear;
        return this;
    }

    @NonNull
    public TreatXLSSheetBuilder setFinancialYear(@NonNull final LocalDate mStartOfFinancialYear,
                                                 @NonNull final LocalDate mEndOfFinancialYear)
    {
        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;
        return this;
    }

    @NonNull
    public LocalDate getMonth()
    {
        return mMonth;
    }

    @NonNull
    public TreatXLSSheetBuilder setMonth(@NonNull final LocalDate mMonth)
    {
        this.mMonth = mMonth;
        return this;
    }

    public boolean autosizeColumnViews()
    {
        return mAutosizeColumnViews;
    }

    @NonNull
    public TreatXLSSheetBuilder setAutosizeColumnViews(final boolean mAutosizeColumnViews)
    {
        this.mAutosizeColumnViews = mAutosizeColumnViews;
        return this;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    @NonNull
    public WritableSheet build()
            throws IllegalArgumentException, NullPointerException, WriteException
    {
        // Check that all necessary parameters are present.
        if (mWorkbook == null) {
            throw new NullPointerException("Unable to create sheet. Workbook is null.");
        } else if (mStartOfFinancialYear == null || mEndOfFinancialYear == null) {
            throw new NullPointerException("The financial year appears to be null.");
        } else if (mMonth == null || mMonthlyQueue == null) {
            throw new NullPointerException("Month and monthly queues cannot be null.");
        }

        // Build the workbook sheet.
        return buildSheet();
    }

    @NonNull
    private WritableSheet buildSheet()
            throws WriteException
    {
        // Create the xls workbook sheet.
        final WritableSheet sheet = mWorkbook.createSheet(
                createSheetName(mMonth),
                mWorkbook.getNumberOfSheets()
        );

        // Create header rows for the sheet.
        createHeaderRows(sheet);
        if (!mMonthlyQueue.isEmpty()) {
            for (final Week week : mMonthlyQueue) {
                createDataRows(sheet, week);
            }
        }

        // If the autosize column views option is set, resize all of the columns.
        if (mAutosizeColumnViews) {
            CellView columnCellView;
            int column = 0, sheetColumns = sheet.getColumns();
            for (; column < sheetColumns; ++column) {
                columnCellView = sheet.getColumnView(column);
                columnCellView.setAutosize(mAutosizeColumnViews);
                sheet.setColumnView(column, columnCellView);
            }
        }

        return sheet;
    }

    @NonNull
    protected abstract String createSheetName(final LocalDate month);

    protected abstract void createHeaderRows(final WritableSheet sheet)
            throws WriteException;

    protected abstract void createDataRows(final WritableSheet sheet, final Week week)
            throws WriteException;

    protected void setCells(final WritableSheet sheet, final WritableCell... writableCells)
            throws WriteException
    {
        for (final WritableCell cell : writableCells) {
            sheet.addCell(cell);
        }
    }

    protected void setBlanks(final WritableSheet sheet, final WritableCellFormat blankCellFormat,
                             final int fromCol, int fromRow, final int toCol, final int toRow)
            throws WriteException
    {
        for (int column; fromRow <= toRow; ++fromRow) {
            for (column = fromCol; column <= toCol; ++column) {
                sheet.addCell(new Blank(column, fromRow, blankCellFormat));
            }
        }
    }

    protected void setBlanks(final WritableSheet sheet, final WritableCellFormat blankCellFormat,
                             int fromCol, int toCol, int row)
            throws WriteException
    {
        for (; fromCol <= toCol; ++ fromCol) {
            sheet.addCell(new Blank(fromCol, row, blankCellFormat));
        }
    }

    protected void setCellsWithBlanks(final WritableSheet sheet, final WritableCellFormat blankCellFormat,
                                      final WritableCell... writableCells)
            throws WriteException
    {
        // Find the minimum and maximum rows and columns.
        int row = 0, column = 0, minRow = 0, maxRow = 0, minColumn = 0, maxColumn = 0;
        for (final WritableCell cell : writableCells) {
            row = cell.getRow();
            column = cell.getColumn();
            if (row < minRow) {
                minRow = row;
            } else if (row > maxRow) {
                maxRow = row;
            }
            if (column < minColumn) {
                minColumn = column;
            } else if (column > maxColumn) {
                maxColumn = column;
            }
        }

        // Add the cells to the sheet.
        setCellsWithBlanks(
                sheet,
                blankCellFormat,
                minColumn,
                minRow,
                maxColumn,
                maxRow,
                writableCells
        );
    }

    protected void setCellsWithBlanks(final WritableSheet sheet, final WritableCellFormat blankCellFormat,
                                      final int colBlanksFrom, final int colBlanksTo,
                                      final WritableCell... writableCells)
            throws WriteException
    {
        // Get the min and max rows.
        int row = writableCells[0].getRow(), minRow = row, maxRow = row;
        for (final WritableCell cell : writableCells) {
            row = cell.getRow();
            if (row < minRow) {
                minRow = row;
            }else if (row > maxRow) {
                maxRow = row;
            }
        }

        // Add cells to the sheet.
        setCellsWithBlanks(
                sheet,
                blankCellFormat,
                colBlanksFrom,
                minRow,
                colBlanksTo,
                maxRow,
                writableCells
        );
    }

    protected void setCellsWithBlanks(final WritableSheet sheet, final WritableCellFormat blankCellFormat,
                                      int colBlanksFrom, final int rowBlanksFrom,
                                      final int colBlanksTo, final int rowBlanksTo,
                                      final WritableCell... writableCells)
            throws WriteException
    {
        // Add all of the cells first.
        for (final WritableCell cell : writableCells) {
            sheet.addCell(cell);
        }

        // Add blanks to the sheet, ensuring set sells have not been overwritten.
        int row = rowBlanksFrom;
        for (; colBlanksFrom <= colBlanksTo; ++colBlanksFrom) {
            rowLoop:
            for (row = rowBlanksFrom; row <= rowBlanksTo; ++row) {
                for (final WritableCell cell : writableCells) {
                    if (cell.getColumn() == colBlanksFrom && cell.getRow() == row) {
                        continue rowLoop;
                    }
                }
                if (blankCellFormat == null) {
                    sheet.addCell(new Blank(colBlanksFrom, row));
                } else {
                    sheet.addCell(new Blank(colBlanksFrom, row, blankCellFormat));
                }
            }
        }
    }
}

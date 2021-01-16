package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls;

import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import android.util.Log;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.formats.DetailedTreatXLSFormats;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.CuboidTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.RoundTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;

public class DetailedTreatXLSSheetBuilder extends TreatXLSSheetBuilder<DetailedTreatXLSFormats>
{

    // The number format used for the length of the timber.
    private static final NumberFormat CELL_SHORT_DOUBLE_VALUE_FORMAT = new NumberFormat("0.0#");

    @SuppressWarnings("WeakerAccess")
    protected DetailedTreatXLSSheetBuilder(@NonNull final WritableWorkbook mWorkbook,
                                           @NonNull final LocalDate mStartOfFinancialYear,
                                           @NonNull final LocalDate mEndOfFinancialYear,
                                           @NonNull final LocalDate mMonth,
                                           @NonNull final Queue<Week> mMonthQueue)
            throws WriteException
    {
        super(
                mWorkbook,
                new DetailedTreatXLSFormats(),
                mStartOfFinancialYear,
                mEndOfFinancialYear,
                mMonth,
                mMonthQueue
        );
    }

    @NonNull
    @Override
    protected String createSheetName(@NonNull final LocalDate month)
    {
        return month.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    @Override
    protected void createHeaderRows(final WritableSheet sheet)
            throws WriteException
    {
        // The merged row columns.
        final int[][] mergedRowColumns = {
                {0, 1},
                {2, 6},
                {7, 10},
                {11, 15}
        };

        // The merged column names.
        final String[] mergedColumnNames = {
                "",
                "Green",
                "Round Green",
                "Brown"
        };

        // The styles used to style the merged cells.
        final DetailedTreatXLSFormats formats = super.getTreatXLSFormats();
        final WritableCellFormat headerBaseCellFormat = formats.getHeaderBaseCellFormat(),
                headerGreenCellFormat = formats.getHeaderGreenCellFormat(),
                headerRoundGreenCellFormat = formats.getHeaderRoundGreenCellFormat(),
                headerBrownCellFormat = formats.getHeaderBrownCellFormat();

        // Formats used for the headers.
        final WritableCellFormat[] mergedColumnFormats = {
                headerBaseCellFormat,
                headerGreenCellFormat,
                headerRoundGreenCellFormat,
                headerBrownCellFormat
        };

        // Merge columns together, and create cells within.
        for (int i = 0; i < mergedRowColumns.length; ++i) {
            sheet.mergeCells(mergedRowColumns[i][0], 0, mergedRowColumns[i][1], 0);
            sheet.addCell(new Label(mergedRowColumns[i][0], 0, mergedColumnNames[i], mergedColumnFormats[i]));
        }

        // The header cell column names.
        final String[] headerColumnNames = {
                "Treat No", "Date",
                "Quantity", "Length", "Breadth", "Height", "m^3",
                "Quantity", "Length", "Diameter", "m^3",
                "Quantity", "Length", "Breadth", "Height", "m^3"
        };

        // The header column header styles / formats.
        final WritableCellFormat[] headerColumnFormats = {
                headerBaseCellFormat, headerBaseCellFormat,
                headerGreenCellFormat, headerGreenCellFormat, headerGreenCellFormat, headerGreenCellFormat, headerGreenCellFormat,
                headerRoundGreenCellFormat, headerRoundGreenCellFormat, headerRoundGreenCellFormat, headerRoundGreenCellFormat,
                headerBrownCellFormat, headerBrownCellFormat, headerBrownCellFormat, headerBrownCellFormat, headerBrownCellFormat
        };

        // Write the header cells beneath the classification cells.
        for (int i = 0; i < headerColumnNames.length; ++i) {
            sheet.addCell(new Label(i, 1, headerColumnNames[i], headerColumnFormats[i]));
        }
    }

    protected void createDataRows(final WritableSheet sheet, final Week week)
            throws WriteException
    {
        // Current dimensions of the sheet.
        int columns = sheet.getColumns(), rows = sheet.getRows();
        Log.wtf("Rows: ", rows + "");

        // Formats used during the write process.
        final DetailedTreatXLSFormats formats = super.getTreatXLSFormats();
        final WritableCellFormat basicBaseFormat = formats.getBasicBaseCellFormat(),
                basicIntFormat = formats.getBasicIntCellFormat(),
                basicDoubleFormat = formats.getBasicDoubleCellFormat(),
                basicShortDoubleFormat = formats.getBasicShortDoubleFormat(),
                summaryBaseFormat = formats.getSummaryBaseCellFormat(),
                summaryDoubleFormat = formats.getSummaryDoubleCellFormat(),
                summaryDateFormat = formats.getSummaryDateCellFormat(),
                summaryBoldIntFormat = formats.getSummaryBoldIntFormat();

        // Get and sort the treatments.
        final List<Treat> treatments = week.getTreatments();
        if (treatments.isEmpty()) {
            return;
        }
        Collections.sort(week.getTreatments(), (x, y) -> Integer.compare(x.getNumber(), y.getNumber()));

        // Volumes that are to be displayed.
        double timberPackVolume = 0,
                totalGreenVolume = 0,
                totalRoundGreenVolume = 0,
                totalBrownVolume = 0;

        // Compute and display each of the treatments.
        List<TimberPack> timberPacks = null;
        RoundTimberPack roundTimberPack = null;
        CuboidTimberPack cuboidTimberPack = null;
        for (final Treat treat : treatments) {

            // Reset previous totals.
            totalGreenVolume = totalRoundGreenVolume = totalBrownVolume = 0;

            // Get (and sort) the timber packs.
            timberPacks = treat.getTimberPacks();
            if (treat.getType() == TreatType.ROUND_GREEN) {
                Collections.sort(timberPacks, (x, y) -> {
                    if (x.getTimberPackType() == y.getTimberPackType()) {
                        return 0;
                    } else if (x.getTimberPackType() == TimberPackType.ROUND
                            && y.getTimberPackType() == TimberPackType.CUBOID) {
                        return 1;
                    } else {
                        return -1;
                    }
                });
            }

            // Write the timber packs to the sheet.
            for (final TimberPack timberPack : timberPacks) {
                timberPackVolume = timberPack.getPackVolume();
                if (timberPack.getTimberPackType() == TimberPackType.ROUND) {
                    roundTimberPack = (RoundTimberPack) timberPack;
                    totalRoundGreenVolume += timberPackVolume;
                    super.setCellsWithBlanks(
                            sheet,
                            basicBaseFormat,
                            0,
                            columns,
                            new Number(6, rows, 0, basicDoubleFormat),
                            new Number(7, rows, roundTimberPack.getQuantity(), basicIntFormat),
                            new Number(8, rows, roundTimberPack.getLengthM(), basicShortDoubleFormat),
                            new Number(9, rows, roundTimberPack.getRadiusM() * 2, basicDoubleFormat),
                            new Number(10, rows, timberPackVolume, basicDoubleFormat),
                            new Number(15, rows, 0, basicDoubleFormat)
                    );
                } else {
                    cuboidTimberPack = (CuboidTimberPack) timberPack;
                    if (treat.getType() == TreatType.BROWN) {
                        totalBrownVolume += timberPackVolume;
                        super.setCellsWithBlanks(
                                sheet,
                                basicBaseFormat,
                                0,
                                columns,
                                new Number(6, rows, 0, basicDoubleFormat),
                                new Number(10, rows, 0, basicDoubleFormat),
                                new Number(11, rows, cuboidTimberPack.getQuantity(), basicIntFormat),
                                new Number(12, rows, cuboidTimberPack.getLengthM(), basicShortDoubleFormat),
                                new Number(13, rows, cuboidTimberPack.getBreadthMM(), basicIntFormat),
                                new Number(14, rows, cuboidTimberPack.getHeightMM(), basicIntFormat),
                                new Number(15, rows, timberPackVolume, basicDoubleFormat)
                        );
                    } else {

                        //if (treat.getType() == TreatType.GREEN) {
                            totalGreenVolume += timberPackVolume;
                        //} else {
                          //  totalRoundGreenVolume += timberPackVolume;
                        //}

                        super.setCellsWithBlanks(
                                sheet,
                                basicBaseFormat,
                                0,
                                columns,
                                new Number(2, rows, cuboidTimberPack.getQuantity(), basicIntFormat),
                                new Number(3, rows, cuboidTimberPack.getLengthM(), basicShortDoubleFormat),
                                new Number(4, rows, cuboidTimberPack.getBreadthMM(), basicIntFormat),
                                new Number(5, rows, cuboidTimberPack.getHeightMM(), basicIntFormat),
                                new Number(6, rows, timberPackVolume, basicDoubleFormat),
                                new Number(10, rows, 0, basicDoubleFormat),
                                new Number(15, rows, 0, basicDoubleFormat)
                        );
                    }
                }

                // Increment row to account for the pack.
                ++rows;
            }

            Log.wtf("GREEN VOLUME: ", "" + totalGreenVolume);

            // Write the treatment summary.
            super.setCellsWithBlanks(
                    sheet,
                    summaryBaseFormat,
                    0,
                    columns,
                    new Number(0, rows, treat.getNumber(), summaryBoldIntFormat),
                    new DateTime(1, rows, DateUtility.toUtilDate(week.getDate()), summaryDateFormat),
                    new Number(6, rows, totalGreenVolume, summaryDoubleFormat),
                    new Number(10, rows, totalRoundGreenVolume, summaryDoubleFormat),
                    new Number(15, rows, totalBrownVolume, summaryDoubleFormat)
            );

            // Increment row to account for summary.
            ++rows;
        }
    }
}

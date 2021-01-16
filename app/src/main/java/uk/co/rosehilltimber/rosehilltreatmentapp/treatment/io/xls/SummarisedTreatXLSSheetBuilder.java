package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls;

import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;

import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.formats.SummarisedTreatXLSFormats;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

public class SummarisedTreatXLSSheetBuilder extends TreatXLSSheetBuilder<SummarisedTreatXLSFormats>
{

    // String formats used within the xls file.
    private static final String SHEET_NAME_FORMAT = "%s Summary";
    private static final String VERBOSE_WEEK_DATE_COLUMN_FORMAT = "Week %d Total";

    @SuppressWarnings("WeakerAccess")
    protected SummarisedTreatXLSSheetBuilder(@NonNull final WritableWorkbook mWorkbook,
                                             @NonNull final LocalDate mStartOfFinancialYear,
                                             @NonNull final LocalDate mEndOfFinancialYear,
                                             @NonNull final LocalDate mMonth,
                                             @NonNull final Queue<Week> mMonthQueue)
            throws WriteException
    {
        // Instantiate the superclass sheet builder.
        super(
                mWorkbook,
                new SummarisedTreatXLSFormats(),
                mStartOfFinancialYear,
                mEndOfFinancialYear,
                mMonth,
                mMonthQueue
        );
    }

    @NonNull
    protected String createSheetName(@NonNull final LocalDate month)
    {
        return String.format(
                SHEET_NAME_FORMAT,
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        );
    }

    protected void createHeaderRows(final WritableSheet sheet)
            throws WriteException
    {

        // The header columns used for displaying the data.
        final String[] headerColumnNames = {
                "Treat No",
                "Week",
                "Date",
                "Green",
                "Round Green",
                "Brown",
                "Total m^3 per week"
        };

        // Define the formats applied to the summary header.
        final SummarisedTreatXLSFormats formats = super.getTreatXLSFormats();
        final WritableCellFormat[] columnFormats = {
                formats.getHeaderBaseCellFormat(),
                formats.getHeaderBaseCellFormat(),
                formats.getHeaderBaseCellFormat(),
                formats.getHeaderGreenCellFormat(),
                formats.getHeaderRoundGreenCellFormat(),
                formats.getHeaderBrownCellFormat(),
                formats.getHeaderYellowFormat()
        };

        // Add all of the cells to the sheet.
        Label headerColumn = null;
        for (int i = 0; i < headerColumnNames.length; ++i) {
            headerColumn = new Label(i, 0, headerColumnNames[i], columnFormats[i]);
            sheet.addCell(headerColumn);
        }

    }

    @Override
    protected void createDataRows(final WritableSheet sheet, final Week week)
            throws WriteException
    {
        // The current sheet dimensions.
        int columns = sheet.getColumns(), row = sheet.getRows();

        // Various different treatment volume based values.
        double greenVol = 0, roundGreenVol = 0, brownVol = 0,
                totalGreenVol = 0, totalRoundGreenVol = 0, totalBrownVol = 0;

        // Formats applied whilst writing the sheet.
        final SummarisedTreatXLSFormats formats = super.getTreatXLSFormats();
        final WritableCellFormat basicBaseFormat = formats.getBasicBaseCellFormat(),
                basicBoldIntFormat = formats.getBasicBoldIntFormat(),
                basicDoubleFormat = formats.getBasicDoubleCellFormat(),
                summaryIntFormat = formats.getSummaryIntCellFormat(),
                summaryDoubleFormat = formats.getSummaryDoubleCellFormat(),
                summaryDateFormat = formats.getSummaryDateCellFormat(),
                summaryStringFormat = formats.getSummaryStringFormat(),
                summaryYellowFormat = formats.getSummaryYellowFormat();

        // Write each of the treatments to the sheet.
        for (final Treat treat : week.getTreatments()) {

            // Compute the treatment volumes.
            greenVol = roundGreenVol = brownVol = 0;
            switch (treat.getType()) {
                case ROUND_GREEN:
                    roundGreenVol = treat.getTotalTreatTypeVolume(TimberPackType.ROUND);
                    totalRoundGreenVol += roundGreenVol;
                case GREEN:
                    greenVol = treat.getTotalTreatTypeVolume(TimberPackType.CUBOID);
                    totalGreenVol += greenVol;
                    break;
                case BROWN:
                    brownVol = treat.getTotalTreatTypeVolume(TimberPackType.CUBOID);
                    totalBrownVol += brownVol;
                    break;
            }

            // Write the treatment data to the sheet.
            super.setCellsWithBlanks(
                    sheet,
                    basicBaseFormat,
                    0,
                    columns,
                    new Number(0, row, treat.getNumber(), basicBoldIntFormat),
                    new Number(3, row, greenVol, basicDoubleFormat),
                    new Number(4, row, roundGreenVol, basicDoubleFormat),
                    new Number(5, row, brownVol, basicDoubleFormat)
            );

            // Increment the sheet row count.
            ++row;
        }

        // JXL requires java.util.Date, hence the conversion.
        final Date weekDate = Date.from(week.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        final long weekNumber = TreatUtility.weekNumber(super.getStartOfFinancialYear(), week.getDate());
        final double totalVol = totalGreenVol + totalRoundGreenVol + totalBrownVol;

        // Write the week summary of the week to the sheet.
        super.setCells(
                sheet,
                new Label(
                        0,
                        row,
                        String.format(
                                Locale.UK,
                                VERBOSE_WEEK_DATE_COLUMN_FORMAT,
                                weekNumber
                        ),
                        summaryStringFormat
                ),
                new Number(1, row, weekNumber, summaryIntFormat),
                new DateTime(
                        2,
                        row,
                        weekDate,
                        summaryDateFormat
                ),
                new Number(3, row, totalGreenVol, summaryDoubleFormat),
                new Number(4, row, totalRoundGreenVol, summaryDoubleFormat),
                new Number(5, row, totalBrownVol, summaryDoubleFormat),
                new Number(6, row, totalVol, summaryYellowFormat)
        );
    }
}

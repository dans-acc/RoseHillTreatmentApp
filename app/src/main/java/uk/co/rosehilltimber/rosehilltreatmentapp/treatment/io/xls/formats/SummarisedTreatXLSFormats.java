package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.formats;

import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WriteException;

public class SummarisedTreatXLSFormats extends CommonTreatXLSFormats
{

    // Additional header cell format.
    private final WritableCellFormat mHeaderYellowFormat;

    // Additional basic cell format.
    private final WritableCellFormat mBasicBoldIntFormat;

    // Additional summary cell formats.
    private final WritableCellFormat mSummaryStringFormat;
    private final WritableCellFormat mSummaryYellowFormat;

    public SummarisedTreatXLSFormats()
            throws WriteException
    {
        // Initialise the common super-class formats.
        super();

        // Initialise the additional header.
        mHeaderYellowFormat = new WritableCellFormat(super.getHeaderBaseCellFormat());
        mHeaderYellowFormat.setBackground(Colour.YELLOW);

        // Initialise the bold int basic format.
        mBasicBoldIntFormat = new WritableCellFormat(super.getBoldFont(), super.getIntNumberFormat());
        super.copyBaseCellFormat(mBasicBoldIntFormat, super.getBasicIntCellFormat());

        // Create the verbose summary week no. format.
        mSummaryStringFormat = new WritableCellFormat(super.getBoldFont());
        super.copyBaseCellFormat(mSummaryStringFormat, super.getSummaryBaseCellFormat());
        mSummaryStringFormat.setAlignment(Alignment.LEFT);

        // Create the summary week total cell format.
        mSummaryYellowFormat = new WritableCellFormat(super.getSummaryDoubleCellFormat());
        mSummaryYellowFormat.setBackground(Colour.YELLOW);
    }

    public final WritableCellFormat getHeaderYellowFormat() {
        return mHeaderYellowFormat;
    }

    public final WritableCellFormat getBasicBoldIntFormat() {
        return mBasicBoldIntFormat;
    }

    public final WritableCellFormat getSummaryStringFormat() {
        return mSummaryStringFormat;
    }

    public final WritableCellFormat getSummaryYellowFormat() {
        return mSummaryYellowFormat;
    }
}

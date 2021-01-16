package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.formats;

import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WriteException;

public class DetailedTreatXLSFormats extends CommonTreatXLSFormats
{

    // The number format used for the length of the timber.
    private static final String CELL_SHORT_DOUBLE_FORMAT = "0.0#";

    // Additional number formats.
    private final NumberFormat mShortDoubleNumberFormat;

    // Additional basic cel format.
    private final WritableCellFormat mBasicShortDoubleFormat;

    // Additional summary cell format.
    private final WritableCellFormat mSummaryBoldIntFormat;

    public DetailedTreatXLSFormats()
            throws WriteException
    {
        // Initialise the super (common) cell formats.
        super();

        // Initialise the short double number format.
        mShortDoubleNumberFormat = new NumberFormat(CELL_SHORT_DOUBLE_FORMAT);

        // Initialise the short double cell format used for defining the length of timber.
        mBasicShortDoubleFormat = new WritableCellFormat(super.getNormalFont(), mShortDoubleNumberFormat);
        super.copyBaseCellFormat(mBasicShortDoubleFormat, super.getBasicBaseCellFormat());

        // Initialise the format used for representing week no.
        mSummaryBoldIntFormat = new WritableCellFormat(super.getBoldFont());
        super.copyBaseCellFormat(mSummaryBoldIntFormat, super.getSummaryIntCellFormat());
    }

    public final NumberFormat getShortDoubleNumberFormat()
    {
        return mShortDoubleNumberFormat;
    }

    public final WritableCellFormat getBasicShortDoubleFormat() {
        return mBasicShortDoubleFormat;
    }

    public final WritableCellFormat getSummaryBoldIntFormat() {
        return mSummaryBoldIntFormat;
    }
}

package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.formats;

import jxl.format.*;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;

public abstract class CommonTreatXLSFormats
{

    // Default font properties employed by cell formats.
    private static final WritableFont.FontName CELL_FONT_NAME = WritableFont.ARIAL;
    private static final int CELL_FONT_SIZE = 11;

    // Default int, double and date cell formats.
    private static final String CELL_INT_FORMAT = "0";
    private static final String CELL_DOUBLE_FORMAT = "0.000";
    private static final String CELL_DATE_FORMAT = "dd/MM/yyyy";

    // Common font types employed by the sheet builder.
    private final WritableFont mNormalFont;
    private final WritableFont mBoldFont;

    // Common int, double, and date formats employed by cell formats.
    private final NumberFormat mIntNumberFormat;
    private final NumberFormat mDoubleNumberFormat;
    private final DateFormat mDateDateFormat;

    // Common header cells are used to display column headers (or titles).
    private final WritableCellFormat mHeaderBaseCellFormat;
    private final WritableCellFormat mHeaderGreenCellFormat;
    private final WritableCellFormat mHeaderRoundGreenCellFormat;
    private final WritableCellFormat mHeaderBrownCellFormat;

    // Common basic treatment cell formats.
    private final WritableCellFormat mBasicBaseCellFormat;
    private final WritableCellFormat mBasicIntCellFormat;
    private final WritableCellFormat mBasicDoubleCellFormat;
    private final WritableCellFormat mBasicDateCellFormat;

    // Common summary treatment cell formats.
    private final WritableCellFormat mSummaryBaseCellFormat;
    private final WritableCellFormat mSummaryIntCellFormat;
    private final WritableCellFormat mSummaryDoubleCellFormat;
    private final WritableCellFormat mSummaryDateCellFormat;

    // These function calls are too expensive!!!

    @SuppressWarnings("WeakerAccess")
    protected CommonTreatXLSFormats()
            throws WriteException
    {
        // Instantiate the fonts.
        mNormalFont = new WritableFont(CELL_FONT_NAME, CELL_FONT_SIZE);
        mBoldFont = new WritableFont(CELL_FONT_NAME, CELL_FONT_SIZE, WritableFont.BOLD);

        // Create the common number and date formats.
        mIntNumberFormat = new NumberFormat(CELL_INT_FORMAT);
        mDoubleNumberFormat = new NumberFormat(CELL_DOUBLE_FORMAT);
        mDateDateFormat = new DateFormat(CELL_DATE_FORMAT);

        // Create the header cell formats.
        mHeaderBaseCellFormat = new WritableCellFormat(mBoldFont);
        mHeaderBaseCellFormat.setWrap(true);
        mHeaderBaseCellFormat.setAlignment(Alignment.CENTRE);
        mHeaderBaseCellFormat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        mHeaderBaseCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        mHeaderBaseCellFormat.setBackground(Colour.WHITE);

        mHeaderGreenCellFormat = new WritableCellFormat(mHeaderBaseCellFormat);
        mHeaderGreenCellFormat.setBackground(Colour.OLIVE_GREEN);

        mHeaderRoundGreenCellFormat = new WritableCellFormat(mHeaderBaseCellFormat);
        mHeaderRoundGreenCellFormat.setBackground(Colour.LIGHT_GREEN);

        mHeaderBrownCellFormat = new WritableCellFormat(mHeaderBaseCellFormat);
        mHeaderBrownCellFormat.setBackground(Colour.BROWN);

        // Create the base cell formats.
        mBasicBaseCellFormat = new WritableCellFormat(mNormalFont);
        mBasicBaseCellFormat.setWrap(true);
        mBasicBaseCellFormat.setAlignment(Alignment.RIGHT);
        mBasicBaseCellFormat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        mBasicBaseCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        mBasicBaseCellFormat.setBackground(Colour.WHITE);

        mBasicIntCellFormat = new WritableCellFormat(mNormalFont, mIntNumberFormat);
        copyBaseCellFormat(mBasicIntCellFormat, mBasicBaseCellFormat);

        mBasicDoubleCellFormat = new WritableCellFormat(mNormalFont, mDoubleNumberFormat);
        copyBaseCellFormat(mBasicDoubleCellFormat, mBasicBaseCellFormat);

        mBasicDateCellFormat = new WritableCellFormat(mNormalFont, mDateDateFormat);
        copyBaseCellFormat(mBasicDateCellFormat, mBasicBaseCellFormat);

        // Create the common summary cell formats.
        mSummaryBaseCellFormat = new WritableCellFormat(mNormalFont);
        mSummaryBaseCellFormat.setWrap(true);
        mSummaryBaseCellFormat.setAlignment(Alignment.RIGHT);
        mSummaryBaseCellFormat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        mSummaryBaseCellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
        mSummaryBaseCellFormat.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM);
        mSummaryBaseCellFormat.setBorder(Border.LEFT, BorderLineStyle.THIN);
        mSummaryBaseCellFormat.setBorder(Border.RIGHT,BorderLineStyle.THIN);
        mSummaryBaseCellFormat.setBackground(Colour.GRAY_25);

        mSummaryIntCellFormat = new WritableCellFormat(mNormalFont, mIntNumberFormat);
        copyBaseCellFormat(mSummaryIntCellFormat, mSummaryBaseCellFormat);

        mSummaryDoubleCellFormat = new WritableCellFormat(mNormalFont, mDoubleNumberFormat);
        copyBaseCellFormat(mSummaryDoubleCellFormat, mSummaryBaseCellFormat);

        mSummaryDateCellFormat = new WritableCellFormat(mNormalFont, mDateDateFormat);
        copyBaseCellFormat(mSummaryDateCellFormat, mSummaryBaseCellFormat);
    }

    public final WritableFont getNormalFont() {
        return mNormalFont;
    }

    public final WritableFont getBoldFont() {
        return mBoldFont;
    }

    public final NumberFormat getIntNumberFormat() {
        return mIntNumberFormat;
    }

    public final NumberFormat getDoubleNumberFormat() {
        return mDoubleNumberFormat;
    }

    public final DateFormat getDateDateFormat() {
        return mDateDateFormat;
    }

    public final WritableCellFormat getHeaderBaseCellFormat() {
        return mHeaderBaseCellFormat;
    }

    public final WritableCellFormat getHeaderGreenCellFormat() {
        return mHeaderGreenCellFormat;
    }

    public final WritableCellFormat getHeaderRoundGreenCellFormat() {
        return mHeaderRoundGreenCellFormat;
    }

    public final WritableCellFormat getHeaderBrownCellFormat() {
        return mHeaderBrownCellFormat;
    }

    public final WritableCellFormat getBasicBaseCellFormat() {
        return mBasicBaseCellFormat;
    }

    public final WritableCellFormat getBasicIntCellFormat() {
        return mBasicIntCellFormat;
    }

    public final WritableCellFormat getBasicDoubleCellFormat() {
        return mBasicDoubleCellFormat;
    }

    public final WritableCellFormat getBasicDateCellFormat() {
        return mBasicDateCellFormat;
    }

    public final WritableCellFormat getSummaryBaseCellFormat() {
        return mSummaryBaseCellFormat;
    }

    public final WritableCellFormat getSummaryIntCellFormat() {
        return mSummaryIntCellFormat;
    }

    public final WritableCellFormat getSummaryDoubleCellFormat() {
        return mSummaryDoubleCellFormat;
    }

    public final WritableCellFormat getSummaryDateCellFormat() {
        return mSummaryDateCellFormat;
    }

    @SuppressWarnings("WeakerAccess")
    protected void copyBaseCellFormat(final WritableCellFormat destCell, final WritableCellFormat srcCell)
            throws WriteException
    {
        destCell.setLocked(srcCell.isLocked());
        destCell.setWrap(srcCell.getWrap());
        destCell.setShrinkToFit(srcCell.isShrinkToFit());
        destCell.setIndentation(srcCell.getIndentation());
        destCell.setOrientation(srcCell.getOrientation());
        destCell.setAlignment(srcCell.getAlignment());
        destCell.setVerticalAlignment(srcCell.getVerticalAlignment());
        destCell.setBorder(
                Border.TOP,
                srcCell.getBorder(Border.TOP),
                srcCell.getBorderColour(Border.TOP
                ));
        destCell.setBorder(
                Border.BOTTOM,
                srcCell.getBorder(Border.BOTTOM),
                srcCell.getBorderColour(Border.BOTTOM
                ));
        destCell.setBorder(
                Border.LEFT,
                srcCell.getBorder(Border.LEFT),
                srcCell.getBorderColour(Border.LEFT
                ));
        destCell.setBorder(
                Border.RIGHT,
                srcCell.getBorder(Border.RIGHT),
                srcCell.getBorderColour(Border.RIGHT
                ));
        destCell.setBackground(srcCell.getBackgroundColour());
    }
}

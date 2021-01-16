package uk.co.rosehilltimber.rosehilltreatmentapp.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import android.widget.ImageView;
import android.widget.TextView;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;

public class FileListAdapter extends BaseListAdapter<File>
{

    private static class FileItemViewHolder
    {
        private TextView mPeriodTextViewField;
        private TextView mFilenameTextViewField;
        private TextView mLastModifiedTextViewField;
        private ImageView mBackupFileImageButton;
        private ImageView mDeleteFileImageButton;
    }

    private static final String FILE_NAME_DELIMITER = "_";
    private static final String FILE_NAME_INVALID_FORMAT = "Invalid file name format.";

    private static final String PERIOD_SINGLE_WEEK = "Week %s (%d - %d)";
    private static final String PERIOD_MULTIPLE_WEEKS = "Weeks %s to %s (%d - %d)";

    private static final String PERIOD_NOT_APPLICABLE = "N/A (Ensure financial year is set.)";
    private static final String PERIOD_OUT_OF_BOUNDS = " (Out of financial year bounds.)";

    private final LocalDate mStartOfFinancialYear;
    private final LocalDate mEndOfFinancialYear;

    public FileListAdapter(final Activity mActivity,
                           final View.OnClickListener mOnClickListener,
                           final List<File> mFiles,
                           final LocalDate mStartOfFinancialYear,
                           final LocalDate mEndOfFinancialYear)
    {
        super(
                mActivity,
                mOnClickListener,
                mFiles,
                R.layout.item_file
        );

        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;
    }

    public final LocalDate getStartOfFinancialYear() {
        return mStartOfFinancialYear;
    }

    public final LocalDate getEndOfFinancialYear() {
        return mEndOfFinancialYear;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View view, @NonNull final ViewGroup viewGroup)
    {
        FileItemViewHolder fileItemViewHolder = null;
        if (view == null) {

            // Create the file item view.
            view = LayoutInflater.from(super.mActivity).inflate(
                    super.mResource,
                    viewGroup,
                    BaseListAdapter.DEFAULT_ATTACH_TO_ROOT
            );

            // View holder grants faster access.
            fileItemViewHolder = new FileItemViewHolder();
            fileItemViewHolder.mPeriodTextViewField = view.findViewById(R.id.item_file_tv_period_field);
            fileItemViewHolder.mFilenameTextViewField = view.findViewById(R.id.item_file_tv_filename_field);
            fileItemViewHolder.mLastModifiedTextViewField = view.findViewById(R.id.item_file_tv_last_modified_field);
            fileItemViewHolder.mBackupFileImageButton = view.findViewById(R.id.item_file_iv_backup_action);
            fileItemViewHolder.mDeleteFileImageButton = view.findViewById(R.id.item_file_iv_delete_option);

            // Set the on click listeners for the backup and delete options.
            fileItemViewHolder.mBackupFileImageButton.setOnClickListener(super.mOnClickListener);
            fileItemViewHolder.mDeleteFileImageButton.setOnClickListener(super.mOnClickListener);

            view.setTag(fileItemViewHolder);
        } else {
            fileItemViewHolder = (FileItemViewHolder) view.getTag();
        }

        // Set the button positions.
        fileItemViewHolder.mBackupFileImageButton.setTag(position);
        fileItemViewHolder.mDeleteFileImageButton.setTag(position);

        final File file = super.getItem(position);
        if (file == null) {
            return view;
        }

        // Get the basic file name of the file.
        String basicFileName = file.getName();
        basicFileName = basicFileName.substring(0, basicFileName.lastIndexOf('.'));

        // Delimit the filename into individual parts / dates.
        final String[] fileNameTokens = basicFileName.split(FILE_NAME_DELIMITER);
        final int fileNameTokensLen = fileNameTokens.length;

        // Update the file name period field.
        if (mStartOfFinancialYear == null || mEndOfFinancialYear == null) {
            fileItemViewHolder.mPeriodTextViewField.setText(PERIOD_NOT_APPLICABLE);
        } else if (fileNameTokensLen != 3 && fileNameTokensLen != 4) {
            fileItemViewHolder.mPeriodTextViewField.setText(FILE_NAME_INVALID_FORMAT);
        } else {
            try {

                // The first two tokens are always the start and end of the financial year.
                int fileStartFinancialYear = Integer.parseInt(fileNameTokens[0]),
                        fileEndFinancialYear = Integer.parseInt(fileNameTokens[1]),
                        fileWeekFrom = -1,
                        fileWeekTo = -1;

                // Determine the most appropriate period filed value based on the name.
                if (fileStartFinancialYear != mStartOfFinancialYear.getYear() || fileEndFinancialYear != mEndOfFinancialYear.getYear()) {
                    if (fileNameTokensLen == 3) {
                        fileItemViewHolder.mPeriodTextViewField.setText(String.format(
                                Locale.UK,
                                PERIOD_SINGLE_WEEK + PERIOD_OUT_OF_BOUNDS,
                                Integer.parseInt(fileNameTokens[2]),
                                fileStartFinancialYear,
                                fileEndFinancialYear
                        ));
                    } else {
                        fileItemViewHolder.mPeriodTextViewField.setText(String.format(
                                Locale.UK,
                                PERIOD_MULTIPLE_WEEKS + PERIOD_OUT_OF_BOUNDS,
                                Integer.parseInt(fileNameTokens[2]),
                                Integer.parseInt(fileNameTokens[3]),
                                fileStartFinancialYear,
                                fileEndFinancialYear
                        ));
                    }
                } else if (fileNameTokensLen == 3) {
                    fileWeekFrom = Integer.parseInt(fileNameTokens[2]);
                    fileItemViewHolder.mPeriodTextViewField.setText(String.format(
                            Locale.UK,
                            PERIOD_SINGLE_WEEK,
                            mStartOfFinancialYear.plusWeeks(fileWeekFrom - 1).format(DateUtility.BASIC_DATE_FORMATTER),
                            fileStartFinancialYear,
                            fileEndFinancialYear
                    ));
                } else {
                    fileWeekFrom = Integer.parseInt(fileNameTokens[2]);
                    fileWeekTo = Integer.parseInt(fileNameTokens[3]);
                    fileItemViewHolder.mPeriodTextViewField.setText(String.format(
                            Locale.UK,
                            PERIOD_MULTIPLE_WEEKS,
                            mStartOfFinancialYear.plusWeeks(fileWeekFrom - 1).format(DateUtility.BASIC_DATE_FORMATTER),
                            mStartOfFinancialYear.plusWeeks(fileWeekTo - 1).format(DateUtility.BASIC_DATE_FORMATTER),
                            fileStartFinancialYear,
                            fileEndFinancialYear
                    ));
                }

            } catch (final NumberFormatException e) {
                fileItemViewHolder.mPeriodTextViewField.setText(FILE_NAME_INVALID_FORMAT);
            }
        }

        // Finally update the file name and the last modified fields.
        fileItemViewHolder.mFilenameTextViewField.setText(file.getName());
        fileItemViewHolder.mLastModifiedTextViewField.setText(
                DateUtility.fromEpochMilli(file.lastModified())
                .format(DateUtility.BASIC_DATE_FORMATTER)
        );

        return view;
    }

}

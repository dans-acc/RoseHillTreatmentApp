package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.xls.TreatXLSWorkbookBuilder;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.WorkbookUtility;

public class AsyncTreatFileWriterTask extends AsyncTask<Void, Void, Boolean>
{


    // The extension used for the file.
    private static final String XLS_FILE_EXTENSION = ".xls";

    // Filename formats - dependent on the number of weeks being written.
    private static final String YYYY_YYYY_WW_FILENAME_FORMAT = "%s_%s_%d";
    private static final String YYYY_YYYY_WW_WW_FILENAME_FORMAT = "%s_%s_%d_%d";

    // The activity responsible for writing the file.
    private final WeakReference<Activity> mWeakActivity;

    // All weeks are to be written relative to the following dates.
    private final LocalDate mStartOfFinancialYear;
    private final LocalDate mEndOfFinancialYear;

    // The directory into which the file is to be written.
    private final File mTargetDir;

    // The weeks that are to be written to the workbook.
    private final Week[] mWeeks;

    @SuppressWarnings("WeakerAccess")
    public AsyncTreatFileWriterTask(@NonNull final WeakReference<Activity> mWeakActivity,
                           @NonNull final LocalDate mStartOfFinancialYear,
                           @NonNull final LocalDate mEndOfFinancialYear,
                           @NonNull final File mTargetDir,
                           @NonNull final Week... mWeeks)
    {
        this.mWeakActivity = mWeakActivity;
        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;
        this.mTargetDir = mTargetDir;
        this.mWeeks = mWeeks;
    }

    public AsyncTreatFileWriterTask(@NonNull final Activity mActivity,
                           @NonNull final LocalDate mStartOfFinancialYear,
                           @NonNull final LocalDate mEndOfFinancialYear,
                           @NonNull final File mTargetDir,
                           @NonNull final Week... mWeeks)
    {
        this(
                new WeakReference<>(mActivity),
                mStartOfFinancialYear,
                mEndOfFinancialYear,
                mTargetDir,
                mWeeks
        );
    }

    @NonNull
    public final WeakReference<Activity> getWeakActivity()
    {
        return mWeakActivity;
    }

    @Nullable
    public final Activity getActivity()
    {
        return mWeakActivity.get();
    }

    @NonNull
    public final File getTargetDir()
    {
        return mTargetDir;
    }

    @NonNull
    public final LocalDate getStartOfFinancialYear()
    {
        return mStartOfFinancialYear;
    }

    @NonNull
    public final LocalDate getEndOfFinancialYear()
    {
        return mEndOfFinancialYear;
    }

    public final Week[] getWeeks()
    {
        return mWeeks;
    }

    @Override
    public Boolean doInBackground(@NonNull Void... avoid)
    {

        // Week data simply must be present.
        if (mWeeks.length == 0) {
            throw new IllegalArgumentException("Cannot write workbook without any data.");
        }

        // Ensure the activity is still 'active'.
        final Activity activity = getActivity();
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return false;
        }

        // Sort the weeks to ensure a correct filename.
        if (mWeeks.length > 1) {
            Arrays.sort(mWeeks);
        }

        // Create and write the workbook to the file.
        final File saveTo = new File(mTargetDir, createWorkbookFilename());
        final WritableWorkbook workbook = buildWorkbook(saveTo);

        if (workbook == null) {
            return false;
        }

        return writeWorkbook(workbook);
    }

    @NonNull
    private String createWorkbookFilename()
    {
        // Create the workbook filename based on the number of weeks.
        final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
        final String workbookFilename;
        if (mWeeks.length == 1) {
            workbookFilename = String.format(
                    Locale.UK,
                    YYYY_YYYY_WW_FILENAME_FORMAT,
                    mStartOfFinancialYear.format(yearFormatter),
                    mEndOfFinancialYear.format(yearFormatter),
                    TreatUtility.weekNumber(mStartOfFinancialYear, mWeeks[0].getDate())
            );
        } else {
            workbookFilename = String.format(
                    Locale.UK,
                    YYYY_YYYY_WW_WW_FILENAME_FORMAT,
                    mStartOfFinancialYear.format(yearFormatter),
                    mEndOfFinancialYear.format(yearFormatter),
                    TreatUtility.weekNumber(mStartOfFinancialYear, mWeeks[0].getDate()),
                    TreatUtility.weekNumber(mStartOfFinancialYear, mWeeks[mWeeks.length - 1].getDate())
            );
        }

        // Add the filename extension.
        return workbookFilename + XLS_FILE_EXTENSION;
    }

    @Nullable
    private WritableWorkbook buildWorkbook(final File workbookFile)
    {
        try {

            // The builder used for creating the workbook.
            final TreatXLSWorkbookBuilder workbookBuilder = new TreatXLSWorkbookBuilder(
                    mStartOfFinancialYear,
                    mEndOfFinancialYear,
                    workbookFile,
                    mWeeks).setSortWeeks(false);

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

    private boolean writeWorkbook(final WritableWorkbook workbook)
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

    @Override
    public void onPostExecute(@NonNull final Boolean successfulWrite)
    {
        final Activity activity = getActivity();
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }

        // Prompt the user with an appropriate message based on whether or not the file has been successfully written.
        if (successfulWrite) {
            Toast.makeText(
                    activity,
                    activity.getString(R.string.treat_file_writer_toast_write_successful),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.treat_file_writer_alert_title_write_unsuccessful)
                    .setMessage(activity.getString(R.string.treat_file_writer_alert_msg_write_unsuccessful))
                    .setNeutralButton(
                            activity.getString(R.string.button_name_ok),
                            (dialog, which) -> dialog.cancel()
                    ).create().show();
        }

        super.onPostExecute(successfulWrite);
    }
}

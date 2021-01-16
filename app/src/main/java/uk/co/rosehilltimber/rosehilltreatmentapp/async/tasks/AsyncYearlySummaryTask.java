package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import android.app.Activity;
import jxl.write.WritableWorkbook;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.SettingsActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatContract;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class AsyncYearlySummaryTask extends AsyncProviderTask<Activity, Void, Void, Boolean>
{

    private static final boolean SHOW_PROGRESS_DIALOG_BY_DEFAULT = true;

    private LocalDate mStartOfFinancialYear;
    private LocalDate mEndOfFinancialYear;

    private StorageUtility mStorageUtility;


    public AsyncYearlySummaryTask(final Activity mActivity,
                                  final LocalDate mStartOfFinancialYear,
                                  final LocalDate mEndOfFinancialYear)
    {
        super(mActivity, SHOW_PROGRESS_DIALOG_BY_DEFAULT, TreatContract.CONTENT_AUTHORITY);

        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;

        this.mStorageUtility = StorageUtility.getInstance(mActivity);

        super.setProgressDialogTitle(mActivity.getString(R.string.dialog_title_please_wait));
        super.setProgressDialogMessage(mActivity.getString(R.string.dialog_message_please_wait_exporting_yearly_summary));
    }

    private StorageUtility getStorageUtility()
    {
        return mStorageUtility;
    }

    @Override
    protected Boolean doInBackground(final Void... avoid)
    {
        final File targetDirectory = mStorageUtility.getExternalXLSDirectory();
        if (targetDirectory == null) {
            return false;
        }

        final List<Treat> treats = DatabaseUtility.selectAllTreats(super.getContentResolver());
        TreatUtility.sortTreatsIntoDescendingOrder(treats);

        final List<Week> weeks = TreatUtility.intoWeeks(treats);

        final WritableWorkbook writableWorkbook = WorkbookUtility.buildWorkbook(
                targetDirectory,
                mStartOfFinancialYear,
                mEndOfFinancialYear,
                weeks
        );

        if (writableWorkbook == null) {
            return false;
        }

        WorkbookUtility.writeWorkbook(writableWorkbook);
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {
        super.dismissProgressDialog();
        final Activity activity = super.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        if (success) {
            DialogUtility.buildOkAlertDialog(
                    activity,
                    R.string.dialog_title_successfully_exported_a_yearly_summary,
                    R.string.dialog_message_successfully_exported_a_yearly_summary
            );
        } else {
            DialogUtility.buildOkAlertDialog(
                    activity,
                    R.string.dialog_title_failed_to_export_yearly_summary,
                    R.string.dialog_message_failed_to_export_yearly_summary
            );
        }
    }

}

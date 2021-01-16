package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import android.database.sqlite.SQLiteException;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.SettingsActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatDatabaseHelper;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;

public class AsyncTruncateTablesTask extends AsyncProgressTask<SettingsActivity, Void, Void, Boolean>
{

    private static final boolean SHOW_PROGRESS_DIALOG_BY_DEFAULT = true;

    private TreatDatabaseHelper mTreatDatabaseHelper;

    public AsyncTruncateTablesTask(final SettingsActivity mSettingsActivity)
    {
        super(mSettingsActivity, SHOW_PROGRESS_DIALOG_BY_DEFAULT);

        this.mTreatDatabaseHelper = TreatDatabaseHelper.getInstance(mSettingsActivity);

        super.setProgressDialogTitle(mSettingsActivity.getString(R.string.dialog_title_please_wait));
        super.setProgressDialogMessage(mSettingsActivity.getString(R.string.dialog_message_please_wait_truncating_tables));
    }

    public TreatDatabaseHelper getTreatDatabaseHelper()
    {
        return mTreatDatabaseHelper;
    }

    @Override
    public Boolean doInBackground(final Void... avoid)
    {
        try {
            mTreatDatabaseHelper.truncateTables();
            return true;
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onPostExecute(final Boolean success)
    {
        super.dismissProgressDialog();
        final SettingsActivity settingsActivity = super.getActivity();
        if (settingsActivity == null || settingsActivity.isFinishing() || settingsActivity.isDestroyed()){
            return;
        }

        if (success) {
            DialogUtility.buildOkAlertDialog(
                    settingsActivity,
                    R.string.dialog_title_tables_successfully_truncated,
                    R.string.dialog_message_tables_successfully_truncated
            );
        } else {
            DialogUtility.buildOkAlertDialog(
                    settingsActivity,
                    R.string.dialog_title_failed_to_truncate_tables,
                    R.string.dialog_message_failed_to_truncate_tables
            );
        }

    }

}

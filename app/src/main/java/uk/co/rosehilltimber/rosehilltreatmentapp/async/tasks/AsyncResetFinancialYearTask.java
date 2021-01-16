package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import android.database.sqlite.SQLiteException;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.SettingsActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatDatabaseHelper;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.SharedPreferenceUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.StorageUtility;

import java.io.File;

public class AsyncResetFinancialYearTask extends AsyncProgressTask<SettingsActivity, Void, Void, Boolean>
{

    private static final boolean SHOW_PROGRESS_DIALOG_BY_DEFAULT = true;

    private StorageUtility mStorageUtility;
    private SharedPreferenceUtility mSharedPreferenceUtility;
    private TreatDatabaseHelper mTreatDatabaseHelper;

    public AsyncResetFinancialYearTask(final SettingsActivity mSettingsActivity)
    {
        super(mSettingsActivity, SHOW_PROGRESS_DIALOG_BY_DEFAULT);

        this.mStorageUtility = StorageUtility.getInstance(mSettingsActivity);
        this.mSharedPreferenceUtility = SharedPreferenceUtility.getInstance(mSettingsActivity);
        this.mTreatDatabaseHelper = TreatDatabaseHelper.getInstance(mSettingsActivity);

        super.setProgressDialogTitle(mSettingsActivity.getString(R.string.dialog_title_please_wait));
        super.setProgressDialogMessage(mSettingsActivity.getString(R.string.dialog_message_please_wait_resetting_financial_year));

    }

    private StorageUtility getStorageUtility()
    {
        return mStorageUtility;
    }

    private SharedPreferenceUtility getSharedPreferenceUtility()
    {
        return mSharedPreferenceUtility;
    }

    private TreatDatabaseHelper getTreatDatabaseHelper()
    {
        return mTreatDatabaseHelper;
    }

    @Override
    protected Boolean doInBackground(final Void... avoid)
    {
        if (mStorageUtility == null || mSharedPreferenceUtility == null) {
            return false;
        }

        try {
            final File[] files = mStorageUtility.getExternalXLSDirectory().listFiles();
            for (final File file : files) {
                if (!file.delete()) {
                    return false;
                }
            }
        } catch (final SecurityException e) {
            e.printStackTrace();
            return false;
        }

        mSharedPreferenceUtility.resetFinancialYearKeys();

        try {
            mTreatDatabaseHelper.truncateTables();
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {
        super.dismissProgressDialog();
        final SettingsActivity settingsActivity = super.getActivity();
        if (settingsActivity == null || settingsActivity.isFinishing() || settingsActivity.isDestroyed()) {
            return;
        }

        settingsActivity.createPreferencesFragment();

        if (success) {
            DialogUtility.buildOkAlertDialog(
                    settingsActivity,
                    R.string.dialog_title_financial_year_successfully_reset,
                    R.string.dialog_message_financial_year_successfully_reset
            );
        } else {
            DialogUtility.buildOkAlertDialog(
                    settingsActivity,
                    R.string.dialog_title_failed_to_reset_financial_year,
                    R.string.dialog_message_failed_to_reset_financial_year
            );
        }
    }
}

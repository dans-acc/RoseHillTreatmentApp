package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.SettingsActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.TreatFileFilter;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.StorageUtility;

import java.io.File;

public class AsyncDeleteAllExportsTask extends AsyncProgressTask<SettingsActivity, Void, Void, Boolean>
{

    private static final boolean SHOW_PROGRESS_DIALOG_BY_DEFAULT = true;

    private StorageUtility mStorageUtility;

    public AsyncDeleteAllExportsTask(final SettingsActivity mSettingsActivity)
    {
        super(mSettingsActivity, SHOW_PROGRESS_DIALOG_BY_DEFAULT);

        this.mStorageUtility = StorageUtility.getInstance(mSettingsActivity);

        super.setProgressDialogTitle(mSettingsActivity
                .getString(R.string.dialog_title_please_wait));
        super.setProgressDialogMessage(mSettingsActivity
                .getString(R.string.dialog_message_please_wait_deleting_all_exported_files));
    }

    private StorageUtility getStorageUtility()
    {
        return mStorageUtility;
    }

    @Override
    protected Boolean doInBackground(final Void... avoid)
    {

        try {
            final File[] files = mStorageUtility.getExternalXLSDirectory().listFiles(new TreatFileFilter());
            if (files == null || files.length == 0) {
                return false;
            }
            for (final File file : files) {
                if (!file.delete()) {
                    return false;
                }
            }
            return true;
        } catch (final SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {
        super.dismissProgressDialog();
        final SettingsActivity settingsActivity = super.getActivity();
        if (settingsActivity == null || settingsActivity.isFinishing() || settingsActivity.isDestroyed()) {
            return;
        }

        if (success) {
            DialogUtility.buildOkAlertDialog(
                    settingsActivity,
                    R.string.dialog_title_successfully_deleted_all_exported_files,
                    R.string.dialog_message_successfully_deleted_all_exported_files
            );
        } else {
            DialogUtility.buildOkAlertDialog(
                    settingsActivity,
                    R.string.dialog_title_failed_to_delete_all_exported_files,
                    R.string.dialog_message_failed_to_delete_all_exported_files
            );
        }
    }

}

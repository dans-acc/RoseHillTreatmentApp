package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.TreatWeekActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatContract;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DatabaseUtility;

import java.util.UUID;

public class AsyncTreatDeleterTask extends AsyncProviderTask<TreatWeekActivity, Void, Void, Boolean>
{

    public interface AsyncTreatDeleterListener
    {
        void onAsyncTreatDeleterResult(final boolean success, final UUID... treatUUIDs);
    }

    private UUID[] mTreatUUIDs;

    public AsyncTreatDeleterTask(final TreatWeekActivity mTreatWeekActivity,
                                 final UUID... mTreatUUIDs)
    {
        super(mTreatWeekActivity, TreatContract.CONTENT_AUTHORITY);

        this.mTreatUUIDs = mTreatUUIDs;

        super.setShowsProgressDialog(mTreatUUIDs.length > 1);
        if (!super.showsProgressDialog()) {
            return;
        }

        super.setProgressDialogTitle(mTreatWeekActivity.getString(R.string.dialog_title_please_wait));
        super.setProgressDialogMessage(mTreatWeekActivity.getString(R.string.dialog_message_please_wait_deleting_treats));
    }

    private UUID[] getTreatUUIDs()
    {
        return mTreatUUIDs;
    }

    @Override
    protected Boolean doInBackground(final Void... avoid)
    {
        return DatabaseUtility.deleteTreats(super.getContentResolver(), mTreatUUIDs);
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {
        super.dismissProgressDialog();
        final TreatWeekActivity treatWeekActivity = super.getActivity();
        if (treatWeekActivity == null || treatWeekActivity.isFinishing() || treatWeekActivity.isDestroyed()) {
            return;
        }
        treatWeekActivity.onAsyncTreatDeleterResult(success, mTreatUUIDs);
    }

}

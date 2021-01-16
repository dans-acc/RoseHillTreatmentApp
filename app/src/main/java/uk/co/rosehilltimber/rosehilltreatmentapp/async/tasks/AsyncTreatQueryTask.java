package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import uk.co.rosehilltimber.rosehilltreatmentapp.activities.TreatWeekActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatContract;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DatabaseUtility;

import java.util.UUID;

public class AsyncTreatQueryTask extends AsyncProviderTask<TreatWeekActivity, Void, Void, Treat>
{

    public interface AsyncTreatQueryListener
    {
        void onAsyncTreatQueryResult(final Treat treat);
    }

    private static final boolean SHOWS_PROGRESS_DIALOG = false;

    private UUID mTreatUUID;

    @SuppressWarnings("WeakerAccess")
    public AsyncTreatQueryTask(final TreatWeekActivity mTreatWeekActivity,
                               final UUID mTreatUUID)
    {
        super(mTreatWeekActivity, SHOWS_PROGRESS_DIALOG, TreatContract.CONTENT_AUTHORITY);

        this.mTreatUUID = mTreatUUID;
    }

    public AsyncTreatQueryTask(final TreatWeekActivity mTreatWeekActivity,
                               final String mTreatUUID)
    {
        this(mTreatWeekActivity, UUID.fromString(mTreatUUID));
    }

    private UUID getTreatUUID()
    {
        return mTreatUUID;
    }

    @Override
    protected Treat doInBackground(final Void... avoid)
    {
        return DatabaseUtility.selectTreat(
                super.getContentResolver(),
                mTreatUUID
        );
    }

    @Override
    protected void onPostExecute(final Treat treat)
    {
        final TreatWeekActivity treatWeekActivity = super.getActivity();
        if (treatWeekActivity == null || treatWeekActivity.isFinishing() || treatWeekActivity.isDestroyed()) {
            return;
        }
        treatWeekActivity.onAsyncTreatQueryResult(treat);
    }
}

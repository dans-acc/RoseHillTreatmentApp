package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteException;
import android.os.RemoteException;
import android.support.annotation.StringRes;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.TreatActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders.TreatBuilder;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders.TreatCreationBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AsyncTreatBuilderTask extends AsyncProviderTask<TreatActivity, Void, Void, Boolean>
{

    public interface AsyncTreatBuilderListener
    {
        void onAsyncTreatBuilderResult(final TreatBuilder treatBuilder, final boolean success);
    }

    private static final boolean SHOWS_PROGRESS_DIALOG = true;

    private final WeakReference<TreatBuilder> mWeakTreatBuilder;
    private final ArrayList<ContentProviderOperation> mTreatBuilderOperations;

    public AsyncTreatBuilderTask(final TreatActivity mTreatActivity,
                                 final TreatBuilder mTreatBuilder,
                                 final String mContentAuthority)
    {
        super(mTreatActivity, SHOWS_PROGRESS_DIALOG, mContentAuthority);

        this.mWeakTreatBuilder = new WeakReference<>(mTreatBuilder);
        this.mTreatBuilderOperations = mTreatBuilder.buildContentProviderOperations();

        @StringRes final int progressDialogMessage = mTreatBuilder instanceof TreatCreationBuilder
                ? R.string.dialog_message_please_wait_creating_treat
                : R.string.dialog_message_please_wait_saving_treat;

        super.setProgressDialogTitle(mTreatActivity.getString(R.string.dialog_title_please_wait));
        super.setProgressDialogMessage(mTreatActivity.getString(progressDialogMessage));
    }

    private WeakReference<TreatBuilder> getWeakTreatBuilder()
    {
        return mWeakTreatBuilder;
    }

    private TreatBuilder getTreatBuilder()
    {
        return mWeakTreatBuilder.get();
    }

    @Override
    protected Boolean doInBackground(final Void... avoid)
    {
        if (mTreatBuilderOperations == null || mTreatBuilderOperations.isEmpty()) {
            return false;
        }

        try {
            super.getContentResolver().applyBatch(
                    super.getContentAuthority(),
                    mTreatBuilderOperations
            );
            return true;
        } catch (final SQLiteException
                | OperationApplicationException
                | RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {
        super.dismissProgressDialog();
        final TreatActivity treatActivity = super.getActivity();
        if (treatActivity == null || treatActivity.isFinishing() || treatActivity.isDestroyed()) {
            return;
        }
        final TreatBuilder treatBuilder = getTreatBuilder();
        if (treatBuilder == null) {
            return;
        }
        treatActivity.onAsyncTreatBuilderResult(treatBuilder, success);
    }
}

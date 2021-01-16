package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;

import java.lang.ref.WeakReference;

@SuppressWarnings("WeakerAccess")
public abstract class AsyncProgressTask <T extends Activity, U, V, W>
        extends AsyncTask<U, V, W>
{

    private static final boolean SHOW_PROGRESS_DIALOG_BY_DEFAULT = false;

    private WeakReference<T> mWeakActivity;

    private boolean mShowProgressDialog;
    private String mProgressDialogTitle;
    private String mProgressDialogMessage;
    private WeakReference<ProgressDialog> mWeakProgressDialog;

    protected AsyncProgressTask(final T mActivity,
                                final boolean mShowProgressDialog)
    {
        this.mWeakActivity = new WeakReference<>(mActivity);

        this.mShowProgressDialog = mShowProgressDialog;
        this.mProgressDialogTitle = null;
        this.mProgressDialogMessage = null;
        this.mWeakProgressDialog = null;
    }

    protected AsyncProgressTask(final T mActivity)
    {
        this(mActivity, SHOW_PROGRESS_DIALOG_BY_DEFAULT);
    }

    protected WeakReference<T> getWeakActivity()
    {
        return mWeakActivity;
    }

    protected T getActivity()
    {
        return mWeakActivity.get();
    }

    protected boolean showsProgressDialog()
    {
        return mShowProgressDialog;
    }

    protected void setShowsProgressDialog(final boolean mShowProgressDialog)
    {
        this.mShowProgressDialog = mShowProgressDialog;
    }

    protected String getProgressDialogTitle()
    {
        return mProgressDialogTitle;
    }

    protected void setProgressDialogTitle(final String mProgressDialogTitle)
    {
        this.mProgressDialogTitle = mProgressDialogTitle;
    }

    protected String getProgressDialogMessage()
    {
        return mProgressDialogMessage;
    }

    protected void setProgressDialogMessage(final String mProgressDialogMessage)
    {
        this.mProgressDialogMessage = mProgressDialogMessage;
    }

    protected WeakReference<ProgressDialog> getWeakProgressDialog()
    {
        return mWeakProgressDialog;
    }

    protected ProgressDialog getProgressDialog()
    {
        return mWeakProgressDialog.get();
    }

    @Override
    protected void onPreExecute()
    {
        if (!mShowProgressDialog) {
            return;
        }

        if (mProgressDialogTitle == null || mProgressDialogMessage == null) {
            throw new IllegalArgumentException("Unable to display progress dialog: title ("
                    + mProgressDialogTitle + ") or message (" + mProgressDialogMessage + ") is null.");
        }

        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        final ProgressDialog progressDialog = DialogUtility.buildProgressDialog(
                activity,
                mProgressDialogTitle,
                mProgressDialogMessage,
                true
        );

        mWeakProgressDialog = new WeakReference<>(progressDialog);
    }

    protected void dismissProgressDialog()
    {
        if (mWeakProgressDialog == null) {
            return;
        }
        final ProgressDialog progressDialog = getProgressDialog();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

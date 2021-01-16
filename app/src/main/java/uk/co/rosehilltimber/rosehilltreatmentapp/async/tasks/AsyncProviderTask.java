package uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;

import java.lang.ref.WeakReference;

@SuppressWarnings("WeakerAccess")
public abstract class AsyncProviderTask<T extends Activity, U, V, W>
        extends AsyncProgressTask<T, U, V, W>
{

    private static final boolean SHOW_PROGRESS_DIALOG_BY_DEFAULT = false;

    private String mContentAuthority;
    private ContentResolver mContentResolver;

    protected AsyncProviderTask(final T mActivity,
                                final boolean mShowProgressDialog,
                                final String mContentAuthority)
    {
        super(mActivity, mShowProgressDialog);

        this.mContentAuthority = mContentAuthority;
        this.mContentResolver = mActivity.getContentResolver();
    }

    protected AsyncProviderTask(final T mActivity, final String mContentAuthority)
    {
        this(mActivity, SHOW_PROGRESS_DIALOG_BY_DEFAULT, mContentAuthority);
    }

    protected String getContentAuthority()
    {
        return mContentAuthority;
    }

    protected void setContentAuthority(final String mContentAuthority)
    {
        this.mContentAuthority = mContentAuthority;
    }

    protected ContentResolver getContentResolver()
    {
        return mContentResolver;
    }
}

package uk.co.rosehilltimber.rosehilltreatmentapp.async.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AsyncLoader<T> extends AsyncTaskLoader<T>
{

    private T mCachedData;
    private boolean mHasResult;

    @SuppressWarnings("WeakerAccess")
    protected AsyncLoader(final Context context)
    {
        super(context);
    }

    protected T getCachedData()
    {
        return mCachedData;
    }

    protected boolean hasResult()
    {
        return mHasResult;
    }

    @Override
    protected void onStartLoading()
    {
        if (super.takeContentChanged()) {
            super.forceLoad();
        }
    }

    @Override
    public void deliverResult(final T mCachedData)
    {
        this.mCachedData = mCachedData;
        mHasResult = this.mCachedData != null;
        super.deliverResult(mCachedData);
    }

    @Override
    protected void onReset()
    {
        super.onReset();
        super.onStopLoading();
        if (mHasResult) {
            onReleaseResources(mCachedData);
            mCachedData = null;
            mHasResult = false;
        }
    }

    protected void onReleaseResources(final T mCachedData) { }
}

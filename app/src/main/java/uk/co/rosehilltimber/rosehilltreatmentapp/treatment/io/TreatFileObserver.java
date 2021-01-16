package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io;

import android.app.Activity;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.File;
import java.lang.ref.WeakReference;

public class TreatFileObserver extends FileObserver
{

    // File directory being observed.
    private File mObservedDir;

    // The array adapter into which we display the files.
    private WeakReference<ListFragment> mWeakListFragment;
    private WeakReference<ArrayAdapter<File>> mWeakFileArrayAdapter;

    // The filter used to filter the files within the directory.
    private TreatFileFilter mTreatFileFilter;

    @SuppressWarnings("WeakerAccess")
    public TreatFileObserver(@NonNull final File mObservedDir,
                             @NonNull final WeakReference<ListFragment> mWeakListFragment,
                             @NonNull final WeakReference<ArrayAdapter<File>> mFileArrayAdapter,
                             @NonNull final TreatFileFilter mTreatFileFilter)
    {
        super(mObservedDir.getAbsolutePath(), FileObserver.ALL_EVENTS);

        this.mObservedDir = mObservedDir;
        this.mWeakListFragment = mWeakListFragment;
        this.mWeakFileArrayAdapter = mFileArrayAdapter;
        this.mTreatFileFilter = mTreatFileFilter;
    }

    public TreatFileObserver(@NonNull final File mObservedDir,
                             @NonNull final WeakReference<ListFragment> mWeakListFragment,
                             @NonNull final WeakReference<ArrayAdapter<File>> mWeakFileArrayAdapter)
    {
        this(
                mObservedDir,
                mWeakListFragment,
                mWeakFileArrayAdapter,
                new TreatFileFilter()
        );
    }

    public TreatFileObserver(@NonNull final File mObservedDir,
                             @NonNull final ListFragment mListFragment,
                             @NonNull final ArrayAdapter<File> mFileArrayAdapter)
    {
        this(
                mObservedDir,
                new WeakReference<>(mListFragment),
                new WeakReference<>(mFileArrayAdapter),
                new TreatFileFilter(false)
        );
    }

    @NonNull
    public File getObservedDir()
    {
        return mObservedDir;
    }

    public void setObservedDir(@NonNull final File mObservedDir)
    {
        this.mObservedDir = mObservedDir;
    }

    @NonNull
    public WeakReference<ListFragment> getWeakListFragment()
    {
        return mWeakListFragment;
    }

    public void setWeakListFragment(@NonNull final WeakReference<ListFragment> mWeakListFragment)
    {
        this.mWeakListFragment = mWeakListFragment;
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public ListFragment getListFragment()
    {
        return mWeakListFragment.get();
    }

    @SuppressWarnings("unused")
    @NonNull
    public WeakReference<ArrayAdapter<File>> getWeakExcelFileListFragment()
    {
        return mWeakFileArrayAdapter;
    }

    public void setWeakFileArrayAdapter(@NonNull final WeakReference<ArrayAdapter<File>> mWeakFileArrayAdapter)
    {
        this.mWeakFileArrayAdapter = mWeakFileArrayAdapter;
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public ArrayAdapter<File> getFileArrayAdapter()
    {
        return mWeakFileArrayAdapter.get();
    }

    @NonNull
    public TreatFileFilter getExcelFileFilter()
    {
        return mTreatFileFilter;
    }

    @Override
    public void onEvent(final int eventType, @Nullable final String filepath)
    {
        // Check that the list view and adapter exist. If not, stop watching - we're done.
        final ListFragment uiFileListFragment = getListFragment();
        final ArrayAdapter<File> uiFileArrayAdapter = getFileArrayAdapter();
        if (uiFileListFragment == null || uiFileArrayAdapter == null
                || uiFileArrayAdapter != uiFileListFragment.getListAdapter()
                || uiFileListFragment.isDetached() || uiFileListFragment.isRemoving()) {
            super.stopWatching();
            return;
        }

        // If the directory being observed is moved or deleted, we can no longer proceed.
        if (eventType == FileObserver.MOVE_SELF || eventType == FileObserver.DELETE_SELF) {
            super.stopWatching();
            throw new SecurityException(String.format(
                    "File (%s) observation stopped. Moved: %b; deleted: %b.",
                    mObservedDir.getPath(),
                    eventType == FileObserver.MOVE_SELF,
                    eventType == FileObserver.DELETE_SELF
            ));
        } else if (eventType == FileObserver.ATTRIB && !mObservedDir.canRead()) {
            super.stopWatching();
            throw new SecurityException(String.format(
                    "File %s can no longer be read",
                    mObservedDir.getPath()
            ));
        }

        // We cannot proceed if the filename is null.
        if (filepath == null) {
            return;
        }

        // Get the list fragments activity - required for running within the UI thread.
        final Activity activity = uiFileListFragment.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            super.stopWatching();
            return;
        }

        // Handle the updating of the excel list fragment within the UI thread to create synchronisation.
        activity.runOnUiThread(() -> {
            final File file = new File(mObservedDir, filepath);
            switch (eventType) {
                case FileObserver.CREATE:
                case FileObserver.MOVED_TO:
                    if (!mTreatFileFilter.accept(file)) {
                        return;
                    }
                    uiFileArrayAdapter.add(file);
                    uiFileArrayAdapter.notifyDataSetChanged();
                    break;
                case FileObserver.DELETE:
                case FileObserver.MOVED_FROM:
                    if (!mTreatFileFilter.accept(file)) {
                        return;
                    }
                    uiFileArrayAdapter.remove(file);
                    uiFileArrayAdapter.notifyDataSetChanged();
                    break;
                case FileObserver.MODIFY:
                    uiFileArrayAdapter.notifyDataSetChanged();
            }
        });
    }
}

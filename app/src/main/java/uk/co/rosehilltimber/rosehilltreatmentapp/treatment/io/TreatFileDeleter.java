package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class TreatFileDeleter extends AsyncTask<Void, Void, Integer>
{

    private final WeakReference<Activity> mWeakActivity;

    private final File[] mFilesToDelete;

    @SuppressWarnings("WeakerAccess")
    public TreatFileDeleter(final WeakReference<Activity> mWeakActivity,
                            final File... mFilesToDelete)
    {
        this.mWeakActivity = mWeakActivity;
        this.mFilesToDelete = mFilesToDelete;
    }

    public TreatFileDeleter(final Activity mActivity,
                            final File... mFilesToDelete)
    {
        this(new WeakReference<>(mActivity), mFilesToDelete);
    }

    public final WeakReference<Activity> getWeakActivity()
    {
        return mWeakActivity;
    }

    public final Activity getActivity()
    {
        return mWeakActivity.get();
    }

    public final File[] getFiles()
    {
        return mFilesToDelete;
    }

    @Override
    public Integer doInBackground(@NonNull Void... avoid)
    {
        int successfulDeletions = 0;
        for (final File file : mFilesToDelete) {
            if (file.delete()) {
                ++successfulDeletions;
            }
        }
        return successfulDeletions;
    }

    @Override
    public void onPostExecute(@NonNull final Integer successfulDeletions)
    {
        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        // Prompt the user with the most appropriate message.
        if (successfulDeletions == mFilesToDelete.length) {
            Toast.makeText(
                    activity,
                    R.string.toast_file_successfully_deleted,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            DialogUtility.buildOkAlertDialog(
                    activity,
                    R.string.dialog_title_failed_to_delete_file,
                    R.string.dialog_message_failed_to_delete_file
            );
        }

        super.onPostExecute(successfulDeletions);
    }

}

package uk.co.rosehilltimber.rosehilltreatmentapp.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.microsoft.onedrivesdk.saver.ISaver;
import com.microsoft.onedrivesdk.saver.Saver;
import com.microsoft.onedrivesdk.saver.SaverException;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.adapters.FileListAdapter;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.TreatFileDeleter;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.TreatFileFilter;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io.TreatFileObserver;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.SharedPreferenceUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.StorageUtility;

public class FileLogFragment extends ListFragment implements View.OnClickListener
{

    public static final IntentFilter TREAT_FILE_OBSERVER_INTENT_FILTER;

    static {
        TREAT_FILE_OBSERVER_INTENT_FILTER = new IntentFilter();
        TREAT_FILE_OBSERVER_INTENT_FILTER.addAction(Intent.ACTION_MEDIA_REMOVED);
        TREAT_FILE_OBSERVER_INTENT_FILTER.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        TREAT_FILE_OBSERVER_INTENT_FILTER.addAction(Intent.ACTION_MEDIA_EJECT);
        TREAT_FILE_OBSERVER_INTENT_FILTER.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        TREAT_FILE_OBSERVER_INTENT_FILTER.addAction(Intent.ACTION_MEDIA_MOUNTED);
    }

    private String mMicrosoftOneDriveAppID;
    private ISaver mMicrosoftOneDriveSaver;

    private LocalDate mStartOfFinancialYear;
    private LocalDate mEndOfFinancialYear;

    private List<File> mFiles;
    private FileListAdapter mFileListAdapter;

    private TreatFileObserver mFileObserver;
    private BroadcastReceiver mExternalMediaBroadcastReceiver;

    public FileLogFragment()
    {
        // Empty constructor - necessary for all fragments.
    }

    @NonNull
    public static FileLogFragment newInstance()
    {
        return new FileLogFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Check that the activity exists.
        final FragmentActivity fragmentActivity = super.getActivity();
        if (fragmentActivity == null) {
            return;
        }

        // Get the necessary shared preferences.
        final SharedPreferenceUtility sharedPreferenceUtility = SharedPreferenceUtility.getInstance(fragmentActivity);
        mMicrosoftOneDriveAppID = sharedPreferenceUtility.getOneDriveAppID();
        mStartOfFinancialYear = sharedPreferenceUtility.getStartOfFinancialYear();
        mEndOfFinancialYear = sharedPreferenceUtility.getEndOfFinancialYear();

        // Create the microsoft one driver instance.
        mMicrosoftOneDriveSaver = Saver.createSaver(mMicrosoftOneDriveAppID);

        // Create the file list adapter.
        mFiles = new ArrayList<>();
        mFileListAdapter = new FileListAdapter(
                fragmentActivity,
                this,
                mFiles,
                mStartOfFinancialYear,
                mEndOfFinancialYear
        );
        mFileListAdapter.setNotifyOnChange(true);
        super.setListAdapter(mFileListAdapter);

        initExternalMediaBroadcastReceiver();
        mFileObserver = initTreatFileObserver();
        if (mFileObserver != null) {
            mFileObserver.startWatching();
        }
    }

    private void initExternalMediaBroadcastReceiver()
    {
        mExternalMediaBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    return;
                }

                final FragmentActivity fragmentActivity = FileLogFragment.this.getActivity();
                if (fragmentActivity == null || fragmentActivity.isFinishing() || fragmentActivity.isDestroyed()) {
                    return;
                }

                switch (intent.getAction()) {

                    case Intent.ACTION_MEDIA_REMOVED:
                    case Intent.ACTION_MEDIA_BAD_REMOVAL:
                    case Intent.ACTION_MEDIA_EJECT:
                    case Intent.ACTION_MEDIA_UNMOUNTED:
                        if (FileLogFragment.this.mFileObserver == null) {
                            return;
                        }
                        FileLogFragment.this.mFileObserver.stopWatching();
                        FileLogFragment.this.mFileObserver = null;
                        mFiles.clear();
                        mFileListAdapter.notifyDataSetChanged();
                        DialogUtility.buildOkAlertDialog(
                                fragmentActivity,
                                R.string.dialog_title_media_unmounted,
                                R.string.dialog_message_media_unmounted
                        );
                        break;

                    case Intent.ACTION_MEDIA_MOUNTED:
                        if (FileLogFragment.this.mFileObserver != null) {
                            FileLogFragment.this.mFileObserver.startWatching();
                            return;
                        }
                        FileLogFragment.this.mFileObserver = initTreatFileObserver();
                        if (FileLogFragment.this.mFileObserver != null) {
                            FileLogFragment.this.mFileObserver.startWatching();
                            mFiles.addAll(new ArrayList<>(Arrays.asList(StorageUtility.getInstance(fragmentActivity)
                                    .getExternalXLSDirectory().listFiles(new TreatFileFilter()))));
                            mFileListAdapter.notifyDataSetChanged();
                        }
                        break;

                }
            }
        };
    }

    private TreatFileObserver initTreatFileObserver()
    {
        final FragmentActivity fragmentActivity = FileLogFragment.this.getActivity();
        if (fragmentActivity == null || fragmentActivity.isFinishing() || fragmentActivity.isDestroyed()) {
            return null;
        }

        final StorageUtility storageUtility = StorageUtility.getInstance(fragmentActivity);
        final File targetFile = storageUtility.getExternalXLSDirectory();
        if (targetFile == null) {
            return null;
        }
        return new TreatFileObserver(
                targetFile,
                this,
                mFileListAdapter
        );
    }

    @Override
    public void onResume()
    {
        final FragmentActivity fragmentActivity = super.getActivity();
        if (fragmentActivity == null || fragmentActivity.isFinishing() || fragmentActivity.isDestroyed()) {
            unregisterListeners();
            return;
        }

        fragmentActivity.registerReceiver(mExternalMediaBroadcastReceiver, TREAT_FILE_OBSERVER_INTENT_FILTER);

        final File targetDirectory = StorageUtility.getInstance(fragmentActivity).getExternalXLSDirectory();
        if (targetDirectory == null) {
            DialogUtility.buildOkAlertDialog(
                    fragmentActivity,
                    R.string.dialog_title_treat_directory_not_found,
                    R.string.dialog_message_treat_directory_not_found
            );
            return;
        }

        mFiles.clear();;
        mFiles.addAll(new ArrayList<>(Arrays.asList(StorageUtility.getInstance(fragmentActivity)
                .getExternalXLSDirectory().listFiles(new TreatFileFilter()))));
        mFileListAdapter.notifyDataSetChanged();

        if (mFileObserver == null) {
            mFileObserver = initTreatFileObserver();
        }

        if (mFileObserver != null) {
            mFileObserver.startWatching();
        }

        super.onResume();
    }

    @Override
    public void onPause()
    {
        unregisterListeners();
        super.onPause();
    }

    private void unregisterListeners()
    {
        final FragmentActivity fragmentActivity = super.getActivity();
        if (fragmentActivity != null) {
            fragmentActivity.unregisterReceiver(mExternalMediaBroadcastReceiver);
        }
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    @Override
    public void onClick(final View view)
    {
        if (view == null) {
            return;
        } else if (view.getId() != R.id.item_file_iv_backup_action
                && view.getId() != R.id.item_file_iv_delete_option) {
            return;
        }

        final File file = getFileFromViewTagPosition(view);
        if (view.getId() == R.id.item_file_iv_backup_action) {
            onUploadFileClicked(file);
        } else {
            onDeleteFileClicked(file);
        }
    }

    public File getFileFromViewTagPosition(final View view)
    {
        if (view == null) {
            return null;
        }

        try {
            final Object tag = view.getTag();
            if (tag == null) {
                return null;
            }
            final int position = (int) tag;
            if (position < 0 || position >= mFiles.size()) {
                return null;
            }
            return mFiles.get(position);
        } catch (final ClassCastException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            mFileListAdapter.notifyDataSetChanged();
        }

        return null;
    }

    private void onUploadFileClicked(final File file)
    {
        final Activity activity = super.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        if (mMicrosoftOneDriveAppID == null || mMicrosoftOneDriveAppID.isEmpty()) {
            DialogUtility.buildOkAlertDialog(
                    activity,
                    R.string.dialog_title_one_drive_undefined_or_invalid_app_id,
                    R.string.dialog_message_one_drive_undefined_or_invalid_app_id
            );
            return;
        }

        mMicrosoftOneDriveSaver.startSaving(
                activity,
                file.getName(),
                Uri.fromFile(file)
        );
    }

    private void onDeleteFileClicked(final File file)
    {
        final TreatFileDeleter treatFileDeleter = new TreatFileDeleter(
                super.getActivity(),
                file
        );
        treatFileDeleter.execute();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        SaverException saverException = null;
        try {
            mMicrosoftOneDriveSaver.handleSave(requestCode, resultCode, data);
        } catch (final SaverException e) {

            Log.e(mMicrosoftOneDriveSaver.getClass().getSimpleName(), e.getErrorType().toString());
            Log.d(mMicrosoftOneDriveSaver.getClass().getSimpleName(), e.getDebugErrorInfo());
            e.printStackTrace();

            saverException = e;
        }

        // Display the saver exception result to the user.
        showMicrosoftSaverResult(saverException);
    }

    private void showMicrosoftSaverResult(final SaverException saverException)
    {
        final FragmentActivity activity = super.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        if (saverException == null) {
            Toast.makeText(
                    activity,
                    R.string.toast_file_upload_successful,
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        switch (saverException.getErrorType()) {
            case Cancelled:
                Toast.makeText(
                        activity,
                        R.string.toast_file_upload_cancelled,
                        Toast.LENGTH_SHORT
                ).show();
                return;
            case OutOfQuota:
                DialogUtility.buildOkAlertDialog(
                        activity,
                        R.string.dialog_title_one_drive_out_of_quota,
                        R.string.dialog_message_one_drive_out_of_quota
                );
                return;
            case InvalidFileName:
                DialogUtility.buildOkAlertDialog(
                        activity,
                        R.string.dialog_title_one_drive_invalid_file_name,
                        R.string.dialog_message_one_drive_invalid_file_name
                );
                return;
            case NoNetworkConnectivity:
                DialogUtility.buildOkAlertDialog(
                        activity,
                        R.string.dialog_title_one_drive_no_network_connection,
                        R.string.dialog_message_one_drive_no_network_connection
                );
                return;
            case CouldNotAccessFile:
                DialogUtility.buildOkAlertDialog(
                        activity,
                        R.string.dialog_title_one_drive_cant_access_file,
                        R.string.dialog_message_one_drive_cant_access_file
                );
                return;
            case NoFileSpecified:
                DialogUtility.buildOkAlertDialog(
                        activity,
                        R.string.dialog_title_one_drive_no_file_specified,
                        R.string.dialog_message_one_drive_no_file_specified
                );
                return;
            default:
                DialogUtility.buildOkAlertDialog(
                        activity,
                        R.string.dialog_title_one_drive_unknown,
                        R.string.dialog_message_one_drive_unknown
                );
        }
    }
}

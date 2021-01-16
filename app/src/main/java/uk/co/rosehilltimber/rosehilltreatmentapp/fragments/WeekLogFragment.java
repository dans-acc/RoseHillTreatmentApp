package uk.co.rosehilltimber.rosehilltreatmentapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.LogActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.TreatWeekActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.activities.PriorWeekActivity;
import uk.co.rosehilltimber.rosehilltreatmentapp.adapters.WeekListAdapter;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncTreatFileWriterTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.loaders.AsyncWeekLoader;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.*;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeekLogFragment extends Fragment
        implements View.OnClickListener,
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<Week>>
{

    // Keys used to pass data into the fragment.
    private static class BundleKey
    {
        private static final String CURRENT_WEEK = "CURRENT_WEEK_INSTANCE";
    }

    // The view holder permits consistent and faster access to the current week.
    private static class CurrentWeekItemViewHolder
    {
        private TextView mDateTextViewField;
        private TextView mWeekTextViewField;
        private TextView mTreatsTextViewField;
        private ImageButton mDownloadImageButton;
    }

    private static final int WEEK_LOADER_ID = 0;

    private static final boolean DEFAULT_ATTACH_TO_ROOT = false;

    private LocalDate mStartOfFinancialYear;
    private LocalDate mEndOfFinancialYear;

    private View mCurrentWeekItemView;
    private CurrentWeekItemViewHolder mCurrentWeekItemViewHolder;

    private Week mCurrentWeek;

    // All weeks within the database.
    private ArrayList<Week> mWeeks;
    private WeekListAdapter mWeekListAdapter;
    private ListView mWeekListView;

    public WeekLogFragment() {
        // Mandatory empty constructor used for the creation of fragments.
    }

    @NonNull
    public static WeekLogFragment newInstance(final Week week)
    {
        // Set the week log fragment arguments.
        final Bundle bundleArguments = new Bundle();
        bundleArguments.putParcelable(BundleKey.CURRENT_WEEK, week);

        // Create and return the week log fragment.
        final WeekLogFragment weekLogFragment = new WeekLogFragment();
        weekLogFragment.setArguments(bundleArguments);
        return weekLogFragment;
    }

    @NonNull
    public static WeekLogFragment newInstance()
    {
        return new WeekLogFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Check whether or not the activity is finishing.
        final Activity activity = super.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        // Use the shared preferences in order to obtain the start and end dates of the financial year.
        final SharedPreferenceUtility sharedPreferenceUtility = SharedPreferenceUtility.getInstance(activity);
        mStartOfFinancialYear = sharedPreferenceUtility.getStartOfFinancialYear();
        mEndOfFinancialYear = sharedPreferenceUtility.getEndOfFinancialYear();

        // Use the bundle arguments in order to obtain the current working week.
        final Bundle bundleArguments = super.getArguments();
        if (bundleArguments != null && bundleArguments.containsKey(BundleKey.CURRENT_WEEK)) {
            mCurrentWeek = bundleArguments.getParcelable(BundleKey.CURRENT_WEEK);
        } else {
            final LocalDate mCurrentWeekDate = sharedPreferenceUtility.getCurrentWorkingWeek();
            if (mCurrentWeekDate != null) {
                mCurrentWeek = new Week(mCurrentWeekDate);
            }
        }

        // Create the adapter responsible for rendering week items within the list view.
        mWeeks = new ArrayList<>();
        mWeekListAdapter = new WeekListAdapter(
                activity,
                this,
                mWeeks,
                mStartOfFinancialYear,
                mEndOfFinancialYear
        );
        mWeekListAdapter.setNotifyOnChange(true);

        initWeekLoader();
    }

    private void initWeekLoader()
    {
        final FragmentActivity activity = super.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        final LoaderManager loaderManager = activity.getSupportLoaderManager();
        final Loader<List<Week>> loader = loaderManager.getLoader(WEEK_LOADER_ID);
        if (loader == null) {
            loaderManager.initLoader(WEEK_LOADER_ID, null, this)
                    .forceLoad();
        } else {
            loaderManager.restartLoader(WEEK_LOADER_ID, null, this);
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull final LayoutInflater layoutInflater, @Nullable final ViewGroup viewGroup,
                             @Nullable final Bundle savedInstanceState) {

        // Inflate the fragment view responsible for displaying the weekly log.
        final View view = layoutInflater.inflate(R.layout.fragment_week_log, viewGroup, DEFAULT_ATTACH_TO_ROOT);

        // Init the current week view (and view holder).
        mCurrentWeekItemView = view.findViewById(R.id.activity_current_week_item_week_summary);
        mCurrentWeekItemViewHolder = new CurrentWeekItemViewHolder();
        mCurrentWeekItemViewHolder.mDateTextViewField = mCurrentWeekItemView.findViewById(R.id.item_week_tv_date_field);
        mCurrentWeekItemViewHolder.mWeekTextViewField = mCurrentWeekItemView.findViewById(R.id.item_week_tv_week_number_field);
        mCurrentWeekItemViewHolder.mTreatsTextViewField = mCurrentWeekItemView.findViewById(R.id.item_week_tv_treatment_count_field);
        mCurrentWeekItemViewHolder.mDownloadImageButton = mCurrentWeekItemView.findViewById(R.id.item_week_ib_download_action);

        // Register the current week view on click listeners.
        mCurrentWeekItemView.setOnClickListener(this);
        mCurrentWeekItemViewHolder.mDownloadImageButton.setOnClickListener(this);

        // Get the list view, init the list view and set it's adapter.
        mWeekListView = view.findViewById(R.id.fragment_week_log_lv_weeks);
        mWeekListView.setAdapter(mWeekListAdapter);
        mWeekListView.setOnItemClickListener(this);

        // Update the current week fields.
        updateCurrentWeekItemViewFields();
        return view;
    }

    private void updateCurrentWeekItemViewFields()
    {
        if (mCurrentWeek == null) {
            mCurrentWeekItemView.setVisibility(View.GONE);
            return;
        }

        // Set the current week date fields - display errors if required.
        final LocalDate weekDate = mCurrentWeek.getDate();
        if (mStartOfFinancialYear != null && mEndOfFinancialYear != null && DateUtility.isWithin(weekDate, mStartOfFinancialYear, mEndOfFinancialYear)) {
            mCurrentWeekItemViewHolder.mDateTextViewField.setText(weekDate.format(DateUtility.BASIC_DATE_FORMATTER));
        } else {
            mCurrentWeekItemViewHolder.mDateTextViewField.setText(super.getString(
                    R.string.week_date_out_of_bounds,
                    weekDate.format(DateUtility.BASIC_DATE_FORMATTER)
            ));
        }

        // Display the week number of the current week relative tot he start of the financial year.
        if (mStartOfFinancialYear == null) {
            mCurrentWeekItemViewHolder.mWeekTextViewField.setText(R.string.week_number_not_applicable);
        } else {
            final long weekNumber = TreatUtility.weekNumber(mStartOfFinancialYear, weekDate);
            mCurrentWeekItemViewHolder.mWeekTextViewField.setText(String.valueOf(weekNumber));
        }

        // Display the number of treatments within the current week.
        mCurrentWeekItemViewHolder.mTreatsTextViewField.setText(String.valueOf(mCurrentWeek.getTreatments().size()));

        if (mCurrentWeekItemView.getVisibility() == View.GONE) {
            mCurrentWeekItemView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view,
                            final int position, final long id)
    {
        // In order to view prior week data, the activity must be open.
        final Activity activity = super.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        } else if (view == null) {
            return;
        } else if (position < 0 || position >= mWeeks.size()) {
            return;
        }

        final Week week = mWeeks.get(position);
        if (week == null) {
            return;
        }

        // Start the prior-week activity for displaying the selected items treatments.
        final Intent priorWeekActivity = new Intent(activity, PriorWeekActivity.class);
        priorWeekActivity.putExtra(TreatWeekActivity.IntentKey.WEEK_INSTANCE, week);
        super.startActivityForResult(priorWeekActivity, PriorWeekActivity.RequestCode.UPDATED_WEEK_INSTANCE);
    }

    @Override
    public void onClick(final View view)
    {
        if (view == null) {
            return;
        }

        // Check if the current week has been clicked - resulting in the log activity closure.
        if (view == mCurrentWeekItemView) {
            final Activity activity = super.getActivity();
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                super.getActivity().onBackPressed();
            }
            return;
        }

        // Attempt to obtain the week instance that's to be written to the file.
        final Week week;
        if (view == mCurrentWeekItemViewHolder.mDownloadImageButton) {
            week = mCurrentWeek;
        } else if (view.getId() == R.id.item_week_ib_download_action) {
            week = mWeekListAdapter.getItemFromViewTagPosition(view);
        } else {
            return;
        }

        // If the week is not null, write it's data to the directory.
        if (week != null) {
            writeWeekFile(week);
        }
    }

    private void writeWeekFile(final Week week)
    {
        // The activity must be alive in order to write a file.
        final FragmentActivity fragmentActivity = super.getActivity();
        if (fragmentActivity == null || fragmentActivity.isFinishing() || fragmentActivity.isDestroyed()) {
            return;
        } else if (week == null) {
            return;
        }

        // Ensure that the financial year is set. This is mandatory in order to export week data.
        if (mStartOfFinancialYear == null || mEndOfFinancialYear == null) {
            DialogUtility.buildOkAlertDialog(
                    fragmentActivity,
                    R.string.dialog_title_undefined_financial_year,
                    R.string.dialog_message_undefined_financial_year
            );
            return;
        }

        // Get the directory into which we're writing the week file.
        final File storageDirectory = StorageUtility.getInstance(fragmentActivity).getExternalXLSDirectory();
        if (storageDirectory == null) {
            DialogUtility.buildOkAlertDialog(
                    fragmentActivity,
                    R.string.dialog_title_treat_directory_not_found,
                    R.string.dialog_message_treat_directory_not_found
            );
            return;
        }

        // Finally, begin a new async task for writing the week data to files - can't block the UI thread.
        final AsyncTreatFileWriterTask asyncTreatFileWriterTask = new AsyncTreatFileWriterTask(
                fragmentActivity,
                mStartOfFinancialYear,
                mEndOfFinancialYear,
                storageDirectory,
                week
        );
        asyncTreatFileWriterTask.execute();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        if (resultCode != Activity.RESULT_OK) {
            return;
        } else if (data == null) {
            return;
        }

        if (requestCode != PriorWeekActivity.RequestCode.UPDATED_WEEK_INSTANCE) {
            return;
        }

        final Week updatedWeek = data.getParcelableExtra(PriorWeekActivity.IntentKey.WEEK_INSTANCE);
        if (updatedWeek == null) {
            return;
        }

        addOrReplaceTreat(updatedWeek);
        notifyWeekLoaderContentChanged();
    }

    private void addOrReplaceTreat(final Week updatedWeek)
    {
        for (final Week week : mWeeks) {
            if (week.getDate().isEqual(updatedWeek.getDate())) {
                mWeeks.remove(week);
                break;
            }
        }
        if (updatedWeek.getTreatments().isEmpty()) {
            mWeekListAdapter.notifyDataSetChanged();
            return;
        }
        mWeeks.add(updatedWeek);
        mWeekListAdapter.notifyDataSetChanged();
    }

    private void notifyWeekLoaderContentChanged()
    {
        final FragmentActivity fragmentActivity = super.getActivity();
        if (fragmentActivity == null || fragmentActivity.isFinishing() || fragmentActivity.isDestroyed()) {
            return;
        }

        final LoaderManager loaderManager = fragmentActivity.getSupportLoaderManager();
        final Loader<List<Week>> loader = loaderManager.getLoader(WEEK_LOADER_ID);
        if (loader != null) {
            loader.onContentChanged();
        }
    }

    @NonNull
    @Override
    public Loader<List<Week>> onCreateLoader(final int id, final Bundle bundleArgs)
    {
        return new AsyncWeekLoader(WeekLogFragment.this.getContext());
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<List<Week>> loader,
                               final List<Week> data)
    {
        if (data == null) {
            return;
        }

        if (mCurrentWeek != null) {
            final Week week = TreatUtility.getWeekByDate(data, mCurrentWeek.getDate());
            if (week != null) {
                mCurrentWeek = week;
                updateCurrentWeekItemViewFields();
                data.remove(week);
            }
        }

        mWeeks.clear();
        mWeeks.addAll(data);
        mWeekListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<List<Week>> loader)
    {
        mWeeks.clear();
    }
}

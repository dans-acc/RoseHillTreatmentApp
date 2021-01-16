package uk.co.rosehilltimber.rosehilltreatmentapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.*;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.adapters.TreatListAdapter;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.loaders.AsyncTreatLoader;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncTreatDeleterTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncTreatQueryTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.SharedPreferenceUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class TreatWeekActivity extends AppCompatActivity implements
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        AsyncTreatQueryTask.AsyncTreatQueryListener,
        AsyncTreatDeleterTask.AsyncTreatDeleterListener,
        LoaderManager.LoaderCallbacks<List<Treat>>
{

    public static class IntentKey
    {
        public static final String WEEK_INSTANCE = "WEEK_INSTANCE";
    }

    @SuppressWarnings("WeakerAccess")
    protected static class WeekSummaryViewHolder
    {
        protected LinearLayout mTreatsTextViewFieldLayout;
        protected LinearLayout mVolumeTextViewFieldLayout;
        protected LinearLayout mUndefinedWeekMessageLayout;

        protected TextView mTreatsTextViewField;
        protected TextView mVolumeTextViewField;

        protected Button mCompleteWeekButton;
    }

    protected static final int TREAT_LOADER_ID = 0;

    protected SharedPreferenceUtility mSharedPreferenceUtility;

    protected LocalDate mStartOfFinancialYear;
    protected LocalDate mEndOfFinancialYear;

    protected LocalDate mWeekDate;

    protected Toolbar mToolbar;

    protected RelativeLayout mWeekSummaryLayout;
    protected WeekSummaryViewHolder mWeekSummaryViewHolder;

    protected List<Treat> mTreats;
    protected TreatListAdapter mTreatListAdapter;
    protected ListView mTreatListView;

    protected Button mCreateNewButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_week);

        mSharedPreferenceUtility = SharedPreferenceUtility.getInstance(this);
        mStartOfFinancialYear = mSharedPreferenceUtility.getStartOfFinancialYear();
        mEndOfFinancialYear = mSharedPreferenceUtility.getEndOfFinancialYear();

        if (mStartOfFinancialYear == null || mEndOfFinancialYear == null) {
            return;
        }

        final Intent intent = super.getIntent();
        final Week week = intent.getParcelableExtra(IntentKey.WEEK_INSTANCE);
        if (week != null) {
            mWeekDate = week.getDate();
            mTreats = week.getTreatments();
        }

        mToolbar = super.findViewById(R.id.activity_base_week_toolbar);
        mToolbar.setTitle(super.getTitle());
        super.setSupportActionBar(mToolbar);

        final ActionBar supportActionBar = super.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
            supportActionBar.setDisplayShowTitleEnabled(true);
        }

        if (mTreats == null) {
            mTreats = new ArrayList<>();
        }
        mTreatListAdapter = new TreatListAdapter(
                this,
                this,
                mTreats
        );
        mTreatListAdapter.setNotifyOnChange(true);

        mTreatListView = super.findViewById(R.id.activity_base_week_lv_treats);
        mTreatListView.setAdapter(mTreatListAdapter);
        mTreatListView.setOnItemClickListener(this);

        mCreateNewButton = super.findViewById(R.id.activity_base_week_btn_create_new);
        mCreateNewButton.setOnClickListener(this);

        initWeekSummaryViewHolder();

        showWeekSummaryFields(mWeekDate != null);
        showWeekSummaryCompleteButton(false);

        showCreateNewButton(false);
    }

    private void initWeekSummaryViewHolder()
    {
        mWeekSummaryLayout = super.findViewById(R.id.activity_base_week_layout_summary);
        mWeekSummaryViewHolder = new WeekSummaryViewHolder();

        mWeekSummaryViewHolder.mTreatsTextViewFieldLayout = mWeekSummaryLayout
                .findViewById(R.id.activity_base_week_layout_summary_treat_count);
        mWeekSummaryViewHolder.mVolumeTextViewFieldLayout = mWeekSummaryLayout
                .findViewById(R.id.activity_base_week_layout_summary_week_total_volume);
        mWeekSummaryViewHolder.mUndefinedWeekMessageLayout = mWeekSummaryLayout
                .findViewById(R.id.activity_base_week_layout_summary_undefined_week_message);

        mWeekSummaryViewHolder.mTreatsTextViewField = mWeekSummaryViewHolder.mTreatsTextViewFieldLayout
                .findViewById(R.id.activity_base_week_tv_summary_treat_count_field);
        mWeekSummaryViewHolder.mVolumeTextViewField = mWeekSummaryViewHolder
                .mVolumeTextViewFieldLayout.findViewById(R.id.activity_base_week_tv_summary_week_total_volume_field);
        mWeekSummaryViewHolder.mCompleteWeekButton = mWeekSummaryLayout
                .findViewById(R.id.activity_base_week_ib_summary_complete_week_action);

        mWeekSummaryViewHolder.mCompleteWeekButton.setOnClickListener(this);
    }

    protected void showWeekSummaryFields(final boolean showWeekSummaryFields)
    {
        if (showWeekSummaryFields) {
            mWeekSummaryViewHolder.mTreatsTextViewFieldLayout.setVisibility(View.VISIBLE);
            mWeekSummaryViewHolder.mVolumeTextViewFieldLayout.setVisibility(View.VISIBLE);
            mWeekSummaryViewHolder.mUndefinedWeekMessageLayout.setVisibility(View.GONE);
            updateWeekSummaryFields();
        } else {
            mWeekSummaryViewHolder.mTreatsTextViewFieldLayout.setVisibility(View.GONE);
            mWeekSummaryViewHolder.mVolumeTextViewFieldLayout.setVisibility(View.GONE);
            mWeekSummaryViewHolder.mUndefinedWeekMessageLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void updateWeekSummaryFields()
    {
        mWeekSummaryViewHolder.mTreatsTextViewField.setText(String.valueOf(mTreats.size()));
        mWeekSummaryViewHolder.mVolumeTextViewField.setText(super.getString(R.string.volume_format, TreatUtility.getTotalWeekVolume(mTreats)));
    }

    protected void showWeekSummaryCompleteButton(final boolean showWeekSummaryCompleteButton)
    {
        if (showWeekSummaryCompleteButton) {
            mWeekSummaryViewHolder.mCompleteWeekButton.setVisibility(View.VISIBLE);
        } else {
            mWeekSummaryViewHolder.mCompleteWeekButton.setVisibility(View.GONE);
        }
    }

    protected void showCreateNewButton(@StringRes final int createButtonText)
    {
        mCreateNewButton.setText(createButtonText);
        mCreateNewButton.setVisibility(View.VISIBLE);
    }

    protected void showCreateNewButton(final boolean showCreateNewButton)
    {
        if (showCreateNewButton) {
            mCreateNewButton.setVisibility(View.VISIBLE);
        } else {
            mCreateNewButton.setVisibility(View.GONE);
        }
    }

    protected void initTreatLoader()
    {
        // The week for which we're loading the data.
        final Bundle loaderArguments = new Bundle();
        loaderArguments.putLong(
                AsyncTreatLoader.BundleKey.WEEK_DATE_TIMESTAMP,
                DateUtility.toEpochMilli(mWeekDate)
        );

        // Initialise the current week treat loader.
        final LoaderManager loaderManager =super.getSupportLoaderManager();
        final Loader<List<Treat>> loader = loaderManager.getLoader(TREAT_LOADER_ID);
        if (loader == null) {
            loaderManager.initLoader(TREAT_LOADER_ID, loaderArguments, this).forceLoad();
        } else {
            loaderManager.restartLoader(TREAT_LOADER_ID, loaderArguments, this);
        }
    }

    protected void destroyTreatLoader()
    {
        final LoaderManager loaderManager = super.getSupportLoaderManager();
        final Loader loader = loaderManager.getLoader(TREAT_LOADER_ID);
        if (loader != null) {
            loaderManager.destroyLoader(TREAT_LOADER_ID);
        }
    }

    @Override
    public void onClick(final View view)
    {
        if (super.isFinishing() || super.isDestroyed()) {
            return;
        } else if (view == null) {
            return;
        }

        if (view == mCreateNewButton && view.getId() == mCreateNewButton.getId()) {
            onCreateNewButtonClicked(view);
            return;
        } else if (view.getId() == mWeekSummaryViewHolder.mCompleteWeekButton.getId()
                && view == mWeekSummaryViewHolder.mCompleteWeekButton) {
            onCompleteWeekButtonClicked(view);
            return;
        }

        final Treat treat = mTreatListAdapter.getItemFromViewTagPosition(view);
        if (treat == null) {
            return;
        }
        onTreatDeleteButtonClicked(treat, view);
    }

    protected abstract void onCompleteWeekButtonClicked(final View view);

    protected abstract void onCreateNewButtonClicked(final View view);

    protected void onCreateNewTreatButtonClicked()
    {
        if (mWeekDate == null) {
            return;
        }
        DialogUtility.buildSelectionDialog(
                this,
                R.string.dialog_title_select_treat_type,
                TreatType.getTreatTypeNames(),
                (dialog, which) -> {
                    dialog.dismiss();
                    startTreatActivity(mWeekDate, TreatType.values()[which]);
                }
        );
    }

    protected abstract void onTreatDeleteButtonClicked(final Treat treat, final View view);

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view,
                            final int position, final long id)
    {
        if (isFinishing() || isDestroyed()) {
            return;
        } else if (view == null) {
            return;
        } else if (position < 0 || position >= mTreats.size()) {
            return;
        }
        final Treat treat = mTreats.get(position);
        if (treat != null) {
            onTreatItemClicked(treat);
        }
    }

    protected abstract void onTreatItemClicked(final Treat treat);

    protected abstract void startTreatActivity(final LocalDate weekDate, final TreatType treatType);

    protected abstract void startTreatActivity(final Treat treat);

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LogActivity.RequestCode.UPDATE_CURRENT_WEEK) {
            notifyTreatLoaderContentChanged();
            return;
        } else if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {

            case TreatActivity.RequestCode.TREAT_UUID:

                final String newTreatStringUUID = data.getStringExtra(TreatActivity.IntentKey.CREATED_TREAT_UUID);
                if (newTreatStringUUID == null || newTreatStringUUID.isEmpty()) {
                    return;
                }

                final AsyncTreatQueryTask treatQuery = new AsyncTreatQueryTask(this, newTreatStringUUID);
                treatQuery.execute();

                Toast.makeText(
                        this,
                        R.string.toast_treat_successfully_created,
                        Toast.LENGTH_LONG
                ).show();
                break;

            case TreatActivity.RequestCode.TREAT_UPDATE:

                final Treat updatedTreat = data.getParcelableExtra(TreatActivity.IntentKey.UPDATE_TREAT);
                if (updatedTreat == null) {
                    return;
                }
                addOrReplaceTreat(updatedTreat);
                notifyActivityResultChanged();
                break;
            default:
                return;
        }

        Log.wtf("Hmm", "here");
        notifyTreatLoaderContentChanged();
    }

    @Override
    public void onAsyncTreatQueryResult(final Treat treat)
    {
        if (treat == null) {
            return;
        }
        addOrReplaceTreat(treat);
        notifyActivityResultChanged();
    }

    protected void notifyTreatLoaderContentChanged()
    {
        final LoaderManager loaderManager = super.getSupportLoaderManager();
        final Loader<List<Treat>> loader = loaderManager.getLoader(TREAT_LOADER_ID);
        if (loader != null) {
            loader.onContentChanged();
        }
    }

    protected void notifyActivityResultChanged()
    {
        final Week week = new Week(mWeekDate, mTreats);

        final Intent resultIntent = new Intent();
        resultIntent.putExtra(IntentKey.WEEK_INSTANCE, week);

        super.setResult(Activity.RESULT_OK, resultIntent);
    }

    private void addOrReplaceTreat(final Treat treat)
    {
        final Treat existingTreat = TreatUtility.getTreatByUUID(mTreats, treat.getUUID());
        if (existingTreat != null) {
            mTreats.remove(existingTreat);
        }

        mTreats.add(TreatUtility.getTreatListInsertIndex(mTreats, treat.getNumber()), treat);
        mTreatListAdapter.notifyDataSetChanged();
        updateWeekSummaryFields();
    }

    protected void onAsyncTreatDeleterResult(final boolean success,
                                             final UUID[] uuids,
                                             final List<Treat> remainingTreats,
                                             final List<Treat> deletedTreats)
    {
        if (!success) {
            return;
        }

        final List<Treat> treats = TreatUtility.getTreatsByUUID(deletedTreats, uuids);
        if (treats.isEmpty()) {
            return;
        }

        for (final Treat deletedTreat : treats) {
            remainingTreats.remove(deletedTreat);
            remainingTreats.forEach((x) -> {
                if (x.getNumber() > deletedTreat.getNumber()) {
                    x.setNumber(x.getNumber() - 1);
                }
            });
        }

        mTreatListAdapter.notifyDataSetChanged();
        notifyTreatLoaderContentChanged();
        notifyActivityResultChanged();
        updateWeekSummaryFields();

        Toast.makeText(
                this,
                R.string.toast_deletion_successful,
                Toast.LENGTH_SHORT
        ).show();
    }

    @NonNull
    @Override
    public Loader<List<Treat>> onCreateLoader(final int id, final Bundle bundleArgs)
    {
        if (bundleArgs == null || !bundleArgs.containsKey(AsyncTreatLoader.BundleKey.WEEK_DATE_TIMESTAMP)) {
            throw new IllegalArgumentException("Unable to create loader " + id + "."
                    + "Check that bundle key " + AsyncTreatLoader.BundleKey.WEEK_DATE_TIMESTAMP + " has been set.");
        }

        final LocalDate weekDate = DateUtility.fromEpochMilli(bundleArgs.getLong(AsyncTreatLoader.BundleKey.WEEK_DATE_TIMESTAMP));
        return new AsyncTreatLoader(TreatWeekActivity.this, weekDate);
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<List<Treat>> loader, final List<Treat> data)
    {
        if (data == null) {
            return;
        }
        mTreats.clear();
        mTreats.addAll(data);
        mTreatListAdapter.notifyDataSetChanged();
        updateWeekSummaryFields();
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<List<Treat>> loader)
    {
        mTreats.clear();
    }
}

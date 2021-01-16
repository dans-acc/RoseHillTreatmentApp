package uk.co.rosehilltimber.rosehilltreatmentapp.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.List;

import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

public class WeekListAdapter extends BaseListAdapter<Week>
{

    private static class WeekItemViewHolder
    {
        private TextView mDateTextViewField;
        private TextView mWeekTextViewField;
        private TextView mTreatsTextViewField;
        private ImageButton mDownloadImageButton;
    }

    private static final String WEEK_NUMBER_NOT_APPLICABLE = "N/A (Ensure the financial year is set.)";

    private final LocalDate mStartOfFinancialYear;
    private final LocalDate mEndOfFinancialYear;

    private boolean mShouldDisplayVolume;

    public WeekListAdapter(final Activity mActivity,
                           final View.OnClickListener mOnClickListener,
                           final List<Week> mWeeks,
                           final LocalDate mStartOfFinancialYear,
                           final LocalDate mEndOfFinancialYear,
                           final boolean mShouldDisplayVolume)
    {
        super(
                mActivity,
                mOnClickListener,
                mWeeks,
                R.layout.item_week
        );

        this.mStartOfFinancialYear = mStartOfFinancialYear;
        this.mEndOfFinancialYear = mEndOfFinancialYear;

        this.mShouldDisplayVolume = mShouldDisplayVolume;
    }

    public WeekListAdapter(final Activity mActivity,
                           final View.OnClickListener mOnClickListener,
                           final List<Week> mWeeks,
                           final LocalDate mStartOfFinancialYear,
                           final LocalDate mEndOfFinancialYear)
    {
        this(
                mActivity,
                mOnClickListener,
                mWeeks,
                mStartOfFinancialYear,
                mEndOfFinancialYear,
                false
        );
    }

    public boolean shouldDisplayVolume()
    {
        return mShouldDisplayVolume;
    }

    public void setShouldDisplayVolume(final boolean mShouldDisplayVolume)
    {
        this.mShouldDisplayVolume = mShouldDisplayVolume;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View view, @NonNull final ViewGroup viewGroup)
    {
        final WeekItemViewHolder weekItemViewHolder;
        if (view == null) {

            // Create the week item based on the provided resource.
            view = LayoutInflater.from(mActivity).inflate(
                    super.mResource,
                    viewGroup,
                    BaseListAdapter.DEFAULT_ATTACH_TO_ROOT
            );

            // Cache the week item in order to yield faster access.
            weekItemViewHolder = new WeekItemViewHolder();
            weekItemViewHolder.mDateTextViewField = view.findViewById(R.id.item_week_tv_date_field);
            weekItemViewHolder.mWeekTextViewField = view.findViewById(R.id.item_week_tv_week_number_field);
            weekItemViewHolder.mTreatsTextViewField = view.findViewById(R.id.item_week_tv_treatment_count_field);
            weekItemViewHolder.mDownloadImageButton = view.findViewById(R.id.item_week_ib_download_action);

            // Add the week item listeners.
            weekItemViewHolder.mDownloadImageButton.setOnClickListener(super.mOnClickListener);

            view.setTag(weekItemViewHolder);
        } else {
            weekItemViewHolder = (WeekItemViewHolder) view.getTag();
        }

        // Update the position of the download button.
        weekItemViewHolder.mDownloadImageButton.setTag(position);

        final Week week = super.getItem(position);
        if (week == null) {
            return view;
        }

        // Set the current week date fields - display errors if required.
        final LocalDate weekDate = week.getDate();
        if (mStartOfFinancialYear != null && mEndOfFinancialYear != null && DateUtility.isWithin(weekDate, mStartOfFinancialYear, mEndOfFinancialYear)) {
            weekItemViewHolder.mDateTextViewField.setText(weekDate.format(DateUtility.BASIC_DATE_FORMATTER));
        } else {
            weekItemViewHolder.mDateTextViewField.setText(super.mActivity.getString(
                    R.string.week_date_out_of_bounds,
                    weekDate.format(DateUtility.BASIC_DATE_FORMATTER)
            ));
        }

        // Set the week number information.
        if (mStartOfFinancialYear == null) {
            weekItemViewHolder.mWeekTextViewField.setText(WEEK_NUMBER_NOT_APPLICABLE);
        } else {
            final long weekNumber = TreatUtility.weekNumber(mStartOfFinancialYear, weekDate);
            weekItemViewHolder.mWeekTextViewField.setText(String.valueOf(weekNumber));
        }

        // Update the treatment number field.
        weekItemViewHolder.mTreatsTextViewField.setText(String.valueOf(week.getTreatments().size()));

        return view;
    }
}

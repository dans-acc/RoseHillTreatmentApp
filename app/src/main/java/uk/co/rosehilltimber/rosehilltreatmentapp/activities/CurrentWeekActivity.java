package uk.co.rosehilltimber.rosehilltreatmentapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.UUID;

import android.view.View;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncTreatDeleterTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.SharedPreferenceUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

public class CurrentWeekActivity extends TreatWeekActivity
{

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (!super.mSharedPreferenceUtility.areRequiredAppPreferencesSet()
                || super.mStartOfFinancialYear == null || mEndOfFinancialYear == null) {
            startSettingsActivity();
            return;
        }

        super.mWeekDate = super.mSharedPreferenceUtility.getCurrentWorkingWeek();

        initCurrentWeekView();

        if (mWeekDate != null) {
            super.initTreatLoader();
        } else {
            super.destroyTreatLoader();
        }
    }

    private void initCurrentWeekView()
    {
        super.mTreatListAdapter.setEditable(true);
        super.mTreatListAdapter.updateListAdapter();

        updateCurrentWeekView();

        super.mCreateNewButton.setVisibility(View.VISIBLE);
    }

    private void updateCurrentWeekView()
    {
        if (super.mWeekDate == null) {
            super.mToolbar.setTitle(R.string.activity_current_week_toolbar_title_current_week_undefined);
            super.mCreateNewButton.setText(R.string.util_new_week);
            super.showWeekSummaryFields(false);
            super.showWeekSummaryCompleteButton(false);
        } else {
            super.mToolbar.setTitle(super.getString(
                    R.string.activity_current_week_toolbar_title_current_week_defined,
                    super.mWeekDate.format(DateUtility.BASIC_DATE_FORMATTER)
            ));
            super.mCreateNewButton.setText(R.string.util_new_treat);
            super.showWeekSummaryFields(true);
            super.showWeekSummaryCompleteButton(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater menuInflater = super.getMenuInflater();
        menuInflater.inflate(R.menu.menu_current_week, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem)
    {
        if (menuItem == null) {
            return false;
        }

        switch (menuItem.getItemId()) {
            case R.id.menu_current_week_item_open_log_action:
                startLogActivity();
                return true;
            case R.id.menu_current_week_item_open_settings_action:
                startSettingsActivity();
                return true;
            default:
                return false;
        }
    }

    private void startLogActivity()
    {
        final Intent logActivity = new Intent(this, LogActivity.class);
        if (super.mWeekDate != null) {
            logActivity.putExtra(LogActivity.IntentKey.CURRENT_WEEK_INSTANCE, new Week(super.mWeekDate, super.mTreats));
        }
        super.startActivityForResult(logActivity, LogActivity.RequestCode.UPDATE_CURRENT_WEEK);
    }

    private void startSettingsActivity()
    {
        super.startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onCompleteWeekButtonClicked(final View view)
    {
        if (super.mWeekDate == null) {
            updateCurrentWeekView();
            return;
        }

        DialogUtility.buildConfirmDialog(
                this,
                R.string.dialog_title_complete_current_working_week,
                R.string.dialog_message_complete_current_working_week,
                (dialog, which) -> {
                    dialog.dismiss();
                    super.mWeekDate = null;
                    super.mSharedPreferenceUtility.remove(SharedPreferenceUtility.SharedPreferenceKey.CURRENT_WEEK);
                    super.destroyTreatLoader();
                    updateCurrentWeekView();
                },
                R.string.util_complete
        );
    }

    @Override
    protected void onCreateNewButtonClicked(final View view)
    {
        if (super.mWeekDate == null) {
            onCreateNewWeekButtonClicked();
        } else {
            super.onCreateNewTreatButtonClicked();
        }
    }

    private void onCreateNewWeekButtonClicked()
    {
        final LocalDate nextWeekMondayDate = DateUtility.getFirstWeekDate(LocalDate.now()).plusWeeks(1);
        final Calendar nextWeekMondayCalendar = DateUtility.toUtilCalendar(nextWeekMondayDate);

        final DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (dialog, year, month, day) -> { },
                nextWeekMondayCalendar.get(Calendar.YEAR),
                nextWeekMondayCalendar.get(Calendar.MONTH),
                nextWeekMondayCalendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.setTitle(R.string.dialog_title_select_current_week_date);
        datePicker.getDatePicker().setMinDate(DateUtility.toUtilCalendar(super.mStartOfFinancialYear).getTimeInMillis());

        datePicker.show();
        datePicker.getButton(DatePickerDialog.BUTTON_POSITIVE).setOnClickListener((view) -> {

            final LocalDate pickedNewWeekDate = DateUtility.fromUtilDate(
                    datePicker.getDatePicker().getYear(),
                    datePicker.getDatePicker().getMonth(),
                    datePicker.getDatePicker().getDayOfMonth()
            );

            final LocalDate pickedNewWeekDateMonday = DateUtility.getFirstWeekDate(pickedNewWeekDate);

            DialogUtility.buildConfirmDialog(
                    this,
                    super.getString(R.string.dialog_title_confirm_new_current_week_date),
                    super.getString(
                            R.string.dialog_message_confirm_new_current_week_date,
                            pickedNewWeekDate.format(DateUtility.BASIC_DATE_FORMATTER),
                            pickedNewWeekDateMonday.format(DateUtility.BASIC_DATE_FORMATTER),
                            TreatUtility.weekNumber(super.mStartOfFinancialYear, pickedNewWeekDateMonday)),
                    (dialog, which) -> {
                        datePicker.dismiss();
                        dialog.dismiss();
                        onSelectedCurrentWeekDate(pickedNewWeekDateMonday);
                    },
                    R.string.util_create,
                    (dialog, which) -> dialog.dismiss(),
                    true
            );
        });
    }

    private void onSelectedCurrentWeekDate(final LocalDate selectedCurrentWeekDate)
    {
        if (selectedCurrentWeekDate.isBefore(super.mStartOfFinancialYear)) {
            DialogUtility.buildOkAlertDialog(
                    this,
                    R.string.dialog_title_week_date_before_the_start_of_financial_year,
                    R.string.dialog_message_week_date_before_the_start_of_financial_year
            );
        } else if (selectedCurrentWeekDate.isAfter(super.mEndOfFinancialYear)) {
            DialogUtility.buildConfirmDialog(
                    this,
                    super.getString(R.string.dialog_title_week_date_after_the_end_of_financial_year),
                    super.getString(R.string.dialog_message_week_date_after_the_end_of_financial_year),
                    (dialog, which) -> {
                        dialog.dismiss();
                        super.mWeekDate = selectedCurrentWeekDate;
                        super.mSharedPreferenceUtility.putCurrentWeek(super.mWeekDate);
                        super.initTreatLoader();
                        updateCurrentWeekView();
                    },
                    R.string.util_proceed_anyway,
                    (dialog, which) -> dialog.dismiss(),
                    true
            );
        } else {
            super.mWeekDate = selectedCurrentWeekDate;
            super.mSharedPreferenceUtility.putCurrentWeek(super.mWeekDate);
            super.initTreatLoader();
            updateCurrentWeekView();
        }
    }

    @Override
    protected void onTreatDeleteButtonClicked(final Treat treat, final View view)
    {
        if (treat == null) {
            return;
        } else if (view == null) {
            return;
        } else if (!mTreats.contains(treat)) {
            return;
        }

        DialogUtility.buildConfirmDialog(
                this,
                super.getString(R.string.dialog_title_delete_current_week_treatment),
                super.getString(
                        R.string.dialog_message_delete_current_week_treatment,
                        treat.getNumber()
                ),
                (dialog, which) -> {
                    dialog.dismiss();
                    final AsyncTreatDeleterTask treatDeleterTask = new AsyncTreatDeleterTask(this, treat.getUUID());
                    treatDeleterTask.execute();
                },
                R.string.util_delete,
                (dialog, which) -> {
                    dialog.cancel();
                },
                true
        );
    }

    protected void onTreatItemClicked(final Treat treat)
    {
        startTreatActivity(treat);
    }

    @Override
    public void startTreatActivity(final LocalDate weekDate, final TreatType treatType)
    {
        final Intent treatActivity = new Intent(this, TreatActivity.class);
        treatActivity.putExtra(TreatActivity.IntentKey.CREATE_TREAT_WEEK_DATE_TIMESTAMP, DateUtility.toEpochMilli(weekDate));
        treatActivity.putExtra(TreatActivity.IntentKey.CREATE_TREAT_TYPE_ORDINAL, treatType.ordinal());
        super.startActivityForResult(treatActivity, TreatActivity.RequestCode.TREAT_UUID);
    }

    @Override
    public void startTreatActivity(final Treat treat)
    {
        final Intent treatActivity = new Intent(this, TreatActivity.class);
        treatActivity.putExtra(TreatActivity.IntentKey.UPDATE_TREAT, treat);
        super.startActivityForResult(treatActivity, TreatActivity.RequestCode.TREAT_UPDATE);
    }

    @Override
    public void onAsyncTreatDeleterResult(final boolean success,
                                          final UUID... uuids)
    {
        super.onAsyncTreatDeleterResult(
                success,
                uuids,
                super.mTreats,
                super.mTreats
        );
    }
}

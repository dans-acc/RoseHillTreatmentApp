package uk.co.rosehilltimber.rosehilltreatmentapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
import android.widget.Toast;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncTreatDeleterTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

import java.time.LocalDate;
import java.util.Stack;
import java.util.UUID;

public class PriorWeekActivity extends TreatWeekActivity
{

    public static class RequestCode
    {
        public static final int UPDATED_WEEK_INSTANCE = 0;
    }

    private static final boolean TREAT_ACTIVITY_PRIOR_WEEK_PARENT_ACTIVITY = true;

    private boolean mEditMode;
    private Stack<Treat> mUndoDeletedTreatsStack;

    private MenuItem mCancelNewOrDeleteMenuItem;
    private MenuItem mUndoDeleteMenuItem;
    private MenuItem mNewOrDeleteWeekMenuItem;
    private MenuItem mSaveWeekMenuItem;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (super.mStartOfFinancialYear == null || super.mEndOfFinancialYear == null || super.mWeekDate == null) {
            super.finish();
            return;
        }

        if (!initPriorWeekView()) {
            super.finish();
            return;
        }

        mEditMode = false;
        mUndoDeletedTreatsStack = new Stack<Treat>();


    }

    private boolean initPriorWeekView()
    {
        super.mToolbar.setTitle(super.getString(
                R.string.activity_prior_week_toolbar_title,
                super.mWeekDate.format(DateUtility.BASIC_DATE_FORMATTER)
        ));

        final ActionBar supportActionBar = super.getSupportActionBar();
        if (supportActionBar == null) {
            return false;
        }
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        super.mCreateNewButton.setText(R.string.util_new_treat);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        final MenuInflater menuInflater = super.getMenuInflater();
        menuInflater.inflate(R.menu.menu_prior_week, menu);

        mCancelNewOrDeleteMenuItem = menu.findItem(R.id.menu_prior_week_item_cancel_new_or_delete_action);
        mUndoDeleteMenuItem = menu.findItem(R.id.menu_prior_week_item_undo_delete_action);
        mNewOrDeleteWeekMenuItem = menu.findItem(R.id.menu_prior_week_item_new_or_delete_treat_action);
        mSaveWeekMenuItem = menu.findItem(R.id.menu_prior_week_item_save_week_action);

        updatePriorWeekView(mEditMode);

        return super.onCreateOptionsMenu(menu);
    }

    private void updatePriorWeekView(final boolean editable)
    {
        mEditMode = editable;

        mCancelNewOrDeleteMenuItem.setVisible(mEditMode);
        mUndoDeleteMenuItem.setVisible(!mEditMode && !mUndoDeletedTreatsStack.isEmpty());
        mNewOrDeleteWeekMenuItem.setVisible(!mEditMode);
        mSaveWeekMenuItem.setVisible(mEditMode);

        super.mTreatListAdapter.setEditable(mEditMode);
        super.mTreatListAdapter.updateListAdapter();

        super.showCreateNewButton(mEditMode);
    }

    private void pushUndoable(final Treat treat)
    {
        if (mUndoDeletedTreatsStack.isEmpty()) {
            mCancelNewOrDeleteMenuItem.setVisible(false);
            mUndoDeleteMenuItem.setVisible(true);
        }

        mTreats.remove(treat);
        mTreatListAdapter.notifyDataSetChanged();
        super.updateWeekSummaryFields();

        mUndoDeletedTreatsStack.push(treat);
    }

    private void popUndoable()
    {
        final Treat treat = mUndoDeletedTreatsStack.pop();
        mTreats.add(TreatUtility.getTreatListInsertIndex(mTreats, treat.getNumber()), treat);
        mTreatListAdapter.notifyDataSetChanged();
        super.updateWeekSummaryFields();

        Toast.makeText(
                this,
                super.getString(
                        R.string.toast_deleted_treat_undone,
                        treat.getNumber()
                ),
                Toast.LENGTH_SHORT
        ).show();

        if (mUndoDeletedTreatsStack.isEmpty()) {
            mCancelNewOrDeleteMenuItem.setVisible(true);
            mUndoDeleteMenuItem.setVisible(false);
        }
    }

    private void popAllUndoables()
    {
        if (mEditMode) {
            mCancelNewOrDeleteMenuItem.setVisible(true);
            mUndoDeleteMenuItem.setVisible(false);
        }

        mTreats.addAll(mUndoDeletedTreatsStack);
        TreatUtility.sortTreatsIntoDescendingOrder(mTreats);
        mTreatListAdapter.notifyDataSetChanged();
        super.updateWeekSummaryFields();

        mUndoDeletedTreatsStack.clear();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem)
    {
        if (menuItem == null) {
            return false;
        }

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_prior_week_item_undo_delete_action:
                popUndoable();
                break;
            case R.id.menu_prior_week_item_cancel_new_or_delete_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_cancel_edits,
                        R.string.dialog_message_cancel_edits,
                        (dialog, which) -> {
                            dialog.dismiss();
                            popAllUndoables();
                            updatePriorWeekView(false);
                        },
                        android.R.string.yes
                );
                break;
            case R.id.menu_prior_week_item_new_or_delete_treat_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_enter_edit_mode,
                        R.string.dialog_message_enter_edit_mode,
                        (dialog, which) -> {
                            dialog.dismiss();
                            mUndoDeletedTreatsStack.clear();
                            updatePriorWeekView(true);
                        },
                        R.string.util_edit
                );
                break;
            case R.id.menu_prior_week_item_save_week_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_save_prior_week_deletions,
                        R.string.dialog_message_save_prior_week_deletions,
                        (dialog, which) -> {
                            dialog.dismiss();
                            save();
                        },
                        R.string.util_save
                );
                break;
            default:
                return false;
        }
        return true;
    }

    private void save()
    {
        if (mUndoDeletedTreatsStack.isEmpty()) {
            DialogUtility.buildOkAlertDialog(
                    this,
                    R.string.dialog_title_cannot_save_prior_week_no_deletions_detected,
                    R.string.dialog_message_cannot_save_prior_week_no_deletions_detected
            );
            return;
        }

        final AsyncTreatDeleterTask asyncTreatDeleterTask = new AsyncTreatDeleterTask(this,
                TreatUtility.getTreatUUIDs(mUndoDeletedTreatsStack));
        asyncTreatDeleterTask.execute();
    }

    protected void onCompleteWeekButtonClicked(final View view)
    {
        throw new UnsupportedOperationException(
                "The onCompleteWeekButtonClicked() method is not supported by the "
                + this.getClass().getName() + " class."
        );
    }

    @Override
    protected void onCreateNewButtonClicked(final View view)
    {
        if (!mEditMode) {
            super.onCreateNewTreatButtonClicked();
            return;
        }

        DialogUtility.buildConfirmDialog(
                this,
                R.string.dialog_title_creating_a_treat_leaves_edit_mode,
                R.string.dialog_message_creating_a_treat_leaves_edit_mode,
                (dialog, which) -> {
                    dialog.dismiss();
                    popAllUndoables();
                    updatePriorWeekView(false);
                    super.onCreateNewTreatButtonClicked();
                },
                R.string.util_continue
        );
    }

    @Override
    protected void onTreatDeleteButtonClicked(final Treat treat, final View view)
    {
        if (!mEditMode) {
            return;
        }
        pushUndoable(treat);
    }

    protected void onTreatItemClicked(final Treat treat)
    {
        if (mEditMode) {
            DialogUtility.buildConfirmDialog(
                    this,
                    R.string.dialog_title_leaving_in_edit_mode,
                    R.string.dialog_message_leaving_in_edit_mode,
                    (dialog, which) -> {
                        dialog.dismiss();
                        popAllUndoables();
                        updatePriorWeekView(false);
                        startTreatActivity(treat);
                    },
                    R.string.util_leave
            );
        } else {
            startTreatActivity(treat);
        }
    }

    @Override
    public void startTreatActivity(final LocalDate weekDate, final TreatType treatType)
    {
        final Intent treatActivity = new Intent(this, TreatActivity.class);
        treatActivity.putExtra(TreatActivity.IntentKey.PRIOR_WEEK_PARENT_ACTIVITY, TREAT_ACTIVITY_PRIOR_WEEK_PARENT_ACTIVITY);
        treatActivity.putExtra(TreatActivity.IntentKey.CREATE_TREAT_WEEK_DATE_TIMESTAMP, DateUtility.toEpochMilli(weekDate));
        treatActivity.putExtra(TreatActivity.IntentKey.CREATE_TREAT_TYPE_ORDINAL, treatType.ordinal());
        super.startActivityForResult(treatActivity, TreatActivity.RequestCode.TREAT_UUID);
    }

    @Override
    public void startTreatActivity(final Treat treat)
    {
        final Intent treatActivity = new Intent(this, TreatActivity.class);
        treatActivity.putExtra(TreatActivity.IntentKey.PRIOR_WEEK_PARENT_ACTIVITY, TREAT_ACTIVITY_PRIOR_WEEK_PARENT_ACTIVITY);
        treatActivity.putExtra(TreatActivity.IntentKey.UPDATE_TREAT, treat);
        super.startActivityForResult(treatActivity, TreatActivity.RequestCode.TREAT_UPDATE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed()
    {
        if (!mEditMode) {
            super.onBackPressed();
            return;
        }

        DialogUtility.buildConfirmDialog(
                this,
                R.string.dialog_title_leaving_in_edit_mode,
                R.string.dialog_message_leaving_in_edit_mode,
                (dialog, which) -> {
                    dialog.dismiss();
                    super.setResult(Activity.RESULT_OK);
                    super.onBackPressed();
                },
                R.string.util_leave
        );
    }

    @Override
    public void onAsyncTreatDeleterResult(final boolean success,
                                          final UUID... uuids)
    {
        super.onAsyncTreatDeleterResult(
                success,
                uuids,
                super.mTreats,
                mUndoDeletedTreatsStack
        );
        mUndoDeletedTreatsStack.clear();
        updatePriorWeekView(false);
    }
}

package uk.co.rosehilltimber.rosehilltreatmentapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.adapters.TimberPackListAdapter;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders.TreatBuilderOperation;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncTreatBuilderTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatContract;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs.TimberPackDialog;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders.TreatBuilder;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders.TreatCreationBuilder;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders.TreatUpdateBuilder;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.SharedPreferenceUtility;

import java.time.LocalDate;
import java.util.UUID;

public class TreatActivity extends AppCompatActivity implements
        View.OnClickListener,
        TimberPackDialog.TimberPackDialogListener,
        AsyncTreatBuilderTask.AsyncTreatBuilderListener
{

    @SuppressWarnings("WeakerAccess")
    protected static class IntentKey
    {
        protected static final String PRIOR_WEEK_PARENT_ACTIVITY = "PRIOR_WEEK_PARENT_ACTIVITY";
        protected static final String UPDATE_TREAT = "UPDATE_TREAT";
        protected static final String CREATE_TREAT_WEEK_DATE_TIMESTAMP = "CREATE_TREAT_WEEK_DATE_TIMESTAMP";
        protected static final String CREATE_TREAT_TYPE_ORDINAL = "CREATE_TREAT_DATE_ORDINAL";
        protected static final String CREATED_TREAT_UUID = "CREATED_TREAT_UUID";
    }

    @SuppressWarnings("WeakerAccess")
    protected static class RequestCode
    {
        protected static final int TREAT_UUID = 0;
        protected static final int TREAT_UPDATE = 1;
    }

    private static class TreatSummaryViewHolder
    {
        private ImageView mTreatTypeColourImageView;
        private TextView mTreatTypeTextViewField;
        private TextView mTreatPackCountTextViewField;
        private TextView mTreatTotalVolumeTextViewField;
    }

    private static final boolean DEFAULT_PRIOR_WEEK_PARENT_ACTIVITY = false;

    private static final boolean TREAT_CREATION_EDIT_MODE = true;
    private static final boolean TREAT_UPDATE_EDIT_MODE = false;

    private static final long INVALID_CREATE_TREAT_WEEK_DATE_TIMESTAMP = -1L;
    private static final int INVALID_CREATE_TREAT_TYPE_ORDINAL = -1;

    private boolean mPriorWeekParentActivity;
    private boolean mEditMode;

    private TreatBuilder mTreatBuilder;

    private Toolbar mToolbar;

    private MenuItem mCancelTreatEditOrCreateMenuItem;
    private MenuItem mUndoOperationMenuItem;
    private MenuItem mEditTreatMenuItem;
    private MenuItem mSaveTreatMenuItem;
    private MenuItem mCreateTreatMenuItem;

    private RelativeLayout mTreatSummaryLayout;
    private TreatSummaryViewHolder mTreatSummaryViewHolder;

    private TimberPackListAdapter mTimberPackListAdapter;
    private ListView mTimberPackListView;

    private Button mCreateNewTimberPackButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_treat);

        final Intent intentArguments = super.getIntent();
        if (intentArguments == null) {
            super.finish();
            return;
        }

        mPriorWeekParentActivity = intentArguments.getBooleanExtra(
                IntentKey.PRIOR_WEEK_PARENT_ACTIVITY,
                DEFAULT_PRIOR_WEEK_PARENT_ACTIVITY
        );

        mTreatBuilder = initTreatBuilder(intentArguments);
        if (mTreatBuilder == null) {
            super.setResult(Activity.RESULT_CANCELED);
            super.finish();
            return;
        }

        mEditMode = mTreatBuilder instanceof TreatCreationBuilder
                ? TREAT_CREATION_EDIT_MODE
                : TREAT_UPDATE_EDIT_MODE;

        if (!initToolbar()) {
            super.setResult(Activity.RESULT_CANCELED);
            super.finish();
            return;
        }

        initTreatSummaryViewHolder();

        mTimberPackListAdapter = new TimberPackListAdapter(
                this,
                this,
                mTreatBuilder.getTimberPacks(),
                mEditMode,
                mTreatBuilder.getType()
        );
        mTimberPackListAdapter.setNotifyOnChange(true);

        mTimberPackListView = super.findViewById(R.id.activity_treat_lv_timber_packs);
        mTimberPackListView.setAdapter(mTimberPackListAdapter);

        mCreateNewTimberPackButton = super.findViewById(R.id.activity_treat_btn_create_new_timber_pack);
        mCreateNewTimberPackButton.setOnClickListener(this);

        updateImmutableTreatSummaryViews();
        updateMutableTreatSummaryViews();
    }

    private TreatBuilder initTreatBuilder(final Intent intentArguments)
    {
        if (intentArguments == null){
            return null;
        }

        final SharedPreferenceUtility sharedPreferenceUtility = SharedPreferenceUtility.getInstance(this);
        if (sharedPreferenceUtility == null) {
            return null;
        }

        final TreatBuilder treatBuilder;
        final Treat treat = intentArguments.getParcelableExtra(IntentKey.UPDATE_TREAT);
        if (treat != null) {

            final float maximumTankVolume = treat.getType() == TreatType.BROWN
                    ? sharedPreferenceUtility.getMaximumBrownTankVolume()
                    : sharedPreferenceUtility.getMaximumGreenTankVolume();
            treatBuilder = new TreatUpdateBuilder(treat, maximumTankVolume);

        } else {

            final long weekDateTimestamp = intentArguments.getLongExtra(
                    IntentKey.CREATE_TREAT_WEEK_DATE_TIMESTAMP,
                    INVALID_CREATE_TREAT_WEEK_DATE_TIMESTAMP
            );
            final int treatTypeOrdinal = intentArguments.getIntExtra(
                    IntentKey.CREATE_TREAT_TYPE_ORDINAL,
                    INVALID_CREATE_TREAT_TYPE_ORDINAL
            );
            if (weekDateTimestamp == INVALID_CREATE_TREAT_WEEK_DATE_TIMESTAMP ||  treatTypeOrdinal < 0
                    || treatTypeOrdinal > TreatType.values().length) {
                return null;
            }

            final LocalDate weekDate = DateUtility.fromEpochMilli(weekDateTimestamp);
            final TreatType treatType= TreatType.values()[treatTypeOrdinal];

            final float maximumTankVolume = treatType == TreatType.BROWN
                    ? sharedPreferenceUtility.getMaximumBrownTankVolume()
                    : sharedPreferenceUtility.getMaximumGreenTankVolume();

            treatBuilder = new TreatCreationBuilder(
                    weekDate,
                    treatType,
                    maximumTankVolume,
                    sharedPreferenceUtility.getInitialTreatNumber()
            );
        }

        return treatBuilder;
    }

    private boolean initToolbar()
    {
        mToolbar = super.findViewById(R.id.activity_treat_toolbar);
        if (mTreatBuilder instanceof TreatCreationBuilder) {
            mToolbar.setTitle(R.string.activity_treat_toolbar_title_create_new_treat);
        } else {
            mToolbar.setTitle(super.getString(
                    R.string.activity_treat_toolbar_title_update_existing_treat,
                    ((TreatUpdateBuilder) mTreatBuilder).getTreatNumber()
            ));
        }
        super.setSupportActionBar(mToolbar);

        final ActionBar supportActionBar = super.getSupportActionBar();
        if (supportActionBar == null) {
            return false;
        }
        supportActionBar.setDisplayShowTitleEnabled(true);
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        return true;
    }

    private void initTreatSummaryViewHolder()
    {
        mTreatSummaryLayout = super.findViewById(R.id.activity_treat_layout_summary);
        mTreatSummaryViewHolder = new TreatSummaryViewHolder();
        mTreatSummaryViewHolder.mTreatTypeColourImageView = mTreatSummaryLayout.findViewById(R.id.activity_treat_iv_colour_tag);
        mTreatSummaryViewHolder.mTreatTypeTextViewField = mTreatSummaryLayout.findViewById(R.id.activity_treat_tv_type_field);
        mTreatSummaryViewHolder.mTreatPackCountTextViewField = mTreatSummaryLayout.findViewById(R.id.activity_treat_tv_packs_count_field);
        mTreatSummaryViewHolder.mTreatTotalVolumeTextViewField = mTreatSummaryLayout.findViewById(R.id.activity_treat_tv_volume_field);
    }

    private void updateImmutableTreatSummaryViews()
    {
        switch (mTreatBuilder.getType()) {
            case GREEN:
                mTreatSummaryViewHolder.mTreatTypeColourImageView.setImageResource(R.drawable.ic_treat_type_green);
                break;
            case ROUND_GREEN:
                mTreatSummaryViewHolder.mTreatTypeColourImageView.setImageResource(R.drawable.ic_treat_type_round_green);
                break;
            case BROWN:
                mTreatSummaryViewHolder.mTreatTypeColourImageView.setImageResource(R.drawable.ic_treat_type_brown);
                break;
        }
        mTreatSummaryViewHolder.mTreatTypeTextViewField.setText(mTreatBuilder.getType().getName());
    }

    private void updateMutableTreatSummaryViews()
    {
        mTreatSummaryViewHolder.mTreatPackCountTextViewField.setText(String.valueOf(mTreatBuilder.getTimberPacks().size()));
        mTreatSummaryViewHolder.mTreatTotalVolumeTextViewField.setText(super.getString(
                R.string.volume_format,
                mTreatBuilder.getTankVolume()
        ));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        final MenuInflater menuInflater = super.getMenuInflater();
        menuInflater.inflate(R.menu.menu_treat, menu);

        mCancelTreatEditOrCreateMenuItem = menu.findItem(R.id.menu_treat_item_cancel_edit_or_save_action);
        mUndoOperationMenuItem = menu.findItem(R.id.menu_treat_item_undo_operation_action);
        mEditTreatMenuItem = menu.findItem(R.id.menu_treat_item_edit_treat_action);
        mSaveTreatMenuItem = menu.findItem(R.id.menu_treat_item_save_editing_treat_action);
        mCreateTreatMenuItem = menu.findItem(R.id.menu_treat_item_create_treat_action);

        updateTreatView(mEditMode);

        return super.onCreateOptionsMenu(menu);
    }

    private void updateTreatView(final boolean editable)
    {
        mEditMode = editable;

        final boolean undoStackEmpty = mTreatBuilder.getUndoStack().empty();
        mCancelTreatEditOrCreateMenuItem.setVisible(mEditMode && undoStackEmpty);
        mUndoOperationMenuItem.setVisible(!mEditMode && !undoStackEmpty);

        if (mTreatBuilder instanceof TreatCreationBuilder) {
            mEditTreatMenuItem.setVisible(false);
            mSaveTreatMenuItem.setVisible(false);
            mCreateTreatMenuItem.setVisible(true);
        } else {
            mEditTreatMenuItem.setVisible(!mEditMode);
            mSaveTreatMenuItem.setVisible(mEditMode);
            mCreateTreatMenuItem.setVisible(false);
        }

        mTimberPackListAdapter.setEditable(mEditMode);
        mTimberPackListAdapter.updateListAdapter();

        if (mEditMode) {
            mCreateNewTimberPackButton.setVisibility(View.VISIBLE);
        } else {
            mCreateNewTimberPackButton.setVisibility(View.GONE);
        }
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
                return true;
            case R.id.menu_treat_item_cancel_edit_or_save_action:
                onCancelActionPressed();
                return true;
            case R.id.menu_treat_item_undo_operation_action:
                onUndoActionPressed();
                return true;
            case R.id.menu_treat_item_edit_treat_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_enter_edit_mode,
                        R.string.dialog_message_enter_edit_mode,
                        (dialog, which) -> {
                            dialog.dismiss();
                            updateTreatView(true);
                        },
                        R.string.util_edit
                );
                return true;
            case R.id.menu_treat_item_save_editing_treat_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_save_edits,
                        R.string.dialog_message_save_edits,
                        (dialog, which) -> {
                            dialog.dismiss();
                            onBuildActionPressed();
                        },
                        R.string.util_save
                );
                return true;
            case R.id.menu_treat_item_create_treat_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_treat_creation_confirmation,
                        R.string.dialog_message_treat_creation_confirmation,
                        (dialog, which) -> {
                            dialog.dismiss();
                            onBuildActionPressed();
                        },
                        R.string.util_create
                );
                return true;
            default:
                return false;
        }
    }

    private void onCancelActionPressed()
    {
        DialogUtility.buildConfirmDialog(
                this,
                R.string.dialog_title_cancel_edits,
                R.string.dialog_message_cancel_edits,
                (dialog, which) -> {

                    dialog.dismiss();
                    mTreatBuilder.undoAllTreatBuilderOperations();
                    updateViewAfterAction();

                    if (mTreatBuilder instanceof TreatCreationBuilder) {
                        super.setResult(Activity.RESULT_CANCELED);
                        super.finish();
                    } else {
                        updateTreatView(false);
                    }

                },
                android.R.string.yes
        );
    }

    private void onUndoActionPressed()
    {
        final Pair<UUID, Pair<TreatBuilderOperation, TimberPack>> undoEntry = mTreatBuilder.undoTreatBuilderOperation();
        if (undoEntry == null) {
            Toast.makeText(
                    this,
                    R.string.toast_failed_to_undo_treat_builder_operation,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            final TreatBuilderOperation treatBuilderOperation = undoEntry.second.first;
            Toast.makeText(
                    this,
                    super.getString(
                            R.string.toast_treat_builder_operation_successfully_undone,
                            treatBuilderOperation.getName()
                    ),
                    Toast.LENGTH_SHORT
            ).show();
            updateViewAfterAction();
        }
    }

    private void onBuildActionPressed()
    {
        if (mTreatBuilder instanceof TreatCreationBuilder) {
            if (mTreatBuilder.getTimberPacks().isEmpty()) {
                DialogUtility.buildOkAlertDialog(
                        this,
                        R.string.dialog_title_treat_creation_failure_no_timber_packs,
                        R.string.dialog_message_treat_creation_failure_no_timber_packs
                );
                return;
            }
        } else {
            if (!mTreatBuilder.hasBuildUpdates()) {
                DialogUtility.buildOkAlertDialog(
                        this,
                        R.string.dialog_title_treat_save_failure_no_changes,
                        R.string.dialog_message_treat_save_failure_no_changes
                );
                return;
            } else if (mTreatBuilder.getTimberPacks().isEmpty()) {
                DialogUtility.buildOkAlertDialog(
                        this,
                        R.string.dialog_title_treat_save_failure_no_timber_packs,
                        R.string.dialog_message_treat_save_failure_no_timber_packs
                );
                return;
            }
        }

        final AsyncTreatBuilderTask buildTask = new AsyncTreatBuilderTask(
                this,
                mTreatBuilder,
                TreatContract.CONTENT_AUTHORITY
        );
        buildTask.execute();
    }

    @Override
    public void onAsyncTreatBuilderResult(final TreatBuilder treatBuilder, final boolean success)
    {
        if (mTreatBuilder instanceof TreatCreationBuilder) {
            if (success) {

                final Intent creationIntentData = new Intent();
                creationIntentData.putExtra(
                        IntentKey.CREATED_TREAT_UUID,
                        mTreatBuilder.getUUID().toString()
                );
                super.setResult(RESULT_OK, creationIntentData);
                super.finish();

            } else {
                DialogUtility.buildOkAlertDialog(
                        this,
                        R.string.dialog_title_creation_failure,
                        R.string.dialog_message_failed_to_create_treat
                );
            }
        } else {
            if (success) {

                mTreatBuilder.notifyResultSuccessful();
                updateTreatView(false);

                final Intent updateIntentData = new Intent();
                updateIntentData.putExtra(
                        IntentKey.UPDATE_TREAT,
                        ((TreatUpdateBuilder) mTreatBuilder).buildTreat()
                );
                super.setResult(Activity.RESULT_OK, updateIntentData);

                Toast.makeText(
                        this,
                        R.string.toast_treat_successfully_updated,
                        Toast.LENGTH_LONG
                ).show();

            } else {
                super.setResult(Activity.RESULT_CANCELED);
                DialogUtility.buildOkAlertDialog(
                        this,
                        R.string.dialog_title_update_failure,
                        R.string.dialog_message_failed_to_update_treat
                );
            }
        }
    }

    public void updateViewAfterAction()
    {
        if (mTreatBuilder.getUndoStack().isEmpty()) {
            mCancelTreatEditOrCreateMenuItem.setVisible(true);
            mUndoOperationMenuItem.setVisible(false);
        } else {
            mCancelTreatEditOrCreateMenuItem.setVisible(false);
            mUndoOperationMenuItem.setVisible(true);
        }
        updateMutableTreatSummaryViews();
        mTimberPackListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(final View view)
    {
        if (isFinishing() || isDestroyed()) {
            return;
        } else if (view == null) {
            return;
        }

        if (view.getId() == R.id.activity_treat_btn_create_new_timber_pack &&
                view == mCreateNewTimberPackButton) {
            onCreateNewTimberPackButtonClicked();
            return;
        }

        final TimberPack timberPack = mTimberPackListAdapter.getItemFromViewTagPosition(view);
        if (timberPack == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.item_timber_pack_ib_edit_action:
                onEditTimberPackButtonClicked(timberPack);
                break;
            case R.id.item_timber_pack_ib_delete_action:
                onDeleteTimberPackButtonClicked(timberPack);
                break;
        }
    }

    private void onCreateNewTimberPackButtonClicked()
    {
        if (!mTreatBuilder.hasTankCapacity()) {
            DialogUtility.buildOkAlertDialog(
                    this,
                    R.string.dialog_title_tank_volume_reached,
                    R.string.dialog_message_tank_volume_reached
            );
            return;
        }

        final TreatType builderTreatType = mTreatBuilder.getType();
        final TimberPackType[] builderTreatTypePackTypes = builderTreatType.getTimberPackTypes();
        if (builderTreatTypePackTypes.length == 1) {
            showCreateTimberPackDialog(builderTreatTypePackTypes[0]);
            return;
        }

        DialogUtility.buildSelectionDialog(
                this,
                R.string.dialog_title_select_timber_pack_type,
                builderTreatType.getTimberPackTypeNames(),
                (dialog, which) -> {
                    dialog.dismiss();
                    showCreateTimberPackDialog(builderTreatTypePackTypes[which]);
                }
        );
    }

    private void showCreateTimberPackDialog(final TimberPackType timberPackType)
    {
        final TimberPackDialog createTimberPackDialog = DialogUtility
                .buildTimberPackDialog(this, timberPackType);
        createTimberPackDialog.show(
                super.getSupportFragmentManager(),
                timberPackType.getName()
        );
    }

    private void onEditTimberPackButtonClicked(final TimberPack timberPack)
    {
        final TimberPackDialog timberPackDialog = DialogUtility
                .buildTimberPackDialog(this, timberPack);
        timberPackDialog.show(
                super.getSupportFragmentManager(),
                timberPack.getTimberPackType().getName()
        );
    }

    @Override
    public boolean onValidateTimberPackDialogResult(final TimberPack timberPack, final boolean newTimberPack)
    {
        if (newTimberPack) {
            if (mTreatBuilder.hasTankCapacity(timberPack)) {
                return true;
            }
        } else {
            final TimberPack existingTimberPack = mTreatBuilder.getTimberPackByUUID(timberPack.getUUID());
            if (existingTimberPack == null) {
                return false;
            } else if (mTreatBuilder.hasTankCapacity(timberPack)) {
                return true;
            }
        }

        DialogUtility.buildOkAlertDialog(
                this,
                R.string.dialog_title_tank_volume_reached,
                R.string.dialog_message_tank_volume_reached
        );

        return false;
    }

    @Override
    public void onTimberPackDialogResult(final TimberPack timberPack, final boolean newTimberPack)
    {
        if (newTimberPack) {
            mTreatBuilder.createTimberPack(timberPack);
        } else {
            mTreatBuilder.updateTimberPack(timberPack);
        }
        updateViewAfterAction();
    }

    private void onDeleteTimberPackButtonClicked(final TimberPack timberPack)
    {
        DialogUtility.buildConfirmDialog(
                this,
                R.string.dialog_title_timber_pack_deletion_conformation,
                R.string.dialog_message_timber_pack_deletion_conformation,
                (dialog, which) -> {
                    dialog.dismiss();
                    mTreatBuilder.deleteTimberPack(timberPack);
                    updateViewAfterAction();
                },
                R.string.util_delete
        );
    }

    @Override
    public Intent getSupportParentActivityIntent()
    {
        return mPriorWeekParentActivity
                ? getParentActivityIntent()
                : super.getSupportParentActivityIntent();
    }

    @Override
    public Intent getParentActivityIntent()
    {
        return mPriorWeekParentActivity
                ? getPriorWeekParentActivity()
                : super.getParentActivityIntent();
    }

    private Intent getPriorWeekParentActivity()
    {
        final Intent priorWeekParentIntent = new Intent(this, PriorWeekActivity.class);
        priorWeekParentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return  priorWeekParentIntent;
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
                    super.setResult(Activity.RESULT_CANCELED);
                    super.onBackPressed();
                },
                R.string.util_leave
        );
    }
}

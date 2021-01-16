package uk.co.rosehilltimber.rosehilltreatmentapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncDeleteAllExportsTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncTruncateTablesTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncResetFinancialYearTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.async.tasks.AsyncYearlySummaryTask;
import uk.co.rosehilltimber.rosehilltreatmentapp.fragments.SettingsPreferenceFragment;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.SharedPreferenceUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs.SettingsAuthorisationDialog;

import java.time.LocalDate;

public class SettingsActivity extends AppCompatActivity
    implements SettingsAuthorisationDialog.PasscodeDialogListener
{

    private Toolbar mToolbar;

    private SharedPreferenceUtility mSharedPreferenceUtility;
    private SharedPreferences mSharedPreferences;

    private String mPasscode;
    private boolean mSettingsAuthorised;

    private MenuItem mBackupTreatsMenuItem;
    private MenuItem mDeleteExportedFilesMenuItem;
    private MenuItem mTruncateTablesMenuItem;
    private MenuItem mResetFinancialYearMenuItem;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_settings);

        mToolbar = super.findViewById(R.id.activity_settings_toolbar);
        mToolbar.setTitle(super.getTitle());
        super.setSupportActionBar(mToolbar);

        final ActionBar supportActionBar = super.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSharedPreferenceUtility = SharedPreferenceUtility.getInstance(this);
        mSharedPreferences = mSharedPreferenceUtility.getSharedPreferences();

        mPasscode = mSharedPreferenceUtility.getPasscode();
        mSettingsAuthorised = mPasscode == null;

        if (mSettingsAuthorised) {
            createPreferencesFragment();
        } else {
            final SettingsAuthorisationDialog settingsAuthorisationDialog = DialogUtility
                    .buildSettingsAuthorisationDialog(this, mSharedPreferenceUtility.areRequiredAppPreferencesSet());
            settingsAuthorisationDialog.show(
                    super.getSupportFragmentManager(),
                    super.getString(R.string.dialog_title_settings_authorisation)
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater menuInflater = super.getMenuInflater();
        menuInflater.inflate(R.menu.menu_settings, menu);

        // Get the menu items for carrying out special actions.
        mBackupTreatsMenuItem = menu.findItem(R.id.menu_settings_item_export_yearly_summary_action);
        mDeleteExportedFilesMenuItem = menu.findItem(R.id.menu_settings_item_delete_exported_files_action);
        mTruncateTablesMenuItem = menu.findItem(R.id.menu_settings_item_truncate_tables_action);
        mResetFinancialYearMenuItem = menu.findItem(R.id.menu_settings_item_reset_financial_year_action);

        enableActivityMenuItems(mPasscode != null);

        return super.onCreateOptionsMenu(menu);
    }

    public void enableActivityMenuItems(final boolean enable)
    {
        mBackupTreatsMenuItem.setEnabled(enable);
        mDeleteExportedFilesMenuItem.setEnabled(enable);
        mTruncateTablesMenuItem.setEnabled(enable);
        mResetFinancialYearMenuItem.setEnabled(enable);
    }

    public void createPreferencesFragment()
    {
        final FragmentManager fragmentManager = super.getSupportFragmentManager();
        if (fragmentManager == null) {
            super.finish();
            return;
        }

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final SettingsPreferenceFragment settingsPreferenceFragment = new SettingsPreferenceFragment();
        fragmentTransaction.replace(R.id.activity_settings_preference_screen, settingsPreferenceFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem)
    {
        if (menuItem == null) {
            return false;
        }

        // Determine which of the actions were clicked.
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_settings_item_export_yearly_summary_action:
                onExportYearlySummaryActionPressed();
                return true;
            case R.id.menu_settings_item_delete_exported_files_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_confirm_delete_all_exported_files,
                        R.string.dialog_message_confirm_delete_all_exported_files,
                        (dialog, which) -> {
                            dialog.dismiss();
                            onDeleteExportedFilesActionPressed();
                        },
                        R.string.util_delete_exported_files
                );
                return true;
            case R.id.menu_settings_item_truncate_tables_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_confirm_truncate_tables,
                        R.string.dialog_message_confirm_truncate_tables,
                        (dialog, which) -> {
                            dialog.dismiss();
                            onTruncateTablesActionPressed();
                        },
                        R.string.util_truncate_tables
                );
                return true;
            case R.id.menu_settings_item_reset_financial_year_action:
                DialogUtility.buildConfirmDialog(
                        this,
                        R.string.dialog_title_confirm_reset_financial_year,
                        R.string.dialog_message_confirm_reset_financial_year,
                        (dialog, which) -> {
                            dialog.dismiss();
                            onResetFinancialYearActionPressed();
                        },
                        R.string.util_reset_financial_year
                );
                return true;
            default:
                return false;
        }

    }

    private void onExportYearlySummaryActionPressed()
    {
        final LocalDate startOfFinancialYear = mSharedPreferenceUtility.getStartOfFinancialYear(),
                endOfFinancialYear = mSharedPreferenceUtility.getEndOfFinancialYear();

        if (startOfFinancialYear == null || endOfFinancialYear == null) {
            DialogUtility.buildOkAlertDialog(
                    this,
                    R.string.dialog_title_undefined_financial_year,
                    R.string.dialog_message_undefined_financial_year
            );
            return;
        }

        final AsyncYearlySummaryTask asyncYearlySummaryTask = new AsyncYearlySummaryTask(
                this,
                startOfFinancialYear,
                endOfFinancialYear
        );
        asyncYearlySummaryTask.execute();
    }

    private void onDeleteExportedFilesActionPressed()
    {
        final AsyncDeleteAllExportsTask asyncDeleteAllExportsTask = new AsyncDeleteAllExportsTask(this);
        asyncDeleteAllExportsTask.execute();
    }

    private void onTruncateTablesActionPressed()
    {
        final AsyncTruncateTablesTask asyncTruncateTablesTask = new AsyncTruncateTablesTask(this);
        asyncTruncateTablesTask.execute();
    }

    private void onResetFinancialYearActionPressed()
    {
        final AsyncResetFinancialYearTask asyncResetFinancialYearTask = new AsyncResetFinancialYearTask(this);
        asyncResetFinancialYearTask.execute();
    }

    @Override
    public boolean onPasscodeAuthorisationAttempt(final String passcode)
    {
        if (mPasscode == null) {
            return true;
        } else if (!mPasscode.equals(passcode)) {
            return false;
        }
        mSettingsAuthorised = true;
        enableActivityMenuItems(true);
        createPreferencesFragment();
        return true;
    }


    @Override
    public void onPasscodeAuthorisationCanceled()
    {
        returnToParentActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed()
    {
        // Prompt the user informing them that not all settings have been applied.
        final SharedPreferenceUtility sharedPreferenceUtility = SharedPreferenceUtility.getInstance(this);
        if (!sharedPreferenceUtility.containsAllBut(SharedPreferenceUtility.SharedPreferenceKey.CURRENT_WEEK)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.activity_settings_alert_title_incomplete_configuration)
                    .setMessage(R.string.activity_settings_alert_msg_incomplete_configuration)
                    .setNeutralButton(
                            android.R.string.ok,
                            (dialog, which) -> dialog.cancel()
                    )
                    .create().show();
            return;
        }

        // Force the parent activity to be restarted.
        returnToParentActivity();
    }

    private void returnToParentActivity()
    {
        final Intent currentWeekIntent = new Intent(this, CurrentWeekActivity.class);
        currentWeekIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        super.startActivity(currentWeekIntent);
    }

}

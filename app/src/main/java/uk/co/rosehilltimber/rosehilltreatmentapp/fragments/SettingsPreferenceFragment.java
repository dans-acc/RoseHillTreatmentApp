package uk.co.rosehilltimber.rosehilltreatmentapp.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.DatePickerPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.SharedPreferenceUtility;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SettingsPreferenceFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener
{

    private SharedPreferenceUtility mSharedPreferenceUtility;
    private SharedPreferences mSharedPreferences;

    private EditTextPreference mPasscodeEditTextPreference;
    private EditTextPreference mOneDriveAppIdEditTextPreference;
    private DatePickerPreference mStartOfFinancialYearDatePickerPreference;
    private DatePickerPreference mEndOfFinancialYearDatePickerPreference;
    private EditTextPreference mInitialTreatNumberEditTextPreference;
    private EditTextPreference mMaximumGreenTankVolumeEditTextPreference;
    private EditTextPreference mMaximumBrownTankVolumeEditTextPreference;

    @Override
    public void onCreatePreferencesFix(final Bundle savedInstanceState,
                                    final String rootKey)
    {
        super.setPreferencesFromResource(
                R.xml.fragment_settings_preference,
                rootKey
        );

        super.setHasOptionsMenu(true);

        mSharedPreferenceUtility = SharedPreferenceUtility.getInstance(super.getContext());
        mSharedPreferences = mSharedPreferenceUtility.getSharedPreferences();

        initPreferenceViews();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initPreferenceViews()
    {
        mPasscodeEditTextPreference = (EditTextPreference) initPreferenceView(SharedPreferenceUtility.SharedPreferenceKey.PASSCODE);
        mOneDriveAppIdEditTextPreference = (EditTextPreference) initPreferenceView(SharedPreferenceUtility.SharedPreferenceKey.ONE_DRIVE_APP_ID);
        mStartOfFinancialYearDatePickerPreference = (DatePickerPreference) initPreferenceView(SharedPreferenceUtility.SharedPreferenceKey.START_OF_FINANCIAL_YEAR);
        mEndOfFinancialYearDatePickerPreference = (DatePickerPreference) initPreferenceView(SharedPreferenceUtility.SharedPreferenceKey.END_OF_FINANCIAL_YEAR);
        mInitialTreatNumberEditTextPreference = (EditTextPreference) initPreferenceView(SharedPreferenceUtility.SharedPreferenceKey.INITIAL_TREAT_NUMBER);
        mMaximumGreenTankVolumeEditTextPreference = (EditTextPreference) initPreferenceView(SharedPreferenceUtility.SharedPreferenceKey.MAXIMUM_GREEN_TANK_VOLUME);
        mMaximumBrownTankVolumeEditTextPreference = (EditTextPreference) initPreferenceView(SharedPreferenceUtility.SharedPreferenceKey.MAXIMUM_BROWN_TANK_VOLUME);

        // Invoke on change using all values in order to update summaries.
        for (final SharedPreferenceUtility.SharedPreferenceKey sharedPreferenceKey : SharedPreferenceUtility.SharedPreferenceKey.values()) {
            updatePreferenceViews(sharedPreferenceKey);
        }
    }

    private Preference initPreferenceView(final SharedPreferenceUtility.SharedPreferenceKey sharedPreferenceKey)
    {
        final Preference preference = super.findPreference(mSharedPreferenceUtility.getSharedPreferenceKeyName(sharedPreferenceKey));
        preference.setOnPreferenceChangeListener(this);
        return preference;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue)
    {
        final SharedPreferenceUtility.SharedPreferenceKey sharedPreferenceKey
                = mSharedPreferenceUtility.getSharedPreferenceKey(preference.getKey());
        if (sharedPreferenceKey == null) {
            return false;
        }

        final boolean failed;
        switch (sharedPreferenceKey) {
            case PASSCODE:
                failed = onChangePasscode(newValue);
                break;
            case ONE_DRIVE_APP_ID:
                failed = onChangeOneDriveAppID(newValue);
                break;
            case START_OF_FINANCIAL_YEAR:
                failed = onChangeStartOfFinancialYear(newValue);
                break;
            case END_OF_FINANCIAL_YEAR:
                failed = onChangeEndOfFinancialYear(newValue);
                break;
            case INITIAL_TREAT_NUMBER:
                failed = onChangeInitialTreatNumber(newValue);
                break;
            case MAXIMUM_GREEN_TANK_VOLUME:
                failed = onChangeMaximumGreenTankVolume(newValue);
                break;
            case MAXIMUM_BROWN_TANK_VOLUME:
                failed = onChangeMaximumBrownTankVolume(newValue);
                break;
            default:
                return false;
        }

        if (!failed) {
            updatePreferenceViews(sharedPreferenceKey);
        }

        return failed;
    }

    private boolean onChangePasscode(final Object newValue)
    {
        final String passcode = (String) newValue;
        if (passcode.isEmpty() || passcode.length() < 4) {
            DialogUtility.buildOkAlertDialog(
                    super.getActivity(),
                    R.string.dialog_title_passcode_too_short_cant_set,
                    R.string.dialog_message_passcode_too_short_cant_set
            );
            return false;
        }

        mSharedPreferenceUtility.putPasscode(passcode);
        return true;
    }

    private boolean onChangeOneDriveAppID(final Object newValue)
    {
        final String oneDriveAppID = (String) newValue;
        if (oneDriveAppID.isEmpty()) {
            mSharedPreferenceUtility.remove(SharedPreferenceUtility
                    .SharedPreferenceKey.ONE_DRIVE_APP_ID);
            return false;
        }
        mSharedPreferenceUtility.putOneDriveAppID(oneDriveAppID);
        return true;
    }

    private boolean onChangeStartOfFinancialYear(final Object newValue)
    {
        // Check that the start date is valid.
        final LocalDate startOfFinancialYear = DateUtility.fromDateWrapper(
                (DatePickerPreference.DateWrapper) newValue);

        if (mSharedPreferenceUtility.contains(SharedPreferenceUtility.SharedPreferenceKey.END_OF_FINANCIAL_YEAR)) {
            final LocalDate endOfFinancialYear = mSharedPreferenceUtility.getEndOfFinancialYear();

            if (startOfFinancialYear.isAfter(endOfFinancialYear)) {
                DialogUtility.buildOkAlertDialog(
                        super.getActivity(),
                        R.string.dialog_title_start_of_financial_year_after_end,
                        R.string.dialog_message_start_of_financial_year_after_end
                );
                return false;
            } else if (startOfFinancialYear.isBefore(endOfFinancialYear.minusYears(1))) {
                DialogUtility.buildOkAlertDialog(
                        super.getActivity(),
                        R.string.dialog_title_financial_year_longer_than_year,
                        R.string.dialog_message_financial_year_longer_than_year
                );
            }
        }

        mSharedPreferenceUtility.putStartOfFinancialYear(startOfFinancialYear);
        return true;
    }

    private boolean onChangeEndOfFinancialYear(final Object newValue)
    {
        final LocalDate endOfFinancialYear = DateUtility.fromDateWrapper(
                (DatePickerPreference.DateWrapper) newValue);

        if (mSharedPreferenceUtility.contains(SharedPreferenceUtility.SharedPreferenceKey.START_OF_FINANCIAL_YEAR)) {
            final LocalDate startOfFinancialYear = mSharedPreferenceUtility.getStartOfFinancialYear();

            if (endOfFinancialYear.isBefore(startOfFinancialYear)) {

                Log.wtf("end change: ", endOfFinancialYear.toString() + " " + startOfFinancialYear.toString());
                DialogUtility.buildOkAlertDialog(
                        super.getActivity(),
                        R.string.dialog_title_end_of_financial_year_before_start,
                        R.string.dialog_message_end_of_financial_year_before_start
                );
                return false;
            } else if (endOfFinancialYear.isAfter(startOfFinancialYear.plusYears(1))) {
                DialogUtility.buildOkAlertDialog(
                        super.getActivity(),
                        R.string.dialog_title_financial_year_longer_than_year,
                        R.string.dialog_message_financial_year_longer_than_year
                );
            }
        }
        mSharedPreferenceUtility.putEndOfFinancialYear(endOfFinancialYear);
        return true;
    }

    private boolean onChangeInitialTreatNumber(final Object newValue)
    {
        final int initialTreatNumber;
        try {
            if (((String) newValue).isEmpty()) {
                mSharedPreferenceUtility.remove(SharedPreferenceUtility.SharedPreferenceKey.INITIAL_TREAT_NUMBER);
                return false;
            }
            initialTreatNumber = Integer.parseInt((String) newValue);
        } catch (final NumberFormatException e) {
            DialogUtility.buildOkAlertDialog(
                    super.getActivity(),
                    R.string.dialog_title_initial_treat_number_overflow,
                    R.string.dialog_message_initial_treat_number_overflow
            );
            return false;
        }
        mSharedPreferenceUtility.putInitialTreatNumber(initialTreatNumber);
        return true;
    }

    private boolean onChangeMaximumGreenTankVolume(final Object newValue)
    {
        return onChangeMaximumTankVolume(
                SharedPreferenceUtility.SharedPreferenceKey.MAXIMUM_GREEN_TANK_VOLUME,
                (String) newValue
        );
    }

    private boolean onChangeMaximumBrownTankVolume(final Object newValue)
    {
        return onChangeMaximumTankVolume(
                SharedPreferenceUtility.SharedPreferenceKey.MAXIMUM_BROWN_TANK_VOLUME,
                (String) newValue
        );
    }

    private boolean onChangeMaximumTankVolume(final SharedPreferenceUtility.SharedPreferenceKey tankKey,
                                              final String newValue)
    {
        final float maximumTankVolume;
        try {
            if (newValue.isEmpty()) {
                mSharedPreferenceUtility.remove(tankKey);
                return false;
            }
            maximumTankVolume = Float.parseFloat(newValue);
            if (maximumTankVolume >= Float.MAX_VALUE) {
                DialogUtility.buildOkAlertDialog(
                        super.getActivity(),
                        R.string.dialog_title_max_tank_volume_overflow,
                        R.string.dialog_message_max_tank_volume_overflow
                );
                return false;
            }
        } catch (final NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        mSharedPreferenceUtility.put(tankKey, maximumTankVolume);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key)
    {

        final SharedPreferenceUtility.SharedPreferenceKey sharedPreferenceKey
                = mSharedPreferenceUtility.getSharedPreferenceKey(key);
        if (sharedPreferenceKey == null) {
            return;
        }
        updatePreferenceViews(sharedPreferenceKey);
    }

    private void updatePreferenceViews(final SharedPreferenceUtility.SharedPreferenceKey sharedPreferenceKey)
    {
        Log.wtf("Updating preference for", sharedPreferenceKey.name());
        boolean keyContainsValue = mSharedPreferenceUtility.contains(sharedPreferenceKey);
        switch (sharedPreferenceKey) {

            case PASSCODE:
                if (!keyContainsValue) {
                    mPasscodeEditTextPreference.setSummary(sharedPreferenceKey.getKeySummary());
                    mPasscodeEditTextPreference.setText("");
                    return;
                }
                final String passcode = mSharedPreferenceUtility.getPasscode();
                mPasscodeEditTextPreference.setSummary(passcode);
                mPasscodeEditTextPreference.setText(String.valueOf(mSharedPreferenceUtility.getPasscode()));
                return;

            case ONE_DRIVE_APP_ID:
                if (!keyContainsValue) {
                    mOneDriveAppIdEditTextPreference.setSummary(sharedPreferenceKey.getKeySummary());
                    mOneDriveAppIdEditTextPreference.setText("");
                    return;
                }
                final String oneDriveAppID = mSharedPreferenceUtility.getOneDriveAppID();
                mOneDriveAppIdEditTextPreference.setSummary(oneDriveAppID);
                mOneDriveAppIdEditTextPreference.setText(oneDriveAppID);
                return;

            case START_OF_FINANCIAL_YEAR:
                if (!keyContainsValue) {
                    mStartOfFinancialYearDatePickerPreference.setSummary(sharedPreferenceKey.getKeySummary());
                    mStartOfFinancialYearDatePickerPreference.setPickerDate(DateUtility.toUtilDate(LocalDate.now()));
                    mEndOfFinancialYearDatePickerPreference.setMinDate(null);
                    Log.wtf("Does not contain a start of key", "Hmmm!");
                    return;
                }
                final LocalDate startOfFinancialYear = mSharedPreferenceUtility.getStartOfFinancialYear();
                mStartOfFinancialYearDatePickerPreference.setSummary(startOfFinancialYear.format(DateUtility.FANCY_DATE_FORMATTER));
                mStartOfFinancialYearDatePickerPreference.setDate(DateUtility.toUtilDate(startOfFinancialYear));
                mEndOfFinancialYearDatePickerPreference.setMinDate(DateUtility.toUtilDate(startOfFinancialYear));
                return;

            case END_OF_FINANCIAL_YEAR:
                if (!keyContainsValue) {
                    mEndOfFinancialYearDatePickerPreference.setSummary(sharedPreferenceKey.getKeySummary());
                    mEndOfFinancialYearDatePickerPreference.setPickerDate(DateUtility.toUtilDate(LocalDate.now()));
                    mStartOfFinancialYearDatePickerPreference.setMaxDate(null);
                    return;
                }
                final LocalDate endOfFinancialYear = mSharedPreferenceUtility.getEndOfFinancialYear();
                mEndOfFinancialYearDatePickerPreference.setSummary(endOfFinancialYear.format(DateUtility.FANCY_DATE_FORMATTER));
                mEndOfFinancialYearDatePickerPreference.setDate(DateUtility.toUtilDate(endOfFinancialYear));
                mStartOfFinancialYearDatePickerPreference.setMaxDate(DateUtility.toUtilDate(endOfFinancialYear));
                return;

            case INITIAL_TREAT_NUMBER:
                if (!keyContainsValue) {
                    mInitialTreatNumberEditTextPreference.setSummary(sharedPreferenceKey.getKeySummary());
                    mInitialTreatNumberEditTextPreference.setText("");
                    return;
                }
                final int initialTreatNumber = mSharedPreferenceUtility.getInitialTreatNumber();
                mInitialTreatNumberEditTextPreference.setSummary(String.valueOf(initialTreatNumber));
                mInitialTreatNumberEditTextPreference.setText(String.valueOf(initialTreatNumber));
                return;

            case MAXIMUM_GREEN_TANK_VOLUME:
                if (!keyContainsValue) {
                    mMaximumGreenTankVolumeEditTextPreference.setSummary(sharedPreferenceKey.getKeySummary());
                    mMaximumGreenTankVolumeEditTextPreference.setText("");
                    return;
                }
                final float maximumGreenTankVolume = mSharedPreferenceUtility.getMaximumGreenTankVolume();
                mMaximumGreenTankVolumeEditTextPreference.setSummary(String.valueOf(maximumGreenTankVolume));
                mMaximumGreenTankVolumeEditTextPreference.setText(String.valueOf(maximumGreenTankVolume));
                return;

            case MAXIMUM_BROWN_TANK_VOLUME:
                if (!keyContainsValue) {
                    mMaximumBrownTankVolumeEditTextPreference.setSummary(sharedPreferenceKey.getKeySummary());
                    mMaximumBrownTankVolumeEditTextPreference.setText("");
                    return;
                }
                final float maximumBrownTankVolume = mSharedPreferenceUtility.getMaximumBrownTankVolume();
                mMaximumBrownTankVolumeEditTextPreference.setSummary(Float.toString(maximumBrownTankVolume));
                mMaximumBrownTankVolumeEditTextPreference.setText(Float.toString(maximumBrownTankVolume));
        }
    }

}

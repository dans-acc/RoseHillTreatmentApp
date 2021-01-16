package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class SharedPreferenceUtility
{

    public enum SharedPreferenceKey
    {

        // Define the set of abstracted keys used to access the shared preferences.
        PASSCODE(R.string.shared_preference_key_passcode, R.string.shared_preference_summary_passcode, String.class),
        ONE_DRIVE_APP_ID(R.string.shared_preference_key_one_drive_app_id, R.string.shared_preference_summary_one_drive_app_id, String.class),
        START_OF_FINANCIAL_YEAR(R.string.shared_preference_key_start_of_financial_year, R.string.shared_preference_summary_start_of_financial_year, Long.class),
        END_OF_FINANCIAL_YEAR(R.string.shared_preference_key_end_of_financial_year, R.string.shared_preference_summary_end_of_financial_year, Long.class),
        INITIAL_TREAT_NUMBER(R.string.shared_preference_key_initial_treat_number, R.string.shared_preference_summary_initial_treat_number, Integer.class),
        CURRENT_WEEK(R.string.shared_preference_key_current_week, R.string.shared_preference_summary_current_week, Long.class),
        MAXIMUM_GREEN_TANK_VOLUME(R.string.shared_preference_key_maximum_green_tank_volume, R.string.shared_preference_summary_maximum_green_tank_volume, Float.class),
        MAXIMUM_BROWN_TANK_VOLUME(R.string.shared_preference_key_maximum_brown_tank_volume, R.string.shared_preference_summary_maximum_brown_tank_volume, Float.class);

        // Abstracted preference key attributes.
        @StringRes private final int mKeyName;
        @StringRes private final int mKeySummary;
        private final Class<?> mValueType;

        SharedPreferenceKey(@StringRes final int mKeyName,
                            @StringRes final int mKeySummary,
                            final Class<?> mValueType)
        {
            this.mKeyName = mKeyName;
            this.mKeySummary = mKeySummary;
            this.mValueType = mValueType;
        }

        @StringRes
        public final int getKeyName()
        {
            return mKeyName;
        }

        @StringRes
        public final int getKeySummary()
        {
            return mKeySummary;
        }

        public final Class<?> getValueType()
        {
            return mValueType;
        }
    }

    private static final SharedPreferenceKey[] REQUIRED_APP_PREFERENCES = {
            SharedPreferenceKey.PASSCODE,
            SharedPreferenceKey.ONE_DRIVE_APP_ID,
            SharedPreferenceKey.START_OF_FINANCIAL_YEAR,
            SharedPreferenceKey.END_OF_FINANCIAL_YEAR,
            SharedPreferenceKey.INITIAL_TREAT_NUMBER,
            SharedPreferenceKey.MAXIMUM_GREEN_TANK_VOLUME,
            SharedPreferenceKey.MAXIMUM_BROWN_TANK_VOLUME
    };

    private static final SharedPreferenceKey[] RESET_FINANCIAL_YEAR_KEYS = {
            SharedPreferenceKey.START_OF_FINANCIAL_YEAR,
            SharedPreferenceKey.END_OF_FINANCIAL_YEAR,
            SharedPreferenceKey.INITIAL_TREAT_NUMBER
    };

    private static volatile SharedPreferenceUtility sInstance;

    private final Context mApplicationContext;
    private final SharedPreferences mSharedPreferences;

    private SharedPreferenceUtility(final Context mApplicationContext)
            throws RuntimeException
    {
        if (sInstance != null) {
            throw new RuntimeException("SharedPreferenceUtility has already been initialised.");
        }
        this.mApplicationContext = mApplicationContext;

        mSharedPreferences = mApplicationContext.getSharedPreferences(
                mApplicationContext.getString(R.string.shared_preference_name),
                Context.MODE_PRIVATE
        );
    }

    @UiThread
    public static SharedPreferenceUtility getInstance(final Context context)
    {
        if (sInstance == null) {
            synchronized (SharedPreferenceUtility.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                try {
                    sInstance = new SharedPreferenceUtility(context.getApplicationContext());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return sInstance;
                }
            }
        }
        return sInstance;
    }

    public Context getApplicationContext()
    {
        return mApplicationContext;
    }

    public SharedPreferences getSharedPreferences()
    {
        return mSharedPreferences;
    }

    public boolean areRequiredAppPreferencesSet()
    {
        return contains(REQUIRED_APP_PREFERENCES);
    }

    public boolean contains(final SharedPreferenceKey... sharedPreferenceKeys)
    {
        for (final SharedPreferenceKey sharedPreferenceKey : sharedPreferenceKeys) {
            if (!mSharedPreferences.contains(mApplicationContext.getString(sharedPreferenceKey.mKeyName))) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAllBut(final SharedPreferenceKey... sharedPreferenceKeys)
    {
        final List<SharedPreferenceKey> keysToAvoid = Arrays.asList(sharedPreferenceKeys);
        for (final SharedPreferenceKey sharedPreferenceKey : SharedPreferenceKey.values()) {
            if (!keysToAvoid.contains(sharedPreferenceKey) && !mSharedPreferences.contains(mApplicationContext.getString(sharedPreferenceKey.mKeyName))) {
                return false;
            }
        }
        return true;
    }

    public void remove(final SharedPreferenceKey sharedPreferenceKey)
    {
        mSharedPreferences
                .edit()
                .remove(mApplicationContext.getString(sharedPreferenceKey.mKeyName))
                .apply();
    }

    public void remove(final SharedPreferenceKey... sharedPreferenceKeys)
    {
        if (sharedPreferenceKeys == null || sharedPreferenceKeys.length == 0) {
            return;
        }

        // Remove all of the provided preferences.
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (final SharedPreferenceKey sharedPreferenceKey : sharedPreferenceKeys) {
            editor.remove(mApplicationContext.getString(sharedPreferenceKey.mKeyName));
        }
        editor.apply();
    }

    public void removeAllBut(final SharedPreferenceKey... sharedPreferenceKeys)
    {
        final List<SharedPreferenceKey> keysToKeep = Arrays.asList(sharedPreferenceKeys);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (final SharedPreferenceKey sharedPreferenceKey : SharedPreferenceKey.values()) {
            if (keysToKeep.contains(sharedPreferenceKey)) {
                continue;
            }
            editor.remove(mApplicationContext.getString(sharedPreferenceKey.mKeyName));
        }
        editor.apply();
    }

    public void resetFinancialYearKeys()
    {
        remove(RESET_FINANCIAL_YEAR_KEYS);
    }

    public void clearAll()
    {
        mSharedPreferences.edit().clear().apply();
    }

    public boolean isEmpty()
    {
        return mSharedPreferences.getAll().isEmpty();
    }

    public SharedPreferenceKey getSharedPreferenceKey(final String keyName)
    {
        for (final SharedPreferenceKey sharedPreferenceKey : SharedPreferenceKey.values()) {
            if (mApplicationContext.getString(sharedPreferenceKey.mKeyName).equals(keyName)) {
                return sharedPreferenceKey;
            }
        }
        return null;
    }

    public CharSequence getSharedPreferenceKeyName(final SharedPreferenceKey sharedPreferenceKey)
    {
        return mApplicationContext.getString(sharedPreferenceKey.mKeyName);
    }

    public Object get(final SharedPreferenceKey sharedPreferenceKey)
    {
        final String sharedPreferenceKeyName = mApplicationContext.getString(sharedPreferenceKey.mKeyName);
        final Class<?> sharedPreferenceValueType = sharedPreferenceKey.mValueType;

        // Use the shared preference value type to determine the type of data stored within.
        if (sharedPreferenceValueType == String.class) {
            return mSharedPreferences.getString(sharedPreferenceKeyName, null);
        } else if (sharedPreferenceValueType == Integer.class) {
            return mSharedPreferences.getInt(sharedPreferenceKeyName, -1);
        } else if (sharedPreferenceValueType == Long.class) {
            return mSharedPreferences.getLong(sharedPreferenceKeyName, -1);
        } else if (sharedPreferenceValueType == Float.class) {
            return mSharedPreferences.getFloat(sharedPreferenceKeyName, -1f);
        }

        return null;
    }

    public void put(final SharedPreferenceKey sharedPreferenceKey, final Object value)
    {
        final String sharedPreferenceKeyName = mApplicationContext.getString(sharedPreferenceKey.mKeyName);
        final Class<?> sharedPreferenceValueType = sharedPreferenceKey.mValueType;
        final SharedPreferences.Editor editor = mSharedPreferences.edit();

        // Use the correct shared preference method to add values.
        if (sharedPreferenceValueType == String.class) {
            editor.putString(sharedPreferenceKeyName, (String) value);
        } else if (sharedPreferenceValueType == Integer.class) {
            editor.putInt(sharedPreferenceKeyName, (int) value);
        } else if (sharedPreferenceValueType == Long.class) {
            editor.putLong(sharedPreferenceKeyName, (Long) value);
        } else if (sharedPreferenceValueType == Float.class) {
            editor.putFloat(sharedPreferenceKeyName, (float) value);
        }

        editor.apply();
    }

    public String getPasscode()
    {
        return mSharedPreferences.getString(mApplicationContext.getString(SharedPreferenceKey.PASSCODE.mKeyName), null);
    }

    public void putPasscode(final String passcode)
    {
        mSharedPreferences
                .edit()
                .putString(mApplicationContext.getString(
                        SharedPreferenceKey.PASSCODE.mKeyName),
                        passcode
                ).apply();
    }

    @Nullable
    public String getOneDriveAppID()
    {
        return mSharedPreferences.getString(mApplicationContext.getString(SharedPreferenceKey.ONE_DRIVE_APP_ID.mKeyName), null);
    }

    public void putOneDriveAppID(@Nullable final String microsoftOneDriveAppID)
    {
        mSharedPreferences
                .edit()
                .putString(
                        mApplicationContext.getString(SharedPreferenceKey.ONE_DRIVE_APP_ID.mKeyName),
                        microsoftOneDriveAppID
                ).apply();
    }

    public LocalDate getStartOfFinancialYear()
    {
        return getDate(SharedPreferenceKey.START_OF_FINANCIAL_YEAR);
    }

    public void putStartOfFinancialYear(final LocalDate startOfFinancialYear)
    {
        putDate(SharedPreferenceKey.START_OF_FINANCIAL_YEAR, startOfFinancialYear);
    }

    public LocalDate getEndOfFinancialYear()
    {
        return getDate(SharedPreferenceKey.END_OF_FINANCIAL_YEAR);
    }

    public void putEndOfFinancialYear(final LocalDate endOfFinancialYear)
    {
        putDate(SharedPreferenceKey.END_OF_FINANCIAL_YEAR, endOfFinancialYear);
    }

    public void putFinancialYear(final LocalDate startOfFinancialYear,
                                    final LocalDate endOfFinancialYear)
    {
        mSharedPreferences
                .edit()
                .putLong(
                        mApplicationContext.getString(SharedPreferenceKey.START_OF_FINANCIAL_YEAR.mKeyName),
                        DateUtility.toEpochMilli(startOfFinancialYear)
                )
                .putLong(
                        mApplicationContext.getString(SharedPreferenceKey.END_OF_FINANCIAL_YEAR.mKeyName),
                        DateUtility.toEpochMilli(endOfFinancialYear)
                ).apply();
    }

    public int getInitialTreatNumber()
    {
        return mSharedPreferences.getInt(mApplicationContext.getString(SharedPreferenceKey.INITIAL_TREAT_NUMBER.mKeyName), -1);
    }

    public void putInitialTreatNumber(final int initialTreatNumber)
    {
        mSharedPreferences
                .edit()
                .putInt(
                        mApplicationContext.getString(SharedPreferenceKey.INITIAL_TREAT_NUMBER.mKeyName),
                        initialTreatNumber
                ).apply();
    }

    public LocalDate getCurrentWorkingWeek()
    {
        return getDate(SharedPreferenceKey.CURRENT_WEEK);
    }

    public void putCurrentWeek(final LocalDate currentWorkingWeek)
    {
        putDate(SharedPreferenceKey.CURRENT_WEEK, currentWorkingWeek);
    }

    public float getMaximumGreenTankVolume()
    {
        return mSharedPreferences.getFloat(mApplicationContext.getString(SharedPreferenceKey.MAXIMUM_GREEN_TANK_VOLUME.mKeyName), -1f);
    }

    public void putMaximumGreenTankVolume(final float maximumGreenTankVolume)
    {
        mSharedPreferences
                .edit()
                .putFloat(
                        mApplicationContext.getString(SharedPreferenceKey.MAXIMUM_GREEN_TANK_VOLUME.mKeyName),
                        maximumGreenTankVolume
                ).apply();
    }

    public float getMaximumBrownTankVolume()
    {
        return mSharedPreferences.getFloat(mApplicationContext.getString(SharedPreferenceKey.MAXIMUM_BROWN_TANK_VOLUME.mKeyName), -1f);
    }

    public void putMaximumBrownTankVolume(final float maximumBrownTankVolume)
    {
        mSharedPreferences
                .edit()
                .putFloat(
                        mApplicationContext.getString(SharedPreferenceKey.MAXIMUM_BROWN_TANK_VOLUME.mKeyName),
                        maximumBrownTankVolume
                ).apply();
    }

    private void putDate(final SharedPreferenceKey sharedPreferenceKey, final LocalDate date)
    {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (date == null) {
            editor.remove(mApplicationContext.getString(sharedPreferenceKey.mKeyName));
        } else {
            editor.putLong(
                    mApplicationContext.getString(sharedPreferenceKey.mKeyName),
                    DateUtility.toEpochMilli(date)
            );
        }
        editor.apply();
    }

    private LocalDate getDate(final SharedPreferenceKey sharedPreferenceKey)
    {
        return mSharedPreferences.contains(mApplicationContext.getString(sharedPreferenceKey.mKeyName))
                ? DateUtility.fromEpochMilli(mSharedPreferences.getLong(sharedPreferenceKey.name(), (long) -1))
                : null;
    }
}

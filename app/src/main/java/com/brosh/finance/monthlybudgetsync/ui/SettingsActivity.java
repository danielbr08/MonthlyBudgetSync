package com.brosh.finance.monthlybudgetsync.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.objects.UserSettings;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private User user;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        user = DBUtil.getInstance().getUser();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save settings when leaving the activity (more reliable than onDestroy)
        saveUserSettings();
    }

    private void saveUserSettings() {
        if (user == null) {
            return;
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        UserSettings userSettings = user.getUserSettings();
        userSettings.setChargeDay(Integer.parseInt(prefs.getString(Definitions.CHARGE_DAY, String.valueOf(userSettings.getChargeDay()))));
        userSettings.setCurrency(prefs.getString(Definitions.CURRENCY, userSettings.getCurrency()));
        userSettings.setActiveTransactionsOnlyByDefault(prefs.getBoolean(Definitions.DEFAULT_SHOW_ACTIVE_ONLY, userSettings.isActiveTransactionsOnlyByDefault()));
        userSettings.setAutoCompleteFrom(prefs.getInt(Definitions.AUTO_COMPLETE, userSettings.getAutoCompleteFrom()));
        userSettings.setEmailUpdates(prefs.getBoolean(Definitions.EMAIL_UPDATES, userSettings.isEmailUpdates()));
        userSettings.setNotifications(prefs.getBoolean(Definitions.NOTIFICATIONS, userSettings.isNotifications()));
        userSettings.setAllowEditPreviousMonths(prefs.getBoolean(Definitions.ALLOW_EDIT_PREVIOUS_MONTHS, userSettings.isAllowEditPreviousMonths()));
        String dbKey = user.getDbKey();
        if (dbKey != null) {
            Config.DatabaseReferenceUsers.child(dbKey).child(Definitions.USER_SETTINGS).setValue(userSettings);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private UserSettings userSettings;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            final Context context = this.getContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            User user = DBUtil.getInstance().getUser();
            userSettings = user.getUserSettings();

            Preference chargeDayPref = findPreference(Definitions.CHARGE_DAY);
            Preference profilePref = findPreference(Definitions.PROFILE);
            SeekBarPreference autoCompleteyPref = findPreference(Definitions.AUTO_COMPLETE);
            SwitchPreferenceCompat activeTransactionsOnly = findPreference(Definitions.DEFAULT_SHOW_ACTIVE_ONLY);

            Preference changeUserNamePref = findPreference(Definitions.CHANGE_USER_NAME);
            Preference changeEmailPref = findPreference(Definitions.CHANGE_EMAIL);
            Preference changePasswordPref = findPreference(Definitions.CHANGE_PASSWORD);
            Preference changePhonePref = findPreference(Definitions.CHANGE_PHONE);

            SwitchPreferenceCompat emailUpdates = findPreference(Definitions.EMAIL_UPDATES);
            SwitchPreferenceCompat notifications = findPreference(Definitions.NOTIFICATIONS);
            SwitchPreferenceCompat allowEditPreviousMonths = findPreference(Definitions.ALLOW_EDIT_PREVIOUS_MONTHS);

            // Initialize SharedPreferences with current values
            prefs.edit().putString(Definitions.CURRENCY, userSettings.getCurrency()).commit();
            prefs.edit().putString(Definitions.CHARGE_DAY, String.valueOf(userSettings.getChargeDay())).commit();
            prefs.edit().putBoolean(Definitions.ALLOW_EDIT_PREVIOUS_MONTHS, userSettings.isAllowEditPreviousMonths()).commit();
            prefs.edit().putBoolean(Definitions.DEFAULT_SHOW_ACTIVE_ONLY, userSettings.isActiveTransactionsOnlyByDefault()).commit();
            prefs.edit().putBoolean(Definitions.EMAIL_UPDATES, userSettings.isEmailUpdates()).commit();
            prefs.edit().putBoolean(Definitions.NOTIFICATIONS, userSettings.isNotifications()).commit();
            
            // Set initial UI states
            activeTransactionsOnly.setChecked(userSettings.isActiveTransactionsOnlyByDefault());
            emailUpdates.setChecked(userSettings.isEmailUpdates());
            notifications.setChecked(userSettings.isNotifications());
            allowEditPreviousMonths.setChecked(userSettings.isAllowEditPreviousMonths());

            chargeDayPref.setSummary(String.valueOf(userSettings.getChargeDay()));
            autoCompleteyPref.setValue(userSettings.getAutoCompleteFrom());
            autoCompleteyPref.setSummary(String.valueOf(userSettings.getAutoCompleteFrom()));

            // Only allow the budget owner to change certain settings
            if (!user.isOwner()) {
                chargeDayPref.setEnabled(false);
                allowEditPreviousMonths.setEnabled(false);
            }

            // Add change listeners to update user object immediately
            activeTransactionsOnly.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (Boolean) newValue;
                userSettings.setActiveTransactionsOnlyByDefault(value);
                prefs.edit().putBoolean(Definitions.DEFAULT_SHOW_ACTIVE_ONLY, value).commit();
                return true;
            });

            emailUpdates.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (Boolean) newValue;
                userSettings.setEmailUpdates(value);
                prefs.edit().putBoolean(Definitions.EMAIL_UPDATES, value).commit();
                return true;
            });

            notifications.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (Boolean) newValue;
                userSettings.setNotifications(value);
                prefs.edit().putBoolean(Definitions.NOTIFICATIONS, value).commit();
                return true;
            });

            allowEditPreviousMonths.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (Boolean) newValue;
                userSettings.setAllowEditPreviousMonths(value);
                prefs.edit().putBoolean(Definitions.ALLOW_EDIT_PREVIOUS_MONTHS, value).commit();
                return true;
            });

            autoCompleteyPref.setOnPreferenceChangeListener((preference, newValue) -> {
                int newVal = Integer.parseInt(newValue.toString());
                ((SeekBarPreference) preference).setValue(newVal);
                preference.setSummary(String.valueOf(newVal));
                userSettings.setAutoCompleteFrom(newVal);
                prefs.edit().putInt(Definitions.AUTO_COMPLETE, newVal).commit();
                return false;
            });

            chargeDayPref.setOnPreferenceClickListener(preference -> {
                // Create fresh view each time to avoid stale visual state
                View dayPeekerView = getLayoutInflater().inflate(R.layout.day_peeker, null);
                
                // Get current charge day from userSettings (most up-to-date value)
                int currentChargeDay = userSettings.getChargeDay();
                List<Integer> ids = UiUtil.getIdTVByName((ViewGroup) dayPeekerView, String.valueOf(currentChargeDay));
                int defaultId = (ids != null && !ids.isEmpty()) ? ids.get(0) : R.id.tv1;
                
                final TextView[] currentSelectionTV = {dayPeekerView.findViewById(defaultId)};
                currentSelectionTV[0].setBackgroundResource(R.drawable.circle_pink_style);
                final TextView[] selectedDayTV = {currentSelectionTV[0]};

                // Set up click listeners for all day TextViews
                List<View> allDayTextViews = UiUtil.findAllTextviews((ViewGroup) dayPeekerView);
                for (View view : allDayTextViews) {
                    if (view instanceof TextView dayTV) {
                        dayTV.setOnClickListener(v -> {
                            // Remove selection from previous day
                            UiUtil.restoreBackground(Arrays.asList(selectedDayTV[0]), dayPeekerView.getBackground());
                            // Set selection on clicked day
                            dayTV.setBackgroundResource(R.drawable.circle_pink_style);
                            selectedDayTV[0] = dayTV;
                        });
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.select_charge_day);
                builder.setView(dayPeekerView)
                    .setPositiveButton(R.string.select, (dialog, which) -> {
                        String selectedDayText = selectedDayTV[0].getText().toString().trim();
                        int chargeDay = Integer.parseInt(selectedDayText);
                        userSettings.setChargeDay(chargeDay);
                        chargeDayPref.setSummary(selectedDayText);
                        prefs.edit().putString(Definitions.CHARGE_DAY, selectedDayText).commit();
                    })
                    .setNegativeButton(R.string.cancel, null);
                builder.create().show();
                return true;
            });

            changeUserNamePref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra(Definitions.UPDATE_TYPE, Definitions.UPDATE_USER_NAME);
                startActivity(intent);
                return true;
            });
            changeEmailPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra(Definitions.UPDATE_TYPE, Definitions.UPDATE_EMAIL);
                startActivity(intent);
                return true;
            });
            changePasswordPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra(Definitions.UPDATE_TYPE, Definitions.UPDATE_PASSWORD);
                startActivity(intent);
                return true;
            });
            changePhonePref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra(Definitions.UPDATE_TYPE, Definitions.UPDATE_PHONE_NUMBER);
                startActivity(intent);
                return true;
            });
        }
    }
}
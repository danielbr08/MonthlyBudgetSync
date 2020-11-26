package com.brosh.finance.monthlybudgetsync.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    protected void onDestroy() {
        super.onDestroy();

        saveUserSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveUserSettings() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        UserSettings userSettings = user.getUserSettings();
        userSettings.setChargeDay(Integer.valueOf(prefs.getString(Definitions.CHARGE_DAY, String.valueOf(userSettings.getChargeDay()))));
        userSettings.setCurrency(prefs.getString(Definitions.CURRENCY, userSettings.getCurrency()));
        userSettings.setActiveTransactionsOnlyByDefault(prefs.getBoolean(Definitions.DEFAULT_SHOW_ACTIVE_ONLY, userSettings.isActiveTransactionsOnlyByDefault()));
        userSettings.setAutoCompleteFrom(prefs.getInt(Definitions.AUTO_COMPLETE, userSettings.getAutoCompleteFrom()));
        userSettings.setEmailUpdates(prefs.getBoolean(Definitions.EMAIL_UPDATES, userSettings.isEmailUpdates()));
        userSettings.setNotifications(prefs.getBoolean(Definitions.NOTIFICATIONS, userSettings.isNotifications()));
        Config.DatabaseReferenceUsers.child(user.getDbKey()).child(Definitions.USER_SETTINGS).setValue(userSettings);
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

            prefs.edit().putString(Definitions.CURRENCY, userSettings.getCurrency()).commit();
            prefs.edit().putString(Definitions.CHARGE_DAY, String.valueOf(userSettings.getChargeDay())).commit();
            activeTransactionsOnly.setChecked(userSettings.isActiveTransactionsOnlyByDefault());
            emailUpdates.setChecked(userSettings.isEmailUpdates());
            notifications.setChecked(userSettings.isNotifications());

            chargeDayPref.setSummary(String.valueOf(userSettings.getChargeDay()));
            autoCompleteyPref.setValue(userSettings.getAutoCompleteFrom());
            autoCompleteyPref.setSummary(String.valueOf(userSettings.getAutoCompleteFrom()));

            if (user.getOwnerUid() != null) {
                chargeDayPref.setEnabled(false);
            }

            autoCompleteyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int newVal = Integer.valueOf(newValue.toString());
                    ((SeekBarPreference) preference).setValue(newVal);
                    preference.setSummary(String.valueOf(newVal));
                    userSettings.setAutoCompleteFrom(newVal);
                    return false;
                }
            });

            View dayPeekerView = this.getLayoutInflater().inflate(R.layout.day_peeker, null);
            Integer defaultId = UiUtil.getIdTVByName((ViewGroup) dayPeekerView, String.valueOf(userSettings.getChargeDay())).get(0);
            final TextView defaultSelectionTV[] = {dayPeekerView.findViewById(defaultId)};
            defaultSelectionTV[0].setBackgroundResource(R.drawable.circle_pink_style);
            final TextView selectedDay[] = {defaultSelectionTV[0]};
            final TextView prevSelectedDay[] = {defaultSelectionTV[0]};

            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        defaultSelectionTV[0] = selectedDay[0];
                        String selectedDaytext = selectedDay[0].getText().toString();
                        userSettings.setChargeDay(Integer.valueOf(selectedDaytext));
                        chargeDayPref.setSummary(selectedDaytext);
                        prefs.edit().putString(Definitions.CHARGE_DAY, selectedDaytext).commit();
                        userSettings.setChargeDay(Integer.valueOf(selectedDaytext));
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked - rollback
                        UiUtil.restoreBackground(Arrays.asList(selectedDay[0]), dayPeekerView.getBackground());
                        defaultSelectionTV[0].setBackgroundResource(R.drawable.circle_pink_style);
                        prevSelectedDay[0] = defaultSelectionTV[0];
                        selectedDay[0] = defaultSelectionTV[0];
                }
            };

            chargeDayPref.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.select_charge_day);
                if (dayPeekerView.getParent() != null) {
                    ((ViewGroup) dayPeekerView.getParent()).removeView(dayPeekerView);
                }
                builder.setView(dayPeekerView).setPositiveButton(R.string.select, dialogClickListener)
                        .setNegativeButton(R.string.cancel, dialogClickListener);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            });

            // todo need to make a layout with buttons of 3 types and any one of them will call by click to profile activity
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
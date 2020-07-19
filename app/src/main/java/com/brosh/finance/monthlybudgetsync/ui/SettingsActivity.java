package com.brosh.finance.monthlybudgetsync.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    protected void onDestroy() {
        super.onDestroy();

        // Save user settings
        saveUserSettings();
    }

    private void saveUserSettings() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        UserSettings userSettings = user.getUserSettings();
        userSettings.setChargeDay(Integer.valueOf(prefs.getString("chargeDay", String.valueOf(userSettings.getChargeDay()))));
        userSettings.setCurrency(prefs.getString("currency", userSettings.getCurrency()));
        userSettings.setActiveTransactionsOnlyByDefault(prefs.getBoolean("defaultShowActiveOnly", userSettings.isActiveTransactionsOnlyByDefault()));
        userSettings.setAutoCompleteFrom(prefs.getInt("autoComplete", userSettings.getAutoCompleteFrom()));
        userSettings.setEmailUpdates(prefs.getBoolean("emailUpdates", userSettings.isEmailUpdates()));
        userSettings.setNotifications(prefs.getBoolean("notifications", userSettings.isNotifications()));
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

//            ListPreference currency = findPreference("currency");
            Preference chargeDayPref = findPreference("chargeDay");
            SeekBarPreference autoCompleteyPref = findPreference("autoComplete");
            SwitchPreferenceCompat activeTransactionsOnly = findPreference("defaultShowActiveOnly");
            SwitchPreferenceCompat emailUpdates = findPreference("emailUpdates");
            SwitchPreferenceCompat notifications = findPreference("notifications");

            prefs.edit().putString("currency", userSettings.getCurrency()).commit();
            prefs.edit().putString("chargeDay", String.valueOf(userSettings.getChargeDay())).commit();
            activeTransactionsOnly.setChecked(userSettings.isActiveTransactionsOnlyByDefault());
            emailUpdates.setChecked(userSettings.isEmailUpdates());
            notifications.setChecked(userSettings.isNotifications());
//            prefs.edit().putString("autoComplete", String.valueOf(userSettings.getAutoCompleteFrom())).apply();
//            prefs.edit().putString("defaultShowActiveOnly", String.valueOf(userSettings.isActiveTransactionsOnlyByDefault())).apply();
//            prefs.edit().putString("emailUpdates", String.valueOf(userSettings.isEmailUpdates())).apply();
//            prefs.edit().putString("notifications", String.valueOf(userSettings.isNotifications())).apply();

            chargeDayPref.setSummary(String.valueOf(userSettings.getChargeDay()));
            autoCompleteyPref.setValue(userSettings.getAutoCompleteFrom());
            autoCompleteyPref.setSummary(String.valueOf(userSettings.getAutoCompleteFrom()));
//            activeTransactionsOnly.setPersistent(userSettings.isActiveTransactionsOnlyByDefault());
//            emailUpdates.setPersistent(userSettings.isEmailUpdates());
//            notifications.setPersistent(userSettings.isNotifications());

            if (user.getOwner() != null) {
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
            defaultSelectionTV[0].setBackgroundResource(R.drawable.circle_style);
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
                        prefs.edit().putString("chargeDay", selectedDaytext);
                        user.getUserSettings().setChargeDay(Integer.valueOf(selectedDaytext));
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked - rollback
                        UiUtil.restoreBackground(Arrays.asList(selectedDay[0]), dayPeekerView.getBackground());
                        defaultSelectionTV[0].setBackgroundResource(R.drawable.circle_style);
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
                List<View> textViews = UiUtil.findAllTextviews((ViewGroup) dayPeekerView);
                for (View tv : textViews) {
                    tv.setOnClickListener(tv1 -> {
                        prevSelectedDay[0] = selectedDay[0];
                        selectedDay[0] = (TextView) tv1;
                        UiUtil.restoreBackground(Arrays.asList(prevSelectedDay[0]), new TextView(context).getBackground());
                        selectedDay[0].setBackgroundResource(R.drawable.circle_style);
                    });
                }
                alertDialog.show();
                return true;
            });
        }
    }

}
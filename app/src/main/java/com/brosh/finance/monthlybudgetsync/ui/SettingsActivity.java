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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.utils.LocaleHelper;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.objects.UserSettings;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private User user;
    private SharedPreferences prefs;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

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

            Preference languagePref = findPreference("language");
            Preference currencyPref = findPreference(Definitions.CURRENCY);
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
            
            // Check if user has premium access
            boolean isPremium = userSettings.isPremium();
            
            // Setup language preference with custom paginated dialog
            if (languagePref != null) {
                setupLanguagePreference(context, languagePref, isPremium);
            }
            
            // Setup currency preference with custom paginated dialog
            if (currencyPref != null) {
                setupCurrencyPreference(context, currencyPref, userSettings, isPremium, prefs);
            }
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

            // Language and Currency preferences are handled by custom dialogs set up above

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
        
        private static final int ITEMS_PER_PAGE = 5;
        
        /**
         * Sets up the language preference with a custom paginated dialog.
         * Shows main options (System Default, English, device locale) always visible,
         * plus paginated "more" options for premium users.
         */
        private void setupLanguagePreference(Context context, Preference languagePref, boolean isPremium) {
            String[] allEntries = getResources().getStringArray(R.array.language_entries);
            String[] allValues = getResources().getStringArray(R.array.language_values);
            
            // Set initial summary
            String currentLang = LocaleHelper.getPersistedLanguage(context);
            languagePref.setSummary(getLanguageDisplayName(allEntries, allValues, currentLang));
            
            String deviceLang = Locale.getDefault().getLanguage();
            if (deviceLang.equals("zh")) {
                String country = Locale.getDefault().getCountry();
                deviceLang = country.equals("TW") || country.equals("HK") ? "zh-TW" : "zh-CN";
            }
            
            List<String> mainEntries = new ArrayList<>();
            List<String> mainValues = new ArrayList<>();
            List<String> moreEntries = new ArrayList<>();
            List<String> moreValues = new ArrayList<>();
            
            for (int i = 0; i < allValues.length; i++) {
                String langCode = allValues[i];
                if (langCode.equals("system") || langCode.equals("en") || 
                    langCode.equals(deviceLang) || 
                    (deviceLang.equals("he") && langCode.equals("iw"))) {
                    mainEntries.add(allEntries[i]);
                    mainValues.add(langCode);
                } else {
                    moreEntries.add(allEntries[i]);
                    moreValues.add(langCode);
                }
            }
            
            languagePref.setOnPreferenceClickListener(preference -> {
                showPaginatedLanguageDialog(context, languagePref, 
                    mainEntries, mainValues, moreEntries, moreValues, isPremium);
                return true;
            });
        }
        
        private String getLanguageDisplayName(String[] entries, String[] values, String langCode) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(langCode)) {
                    return entries[i];
                }
            }
            return langCode;
        }
        
        /**
         * Shows a custom dialog with paginated language options.
         */
        private void showPaginatedLanguageDialog(Context context, Preference languagePref,
                                                  List<String> mainEntries, List<String> mainValues,
                                                  List<String> moreEntries, List<String> moreValues,
                                                  boolean isPremium) {
            String currentValue = LocaleHelper.getPersistedLanguage(context);
            final int[] currentPage = {0};
            final int totalPages = (int) Math.ceil((double) moreEntries.size() / ITEMS_PER_PAGE);
            final String[] selectedValue = {currentValue};
            
            // Check if current selection is in "more" list and set initial page
            for (int i = 0; i < moreValues.size(); i++) {
                if (moreValues.get(i).equals(currentValue)) {
                    currentPage[0] = i / ITEMS_PER_PAGE;
                    break;
                }
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.language);
            
            LinearLayout mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(24, 16, 24, 0);
            
            // Main options (always visible) - use LinearLayout for manual control
            LinearLayout mainRadioGroup = new LinearLayout(context);
            mainRadioGroup.setOrientation(LinearLayout.VERTICAL);
            
            for (int i = 0; i < mainEntries.size(); i++) {
                RadioButton rb = createStyledRadioButton(context, mainEntries.get(i), mainValues.get(i));
                if (mainValues.get(i).equals(currentValue)) {
                    rb.setChecked(true);
                }
                final String value = mainValues.get(i);
                rb.setOnClickListener(v -> {
                    selectedValue[0] = value;
                    // Uncheck all, then check clicked one
                    for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                        ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                    }
                    ((RadioButton) v).setChecked(true);
                });
                mainRadioGroup.addView(rb);
            }
            mainLayout.addView(mainRadioGroup);
            
            // Divider and "More options" section for premium
            if (isPremium && !moreEntries.isEmpty()) {
                // Divider
                View divider = new View(context);
                divider.setBackgroundColor(context.getResources().getColor(R.color.colorDivider, null));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2);
                dividerParams.setMargins(0, 24, 0, 16);
                divider.setLayoutParams(dividerParams);
                mainLayout.addView(divider);
                
                // "More options" label
                TextView moreLabel = new TextView(context);
                moreLabel.setText(R.string.more_options);
                moreLabel.setTextSize(14);
                moreLabel.setTextColor(context.getResources().getColor(R.color.colorTextSecondary, null));
                moreLabel.setPadding(12, 0, 0, 8);
                mainLayout.addView(moreLabel);
                
                // Paginated radio group container - use LinearLayout for manual control
                LinearLayout paginatedContainer = new LinearLayout(context);
                paginatedContainer.setOrientation(LinearLayout.VERTICAL);
                
                LinearLayout moreRadioGroup = new LinearLayout(context);
                moreRadioGroup.setOrientation(LinearLayout.VERTICAL);
                paginatedContainer.addView(moreRadioGroup);
                
                // Navigation bar - arrows direction based on RTL/LTR
                LinearLayout navBar = new LinearLayout(context);
                navBar.setOrientation(LinearLayout.HORIZONTAL);
                navBar.setGravity(android.view.Gravity.CENTER);
                navBar.setPadding(0, 16, 0, 8);
                
                // RTL: back is right (▶), forward is left (◀)
                // LTR: back is left (◀), forward is right (▶)
                boolean isRtl = context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                String prevArrow = isRtl ? " ▶" : " ◀";
                String nextArrow = isRtl ? " ◀" : " ▶";
                
                TextView prevBtn = new TextView(context);
                prevBtn.setText(getString(R.string.previous) + prevArrow);
                prevBtn.setPadding(32, 16, 32, 16);
                prevBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary, null));
                
                TextView pageIndicator = new TextView(context);
                pageIndicator.setPadding(24, 16, 24, 16);
                pageIndicator.setTextColor(context.getResources().getColor(R.color.colorTextSecondary, null));
                
                TextView nextBtn = new TextView(context);
                nextBtn.setText(getString(R.string.next) + nextArrow);
                nextBtn.setPadding(32, 16, 32, 16);
                nextBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary, null));
                
                // Always: [Prev] [Page] [Next]
                navBar.addView(prevBtn);
                navBar.addView(pageIndicator);
                navBar.addView(nextBtn);
                paginatedContainer.addView(navBar);
                
                // Update page function
                Runnable updatePage = () -> {
                    moreRadioGroup.removeAllViews();
                    int start = currentPage[0] * ITEMS_PER_PAGE;
                    int end = Math.min(start + ITEMS_PER_PAGE, moreEntries.size());
                    
                    for (int i = start; i < end; i++) {
                        RadioButton rb = createStyledRadioButton(context, moreEntries.get(i), moreValues.get(i));
                        if (moreValues.get(i).equals(selectedValue[0])) {
                            rb.setChecked(true);
                            // Uncheck all in main group
                            for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                                ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                            }
                        }
                        final String value = moreValues.get(i);
                        rb.setOnClickListener(v -> {
                            selectedValue[0] = value;
                            // Uncheck all in main group
                            for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                                ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                            }
                            // Uncheck all in more group, then check this one
                            for (int j = 0; j < moreRadioGroup.getChildCount(); j++) {
                                ((RadioButton) moreRadioGroup.getChildAt(j)).setChecked(false);
                            }
                            ((RadioButton) v).setChecked(true);
                        });
                        moreRadioGroup.addView(rb);
                    }
                    
                    pageIndicator.setText(String.format(getString(R.string.page_indicator), 
                        currentPage[0] + 1, totalPages));
                    prevBtn.setAlpha(currentPage[0] > 0 ? 1.0f : 0.3f);
                    nextBtn.setAlpha(currentPage[0] < totalPages - 1 ? 1.0f : 0.3f);
                };
                
                prevBtn.setOnClickListener(v -> {
                    if (currentPage[0] > 0) {
                        currentPage[0]--;
                        updatePage.run();
                    }
                });
                
                nextBtn.setOnClickListener(v -> {
                    if (currentPage[0] < totalPages - 1) {
                        currentPage[0]++;
                        updatePage.run();
                    }
                });
                
                // Update main group click listeners to also clear more group
                for (int i = 0; i < mainRadioGroup.getChildCount(); i++) {
                    RadioButton mainRb = (RadioButton) mainRadioGroup.getChildAt(i);
                    final String value = (String) mainRb.getTag();
                    mainRb.setOnClickListener(v -> {
                        selectedValue[0] = value;
                        // Uncheck all in more group
                        for (int j = 0; j < moreRadioGroup.getChildCount(); j++) {
                            ((RadioButton) moreRadioGroup.getChildAt(j)).setChecked(false);
                        }
                        // Uncheck all in main group, then check this one
                        for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                            ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                        }
                        ((RadioButton) v).setChecked(true);
                    });
                }
                
                mainLayout.addView(paginatedContainer);
                updatePage.run();
            }
            
            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(mainLayout);
            
            builder.setView(scrollView)
                .setPositiveButton(R.string.select, (dialog, which) -> {
                    if (selectedValue[0] != null && !selectedValue[0].equals(currentValue)) {
                        LocaleHelper.setPersistedLanguage(context, selectedValue[0]);
                        languagePref.setSummary(findEntryForValue(mainEntries, mainValues, 
                            moreEntries, moreValues, selectedValue[0]));
                        
                        android.widget.Toast.makeText(context, R.string.language_changed_restart, 
                            android.widget.Toast.LENGTH_SHORT).show();
                        
                        Intent intent = new Intent(context, com.brosh.finance.monthlybudgetsync.login.Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        
                        if (getActivity() != null) getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        }
        
        /**
         * Sets up the currency preference with a custom paginated dialog.
         */
        private void setupCurrencyPreference(Context context, Preference currencyPref,
                                              UserSettings userSettings, boolean isPremium, 
                                              SharedPreferences prefs) {
            String[] allEntries = getResources().getStringArray(R.array.currency_entries);
            String[] allValues = getResources().getStringArray(R.array.currency_values);
            
            String localeCurrency = context.getString(R.string.default_currency);
            String currentCurrency = userSettings.getCurrency();
            
            // Set initial summary
            currencyPref.setSummary(getCurrencyDisplayName(allEntries, allValues, currentCurrency));
            
            List<String> mainEntries = new ArrayList<>();
            List<String> mainValues = new ArrayList<>();
            List<String> moreEntries = new ArrayList<>();
            List<String> moreValues = new ArrayList<>();
            
            for (int i = 0; i < allValues.length; i++) {
                String currencyValue = allValues[i];
                if (currencyValue.equals("$") || currencyValue.equals(localeCurrency) || 
                    currencyValue.equals(currentCurrency)) {
                    mainEntries.add(allEntries[i]);
                    mainValues.add(currencyValue);
                } else {
                    moreEntries.add(allEntries[i]);
                    moreValues.add(currencyValue);
                }
            }
            
            currencyPref.setOnPreferenceClickListener(preference -> {
                showPaginatedCurrencyDialog(context, currencyPref, userSettings,
                    mainEntries, mainValues, moreEntries, moreValues, isPremium, prefs);
                return true;
            });
        }
        
        private String getCurrencyDisplayName(String[] entries, String[] values, String currencyCode) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(currencyCode)) {
                    return entries[i];
                }
            }
            return currencyCode;
        }
        
        /**
         * Shows a custom dialog with paginated currency options.
         */
        private void showPaginatedCurrencyDialog(Context context, Preference currencyPref,
                                                  UserSettings userSettings,
                                                  List<String> mainEntries, List<String> mainValues,
                                                  List<String> moreEntries, List<String> moreValues,
                                                  boolean isPremium, SharedPreferences prefs) {
            String currentValue = userSettings.getCurrency();
            final int[] currentPage = {0};
            final int totalPages = (int) Math.ceil((double) moreEntries.size() / ITEMS_PER_PAGE);
            final String[] selectedValue = {currentValue};
            
            // Check if current selection is in "more" list
            for (int i = 0; i < moreValues.size(); i++) {
                if (moreValues.get(i).equals(currentValue)) {
                    currentPage[0] = i / ITEMS_PER_PAGE;
                    break;
                }
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.currency);
            
            LinearLayout mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(24, 16, 24, 0);
            
            // Main options - use LinearLayout for manual control
            LinearLayout mainRadioGroup = new LinearLayout(context);
            mainRadioGroup.setOrientation(LinearLayout.VERTICAL);
            
            for (int i = 0; i < mainEntries.size(); i++) {
                RadioButton rb = createStyledRadioButton(context, mainEntries.get(i), mainValues.get(i));
                if (mainValues.get(i).equals(currentValue)) {
                    rb.setChecked(true);
                }
                final String value = mainValues.get(i);
                rb.setOnClickListener(v -> {
                    selectedValue[0] = value;
                    // Uncheck all, then check clicked one
                    for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                        ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                    }
                    ((RadioButton) v).setChecked(true);
                });
                mainRadioGroup.addView(rb);
            }
            mainLayout.addView(mainRadioGroup);
            
            // More options section for premium
            if (isPremium && !moreEntries.isEmpty()) {
                View divider = new View(context);
                divider.setBackgroundColor(context.getResources().getColor(R.color.colorDivider, null));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2);
                dividerParams.setMargins(0, 24, 0, 16);
                divider.setLayoutParams(dividerParams);
                mainLayout.addView(divider);
                
                TextView moreLabel = new TextView(context);
                moreLabel.setText(R.string.more_options);
                moreLabel.setTextSize(14);
                moreLabel.setTextColor(context.getResources().getColor(R.color.colorTextSecondary, null));
                moreLabel.setPadding(12, 0, 0, 8);
                mainLayout.addView(moreLabel);
                
                LinearLayout paginatedContainer = new LinearLayout(context);
                paginatedContainer.setOrientation(LinearLayout.VERTICAL);
                
                LinearLayout moreRadioGroup = new LinearLayout(context);
                moreRadioGroup.setOrientation(LinearLayout.VERTICAL);
                paginatedContainer.addView(moreRadioGroup);
                
                // Navigation bar - arrows direction based on RTL/LTR
                LinearLayout navBar = new LinearLayout(context);
                navBar.setOrientation(LinearLayout.HORIZONTAL);
                navBar.setGravity(android.view.Gravity.CENTER);
                navBar.setPadding(0, 16, 0, 8);
                
                // RTL: back is right (▶), forward is left (◀)
                // LTR: back is left (◀), forward is right (▶)
                boolean isRtl = context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                String prevArrow = isRtl ? " ▶" : " ◀";
                String nextArrow = isRtl ? " ◀" : " ▶";
                
                TextView prevBtn = new TextView(context);
                prevBtn.setText(getString(R.string.previous) + prevArrow);
                prevBtn.setPadding(32, 16, 32, 16);
                prevBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary, null));
                
                TextView pageIndicator = new TextView(context);
                pageIndicator.setPadding(24, 16, 24, 16);
                pageIndicator.setTextColor(context.getResources().getColor(R.color.colorTextSecondary, null));
                
                TextView nextBtn = new TextView(context);
                nextBtn.setText(getString(R.string.next) + nextArrow);
                nextBtn.setPadding(32, 16, 32, 16);
                nextBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary, null));
                
                // Always: [Prev] [Page] [Next]
                navBar.addView(prevBtn);
                navBar.addView(pageIndicator);
                navBar.addView(nextBtn);
                paginatedContainer.addView(navBar);
                
                Runnable updatePage = () -> {
                    moreRadioGroup.removeAllViews();
                    int start = currentPage[0] * ITEMS_PER_PAGE;
                    int end = Math.min(start + ITEMS_PER_PAGE, moreEntries.size());
                    
                    for (int i = start; i < end; i++) {
                        RadioButton rb = createStyledRadioButton(context, moreEntries.get(i), moreValues.get(i));
                        if (moreValues.get(i).equals(selectedValue[0])) {
                            rb.setChecked(true);
                            // Uncheck all in main group
                            for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                                ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                            }
                        }
                        final String value = moreValues.get(i);
                        rb.setOnClickListener(v -> {
                            selectedValue[0] = value;
                            // Uncheck all in main group
                            for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                                ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                            }
                            // Uncheck all in more group, then check this one
                            for (int j = 0; j < moreRadioGroup.getChildCount(); j++) {
                                ((RadioButton) moreRadioGroup.getChildAt(j)).setChecked(false);
                            }
                            ((RadioButton) v).setChecked(true);
                        });
                        moreRadioGroup.addView(rb);
                    }
                    
                    pageIndicator.setText(String.format(getString(R.string.page_indicator), 
                        currentPage[0] + 1, totalPages));
                    prevBtn.setAlpha(currentPage[0] > 0 ? 1.0f : 0.3f);
                    nextBtn.setAlpha(currentPage[0] < totalPages - 1 ? 1.0f : 0.3f);
                };
                
                prevBtn.setOnClickListener(v -> {
                    if (currentPage[0] > 0) {
                        currentPage[0]--;
                        updatePage.run();
                    }
                });
                
                nextBtn.setOnClickListener(v -> {
                    if (currentPage[0] < totalPages - 1) {
                        currentPage[0]++;
                        updatePage.run();
                    }
                });
                
                // Update main group click listeners to also clear more group
                for (int i = 0; i < mainRadioGroup.getChildCount(); i++) {
                    RadioButton mainRb = (RadioButton) mainRadioGroup.getChildAt(i);
                    final String value = (String) mainRb.getTag();
                    mainRb.setOnClickListener(v -> {
                        selectedValue[0] = value;
                        // Uncheck all in more group
                        for (int j = 0; j < moreRadioGroup.getChildCount(); j++) {
                            ((RadioButton) moreRadioGroup.getChildAt(j)).setChecked(false);
                        }
                        // Uncheck all in main group, then check this one
                        for (int j = 0; j < mainRadioGroup.getChildCount(); j++) {
                            ((RadioButton) mainRadioGroup.getChildAt(j)).setChecked(false);
                        }
                        ((RadioButton) v).setChecked(true);
                    });
                }
                
                mainLayout.addView(paginatedContainer);
                updatePage.run();
            }
            
            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(mainLayout);
            
            builder.setView(scrollView)
                .setPositiveButton(R.string.select, (dialog, which) -> {
                    if (selectedValue[0] != null) {
                        userSettings.setCurrency(selectedValue[0]);
                        prefs.edit().putString(Definitions.CURRENCY, selectedValue[0]).commit();
                        currencyPref.setSummary(findEntryForValue(mainEntries, mainValues, 
                            moreEntries, moreValues, selectedValue[0]));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        }
        
        // Helper methods
        
        private RadioButton createStyledRadioButton(Context context, String text, String value) {
            RadioButton rb = new RadioButton(context);
            rb.setText(text);
            rb.setTag(value);
            rb.setPadding(12, 20, 12, 20);
            rb.setTextSize(16);
            return rb;
        }
        
        private String findEntryForValue(List<String> mainEntries, List<String> mainValues,
                                         List<String> moreEntries, List<String> moreValues, String value) {
            for (int i = 0; i < mainValues.size(); i++) {
                if (mainValues.get(i).equals(value)) return mainEntries.get(i);
            }
            for (int i = 0; i < moreValues.size(); i++) {
                if (moreValues.get(i).equals(value)) return moreEntries.get(i);
            }
            return value;
        }
    }
}
package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.preference.PreferenceManager;

import java.util.Locale;

/**
 * Helper class for managing app locale/language settings.
 * Allows users to override the system language with English.
 */
public class LocaleHelper {

    private static final String LANGUAGE_KEY = "language";
    private static final String LANGUAGE_SYSTEM = "system";
    private static final String LANGUAGE_ENGLISH = "en";

    /**
     * Apply the saved language preference to the given context.
     * Call this in attachBaseContext() of activities.
     *
     * @param context The base context
     * @return Context with updated locale configuration
     */
    public static Context applyLocale(Context context) {
        String language = getPersistedLanguage(context);
        
        if (LANGUAGE_SYSTEM.equals(language)) {
            // Use system default - no override needed
            return context;
        }
        
        return updateResources(context, language);
    }

    /**
     * Get the currently saved language preference.
     *
     * @param context Context to access SharedPreferences
     * @return Language code ("system" or "en")
     */
    public static String getPersistedLanguage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(LANGUAGE_KEY, LANGUAGE_SYSTEM);
    }

    /**
     * Save the language preference.
     *
     * @param context  Context to access SharedPreferences
     * @param language Language code to save
     */
    public static void setPersistedLanguage(Context context, String language) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(LANGUAGE_KEY, language).apply();
    }

    /**
     * Check if the app is using the system default language.
     *
     * @param context Context to access SharedPreferences
     * @return true if using system default
     */
    public static boolean isUsingSystemLanguage(Context context) {
        return LANGUAGE_SYSTEM.equals(getPersistedLanguage(context));
    }

    /**
     * Update the app resources with the specified locale.
     *
     * @param context  Context to update
     * @param language Language code
     * @return Updated context
     */
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new LocaleList(locale));
            return context.createConfigurationContext(config);
        } else {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        }
    }

    /**
     * Get the display name for a language code.
     *
     * @param context      Context for string resources
     * @param languageCode Language code
     * @return Display name for the language
     */
    public static String getLanguageDisplayName(Context context, String languageCode) {
        if (LANGUAGE_SYSTEM.equals(languageCode)) {
            return context.getString(com.brosh.finance.monthlybudgetsync.R.string.language_system_default);
        } else if (LANGUAGE_ENGLISH.equals(languageCode)) {
            return context.getString(com.brosh.finance.monthlybudgetsync.R.string.language_english);
        }
        return languageCode;
    }
}

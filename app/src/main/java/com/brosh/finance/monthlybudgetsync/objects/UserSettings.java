package com.brosh.finance.monthlybudgetsync.objects;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;

import java.io.Serial;
import java.io.Serializable;

/**
 * User preferences and settings.
 * Note: Setters are required for Firebase deserialization.
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class UserSettings implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    // Default values
    private static final int DEFAULT_CHARGE_DAY = 1;
    private static final boolean DEFAULT_AD_ENABLED = true;
    private static final String DEFAULT_CURRENCY = "$";
    private static final int DEFAULT_AUTO_COMPLETE_FROM = 2;
    private static final boolean DEFAULT_ACTIVE_ONLY = true;
    private static final boolean DEFAULT_EMAIL_UPDATES = false;
    private static final boolean DEFAULT_NOTIFICATIONS = false;
    
    private int chargeDay;
    private boolean isAdEnabled;
    @NonNull private String currency;
    private int autoCompleteFrom;
    private boolean activeTransactionsOnlyByDefault;
    private boolean emailUpdates;
    private boolean notifications;

    /**
     * Creates UserSettings with all parameters specified.
     */
    public UserSettings(int chargeDay, boolean isAdEnabled, @Nullable String currency, 
                        int autoCompleteFrom, boolean activeTransactionsOnlyByDefault, 
                        boolean emailUpdates, boolean notifications) {
        this.chargeDay = Math.max(1, Math.min(31, chargeDay));
        this.isAdEnabled = isAdEnabled;
        this.currency = currency != null ? currency : DEFAULT_CURRENCY;
        this.autoCompleteFrom = Math.max(1, autoCompleteFrom);
        this.activeTransactionsOnlyByDefault = activeTransactionsOnlyByDefault;
        this.emailUpdates = emailUpdates;
        this.notifications = notifications;
    }

    /**
     * Default constructor with sensible defaults.
     */
    public UserSettings() {
        this.chargeDay = DEFAULT_CHARGE_DAY;
        this.isAdEnabled = DEFAULT_AD_ENABLED;
        this.autoCompleteFrom = DEFAULT_AUTO_COMPLETE_FROM;
        this.activeTransactionsOnlyByDefault = DEFAULT_ACTIVE_ONLY;
        this.emailUpdates = DEFAULT_EMAIL_UPDATES;
        this.notifications = DEFAULT_NOTIFICATIONS;
        
        // Try to get default currency from resources
        try {
            Context context = DBUtil.getInstance().getContext();
            if (context != null) {
                this.currency = context.getString(R.string.default_currency);
            } else {
                this.currency = DEFAULT_CURRENCY;
            }
        } catch (Exception e) {
            this.currency = DEFAULT_CURRENCY;
        }
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getAutoCompleteFrom() {
        return autoCompleteFrom;
    }

    public void setAutoCompleteFrom(int autoCompleteFrom) {
        this.autoCompleteFrom = autoCompleteFrom;
    }

    public boolean isActiveTransactionsOnlyByDefault() {
        return activeTransactionsOnlyByDefault;
    }

    public void setActiveTransactionsOnlyByDefault(boolean activeTransactionsOnlyByDefault) {
        this.activeTransactionsOnlyByDefault = activeTransactionsOnlyByDefault;
    }

    public boolean isEmailUpdates() {
        return emailUpdates;
    }

    public void setEmailUpdates(boolean emailUpdates) {
        this.emailUpdates = emailUpdates;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public int getChargeDay() {
        return chargeDay;
    }

    public void setChargeDay(int chargeDay) {
        this.chargeDay = chargeDay;
    }

    public boolean isAdEnabled() {
        return isAdEnabled;
    }

    public void setAdEnabled(boolean adEnabled) {
        isAdEnabled = adEnabled;
    }
}

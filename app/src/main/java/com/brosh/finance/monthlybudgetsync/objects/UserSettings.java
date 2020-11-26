package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;

import java.io.Serializable;

public class UserSettings implements Serializable {
    private int chargeDay;
    private boolean isAdEnabled;
    private String currency;
    private int autoCompleteFrom;
    private boolean activeTransactionsOnlyByDefault;
    private boolean emailUpdates;
    private boolean notifications;

    public UserSettings(int chargeDay, boolean isAdEnabled, String currency, int autoCompleteFrom, boolean activeTransactionsOnlyByDefault, boolean emailUpdates, boolean notifications) {
        this.chargeDay = chargeDay;
        this.isAdEnabled = isAdEnabled;
        this.currency = currency;
        this.autoCompleteFrom = autoCompleteFrom;
        this.activeTransactionsOnlyByDefault = activeTransactionsOnlyByDefault;
        this.emailUpdates = emailUpdates;
        this.notifications = notifications;
    }

    public UserSettings() {
        this.chargeDay = 1;
        this.isAdEnabled = true;
        this.currency = DBUtil.getInstance().getContext().getString(R.string.default_currency);
        this.autoCompleteFrom = 2;
        this.activeTransactionsOnlyByDefault = true;
        this.emailUpdates = false;
        this.notifications = false;
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

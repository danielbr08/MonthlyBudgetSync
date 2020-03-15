package com.brosh.finance.monthlybudgetsync.config;

public class UserConfig {
    private String language;
    private int chargeDay;
    private boolean isAdEnabled;

    public UserConfig(String language, int chargeDay, boolean isAdEnabled) {
        this.language = language;
        this.chargeDay = chargeDay;
        this.isAdEnabled = isAdEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

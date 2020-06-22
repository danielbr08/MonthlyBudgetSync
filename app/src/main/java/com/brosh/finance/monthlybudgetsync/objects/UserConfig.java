package com.brosh.finance.monthlybudgetsync.objects;

import java.io.Serializable;

public class UserConfig implements Serializable {
    private int chargeDay;
    private boolean isAdEnabled;

    public UserConfig(int chargeDay, boolean isAdEnabled) {
        this.chargeDay = chargeDay;
        this.isAdEnabled = isAdEnabled;
    }

    public UserConfig() {
        this.chargeDay = 1;
        this.isAdEnabled = true;
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

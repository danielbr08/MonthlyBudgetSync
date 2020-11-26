package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String dbKey;
    private String ownerUid; // if he is the owner of the DB

    private UserSettings userSettings;

    public String getEmail() {
        return email;
    }

    @Exclude
    public String getEmailComma() {
        return TextUtil.getEmailComma(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User() {
    }

    public User(String uid, String name, String email, String phone, String dbKey) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dbKey = dbKey != null ? dbKey : uid;
        this.ownerUid = uid;
        this.userSettings = new UserSettings();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    @Exclude
    public boolean isOwner() {
        return this.uid.equals(dbKey);
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
}

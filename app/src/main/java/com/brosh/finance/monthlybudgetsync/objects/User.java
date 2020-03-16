package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.config.UserConfig;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String dbKey;
    private boolean isOwner; // if he is the owner of the DB

    private UserConfig usserConfig;
    //private String groupID;

    public String getEmail() {
        return email;
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

//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

    //    public String getGroupID() {
//        return groupID;
//    }
//
//    public void setGroupID(String groupID) {
//        this.groupID = groupID;
//    }
//
//

    public User() {
    }

    public User(String name, String email, String phone, String password, String dbKey, boolean isOwner) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.dbKey = dbKey;
        this.isOwner = isOwner;
    }

    public User(String name, String email, String phone, String password, String dbKey) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dbKey = dbKey;
        this.password = password;
    }

    public User(String name, String email, String phone, String dbKey) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dbKey = dbKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public UserConfig getUsserConfig() {
        return usserConfig;
    }

    public void setUsserConfig(UserConfig usserConfig) {
        this.usserConfig = usserConfig;
    }
}

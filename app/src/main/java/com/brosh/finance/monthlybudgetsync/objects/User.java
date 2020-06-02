package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.config.UserConfig;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.TextService;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String dbKey;
    private String owner; // if he is the owner of the DB

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

    public User(String name, String email, String phone, String password, String dbKey, String owner) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.dbKey = dbKey != null ? dbKey : TextService.getEmailComma(email);
        this.owner = owner;
    }

    public User(String name, String email, String phone, String password, String dbKey) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dbKey = dbKey != null ? dbKey : TextService.getEmailComma(email);
        this.password = password;
        this.owner = null;
        this.usserConfig = new UserConfig();
    }

    public User(String name, String email, String phone, String dbKey) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dbKey = dbKey != null ? dbKey : TextService.getEmailComma(email);
        this.owner = null;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public UserConfig getUsserConfig() {
        return usserConfig;
    }

    public void setUsserConfig(UserConfig usserConfig) {
        this.usserConfig = usserConfig;
    }
}

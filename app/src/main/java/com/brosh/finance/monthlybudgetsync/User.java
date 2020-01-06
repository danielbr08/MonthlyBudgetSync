package com.brosh.finance.monthlybudgetsync;

public class User {
    private String name;
    private String email;
    private String phone;
    private String password;
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

    public User(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}

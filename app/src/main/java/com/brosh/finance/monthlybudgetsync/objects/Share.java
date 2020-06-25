package com.brosh.finance.monthlybudgetsync.objects;

public class Share {
    private String ownerEmail;
    private String userEmail;
    private String dbKey;
    private ShareStatus status;

    public Share() {
    }

    public Share(String ownerEmail, String userEmail, String dbKey, ShareStatus status) {
        this.ownerEmail = ownerEmail;
        this.userEmail = userEmail;
        this.dbKey = dbKey;
        this.status = status;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public ShareStatus getStatus() {
        return status;
    }

    public void setStatus(ShareStatus status) {
        this.status = status;
    }
}

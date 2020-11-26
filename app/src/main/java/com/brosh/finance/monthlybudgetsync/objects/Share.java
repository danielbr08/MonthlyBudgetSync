package com.brosh.finance.monthlybudgetsync.objects;

public class Share {
    private String guestUid;
    private String ownerUid;
    private String guestEmail;
    private String dbKey;
    private ShareStatus status;

    public Share() {
    }

    public Share(String guestUid, String ownerUid, String guestEmail, String dbKey, ShareStatus status) {
        this.guestUid = guestUid;
        this.ownerUid = ownerUid;
        this.guestEmail = guestEmail;
        this.dbKey = dbKey;
        this.status = status;
    }

    public String getGuestUid() {
        return guestUid;
    }

    public void setGuestUid(String guestUid) {
        this.guestUid = guestUid;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public ShareStatus getStatus() {
        return status;
    }

    public void setStatus(ShareStatus status) {
        this.status = status;
    }
}

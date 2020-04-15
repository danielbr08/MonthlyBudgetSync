package com.brosh.finance.monthlybudgetsync.objects;

public class Share {
    private String owner;
    private String user;
    private String status;

    public Share() {
    }

    public Share(String owner, String user, String status) {
        this.owner = owner;
        this.user = user;
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

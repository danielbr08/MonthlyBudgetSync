package com.brosh.finance.monthlybudgetsync.objects;

import java.io.Serializable;
import java.util.Date;

public class ContactUs implements Serializable {

    private String subject;
    private String message;
    boolean active;
    Date creationDate;

    public ContactUs() {
    }

    public ContactUs(String subject, String message, boolean active, Date creationDate) {
        this.subject = subject;
        this.message = message;
        this.active = active;
        this.creationDate = creationDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}

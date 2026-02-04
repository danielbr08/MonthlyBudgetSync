package com.brosh.finance.monthlybudgetsync.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.google.firebase.database.Exclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a user of the application.
 * Contains authentication info, profile data, and settings.
 * Note: Setters are required for Firebase deserialization.
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Nullable private String uid;
    @Nullable private String name;
    @Nullable private String email;
    @Nullable private String phone;
    @Nullable private String dbKey;
    @Nullable private String ownerUid;  // If sharing someone else's budget, this is the owner's UID
    @NonNull private UserSettings userSettings;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public User() {
        this.userSettings = new UserSettings();
    }

    /**
     * Creates a new User with the specified parameters.
     */
    public User(@Nullable String uid, @Nullable String name, @Nullable String email, 
                @Nullable String phone, @Nullable String dbKey) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dbKey = dbKey != null ? dbKey : uid;
        this.ownerUid = uid;
        this.userSettings = new UserSettings();
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    /**
     * Returns email with dots replaced by commas (for Firebase key compatibility).
     */
    @Exclude
    @NonNull
    public String getEmailComma() {
        return TextUtil.getEmailComma(email);
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }

    @Nullable
    public String getUid() {
        return uid;
    }

    public void setUid(@Nullable String uid) {
        this.uid = uid;
    }

    @Nullable
    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(@Nullable String dbKey) {
        this.dbKey = dbKey;
    }

    @Nullable
    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(@Nullable String ownerUid) {
        this.ownerUid = ownerUid;
    }

    /**
     * Checks if this user owns their budget (not sharing someone else's).
     */
    @Exclude
    public boolean isOwner() {
        return uid != null && uid.equals(dbKey);
    }

    /**
     * Returns the user settings. Never null.
     */
    @NonNull
    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(@Nullable UserSettings userSettings) {
        this.userSettings = userSettings != null ? userSettings : new UserSettings();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User other = (User) obj;
        return Objects.equals(uid, other.uid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isOwner=" + isOwner() +
                '}';
    }
}

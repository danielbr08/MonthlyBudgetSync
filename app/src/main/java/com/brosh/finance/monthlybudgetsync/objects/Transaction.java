package com.brosh.finance.monthlybudgetsync.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a financial transaction within a budget category.
 * Note: Setters are required for Firebase deserialization.
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class Transaction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Nullable private String id;
    private int idPerMonth;
    @Nullable private String category;
    @Nullable private String paymentMethod;
    @Nullable private String shop;
    @Nullable private Date payDate;
    private double price;
    @Nullable private Date registrationDate;
    private boolean deleted;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Transaction() {
    }

    /**
     * Normalizes date fields to a consistent format.
     */
    @SuppressWarnings("unused") // Called from constructor and may be used for future functionality
    public void formatDateFields() {
        if (this.registrationDate != null) {
            this.registrationDate = DateUtil.changeDateFormat(this.registrationDate, Config.DATE_FORMAT);
        }
        if (this.payDate != null) {
            this.payDate = DateUtil.changeDateFormat(this.payDate, Config.DATE_FORMAT);
        }
    }

    /**
     * Creates a new Transaction with the specified parameters.
     */
    public Transaction(@Nullable String id, int idPerMonth, @Nullable String category, 
                       @Nullable String paymentMethod, @Nullable String shop, 
                       @Nullable Date payDate, double price) {
        this.id = id;
        this.idPerMonth = idPerMonth;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.shop = shop;
        this.payDate = payDate;
        this.price = Math.max(0, price);  // Ensure non-negative
        this.registrationDate = DateUtil.getTodayDate();
        this.deleted = false;

        formatDateFields();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Transaction other = (Transaction) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", price=" + price +
                ", deleted=" + deleted +
                '}';
    }

    @Nullable
    public String getId() {
        return id;
    }

    public int getIdPerMonth() {
        return idPerMonth;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    @Nullable
    public String getPaymentMethod() {
        return paymentMethod;
    }

    @Nullable
    public String getShop() {
        return shop;
    }

    @Nullable
    public Date getPayDate() {
        return payDate;
    }

    public double getPrice() {
        return price;
    }

    @Nullable
    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setIdPerMonth(int idPerMonth) {
        this.idPerMonth = idPerMonth;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    public void setPaymentMethod(@Nullable String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setShop(@Nullable String shop) {
        this.shop = shop;
    }

    public void setPayDate(@Nullable Date payDate) {
        this.payDate = payDate;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRegistrationDate(@Nullable Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
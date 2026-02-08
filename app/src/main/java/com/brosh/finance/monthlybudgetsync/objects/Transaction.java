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
 * 
 * <p>Tracks payment details including amount, date, payment method,
 * and soft-delete status.</p>
 * 
 * <p>Note: Setters are required for Firebase deserialization.</p>
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ============================================
    // FIELDS
    // ============================================

    @Nullable
    private String id;

    private int idPerMonth;

    @Nullable
    private String category;

    @Nullable
    private String paymentMethod;

    @Nullable
    private String shop;

    @Nullable
    private Date payDate;

    private double price;

    @Nullable
    private Date registrationDate;

    private boolean deleted;

    @Nullable
    private String comment;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Transaction() {
    }

    /**
     * Creates a new Transaction with the specified parameters.
     *
     * @param id            unique identifier
     * @param idPerMonth    sequential ID within the month
     * @param category      the category name
     * @param paymentMethod how the payment was made
     * @param shop          where the purchase was made
     * @param payDate       when the payment occurred
     * @param price         the transaction amount
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
        this.price = Math.max(0, price);
        this.registrationDate = DateUtil.getTodayDate();
        this.deleted = false;

        formatDateFields();
    }

    // ============================================
    // OBJECT METHODS
    // ============================================

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
                ", idPerMonth=" + idPerMonth +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", price=" + price +
                ", deleted=" + deleted +
                ", comment='" + comment + '\'' +
                '}';
    }

    // ============================================
    // GETTERS
    // ============================================

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

    public boolean isDeleted() {
        return deleted;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    // ============================================
    // SETTERS
    // ============================================

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
        this.price = Math.max(0, price);
    }

    public void setRegistrationDate(@Nullable Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Normalizes date fields to a consistent format.
     */
    public void formatDateFields() {
        if (this.registrationDate != null) {
            this.registrationDate = DateUtil.changeDateFormat(this.registrationDate, Config.DATE_FORMAT);
        }
        if (this.payDate != null) {
            this.payDate = DateUtil.changeDateFormat(this.payDate, Config.DATE_FORMAT);
        }
    }

    /**
     * Checks if this is an active (non-deleted) transaction.
     *
     * @return true if not deleted
     */
    public boolean isActive() {
        return !deleted;
    }

    /**
     * Marks this transaction as deleted (soft delete).
     */
    public void markDeleted() {
        this.deleted = true;
    }

    /**
     * Restores a deleted transaction.
     */
    public void restore() {
        this.deleted = false;
    }
}

package com.brosh.finance.monthlybudgetsync.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a budget category with its settings.
 * Used to define recurring and one-time budget allocations.
 * Note: Setters are required for Firebase deserialization.
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class Budget implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Nullable private String id;
    @Nullable private String categoryName;
    private int value;
    private boolean isConstPayment;
    @Nullable private String shop;
    private int chargeDay;
    private int catPriority;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Budget() {
    }

    /**
     * Creates a new Budget with the specified parameters.
     */
    public Budget(@Nullable String categoryName, int value, boolean isConstPayment, 
                  @Nullable String shop, int chargeDay, int catPriority) {
        this.categoryName = categoryName;
        this.value = Math.max(0, value);  // Ensure non-negative
        this.isConstPayment = isConstPayment;
        this.shop = shop;
        this.chargeDay = Math.max(1, Math.min(31, chargeDay));  // Clamp to valid day range
        this.catPriority = catPriority;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Budget other = (Budget) obj;
        return value == other.value
                && isConstPayment == other.isConstPayment
                && chargeDay == other.chargeDay
                && Objects.equals(categoryName, other.categoryName)
                && Objects.equals(shop, other.shop);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(categoryName, value, isConstPayment, shop, chargeDay);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Budget{" +
                "id='" + id + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", value=" + value +
                ", isConstPayment=" + isConstPayment +
                ", shop='" + shop + '\'' +
                ", chargeDay=" + chargeDay +
                ", catPriority=" + catPriority +
                '}';
    }

    @Nullable
    public String getCategoryName() {
        return categoryName;
    }

    public int getValue() {
        return value;
    }

    public boolean isConstPayment() {
        return isConstPayment;
    }

    @Nullable
    public String getShop() {
        return shop;
    }

    public int getChargeDay() {
        return chargeDay;
    }

    public int getCatPriority() {
        return catPriority;
    }

    public void setCategoryName(@Nullable String categoryName) {
        this.categoryName = categoryName;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setConstPayment(boolean constPayment) {
        isConstPayment = constPayment;
    }

    public void setShop(@Nullable String shop) {
        this.shop = shop;
    }

    public void setChargeDay(int chargeDay) {
        this.chargeDay = chargeDay;
    }

    public void setCatPriority(int catPriority) {
        this.catPriority = catPriority;
    }
}



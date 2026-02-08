package com.brosh.finance.monthlybudgetsync.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a budget category template with its settings.
 * Used to define recurring and one-time budget allocations.
 * 
 * <p>Note: Setters are required for Firebase deserialization.</p>
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class Budget implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    // ============================================
    // FIELDS
    // ============================================

    @Nullable 
    private String id;
    
    @Nullable 
    private String categoryName;
    
    private int value;
    private boolean isConstPayment;
    
    @Nullable 
    private String shop;
    
    private int chargeDay;
    private int catPriority;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Budget() {
    }

    /**
     * Creates a new Budget with the specified parameters.
     *
     * @param categoryName  the name of the budget category
     * @param value         the budget amount (will be clamped to non-negative)
     * @param isConstPayment whether this is a recurring constant payment
     * @param shop          the shop name for constant payments
     * @param chargeDay     the day of month for charging (1-31)
     * @param catPriority   the display priority of the category
     */
    public Budget(@Nullable String categoryName, int value, boolean isConstPayment,
                  @Nullable String shop, int chargeDay, int catPriority) {
        this.categoryName = categoryName;
        this.value = Math.max(0, value);
        this.isConstPayment = isConstPayment;
        this.shop = shop;
        this.chargeDay = clampChargeDay(chargeDay);
        this.catPriority = catPriority;
    }

    // ============================================
    // OBJECT METHODS
    // ============================================

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

    // ============================================
    // GETTERS
    // ============================================

    @Nullable
    public String getId() {
        return id;
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

    // ============================================
    // SETTERS
    // ============================================

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setCategoryName(@Nullable String categoryName) {
        this.categoryName = categoryName;
    }

    public void setValue(int value) {
        this.value = Math.max(0, value);
    }

    public void setConstPayment(boolean constPayment) {
        this.isConstPayment = constPayment;
    }

    public void setShop(@Nullable String shop) {
        this.shop = shop;
    }

    public void setChargeDay(int chargeDay) {
        this.chargeDay = clampChargeDay(chargeDay);
    }

    public void setCatPriority(int catPriority) {
        this.catPriority = catPriority;
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Clamps the charge day to a valid range (1-31).
     */
    private static int clampChargeDay(int day) {
        return Math.max(1, Math.min(31, day));
    }
}
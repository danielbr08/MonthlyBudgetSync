package com.brosh.finance.monthlybudgetsync.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.google.firebase.database.Exclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a budget month with its categories.
 * Immutable where possible, with controlled access to mutable collections.
 * Note: Setters are required for Firebase deserialization.
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class Month implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int DEFAULT_TRAN_ID_NUMERATOR = 1;

    @Nullable private String id;
    @Nullable private Date refMonth;
    @Nullable private String yearMonth;
    private boolean isActive;
    private int chargeDay;
    @NonNull private Map<String, Category> categories;
    private int tranIdNumerator;
    private long budgetNumber;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Month() {
        this.categories = new HashMap<>();
        this.tranIdNumerator = DEFAULT_TRAN_ID_NUMERATOR;
    }

    /**
     * Creates a new Month with the specified parameters.
     * 
     * @param yearMonth the year-month string (e.g., "2024-01")
     * @param budgetNumber the budget number/version
     * @param chargeDay the day of month when charges apply
     */
    public Month(@NonNull String yearMonth, long budgetNumber, int chargeDay) {
        this.chargeDay = chargeDay;
        this.id = yearMonth;
        this.budgetNumber = budgetNumber;
        this.categories = new HashMap<>();
        this.refMonth = DateUtil.setDayToDate(DateUtil.getDate(yearMonth), chargeDay);
        this.yearMonth = yearMonth;
        this.tranIdNumerator = DEFAULT_TRAN_ID_NUMERATOR;
        updateActiveStatus();
    }

    public int getChargeDay() {
        return chargeDay;
    }

    public void setChargeDay(int chargeDay) {
        this.chargeDay = chargeDay;
    }

    public long getBudgetNumber() {
        return budgetNumber;
    }

    public void setBudgetNumber(long budgetNumber) {
        this.budgetNumber = budgetNumber;
    }

    /**
     * Updates a specific category in this month.
     * 
     * @param categoryId the category ID
     * @param category the category to update
     */
    public void updateSpecificCategory(@Nullable String categoryId, @Nullable Category category) {
        if (categoryId != null && category != null) {
            this.categories.put(categoryId, category);
        }
    }

    @Nullable
    public Date getRefMonth() {
        return refMonth;
    }

    public void setRefMonth(@Nullable Date refMonth) {
        this.refMonth = refMonth;
    }

    /**
     * Checks if this month is the active (current) month.
     * 
     * @return true if this is the current month
     */
    @Exclude
    public boolean isActive() {
        return isActive;
    }

    public void setCategories(@Nullable Map<String, Category> categories) {
        this.categories = categories != null ? categories : new HashMap<>();
    }

    /**
     * Updates the active status based on the current date.
     * Call this to refresh the active status.
     */
    public void setIsActive() {
        updateActiveStatus();
    }
    
    /**
     * Updates the active status by comparing refMonth to today's date.
     */
    private void updateActiveStatus() {
        Date today = DateUtil.getTodayDate();
        isActive = refMonth != null && DateUtil.isSameYearMonth(refMonth, today);
    }

    public int getTranIdNumerator() {
        return tranIdNumerator;
    }

    public void setTranIdNumerator(int tranIdNumerator) {
        this.tranIdNumerator = tranIdNumerator;
    }
    
    /**
     * Returns the next transaction ID and increments the numerator.
     * 
     * @return the next transaction ID
     */
    @Exclude
    public int getNextTransactionId() {
        return tranIdNumerator++;
    }

    /**
     * Returns the categories map. Never null.
     * 
     * @return the categories map
     */
    @NonNull
    public Map<String, Category> getCategories() {
        return categories;
    }
    
    /**
     * Returns an unmodifiable view of the categories.
     * Use this when you need read-only access.
     * 
     * @return unmodifiable map of categories
     */
    @Exclude
    @NonNull
    public Map<String, Category> getCategoriesReadOnly() {
        return Collections.unmodifiableMap(categories);
    }

    /**
     * Adds a category to this month.
     * 
     * @param categoryId the category ID
     * @param category the category to add
     */
    public void addCategory(@Nullable String categoryId, @Nullable Category category) {
        if (categoryId != null && category != null) {
            categories.put(categoryId, category);
        }
    }
    
    /**
     * Checks if a category exists in this month.
     * 
     * @param categoryId the category ID to check
     * @return true if the category exists
     */
    @Exclude
    public boolean hasCategory(@Nullable String categoryId) {
        return categoryId != null && categories.containsKey(categoryId);
    }
    
    /**
     * Gets a category by ID.
     * 
     * @param categoryId the category ID
     * @return the category, or null if not found
     */
    @Exclude
    @Nullable
    public Category getCategory(@Nullable String categoryId) {
        return categoryId != null ? categories.get(categoryId) : null;
    }
    
    /**
     * Returns the number of categories in this month.
     * 
     * @return category count
     */
    @Exclude
    public int getCategoryCount() {
        return categories.size();
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(@Nullable String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Month other = (Month) obj;
        return Objects.equals(id, other.id) && Objects.equals(yearMonth, other.yearMonth);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, yearMonth);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Month{" +
                "id='" + id + '\'' +
                ", yearMonth='" + yearMonth + '\'' +
                ", isActive=" + isActive +
                ", categories=" + categories.size() +
                '}';
    }
}

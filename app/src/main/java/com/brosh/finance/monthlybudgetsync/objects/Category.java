package com.brosh.finance.monthlybudgetsync.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a budget category with its current balance and transactions.
 * 
 * <p>A Category is an instance of a Budget for a specific month, tracking
 * the allocated budget, remaining balance, and all transactions.</p>
 * 
 * <p>Note: Setters are required for Firebase deserialization.</p>
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class Category implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ============================================
    // FIELDS
    // ============================================

    @Nullable
    private String id;

    @Nullable
    private String name;

    private int budget;
    private double balance;

    @NonNull
    private Map<String, Transaction> transactions;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Category() {
        this.transactions = new HashMap<>();
    }

    /**
     * Creates a new Category with the specified parameters.
     *
     * @param id      unique identifier for this category
     * @param name    display name of the category
     * @param balance current remaining balance
     * @param budget  allocated budget amount
     */
    public Category(@Nullable String id, @Nullable String name, double balance, int budget) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budget = Math.max(0, budget);
        this.transactions = new HashMap<>();
    }

    // ============================================
    // OBJECT METHODS
    // ============================================

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        Category cloned = (Category) super.clone();
        cloned.transactions = new HashMap<>(this.transactions);
        return cloned;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Category other = (Category) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @NonNull
    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", budget=" + budget +
                ", balance=" + balance +
                ", transactionCount=" + transactions.size() +
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
    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Returns the transactions map. Never null.
     */
    @NonNull
    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    // ============================================
    // SETTERS
    // ============================================

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setBudget(int budget) {
        this.budget = Math.max(0, budget);
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setTransactions(@Nullable Map<String, Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new HashMap<>();
    }

    // ============================================
    // TRANSACTION OPERATIONS
    // ============================================

    /**
     * Adds a transaction to this category.
     *
     * @param id          the transaction ID
     * @param transaction the transaction to add
     */
    public void addTransactions(@NonNull String id, @NonNull Transaction transaction) {
        this.transactions.put(id, transaction);
    }

    /**
     * Removes a transaction from this category.
     *
     * @param id the transaction ID to remove
     * @return the removed transaction, or null if not found
     */
    @Nullable
    public Transaction removeTransaction(@NonNull String id) {
        return this.transactions.remove(id);
    }

    /**
     * Gets a transaction by ID.
     *
     * @param id the transaction ID
     * @return the transaction, or null if not found
     */
    @Nullable
    public Transaction getTransaction(@NonNull String id) {
        return this.transactions.get(id);
    }

    /**
     * Checks if a transaction exists.
     *
     * @param id the transaction ID
     * @return true if the transaction exists
     */
    public boolean hasTransaction(@NonNull String id) {
        return this.transactions.containsKey(id);
    }

    // ============================================
    // BALANCE OPERATIONS
    // ============================================

    /**
     * Withdraws an amount from the balance.
     *
     * @param amount the amount to withdraw (positive value)
     */
    public void withdrawal(double amount) {
        this.balance -= Math.abs(amount);
    }

    /**
     * Deposits an amount to the balance.
     *
     * @param amount the amount to deposit (positive value)
     */
    public void deposit(double amount) {
        this.balance += Math.abs(amount);
    }

    // ============================================
    // COMPUTED PROPERTIES
    // ============================================

    /**
     * Calculates the total amount spent (budget - balance).
     *
     * @return the total spent amount
     */
    public double getTotalSpent() {
        return budget - balance;
    }

    /**
     * Checks if the category is over budget.
     *
     * @return true if balance is negative
     */
    public boolean isOverBudget() {
        return balance < 0;
    }

    /**
     * Calculates the percentage of budget used.
     *
     * @return percentage (0-100+), or 0 if budget is 0
     */
    public double getUsagePercentage() {
        if (budget == 0) return 0;
        return (getTotalSpent() / budget) * 100;
    }
}

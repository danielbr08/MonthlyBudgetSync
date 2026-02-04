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
 * Note: Setters are required for Firebase deserialization.
 */
@SuppressWarnings("unused") // Setters used by Firebase deserialization
public class Category implements Serializable, Cloneable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Nullable private String id;
    @Nullable private String name;
    private double balance;
    private int budget;
    @NonNull private Map<String, Transaction> transactions;

    /**
     * Creates a deep copy of this category.
     * Note: Transactions map is shallow copied.
     */
    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        Category cloned = (Category) super.clone();
        cloned.transactions = new HashMap<>(this.transactions);
        return cloned;
    }

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Category() {
        this.transactions = new HashMap<>();
    }

    /**
     * Creates a new Category with the specified parameters.
     */
    public Category(@Nullable String id, @Nullable String name, double balance, int budget) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budget = Math.max(0, budget);  // Ensure non-negative
        this.transactions = new HashMap<>();
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
                ", balance=" + balance +
                ", budget=" + budget +
                ", transactionCount=" + transactions.size() +
                '}';
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }


    /**
     * Returns the transactions map. Never null.
     */
    @NonNull
    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(@Nullable Map<String, Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new HashMap<>();
    }

    /**
     * Adds a transaction to this category.
     */
    public void addTransactions(@NonNull String id, @NonNull Transaction transaction) {
        this.transactions.put(id, transaction);
    }

    /**
     * Withdraws an amount from the balance.
     * @param amount the amount to withdraw (positive value)
     */
    public void withdrawal(double amount) {
        this.balance -= Math.abs(amount);
    }
    
    /**
     * Deposits an amount to the balance.
     * @param amount the amount to deposit (positive value)
     */
    @SuppressWarnings("unused") // May be used for future functionality
    public void deposit(double amount) {
        this.balance += Math.abs(amount);
    }
    
    /**
     * Calculates the total spent (budget - balance).
     */
    @SuppressWarnings("unused") // May be used for future functionality
    public double getTotalSpent() {
        return budget - balance;
    }
    
    /**
     * Checks if the category is over budget.
     */
    @SuppressWarnings("unused") // May be used for future functionality
    public boolean isOverBudget() {
        return balance < 0;
    }
}

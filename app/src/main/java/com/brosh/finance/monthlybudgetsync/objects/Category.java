package com.brosh.finance.monthlybudgetsync.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Category implements Serializable, Cloneable {
    private String id;

    private String name;
    private double balance;
    private int budget;
    private Map<String, Transaction> transactions;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Category() {
        this.transactions = new HashMap<>();
    }

    public Category(String id, String name, double balance, int budget) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budget = budget;
        this.transactions = new HashMap<>();

    }

    public Category(String id, String name, double balance, int budget, Map<String, Transaction> transactions) {
        // services

        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budget = budget;
        this.transactions = transactions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }


    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransactions(String id, Transaction transactions) {
        this.transactions.put(id, transactions);
    }

    public void withdrawal(double trnPrice) {
        this.balance -= trnPrice;
    }

    public void addTransaction(Transaction trn) {
        this.transactions.put(trn.getId(), trn);
    }

}

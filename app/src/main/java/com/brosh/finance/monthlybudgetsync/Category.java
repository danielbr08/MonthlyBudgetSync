package com.brosh.finance.monthlybudgetsync;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Category implements Serializable {
    private String id;

    private String name;
    private double balance;
    private int budget;
    private Map<String, Transaction> transactionHMDB;

    public Category(String id, String name, double balance, int budget) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budget = budget;
        this.transactionHMDB = new HashMap<>();

    }

    public Category(String id, String name, double balance, int budget, Map<String, Transaction> transactionHMDB){
        // services

        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budget = budget;
        this.transactionHMDB = transactionHMDB;
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


    public Map<String, Transaction> getTransactionHMDB() {
        return transactionHMDB;
    }

    public void setTransactionHMDB(Map<String, Transaction> transactionHMDB) {
        this.transactionHMDB = transactionHMDB;
    }

    public void addTransactions(String id, Transaction transactions) {
        this.transactionHMDB.put(id,transactions);
    }

    public void withdrawal(double trnPrice){
        this.balance -= trnPrice;
    }

}

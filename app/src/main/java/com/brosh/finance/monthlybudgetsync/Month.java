package com.brosh.finance.monthlybudgetsync;

import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Month {
    private Map<String,Budget> budgetHMDBData;
    private Map<String,Transaction> transactionHMDBData;
    private Map<String,Category> categoryHMDBData;
    // services
    private DateService dateService;
    private DBService dbService;

    private String id;
    private Date refMonth;
    private boolean isActive;
    private Map<String,Category> categories;
    private int tranIdNumerator;

    //private ArrayList<Transaction> transactions;

    public Month(DBService dbService, DateService dateService, String id, Date refMonth)
    {
        this.dateService = dateService;
        this.dbService = dbService;

        this.budgetHMDBData = new HashMap<String,Budget>();
        this.transactionHMDBData = new HashMap<String,Transaction>();
        this.categoryHMDBData = new HashMap<String,Category>();

        this.id = id;
        this.refMonth = refMonth;
        this.categories = new HashMap<String,Category>();
        this.tranIdNumerator = 1;
//        transactions = new ArrayList<Transaction>();
        setIsActive();
//        initCategories();
    }

    public void updateSpecificBudget(String id, Budget bgt){
        this.budgetHMDBData.put(id, bgt);
    }

    public void updateSpecificTransaction(String id, Transaction trn){
        this.transactionHMDBData.put(id, trn);
    }

    public void updateSpecificCategory(String id, Category cat){
        this.categoryHMDBData.put(id, cat);
    }

    public Date getRefMonth() {
        return refMonth;
    }

    public void setRefMonth(Date refMonth) {
        this.refMonth = refMonth;
    }

    public boolean isActive() {
        return isActive;
    }

    public Map<String,Category> getCategories() {
        return categories;
    }

    public void setCategories(Map<String,Category> categories) {
        this.categories = categories;
    }

    private void setIsActive(){
        Date today = this.dateService.getTodayDate();
        isActive = refMonth.getYear() == today.getYear() && refMonth.getMonth() == today.getMonth();
    }

    public int getTranIdNumerator() {
        return tranIdNumerator;
    }

    public void setTranIdNumerator(int tranIdNumerator) {
        this.tranIdNumerator = tranIdNumerator;
    }

    public Map<String, Budget> getBudgetHMDBData() {
        return budgetHMDBData;
    }

    public Map<String, Transaction> getTransactionHMDBData() {
        return transactionHMDBData;
    }

    public Map<String, Category> getCategoryHMDBData() {
        return categoryHMDBData;
    }

    public String getId() {
        return id;
    }

    public void initCategories()
    {
//        if(!this.dbService.isCurrentMBExists()) {
//            this.dbService.writeMBFromBudget(this.refMonth);
//        }
//        categories = getCategoriesFromDB();
    }
}



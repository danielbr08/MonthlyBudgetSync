package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Month implements Serializable {
//    private Map<String,HashMap<String,Budget>> budgetHMDBData;
    private Map<String,Transaction> transactionHMDB;
    private Map<String,Category> categoryHMDB;
    // services
    private DateService dateService;
    private DBService dbService;

    private String id;
    private Date refMonth;
    private boolean isActive;
    private Map<String,Category> categories;
    private int tranIdNumerator;
    private String budgetNumber;

    //private ArrayList<Transaction> transactions;

//    public Month(DBService dbService, DateService dateService, String id, Date refMonth)
    public Month(String id, Date refMonth)
    {
//        this.dateService = dateService;
//        this.dbService = dbService;

//        this.budgetHMDB = new HashMap<String,HashMap<String,Budget>>();
        this.transactionHMDB = new HashMap<String,Transaction>();
        this.categoryHMDB = new HashMap<String,Category>();

        this.id = id;
        this.refMonth = refMonth;
        this.categories = new HashMap<String,Category>();
        this.tranIdNumerator = 1;
//        transactions = new ArrayList<Transaction>();
        setIsActive();
//        initCategories();
    }

    public String getBudgetNumber() {
        return budgetNumber;
    }

    public void setBudgetNumber(String budgetNumber) {
        this.budgetNumber = budgetNumber;
    }

//    public void updateSpecificBudget(String id, Budget bgt){
//        this.budgetHMDB.put(id, bgt);
//    }

    public void updateSpecificTransaction(String id, Transaction trn){
        this.transactionHMDB.put(id, trn);
    }

    public void updateSpecificCategory(String id, Category cat){
        this.categoryHMDB.put(id, cat);
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

//    public Map<String, Budget> getbudgetHMDB() {
//        return budgetHMDB;
//    }

    public Map<String, Transaction> getTransactionHMDB() {
        return transactionHMDB;
    }

    public Map<String, Category> getCategoryHMDB() {
        return categoryHMDB;
    }

    public void addCategory(String id, Category category){
        categoryHMDB.put(id,category);
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



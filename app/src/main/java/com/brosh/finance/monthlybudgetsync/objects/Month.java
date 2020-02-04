package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Month implements Serializable {
    // services
    private DateService dateService;
//    private DBService dbService;

    private String id;
    private Date refMonth;
    private boolean isActive;
    private Map<String,Category> categoryHMDB;
    private int tranIdNumerator;
    private String budgetNumber;

    public Month() {
        categoryHMDB = new HashMap<String,Category>();
    }

    //    public Month(DBService dbService, DateService dateService, String id, Date refMonth)
    public Month(String refMonth)
    {
//        this.dateService = dateService;
//        this.dbService = dbService;

        this.id = refMonth;
        this.categoryHMDB = new HashMap<String,Category>();
        this.refMonth = DateService.getDate(refMonth);
        this.tranIdNumerator = 1;
        setIsActive();
//        initCategories();
    }

    public DateService getDateService() {
        return dateService;
    }

    public void setDateService(DateService dateService) {
        this.dateService = dateService;
    }

    public String getBudgetNumber() {
        return budgetNumber;
    }

    public void setBudgetNumber(String budgetNumber) {
        this.budgetNumber = budgetNumber;
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

    public void setCategories(Map<String,Category> categories) {
        this.categoryHMDB = categories;
    }

    private void setIsActive(){
        Date today = this.dateService.getTodayDate();
        isActive = DateService.isSameYearMonth(refMonth,today);
    }

    public int getTranIdNumerator() {
        return tranIdNumerator;
    }

    public void setTranIdNumerator(int tranIdNumerator) {
        this.tranIdNumerator = tranIdNumerator;
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



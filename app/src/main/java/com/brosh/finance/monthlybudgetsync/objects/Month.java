package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Month implements Serializable {

    private String id;
    private Date refMonth;
    private String yearMonth;
    private boolean isActive;
    private Map<String, Category> categories;

    private int tranIdNumerator;
    private long budgetNumber;

    public Month() {
        categories = new HashMap<String, Category>();
    }

    //    public Month(DBService dbService, DateService dateService, String id, Date refMonth)

    public Month(String yearMonth, long budgetNumber) {
//        this.dateService = dateService;
//        this.dbService = dbService;

        this.id = yearMonth;
        this.budgetNumber = budgetNumber;
        this.categories = new HashMap<String, Category>();
        this.refMonth = DateService.getDate(yearMonth);
        this.yearMonth = yearMonth;
        this.tranIdNumerator = 1;
        setIsActive();
//        initCategories();
    }

    public long getBudgetNumber() {
        return budgetNumber;
    }

    public void setBudgetNumber(long budgetNumber) {
        this.budgetNumber = budgetNumber;
    }

    public void updateSpecificCategory(String id, Category cat) {
        this.categories.put(id, cat);
    }

    public Date getRefMonth() {
        return refMonth;
    }

    public void setRefMonth(Date refMonth) {
        this.refMonth = refMonth;
    }

    @Exclude
    public boolean isActive() {
        return isActive;
    }

    public void setCategories(Map<String, Category> categories) {
        this.categories = categories;
    }

    public void setIsActive() {
        Date today = DateService.getTodayDate();
        isActive = DateService.isSameYearMonth(refMonth, today);
    }

    public int getTranIdNumerator() {
        return tranIdNumerator;
    }

    public void setTranIdNumerator(int tranIdNumerator) {
        this.tranIdNumerator = tranIdNumerator;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public void addCategory(String id, Category category) {
        categories.put(id, category);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void initCategories() {
//        if(!this.dbService.isCurrentMBExists()) {
//            this.dbService.writeMBFromBudget(this.refMonth);
//        }
//        categories = getCategoriesFromDB();
    }
}

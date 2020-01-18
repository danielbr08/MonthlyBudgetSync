package com.brosh.finance.monthlybudgetsync.services;

import com.brosh.finance.monthlybudgetsync.Budget;
import com.brosh.finance.monthlybudgetsync.Category;
import com.brosh.finance.monthlybudgetsync.Month;
import com.brosh.finance.monthlybudgetsync.Transaction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class DBService implements Serializable {
    private Map<String, Map<String, Budget>> budgetDBHM = new HashMap<>();
    private Map<String, Month> monthDBHM = new HashMap<>();

    public Map<String, Map<String, Budget>> getBudgetDBHM() {
        return budgetDBHM;
    }

    public void setBudgetDBHM(Map<String, Map<String, Budget>> budgetDBHM) {
        this.budgetDBHM = budgetDBHM;
    }

    public Map<String, Month> getMonthDBHM() {
        return monthDBHM;
    }

    public void setMonthDBHM(Map<String, Month> monthDBHM) {
        this.monthDBHM = monthDBHM;
    }

    public void updateSpecificCategory(String refMonthKey, String categoryObjkey, Category categoryObj){
//        if(monthDBHM.get(refMonthKey) == null)
//            monthDBHM.put(refMonthKey,null);
        monthDBHM.get(refMonthKey).addCategory(categoryObjkey,categoryObj);
    }

    public void updateSpecificTransaction(String refMonthKey,String categoryObjkey,String transactionObj, Transaction trnObj){
//        if(monthDBHM.get(refMonthKey) == null)
//            monthDBHM.put(refMonthKey,null);
        monthDBHM.get(refMonthKey).getCategoryHMDB().get(categoryObjkey).addTransactions(transactionObj,trnObj);
    }

    public void updateSpecificBudget(String budgetNumber,String budgetObjkey,Budget budgetObj){
        if(budgetDBHM.get(budgetNumber) == null)
            budgetDBHM.put(budgetNumber,null);
        budgetDBHM.get(budgetNumber).put(budgetObjkey,budgetObj);
    }

    public void updateSpecificMonth(String refMonthKey,Month monthObj){
            monthDBHM.put(refMonthKey,monthObj);
    }
}

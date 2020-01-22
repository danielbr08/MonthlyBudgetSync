package com.brosh.finance.monthlybudgetsync.services;

import com.brosh.finance.monthlybudgetsync.Budget;
import com.brosh.finance.monthlybudgetsync.Category;
import com.brosh.finance.monthlybudgetsync.Month;
import com.brosh.finance.monthlybudgetsync.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    public void updateSpecificCategory(String refMonthKey, Category categoryObj){
        if(monthDBHM.get(refMonthKey) == null)
            monthDBHM.put(refMonthKey,new Month(refMonthKey,new Date(refMonthKey)));
        monthDBHM.get(refMonthKey).addCategory(categoryObj.getId(),categoryObj);
    }

    public void updateSpecificTransaction(String refMonthKey,String categoryObjkey,String transactionObj, Transaction trnObj){
        if(monthDBHM.get(refMonthKey) == null)
            monthDBHM.put(refMonthKey,null);
        monthDBHM.get(refMonthKey).getCategoryHMDB().get(categoryObjkey).addTransactions(transactionObj,trnObj);
    }

    public void updateSpecificBudget(String budgetNumber, Budget budgetObj){
        if(budgetDBHM.get(budgetNumber) == null)
            budgetDBHM.put(budgetNumber,new HashMap<String, Budget>());
        budgetDBHM.get(budgetNumber).put(budgetObj.getId(),budgetObj);
    }

    public void updateSpecificMonth(String refMonthKey,Month monthObj){
            monthDBHM.put(refMonthKey,monthObj);
    }

    public int getMaxBudgetNumber(){
        return Integer.valueOf(Collections.max(budgetDBHM.keySet()));

    }

    public ArrayList<Budget> getBudgetDataFromDB(int budgetNumber) {
        return new ArrayList<Budget>(budgetDBHM.get(budgetNumber).values());
    }

    public boolean checkCurrentRefMonthExists() {
        String currentRefMonth = "";
        return (monthDBHM.get(currentRefMonth) != null && monthDBHM.get(currentRefMonth).getCategoryHMDB().size() > 0);
    }

    public void deleteDataRefMonth(String refMonth) {
        monthDBHM.get(refMonth).getCategoryHMDB().clear();
        //monthDBHM.get(refMonth).setTranIdNumerator(0);
    }

    public int getMaxIDPerMonthTRN(String refMonth) {
        //return monthDBHM.get(refMonth).getTranIdNumerator();
        int maxId = -1;
        List<Category> categories = new ArrayList<Category>(monthDBHM.get(refMonth).getCategoryHMDB().values());
        for (Category cat:categories) {
            List<Transaction> transactions =  new ArrayList<Transaction>(cat.getTransactionHMDB().values());
            for (Transaction trn:transactions) {
                if(trn.getIdPerMonth() > maxId){
                    maxId = trn.getIdPerMonth();
                }
            }
        }
        return maxId;
    }

    public void addToCategoriesFromBudget(String startCurrentMonth, ArrayList<Budget> catAdd) {
        for (Budget bgt: catAdd) {
            monthDBHM.get(startCurrentMonth).getCategoryHMDB().put(bgt.getId(),bgt);
        }
    }

    public void updateBudgetNumberMB(String startCurrentMonth, int budgetNumber) {
    }
}

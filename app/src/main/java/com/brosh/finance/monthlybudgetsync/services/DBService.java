package com.brosh.finance.monthlybudgetsync.services;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.Budget;
import com.brosh.finance.monthlybudgetsync.Category;
import com.brosh.finance.monthlybudgetsync.MainActivity;
import com.brosh.finance.monthlybudgetsync.Month;
import com.brosh.finance.monthlybudgetsync.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
    private AppCompatActivity tempActivity;

    public DBService(){}
    public DBService(AppCompatActivity activity){
        tempActivity = activity;
    }

    public AppCompatActivity getTempActivity() {
        return tempActivity;
    }

    public void setTempActivity(AppCompatActivity tempActivity) {
        this.tempActivity = tempActivity;
    }

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

    public void updateBudgetNumberMB(String startCurrentMonth, int budgetNumber) {
    }

    public void initDB(String userKey){
        final DBService thisObject = this;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Monthly Budget").child(userKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    String dbKey =  myDataSnapshot.getKey().toString();
                    switch(dbKey){
                        case "Budget":{
                            setBudgetDB(myDataSnapshot);
                            break;
                        }
                        case "Months":{
                            setMonthsDB(myDataSnapshot);
                            break;
                        }
                    }
                }
                Intent mainActivityIntent = new Intent(tempActivity.getApplicationContext(), MainActivity.class);
                mainActivityIntent.putExtra("dbService",thisObject);
                tempActivity.startActivity(mainActivityIntent);
                tempActivity.finish();
            }
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void setBudgetDB(DataSnapshot budgetsSnapshot){
        for(DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
            String budgetNumber =  budgetSnapshot.getKey().toString();
            for(DataSnapshot mySnapshot : budgetsSnapshot.child(budgetNumber).getChildren()) {
                String budgetObjkey = mySnapshot.getKey().toString();
                Budget budgetObj = mySnapshot.getValue(Budget.class);
                updateSpecificBudget(budgetNumber,budgetObj);
                setBudgetEventUpdateValue(budgetSnapshot.getRef(),budgetNumber,budgetObjkey);
            }
        }
    }

    public void setCategoriesEventUpdateValue(DataSnapshot categoriesSnapshot, String refMonthKey){
        for(DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
            for(DataSnapshot mySnapshot : categoriesSnapshot.child(refMonthKey).getChildren()) {
                String categoryObjkey = mySnapshot.getKey().toString();
                setCategoryEventUpdateValue(categorySnapshot.getRef(),refMonthKey,categoryObjkey);
            }
        }
    }

    public void setMonthsDB(DataSnapshot monthsSnapshot){
        for(DataSnapshot monthSnapshot : monthsSnapshot.getChildren()) {
            String refMonthKey =  monthSnapshot.getKey().toString();
            Month monthObj = monthSnapshot.getValue(Month.class);
            updateSpecificMonth(refMonthKey,monthObj);
            DataSnapshot categoriesDatabaseReference = monthSnapshot.child(refMonthKey).child("Categories");
            setCategoriesEventUpdateValue(categoriesDatabaseReference,refMonthKey);
            setMonthEventUpdateValue(monthSnapshot.getRef(),refMonthKey);
        }
    }

    public void setBudgetEventUpdateValue(DatabaseReference budgetDBReference, final String refMonthKey, String objId) {
        budgetDBReference.child(refMonthKey).child(objId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Budget budgetObj = dataSnapshot.getValue(Budget.class);
                    updateSpecificBudget(refMonthKey, budgetObj);
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void setCategoryEventUpdateValue(final DatabaseReference categoryDBReference, final String refMonthKey,final String catObjId) {
        categoryDBReference.child(catObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Category cat = dataSnapshot.getValue(Category.class);
                    updateSpecificCategory(refMonthKey, cat);
                    DataSnapshot transactionDBReference = dataSnapshot.child(catObjId).child("Transactions");
                    for(DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                        String trnObjkey = transactionSnapshot.getKey().toString();
                        setTransactionEventUpdateValue(transactionDBReference.getRef(),refMonthKey,catObjId,trnObjkey);
                    }
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setTransactionEventUpdateValue(DatabaseReference transactionDBReference, final String refMonthKey,final String catObjId, final String trnObjId) {
        transactionDBReference.child(trnObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Transaction trn = dataSnapshot.getValue(Transaction.class);
                    updateSpecificTransaction(refMonthKey,catObjId, trnObjId,trn);
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setMonthEventUpdateValue(DatabaseReference monthDBReference, final String refMonthKey) {
        monthDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Month month = dataSnapshot.getValue(Month.class);
                    updateSpecificMonth(refMonthKey, month);
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setAddedCategoriesIncludeEventUpdateValue(DatabaseReference categoryDBReference,final String refMonthKey, final ArrayList<Budget> addedBudgets){
        categoryDBReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference categoryNode = dataSnapshot.getRef();
                for (Budget bgt : addedBudgets) {
                    String catId = categoryNode.push().getKey();
                    Category cat = new Category(catId,bgt.getCategoryName(),bgt.getValue(),bgt.getValue());
                    categoryNode.child(catId).setValue(cat);
                    updateSpecificCategory(refMonthKey,cat);
                    setCategoryEventUpdateValue(categoryNode,refMonthKey,catId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

package com.brosh.finance.monthlybudgetsync.services;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.ui.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DBService implements Serializable {
    private Map<String, Map<String, Budget>> budgetDBHM = new HashMap<>();
    private Map<String, Month> monthDBHM = new HashMap<>();
    private Language language = new Language(Config.DEFAULT_LANGUAGE);

    private Month month;

    public DBService(){
//        month = new Month("",new Date());
        month = new Month();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
    public DBService(AppCompatActivity activity){
        //tempActivity = activity;
//        month = new Month("",new Date());
        month = new Month();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

//    public AppCompatActivity getTempActivity() {
//        return tempActivity;
//    }

//    public void setTempActivity(AppCompatActivity tempActivity) {
//        this.tempActivity = tempActivity;
//    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
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
            monthDBHM.put(refMonthKey,new Month(refMonthKey));
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

    public List<Budget> getBudgetDataFromDB(int budgetNumber) {
        List<Budget> budgets = new ArrayList<Budget>(budgetDBHM.get(String.valueOf(budgetNumber)).values());
        Collections.sort(budgets,ComparatorService.COMPARE_BY_CATEGORY_PRIORITY);
        return budgets;
    }

    public boolean isCurrentRefMonthExists() {
        String currentRefMonth =  DateService.getYearMonth(DateService.getTodayDate(), Definition.dash);
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

    public void initDB(final String userKey, final Activity activity){
        final DBService thisObject = this;
        DatabaseReference databaseReference = Config.DatabaseReferenceMonthlyBudget.child(userKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    String dbKey =  myDataSnapshot.getKey().toString();
                    switch(dbKey){
                        case Definition.BUDGETS:{
                            setBudgetDB(myDataSnapshot);
                            break;
                        }
                        case Definition.MONTHS:{
                            setMonthsDB(myDataSnapshot);
                            break;
                        }
                    }
                }
                try {
                    Intent mainActivityIntent = new Intent(activity.getApplicationContext(), MainActivity.class);
                    mainActivityIntent.putExtra(activity.getString(R.string.db_service), thisObject);
                    mainActivityIntent.putExtra(activity.getString(R.string.user), userKey);
                    activity.startActivity(mainActivityIntent);
                    activity.finish();
                }
                catch(Exception e){
                    String s = e.getMessage().toString();// todo remove those lines
                    s=s;
                }
            }
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void setBudgetDB(DataSnapshot budgetsSnapshot){
        for(DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
            String budgetNumber =  budgetSnapshot.getKey();
            for(DataSnapshot mySnapshot : budgetsSnapshot.child(budgetNumber).getChildren()) {
                String budgetObjkey = mySnapshot.getKey();
                Budget budgetObj = mySnapshot.getValue(Budget.class);
                updateSpecificBudget(budgetNumber,budgetObj); // todo check if needed because this call from setBudgetEventUpdateValue function(next line)
                setBudgetEventUpdateValue(budgetSnapshot.getRef(),budgetNumber,budgetObjkey);
            }
        }
    }

    public void setCategoriesEventUpdateValue(DataSnapshot categoriesSnapshot, String refMonthKey){
        for(DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
                String categoryObjkey = categorySnapshot.getKey();
                Category cat = categoriesSnapshot.getValue(Category.class);
                updateSpecificCategory(refMonthKey, cat);
                setCategoryEventUpdateValue(categoriesSnapshot.getRef(),refMonthKey,categoryObjkey);
        }
    }

    public void setMonthsDB(DataSnapshot monthsSnapshot){
        for(DataSnapshot monthSnapshot : monthsSnapshot.getChildren()) {
            String refMonthKey = monthSnapshot.getKey();
//            Month month = monthSnapshot.getValue(Month.class);
//            updateSpecificMonth(refMonthKey, month);
            setMonthEventUpdateValue(monthSnapshot.getRef(),refMonthKey);
        }
    }

    public void setBudgetEventUpdateValue(DatabaseReference budgetDBReference, final String refMonthKey, String objId) {
        budgetDBReference.child(objId).addValueEventListener(new ValueEventListener() {
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

    public void setCategoryEventUpdateValue(final DatabaseReference categoryDBReference, final String refMonthKey, final String catObjId) {
        categoryDBReference.child(catObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Category cat = dataSnapshot.getValue(Category.class);
                    updateSpecificCategory(refMonthKey, cat);
                    DataSnapshot transactionDBReference = dataSnapshot.child(Definition.TRANSACTIONS);
                    for(DataSnapshot transactionSnapshot : transactionDBReference.getChildren()) {
                        String trnObjkey = transactionSnapshot.getKey();
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
                    DataSnapshot categoryDBReference = dataSnapshot.child(Definition.CATEGORIES);
                    Month month = dataSnapshot.getValue(Month.class);
                    updateSpecificMonth(refMonthKey, month);
                    setCategoriesEventUpdateValue(categoryDBReference,refMonthKey);
                }
                catch(Exception ex){
                    String message = ex.getMessage();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setAddedCategoriesIncludeEventUpdateValue(DatabaseReference categoryDBReference,final String refMonthKey, final List<Budget> addedBudgets,String operation){
        try {
            categoryDBReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference categoryNode = dataSnapshot.getRef();
                    for (Budget bgt : addedBudgets) {
                        Transaction tran = createTransactionByBudget(bgt);
                        String catId = categoryNode.push().getKey();
                        Category cat = new Category(catId, bgt.getCategoryName(), bgt.getValue(), bgt.getValue());
                        if (isFrqTranExists(bgt)) {
                            cat.withdrawal(tran.getPrice());
                            DatabaseReference transactionsNode = categoryNode.child(Definition.TRANSACTIONS).getRef();
                            setFrqTranIncludeEventUpdateValue(transactionsNode, refMonthKey, catId, tran);
                        }
//                        categoryNode.child(catId).setValue(cat);
//                        updateSpecificCategory(refMonthKey, cat);
                        setCategoryEventUpdateValue(categoryNode, refMonthKey, catId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        catch(Exception e){
            String s = e.getMessage().toString();
            s=s;
        }
    }

    private boolean isFrqTranExists(Budget bgt){
        return bgt.isConstPayment();
    }

    private Transaction createTransactionByBudget(Budget bgt) {
        int idPerMonth = 0;
        String paymentMethod = language.creditCardName;
        Date payDate = DateService.getCurrentDate(bgt.getChargeDay());
        return new Transaction(idPerMonth,bgt.getCategoryName(),paymentMethod,bgt.getShop(), payDate, bgt.getValue());
    }

    private void setFrqTranIncludeEventUpdateValue(DatabaseReference transactionsNode, String refMonthKey, String catId, Transaction tran) {
        String tranId = transactionsNode.push().getKey();
        transactionsNode.child(catId).setValue(tran);
        updateSpecificTransaction(refMonthKey,catId,tranId,tran);
        setTransactionEventUpdateValue(transactionsNode,refMonthKey,catId,tranId);
        }

    public void setMonthIncludeEventUpdateValue(DatabaseReference monthDBReference,final  String refMonthKey, final List<Budget> catToAdd, final String operation) {
        try {
            monthDBReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference monthNode = dataSnapshot.getRef();
                    Month month = new Month(refMonthKey);
                    monthNode.child(refMonthKey).setValue(month);
                    updateSpecificMonth(refMonthKey, month);
                    setMonthEventUpdateValue(monthNode, refMonthKey);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        }
        catch(Exception e){
            String message = e.getMessage().toString();
            message = message;
        }
    }

//    public void setFrqTrans(ArrayList<Budget> freqBudgets, int idPerMonth, String refMonth)
//    {
//        int maxBudgetNumberBGT = getMaxBudgetNumber();
//        ArrayList<Budget> allBudget = freqBudgets;
//        if(allBudget == null)
//            allBudget= getBudgetDataFromDB(maxBudgetNumberBGT);
//        for (Budget budget:allBudget)
//        {
//            if(!budget.isConstPayment())
//                continue;
//
//            String categoryName = budget.getCategoryName();
//            double transactionPrice = Double.valueOf(budget.getValue());
//            String shop = budget.getShop();
//            int chargeDay = budget.getChargeDay();
//            Date payDate = DateService.getCurrentDate(chargeDay);
//            String paymentMethod = language.creditCardName;
//
//            //Insert data
//            Transaction transaction = new Transaction("", getMaxIDPerMonthTRN(refMonth), categoryName,paymentMethod , shop, payDate, transactionPrice, new Date());
//            transaction.setIsStorno(false);
//            transaction.setStornoOf(-1);
//
//            for (Category cat : month.getCategories())
//            {
//                if (categoryName.equals(cat.getName()))
//                {
//                    cat.subValRemaining(transactionPrice);
//                    cat.addTransaction(transaction);
//                }
//            }
//            shopsSet.add(shop);
//        }
//        month.setAllTransactions();
//        if( month.getTransChanged())
//            month.updateMonthData(idPerMonth + 1);
//        month.setTransChanged(false);
//    }
}

package com.brosh.finance.monthlybudgetsync.services;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.ui.MainActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
    private String userKey;

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public DBService(){
        month = new Month();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

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

    public Map<String, Map<String, Budget> > getBudgetDBHM() {
        return budgetDBHM;
    }

    public void setBudgetDBHM(Map<String, Map<String, Budget> > budgetDBHM) {
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
        monthDBHM.get(refMonthKey).getCategories().get(categoryObjkey).addTransactions(transactionObj,trnObj);
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
        String currentRefMonth =  DateService.getYearMonth(DateService.getTodayDate(), Definition.DASH);
        return (monthDBHM.get(currentRefMonth) != null && monthDBHM.get(currentRefMonth).getCategories().size() > 0);
    }

    public void deleteDataRefMonth(String refMonth) {
        monthDBHM.remove(refMonth);
        Config.DatabaseReferenceMonthlyBudget.child(userKey).child(refMonth).removeValue();
        //removeEventListners();
    }

    public int getMaxIDPerMonthTRN(String refMonth) {
        //return monthDBHM.get(refMonth).getTranIdNumerator();
        int maxId = -1;
        List<Category> categories = new ArrayList<Category>(monthDBHM.get(refMonth).getCategories().values());
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
                    String keyNode =  myDataSnapshot.getKey();
                    switch(keyNode){
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
                    startApp(thisObject, activity);
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
        // Set data
        for(DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
            String budgetNumber =  budgetSnapshot.getKey();
            for(DataSnapshot mySnapshot : budgetsSnapshot.child(budgetNumber).getChildren()) {
                Budget budgetObj = mySnapshot.getValue(Budget.class);
                updateSpecificBudget(budgetNumber,budgetObj);
            }
        }
        // Set event add child
        final DBService thisObject = this;
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String budgetNumber = dataSnapshot.getKey();
                Map<String,Budget> budgets = (Map<String,Budget>)dataSnapshot.getValue();
                thisObject.budgetDBHM.put(budgetNumber,budgets);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        budgetsSnapshot.getRef().addChildEventListener(addChildEvent);
    }

    public void setMonthsDB(DataSnapshot monthsSnapshot){
        // Set data
        for(DataSnapshot currentMonthDataSnapshot : monthsSnapshot.getChildren()) {
            String refMonthKey = currentMonthDataSnapshot.getKey();
            Month month = currentMonthDataSnapshot.getValue(Month.class);
            updateSpecificMonth(refMonthKey, month);
        }

        // Set event add child
        final DBService thisObject = this;
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String refMonth = dataSnapshot.getKey();
                Month month = dataSnapshot.getValue(Month.class);
                thisObject.monthDBHM.put(refMonth,month);
                DataSnapshot categoriesDBSnapShot = dataSnapshot.child(Definition.CATEGORIES);
                if(categoriesDBSnapShot.exists())
                    setCategoriesFieldsEventUpdateValue(categoriesDBSnapShot, refMonth);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String refMonth = dataSnapshot.getKey();
                thisObject.monthDBHM.remove(refMonth);
//                removeEventListner(); todo implement
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        monthsSnapshot.getRef().addChildEventListener(addChildEvent);
    }

    public void startApp(DBService thisObject, Activity activity){
        Intent mainActivityIntent = new Intent(activity.getApplicationContext(), MainActivity.class);
        mainActivityIntent.putExtra(activity.getString(R.string.db_service), thisObject);
        mainActivityIntent.putExtra(activity.getString(R.string.user), userKey);
        activity.startActivity(mainActivityIntent);
        activity.finish();
    }

    public void setTransactionsFieldsEventUpdateValue(DataSnapshot transactionsSnapshot, String refMonthKey,String categoryObjkey){
        Map<String, Transaction> currentTransactions = this.monthDBHM.get(refMonthKey).getCategories().get(categoryObjkey).getTransactionHMDB();
        for(String tranId : currentTransactions.keySet()) {
            DataSnapshot transactionFieldsDataBaseReference = transactionsSnapshot.child(tranId);
            setTransactionFieldsEventUpdateValue(transactionFieldsDataBaseReference, refMonthKey, categoryObjkey);
        }
    }

    public void setCategoryEventUpdateValue(final DatabaseReference categoryDBReference, final String refMonthKey, final String catObjId) {
        categoryDBReference.child(catObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Category cat = dataSnapshot.getValue(Category.class);
                    updateSpecificCategory(refMonthKey, cat);
                    setCategoryFieldsEventUpdateValue(dataSnapshot, refMonthKey);
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

    public void setCategoriesFieldsEventUpdateValue(DataSnapshot categoriesDBSnapShot, String refMonth){
        Map<String,Category> currentCategories = this.monthDBHM.get(refMonth).getCategories();
        for(String catId: currentCategories.keySet())
            setCategoryFieldsEventUpdateValue(categoriesDBSnapShot.child(catId), refMonth);
    }

    private void setCategoryFieldsEventUpdateValue(final DataSnapshot categoryDBDataSnapshot, String refMonthKey) {
        String catId = categoryDBDataSnapshot.getKey();
        List<String> categoryFields = Arrays.asList(Definition.BALANCE, Definition.BUDGET, Definition.NAME);
        for (String categoryField : categoryFields) {
            DatabaseReference categoryFieldDataBaseReference = categoryDBDataSnapshot.child(categoryField).getRef();
            setCategoryFieldEventUpdateValue(categoryFieldDataBaseReference, refMonthKey, catId);
        }

        DataSnapshot transactionDBDataSnapshot = categoryDBDataSnapshot.child(Definition.TRANSACTIONS);
        if(transactionDBDataSnapshot.exists())
            setTransactionsFieldsEventUpdateValue(transactionDBDataSnapshot, refMonthKey, catId);
    }

    private void setCategoryFieldEventUpdateValue(final DatabaseReference categoryFieldDataBaseReference, final String refMonthKey, final String catId){
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String fieldName = dataSnapshot.getKey();
                    Category curentCategory = monthDBHM.get(refMonthKey).getCategories().get(catId);
                    switch(fieldName){
                        case Definition.BALANCE:
                            double balance = Double.valueOf(dataSnapshot.getValue().toString());
                            curentCategory.setBalance(balance);
                            break;
                        case Definition.BUDGET:
                            int budget = Integer.valueOf(dataSnapshot.getValue().toString());
                            curentCategory.setBudget(budget);
                            break;
                        case Definition.NAME:
                            String name = dataSnapshot.getValue().toString();
                            curentCategory.setName(name);
                            break;
                    }
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        categoryFieldDataBaseReference.addValueEventListener(event);
    }

    private void setTransactionFieldsEventUpdateValue(final DataSnapshot transactionDBDataSnapshot, String refMonth, String catId){
        String tranId = transactionDBDataSnapshot.getKey();
        List<String> transactionFields = Arrays.asList(Definition.IS_STORNO, Definition.STORNO_OF);
        for (String transactionField : transactionFields) {
            DatabaseReference transactionFieldDataBaseReference = transactionDBDataSnapshot.child(transactionField).getRef();
            setTransactionFieldEventUpdateValue(transactionFieldDataBaseReference, refMonth, catId, tranId);
        }
    }

    private void setTransactionFieldEventUpdateValue(final DatabaseReference transactionFieldDataBaseReference, final String refMonthKey, final String catId, final String tranId){
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String fieldName = dataSnapshot.getKey();
                    Transaction curentTransaction = monthDBHM.get(refMonthKey).getCategories().get(catId).getTransactionHMDB().get(tranId);
                    switch(fieldName){
                        case Definition.IS_STORNO:
                            boolean isStorno = Boolean.valueOf(dataSnapshot.getValue().toString());
                            curentTransaction.setStorno(isStorno);
                            break;
                        case Definition.STORNO_OF:
                            int stornoOf = Integer.valueOf(dataSnapshot.getValue().toString());
                            curentTransaction.setStornoOf(stornoOf);
                            break;
                    }
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        transactionFieldDataBaseReference.addValueEventListener(event);
    }

    //****************************************************************************************
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
        setTransactionEventUpdateValue(transactionsNode,refMonthKey,catId);
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
//
//    public void setBudgetEventUpdateValue(DatabaseReference budgetDBReference, final String refMonthKey, String objId) {
//        budgetDBReference.child(objId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                try {
//                    Budget budgetObj = dataSnapshot.getValue(Budget.class);
//                    updateSpecificBudget(refMonthKey, budgetObj);
//                }
//                catch(Exception ex){
//                    String message = ex.getMessage().toString();
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
//    }

    public void setTransactionEventUpdateValue(DatabaseReference transactionDBReference, final String refMonthKey,final String catObjId) {
        transactionDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String tranId = dataSnapshot.getKey();
                    Transaction trn = dataSnapshot.getValue(Transaction.class);
                    updateSpecificTransaction(refMonthKey,catObjId,tranId,trn);
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
                    setCategoriesFieldsEventUpdateValue(categoryDBReference,refMonthKey);
//                    setCategoriesEventUpdateValue(categoryDBReference,refMonthKey); // todo remove
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

    public void setCategoriesEventUpdateValue(DataSnapshot categoriesSnapshot, String refMonthKey){
        for(DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
            String categoryObjkey = categorySnapshot.getKey();
            Category cat = categoriesSnapshot.getValue(Category.class);
            updateSpecificCategory(refMonthKey, cat);
            setCategoryEventUpdateValue(categoriesSnapshot.getRef(),refMonthKey,categoryObjkey);
        }
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
                            setFrqTranIncludeEventUpdateValue(transactionsNode, refMonthKey, catId, tran); //TODO  CHECK THIS EVENT LISTNER TRANSACTION PATH
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

    public static Map<String,Budget> listToMap(List<Budget> budgets){
        Map<String, Budget> mapedItems = new HashMap<>();
        for (Budget budget: budgets) {
            mapedItems.put(budget.getId(),budget);
        }
        return mapedItems;
    }

    public DatabaseReference getDBUserRootPath(){
        return FirebaseDatabase.getInstance().getReference(Definition.MONTHLY_BUDGET).child(userKey);
    }

    public DatabaseReference getDBBudgetPath(){
        return getDBUserRootPath().child(Definition.BUDGETS);
    }

    public DatabaseReference getDBMonthstPath(){
        return getDBUserRootPath().child(Definition.MONTHS);
    }

    public DatabaseReference getDBCategoriesPath(String refMonth){
        return getDBMonthstPath().child(refMonth).child(Definition.CATEGORIES);
    }

    public DatabaseReference getDBUserTransactionsPath(String refMonth, String catId){
        return getDBCategoriesPath(refMonth).child(catId).child(Definition.TRANSACTIONS);
    }

    public Map<String, Budget> getBudget(String budgetNumber){
        if(budgetDBHM.containsKey(budgetNumber))
            return budgetDBHM.get(budgetNumber);
        return null;
    }
}

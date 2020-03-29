package com.brosh.finance.monthlybudgetsync.services;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.config.Language;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.EventListenerMap;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DBService implements Serializable {

    private static DBService instance;

    private DBService() {
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static DBService getInstance() {
        if (instance == null)
            instance = new DBService();
        return instance;
    }

    private Map<String, Map<String, Budget>> budgetDBHM = new HashMap<>();
    private Map<String, Month> monthDBHM = new HashMap<>();
    private Set<String> shopsSet = new HashSet<String>();

    private Language language = new Language(Config.DEFAULT_LANGUAGE);

    private String userKey;

    public Set<String> getShopsSet() {
        return shopsSet;
    }

    public void setShopsSet(Set<String> shopsSet) {
        this.shopsSet = shopsSet;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Month getMonth(String refMonth) {
        return monthDBHM.get(refMonth);
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

    public void updateSpecificCategory(String refMonthKey, int budgetNumber, Category categoryObj) {
        if (monthDBHM.get(refMonthKey) == null) {
            monthDBHM.put(refMonthKey, new Month(refMonthKey, budgetNumber));
        }
        monthDBHM.get(refMonthKey).addCategory(categoryObj.getId(), categoryObj);
    }

    public void updateSpecificTransaction(String refMonthKey, String categoryObjkey, String transactionObj, Transaction trnObj) {
        if (monthDBHM.get(refMonthKey) == null)
            monthDBHM.put(refMonthKey, null);
        monthDBHM.get(refMonthKey).getCategories().get(categoryObjkey).addTransactions(transactionObj, trnObj);
    }

    public void updateSpecificBudget(String budgetNumber, Budget budgetObj) {
        if (budgetDBHM.get(budgetNumber) == null)
            budgetDBHM.put(budgetNumber, new HashMap<String, Budget>());
        budgetDBHM.get(budgetNumber).put(budgetObj.getId(), budgetObj);
    }

    public void updateSpecificMonth(String refMonthKey, Month monthObj) {
        monthDBHM.put(refMonthKey, monthObj);
    }

    public int getMaxBudgetNumber() {
        return Collections.max(getBudgetNumbesrAsInt());

    }

    public List<Integer> getBudgetNumbesrAsInt() {
        List<Integer> budetNumbers = new ArrayList<>();
        for (String bugetNumber : budgetDBHM.keySet()) {
            budetNumbers.add(Integer.valueOf(bugetNumber));
        }
        return budetNumbers;
    }

    public List<Budget> getBudgetDataFromDB(long budgetNumber) {
        List<Budget> budgets = new ArrayList<Budget>(budgetDBHM.get(String.valueOf(budgetNumber)).values());
        try {
            Collections.sort(budgets, ComparatorService.COMPARE_BY_CATEGORY_PRIORITY);
        } catch (Exception e) {
            String s = e.getMessage();
            s = s;
        }
        return budgets;
    }

    public boolean isCurrentRefMonthExists() {
        String currentRefMonth = DateService.getYearMonth(DateService.getTodayDate(), Definition.DASH);
        return (monthDBHM.get(currentRefMonth) != null && monthDBHM.get(currentRefMonth).getCategories().size() > 0);
    }

    public void deleteDataRefMonth(String refMonth) {
        monthDBHM.remove(refMonth);
        getDBMonthtPath(refMonth).removeValue(); // todo  check if this call will run delete event listener node. if yes, the next line is not needed.
//        deleteChildValueEventsListener(Config.DatabaseReferenceMonthlyBudget.child(userKey).child(refMonth)); // todo add support databasereference parameter
    }

    public int getMaxIDPerMonthTRN(String refMonth) {
        //return monthDBHM.get(refMonth).getTranIdNumerator();
        int maxId = -1;
        List<Category> categories = new ArrayList<Category>(getCategories(refMonth).values());
        for (Category cat : categories) {
            List<Transaction> transactions = new ArrayList<Transaction>(cat.getTransactions().values());
            for (Transaction trn : transactions) {
                if (trn.getIdPerMonth() > maxId) {
                    maxId = trn.getIdPerMonth();
                }
            }
        }
        return maxId;
    }

    public void updateBudgetNumberMB(String startCurrentMonth, int budgetNumber) {
    }

    public void initDB(final String userKey, final Activity activity) {
        this.userKey = userKey;
        final DBService thisObject = this;
        DatabaseReference databaseReference = Config.DatabaseReferenceMonthlyBudget.child(userKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    return;
                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    String keyNode = myDataSnapshot.getKey();
                    switch (keyNode) {
                        case Definition.BUDGETS: {
                            setBudgetDB(myDataSnapshot);
                            break;
                        }
                        case Definition.MONTHS: {
                            setMonthsDB(myDataSnapshot);
                            break;
                        }
                    }
                }
                try {
                    startApp(thisObject, activity);
                } catch (Exception e) {
                    String s = e.getMessage();// todo remove those lines
                    s = s;
                }
            }

            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void setBudgetDB(DataSnapshot budgetsSnapshot) {
        // Set data
        for (DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
            String budgetNumber = budgetSnapshot.getKey();
            for (DataSnapshot mySnapshot : budgetsSnapshot.child(budgetNumber).getChildren()) {
                Budget budgetObj = mySnapshot.getValue(Budget.class);
                updateSpecificBudget(budgetNumber, budgetObj);
            }
        }
        // Set event add child
        final DBService thisObject = this;
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GenericTypeIndicator<Map<String, Budget>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Budget>>() {
                };
                String budgetNumber = dataSnapshot.getKey();
                Map<String, Budget> budgets = (Map<String, Budget>) dataSnapshot.getValue(genericTypeIndicator);
                thisObject.budgetDBHM.put(budgetNumber, budgets);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String budgetNumber = dataSnapshot.getKey();
                thisObject.budgetDBHM.remove(budgetNumber);
                deleteChildValueEventsListener(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!EventListenerMap.getInstance().isEventAlreadyExists(budgetsSnapshot.getRef())) {
            budgetsSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(budgetsSnapshot.getRef(), addChildEvent);
        }
    }

    public void setMonthsDB(DataSnapshot monthsSnapshot) {
        // Set data
        for (DataSnapshot currentMonthDataSnapshot : monthsSnapshot.getChildren()) {
            String refMonthKey = currentMonthDataSnapshot.getKey();
            Month month = currentMonthDataSnapshot.getValue(Month.class);
            month.setIsActive();
            updateSpecificMonth(refMonthKey, month);
        }

        // Set event add child
        setAddChildMonthEvent(monthsSnapshot);
    }

    public void setAddChildMonthEvent(DataSnapshot MonthDataSnapshot) {
        final DBService thisObject = this;
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String refMonth = dataSnapshot.getKey();
                Month month = dataSnapshot.getValue(Month.class);
                month.setIsActive();
                thisObject.monthDBHM.put(refMonth, month);
                DataSnapshot categoriesDBSnapShot = dataSnapshot.child(Definition.CATEGORIES);
                if (categoriesDBSnapShot.exists()) {
                    setAddChildCategoryEvent(categoriesDBSnapShot, refMonth);
                    setCategoriesFieldsEventUpdateValue(categoriesDBSnapShot, refMonth);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                String refMonth = dataSnapshot.getKey();
//                Month month = dataSnapshot.getValue(Month.class);
//                thisObject.monthDBHM.put(refMonth,month);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                String refMonth = dataSnapshot.getKey();
//                thisObject.monthDBHM.remove(refMonth);
                deleteChildValueEventsListener(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!EventListenerMap.getInstance().isEventAlreadyExists(MonthDataSnapshot.getRef())) {
            MonthDataSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(MonthDataSnapshot.getRef(), addChildEvent);
        }
    }

    public void setAddChildCategoryEvent(DataSnapshot categoryDataSnapshot, final String refMonth) {
        final DBService thisObject = this;
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Category cat = dataSnapshot.getValue(Category.class);
                thisObject.getCategories(refMonth).put(cat.getId(), cat);
                setCategoryFieldsEventUpdateValue(dataSnapshot, refMonth);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String catId = dataSnapshot.getKey();
                thisObject.getCategories(refMonth).remove(catId);
                deleteChildValueEventsListener(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!EventListenerMap.getInstance().isEventAlreadyExists(categoryDataSnapshot.getRef())) {
            categoryDataSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(categoryDataSnapshot.getRef(), addChildEvent);
        }
    }

    public void startApp(DBService thisObject, Activity activity) {
        Intent mainActivityIntent = new Intent(activity.getApplicationContext(), MainActivity.class);
        mainActivityIntent.putExtra(activity.getString(R.string.user), userKey);
        activity.startActivity(mainActivityIntent);
        activity.finish();
    }

    public void setTransactionsFieldsEventUpdateValue(DataSnapshot transactionsSnapshot, String refMonthKey, String categoryObjkey) {
        Map<String, Transaction> currentTransactions = this.monthDBHM.get(refMonthKey).getCategories().get(categoryObjkey).getTransactions();
        for (String tranId : currentTransactions.keySet()) {
            DataSnapshot transactionFieldsDataBaseReference = transactionsSnapshot.child(tranId);
            setTransactionFieldsEventUpdateValue(transactionFieldsDataBaseReference, refMonthKey, categoryObjkey);
        }
    }

    public void setCategoryEventUpdateValue(final DatabaseReference categoryDatabaseReference, final int budgetNumber, final String refMonthKey, final String catObjId) {
        final DBService thisObject = this;
        ChildEventListener event = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Category cat = dataSnapshot.getValue(Category.class);
                updateSpecificCategory(refMonthKey, budgetNumber, cat);
                setCategoryFieldsEventUpdateValue(dataSnapshot, refMonthKey);
                if (!EventListenerMap.getInstance().isEventAlreadyExists(dataSnapshot.getRef())) {
                    addChildValueEventListener(dataSnapshot.getRef(), this);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String catId = dataSnapshot.getKey();
                thisObject.getCategories(refMonthKey).remove(catId);
                deleteChildValueEventsListener(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!EventListenerMap.getInstance().isEventAlreadyExists(categoryDatabaseReference)) {
            categoryDatabaseReference.child(catObjId).addChildEventListener(event);
        }
    }

    public void setCategoriesFieldsEventUpdateValue(DataSnapshot categoriesDBSnapShot, String refMonth) {
        Map<String, Category> currentCategories = getCategories(refMonth);
        for (String catId : currentCategories.keySet())
            setCategoryFieldsEventUpdateValue(categoriesDBSnapShot.child(catId), refMonth);
    }

    private void setCategoryFieldsEventUpdateValue(final DataSnapshot categoryDBDataSnapshot, String refMonthKey) {
        String catId = categoryDBDataSnapshot.getKey();
        setCategoryFieldEventUpdateValue(categoryDBDataSnapshot, refMonthKey, catId);

        DataSnapshot transactionDBDataSnapshot = categoryDBDataSnapshot.child(Definition.TRANSACTIONS);
        if (transactionDBDataSnapshot.exists())
            setTransactionsFieldsEventUpdateValue(transactionDBDataSnapshot, refMonthKey, catId);
    }

    private void setCategoryFieldEventUpdateValue(final DataSnapshot categoryFieldDataSnapshot, final String refMonthKey, final String catId) {
        ChildEventListener event = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    String fieldName = dataSnapshot.getKey();
                    Category curentCategory = monthDBHM.get(refMonthKey).getCategories().get(catId);
                    switch (fieldName) {
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
                } catch (Exception ex) {
                    String message = ex.getMessage().toString();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                deleteChildValueEventsListener(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!EventListenerMap.getInstance().isEventAlreadyExists(categoryFieldDataSnapshot.getRef())) {
            categoryFieldDataSnapshot.getRef().addChildEventListener(event);
            addChildValueEventListener(categoryFieldDataSnapshot.getRef(), event);
        }
    }

    private void setTransactionFieldsEventUpdateValue(final DataSnapshot transactionDBDataSnapshot, String refMonth, String catId) {
        String tranId = transactionDBDataSnapshot.getKey();
        List<String> transactionFields = Arrays.asList(Definition.IS_STORNO, Definition.STORNO_OF);
        for (String transactionField : transactionFields) {
            DataSnapshot transactionFieldDataBaseReference = transactionDBDataSnapshot.child(transactionField);
            setTransactionFieldEventUpdateValue(transactionFieldDataBaseReference, refMonth, catId, tranId);
        }
    }

    private void setTransactionFieldEventUpdateValue(final DataSnapshot transactionFieldDataSnapshot, final String refMonthKey, final String catId, final String tranId) {
        ChildEventListener event = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    String fieldName = dataSnapshot.getKey();
                    Transaction curentTransaction = monthDBHM.get(refMonthKey).getCategories().get(catId).getTransactions().get(tranId);
                    switch (fieldName) {
                        case Definition.IS_STORNO:
                            boolean isStorno = Boolean.valueOf(dataSnapshot.getValue().toString());
                            curentTransaction.setStorno(isStorno);
                            break;
                        case Definition.STORNO_OF:
                            int stornoOf = Integer.valueOf(dataSnapshot.getValue().toString());
                            curentTransaction.setStornoOf(stornoOf);
                            break;
                    }
                } catch (Exception ex) {
                    String message = ex.getMessage().toString();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                deleteChildValueEventsListener(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!EventListenerMap.getInstance().isEventAlreadyExists(transactionFieldDataSnapshot.getRef())) {
            transactionFieldDataSnapshot.getRef().addChildEventListener(event);
            addChildValueEventListener(transactionFieldDataSnapshot.getRef(), event);
        }
    }

    public void deleteChildValueEventListener(DatabaseReference databaseReference) {
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = EventListenerMap.getInstance().getChildEventListenersHM();
        ChildEventListener event = childEventListenersHM.get(databaseReference);
        databaseReference.removeEventListener(event);
    }

    public void deleteChildValueEventsListener(DataSnapshot dataSnapshot) {
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = EventListenerMap.getInstance().getChildEventListenersHM();
        for (DataSnapshot node : dataSnapshot.getChildren()) {
            if (childEventListenersHM.containsKey(node.getRef())) {
                deleteChildValueEventsListener(node);
            }
        }
        if (childEventListenersHM.containsKey(dataSnapshot.getRef()))
            deleteChildValueEventListener(dataSnapshot.getRef());
    }

    private void addChildValueEventListener(DatabaseReference databaseReference, ChildEventListener event) {
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = EventListenerMap.getInstance().getChildEventListenersHM();
        childEventListenersHM.put(databaseReference, event);
    }

    // Returns new categories wroted
    public List<Category> writeCategoriesByBudgets(final String refMonth, int budgetNumber, List<Budget> budgets) {
        List<Category> wrotedCategories = new ArrayList<>();

        int idPerMonth = 0;
        for (Budget bgt : budgets) {
            if (isFrqTran(bgt))
                idPerMonth++; // todo set tranIdNumerator in month obj
            String catId = getDBCategoriesPath(refMonth).push().getKey();
            Category cat = budgetToCategory(bgt, catId, idPerMonth);
//            getDBCategoriesPath(refMonth).child(catId).setValue(cat);
            updateSpecificCategory(refMonth, budgetNumber, cat);
            wrotedCategories.add(cat);
        }
        setIdNumerator(refMonth, idPerMonth);
        return wrotedCategories;
    }

    public Category budgetToCategory(Budget budget, String catId, int idPerMonth) {
        Category cat = new Category(catId, budget.getCategoryName(), budget.getValue(), budget.getValue());
        Map<String, Transaction> transactions = new HashMap();
        if (isFrqTran(budget)) {
            String paymentMethod = language.creditCardName;
            Date payDate = DateService.getCurrentDate(budget.getChargeDay());
            String yearMonth = DateService.getYearMonth(DateService.getTodayDate(), Config.SEPARATOR);
            String tranId = getDBUserTransactionsPath(yearMonth, catId).push().getKey();
            Transaction transaction = new Transaction(tranId, idPerMonth, budget.getCategoryName(), paymentMethod, budget.getShop(), payDate, budget.getValue());
            transactions.put(tranId, transaction);
            cat.setTransactions(transactions);
        }
        return cat;
    }

    public boolean isAnyBudgetExists() {
        return this.budgetDBHM.size() > 0;
    }

    public void createNewMonth(int budgetNumber, String refMonth) {
        List<Budget> budgetsToConvert = getBudgetDataFromDB(budgetNumber);
        List<Category> wrotedCategories = writeCategoriesByBudgets(refMonth, budgetNumber, budgetsToConvert);
        Month newMonth = getMonth(DateService.getYearMonth(DateService.getTodayDate(), Config.SEPARATOR));

        getDBMonthtPath(refMonth).setValue(newMonth);
        setAddedCategoriesEventUpdateValue(getDBCategoriesPath(refMonth), budgetNumber, refMonth, wrotedCategories);
    }

    public DatabaseReference getDBUserRootPath() {
        return FirebaseDatabase.getInstance().getReference(Definition.MONTHLY_BUDGET).child(userKey);
    }

    public DatabaseReference getDBBudgetPath() {
        return getDBUserRootPath().child(Definition.BUDGETS);
    }

    public DatabaseReference getDBMonthstPath() {
        return getDBUserRootPath().child(Definition.MONTHS);
    }

    public DatabaseReference getDBMonthtPath(String refMonth) {
        return getDBMonthstPath().child(refMonth);
    }

    public DatabaseReference getDBCategoriesPath(String refMonth) {
        return getDBMonthstPath().child(refMonth).child(Definition.CATEGORIES);
    }

    public DatabaseReference getDBUserTransactionsPath(String refMonth, String catId) {
        return getDBCategoriesPath(refMonth).child(catId).child(Definition.TRANSACTIONS);
    }

    public Map<String, Budget> getBudget(String budgetNumber) {
        if (budgetDBHM.containsKey(budgetNumber))
            return budgetDBHM.get(budgetNumber);
        return null;
    }

    public Map<String, Category> getCategories(String refMonth) {
        if (monthDBHM.containsKey(refMonth)) {
            return monthDBHM.get(refMonth).getCategories();
        }
        return null;
    }

    public Map<String, Category> getCategoriesClone(String refMonth) {
        try {
            Map<String, Category> categoriesClone = new HashMap<>();
            if (monthDBHM.containsKey(refMonth)) {
                for (Category cat : monthDBHM.get(refMonth).getCategories().values()) {
                    categoriesClone.put(cat.getId(), (Category) cat.clone());
                }
                return categoriesClone;
            }
            return null;
        } catch (Exception e) {
            String s = e.getMessage();
            s = s;
            return null;
        }
    }

    public void setAddedCategoriesEventUpdateValue(final DatabaseReference DatabaseReference, final int budgetNumber, final String refMonthKey, List<Category> addedCategories) {
        for (Category cat : addedCategories) {
            setCategoryEventUpdateValue(DatabaseReference, budgetNumber, refMonthKey, cat.getId());
        }
    }

    public List<Category> getCategoriesByPriority(String refMonth) { // todo check performance
        long budgetNumber = getMonthDBHM().get(refMonth).getBudgetNumber();
        Map<String, Category> categoriesClone = getCategoriesClone(refMonth);
        List<Budget> sortedBudgets = getBudgetDataFromDB(budgetNumber);
        List<Category> sortedCategories = new ArrayList<>();

        for (Budget budget : sortedBudgets) {
            for (Category cat : categoriesClone.values()) {
                if (budget.getCategoryName().equals(cat.getName()) && budget.getValue() == cat.getBudget()) {
                    sortedCategories.add(cat);
                    categoriesClone.remove(cat.getId());
                    break;
                }
            }
        }
        return sortedCategories;
    }

    public List<String> getAllMonthesYearMonth() {
        List<String> monthsList = new ArrayList<String>(monthDBHM.keySet());
        java.util.Collections.sort(monthsList);
        java.util.Collections.reverse(monthsList);
        return monthsList;
    }

    public List<String> getCategoriesNames(String refMonth) {
        List<String> categoriesNamesList = new ArrayList<>();
        Set<String> categoriesNamesSet = new HashSet<>();

        for (Category cat : getCategoriesByPriority(refMonth)) {
            String categoryName = cat.getName();
            if (!categoriesNamesSet.contains(categoryName)) {
                categoriesNamesList.add(categoryName);
                categoriesNamesSet.add(categoryName);
            }
        }
        return categoriesNamesList;
    }

    public List<Transaction> getTransactions(String refMonth, String catName) {
        if (catName.equals(language.all)) {
            return getTransactions(refMonth);
        }
        Category category = getCategoryByName(refMonth, catName);
        if (category.getTransactions() != null)
            return new ArrayList<Transaction>(category.getTransactions().values());
        return null;
    }

    public List<Transaction> getTransactions(String refMonth) {
        Map<String, Category> categoriesHM = getCategories(refMonth);
        List<Transaction> transactions = new ArrayList<>();
        for (Category cat : categoriesHM.values()) {
            transactions.addAll(cat.getTransactions().values());
        }
        return transactions;
    }

    public Category getCategoryByName(String refMonth, String catName) {
        Map<String, Category> categoriesHM = getCategories(refMonth);
        for (Category cat : categoriesHM.values()) {
            if (catName.equals(cat.getName())) {
                return cat;
            }
        }
        return null;
    }

    public int getMaxIdPerMonth(String refMonth, String catId) {
        List<Transaction> catTransactions = getTransactions(refMonth, catId);
        int maxId = 0;
        for (Transaction trn : catTransactions) {
            if (trn.getIdPerMonth() > maxId) {
                maxId = trn.getIdPerMonth();
            }
        }
        return maxId;
    }

    private boolean isFrqTran(Budget bgt) {
        return bgt.isConstPayment();
    }

    private Transaction createTransactionByBudget(Budget bgt, String catId) {
        int idPerMonth = 0;
        String paymentMethod = language.creditCardName;
        Date payDate = DateService.getCurrentDate(bgt.getChargeDay());
        return new Transaction(idPerMonth, bgt.getCategoryName(), paymentMethod, bgt.getShop(), payDate, bgt.getValue());
    }

    private void setIdNumerator(String refMonth, int idPerMonth) {
        monthDBHM.get(refMonth).setTranIdNumerator(idPerMonth);
    }
    //****************************************************************************************

//    private void setFrqTranIncludeEventUpdateValue(DatabaseReference transactionsNode, String refMonthKey, String catId, Transaction tran) {
//        String tranId = transactionsNode.push().getKey();
//        tran.setId(tranId);
//        transactionsNode.child(catId).setValue(tran);
//        updateSpecificTransaction(refMonthKey, catId, tranId, tran);
//        setTransactionEventUpdateValue(transactionsNode, refMonthKey, catId);
//    }

//    public void setMonthIncludeEventUpdateValue(DatabaseReference monthDBReference, final String refMonthKey, final List<Budget> catToAdd, final String operation) {
//        try {
//            monthDBReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    DatabaseReference monthNode = dataSnapshot.getRef();
//                    Month month = new Month(refMonthKey);
//                    monthNode.child(refMonthKey).setValue(month);
//                    updateSpecificMonth(refMonthKey, month);
//                    setMonthEventUpdateValue(monthNode, refMonthKey);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                }
//            });
//
//        } catch (Exception e) {
//            String message = e.getMessage().toString();
//            message = message;
//        }
//    }

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

//    public void setTransactionEventUpdateValue(DatabaseReference transactionDBReference, final String refMonthKey, final String catObjId) {
//        transactionDBReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                try {
//                    String tranId = dataSnapshot.getKey();
//                    Transaction trn = dataSnapshot.getValue(Transaction.class);
//                    updateSpecificTransaction(refMonthKey, catObjId, tranId, trn);
//                } catch (Exception ex) {
//                    String message = ex.getMessage().toString();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    public void setMonthEventUpdateValue(DatabaseReference monthDBReference, final String refMonthKey) {
//        monthDBReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                try {
//                    DataSnapshot categoryDBReference = dataSnapshot.child(Definition.CATEGORIES);
//                    Month month = dataSnapshot.getValue(Month.class);
//                    updateSpecificMonth(refMonthKey, month);
//                    //delete old events
//
//
//                    setCategoriesFieldsEventUpdateValue(categoryDBReference, refMonthKey);
////                    setCategoriesEventUpdateValue(categoryDBReference,refMonthKey); // todo remove
//                } catch (Exception ex) {
//                    String message = ex.getMessage();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    // ****************************************************************************

    //    public void setCategoriesEventUpdateValue(DataSnapshot categoriesSnapshot, String refMonthKey){
//        for(DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
//            String categoryObjkey = categorySnapshot.getKey();
//            Category cat = categoriesSnapshot.getValue(Category.class);
//            updateSpecificCategory(refMonthKey, cat);
//            setCategoryEventUpdateValue(categoriesSnapshot, refMonthKey,categoryObjkey);
//        }
//    }
}

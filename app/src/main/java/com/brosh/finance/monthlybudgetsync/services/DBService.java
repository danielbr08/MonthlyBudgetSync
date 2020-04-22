package com.brosh.finance.monthlybudgetsync.services;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.login.Login;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.ChildEventListenerMap;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.objects.UserStartApp;
import com.brosh.finance.monthlybudgetsync.objects.ValueEventListenerMap;
import com.brosh.finance.monthlybudgetsync.ui.MainActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DBService {

    private static DBService instance;

    private DBService() {
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static DBService getInstance() {
        if (instance == null)
            instance = new DBService();
        return instance;
    }

    private ValueEventListener rootEventListener;

    private Map<String, Map<String, Budget>> budgetDBHM = new HashMap<>();
    private Map<String, Month> monthDBHM = new HashMap<>();
    private Set<String> shopsSet = new HashSet<String>();
//    private Map<String,String> sharesMap = new HashMap<>();

    private String userKey;
    private User user;

    public ValueEventListener getRootEventListener() {
        return rootEventListener;
    }

    public void setRootEventListener(ValueEventListener rootEventListener) {
        this.rootEventListener = rootEventListener;
    }

//    public Map<String, String> getSharesMap() {
//        return sharesMap;
//    }
//
//    public void setSharesMap(Map<String, String> sharesMap) {
//        this.sharesMap = sharesMap;
//    }

    public Set<String> getShopsSet() {
        return shopsSet;
    }

    public void setShopsSet(Set<String> shopsSet) {
        this.shopsSet = shopsSet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
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
            int chargeDay = 1; //todo dynamic
            monthDBHM.put(refMonthKey, new Month(refMonthKey, budgetNumber, chargeDay));
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
        List<Integer> budetNumbers = new ArrayList<>(Arrays.asList(0));
        for (String bugetNumber : budgetDBHM.keySet()) {
            budetNumbers.add(Integer.valueOf(bugetNumber));
        }
        return budetNumbers;
    }

    public List<Budget> getBudgetDataFromDB(long budgetNumber) {
        List<Budget> budgets = new ArrayList<>();
        if (!budgetDBHM.containsKey(String.valueOf(budgetNumber)))
            return budgets;
        budgets = new ArrayList<Budget>(budgetDBHM.get(String.valueOf(budgetNumber)).values());
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
        getDBMonthPath(refMonth).removeValue(); // todo  check if this call will run delete event listener node. if yes, the next line is not needed.
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

    public void initDB(final User user, Activity activity) {

        this.userKey = user.getDbKey();
        this.user = user;
        DatabaseReference databaseReference = Config.DatabaseReferenceMonthlyBudget.child(userKey);

        rootEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    startApp(activity);
                    return;
                }
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
                        case Definition.SHOPS: {
                            setShopsDB(myDataSnapshot);
                            break;
                        }
//                        case Definition.SHARES: {
//                            setSharesDB(myDataSnapshot);
//                            break;
//                        }
                    }
                }
                try {
                    startApp(activity);
                } catch (Exception e) {
                    String s = e.getMessage();// todo remove those lines
                    s = s;
                }
            }

            public void onCancelled(DatabaseError firebaseError) {
            }
        };

        databaseReference.addListenerForSingleValueEvent(rootEventListener);
    }

    public void setBudgetDB(DataSnapshot budgetsSnapshot) {
        // Set data
        for (DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
            String budgetNumber = budgetSnapshot.getKey();
            for (DataSnapshot mySnapshot : budgetsSnapshot.child(budgetNumber).getChildren()) {
                Budget budgetObj = mySnapshot.getValue(Budget.class);
                updateSpecificBudget(budgetNumber, budgetObj);
            }
            setAddChildBudgetNumberEvent(budgetSnapshot, budgetNumber);
        }
        // Set event add child
        setAddChildBudgetsEvent(budgetsSnapshot);
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

    public void setShopsDB(DataSnapshot shopsSnapshot) {
        ValueEventListener updateShopsEvent = new ValueEventListener() {//todo check if can delete
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                List<String> shops = value != null ? (List<String>) value : null;
                if (shops != null) {
                    shopsSet.clear();
                    shopsSet.addAll(shops);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(shopsSnapshot.getRef())) {
            shopsSnapshot.getRef().addValueEventListener(updateShopsEvent);
            addValueEventListener(shopsSnapshot.getRef(), updateShopsEvent);
        }
//
//        ChildEventListener addChildEvent = new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                String shop = dataSnapshot.getValue().toString();
//                shopsSet.add(shop);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                String key = dataSnapshot.getKey();
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                String shop = dataSnapshot.getValue().toString();
//                shopsSet.remove(shop);
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        };
//        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(shopsSnapshot.getRef())) {
//            shopsSnapshot.getRef().addChildEventListener(addChildEvent);
//            addChildValueEventListener(shopsSnapshot.getRef(), addChildEvent);
//        }
    }

//    public void setSharesDB(DataSnapshot shopsSnapshot) {
//        ChildEventListener addChildEvent = new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                String newUser = dataSnapshot.getKey();
//                String owner = dataSnapshot.getValue().toString();
//                sharesMap.put(newUser, owner);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                String key = dataSnapshot.getKey();
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                String shop = dataSnapshot.getValue().toString();
//                shopsSet.remove(shop);
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        };
//        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(shopsSnapshot.getRef())) {
//            shopsSnapshot.getRef().addChildEventListener(addChildEvent);
//            addChildValueEventListener(shopsSnapshot.getRef(), addChildEvent);
//        }
//    }

    public void setAddChildMonthEvent(DataSnapshot MonthDataSnapshot) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String refMonth = dataSnapshot.getKey();
                Month month = dataSnapshot.getValue(Month.class);
                month.setIsActive();
                DBService.getInstance().monthDBHM.put(refMonth, month);
                DataSnapshot categoriesDBSnapShot = dataSnapshot.child(Definition.CATEGORIES);
                if (categoriesDBSnapShot.exists()) {
                    setAddChildCategoryEvent(categoriesDBSnapShot, refMonth);
                }
                setTranIdNumeratorEventUpdateValue(dataSnapshot.child(Definition.TRAN_ID_NUMERATOR), refMonth); // todo check why fired twice(next line also)
                setBudgetNumberEventUpdateValue(dataSnapshot.child(Definition.BUDGET_NUMBER), refMonth);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                String refMonth = dataSnapshot.getKey();
//                thisObject.monthDBHM.remove(refMonth);
                deleteEventsListener(dataSnapshot.getRef());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(MonthDataSnapshot.getRef())) {
            MonthDataSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(MonthDataSnapshot.getRef(), addChildEvent);
        }
    }

    public void setAddChildCategoryEvent(DataSnapshot categoryDataSnapshot, final String refMonth) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Category cat = dataSnapshot.getValue(Category.class);
                DBService.getInstance().getCategories(refMonth).put(cat.getId(), cat);
                setCategoryFieldsEventUpdateValue(dataSnapshot, refMonth);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String catId = dataSnapshot.getKey();
                DBService.getInstance().getCategories(refMonth).remove(catId);
                deleteEventsListener(dataSnapshot.getRef());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(categoryDataSnapshot.getRef())) {
            categoryDataSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(categoryDataSnapshot.getRef(), addChildEvent);
        }
    }

    public void setAddChildTransactionEvent(DataSnapshot transactionDataSnapshot, final String refMonth, final String catId) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String tranId = dataSnapshot.getKey();
                Transaction tran = dataSnapshot.getValue(Transaction.class);
                DBService.getInstance().getCategoryById(refMonth, catId).getTransactions().put(tranId, tran);
                setTransactionFieldsEventUpdateValue(dataSnapshot, refMonth, catId);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String tranId = dataSnapshot.getKey();
                if (isTranExists(refMonth, catId, tranId)) { // todo for all child removed event
                    DBService.getInstance().getTransactions(refMonth, catId).remove(tranId);
                    deleteEventsListener(dataSnapshot.getRef());// todo delete anyway (method need to support case of have no event listener for this node)
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(transactionDataSnapshot.getRef())) {
            transactionDataSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(transactionDataSnapshot.getRef(), addChildEvent);
        }
    }

    private boolean isTranExists(String refMonth, String catId, String tranId) {
        if (isCatExists(refMonth, catId))
            return getTransactions(refMonth).contains(tranId);
        return false;
    }

    private boolean isCatExists(String refMonth, String catId) {
        if (isRefMonthExists(refMonth))
            return getCategories(refMonth).keySet().contains(catId);
        return false;
    }

    private boolean isRefMonthExists(String refMonth) {
        return monthDBHM.containsKey(refMonth);
    }

    public void startApp(Activity activity) {
        Intent mainActivityIntent = new Intent(activity.getApplicationContext(), MainActivity.class);
        mainActivityIntent.putExtra(Definition.USER, userKey);
        activity.startActivity(mainActivityIntent);
        ((UserStartApp) activity).getProgressBar().setVisibility(View.GONE);
        activity.finish();
    }

    private void setCategoryFieldsEventUpdateValue(final DataSnapshot categoryDBDataSnapshot, String refMonthKey) {
        String catId = categoryDBDataSnapshot.getKey();
        setCategoryBalanceEventUpdateValue(categoryDBDataSnapshot.child(Definition.BALANCE), refMonthKey, catId);
        setAddChildTransactionEvent(categoryDBDataSnapshot.child(Definition.TRANSACTIONS), refMonthKey, catId);
    }

    private void setCategoryBalanceEventUpdateValue(DataSnapshot categoryBalanceDBDataSnapshot, String refMonthKey, String catId) {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object value = (Object) dataSnapshot.getValue();
                if (value != null) {
                    Category currentCategory = monthDBHM.get(refMonthKey).getCategories().get(catId);
                    double balance = Double.valueOf(value.toString());
                    currentCategory.setBalance(balance);
                }
//                else
//                    deleteEventsListener(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        categoryBalanceDBDataSnapshot.getRef().addValueEventListener(event);
        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(categoryBalanceDBDataSnapshot.getRef())) {
            categoryBalanceDBDataSnapshot.getRef().addValueEventListener(event);
            addValueEventListener(categoryBalanceDBDataSnapshot.getRef(), event);
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
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fieldName = dataSnapshot.getKey();
                Object value = dataSnapshot.getValue();
                if (value != null) {
                    Transaction currentTransaction = monthDBHM.get(refMonthKey).getCategories().get(catId).getTransactions().get(tranId);
                    switch (fieldName) {
                        case Definition.IS_STORNO:
                            boolean isStorno = Boolean.valueOf(dataSnapshot.getValue().toString());
                            currentTransaction.setIsStorno(isStorno);
                            break;
                        case Definition.STORNO_OF:
                            int stornoOf = Integer.valueOf(dataSnapshot.getValue().toString());
                            currentTransaction.setStornoOf(stornoOf);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        transactionFieldDataSnapshot.getRef().addValueEventListener(event);

        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(transactionFieldDataSnapshot.getRef())) {
            transactionFieldDataSnapshot.getRef().addValueEventListener(event);
            addValueEventListener(transactionFieldDataSnapshot.getRef(), event);
        }
    }

    public void deleteEventListener(DatabaseReference eventDatabaseReference) {
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = ChildEventListenerMap.getInstance().getChildEventListenersHM();
        Map<DatabaseReference, ValueEventListener> valueEventListenersHM = ValueEventListenerMap.getInstance().getValueEventListenerHM();
        if (childEventListenersHM.containsKey(eventDatabaseReference)) {
            ChildEventListener childEvent = childEventListenersHM.get(eventDatabaseReference);
            eventDatabaseReference.removeEventListener(childEvent);
            childEventListenersHM.remove(eventDatabaseReference);
        } else if (valueEventListenersHM.containsKey(eventDatabaseReference)) {
            ValueEventListener valueEvent = valueEventListenersHM.get(eventDatabaseReference);
            eventDatabaseReference.removeEventListener(valueEvent);
            valueEventListenersHM.remove(eventDatabaseReference);
        }
    }

    public void deleteEventsListener(DatabaseReference eventDatabaseReference) {
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = ChildEventListenerMap.getInstance().getChildEventListenersHM();
        Map<DatabaseReference, ValueEventListener> valueEventListenersHM = ValueEventListenerMap.getInstance().getValueEventListenerHM();

        Set<DatabaseReference> allEventsPathDBreference = new HashSet();
        allEventsPathDBreference.addAll(childEventListenersHM.keySet());
        allEventsPathDBreference.addAll(valueEventListenersHM.keySet());

        String eventNodePath = eventDatabaseReference.toString();
        for (DatabaseReference childDatabaseReference : allEventsPathDBreference) { // find childs nodes of this event
            String eventNodePathInMap = childDatabaseReference.toString();
            if (eventNodePathInMap.startsWith(eventNodePath))
                deleteEventListener(childDatabaseReference);
        }
    }

    private void addChildValueEventListener(DatabaseReference databaseReference, ChildEventListener event) {
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = ChildEventListenerMap.getInstance().getChildEventListenersHM();
        childEventListenersHM.put(databaseReference, event);
    }

    private void addValueEventListener(DatabaseReference databaseReference, ValueEventListener event) {
        Map<DatabaseReference, ValueEventListener> valueEventListenersHM = ValueEventListenerMap.getInstance().getValueEventListenerHM();
        valueEventListenersHM.put(databaseReference, event);
    }

    // Returns new categories wroted
    public List<Category> writeCategoriesByBudgets(final String refMonth, int budgetNumber, List<Budget> budgets) {
        List<Category> wrotedCategories = new ArrayList<>();

        int idPerMonth = 0;
        for (Budget bgt : budgets) {
            if (isFrqTran(bgt))
                idPerMonth++;
            String catId = getDBCategoriesPath(refMonth).push().getKey();
            Category cat = budgetToCategory(bgt, catId, idPerMonth);
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
            String paymentMethod = Resources.getSystem().getString(R.string.payment_method);
            Date payDate = DateService.getCurrentDate(budget.getChargeDay());
            String yearMonth = DateService.getYearMonth(DateService.getTodayDate(), Config.SEPARATOR);
            String tranId = getDBTransactionsPath(yearMonth, catId).push().getKey();
            Transaction transaction = new Transaction(tranId, idPerMonth, budget.getCategoryName(), paymentMethod, budget.getShop(), payDate, budget.getValue());
            transactions.put(tranId, transaction);
            shopsSet.add(budget.getShop());
            cat.setTransactions(transactions);
            cat.withdrawal(budget.getValue());
        }
        return cat;
    }

    public boolean isAnyBudgetExists() {
        return this.budgetDBHM.size() > 0;
    }

    public void createNewMonth(int budgetNumber, String refMonth) {
        List<Budget> budgetsToConvert = getBudgetDataFromDB(budgetNumber);
        writeCategoriesByBudgets(refMonth, budgetNumber, budgetsToConvert);
        Month newMonth = getMonth(DateService.getYearMonth(DateService.getTodayDate(), Config.SEPARATOR));

        getDBMonthPath(refMonth).setValue(newMonth);
        updateShopsFB();

//        Set<String> updatedShops = new HashSet<String>(shopsSet);
//        updatedShops.removeAll(oldShops);
//        writeNewShopFB(updatedShops, oldShops.size());

    }

    public DatabaseReference getDBUserRootPath() {
        return FirebaseDatabase.getInstance().getReference(Definition.MONTHLY_BUDGET).child(userKey);
    }

    public DatabaseReference getDBBudgetsPath() {
        return getDBUserRootPath().child(Definition.BUDGETS);
    }

    public DatabaseReference getDBMonthsPath() {
        return getDBUserRootPath().child(Definition.MONTHS);
    }

    public DatabaseReference getDBMonthPath(String refMonth) {
        return getDBMonthsPath().child(refMonth);
    }

    public DatabaseReference getDBCategoriesPath(String refMonth) {
        return getDBMonthsPath().child(refMonth).child(Definition.CATEGORIES);
    }

    public DatabaseReference getDBTransactionsPath(String refMonth, String catId) {
        return getDBCategoriesPath(refMonth).child(catId).child(Definition.TRANSACTIONS);
    }

    public DatabaseReference getDBShopsPath() {
        return getDBUserRootPath().child(Definition.SHOPS);
    }

    public DatabaseReference getDBSaresPath() {
        return getDBUserRootPath().child(Definition.SHARES);
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

    public List<String> getAllMonthsYearMonth() {
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

    public List<Transaction> getTransactions(String refMonth, String catId) {
        if (catId == null) {
            return getTransactions(refMonth);
        }
        Category category = getCategoryById(refMonth, catId);
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

    public Category getCategoryById(String refMonth, String catId) {
        return getCategories(refMonth).get(catId);
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

//    private Transaction createTransactionByBudget(Budget bgt, String catId) {
//        int idPerMonth = 0;
//        String paymentMethod = getString(R.string.credit_card);
//        Date payDate = DateService.getCurrentDate(bgt.getChargeDay());
//        return new Transaction(idPerMonth, bgt.getCategoryName(), paymentMethod, bgt.getShop(), payDate, bgt.getValue());
//    }

    private void setIdNumerator(String refMonth, int idPerMonth) {
        monthDBHM.get(refMonth).setTranIdNumerator(idPerMonth);
    }

    private void setTranIdNumeratorEventUpdateValue(final DataSnapshot tranIdNumeratorDB, final String refMonthKey) {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long value = (Long) dataSnapshot.getValue();
                if (value != null)
                    monthDBHM.get(refMonthKey).setTranIdNumerator(value.intValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        tranIdNumeratorDB.getRef().addValueEventListener(event);

        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(tranIdNumeratorDB.getRef())) {
            tranIdNumeratorDB.getRef().addValueEventListener(event);
            addValueEventListener(tranIdNumeratorDB.getRef(), event);
        }
    }

    private void setBudgetNumberEventUpdateValue(final DataSnapshot budgetNumberDB, final String refMonthKey) {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long value = (Long) dataSnapshot.getValue();
                if (value != null)
                    monthDBHM.get(refMonthKey).setBudgetNumber(value.intValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        budgetNumberDB.getRef().addValueEventListener(event);

        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(budgetNumberDB.getRef())) {
            budgetNumberDB.getRef().addValueEventListener(event);
            addValueEventListener(budgetNumberDB.getRef(), event);
        }
    }

    public void addNewCategoriesToExistingMonth(String refMonth, int budgetNumber, List<Budget> budgets) {
        int idPerMonth = monthDBHM.get(refMonth).getTranIdNumerator() + 1;
        DatabaseReference categoriesDBReference = getDBCategoriesPath(refMonth);
        String catId = categoriesDBReference.push().getKey();
        for (Budget budget : budgets) {
            Category cat = budgetToCategory(budget, catId, idPerMonth++);
            updateSpecificCategory(refMonth, budgetNumber, cat);
            categoriesDBReference.child(catId).setValue(cat);
        }
    }

    private void setAddChildBudgetNumberEvent(DataSnapshot budgetSnapshot, final String budgetNumber) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Budget budget = dataSnapshot.getValue(Budget.class);
                DBService.getInstance().updateSpecificBudget(budgetNumber, budget);
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
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(budgetSnapshot.getRef())) {
            budgetSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(budgetSnapshot.getRef(), addChildEvent);
        }
    }

    private void setAddChildBudgetsEvent(final DataSnapshot budgetsSnapshot) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GenericTypeIndicator<Map<String, Budget>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Budget>>() {
                };
                String budgetNumber = dataSnapshot.getKey();
                Map<String, Budget> budgets = (Map<String, Budget>) dataSnapshot.getValue(genericTypeIndicator);
                DBService.getInstance().budgetDBHM.put(budgetNumber, budgets);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String budgetNumber = dataSnapshot.getKey();
                DBService.getInstance().budgetDBHM.remove(budgetNumber);
                deleteEventsListener(dataSnapshot.getRef());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(budgetsSnapshot.getRef())) {
            budgetsSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(budgetsSnapshot.getRef(), addChildEvent);
        }
    }

    public void updateBudgetNumberFB(String refMonth, int budgetNumber) {
        getDBMonthPath(refMonth).child(Definition.BUDGET_NUMBER).setValue(budgetNumber);
    }

    public void updateBudgetNumber(String refMonth, int budgetNumber) {
        getMonth(refMonth).setBudgetNumber(budgetNumber);
    }

    public void writeNewShopFB(String newShop) {
        int size = shopsSet.size();
        size = shopsSet.contains(newShop) ? size - 1 : size;
        String indexShopKey = String.valueOf(size);
        getDBShopsPath().child(indexShopKey).setValue(newShop);
    }

    public void updateShopsFB() {
        List<String> shops = new ArrayList<>(shopsSet);
        getDBShopsPath().setValue(shops);
    }

//    public void share(String emailToShare) throws Exception {
//        if(isEmailAlreadyShared(emailToShare)){
//            throw new Exception(language.emailAlreadyshared);
//        }
//        String emailCommaReplaced = emailToShare.replace(Definition.DOT, Definition.COMMA);
//        getDBSaresPath().child(emailCommaReplaced).setValue(userKey);
//    }
//
//    public boolean isEmailAlreadyShared(String emailToShare) {
//        String emailCommaReplaced = emailToShare.replace(Definition.DOT, Definition.COMMA);
//        return sharesMap.containsKey(emailCommaReplaced) ? true : false;
//    }


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

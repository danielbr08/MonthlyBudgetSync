package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.ChildEventListenerMap;
import com.brosh.finance.monthlybudgetsync.objects.Share;
import com.brosh.finance.monthlybudgetsync.objects.ShareStatus;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DBUtil {
    private static final String TAG = "DBUtil";

    private static DBUtil instance;
    private static FirebaseDatabase database;

    private ValueEventListener rootEventListener;

    private static User user;
    private static Map<String, Map<String, Budget>> budgetDBHM = new HashMap<>();
    private static Map<String, Month> monthDBHM = new HashMap<>();
    private static Set<String> shopsSet = new HashSet<String>();
    private static Map<String, Share> sharesMap = new HashMap<>();

    private String userKey;
    private Context context;


    public void clear() {
        instance = new DBUtil();
        budgetDBHM.clear();
        monthDBHM.clear();
        shopsSet.clear();
        sharesMap.clear();
        user = null;
    }

    private DBUtil() {
    }

    public static DBUtil getInstance() {
        if (instance == null)
            instance = new DBUtil();
        return instance;
    }

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
            database.getReference().keepSynced(true);
        }
        return database;
    }

    public ValueEventListener getRootEventListener() {
        return rootEventListener;
    }

    public void setRootEventListener(ValueEventListener rootEventListener) {
        this.rootEventListener = rootEventListener;
    }

    public Map<String, Share> getSharesMap() {
        return sharesMap;
    }

    public void setSharesMap(Map<String, Share> sharesMap) {
        this.sharesMap = sharesMap;
    }

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
            int chargeDay = user.getUserSettings().getChargeDay();
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
            budgetDBHM.put(budgetNumber, new HashMap<>());
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
        budgets = new ArrayList<>(budgetDBHM.get(String.valueOf(budgetNumber)).values());
        try {
            Collections.sort(budgets, ComparatorUtil.COMPARE_BY_CATEGORY_PRIORITY);
        } catch (Exception e) {
            String s = e.getMessage();
            s = s;
            Log.e(TAG, e.getMessage());
        }
        return budgets;
    }

    public boolean isCurrentRefMonthExists() {
        String currentRefMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), Definitions.DASH);
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
        this.context = activity;
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
                    Object value = myDataSnapshot.getValue();
                    boolean hasData = !(value instanceof String && value.equals(""));
                    switch (keyNode) {
                        case Definitions.BUDGETS: {
                            if (hasData) {
                                setBudgetDB(myDataSnapshot);
                            }
                            // Set event add child
                            setAddChildBudgetsEvent(myDataSnapshot);
                            break;
                        }
                        case Definitions.MONTHS: {
                            if (hasData) {
                                setMonthsDB(myDataSnapshot);
                            }
                            // Set event add child
                            setAddChildMonthEvent(myDataSnapshot);
                            break;
                        }
                        case Definitions.SHOPS: {
                            if (hasData) {
                                setShopsDB(myDataSnapshot);
                            }
                            break;
                        }
                    }
                }
                try {
                    startApp(activity);
                } catch (Exception e) {
                    String s = e.getMessage();// todo remove those lines
                    s = s;
                    Log.e(TAG, e.getMessage());
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
    }

    public void setMonthsDB(DataSnapshot monthsSnapshot) {
        // Set data
        for (DataSnapshot currentMonthDataSnapshot : monthsSnapshot.getChildren()) {
            String refMonthKey = currentMonthDataSnapshot.getKey();
            Month month = currentMonthDataSnapshot.getValue(Month.class);
            month.setIsActive();
            updateSpecificMonth(refMonthKey, month);
        }
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

    public void setSharesDB(DataSnapshot shopsSnapshot) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String sharedUser = dataSnapshot.getKey();
                Share share = dataSnapshot.getValue(Share.class);
                sharesMap.put(sharedUser, share);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
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
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(shopsSnapshot.getRef())) {
            shopsSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(shopsSnapshot.getRef(), addChildEvent);
        }
    }

    public void setAddChildMonthEvent(DataSnapshot MonthDataSnapshot) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String refMonth = dataSnapshot.getKey();
                Month month = dataSnapshot.getValue(Month.class);
                month.setIsActive();
                monthDBHM.put(refMonth, month);
                DataSnapshot categoriesDBSnapShot = dataSnapshot.child(Definitions.CATEGORIES);
                if (categoriesDBSnapShot.exists()) {
                    setAddChildCategoryEvent(categoriesDBSnapShot, refMonth);
                }
                setTranIdNumeratorEventUpdateValue(dataSnapshot.child(Definitions.TRAN_ID_NUMERATOR), refMonth); // todo check why fired twice(next line also)
                setBudgetNumberEventUpdateValue(dataSnapshot.child(Definitions.BUDGET_NUMBER), refMonth);
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
                getCategories(refMonth).put(cat.getId(), cat);
                setCategoryFieldsEventUpdateValue(dataSnapshot, refMonth);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String catId = dataSnapshot.getKey();
                getCategories(refMonth).remove(catId);
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
                Category cat = getCategoryById(refMonth, catId);
                cat.getTransactions().put(tranId, tran);
                double newBalance = cat.getBudget() - getTotalTransactionsSum(refMonth, catId, true);
                if (cat.getBalance() != newBalance) {
                    getDBCategoriesPath(refMonth).child(catId).child(Definitions.BALANCE).setValue(newBalance);
                }
                getCategoryById(refMonth, catId).setBalance(newBalance);
                setTransactionFieldsEventUpdateValue(dataSnapshot, refMonth, catId);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String tranId = dataSnapshot.getKey();
                if (isTranExists(refMonth, catId, tranId)) { // todo for all child removed event
                    getTransactions(refMonth, catId).remove(tranId);
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

    private double getTotalTransactionsSum(String refMonth, String catId, boolean onlyActive) {
        List<Transaction> catTrans = this.getTransactions(refMonth, catId);
        double sum = 0;
        for (Transaction trn : catTrans) {
            if (onlyActive && trn.isDeleted())
                continue;
            sum += trn.getPrice();
        }
        return sum;
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
        mainActivityIntent.putExtra(Definitions.USER, user);
        activity.startActivity(mainActivityIntent);
        ((UserStartApp) activity).getProgressBar().setVisibility(View.GONE);
        activity.finish();
    }

    private void setCategoryFieldsEventUpdateValue(final DataSnapshot categoryDBDataSnapshot, String refMonthKey) {
        String catId = categoryDBDataSnapshot.getKey();
//        setCategoryBalanceEventUpdateValue(categoryDBDataSnapshot.child(Definition.BALANCE), refMonthKey, catId);
        setAddChildTransactionEvent(categoryDBDataSnapshot.child(Definitions.TRANSACTIONS), refMonthKey, catId);
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
        List<String> transactionFields = Arrays.asList(Definitions.DELETED);
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
                        case Definitions.DELETED:
                            boolean deleted = Boolean.valueOf(dataSnapshot.getValue().toString());
                            currentTransaction.setDeleted(deleted);
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
            String paymentMethod = context.getString(R.string.credit_card);
            Date payDate = DateUtil.getCurrentDate(budget.getChargeDay());
            String yearMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), Config.SEPARATOR);
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
        Month newMonth = getMonth(refMonth);

        getDBMonthPath(refMonth).setValue(newMonth);
        updateShopsFB();

//        Set<String> updatedShops = new HashSet<String>(shopsSet);
//        updatedShops.removeAll(oldShops);
//        writeNewShopFB(updatedShops, oldShops.size());

    }

    public DatabaseReference getDBUserRootPath() {
        return getDatabase().getReference(Definitions.MONTHLY_BUDGET).child(userKey);
    }

    public DatabaseReference getDBBudgetsPath() {
        return getDBUserRootPath().child(Definitions.BUDGETS);
    }

    public DatabaseReference getDBMonthsPath() {
        return getDBUserRootPath().child(Definitions.MONTHS);
    }

    public DatabaseReference getDBMonthPath(String refMonth) {
        return getDBMonthsPath().child(refMonth);
    }

    public DatabaseReference getDBCategoriesPath(String refMonth) {
        return getDBMonthsPath().child(refMonth).child(Definitions.CATEGORIES);
    }

    public DatabaseReference getDBTransactionsPath(String refMonth, String catId) {
        return getDBCategoriesPath(refMonth).child(catId).child(Definitions.TRANSACTIONS);
    }

    public DatabaseReference getDBShopsPath() {
        return getDBUserRootPath().child(Definitions.SHOPS);
    }

    public DatabaseReference getDBSaresPath() {
        return Config.DatabaseReferenceShares;
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
            Log.e(TAG, e.getMessage());
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
            return new ArrayList<>(category.getTransactions().values());
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

    public List<Transaction> getTransactions(String refMonth, String catId, boolean onlyActive) {
        if (onlyActive) {
            List<Transaction> activeTtransactions = new ArrayList<>();
            for (Transaction tran : getTransactions(refMonth, catId)) {
                if (!tran.isDeleted()) {
                    activeTtransactions.add(tran);
                }
            }
            return activeTtransactions;
        }
        return getTransactions(refMonth, catId);
    }

    public double getTransactionsSum(String refMonth, String catId, boolean onlyActive) {
        List<Transaction> transactions = getTransactions(refMonth, catId, onlyActive);
        double sum = 0d;
        for (Transaction tran : transactions) {
            sum += tran.getPrice();
        }
        return sum;
    }

    public double getTransactionsSum(String refMonth, boolean onlyActive) {
        List<Transaction> transactions = getTransactions(refMonth);
        double sum = 0d;
        for (Transaction tran : transactions) {
            if (!onlyActive || !tran.isDeleted()) {
                sum += tran.getPrice();
            }
        }
        return sum;
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
                updateSpecificBudget(budgetNumber, budget);
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
                Map<String, Budget> budgets = dataSnapshot.getValue(genericTypeIndicator);
                budgetDBHM.put(budgetNumber, budgets);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String budgetNumber = dataSnapshot.getKey();
                budgetDBHM.remove(budgetNumber);
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
        getDBMonthPath(refMonth).child(Definitions.BUDGET_NUMBER).setValue(budgetNumber);
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

    public void markDeleteTransaction(String refMonth, Transaction tran) {
        String catId = getCategoryByName(refMonth, tran.getCategory()).getId();
        getDBTransactionsPath(refMonth, catId).runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.child(tran.getId()).child(Definitions.DELETED).setValue(tran.isDeleted());
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }

    public void updateCategoryBudgetValue(String refMonth, String catId) {
        Double balance = getCategoryById(refMonth, catId).getBudget() - getTotalTransactionsSum(refMonth, catId, true);
        getDBCategoriesPath(refMonth).runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.child(catId).child(Definitions.BALANCE).setValue(balance);
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }

    public void share(String emailToShare) throws Exception {
        if (isEmailAlreadyShared(emailToShare)) {
            // todo ask if want to share anyway(loss data)
            throw new Exception(Definitions.EMAIL_ALREADY_SHARED);
        }
        String emailCommaReplaced = emailToShare.replace(Definitions.DOT, Definitions.COMMA);
        Share share = new Share(user.getEmail(), emailToShare, user.getDbKey(), ShareStatus.PENDING);
        getDBSaresPath().child(emailCommaReplaced).setValue(share);
    }

    public boolean isEmailAlreadyShared(String emailToShare) {
        String emailCommaReplaced = emailToShare.replace(Definitions.DOT, Definitions.COMMA);
        Share share = sharesMap.containsKey(emailCommaReplaced) ? sharesMap.get(emailCommaReplaced) : null;
        return share != null && share.getStatus() == ShareStatus.SUCCESSFULLY_SHARED ? true : false;
    }

    public static void showShareDialogEnterApp(Context context, DataSnapshot snapshot, User user) {
        Share share = snapshot.child(Definitions.SHARES).child(user.getDbKey()).getValue(Share.class);
        if (share.getStatus() == ShareStatus.PENDING) {
            String ownerDBKey = share.getDbKey();
            User ownerUser = snapshot.child(Definitions.USERS).child(ownerDBKey).getValue(User.class);
            String userName = ownerUser.getName();
            String question = String.format(context.getString(R.string.share_budget_question), userName);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            share.setStatus(ShareStatus.SUCCESSFULLY_SHARED);
                            user.setDbKey(ownerDBKey);
                            snapshot.child(Definitions.USERS).child(TextUtil.getEmailComma(share.getUserEmail())).child(Definitions.dbKey).getRef().setValue(ownerDBKey);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            share.setStatus(ShareStatus.DENY);
                            break;
                    }
                    snapshot.child(Definitions.SHARES).child(TextUtil.getEmailComma(share.getUserEmail())).getRef().setValue(share);
                    DBUtil.getInstance().initDB(user, (Activity) context);
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(question).setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();

        } else {
            DBUtil.getInstance().initDB(user, (Activity) context);
        }
    }


    //****************************************************************************************
}

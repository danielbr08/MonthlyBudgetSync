package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.login.Login;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database utility class for Firebase operations.
 * Implements thread-safe singleton pattern with proper null safety.
 */
public final class DBUtil {
    private static final String TAG = "DBUtil";

    // Thread-safe singleton with volatile
    private static volatile DBUtil instance;
    private static volatile FirebaseDatabase database;
    private static final Object LOCK = new Object();
    private static final Object DB_LOCK = new Object();

    private volatile ValueEventListener rootEventListener;

    // Use thread-safe collections
    private static volatile User user;
    private static final Map<String, Map<String, Budget>> budgetDBHM = new ConcurrentHashMap<>();
    private static final Map<String, Month> monthDBHM = new ConcurrentHashMap<>();
    private static final Set<String> shopsSet = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, String> userMailUidMap = new ConcurrentHashMap<>();
    private static final Map<String, Share> sharesMap = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> ownersMap = new ConcurrentHashMap<>();

    private String userKey;
    // Use WeakReference to avoid memory leaks
    private WeakReference<Context> contextRef;

    /**
     * Clears all cached data. Call this on logout.
     */
    public void clear() {
        synchronized (LOCK) {
            budgetDBHM.clear();
            monthDBHM.clear();
            shopsSet.clear();
            sharesMap.clear();
            userMailUidMap.clear();
            ownersMap.clear();
            user = null;
            userKey = null;
            rootEventListener = null;
        }
    }

    private DBUtil() {
        Context ctx = Login.getContext();
        contextRef = new WeakReference<>(ctx);
    }

    /**
     * Returns the application context safely.
     * @return Context or null if not available
     */
    @Nullable
    public Context getContext() {
        Context ctx = contextRef != null ? contextRef.get() : null;
        if (ctx == null) {
            ctx = Login.getContext();
            if (ctx != null) {
                contextRef = new WeakReference<>(ctx);
            }
        }
        return ctx;
    }

    /**
     * Thread-safe singleton accessor using double-checked locking.
     * @return DBUtil instance
     */
    public static DBUtil getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DBUtil();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the Firebase database instance, initializing if needed.
     * Thread-safe with proper persistence configuration.
     * @return FirebaseDatabase instance
     */
    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            synchronized (DB_LOCK) {
                if (database == null) {
                    try {
                        database = FirebaseDatabase.getInstance();
                        database.setPersistenceEnabled(true);
                        database.getReference().keepSynced(true);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to initialize Firebase database", e);
                        // Return default instance without persistence if already set
                        database = FirebaseDatabase.getInstance();
                    }
                }
            }
        }
        return database;
    }

    public ValueEventListener getRootEventListener() {
        return rootEventListener;
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static Map<String, Set<String>> getOwnersMap() {
        return DBUtil.ownersMap;
    }

    public static Map<String, Share> getSharesMap() {
        return sharesMap;
    }

    public static Map<String, String> getUserMailUidMap() {
        return userMailUidMap;
    }

    public Set<String> getShopsSet() {
        return shopsSet;
    }

    public User getUser() {
        return user;
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public void setUser(User user) {
        DBUtil.user = user;
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public String getUserKey() {
        return userKey;
    }

    public Month getMonth(String refMonth) {
        return monthDBHM.get(refMonth);
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public Map<String, Map<String, Budget>> getBudgetDBHM() {
        return budgetDBHM;
    }

    // Used internally by getCategoriesByPriority
    private Map<String, Month> getMonthDBHM() {
        return monthDBHM;
    }

    public void updateSpecificCategory(String refMonthKey, int budgetNumber, Category categoryObj) {
        if (refMonthKey == null || categoryObj == null) {
            Log.w(TAG, "updateSpecificCategory called with null parameters");
            return;
        }
        
        Month month = monthDBHM.get(refMonthKey);
        if (month == null) {
            int chargeDay = 1; // Default charge day
            if (user != null) {
                chargeDay = user.getUserSettings().getChargeDay();
            }
            month = new Month(refMonthKey, budgetNumber, chargeDay);
            monthDBHM.put(refMonthKey, month);
        }
        month.addCategory(categoryObj.getId(), categoryObj);
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public void updateSpecificTransaction(String refMonthKey, String categoryObjKey, String transactionObj, Transaction trnObj) {
        if (refMonthKey == null || categoryObjKey == null || transactionObj == null || trnObj == null) {
            Log.w(TAG, "updateSpecificTransaction called with null parameters");
            return;
        }
        
        Month month = monthDBHM.get(refMonthKey);
        if (month == null) {
            Log.w(TAG, "Month not found for key: " + refMonthKey);
            return;
        }
        
        Map<String, Category> categories = month.getCategories();
        if (categories == null) {
            Log.w(TAG, "Categories map is null for month: " + refMonthKey);
            return;
        }
        
        Category category = categories.get(categoryObjKey);
        if (category == null) {
            Log.w(TAG, "Category not found: " + categoryObjKey);
            return;
        }
        
        category.addTransactions(transactionObj, trnObj);
    }

    public void updateSpecificBudget(String budgetNumber, Budget budgetObj) {
        if (budgetNumber == null || budgetObj == null) {
            Log.w(TAG, "updateSpecificBudget called with null parameters");
            return;
        }
        
        Map<String, Budget> budgetMap = budgetDBHM.computeIfAbsent(budgetNumber, k -> new HashMap<>());
        budgetMap.put(budgetObj.getId(), budgetObj);
    }

    public void updateSpecificMonth(String refMonthKey, Month monthObj) {
        monthDBHM.put(refMonthKey, monthObj);
    }

    public int getMaxBudgetNumber() {
        return Collections.max(getBudgetNumbersAsInt());
    }

    public List<Integer> getBudgetNumbersAsInt() {
        List<Integer> budgetNumbers = new ArrayList<>(List.of(0));
        for (String budgetNumber : budgetDBHM.keySet()) {
            budgetNumbers.add(Integer.parseInt(budgetNumber));
        }
        return budgetNumbers;
    }

    /**
     * Retrieves budget data from local cache sorted by category priority.
     * @param budgetNumber the budget number to retrieve
     * @return List of budgets, never null (empty list if not found)
     */
    @NonNull
    public List<Budget> getBudgetDataFromDB(long budgetNumber) {
        String budgetKey = String.valueOf(budgetNumber);
        Map<String, Budget> budgetMap = budgetDBHM.get(budgetKey);
        
        if (budgetMap == null || budgetMap.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Budget> budgets = new ArrayList<>(budgetMap.values());
        try {
            budgets.sort(ComparatorUtil.COMPARE_BY_CATEGORY_PRIORITY);
        } catch (Exception e) {
            Log.e(TAG, "Error sorting budgets: " + e.getMessage(), e);
        }
        return budgets;
    }

    public boolean isCurrentRefMonthExists() {
        String currentRefMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), Definitions.DASH);
        Month month = monthDBHM.get(currentRefMonth);
        if (month == null) {
            return false;
        }
        Map<String, Category> categories = month.getCategories();
        return categories != null && !categories.isEmpty();
    }

    /**
     * Deletes a specific month's data from both local cache and Firebase.
     * @param refMonth the reference month key to delete
     */
    public void deleteDataRefMonth(String refMonth) {
        if (refMonth == null) {
            return;
        }
        monthDBHM.remove(refMonth);
        getDBMonthPath(refMonth).removeValue();
    }


    /**
     * Initializes the database with user data.
     * Sets up all necessary listeners for real-time updates.
     * 
     * @param user the authenticated user
     * @param activity the calling activity for UI operations
     * @throws IllegalArgumentException if user or activity is null
     */
    public void initDB(@NonNull final User user, @NonNull Activity activity) {
        // @NonNull annotation ensures these are not null, but dbKey check is still needed
        if (user.getDbKey() == null) {
            Log.e(TAG, "User dbKey is null, cannot initialize database");
            return;
        }

        this.userKey = user.getDbKey();
        DBUtil.user = user;
        this.contextRef = new WeakReference<>(activity);
        DatabaseReference databaseReference = Config.DatabaseReferenceRoot;

        rootEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    DataSnapshot monthlyBudgetDataSnapshot = dataSnapshot.child(Definitions.MONTHLY_BUDGET).child(userKey);
                    if (!monthlyBudgetDataSnapshot.exists()) {
                        startApp(activity);
                        return;
                    }
                    
                    // Process shares first for proper initialization order
                    DataSnapshot sharesSnapshot = dataSnapshot.child(Definitions.SHARES);
                    if (sharesSnapshot.exists()) {
                        setSharesDB(sharesSnapshot);
                    }
                    
                    setUsersDB(dataSnapshot);
                    setOwnersDB(dataSnapshot);
                    
                    for (DataSnapshot myDataSnapshot : monthlyBudgetDataSnapshot.getChildren()) {
                        String keyNode = myDataSnapshot.getKey();
                        if (keyNode == null) continue;
                        
                        Object value = myDataSnapshot.getValue();
                        boolean hasData = value != null && !(value instanceof String str && str.isEmpty());
                        
                        switch (keyNode) {
                            case Definitions.BUDGETS:
                                if (hasData) {
                                    setBudgetDB(myDataSnapshot);
                                }
                                setAddChildBudgetsEvent(myDataSnapshot);
                                break;
                            case Definitions.MONTHS:
                                if (hasData) {
                                    setMonthsDB(myDataSnapshot);
                                }
                                setAddChildMonthEvent(myDataSnapshot);
                                break;
                            case Definitions.SHOPS:
                                if (hasData) {
                                    setShopsDB(myDataSnapshot);
                                }
                                break;
                            default:
                                // Ignore unknown nodes
                                break;
                        }
                    }
                    startApp(activity);
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
                    // Still try to start app even on partial failure
                    startApp(activity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError firebaseError) {
                Log.e(TAG, "Database initialization cancelled: " + firebaseError.getMessage());
            }
        };

        databaseReference.addListenerForSingleValueEvent(rootEventListener);
    }

    private void setSharesDB(DataSnapshot sharesSnapshot) {
        // Set data
        for (DataSnapshot shareSnapshot : sharesSnapshot.getChildren()) {
            String sharedUserUid = shareSnapshot.getKey();
            Share share = shareSnapshot.getValue(Share.class);
            sharesMap.put(sharedUserUid, share);
        }
        setAddChildSharesDB(sharesSnapshot);
    }

    public void setUsersDB(DataSnapshot rootSnapshot) {
        DataSnapshot emailUidSnapshot = rootSnapshot.child(Definitions.EMAIL_UID);
        DataSnapshot usersSnapshot = rootSnapshot.child(Definitions.USERS);
        // Set data
        for (DataSnapshot snapshot : emailUidSnapshot.getChildren()) {
            String email = snapshot.getKey();
            Object userUidObj = snapshot.getValue();
            if (email != null && userUidObj != null) {
                String userUid = userUidObj.toString();
                userMailUidMap.put(TextUtil.getEmailComma(email), userUid);
            }
        }
        setAddChildUsersDB(usersSnapshot);
    }

    public void setOwnersDB(DataSnapshot rootSnapshot) {
        DataSnapshot ownersSnapshot = rootSnapshot.child(Definitions.OWNERS);
        // Set data
        Object value = ownersSnapshot.getValue();
        @SuppressWarnings("unchecked")
        Map<String, List<String>> owners = value != null ? (Map<String, List<String>>) value : null;

        if (owners != null) {
            ownersMap.clear();
            for (Map.Entry<String, List<String>> entry : owners.entrySet()) {
                String owner = entry.getKey();
                List<String> guests = entry.getValue();
                if (owner == null || guests == null)
                    continue;
                Set<String> ownerGuestsSet = new HashSet<>(guests);
                ownersMap.put(owner, ownerGuestsSet);
            }
        }
        setAddChildOwnersDB(ownersSnapshot);
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
            if (month != null) {
                month.setIsActive();
                updateSpecificMonth(refMonthKey, month);
            }
        }
    }

    /**
     * Sets up the shops database listener to sync shop names.
     */
    public void setShopsDB(DataSnapshot shopsSnapshot) {
        ValueEventListener updateShopsEvent = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> shops = (List<String>) value;
                    synchronized (shopsSet) {
                        shopsSet.clear();
                        shopsSet.addAll(shops);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Shops listener cancelled: " + databaseError.getMessage());
            }
        };
        
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(shopsSnapshot.getRef())) {
            shopsSnapshot.getRef().addValueEventListener(updateShopsEvent);
            addValueEventListener(shopsSnapshot.getRef(), updateShopsEvent);
        }
    }

    public void setAddChildUsersDB(DataSnapshot shopsSnapshot) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User userSnapshot = dataSnapshot.getValue(User.class);
                if (userSnapshot != null && userSnapshot.getEmail() != null && userSnapshot.getUid() != null) {
                    userMailUidMap.put(TextUtil.getEmailComma(userSnapshot.getEmail()), userSnapshot.getUid());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User userSnapshot = dataSnapshot.getValue(User.class);
                if (userSnapshot != null && userSnapshot.getEmail() != null && userSnapshot.getUid() != null) {
                    userMailUidMap.put(TextUtil.getEmailComma(userSnapshot.getEmail()), userSnapshot.getUid());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                User userSnapshot = dataSnapshot.getValue(User.class);
                if (userSnapshot != null && userSnapshot.getEmail() != null) {
                    userMailUidMap.remove(TextUtil.getEmailComma(userSnapshot.getEmail()));
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // No action needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Users listener cancelled: " + databaseError.getMessage());
            }
        };
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(shopsSnapshot.getRef())) {
            shopsSnapshot.getRef().addChildEventListener(addChildEvent);
            addChildValueEventListener(shopsSnapshot.getRef(), addChildEvent);
        }
    }

    public void setAddChildOwnersDB(DataSnapshot shopsSnapshot) {
        ValueEventListener updateOwnersEvent = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                @SuppressWarnings("unchecked")
                Map<String, List<String>> owners = value != null ? (Map<String, List<String>>) value : null;
                if (owners != null) {
                    ownersMap.clear();
                    for (Map.Entry<String, List<String>> entry : owners.entrySet()) {
                        String owner = entry.getKey();
                        List<String> guests = entry.getValue();
                        if (owner == null || guests == null)
                            continue;
                        Set<String> ownerGuestsSet = new HashSet<>(guests);
                        ownersMap.put(owner, ownerGuestsSet);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        if (!ChildEventListenerMap.getInstance().isEventAlreadyExists(shopsSnapshot.getRef())) {
            shopsSnapshot.getRef().addValueEventListener(updateOwnersEvent);
            addValueEventListener(shopsSnapshot.getRef(), updateOwnersEvent);
        }
    }

    public void setAddChildSharesDB(DataSnapshot shopsSnapshot) {
        ChildEventListener addChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String sharedUserUid = dataSnapshot.getKey();
                Share share = dataSnapshot.getValue(Share.class);
                sharesMap.put(sharedUserUid, share);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String sharedUser = dataSnapshot.getKey();
                Share share = dataSnapshot.getValue(Share.class);
                sharesMap.put(sharedUser, share);
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
                setTranIdNumeratorEventUpdateValue(dataSnapshot.child(Definitions.TRAN_ID_NUMERATOR), refMonth);
                setBudgetNumberEventUpdateValue(dataSnapshot.child(Definitions.BUDGET_NUMBER), refMonth);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
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
                if (cat != null && cat.getId() != null) {
                    Map<String, Category> categories = getCategories(refMonth);
                    categories.put(cat.getId(), cat);
                    setCategoryFieldsEventUpdateValue(dataSnapshot, refMonth);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // No action needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String catId = dataSnapshot.getKey();
                if (catId != null) {
                    getCategories(refMonth).remove(catId);
                }
                deleteEventsListener(dataSnapshot.getRef());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // No action needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Category listener cancelled: " + databaseError.getMessage());
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
                if (tranId == null || tran == null) {
                    return;
                }
                
                Category cat = getCategoryById(refMonth, catId);
                if (cat == null) {
                    Log.w(TAG, "Category not found for transaction: " + tranId);
                    return;
                }
                
                cat.getTransactions().put(tranId, tran);
                double newBalance = cat.getBudget() - getTotalTransactionsSum(refMonth, catId, true);
                if (Double.compare(cat.getBalance(), newBalance) != 0) {
                    getDBCategoriesPath(refMonth).child(catId).child(Definitions.BALANCE).setValue(newBalance);
                }
                cat.setBalance(newBalance);
                setTransactionFieldsEventUpdateValue(dataSnapshot, refMonth, catId);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // No action needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String tranId = dataSnapshot.getKey();
                if (tranId != null && isTranExists(refMonth, catId, tranId)) {
                    Category cat = getCategoryById(refMonth, catId);
                    if (cat != null) {
                        cat.getTransactions().remove(tranId);
                    }
                }
                deleteEventsListener(dataSnapshot.getRef());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // No action needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Transaction listener cancelled: " + databaseError.getMessage());
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
        if (!isCatExists(refMonth, catId) || tranId == null) {
            return false;
        }
        Category category = getCategoryById(refMonth, catId);
        return category != null && category.getTransactions().containsKey(tranId);
    }

    private boolean isCatExists(String refMonth, String catId) {
        if (isRefMonthExists(refMonth))
            return getCategories(refMonth).containsKey(catId);
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
        setAddChildTransactionEvent(categoryDBDataSnapshot.child(Definitions.TRANSACTIONS), refMonthKey, catId);
    }

    @SuppressWarnings("unused") // May be used for future functionality
    private void setCategoryBalanceEventUpdateValue(DataSnapshot categoryBalanceDBDataSnapshot, String refMonthKey, String catId) {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                if (value != null) {
                    Month month = monthDBHM.get(refMonthKey);
                    if (month == null || month.getCategories() == null) {
                        return;
                    }
                    Category currentCategory = month.getCategories().get(catId);
                    if (currentCategory != null) {
                        try {
                            double balance = Double.parseDouble(value.toString());
                            currentCategory.setBalance(balance);
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Invalid balance value: " + value);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Balance listener cancelled: " + databaseError.getMessage());
            }
        };
        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(categoryBalanceDBDataSnapshot.getRef())) {
            categoryBalanceDBDataSnapshot.getRef().addValueEventListener(event);
            addValueEventListener(categoryBalanceDBDataSnapshot.getRef(), event);
        }
    }

    private void setTransactionFieldsEventUpdateValue(final DataSnapshot transactionDBDataSnapshot, String refMonth, String catId) {
        String tranId = transactionDBDataSnapshot.getKey();
        List<String> transactionFields = Collections.singletonList(Definitions.DELETED);
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
                if (value == null) {
                    return;
                }
                
                Month month = monthDBHM.get(refMonthKey);
                if (month == null || month.getCategories() == null) {
                    return;
                }
                
                Category category = month.getCategories().get(catId);
                if (category == null || category.getTransactions() == null) {
                    return;
                }
                
                Transaction currentTransaction = category.getTransactions().get(tranId);
                if (currentTransaction == null) {
                    return;
                }
                
                if (Definitions.DELETED.equals(fieldName)) {
                    boolean deleted = Boolean.parseBoolean(value.toString());
                    currentTransaction.setDeleted(deleted);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Transaction field listener cancelled: " + databaseError.getMessage());
            }
        };

        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(transactionFieldDataSnapshot.getRef())) {
            transactionFieldDataSnapshot.getRef().addValueEventListener(event);
            addValueEventListener(transactionFieldDataSnapshot.getRef(), event);
        }
    }

    public void deleteEventListener(DatabaseReference eventDatabaseReference) {
        if (eventDatabaseReference == null) {
            return;
        }
        
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = ChildEventListenerMap.getInstance().getChildEventListenersHM();
        Map<DatabaseReference, ValueEventListener> valueEventListenersHM = ValueEventListenerMap.getInstance().getValueEventListenerHM();
        
        ChildEventListener childEvent = childEventListenersHM.get(eventDatabaseReference);
        if (childEvent != null) {
            eventDatabaseReference.removeEventListener(childEvent);
            childEventListenersHM.remove(eventDatabaseReference);
            return;
        }
        
        ValueEventListener valueEvent = valueEventListenersHM.get(eventDatabaseReference);
        if (valueEvent != null) {
            eventDatabaseReference.removeEventListener(valueEvent);
            valueEventListenersHM.remove(eventDatabaseReference);
        }
    }

    public void deleteEventsListener(DatabaseReference eventDatabaseReference) {
        Map<DatabaseReference, ChildEventListener> childEventListenersHM = ChildEventListenerMap.getInstance().getChildEventListenersHM();
        Map<DatabaseReference, ValueEventListener> valueEventListenersHM = ValueEventListenerMap.getInstance().getValueEventListenerHM();

        Set<DatabaseReference> allEventsPathDBreference = new HashSet<>();
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

    public void writeCategoriesByBudgets(final String refMonth, int budgetNumber, List<Budget> budgets) {
        int idPerMonth = 0;
        for (Budget bgt : budgets) {
            if (isFrqTran(bgt))
                idPerMonth++;
            String catId = getDBCategoriesPath(refMonth).push().getKey();
            if (catId == null) {
                continue;
            }
            Category cat = budgetToCategory(bgt, catId, idPerMonth);
            updateSpecificCategory(refMonth, budgetNumber, cat);
        }
        setIdNumerator(refMonth, idPerMonth);
    }

    public Category budgetToCategory(Budget budget, String catId, int idPerMonth) {
        Category cat = new Category(catId, budget.getCategoryName(), budget.getValue(), budget.getValue());
        Map<String, Transaction> transactions = new HashMap<>();
        if (isFrqTran(budget)) {
            Context ctx = getContext();
            String paymentMethod = ctx != null ? ctx.getString(R.string.credit_card) : "Credit Card";
            Date payDate = DateUtil.getCurrentDate(budget.getChargeDay());
            String yearMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), Config.SEPARATOR);
            String tranId = getDBTransactionsPath(yearMonth, catId).push().getKey();
            if (tranId == null) {
                cat.setTransactions(transactions);
                return cat;
            }
            Transaction transaction = new Transaction(tranId, idPerMonth, budget.getCategoryName(), paymentMethod, budget.getShop(), payDate, budget.getValue());
            transactions.put(tranId, transaction);
            String shop = budget.getShop();
            if (shop != null) {
                shopsSet.add(shop);
            }
            cat.setTransactions(transactions);
            cat.withdrawal(budget.getValue());
        }
        return cat;
    }

    public boolean isAnyBudgetExists() {
        return !budgetDBHM.isEmpty();
    }

    public void createNewMonth(int budgetNumber, String refMonth) {
        List<Budget> budgetsToConvert = getBudgetDataFromDB(budgetNumber);
        writeCategoriesByBudgets(refMonth, budgetNumber, budgetsToConvert);
        Month newMonth = getMonth(refMonth);

        getDBMonthPath(refMonth).setValue(newMonth);
        updateShopsFB();
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

    public DatabaseReference getDBSharesPath() {
        return Config.DatabaseReferenceShares;
    }

    public DatabaseReference getDBUserEmailUidPath() {
        return getDatabase().getReference().child(Definitions.EMAIL_UID);
    }

    public DatabaseReference getDBUsersPath() {
        return Config.DatabaseReferenceUsers;
    }

    public DatabaseReference getDBOwnersPath() {
        return Config.DatabaseReferenceOwners;
    }

    public Map<String, Budget> getBudget(String budgetNumber) {
        if (budgetDBHM.containsKey(budgetNumber))
            return budgetDBHM.get(budgetNumber);
        return null;
    }

    /**
     * Returns the categories for a given month.
     * @param refMonth the reference month key
     * @return Map of categories, or empty map if not found (never null)
     */
    @NonNull
    public Map<String, Category> getCategories(String refMonth) {
        if (refMonth == null) {
            return new HashMap<>();
        }
        Month month = monthDBHM.get(refMonth);
        if (month != null && month.getCategories() != null) {
            return month.getCategories();
        }
        return new HashMap<>();
    }

    /**
     * Returns a deep clone of categories for the given month.
     * @param refMonth the reference month key
     * @return Map of category clones, or null if month not found
     */
    @Nullable
    public Map<String, Category> getCategoriesClone(String refMonth) {
        if (refMonth == null) {
            return null;
        }
        
        Month month = monthDBHM.get(refMonth);
        if (month == null || month.getCategories() == null) {
            return null;
        }
        
        try {
            Map<String, Category> categoriesClone = new HashMap<>();
            for (Category cat : month.getCategories().values()) {
                if (cat != null) {
                    categoriesClone.put(cat.getId(), (Category) cat.clone());
                }
            }
            return categoriesClone;
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "Error cloning categories: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns categories sorted by budget priority.
     * @param refMonth the reference month key
     * @return List of categories sorted by priority, never null
     */
    @NonNull
    public List<Category> getCategoriesByPriority(String refMonth) {
        List<Category> sortedCategories = new ArrayList<>();
        
        if (refMonth == null) {
            return sortedCategories;
        }
        
        Month month = getMonthDBHM().get(refMonth);
        if (month == null) {
            return sortedCategories;
        }
        
        long budgetNumber = month.getBudgetNumber();
        Map<String, Category> categoriesClone = getCategoriesClone(refMonth);
        if (categoriesClone == null || categoriesClone.isEmpty()) {
            return sortedCategories;
        }
        
        List<Budget> sortedBudgets = getBudgetDataFromDB(budgetNumber);

        for (Budget budget : sortedBudgets) {
            if (budget == null || budget.getCategoryName() == null) {
                continue;
            }
            
            Category matchedCategory = null;
            for (Category cat : categoriesClone.values()) {
                if (cat != null && budget.getCategoryName().equals(cat.getName()) 
                        && Double.compare(budget.getValue(), cat.getBudget()) == 0) {
                    matchedCategory = cat;
                    break;
                }
            }
            
            if (matchedCategory != null) {
                sortedCategories.add(matchedCategory);
                categoriesClone.remove(matchedCategory.getId());
            }
        }
        return sortedCategories;
    }

    public List<String> getAllMonthsYearMonth() {
        List<String> monthsList = new ArrayList<>(monthDBHM.keySet());
        monthsList.sort(Collections.reverseOrder());
        return monthsList;
    }

    public List<String> getCategoriesNames(String refMonth) {
        List<String> categoriesNamesList = new ArrayList<>();
        Set<String> categoriesNamesSet = new HashSet<>();

        for (Category cat : getCategoriesByPriority(refMonth)) {
            String categoryName = cat.getName();
            if (categoryName != null && !categoriesNamesSet.contains(categoryName)) {
                categoriesNamesList.add(categoryName);
                categoriesNamesSet.add(categoryName);
            }
        }
        return categoriesNamesList;
    }

    /**
     * Returns transactions for a specific category or all categories.
     * @param refMonth the reference month key
     * @param catId category ID, or null for all categories
     * @return List of transactions, never null (empty list if not found)
     */
    @NonNull
    public List<Transaction> getTransactions(String refMonth, @Nullable String catId) {
        if (catId == null) {
            return getTransactions(refMonth);
        }
        
        Category category = getCategoryById(refMonth, catId);
        if (category == null || category.getTransactions() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(category.getTransactions().values());
    }

    /**
     * Returns all transactions for a given month.
     * @param refMonth the reference month key
     * @return List of all transactions, never null
     */
    @NonNull
    public List<Transaction> getTransactions(String refMonth) {
        Map<String, Category> categoriesHM = getCategories(refMonth);
        List<Transaction> transactions = new ArrayList<>();
        
        for (Category cat : categoriesHM.values()) {
            if (cat != null) {
                transactions.addAll(cat.getTransactions().values());
            }
        }
        return transactions;
    }

    public List<Transaction> getTransactions(String refMonth, String catId, boolean onlyActive) {
        if (onlyActive) {
            List<Transaction> activeTransactions = new ArrayList<>();
            for (Transaction tran : getTransactions(refMonth, catId)) {
                if (!tran.isDeleted()) {
                    activeTransactions.add(tran);
                }
            }
            return activeTransactions;
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

    /**
     * Finds a category by name in a given month.
     * @param refMonth the reference month key
     * @param catName the category name to find
     * @return Category if found, null otherwise
     */
    @Nullable
    public Category getCategoryByName(String refMonth, String catName) {
        if (refMonth == null || catName == null) {
            return null;
        }
        
        Map<String, Category> categoriesHM = getCategories(refMonth);
        for (Category cat : categoriesHM.values()) {
            if (cat != null && catName.equals(cat.getName())) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Finds a category by ID in a given month.
     * @param refMonth the reference month key
     * @param catId the category ID to find
     * @return Category if found, null otherwise
     */
    @Nullable
    public Category getCategoryById(String refMonth, String catId) {
        if (refMonth == null || catId == null) {
            return null;
        }
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

    private void setIdNumerator(String refMonth, int idPerMonth) {
        Month month = monthDBHM.get(refMonth);
        if (month != null) {
            month.setTranIdNumerator(idPerMonth);
        }
    }

    private void setTranIdNumeratorEventUpdateValue(final DataSnapshot tranIdNumeratorDB, final String refMonthKey) {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object valueObj = dataSnapshot.getValue();
                if (valueObj instanceof Long longValue) {
                    Month month = monthDBHM.get(refMonthKey);
                    if (month != null) {
                        month.setTranIdNumerator(longValue.intValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "TranIdNumerator listener cancelled: " + databaseError.getMessage());
            }
        };

        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(tranIdNumeratorDB.getRef())) {
            tranIdNumeratorDB.getRef().addValueEventListener(event);
            addValueEventListener(tranIdNumeratorDB.getRef(), event);
        }
    }

    private void setBudgetNumberEventUpdateValue(final DataSnapshot budgetNumberDB, final String refMonthKey) {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object valueObj = dataSnapshot.getValue();
                if (valueObj instanceof Long longValue) {
                    Month month = monthDBHM.get(refMonthKey);
                    if (month != null) {
                        month.setBudgetNumber(longValue.intValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "BudgetNumber listener cancelled: " + databaseError.getMessage());
            }
        };

        if (!ValueEventListenerMap.getInstance().isEventAlreadyExists(budgetNumberDB.getRef())) {
            budgetNumberDB.getRef().addValueEventListener(event);
            addValueEventListener(budgetNumberDB.getRef(), event);
        }
    }

    public void addNewCategoriesToExistingMonth(String refMonth, int budgetNumber, List<Budget> budgets) {
        if (refMonth == null || budgets == null) {
            Log.w(TAG, "addNewCategoriesToExistingMonth called with null parameters");
            return;
        }
        
        Month month = monthDBHM.get(refMonth);
        if (month == null) {
            Log.w(TAG, "Month not found: " + refMonth);
            return;
        }
        
        int idPerMonth = month.getTranIdNumerator() + 1;
        DatabaseReference categoriesDBReference = getDBCategoriesPath(refMonth);
        
        for (Budget budget : budgets) {
            if (budget == null) {
                continue;
            }
            String catId = categoriesDBReference.push().getKey();
            if (catId == null) {
                continue;
            }
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
                GenericTypeIndicator<Map<String, Budget>> genericTypeIndicator = new GenericTypeIndicator<>() {
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
        Month month = getMonth(refMonth);
        if (month != null) {
            month.setBudgetNumber(budgetNumber);
        } else {
            Log.w(TAG, "Cannot update budget number - month not found: " + refMonth);
        }
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
        if (refMonth == null || tran == null || tran.getCategory() == null) {
            Log.w(TAG, "markDeleteTransaction called with null parameters");
            return;
        }
        
        Category category = getCategoryByName(refMonth, tran.getCategory());
        if (category == null || category.getId() == null) {
            Log.w(TAG, "Category not found for transaction: " + tran.getCategory());
            return;
        }
        
        String catId = category.getId();
        getDBTransactionsPath(refMonth, catId).runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                if (tran.getId() != null) {
                    mutableData.child(tran.getId()).child(Definitions.DELETED).setValue(tran.isDeleted());
                }
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.w(TAG, "markDeleteTransaction failed: " + databaseError.getMessage());
                }
            }
        });
    }

    public void updateCategoryBudgetValue(String refMonth, String catId) {
        if (refMonth == null || catId == null) {
            Log.w(TAG, "updateCategoryBudgetValue called with null parameters");
            return;
        }
        
        Category category = getCategoryById(refMonth, catId);
        if (category == null) {
            Log.w(TAG, "Category not found: " + catId);
            return;
        }
        
        double balance = category.getBudget() - getTotalTransactionsSum(refMonth, catId, true);
        getDBCategoriesPath(refMonth).runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.child(catId).child(Definitions.BALANCE).setValue(balance);
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.w(TAG, "updateCategoryBudgetValue failed: " + databaseError.getMessage());
                }
            }
        });
    }

    public void share(String emailToShare) throws Exception {
        String emailComma = TextUtil.getEmailComma(emailToShare);
        if (isEmailAlreadyShared(emailToShare)) {
            throw new Exception(Definitions.EMAIL_ALREADY_SHARED);
        }
        if (!userMailUidMap.containsKey(emailComma)) {
            Context ctx = getContext();
            String errorMsg = ctx != null ? ctx.getString(R.string.user_not_exists) : "User not exists";
            throw new Exception(errorMsg);
        }
        String guestUid = userMailUidMap.get(emailComma);
        if (guestUid == null) {
            Context ctx = getContext();
            String errorMsg = ctx != null ? ctx.getString(R.string.user_not_exists) : "User not exists";
            throw new Exception(errorMsg);
        }
        Share share = new Share(guestUid, user.getUid(), emailToShare, user.getDbKey(), ShareStatus.PENDING);
        getDBSharesPath().child(guestUid).setValue(share);

        String ownerUid = user.getUid();
        Set<String> guestsUids = ownersMap.getOrDefault(ownerUid, new HashSet<>());
        if (guestsUids.contains(guestUid)) {
            throw new Exception(Definitions.EMAIL_ALREADY_SHARED);
        }
    }

    public boolean isEmailAlreadyShared(String emailToShare) {
        String emailComma = TextUtil.getEmailComma(emailToShare);
        String ownerUid = userMailUidMap.get(emailComma);
        Share share = ownerUid != null ? sharesMap.containsKey(ownerUid) ? sharesMap.get(emailComma) : null : null;
        return share != null && share.getStatus() == ShareStatus.SUCCESSFULLY_SHARED;
    }

    public static void showShareDialogEnterApp(Context context, DataSnapshot snapshot, User user) {
        if (context == null || snapshot == null || user == null || user.getDbKey() == null) {
            Log.w(TAG, "showShareDialogEnterApp called with null parameters");
            return;
        }
        
        Share share = snapshot.child(Definitions.SHARES).child(user.getDbKey()).getValue(Share.class);
        if (share == null || share.getStatus() != ShareStatus.PENDING) {
            DBUtil.getInstance().initDB(user, (Activity) context);
            return;
        }
        
        String ownerDBKey = share.getDbKey();
        if (ownerDBKey == null) {
            DBUtil.getInstance().initDB(user, (Activity) context);
            return;
        }
        
        User ownerUser = snapshot.child(Definitions.USERS).child(ownerDBKey).getValue(User.class);
        String userName = ownerUser != null ? ownerUser.getName() : "Unknown";
        if (userName == null) {
            userName = "Unknown";
        }
        
        String question = String.format(context.getString(R.string.share_budget_question), userName);
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    share.setStatus(ShareStatus.SUCCESSFULLY_SHARED);
                    user.setDbKey(ownerDBKey);
                    
                    String guestUid = share.getGuestUid();
                    if (guestUid != null) {
                        snapshot.child(Definitions.USERS).child(guestUid).child(Definitions.DBKEY).getRef().setValue(ownerDBKey);
                        user.setOwnerUid(share.getOwnerUid());
                        snapshot.child(Definitions.USERS).child(guestUid).getRef().setValue(user);
                    }

                    String ownerUid = share.getOwnerUid();
                    if (ownerUid != null && user.getUid() != null) {
                        Set<String> guestsUids = ownersMap.getOrDefault(ownerUid, new HashSet<>());
                        guestsUids.add(user.getUid());
                        DBUtil.getInstance().getDBOwnersPath().child(ownerUid).setValue(new ArrayList<>(guestsUids));
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    share.setStatus(ShareStatus.DENY);
                    break;
                    
                default:
                    break;
            }
            
            String shareGuestUid = share.getGuestUid();
            if (shareGuestUid != null) {
                snapshot.child(Definitions.SHARES).child(shareGuestUid).getRef().setValue(share);
            }
            DBUtil.getInstance().initDB(user, (Activity) context);
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(question)
                .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener)
                .show();
    }
}

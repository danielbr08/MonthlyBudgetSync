package com.brosh.finance.monthlybudgetsync.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.CreateBudgetViewAdapter;
import com.brosh.finance.monthlybudgetsync.adapters.CreateBudgetViewHolder;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.BudgetInputUtil;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.LocaleHelper;
import com.brosh.finance.monthlybudgetsync.utils.SessionGuardUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.brosh.finance.monthlybudgetsync.utils.ValidationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Activity for creating and managing budget templates.
 * Allows users to define categories, values, and recurring payments.
 */
public class CreateBudgetActivity extends AppCompatActivity {
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }
    
    // ============================================
    // CONSTANTS
    // ============================================
    
    private static final int DIALOG_DISMISS_DELAY_MS = 1000;
    
    // ============================================
    // DATA
    // ============================================
    
    private DBUtil dbUtil;
    @Nullable
    private Month month;

    private List<Budget> allBudgets;
    private List<Budget> budgets;
    private ArrayList<String> allCategories;
    private boolean isInputValid;
    
    // ============================================
    // UI COMPONENTS
    // ============================================

    private CreateBudgetViewAdapter adapter;
    private RecyclerView budgetsRowsRecycler;
    private SwipeRefreshLayout refreshLayout;

    // ============================================
    // LIFECYCLE
    // ============================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);

        initializeData();
        if (isFinishing()) {
            return;
        }
        initializeViews();
        setupRefreshListener();
        setupBudgetGui();
    }

    // ============================================
    // INITIALIZATION
    // ============================================

    /**
     * Initializes data from intent and database.
     */
    private void initializeData() {
        User user = DBUtil.getInstance().getUser();
        if (!SessionGuardUtil.requireUser(this, user)) {
            return;
        }
        setupAdVisibility(user);

        String userKey = user.getDbKey();
        String refMonth = getRefMonthFromIntent();
        
        dbUtil = DBUtil.getInstance();
        month = dbUtil.getMonth(refMonth);
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);

        if (userKey != null) {
            DBUtil.getDatabase().getReference(Definitions.MONTHLY_BUDGET).child(userKey);
        }

        allBudgets = new ArrayList<>();
        allCategories = new ArrayList<>();
    }

    /**
     * Retrieves the reference month from intent extras.
     */
    @Nullable
    private String getRefMonthFromIntent() {
        Bundle extras = getIntent().getExtras();
        return extras != null ? extras.getString(Definitions.MONTH) : null;
    }

    /**
     * Sets up ad visibility based on user settings.
     */
    private void setupAdVisibility(@NonNull User user) {
        if (user.getUserSettings().isAdEnabled()) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            View adView = findViewById(R.id.adView);
            if (adView != null) {
                adView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Initializes view references.
     */
    private void initializeViews() {
        budgetsRowsRecycler = findViewById(R.id.budgets_rows);
        adapter = new CreateBudgetViewAdapter(this, budgets);
    }

    // ============================================
    // REFRESH HANDLING
    // ============================================

    /**
     * Sets up the pull-to-refresh listener with confirmation dialog.
     */
    private void setupRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_create_budget);
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(this::showRefreshConfirmationDialog);
        }
    }

    /**
     * Shows a confirmation dialog before refreshing.
     */
    private void showRefreshConfirmationDialog() {
        TextView titleView = createDialogTitleView();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setCustomTitle(titleView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    setupBudgetGui();
                    refreshLayout.setRefreshing(false);
                })
                .setNegativeButton(getString(R.string.no), (dialog, i) ->
                    refreshLayout.setRefreshing(false))
                .show();
    }

    /**
     * Creates a styled title view for dialogs.
     */
    @NonNull
    private TextView createDialogTitleView() {
        TextView tv = new TextView(this);
        tv.setTextColor(ContextCompat.getColor(this, R.color.colorLoginBackground));
        tv.setText(R.string.are_you_sure_you_want_to_refresh);
        tv.setPadding(40, 40, 40, 0);
        return tv;
    }

    // ============================================
    // BUDGET GUI SETUP
    // ============================================

    /**
     * Sets up the budget GUI with existing or new budget data.
     */
    public void setupBudgetGui() {
        this.budgets = new ArrayList<>(dbUtil.getBudgetDataFromDB(dbUtil.getMaxBudgetNumber()));
        
        if (this.budgets.isEmpty()) {
            addEmptyBudgetRow();
        }
        
        this.adapter = new CreateBudgetViewAdapter(this, budgets);
        
        // Create and attach ItemTouchHelper for drag & swipe
        ItemTouchHelper touchHelper = new ItemTouchHelper(createItemTouchCallback());
        adapter.setItemTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(budgetsRowsRecycler);
        
        budgetsRowsRecycler.setAdapter(adapter);
        budgetsRowsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }
    
    // Kept for backward compatibility
    public void setBudgetGui() {
        setupBudgetGui();
    }

    /**
     * Adds an empty budget row placeholder.
     */
    private void addEmptyBudgetRow() {
        Budget emptyBudget = new Budget("", 0, false, "", 2, budgets.size() + 1);
        budgets.add(emptyBudget);
    }

    /**
     * Creates the ItemTouchHelper callback for swipe-to-delete and drag-to-reorder.
     */
    @NonNull
    private ItemTouchHelper.SimpleCallback createItemTouchCallback() {
        return new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,  // Drag directions
                ItemTouchHelper.START | ItemTouchHelper.END  // Swipe directions
        ) {
            @Override
            public boolean isLongPressDragEnabled() {
                // Disable built-in long press drag - we handle it manually via startDrag()
                return false;
            }
            
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, 
                                       @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getBindingAdapterPosition();
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                // Don't allow swiping the first row
                int swipeFlags = position == 0 ? 0 : ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
            
            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                // Disable SwipeRefreshLayout during drag/swipe to prevent interference
                if (refreshLayout != null) {
                    boolean isDragging = actionState == ItemTouchHelper.ACTION_STATE_DRAG || 
                                        actionState == ItemTouchHelper.ACTION_STATE_SWIPE;
                    refreshLayout.setEnabled(!isDragging);
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                 @NonNull RecyclerView.ViewHolder viewHolder, 
                                 @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                if (fromPosition != RecyclerView.NO_POSITION && toPosition != RecyclerView.NO_POSITION) {
                    return adapter.moveBudget(fromPosition, toPosition);
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    adapter.removeBudget(position);
                }
            }
            
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, 
                                 @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Clear selection after drag/swipe ends
                adapter.clearSelection();
                // Re-enable SwipeRefreshLayout
                if (refreshLayout != null) {
                    refreshLayout.setEnabled(true);
                }
            }
        };
    }

    // ============================================
    // BUDGET COLLECTION
    // ============================================

    /**
     * Collects and validates all budget data from the RecyclerView adapter.
     * Visible rows are read from ViewHolders; off-screen rows use the adapter backing list
     * (synced from visible rows first).
     */
    public void setBudgets() {
        allCategories.clear();
        allBudgets.clear();
        isInputValid = true;

        if (adapter == null) {
            return;
        }

        syncVisibleRowsToAdapter();

        int catPriority = 1;
        List<Budget> backingList = adapter.getBudgets();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            CreateBudgetViewHolder holder = (CreateBudgetViewHolder)
                    budgetsRowsRecycler.findViewHolderForAdapterPosition(i);

            String category;
            int value;
            boolean constPayment;
            String shop;
            int chargeDay;

            if (holder != null) {
                category = holder.getCategoryText();
                value = holder.getParsedValue();
                constPayment = holder.isConstPaymentChecked();
                shop = holder.getShopText();
                chargeDay = holder.getParsedChargeDay();
            } else {
                Budget existing = backingList.get(i);
                category = ValidationUtil.safeTrim(existing.getCategoryName());
                value = existing.getValue();
                constPayment = existing.isConstPayment();
                shop = existing.getShop();
                chargeDay = existing.getChargeDay();
            }

            allCategories.add(category);
            BudgetInputUtil.RowError error = BudgetInputUtil.validateRow(
                    category, value, constPayment, shop, allCategories);
            if (error != BudgetInputUtil.RowError.NONE) {
                isInputValid = false;
                if (holder != null) {
                    applyRowValidationError(holder, error);
                }
                return;
            }

            if (holder != null) {
                allBudgets.add(holder.toBudget(catPriority++));
            } else {
                allBudgets.add(BudgetInputUtil.buildBudget(
                        category, value, constPayment, shop, chargeDay, catPriority++));
            }
        }
        budgets = allBudgets;
    }

    /**
     * Persists edits from currently visible rows into the adapter backing list.
     */
    private void syncVisibleRowsToAdapter() {
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < budgetsRowsRecycler.getChildCount(); i++) {
            View child = budgetsRowsRecycler.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = budgetsRowsRecycler.getChildViewHolder(child);
            if (!(viewHolder instanceof CreateBudgetViewHolder holder)) {
                continue;
            }
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && position < adapter.getBudgets().size()) {
                adapter.getBudgets().set(position, holder.toBudget(position + 1));
            }
        }
    }

    private void applyRowValidationError(@NonNull CreateBudgetViewHolder holder,
                                       @NonNull BudgetInputUtil.RowError error) {
        switch (error) {
            case DUPLICATE_CATEGORY:
                setFieldError(holder.getCategoryEditText(), getString(R.string.duplicate_category));
                break;
            case ILLEGAL_CATEGORY_CHAR:
                setFieldError(holder.getCategoryEditText(), getString(R.string.illegal_Character));
                break;
            case EMPTY_CATEGORY:
                setFieldError(holder.getCategoryEditText(), getString(R.string.please_insert_category));
                break;
            case ZERO_VALUE:
                setFieldError(holder.getValueEditText(), getString(R.string.please_insert_value));
                break;
            case EMPTY_SHOP:
                setFieldError(holder.getShopEditText(), getString(R.string.please_insert_store));
                break;
            case ILLEGAL_SHOP_CHAR:
                setFieldError(holder.getShopEditText(), getString(R.string.please_insert_value));
                break;
            default:
                break;
        }
    }

    // ============================================
    // VALIDATION
    // ============================================

    /**
     * Validates budget input fields.
     */
    public void verifyBudgetInput(EditText categoryET, EditText valueET, 
                                   CheckBox constPaymentCB, EditText shopET) {
        isInputValid = true;
        
        String category = categoryET.getText().toString().trim();
        String valueStr = valueET.getText().toString().trim()
                .replace(Definitions.COMMA, getString(R.string.empty));
        boolean constPayment = constPaymentCB.isChecked();
        String shop = shopET.getText().toString().trim();

        int value = BudgetInputUtil.parseBudgetAmount(valueStr);

        validateCategoryName(categoryET, category);
        validateBudgetValue(valueET, value);
        validateShopField(shopET, constPayment, shop);
    }

    /**
     * Validates the category name field.
     */
    private void validateCategoryName(@NonNull EditText categoryET, @NonNull String category) {
        if (Collections.frequency(allCategories, category) > 1) {
            setFieldError(categoryET, getString(R.string.duplicate_category));
        }
        if (category.contains(TextUtil.getSeparator())) {
            setFieldError(categoryET, getString(R.string.illegal_Character));
        }
        if (category.isEmpty()) {
            setFieldError(categoryET, getString(R.string.please_insert_category));
        }
    }

    /**
     * Validates the budget value field.
     */
    private void validateBudgetValue(@NonNull EditText valueET, int value) {
        if (value == 0) {
            setFieldError(valueET, getString(R.string.please_insert_value));
        }
    }

    /**
     * Validates the shop field for constant payments.
     */
    private void validateShopField(@NonNull EditText shopET, boolean constPayment, 
                                   @NonNull String shop) {
        if (constPayment && shop.isEmpty()) {
            setFieldError(shopET, getString(R.string.please_insert_store));
        }
        if (shop.contains(TextUtil.getSeparator())) {
            setFieldError(shopET, getString(R.string.please_insert_value));
        }
    }

    /**
     * Sets an error on an EditText field.
     */
    private void setFieldError(@NonNull EditText et, @NonNull String errorMessage) {
        et.setError(errorMessage);
        isInputValid = false;
    }

    public void setErrorEditText(EditText et, String errorMessage) {
        setFieldError(et, errorMessage);
    }

    // ============================================
    // BUDGET COMPARISON
    // ============================================

    /**
     * Checks if the original budget content has changed.
     */
    public boolean isOriginBudgetChanged(int budgetNumber) {
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
        return BudgetInputUtil.hasOriginContentChanged(oldBudget, allBudgets);
    }

    /**
     * Checks if there are any changes in the budget.
     */
    public boolean isBudgetChange(int budgetNumber) {
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
        return BudgetInputUtil.hasBudgetChanges(oldBudget, allBudgets);
    }

    /**
     * Gets budgets that were added compared to the original.
     */
    @NonNull
    public ArrayList<Budget> getAddedBudgets(int budgetNumber) {
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
        return new ArrayList<>(BudgetInputUtil.getAddedBudgets(oldBudget, allBudgets));
    }

    // ============================================
    // DIALOGS
    // ============================================

    /**
     * Shows a confirmation dialog for deleting current month.
     */
    public void showQuestionDeleteCurrentMonth(@NonNull String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> 
                    createBudget(Definitions.DELETE_CODE))
                .setNegativeButton(getString(R.string.no), (dialog, which) -> 
                    questionFalseAnswer())
                .show();
    }

    /**
     * Shows a brief message without buttons that auto-dismisses.
     */
    public void showMessageNoButton(@NonNull String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();

        new Handler().postDelayed(dialog::dismiss, DIALOG_DISMISS_DELAY_MS);
    }

    private void questionFalseAnswer() {
        // No action needed
    }

    // ============================================
    // BUDGET CREATION
    // ============================================

    /**
     * Creates or updates the budget based on the operation type.
     */
    private void createBudget(@NonNull String operation) {
        int budgetNumber = dbUtil.getMaxBudgetNumber() + 1;
        ArrayList<Budget> addedBudgets = new ArrayList<>();

        if (Definitions.ADD_CODE.equals(operation)) {
            addedBudgets = getAddedBudgets(budgetNumber - 1);
        } else if (Definitions.DELETE_CODE.equals(operation) && month != null) {
            dbUtil.deleteDataRefMonth(month.getYearMonth());
        }

        String refMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), getString(R.string.separator));
        writeBudget(budgetNumber, allBudgets);
        writeBudgetsToTreeFB(budgetNumber);

        if (Definitions.ADD_CODE.equals(operation)) {
            updateExistingMonth(refMonth, budgetNumber, addedBudgets);
        } else {
            dbUtil.createNewMonth(budgetNumber, refMonth);
        }

        TextUtil.showMessage(getString(R.string.budget_created_successfully), 
                Toast.LENGTH_LONG, getApplicationContext());
        finish();
    }

    /**
     * Updates an existing month with new budget data.
     */
    private void updateExistingMonth(@NonNull String refMonth, int budgetNumber, 
                                     @NonNull ArrayList<Budget> addedBudgets) {
        dbUtil.updateBudgetNumber(refMonth, budgetNumber);
        dbUtil.addNewCategoriesToExistingMonth(refMonth, budgetNumber, addedBudgets);
        dbUtil.updateBudgetNumberFB(refMonth, budgetNumber);
        dbUtil.updateShopsFB();
    }

    /**
     * Writes budgets to the local database.
     */
    public void writeBudget(int budgetNumber, @NonNull List<Budget> budgets) {
        String budgetNumberStr = String.valueOf(budgetNumber);
        for (Budget budget : budgets) {
            String budgetId = dbUtil.getDBBudgetsPath().child(budgetNumberStr).push().getKey();
            if (budgetId == null) {
                continue;
            }
            budget.setId(budgetId);
            dbUtil.updateSpecificBudget(String.valueOf(budgetNumber), budget);
        }
    }

    /**
     * Writes budgets to Firebase.
     */
    private void writeBudgetsToTreeFB(int budgetNumber) {
        String budgetNumberStr = String.valueOf(budgetNumber);
        Map<String, Budget> hmBudgets = dbUtil.getBudget(budgetNumberStr);
        if (hmBudgets != null) {
            dbUtil.getDBBudgetsPath().child(budgetNumberStr).setValue(hmBudgets);
        }
    }

    // ============================================
    // CLICK HANDLERS
    // ============================================

    /**
     * Handles create budget button click.
     */
    public void onCreateBudgetClicked(View view) {
        setBudgets();
        
        if (allBudgets.isEmpty()) {
            showMessageNoButton(getString(R.string.please_insert_budget));
            return;
        }

        int budgetNumber = dbUtil.getMaxBudgetNumber();
        ArrayList<Budget> newBudgets = getAddedBudgets(budgetNumber);
        boolean isOriginContentBudgetChanged = isOriginBudgetChanged(budgetNumber);
        boolean isBudgetChange = isBudgetChange(budgetNumber);
        boolean isAddedBudgetsExists = !newBudgets.isEmpty();

        if (!isInputValid) {
            return;
        }

        if (!isBudgetChange) {
            showMessageNoButton(getString(R.string.no_budget_changes));
            return;
        }

        if (month == null) {
            createBudget(Definitions.CREATE_CODE);
        } else if (isOriginContentBudgetChanged) {
            if (dbUtil.isCurrentRefMonthExists()) {
                showQuestionDeleteCurrentMonth(getString(R.string.create_budget_question));
            } else {
                createBudget(Definitions.DELETE_CODE);
            }
        } else if (isAddedBudgetsExists) {
            createBudget(Definitions.ADD_CODE);
        }
    }

    /**
     * Adds a new input row to the budget list.
     */
    public void addInputRow(View view) {
        if (adapter != null) {
            int currentSize = adapter.getBudgets().size();
            Budget budget = new Budget("", 0, false, "", 2, currentSize + 1);
            adapter.addBudget(budget);
            // Scroll to the new item
            budgetsRowsRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    // For backward compatibility
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = createItemTouchCallback();
}

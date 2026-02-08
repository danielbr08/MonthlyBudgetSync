package com.brosh.finance.monthlybudgetsync.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Activity for creating and managing budget templates.
 * Allows users to define categories, values, and recurring payments.
 */
public class CreateBudgetActivity extends AppCompatActivity {
    
    // ============================================
    // CONSTANTS
    // ============================================
    
    private static final int BUTTON_SIZE_PX = 100;
    private static final int TEXT_SIZE_SP = 12;
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
    // UI DIMENSIONS
    // ============================================
    
    private int screenWidth;
    private int buttonSize;
    private Drawable defaultBackground;
    
    // ============================================
    // UI COMPONENTS
    // ============================================

    private CreateBudgetViewAdapter adapter;
    private RecyclerView budgetsRowsRecycler;
    private LinearLayout mainLayout;
    private LinearLayout budgetsLayout;
    private SwipeRefreshLayout refreshLayout;

    // ============================================
    // LIFECYCLE
    // ============================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);

        initializeData();
        initializeViews();
        initializeScreenDimensions();
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
        mainLayout = findViewById(R.id.LLMainCreateBudget);
        budgetsLayout = new LinearLayout(this);
        budgetsLayout.setOrientation(LinearLayout.VERTICAL);
        budgetsRowsRecycler = findViewById(R.id.budgets_rows);
        defaultBackground = new View(this).getBackground();
        
        adapter = new CreateBudgetViewAdapter(this, budgets);
    }

    /**
     * Initializes screen dimension values.
     */
    private void initializeScreenDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        buttonSize = BUTTON_SIZE_PX;
    }

    // ============================================
    // REFRESH HANDLING
    // ============================================

    /**
     * Sets up the pull-to-refresh listener with confirmation dialog.
     */
    private void setupRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_create_budget);
        refreshLayout.setOnRefreshListener(this::showRefreshConfirmationDialog);
    }

    /**
     * Shows a confirmation dialog before refreshing.
     */
    private void showRefreshConfirmationDialog() {
        TextView titleView = createDialogTitleView();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setCustomTitle(titleView)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.yes), (dialog, id) -> {
                    setupBudgetGui();
                    refreshLayout.setRefreshing(false);
                })
                .setPositiveButton(getString(R.string.no), (dialog, i) -> 
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
        new ItemTouchHelper(createItemTouchCallback()).attachToRecyclerView(budgetsRowsRecycler);
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
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, 
                                       @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getBindingAdapterPosition();
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = position == 0 ? 0 : ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                 @NonNull RecyclerView.ViewHolder viewHolder, 
                                 @NonNull RecyclerView.ViewHolder target) {
                adapter.notifyItemMoved(
                    viewHolder.getBindingAdapterPosition(), 
                    target.getBindingAdapterPosition()
                );
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (budgets.size() < 2) return;
                
                int position = viewHolder.getBindingAdapterPosition();
                budgets.remove(position);
                adapter.notifyItemRemoved(position);
            }
        };
    }

    // ============================================
    // BUDGET COLLECTION
    // ============================================

    /**
     * Collects and validates all budget data from the UI.
     */
    public void setBudgets() {
        allCategories.clear();
        allBudgets.clear();
        int catPriority = 1;

        for (int i = 0; i < budgetsRowsRecycler.getChildCount(); i++) {
            BudgetRowData rowData = extractRowData(i);
            if (rowData == null) continue;

            allCategories.add(rowData.getCategory());
            
            if (validateBudgetRow(rowData)) {
                allBudgets.add(createBudgetFromRow(rowData, catPriority++));
            } else {
                return;
            }
        }
        budgets = allBudgets;
    }

    /**
     * Extracts budget data from a row at the given position.
     */
    @Nullable
    private BudgetRowData extractRowData(int rowIndex) {
        LinearLayout row = (LinearLayout) budgetsRowsRecycler.getChildAt(rowIndex);
        if (row == null) return null;

        int j = 0;
        EditText categoryET = (EditText) row.getChildAt(j++);
        EditText valueET = (EditText) row.getChildAt(j++);
        CheckBox constPaymentCB = (CheckBox) row.getChildAt(j++);
        EditText shopET = (EditText) row.getChildAt(j++);
        TextView chargeDayTV = (TextView) row.getChildAt(j);

        return new BudgetRowData(categoryET, valueET, constPaymentCB, shopET, chargeDayTV);
    }

    /**
     * Validates a budget row and sets error messages if invalid.
     */
    private boolean validateBudgetRow(@NonNull BudgetRowData rowData) {
        verifyBudgetInput(
            rowData.categoryET, 
            rowData.valueET, 
            rowData.constPaymentCB, 
            rowData.shopET
        );
        return isInputValid;
    }

    /**
     * Creates a Budget object from row data.
     */
    @NonNull
    private Budget createBudgetFromRow(@NonNull BudgetRowData rowData, int priority) {
        String category = rowData.getCategory();
        int value = rowData.getValue();
        boolean constPayment = rowData.constPaymentCB.isChecked();
        String shop = constPayment ? rowData.getShop() : null;
        int chargeDay = constPayment ? rowData.getChargeDay() : 1;

        if (!constPayment) {
            rowData.shopET.setText(R.string.empty);
        }

        return new Budget(category, value, constPayment, shop, chargeDay, priority);
    }

    /**
     * Data holder for budget row UI elements.
     */
    private class BudgetRowData {
        final EditText categoryET;
        final EditText valueET;
        final CheckBox constPaymentCB;
        final EditText shopET;
        final TextView chargeDayTV;

        BudgetRowData(EditText categoryET, EditText valueET, CheckBox constPaymentCB, 
                     EditText shopET, TextView chargeDayTV) {
            this.categoryET = categoryET;
            this.valueET = valueET;
            this.constPaymentCB = constPaymentCB;
            this.shopET = shopET;
            this.chargeDayTV = chargeDayTV;
        }

        String getCategory() {
            return categoryET.getText().toString().trim();
        }

        int getValue() {
            String valueStr = valueET.getText().toString().trim()
                    .replace(Definitions.COMMA, "");
            if (valueStr.isEmpty()) return 0;
            return Integer.parseInt(valueStr);
        }

        String getShop() {
            return shopET.getText().toString().trim();
        }

        int getChargeDay() {
            String dayStr = chargeDayTV.getText().toString().trim();
            if (dayStr.isEmpty()) return 1;
            return Integer.parseInt(dayStr);
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

        int value = valueStr.isEmpty() ? 0 : Integer.parseInt(valueStr);

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
        int matchCount = countMatchingBudgets(oldBudget, allBudgets);
        return matchCount != oldBudget.size();
    }

    /**
     * Checks if there are any changes in the budget.
     */
    public boolean isBudgetChange(int budgetNumber) {
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
        
        for (Budget bgt : allBudgets) {
            if (!budgetExistsIn(bgt, oldBudget)) {
                return true;
            }
        }
        return allBudgets.size() != oldBudget.size();
    }

    /**
     * Gets budgets that were added compared to the original.
     */
    @NonNull
    public ArrayList<Budget> getAddedBudgets(int budgetNumber) {
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
        ArrayList<Budget> addedBudgets = new ArrayList<>();

        for (Budget bgt : allBudgets) {
            if (!budgetExistsIn(bgt, oldBudget)) {
                addedBudgets.add(bgt);
            }
        }
        return addedBudgets;
    }

    /**
     * Counts how many budgets from source exist in target.
     */
    private int countMatchingBudgets(@NonNull List<Budget> source, @NonNull List<Budget> target) {
        int count = 0;
        for (Budget oldBgt : source) {
            if (budgetExistsIn(oldBgt, target)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if a budget exists in the given list.
     */
    private boolean budgetExistsIn(@NonNull Budget budget, @NonNull List<Budget> list) {
        for (Budget item : list) {
            if (budget.equals(item)) {
                return true;
            }
        }
        return false;
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
        dbUtil.getDBBudgetsPath().child(budgetNumberStr).setValue(hmBudgets);
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

        if (!isInputValid || !isBudgetChange) {
            return;
        }

        if (month == null) {
            createBudget(Definitions.CREATE_CODE);
        } else if (isOriginContentBudgetChanged) {
            if (dbUtil.isCurrentRefMonthExists()) {
                showQuestionDeleteCurrentMonth(getString(R.string.create_budget_question));
            }
        } else if (isAddedBudgetsExists) {
            createBudget(Definitions.ADD_CODE);
        }
    }

    /**
     * Adds a new input row to the budget list.
     */
    public void addInputRow(View view) {
        Budget budget = new Budget("", 0, false, "", 2, budgets.size() + 1);
        budgets.add(budget);
        adapter.notifyItemInserted(budgets.size() - 1);
    }

    // ============================================
    // LEGACY ROW METHODS (kept for compatibility)
    // ============================================

    @SuppressWarnings("unused")
    public void setAddAndDeleteButton() {
        LinearLayout newll = createButtonRow();
        mainLayout.addView(newll);
    }

    @NonNull
    private LinearLayout createButtonRow() {
        LinearLayout newll = new LinearLayout(this);
        newll.setOrientation(LinearLayout.HORIZONTAL);

        ImageButton addRowButton = createAddRowButton();
        ImageButton deleteRowsButton = createDeleteRowsButton();
        TextView emptyTV = createEmptySpacerView();

        newll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT));
        newll.addView(addRowButton);
        newll.addView(deleteRowsButton);
        newll.addView(emptyTV);
        
        return newll;
    }

    @NonNull
    private ImageButton createAddRowButton() {
        ImageButton button = new ImageButton(this);
        button.setBackground(defaultBackground);
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.add_button_md));
        button.setScaleType(ImageView.ScaleType.FIT_XY);
        button.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        button.setAdjustViewBounds(true);

        button.setOnClickListener(view -> onAddRowClicked());
        return button;
    }

    @NonNull
    private ImageButton createDeleteRowsButton() {
        ImageButton button = new ImageButton(this);
        button.setBackground(defaultBackground);
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.clean_screen));
        button.setScaleType(ImageView.ScaleType.FIT_XY);
        button.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        button.setAdjustViewBounds(true);

        button.setOnClickListener(view -> {
            budgetsLayout.removeAllViews();
            add_New_row(null, 0, false, null, 0);
        });
        return button;
    }

    @NonNull
    private TextView createEmptySpacerView() {
        TextView emptyTV = new TextView(this);
        emptyTV.setLayoutParams(new LinearLayout.LayoutParams(
                screenWidth - 2 * buttonSize, buttonSize));
        return emptyTV;
    }

    private void onAddRowClicked() {
        int budgetSize = budgetsLayout.getChildCount() - 1;
        LinearLayout lastBudgetRow = (LinearLayout) budgetsLayout.getChildAt(budgetSize);
        
        EditText categoryNameET = (EditText) lastBudgetRow.getChildAt(1);
        EditText categoryValueET = (EditText) lastBudgetRow.getChildAt(2);
        
        boolean isLastRowValid = !categoryNameET.getText().toString().trim().isEmpty() 
                && !categoryValueET.getText().toString().trim().isEmpty();
        
        if (isLastRowValid) {
            add_New_row(null, 0, false, null, 0);
        }
    }

    public void add_New_row(@Nullable String categoryName, int categoryValue, 
                            boolean isConstPayment, @Nullable String shop, int chargeDay) {
        LinearLayout newll = new LinearLayout(this);
        EditText categoryNameET = new EditText(this);
        EditText categoryValueET = new EditText(this);
        EditText shopET = new EditText(this);
        Spinner optionalDaysSpinner = new Spinner(this);
        CheckBox constPaymentCB = new CheckBox(this);

        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        List<View> rowViews = Arrays.asList(categoryNameET, categoryValueET, 
                constPaymentCB, shopET, optionalDaysSpinner);
        List<String> viewsText = Arrays.asList(categoryName, String.valueOf(categoryValue), 
                String.valueOf(isConstPayment), shop, String.valueOf(chargeDay - 1));
        List<View> textInputType = Arrays.asList(categoryNameET, constPaymentCB, shopET);
        List<View> numberInputType = Collections.singletonList(categoryValueET);
        
        UiUtil.setViewsText(rowViews, viewsText);
        UiUtil.setTxtSize(rowViews, TEXT_SIZE_SP);
        setViewsInput(textInputType, numberInputType);
        UiUtil.setInputFocus(categoryNameET);
        UiUtil.setDaysInMonthSpinner(optionalDaysSpinner, this);
        UiUtil.setWidthCreateBudgetPageDataWidgets(rowViews, screenWidthReduceButtonSize, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        
        setupConstPaymentListener(constPaymentCB, shopET, optionalDaysSpinner);
        setupSpinnerTouchListener(optionalDaysSpinner, categoryNameET, categoryValueET, shopET);

        newll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);

        ImageButton deleteRowButton = createDeleteSingleRowButton(newll);

        if (!constPaymentCB.isChecked()) {
            shopET.setVisibility(View.INVISIBLE);
            optionalDaysSpinner.setVisibility(View.INVISIBLE);
        }

        newll.addView(deleteRowButton);
        newll.addView(categoryNameET);
        newll.addView(categoryValueET);
        newll.addView(constPaymentCB);
        newll.addView(shopET);
        newll.addView(optionalDaysSpinner);

        budgetsLayout.addView(newll);
    }

    @NonNull
    private ImageButton createDeleteSingleRowButton(@NonNull LinearLayout targetRow) {
        ImageButton button = new ImageButton(this);
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.delete_icon));
        button.setBackground(defaultBackground);
        button.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        button.setAdjustViewBounds(true);
        button.setOnClickListener(view -> budgetsLayout.removeView(targetRow));
        return button;
    }

    private void setupConstPaymentListener(@NonNull CheckBox constPaymentCB, 
                                           @NonNull EditText shopET, 
                                           @NonNull Spinner optionalDaysSpinner) {
        constPaymentCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
            shopET.setVisibility(visibility);
            optionalDaysSpinner.setVisibility(visibility);
        });
    }

    private void setupSpinnerTouchListener(@NonNull Spinner spinner, 
                                           @NonNull EditText... editTexts) {
        spinner.setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) getApplicationContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            for (EditText et : editTexts) {
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
            }
            v.performClick();
            return false;
        });
    }

    private void setViewsInput(@NonNull List<View> textInputType, 
                               @NonNull List<View> numberInputType) {
        for (View view : textInputType) {
            UiUtil.setViewInputTypeText(view);
        }
        for (View view : numberInputType) {
            UiUtil.setViewInputTypeNumber(view);
        }
    }

    // ============================================
    // UNUSED METHODS (kept for compatibility)
    // ============================================

    @SuppressWarnings("unused")
    private List<Category> budgetToCategories(@NonNull List<Budget> budgets) {
        List<Category> categories = new ArrayList<>();
        for (Budget budget : budgets) {
            Category cat = new Category("", budget.getCategoryName(), 
                    budget.getValue(), budget.getValue());
            categories.add(cat);
        }
        return categories;
    }

    // For backward compatibility
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = createItemTouchCallback();
}

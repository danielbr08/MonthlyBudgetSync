package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.SpinnerAdapter;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.adapters.TransactionsViewAdapter;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.ComparatorUtil;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.FormatUtil;
import com.brosh.finance.monthlybudgetsync.utils.LocaleHelper;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity for displaying and managing transactions.
 * Supports filtering, sorting, and swipe-to-delete functionality.
 */
public class TransactionsActivity extends AppCompatActivity {
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }
    
    private static final String EXTRA_CATEGORY_NAME = "categoryName";
    
    // UI components
    private Spinner categoriesSpinner;
    private RecyclerView transactionsRows;
    private TransactionsViewAdapter adapter;
    private CheckBox transactionsActiveFilterCB;
    private LinearLayout noTransMessageLL;
    private TextView totalTransactionsTV;
    private LinearLayout headersLL;
    
    // Data
    private List<Transaction> transactions;
    private Month month;
    private DBUtil dbUtil;
    private User user;
    private String refMonth;
    private List<String> defaultTextTVHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        
        initializeData();
        initializeViews();
        setupAds();
        setupToolbar();
        setupUI(savedInstanceState);
    }
    
    /**
     * Initializes data from intent and database.
     */
    private void initializeData() {
        Bundle extras = getIntent().getExtras();
        refMonth = extras != null ? extras.getString(Definitions.MONTH, null) : null;
        dbUtil = DBUtil.getInstance();
        user = dbUtil.getUser();
        month = dbUtil.getMonth(refMonth);
        transactions = dbUtil.getTransactions(refMonth);
        
        // Initialize default header texts
        defaultTextTVHeaders = Arrays.asList(
                getString(R.string.id),
                getString(R.string.category),
                getString(R.string.store),
                getString(R.string.charge_date),
                getString(R.string.payment_method),
                getString(R.string.price)
        );
    }
    
    /**
     * Initializes view references.
     */
    private void initializeViews() {
        transactionsRows = findViewById(R.id.transactions_rows);
        categoriesSpinner = findViewById(R.id.categorySpinnerTransactions);
        transactionsActiveFilterCB = findViewById(R.id.transactionsFilterCB);
        noTransMessageLL = findViewById(R.id.ll_no_trans_message);
        totalTransactionsTV = findViewById(R.id.tv_total_transactions_top);
        headersLL = findViewById(R.id.headersTV);
    }
    
    /**
     * Sets up advertisement visibility.
     */
    private void setupAds() {
        boolean adEnabled = user != null && user.getUserSettings().isAdEnabled();
        if (adEnabled) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            View adView = findViewById(R.id.adView);
            if (adView != null) {
                adView.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Sets up the toolbar.
     */
    private void setupToolbar() {
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);
    }
    
    /**
     * Sets up UI components and listeners.
     */
    private void setupUI(@Nullable Bundle extras) {
        Bundle intentExtras = getIntent().getExtras();
        String selectedCategory = intentExtras != null ? intentExtras.getString(EXTRA_CATEGORY_NAME, null) : null;
        
        transactionsActiveFilterCB.setChecked(true);
        setActiveTransactionListener();
        
        updateNoTransactionsVisibility();
        init(selectedCategory);
        setOnClickTextViews();
        setupCategorySpinnerListener();
        setupRefreshListener();
    }

    /**
     * Initializes the category spinner with available categories.
     */
    public void init(@Nullable String selectedCategory) {
        if (month == null) return;
        
        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<String> monthCategories = new ArrayList<>(dbUtil.getCategoriesNames(currentRefMonth));
        monthCategories.add(0, getString(R.string.all));
        
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(monthCategories, this, R.layout.custom_spinner);
        categoriesSpinner.setAdapter(spinnerAdapter);
        
        if (selectedCategory != null) {
            int position = monthCategories.indexOf(selectedCategory);
            if (position >= 0) {
                categoriesSpinner.setSelection(position);
            }
        }
    }
    
    /**
     * Sets up the category spinner selection listener.
     */
    private void setupCategorySpinnerListener() {
        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setDefaultHeadersStyleExceptSelected(null);
                String categoryName = categoriesSpinner.getSelectedItem().toString();
                setTransactionsInGui(categoryName, Definitions.SORT_BY_ID, Definitions.ARROW_UP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action needed
            }
        });
    }

    /**
     * Populates the transactions RecyclerView and updates the total.
     */
    public void setTransactionsInGui(String catName, @Nullable Integer sortBy, char ascOrDesc) {
        if (month == null || user == null) return;
        
        String currency = user.getUserSettings().getCurrency();
        boolean isAllCategories = catName.equals(getString(R.string.all));
        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        
        // Get category ID if specific category selected
        String catId = null;
        if (!isAllCategories) {
            Category category = dbUtil.getCategoryByName(currentRefMonth, catName);
            catId = category != null ? category.getId() : null;
        }
        
        // Get transactions
        boolean onlyActive = transactionsActiveFilterCB.isChecked();
        this.transactions = dbUtil.getTransactions(currentRefMonth, catId, onlyActive);
        
        // Update UI based on transaction availability
        if (transactions == null || transactions.isEmpty()) {
            noTransMessageLL.setVisibility(View.VISIBLE);
            updateTotalLabel(currency, 0);
        } else {
            noTransMessageLL.setVisibility(View.GONE);
            
            // Sort transactions
            if (sortBy != null) {
                ComparatorUtil.sort(transactions, sortBy, ascOrDesc);
            }
            
            // Calculate and display total
            double transactionSum = calculateTransactionSum(transactions);
            updateTotalLabel(currency, transactionSum);
        }
        
        // Setup RecyclerView
        adapter = new TransactionsViewAdapter(this, transactions, isAllCategories);
        adapter.setOnTransactionLongClickListener(this::showTransactionDetailsDialog);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(transactionsRows);
        transactionsRows.setAdapter(adapter);
        transactionsRows.setLayoutManager(new LinearLayoutManager(this));
    }
    
    /**
     * Shows a dialog with full transaction details.
     * @param transaction the transaction to display
     */
    private void showTransactionDetailsDialog(@NonNull Transaction transaction) {
        String currency = user != null ? user.getUserSettings().getCurrency() : "";
        float density = getResources().getDisplayMetrics().density;
        int padding = (int) (16 * density);
        
        // Create compact content layout with solid background
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        
        // Create background with solid color and border
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(ContextCompat.getColor(this, R.color.colorApp));
        background.setCornerRadius(8 * density);
        background.setStroke((int)(1 * density), Color.WHITE);
        contentLayout.setBackground(background);
        contentLayout.setPadding(padding, padding, padding, padding);
        
        // Add transaction details
        addDetailRow(contentLayout, getString(R.string.id), String.valueOf(transaction.getIdPerMonth()));
        addDetailRow(contentLayout, getString(R.string.category), transaction.getCategory());
        addDetailRow(contentLayout, getString(R.string.store), transaction.getShop());
        addDetailRow(contentLayout, getString(R.string.charge_date), 
                DateUtil.convertDateToString(transaction.getPayDate(), Config.DATE_FORMAT));
        addDetailRow(contentLayout, getString(R.string.payment_method), transaction.getPaymentMethod());
        addDetailRow(contentLayout, getString(R.string.price), 
                FormatUtil.formatCurrency(transaction.getPrice(), currency));
        
        // Add comment if exists
        String comment = transaction.getComment();
        if (comment != null && !comment.isEmpty()) {
            addCommentRow(contentLayout, getString(R.string.comment), comment, density);
        }
        
        // Add close button
        TextView closeButton = new TextView(this);
        closeButton.setText(getString(R.string.close));
        closeButton.setTextSize(12);
        closeButton.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
        closeButton.setBackgroundResource(R.drawable.btn_close_dialog);
        closeButton.setGravity(android.view.Gravity.CENTER);
        closeButton.setPadding((int)(16*density), (int)(6*density), (int)(16*density), (int)(6*density));
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.gravity = android.view.Gravity.CENTER;
        btnParams.topMargin = (int) (12 * density);
        closeButton.setLayoutParams(btnParams);
        contentLayout.addView(closeButton);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(contentLayout)
                .create();
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
        
        // Remove default dialog padding, keep dim background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.6f);
        }
    }
    
    /**
     * Adds a detail row to the dialog layout.
     */
    private void addDetailRow(LinearLayout parent, String label, String value) {
        TextView textView = new TextView(this);
        textView.setText(label + ": " + (value != null ? value : ""));
        textView.setTextSize(13);
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        int vertPadding = (int) (2 * getResources().getDisplayMetrics().density);
        textView.setPadding(0, vertPadding, 0, vertPadding);
        parent.addView(textView);
    }
    
    /**
     * Adds a comment row with label and multiline value to the dialog layout.
     */
    private void addCommentRow(LinearLayout parent, String label, String value, float density) {
        // Add separator line
        View separator = new View(this);
        separator.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
        LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * density));
        sepParams.topMargin = (int) (8 * density);
        sepParams.bottomMargin = (int) (4 * density);
        separator.setLayoutParams(sepParams);
        parent.addView(separator);
        
        // Add label
        TextView labelView = new TextView(this);
        labelView.setText(label + ":");
        labelView.setTextSize(13);
        labelView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        labelView.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(labelView);
        
        // Add comment value (multiline)
        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(12);
        valueView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        int padding = (int) (4 * density);
        valueView.setPadding(padding, padding, padding, padding);
        parent.addView(valueView);
    }
    
    /**
     * Calculates the sum of transaction prices.
     */
    private double calculateTransactionSum(@NonNull List<Transaction> transactions) {
        double sum = 0;
        for (Transaction tran : transactions) {
            sum += tran.getPrice();
        }
        return FormatUtil.roundToTwoDecimals(sum);
    }
    
    /**
     * Updates the total label with formatted currency value.
     */
    private void updateTotalLabel(String currency, double amount) {
        totalTransactionsTV.setText(FormatUtil.formatCurrency(amount, currency));
    }
    
    /**
     * Updates visibility of the "no transactions" message.
     */
    private void updateNoTransactionsVisibility() {
        int visibility = (transactions == null || transactions.isEmpty()) ? View.VISIBLE : View.GONE;
        noTransMessageLL.setVisibility(visibility);
    }

    /**
     * Sets up click listeners for sortable headers.
     */
    public void setOnClickTextViews() {
        for (int i = 0; i < headersLL.getChildCount(); i++) {
            final int index = i;
            View child = headersLL.getChildAt(index);
            if (child instanceof TextView) {
                child.setOnClickListener(view -> handleHeaderClick((TextView) view));
            }
        }
    }
    
    /**
     * Handles header click for sorting.
     */
    private void handleHeaderClick(TextView headerTV) {
        String allText = headerTV.getText().toString();
        char ascOrDesc = allText.charAt(allText.length() - 1);
        String text;
        
        // Determine current sort state and toggle
        if (ascOrDesc != Definitions.ARROW_UP && ascOrDesc != Definitions.ARROW_DOWN) {
            text = allText;
            ascOrDesc = Definitions.ARROW_UP;
        } else {
            text = allText.substring(0, allText.length() - 1);
            ascOrDesc = (ascOrDesc == Definitions.ARROW_DOWN) ? Definitions.ARROW_UP : Definitions.ARROW_DOWN;
        }
        
        // Update header appearance
        headerTV.setText(getString(R.string.header_with_arrow, text, ascOrDesc));
        headerTV.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        
        setDefaultHeadersStyleExceptSelected(headerTV);
        int sortBy = getSortBy(text);
        setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), sortBy, ascOrDesc);
    }

    /**
     * Returns the sort constant for a given header text.
     */
    public int getSortBy(String header) {
        if (header.equals(getString(R.string.id))) return Definitions.SORT_BY_ID;
        if (header.equals(getString(R.string.category))) return Definitions.SORT_BY_CATEGORY;
        if (header.equals(getString(R.string.payment_method))) return Definitions.SORT_BY_PAYMENT_METHOD;
        if (header.equals(getString(R.string.store))) return Definitions.SORT_BY_STORE;
        if (header.equals(getString(R.string.charge_date))) return Definitions.SORT_BY_CHARGE_DATE;
        if (header.equals(getString(R.string.price))) return Definitions.SORT_BY_PRICE;
        return Definitions.SORT_BY_ID;
    }

    /**
     * Resets header styling except for the selected header.
     */
    public void setDefaultHeadersStyleExceptSelected(@Nullable TextView selectedHeader) {
        for (int i = 0; i < headersLL.getChildCount(); i++) {
            View child = headersLL.getChildAt(i);
            if (child instanceof TextView currentTV && currentTV != selectedHeader) {
                currentTV.setText(defaultTextTVHeaders.get(i));
                currentTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            }
        }
    }

    /**
     * Sets up the pull-to-refresh listener.
     */
    private void setupRefreshListener() {
        SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout_transactions);
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definitions.SORT_BY_ID, Definitions.ARROW_UP);
                refreshLayout.setRefreshing(false);
            });
        }
    }

    /**
     * Sets up the active transactions filter checkbox listener.
     */
    public void setActiveTransactionListener() {
        transactionsActiveFilterCB.setOnCheckedChangeListener((buttonView, isChecked) -> 
            setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definitions.SORT_BY_ID, Definitions.ARROW_UP));
    }

    /**
     * Item touch helper for swipe-to-delete/restore functionality.
     */
    private final ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, 
                              @NonNull RecyclerView.ViewHolder target) {
            adapter.notifyItemMoved(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            handleSwipe(viewHolder.getBindingAdapterPosition(), direction);
        }
    };
    
    /**
     * Handles swipe action on a transaction.
     * Right swipe: delete, Left swipe: restore.
     */
    private void handleSwipe(int position, int direction) {
        if (position < 0 || position >= transactions.size()) return;
        
        Transaction tran = transactions.get(position);
        boolean onlyActive = transactionsActiveFilterCB.isChecked();
        boolean isSpecificCategory = categoriesSpinner.getSelectedItemPosition() > 0;
        
        Category category = dbUtil.getCategoryByName(refMonth, tran.getCategory());
        if (category == null) return;
        
        String catId = category.getId();
        boolean update = false;
        
        // Handle swipe direction
        if (direction == ItemTouchHelper.RIGHT && !tran.isDeleted()) {
            tran.setDeleted(true);
            update = true;
        } else if (direction == ItemTouchHelper.LEFT && tran.isDeleted()) {
            tran.setDeleted(false);
            update = true;
        }
        
        if (update) {
            dbUtil.markDeleteTransaction(refMonth, tran);
            dbUtil.updateCategoryBudgetValue(refMonth, catId);
        }
        
        // Update UI
        transactions.remove(position);
        adapter.notifyItemRemoved(position);
        
        if (!tran.isDeleted() || !onlyActive) {
            transactions.add(position, tran);
            adapter.notifyItemInserted(position);
        }
        
        if (update) {
            updateTotalAfterSwipe(isSpecificCategory ? catId : null, onlyActive);
        }
    }
    
    /**
     * Updates the total label after a swipe action.
     */
    private void updateTotalAfterSwipe(@Nullable String catId, boolean onlyActive) {
        if (user == null) return;
        
        String currency = user.getUserSettings().getCurrency();
        double sum = catId != null 
            ? dbUtil.getTransactionsSum(refMonth, catId, onlyActive)
            : dbUtil.getTransactionsSum(refMonth, onlyActive);
        
        updateTotalLabel(currency, FormatUtil.roundToTwoDecimals(sum));
    }
}

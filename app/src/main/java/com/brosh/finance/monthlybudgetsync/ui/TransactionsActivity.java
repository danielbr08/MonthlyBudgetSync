package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.adapters.TransactionsViewAdapter;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.ComparatorUtil;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {
    private Spinner categoriesSpinner;
    private List<Transaction> transactions;
    private RecyclerView transactionsRows;
    private TransactionsViewAdapter adapter;

    private Month month;
    private DBUtil dbUtil;
    private User user;
    private String refMonth;

    private SwipeRefreshLayout refreshLayout;
    private CheckBox transactionsActiveFilterCB;
    private List<String> defaultTextTVHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        // Initialize default header texts
        defaultTextTVHeaders = Arrays.asList(
                getString(R.string.id),
                getString(R.string.category),
                getString(R.string.store),
                getString(R.string.charge_date),
                getString(R.string.payment_method),
                getString(R.string.price)
        );

        transactionsRows = findViewById(R.id.transactions_rows);
        adapter = null;
        Bundle extras = getIntent().getExtras();
        refMonth = extras != null ? extras.getString(Definitions.MONTH, null) : null;
        dbUtil = DBUtil.getInstance();
        user = dbUtil.getUser();
        
        boolean adEnabled = user != null && user.getUserSettings().isAdEnabled();
        if (adEnabled) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        
        month = dbUtil.getMonth(refMonth);
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);
        String selectedCategory = extras != null && extras.containsKey("categoryName") ? extras.getString("categoryName") : null;

        transactionsActiveFilterCB = findViewById(R.id.transactionsFilterCB);
        transactionsActiveFilterCB.setChecked(true);
        setActiveTransactionListener();
        this.transactions = dbUtil.getTransactions(refMonth);
        LinearLayout llNoTransMessage = findViewById(R.id.ll_no_trans_message);
        int noTransMessageVisibility = (this.transactions == null || this.transactions.isEmpty()) ? View.VISIBLE : View.GONE;
        llNoTransMessage.setVisibility(noTransMessageVisibility);
        categoriesSpinner = findViewById(R.id.categorySpinnerTransactions);
        init(selectedCategory);
        setOnClickTextViews();
        //setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definitions.SORT_BY_ID, Definitions.ARROW_UP);

        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setDefaultHeadersStyleExeptSelected(null);
                String categoryName = categoriesSpinner.getSelectedItem().toString();
                setTransactionsInGui(categoryName, Definitions.SORT_BY_ID, Definitions.ARROW_UP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action needed
            }
        });
        setRefreshListener();
    }

    public void init(String selectedCategory) {
        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<String> monthCategories = new ArrayList<>(dbUtil.getCategoriesNames(currentRefMonth));
        monthCategories.add(0, getString(R.string.all));
        SpinnerAdapter adapter = new SpinnerAdapter(monthCategories, this, R.layout.custom_spinner);
        categoriesSpinner.setAdapter(adapter);
        if (selectedCategory != null)
            categoriesSpinner.setSelection(monthCategories.indexOf(selectedCategory));
    }

    public void setTransactionsInGui(String catName, Integer sortBy, char ascOrDesc) {
        ((TextView) findViewById(R.id.tv_total_transactions_top)).setText(String.format("%s %s", user.getUserSettings().getCurrency(), getString(R.string.zero)));
        boolean isIncludeCategory = catName.equals(getString(R.string.all));

        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        String catId = isIncludeCategory ? null : dbUtil.getCategoryByName(currentRefMonth, catName).getId();
        boolean onlyActive = transactionsActiveFilterCB.isChecked();
        this.transactions = dbUtil.getTransactions(currentRefMonth, catId, onlyActive);
        LinearLayout noTranMessageLL = findViewById(R.id.ll_no_trans_message);
        if (transactions == null || transactions.isEmpty()) {
            noTranMessageLL.setVisibility(View.VISIBLE);
        } else {
            if (sortBy != null)
                ComparatorUtil.sort(transactions, sortBy, ascOrDesc);
            double tranSum = 0;
            for (Transaction tran : transactions) {
                tranSum += tran.getPrice();
            }
            tranSum = Math.round(tranSum * 100.d) / 100.0d;
            DecimalFormat decim = new DecimalFormat("#,###.##");
            ((TextView) findViewById(R.id.tv_total_transactions_top)).setText(String.format("%s %s", user.getUserSettings().getCurrency(), decim.format(tranSum)));
            noTranMessageLL.setVisibility(View.GONE);
        }
        transactionsRows = findViewById(R.id.transactions_rows);
        adapter = new TransactionsViewAdapter(this, transactions, isIncludeCategory);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(transactionsRows);
        transactionsRows.setAdapter(adapter);
        transactionsRows.setLayoutManager(new LinearLayoutManager(this));
    }

    public void setOnClickTextViews() {
        final LinearLayout rowLL = findViewById(R.id.headersTV);
        for (int i = 0; i < rowLL.getChildCount(); i++) {
            final int j = i;
            ((TextView) rowLL.getChildAt(j)).setOnClickListener(view -> {
                TextView headerTV = ((TextView) rowLL.getChildAt(j));
                String allText = headerTV.getText().toString();
                char ascOrDesc = allText.charAt(allText.length() - 1);
                String text;
                int sortBY;
                if (ascOrDesc != Definitions.ARROW_UP && ascOrDesc != Definitions.ARROW_DOWN) {
                    ascOrDesc = 'X';
                    text = allText;
                } else {
                    text = allText.substring(0, allText.length() - 1);
                }

                switch (ascOrDesc) {
                    case ('ꜜ'): {
                        ascOrDesc = Definitions.ARROW_UP;
                        headerTV.setText(getString(R.string.header_with_arrow, text, Definitions.ARROW_UP));
                        headerTV.setTextColor(Color.RED);
                        break;
                    }
                    case ('ꜛ'): {
                        ascOrDesc = Definitions.ARROW_DOWN;
                        headerTV.setText(getString(R.string.header_with_arrow, text, Definitions.ARROW_DOWN));
                        headerTV.setTextColor(Color.RED);
                        break;
                    }
                    default: {
                        headerTV.setText(getString(R.string.header_with_arrow, text, Definitions.ARROW_UP));
                        headerTV.setTextColor(Color.RED);
                        ascOrDesc = Definitions.ARROW_UP;
                        break;
                    }
                }
                setDefaultHeadersStyleExeptSelected(headerTV);
                sortBY = getSortBy(text);
                setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), sortBY, ascOrDesc);
            });
        }
    }

    public int getSortBy(String header) {
        if (header.equals(getString(R.string.id)))
            return Definitions.SORT_BY_ID;
        else if (header.equals(getString(R.string.category)))
            return Definitions.SORT_BY_CATEGORY;
        if (header.equals(getString(R.string.payment_method)))
            return Definitions.SORT_BY_PAYMENT_METHOD;
        else if (header.equals(getString(R.string.store)))
            return Definitions.SORT_BY_STORE;
        if (header.equals(getString(R.string.charge_date)))
            return Definitions.SORT_BY_CHARGE_DATE;
        else if (header.equals(getString(R.string.price)))
            return Definitions.SORT_BY_PRICE;
        else
            return Definitions.SORT_BY_ID;
    }

    public void setDefaultHeadersStyleExeptSelected(TextView headerTV) {
        LinearLayout headersTV = findViewById(R.id.headersTV);
        for (int i = 0; i < headersTV.getChildCount(); i++) {
            TextView currentTV = (TextView) headersTV.getChildAt(i);
            if (currentTV != headerTV) {
                currentTV.setText(defaultTextTVHeaders.get(i));
                currentTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            }
        }
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_transactions);
        refreshLayout.setOnRefreshListener(() -> {
            setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definitions.SORT_BY_ID, Definitions.ARROW_UP);
            refreshLayout.setRefreshing(false);
        });
    }

    public void setActiveTransactionListener() {
        transactionsActiveFilterCB.setOnCheckedChangeListener((buttonView, isChecked) -> 
            setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definitions.SORT_BY_ID, Definitions.ARROW_UP));
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            adapter.notifyItemMoved(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            Transaction tran = transactions.get(position);
            boolean update = false;
            boolean onlyActive = ((CheckBox) findViewById(R.id.transactionsFilterCB)).isChecked();
            boolean isSpecificCategory = categoriesSpinner.getSelectedItemPosition() > 0;

            String catId = DBUtil.getInstance().getCategoryByName(refMonth, tran.getCategory()).getId();
            if (ItemTouchHelper.RIGHT == direction) {
                if (!tran.isDeleted()) {
                    tran.setDeleted(true);
                    update = true;
                }
            } else if (ItemTouchHelper.LEFT == direction) {
                if (tran.isDeleted()) {
                    tran.setDeleted(false);
                    update = true;
                }
            }
            if (update) {
                DBUtil.getInstance().markDeleteTransaction(refMonth, tran);
                DBUtil.getInstance().updateCategoryBudgetValue(refMonth, catId);
            }
            transactions.remove(position);
            adapter.notifyItemRemoved(position);
            if (!tran.isDeleted() || !onlyActive) {
                transactions.add(position, tran);
                adapter.notifyItemInserted(position);
            }
            if (update) {
                catId = isSpecificCategory ? catId : null;
                updateTotalLabel(catId, onlyActive);
            }
        }
    };

    private void updateTotalLabel(String catId, boolean onlyActive) {
        if (catId == null) {
            updateTotalLabel(onlyActive);
            return;
        }
        double activeTransactionsSum = dbUtil.getTransactionsSum(refMonth, catId, onlyActive);
        activeTransactionsSum = Math.round(activeTransactionsSum * 100.d) / 100.0d;
        DecimalFormat decim = new DecimalFormat("#,###.##");
        ((TextView) findViewById(R.id.tv_total_transactions_top)).setText(String.format("%s %s", user.getUserSettings().getCurrency(), decim.format(activeTransactionsSum)));
    }

    private void updateTotalLabel(boolean onlyActive) {
        double activeTransactionsSum = dbUtil.getTransactionsSum(refMonth, onlyActive);
        activeTransactionsSum = Math.round(activeTransactionsSum * 100.d) / 100.0d;
        DecimalFormat decim = new DecimalFormat("#,###.##");
        ((TextView) findViewById(R.id.tv_total_transactions_top)).setText(String.format("%s %s", user.getUserSettings().getCurrency(), decim.format(activeTransactionsSum)));
    }
}

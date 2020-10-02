package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private static final String TAG = "TransactionsActivity";

    private Spinner categoriesSpinner;
    private List<Transaction> transactions;
    private List<String> defaultTextTVHeaders;
    private RecyclerView transactions_rows;
    private TransactionsViewAdapter adapter;

    private Month month;
    private DBUtil dbUtil;
    private User user;
    private String refMonth;

    private SwipeRefreshLayout refreshLayout;
    private CheckBox transactionsActiveFilterCB;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        transactions_rows = findViewById(R.id.transactions_rows);
        adapter = null;
        Bundle extras = getIntent().getExtras();
        refMonth = extras.getString(Definitions.MONTH, null);
        user = (User) getIntent().getExtras().getSerializable(Definitions.USER);
        if (user.getUserSettings().isAdEnabled()) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        dbUtil = DBUtil.getInstance();
        month = dbUtil.getMonth(refMonth);
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);
        String selectedCategory = extras.containsKey("categoryName") ? extras.getString("categoryName") : null;

        transactionsActiveFilterCB = findViewById(R.id.transactionsFilterCB);
        transactionsActiveFilterCB.setChecked(true); // todo this by user choice
        setActiveTransactionListener();
        this.transactions = dbUtil.getTransactions(refMonth);
        LinearLayout llNoTransMessage = findViewById(R.id.ll_no_trans_message);
        int noTransMessageVisibility = (this.transactions == null || this.transactions.size() == 0) ? View.VISIBLE : View.GONE;
        llNoTransMessage.setVisibility(noTransMessageVisibility);

        this.defaultTextTVHeaders = Arrays.asList(getString(R.string.id), getString(R.string.category), getString(R.string.store), getString(R.string.charge_date), getString(R.string.payment_method), getString(R.string.price));
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//Rotate the screen to to be on landspace moade only
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
            }
        });
        setRefreshListener();
    }

    public void init(String selectedCategory) {
        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<String> monthCategories = new ArrayList<String>(dbUtil.getCategoriesNames(currentRefMonth));
        monthCategories.add(0, getString(R.string.all));
        SpinnerAdapter adapter = new SpinnerAdapter(monthCategories, this, R.layout.custom_spinner);
        categoriesSpinner.setAdapter(adapter);
        if (selectedCategory != null)
            categoriesSpinner.setSelection(monthCategories.indexOf(selectedCategory));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTransactionsInGui(String catName, Integer sortBy, char ascOrDesc) {
        ((TextView) findViewById(R.id.tv_total_transactions_top)).setText(String.format("%s %s", user.getUserSettings().getCurrency(), getString(R.string.zero)));
        Boolean isIncludeCategory = false;
        if (catName.equals(getString(R.string.all)))
            isIncludeCategory = true;

        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        String catId = isIncludeCategory ? null : dbUtil.getCategoryByName(currentRefMonth, catName).getId();
        boolean onlyActive = transactionsActiveFilterCB.isChecked();
        this.transactions = dbUtil.getTransactions(currentRefMonth, catId, onlyActive);
        LinearLayout noTranMessageLL = findViewById(R.id.ll_no_trans_message);
        if (transactions == null || transactions.size() == 0) {
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
        transactions_rows = findViewById(R.id.transactions_rows);
        adapter = new TransactionsViewAdapter(this, transactions, isIncludeCategory);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(transactions_rows);
        transactions_rows.setAdapter(adapter);
        transactions_rows.setLayoutManager(new LinearLayoutManager(this));
    }

    public void setOnClickTextViews() {
        final LinearLayout rowLL = findViewById(R.id.headersTV);
        for (int i = 0; i < rowLL.getChildCount(); i++) {
            final int j = i;
            ((TextView) rowLL.getChildAt(j)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onClick(View view) {
                    TextView headerTV = ((TextView) rowLL.getChildAt(j));
                    String allText = headerTV.getText().toString();
                    char ascOrDesc = allText.charAt(allText.length() - 1);
                    String text = "";
                    int sortBY;
                    if (ascOrDesc != Definitions.ARROW_UP && ascOrDesc != Definitions.ARROW_DOWN) {
                        ascOrDesc = 'X';
                        text = allText;
                    } else
                        text = new String(allText.substring(0, allText.length() - 1));

//                    setTextViewsHeader();
                    switch (ascOrDesc) {
                        case ('ꜜ'): {
                            ascOrDesc = Definitions.ARROW_UP;
                            headerTV.setText(text + Definitions.ARROW_UP);
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        case ('ꜛ'): {
                            ascOrDesc = Definitions.ARROW_DOWN;
                            headerTV.setText(text + Definitions.ARROW_DOWN);
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        default: {
                            headerTV.setText(text + Definitions.ARROW_UP);
                            headerTV.setTextColor(Color.RED);
                            ascOrDesc = Definitions.ARROW_UP;
                            break;
                        }
                    }
                    setDefaultHeadersStyleExeptSelected(headerTV);
                    sortBY = getSortBy(text);
                    setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), sortBY, ascOrDesc);
                }
            });
        }
    }

    public int getSortBy(String header) {
        if (header.equals(getString(R.string.id)))
            return Definitions.SORT_BY_ID;
        else if (header.equals(getString(R.string.category)))
            return Definitions.SORT_BY_CATEGORY;
        if (header.equals(getString(R.string.payment_method)))
            return Definitions.SORT_BY_PAYMRNT_METHOD;
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
                currentTV.setTextColor(getResources().getColor(R.color.colorWhite));
            }
        }
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_transactions);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onRefresh() {
                setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definitions.SORT_BY_ID, Definitions.ARROW_UP);
                refreshLayout.setRefreshing(false);
            }
        });
    }

    public void setActiveTransactionListener() {
        transactionsActiveFilterCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definitions.SORT_BY_ID, Definitions.ARROW_UP); // todo only reload recycler. listener are not needed(also for delete tran event)
            }
        });
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
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
            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
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

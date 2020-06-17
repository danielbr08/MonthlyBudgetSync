package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.brosh.finance.monthlybudgetsync.adapters.TransactionViewHolder;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.adapters.TransactionsViewAdapter;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.services.ComparatorService;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.services.UIService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    private Spinner categoriesSpinner;
    private List<TextView[]> textViews = new ArrayList<TextView[]>();

    private int widthDisplay;
    private List<Transaction> transactions;
    private List<String> defaultTextTVHeaders;
    private RecyclerView transactions_rows;
    private TransactionsViewAdapter adapter;

    //todo get thos fields from caller intent
    private Month month;
    private DBService dbService;
    private String userKey;
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
        refMonth = extras.getString(Definition.MONTH, null);
        user = (User) getIntent().getExtras().getSerializable(Definition.USER);
        if (user.getUserConfig().isAdEnabled()) {
            UIService.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        userKey = user.getDbKey();
        dbService = DBService.getInstance();
        month = dbService.getMonth(refMonth);
        setToolbar();
        String selectedCategory = extras.containsKey("categoryName") ? extras.getString("categoryName") : null;

        transactionsActiveFilterCB = findViewById(R.id.transactionsFilterCB);
        transactionsActiveFilterCB.setChecked(true); // todo this by user choice
        setActiveTransactionListener();
        this.transactions = dbService.getTransactions(refMonth);
        LinearLayout llNoTransMessage = findViewById(R.id.ll_no_trans_message);
        int noTransMessageVisibility = (this.transactions == null || this.transactions.size() == 0) ? View.VISIBLE : View.GONE;
        llNoTransMessage.setVisibility(noTransMessageVisibility);

        this.defaultTextTVHeaders = Arrays.asList(getString(R.string.id), getString(R.string.category), getString(R.string.store), getString(R.string.charge_date), getString(R.string.payment_method), getString(R.string.price));
//        setTitle( getYearMonth(month.getMonth(),'.'));
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//Rotate the screen to to be on landspace moade only
        categoriesSpinner = (Spinner) findViewById(R.id.categorySpinnerTransactions);
        init(selectedCategory);
        setOnClickTextViews();
        setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definition.SORT_BY_ID, Definition.ARROW_UP);

        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setDefaultHeadersStyleExeptSelected(null);
                String categoryName = categoriesSpinner.getSelectedItem().toString();
                setTransactionsInGui(categoryName, Definition.SORT_BY_ID, Definition.ARROW_UP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        setRefreshListener();
    }

    public void init(String selectedCategory) {
        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<String> monthCategories = new ArrayList<String>(dbService.getCategoriesNames(currentRefMonth));
        monthCategories.add(0, getString(R.string.all));
        SpinnerAdapter adapter = new SpinnerAdapter(monthCategories, this);
        categoriesSpinner.setAdapter(adapter);
        if (selectedCategory != null)
            categoriesSpinner.setSelection(monthCategories.indexOf(selectedCategory));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTransactionsInGui(String catName, Integer sortBy, char ascOrDesc) {
        ((TextView) findViewById(R.id.tv_total_transactions_top)).setText(getString(R.string.zero));
        Boolean isIncludeCategory = false;
        if (catName.equals(getString(R.string.all)))
            isIncludeCategory = true;

        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        String catId = isIncludeCategory ? null : dbService.getCategoryByName(currentRefMonth, catName).getId();
        boolean onlyActive = transactionsActiveFilterCB.isChecked();
        this.transactions = dbService.getTransactions(currentRefMonth, catId, onlyActive);
        LinearLayout noTranMessageLL = findViewById(R.id.ll_no_trans_message);
        if (transactions == null || transactions.size() == 0) {
            noTranMessageLL.setVisibility(View.VISIBLE);
        } else {
            if (sortBy != null)
                ComparatorService.sort(transactions, sortBy, ascOrDesc);
            double tranSum = 0;
            for (Transaction tran : transactions) {
                tranSum += tran.getPrice();
            }
            tranSum = Math.round(tranSum * 100.d) / 100.0d;
            DecimalFormat decim = new DecimalFormat("#,###.##");
            ((TextView) findViewById(R.id.tv_total_transactions_top)).setText(decim.format(tranSum));
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
                    int sortBY = Definition.SORT_BY_ID;
                    if (ascOrDesc != Definition.ARROW_UP && ascOrDesc != Definition.ARROW_DOWN) {
                        ascOrDesc = 'X';
                        text = allText;
                    } else
                        text = new String(allText.substring(0, allText.length() - 1));

//                    setTextViewsHeader();
                    switch (ascOrDesc) {
                        case ('ꜜ'): {
                            ascOrDesc = Definition.ARROW_UP;
                            headerTV.setText(text + Definition.ARROW_UP);
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        case ('ꜛ'): {
                            ascOrDesc = Definition.ARROW_DOWN;
                            headerTV.setText(text + Definition.ARROW_DOWN);
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        default: {
                            headerTV.setText(text + Definition.ARROW_UP);
                            headerTV.setTextColor(Color.RED);
                            ascOrDesc = Definition.ARROW_UP;
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
            return Definition.SORT_BY_ID;
        else if (header.equals(getString(R.string.category)))
            return Definition.SORT_BY_CATEGORY;
        if (header.equals(getString(R.string.payment_method)))
            return Definition.SORT_BY_PAYMRNT_METHOD;
        else if (header.equals(getString(R.string.store)))
            return Definition.SORT_BY_STORE;
        if (header.equals(getString(R.string.charge_date)))
            return Definition.SORT_BY_CHARGE_DATE;
        else if (header.equals(getString(R.string.price)))
            return Definition.SORT_BY_PRICE;
        else
            return Definition.SORT_BY_ID;
    }

    public void setDefaultHeadersStyleExeptSelected(TextView headerTV) {
        LinearLayout headersTV = findViewById(R.id.headersTV);
        for (int i = 0; i < headersTV.getChildCount(); i++) {
            TextView currentTV = (TextView) headersTV.getChildAt(i);
            if (currentTV != headerTV) {
                currentTV.setText(defaultTextTVHeaders.get(i));
                currentTV.setTextColor(Color.BLACK);
            }
        }
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_transactions);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onRefresh() {
                setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definition.SORT_BY_ID, Definition.ARROW_UP);
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void setTitleText() {
        String title = getString(R.string.app_name);
        title += month != null ? "\n" + month.getYearMonth() : "";
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);
    }

    private void setToolbar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        setTitleText();
    }

    public void setActiveTransactionListener() {
        transactionsActiveFilterCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), Definition.SORT_BY_ID, Definition.ARROW_UP);
            }
        });
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Transaction tran = transactions.get(position);
            if (ItemTouchHelper.START == direction) {
                if (tran.isDeleted()) {
                    return;
                }
                tran.setDeleted(true);
            } else if (ItemTouchHelper.END == direction) {
                if (!tran.isDeleted()) {
                    return;
                }
                tran.setDeleted(false);
            }
//            String catId = DBService.getInstance().getCategoryByName(refMonth, tran.getCategory()).getId(); // todo uncomment
//            DBService.getInstance().markDeleteTransaction(refMonth, tran);
//            DBService.getInstance().updateCategoryBudgetValue(refMonth, catId);
            if (ItemTouchHelper.START == direction) {
                transactions.remove(position);
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            } else if (ItemTouchHelper.END == direction) {
                ((TransactionViewHolder)viewHolder).setStrikeThroughText(false);
//                adapter.notifyDataSetChanged();
                // todo UIService.strikeThroughText(Arrays.asList(this.id, this.catName, this.paymentMethod, this.store, this.chargeDate, this.price));
            }
        }
    };
}

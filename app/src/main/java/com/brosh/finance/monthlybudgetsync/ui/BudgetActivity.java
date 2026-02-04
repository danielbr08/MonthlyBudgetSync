package com.brosh.finance.monthlybudgetsync.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.adapters.CategoriesViewAdapter;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;


import java.text.DecimalFormat;
import java.util.List;

public class BudgetActivity extends AppCompatActivity {
    private Month month;
    private DBUtil dbUtil;
    private User user;

    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        Bundle extras = getIntent().getExtras();
        String refMonth = extras != null ? extras.getString(Definitions.MONTH, null) : null;
        dbUtil = DBUtil.getInstance();
        user = dbUtil.getUser();
        
        boolean adEnabled = user != null && user.getUserSettings().isAdEnabled();
        if (adEnabled) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        
        month = refMonth != null ? dbUtil.getMonth(refMonth) : null;
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);

        if (month != null) {
            setCategoriesInGui();
        }
        setRefreshListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setCategoriesInGui() {
        if (month == null || user == null) {
            return;
        }
        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<Category> categories = dbUtil.getCategoriesByPriority(currentRefMonth);

        int totalBudget = 0;
        double totalBalance = 0;

        for (Category category : categories) {
            Double balance = Math.round(category.getBalance() * 100.d) / 100.0d;
            int budget = category.getBudget();
            totalBudget += budget;
            totalBalance += balance;
        }
        totalBalance = Math.round(totalBalance * 100.d) / 100.0d;
        DecimalFormat decim = new DecimalFormat("#,###.##");
        ((TextView) findViewById(R.id.totalBalance)).setText(String.format("%s %s", user.getUserSettings().getCurrency(), decim.format(totalBalance)));
        ((TextView) findViewById(R.id.totalBudget)).setText(String.format("%s %s", user.getUserSettings().getCurrency(), decim.format(totalBudget)));
        CategoriesViewAdapter adapter = new CategoriesViewAdapter(this, categories);
        RecyclerView categories_rows = findViewById(R.id.categories_rows);
        categories_rows.setAdapter(adapter);
        categories_rows.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_budgets);
        refreshLayout.setOnRefreshListener(() -> {
            setCategoriesInGui();
            refreshLayout.setRefreshing(false);
        });
    }

    public Month getMonth() {
        return month;
    }

    public User getUser() {
        return user;
    }
}

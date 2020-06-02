package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.RequiresApi;
//import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.adapters.CategoriesViewAdapter;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.services.UIService;


import java.text.DecimalFormat;
import java.util.List;

public class BudgetActivity extends AppCompatActivity {


    private LinearLayout ll;
    //todo get thos fields from caller intent
    private Month month;
    private DBService dbService;
    private String userKey;
    private User user;

    private SwipeRefreshLayout refreshLayout;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        Bundle extras = getIntent().getExtras();
        String refMonth = extras.getString(Definition.MONTH, null);
        user = (User) getIntent().getExtras().getSerializable(Definition.USER);
        if (user.getUsserConfig().isAdEnabled()) {
            UIService.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        userKey = user.getDbKey();
        dbService = DBService.getInstance();
        month = dbService.getMonth(refMonth);
        setToolbar();

        ll = (LinearLayout) findViewById(R.id.LLBudget);
        setCategoriesInGui();
        setRefreshListener();

        //setCloseButton();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addCategoryRow(String categoryName, String Budget, String balance, boolean isExceptionFromBudget)//Bundle savedInstanceState)
    {
        TextView categoryNameTextView = new TextView(BudgetActivity.this);
        TextView budgetTextView = new TextView(BudgetActivity.this);
        TextView balanceTextView = new TextView(BudgetActivity.this);

        LinearLayout newll = new LinearLayout(BudgetActivity.this);

        categoryNameTextView.setText(categoryName);
        balanceTextView.setText(balance);
        budgetTextView.setText(Budget);

        categoryNameTextView.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        budgetTextView.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        balanceTextView.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);

        categoryNameTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        budgetTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        if (categoryName == getString(R.string.total)) {
            categoryNameTextView.setTypeface(null, Typeface.BOLD);
            categoryNameTextView.setTextSize(13);
            categoryNameTextView.setTextColor(Color.BLACK);
            budgetTextView.setTypeface(null, Typeface.BOLD);
            budgetTextView.setTextSize(13);
            budgetTextView.setTextColor(Color.BLACK);
            balanceTextView.setTypeface(null, Typeface.BOLD);
            balanceTextView.setTextSize(13);
            balanceTextView.setTextColor(Color.BLACK);
        }

        if (isExceptionFromBudget == true) {
            categoryNameTextView.setTextColor(Color.RED);
            budgetTextView.setTextColor(Color.RED);
            balanceTextView.setTextColor(Color.RED);
        }

        Display display = getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();
        categoryNameTextView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 4, ViewGroup.LayoutParams.WRAP_CONTENT));
        budgetTextView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 4, ViewGroup.LayoutParams.WRAP_CONTENT));
        balanceTextView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 4, ViewGroup.LayoutParams.WRAP_CONTENT));
        //categoryValueEditText.setTextSize(18);

        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);

        newll.addView(categoryNameTextView);
        newll.addView(budgetTextView);
        newll.addView(balanceTextView);

        ll.addView(newll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  onResumeFragments();
        // setCategoriesInGui();
    }

    public void setCloseButton() {
        final Button myButton = new Button(this);
        myButton.setText(getString(R.string.close));
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout newll = new LinearLayout(BudgetActivity.this);

        LinearLayout.LayoutParams paramLL = (new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(myButton, lp);
        ll.addView(newll);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setCategoriesInGui() {
        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<Category> categories = dbService.getCategoriesByPriority(currentRefMonth);

        int totalBudget = 0;
        double totalBalance = 0;
        boolean isExceptionFromBudget = false;

        for (Category category : categories) {
            String categoryName = category.getName();
            double balance = category.getBalance();
            balance = Math.round(balance * 100.d) / 100.0d;
            int budget = category.getBudget();
//            if (balance < 0)
            isExceptionFromBudget = true;
//            addCategoryRow(categoryName, String.valueOf(budget), String.valueOf(balance), isExceptionFromBudget);

            totalBudget += budget;
            totalBalance += balance;
            isExceptionFromBudget = false;
        }
        totalBalance = Math.round(totalBalance * 100.d) / 100.0d;
//        if (totalBalance < 0)
//            isExceptionFromBudget = true;
//        addCategoryRow(getString(R.string.total), String.valueOf(totalBudget), String.valueOf(totalBalance), isExceptionFromBudget);
//        Category fictiveCategory = new Category(null, getString(R.string.total), totalBalance, totalBudget);// Total(last) row
//        categories.add(fictiveCategory);
        DecimalFormat decim = new DecimalFormat("#,###.##");
        ((TextView) findViewById(R.id.totalBalance)).setText(decim.format(totalBalance));
        ((TextView) findViewById(R.id.totalBudget)).setText(decim.format(totalBudget));
        CategoriesViewAdapter adapter = new CategoriesViewAdapter(this, categories);
        RecyclerView categories_rows = (RecyclerView) findViewById(R.id.categories_rows);
        categories_rows.setAdapter(adapter);
        categories_rows.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_budgets);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onRefresh() {
                setCategoriesInGui();
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

    public Month getMonth() {
        return month;
    }

    public String getUserKey() {
        return userKey;
    }
}

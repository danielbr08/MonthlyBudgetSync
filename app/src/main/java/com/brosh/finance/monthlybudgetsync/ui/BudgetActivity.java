package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.config.Language;

public class BudgetActivity extends AppCompatActivity {


    private LinearLayout ll;
    //todo get thos fields from caller intent
    private Month month;
    private Language language;
    private DBService dbService;
    private String userKey;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        Bundle extras = getIntent().getExtras();
        String selectedLanguage = extras.getString(getString(R.string.language), getString(R.string.english));
        String refMonth = extras.getString(getString(R.string.month), null);
        language = new Language(selectedLanguage);
        userKey = extras.getString(getString(R.string.user), getString(R.string.empty));
        dbService = DBService.getInstance();
        month = dbService.getMonth(refMonth);
        setButtonsNames();
//        setTitle(getYearMonth(month.getMonth(), '.'));

        ll = (LinearLayout) findViewById(R.id.LLBudget);
        setCategoriesInGui();
        //setCloseButton();
    }
//        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
//        public void setTitle(String refMonth) {
//            //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//            android.support.v7.app.ActionBar ab = getSupportActionBar();
//            TextView tv = new TextView(getApplicationContext());
//
//            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
//                    ActionBar.LayoutParams.MATCH_PARENT, // Width of TextView
//                    ActionBar.LayoutParams.WRAP_CONTENT);
//            tv.setLayoutParams(lp);
//            tv.setTypeface(null, Typeface.BOLD);
//            tv.setTextColor(Color.WHITE);
//            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//            tv.setText(LANGUAGE.appName + "\n" + refMonth);
//            tv.setTextSize(18);
//
//            ab.setCustomView(tv);
//            ab.setDisplayShowCustomEnabled(true); //show custom title
//            ab.setDisplayShowTitleEnabled(false); //hide the default title
//        }

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

        budgetTextView.setTextDirection(View.TEXT_DIRECTION_LTR);
        balanceTextView.setTextDirection(View.TEXT_DIRECTION_LTR);

        if (language.isLTR()) {
            categoryNameTextView.setTextDirection(View.TEXT_DIRECTION_LTR);
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            categoryNameTextView.setTextDirection(View.TEXT_DIRECTION_RTL);
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }

        budgetTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        //budgetTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        //balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        categoryNameTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        if (categoryName == language.totalName) {
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

        if (!language.isLTR()) {
            newll.addView(categoryNameTextView);
            newll.addView(budgetTextView);
            newll.addView(balanceTextView);

        } else if (language.isLTR()) {
            newll.addView(balanceTextView);
            newll.addView(budgetTextView);
            newll.addView(categoryNameTextView);
        }

        ll.addView(newll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  onResumeFragments();
        // setCategoriesInGui();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setButtonsNames() {
        LinearLayout llBudgetTitle = (LinearLayout) findViewById(R.id.LLBudgetTitle);
        TextView budgetTitleTV = ((TextView) findViewById(R.id.BudgetTitleLabel));
        TextView categoryTV = ((TextView) findViewById(R.id.categoryLabel));
        TextView budgetTV = ((TextView) findViewById(R.id.budgetLabel));
        TextView balanceTV = ((TextView) findViewById(R.id.balanceLabel));
        // Budget window buttons
        budgetTitleTV.setText(language.budgetName);
        categoryTV.setText(language.budgetCategoryName);
        budgetTV.setText(language.budgetName);
        balanceTV.setText(language.balanceName);

        if (!language.isLTR()) {
            llBudgetTitle.removeAllViews();
            llBudgetTitle.addView(categoryTV);
            llBudgetTitle.addView(budgetTV);
            llBudgetTitle.addView(balanceTV);
        } else if (language.isLTR()) {
            llBudgetTitle.removeAllViews();
            llBudgetTitle.addView(balanceTV);
            llBudgetTitle.addView(budgetTV);
            llBudgetTitle.addView(categoryTV);
        }

//        if (month != null)
//            setTitle(getYearMonth(month.getMonth(), '.'));
    }

    public void setCloseButton() {
        final Button myButton = new Button(this);
        myButton.setText(language.close);
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
        int budgetTotal = 0;
        double remainderTotal = 0;
        boolean isExceptionFromBudget = false;

        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        for (Category category : dbService.getCategoriesByPriority(currentRefMonth)) {
            String categoryName = category.getName();
            double remaining = category.getBalance();
            remaining = Math.round(remaining * 100.d) / 100.0d;
            int budget = category.getBudget();
            if (remaining < 0)
                isExceptionFromBudget = true;
            addCategoryRow(categoryName, String.valueOf(budget), String.valueOf(remaining), isExceptionFromBudget);

            budgetTotal += budget;
            remainderTotal += remaining;
            isExceptionFromBudget = false;
        }
        remainderTotal = Math.round(remainderTotal * 100.d) / 100.0d;
        if (remainderTotal < 0)
            isExceptionFromBudget = true;
        addCategoryRow(language.totalName, String.valueOf(budgetTotal), String.valueOf(remainderTotal), isExceptionFromBudget);

    }
}

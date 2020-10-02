package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.login.Login;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private DBUtil dbUtil;
    private String userKey;
    private User user;

    private Intent budgetScreen, transactionsScreen, insertTransactionScreen, createBudgetScreen;
    private Spinner refMonthSpinner;
    private Button insertTransactionButton;
    private Button budgetButton;
    private Button transactionsButton;
    private Button createBudgetButton;
    private Month month;
    private SwipeRefreshLayout refreshLayout;
    private TextView userLogeedInTV;

    public void initRefMonthSpinner() {
        List<String> allMonths = dbUtil.getAllMonthsYearMonth();
        String refMonth;
        if (month != null) {
            refMonth = month.getYearMonth();
            if (allMonths.contains(refMonth)) {
                allMonths.remove(refMonth);
                allMonths.add(0, refMonth);// put the current month first
            }
        }
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner, allMonths);
        refMonthSpinner.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsItem:
//                Toast.makeText(this, "settings item selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                addParametersToActivity(intent);
                startActivity(intent);
                return true;
            case R.id.shareItem:
                openShareDialog();
                return true;
            case R.id.recommend_to_friend:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Config.APP_URL);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
                return true;
            case R.id.helpItem:
                Toast.makeText(this, "help item selected", Toast.LENGTH_SHORT).show();
                return true;
//            case R.id.aboutItem:
//                Toast.makeText(this, "about item selected", Toast.LENGTH_SHORT).show();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = DBUtil.getInstance().getUser();
        if (user.getUserSettings().isAdEnabled()) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        userKey = user.getDbKey();
        userLogeedInTV = findViewById(R.id.tv_user_logeed_in);
        userLogeedInTV.setText(String.format("%s %s", getString(R.string.logged_as), user.getName()));
        dbUtil = DBUtil.getInstance();

        refMonthSpinner = findViewById(R.id.monthSpinner);

        budgetScreen = null;
        transactionsScreen = null;
        insertTransactionScreen = null;
        createBudgetScreen = null;
        refMonthSpinner = findViewById(R.id.monthSpinner);

        insertTransactionButton = findViewById(R.id.insertTransactionButton);
        budgetButton = findViewById(R.id.budgetButton);
        transactionsButton = findViewById(R.id.transactionsButton);
        createBudgetButton = findViewById(R.id.createBudgetButton);
        refresh();
        setRefreshListener();
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);

        budgetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (budgetScreen == null)
                    budgetScreen = new Intent(getApplicationContext(), BudgetActivity.class);
                addParametersToActivity(budgetScreen);
                startActivity(budgetScreen);
            }
        });

        insertTransactionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (insertTransactionScreen == null)
                    insertTransactionScreen = new Intent(getApplicationContext(), InsertTransactionActivity.class);
                addParametersToActivity(insertTransactionScreen);
                startActivity(insertTransactionScreen);

            }
        });

        transactionsButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (transactionsScreen == null)
                    transactionsScreen = new Intent(getApplicationContext(), TransactionsActivity.class);
                addParametersToActivity(transactionsScreen);
                startActivity(transactionsScreen);
            }
        });

        createBudgetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (createBudgetScreen == null)
                    createBudgetScreen = new Intent(getApplicationContext(), CreateBudgetActivity.class);
                addParametersToActivity(createBudgetScreen);
                startActivity(createBudgetScreen);
            }
        });

        refMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String refMonth = refMonthSpinner.getSelectedItem().toString();
                month = dbUtil.getMonth(refMonth);
                boolean isActive = month != null && month.isActive();
                Drawable circleButton = isActive ? getResources().getDrawable(R.drawable.circle_pink_style) : getResources().getDrawable(R.drawable.circle_gray_style);
                insertTransactionButton.setEnabled(isActive);
                createBudgetButton.setEnabled(isActive);
                insertTransactionButton.setBackground(circleButton);
                createBudgetButton.setBackground(circleButton);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    public void logout(View view) {
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("rememberMe", "false");
        if (preferences.contains("email"))
            editor.remove("email");
        if (preferences.contains("password"))
            editor.remove("password");
        editor.apply();

        FirebaseAuth.getInstance().signOut();//logout
        DBUtil.getInstance().clear();
        userLogeedInTV.setText(getString(R.string.empty));
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void openShareDialog() {
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText emailInput = new EditText(this);
        emailInput.setHint(getString(R.string.please_enter_user_email_to_share));
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setTitle(getString(R.string.share_budget));

        builder.setView(emailInput);
        builder.setPositiveButton(getString(R.string.share), null);
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = emailInput.getText().toString();
                if (!TextUtil.isEmailValid(emailText)) {
                    emailInput.setError(getString(R.string.invalid_email));
                } else {
                    try {
                        DBUtil.getInstance().share(emailText);
                        TextUtil.showMessage(getString(R.string.successfully_shared), Toast.LENGTH_LONG, context);
                    } catch (Exception e) {
                        TextUtil.showMessage(e.getMessage(), Toast.LENGTH_LONG, context);
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    private void createNewMonth(Date refMonthDate) {
        String refMonth = DateUtil.getYearMonth(refMonthDate, Config.SEPARATOR);
        int budgetNumber = dbUtil.getMaxBudgetNumber();
        dbUtil.createNewMonth(budgetNumber, refMonth);
    }

//    public void share(View view) {
//        String shareWith = ((EditText)findViewById(R.id.etShare)).getText().toString().trim().replace('.',',');
//        DatabaseReferenceShares.child(shareWith).setValue(email.getText().toString().trim().replace('.',','));
//    }

//    public void openCreateBudgetActivity(String userKey){
////        Intent intent = new Intent(MainActivity.this, Create_Budget_Activity.class);
////        intent.putExtra(getString(R.string.language),getString(R.string.hebrew));
//////        String userKey = getIntent().getExtras().getString(getString(R.string.user),getString(R.string.empty));
////        intent.putExtra(getString(R.string.user),userKey);
////        intent.putExtra( getString(R.string.db_service),dbService);
////        startActivity(intent);
////    }

    public void addParametersToActivity(Intent activity) {
        activity.putExtra(Definitions.MONTH, month == null ? null : month.getYearMonth());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!dbUtil.isAnyBudgetExists()) { // Budget not exists at all
            budgetButton.setEnabled(false);
            transactionsButton.setEnabled(false);
            insertTransactionButton.setEnabled(false);
            budgetButton.setBackground(getResources().getDrawable(R.drawable.circle_gray_style));
            transactionsButton.setBackground(getResources().getDrawable(R.drawable.circle_gray_style));
            insertTransactionButton.setBackground(getResources().getDrawable(R.drawable.circle_gray_style));
            month = null;
        } else {
            if (month == null) { // After create budget first time
                if (dbUtil.isCurrentRefMonthExists()) {
                    month = dbUtil.getMonth(DateUtil.getYearMonth(DateUtil.getTodayDate(), Config.SEPARATOR));
                    initRefMonthSpinner();
                    budgetButton.setEnabled(true);
                    transactionsButton.setEnabled(true);
                    insertTransactionButton.setEnabled(true);
                    budgetButton.setBackground(getResources().getDrawable(R.drawable.circle_pink_style));
                    transactionsButton.setBackground(getResources().getDrawable(R.drawable.circle_pink_style));
                    insertTransactionButton.setBackground(getResources().getDrawable(R.drawable.circle_pink_style));
                } else { // todo Exception should throws

                }
            } else { // Same month as before
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ValueEventListener rootEventListener = DBUtil.getInstance().getRootEventListener();
        if (rootEventListener != null) {
            Config.DatabaseReferenceMonthlyBudget.child(userKey).removeEventListener(rootEventListener);
        }
    }

    private void refresh() {
        if (!dbUtil.isAnyBudgetExists()) {
            budgetButton.setEnabled(false);
            transactionsButton.setEnabled(false);
            insertTransactionButton.setEnabled(false);
            budgetButton.setBackground(getResources().getDrawable(R.drawable.circle_gray_style));
            transactionsButton.setBackground(getResources().getDrawable(R.drawable.circle_gray_style));
            insertTransactionButton.setBackground(getResources().getDrawable(R.drawable.circle_gray_style));
            month = null;
        } else {
            if (!dbUtil.isCurrentRefMonthExists()) {
                createNewMonth(new Date());
                insertTransactionButton.setEnabled(true);
                insertTransactionButton.setBackground(getResources().getDrawable(R.drawable.circle_pink_style));
            }
            budgetButton.setEnabled(true);
            transactionsButton.setEnabled(true);
            budgetButton.setBackground(getResources().getDrawable(R.drawable.circle_pink_style));
            transactionsButton.setBackground(getResources().getDrawable(R.drawable.circle_pink_style));
            month = dbUtil.getMonth(DateUtil.getYearMonth(DateUtil.getTodayDate(), Config.SEPARATOR));
            Date nextRefMonth = DateUtil.getNextRefMonth(month.getRefMonth());
            if (new Date().after(nextRefMonth)) {
                createNewMonth(nextRefMonth);
                insertTransactionButton.setEnabled(true);
                insertTransactionButton.setBackground(getResources().getDrawable(R.drawable.circle_pink_style));
            }
            initRefMonthSpinner();
        }
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_main);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onRefresh() {
                refresh();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // todo check for better solution for buttons order
        TextView tv = new TextView(this);
        tv.setTextColor(getResources().getColor(R.color.colorLoginBackground));
        tv.setText(R.string.are_you_sure_you_want_to_exit);
        tv.setPadding(40, 40, 40, 0);
        new AlertDialog.Builder(this)
                .setCustomTitle(tv)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() { // Negative is actually positive
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setPositiveButton(getString(R.string.no), null) // Positive is actually negative
                .show();
    }
}
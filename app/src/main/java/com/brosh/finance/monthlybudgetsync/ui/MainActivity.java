package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
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
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.login.Login;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.services.DBService;
//import com.brosh.finance.monthlybudgetsync.services.NetworkService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.config.Language;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference DatabaseReferenceUserMonthlyBudget;
    private DatabaseReference DatabaseReferenceShares;
    private DBService dbService;
    private Language language;
    private String userKey;

    private SharedPreferences sharedpreference;
    private Intent budgetScreen, transactionsScreen, insertTransactionScreen, createBudgetScreen;
    private Spinner refMonthSpinner, languageSpinner;
    private Button insertTransactionButton;
    private Button budgetButton;
    private Button transactionsButton;
    private Button createBudgetButton;
    private Button closeMainButton;
    private boolean touched = false; // Indicate for language spinner
    private Month month;

    public void initLanguageSpinner() {
        List<String> allMonths = new ArrayList<>();
        allMonths.add(getString(R.string.hebrew));
        allMonths.add(getString(R.string.english));

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, allMonths);
        languageSpinner.setAdapter(adapter);
    }

    public void changeTouchValue(boolean touched) {
        this.touched = touched;
    }

    public void initRefMonthSpinner() {
        List<String> allMonths = dbService.getAllMonthsYearMonth();
        String refMonth;
        if (month != null) {
            refMonth = month.getYearMonth();
            if (allMonths.contains(refMonth)) {
                allMonths.remove(refMonth);
                allMonths.add(0, refMonth);// put the current month first
            }
        }
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, allMonths);
        refMonthSpinner.setAdapter(adapter);
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
//    public void setTitle(String refMonth)
//    {
//        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        android.support.v7.app.ActionBar ab = getSupportActionBar();
//        TextView tv = new TextView(getApplicationContext());
//
//        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
//                ActionBar.LayoutParams.MATCH_PARENT, // Width of TextView
//                ActionBar.LayoutParams.WRAP_CONTENT);
//        tv.setLayoutParams(lp);
//        tv.setTypeface(null, Typeface.BOLD);
//        tv.setTextColor(Color.WHITE);
//        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        tv.setText(language.appName + "\n"  + refMonth);
//        tv.setTextSize(18);
//
//        ab.setCustomView(tv);
//        ab.setDisplayShowCustomEnabled(true); //show custom title
//        ab.setDisplayShowTitleEnabled(false); //hide the default title
//    }

    public void setButtonsNames() {
        // Main window buttons
//        ((TextView) findViewById(R.id.monthLabel)).setText(language.monthName);
        ((Button) findViewById(R.id.budgetButton)).setText(language.budgetButtonName);
        ((Button) findViewById(R.id.transactionsButton)).setText(language.transactionsButtonName);
        ((Button) findViewById(R.id.insertTransactionButton)).setText(language.insertTransactionButtonName);
        ((Button) findViewById(R.id.createBudgetButton)).setText(language.createBudgetButtonName);
        ((Button) findViewById(R.id.closeMainButton)).setText(language.close);
//        if(month != null)
//            setTitle(getYearMonth(month.getMonth(), '.'));
/*
        // Budget window buttons
        ((TextView)findViewById(R.id.budgetLabel)).setText(language.budgetTitleName);
        ((TextView)findViewById(R.id.categoryLabel)).setText(language.categoryName);
        ((TextView)findViewById(R.id.budgetLabel)).setText(language.budgetName);
        ((TextView)findViewById(R.id.balanceLabel)).setText(language.balanceName);

        // Transactions window buttons
        ((TextView)findViewById(R.id.textViewTotalTransactions)).setText(language.totalName);

        // Transactions window buttons
        ((TextView)findViewById(R.id.createBudgetLabel)).setText(language.createBudgetName);*/
    }

    public void setSpinnerAllignment(boolean isFirstTime) {
        String lang = languageSpinner.getSelectedItem().toString();
        if (isFirstTime)
            lang = Config.DEFAULT_LANGUAGE;
        ArrayAdapter<String> adapter;
        ArrayList<String> allMonths = new ArrayList();
        allMonths.add(getString((R.string.hebrew)));
        allMonths.add(getString((R.string.english)));
        if (lang.equals(getString((R.string.hebrew)))) {
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner, allMonths);
            adapter.setDropDownViewResource(R.layout.custom_spinner);
            languageSpinner.setAdapter(adapter);
            languageSpinner.setSelection(0, true);
        } else if (lang.equals(getString((R.string.english)))) {
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner_eng, allMonths);
            adapter.setDropDownViewResource(R.layout.custom_spinner_eng);
            languageSpinner.setAdapter(adapter);
            languageSpinner.setSelection(1, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        language = new Language(Config.DEFAULT_LANGUAGE);
        userKey = getIntent().getExtras().getString(getString(R.string.user), getString(R.string.empty));
        dbService = DBService.getInstance();

        DatabaseReferenceUserMonthlyBudget = FirebaseDatabase.getInstance().getReference(getString(R.string.monthly_budget)).child(userKey);
        DatabaseReferenceShares = FirebaseDatabase.getInstance().getReference(getString(R.string.shares));

        refMonthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
        initLanguageSpinner();


        setSpinnerAllignment(true);
        setButtonsNames();

        budgetScreen = null;
        transactionsScreen = null;
        insertTransactionScreen = null;
        createBudgetScreen = null;
        refMonthSpinner = (Spinner) findViewById(R.id.monthSpinner);

        insertTransactionButton = (Button) findViewById(R.id.insertTransactionButton);
        budgetButton = (Button) findViewById(R.id.budgetButton);
        transactionsButton = (Button) findViewById(R.id.transactionsButton);
        createBudgetButton = (Button) findViewById(R.id.createBudgetButton);
        closeMainButton = (Button) findViewById(R.id.closeMainButton);

        if (!dbService.isAnyBudgetExists()) {
            budgetButton.setEnabled(false);
            transactionsButton.setEnabled(false);
            insertTransactionButton.setEnabled(false);
            month = null;
        } else {
            if (!dbService.isCurrentRefMonthExists()) {
                createNewMonth(new Date());
            }
            month = dbService.getMonth(DateService.getYearMonth(DateService.getTodayDate(), Config.SEPARATOR));
            Date nextRefMonth = DateService.getNextRefMonth(month.getRefMonth());
            if (new Date().after(nextRefMonth)) {
                createNewMonth(nextRefMonth);
            }
//            setTitle(getYearMonth(month.getMonth(), '.'));
            initRefMonthSpinner();
        }

        //month = new Month(getTodayDate());
        //setTitle(month.getYearMonth(month.getMonth(), '.'));
        //initRefMonthSpinner();

        //Listening to button event
        budgetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (budgetScreen == null)
                    budgetScreen = new Intent(getApplicationContext(), BudgetActivity.class);
                addParametersToActivity(budgetScreen);
                startActivity(budgetScreen);
            }
        });

        //Listening to button event
        insertTransactionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (insertTransactionScreen == null)
                    insertTransactionScreen = new Intent(getApplicationContext(), InsertTransactionActivity.class);
                addParametersToActivity(insertTransactionScreen);
                startActivity(insertTransactionScreen);

            }
        });

        //Listening to button event
        transactionsButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (transactionsScreen == null)
                    transactionsScreen = new Intent(getApplicationContext(), TransactionsActivity.class);
                addParametersToActivity(transactionsScreen);
                startActivity(transactionsScreen);
            }
        });

        //Listening to button event
        createBudgetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (createBudgetScreen == null)
                    createBudgetScreen = new Intent(getApplicationContext(), Create_Budget_Activity.class);
                //initRefMonthSpinner();
                addParametersToActivity(createBudgetScreen);
                startActivity(createBudgetScreen);
            }
        });

        //Listening to button event
        closeMainButton.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View arg0) {
                // finish();
                // System.exit(0);
                // finish();
            }
        });

        languageSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                changeTouchValue(true);
                return false;
            }
        });

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!touched)
                    return;
                String lang = languageSpinner.getSelectedItem().toString();
                language.setLanguage(lang);

//                if (lang.equals(getString(R.string.hebrew))) { // todo set default language in user object
//
//                    language = new Language(getString(R.string.hebrew));
//                    sharedpreference.edit().putString("default_language",getString(R.string.hebrew)).apply();
//                    sharedpreference.edit().commit();
//
//                }
//                else if (lang.equals(getString(R.string.english))) {
//                    language = new Language(getString(R.string.english));
//                    sharedpreference.edit().putString("default_language",getString(R.string.english)).apply();
//                    sharedpreference.edit().commit();
//                }

                setSpinnerAllignment(false);
                setButtonsNames();
                changeTouchValue(false);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        refMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String refMonth = refMonthSpinner.getSelectedItem().toString();
//                refMonth = (refMonth + ".01");
//                refMonth = DateService.reverseDateString(refMonth,"\\.");
//                refMonth = refMonth.replace('.', '/');
//                Date selectedDate = DateService.convertStringToDate(refMonth, Config.DATE_FORMAT);
//                refMonth = DateService.getYearMonth(selectedDate, Config.DATE_FORMAT_CHARACTER);
                month = dbService.getMonth(refMonth);
                boolean isActive = month != null && month.isActive();
//                if(month != null)
//                setTitle(DateService.getYearMonth(month.getMonth(), '.'));

                insertTransactionButton.setEnabled(isActive);
                createBudgetButton.setEnabled(isActive);
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
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

//    public void openShareDialog() {
//        final Context context = this;
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(language.shareMonthlyBudget);
//        final EditText emailInput = new EditText(this);
//        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//        emailInput.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                String emailText = emailInput.getText().toString();
//                if (dbService.isEmailAlreadyShared(emailText)) {
//                    emailInput.setError(language.emailAlreadyshared);
//                } else {
//                    emailInput.setError(null);
//                }
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String emailText = emailInput.getText().toString();
//                if (dbService.isEmailAlreadyShared(emailText)) {
//                    emailInput.setError(language.emailAlreadyshared);
//                } else {
//                    emailInput.setError(null);
//                }
//            }
//        });
//
//        builder.setView(emailInput);
//        builder.setPositiveButton(language.insert, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String emailText = emailInput.getText().toString();
//                try {
//                    dbService.share(emailText);
//                    TextService.showMessage(language.successfullyShared, Toast.LENGTH_LONG, context);
//                } catch (Exception e) {
//                    TextService.showMessage(e.getMessage(), Toast.LENGTH_LONG, context);
//                }
//            }
//        });
//        builder.setNegativeButton(language.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        builder.show();
//    }

    private void createNewMonth(Date refMonthDate) {
        String refMonth = DateService.getYearMonth(refMonthDate, Config.SEPARATOR);
        int budgetNumber = dbService.getMaxBudgetNumber();
        dbService.createNewMonth(budgetNumber, refMonth);
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
        activity.putExtra(getString(R.string.language), language.getLanguage());
        activity.putExtra(getString(R.string.user), userKey);
        activity.putExtra(getString(R.string.month), month == null ? month : month.getYearMonth());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!dbService.isAnyBudgetExists()) { // Budget not exists at all
            budgetButton.setEnabled(false);
            transactionsButton.setEnabled(false);
            insertTransactionButton.setEnabled(false);
            month = null;
        } else {
            if (month == null) { // After create budget first time
                if (dbService.isCurrentRefMonthExists()) {
                    month = dbService.getMonth(DateService.getYearMonth(DateService.getTodayDate(), Config.SEPARATOR));
                    initRefMonthSpinner();
                    budgetButton.setEnabled(true);
                    transactionsButton.setEnabled(true);
                    insertTransactionButton.setEnabled(true);
                } else { // todo Exception should throws

                }
            } else { // Same month as before

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ValueEventListener rootEventListener = DBService.getInstance().getRootEventListener();
        if (rootEventListener != null) {
            Config.DatabaseReferenceMonthlyBudget.child(userKey).removeEventListener(rootEventListener);
        }
    }
}

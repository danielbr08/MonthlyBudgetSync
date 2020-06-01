package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.SpinnerAdapter;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class InsertTransactionActivity extends AppCompatActivity {

//    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_insert_transaction);
////    }

    private Spinner categoriesSpinner;
    private Spinner paymentTypeSpinner;
    private Button btnSendTransaction;
    private Button btnClose;
    private EditText payDateEditText;

    private DBService dbService;
    private String userKey;
    private User user;
    private Month month;

    private Set<String> shopsSet;

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

    public void setSpinnersAllignment() {
        List<String> categoriesNames = dbService.getCategoriesNames(month.getYearMonth());
        List<String> paymentMethod = getPaymentMethodList();
        SpinnerAdapter namesAdapter = new SpinnerAdapter(categoriesNames, this);
        SpinnerAdapter PaymentMethodAdapter = new SpinnerAdapter(paymentMethod, this);
        categoriesSpinner.setAdapter(namesAdapter);
        paymentTypeSpinner.setAdapter(PaymentMethodAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void init() {
        setSpinnersAllignment();
        if (shopsSet.size() == 0) {
            shopsSet.addAll(shopsSet);
        }
        List<String> shopsList = new ArrayList<String>(shopsSet);
        AutoCompleteTextView shposAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.shopAutoCompleteTextView);
        ArrayAdapter<String> anotherAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, shopsList);
        shposAutoCompleteTextView.setAdapter(anotherAdapter);
        shposAutoCompleteTextView.setThreshold(2);// Set auto complete from the first character
    }

    public String getCurrentDate() {
        Calendar mcurrentDate = Calendar.getInstance();
        int mYear = mcurrentDate.get(Calendar.YEAR);
        int mMonth = mcurrentDate.get(Calendar.MONTH);
        int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        String day, month;
        if (mDay < 10)
            day = "0" + mDay;
        else
            day = String.valueOf(mDay);
        if ((mMonth + 1) < 10)
            month = "0" + (int) (mMonth + 1);
        else
            month = String.valueOf(mMonth + 1);
        return (day + "/" + month + "/" + mYear);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_transaction);

        Bundle extras = getIntent().getExtras();
        String refMonth = extras.getString(Definition.MONTH, null);
        user = (User) getIntent().getExtras().getSerializable(Definition.USER);
        userKey = user.getDbKey();
        dbService = DBService.getInstance();
        month = dbService.getMonth(refMonth);
        setToolbar();

        shopsSet = dbService.getShopsSet();
//        setTitle( getYearMonth(month.getMonth(),'.'));
        categoriesSpinner = (Spinner) findViewById(R.id.categorySpinner);
        paymentTypeSpinner = (Spinner) findViewById(R.id.paymentMethodSpinner);
        btnSendTransaction = (Button) findViewById(R.id.sendTransactionButton);
        init();

        payDateEditText = (EditText) findViewById(R.id.payDatePlainText);
        payDateEditText.setText(getCurrentDate());

        payDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //To show current date in the datepicker
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getCurrentFocus();
                if(focusedView != null){
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(InsertTransactionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        /*      Your code   to get date and time    */
                        String day, month;
                        if (selectedday < 10)
                            day = "0" + selectedday;
                        else
                            day = String.valueOf(selectedday);
                        if ((selectedmonth + 1) < 10)
                            month = "0" + (int) (selectedmonth + 1);
                        else
                            month = String.valueOf(selectedmonth + 1);
                        payDateEditText.setText(day + "/" + month + "/" + selectedyear);
                        payDateEditText.setError(null);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle(getString(R.string.selecting_date));
                mDatePicker.show();
            }
        });


        //Intent i = getIntent();
        // Receiving the Data
        //String name = i.getStringExtra("name");
        // String email = i.getStringExtra("email");

        // Displaying Received data
        //txtName.setText(name);
        //txtEmail.setText(email);

        // Binding Click event to Button
//        btnClose.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View arg0) {
//                //Closing SecondScreen Activity
//                finish();
//            }
//        });

        btnSendTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                insertTransaction(refMonth);
            }
        });
    }

    public void tryInsertTransactionAgain(String refMonth) {
        try {
            Thread.sleep(1000);
            insertTransaction(refMonth);
        } catch (Exception e) {
        }
    }

    public void insertTransaction(String refMonth) {
        EditText shopET = ((EditText) findViewById(R.id.shopAutoCompleteTextView));
        EditText payDateET = ((EditText) findViewById(R.id.payDatePlainText));
        EditText transactionPriceET = ((EditText) findViewById(R.id.transactionPricePlainText));
        if (setErrorEditText(payDateET) || setErrorEditText(transactionPriceET))
            return;
        int idPerMonth = month.getTranIdNumerator() + 1;

        DatabaseReference monthDB = dbService.getDBMonthPath(refMonth);
        final Context context = this;

        //Insert data
        String categoryName = categoriesSpinner.getSelectedItem().toString();
        String paymentMethod = paymentTypeSpinner.getSelectedItem().toString();
        //String category = getCategoryName(categoryHeb);
        String shop = shopET.getText().toString();
        Date payDate = DateService.convertStringToDate(payDateET.getText().toString(), Config.DATE_FORMAT);
        double transactionPrice = Double.valueOf(transactionPriceET.getText().toString());
        //init on create
        String catId = dbService.getCategoryByName(month.getYearMonth(), categoryName).getId();
        DatabaseReference transactionsNode = dbService.getDBTransactionsPath(month.getYearMonth(), catId);
        String tranId = transactionsNode.push().getKey();
        com.brosh.finance.monthlybudgetsync.objects.Transaction transaction = new com.brosh.finance.monthlybudgetsync.objects.Transaction(tranId, idPerMonth, categoryName, paymentMethod, shop, payDate, transactionPrice);
        boolean isStorno = false;
        int stornoOf = -1;

        for (Category cat : month.getCategories().values()) {
            if (categoryName.equals(cat.getName())) {
                cat.withdrawal(transactionPrice);
                List<com.brosh.finance.monthlybudgetsync.objects.Transaction> catTrans = dbService.getTransactions(month.getYearMonth(), cat.getId());
                for (com.brosh.finance.monthlybudgetsync.objects.Transaction tran : catTrans) {
                    isStorno = tran.isStorno(transaction);
                    if (isStorno == true) {
                        stornoOf = tran.getIdPerMonth();
                        tran.setIsStorno(true);
                        tran.setStornoOf(transaction.getIdPerMonth());
                        break;
                    }
                }
                transaction.setIsStorno(isStorno);
                transaction.setStornoOf(stornoOf);
                cat.addTransaction(transaction);
                month.setTranIdNumerator(idPerMonth);
                break;
            }
        }
        shopsSet.add(shop); // todo check if event listener call
        dbService.writeNewShopFB(shop);
        Category category = dbService.getCategoryById(refMonth, catId);
//                    monthDB.child(Definition.TRAN_ID_NUMERATOR).setValue(idPerMonth);
//                categoriesDB.child(Definition.BALANCE)
        monthDB.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Object trnNumeratorFB = mutableData.child(Definition.TRAN_ID_NUMERATOR).getValue();
                int idPerMonth = Integer.valueOf(trnNumeratorFB.toString()) + 1;
                transaction.setIdPerMonth(idPerMonth);
                mutableData.child(Definition.TRAN_ID_NUMERATOR).setValue(idPerMonth);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                mutableData.child(Definition.BALANCE).setValue(category.getBalance());
                mutableData.child(Definition.CATEGORIES).child(category.getId()).child(Definition.TRANSACTIONS).child(tranId).setValue(transaction); // todo check if event listener call
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                // send message and close window
                String message = databaseError != null ? "Error" : getString(R.string.transaction_inserted_successfully);
                try {
                    TextService.showMessage(getString(R.string.transaction_inserted_successfully), Toast.LENGTH_LONG, context);
                    ((Activity) context).finish();
                } catch (Exception e) {
                    String s = e.getMessage();
                    s = s;
                }
            }
        });
    }

    public void showMessageToast(String message, boolean isFinishNeeded) {
        Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                if (isFinishNeeded)
                    finish();
            }
        });
    }

    public boolean setErrorEditText(EditText et) {
        if (et.length() == 0) {
            et.setError(getString(R.string.requiredField));
            return true;
        }
        return false;
    }

    public void showMessageNoButton(String message)//View view)
    {
        final AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage(message);//.create()
        myAlert.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                myAlert.create().dismiss();
                finish();
            }
        }, 1000); // 3000 milliseconds delay

    }

    private List<String> getPaymentMethodList() {
        List<String> paymentMethodList = new ArrayList<>();
        paymentMethodList.add(getString(R.string.credit_card));
        paymentMethodList.add(getString(R.string.cash));
        paymentMethodList.add(getString(R.string.chek));
        paymentMethodList.add(getString(R.string.bank_wired));

        return paymentMethodList;
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
}

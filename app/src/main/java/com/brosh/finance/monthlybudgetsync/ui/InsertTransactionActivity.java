package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.SpinnerAdapter;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
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
    private static final String TAG = "InsertTransactionActivity";

    private Spinner categoriesSpinner;
    private Spinner paymentTypeSpinner;
    private Button btnSendTransaction;
    private EditText payDateEditText;
    private ProgressBar progressBar;

    private DBUtil dbUtil;
    private User user;
    private Month month;

    private Set<String> shopsSet;

    public void setSpinnersAllignment() {
        List<String> categoriesNames = dbUtil.getCategoriesNames(month.getYearMonth());
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

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_transaction);

        progressBar = findViewById(R.id.progress_circular);
        Bundle extras = getIntent().getExtras();
        String refMonth = extras.getString(Definitions.MONTH, null);
        user = (User) getIntent().getExtras().getSerializable(Definitions.USER);
        if (user.getUserSettings().isAdEnabled()) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        dbUtil = DBUtil.getInstance();
        month = dbUtil.getMonth(refMonth);
        setToolbar();

        shopsSet = dbUtil.getShopsSet();
        categoriesSpinner = findViewById(R.id.categorySpinner);
        paymentTypeSpinner = findViewById(R.id.paymentMethodSpinner);
        btnSendTransaction = findViewById(R.id.sendTransactionButton);
        init();

        payDateEditText = findViewById(R.id.payDatePlainText);
        payDateEditText.setText(DateUtil.getCurrentDate(Config.DATE_FORMAT_CHARACTER));

        payDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //To show current date in the datepicker
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(InsertTransactionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        String day, month;
                        day = selectedday < 10 ? "0" + selectedday : String.valueOf(selectedday);
                        month = (selectedmonth + 1) < 10 ? "0" + selectedmonth + 1 : String.valueOf(selectedmonth + 1);
                        payDateEditText.setText(day + "/" + month + "/" + selectedyear);
                        payDateEditText.setError(null);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle(getString(R.string.selecting_date));
                mDatePicker.show();
            }
        });

        btnSendTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                insertTransaction(refMonth);
                view.setEnabled(false);
            }
        });
    }

    public void insertTransaction(String refMonth) {
        EditText shopET = (findViewById(R.id.shopAutoCompleteTextView));
        EditText payDateET = (findViewById(R.id.payDatePlainText));
        EditText transactionPriceET = (findViewById(R.id.transactionPricePlainText));
        if (setErrorEditText(payDateET) || setErrorEditText(transactionPriceET))
            return;
        int idPerMonth = month.getTranIdNumerator() + 1;
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference monthDB = dbUtil.getDBMonthPath(refMonth);
        final Context context = this;

        //Insert data
        String categoryName = categoriesSpinner.getSelectedItem().toString();
        String paymentMethod = paymentTypeSpinner.getSelectedItem().toString();
        String shop = shopET.getText().toString();
        Date payDate = DateUtil.convertStringToDate(payDateET.getText().toString(), Config.DATE_FORMAT);
        double transactionPrice = Double.valueOf(transactionPriceET.getText().toString());
        String catId = dbUtil.getCategoryByName(month.getYearMonth(), categoryName).getId();
        DatabaseReference transactionsNode = dbUtil.getDBTransactionsPath(month.getYearMonth(), catId);
        String tranId = transactionsNode.push().getKey();
        com.brosh.finance.monthlybudgetsync.objects.Transaction transaction = new com.brosh.finance.monthlybudgetsync.objects.Transaction(tranId, idPerMonth, categoryName, paymentMethod, shop, payDate, transactionPrice);

        shopsSet.add(shop); // todo check if event listener call
        dbUtil.writeNewShopFB(shop);
        Category category = dbUtil.getCategoryById(refMonth, catId);
        monthDB.runTransaction(new Transaction.Handler() { // todo move to DBService
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Object trnNumeratorFB = mutableData.child(Definitions.TRAN_ID_NUMERATOR).getValue();
                int idPerMonth = Integer.valueOf(trnNumeratorFB.toString()) + 1;
                transaction.setIdPerMonth(idPerMonth);
                mutableData.child(Definitions.TRAN_ID_NUMERATOR).setValue(idPerMonth);
                mutableData.child(Definitions.BALANCE).setValue(category.getBalance());
                mutableData.child(Definitions.CATEGORIES).child(category.getId()).child(Definitions.TRANSACTIONS).child(tranId).setValue(transaction); // todo check if event listener call
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b,
                                   @Nullable DataSnapshot dataSnapshot) {
                // send message and close window
                String message = databaseError != null ? "Error" : getString(R.string.transaction_inserted_successfully);
                try {
                    TextUtil.showMessage(getString(R.string.transaction_inserted_successfully), Toast.LENGTH_LONG, context);
                    ((Activity) context).finish();
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    String s = e.getMessage();
                    s = s;
                }
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

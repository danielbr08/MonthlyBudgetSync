package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DatePickerHelper;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.IntentHelper;
import com.brosh.finance.monthlybudgetsync.utils.KeyboardUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.brosh.finance.monthlybudgetsync.utils.ValidationUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class InsertTransactionActivity extends AppCompatActivity {
    private Spinner categoriesSpinner;
    private Spinner paymentTypeSpinner;
    private Button btnSendTransaction;
    private EditText payDateEditText;
    private EditText commentEditText;
    private CheckBox addCommentCheckBox;
    private ProgressBar progressBar;

    private DBUtil dbUtil;
    private User user;
    private Month month;

    private Set<String> shopsSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_transaction);

        // Let the system handle orientation based on device capabilities

        progressBar = findViewById(R.id.progress_circular);
        String refMonth = IntentHelper.getYearMonth(this);
        user = DBUtil.getInstance().getUser();
        
        // Setup ads visibility with null safety
        boolean adEnabled = user != null && user.getUserSettings().isAdEnabled();
        if (adEnabled) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            View adView = findViewById(R.id.adView);
            if (adView != null) {
                adView.setVisibility(View.GONE);
            }
        }
        
        dbUtil = DBUtil.getInstance();
        month = dbUtil.getMonth(refMonth);
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);

        shopsSet = dbUtil.getShopsSet();
        categoriesSpinner = findViewById(R.id.categorySpinner);
        paymentTypeSpinner = findViewById(R.id.paymentMethodSpinner);
        btnSendTransaction = findViewById(R.id.sendTransactionButton);
        init();

        payDateEditText = findViewById(R.id.payDatePlainText);
        payDateEditText.setText(DateUtil.getCurrentDate(Config.DATE_FORMAT_CHARACTER));

        // Use DatePickerHelper for cleaner date picker handling
        payDateEditText.setOnClickListener(v -> 
            DatePickerHelper.showDatePicker(InsertTransactionActivity.this, payDateEditText)
        );

        // Initialize comment checkbox and EditText
        addCommentCheckBox = findViewById(R.id.addCommentCheckBox);
        commentEditText = findViewById(R.id.commentEditText);
        setupCommentCheckboxBehavior();
        setupCommentEditTextBehavior();

        btnSendTransaction.setOnClickListener(view -> {
            insertTransaction(refMonth);
            view.setEnabled(false);
        });
    }

    /**
     * Sets up the checkbox to toggle the comment EditText visibility.
     */
    private void setupCommentCheckboxBehavior() {
        addCommentCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                commentEditText.setVisibility(View.VISIBLE);
                commentEditText.requestFocus();
            } else {
                commentEditText.setVisibility(View.GONE);
                commentEditText.setText("");
                KeyboardUtil.hideKeyboard(this);
            }
        });
    }

    /**
     * Sets up the comment EditText to expand when focused and collapse when not focused.
     */
    private void setupCommentEditTextBehavior() {
        commentEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Expand: allow multiple lines
                commentEditText.setMaxLines(5);
                commentEditText.setMinHeight((int) (100 * getResources().getDisplayMetrics().density));
            } else {
                // Collapse: show single line when not focused
                commentEditText.setMaxLines(1);
                commentEditText.setMinHeight((int) (40 * getResources().getDisplayMetrics().density));
            }
        });
    }

    public void setSpinnersAllignment() {
        String yearMonth = month != null ? month.getYearMonth() : null;
        List<String> categoriesNames = yearMonth != null ? dbUtil.getCategoriesNames(yearMonth) : new ArrayList<>();
        List<String> paymentMethod = getPaymentMethodList();
        ArrayAdapter<String> categoriesNamesAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_insert_transaction, categoriesNames);
        ArrayAdapter<String> paymentMethodAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_insert_transaction, paymentMethod);
        categoriesNamesAdapter.setDropDownViewResource(R.layout.spinner_selector_insert_transaction);
        paymentMethodAdapter.setDropDownViewResource(R.layout.spinner_selector_insert_transaction);
        categoriesSpinner.setAdapter(categoriesNamesAdapter);
        paymentTypeSpinner.setAdapter(paymentMethodAdapter);
    }

    public void init() {
        setSpinnersAllignment();
        List<String> shopsList = new ArrayList<>(shopsSet);
        AutoCompleteTextView shopsAutoCompleteTextView = findViewById(R.id.shopAutoCompleteTextView);
        ArrayAdapter<String> anotherAdapter = new ArrayAdapter<>(this, R.layout.shops_spinner, shopsList);
        shopsAutoCompleteTextView.setAdapter(anotherAdapter);
        shopsAutoCompleteTextView.setThreshold(2);// Set auto complete from the first character
    }

    /**
     * Validates input and inserts a new transaction into the database.
     * @param refMonth the reference month for the transaction
     */
    public void insertTransaction(String refMonth) {
        if (refMonth == null || month == null) {
            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, this);
            btnSendTransaction.setEnabled(true);
            return;
        }
        
        EditText shopET = findViewById(R.id.shopAutoCompleteTextView);
        EditText payDateET = findViewById(R.id.payDatePlainText);
        EditText transactionPriceET = findViewById(R.id.transactionPricePlainText);
        
        // Validate inputs
        if (setErrorEditText(payDateET) || setErrorEditText(transactionPriceET)) {
            btnSendTransaction.setEnabled(true);
            return;
        }
        
        // Parse transaction data
        String categoryName = categoriesSpinner.getSelectedItem() != null 
            ? categoriesSpinner.getSelectedItem().toString() : "";
        String paymentMethod = paymentTypeSpinner.getSelectedItem() != null 
            ? paymentTypeSpinner.getSelectedItem().toString() : "";
        String shop = shopET.getText().toString().trim();
        
        // Parse price safely
        double transactionPrice;
        try {
            transactionPrice = Double.parseDouble(transactionPriceET.getText().toString().trim());
        } catch (NumberFormatException e) {
            transactionPriceET.setError(getString(R.string.requiredField));
            btnSendTransaction.setEnabled(true);
            return;
        }
        
        // Parse date
        Date payDate = DateUtil.convertStringToDate(payDateET.getText().toString(), Config.DATE_FORMAT);
        if (payDate == null) {
            payDateET.setError(getString(R.string.requiredField));
            btnSendTransaction.setEnabled(true);
            return;
        }
        
        // Get category
        Category category = dbUtil.getCategoryByName(month.getYearMonth(), categoryName);
        if (category == null) {
            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, this);
            btnSendTransaction.setEnabled(true);
            return;
        }
        
        String catId = category.getId();
        progressBar.setVisibility(View.VISIBLE);
        
        // Prepare transaction
        int idPerMonth = month.getTranIdNumerator() + 1;
        DatabaseReference transactionsNode = dbUtil.getDBTransactionsPath(month.getYearMonth(), catId);
        String tranId = transactionsNode.push().getKey();
        
        // Get comment (optional)
        String comment = commentEditText.getText().toString().trim();
        
        com.brosh.finance.monthlybudgetsync.objects.Transaction transaction = 
            new com.brosh.finance.monthlybudgetsync.objects.Transaction(
                tranId, idPerMonth, categoryName, paymentMethod, shop, payDate, transactionPrice);
        
        // Set comment if provided
        if (!comment.isEmpty()) {
            transaction.setComment(comment);
        }

        // Add shop to set
        if (!shop.isEmpty() && shopsSet != null) {
            shopsSet.add(shop);
            dbUtil.writeNewShopFB(shop);
        }
        
        // Execute Firebase transaction
        DatabaseReference monthDB = dbUtil.getDBMonthPath(refMonth);
        final Context context = this;
        
        monthDB.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                try {
                    Object trnNumeratorFB = mutableData.child(Definitions.TRAN_ID_NUMERATOR).getValue();
                    int newIdPerMonth = trnNumeratorFB != null 
                        ? Integer.parseInt(trnNumeratorFB.toString()) + 1 : 1;
                    
                    transaction.setIdPerMonth(newIdPerMonth);
                    mutableData.child(Definitions.TRAN_ID_NUMERATOR).setValue(newIdPerMonth);
                    mutableData.child(Definitions.BALANCE).setValue(category.getBalance());
                    mutableData.child(Definitions.CATEGORIES)
                        .child(category.getId())
                        .child(Definitions.TRANSACTIONS)
                        .child(tranId).setValue(transaction);
                    
                    return Transaction.success(mutableData);
                } catch (Exception e) {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed,
                                   @Nullable DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                
                if (databaseError != null || !committed) {
                    TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_LONG, context);
                    btnSendTransaction.setEnabled(true);
                } else {
                    TextUtil.showMessage(getString(R.string.transaction_inserted_successfully), 
                        Toast.LENGTH_LONG, context);
                    finish();
                }
            }
        });
    }

    /**
     * Validates that an EditText is not empty.
     * Uses ValidationUtil for consistent validation.
     * 
     * @param editText the EditText to validate
     * @return true if empty (has error), false if valid
     */
    public boolean setErrorEditText(EditText editText) {
        return !ValidationUtil.validateNotEmpty(editText, getString(R.string.requiredField));
    }

    private List<String> getPaymentMethodList() {
        List<String> paymentMethodList = new ArrayList<>();
        paymentMethodList.add(getString(R.string.credit_card));
        paymentMethodList.add(getString(R.string.cash));
        paymentMethodList.add(getString(R.string.chek));
        paymentMethodList.add(getString(R.string.bank_wired));

        return paymentMethodList;
    }
}

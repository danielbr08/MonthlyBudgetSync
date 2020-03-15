package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.config.Language;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.google.firebase.database.DatabaseReference;

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

    private Language language;// todo init those fieds using put extra
    private DBService dbService;
    private String userKey;
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
        ArrayAdapter<String> adapter;
        List<String> categoriesNames = dbService.getCategoriesNames(month.getYearMonth());
        List<String> paymentMethod = getPaymentMethodList();
        if (language.isLTR()) {
            //global.setCatArrayHebNames();
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner_eng, categoriesNames);
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categoriesSpinner.setAdapter(adapter);

            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner_eng, paymentMethod);
            paymentTypeSpinner.setAdapter(adapter);
        } else if (!language.isLTR()) {
            //global.setCatArrayHebNames();
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner, categoriesNames);
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categoriesSpinner.setAdapter(adapter);

            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner, paymentMethod);
            paymentTypeSpinner.setAdapter(adapter);
        }
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

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setButtonsNames() {
        // Main window buttons
        ((TextView) findViewById(R.id.chooseCategoryLabel)).setText(TextService.getSentenceCapitalLetter(language.categoryName, '.'));
        ((TextView) findViewById(R.id.choosePaymenMethodtLabel)).setText(TextService.getSentenceCapitalLetter(language.paymentMethodName, '.'));
        ((TextView) findViewById(R.id.shopLabel)).setText(TextService.getSentenceCapitalLetter(language.shopName, '.'));
        ((TextView) findViewById(R.id.payDateLabel)).setText(TextService.getSentenceCapitalLetter(language.chargeDateName, '.'));
        ((TextView) findViewById(R.id.transactionPriceLabel)).setText(TextService.getSentenceCapitalLetter(language.transactionPrice, '.'));
        ((TextView) findViewById(R.id.sendTransactionButton)).setText(TextService.getSentenceCapitalLetter(language.insert, '.'));
        ((TextView) findViewById(R.id.inserTransactionLabel)).setText(TextService.getSentenceCapitalLetter(language.insertTransactionButtonName, '.'));

        if (language.isLTR()) {
            LinearLayout llMain = (LinearLayout) findViewById(R.id.LLMainInsertTranssaction);
            for (int i = 1; i < llMain.getChildCount() - 1; i++) {
                LinearLayout currentLL = (LinearLayout) llMain.getChildAt(i);
                //llMain.removeViewAt(i);
                View v1 = currentLL.getChildAt(0);
                View v2 = currentLL.getChildAt(1);
                int v1ID = v1.getId(), v2ID = v2.getId();
                currentLL.removeAllViews();
                currentLL.addView(v2);
                currentLL.addView(v1);
                //llMain.addView(currentLL,i);
            }
            TextView categoryTV = ((TextView) findViewById(R.id.chooseCategoryLabel));
            TextView paymentmethodTV = ((TextView) findViewById(R.id.choosePaymenMethodtLabel));
            TextView shopTV = ((TextView) findViewById(R.id.shopLabel));
            TextView payDateTV = ((TextView) findViewById(R.id.payDateLabel));
            TextView priceTV = ((TextView) findViewById(R.id.transactionPriceLabel));
            TextView sendTransactionTV = ((TextView) findViewById(R.id.sendTransactionButton));

            Spinner categorySpinner = ((Spinner) findViewById(R.id.categorySpinner));
            Spinner paymentMethodSpinner = ((Spinner) findViewById(R.id.paymentMethodSpinner));
            TextView shopET = ((TextView) findViewById(R.id.shopAutoCompleteTextView));
            TextView payDateET = ((TextView) findViewById(R.id.payDatePlainText));
            TextView priceET = ((TextView) findViewById(R.id.transactionPricePlainText));

            ArrayAdapter<String> adapter;
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner_eng, dbService.getCategoriesNames(month.getYearMonth()));
            adapter.setDropDownViewResource(R.layout.custom_spinner_eng);

            categorySpinner.setAdapter(adapter);
/*            categoryTV.setTextDirection(View.TEXT_DIRECTION_LTR);
            paymentmethodTV.setTextDirection(View.TEXT_DIRECTION_LTR);
            shopTV.setTextDirection(View.TEXT_DIRECTION_LTR);
            payDateTV.setTextDirection(View.TEXT_DIRECTION_LTR);
            priceTV.setTextDirection(View.TEXT_DIRECTION_LTR);
            sendTransactionTV.setTextDirection(View.TEXT_DIRECTION_LTR);*/

            categorySpinner.setTextDirection(View.LAYOUT_DIRECTION_LTR);
            paymentMethodSpinner.setTextDirection(View.TEXT_DIRECTION_LTR);
            shopET.setTextDirection(View.TEXT_DIRECTION_LTR);
            payDateET.setTextDirection(View.TEXT_DIRECTION_LTR);
            priceET.setTextDirection(View.TEXT_DIRECTION_LTR);

            categoryTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            paymentmethodTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            shopTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            payDateTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            priceTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            //sendTransactionTV.setTextDirection(View.TEXT_DIRECTION_LTR);
            //TextView balanceTV = ((TextView) findViewById(R.id.inserTransactionLabel));

            //categorySpinner.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            //paymentMethodSpinner.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            shopET.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            payDateET.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            priceET.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }

//        if(month != null)
//            setTitle(getYearMonth(month.getMonth(), '.'));
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_transaction);

        Bundle extras = getIntent().getExtras();
        String selectedLanguage = extras.getString(getString(R.string.language), getString(R.string.english));
        String refMonth = extras.getString(getString(R.string.month), null);
        language = new Language(selectedLanguage);
        userKey = extras.getString(getString(R.string.user), getString(R.string.empty));
        dbService = (DBService) getIntent().getSerializableExtra(getString(R.string.db_service));
        month = dbService.getMonth(refMonth);

        setButtonsNames();
        shopsSet = dbService.getShopsSet();
//        setTitle( getYearMonth(month.getMonth(),'.'));
        categoriesSpinner = (Spinner) findViewById(R.id.categorySpinner);
        paymentTypeSpinner = (Spinner) findViewById(R.id.paymentMethodSpinner);
        btnSendTransaction = (Button) findViewById(R.id.sendTransactionButton);
        //btnClose = (Button) findViewById(R.id.closeInsertTransactionButton);
        init();

        payDateEditText = (EditText) findViewById(R.id.payDatePlainText);
        payDateEditText.setText(getCurrentDate());

        //payDateEditText.setEnabled(false);

        payDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
                mDatePicker.setTitle(language.selectingDate);
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
                EditText shopET = ((EditText) findViewById(R.id.shopAutoCompleteTextView));
                EditText payDateET = ((EditText) findViewById(R.id.payDatePlainText));
                EditText transactionPriceET = ((EditText) findViewById(R.id.transactionPricePlainText));
                if (setErrorEditText(payDateET) || setErrorEditText(transactionPriceET))
                    return;
                //Insert data
                String categoryName = categoriesSpinner.getSelectedItem().toString();

                String paymentMethod = paymentTypeSpinner.getSelectedItem().toString();
                //String category = getCategoryName(categoryHeb);
                String shop = shopET.getText().toString();
                Date payDate = DateService.convertStringToDate(payDateET.getText().toString(), Config.DATE_FORMAT);
                double transactionPrice = Double.valueOf(transactionPriceET.getText().toString());
                int idPerMonth = dbService.getMaxIdPerMonth(month.getYearMonth(), categoryName) + 1;
                //TRAN_ID_PER_MONTH_NUMERATOR = idPerMonth;
                //init on create
                //int idPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth()) + 1;
                //long transID = monthlyBudgetDB.getMaxIDTRN() + 1;
                Transaction transaction = new Transaction(idPerMonth, categoryName, paymentMethod, shop, payDate, transactionPrice);
                boolean isStorno = false;
                int stornoOf = -1;

                for (Category cat : month.getCategories().values()) {
                    if (categoryName.equals(cat.getName())) {
                        cat.withdrawal(transactionPrice);
                        List<Transaction> catTrans = dbService.getTransactions(month.getYearMonth(), categoryName);
                        for (Transaction tran : catTrans) {
                            isStorno = tran.isStorno(transaction);
                            if (isStorno == true) {
                                stornoOf = tran.getIdPerMonth();
                                tran.setStorno(true);
                                tran.setStornoOf(transaction.getIdPerMonth());
                                break;
                            }
                        }
                        transaction.setStorno(isStorno);
                        transaction.setStornoOf(stornoOf);
                        cat.addTransaction(transaction);
                        break;
                    }
                }
                String catId = dbService.getCategoryByName(month.getYearMonth(), categoryName).getId();
                DatabaseReference transactionsNode = dbService.getDBUserTransactionsPath(month.getYearMonth(), catId);
                String tranId = transactionsNode.push().getKey();
                transaction.setId(tranId);
                transactionsNode.child(catId).setValue(transaction); // todo check if event listener call
                shopsSet.add(shop); // todo check if event listener call

                // send message and close window
                //showMessage("העסקה הוכנסה בהצלחה!");
                showMessageNoButton(language.transactionInsertedSuccessfully);

                //finish();
            }
        });
    }

    public void showMessage(String message)//View view)
    {
        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage(message).setPositiveButton(language.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        }).setTitle(language.messageName)
                .create();
        myAlert.show();
    }

    public boolean setErrorEditText(EditText et) {
        if (et.length() == 0) {
            et.setError(language.requiredField);
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
        paymentMethodList.add(language.creditCardName);
        paymentMethodList.add(language.cashName);
        paymentMethodList.add(language.checkName);
        paymentMethodList.add(language.bankTransferName);

        return paymentMethodList;
    }
}

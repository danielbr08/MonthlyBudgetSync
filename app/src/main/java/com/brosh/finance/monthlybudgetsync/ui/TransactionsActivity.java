package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.services.ComparatorService;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.config.Language;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.brosh.finance.monthlybudgetsync.services.UIService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    LinearLayout ll;
    Spinner categoriesSpinner;
    List<TextView[]> textViews = new ArrayList<TextView[]>();

    int widthDisplay;

    //todo get thos fields from caller intent
    private Month month;
    private Language language;
    private DBService dbService;
    private String userKey;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        Bundle extras = getIntent().getExtras();
        String selectedLanguage = extras.getString(getString(R.string.language), getString(R.string.english));
        String refMonth = extras.getString(getString(R.string.month), null);
        language = new Language(selectedLanguage);
        userKey = extras.getString(getString(R.string.user), getString(R.string.empty));
        dbService = (DBService) getIntent().getSerializableExtra(getString(R.string.db_service));
        month = dbService.getMonth(refMonth);
        setButtonsNames();
//        setTitle( getYearMonth(month.getMonth(),'.'));
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//Rotate the screen to to be on landspace moade only

        if (language.isLTR())
            setLanguageConf();//send ll parameter to static method like this method

        textViews.clear();
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthDisplay = dm.widthPixels;

        ll = (LinearLayout) findViewById(R.id.LLTransactions);
        categoriesSpinner = (Spinner) findViewById(R.id.categorySpinnerTransactions);
        setSpinnersAllignment();

        addCategoryRow(null, language.IDName, language.categoryName, language.paymentMethodName, language.shopName, language.chargeDateName, language.sumName, true, true);

        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String categoryName = categoriesSpinner.getSelectedItem().toString();
                //clearLayout();
                clearRowsExeptHaeder();
                //addCategoryRow(null, language.IDName,language.categoryName, language.paymentMethodName, language.shopName, language.chargeDateName, language.sumName,true, true);
                setTextViewsHeader();

                LinearLayout llTitleTrans = ((LinearLayout) ll.getChildAt(0));

                // Default is Hebrew
                TextView IDTV = (TextView) llTitleTrans.getChildAt(0);
                TextView catNameTV = (TextView) llTitleTrans.getChildAt(1);
                TextView shopTV = (TextView) llTitleTrans.getChildAt(2);
                TextView tranDateTV = (TextView) llTitleTrans.getChildAt(3);
                TextView paymentMethodTV = (TextView) llTitleTrans.getChildAt(4);
                TextView tranPriceTV = (TextView) llTitleTrans.getChildAt(5);

                // English case
                if (language.isLTR()) {
                    IDTV = (TextView) llTitleTrans.getChildAt(5);
                    catNameTV = (TextView) llTitleTrans.getChildAt(4);
                    shopTV = (TextView) llTitleTrans.getChildAt(3);
                    tranDateTV = (TextView) llTitleTrans.getChildAt(2);
                    paymentMethodTV = (TextView) llTitleTrans.getChildAt(1);
                    tranPriceTV = (TextView) llTitleTrans.getChildAt(0);
                }
                //Except category
                if (categoryName != language.all) {
                    LinearLayout headerRowLL = (LinearLayout) ll.getChildAt(0);
                    catNameTV.setVisibility(View.GONE);
                    setTVLayoutParams(IDTV, widthDisplay * 16 / 100);
                    setTVLayoutParams(shopTV, widthDisplay * 19 / 100);
                    setTVLayoutParams(tranDateTV, widthDisplay * 23 / 100);
                    setTVLayoutParams(paymentMethodTV, widthDisplay * 24 / 100);//replace 20 in 25
                    setTVLayoutParams(tranPriceTV, widthDisplay * 18 / 100);
                }
                // addCategoryRow(null, "מזהה" ,"קטגוריה","חנות", "תאריך", "סכום",true, true);
                else {
                    catNameTV.setVisibility(View.VISIBLE);
                    setTVLayoutParams(IDTV, widthDisplay * 12 / 100);
                    setTVLayoutParams(catNameTV, widthDisplay * 20 / 100);
                    setTVLayoutParams(shopTV, widthDisplay * 15 / 100);
                    setTVLayoutParams(tranDateTV, widthDisplay * 19 / 100);
                    setTVLayoutParams(paymentMethodTV, widthDisplay * 20 / 100);//replac e 20 in 25
                    setTVLayoutParams(tranPriceTV, widthDisplay * 14 / 100);
                }
                //Not include category
                // addCategoryRow(null, "מזהה" ,"categoryName","חנות", "תאריך", "סכום",false, true);
                setTransactionsInGui(categoryName, language.IDName, Definition.ARROW_UP);

                if (ll.getChildCount() == 1)//if no transactions and only header in layout
                {
                    TextView messageTV = new TextView(TransactionsActivity.this);
                    messageTV.setText(language.noTransactionsExists);
                    messageTV.setTextSize(24);
                    messageTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    //messageTV.setLayoutParams(paramsTV);
                    ll.addView(messageTV);
                }
                //setCloseButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        init();
        //addCategoryRow(null, language.IDName,language.categoryName, language.paymentMethodName, language.shopName, language.chargeDateName, language.sumName,true, true);
        //textViews.clear();
        //setTransactionsInGui(language.all, language.IDName, UP_ARROW);
        //setCloseButton();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthDisplay = dm.widthPixels;
/*        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            DisplayMetrics dm = new DisplayMetrics();
            this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
            widthDisplay = dm.widthPixels;
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {

        }*/
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

    public void init() {
        //global.setCatArrayHebNames();
        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<String> spinnerCategories = new ArrayList<String>(dbService.getCategoriesNames(currentRefMonth));
        spinnerCategories.add(0, language.all);

        setSpinnersAllignment();

    }

    public int getIndexRowById(long ID) {
        for (int i = 0; i < textViews.size(); i++) {
            if (textViews.get(i)[0].getText().toString().equals(String.valueOf(ID)))
                return i;
        }
        return -1;
    }

    public TextView[] getStornoRow(String catName, Transaction transaction) {
        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<Transaction> categoryTrans = dbService.getTransactions(currentRefMonth, catName);
        if (categoryTrans == null || transaction == null)
            return null;
        for (Transaction tran : categoryTrans) {
            if (tran == transaction)
                continue;
            if (transaction.getIsStorno() == false)
                continue;
            int i = getIndexRowById(transaction.getIdPerMonth());
            if (i == -1)
                return null;
            return textViews.get(i);
        }
        return null;
    }

    public void setTVLayoutParams(TextView TV, int width) {
        LinearLayout.LayoutParams paramsTV = (new LinearLayout.LayoutParams(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TV.setLayoutParams(paramsTV);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setLanguageProperties(TextView[] row, boolean isHeaderLine) {
        int i = 0;
        if (language.isLTR()) {
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_LTR);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_LTR);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_LTR);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_LTR);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_LTR);
            ((TextView) row[i]).setTextDirection(View.TEXT_DIRECTION_RTL);

            i = 0;
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        } else {
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_RTL);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_RTL);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_RTL);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_RTL);
            ((TextView) row[i++]).setTextDirection(View.TEXT_DIRECTION_RTL);
            ((TextView) row[i]).setTextDirection(View.TEXT_DIRECTION_LTR);

            i = 0;
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i++]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ((TextView) row[i]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }

/*        if(isHeaderLine)
        {
            if(language.language.equals("EN"))
            {
                ((TextView) row[i]).setTextDirection(View.TEXT_DIRECTION_LTR);
                ((TextView) row[i]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
            else if(language.language.equals("HEB"))
            {
                ((TextView) row[i]).setTextDirection(View.TEXT_DIRECTION_RTL);
                ((TextView) row[i]).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        }*/
    }

    public void setTextViewsLength(TextView[] row, boolean isIncludeCategory) {
        if (!language.isLTR()) {
            int i = 0;
            if (isIncludeCategory) {
                setTVLayoutParams(row[i++], widthDisplay * 12 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 20 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 15 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 19 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 20 / 100);//replace 20 in 25
                setTVLayoutParams(row[i++], widthDisplay * 14 / 100);
            } else {
                setTVLayoutParams(row[i++], widthDisplay * 16 / 100);
                i++;
                setTVLayoutParams(row[i++], widthDisplay * 19 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 23 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 24 / 100);//replace 20 in 25
                setTVLayoutParams(row[i++], widthDisplay * 18 / 100);
            }
        } else {
            int i = 0;
            if (isIncludeCategory) {
                setTVLayoutParams(row[i++], widthDisplay * 12 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 20 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 15 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 19 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 20 / 100);//replace 20 in 25
                setTVLayoutParams(row[i++], widthDisplay * 14 / 100);
            } else {
                setTVLayoutParams(row[i++], widthDisplay * 16 / 100);
                i++;
                setTVLayoutParams(row[i++], widthDisplay * 19 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 23 / 100);
                setTVLayoutParams(row[i++], widthDisplay * 24 / 100);//replace 20 in 25
                setTVLayoutParams(row[i++], widthDisplay * 18 / 100);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addCategoryRow(Transaction tran, String ID, String categoryName, String paymentMethod, String shop, String tranDate, String tranPrice, boolean isIncludeCategory, boolean isHeaderLine) {
        TextView IDTV = new TextView(TransactionsActivity.this);
        TextView catNameTV = new TextView(TransactionsActivity.this);
        TextView paymentMethodTV = new TextView(TransactionsActivity.this);
        TextView shopTV = new TextView(TransactionsActivity.this);
        TextView tranDateTV = new TextView(TransactionsActivity.this);
        TextView tranPriceTV = new TextView(TransactionsActivity.this);

        TextView[] row = new TextView[6];
        int index = 0;
        row[index++] = IDTV;
        row[index++] = catNameTV;
        row[index++] = shopTV;
        row[index++] = tranDateTV;
        row[index++] = paymentMethodTV;
        row[index] = tranPriceTV;

        setLanguageProperties(row, isHeaderLine);
        textViews.add(row);

        if (isIncludeCategory == true)
            catNameTV.setText(categoryName);
        IDTV.setText(ID);
        shopTV.setText(shop);
        paymentMethodTV.setText(paymentMethod);
        tranDateTV.setText(String.valueOf(tranDate));
        tranPriceTV.setText(String.valueOf(tranPrice));

        LinearLayout newll = new LinearLayout(TransactionsActivity.this);

        TableLayout.LayoutParams paramsTV = (new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        newll.setOrientation(LinearLayout.HORIZONTAL);

        setTextViewsLength(row, isIncludeCategory);

        IDTV.setTextSize(11);
        catNameTV.setTextSize(11);
        paymentMethodTV.setTextSize(11);
        shopTV.setTextSize(11);
        tranDateTV.setTextSize(11);
        tranPriceTV.setTextSize(11);

        if (ID == language.totalName) {
            IDTV.setTypeface(null, Typeface.BOLD);
            IDTV.setTextSize(11);
            IDTV.setTextColor(Color.BLACK);
            tranPriceTV.setTypeface(null, Typeface.BOLD);
            tranPriceTV.setTextSize(11);
            tranPriceTV.setTextColor(Color.BLACK);
        }

        if (isHeaderLine) {
            final LinearLayout titleLL = new LinearLayout(TransactionsActivity.this);
            List<TextView> titlesTV = new ArrayList<>(Arrays.asList(IDTV, catNameTV, paymentMethodTV, shopTV, tranDateTV, tranPriceTV));
            UIService.setTitleStyle(titlesTV);//, titleLL);
        }

        newll.setLayoutParams(paramsTV);

        if (!isHeaderLine) {
            TextView[] stornoRow = getStornoRow(categoryName, tran);
            Boolean isStornoRow = (stornoRow != null);
            if (isStornoRow == true) {
                ArrayList<TextView> tvRow = new ArrayList<>(Arrays.asList(IDTV, catNameTV, paymentMethodTV, shopTV, tranDateTV, tranPriceTV));
                UIService.setRowStrikeThroughStyle(tvRow);
            }
        }

        newll.addView(IDTV);
        if (isIncludeCategory == true)
            newll.addView(catNameTV);

        newll.addView(shopTV);
        newll.addView(tranDateTV);
        newll.addView(paymentMethodTV);
        newll.addView(tranPriceTV);

        if (language.isLTR())
            UIService.reverseLinearLayout(newll);
        ll.addView(newll);

        if (isHeaderLine == true)
            setOnClickTextViews();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTransactionsInGui(String catName, String sortBy, char ascOrDesc) {
        ((TextView) findViewById(R.id.textViewTotalTransactions)).setText(String.valueOf(0));
        Boolean isIncludeCategory = false;
        if (catName.equals(language.all))
            isIncludeCategory = true;
        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<Transaction> transactions = dbService.getTransactions(currentRefMonth, catName);
        if (transactions == null || transactions.size() == 0)
            return;
        if (sortBy != null)
            ComparatorService.sort(transactions, sortBy, ascOrDesc);
        double tranSum = 0;
        for (Transaction tran : transactions) {
            long ID = tran.getIdPerMonth();
            String categoryName = tran.getCategory();
            String paymentMethod = tran.getPaymentMethod();
            String shop = tran.getShop();
            Date payDate = tran.getPayDate();
            double transactionPrice = tran.getPrice();
            tranSum += transactionPrice;
            transactionPrice = Math.round(transactionPrice * 100.d) / 100.0d;
            addCategoryRow(tran, String.valueOf(ID), categoryName, paymentMethod, shop, DateService.convertDateToString(payDate, Config.DATE_FORMAT), String.valueOf(transactionPrice), isIncludeCategory, false);
        }
        if (transactions.size() > 0) {
            tranSum = Math.round(tranSum * 100.d) / 100.0d;
            addCategoryRow(null, language.totalName, "", "", "", "", String.valueOf(tranSum), isIncludeCategory, false);
            ((TextView) findViewById(R.id.textViewTotalTransactions)).setText(String.valueOf(tranSum));
        }
    }

    public void setCloseButton() {
        final Button myButton = new Button(this);
        myButton.setText(language.close);
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Rotate the screen to to be on PORTRAIT moade only
                finish();
            }
        });

        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout newll = new LinearLayout(TransactionsActivity.this);

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(myButton, lp);
        ll.addView(newll);
    }

/*    public void setCloseButton()
    {
        final Button myButton = (Button) findViewById( R.id.categoryCloseButtonTransactions);
        myButton.setText("סגור");
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        //TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        //ll.addView(myButton, lp);
    }*/


    public void setSpinnersAllignment() {
        ArrayAdapter<String> adapter;
        String currentRefMonth = DateService.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<String> allCategories = dbService.getCategoriesNames(currentRefMonth);
        allCategories.add(0, language.all);
        if (language.isLTR()) {
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner_eng, allCategories);
            categoriesSpinner.setAdapter(adapter);
        } else if (!language.isLTR()) {
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner, allCategories);
            categoriesSpinner.setAdapter(adapter);
        }
    }

    public void setButtonsNames() {
        // Transactions window buttons
        ((TextView) findViewById(R.id.textViewTotalTransactionsLabel)).setText(language.totalName);
        ((TextView) findViewById(R.id.transactionsTitleLabel)).setText(language.transactionsName);
//        if (month != null)
//            setTitle(DateService.getYearMonth(month.getRefMonth(), Config.DATE_FORMAT_CHARACTER));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setLanguageConf() {
        LinearLayout firstLL = (LinearLayout) findViewById(R.id.Cat_TotalLL);
        for (int i = 0; i < firstLL.getChildCount(); i++) {
            View v = firstLL.getChildAt(i);
            v.setTextDirection(View.TEXT_DIRECTION_LTR);
            v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            if (i == 3) {
                v.setTextDirection(View.TEXT_DIRECTION_RTL);
                v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        }
        UIService.reverseLinearLayout(firstLL);
    }

    public void clearLayout() {
        if (ll.getChildCount() > 0) {
            ll.removeAllViews();
            ll.invalidate();
        }
    }

/*    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setHeaderProperties(TextView tv)
    {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(14);
        tv.setClickable(true);
        Linkify.addLinks(tv,Linkify.ALL);
    }*/

    public void setTextViewsHeader() {
        LinearLayout rowLL = (LinearLayout) ll.getChildAt(0);//textViews.get(0);
        ((TextView) rowLL.getChildAt(0)).setText(language.IDName);
        ((TextView) rowLL.getChildAt(1)).setText(language.categoryName);
        ((TextView) rowLL.getChildAt(2)).setText(language.shopName);
        ((TextView) rowLL.getChildAt(3)).setText(language.chargeDateName);
        ((TextView) rowLL.getChildAt(4)).setText(language.paymentMethodName);
        ((TextView) rowLL.getChildAt(5)).setText(language.sumName);
        if (!language.isLTR()) {
            ((TextView) rowLL.getChildAt(0)).setText(language.IDName);
            ((TextView) rowLL.getChildAt(1)).setText(language.categoryName);
            ((TextView) rowLL.getChildAt(2)).setText(language.shopName);
            ((TextView) rowLL.getChildAt(3)).setText(language.chargeDateName);
            ((TextView) rowLL.getChildAt(4)).setText(language.paymentMethodName);
            ((TextView) rowLL.getChildAt(5)).setText(language.sumName);
        } else {
            //ViewCompat.setLayoutDirection(rowLL.getChildAt(0), View.LAYOUT_DIRECTION_LTR);
            ((TextView) rowLL.getChildAt(0)).setText(TextService.getSentenceCapitalLetter(language.sumName, '.'));
            ((TextView) rowLL.getChildAt(1)).setText(TextService.getSentenceCapitalLetter(language.paymentMethodName, '.'));
            ((TextView) rowLL.getChildAt(2)).setText(TextService.getSentenceCapitalLetter(language.chargeDateName, '.'));
            ((TextView) rowLL.getChildAt(3)).setText(TextService.getSentenceCapitalLetter(language.shopName, '.'));
            ((TextView) rowLL.getChildAt(4)).setText(TextService.getSentenceCapitalLetter(language.categoryName, '.'));
            ((TextView) rowLL.getChildAt(5)).setText(TextService.getSentenceCapitalLetter(language.IDName, '.'));
        }

        for (int i = 0; i < 6; i++)
            ((TextView) rowLL.getChildAt(i)).setTextColor(Color.BLACK);

/*        for (int i = 0; i < 3; i++) {
            TextView leftTV = (TextView)rowLL.getChildAt(i);
            TextView rightTV = (TextView)rowLL.getChildAt(5-i);
            rowLL.removeViewAt(i);
            rowLL.removeViewAt(5-i-1);
            rowLL.addView(rightTV,i);
            rowLL.addView(leftTV,5-i);
        }*/
    }

    public void setOnClickTextViews() {
        for (int i = 0; i < 6; i++) {
            final int j = i;
            LinearLayout rowLL = (LinearLayout) ll.getChildAt(0);
            ((TextView) rowLL.getChildAt(j)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onClick(View view) {
                    LinearLayout rowLL2 = (LinearLayout) ll.getChildAt(0);
                    TextView headerTV = ((TextView) rowLL2.getChildAt(j));
                    String allText = headerTV.getText().toString();
                    char ascOrDesc = allText.charAt(allText.length() - 1);
                    String text = "";
                    if (ascOrDesc != Definition.ARROW_UP && ascOrDesc != Definition.ARROW_DOWN) {
                        ascOrDesc = 'X';
                        text = allText;
                    } else
                        text = new String(allText.substring(0, allText.length() - 1));

                    setTextViewsHeader();
                    switch (ascOrDesc) {
                        case ('ꜜ'): {
                            //sort(text,upArrow);
                            ascOrDesc = Definition.ARROW_UP;
                            headerTV.setText(text + String.valueOf(Definition.ARROW_UP));
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        case ('ꜛ'): {
                            //sort(text,downArrow);
                            ascOrDesc = Definition.ARROW_DOWN;
                            headerTV.setText(text + String.valueOf(Definition.ARROW_DOWN));
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        default: {
                            //sort(text,upArrow);
                            headerTV.setText(text + String.valueOf(Definition.ARROW_UP));
                            headerTV.setTextColor(Color.RED);
                            ascOrDesc = Definition.ARROW_UP;
                            break;
                        }
                    }
                    clearRowsExeptHaeder();
                    //addCategoryRow(null, language.IDName,language.categoryName, language.paymentMethodName, language.shopName, language.chargeDateName, language.sumName,true, true);
                    setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), text, ascOrDesc);
                    //setCloseButton();
                }
            });
        }
    }

    public void clearRowsExeptHaeder() {
        for (int i = ll.getChildCount() - 1; i > 0; i--)
            ll.removeViewAt(i);
    }
}

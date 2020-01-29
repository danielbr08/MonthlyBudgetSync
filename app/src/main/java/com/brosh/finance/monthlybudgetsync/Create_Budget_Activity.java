package com.brosh.finance.monthlybudgetsync;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//import android.support.annotation.RequiresApi;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
//import com.brosh.finance.monthlybudgetsync.services.NetworkService;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.brosh.finance.monthlybudgetsync.services.UIService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Create_Budget_Activity extends AppCompatActivity {

    private UIService uiService;
    private TextService textService;
    private DateService dateService;
    private DBService dbService;

    DatabaseReference DatabaseReferenceUserMonthlyBudget;
    private Month month;
    private Language language;

    TextView emptyTV, categoryNameTV, categoryValueTV, constPaymentTV, shopTV, payDateTV;


    AlertDialog.Builder myAlert;
    private ArrayList<Budget> allBudgets;
    private ArrayList<String> allCategories;
    private boolean isInputValid;
    private Display display;
    private int screenWidth;
    private int buttonSize = 120;

    private Drawable dfaultBackground;

    private LinearLayout LLMain;
    //Button to add a row
    private Button addCategoryButton;
    //Button to write all the inserted categories to budget file
    private Button OKButton;
    private LinearLayout newll;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userKey = getIntent().getExtras().getString(getString(R.string.user), "");
        dbService = (DBService) getIntent().getSerializableExtra(getString(R.string.db_service));
        month = (Month) getIntent().getSerializableExtra(getString(R.string.month));

        dateService = new DateService();

        DatabaseReferenceUserMonthlyBudget = FirebaseDatabase.getInstance().getReference(getString(R.string.monthly_budget)).child(userKey);
        setContentView(R.layout.activity_create_budget);
        LLMain = (LinearLayout) findViewById(R.id.LLMainCreateBudget);
        allBudgets = new ArrayList<>();
        allCategories = new ArrayList<>();
        String choosenLanguage = getIntent().getExtras().getString(getString(R.string.language), getString(R.string.hebrew));
        language = new Language(choosenLanguage);
        setTitle(language.appName);
        dfaultBackground = new View(this).getBackground();
        setButtonsNames();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Rotate the screen to to be on portrait moade only
        display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        addCategoryButton = new Button(Create_Budget_Activity.this);
        OKButton = new Button(Create_Budget_Activity.this);
        newll = new LinearLayout(Create_Budget_Activity.this);

        //setAddButton();
        setTitleRow();
        setBudgetGui();

        if (language.isEn()) {
/*            for (int i=1;i<LLMain.getChildCount() - 1;i++)
            {
                //LinearLayout currentLL = (LinearLayout) LLMain.getChildAt(i);
                //for (int j = 0; j < currentLL.getChildCount(); j++)

                setLanguageConf((LinearLayout) LLMain.getChildAt(i));
            }*/
            int lastRowIndex = LLMain.getChildCount() - 2;
            uiService.setLanguageConf((LinearLayout) LLMain.getChildAt(1));
            //setLanguageConf((LinearLayout) LLMain.getChildAt(lastRowIndex));
        }
    }


//    public void setSpinnerAllignment(Spinner spinner)
//    {
//        ArrayAdapter<String> adapter;
//        if(language.isEn())
//        {
//            adapter = new ArrayAdapter<String>(this,
//                    R.layout.custom_spinner_eng, month.getCategoriesNames());
//            spinner.setAdapter(adapter);
//        }
//        else if(language.isHeb())
//        {
//            adapter = new ArrayAdapter<String>(this,
//                    R.layout.custom_spinner, month.getCategoriesNames());
//            spinner.setAdapter(adapter);
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void addRow(LinearLayout ll, boolean isWithCloseBtn) {
        LLMain = (LinearLayout) findViewById(R.id.LLMainCreateBudget);
        if (LLMain.getChildCount() > 2) {
            LLMain.removeViewAt(LLMain.getChildCount() - 1);// Remove Close button
            LLMain.removeViewAt(LLMain.getChildCount() - 1);// Remove add button
        }
        LLMain.addView(ll);
        setAddAndDeleteButton();// Adding add button
        LinearLayout addButtonRowLL = (LinearLayout) LLMain.getChildAt(LLMain.getChildCount() - 1);
        if (isWithCloseBtn)
            setCloseButton();// Adding close button
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setAddAndDeleteButton() {
        final LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);

        final ImageButton addRowButton = new ImageButton(this);
        final ImageButton deleteRowsButton = new ImageButton(this);
        final TextView emptyTV = new TextView(this);

        //Set default background color
        addRowButton.setBackgroundDrawable(dfaultBackground);
        deleteRowsButton.setBackgroundDrawable(dfaultBackground);

        //addRowButton.setTooltipText("הוסף שורה");
        //deleteRowsButton.setTooltipText("נקה");

        addRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isEmptyRowExists = false;
                for (int i = 2; i < LLMain.getChildCount() - 2; i++) {
                    LinearLayout rowLL = (LinearLayout) LLMain.getChildAt(i);
                    if (language.isEn())
                        uiService.reverseLinearLayout(rowLL);
                    EditText categoryNameET = (EditText) rowLL.getChildAt(1);
                    EditText categoryValueET = (EditText) rowLL.getChildAt(2);
                    if (language.isEn())
                        uiService.reverseLinearLayout(rowLL);
                    if (categoryNameET.getText().toString().equals( getString(R.string.empty)) ||
                            categoryValueET.getText().toString().equals( getString(R.string.empty))) {
                        isEmptyRowExists = true;
                        break;
                    }
                }
                if (!isEmptyRowExists)
                    add_New_row(null, 0, false, null, 0);
            }
        });

        deleteRowsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LLMain.removeViews(2, LLMain.getChildCount() - 2);
/*                for (int i = 1; i < LLMain.getChildCount() - 2; i++)
                {
                    deleteSpecificRow(i);
                }*/
                add_New_row(null, 0, false, null, 0);
            }
        });
        deleteRowsButton.setImageDrawable(getResources().getDrawable(R.drawable.clean_screen));
        deleteRowsButton.setScaleType(ImageView.ScaleType.FIT_XY);

        addRowButton.setImageDrawable(getResources().getDrawable(R.drawable.add_button_md));
        //addRowButton.setBackground(null);
        //addRowButton.getLayoutParams().width = 40;
        //addRowButton.getLayoutParams().height = 40;
        addRowButton.setScaleType(ImageView.ScaleType.FIT_XY);


        addRowButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        deleteRowsButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        addRowButton.setAdjustViewBounds(true);
        deleteRowsButton.setAdjustViewBounds(true);

        int a = screenWidth - 2 * buttonSize;
        emptyTV.setLayoutParams(new LinearLayout.LayoutParams(screenWidth - 2 * buttonSize, buttonSize));
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(40, 40);

        //addRowButton.setPadding();
        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(addRowButton);//,lp);
        newll.addView(deleteRowsButton);
        newll.addView(emptyTV);
        if (language.isEn())
            uiService.setLanguageConf(newll);
        LLMain.addView(newll);
    }

    public void setCloseButton() {
        final Button closeButton = new Button(this);
        int size = (150 * buttonSize) / 100;
        closeButton.setHeight(size);
        closeButton.setText(language.createButton);
        closeButton.setTextColor(Color.BLACK);
        closeButton.setTypeface(null, Typeface.BOLD);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View view) {
                setBudgets();
                int budgetNumber = dbService.getMaxBudgetNumber();
                ArrayList<Budget> newBudgets = getAddedBudgets(budgetNumber);
                boolean isOriginContentBudgetChanged = isOriginBudgetChanged(budgetNumber);
                boolean isBudgetChange = isBudgetChange(budgetNumber);
                boolean isAddedBudgetsExists = newBudgets.size() > 0;
                if (!isInputValid || !isBudgetChange)
                    return;
                else if (allBudgets.size() == 0) {// Nothing needed to do
                    showMessageNoButton(language.pleaseInsertBudget);
                    return;
                }
                else if (month == null)
                    createBudget(getString(R.string.create));// First time create budget
                else if (isOriginContentBudgetChanged){// ReWriting of monthly budget needed
                    if (dbService.isCurrentRefMonthExists())
                        showQuestion(language.createBudgetQuestion);
                    return;
                }
                else if (isAddedBudgetsExists)// Insert the added budgets needed only
                    createBudget(getString(R.string.add));// Values of old budget updated only
            }
        });


        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(closeButton, lp);
        LLMain.addView(newll);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeBudgetIncludeUpdateCategory(int budgetNumber, final ArrayList<Budget> budgets, final String operation, final List<Budget> addedBudgets) {
        final String maxBudgetNumber = String.valueOf(budgetNumber);
        Query addBudgetQuery = DatabaseReferenceUserMonthlyBudget.child(getString(R.string.budget)).child(maxBudgetNumber);
//        Query query = DatabaseReferenceUserMonthlyBudget.child("Budget").orderByKey().limitToLast(1);
        addBudgetQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference budgetNode = DatabaseReferenceUserMonthlyBudget.child(getString(R.string.budget));
                for (Budget bgt : budgets) {
                    String id = budgetNode.child(maxBudgetNumber).push().getKey();
                    bgt.setId(id);
                    budgetNode.child(maxBudgetNumber).child(id).setValue(bgt);
                    dbService.updateSpecificBudget(maxBudgetNumber,bgt);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        List<Budget> budgetsToAdd  = addedBudgets;
        if (operation.equals(getString(R.string.add))){
            budgetsToAdd = budgets;
        }
        addCategoriesToMonthlyBudget(budgetsToAdd,budgetNumber,operation);

    }

    private void writeAddedCategories(final String yearMonthKey, final ArrayList<Budget> budgets) {
        Query addCategoriesQuery = DatabaseReferenceUserMonthlyBudget.child(getString(R.string.month)).child(yearMonthKey);
//        Query query = DatabaseReferenceUserMonthlyBudget.child("Budget").orderByKey().limitToLast(1);
        addCategoriesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference categoryNode = DatabaseReferenceUserMonthlyBudget.child(getString(R.string.category));
                for (Budget bgt : budgets) {
                    String categoryId = categoryNode.push().getKey();
                    Category category = new Category(categoryId,bgt.getCategoryName(),bgt.getValue(),bgt.getValue());
                    categoryNode.child(yearMonthKey).child(categoryId).setValue(category);
                    dbService.updateSpecificCategory(yearMonthKey,category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setBudgets() {
        allCategories.clear();
        allBudgets.clear();
        int priorityCat = 1;
        for (int i = 2; i < LLMain.getChildCount() - 2; i++) {
            EditText categoryET, valueET, shopET;
            CheckBox constPaymentCB;
            Spinner chargeDaySP;
            if (!language.isEn()) {
                categoryET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(1));
                valueET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(2));
                constPaymentCB = ((CheckBox) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(3));
                shopET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(4));
                chargeDaySP = ((Spinner) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(5));
            } else {
                categoryET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(4));
                valueET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(3));
                constPaymentCB = ((CheckBox) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(2));
                shopET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(1));
                chargeDaySP = ((Spinner) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(0));
            }
            String category = categoryET.getText().toString().trim();
            String valueStr = valueET.getText().toString().trim();
            boolean constPayment = constPaymentCB.isChecked();
            String shop = shopET.getText().toString().trim();
            //String chargeDayStr = chargeDayET.getText().toString().trim();
            String chargeDayStr = chargeDaySP.getSelectedItem().toString().trim();
            int chargeDay = 0;

            if (!constPayment) {
                shopET.setText(R.string.empty);
                shop = null;
                chargeDayStr = getString(R.string.zero);
//                chargeDayET.setText(chargeDayStr);
            }
            chargeDay = Integer.valueOf(chargeDayStr);

            if (valueStr.equals(getString(R.string.empty)))
                valueStr = getString(R.string.zero);
            int value = Integer.valueOf(valueStr);


            allCategories.add(category);
            //String categorySon = ((EditText)((LinearLayout)LLMain.getChildAt(i)).getChildAt(1)).getText().toString();
            verifyBudgetInput(categoryET, valueET, constPaymentCB, shopET, chargeDaySP);// chargeDayET);
            if (isInputValid)
                allBudgets.add(new Budget(category, value, constPayment, shop, chargeDay, priorityCat++));
            else
                return;
        }
    }

    public void verifyBudgetInput(EditText categoryET, EditText valueET, CheckBox constPaymentCB, EditText shopET, Spinner chargeDaySP) {//EditText chargeDayET) {
        isInputValid = true;
        String category = categoryET.getText().toString().trim();
        String valueStr = valueET.getText().toString().trim();
        boolean constPayment = constPaymentCB.isChecked();
        String shop = shopET.getText().toString().trim();
        //String chargeDayStr = chargeDayET.getText().toString().trim();
        String chargeDayStr = chargeDaySP.getSelectedItem().toString().trim();

        if (chargeDayStr.equals(""))
            chargeDayStr = getString(R.string.empty);
        int chargeDay = Integer.valueOf(chargeDayStr);

        if (valueStr.equals(getString(R.string.empty)))
            valueStr = getString(R.string.zero);
        int value = Integer.valueOf(valueStr);

        //Check duplicate of category
        if (Collections.frequency(allCategories, category) > 1) {
            setErrorEditText(categoryET, language.duplicateCategory);
            isInputValid = false;
        }

        //Check illegal characters
        if (category.contains(textService.getSeperator())) {
            setErrorEditText(categoryET, language.illegalCharacter);
            isInputValid = false;
        }
        //Check illegal category
        if (category.length() == 0) {
            setErrorEditText(categoryET, language.pleaseInsertCategory);
            isInputValid = false;
        }
        //Check illegal value
        if (value == 0) {
            setErrorEditText(valueET, language.pleaseInsertValue);
            isInputValid = false;
        }

/*        if(constPayment && shop.length() == 0 && chargeDayStr.length()  == 0)
        {
            setErrorEditText(shopET, "נא להזין חנות!");
            setErrorEditText(chargeDayET, "נא להזין יום לחיוב!");
            isInputValid = false;
        }*/

        if (constPayment && shop.length() == 0) {
            setErrorEditText(shopET, language.pleaseInsertShop);
            isInputValid = false;
        }

/*        if(constPayment && chargeDay == 0)
        {
            setErrorEditText(chargeDayET, "נא להזין יום לחיוב!");
            isInputValid = false;
        }*/

/*        if(constPayment && chargeDay > 31)
        {
            setErrorEditText(chargeDayET, "נא להזין יום חוקי לחיוב!");
            isInputValid = false;
        }*/

        //Check illegal characters
        if (shop.contains(textService.getSeperator())) {
            setErrorEditText(shopET, language.illegalCharacter);
            isInputValid = false;
        }

        //allCategories.add(budget.getCategory());
        // Need to reduce duplicates categories by define set of categories
        if (!isInputValid)
            return;
    }

    public void setErrorEditText(EditText et, String errorMesage) {
        et.setError(errorMesage);
    }

    public void showQuestion(String message) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        createBudget(getString(R.string.delete));
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        questionFalseAnswer();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton(language.yes, dialogClickListener)
                .setNegativeButton(language.no, dialogClickListener).show();
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
                //finish();
            }
        }, 1000); // 1000 milliseconds delay
    }

    public void showMessageToast(String message, boolean isFinishNeeded)//View view)
    {
        Toast.makeText(this, message,
                Toast.LENGTH_LONG).show();
        if (isFinishNeeded)
            finish();
/*        TextView msg = new TextView(this);
        // You Can Customise your Title here
        msg.setText(message);
        //title.setBackgroundColor(Color.DKGRAY);
        msg.setPadding(10, 10, 10, 10);
        msg.setGravity(Gravity.CENTER);
        msg.setTextColor(Color.BLACK);
        msg.setTextSize(20);
        myAlert = new AlertDialog.Builder(this);
        myAlert.setView(msg);//.create()
        myAlert.show();
        Handler handler = new Handler();
        if(isFinishNeeded)
            handler.postDelayed(new Runnable() {
                public void run() {
                    myAlert.create().dismiss();
                    finish();
                }
            }, 1000); // 1000 milliseconds delay
        else
        handler.postDelayed(new Runnable() {
            public void run() {
                myAlert.create().dismiss();
                //finish();
            }
        }, 1000); // 1000 milliseconds delay*/

    }

//    public void writeBudget(long budgetNumber)
//    {
//        long status = 0;
//        int categoryID = 0;
//        int subCategoryID = 0;
//        for (Budget bgt:allBudgets)
//        {
//            categoryID = monthlyBudgetDB.getCategoryId(bgt.getCategory());
//            if(categoryID == -1) {
//                status = monthlyBudgetDB.insertCategoryData(bgt.getCategory());
//                categoryID = monthlyBudgetDB.getCategoryId(bgt.getCategory());
//            }
//            subCategoryID = monthlyBudgetDB.getSubCategoryId(categoryID,bgt.getCategorySon());
//            if(subCategoryID == -1) {
//                status = monthlyBudgetDB.insertSubCategoryData(categoryID, bgt.getCategorySon());
//                subCategoryID = monthlyBudgetDB.getSubCategoryId(categoryID, bgt.getCategorySon());
//            }
//            int catPriority = bgt.getCatPriority();
//            status = monthlyBudgetDB.insertBudgetTableData(budgetNumber, categoryID, subCategoryID,catPriority, bgt.getValue(), bgt.isConstPayment(), bgt.getShop(), bgt.getChargeDay());
//        }
//    }

    public boolean isOriginBudgetChanged(int budgetNumber) {
        ArrayList<Budget> oldBudget = dbService.getBudgetDataFromDB(budgetNumber);
        int counter = 0;
        for (Budget oldBgt : oldBudget)
            for (Budget bgt : allBudgets)
                if (oldBgt.equals(bgt)) {
                    counter++;
                    break;
                }
        return counter != oldBudget.size();
    }

    public boolean isBudgetChange(int budgetNumber) {
        ArrayList<Budget> oldBudget = dbService.getBudgetDataFromDB(budgetNumber);
        boolean isBudgetsEquals = false;
        for (Budget bgt : allBudgets) {
            for (Budget oldBgt : oldBudget) {
                if (bgt.equals(oldBgt)) {
                    isBudgetsEquals = true;
                    break;
                }
            }
            if (isBudgetsEquals == false)
                return true;
            isBudgetsEquals = false;
        }
        return allBudgets.size() != oldBudget.size();
    }

    public ArrayList<Budget> getAddedBudgets(int budgetNumber) {
        ArrayList<Budget> oldBudget = dbService.getBudgetDataFromDB(budgetNumber);
        ArrayList<Budget> addedBudgets = new ArrayList<>();
        boolean isBudgetExists = false;
        for (Budget bgt : allBudgets) {
            for (Budget oldBgt : oldBudget) {
                if (bgt.equals(oldBgt)) {
                    isBudgetExists = true;
                    continue;
                }
            }
            if (isBudgetExists == false)
                addedBudgets.add(bgt);
            isBudgetExists = false;
        }
        return addedBudgets;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addCategoriesToMonthlyBudget(List<Budget> catToAdd, int budgetNumber,String operation) {
        String yearMonth = DateService.getYearMonth(DateService.getTodayDate(), getString(R.string.seperator));
        if(operation.equals(getString(R.string.add)) && !dbService.isCurrentRefMonthExists()){
            // Exception - add operation cannot on empty month
            return;
        }
        DatabaseReference categoryDBReference = DatabaseReferenceUserMonthlyBudget.child(yearMonth).child(getString(R.string.category));
        dbService.setAddedCategoriesIncludeEventUpdateValue(categoryDBReference,yearMonth,catToAdd,operation);
        dbService.updateBudgetNumberMB(yearMonth, budgetNumber);
        int maxIDPerMonth = dbService.getMaxIDPerMonthTRN(yearMonth);
        month.initCategories();
        setFrqTrans(catToAdd, maxIDPerMonth);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createBudget(String operation) {
        int budgetNumber = dbService.getMaxBudgetNumber() + 1;
        ArrayList<Budget> addedBudgets = new ArrayList<>();
        if (!operation.equals(getString(R.string.create)))
            addedBudgets = getAddedBudgets(budgetNumber - 1);
//        if (operation.equals(getString(R.string.add)))
//            addCategoriesToMonthlyBudget(addedBudget, budgetNumber);
        else if (operation.equals(getString(R.string.delete)))
            dbService.deleteDataRefMonth(dateService.getYearMonth(month.getRefMonth(),getString(R.string.seperator)));
//        else if (operation.equals(getString(R.string.create)))
//            ; // Delete or add not needed
        writeBudgetIncludeUpdateCategory(budgetNumber,allBudgets,operation,addedBudgets);

        //deleteCurrentMonth();
        month = null;
        showMessageToast(language.budgetCreatedSuccessfully, true);
    }

    private void questionFalseAnswer() {
    }

/*    public void writeBudget(ArrayList<String> lines, String fileName, String dirPath) {
        // Writing the categories and budget values to file
        writeToFile(lines, fileName, dirPath);
    }*/

    public void setButtonsNames() {
        // CreateBudget window buttons
        ((TextView) findViewById(R.id.createBudgetLabel)).setText(language.createBudgetName);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTitleRow() {
        final LinearLayout titleLL = new LinearLayout(Create_Budget_Activity.this);
        initTitlesTv();

        //int screenHeight = display.getHeight();
        //categoryNameLabel.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 4 , ViewGroup.LayoutParams.WRAP_CONTENT));
        //categoryFamilyEditText.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 3 ,ViewGroup.LayoutParams.WRAP_CONTENT));
        //categoryValueLabel.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 4 ,ViewGroup.LayoutParams.WRAP_CONTENT));

        //categoryValueEditText.setTextSize(18);
        //LinearLayout.LayoutParams lp =  new LinearLayout.LayoutParams(screenWidth / 4,categoryNameEditText.getHeight());

        ArrayList<TextView> titlesTV = new ArrayList<>(Arrays.asList(emptyTV, categoryNameTV, categoryValueTV, constPaymentTV, shopTV, payDateTV));
        setTitleStyle(titlesTV, titleLL);

        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);
        LLMain.addView(titleLL);
    }

    private void initTitlesTv() {
        emptyTV = new TextView(Create_Budget_Activity.this);
        categoryNameTV = new TextView(Create_Budget_Activity.this);
        categoryValueTV = new TextView(Create_Budget_Activity.this);
        constPaymentTV = new TextView(Create_Budget_Activity.this);
        shopTV = new TextView(Create_Budget_Activity.this);
        payDateTV = new TextView(Create_Budget_Activity.this);
        List<View> widgets = Arrays.asList((View)categoryNameTV, (View)categoryNameTV, (View)constPaymentTV, (View)shopTV, (View)payDateTV);

        uiService.setTextTitleWidgets(widgets);
        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        uiService.setWidthCreateBudgetPageTitleWidgets(widgets,screenWidthReduceButtonSize,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void setTextTitleWidgets() {
        categoryNameTV.setText(textService.getWordCapitalLetter(language.categoryName));
        categoryValueTV.setText(textService.getWordCapitalLetter(language.budgetName));
        constPaymentTV.setText(textService.getWordCapitalLetter(language.constantDate));
        shopTV.setText(textService.getWordCapitalLetter(language.shopName));
        payDateTV.setText(textService.getWordCapitalLetter(language.chargeDay));

    }

    private void setTitleStyle(ArrayList<TextView> titlesTV, LinearLayout titleLL) {
        for (TextView titletv : titlesTV) {
            titletv.setTextSize(15);
            uiService.setHeaderProperties(titletv);
            titleLL.addView(titletv);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setBudgetGui() {
        allBudgets = dbService.getBudgetDataFromDB(dbService.getMaxBudgetNumber());
        if (allBudgets == null || allBudgets.size() == 0) {
            add_New_row(null, 0, false, null, 0);
            allBudgets = new ArrayList<>();
        } else
            for (Budget budget : allBudgets) {
                add_New_row(budget.getCategoryName(), budget.getValue(), budget.isConstPayment(), budget.getShop(), budget.getChargeDay());
                setCloseButton();// Adding close button
            }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void add_New_row(String categoryName, int categoryValue, boolean isConstPayment, String shop, int chargeDay) {
        boolean isEmptyRow = (categoryName == null && categoryValue == 0 && isConstPayment == false && shop == null && chargeDay == 0);
        final LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);
        final EditText categoryNameET = new EditText(Create_Budget_Activity.this),
                       categoryValueET = new EditText(Create_Budget_Activity.this),
                       shopET = new EditText(Create_Budget_Activity.this);
        final Spinner optionalDaysSpinner = new Spinner(Create_Budget_Activity.this);
        final CheckBox constPaymentCB = new CheckBox(Create_Budget_Activity.this);;
        setSpinnerOptionalDays(optionalDaysSpinner);

        List<View> widgets = Arrays.asList((View)categoryNameET, (View)categoryNameET, (View)categoryValueET, (View)constPaymentCB, (View)shopET, (View)optionalDaysSpinner);
        // todo check the width of widgets
        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        uiService.setWidthCreateBudgetPageDataWidgets(widgets,screenWidthReduceButtonSize,ViewGroup.LayoutParams.WRAP_CONTENT);
        setConstPaymentCBOnCheckChangedListner(constPaymentCB,shopET,optionalDaysSpinner);

        if (!isEmptyRow)
            categoryNameET.setText(categoryName);
        if (!isEmptyRow)
            categoryValueET.setText(String.valueOf(categoryValue));

        constPaymentCB.setChecked(isConstPayment);

        if (!isEmptyRow) {
            shopET.setText(shop);
            optionalDaysSpinner.setSelection(chargeDay - 1);
        }

        categoryNameET.requestFocus();
        categoryNameET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        categoryValueET.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        constPaymentCB.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        shopET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);

        categoryNameET.setTextSize(12);
        categoryValueET.setTextSize(12);
        constPaymentCB.setTextSize(12);
        shopET.setTextSize(12);

        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);

        optionalDaysSpinner.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(categoryNameET.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(categoryValueET.getWindowToken(), 0);
                //imm.hideSoftInputFromWindow(constPaymentCB.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(shopET.getWindowToken(), 0);
                return false;
            }
        });

        final ImageButton deleteRowButton = new ImageButton(this);

        deleteRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LLMain.removeView(newll);
                //for (int i = 1; i < LLMain.getChildCount() - 2; i++)
                //deleteSpecificRow(i);
                if (LLMain.getChildCount() == 4)
                    add_New_row(null, 0, false, null, 0);
            }
        });
        deleteRowButton.setImageDrawable(getResources().getDrawable(R.drawable.delete_icon));
        deleteRowButton.setBackgroundDrawable(dfaultBackground);
        //deleteRowButton.setBackgroundColor(Color.parseColor("#FAFAFA"));View v;v.getBackground().deleteRowButton.setScaleType(ImageView.ScaleType.FIT_XY);
        deleteRowButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        deleteRowButton.setAdjustViewBounds(true);

//        if(language.isHeb()) {

        newll.addView(deleteRowButton);

        if (!constPaymentCB.isChecked()) {
            shopET.setVisibility(View.INVISIBLE);
            optionalDaysSpinner.setVisibility(View.INVISIBLE);
        }

        newll.addView(categoryNameET);
        newll.addView(categoryValueET);
        newll.addView(constPaymentCB);
        newll.addView(shopET);
        newll.addView(optionalDaysSpinner);
        if (language.isEn())
            uiService.setLanguageConf(newll);
//        }
/*        else if(language.isEn())
        {
            newll.addView(optionalDaysSpinner);
            //newll.addView(chargeDayET);
            if (!constPaymentCB.isChecked()) {
                shopET.setVisibility(View.INVISIBLE);
                //chargeDayET.setVisibility(View.INVISIBLE);
                optionalDaysSpinner.setVisibility(View.INVISIBLE);
            }
            newll.addView(shopET);
            newll.addView(constPaymentCB);
            newll.addView(categoryValueET);
            newll.addView(categoryNameET);


            newll.addView(deleteRowButton);

        }*/
        boolean isWithCloseBtn = isEmptyRow;
        addRow(newll, isWithCloseBtn);
    }

    private void setConstPaymentCBOnCheckChangedListner(CheckBox constPaymentCB, final EditText shopET, final Spinner optionalDaysSpinner) {
        constPaymentCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                shopET.setVisibility(View.VISIBLE);
                optionalDaysSpinner.setVisibility(View.VISIBLE);
            } else {
                shopET.setVisibility(View.INVISIBLE);
                optionalDaysSpinner.setVisibility(View.INVISIBLE);
            }
        }
    });
    }

    public void setSpinnerOptionalDays(Spinner OptionalDaysSP) {
        ArrayList<String> daysInMonth = new ArrayList<>();
        int i = 1;
        while (i <= 31)
            daysInMonth.add(String.valueOf(i++));
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, daysInMonth);
        if (language.language.equals(getString(R.string.hebrew)))
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner, daysInMonth);
        else if (language.language.equals(getString(R.string.english)))
            adapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner_eng, daysInMonth);
        OptionalDaysSP.setAdapter(adapter);
        OptionalDaysSP.setSelection(1, true);
    }


    public void deleteSpecificRow(int index) {
        LLMain.removeViewAt(index);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setDeleteButton() {
        final ImageButton deleteRowButton = new ImageButton(this);

        deleteRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

            }
        });
        deleteRowButton.setImageDrawable(getResources().getDrawable(R.drawable.delete_icon));
        //addRowButton.setBackground(null);
        //addRowButton.getLayoutParams().width = 40;
        //addRowButton.getLayoutParams().height = 40;
        deleteRowButton.setScaleType(ImageView.ScaleType.FIT_XY);


        deleteRowButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        deleteRowButton.setAdjustViewBounds(true);
        LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(deleteRowButton);
        LLMain.addView(newll);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myAlert != null)
            myAlert.create().dismiss();
    }

}

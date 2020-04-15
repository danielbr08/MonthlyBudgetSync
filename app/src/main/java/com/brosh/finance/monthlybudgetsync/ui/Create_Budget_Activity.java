package com.brosh.finance.monthlybudgetsync.ui;

import android.annotation.SuppressLint;
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
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.brosh.finance.monthlybudgetsync.services.UIService;
import com.brosh.finance.monthlybudgetsync.config.Language;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import android.support.annotation.RequiresApi;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import com.brosh.finance.monthlybudgetsync.services.NetworkService;

public class Create_Budget_Activity extends AppCompatActivity {

    private DBService dbService;

    DatabaseReference DatabaseReferenceUserMonthlyBudget;
    private Month month;
    private Language language;

    TextView emptyTV, categoryNameTV, categoryValueTV, constPaymentTV, shopTV, payDateTV;


    AlertDialog.Builder myAlert;
    private List<Budget> allBudgets;
    private ArrayList<String> allCategories;
    private boolean isInputValid;
    private Display display;
    private int screenWidth;
    private int buttonSize = 120;

    private Drawable dfaultBackground;

    private LinearLayout LLMain;
    private LinearLayout LLBudgets;
    //Button to add a row
    private Button addCategoryButton;

    //Button to write all the inserted categories to budget file
    private Button OKButton;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userKey = getIntent().getExtras().getString(getString(R.string.user), "");
        String refMonth = getIntent().getExtras().getString(getString(R.string.month));
        dbService = DBService.getInstance();
        month = dbService.getMonth(refMonth);

        DatabaseReferenceUserMonthlyBudget = FirebaseDatabase.getInstance().getReference(Definition.MONTHLY_BUDGET).child(userKey);
        setContentView(R.layout.activity_create_budget);
        LLMain = (LinearLayout) findViewById(R.id.LLMainCreateBudget);
        LLBudgets = new LinearLayout(this);
        LLBudgets.setOrientation(LinearLayout.VERTICAL);
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

//        try {
        setBudgetGui();
//        } catch (Exception e) {
//            String s = e.getMessage();
//            s = s;
//        }
        if (language.isEn()) {
/*            for (int i=1;i<LLMain.getChildCount() - 1;i++)
            {
                //LinearLayout currentLL = (LinearLayout) LLMain.getChildAt(i);
                //for (int j = 0; j < currentLL.getChildCount(); j++)

                setLanguageConf((LinearLayout) LLMain.getChildAt(i));
            }*/
            UIService.setLanguageConf((LinearLayout) LLMain.getChildAt(1));
            //setLanguageConf((LinearLayout) LLMain.getChildAt(lastRowIndex));
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setAddAndDeleteButton() {
        final LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);
        newll.setOrientation(LinearLayout.HORIZONTAL);

        final ImageButton addRowButton = new ImageButton(this);
        final ImageButton deleteRowsButton = new ImageButton(this);
        final TextView emptyTV = new TextView(this);

        //Set default background color
        addRowButton.setBackgroundDrawable(dfaultBackground);
        deleteRowsButton.setBackgroundDrawable(dfaultBackground);

        addRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                int budgetSize = LLBudgets.getChildCount() - 1;
                LinearLayout lastBudgetRow = (LinearLayout) LLBudgets.getChildAt(budgetSize);
                int categoryNameIndex = language.isLTR() ? lastBudgetRow.getChildCount() - 2 : 1;
                int categoryValueIndex = language.isLTR() ? lastBudgetRow.getChildCount() - 3 : 2;
                EditText categoryNameET = (EditText) lastBudgetRow.getChildAt(categoryNameIndex);
                EditText categoryValueET = (EditText) lastBudgetRow.getChildAt(categoryValueIndex);
                boolean isLastRowValid = !categoryNameET.getText().toString().trim().equals("") && !categoryValueET.getText().toString().trim().equals("");
                if (isLastRowValid)
                    add_New_row(null, 0, false, null, 0);
            }
        });

        deleteRowsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LLBudgets.removeAllViews();
                add_New_row(null, 0, false, null, 0);
            }
        });
        deleteRowsButton.setImageDrawable(getResources().getDrawable(R.drawable.clean_screen));
        deleteRowsButton.setScaleType(ImageView.ScaleType.FIT_XY);

        addRowButton.setImageDrawable(getResources().getDrawable(R.drawable.add_button_md));
        addRowButton.setScaleType(ImageView.ScaleType.FIT_XY);


        addRowButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        deleteRowsButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        addRowButton.setAdjustViewBounds(true);
        deleteRowsButton.setAdjustViewBounds(true);

        emptyTV.setLayoutParams(new LinearLayout.LayoutParams(screenWidth - 2 * buttonSize, buttonSize));
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(40, 40);

        //addRowButton.setPadding();
        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(addRowButton);//,lp);
        newll.addView(deleteRowsButton);
        newll.addView(emptyTV);
        if (language.isEn())
            UIService.setLanguageConf(newll);
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
                if (allBudgets.size() == 0) {// Nothing needed to do
                    showMessageNoButton(language.pleaseInsertBudget);
                    return;
                }
                int budgetNumber = dbService.getMaxBudgetNumber();
                ArrayList<Budget> newBudgets = getAddedBudgets(budgetNumber);
                boolean isOriginContentBudgetChanged = isOriginBudgetChanged(budgetNumber);
                boolean isBudgetChange = isBudgetChange(budgetNumber);
                boolean isAddedBudgetsExists = newBudgets.size() > 0;
                if (!isInputValid || !isBudgetChange)
                    return;
                if (month == null)
                    createBudget(getString(R.string.create));// First time create budget
                else if (isOriginContentBudgetChanged) {// Rewriting of monthly budget needed
                    if (dbService.isCurrentRefMonthExists())
                        showQuestionDeleteCurrentMonth(language.createBudgetQuestion);
                    return;
                } else if (isAddedBudgetsExists)// Insert the added budgets needed only
                    createBudget(getString(R.string.add));// Values of old budget updated only
            }
        });


        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(closeButton, lp);
        LLMain.addView(newll);
    }

    public void writeBudget(int budgetNumber, final List<Budget> budgets) {
        String budgetNumberStr = String.valueOf(budgetNumber);
        for (Budget budget : budgets) {
            String budgetId = dbService.getDBBudgetsPath().child(budgetNumberStr).push().getKey();
            budget.setId(budgetId);
            dbService.updateSpecificBudget(String.valueOf(budgetNumber), budget);
        }
    }

    public void setBudgets() {
        allCategories.clear();
        allBudgets.clear();
        int catPriority = 1;
        for (int i = 0; i < LLBudgets.getChildCount(); i++) {
            EditText categoryET, valueET, shopET;
            CheckBox constPaymentCB;
            Spinner chargeDaySP;
            int firstViewIndex = language.isEn() ? 5 : 0;
            int addToNextIndex = language.isEn() ? -1 : 1;
            int addToNextIndexCounter = 1;
            categoryET = ((EditText) ((LinearLayout) LLBudgets.getChildAt(i)).getChildAt(firstViewIndex + (addToNextIndexCounter++ * addToNextIndex)));// todo this expression generc
            valueET = ((EditText) ((LinearLayout) LLBudgets.getChildAt(i)).getChildAt(firstViewIndex + (addToNextIndexCounter++ * addToNextIndex)));
            constPaymentCB = ((CheckBox) ((LinearLayout) LLBudgets.getChildAt(i)).getChildAt(firstViewIndex + (addToNextIndexCounter++ * addToNextIndex)));
            shopET = ((EditText) ((LinearLayout) LLBudgets.getChildAt(i)).getChildAt(firstViewIndex + (addToNextIndexCounter++ * addToNextIndex)));
            chargeDaySP = ((Spinner) ((LinearLayout) LLBudgets.getChildAt(i)).getChildAt(firstViewIndex + (addToNextIndexCounter * addToNextIndex)));

            String category = categoryET.getText().toString().trim();
            String valueStr = valueET.getText().toString().trim();
            boolean constPayment = constPaymentCB.isChecked();
            String shop = shopET.getText().toString().trim();
            String chargeDayStr = chargeDaySP.getSelectedItem().toString().trim();
            int chargeDay = 0;

            if (!constPayment) {
                shopET.setText(R.string.empty);
                shop = null;
                chargeDayStr = getString(R.string.zero);
            }
            chargeDay = Integer.valueOf(chargeDayStr);

            if (valueStr.equals(getString(R.string.empty)))
                valueStr = getString(R.string.zero);
            int value = Integer.valueOf(valueStr);


            allCategories.add(category);
            verifyBudgetInput(categoryET, valueET, constPaymentCB, shopET, chargeDaySP);// chargeDayET);
            if (isInputValid)
                allBudgets.add(new Budget(category, value, constPayment, shop, chargeDay, catPriority++));
            else {
                allBudgets.clear();
                return;
            }
        }
    }

    public void verifyBudgetInput(EditText categoryET, EditText valueET, CheckBox constPaymentCB, EditText shopET, Spinner chargeDaySP) {//EditText chargeDayET) {
        isInputValid = true;
        String category = categoryET.getText().toString().trim();
        String valueStr = valueET.getText().toString().trim();
        boolean constPayment = constPaymentCB.isChecked();
        String shop = shopET.getText().toString().trim();
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
        if (category.contains(TextService.getSeperator())) {
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

        //Check illegal characters
        if (shop.contains(TextService.getSeperator())) {
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

    public void showQuestionDeleteCurrentMonth(String message) {
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

    public boolean isOriginBudgetChanged(int budgetNumber) {
        List<Budget> oldBudget = dbService.getBudgetDataFromDB(budgetNumber);
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
        List<Budget> oldBudget = dbService.getBudgetDataFromDB(budgetNumber);
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
        List<Budget> oldBudget = dbService.getBudgetDataFromDB(budgetNumber);
        ArrayList<Budget> addedBudgets = new ArrayList<>();
        boolean isBudgetExists = false;
        for (Budget bgt : allBudgets) {
            for (Budget oldBgt : oldBudget) {
                if (bgt.equals(oldBgt)) {
                    isBudgetExists = true;
                    break;
                }
            }
            if (isBudgetExists == false)
                addedBudgets.add(bgt);
            isBudgetExists = false;
        }
        return addedBudgets;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createBudget(String operation) {
        int budgetNumber = dbService.getMaxBudgetNumber() + 1;
        ArrayList<Budget> addedBudgets = new ArrayList<>();
        if (operation.equals(getString(R.string.add)))
            addedBudgets = getAddedBudgets(budgetNumber - 1);
        else if (operation.equals(getString(R.string.delete)))
            dbService.deleteDataRefMonth(month.getYearMonth());

        String refMonth = DateService.getYearMonth(DateService.getTodayDate(), getString(R.string.seperator));
        writeBudget(budgetNumber, allBudgets);
        writeBudgetsToTreeFB(budgetNumber);

        if (operation.equals(getString(R.string.add))) {
            dbService.updateBudgetNumber(refMonth, budgetNumber);
            dbService.addNewCategoriesToExistingMonth(refMonth, budgetNumber, addedBudgets);
            dbService.updateBudgetNumberFB(refMonth, budgetNumber);
            dbService.updateShopsFB();

        } else
            dbService.createNewMonth(budgetNumber, refMonth);

        //deleteCurrentMonth();
//        month = null;
        TextService.showMessage(language.budgetCreatedSuccessfully, Toast.LENGTH_LONG, getApplicationContext());
        finish();
    }

    private List<Category> budgetToCategories(List<Budget> budgets) {
        List<Category> categories = new ArrayList<>();
        for (Budget budget : budgets) {
            Category cat = new Category("", budget.getCategoryName(), budget.getValue(), budget.getValue());
            categories.add(cat);
        }
        return categories;
    }

    private void writeBudgetsToTreeFB(final int budgetNumber) { // todo move this function to dbService
        String budgetNumberStr = String.valueOf(budgetNumber);
        Map<String, Budget> hmBudgets = dbService.getBudget(budgetNumberStr);
        dbService.getDBBudgetsPath().child(budgetNumberStr).setValue(hmBudgets);
    }

    private void questionFalseAnswer() {
    }

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

//        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        newll.setOrientation(LinearLayout.HORIZONTAL);
        LLMain.addView(titleLL);
    }

    private void initTitlesTv() {
        emptyTV = new TextView(Create_Budget_Activity.this);
        categoryNameTV = new TextView(Create_Budget_Activity.this);
        categoryValueTV = new TextView(Create_Budget_Activity.this);
        constPaymentTV = new TextView(Create_Budget_Activity.this);
        shopTV = new TextView(Create_Budget_Activity.this);
        payDateTV = new TextView(Create_Budget_Activity.this);
        List<View> widgets = Arrays.asList((View) categoryNameTV, (View) categoryValueTV, (View) constPaymentTV, (View) shopTV, (View) payDateTV);

        UIService.setTextTitleWidgets(widgets);
        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        UIService.setWidthCreateBudgetPageTitleWidgets(widgets, screenWidthReduceButtonSize, ViewGroup.LayoutParams.WRAP_CONTENT);
        emptyTV.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
    }

    private void setTitleStyle(ArrayList<TextView> titlesTV, LinearLayout titleLL) {
        for (TextView titletv : titlesTV) {
            UIService.setHeaderProperties(titletv);
            titleLL.addView(titletv);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setBudgetGui() {
        setTitleRow(); // index 1
        LLMain.addView(LLBudgets);
        allBudgets = dbService.getBudgetDataFromDB(dbService.getMaxBudgetNumber());
        for (Budget budget : allBudgets) {
            add_New_row(budget.getCategoryName(), budget.getValue(), budget.isConstPayment(), budget.getShop(), budget.getChargeDay());
        }
        if (allBudgets.size() == 0) // No any budget exists
            add_New_row("", 0, false, "", 2);
        setAddAndDeleteButton(); // add row and clean buttons
        setCloseButton();// Adding close button (last index)
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void add_New_row(String categoryName, int categoryValue, boolean isConstPayment, String shop, int chargeDay) {
        final LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);
        final EditText categoryNameET = new EditText(Create_Budget_Activity.this),
                categoryValueET = new EditText(Create_Budget_Activity.this),
                shopET = new EditText(Create_Budget_Activity.this);
        final Spinner optionalDaysSpinner = new Spinner(Create_Budget_Activity.this);
        final CheckBox constPaymentCB = new CheckBox(Create_Budget_Activity.this);

        // todo check the width of widgets
        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        List<View> rowViews = Arrays.asList(categoryNameET, categoryValueET, constPaymentCB, shopET, optionalDaysSpinner);
        List<String> viewsText = Arrays.asList(categoryName, String.valueOf(categoryValue), String.valueOf(isConstPayment), shop, String.valueOf(chargeDay - 1));
        List<View> textInputType = Arrays.asList(categoryNameET, constPaymentCB, shopET);
        List<View> numberInputType = Arrays.asList(categoryValueET);
        UIService.setViewsText(rowViews, viewsText);
        UIService.setTxtSize(rowViews, 12);
        setViewsInput(textInputType, numberInputType);
        UIService.setInputFocus(categoryNameET);
        setSpinnerOptionalDays(optionalDaysSpinner);

        UIService.setWidthCreateBudgetPageDataWidgets(rowViews, screenWidthReduceButtonSize, ViewGroup.LayoutParams.WRAP_CONTENT);
        setConstPaymentCBOnCheckChangedListner(constPaymentCB, shopET, optionalDaysSpinner);


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
                LLBudgets.removeView(newll);
            }
        });
        deleteRowButton.setImageDrawable(getResources().getDrawable(R.drawable.delete_icon));
        deleteRowButton.setBackgroundDrawable(dfaultBackground);
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
        if (language.isLTR())
            UIService.setLanguageConf(newll);
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
        LLBudgets.addView(newll);
    }

    private void setViewsInput(List<View> textInputType, List<View> numberInputType) {
        for (View view : textInputType) {
            UIService.setViewInputTypeText(view);
        }
        for (View view : numberInputType) {
            UIService.setViewInputTypeNumber(view);
        }
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

    @SuppressLint("NewApi")
    public void setSpinnerOptionalDays(Spinner OptionalDaysSP) {
        List<Integer> daysInMonth = new ArrayList<>();
//        int i = 1;
//        while (i <= 31)
//            daysInMonth.add(String.valueOf(i++));
        daysInMonth = IntStream.range(1, 31).boxed().collect(Collectors.toList());
        ArrayAdapter<Integer> adapter;
        adapter = new ArrayAdapter<Integer>(this,
                R.layout.custom_spinner, daysInMonth);
        if (!language.isLTR())
            adapter = new ArrayAdapter<Integer>(this,
                    R.layout.custom_spinner, daysInMonth);
        else
            adapter = new ArrayAdapter<Integer>(this,
                    R.layout.custom_spinner_eng, daysInMonth);
        OptionalDaysSP.setAdapter(adapter);
        OptionalDaysSP.setSelection(1, true);
        OptionalDaysSP.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        if (myAlert != null)
//            myAlert.create().dismiss();
//    }
}

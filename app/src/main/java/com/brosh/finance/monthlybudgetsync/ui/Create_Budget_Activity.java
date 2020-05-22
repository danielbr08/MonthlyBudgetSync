package com.brosh.finance.monthlybudgetsync.ui;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.CreateBudgetViewAdapter;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.brosh.finance.monthlybudgetsync.services.UIService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//import android.support.annotation.RequiresApi;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import com.brosh.finance.monthlybudgetsync.services.NetworkService;

public class Create_Budget_Activity extends AppCompatActivity {

    private DBService dbService;

    DatabaseReference DatabaseReferenceUserMonthlyBudget;
    private Month month;

    TextView emptyTV, categoryNameTV, categoryValueTV, constPaymentTV, shopTV, payDateTV;


    AlertDialog.Builder myAlert;
    private List<Budget> allBudgets;
    private List<Budget> budgets;
    private ArrayList<String> allCategories;
    private boolean isInputValid;
    private Display display;
    private int screenWidth;
    private int buttonSize = 120;

    private Drawable dfaultBackground;

    CreateBudgetViewAdapter adapter;
    RecyclerView budgetsRowsRecycler;

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
        setContentView(R.layout.activity_create_budget);

        String userKey = getIntent().getExtras().getString(Definition.USER, "");
        String refMonth = getIntent().getExtras().getString(Definition.MONTH);
        dbService = DBService.getInstance();
        month = dbService.getMonth(refMonth);

        adapter = new CreateBudgetViewAdapter(this, budgets);
        budgetsRowsRecycler = findViewById(R.id.budgets_rows);

        DatabaseReferenceUserMonthlyBudget = FirebaseDatabase.getInstance().getReference(Definition.MONTHLY_BUDGET).child(userKey);
        LLMain = (LinearLayout) findViewById(R.id.LLMainCreateBudget);
        LLBudgets = new LinearLayout(this);
        LLBudgets.setOrientation(LinearLayout.VERTICAL);
        allBudgets = new ArrayList<>();
        allCategories = new ArrayList<>();
//        setTitle(getString(R.string.app_name));
        dfaultBackground = new View(this).getBackground();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Rotate the screen to to be on portrait moade only
        display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        addCategoryButton = new Button(Create_Budget_Activity.this);
        OKButton = new Button(Create_Budget_Activity.this);

        setBudgetGui();
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
                int categoryNameIndex = 1;
                int categoryValueIndex = 2;
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
        LLMain.addView(newll);
    }

    public void setCloseButton() {
        final Button closeButton = new Button(this);
        int size = (150 * buttonSize) / 100;
        closeButton.setHeight(size);
        closeButton.setText(getString(R.string.create));
        closeButton.setTextColor(Color.BLACK);
        closeButton.setTypeface(null, Typeface.BOLD);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View view) {
                setBudgets();
                if (allBudgets.size() == 0) {// Nothing needed to do
                    showMessageNoButton(getString(R.string.please_insert_budget));
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
                    createBudget(Definition.CREATE_CODE);// First time create budget
                else if (isOriginContentBudgetChanged) {// Rewriting of monthly budget needed
                    if (dbService.isCurrentRefMonthExists())
                        showQuestionDeleteCurrentMonth(getString(R.string.create_budget_question));
                    return;
                } else if (isAddedBudgetsExists)// Insert the added budgets needed only
                    createBudget(Definition.ADD_CODE);// Values of old budget updated only
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
        RecyclerView LLBudgets = findViewById(R.id.budgets_rows);
        int catPriority = 1;
        for (int i = 0; i < budgetsRowsRecycler.getChildCount(); i++) {
            EditText categoryET, valueET, shopET;
            CheckBox constPaymentCB;
            Spinner chargeDaySP;
            int j = 0;
            categoryET = ((EditText) ((LinearLayout) budgetsRowsRecycler.getChildAt(i)).getChildAt(j++));// todo this expression generic
            valueET = ((EditText) ((LinearLayout) budgetsRowsRecycler.getChildAt(i)).getChildAt(j++));
            constPaymentCB = ((CheckBox) ((LinearLayout) budgetsRowsRecycler.getChildAt(i)).getChildAt(j++));
            shopET = ((EditText) ((LinearLayout) budgetsRowsRecycler.getChildAt(i)).getChildAt(j++));
            chargeDaySP = ((Spinner) ((LinearLayout) budgetsRowsRecycler.getChildAt(i)).getChildAt(j++));

            String category = categoryET.getText().toString().trim();
            String valueStr = valueET.getText().toString().trim().replace(Definition.COMMA, "");
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
//                allBudgets.clear();
                return;
            }
        }
        budgets = allBudgets;
    }

    public void verifyBudgetInput(EditText categoryET, EditText valueET, CheckBox constPaymentCB, EditText shopET, Spinner chargeDaySP) {//EditText chargeDayET) {
        isInputValid = true;
        String category = categoryET.getText().toString().trim();
        String valueStr = valueET.getText().toString().trim().replace(Definition.COMMA, getString(R.string.empty));
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
            setErrorEditText(categoryET, getString(R.string.duplicate_category));
            isInputValid = false;
        }

        //Check illegal characters
        if (category.contains(TextService.getSeperator())) {
            setErrorEditText(categoryET, getString(R.string.illegal_Character));
            isInputValid = false;
        }
        //Check illegal category
        if (category.length() == 0) {
            setErrorEditText(categoryET, getString(R.string.please_insert_category));
            isInputValid = false;
        }
        //Check illegal value
        if (value == 0) {
            setErrorEditText(valueET, getString(R.string.please_insert_value));
            isInputValid = false;
        }

        if (constPayment && shop.length() == 0) {
            setErrorEditText(shopET, getString(R.string.please_insert_store));
            isInputValid = false;
        }

        //Check illegal characters
        if (shop.contains(TextService.getSeperator())) {
            setErrorEditText(shopET, getString(R.string.please_insert_value));
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
                        createBudget(Definition.DELETE_CODE);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        questionFalseAnswer();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
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
        if (operation.equals(Definition.ADD_CODE))
            addedBudgets = getAddedBudgets(budgetNumber - 1);
        else if (operation.equals(Definition.DELETE_CODE))
            dbService.deleteDataRefMonth(month.getYearMonth());

        String refMonth = DateService.getYearMonth(DateService.getTodayDate(), getString(R.string.seperator));
        writeBudget(budgetNumber, allBudgets);
        writeBudgetsToTreeFB(budgetNumber);

        if (operation.equals(Definition.ADD_CODE)) {
            dbService.updateBudgetNumber(refMonth, budgetNumber);
            dbService.addNewCategoriesToExistingMonth(refMonth, budgetNumber, addedBudgets);
            dbService.updateBudgetNumberFB(refMonth, budgetNumber);
            dbService.updateShopsFB();

        } else
            dbService.createNewMonth(budgetNumber, refMonth);

        //deleteCurrentMonth();
//        month = null;
        TextService.showMessage(getString(R.string.budget_created_successfully), Toast.LENGTH_LONG, getApplicationContext());
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
        ((TextView) findViewById(R.id.createBudgetLabel)).setText(getString(R.string.create_budget));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTitleRow() {
        final LinearLayout titleLL = new LinearLayout(Create_Budget_Activity.this);
        initTitlesTv();

        ArrayList<TextView> titlesTV = new ArrayList<>(Arrays.asList(emptyTV, categoryNameTV, categoryValueTV, constPaymentTV, shopTV, payDateTV));
        setTitleStyle(titlesTV, titleLL);

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

        List<String> titles = Arrays.asList(getString(R.string.category), getString(R.string.budget), getString(R.string.constant_date), getString(R.string.store), getString(R.string.charge_day));
        UIService.setTextTitleWidgets(widgets, titles);
        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        UIService.setWidthCreateBudgetPageTitleWidgets(widgets, screenWidthReduceButtonSize, ViewGroup.LayoutParams.WRAP_CONTENT);
        emptyTV.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
    }

    private void setTitleStyle(ArrayList<TextView> titlesTV, LinearLayout titleLL) {
        for (TextView titletv : titlesTV) {
            UIService.setHeaderProperties(titletv, 15, true);
            titleLL.addView(titletv);
        }
    }

    //    @TargetApi(Build.VERSION_CODES.O)
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setBudgetGui() {
        this.budgets = new ArrayList<>(dbService.getBudgetDataFromDB(dbService.getMaxBudgetNumber()));
        if (this.budgets.size() == 0)
            this.budgets.add(new Budget("", 0, false, "", 2, budgets.size() + 1));
        this.adapter = new CreateBudgetViewAdapter(this, budgets);
        this.budgetsRowsRecycler = findViewById(R.id.budgets_rows);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(budgetsRowsRecycler);
        budgetsRowsRecycler.setAdapter(adapter);
        budgetsRowsRecycler.setLayoutManager(new LinearLayoutManager(this));
//        setTitleRow(); // index 1
//        LLMain.addView(LLBudgets);
//        allBudgets = dbService.getBudgetDataFromDB(dbService.getMaxBudgetNumber());
//        for (Budget budget : allBudgets) {
//            add_New_row(budget.getCategoryName(), budget.getValue(), budget.isConstPayment(), budget.getShop(), budget.getChargeDay());
//        }
//        if (allBudgets.size() == 0) // No any budget exists
//            add_New_row("", 0, false, "", 2);
//        setAddAndDeleteButton(); // add row and clean buttons
//        setCloseButton();// Adding close button (last index)
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
        UIService.setDaysInMonthSpinner(optionalDaysSpinner, this);

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

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getAdapterPosition();
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = position == 0 ? 0 : ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (budgets.size() < 2)
                return;
            budgets.remove(viewHolder.getAdapterPosition());
            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreateBudgetClicked(View view) {
        {
            setBudgets();
            if (allBudgets.size() == 0) {// Nothing needed to do
                showMessageNoButton(getString(R.string.please_insert_budget));
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
                createBudget(Definition.CREATE_CODE);// First time create budget
            else if (isOriginContentBudgetChanged) {// Rewriting of monthly budget needed
                if (dbService.isCurrentRefMonthExists())
                    showQuestionDeleteCurrentMonth(getString(R.string.create_budget_question));
                return;
            } else if (isAddedBudgetsExists)// Insert the added budgets needed only
                createBudget(Definition.ADD_CODE);// Values of old budget updated only
        }
    }

    public void addInputRow(View view) {
        Budget budget = new Budget("", 0, false, "", 2, budgets.size() + 1);
        budgets.add(budget);
        this.adapter.notifyItemInserted(budgets.size() - 1);
    }

}

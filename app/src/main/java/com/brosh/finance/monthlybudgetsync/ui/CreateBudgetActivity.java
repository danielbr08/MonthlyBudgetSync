package com.brosh.finance.monthlybudgetsync.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.CreateBudgetViewAdapter;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CreateBudgetActivity extends AppCompatActivity {
    private static final String TAG = "CreateBudgetActivity";

    private DBUtil dbUtil;
    DatabaseReference DatabaseReferenceUserMonthlyBudget;
    private Month month;

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
    private User user;

    private SwipeRefreshLayout refreshLayout;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);

        user = (User) getIntent().getExtras().getSerializable(Definitions.USER);
        if (user.getUserSettings().isAdEnabled()) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        String userKey = user.getDbKey();
        String refMonth = getIntent().getExtras().getString(Definitions.MONTH);
        dbUtil = DBUtil.getInstance();
        month = dbUtil.getMonth(refMonth);
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);

        adapter = new CreateBudgetViewAdapter(this, budgets);
        budgetsRowsRecycler = findViewById(R.id.budgets_rows);

        DatabaseReferenceUserMonthlyBudget = DBUtil.getDatabase().getReference(Definitions.MONTHLY_BUDGET).child(userKey);
        LLMain = findViewById(R.id.LLMainCreateBudget);
        LLBudgets = new LinearLayout(this);
        LLBudgets.setOrientation(LinearLayout.VERTICAL);
        allBudgets = new ArrayList<>();
        allCategories = new ArrayList<>();
        dfaultBackground = new View(this).getBackground();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Rotate the screen to to be on portrait moade only
        display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        setRefreshListener();
        setBudgetGui();
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_create_budget);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setBudgetGui();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setAddAndDeleteButton() {
        final LinearLayout newll = new LinearLayout(CreateBudgetActivity.this);
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
        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(addRowButton);//,lp);
        newll.addView(deleteRowsButton);
        newll.addView(emptyTV);
        LLMain.addView(newll);
    }

    public void writeBudget(int budgetNumber, final List<Budget> budgets) {
        String budgetNumberStr = String.valueOf(budgetNumber);
        for (Budget budget : budgets) {
            String budgetId = dbUtil.getDBBudgetsPath().child(budgetNumberStr).push().getKey();
            budget.setId(budgetId);
            dbUtil.updateSpecificBudget(String.valueOf(budgetNumber), budget);
        }
    }

    public void setBudgets() {
        allCategories.clear();
        allBudgets.clear();
        int catPriority = 1;
        for (int i = 0; i < budgetsRowsRecycler.getChildCount(); i++) {
            EditText categoryET, valueET, shopET;
            CheckBox constPaymentCB;
            TextView chargeDayTV;
            int j = 0;
            LinearLayout row = (LinearLayout) budgetsRowsRecycler.getChildAt(i);
            categoryET = (EditText) row.getChildAt(j++);
            valueET = (EditText) row.getChildAt(j++);
            constPaymentCB = (CheckBox) row.getChildAt(j++);
            shopET = (EditText) row.getChildAt(j++);
            chargeDayTV = (TextView) row.getChildAt(j);

            String category = categoryET.getText().toString().trim();
            String valueStr = valueET.getText().toString().trim().replace(Definitions.COMMA, "");
            boolean constPayment = constPaymentCB.isChecked();
            String shop = shopET.getText().toString().trim();
            String chargeDayStr = chargeDayTV.getText().toString().trim();
            int chargeDay = 1;

            if (!constPayment) {
                shopET.setText(R.string.empty);
                shop = null;
                chargeDayStr = getString(R.string.one);
            }
            chargeDay = Integer.valueOf(chargeDayStr);
            if (valueStr.equals(getString(R.string.empty)))
                valueStr = getString(R.string.one);
            int value = Integer.valueOf(valueStr);
            allCategories.add(category);
            verifyBudgetInput(categoryET, valueET, constPaymentCB, shopET, chargeDayTV);
            if (isInputValid)
                allBudgets.add(new Budget(category, value, constPayment, shop, chargeDay, catPriority++));
            else
                return;
        }
        budgets = allBudgets;
    }

    public void verifyBudgetInput(EditText categoryET, EditText valueET, CheckBox constPaymentCB, EditText shopET, TextView chargeDayTV) {//EditText chargeDayET) {
        isInputValid = true;
        String category = categoryET.getText().toString().trim();
        String valueStr = valueET.getText().toString().trim().replace(Definitions.COMMA, getString(R.string.empty));
        boolean constPayment = constPaymentCB.isChecked();
        String shop = shopET.getText().toString().trim();

        if (valueStr.equals(getString(R.string.empty)))
            valueStr = getString(R.string.zero);
        int value = Integer.valueOf(valueStr);

        //Check duplicate of category
        if (Collections.frequency(allCategories, category) > 1) {
            setErrorEditText(categoryET, getString(R.string.duplicate_category));
            isInputValid = false;
        }

        //Check illegal characters
        if (category.contains(TextUtil.getSeperator())) {
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
        if (shop.contains(TextUtil.getSeperator())) {
            setErrorEditText(shopET, getString(R.string.please_insert_value));
            isInputValid = false;
        }

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
                        createBudget(Definitions.DELETE_CODE);
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
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
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
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
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
        List<Budget> oldBudget = dbUtil.getBudgetDataFromDB(budgetNumber);
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
        int budgetNumber = dbUtil.getMaxBudgetNumber() + 1;
        ArrayList<Budget> addedBudgets = new ArrayList<>();
        if (operation.equals(Definitions.ADD_CODE))
            addedBudgets = getAddedBudgets(budgetNumber - 1);
        else if (operation.equals(Definitions.DELETE_CODE))
            dbUtil.deleteDataRefMonth(month.getYearMonth());

        String refMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), getString(R.string.seperator));
        writeBudget(budgetNumber, allBudgets);
        writeBudgetsToTreeFB(budgetNumber);

        if (operation.equals(Definitions.ADD_CODE)) {
            dbUtil.updateBudgetNumber(refMonth, budgetNumber);
            dbUtil.addNewCategoriesToExistingMonth(refMonth, budgetNumber, addedBudgets);
            dbUtil.updateBudgetNumberFB(refMonth, budgetNumber);
            dbUtil.updateShopsFB();

        } else
            dbUtil.createNewMonth(budgetNumber, refMonth);

        //deleteCurrentMonth();
//        month = null;
        TextUtil.showMessage(getString(R.string.budget_created_successfully), Toast.LENGTH_LONG, getApplicationContext());
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
        Map<String, Budget> hmBudgets = dbUtil.getBudget(budgetNumberStr);
        dbUtil.getDBBudgetsPath().child(budgetNumberStr).setValue(hmBudgets);
    }

    private void questionFalseAnswer() {
    }

    public void setBudgetGui() {
        this.budgets = new ArrayList<>(dbUtil.getBudgetDataFromDB(dbUtil.getMaxBudgetNumber()));
        if (this.budgets.size() == 0)
            this.budgets.add(new Budget("", 0, false, "", 2, budgets.size() + 1));
        this.adapter = new CreateBudgetViewAdapter(this, budgets);
        this.budgetsRowsRecycler = findViewById(R.id.budgets_rows);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(budgetsRowsRecycler);
        budgetsRowsRecycler.setAdapter(adapter);
        budgetsRowsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void add_New_row(String categoryName, int categoryValue, boolean isConstPayment, String shop, int chargeDay) {
        final LinearLayout newll = new LinearLayout(CreateBudgetActivity.this);
        final EditText categoryNameET = new EditText(CreateBudgetActivity.this),
                categoryValueET = new EditText(CreateBudgetActivity.this),
                shopET = new EditText(CreateBudgetActivity.this);
        final Spinner optionalDaysSpinner = new Spinner(CreateBudgetActivity.this);
        final CheckBox constPaymentCB = new CheckBox(CreateBudgetActivity.this);

        // todo check the width of widgets
        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        List<View> rowViews = Arrays.asList(categoryNameET, categoryValueET, constPaymentCB, shopET, optionalDaysSpinner);
        List<String> viewsText = Arrays.asList(categoryName, String.valueOf(categoryValue), String.valueOf(isConstPayment), shop, String.valueOf(chargeDay - 1));
        List<View> textInputType = Arrays.asList(categoryNameET, constPaymentCB, shopET);
        List<View> numberInputType = Arrays.asList(categoryValueET);
        UiUtil.setViewsText(rowViews, viewsText);
        UiUtil.setTxtSize(rowViews, 12);
        setViewsInput(textInputType, numberInputType);
        UiUtil.setInputFocus(categoryNameET);
        UiUtil.setDaysInMonthSpinner(optionalDaysSpinner, this);

        UiUtil.setWidthCreateBudgetPageDataWidgets(rowViews, screenWidthReduceButtonSize, ViewGroup.LayoutParams.WRAP_CONTENT);
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
            UiUtil.setViewInputTypeText(view);
        }
        for (View view : numberInputType) {
            UiUtil.setViewInputTypeNumber(view);
        }
    }

    private void setConstPaymentCBOnCheckChangedListner(CheckBox constPaymentCB, final EditText shopET, final Spinner optionalDaysSpinner) {
        constPaymentCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
                shopET.setVisibility(visibility);
                optionalDaysSpinner.setVisibility(visibility);
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
            int budgetNumber = dbUtil.getMaxBudgetNumber();
            ArrayList<Budget> newBudgets = getAddedBudgets(budgetNumber);
            boolean isOriginContentBudgetChanged = isOriginBudgetChanged(budgetNumber);
            boolean isBudgetChange = isBudgetChange(budgetNumber);
            boolean isAddedBudgetsExists = newBudgets.size() > 0;
            if (!isInputValid || !isBudgetChange)
                return;
            if (month == null)
                createBudget(Definitions.CREATE_CODE);// First time create budget
            else if (isOriginContentBudgetChanged) {// Rewriting of monthly budget needed
                if (dbUtil.isCurrentRefMonthExists())
                    showQuestionDeleteCurrentMonth(getString(R.string.create_budget_question));
                return;
            } else if (isAddedBudgetsExists)// Insert the added budgets needed only
                createBudget(Definitions.ADD_CODE);// Values of old budget updated only
        }
    }

    public void addInputRow(View view) {
        Budget budget = new Budget("", 0, false, "", 2, budgets.size() + 1);
        budgets.add(budget);
        this.adapter.notifyItemInserted(budgets.size() - 1);
    }

}

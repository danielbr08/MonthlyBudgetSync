package com.brosh.finance.monthlybudgetsync.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CreateBudgetActivity extends AppCompatActivity {
    private DBUtil dbUtil;
    private Month month;

    private List<Budget> allBudgets;
    private List<Budget> budgets;
    private ArrayList<String> allCategories;
    private boolean isInputValid;
    private int screenWidth;
    private int buttonSize;

    private Drawable defaultBackground;

    CreateBudgetViewAdapter adapter;
    RecyclerView budgetsRowsRecycler;

    private LinearLayout LLMain;
    private LinearLayout LLBudgets;

    private SwipeRefreshLayout refreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);

        User user = DBUtil.getInstance().getUser();
        if (user.getUserSettings().isAdEnabled()) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
        String userKey = user.getDbKey();
        Bundle extras = getIntent().getExtras();
        String refMonth = extras != null ? extras.getString(Definitions.MONTH) : null;
        dbUtil = DBUtil.getInstance();
        month = dbUtil.getMonth(refMonth);
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);

        adapter = new CreateBudgetViewAdapter(this, budgets);
        budgetsRowsRecycler = findViewById(R.id.budgets_rows);

        if (userKey != null) {
            DBUtil.getDatabase().getReference(Definitions.MONTHLY_BUDGET).child(userKey);
        }
        LLMain = findViewById(R.id.LLMainCreateBudget);
        LLBudgets = new LinearLayout(this);
        LLBudgets.setOrientation(LinearLayout.VERTICAL);
        allBudgets = new ArrayList<>();
        allCategories = new ArrayList<>();
        defaultBackground = new View(this).getBackground();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        buttonSize = 100; // Fixed button size in pixels

        setRefreshListener();
        setBudgetGui();
    }

    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_create_budget);
        refreshLayout.setOnRefreshListener(() -> {
            TextView tv = new TextView(this);
            tv.setTextColor(ContextCompat.getColor(this, R.color.colorLoginBackground));
            tv.setText(R.string.are_you_sure_you_want_to_refresh);
            tv.setPadding(40, 40, 40, 0);
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setCustomTitle(tv)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.yes), (dialog, id) -> { // Negative is actually positive
                        setBudgetGui();
                        refreshLayout.setRefreshing(false);
                    })
                    .setPositiveButton(getString(R.string.no), (dialogInterface, i) -> refreshLayout.setRefreshing(false)) // Positive is actually negative
                    .show();
        });
    }

    @SuppressWarnings("unused")
    public void setAddAndDeleteButton() {
        final LinearLayout newll = new LinearLayout(CreateBudgetActivity.this);
        newll.setOrientation(LinearLayout.HORIZONTAL);

        final ImageButton addRowButton = new ImageButton(this);
        final ImageButton deleteRowsButton = new ImageButton(this);
        final TextView emptyTV = new TextView(this);

        //Set default background color
        addRowButton.setBackground(defaultBackground);
        deleteRowsButton.setBackground(defaultBackground);

        addRowButton.setOnClickListener(view -> {
            int budgetSize = LLBudgets.getChildCount() - 1;
            LinearLayout lastBudgetRow = (LinearLayout) LLBudgets.getChildAt(budgetSize);
            int categoryNameIndex = 1;
            int categoryValueIndex = 2;
            EditText categoryNameET = (EditText) lastBudgetRow.getChildAt(categoryNameIndex);
            EditText categoryValueET = (EditText) lastBudgetRow.getChildAt(categoryValueIndex);
            boolean isLastRowValid = !categoryNameET.getText().toString().trim().isEmpty() && !categoryValueET.getText().toString().trim().isEmpty();
            if (isLastRowValid)
                add_New_row(null, 0, false, null, 0);
        });

        deleteRowsButton.setOnClickListener(view -> {
            LLBudgets.removeAllViews();
            add_New_row(null, 0, false, null, 0);
        });
        deleteRowsButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.clean_screen));
        deleteRowsButton.setScaleType(ImageView.ScaleType.FIT_XY);

        addRowButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.add_button_md));
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

            if (!constPayment) {
                shopET.setText(R.string.empty);
                shop = null;
                chargeDayStr = getString(R.string.one);
            }
            int chargeDay = Integer.parseInt(chargeDayStr);
            if (valueStr.equals(getString(R.string.empty)))
                valueStr = getString(R.string.one);
            int value = Integer.parseInt(valueStr);
            allCategories.add(category);
            verifyBudgetInput(categoryET, valueET, constPaymentCB, shopET);
            if (isInputValid)
                allBudgets.add(new Budget(category, value, constPayment, shop, chargeDay, catPriority++));
            else
                return;
        }
        budgets = allBudgets;
    }

    public void verifyBudgetInput(EditText categoryET, EditText valueET, CheckBox constPaymentCB, EditText shopET) {
        isInputValid = true;
        String category = categoryET.getText().toString().trim();
        String valueStr = valueET.getText().toString().trim().replace(Definitions.COMMA, getString(R.string.empty));
        boolean constPayment = constPaymentCB.isChecked();
        String shop = shopET.getText().toString().trim();

        if (valueStr.equals(getString(R.string.empty)))
            valueStr = getString(R.string.zero);
        int value = Integer.parseInt(valueStr);

        //Check duplicate of category
        if (Collections.frequency(allCategories, category) > 1) {
            setErrorEditText(categoryET, getString(R.string.duplicate_category));
            isInputValid = false;
        }

        //Check illegal characters
        if (category.contains(TextUtil.getSeparator())) {
            setErrorEditText(categoryET, getString(R.string.illegal_Character));
            isInputValid = false;
        }
        //Check illegal category
        if (category.isEmpty()) {
            setErrorEditText(categoryET, getString(R.string.please_insert_category));
            isInputValid = false;
        }
        //Check illegal value
        if (value == 0) {
            setErrorEditText(valueET, getString(R.string.please_insert_value));
            isInputValid = false;
        }

        if (constPayment && shop.isEmpty()) {
            setErrorEditText(shopET, getString(R.string.please_insert_store));
            isInputValid = false;
        }

        //Check illegal characters
        if (shop.contains(TextUtil.getSeparator())) {
            setErrorEditText(shopET, getString(R.string.please_insert_value));
            isInputValid = false;
        }
    }

    public void setErrorEditText(EditText et, String errorMessage) {
        et.setError(errorMessage);
    }

    public void showQuestionDeleteCurrentMonth(String message) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
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
        handler.postDelayed(() -> myAlert.create().dismiss(), 1000); // 1000 milliseconds delay
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
            if (!isBudgetsEquals)
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
            if (!isBudgetExists)
                addedBudgets.add(bgt);
            isBudgetExists = false;
        }
        return addedBudgets;
    }

    private void createBudget(String operation) {
        int budgetNumber = dbUtil.getMaxBudgetNumber() + 1;
        ArrayList<Budget> addedBudgets = new ArrayList<>();
        if (operation.equals(Definitions.ADD_CODE))
            addedBudgets = getAddedBudgets(budgetNumber - 1);
        else if (operation.equals(Definitions.DELETE_CODE))
            dbUtil.deleteDataRefMonth(month.getYearMonth());

        String refMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), getString(R.string.separator));
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

    @SuppressWarnings("unused")
    private List<Category> budgetToCategories(List<Budget> budgets) {
        List<Category> categories = new ArrayList<>();
        for (Budget budget : budgets) {
            Category cat = new Category("", budget.getCategoryName(), budget.getValue(), budget.getValue());
            categories.add(cat);
        }
        return categories;
    }

    private void writeBudgetsToTreeFB(final int budgetNumber) {
        String budgetNumberStr = String.valueOf(budgetNumber);
        Map<String, Budget> hmBudgets = dbUtil.getBudget(budgetNumberStr);
        dbUtil.getDBBudgetsPath().child(budgetNumberStr).setValue(hmBudgets);
    }

    private void questionFalseAnswer() {
    }

    public void setBudgetGui() {
        this.budgets = new ArrayList<>(dbUtil.getBudgetDataFromDB(dbUtil.getMaxBudgetNumber()));
        if (this.budgets.isEmpty())
            this.budgets.add(new Budget("", 0, false, "", 2, budgets.size() + 1));
        this.adapter = new CreateBudgetViewAdapter(this, budgets);
        this.budgetsRowsRecycler = findViewById(R.id.budgets_rows);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(budgetsRowsRecycler);
        budgetsRowsRecycler.setAdapter(adapter);
        budgetsRowsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    public void add_New_row(String categoryName, int categoryValue, boolean isConstPayment, String shop, int chargeDay) {
        final LinearLayout newll = new LinearLayout(CreateBudgetActivity.this);
        final EditText categoryNameET = new EditText(CreateBudgetActivity.this),
                categoryValueET = new EditText(CreateBudgetActivity.this),
                shopET = new EditText(CreateBudgetActivity.this);
        final Spinner optionalDaysSpinner = new Spinner(CreateBudgetActivity.this);
        final CheckBox constPaymentCB = new CheckBox(CreateBudgetActivity.this);

        int screenWidthReduceButtonSize = screenWidth - buttonSize;
        List<View> rowViews = Arrays.asList(categoryNameET, categoryValueET, constPaymentCB, shopET, optionalDaysSpinner);
        List<String> viewsText = Arrays.asList(categoryName, String.valueOf(categoryValue), String.valueOf(isConstPayment), shop, String.valueOf(chargeDay - 1));
        List<View> textInputType = Arrays.asList(categoryNameET, constPaymentCB, shopET);
        List<View> numberInputType = Collections.singletonList(categoryValueET);
        UiUtil.setViewsText(rowViews, viewsText);
        UiUtil.setTxtSize(rowViews, 12);
        setViewsInput(textInputType, numberInputType);
        UiUtil.setInputFocus(categoryNameET);
        UiUtil.setDaysInMonthSpinner(optionalDaysSpinner, this);

        UiUtil.setWidthCreateBudgetPageDataWidgets(rowViews, screenWidthReduceButtonSize, ViewGroup.LayoutParams.WRAP_CONTENT);
        setConstPaymentCBOnCheckChangedListener(constPaymentCB, shopET, optionalDaysSpinner);


        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);

        optionalDaysSpinner.setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(categoryNameET.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(categoryValueET.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(shopET.getWindowToken(), 0);
            v.performClick();
            return false;
        });

        final ImageButton deleteRowButton = new ImageButton(this);
        deleteRowButton.setOnClickListener(view -> LLBudgets.removeView(newll));
        deleteRowButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.delete_icon));
        deleteRowButton.setBackground(defaultBackground);
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

    private void setConstPaymentCBOnCheckChangedListener(CheckBox constPaymentCB, final EditText shopET, final Spinner optionalDaysSpinner) {
        constPaymentCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
            shopET.setVisibility(visibility);
            optionalDaysSpinner.setVisibility(visibility);
        });
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getBindingAdapterPosition();
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = position == 0 ? 0 : ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            adapter.notifyItemMoved(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (budgets.size() < 2)
                return;
            int position = viewHolder.getBindingAdapterPosition();
            budgets.remove(position);
            adapter.notifyItemRemoved(position);
        }

    };

    public void onCreateBudgetClicked(View view) {
        setBudgets();
        if (allBudgets.isEmpty()) { // Nothing needed to do
            showMessageNoButton(getString(R.string.please_insert_budget));
            return;
        }
        int budgetNumber = dbUtil.getMaxBudgetNumber();
        ArrayList<Budget> newBudgets = getAddedBudgets(budgetNumber);
        boolean isOriginContentBudgetChanged = isOriginBudgetChanged(budgetNumber);
        boolean isBudgetChange = isBudgetChange(budgetNumber);
        boolean isAddedBudgetsExists = !newBudgets.isEmpty();
        if (!isInputValid || !isBudgetChange)
            return;
        if (month == null) {
            createBudget(Definitions.CREATE_CODE);// First time create budget
        } else if (isOriginContentBudgetChanged) {// Rewriting of monthly budget needed
            if (dbUtil.isCurrentRefMonthExists())
                showQuestionDeleteCurrentMonth(getString(R.string.create_budget_question));
        } else if (isAddedBudgetsExists) {// Insert the added budgets needed only
            createBudget(Definitions.ADD_CODE);// Values of old budget updated only
        }
    }

    public void addInputRow(View view) {
        Budget budget = new Budget("", 0, false, "", 2, budgets.size() + 1);
        budgets.add(budget);
        this.adapter.notifyItemInserted(budgets.size() - 1);
    }

}

package com.brosh.finance.monthlybudgetsync.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.PaginatedYearAdapter;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.login.Login;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main activity of the application.
 * Provides navigation to budget, transactions, and other features.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_CHECKBOX = "checkbox";
    private static final String PREFS_REMEMBER_ME = "rememberMe";
    private static final String PREFS_EMAIL = "email";
    private static final String PREFS_PASSWORD = "password";

    @Nullable
    private InterstitialAd interstitialAd;
    private DBUtil dbUtil;
    private String userKey;
    @Nullable
    private User user;

    // UI components
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private View insertTransactionButton;
    private View budgetButton;
    private View transactionsButton;
    private View createBudgetButton;
    private SwipeRefreshLayout refreshLayout;
    @Nullable private TextView userLoggedInTV;
    
    // Activity state
    private boolean isDestroyed = false;
    
    // Month selection state
    private List<String> allMonths;
    private List<String> availableYears;
    private String selectedYear;
    private boolean isSpinnerInitializing;
    private PaginatedYearAdapter paginatedYearAdapter;
    
    // State
    @Nullable private Month month;

    /**
     * Initializes the year and month spinners with available months.
     * Uses a two-level approach: select year first, then month.
     * Year spinner uses pagination when there are many years (5+).
     */
    public void initRefMonthSpinner() {
        if (yearSpinner == null || monthSpinner == null || dbUtil == null) {
            Log.w(TAG, "Cannot init spinners - null references");
            return;
        }
        
        allMonths = dbUtil.getAllMonthsYearMonth();
        if (allMonths == null || allMonths.isEmpty()) {
            return;
        }
        
        // Extract unique years and sort descending (newest first)
        availableYears = extractUniqueYears(allMonths);
        if (availableYears.isEmpty()) {
            return;
        }
        
        isSpinnerInitializing = true;
        
        // Determine initial year selection
        String currentYearMonth = month != null ? month.getYearMonth() : null;
        String initialYear = availableYears.get(0); // Default to first (most recent)
        
        if (currentYearMonth != null && currentYearMonth.contains(Config.SEPARATOR)) {
            String yearFromMonth = currentYearMonth.split(Config.SEPARATOR)[0];
            if (availableYears.contains(yearFromMonth)) {
                initialYear = yearFromMonth;
            }
        }
        
        // Setup year spinner with pagination support
        paginatedYearAdapter = new PaginatedYearAdapter(this, availableYears);
        
        // Position window to show the initial year
        paginatedYearAdapter.showYear(initialYear);
        yearSpinner.setAdapter(paginatedYearAdapter);
        
        // Select the initial year in the spinner
        int yearPosition = paginatedYearAdapter.getYearPosition(initialYear);
        if (yearPosition >= 0) {
            yearSpinner.setSelection(yearPosition);
        }
        
        selectedYear = initialYear;
        
        // Setup month spinner for selected year
        updateMonthSpinnerForYear(initialYear, currentYearMonth);
        
        isSpinnerInitializing = false;
    }
    
    /**
     * Extracts unique years from month list and sorts them descending.
     */
    private List<String> extractUniqueYears(List<String> months) {
        List<String> years = new ArrayList<>();
        for (String yearMonth : months) {
            if (yearMonth != null && yearMonth.contains(Config.SEPARATOR)) {
                String year = yearMonth.split(Config.SEPARATOR)[0];
                if (!years.contains(year)) {
                    years.add(year);
                }
            }
        }
        // Sort descending (newest first)
        years.sort(java.util.Collections.reverseOrder());
        return years;
    }
    
    /**
     * Updates month spinner with months available in the selected year.
     */
    private void updateMonthSpinnerForYear(String year, @Nullable String selectYearMonth) {
        List<String> monthsForYear = getMonthsForYear(year);
        if (monthsForYear.isEmpty()) {
            return;
        }
        
        // Convert month numbers to display names
        List<String> displayMonths = new ArrayList<>();
        for (String ym : monthsForYear) {
            displayMonths.add(formatMonthForDisplay(ym));
        }
        
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner, displayMonths);
        monthAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        
        // Select appropriate month
        int monthPosition = 0;
        if (selectYearMonth != null) {
            String displayName = formatMonthForDisplay(selectYearMonth);
            int idx = displayMonths.indexOf(displayName);
            if (idx >= 0) {
                monthPosition = idx;
            }
        }
        monthSpinner.setSelection(monthPosition);
    }
    
    /**
     * Gets all months (yearMonth format) for a specific year.
     */
    private List<String> getMonthsForYear(String year) {
        List<String> result = new ArrayList<>();
        if (allMonths == null) return result;
        
        for (String ym : allMonths) {
            if (ym != null && ym.startsWith(year + Config.SEPARATOR)) {
                result.add(ym);
            }
        }
        // Sort descending (newest month first)
        result.sort(java.util.Collections.reverseOrder());
        return result;
    }
    
    /**
     * Formats a yearMonth string for display (e.g., "2024-01" -> "01 - Jan").
     */
    private String formatMonthForDisplay(String yearMonth) {
        if (yearMonth == null || !yearMonth.contains(Config.SEPARATOR)) {
            return yearMonth != null ? yearMonth : "";
        }
        
        String[] parts = yearMonth.split(Config.SEPARATOR);
        if (parts.length < 2) return yearMonth;
        
        String monthNum = parts[1];
        String monthName = getMonthName(monthNum);
        return monthNum + " - " + monthName;
    }
    
    /**
     * Gets the localized month name from month number.
     * Uses the device's locale for proper localization.
     */
    private String getMonthName(String monthNum) {
        try {
            int idx = Integer.parseInt(monthNum) - 1;
            if (idx >= 0 && idx < 12) {
                java.text.DateFormatSymbols symbols = new java.text.DateFormatSymbols(java.util.Locale.getDefault());
                String[] monthNames = symbols.getShortMonths();
                return monthNames[idx];
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
        return monthNum;
    }
    
    /**
     * Handles selection of pagination markers in the year spinner.
     * Loads more years WITHOUT auto-selecting - lets user browse and pick.
     *
     * @param marker The pagination marker that was selected
     */
    private void handleYearPaginationMarker(String marker) {
        if (paginatedYearAdapter == null) return;
        
        isSpinnerInitializing = true;
        
        if (PaginatedYearAdapter.isLoadNewerMarker(marker)) {
            // Load newer (more recent) years
            paginatedYearAdapter.loadNewer();
        } else if (PaginatedYearAdapter.isLoadOlderMarker(marker)) {
            // Load older years
            paginatedYearAdapter.loadOlder();
        } else {
            isSpinnerInitializing = false;
            return;
        }
        
        // Re-set the adapter to refresh the spinner
        yearSpinner.setAdapter(paginatedYearAdapter);
        
        // Try to keep the previously selected year visible if it's in the new window
        int previousPosition = paginatedYearAdapter.getYearPosition(selectedYear);
        if (previousPosition >= 0) {
            // Previously selected year is still visible - keep it selected
            yearSpinner.setSelection(previousPosition);
        } else {
            // Select the first real year (skip any pagination marker at top)
            String firstYear = paginatedYearAdapter.getFirstVisibleYear();
            if (firstYear != null) {
                int firstPosition = paginatedYearAdapter.getYearPosition(firstYear);
                yearSpinner.setSelection(Math.max(0, firstPosition));
            }
        }
        
        isSpinnerInitializing = false;
        
        // Re-open the dropdown so user can continue browsing and select
        yearSpinner.postDelayed(() -> yearSpinner.performClick(), 100);
    }
    
    /**
     * Gets the yearMonth key from the current spinner selections.
     */
    private String getSelectedYearMonth() {
        if (yearSpinner == null || monthSpinner == null) return null;
        
        Object yearObj = yearSpinner.getSelectedItem();
        Object monthObj = monthSpinner.getSelectedItem();
        
        if (yearObj == null || monthObj == null) return null;
        
        String year = yearObj.toString();
        String monthDisplay = monthObj.toString();
        
        // Extract month number from display format "01 - Jan"
        String monthNum = monthDisplay.split(" - ")[0];
        
        return year + Config.SEPARATOR + monthNum;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        try {
            if (itemId == R.id.settingsItem) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                addParametersToActivity(intent);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.shareItem) {
                openShareDialog();
                return true;
            } else if (itemId == R.id.recommend_to_friend) {
                shareAppWithFriend();
                return true;
            } else if (itemId == R.id.app_guide) {
                openAppGuide();
                return true;
            } else if (itemId == R.id.contactUsItem) {
                Intent intent = new Intent(getApplicationContext(), ContactUsActivity.class);
                startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu item: " + e.getMessage(), e);
            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, this);
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Shares the app URL with a friend via share intent.
     */
    private void shareAppWithFriend() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, Config.APP_URL);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
    
    /**
     * Opens the app guide YouTube video.
     */
    private void openAppGuide() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.tutorial_app_youtube)));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Could not open app guide", e);
            TextUtil.showMessage("Could not open guide", Toast.LENGTH_SHORT, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle back press with confirmation dialog
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFinishing()) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    return;
                }
                
                // Show confirmation dialog before exiting
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.are_you_sure_you_want_to_exit)
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
        });

        // Let the system handle orientation based on device capabilities

        // Initialize ads
        MobileAds.initialize(this, initializationStatus -> {});
        initAdFields();

        // Get user data
        dbUtil = DBUtil.getInstance();
        user = dbUtil.getUser();
        
        if (user == null) {
            Log.e(TAG, "User is null, redirecting to login");
            redirectToLogin();
            return;
        }
        
        // Setup ads visibility
        if (user.getUserSettings().isAdEnabled()) {
            UiUtil.addAdvertiseToActivity(this);
        } else {
            View adView = findViewById(R.id.adView);
            if (adView != null) {
                adView.setVisibility(View.GONE);
            }
        }
        
        userKey = user.getDbKey();
        initializeViews();
        setUserNameLabel();
        refresh();
        setRefreshListener();
        setupButtonListeners();
        setupSpinnerListener();
        
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);
    }
    
    /**
     * Initializes all view references.
     */
    private void initializeViews() {
        yearSpinner = findViewById(R.id.yearSpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        insertTransactionButton = findViewById(R.id.insertTransactionButton);
        budgetButton = findViewById(R.id.budgetButton);
        transactionsButton = findViewById(R.id.transactionsButton);
        createBudgetButton = findViewById(R.id.createBudgetButton);
    }
    
    /**
     * Sets up click listeners for all navigation buttons.
     */
    private void setupButtonListeners() {
        budgetButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BudgetActivity.class);
            addParametersToActivity(intent);
            startActivity(intent);
        });
        
        insertTransactionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), InsertTransactionActivity.class);
            addParametersToActivity(intent);
            startActivity(intent);
        });
        
        transactionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TransactionsActivity.class);
            addParametersToActivity(intent);
            startActivity(intent);
        });
        
        createBudgetButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateBudgetActivity.class);
            addParametersToActivity(intent);
            startActivity(intent);
        });
    }
    
    /**
     * Sets up the year and month spinner selection listeners.
     */
    private void setupSpinnerListener() {
        // Year spinner listener - updates month spinner when year changes
        // Also handles pagination markers to load more years
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (isSpinnerInitializing) return;
                
                Object selectedItem = yearSpinner.getSelectedItem();
                if (selectedItem == null) return;
                
                String itemValue = selectedItem.toString();
                
                // Handle pagination markers
                if (PaginatedYearAdapter.isPaginationMarker(itemValue)) {
                    handleYearPaginationMarker(itemValue);
                    return;
                }
                
                // Normal year selection
                String newYear = itemValue;
                if (!newYear.equals(selectedYear)) {
                    selectedYear = newYear;
                    // Update month spinner for new year, select first month
                    List<String> monthsForYear = getMonthsForYear(newYear);
                    if (!monthsForYear.isEmpty()) {
                        updateMonthSpinnerForYear(newYear, monthsForYear.get(0));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action needed
            }
        });
        
        // Month spinner listener - loads selected month data
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (isSpinnerInitializing) return;
                
                String yearMonth = getSelectedYearMonth();
                if (yearMonth == null) return;
                
                month = dbUtil.getMonth(yearMonth);
                boolean isActive = month != null && month.isActive();
                updateButtonStates(isActive);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action needed
            }
        });
    }
    
    /**
     * Updates button enabled states and backgrounds.
     */
    private void updateButtonStates(boolean isActive) {
        insertTransactionButton.setEnabled(isActive);
        createBudgetButton.setEnabled(isActive);
        
        // Update backgrounds for nav card style
        insertTransactionButton.setBackground(ContextCompat.getDrawable(this,
            isActive ? R.drawable.nav_card_insert : R.drawable.nav_card_disabled));
        createBudgetButton.setBackground(ContextCompat.getDrawable(this,
            isActive ? R.drawable.nav_card_create : R.drawable.nav_card_disabled));
        
        // Update alpha for visual feedback
        insertTransactionButton.setAlpha(isActive ? 1.0f : 0.5f);
        createBudgetButton.setAlpha(isActive ? 1.0f : 0.5f);
    }
    
    /**
     * Redirects user to login screen and finishes this activity.
     */
    private void redirectToLogin() {
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    /**
     * Logs out the current user and returns to login screen.
     */
    public void logout(View view) {
        try {
            // Clear saved credentials
            SharedPreferences preferences = getSharedPreferences(PREFS_CHECKBOX, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREFS_REMEMBER_ME, "false");
            editor.remove(PREFS_EMAIL);
            editor.remove(PREFS_PASSWORD);
            editor.apply();

            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();
            
            // Clear local data
            DBUtil.getInstance().clear();
            
            // Update UI
            if (userLoggedInTV != null) {
                userLoggedInTV.setText(getString(R.string.empty));
            }
            
            // Navigate to login
            redirectToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage(), e);
            // Still try to redirect to login even if there's an error
            redirectToLogin();
        }
    }

    /**
     * Opens a dialog to share budget with another user.
     */
    public void openShareDialog() {
        if (isFinishing() || isDestroyed) {
            return;
        }
        
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText emailInput = new EditText(this);
        emailInput.setHint(getString(R.string.please_enter_user_email_to_share));
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setTitle(getString(R.string.share_budget));

        builder.setView(emailInput);
        builder.setPositiveButton(getString(R.string.share), null);
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String emailText = TextUtil.safeTrim(emailInput.getText().toString());
            
            if (!TextUtil.isEmailValid(emailText)) {
                emailInput.setError(getString(R.string.invalid_email));
                return;
            }
            
            try {
                DBUtil.getInstance().share(emailText);
                TextUtil.showMessage(getString(R.string.successfully_shared), Toast.LENGTH_LONG, context);
                dialog.dismiss();
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg == null || errorMsg.isEmpty()) {
                    errorMsg = getString(R.string.error);
                }
                TextUtil.showMessage(errorMsg, Toast.LENGTH_LONG, context);
            }
        });
    }

    private void createNewMonth(Date refMonthDate) {
        String refMonth = DateUtil.getYearMonth(refMonthDate, Config.SEPARATOR);
        int budgetNumber = dbUtil.getMaxBudgetNumber();
        dbUtil.createNewMonth(budgetNumber, refMonth);
    }

    public void addParametersToActivity(Intent activity) {
        activity.putExtra(Definitions.MONTH, month == null ? null : month.getYearMonth());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (user == null || dbUtil == null) {
            return;
        }

        setUserNameLabel();
        
        // Show interstitial ad if enabled
        if (user.getUserSettings().isAdEnabled()) {
            showAD();
        }
        
        // Update UI based on budget state
        if (dbUtil.isAnyBudgetExists()) {
            handleBudgetExists();
        } else {
            handleNoBudget();
        }
    }
    
    /**
     * Handles UI state when at least one budget exists.
     */
    private void handleBudgetExists() {
        if (month == null && dbUtil.isCurrentRefMonthExists()) {
            // After creating budget for the first time
            month = dbUtil.getMonth(DateUtil.getYearMonth(DateUtil.getTodayDate(), Config.SEPARATOR));
            initRefMonthSpinner();
            enableAllButtons();
        }
    }
    
    /**
     * Handles UI state when no budgets exist.
     */
    private void handleNoBudget() {
        disableAllButtons();
        month = null;
    }
    
    /**
     * Enables all navigation buttons with active styling.
     */
    private void enableAllButtons() {
        budgetButton.setEnabled(true);
        transactionsButton.setEnabled(true);
        insertTransactionButton.setEnabled(true);
        createBudgetButton.setEnabled(true);
        
        budgetButton.setBackground(ContextCompat.getDrawable(this, R.drawable.nav_card_budget));
        transactionsButton.setBackground(ContextCompat.getDrawable(this, R.drawable.nav_card_transactions));
        insertTransactionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.nav_card_insert));
        createBudgetButton.setBackground(ContextCompat.getDrawable(this, R.drawable.nav_card_create));
        
        budgetButton.setAlpha(1.0f);
        transactionsButton.setAlpha(1.0f);
        insertTransactionButton.setAlpha(1.0f);
        createBudgetButton.setAlpha(1.0f);
    }
    
    /**
     * Disables all navigation buttons with inactive styling.
     */
    private void disableAllButtons() {
        Drawable inactiveDrawable = ContextCompat.getDrawable(this, R.drawable.nav_card_disabled);
        
        budgetButton.setEnabled(false);
        transactionsButton.setEnabled(false);
        insertTransactionButton.setEnabled(false);
        
        budgetButton.setBackground(inactiveDrawable);
        transactionsButton.setBackground(inactiveDrawable);
        insertTransactionButton.setBackground(inactiveDrawable);
        
        budgetButton.setAlpha(0.5f);
        transactionsButton.setAlpha(0.5f);
        insertTransactionButton.setAlpha(0.5f);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        
        // Clean up Firebase listeners to prevent memory leaks
        try {
            DBUtil dbUtilInstance = DBUtil.getInstance();
            if (dbUtilInstance != null) {
                ValueEventListener rootEventListener = dbUtilInstance.getRootEventListener();
                if (rootEventListener != null && userKey != null) {
                    Config.DatabaseReferenceMonthlyBudget.child(userKey).removeEventListener(rootEventListener);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error cleaning up listeners: " + e.getMessage());
        }
        
        // Clean up ad
        interstitialAd = null;
        
        super.onDestroy();
    }

    /**
     * Refreshes the main activity state and UI.
     */
    private void refresh() {
        if (dbUtil == null) {
            return;
        }
        
        if (dbUtil.isAnyBudgetExists()) {
            month = null;
            
            // Create current month if it doesn't exist
            if (!dbUtil.isCurrentRefMonthExists()) {
                createNewMonth(new Date());
                insertTransactionButton.setEnabled(true);
                insertTransactionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.nav_card_insert));
                insertTransactionButton.setAlpha(1.0f);
            }
            
            enableAllButtons();
            
            String currentYearMonth = DateUtil.getYearMonth(DateUtil.getTodayDate(), Config.SEPARATOR);
            month = dbUtil.getMonth(currentYearMonth);
            
            // Check if we need to create next month
            if (month != null && month.getRefMonth() != null) {
                Date nextRefMonth = DateUtil.getNextRefMonth(month.getRefMonth());
                if (nextRefMonth != null && new Date().after(nextRefMonth)) {
                    createNewMonth(nextRefMonth);
                    insertTransactionButton.setEnabled(true);
                    insertTransactionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.nav_card_insert));
                    insertTransactionButton.setAlpha(1.0f);
                }
            }
            
            initRefMonthSpinner();
        } else {
            disableAllButtons();
        }
    }

    /**
     * Sets up the pull-to-refresh listener.
     */
    private void setRefreshListener() {
        refreshLayout = findViewById(R.id.refresh_layout_main);
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                refresh();
                refreshLayout.setRefreshing(false);
            });
        }
    }

    /**
     * Initializes ad-related fields.
     */
    public void initAdFields() {
        loadInterstitialAd();
    }

    /**
     * Loads an interstitial ad.
     */
    private void loadInterstitialAd() {
        if (isDestroyed || isFinishing()) {
            return;
        }
        
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(this, getString(R.string.admob_transition_unit_id), adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd ad) {
                            if (isDestroyed) {
                                return;
                            }
                            interstitialAd = ad;
                            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    interstitialAd = null;
                                    loadInterstitialAd();
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.d(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                            interstitialAd = null;
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "Error loading interstitial ad", e);
        }
    }

    /**
     * Shows the interstitial ad if available.
     */
    public void showAD() {
        if (isDestroyed || isFinishing()) {
            return;
        }
        
        try {
            if (interstitialAd != null) {
                interstitialAd.show(this);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error showing ad", e);
        }
    }

    /**
     * Updates the user name label with current user information.
     */
    private void setUserNameLabel() {
        if (userLoggedInTV == null) {
            userLoggedInTV = findViewById(R.id.tv_user_logeed_in);
        }
        
        if (userLoggedInTV != null && user != null) {
            String userName = user.getName();
            if (userName == null || userName.isEmpty()) {
                userName = getString(R.string.empty);
            }
            userLoggedInTV.setText(String.format("%s %s", getString(R.string.logged_as), userName));
        }
    }
}
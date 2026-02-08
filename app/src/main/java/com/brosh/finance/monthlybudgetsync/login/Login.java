package com.brosh.finance.monthlybudgetsync.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.objects.UserStartApp;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.DialogHelper;
import com.brosh.finance.monthlybudgetsync.utils.FirebaseErrorHandler;
import com.brosh.finance.monthlybudgetsync.utils.NetworkUtil;
import com.brosh.finance.monthlybudgetsync.utils.PreferencesManager;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.brosh.finance.monthlybudgetsync.utils.ValidationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;

/**
 * Login activity for user authentication.
 * Handles email/password login and "remember me" functionality.
 */
public class Login extends AppCompatActivity implements UserStartApp {

    // ============================================
    // CONSTANTS
    // ============================================

    private static final String TAG = "Login";
    private static final String PREFS_NAME = "checkbox";
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";

    // ============================================
    // STATIC FIELDS
    // ============================================

    /** WeakReference to avoid memory leaks when accessing context statically */
    @Nullable
    private static WeakReference<Context> contextRef;

    // ============================================
    // UI COMPONENTS
    // ============================================

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView createAccountButton;
    private ProgressBar progressBar;
    private CheckBox rememberMeCheckbox;

    // ============================================
    // DATA & SERVICES
    // ============================================

    private FirebaseAuth firebaseAuth;
    private DBUtil dbUtil;
    private Activity currentActivity;
    private DatabaseReference databaseReferenceRoot;
    private SharedPreferences preferences;
    private String userDBKey;

    // ============================================
    // STATIC METHODS
    // ============================================

    /**
     * Returns the application context for static access.
     * Uses WeakReference to prevent memory leaks.
     *
     * @return Context or null if not available
     */
    @Nullable
    public static Context getContext() {
        return contextRef != null ? contextRef.get() : null;
    }

    // ============================================
    // INTERFACE IMPLEMENTATIONS
    // ============================================

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    // ============================================
    // AUTHENTICATION
    // ============================================

    /**
     * Attempts to log in with the provided credentials.
     *
     * @param email    user's email address
     * @param password user's password
     */
    private void login(@Nullable String email, @Nullable String password) {
        // Validate inputs
        boolean hasError = validateLoginInputs(email, password);
        if (hasError) {
            loginButton.setEnabled(true);
            return;
        }

        // Check network availability
        if (!NetworkUtil.isNetworkAvailable(this)) {
            TextUtil.showMessage(getString(R.string.network_error), Toast.LENGTH_SHORT, this);
            loginButton.setEnabled(true);
            return;
        }

        // Show progress and authenticate
        progressBar.setVisibility(View.VISIBLE);
        performFirebaseLogin(email, password);
    }

    /**
     * Validates email and password inputs.
     *
     * @return true if there are validation errors
     */
    private boolean validateLoginInputs(@Nullable String email, @Nullable String password) {
        boolean hasError = false;

        if (TextUtils.isEmpty(email)) {
            emailInput.setError(getString(R.string.email_is_required));
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.password_is_required));
            hasError = true;
        }

        return hasError;
    }

    /**
     * Performs Firebase authentication.
     */
    private void performFirebaseLogin(@NonNull String email, @NonNull String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                onLoginSuccess(email, password, task.getResult().getUser().getUid());
            } else {
                onLoginFailure(task.getException());
            }
        });
    }

    /**
     * Handles successful login.
     */
    private void onLoginSuccess(@NonNull String email, @NonNull String password, @NonNull String uid) {
        DBUtil.getInstance();
        saveLoginPreferences(email, password);
        TextUtil.showMessage(getString(R.string.logged_in_successfully), Toast.LENGTH_SHORT, currentActivity);

        userDBKey = uid;
        setUserStartApp(null);
    }

    /**
     * Handles login failure.
     */
    private void onLoginFailure(@Nullable Exception exception) {
        handleLoginError(exception);
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
    }
    
    /**
     * Handles login errors and displays appropriate messages.
     * Uses centralized FirebaseErrorHandler for consistent error handling.
     */
    private void handleLoginError(@Nullable Exception exception) {
        FirebaseErrorHandler.handleLoginError(currentActivity, exception, emailInput, passwordInput);
    }

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Store context using WeakReference for static access
        contextRef = new WeakReference<>(getApplicationContext());

        // Setup toolbar
        UiUtil.setToolbar(this, null);

        // Initialize services
        currentActivity = this;
        dbUtil = DBUtil.getInstance();
        databaseReferenceRoot = Config.DatabaseReferenceRoot;

        // Setup UI
        initializeViews();
        setupListeners();
        
        // Attempt auto-login if credentials are saved
        tryToLoginByPreferences();
    }
    
    // ============================================
    // UI SETUP
    // ============================================

    /**
     * Initializes all view references.
     */
    private void initializeViews() {
        // Input fields
        emailInput = findViewById(R.id.Email);
        passwordInput = findViewById(R.id.password);
        
        // Buttons
        loginButton = findViewById(R.id.loginBtn);
        createAccountButton = findViewById(R.id.createText);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckBox);
        
        // Progress indicator
        progressBar = findViewById(R.id.progressBar);
        
        // Services
        firebaseAuth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }
    
    /**
     * Sets up click and change listeners for UI elements.
     */
    private void setupListeners() {
        // Remember me checkbox - save preference on change
        rememberMeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_REMEMBER_ME, isChecked ? "true" : "false");
            editor.apply();
        });

        // Login button - validate and authenticate
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            v.setEnabled(false);
            login(email, password);
        });

        // Create account button - navigate to registration
        createAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
        });
    }

    // ============================================
    // USER INITIALIZATION
    // ============================================

    @Override
    public void setUserStartApp(User user) {
        final Context context = this;
        
        databaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleUserDataLoaded(context, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    /**
     * Handles successful user data load from Firebase.
     */
    private void handleUserDataLoaded(@NonNull Context context, @NonNull DataSnapshot snapshot) {
        User loadedUser = snapshot.child(Definitions.USERS).child(userDBKey).getValue(User.class);
        
        if (loadedUser == null) {
            Log.e(TAG, "Failed to load user data");
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, context);
            return;
        }

        String dbKey = loadedUser.getDbKey();
        if (dbKey != null && snapshot.child(Definitions.SHARES).hasChild(dbKey)) {
            DBUtil.showShareDialogEnterApp(context, snapshot, loadedUser);
        } else {
            dbUtil.initDB(loadedUser, currentActivity);
        }
    }

    /**
     * Handles database error during user load.
     */
    private void handleDatabaseError(@NonNull DatabaseError databaseError) {
        Log.e(TAG, "Database error: " + databaseError.getMessage());
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
    }

    // ============================================
    // PREFERENCES
    // ============================================

    /**
     * Attempts automatic login using saved preferences.
     */
    private void tryToLoginByPreferences() {
        String rememberMe = preferences.getString(PREF_REMEMBER_ME, "");
        
        if (!"true".equals(rememberMe)) {
            return;
        }

        String email = preferences.getString(PREF_EMAIL, "");
        String password = preferences.getString(PREF_PASSWORD, "");

        if (email.isEmpty() || password.isEmpty()) {
            return;
        }

        // Auto-fill and login
        rememberMeCheckbox.setChecked(true);
        emailInput.setText(email);
        passwordInput.setText(password);
        loginButton.setEnabled(false);
        login(email, password);
    }

    /**
     * Saves or clears login credentials based on remember me setting.
     */
    private void saveLoginPreferences(@Nullable String email, @Nullable String password) {
        SharedPreferences.Editor editor = preferences.edit();
        String rememberMe = preferences.getString(PREF_REMEMBER_ME, "");
        
        if ("true".equals(rememberMe) && email != null && password != null) {
            editor.putString(PREF_EMAIL, email);
            editor.putString(PREF_PASSWORD, password);
        } else {
            editor.remove(PREF_EMAIL);
            editor.remove(PREF_PASSWORD);
        }
        editor.apply();
    }

    // ============================================
    // PASSWORD RESET
    // ============================================

    /**
     * Opens the forgot password dialog.
     * Uses DialogHelper for cleaner dialog creation.
     */
    public void openForgotPassword(View view) {
        DialogHelper.showEmailInputDialog(
            this,
            getString(R.string.reset_password),
            getString(R.string.please_enter_user_email_to_reset_password),
            getString(R.string.send),
            (email, dialog) -> {
                // Check if email is already shared
                if (DBUtil.getInstance().isEmailAlreadyShared(email)) {
                    TextUtil.showMessage(getString(R.string.user_already_shared), Toast.LENGTH_SHORT, this);
                    return;
                }
                
                // Send password reset email
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            TextUtil.showMessage(
                                getString(R.string.email_sent_to_you_to_reset_password), 
                                Toast.LENGTH_LONG, 
                                this
                            );
                            dialog.dismiss();
                        } else {
                            FirebaseErrorHandler.logError(TAG, "Password reset failed", task.getException());
                            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, this);
                        }
                    });
            }
        );
    }
}

package com.brosh.finance.monthlybudgetsync.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.brosh.finance.monthlybudgetsync.utils.FirebaseErrorHandler;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.brosh.finance.monthlybudgetsync.utils.ValidationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Registration activity for creating new user accounts.
 * Handles user input validation, Firebase Authentication, and initial data setup.
 */
public class Register extends AppCompatActivity implements UserStartApp {

    // ============================================
    // CONSTANTS
    // ============================================

    private static final String TAG = "Register";
    private static final String PREFS_NAME = "checkbox";
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";

    // ============================================
    // UI COMPONENTS
    // ============================================

    private EditText fullNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText retypePasswordInput;
    private EditText phoneInput;
    private Button registerButton;
    private TextView loginButton;
    private ProgressBar progressBar;

    // ============================================
    // DATA & SERVICES
    // ============================================

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private DBUtil dbUtil;
    private Activity currentActivity;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceRoot;
    private SharedPreferences preferences;
    
    private String userDBKey;
    private String userID;

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize services
        initializeServices();

        // Setup UI
        UiUtil.setToolbar(this, null);
        initializeViews();
        setupListeners();

        // Check for existing login or auto-redirect
        checkExistingSession();
    }

    // ============================================
    // INITIALIZATION
    // ============================================

    /**
     * Initializes Firebase and other services.
     */
    private void initializeServices() {
        dbUtil = DBUtil.getInstance();
        currentActivity = this;
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        databaseReferenceRoot = Config.DatabaseReferenceRoot;
        databaseReferenceUsers = Config.DatabaseReferenceUsers;
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Ensure user is signed out for registration
        firebaseAuth.signOut();
    }

    /**
     * Initializes all view references.
     */
    private void initializeViews() {
        // Input fields
        fullNameInput = findViewById(R.id.fullName);
        emailInput = findViewById(R.id.Email);
        passwordInput = findViewById(R.id.password);
        retypePasswordInput = findViewById(R.id.reTypePassword);
        phoneInput = findViewById(R.id.phone);

        // Buttons
        registerButton = findViewById(R.id.registerBtn);
        loginButton = findViewById(R.id.createText);

        // Progress indicator
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private void setupListeners() {
        registerButton.setOnClickListener(v -> attemptRegistration());
        loginButton.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Checks if user is already logged in or should auto-redirect to login.
     */
    private void checkExistingSession() {
        // Check for saved "remember me" credentials
        tryToLoginByPreferences();

        // Check if user is already authenticated
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userDBKey = currentUser.getEmail().trim().replace(Definitions.DOT, Definitions.COMMA);
            setUserStartApp(null);
        }
    }

    // ============================================
    // INTERFACE IMPLEMENTATIONS
    // ============================================

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    // ============================================
    // REGISTRATION
    // ============================================

    /**
     * Attempts to register a new user with input validation.
     */
    private void attemptRegistration() {
        // Gather input values
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString();
        final String retypePassword = retypePasswordInput.getText().toString();
        final String fullName = fullNameInput.getText().toString().trim();
        final String phone = phoneInput.getText().toString().trim();

        // Validate all inputs
        if (!validateInputs(email, password, retypePassword)) {
            return;
        }

        // Show progress and disable button
        setLoadingState(true);

        // Register the user in Firebase
        performFirebaseRegistration(email, password, fullName, phone);
    }

    /**
     * Validates all registration input fields.
     *
     * @return true if all inputs are valid
     */
    private boolean validateInputs(String email, String password, String retypePassword) {
        // Validate email
        if (!ValidationUtil.validateNotEmpty(emailInput, getString(R.string.email_is_required))) {
            return false;
        }
        if (!ValidationUtil.validateEmail(emailInput, getString(R.string.error_invalid_email))) {
            return false;
        }

        // Validate password
        if (!ValidationUtil.validatePassword(passwordInput,
                getString(R.string.password_is_required),
                getString(R.string.password_length_must_be_at_least_6))) {
            return false;
        }

        // Validate password match
        if (!ValidationUtil.doPasswordsMatch(password, retypePassword)) {
            retypePasswordInput.setError(getString(R.string.password_are_not_matching));
            retypePasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Performs Firebase authentication registration.
     */
    private void performFirebaseRegistration(String email, String password, String fullName, String phone) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        onRegistrationSuccess(email, fullName, phone);
                    } else {
                        onRegistrationFailure(task.getException());
                    }
                });
    }

    /**
     * Handles successful registration.
     */
    private void onRegistrationSuccess(String email, String fullName, String phone) {
        TextUtil.showMessage(getString(R.string.user_created), Toast.LENGTH_SHORT, currentActivity);

        userID = firebaseAuth.getCurrentUser().getUid();

        // Save user to Firestore
        saveUserToFirestore(email, fullName, phone);

        // Create user object and initialize app
        try {
            userDBKey = userID;
            final User user = new User(userID, fullName, email, phone, userDBKey);
            setUserStartApp(user);
        } catch (Exception e) {
            FirebaseErrorHandler.logError(TAG, "Error creating user object for id: " + userID, e);
        }
    }

    /**
     * Saves user profile to Firestore.
     */
    private void saveUserToFirestore(String email, String fullName, String phone) {
        DocumentReference documentReference = firestore.collection(getString(R.string.users)).document(userID);

        final Map<String, Object> userData = new HashMap<>();
        userData.put(getString(R.string.first_name), fullName);
        userData.put(getString(R.string.email), email);
        userData.put(getString(R.string.phone), phone);

        documentReference.set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created for: " + userID))
                .addOnFailureListener(e -> FirebaseErrorHandler.logError(TAG, "Failed to create user profile", e));
    }

    /**
     * Handles registration failure using centralized error handler.
     */
    private void onRegistrationFailure(@Nullable Exception exception) {
        FirebaseErrorHandler.handleRegistrationError(currentActivity, exception, emailInput, passwordInput);
        setLoadingState(false);
    }

    // ============================================
    // USER INITIALIZATION
    // ============================================

    @Override
    public void setUserStartApp(@Nullable final User newUser) {
        final Context context = this;

        databaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleUserDataLoaded(context, snapshot, newUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Handles user data loaded from Firebase.
     */
    private void handleUserDataLoaded(@NonNull Context context, @NonNull DataSnapshot snapshot,
                                      @Nullable User newUser) {
        User user;

        if (newUser != null) {
            // New user registration
            user = newUser;
            if (snapshot.child(Definitions.USERS).hasChild(user.getDbKey())) {
                // User already exists - cannot create duplicate
                return;
            }
            initializeNewUserInDatabase(user);
        } else {
            // Existing user login
            user = snapshot.child(Definitions.USERS).child(userDBKey).getValue(User.class);
        }

        // Check for shared data and initialize
        if (user != null && snapshot.child(Definitions.SHARES).hasChild(userDBKey)) {
            DBUtil.showShareDialogEnterApp(context, snapshot, user);
        } else if (user != null) {
            dbUtil.initDB(user, currentActivity);
        }
    }

    /**
     * Creates initial database entries for a new user.
     */
    private void initializeNewUserInDatabase(@NonNull User user) {
        // Save user to Users node
        databaseReferenceUsers.child(user.getDbKey()).setValue(user);

        // Save email-to-UID mapping
        DBUtil.getInstance().getDBUserEmailUidPath().child(TextUtil.getEmailComma(user.getEmail())).setValue(user.getUid());

        // Initialize monthly budget structure
        DatabaseReference userBudgetRef = Config.DatabaseReferenceMonthlyBudget.child(user.getDbKey());
        userBudgetRef.child(Definitions.BUDGETS).setValue("");
        userBudgetRef.child(Definitions.MONTHS).setValue("");
        userBudgetRef.child(Definitions.SHOPS).setValue("");
    }

    // ============================================
    // NAVIGATION & PREFERENCES
    // ============================================

    /**
     * Navigates to the Login activity.
     */
    private void navigateToLogin() {
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    /**
     * Checks if user has "remember me" enabled and redirects to login if so.
     */
    private void tryToLoginByPreferences() {
        String rememberMe = preferences.getString(PREF_REMEMBER_ME, "");

        if (!"true".equals(rememberMe)) {
            return;
        }

        String email = preferences.getString(PREF_EMAIL, "");
        String password = preferences.getString(PREF_PASSWORD, "");

        if (!ValidationUtil.isEmpty(email) && !ValidationUtil.isEmpty(password)) {
            navigateToLogin();
        }
    }

    // ============================================
    // UI HELPERS
    // ============================================

    /**
     * Sets the loading state of the UI.
     *
     * @param isLoading true to show loading, false to hide
     */
    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!isLoading);
    }
}

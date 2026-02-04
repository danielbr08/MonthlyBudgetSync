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
import com.brosh.finance.monthlybudgetsync.utils.NetworkUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;

/**
 * Login activity for user authentication.
 * Handles email/password login and remember me functionality.
 */
public class Login extends AppCompatActivity implements UserStartApp {
    private static final String TAG = "Login";
    private static final String PREFS_NAME = "checkbox";
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";

    // Use WeakReference to avoid memory leaks
    @Nullable
    private static WeakReference<Context> contextRef;

    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mCreateBtn;
    private ProgressBar progressBar;
    FirebaseAuth fAuth;
    private DBUtil dbUtil;
    private Activity currentActivity;
    private DatabaseReference DatabaseReferenceRoot;
    private String userDBKey;

    private CheckBox rememberMeCB;
    private SharedPreferences preferences;

    /**
     * Returns the application context for static access.
     * Uses WeakReference to prevent memory leaks.
     * @return Context or null if not available
     */
    @Nullable
    public static Context getContext() {
        return contextRef != null ? contextRef.get() : null;
    }

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Attempts to log in with the provided credentials.
     * @param email user's email address
     * @param password user's password
     */
    private void login(@Nullable String email, @Nullable String password) {
        boolean hasError = false;
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.email_is_required));
            hasError = true;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.password_is_required));
            hasError = true;
        }

        if (hasError) {
            mLoginBtn.setEnabled(true);
            return;
        }
        
        // Check network availability
        if (!NetworkUtil.isNetworkAvailable(this)) {
            TextUtil.showMessage(getString(R.string.network_error), Toast.LENGTH_SHORT, this);
            mLoginBtn.setEnabled(true);
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);

        // Authenticate the user
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                // Initialize DBUtil context
                DBUtil.getInstance();
                saveLoginPreferences(email, password);
                TextUtil.showMessage(getString(R.string.logged_in_successfully), Toast.LENGTH_SHORT, currentActivity);
                
                userDBKey = task.getResult().getUser().getUid();
                setUserStartApp(null);
            } else {
                handleLoginError(task.getException());
                progressBar.setVisibility(View.GONE);
                mLoginBtn.setEnabled(true);
            }
        });
    }
    
    /**
     * Handles login errors and displays appropriate messages.
     */
    private void handleLoginError(@Nullable Exception exception) {
        if (exception == null) {
            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, currentActivity);
            return;
        }
        
        Log.e(TAG, "Login error: " + exception.getMessage(), exception);
        
        if (exception instanceof FirebaseNetworkException) {
            TextUtil.showMessage(getString(R.string.network_error), Toast.LENGTH_SHORT, currentActivity);
        } else if (exception instanceof FirebaseAuthException authException) {
            String errorCode = authException.getErrorCode();
            
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    mEmail.setError(getString(R.string.error_invalid_email));
                    mEmail.requestFocus();
                    break;
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_INVALID_CREDENTIAL":
                    mPassword.setError(getString(R.string.error_invalid_password));
                    mPassword.requestFocus();
                    break;
                case "ERROR_USER_NOT_FOUND":
                    mEmail.setError(getString(R.string.user_not_found));
                    mEmail.requestFocus();
                    break;
                case "ERROR_USER_DISABLED":
                    TextUtil.showMessage("Account has been disabled", Toast.LENGTH_LONG, currentActivity);
                    break;
                case "ERROR_TOO_MANY_REQUESTS":
                    TextUtil.showMessage("Too many attempts. Please try again later.", Toast.LENGTH_LONG, currentActivity);
                    break;
                default:
                    TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, currentActivity);
                    break;
            }
        } else {
            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, currentActivity);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Store context using WeakReference
        contextRef = new WeakReference<>(getApplicationContext());

        // Let the system handle orientation based on device capabilities
        UiUtil.setToolbar(this, null);
        
        currentActivity = this;
        dbUtil = DBUtil.getInstance();
        DatabaseReferenceRoot = Config.DatabaseReferenceRoot;
        
        initializeViews();
        setupListeners();
        tryToLoginByPreferences();
    }
    
    /**
     * Initializes all view references.
     */
    private void initializeViews() {
        rememberMeCB = findViewById(R.id.rememberMeCheckBox);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.loginBtn);
        mCreateBtn = findViewById(R.id.createText);
    }
    
    /**
     * Sets up click and change listeners for UI elements.
     */
    private void setupListeners() {
        // Remember me checkbox
        rememberMeCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_REMEMBER_ME, isChecked ? "true" : "false");
            editor.apply();
        });

        // Login button
        mLoginBtn.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            v.setEnabled(false);
            login(email, password);
        });

        // Create account button
        mCreateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
        });
    }

    public void setUserStartApp(User user) {
        final Context context = this;
        DatabaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User loadedUser = snapshot.child(Definitions.USERS).child(userDBKey).getValue(User.class);
                if (loadedUser == null) {
                    Log.e(TAG, "Failed to load user data");
                    progressBar.setVisibility(View.GONE);
                    mLoginBtn.setEnabled(true);
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
                mLoginBtn.setEnabled(true);
            }
        });
    }

    /**
     * Attempts automatic login using saved preferences.
     */
    private void tryToLoginByPreferences() {
        String rememberMe = preferences.getString(PREF_REMEMBER_ME, "");
        if ("true".equals(rememberMe)) {
            String email = preferences.getString(PREF_EMAIL, "");
            String password = preferences.getString(PREF_PASSWORD, "");
            
            if (!email.isEmpty() && !password.isEmpty()) {
                rememberMeCB.setChecked(true);
                mEmail.setText(email);
                mPassword.setText(password);
                mLoginBtn.setEnabled(false);
                login(email, password);
            }
        }
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

    @SuppressLint("NewApi")
    public void openForgotPassword(View view) {
        final Context context = this;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.reset_password));
        final EditText emailInput = new EditText(builder.getContext());
        emailInput.setHint(getString(R.string.please_enter_user_email_to_reset_password));
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String emailText = emailInput.getText().toString();
                if (DBUtil.getInstance().isEmailAlreadyShared(emailText)) {
                    emailInput.setError(getString(R.string.user_already_shared));
                } else {
                    emailInput.setError(null);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String emailText = emailInput.getText().toString();
                if (DBUtil.getInstance().isEmailAlreadyShared(emailText)) {
                    emailInput.setError(getString(R.string.user_already_shared));
                } else {
                    emailInput.setError(null);
                }
            }
        });

        builder.setView(emailInput);
        builder.setPositiveButton(getString(R.string.send), (dialog, which) -> {
                String emailText = emailInput.getText().toString();
                if (!TextUtil.isEmailValid(emailText)) {
                    emailInput.setError(getString(R.string.invalid_email));
                } else {
                    try {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.sendPasswordResetEmail(emailText)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        TextUtil.showMessage(getString(R.string.email_sent_to_you_to_reset_password), Toast.LENGTH_LONG, context);
                                    }
                                });
                    } catch (Exception e) {
                        TextUtil.showMessage(e.getMessage(), Toast.LENGTH_LONG, context);
                    }
                }
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }
}

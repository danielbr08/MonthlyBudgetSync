package com.brosh.finance.monthlybudgetsync.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.objects.UserStartApp;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements UserStartApp {
    public static final String TAG = "Register";
    EditText mFullName, mEmail, mPassword, mPhone;
    private String userDBKey;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;
    private DBUtil dbUtil;
    private Activity currentActivity;
    private DatabaseReference DatabaseReferenceUsers;
    private DatabaseReference DatabaseReferenceRoot;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dbUtil = DBUtil.getInstance();
        currentActivity = this;

        DatabaseReferenceRoot = Config.DatabaseReferenceRoot;
        DatabaseReferenceUsers = Config.DatabaseReferenceUsers;
        UiUtil.setToolbar(this, null);

        FirebaseAuth.getInstance().signOut();//logout

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createText);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        preferences = getSharedPreferences("checkbox", MODE_PRIVATE);

        tryTologinByPreferences();

        if (fAuth.getCurrentUser() != null) {
            userDBKey = fAuth.getCurrentUser().getEmail().trim().replace(Definitions.DOT, Definitions.COMMA);
            setUserStartApp(null);
            return;
        }


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                final String fullName = mFullName.getText().toString();
                final String phone = mPhone.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError(getString(R.string.email_is_required));
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError(getString(R.string.password_is_required));
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError(getString(R.string.password_length_must_be_at_least_6));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                mLoginBtn.setEnabled(false);

                // register the user in firebase
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            TextUtil.showMessage(getString(R.string.user_created), Toast.LENGTH_SHORT, currentActivity);
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection(getString(R.string.users)).document(userID);
                            final Map<String, Object> userFS = new HashMap<>();
                            userFS.put(getString(R.string.first_name), fullName);
                            userFS.put(getString(R.string.email), email);
                            userFS.put(getString(R.string.phone), phone);
                            documentReference.set(userFS).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                    return;// todo check it
                                }
                            });
                            try {
                                userDBKey = TextUtil.getEmailComma(email);
                                final User user = new User(fullName, email, phone, userDBKey);
                                setUserStartApp(user);
                            } catch (Exception e) {
                                String s = e.getMessage();
                                Log.e(TAG, e.getMessage() + "\nuser id: " + userID);
                            }

                        } else {
                            if (task.getException() instanceof FirebaseNetworkException) {
                                TextUtil.showMessage(getString(R.string.network_error), Toast.LENGTH_SHORT, currentActivity);
                            } else if (task.getException() instanceof FirebaseAuthException) {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        mEmail.setError(getString(R.string.error_invalid_email));
                                        mEmail.requestFocus();
                                        break;
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        mEmail.setError(getString(R.string.error_email_already_in_use));
                                        mEmail.requestFocus();
                                        break;
                                    case "ERROR_WEAK_PASSWORD":
                                        mPassword.setError(getString(R.string.error_weak_password));
                                        mPassword.requestFocus();
                                        break;
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                            mRegisterBtn.setEnabled(true);
                            Log.e(TAG, task.getException().getMessage());
                        }
                    }
                });
            }
        });


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setUserStartApp(final User newUser) {
        final Context context = this;
        DatabaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user;
                boolean isNewUser = newUser != null;
//                String ownerDBKey = null;
//                if (userDBKey != null && snapshot.child(Definition.SHARES).hasChild(userDBKey)) {
//                    Share share = snapshot.child(userDBKey).getValue(Share.class);
//                    ownerDBKey = share.getOwner();
//                }
                if (newUser != null) { // New user case
                    user = newUser;
                    if (snapshot.child(Definitions.USERS).hasChild(user.getDbKey())) { // User already exists - cannot create user(duplicate)
                        return;
                    } else {
                        DatabaseReferenceUsers.child(user.getDbKey()).setValue(user);
                        Config.DatabaseReferenceMonthlyBudget.child(user.getDbKey()).child(Definitions.BUDGETS).setValue("");
                        Config.DatabaseReferenceMonthlyBudget.child(user.getDbKey()).child(Definitions.MONTHS).setValue("");
                        Config.DatabaseReferenceMonthlyBudget.child(user.getDbKey()).child(Definitions.SHOPS).setValue("");
                    }
//                    user.setOwner(ownerDBKey);
                } else { // Already registered
                    user = snapshot.child(Definitions.USERS).child(userDBKey).getValue(User.class);//
                }
                if (snapshot.child(Definitions.SHARES).hasChild(userDBKey)) {
                    DBUtil.showShareDialogEnterApp(context, snapshot, user);
                } else {
                    DBUtil.getInstance().setSharesDB(snapshot.child(Definitions.SHARES));
                    dbUtil.initDB(user, currentActivity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void tryTologinByPreferences() {
        String rememberMe = preferences.getString("rememberMe", "");
        if (rememberMe.equals("true")) {
            String email = preferences.getString("email", "");
            String password = preferences.getString("password", "");
            if (!email.equals("") && !password.equals(("")))
                startLoginActivity();
        }
    }

    private void startLoginActivity() {
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

}

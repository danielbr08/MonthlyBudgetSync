package com.brosh.finance.monthlybudgetsync.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.Share;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.objects.UserStartApp;
import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.brosh.finance.monthlybudgetsync.services.TextService;
import com.brosh.finance.monthlybudgetsync.ui.InitAppActivity;
import com.brosh.finance.monthlybudgetsync.ui.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements UserStartApp {
    public static final String TAG = "TAG";
    EditText mFullName, mEmail, mPassword, mPhone;
    private String userDBKey;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;
    private DBService dbService;
    private Activity currentActivity;
    private DatabaseReference DatabaseReferenceUsers;
    private DatabaseReference DatabaseReferenceRoot;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbService = DBService.getInstance();
        currentActivity = this;

        DatabaseReferenceRoot = Config.DatabaseReferenceRoot;
        DatabaseReferenceUsers = Config.DatabaseReferenceUsers;

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
            userDBKey = fAuth.getCurrentUser().getEmail().trim().replace(Definition.DOT, Definition.COMMA);
            setUserStartApp(null);
//            dbService.initDB(userDBKey, this);
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
                    mEmail.setError("Email is Required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required.");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // register the user in firebase

                try {
                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                TextService.showMessage(getString(R.string.user_created), Toast.LENGTH_SHORT, currentActivity);
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
                                    }
                                });
                                try {
//                                user.put("fName",fullName);
//                                user.put("email",email);
//                                user.put("phone",phone);
                                    final User user = new User(fullName, email, phone, password, userDBKey);
                                    setUserStartApp(user);
                                } catch (Exception e) {
                                    String s = e.getMessage().toString();
                                    s = s;
                                }

                            } else {
                                TextService.showMessage("Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT, currentActivity);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                } catch (Exception e) {
                    String s = e.getMessage();
                    s = s;
                }
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
        DatabaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = newUser;
                boolean isNewUser = newUser != null;
//                String ownerDBKey = null;
//                if (userDBKey != null && snapshot.child(Definition.SHARES).hasChild(userDBKey)) {
//                    Share share = snapshot.child(userDBKey).getValue(Share.class);
//                    ownerDBKey = share.getOwner();
//                }
                if (user != null) { // New user case
                    if (snapshot.child(Definition.USERS).hasChild(user.getDbKey())) { // User already exists - cannot create user(duplicate)
                        return;
                    } else {
                        DatabaseReferenceUsers.child(user.getDbKey()).setValue(user);
                        Config.DatabaseReferenceMonthlyBudget.child(user.getDbKey()).child(Definition.BUDGETS).setValue("");
                        Config.DatabaseReferenceMonthlyBudget.child(user.getDbKey()).child(Definition.MONTHS).setValue("");
                        Config.DatabaseReferenceMonthlyBudget.child(user.getDbKey()).child(Definition.SHOPS).setValue("");
                    }
//                    user.setOwner(ownerDBKey);
                } else { // Already registered
                    user = snapshot.child(Definition.USERS).child(userDBKey).getValue(User.class);//
                }
//                if (ownerDBKey != null)
//                    user.setOwner(ownerDBKey);
//                Intent intent = new Intent(getApplicationContext(), InitAppActivity.class);
//                intent.putExtra(getString(R.string.user), user);
//                intent.putExtra(getString(R.string.isNewUser), isNewUser);
//                currentActivity.startActivity(intent);
//                currentActivity.finish();
                dbService.initDB(user, currentActivity);
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

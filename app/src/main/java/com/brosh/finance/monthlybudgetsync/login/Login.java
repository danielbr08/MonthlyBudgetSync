package com.brosh.finance.monthlybudgetsync.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.UserConfig;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.objects.UserStartApp;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity implements UserStartApp {
    private static final String TAG = "Login";

    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mCreateBtn;
    private ProgressBar progressBar;
    FirebaseAuth fAuth;
    private DBUtil dbUtil;
    private Activity currentActivity;
    private DatabaseReference DatabaseReferenceRoot;
    private DatabaseReference DatabaseReferenceUsers;
    private String userDBKey;

    private CheckBox rememberMeCB;
    private SharedPreferences preferences;
    String rememberMe;

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    private void login(String email, String password) {
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

        // authenticate the user
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveLoginPreferences(email, password);
                    TextUtil.showMessage(getString(R.string.logged_in_successfully), Toast.LENGTH_SHORT, currentActivity);
                    userDBKey = email.replace(Definitions.DOT, Definitions.COMMA);
                    setUserStartApp(null);
                } else {
                    TextUtil.showMessage(getString(R.string.network_error), Toast.LENGTH_SHORT, currentActivity);
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, task.getException().getMessage());
                }

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setToolbar();
        currentActivity = this;
        dbUtil = DBUtil.getInstance();
        DatabaseReferenceRoot = Config.DatabaseReferenceRoot;
        DatabaseReferenceUsers = Config.DatabaseReferenceUsers;
        rememberMeCB = findViewById(R.id.rememberMeCheckBox);
        preferences = getSharedPreferences("checkbox", MODE_PRIVATE);

        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.loginBtn);
        mCreateBtn = findViewById(R.id.createText);

        tryTologinByPreferences();

        rememberMeCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (buttonView.isChecked()) {
                    editor.putString("rememberMe", "true");
                } else {
                    editor.putString("rememberMe", "false");
                }
                editor.apply();
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                login(email, password);
            }
        });


        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });
    }

    public void setUserStartApp(User user) {
        DatabaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.child(Definitions.USERS).child(userDBKey).getValue(User.class);
                if (user.getUserConfig() == null) { // todo remove after add config object for all users
                    user.setUserConfig(new UserConfig());
                    snapshot.child(Definitions.USERS).child(userDBKey).getRef().setValue(user);
                }
                //                String ownerDBKey = null;
//                if (snapshot.child(Definition.SHARES).hasChild(userDBKey)) {
//                    ownerDBKey = snapshot.child(userDBKey).getValue().toString();
//                    user.setOwner(ownerDBKey);
//                }
//                Intent intent = new Intent(getApplicationContext(), InitAppActivity.class);
//                intent.putExtra(getString(R.string.user), user);
//                intent.putExtra(getString(R.string.isNewUser), false);
//                currentActivity.startActivity(intent);
//                currentActivity.finish();
                dbUtil.initDB(user, currentActivity);
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
            rememberMeCB.setChecked(true);
            mEmail.setText(email);
            mPassword.setText(password);
            login(email, password);
        }
    }

    private void saveLoginPreferences(String email, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        String rememberMe = preferences.getString("rememberMe", "");
        if (rememberMe.equals("true")) {
            editor.putString("email", email);
            editor.putString("password", password);
        } else {
            editor.remove("email");
            editor.remove("password");
        }
        editor.apply();
    }

    private void setTitleText() {
        String title = getString(R.string.app_name);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);
    }

    private void setToolbar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        setTitleText();
    }
}

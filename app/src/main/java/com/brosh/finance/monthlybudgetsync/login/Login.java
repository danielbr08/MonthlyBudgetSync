package com.brosh.finance.monthlybudgetsync.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Share;
import com.brosh.finance.monthlybudgetsync.objects.ShareStatus;
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
        boolean hasError = false;
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.email_is_required));
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.password_is_required));
            hasError = true;
        }

        if (password.length() < 6) {
            mPassword.setError(getString(R.string.password_length_must_be_at_least_6));
            hasError = true;
        }
        if (hasError) {
            mLoginBtn.setEnabled(true);
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
                    mLoginBtn.setEnabled(true);
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
                v.setEnabled(false);
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
        final Context context = this;
        DatabaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.child(Definitions.USERS).child(userDBKey).getValue(User.class);
                if (user.getUserConfig() == null) { // todo remove after add config object for all users
                    user.setUserConfig(new UserConfig());
                    snapshot.child(Definitions.USERS).child(userDBKey).getRef().setValue(user);
                }
                DBUtil.getInstance().setSharesDB(snapshot.child(Definitions.SHARES));
                //                String ownerDBKey = null;
                if (snapshot.child(Definitions.SHARES).hasChild(userDBKey)) {
                    Share share = snapshot.child(Definitions.SHARES).child(userDBKey).getValue(Share.class);
                    if (share.getStatus() == ShareStatus.PENDING) {
                        String ownerDBKey = share.getDbKey();
                        User ownerUser = snapshot.child(Definitions.USERS).child(ownerDBKey).getValue(User.class);
                        String userName = ownerUser.getName();
                        String question = String.format(getString(R.string.share_budget_question), userName);
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        share.setStatus(ShareStatus.SUCCESSFULLY_SHARED);
                                        user.setDbKey(ownerDBKey);
                                        snapshot.child(Definitions.USERS).child(userDBKey).child(Definitions.dbKey).getRef().setValue(ownerDBKey);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        share.setStatus(ShareStatus.DENY);
                                        break;
                                }
                                snapshot.child(Definitions.SHARES).child(userDBKey).getRef().setValue(share);
                                dbUtil.initDB(user, currentActivity);
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(question).setPositiveButton(getString(R.string.yes), dialogClickListener)
                                .setNegativeButton(getString(R.string.no), dialogClickListener).show();

                    } else {
                        dbUtil.initDB(user, currentActivity);
                    }
                } else {
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
            rememberMeCB.setChecked(true);
            mEmail.setText(email);
            mPassword.setText(password);
            mLoginBtn.setEnabled(false);
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

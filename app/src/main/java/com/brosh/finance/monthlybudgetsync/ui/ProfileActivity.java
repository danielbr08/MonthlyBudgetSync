package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Share;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;
import java.util.Set;

import static com.brosh.finance.monthlybudgetsync.config.Definitions.*;

public class ProfileActivity extends AppCompatActivity {

    private int updateType;
    private TextView oldValueTV;
    private EditText newValueET, passwordToConfirmET;
    private Button submit;
    private ProgressBar progressBar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle extras = getIntent().getExtras();
        user = DBUtil.getInstance().getUser();
        updateType = extras.getInt(UPDATE_TYPE);

        oldValueTV = (TextView) findViewById(R.id.oldValueTV);
        newValueET = (EditText) findViewById(R.id.newValueET);
        passwordToConfirmET = (EditText) findViewById(R.id.passwordToConfirmET);
        submit = (Button) findViewById(R.id.submitUpdateProfile);
        progressBar = (ProgressBar) findViewById(R.id.progressBarUpdateProfile);
        prepareUI();
    }

    public void prepareUI() {
        switch (updateType) {
            case UPDATE_EMAIL:
                oldValueTV.setText(user.getEmail());
                newValueET.setHint(R.string.please_enter_new_email);
                passwordToConfirmET.setHint(getString(R.string.please_enter_password_for_confirmation));
                break;
            case UPDATE_PASSWORD:
                oldValueTV.setText(getString(R.string.empty));
                oldValueTV.setVisibility(View.GONE);
                newValueET.setHint(R.string.please_enter_new_password);
                passwordToConfirmET.setHint(getString(R.string.please_enter_password_for_confirmation));
                break;
            case UPDATE_PHONE_NUMBER:
                oldValueTV.setText(user.getPhone());
                newValueET.setHint(R.string.please_enter_new_phone);
                passwordToConfirmET.setHint(getString(R.string.please_enter_password_for_confirmation));
                break;
            case UPDATE_USER_NAME:
                oldValueTV.setText(user.getName());
                newValueET.setHint(R.string.please_enter_new_user_name);
                passwordToConfirmET.setHint(getString(R.string.please_enter_password_for_confirmation));
                break;
            default:
                break;
        }
    }

    public void update(View view) {
        submit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        String newValue = newValueET.getText().toString().trim();
        String passwordConfirm = passwordToConfirmET.getText().toString().trim();

        try {
            // todo need to validate new value and password
            switch (updateType) {
                case UPDATE_EMAIL:
                    changeEmail(newValue, passwordConfirm);
                    break;
                case UPDATE_PASSWORD:
                    changePassword(newValue, passwordConfirm);
                    break;
                case UPDATE_PHONE_NUMBER:
                    changePoneNumber(newValue, passwordConfirm);
                    break;
                case UPDATE_USER_NAME:
                    changeUserName(newValue, passwordConfirm);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            String message = getString(R.string.error);
            TextUtil.showMessage(message, Toast.LENGTH_LONG, getApplicationContext());
        }
    }

    private void changeEmail(String newEmail, String password) {
        Activity activity = this;
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
        String oldEmail = usr.getEmail();
        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider
                .getCredential(oldEmail, password); // Current Login Credentials \\
        // Prompt the user to re-provide their sign-in credentials
        usr.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task1) {
                                        if (task1.isSuccessful()) {
                                            String newEmailComma = TextUtil.getEmailComma(newEmail);
                                            String oldEmailComma = TextUtil.getEmailComma(oldEmail);

                                            // update email uid node
                                            String userUid = DBUtil.getUserMailUidMap().remove(oldEmailComma);
                                            DBUtil.getUserMailUidMap().put(newEmailComma, userUid);
                                            DBUtil.getInstance().getDBUserEmailUidPath().child(newEmailComma).setValue(userUid);
                                            DBUtil.getInstance().getDBUserEmailUidPath().child(oldEmailComma).removeValue();

                                            // update user node
                                            User userObject = DBUtil.getInstance().getUser();
                                            userObject.setEmail(newEmail);
                                            DBUtil.getInstance().getDBUsersPath().child(userObject.getUid()).setValue(userObject);

                                            // case user is owner
                                            // nothing to do

                                            // case user is guest
                                            Map<String, Share> shareMap = DBUtil.getSharesMap();
                                            if (shareMap.containsKey(userUid)) {
                                                Share share = shareMap.get(userUid);
                                                share.setGuestEmail(newEmail);
                                                shareMap.put(userUid, share);
                                                DBUtil.getInstance().getDBSharesPath().child(userUid).setValue(share);
                                            }

                                            String message = getString(R.string.email_successfully_changed);
                                            TextUtil.showMessage(message, Toast.LENGTH_LONG, getApplicationContext());
                                            finish();
                                        } else {
                                            submit.setEnabled(true);
                                            progressBar.setVisibility(View.GONE);
                                            if (task.getException() instanceof FirebaseNetworkException) {
                                                TextUtil.showMessage(getString(R.string.network_error), Toast.LENGTH_SHORT, getApplicationContext());
                                            } else if (task.getException() instanceof FirebaseAuthException) {
                                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                                switch (errorCode) {
                                                    case "ERROR_INVALID_EMAIL":
                                                        newValueET.setError(getString(R.string.error_invalid_email));
                                                        newValueET.requestFocus();
                                                        break;
                                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                                        newValueET.setError(getString(R.string.error_email_already_in_use));
                                                        newValueET.requestFocus();
                                                        break;
                                                }
                                            }
                                        }
                                    }
                                });
                    }
                });
    }

    private void changePassword(String newPassword, String passwordConfirm) {
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(usr.getEmail(), passwordConfirm);
        usr.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    String message = getString(R.string.password_successfully_changed);
                                    TextUtil.showMessage(message, Toast.LENGTH_LONG, getApplicationContext());
                                    finish();
                                } else {
                                    submit.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                    if (task.getException() instanceof FirebaseNetworkException) {
                                        TextUtil.showMessage(getString(R.string.network_error), Toast.LENGTH_SHORT, getApplicationContext());
                                        return;
                                    } else if (task.getException() instanceof FirebaseAuthException) {
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        switch (errorCode) {
                                            case "ERROR_WEAK_PASSWORD":
                                                newValueET.setError(getString(R.string.error_weak_password));
                                                newValueET.requestFocus();
                                                break;
                                        }
                                    }
                                }
                            });
                });
    }

    private void changePoneNumber(String phoneNumber, String password) {
        User user = DBUtil.getInstance().getUser();
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(usr.getEmail(), password);
        usr.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.setPhone(phoneNumber);
                        Config.DatabaseReferenceUsers.child(user.getUid()).setValue(user);
                        String message = getString(R.string.phone_number_successfully_changed);
                        TextUtil.showMessage(message, Toast.LENGTH_LONG, getApplicationContext());
                        finish();
                    } else {
                        submit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        String message = getString(R.string.error);
                        TextUtil.showMessage(message, Toast.LENGTH_LONG, getApplicationContext());
                    }

                });
    }

    private void changeUserName(String newUserName, String password) {
        User user = DBUtil.getInstance().getUser();
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(usr.getEmail(), password);
        usr.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.setName(newUserName);
                        Config.DatabaseReferenceUsers.child(user.getUid()).setValue(user);
                        String message = getString(R.string.user_name_successfully_changed);
                        TextUtil.showMessage(message, Toast.LENGTH_LONG, getApplicationContext());
                        finish();
                    } else {
                        submit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        String message = getString(R.string.error);
                        TextUtil.showMessage(message, Toast.LENGTH_LONG, getApplicationContext());
                    }
                });
    }

}
package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.google.firebase.database.DatabaseReference;

public class InitAppActivity extends AppCompatActivity {
    private DatabaseReference DatabaseReferenceUserMonthlyBudget;
    private DBUtil dbUtil;
    private User user;
    private boolean isNewUser;
    private RelativeLayout loadingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_app);
        dbUtil = DBUtil.getInstance();
        User user = (User) getIntent().getSerializableExtra(Definitions.USER);
        isNewUser = (boolean) getIntent().getExtras().get("isNewUser");
//        if(user.getOwner() != null){
//            showQuestionChangeDB(language.questionChangeDB);
//        }
//        else{
        loadingPanel = findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.VISIBLE);

        dbUtil.initDB(user, this);
//        }
    }

    public void changeDB() {
        user.setDbKey(user.getOwner());
    }

    public void removeOwner() {
        user.setOwner(null);
    }

    public void showQuestionChangeDB(String message) {
        Activity currentActivity = this;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        changeDB();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        removeOwner();
                        break;
                }
                dbUtil.initDB(user, currentActivity);
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }
}

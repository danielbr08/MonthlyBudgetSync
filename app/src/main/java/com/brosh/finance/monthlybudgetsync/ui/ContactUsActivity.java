package com.brosh.finance.monthlybudgetsync.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.ContactUs;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.LocaleHelper;
import com.brosh.finance.monthlybudgetsync.utils.SessionGuardUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.ValidationUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.Date;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    private EditText messageET, subjectET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        UiUtil.setToolbar(this, null);

        subjectET = findViewById(R.id.subjectET);
        messageET = findViewById(R.id.messageET);
    }

    public void saveMessage(View view) {
        if (subjectET == null || messageET == null) {
            return;
        }
        User user = DBUtil.getInstance().getUser();
        if (!SessionGuardUtil.requireUser(this, user) || ValidationUtil.isEmpty(user.getEmail())) {
            TextUtil.showMessage(getString(R.string.error), Toast.LENGTH_SHORT, this);
            return;
        }

        String subject = subjectET.getText().toString().trim();
        String message = messageET.getText().toString().trim();
        if (ValidationUtil.isEmpty(subject) || ValidationUtil.isEmpty(message)) {
            TextUtil.showMessage(getString(R.string.requiredField), Toast.LENGTH_SHORT, this);
            return;
        }

        ContactUs contactUs = new ContactUs(subject, message, true, new Date());
        String msg;
        try {
            Config.DatabaseReferenceRoot.child(Definitions.CONTACT_US)
                    .child(TextUtil.getEmailComma(user.getEmail()))
                    .child(contactUs.getCreationDate().toString())
                    .setValue(contactUs);
            msg = getString(R.string.your_message_was_sent_successfully);
        } catch (Exception e) {
            msg = getString(R.string.error);
        }
        TextUtil.showMessage(msg, Toast.LENGTH_SHORT, this);
        finish();
    }
}
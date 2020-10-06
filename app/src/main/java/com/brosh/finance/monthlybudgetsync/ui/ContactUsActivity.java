package com.brosh.finance.monthlybudgetsync.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.ContactUs;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.Date;

public class ContactUsActivity extends AppCompatActivity {

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
        String subject = subjectET.getText().toString();
        String message = messageET.getText().toString();

        ContactUs contactUs = new ContactUs(subject, message, true, new Date());
        String msg;
        try {
            Config.DatabaseReferenceRoot.child(Definitions.CONTACT_US).child(TextUtil.getEmailComma(DBUtil.getInstance().getUser().getEmail())).child(contactUs.getCreationDate().toString()).setValue(contactUs);
            msg = getString(R.string.your_message_was_sent_successfully);
        } catch (Exception e) {
            msg = getString(R.string.error);
        }
        TextUtil.showMessage(msg, Toast.LENGTH_SHORT, this);
        finish();
    }
}
package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.login.Login;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.services.DBService;
//import com.brosh.finance.monthlybudgetsync.services.NetworkService;
import com.brosh.finance.monthlybudgetsync.ui.Create_Budget_Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.auth.AuthResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView fullName,email,phone;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    DatabaseReference DatabaseReferenceUserMonthlyBudget;
    DatabaseReference DatabaseReferenceShares;
    DBService dbService;
    private Button createActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createActivityButton =  findViewById(R.id.openCreateBudget);
        final String userKey = getIntent().getExtras().getString(getString(R.string.user),getString(R.string.empty));
        createActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateBudgetActivity(userKey);
            }
        }) ;
        dbService = (DBService) getIntent().getSerializableExtra(getString(R.string.db_service));

        DatabaseReferenceUserMonthlyBudget = FirebaseDatabase.getInstance().getReference(getString(R.string.monthly_budget)).child(userKey);
        DatabaseReferenceShares = FirebaseDatabase.getInstance().getReference(getString(R.string.shares));
//        DatabaseReferenceUserMonthlyBudget.setValue("");

//        phone = findViewById(R.id.phone);
//        fullName = findViewById(R.id.fullName);
//        email    = findViewById(R.id.Email);
//
//        fAuth = FirebaseAuth.getInstance();
//        fStore = FirebaseFirestore.getInstance();
//
//        userId = fAuth.getCurrentUser().getUid();
//
//        DocumentReference documentReference = fStore.collection(getString(R.string.users)).document(userId);
//        //NetworkService networkService = new NetworkService(new Month("",new Date()),userKey);
//        //networkService.init();
//        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                try {
//                    if (e != null) {
//                        System.err.println("Listen failed: " + e);
//                        return;
//                    }
//                    phone.setText(documentSnapshot.getString( getString(R.string.phone)));
//                    fullName.setText(documentSnapshot.getString(getString(R.string.first_name)));
//                    email.setText(documentSnapshot.getString( getString(R.string.email)));
//
//                }
//                catch(Exception ex){
//                    String s = ex.getMessage().toString();
//                    s=s;
//                }
//            }
//        });
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    public void share(View view) {
        String shareWith = ((EditText)findViewById(R.id.etShare)).getText().toString().trim().replace('.',',');
        DatabaseReferenceShares.child(shareWith).setValue(email.getText().toString().trim().replace('.',','));
    }

    public void openCreateBudgetActivity(String userKey){
        Intent intent = new Intent(MainActivity.this, Create_Budget_Activity.class);
        intent.putExtra(getString(R.string.language),getString(R.string.hebrew));
//        String userKey = getIntent().getExtras().getString(getString(R.string.user),getString(R.string.empty));
        intent.putExtra(getString(R.string.user),userKey);
        intent.putExtra( getString(R.string.db_service),dbService);
        startActivity(intent);
    }
}

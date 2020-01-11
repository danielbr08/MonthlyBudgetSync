package com.brosh.finance.monthlybudgetsync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.services.NetworkService;
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

public class MainActivity extends AppCompatActivity {

    TextView fullName,email,phone;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    DatabaseReference DatabaseReferenceUserMonthlyBudget;
    DatabaseReference DatabaseReferenceShares;
    private Month month;
    private Button createActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createActivityButton =  findViewById(R.id.openCreateBudget);
        createActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateBudgetActivity();
            }
        }) ;
        downloadData();// list of month exists(year and month only)

        String userKey = getIntent().getExtras().getString(getString(R.string.user),"");

        DatabaseReferenceUserMonthlyBudget = FirebaseDatabase.getInstance().getReference("Monthly Budget").child(userKey);
        DatabaseReferenceShares = FirebaseDatabase.getInstance().getReference("Shares");
//        DatabaseReferenceUserMonthlyBudget.setValue("");

        phone = findViewById(R.id.phone);
        fullName = findViewById(R.id.fullName);
        email    = findViewById(R.id.Email);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        NetworkService networkService = new NetworkService(new Month("",new Date()),userKey);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        System.err.println("Listen failed: " + e);
                        return;
                    }
                    phone.setText(documentSnapshot.getString("phone"));
                    fullName.setText(documentSnapshot.getString("fName"));
                    email.setText(documentSnapshot.getString("email"));

                }
                catch(Exception ex){
                    String s = ex.getMessage().toString();
                    s=s;
                }
            }
        });


    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }

    public void share(View view) {
        String shareWith = ((EditText)findViewById(R.id.etShare)).getText().toString().trim().replace('.',',');
        DatabaseReferenceShares.child(shareWith).setValue(email.getText().toString().trim().replace('.',','));
    }

    public void openCreateBudgetActivity(){
        Intent intent = new Intent(MainActivity.this,Create_Budget_Activity.class);
        intent.putExtra(getString(R.string.language),getString(R.string.heb));
        String userKey = getIntent().getExtras().getString(getString(R.string.user),"");
        intent.putExtra(getString(R.string.user),userKey);
        startActivity(intent);
    }

    private void downloadData(){
//        DatabaseReference monthsDB = FirebaseDatabase.getInstance().getReference("months");
//        monthsDB.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }
}

package com.brosh.finance.monthlybudgetsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.brosh.finance.monthlybudgetsync.services.DBService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class InitAppActivity extends AppCompatActivity {
    private DatabaseReference DatabaseReferenceUserMonthlyBudget;
    private DBService dbService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_app);
        dbService = new DBService();
        String userKey = getIntent().getExtras().getString(getString(R.string.USER),"");
        initDB(userKey);
    }

    public void initDB(String userKey){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Monthly Budget").child(userKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    String dbKey =  myDataSnapshot.getKey().toString();
                    switch(dbKey){
                        case "Budget":{
                            setBudgetDB(myDataSnapshot);
                            break;
                        }
                        case "Months":{
                            setMonthsDB(myDataSnapshot);
                            break;
                        }
                    }
                }
                Intent mainActivityIntent = new Intent(getApplicationContext(),MainActivity.class);
                mainActivityIntent.putExtra("dbService",dbService);
            }
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void setBudgetDB(DataSnapshot budgetsSnapshot){
        for(DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
            String budgetNumber =  budgetSnapshot.getKey().toString();
            for(DataSnapshot mySnapshot : budgetsSnapshot.child(budgetNumber).getChildren()) {
                String budgetObjkey = mySnapshot.getKey().toString();
                Budget budgetObj = mySnapshot.getValue(Budget.class);
                dbService.updateSpecificBudget(budgetNumber,budgetObjkey,budgetObj);
                setBudgetEventUpdateValue(budgetSnapshot.getRef(),budgetNumber,budgetObjkey);
            }
        }
    }

    public void setCategoriesEventUpdateValue(DataSnapshot categoriesSnapshot, String refMonthKey){
        for(DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
            for(DataSnapshot mySnapshot : categoriesSnapshot.child(refMonthKey).getChildren()) {
                String categoryObjkey = mySnapshot.getKey().toString();
                setCategoryEventUpdateValue(categorySnapshot.getRef(),refMonthKey,categoryObjkey);
            }
        }
    }

    public void setMonthsDB(DataSnapshot monthsSnapshot){
        for(DataSnapshot monthSnapshot : monthsSnapshot.getChildren()) {
            String refMonthKey =  monthSnapshot.getKey().toString();
            Month monthObj = monthSnapshot.getValue(Month.class);
            dbService.updateSpecificMonth(refMonthKey,monthObj);
            DataSnapshot categoriesDatabaseReference = monthSnapshot.child(refMonthKey).child("Categories");
            setCategoriesEventUpdateValue(categoriesDatabaseReference,refMonthKey);
            setMonthEventUpdateValue(monthSnapshot.getRef(),refMonthKey);
        }
    }

    public void setBudgetEventUpdateValue(DatabaseReference budgetDBReference, final String refMonthKey,final String objId) {
        budgetDBReference.child(refMonthKey).child(objId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Budget budgetObj = dataSnapshot.getValue(Budget.class);
                    dbService.updateSpecificBudget(refMonthKey,objId, budgetObj);
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void setCategoryEventUpdateValue(final DatabaseReference categoryDBReference, final String refMonthKey,final String catObjId) {
        categoryDBReference.child(catObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Category cat = dataSnapshot.getValue(Category.class);
                    dbService.updateSpecificCategory(refMonthKey,catObjId, cat);
                    DataSnapshot transactionDBReference = dataSnapshot.child(catObjId).child("Transactions");
                    for(DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                            String trnObjkey = transactionSnapshot.getKey().toString();
                            setTransactionEventUpdateValue(transactionDBReference.getRef(),refMonthKey,catObjId,trnObjkey);
                        }
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setTransactionEventUpdateValue(DatabaseReference transactionDBReference, final String refMonthKey,final String catObjId, final String trnObjId) {
        transactionDBReference.child(trnObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Transaction trn = dataSnapshot.getValue(Transaction.class);
                    dbService.updateSpecificTransaction(refMonthKey,catObjId, trnObjId,trn);
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setMonthEventUpdateValue(DatabaseReference monthDBReference, final String refMonthKey) {
        monthDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Month month = dataSnapshot.getValue(Month.class);
                    dbService.updateSpecificMonth(refMonthKey, month);
                }
                catch(Exception ex){
                    String message = ex.getMessage().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

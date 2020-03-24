package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
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
        String userKey = getIntent().getExtras().getString(getString(R.string.user), "");
        initDB(userKey);
    }

    public void initDB(String userKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.monthly_budget)).child(userKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    return;
                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    String dbKey = myDataSnapshot.getKey();
                    if (dbKey.equals(getString(R.string.budget))) {
                        setBudgetDB(myDataSnapshot);
                    } else if (dbKey.equals(getString(R.string.months))) {
                        setMonthsDB(myDataSnapshot);
                    }
                }
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainActivityIntent.putExtra(getString(R.string.db_service), dbService);
                startActivity(mainActivityIntent);
            }

            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void setBudgetDB(DataSnapshot budgetsSnapshot) {
        for (DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
            String budgetNumber = budgetSnapshot.getKey();
            for (DataSnapshot mySnapshot : budgetsSnapshot.child(budgetNumber).getChildren()) {
                String budgetObjkey = mySnapshot.getKey();
                Budget budgetObj = mySnapshot.getValue(Budget.class);
                dbService.updateSpecificBudget(budgetNumber, budgetObj);
                setBudgetEventUpdateValue(budgetSnapshot.getRef(), budgetNumber, budgetObjkey);
            }
        }
    }

    public void setCategoriesEventUpdateValue(DataSnapshot categoriesSnapshot, String refMonthKey, int budgetNumber) {
        for (DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
            for (DataSnapshot mySnapshot : categoriesSnapshot.child(refMonthKey).getChildren()) {
                String categoryObjkey = mySnapshot.getKey();
                setCategoryEventUpdateValue(categorySnapshot.getRef(), refMonthKey, budgetNumber, categoryObjkey);
            }
        }
    }

    public void setMonthsDB(DataSnapshot monthsSnapshot) {
        for (DataSnapshot monthSnapshot : monthsSnapshot.getChildren()) {
            String refMonthKey = monthSnapshot.getKey();
            Month monthObj = monthSnapshot.getValue(Month.class);
            dbService.updateSpecificMonth(refMonthKey, monthObj);
            DataSnapshot categoriesDatabaseReference = monthSnapshot.child(refMonthKey).child(Definition.CATEGORIES);
            setCategoriesEventUpdateValue(categoriesDatabaseReference, refMonthKey, (int) monthObj.getBudgetNumber());
            setMonthEventUpdateValue(monthSnapshot.getRef(), refMonthKey);
        }
    }

    public void setBudgetEventUpdateValue(DatabaseReference budgetDBReference, final String refMonthKey, String objId) {
        budgetDBReference.child(refMonthKey).child(objId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Budget budgetObj = dataSnapshot.getValue(Budget.class);
                    dbService.updateSpecificBudget(refMonthKey, budgetObj);
                } catch (Exception ex) {
                    String message = ex.getMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void setCategoryEventUpdateValue(final DatabaseReference categoryDBReference, final String refMonthKey, final int budgetNumber, final String catObjId) {
        categoryDBReference.child(catObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Category cat = dataSnapshot.getValue(Category.class);
                    dbService.updateSpecificCategory(refMonthKey, budgetNumber, cat);
                    DataSnapshot transactionDBReference = dataSnapshot.child(catObjId).child(Definition.TRANSACTIONS);
                    for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                        String trnObjkey = transactionSnapshot.getKey();
                        setTransactionEventUpdateValue(transactionDBReference.getRef(), refMonthKey, catObjId, trnObjkey);
                    }
                } catch (Exception ex) {
                    String message = ex.getMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setTransactionEventUpdateValue(DatabaseReference transactionDBReference, final String refMonthKey, final String catObjId, final String trnObjId) {
        transactionDBReference.child(trnObjId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Transaction trn = dataSnapshot.getValue(Transaction.class);
                    dbService.updateSpecificTransaction(refMonthKey, catObjId, trnObjId, trn);
                } catch (Exception ex) {
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
                } catch (Exception ex) {
                    String message = ex.getMessage().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

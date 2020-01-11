package com.brosh.finance.monthlybudgetsync.services;

import androidx.annotation.NonNull;
import com.brosh.finance.monthlybudgetsync.*;
import com.brosh.finance.monthlybudgetsync.Transaction;
import com.google.firebase.database.*;

public class NetworkService {
    private DatabaseReference budgetDB;
    private DatabaseReference transactionDB;
    private DatabaseReference categoryDB;

    private DateService dateService;

    private Month month;

    public NetworkService(Month month,String userKey){
        this.month = month;
        String yearMonth = dateService.getYearMonth(month.getRefMonth(),'-');
        budgetDB = FirebaseDatabase.getInstance().getReference("Monthly Budget").child(userKey).child("Budget");
//        transactionDB = FirebaseDatabase.getInstance().getReference(yearMonth).child("transaction");
//        categoryDB = FirebaseDatabase.getInstance().getReference(yearMonth).child("category");

        Query budgetNumberQuery = budgetDB.orderByKey().limitToLast(1);
        budgetNumberQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    setBudgetDBData(myDataSnapshot.getKey());
                }
            }
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
//        setTransactionDBData();
//        setCategoryDBData();
    }

    public void setBudgetDBData(String budgetNumber) {
        DatabaseReference currentBudgetDB = budgetDB.child(budgetNumber);
        currentBudgetDB.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    String id = myDataSnapshot.child("id").getValue().toString();
                    try {
                        Budget bgt = (Budget) myDataSnapshot.getValue(Budget.class);
                        //Budget bgt = new Budget(id,budgetNumber,catPriority,category,subCategory,value,constPayment,"",chargeDay);
                        month.updateSpecificBudget(id,bgt);
                        setBudgetEventUpdateValue(myDataSnapshot.getKey());
                        //budgetHMDBData.put(myDataSnapshot.getKey(),bgt);
                    } catch (Exception ex) {
                        String err = ex.getMessage().toString();
                    }
                }
            }
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void setBudgetEventUpdateValue(final String objId) {
        budgetDB.child(objId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //String id = dataSnapshot.getKey().toString();
                try {
                    Budget bgt = dataSnapshot.getValue(Budget.class);
                    month.updateSpecificBudget(objId, bgt);
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

    public void setCategoryDBData() {
        categoryDB.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    String id = myDataSnapshot.child("id").getValue().toString();
                    try {
                        Category cat = myDataSnapshot.getValue(Category.class);
                        month.updateSpecificCategory(id,cat);
                        setCategoryEventUpdateValue(myDataSnapshot.getKey());
                    } catch (Exception ex) {
                        String err = ex.getMessage().toString();
                    }
                }
            }
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void setCategoryEventUpdateValue(final String objId) {
        categoryDB.child(objId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Category cat = dataSnapshot.getValue(Category.class);
                    month.updateSpecificCategory(objId, cat);
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

    public void setTransactionDBData(){
        transactionDB.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {

                    String objId = myDataSnapshot.child("id").getValue().toString();

                    Transaction trn = myDataSnapshot.getValue(Transaction.class);
                    month.updateSpecificTransaction(objId, trn);
                    setTransactionEventUpdateValue(myDataSnapshot.getKey());
                }
            }
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void setTransactionEventUpdateValue(final String objId) {
        transactionDB.child(objId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String id = dataSnapshot.getKey().toString();
                Transaction trn = dataSnapshot.getValue(Transaction.class);
                month.updateSpecificTransaction(objId, trn);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

/*
 * Copyright (c) Daniel Brosh.
 */

package com.brosh.finance.monthlybudgetsync.services;

import java.io.Serializable;

public class Language implements Serializable {
    String language;

    public boolean isLTR() {
        return isLTR;
    }

    public void setLTR(boolean LTR) {
        isLTR = LTR;
    }

    private boolean isLTR;

    // Application name
    public String appName = "תקציב חודשי";
    public String empty="";

    // Payment method
    public String creditCardName;
    public String cashName;
    public String checkName;
    public String bankTransferName;

    // Sort by
    public String IDName = "מזהה";
    public String categoryName = "קטגוריה";
    public String paymentMethodName = "א.תשלום";
    public String shopName = "חנות";
    public String chargeDateName = "ת.עסקה";
    public String sumName = "סכום";
    public String regisrationDateName = "ת.רישום";

    // Category
    public String subCategoryName = "ללא";

    // Transaction
    public String subCategory = "ללא";
    public String all = "הכל";

    // Main Activity
    public String monthName = "חודש";
    public String budgetButtonName = "תקציב";
    public String transactionsButtonName = "עסקאות";
    public String insertTransactionButtonName = "הכנסת עסקה";
    public String insert = "הכנס";
    public String createBudgetButtonName = "יצירת תקציב";
    public String close = "סגור";

    // Budget Activity
    public String budgetTitleName = "תקציב";
    public String budgetName = "תקציב";
    public String budgetCategoryName = "קטגוריה";
    public String transactionsName = "עסקאות";
    public String balanceName = "יתרה";
    public char quotes ='"';
    public String totalName = "סה"+ quotes + "כ";

     // Transactions Activity
     String noTransactionsExists;

    // CreateBudget Activity
    public String createBudgetQuestion = "יצירת תקציב חדש תגרום למחיקת נתוני חודש נוכחי, האם להמשיך?";
    public String createBudgetName = "בניית תקציב";
    public String createBudgetButton = "צור תקציב";
    public String createButton = "צור";
    public String duplicateCategory = "קטגוריה כפולה!";
    public String illegalCharacter = "תו לא חוקי!";
    public String pleaseInsertCategory = "נא להזין קטגוריה!";
    public String pleaseInsertValue = "נא להזין ערך!";
    public String pleaseInsertShop = "נא להזין חנות!";
    public String pleaseInsertBudget = "אנא הזן תקציב!";
    public String constantDate = "ת. קבוע";
    public String chargeDay = "יום לחיוב";
    public String yes = "כן";
    public String no = "לא";
    public String budgetCreatedSuccessfully = "תקציב נוצר בהצלחה!";

    // InsertTransaction Activity
    public String transactionPrice = "מחיר";
    public String selectingDate = "בחירת תאריך";
    public String transactionInsertedSuccessfully = "העסקה הוכנסה בהצלחה!";
    public String messageName = "הודעה";
    public String requiredField = "שדה חובה!";

    public Language(String language)
    {
        this.language = language;
        if(language.equals("HEB")) {
            isLTR = false;
            setParamsHeb();
        }
        else if(language.equals("EN")) {
            isLTR = true;
            setParamsEN();
        }
    }

    public boolean isHeb(){
        return language.equals("HEB");
    }

    public boolean isEn(){
        return language.equals("EN");
    }

    void setParamsHeb()
    {
        // Payment method
        creditCardName = "כרטיס אשראי";
        cashName ="מזומן";
        checkName = "צ'ק";
        bankTransferName = "העברה בנקאית";

        // Application name
        appName = "תקציב חודשי";

        // Sort by
        IDName = "מזהה";
        categoryName = "קטגוריה";
        paymentMethodName = "א.תשלום";
        shopName = "חנות";
        chargeDateName = "ת.עסקה";
        sumName = "סכום";
        regisrationDateName = "ת.רישום";

        // Category
        subCategoryName = "ללא";

        // Transaction
        subCategory = "ללא";
        all = "הכל";

        // Main Activity
        monthName = "";// "חודש";
        budgetButtonName = "תקציב";
        transactionsButtonName = "עסקאות";
        insertTransactionButtonName = "הכנסת עסקה";
        insert = "הכנס";
        createBudgetButtonName = "יצירת תקציב";
        close = "סגור";

        // Budget Activity
        budgetTitleName = "תקציב";
        budgetName = "תקציב";
        budgetCategoryName = "קטגוריה";
        balanceName = "יתרה";
        char quotes ='"';
        totalName = "סה"+ quotes + "כ";

        // Transactions Activity
        noTransactionsExists = "לא נמצאו עסקאות!";

        // CreateBudget Activity
        createBudgetQuestion = "יצירת תקציב חדש תגרום למחיקת נתוני חודש נוכחי, האם להמשיך?";
        createBudgetName = "בניית תקציב";
        createBudgetButton = "צור תקציב";
        duplicateCategory = "קטגוריה כפולה!";
        illegalCharacter = "תו לא חוקי!";
        pleaseInsertCategory = "נא להזין קטגוריה!";
        pleaseInsertValue = "נא להזין ערך!";
        pleaseInsertShop = "נא להזין חנות!";
        pleaseInsertBudget = "אנא הזן תקציב!";
        constantDate = "ת. קבוע";
        chargeDay = "יום לחיוב";
        yes = "כן";
        no = "לא";
        budgetCreatedSuccessfully = "תקציב נוצר בהצלחה!";

        // InsertTransaction Activity
        selectingDate = "בחירת תאריך";
        transactionInsertedSuccessfully = "העסקה הוכנסה בהצלחה!";
        messageName = "הודעה";
        requiredField = "שדה חובה!";
    }

    void setParamsEN()
    {
        // Payment method
        creditCardName = "Credit Card";
        cashName ="Cash";
        checkName = "Check";
        bankTransferName = "Bank Transfer";

        // Application name
        appName = "Monthly Budget";

        // Sort by
        IDName = "ID";
        categoryName = "category";
        paymentMethodName = "p.method";
        shopName = "shop";
        chargeDateName = "c.date";
        sumName = "sum";
        regisrationDateName = "registration date";

        // Category
        subCategoryName = "none";

        // Transaction
        subCategory = "none";
        all = "All";

        // Main Activity
        monthName = "";//"month";
        budgetButtonName = "Budget";
        transactionsButtonName = "Transactions";
        insertTransactionButtonName = "Insert Transaction";
        insert = "Insert";
        createBudgetButtonName = "Create Budget";
        close = "Close";

        // Budget Activity
        budgetTitleName = "Budget";
        budgetName = "Budget";
        transactionsName = "Transactions";
        budgetCategoryName = "Category";
        balanceName = "Balance";
        totalName = "Total";

        // Transactions Activity
        noTransactionsExists = "No transactions found!";

        // CreateBudget Activity
        createBudgetQuestion = "Creating a new budget will delete the current month's data, continue?";
        createBudgetName = "Create Budget";
        createBudgetButton = "Create Budget";
        createButton = "Create";
        duplicateCategory = "Duplicate category!";
        illegalCharacter = "Illegal character!";
        pleaseInsertCategory = "Please insert a category!";
        pleaseInsertValue = "Please insert a value!";
        pleaseInsertShop = "Please insert a shop!";
        pleaseInsertBudget = "Please insert a budget!";
        constantDate = "Auto reg";
        chargeDay = "Pay day";
        yes = "yes";
        no = "no";
        budgetCreatedSuccessfully = "Budget created successfully!";

        // InsertTransaction Activity
        transactionPrice = "Price";
        selectingDate = "Selecting Date";
        transactionInsertedSuccessfully = "Transaction inserted successfully!";
        messageName = "Message";
        requiredField = "Required field!";
    }
}

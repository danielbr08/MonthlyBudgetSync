/*
 * Copyright (c) Daniel Brosh.
 */

package com.brosh.finance.monthlybudgetsync;

public class Language {
    String language;

    // Application name
    String appName = "תקציב חודשי";

    // Payment method
    String creditCardName;
    String cashName;
    String checkName;
    String bankTransferName;

    // Sort by
    String IDName = "מזהה";
    String categoryName = "קטגוריה";
    String paymentMethodName = "א.תשלום";
    String shopName = "חנות";
    String chargeDateName = "ת.עסקה";
    String sumName = "סכום";
    String regisrationDateName = "ת.רישום";

    // Category
    String subCategoryName = "ללא";

    // Transaction
    String subCategory = "ללא";
    String all = "הכל";

    // Main Activity
    String monthName = "חודש";
    String budgetButtonName = "תקציב";
    String transactionsButtonName = "עסקאות";
    String insertTransactionButtonName = "הכנסת עסקה";
    String insert = "הכנס";
    String createBudgetButtonName = "יצירת תקציב";
    String close = "סגור";

    // Budget Activity
    String budgetTitleName = "תקציב";
    String budgetName = "תקציב";
    String budgetCategoryName = "קטגוריה";
    String transactionsName = "עסקאות";
    String balanceName = "יתרה";
    char quotes ='"';
    String totalName = "סה"+ quotes + "כ";

    // Transactions Activity
    String noTransactionsExists;

    // CreateBudget Activity
    String createBudgetQuestion = "יצירת תקציב חדש תגרום למחיקת נתוני חודש נוכחי, האם להמשיך?";
    String createBudgetName = "בניית תקציב";
    String createBudgetButton = "צור תקציב";
    String createButton = "צור";
    String duplicateCategory = "קטגוריה כפולה!";
    String illegalCharacter = "תו לא חוקי!";
    String pleaseInsertCategory = "נא להזין קטגוריה!";
    String pleaseInsertValue = "נא להזין ערך!";
    String pleaseInsertShop = "נא להזין חנות!";
    String pleaseInsertBudget = "אנא הזן תקציב!";
    String constantDate = "ת. קבוע";
    String chargeDay = "יום לחיוב";
    String yes = "כן";
    String no = "לא";
    String budgetCreatedSuccessfully = "תקציב נוצר בהצלחה!";

    // InsertTransaction Activity
    String transactionPrice = "מחיר";
    String selectingDate = "בחירת תאריך";
    String transactionInsertedSuccessfully = "העסקה הוכנסה בהצלחה!";
    String messageName = "הודעה";
    String requiredField = "שדה חובה!";

    public Language(String language)
    {
        this.language = language;
        if(language.equals("HEB"))
            setParamsHeb();
        else if(language.equals("EN"))
            setParamsEN();
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

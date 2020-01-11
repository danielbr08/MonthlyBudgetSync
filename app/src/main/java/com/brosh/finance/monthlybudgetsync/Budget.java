package com.brosh.finance.monthlybudgetsync;

public class Budget {
    private String id;
    private String categoryName;
    private int value;
    private boolean isConstPayment;
    private String shop;
    private int chargeDay;
    private int catPriority;

    public Budget() {
    }

    public Budget(String categoryName, int value, boolean isConstPayment, String shop, int chargeDay, int catPriority) {
        this.categoryName = categoryName;
        this.value = value;
        this.isConstPayment = isConstPayment;
        this.shop = shop;
        this.chargeDay = chargeDay;
        this.catPriority = catPriority;
    }

    public boolean equals(Object object2) {
        return object2 instanceof Budget
                && categoryName.equals(((Budget)object2).categoryName)
                && value ==((Budget)object2).value
                && isConstPayment ==((Budget)object2).isConstPayment
                && ((shop == ((Budget)object2).shop)//null
                || shop != null && shop.equals(((Budget)object2).shop))
                && chargeDay ==((Budget)object2).chargeDay;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getValue() {
        return value;
    }

    public boolean isConstPayment() {
        return isConstPayment;
    }

    public String getShop() {
        return shop;
    }

    public int getChargeDay() {
        return chargeDay;
    }

    public int getCatPriority() {
        return catPriority;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}



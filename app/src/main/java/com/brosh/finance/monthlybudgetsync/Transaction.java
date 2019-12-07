package com.brosh.finance.monthlybudgetsync;

import java.util.Date;

public class Transaction {
    private String id;
    private int idPerMonth;
    private String category;
    private String subCategory;
    private String paymentMethod;
    private String shop;
    private Date payDate;
    private double price;
    private Date registrationDate;
    private boolean isStorno;
    private int stornoOf;

    public Transaction(String id, int idPerMonth, String category, String subCategory, String paymentMethod, String shop, Date payDate, double price, Date registrationDate, boolean isStorno, int stornoOf) {
        this.id = id;
        this.idPerMonth = idPerMonth;
        this.category = category;
        this.subCategory = subCategory;
        this.paymentMethod = paymentMethod;
        this.shop = shop;
        this.payDate = payDate;
        this.price = price;
        this.registrationDate = registrationDate;
        this.isStorno = isStorno;
        this.stornoOf = stornoOf;
    }

    public String getId() {
        return id;
    }

    public int getIdPerMonth() {
        return idPerMonth;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getShop() {
        return shop;
    }

    public Date getPayDate() {
        return payDate;
    }

    public double getPrice() {
        return price;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public boolean getIsStorno() {
        return isStorno;
    }

    public int getStornoOf() {
        return stornoOf;
    }



    public boolean isStorno(Transaction tran) {
            return ((this.getId() != tran.getId()) &&
                    (this.getIdPerMonth() != tran.getIdPerMonth()) &&
                    this.getCategory().equals(tran.getCategory()) &&
                    (this.getPayDate().compareTo(tran.getPayDate()) == 0) &&
                    this.getPrice() == -tran.getPrice() &&
                    this.getShop().equals(tran.getShop()) &&
                    ((this.getIsStorno() == false)));
        }
}
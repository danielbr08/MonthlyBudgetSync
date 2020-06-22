package com.brosh.finance.monthlybudgetsync.objects;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.services.DateService;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private String id;
    private int idPerMonth;
    private String category;
    private String paymentMethod;
    private String shop;
    private Date payDate;
    private double price;
    private Date registrationDate;
    private boolean deleted;

    public Transaction() {
    }

    private void postConstructor() {
        this.formatDateFields();
    }

    public void formatDateFields() {
        this.registrationDate = this.registrationDate != null ? DateService.changeDateFormat(this.registrationDate, Config.DATE_FORMAT) : this.registrationDate;
        this.payDate = this.payDate != null ? DateService.changeDateFormat(this.payDate, Config.DATE_FORMAT) : this.payDate;
    }

    //    public Transaction(String id, int idPerMonth, String category, String paymentMethod, String shop, Date payDate, double price, Date registrationDate) {
//        this.id = id;
//        this.idPerMonth = idPerMonth;
//        this.category = category;
//        this.paymentMethod = paymentMethod;
//        this.shop = shop;
//        this.payDate = payDate;
//        this.price = price;
//        this.registrationDate = registrationDate;
//    }
//
//    public Transaction(int idPerMonth, String category, String paymentMethod, String shop, Date payDate, double price, Date registrationDate) {
//        this.id = id;
//        this.idPerMonth = idPerMonth;
//        this.category = category;
//        this.paymentMethod = paymentMethod;
//        this.shop = shop;
//        this.payDate = payDate;
//        this.price = price;
//        this.registrationDate = registrationDate;
//    }

    public Transaction(int idPerMonth, String category, String paymentMethod, String shop, Date payDate, double price) {
        this.idPerMonth = idPerMonth;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.shop = shop;
        this.payDate = payDate;
        this.price = price;
        this.registrationDate = DateService.getTodayDate();
        postConstructor();
    }

    public Transaction(String id, int idPerMonth, String category, String paymentMethod, String shop, Date payDate, double price) {
        this.id = id;
        this.idPerMonth = idPerMonth;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.shop = shop;
        this.payDate = payDate;
        this.price = price;
        this.registrationDate = DateService.getTodayDate();

        postConstructor();
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

    public void setId(String id) {
        this.id = id;
    }

    public void setIdPerMonth(int idPerMonth) {
        this.idPerMonth = idPerMonth;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
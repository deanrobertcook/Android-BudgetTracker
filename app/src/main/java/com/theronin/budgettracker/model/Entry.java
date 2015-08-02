package com.theronin.budgettracker.model;

public class Entry {
    public final long id;
    public final String categoryName;
    public final String dateEntered;
    public final long amount;

    public Entry(String categoryName, String dateEntered, long amount) {
        this(-1, categoryName, dateEntered, amount);
    }

    public Entry (long id, String categoryName, String dateEntered, long amount) {
        this.id = id;
        this.categoryName = categoryName;
        this.dateEntered = dateEntered;
        this.amount = amount;
    }


}

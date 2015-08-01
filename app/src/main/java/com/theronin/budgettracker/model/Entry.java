package com.theronin.budgettracker.model;

public class Entry {
    public final String categoryName;
    public final String dateEntered;
    public final long amount;

    public Entry(String categoryName,
                 String dateEntered,
                 long amount) {
        this.categoryName = categoryName;
        this.dateEntered = dateEntered;
        this.amount = amount;
    }
}

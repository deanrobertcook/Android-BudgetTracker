package com.theronin.budgettracker.model;

public class Entry {
    public final long categoryId;
    public final String dateEntered;
    public final long amount;

    public Entry(long categoryId,
                 String dateEntered,
                 long amount) {
        this.categoryId = categoryId;
        this.dateEntered = dateEntered;
        this.amount = amount;
    }
}

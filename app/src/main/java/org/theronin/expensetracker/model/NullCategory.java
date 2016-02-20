package org.theronin.expensetracker.model;

public class NullCategory extends Category {

    public static final String NAME = "$$null_category$$";

    public NullCategory() {
        super(NAME);
    }
}

package org.theronin.budgettracker.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class DateView extends View {

    private String date;

    public DateView(Context context) {
        super(context);
    }

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {

    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return date;
    }


}

package org.theronin.budgettracker.pages.reusable;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Container container;

    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePickerView, int year, int month, int day) {
        if (container != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            container.onDateSelected(new Date(calendar.getTimeInMillis()));
        }
    }

    public interface Container {
        void onDateSelected(Date date);
    }
}

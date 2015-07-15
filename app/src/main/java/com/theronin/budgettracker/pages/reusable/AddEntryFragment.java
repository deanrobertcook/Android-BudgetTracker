package com.theronin.budgettracker.pages.reusable;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetDbHelper;
import com.theronin.budgettracker.utils.DateUtils;
import com.theronin.budgettracker.utils.MoneyUtils;

import java.util.Arrays;
import java.util.List;

public class AddEntryFragment extends Fragment implements AdapterView.OnItemSelectedListener, DatePickerFragment.Container{

    private static final String TAG = AddEntryFragment.class.getName();

    private TextView currencySymbolTextView;
    private Button confirmEntryButton;
    private Spinner categorySpinner;
    private EditText dateTextView;
    private BudgetDbHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        dbHelper = new BudgetDbHelper(getActivity());
    }

    @Override
    public void onPause() {
        dbHelper = null;
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__add_entry, container, false);

        currencySymbolTextView = (TextView) rootView.findViewById(R.id.tv__currency_symbol);
        currencySymbolTextView.setText(MoneyUtils.getCurrencySymbol());

        confirmEntryButton = (Button) rootView.findViewById(R.id.btn__add_entry_confirm);
        confirmEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test(view);
            }
        });

        categorySpinner = (Spinner) rootView.findViewById(R.id.spn__entry_category);
        populateCategorySpinner();
        categorySpinner.setOnItemSelectedListener(this);

        dateTextView = (EditText) rootView.findViewById(R.id.et__entry_date);
        setDateText(System.currentTimeMillis());
        dateTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                dateEditTextClicked(view);
                return true;
            }
        });

        return rootView;
    }

    private void dateEditTextClicked(View view) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setContainer(this);
        datePickerFragment.show(getFragmentManager(), TAG);
    }

    private void test(View view) {
        Toast.makeText(getActivity(), "I'll get round to adding it...maybe", Toast.LENGTH_SHORT).show();
    }

    private void populateCategorySpinner() {
        List<String> exampleArray = Arrays.asList(getResources().getStringArray(R.array
                .arr__example_categories));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout
                .simple_spinner_item, exampleArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    public void setDateText(long utcDate) {
        String date = DateUtils.formatDate(utcDate);
        if (dateTextView != null) {
            dateTextView.setText(date);
        }
    }

    @Override
    public void onDestroyView() {
        this.currencySymbolTextView = null;
        this.confirmEntryButton = null;
        this.categorySpinner = null;

        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (view != null) {
            Log.d(TAG, ((TextView) view).getText() + " selected");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}


    @Override
    public void onDateSelected(long utcDate) {
        setDateText(utcDate);
    }
}
